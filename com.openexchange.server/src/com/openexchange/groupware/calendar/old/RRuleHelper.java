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

package com.openexchange.groupware.calendar.old;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Recur;

/**
 * {@link RRuleHelper} - From com.openexchange.data.conversion.ical.ical4j.internal.calendar.Recurrence
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class RRuleHelper {

    private static final Map<String, Integer> WEEKDAYS = new HashMap<String, Integer>();

    private static final Map<Integer, String> REVERSEDAYS = new HashMap<Integer, String>();

    private static final List<Integer> ALLDAYS = new LinkedList<Integer>();

    static {
        WEEKDAYS.put("MO", Integer.valueOf(CalendarObject.MONDAY));
        WEEKDAYS.put("TU", Integer.valueOf(CalendarObject.TUESDAY));
        WEEKDAYS.put("WE", Integer.valueOf(CalendarObject.WEDNESDAY));
        WEEKDAYS.put("TH", Integer.valueOf(CalendarObject.THURSDAY));
        WEEKDAYS.put("FR", Integer.valueOf(CalendarObject.FRIDAY));
        WEEKDAYS.put("SA", Integer.valueOf(CalendarObject.SATURDAY));
        WEEKDAYS.put("SU", Integer.valueOf(CalendarObject.SUNDAY));

        for (final Map.Entry<String, Integer> entry : WEEKDAYS.entrySet()) {
            ALLDAYS.add(entry.getValue());
            REVERSEDAYS.put(entry.getValue(), entry.getKey());
        }
        Collections.sort(ALLDAYS); // nicer order in BYDAYS
    }

    /**
     * Initializes a new {@link RRuleHelper}.
     */
    private RRuleHelper() {
        super();
    }

    /**
     * Generates an RRule based on an {@link CalendarObject}
     *
     * @param calendarObject The calendarObject
     * @return The RRULE as {@link String}
     */
    public static String getRecurrenceRule(CalendarObject calendarObject) {
        if (calendarObject.isException()) {
            return "";
        }
        switch (calendarObject.getRecurrenceType()) {
            case CalendarObject.DAILY:
                return getDailyRecurrenceRule(calendarObject);
            case CalendarObject.WEEKLY:
                return getWeeklyRecurrenceRule(calendarObject);
            case CalendarObject.MONTHLY:
                return getMonthlyRecurrenceRule(calendarObject);
            case CalendarObject.YEARLY:
                return getYearlyRecurrenceRule(calendarObject);
            default:
                break;
        }
        return "";
    }

    /**
     * Gets a yearly recurrence rule from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The yearly recurrence rule
     */
    private static String getYearlyRecurrenceRule(CalendarObject calendarObject) {
        final StringBuilder recur = startRecurrenceRule("YEARLY", calendarObject);
        if (calendarObject.containsDays()) {
            addDays(calendarObject.getDays(), recur);
            recur.append(";BYMONTH=").append(calendarObject.getMonth() + 1);
            recur.append(";BYSETPOS=").append(calendarObject.getDayInMonth());
        } else {
            recur.append(";BYMONTH=").append(calendarObject.getMonth() + 1).append(";BYMONTHDAY=").append(calendarObject.getDayInMonth());
        }
        return recur.toString();
    }

    /**
     * Gets a monthly recurrence rule from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The monthly recurrence rule
     */
    private static String getMonthlyRecurrenceRule(CalendarObject calendarObject) {
        final StringBuilder recur = startRecurrenceRule("MONTHLY", calendarObject);
        if (calendarObject.containsDays()) {
            addDays(calendarObject.getDays(), recur);
            int weekNo = calendarObject.getDayInMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.append(";BYSETPOS=").append(weekNo);
        } else if (calendarObject.containsDayInMonth()) {
            recur.append(";BYMONTHDAY=").append(calendarObject.getDayInMonth());
        }
        return recur.toString();
    }

    /**
     * Gets a weekly recurrence rule from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The weekly recurrence rule
     */
    private static String getWeeklyRecurrenceRule(CalendarObject calendarObject) {
        final StringBuilder recur = startRecurrenceRule("WEEKLY", calendarObject);
        if (calendarObject.containsDays()) {
            addDays(calendarObject.getDays(), recur);
        }
        return recur.toString();
    }

    /**
     * Gets a daily recurrence rule from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The daily recurrence rule
     */
    private static String getDailyRecurrenceRule(CalendarObject calendarObject) {
        final Recur recur = getRecur("DAILY", calendarObject);
        recur.setInterval(calendarObject.getInterval());
        return recur.toString();
    }

    /**
     * Adds "BYDAY" rule to the given recurrence rule
     *
     * @param days The days to add
     * @param recur The recurrence rule
     */
    private static void addDays(final int days, final StringBuilder recur) {
        recur.append(';').append("BYDAY").append('=');
        for (final int day : ALLDAYS) {
            if (day == (day & days)) {
                recur.append(REVERSEDAYS.get(Integer.valueOf(day))).append(',');
            }
        }
        recur.setLength(recur.length() - 1);
    }

    /**
     * Creates a {@link StringBuilder} containing a recurrence rule.
     *
     * @param frequency The frequency
     * @param calendarObject The {@link CalendarObject}
     * @return The {@link StringBuilder} containing the recurrence rule
     */
    private static StringBuilder startRecurrenceRule(final String frequency, CalendarObject calendarObject) {
        final StringBuilder recur = new StringBuilder("FREQ=").append(frequency).append(";INTERVAL=").append(calendarObject.getInterval());
        if (calendarObject.containsOccurrence()) {
            recur.append(";COUNT=").append(calendarObject.getOccurrence());
        } else if (calendarObject.containsUntil()) {
            recur.append(";UNTIL=").append(getUntil(calendarObject).toString());
        }
        return recur;
    }

    /**
     * Gets a {@link Recur} from the given {@link CalendarObject} and frequency
     *
     * @param frequency The frequency
     * @param calendarObject The {@link CalendarObject}
     * @return The {@link Recur}
     */
    private static Recur getRecur(final String frequency, CalendarObject calendarObject) {
        final Recur retval;
        if (calendarObject.containsOccurrence()) {
            retval = new Recur(frequency, calendarObject.getOccurrence());
        } else if (calendarObject.containsUntil()) {
            retval = new Recur(frequency, getUntil(calendarObject));
        } else {
            retval = new Recur(frequency, null);
        }
        return retval;
    }

    /**
     * Gets the until value from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The until value as a {@link Date}
     */
    private static net.fortuna.ical4j.model.Date getUntil(CalendarObject calendarObject) {
        if (calendarObject.containsUntil()) {
            /*
             * OX stores series end as date without time (00:00 UTC).
             */
            if (calendarObject.getFullTime()) {
                /*
                 * use DATE value type with UTC time - equal to the stored date
                 */
                return new net.fortuna.ical4j.model.Date(calendarObject.getUntil());
            }
            /*
             * Since DTSTART is specified as date with local time and time zone
             * reference, use DATE-TIME value type with UTC time for non-whole-day
             * events.
             */
            Calendar utcUntilCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcUntilCalendar.setTime(calendarObject.getUntil());
            /*
             * The effective last possible until date-time is 23:59:59 on the
             * series until date in the recurring appointments timezone, so we
             * first need to assume the series end being in the appointment's
             * timezone, and afterwards add this portion of time.
             */
            java.util.TimeZone appointmentTimeZone = getTimeZone(calendarObject);
            Calendar effectiveUntilCalendar = Calendar.getInstance(appointmentTimeZone);
            effectiveUntilCalendar.set(utcUntilCalendar.get(Calendar.YEAR), utcUntilCalendar.get(Calendar.MONTH), utcUntilCalendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
            /*
             * finally, build an ical4j date-time
             */
            net.fortuna.ical4j.model.DateTime dateTime = new net.fortuna.ical4j.model.DateTime(true);
            dateTime.setTime(effectiveUntilCalendar.getTime().getTime());
            return dateTime;
        }

        return null;
    }

    /**
     * Gets the timezone from the given {@link CalendarObject}
     *
     * @param calendarObject The {@link CalendarObject}
     * @return The {@link TimeZone}
     */
    private static TimeZone getTimeZone(CalendarObject calendarObject) {
        String timeZoneID = null;
        if (null != calendarObject && Appointment.class.isInstance(calendarObject)) {
            timeZoneID = ((Appointment) calendarObject).getTimezone();
        }
        return java.util.TimeZone.getTimeZone(null != timeZoneID ? timeZoneID : "UTC");
    }

}
