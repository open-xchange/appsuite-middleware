/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.extractEMailAddress;
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.objectusecount.BatchIncrementArguments;
import com.openexchange.objectusecount.BatchIncrementArguments.Builder;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AttendeeUsageTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeUsageTracker {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttendeeUsageTracker.class);

    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link AttendeeUsageTracker}.
     *
     * @param entityResolver The entity resolver
     */
    public AttendeeUsageTracker(EntityResolver entityResolver) {
        super();
        this.entityResolver = entityResolver;
    }

    public void track(CalendarEvent event) {
        Session session = event.getSession();
        List<Attendee> attendees = getAddedAttendees(event);
        if (null == attendees || attendees.isEmpty() || null == session) {
            return;
        }
        /*
         * build increment arguments for the use count service for all added attendees
         */
        boolean collectEmailAddresses = isCollectEmailAddresses(session);
        List<IncrementArguments> incrementArguments = getUseCountIncrementArguments(session, attendees, collectEmailAddresses);
        if (0 < incrementArguments.size()) {
            ObjectUseCountService useCountService = Services.getService(ObjectUseCountService.class);
            if (null == useCountService) {
                LOG.debug("{} not available, skipping use count incrementation.", ObjectUseCountService.class);
            } else {
                /*
                 * do increment each use count
                 */
                try {
                    for (IncrementArguments arguments : incrementArguments) {
                        useCountService.incrementObjectUseCount(session, arguments);
                    }
                } catch (OXException e) {
                    LOG.warn("Error incrementing object use count", e);
                }
            }
        }

        PrincipalUseCountService useCountService = Services.getService(PrincipalUseCountService.class);
        if (useCountService != null) {
            // Check for used resources, Groups are checked on json layer
            for (Attendee att : attendees) {
                if (CalendarUserType.GROUP.equals(att.getCuType()) || CalendarUserType.RESOURCE.equals(att.getCuType())) {
                    try {
                        useCountService.increment(session, att.getEntity());
                    } catch (OXException e) {
                        LOG.warn("Error incrementing principal use count", e);
                    }
                }
            }
        } else {
            LOG.debug("{} not available, skipping principal use count incrementation.", PrincipalUseCountService.class);
        }
        if (collectEmailAddresses) {
            /*
             * gather collectible addresses from external attendees & pass to contact collector (use count incrementation for already
             * existing contacts can be performed from there)
             */
            List<InternetAddress> collectibleAddresses = getCollectibleAddresses(attendees);
            if (0 < collectibleAddresses.size()) {
                ContactCollectorService contactCollectorService = Services.getService(ContactCollectorService.class);
                if (null == contactCollectorService) {
                    LOG.debug("{} not available, skipping use count incrementation.", ContactCollectorService.class);
                } else {
                    contactCollectorService.memorizeAddresses(collectibleAddresses, true, session);
                }
            }
        }
    }

    /**
     * Prepares a list of internet addresses for use with the contact collector service.
     *
     * @param attendees The attendees to get the addresses for
     * @return The list of addresses, or an empty list if no suitable attendees contained
     */
    private List<InternetAddress> getCollectibleAddresses(List<Attendee> attendees) {
        if (null == attendees || 0 == attendees.size()) {
            return Collections.emptyList();
        }
        List<InternetAddress> addresses = new ArrayList<InternetAddress>(attendees.size());
        for (Attendee attendee : filter(attendees, Boolean.FALSE, CalendarUserType.INDIVIDUAL)) {
            try {
                InternetAddress address = new InternetAddress(extractEMailAddress(attendee.getUri()));
                if (null != attendee.getCn()) {
                    address.setPersonal(attendee.getCn());
                }
                addresses.add(address);
            } catch (AddressException | UnsupportedEncodingException e) {
                LOG.warn("Error constructing internet address for attendee {}, skipping contact collection.", attendee, e);
            }
        }
        return addresses;
    }

    /**
     * Prepares a list of increment arguments for the supplied list of attendees for use with the object use count service.
     *
     * @param session The underlying calendar session
     * @param attendees The attendees to get the increment arguments for
     * @param skipExternals <code>true</code> to only consider <i>internal</i> attendees, <code>false</code>, otherwise
     * @return The increment arguments, or an empty list if no suitable attendees contained
     */
    private List<IncrementArguments> getUseCountIncrementArguments(Session session, List<Attendee> attendees, boolean skipExternals) {
        if (null == attendees || 0 == attendees.size()) {
            return Collections.emptyList();
        }
        List<IncrementArguments> argumentsList = new ArrayList<IncrementArguments>(attendees.size());
        /*
         * add arguments for all internal attendees (by global addressbook entry)
         */
        Builder batchIncrementArgumentsBuilder = new BatchIncrementArguments.Builder();
        for (Attendee attendee : filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            if (session.getUserId() != attendee.getEntity() && null == attendee.getMember()) {
                try {
                    batchIncrementArgumentsBuilder.add(entityResolver.getContactId(attendee.getEntity()), FolderObject.SYSTEM_LDAP_FOLDER_ID);
                } catch (OXException e) {
                    LOG.warn("Error retrieving internal user {} for use count increment; skipping.", I(attendee.getEntity()));
                }
            }
        }
        BatchIncrementArguments batchIncrementArguments = batchIncrementArgumentsBuilder.build();
        if (false == batchIncrementArguments.getCounts().isEmpty()) {
            argumentsList.add(batchIncrementArguments);
        }
        /*
         * add arguments for all external attendees (by e-mail address)
         */
        if (false == skipExternals) {
            for (Attendee attendee : filter(attendees, Boolean.FALSE, CalendarUserType.INDIVIDUAL)) {
                argumentsList.add(new IncrementArguments.Builder(extractEMailAddress(attendee.getUri())).build());
            }
        }
        return argumentsList;
    }

    /**
     * Gets a value indicating whether collection of e-mail addresses is enabled or not.
     *
     * @return <code>true</code> if collecting e-mail address is enabled, <code>false</code>, otherwise
     */
    private static boolean isCollectEmailAddresses(Session session) {
        try {
            return ServerSessionAdapter.valueOf(session).getUserConfiguration().isCollectEmailAddresses();
        } catch (OXException e) {
            LOG.warn("Error getting user configuration to query if collection of e-mail addresses is enabled, assuming \"false\"");
            return false;
        }
    }

    /**
     * Gets a list of newly added attendees from each "create"- and "update" result included in the supplied calendar event.
     *
     * @param event The calendar event to extract the new attendees from
     * @return The newly added attendees, or <code>null</code> if there are none
     */
    private static List<Attendee> getAddedAttendees(CalendarEvent event) {
        if (null == event || event.getCreations().isEmpty() && event.getUpdates().isEmpty()) {
            return null;
        }
        List<Attendee> attendees = new ArrayList<Attendee>();
        for (CreateResult createResult : event.getCreations()) {
            List<Attendee> newAttendees = createResult.getCreatedEvent().getAttendees();
            if (null != newAttendees) {
                attendees.addAll(newAttendees);
            }
        }
        for (UpdateResult updateResult : event.getUpdates()) {
            attendees.addAll(updateResult.getAttendeeUpdates().getAddedItems());
        }
        return attendees.isEmpty() ? null : attendees;
    }

}
