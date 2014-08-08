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
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Creator;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.subscribe.google.osgi.Services;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link CalendarEventParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarEventParser {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CalendarEventParser.class);

    private static enum ResponseStatus {
        NEEDS_ACTION("needsAction", ConfirmStatus.NONE),
        ACCEPTED("accepted", ConfirmStatus.ACCEPT),
        DECLINED("declined", ConfirmStatus.DECLINE),
        TENTATIVE("tentative", ConfirmStatus.TENTATIVE);

        private final String str;
        private final ConfirmStatus confirmStatus;

        private ResponseStatus(String str, ConfirmStatus confirmStatus) {
            this.str = str;
            this.confirmStatus = confirmStatus;
        }

        static ConfirmStatus parse(final String status) {
            if (null == status) {
                return null;
            }
            for (ResponseStatus rs : ResponseStatus.values()) {
                if (rs.str.equalsIgnoreCase(status)) {
                    return rs.confirmStatus;
                }
            }
            return null;
        }

    }

    // ------------------------------------------------------------------------------------------------------------------ //

    private final ServerSession session;

    /**
     * Initializes a new {@link CalendarEventParser}.
     */
    public CalendarEventParser(final ServerSession session) {
        super();
        this.session = session;
    }

    /**
     * Parse an Event to a CalendarDataObject
     *
     * @param event The Event
     * @param calendarObject The CalendarDataObject
     * @throws OXException
     */
    public void parseCalendarEvent(final Event event, final CalendarDataObject calendarObject) throws OXException {
        calendarObject.setContext(session.getContext());
        calendarObject.setUid(event.getICalUID());

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
        if (event.getStart() != null) {
            final EventDateTime eventDateTime = event.getStart();
            final long startDate;
            if (eventDateTime.getDate() != null) {
                startDate = eventDateTime.getDate().getValue();
            } else if (eventDateTime.getDateTime() != null) {
                startDate = eventDateTime.getDateTime().getValue();
            } else {
                throw new OXException();
            }
            calendarObject.setStartDate(new Date(startDate));
            if (eventDateTime.getTimeZone() != null) {
                calendarObject.setTimezone(eventDateTime.getTimeZone());
            }
        }
        if (event.getEnd() != null) {
            final EventDateTime eventDateTime = event.getStart();
            final long endDate;
            if (eventDateTime.getDate() != null) {
                endDate = eventDateTime.getDate().getValue();
            } else if (eventDateTime.getDateTime() != null) {
                endDate = eventDateTime.getDateTime().getValue();
            } else {
                throw new OXException();
            }
            calendarObject.setEndDate(new Date(endDate));
        }
        if (event.getCreated() != null) {
            final DateTime dateTime = event.getCreated();
            calendarObject.setCreationDate(new Date(dateTime.getValue()));
        }

        if (event.getCreator() != null) {
            final Creator creator = event.getCreator();
            if (creator.getSelf() != null && creator.getSelf()) {
                calendarObject.setCreatedBy(session.getUserId());
            } else {
                // add external creator?
            }
        }

        // We only support one reminder per calendar Object, thus the first one of the event
        final Reminders reminders = event.getReminders();
        if (reminders.getOverrides() != null && reminders.getOverrides().size() > 0) {
            final EventReminder eventReminder = reminders.getOverrides().get(0);
            calendarObject.setAlarm(eventReminder.getMinutes());
        }

        // Participants and confirmations
        final List<EventAttendee> attendees = event.getAttendees();
        if (attendees != null) {
            final List<Participant> participants = new ArrayList<Participant>(attendees.size());
            final List<ConfirmableParticipant> confParts = new ArrayList<ConfirmableParticipant>(attendees.size());
            for (EventAttendee a : attendees) {
                final Participant p;
                p = new ExternalUserParticipant(a.getEmail());

                // Confirmations
                ConfirmStatus confirmStatus = ResponseStatus.parse(a.getResponseStatus());
                if (null == confirmStatus) {
                    LOGGER.warn("The provided status \"{}\" cannot be parsed to a valid {}", a.getResponseStatus(), ConfirmStatus.class.getSimpleName());
                    confirmStatus = ConfirmStatus.NONE;
                }

                String confirmMessage = a.getComment();
                ConfirmableParticipant cp = new ExternalUserParticipant(a.getEmail());
                cp.setStatus(confirmStatus);
                cp.setMessage(confirmMessage);

                if (a.getDisplayName() != null) {
                    p.setDisplayName(a.getDisplayName());
                    cp.setDisplayName(a.getDisplayName());
                }
                confParts.add(cp);

                if (a.getOrganizer() != null && a.getOrganizer()) {
                    calendarObject.setOrganizer(a.getEmail());
                }
                participants.add(p);
            }
            calendarObject.setConfirmations(confParts);
            calendarObject.setParticipants(participants);
        }

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
                    foundUser = userService.searchUser(part.getEmailAddress(), session.getContext());
                    if (foundUser == null) {
                        continue;
                    }
                    participants[pos] = new UserParticipant(foundUser.getId());

                    if (foundUser.getMail().equals(calendarObject.getOrganizer())) {
                        calendarObject.setOrganizerId(foundUser.getId());
                    }

                } catch (final OXException e) {
                    LOGGER.debug("Couldn't resolve E-Mail address to an internal user: {}", part.getEmailAddress(), e);
                }
            }
        }
        calendarObject.setParticipants(participants);
    }
}
