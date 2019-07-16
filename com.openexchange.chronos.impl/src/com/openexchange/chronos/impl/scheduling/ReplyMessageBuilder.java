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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ReplyMessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ReplyMessageBuilder extends AbstractMessageBuilder {

    /**
     * Initializes a new {@link ReplyMessageBuilder}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param session The {@link CalendarSession}
     * @param calendarUser The {@link CalendarUser}
     * @throws OXException In case originator can't be found
     */
    public ReplyMessageBuilder(ServiceLookup serviceLookup, CalendarSession session, CalendarUser calendarUser) throws OXException {
        super(serviceLookup, session, calendarUser);
    }

    /**
     * 
     * Builds an reply message for the organizer
     *
     * @param originalsToUpdated A map containing the original events mapped to the updated event
     * @return A {@link List} of messages
     * @throws OXException
     */
    public List<SchedulingMessage> build(Map<Event, Event> originalsToUpdated) throws OXException {
        if (inITipTransaction() || null == originalsToUpdated || originalsToUpdated.isEmpty()) {
            return messages;
        }

        Organizer organizer = findOrganizer(originalsToUpdated);
        //@formatter:off
        LinkedList<Event> events = new LinkedList<>(originalsToUpdated.values());
        messages.add(new MessageBuilder()
            .setMethod(SchedulingMethod.REPLY)
            .setOriginator(originator)
            .setRecipient(organizer)
            .setResource(new DefaultCalendarObjectResource(ensureSingleAttendee(originalsToUpdated.values())))
            .setScheduleChange(schedulingChangeService.describeReply(
                session.getContextId(), 
                originator, 
                organizer,
                getCommentForRecipient(),
                events,
                getChanges(convert(originalsToUpdated), organizer, (eventUpdate, recipient) ->  descriptionService.describeOnly(eventUpdate, session.getContextId(), originator, recipient, EventField.ATTENDEES))))
            .setAdditionals(getAdditionalsFromSession())
            .build());
        //@formatter:on
        return messages;
    }

    /**
     * Ensures that only the originator is within the attendees list
     *
     * @param events The events to strip
     * @return Stripped events
     * @throws OXException If copying fails
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.3">RFC5546 section-3.2.3</a>
     */
    private List<Event> ensureSingleAttendee(Collection<Event> events) throws OXException {
        ArrayList<Event> stripped = new ArrayList<Event>(events.size());
        for (Event e : events) {
            Event copy = EventMapper.getInstance().copy(e, null, (EventField[]) null);
            /*
             * Ensure only acting user is within the attendees list
             */
            Attendee attendee = CalendarUtils.find(e.getAttendees(), originator);
            copy.setAttendees(Collections.singletonList(attendee));
            stripped.add(copy);

        }
        return stripped;
    }

    private Organizer findOrganizer(Map<Event, Event> originalToUpdated) throws OXException {
        for (Event event : originalToUpdated.keySet()) {
            if (null != event.getOrganizer()) {
                return event.getOrganizer();
            }
        }
        throw CalendarExceptionCodes.MISSING_ORGANIZER.create();
    }
}
