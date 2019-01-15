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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.chronos.scheduling.changes.Description.EMPTY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link AbstractMessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
abstract class AbstractMessageBuilder {

    protected final List<SchedulingMessage> messages;

    protected final ServiceLookup serviceLookup;

    protected final CalendarSession session;

    protected final CalendarUser calendarUser;

    protected final CalendarUser originator;

    protected final DescriptionService descriptionService;

    /**
     * Initializes a new {@link AbstractMessageBuilder}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param session The {@link CalendarSession}
     * @param calendarUser The {@link CalendarUser}
     * @throws OXException In case originator can't be found
     */
    public AbstractMessageBuilder(ServiceLookup serviceLookup, CalendarSession session, CalendarUser calendarUser) throws OXException {
        super();
        this.serviceLookup = serviceLookup;
        this.session = session;
        this.calendarUser = calendarUser;
        this.messages = new ArrayList<>();
        this.originator = getOriginator();
        this.descriptionService = getDescriptionService();
    }

    /**
     * 
     * Whether an iTIP transaction is already running or not
     *
     * @return <code>true</code> if an iTIP transaction is in progress, <code>false</code> otherwise
     */
    protected boolean inITipTransaction() {
        return session.get(CalendarSession.PARAMETER_SUPPRESS_ITIP, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Determines the correct originator. If someone is acting on behalf of the organizer,
     * the {@link CalendarUser#getSentBy()} field will be set accordingly
     *
     * @return The originator
     * @throws OXException If loading the acting user fails
     */
    protected CalendarUser getOriginator() throws OXException {
        if (session.getUserId() == calendarUser.getEntity()) {
            // User is acting
            return calendarUser;
        }
        // Someone is acting on behalf of the user
        User user = serviceLookup.getServiceSafe(UserService.class).getUser(session.getUserId(), session.getContextId());
        CalendarUser originator = new CalendarUser();
        originator.setCn(user.getDisplayName());
        originator.setEMail(user.getMail());
        originator.setEntity(user.getId());
        originator.setUri(CalendarUtils.getURI(user.getMail()));

        CalendarUser cu = new CalendarUser(calendarUser);
        cu.setSentBy(originator);
        return cu;
    }

    protected DescriptionService getDescriptionService() {
        DescriptionService descriptionService = serviceLookup.getOptionalService(DescriptionService.class);
        if (null == descriptionService) {
            /*
             * If no service is registered, user empty descriptions. Still construct messages.
             */
            return new EmptyDescriptionService();
        }
        return descriptionService;
    }

    /**
     * 
     * Whether the given list is empty or not
     *
     * @param list The list
     * @return <code>true</code> if the list is not <code>null</code> or empty, <code>false</code> otherwise
     */
    protected boolean isEmpty(List<?> list) {
        return null == list || list.isEmpty();
    }

    /**
     * Gets an optional comment to provide to the recipient
     *
     * @return An comment or <code>null</code>
     */
    protected String getCommentForRecipient() {
        return session.get(CalendarParameters.PARAMETER_COMMENT, String.class);
    }

    /**
     * Converts session parameter from the session into an map
     *
     * @return The map with the session parameter
     */
    protected Map<String, Object> getAdditionalsFromSession() {
        Map<String, Object> map = new HashMap<>(2);
        map.put(CalendarParameters.PARAMETER_NOTIFICATION, session.get(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.class, Boolean.TRUE));
        return map;
    }

    /**
     * 
     * {@link EmptyDescriptionService} - Always return empty descriptions
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.3
     */
    class EmptyDescriptionService implements DescriptionService {

        @Override
        public Description describeCancel(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event removedEvent) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate, boolean isExceptionCreate) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeDeclineCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event declinedEvent) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeReply(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeCreationRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, Event created) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeUpdateRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeNewException(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
            return EMPTY;
        }

        @Override
        public Description describeUpdateAfterSplit(int contextId, CalendarUser originator, CalendarUser recipient, String comment, EventUpdate eventUpdate) throws OXException {
            return EMPTY;
        }

    }

}
