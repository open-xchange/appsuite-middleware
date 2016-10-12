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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.common;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;

/**
 * {@link CalendarUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarUtils {

    /**
     * Looks up a specific internal attendee in a collection of attendees, utilizing the
     * {@link CalendarUtils#matches(Attendee, Attendee)} routine.
     *
     * @param attendees The attendees to search
     * @param attendee The attendee to lookup
     * @return The matching attendee, or <code>null</code> if not found
     * @see Utils#matches(Attendee, Attendee)
     */
    public static Attendee find(List<Attendee> attendees, Attendee attendee) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee candidateAttendee : attendees) {
                if (matches(attendee, candidateAttendee)) {
                    return candidateAttendee;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a specific attendee is present in a collection of attendees, utilizing the
     * {@link CalendarUtils#matches(Attendee, Attendee)} routine.
     *
     * @param attendees The attendees to search
     * @param attendee The attendee to lookup
     * @return <code>true</code> if the attendee is contained in the collection of attendees, <code>false</code>, otherwise
     * @see Utils#matches(Attendee, Attendee)
     */
    public static boolean contains(List<Attendee> attendees, Attendee attendee) {
        return null != find(attendees, attendee);
    }

    /**
     * Gets a value indicating whether one calendar user matches another, by comparing the entity identifier for internal calendar users,
     * or trying to match the calendar user's URI for external ones.
     *
     * @param user1 The first calendar user to check
     * @param user2 The second calendar user to check
     * @return <code>true</code> if the objects <i>match</i>, i.e. are targeting the same calendar user, <code>false</code>, otherwise
     */
    public static boolean matches(CalendarUser user1, CalendarUser user2) {
        if (null == user1) {
            return null == user2;
        } else if (null != user2) {
            if (0 < user1.getEntity() && user1.getEntity() == user2.getEntity()) {
                return true;
            }
            if (null != user1.getUri() && user1.getUri().equals(user2.getUri())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks up a specific internal attendee in a collection of attendees based on its entity identifier.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return The matching attendee, or <code>null</code> if not found
     */
    public static Attendee find(List<Attendee> attendees, int entity) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee attendee : attendees) {
                if (entity == attendee.getEntity()) {
                    return attendee;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether an attendee represents an <i>internal</i> entity, i.e. an internal user, group or resource, or not.
     *
     * @param attendee The attendee to check
     * @return <code>true</code> if the attendee is internal, <code>false</code>, otherwise
     */
    public static boolean isInternal(Attendee attendee) {
        return 0 < attendee.getEntity() || 0 == attendee.getEntity() && CalendarUserType.GROUP.equals(attendee.getCuType());
    }

    /**
     * Gets a value indicating whether a collection of attendees contains a specific internal attendee based on its entity identifier or
     * not.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return <code>true</code> if the attendee was found, <code>false</code>, otherwise
     */
    public static boolean contains(List<Attendee> attendees, int entity) {
        return null != find(attendees, entity);
    }

    /**
     * Gets a value indicating whether a specific user is the organizer of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is the organizer, <code>false</code>, otherwise
     */
    public static boolean isOrganizer(Event event, int userId) {
        return null != event.getOrganizer() && userId == event.getOrganizer().getEntity();
    }

    /**
     * Gets a value indicating whether a specific event is organized externally, i.e. no internal organizer entity is responsible.
     *
     * @param event The event to check
     * @return <code>true</code> if the event has an <i>external</i> organizer, <code>false</code>, otherwise
     */
    public static boolean hasExternalOrganizer(Event event) {
        return null != event.getOrganizer() && 0 >= event.getOrganizer().getEntity();
    }

    /**
     * Gets a value indicating whether a specific user is an attendee of an event or not.
     *
     * @param event The event
     * @param userId The identifier of the user to check
     * @return <code>true</code> if the user with the supplied identifier is an attendee, <code>false</code>, otherwise
     */
    public static boolean isAttendee(Event event, int userId) {
        return contains(event.getAttendees(), userId);
    }

    /**
     * Truncates the time part of the supplied date, i.e. sets the fields {@link Calendar#HOUR_OF_DAY}, {@link Calendar#MINUTE},
     * {@link Calendar#SECOND} and {@link Calendar#MILLISECOND} to <code>0</code>.
     *
     * @param date The date to truncate the time part for
     * @param timeZone The timezone to consider
     * @return A new date instance based on the supplied date with the time fraction truncated
     */
    public static Date truncateTime(Date date, TimeZone timeZone) {
        return truncateTime(initCalendar(timeZone, date)).getTime();
    }

    /**
     * Truncates the time part in the supplied calendar reference, i.e. sets the fields {@link Calendar#HOUR_OF_DAY},
     * {@link Calendar#MINUTE}, {@link Calendar#SECOND} and {@link Calendar#MILLISECOND} to <code>0</code>.
     *
     * @param calendar The calendar reference to truncate the time part in
     * @param timeZone The timezone to consider
     * @return The calendar reference
     */
    public static Calendar truncateTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Converts a so-called <i>floating</i> date into a date in a concrete timezone by applying the actual timezone offset on that date.
     *
     * @param floatingDate The floating date to convert (usually the raw date in <code>UTC</code>)
     * @param timeZone The target timezone
     * @return The date in the target timezone, with the corresponding timezone offset applied
     */
    public static Date getDateInTimeZone(Date floatingDate, TimeZone timeZone) {
        return new Date(floatingDate.getTime() - timeZone.getOffset(floatingDate.getTime()));
    }

    /**
     * Gets the identifiers of the supplied events in an array.
     *
     * @param events The events to get the identifiers for
     * @return The object identifiers
     */
    public static int[] getObjectIDs(List<Event> events) {
        int[] objectIDs = new int[events.size()];
        for (int i = 0; i < events.size(); i++) {
            objectIDs[i] = events.get(i).getId();
        }
        return objectIDs;
    }

    /**
     * Gets a value indicating whether the supplied event is considered as the <i>master</i> event of a recurring series or not, based
     * on the properties {@link EventField#ID} and {@link EventField#SERIES_ID} for equality.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    public static boolean isSeriesMaster(Event event) {
        return null != event && event.getId() == event.getSeriesId();
    }

    /**
     * Gets a value indicating whether the supplied event is considered as an exceptional event of a recurring series or not, based on
     * the properties {@link EventField#ID} and {@link EventField#SERIES_ID}.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is the series master, <code>false</code>, otherwise
     */
    public static boolean isSeriesException(Event event) {
        return null != event && 0 < event.getSeriesId() && event.getSeriesId() != event.getId();
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     *
     * @param timeZone The timezone to use for the calendar
     * @param time The initial time to set, or <code>null</code> to intialize with the default time
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, Date time) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        if (null != time) {
            calendar.setTime(time);
        }
        return calendar;
    }

    /**
     * Initializes a new calendar in a specific timezone and sets the initial time.
     *
     * @param timeZone The timezone to use for the calendar
     * @param time The initial time in UTC milliseconds from the epoch
     * @return A new calendar instance
     */
    public static Calendar initCalendar(TimeZone timeZone, long time) {
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        calendar.setTimeInMillis(time);
        return calendar;
    }

    /**
     * Gets a value indicating whether a specific event falls (at least partly) into a time range.
     *
     * @param event The event to check
     * @param from The lower inclusive limit of the range, i.e. the event should start on or after this date, or <code>null</code> for no limit
     * @param until The upper exclusive limit of the range, i.e. the event should end before this date, or <code>null</code> for no limit
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event falls into the time range, <code>false</code>, otherwise
     */
    public static boolean isInRange(Event event, Date from, Date until, TimeZone timeZone) {
        // TODO floating events that are not "all-day"
        Date startDate = isFloating(event) ? getDateInTimeZone(event.getStartDate(), timeZone) : event.getStartDate();
        Date endDate = isFloating(event) ? getDateInTimeZone(event.getEndDate(), timeZone) : event.getEndDate();
        return (null == until || startDate.before(until)) && (null == from || endDate.after(from));
    }

    /**
     * Gets a value indicating whether a specific event is over, i.e. it's end-time falls into the past, based on the system time.
     *
     * @param event The event to check
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return <code>true</code> if the event is in the past, <code>false</code>, otherwise
     */
    public static boolean isInPast(Event event, TimeZone timeZone) {
        return false == isInRange(event, new Date(), null, timeZone);
    }

    /**
     * Gets a value indicating whether the supplied event contains so-called <i>floating</i> dates, i.e. the event doesn't start- and end
     * at a fixed date and time, but is always rendered in the view of the user's current timezone.
     *
     * @param event The event to check
     * @return <code>true</code> if the event is <i>floating</i>, <code>false</code>, otherwise
     */
    public static boolean isFloating(Event event) {
        // - floating events that are not "all-day"?
        // - non floating all-day events?
        // - better rely on null == event.getStartTimeZone()?
        return event.isAllDay();
    }

    /**
     * Filters a list of attendees based on their calendaruser type, and whether they represent "internal" attendees or not.
     *
     * @param attendees The attendees to filter
     * @param internal {@link Boolean#TRUE} to only consider internal entities, {@link Boolean#FALSE} for non-internal ones,
     *            or <code>null</code> to not filter by internal/external
     * @param cuType The {@link CalendarUserType} to consider, or <code>null</code> to not filter by calendar user type
     * @return The filtered attendees
     */
    public static List<Attendee> filter(List<Attendee> attendees, Boolean internal, CalendarUserType cuType) {
        if (null == attendees) {
            return null;
        }
        List<Attendee> filteredAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            if (null == cuType || cuType.equals(attendee.getCuType())) {
                if (null == internal || internal.booleanValue() == isInternal(attendee)) {
                    filteredAttendees.add(attendee);
                }
            }
        }
        return filteredAttendees;
    }

    /**
     * Gets the entity identifiers of all attendees representing internal users.
     *
     * @param attendees The attendees to extract the user identifiers for
     * @return The user identifiers, or an empty array if there are none
     */
    public static int[] getUserIDs(List<Attendee> attendees) {
        if (null == attendees || 0 == attendees.size()) {
            return new int[0];
        }
        List<Integer> userIDs = new ArrayList<Integer>(attendees.size());
        for (Attendee attendee : attendees) {
            if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && isInternal(attendee)) {
                userIDs.add(I(attendee.getEntity()));
            }
        }
        return I2i(userIDs);
    }

    /**
     * Parses a trigger duration string.
     *
     * @param duration The duration to parse
     * @return The total milliseconds of the parsed duration
     * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.3.6">RFC 5545, section 3.3.6</a>
     */
    public static long getTriggerDuration(String duration) {
        long totalMillis = 0;
        boolean negative = false;
        String token = null;
        String previousToken = null;
        StringTokenizer tokenizer = new StringTokenizer(duration.toUpperCase(), "+-PWDTHMS", true);
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            switch (token) {
                case "+":
                    negative = false;
                    break;
                case "-":
                    negative = true;
                    break;
                case "W":
                    totalMillis += TimeUnit.DAYS.toMillis(7 * Long.parseLong(previousToken));
                    break;
                case "D":
                    totalMillis += TimeUnit.DAYS.toMillis(Long.parseLong(previousToken));
                    break;
                case "H":
                    totalMillis += TimeUnit.HOURS.toMillis(Long.parseLong(previousToken));
                    break;
                case "M":
                    totalMillis += TimeUnit.MINUTES.toMillis(Long.parseLong(previousToken));
                    break;
                case "S":
                    totalMillis += TimeUnit.SECONDS.toMillis(Long.parseLong(previousToken));
                    break;
                case "T":
                case "P":
                default:
                    // skip
                    break;
            }
            previousToken = token;
        }
        return negative ? -1 * totalMillis : totalMillis;
    }

}
