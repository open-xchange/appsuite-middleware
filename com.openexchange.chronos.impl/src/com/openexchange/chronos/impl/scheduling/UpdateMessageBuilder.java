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
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.changes.Description;
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
     * 
     * Build update messages for each given attendee
     *
     * @param deleted The deleted event
     * @param attendees The attendees to build a message for
     * @return A {@link List} of messages
     * @throws OXException
     */
    public List<SchedulingMessage> build(Event original, Event updated) throws OXException {
        return build(original, updated, (attendee, eventUpdate) -> descriptionService.describeUpdateRequest(session.getContextId(), originator, attendee, getCommentForRecipient(), eventUpdate));
    }

    /**
     * 
     * Build update messages for each given attendee
     *
     * @param deleted The deleted event
     * @param attendees The attendees to build a message for
     * @return A {@link List} of messages
     * @throws OXException
     */
    public List<SchedulingMessage> buildForNewException(Event original, Event changeException) throws OXException {
        return build(original, changeException, (attendee, eventUpdate) -> descriptionService.describeNewException(session.getContextId(), originator, attendee, getCommentForRecipient(), eventUpdate));
    }

    /**
     * 
     * Build update messages after an series split
     *
     * @param masterEvent The master event
     * @param updates The event that were updated
     * @return A {@link List} of messages
     * @throws OXException
     */
    public List<SchedulingMessage> buildAfterSplit(Event masterEvent, List<UpdateResult> updates) throws OXException {
        List<Attendee> attendees = masterEvent.getAttendees();
        for (UpdateResult update : updates) {
            Event event = update.getOriginal();
            if (CalendarUtils.isSeriesMaster(event)) {
                /*
                 * Send complete update to attendees
                 */
                build(update, event.getAttendees(), (a, eventUpdate) -> descriptionService.describeUpdateAfterSplit(session.getContextId(), calendarUser, a, getCommentForRecipient(), update));
            } else {
                /*
                 * Inform additional attendees as needed
                 */
                for (Attendee attendee : event.getAttendees()) {
                    if (false == CalendarUtils.contains(attendees, attendee)) {
                        build(update, Collections.singletonList(attendee), (a, eventUpdate) -> descriptionService.describeUpdateAfterSplit(session.getContextId(), calendarUser, a, getCommentForRecipient(), update));
                    }
                }
            }
        }
        return messages;
    }

    /**
     * 
     * Build update messages for each given attendee
     *
     * @param original The original event
     * @param updated The updated event
     * @param f Callback to get description
     * @return A {@link List} of messages
     * @throws OXException
     */
    private List<SchedulingMessage> build(Event original, Event updated, DescWrapper f) throws OXException {
        EventUpdate eventUpdate = DefaultEventUpdate.builder().considerUnset(true).originalEvent(original).updatedEvent(updated).build();
        return build(eventUpdate, eventUpdate.getUpdate().getAttendees(), f);
    }

    /**
     * 
     * Build update messages for each given attendee
     *
     * @param eventUpdate The event update to propagate
     * @param attendees The attendees to build a message for
     * @param f Callback to get description
     * @return A {@link List} of messages
     * @throws OXException
     */
    private List<SchedulingMessage> build(EventUpdate eventUpdate, List<Attendee> toNotify, DescWrapper f) throws OXException {
        if (inITipTransaction()) {
            return messages;
        }

        /*
         * Send invitations to added attendees, send cancel mails to removed attendees
         */
        List<Attendee> attendees = new ArrayList<>(toNotify);
        if (null != eventUpdate.getAttendeeUpdates() && false == eventUpdate.getAttendeeUpdates().isEmpty()) {
            CollectionUpdate<Attendee, AttendeeField> attendeeUpdate = eventUpdate.getAttendeeUpdates();
            // Avoid sending an update to added attendees
            if (null != attendeeUpdate.getAddedItems() && false == isEmpty(attendeeUpdate.getAddedItems())) {
                // Avoid sending an update to added attendees
                messages.addAll(new CreateMessageBuilder(serviceLookup, session, calendarUser).build(eventUpdate.getUpdate(), attendeeUpdate.getAddedItems()));
                attendees.removeAll(attendeeUpdate.getAddedItems());
            }
            messages.addAll(new CancelMessageBuilder(serviceLookup, session, calendarUser).build(eventUpdate.getUpdate(), eventUpdate.getAttendeeUpdates().getRemovedItems()));
        }
        /*
         * Send update notification to existing attendees
         */
        for (Attendee attendee : attendees) {
            //@formatter:off
            messages.add(new MessageBuilder()
                .setMethod(SchedulingMethod.REQUEST)
                .setOriginator(originator)
                .setRecipient(attendee)
                .setResource(new DefaultCalendarObjectResource(eventUpdate.getUpdate()))
                .setDescription(f.getDescription(attendee, eventUpdate))
                .setAttachmentDataProvider(new AttachmentDataProvider(serviceLookup, session.getContextId()))
                .setAdditionals(getAdditionalsFromSession())
                .build());
            //@formatter:on
        }
        return messages;
    }

    @FunctionalInterface
    interface DescWrapper {

        Description getDescription(Attendee attendee, EventUpdate eventUpdate) throws OXException;
    }

}
