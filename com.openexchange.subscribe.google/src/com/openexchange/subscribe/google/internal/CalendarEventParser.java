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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Creator;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.ical.values.Frequency;
import com.google.ical.values.RRule;
import com.google.ical.values.WeekdayNum;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.subscribe.SubscriptionErrorMessage;
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
            final EventDateTime eventDateTime = event.getEnd();
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
            Creator creator = event.getCreator();
            Boolean isSelf = creator.getSelf();
            if (isSelf != null && isSelf.booleanValue()) {
                calendarObject.setCreatedBy(session.getUserId());
            } else {
                // add external creator?
            }
        }

        // We only support one reminder per calendar Object, thus the first one of the event
        final Reminders reminders = event.getReminders();
        if (reminders.getOverrides() != null && reminders.getOverrides().size() > 0) {
            final EventReminder eventReminder = reminders.getOverrides().get(0);
            calendarObject.setAlarm(eventReminder.getMinutes().intValue());
        }

        // Set recurrences
        final List<String> recurrence = event.getRecurrence();
        if (recurrence != null && recurrence.size() > 0) {
            // Recurrence string is the first element
            handleRecurrence(recurrence.get(0), calendarObject);
        } else if (event.getRecurringEventId() != null) { // Series exception
            //calendarObject.setE

        }

        // Participants and confirmations
        final List<EventAttendee> attendees = event.getAttendees();
        if (attendees != null) {
            final List<Participant> participants = new ArrayList<Participant>(attendees.size());
            final List<ConfirmableParticipant> confParts = new ArrayList<ConfirmableParticipant>(attendees.size());
            for (EventAttendee a : attendees) {
                String emailAddress = a.getEmail();
                Participant p = new ExternalUserParticipant(emailAddress);

                // Confirmations
                String status = a.getResponseStatus();
                ConfirmStatus confirmStatus = ResponseStatus.parse(status);
                if (null == confirmStatus) {
                    LOGGER.warn("The provided status \"{}\" cannot be parsed to a valid {}", status, ConfirmStatus.class.getSimpleName());
                    confirmStatus = ConfirmStatus.NONE;
                }

                String confirmMessage = a.getComment();
                ConfirmableParticipant cp = new ExternalUserParticipant(emailAddress);
                cp.setStatus(confirmStatus);
                cp.setMessage(confirmMessage);

                String displayName = a.getDisplayName();
                if (displayName != null) {
                    p.setDisplayName(displayName);
                    cp.setDisplayName(displayName);
                }
                confParts.add(cp);

                Boolean isOrganizer = a.getOrganizer();
                if (isOrganizer != null && isOrganizer.booleanValue()) {
                    calendarObject.setOrganizer(emailAddress);
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

    private void handleRecurrence(final String recurrence, final CalendarDataObject calendarObject) throws OXException {
        try {
            final RRule r = new RRule(recurrence);

            // Set recurrence type
            // No support for secondly, minutely and hourly recurrences, set as dailies ?
            {
                final Frequency f = r.getFreq();
                if (f.ordinal() > 2) {
                    calendarObject.setRecurrenceType(f.ordinal() - 2);
                }
            }

            // Set the interval
            calendarObject.setInterval((r.getInterval() > 0) ? r.getInterval() : 1);

            // Meet preconditions for the relevant recurrence type
            if (calendarObject.getRecurrenceType() == CalendarDataObject.DAILY && r.getUntil() != null) {
                // DAILY

            } else if (calendarObject.getRecurrenceType() == CalendarDataObject.WEEKLY && r.getByDay() != null && r.getByDay().size() > 0) {
                // WEEKLY
                final List<WeekdayNum> weekdays = r.getByDay();
                int days = 0;
                for (WeekdayNum w : weekdays) {
                    days |= (int)Math.pow(2, w.wday.jsDayNum);
                }
                calendarObject.setDays(days);
            } else if (calendarObject.getRecurrenceType() == CalendarDataObject.MONTHLY) {
                // MONTHLY
                final List<WeekdayNum> weekdays = r.getByDay();
                // When it comes to monthly events, the rule should only contain one entry
                if (weekdays.size() == 1) {
                    final WeekdayNum weekdayNum = weekdays.get(0);
                    calendarObject.setDayInMonth(weekdayNum.num);
                    calendarObject.setDays((int)Math.pow(2, weekdayNum.wday.javaDayNum));
                }
            } else if (calendarObject.getRecurrenceType() == CalendarDataObject.YEARLY) {
                // YEARLY
                final Calendar c = Calendar.getInstance(TimeZone.getTimeZone(calendarObject.getTimezone()));
                c.setTime(calendarObject.getStartDate());
                calendarObject.setDayInMonth(c.get(Calendar.DAY_OF_WEEK_IN_MONTH));
                calendarObject.setDays(c.get(Calendar.DAY_OF_WEEK));
                calendarObject.setMonth(c.get(Calendar.MONTH));
            }

            // Set occurrence or until
            if (r.getUntil() != null) {
                final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(calendarObject.getTimezone()));
                cal.set(Calendar.YEAR, r.getUntil().year());
                cal.set(Calendar.MONTH, r.getUntil().month());
                cal.set(Calendar.DAY_OF_MONTH, r.getUntil().day());
                calendarObject.setUntil(cal.getTime());
            } else if (r.getCount() > 0) {
                calendarObject.setOccurrence(r.getCount());
            }
        } catch (ParseException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
