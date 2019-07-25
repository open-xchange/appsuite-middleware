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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.changes.SchedulingChangeService;
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

    protected final ServiceLookup serviceLookup;
    protected final SchedulingChangeService schedulingChangeService;
    protected final DescriptionService descriptionService;

    protected final List<SchedulingMessage> messages;

    protected final CalendarSession session;
    protected final CalendarUser calendarUser;
    protected final CalendarUser originator;

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
        this.schedulingChangeService = getSchedulingChangeService();
        this.descriptionService = getDescriptionService();
    }

    /**
     * 
     * Whether an iTIP transaction is already running or not
     *
     * @return <code>true</code> if an iTIP transaction is in progress, <code>false</code> otherwise
     */
    protected boolean inITipTransaction() {
        return SchedulingControl.NONE.equals(session.get(CalendarSession.PARAMETER_SCHEDULING, SchedulingControl.class));
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
        map.put(CalendarParameters.PARAMETER_SCHEDULING, session.get(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.class));
        return map;
    }

    /**
     * Build an event update from the given event pair
     *
     * @param entry The event pair
     * @return An event update
     */
    protected EventUpdate buildEventUpdate(Entry<Event, Event> entry) {
        return DefaultEventUpdate.builder().considerUnset(true).originalEvent(entry.getKey()).updatedEvent(entry.getValue()).build();
    }

    /**
     * Converts a map of original and updated events to a list of event updates
     *
     * @param originalsToUpdated The events to convert
     * @return A list of event updates
     */
    protected List<EventUpdate> convert(Map<Event, Event> originalsToUpdated) {
        List<EventUpdate> updates = new LinkedList<EventUpdate>();
        for (Entry<Event, Event> entry : originalsToUpdated.entrySet()) {
            updates.add(buildEventUpdate(entry));
        }
        return updates;
    }

    /**
     * Get the changes to describe
     *
     * @param originalsToUpdated original events mapped to updated
     * @param recipient The recipient
     * @param callback A {@link DescriptionServiceCallback}
     * @return A list of changes
     */
    protected List<Change> getChanges(List<? extends EventUpdate> eventUpdates, CalendarUser recipient, DescriptionServiceCallback callback) {
        List<Change> changes = new LinkedList<Change>();
        for (EventUpdate eventUpdate : eventUpdates) {
            changes.add(getChange(eventUpdate, recipient, callback));
        }
        return changes;
    }

    /**
     * Describes the change for the given event update.
     * <p>
     * Does only describe the change on the replying originators participant status
     *
     * @param eventUpdate The event update
     * @param recipient The recipient
     * @return A change containing the description
     */
    private Change getChange(EventUpdate eventUpdate, CalendarUser recipient, DescriptionServiceCallback callback) {
        //@formatter:off
        return new ChangeBuilder()
            .setRecurrenceId(eventUpdate.getUpdate().getRecurrenceId())
            .setDescriptions(callback.getDescriptions(eventUpdate, recipient))
            .build();
        //@formatter:on
    }

    @FunctionalInterface
    interface DescriptionServiceCallback {

        List<Description> getDescriptions(EventUpdate eventUpdate, CalendarUser recipient);
    }

    private DescriptionService getDescriptionService() {
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
     * {@link EmptyDescriptionService} - Always return empty descriptions
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.3
     */
    class EmptyDescriptionService implements DescriptionService {

        @Override
        public List<Description> describe(EventUpdate eventUpdate, int contextId, CalendarUser originator, CalendarUser recipient, EventField... ignorees) {
            return Collections.emptyList();
        }

        @Override
        public List<Description> describeOnly(EventUpdate eventUpdate, int contextId, CalendarUser originator, CalendarUser recipient, EventField... toDescribe) {
            return Collections.emptyList();
        }

        @Override
        public List<Description> describe(EventUpdate eventUpdate, TimeZone timeZone, Locale locale, EventField... ignorees) {
            return Collections.emptyList();
        }

        @Override
        public List<Description> describeOnly(EventUpdate eventUpdate, TimeZone timeZone, Locale locale, EventField... toDescribe) {
            return Collections.emptyList();
        }

    }

    private SchedulingChangeService getSchedulingChangeService() {
        SchedulingChangeService schedulingChangeService = serviceLookup.getOptionalService(SchedulingChangeService.class);
        if (null == schedulingChangeService) {
            /*
             * If no service is registered, user empty changes. Still construct messages.
             */
            return new EmptyChangeService();
        }
        return schedulingChangeService;
    }

    /**
     * 
     * {@link EmptyChangeService} - Always return empty changes
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.3
     */
    class EmptyChangeService implements SchedulingChangeService {

        private final ScheduleChange EMPTY = new ScheduleChange() {

            @Override
            public DateTime getTimeStamp() {
                return new DateTime(0);
            }

            @Override
            public ChangeAction getAction() {
                return ChangeAction.NONE;
            }

            @Override
            public List<Change> getChanges() {
                return Collections.emptyList();
            }

            @Override
            public String getText() {
                return "".intern();
            }

            @Override
            public String getHtml() {
                return "".intern();
            }

        };

        @Override
        public ScheduleChange describeCancel(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> removedEvents) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> countered, List<Change> changes, boolean isExceptionCreate) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeDeclineCounter(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> declinedEvent) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeReply(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> change) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeCreationRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> created) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeUpdateRequest(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException {
            return EMPTY;
        }

        @Override
        public ScheduleChange describeNewException(int contextId, CalendarUser originator, CalendarUser recipient, String comment, List<Event> updated, List<Change> changes) throws OXException {
            return EMPTY;
        }

    }

}
