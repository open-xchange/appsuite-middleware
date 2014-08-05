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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.subscribe.google.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link CalendarEventParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarEventParser {

    private enum ResponseStatus {
        needsAction, accepted, declined, tentative;

        final static ConfirmStatus parse(final String status) {
            if (status.equals(ResponseStatus.needsAction.toString())) {
                return ConfirmStatus.NONE;
            } else if (status.equals(ResponseStatus.accepted.toString())) {
                return ConfirmStatus.ACCEPT;
            } else if (status.equals(ResponseStatus.declined.toString())) {
                return ConfirmStatus.DECLINE;
            } else if (status.equals(ResponseStatus.tentative.toString())) {
                return ConfirmStatus.TENTATIVE;
            } else {
                throw new IllegalArgumentException("The provided status \"" + status + "\" cannot be parsed to a valid ConfirmStatus");
            }
        }

    }

    final Logger logger = LoggerFactory.getLogger(CalendarEventParser.class);

    private Context context;

    /**
     * Initializes a new {@link CalendarEventParser}.
     */
    public CalendarEventParser(final Context context) {
        super();
        this.context = context;
    }

    /**
     * Parse an Event to a CalendarDataObject
     * 
     * @param event The Event
     * @param calendarObject The CalendarDataObject
     */
    public void parseCalendarEvent(final Event event, final CalendarDataObject calendarObject) {
        calendarObject.setContext(context);

        // Common stuff
        if (event.getSummary() != null) {
            calendarObject.setTitle(event.getSummary());
        }
        if (event.getLocation() != null) {
            calendarObject.setLocation(event.getLocation());
        }
        if (event.getDescription() != null) {
            calendarObject.setNote(event.getDescription());
        }

        // Start, end and creation time
        if (event.getOriginalStartTime() != null) {
            final EventDateTime eventDateTime = event.getOriginalStartTime();
            calendarObject.setStartDate(new Date(eventDateTime.getDate().getValue()));
            calendarObject.setTimezone(eventDateTime.getTimeZone());
        }
        if (event.getEnd() != null) {
            calendarObject.setEndDate(new Date(event.getEnd().getDate().getValue()));
        }
        if (event.getCreated() != null) {
            final DateTime dateTime = event.getCreated();
            calendarObject.setCreationDate(new Date(dateTime.getValue()));
        }

        try {
            calendarObject.setCreatedBy(fetchUserByEmail(event.getCreator().getEmail()).getId());
        } catch (OXException e) {
            logger.warn("The calendar object {} has no creator assigned to it.", calendarObject.toString());
        }

        // We only support one reminder per calendar Object, thus the first one of the event
        final Reminders reminders = event.getReminders();
        if (reminders.getOverrides() != null && reminders.getOverrides().size() > 0) {
            final EventReminder eventReminder = reminders.getOverrides().get(0);
            calendarObject.setAlarm(eventReminder.getMinutes());
        }

        // Participants and confirmations
        final List<EventAttendee> attendees = event.getAttendees();
        final List<Participant> participants = new ArrayList<Participant>(attendees.size());
        final List<ConfirmableParticipant> confParts = new ArrayList<ConfirmableParticipant>(attendees.size());
        for (EventAttendee a : attendees) {
            final Participant p;
            if (!a.getResource()) {
                p = new ExternalUserParticipant(a.getEmail());

                // Confirmations
                final ConfirmStatus confirmStatus = ResponseStatus.parse(a.getResponseStatus());
                final String confirmMessage = a.getComment();
                final ConfirmableParticipant cp = new ExternalUserParticipant(a.getEmail());
                cp.setStatus(confirmStatus);
                cp.setMessage(confirmMessage);

                if (a.getDisplayName() != null) {
                    p.setDisplayName(a.getDisplayName());
                    cp.setDisplayName(a.getDisplayName());
                }
                confParts.add(cp);

                if (a.getOrganizer()) {
                    calendarObject.setOrganizer(a.getEmail());
                }
                participants.add(p);
            }
        }
        calendarObject.setConfirmations(confParts);
        calendarObject.setParticipants(participants);
        convertExternalToInternal(calendarObject);
    }

    /**
     * Convert the external participants to internal users if possible.
     * 
     * @param calendarObject The calendar object that contains the participant list
     */
    private void convertExternalToInternal(final CalendarDataObject calendarObject) {
        final Participant[] participants = calendarObject.getParticipants();
        if (participants == null || participants.length == 0) {
            return;
        }

        final UserService userService = Services.getService(UserService.class);
        for (int pos = 0; pos < participants.length; pos++) {
            final Participant part = participants[pos];
            if (part.getType() == Participant.EXTERNAL_USER) {
                User foundUser;
                try {
                    foundUser = userService.searchUser(part.getEmailAddress(), context);
                    if (foundUser == null) {
                        continue;
                    }
                    participants[pos] = new UserParticipant(foundUser.getId());

                    if (foundUser.getMail().equals(calendarObject.getOrganizer())) {
                        calendarObject.setOrganizerId(foundUser.getId());
                    }

                } catch (final OXException e) {
                    logger.debug("Couldn't resolve E-Mail address to an internal user: {}", part.getEmailAddress(), e);
                }
            }
        }
        calendarObject.setParticipants(participants);
    }

    private User fetchUserByEmail(final String email) throws OXException {
        final UserService userService = Services.getService(UserService.class);
        return userService.searchUser(email, context);
    }
}
