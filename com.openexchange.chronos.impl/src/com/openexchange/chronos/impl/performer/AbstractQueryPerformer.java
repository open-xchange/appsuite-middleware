/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.isAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AbstractQueryPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractQueryPerformer {

    protected final CalendarSession session;
    protected final CalendarStorage storage;

    private SelfProtection selfProtection;

    /**
     * Initializes a new {@link AbstractQueryPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    protected AbstractQueryPerformer(CalendarSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
    }

    protected SelfProtection getSelfProtection() {
        if(selfProtection==null){
            LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
            selfProtection = SelfProtectionFactory.createSelfProtection(leanConfigurationService);
        }
        return selfProtection;
    }

    /**
     * Gets a value indicating whether a specific event is visible for the current session's user, independently of the underlying folder
     * permissions, i.e. only considering if the user participates in the event in any form (organizer, attendees, creator, calendar user).
     *
     * @param event The event to check
     * @return <code>true</code> if the event can be read by the current session's user, <code>false</code>, otherwise
     */
    protected boolean hasReadPermission(Event event) {
        int userId = session.getUserId();
        return matches(event.getCalendarUser(), userId) || matches(event.getCreatedBy(), userId) || isAttendee(event, userId) || isOrganizer(event, userId);
    }

    protected List<Event> readEventsInFolder(CalendarFolder folder, String[] objectIDs, boolean tombstones, Long updatedSince) throws OXException {
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getFolderIdTerm(session, folder));
        if (null != objectIDs) {
            if (0 == objectIDs.length) {
                return Collections.emptyList();
            } else if (1 == objectIDs.length) {
                searchTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectIDs[0]));
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String objectID : objectIDs) {
                    orTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectID));
                }
                searchTerm.addSearchTerm(orTerm);
            }
        }
        if (null != updatedSince) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, updatedSince));
        }
        /*
         * perform search & userize the results
         */
        EventField[] fields = getFields(session);
        if (tombstones) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, new SearchOptions(session), fields);
            return storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
        } else {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            return storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
        }
    }

    /**
     * Get the configured quota and the actual usage of the underlying calendar account.
     *
     * @return The quota
     */
    protected Quota getQuota() throws OXException {
        return Utils.getQuota(session, storage);
    }

    /**
     * Sorts a list of events if requested, based on the search options set in the underlying calendar parameters.
     *
     * @param events The events to sort
     * @return The sorted events
     */
    protected List<Event> sortEvents(List<Event> events) throws OXException {
        return CalendarUtils.sortEvents(events, new SearchOptions(session).getSortOrders(), Utils.getTimeZone(session));
    }

    /**
     * Initializes a new event post processor for this performed.
     * 
     * @return The event post processor
     */
    protected EventPostProcessor postProcessor() throws OXException {
        return new EventPostProcessor(session, storage, getSelfProtection());
    }

}
