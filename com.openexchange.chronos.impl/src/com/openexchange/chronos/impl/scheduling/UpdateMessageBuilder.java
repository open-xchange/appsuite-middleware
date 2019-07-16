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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link UpdateMessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class UpdateMessageBuilder extends AbstractMessageBuilder {

    /**
     * Initializes a new {@link UpdateMessageBuilder}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param session The {@link CalendarSession}
     * @param calendarUser The {@link CalendarUser}
     * @throws OXException In case originator can't be found
     */
    public UpdateMessageBuilder(ServiceLookup serviceLookup, CalendarSession session, CalendarUser calendarUser) throws OXException {
        super(serviceLookup, session, calendarUser);
    }

    /**
     * Builds scheduling messages for the given updated events
     *
     * @param originalsToUpdated Original events mapped to the updated events
     * @return Scheduling messages
     * @throws OXException In case of error
     */
    public List<SchedulingMessage> build(Map<Event, Event> originalsToUpdated) throws OXException {
        DescriptionServiceCallback callback = (e, r) -> descriptionService.describe(e, session.getContextId(), originator, r, (EventField[]) null);
        List<EventUpdate> eventUpdates = convert(originalsToUpdated);
        return build(eventUpdates, (recipient, events) -> {
            return schedulingChangeService.describeUpdateRequest(session.getContextId(), originator, recipient, getCommentForRecipient(), events, getChanges(eventUpdates, recipient, callback));
        });
    }

    /**
     * Builds scheduling messages for the given new change exceptions
     * 
     * @param originalsToNewExceptions Original events mapped to the new change exceptions
     * @return Scheduling messages
     * @throws OXException In case of error
     */
    public List<SchedulingMessage> buildForNewExceptions(Map<Event, Event> originalsToNewExceptions) throws OXException {
        DescriptionServiceCallback callback = (e, r) -> descriptionService.describe(e, session.getContextId(), originator, r, (EventField[]) null);
        List<EventUpdate> eventUpdates = convert(originalsToNewExceptions);
        return build(eventUpdates, (recipient, events) -> {
            return schedulingChangeService.describeNewException(session.getContextId(), originator, recipient, getCommentForRecipient(), events, getChanges(eventUpdates, recipient, callback));
        });
    }

    /**
     * Build update messages after an series split
     *
     * @param eventUpdates The event updates
     * @return Scheduling messages
     * @throws OXException In case of error
     */
    public List<SchedulingMessage> buildAfterSplit(List<UpdateResult> eventUpdates) throws OXException {
        /*
         * Participant status will be reset by a split. Therefore suppress attendee description
         */
        DescriptionServiceCallback callback = (e, r) -> descriptionService.describe(e, session.getContextId(), originator, r, EventField.ATTENDEES);
        return build(eventUpdates, (recipient, events) -> {
            return schedulingChangeService.describeUpdateRequest(session.getContextId(), originator, recipient, getCommentForRecipient(), events, getChanges(eventUpdates, recipient, callback));
        });
    }

    /**
     * Builds the schedule messages
     *
     * @param eventUpdates The event updates to generate messages for
     * @param callback The callback to generate changes for a specific recipient
     * @return The generated messages
     * @throws OXException In case of error
     */
    private List<SchedulingMessage> build(List<? extends EventUpdate> eventUpdates, ScheduleChangeCallback callback) throws OXException {
        if (inITipTransaction() || isEmpty(eventUpdates)) {
            return messages;
        }

        /*
         * Search for master update and gather updates to send
         */
        EventUpdate masterUpdate = getMasterUpdate(eventUpdates);
        List<Event> updates = new LinkedList<Event>();
        for (EventUpdate eventUpdate : eventUpdates) {
            updates.add(eventUpdate.getUpdate());
        }
        updates = CalendarUtils.sortSeriesMasterFirst(updates);

        /*
         * Send invitations to added attendees, send cancel mails to removed attendees
         */
        List<Attendee> attendees = new ArrayList<>(masterUpdate.getUpdate().getAttendees());
        if (null != masterUpdate.getAttendeeUpdates() && false == masterUpdate.getAttendeeUpdates().isEmpty()) {
            CollectionUpdate<Attendee, AttendeeField> attendeeUpdate = masterUpdate.getAttendeeUpdates();
            if (null != attendeeUpdate.getAddedItems() && false == isEmpty(attendeeUpdate.getAddedItems())) {
                messages.addAll(new CreateMessageBuilder(serviceLookup, session, calendarUser).build(updates, attendeeUpdate.getAddedItems()));
                // Avoid sending an update to added attendees
                attendees.removeAll(attendeeUpdate.getAddedItems());
            }
            messages.addAll(new CancelMessageBuilder(serviceLookup, session, calendarUser).build(masterUpdate.getUpdate(), attendeeUpdate.getRemovedItems()));
        }

        /*
         * Send update notification to existing attendees
         */
        for (Attendee attendee : attendees) {
            messages.add(build(updates, callback, attendee));
        }

        /*
         * Handle exceptions with additional attendees
         */
        attendees = new ArrayList<>(masterUpdate.getUpdate().getAttendees());
        for (Event update : updates) {
            if (false == isEmpty(update.getAttendees())) {
                for (Attendee attendee : update.getAttendees()) {
                    if (false == CalendarUtils.contains(attendees, attendee)) {
                        build(Collections.singletonList(update), callback, attendee);
                    }
                }
            }
        }

        return messages;
    }

    private SchedulingMessage build(List<Event> updates, ScheduleChangeCallback callback, Attendee attendee) throws OXException {
        //@formatter:off
        return new MessageBuilder()
            .setMethod(SchedulingMethod.REQUEST)
            .setOriginator(originator)
            .setRecipient(attendee)
            .setResource(new DefaultCalendarObjectResource(updates))
            .setScheduleChange(callback.getChange(attendee, updates))
            .setAttachmentDataProvider(new AttachmentDataProvider(serviceLookup, session.getContextId()))
            .setAdditionals(getAdditionalsFromSession())
            .build();
        //@formatter:on
    }

    private EventUpdate getMasterUpdate(List<? extends EventUpdate> eventUpdates) {
        /*
         * Look up if the master was updated
         */
        for (EventUpdate eventUpdate : eventUpdates) {
            if (CalendarUtils.isSeriesMaster(eventUpdate.getUpdate())) {
                return eventUpdate;
            }
        }
        /*
         * Order is irrelevant ..
         */
        return eventUpdates.get(0);
    }

    /**
     * 
     * {@link ScheduleChangeCallback} - Callback to generate scheduling changes
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.3
     */
    @FunctionalInterface
    interface ScheduleChangeCallback {

        /**
         * Generates a schedule change for the given recipient
         *
         * @param recipient The recipient
         * @param events The events to describe in the change
         * @return A schedule change
         * @throws OXException In case of error
         */
        ScheduleChange getChange(CalendarUser recipient, List<Event> events) throws OXException;
    }

}
