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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.chronos.recurrence.service;

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import java.util.Calendar;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.dmfs.rfc5545.recurrenceset.RecurrenceList;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RecurrenceUtils {

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule.
     *
     * @param recurrenceData The recurrence data
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static RecurrenceSetIterator getRecurrenceIterator(RecurrenceData recurrenceData) throws OXException {
        return getRecurrenceIterator(recurrenceData, false);
    }

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule, optionally advancing to the first occurrence. The latter
     * option ensures that the first date delivered by the iterator matches the start-date of the first occurrence.
     *
     * @param recurrenceData The recurrence data
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    public static RecurrenceSetIterator getRecurrenceIterator(RecurrenceData recurrenceData, boolean forwardToOccurrence) throws OXException {
        RecurrenceRule rule = initRecurrenceRule(recurrenceData.getRecurrenceRule());
        return getRecurrenceIterator(rule, recurrenceData.getSeriesStart(), recurrenceData.getRecurrenceDates(), forwardToOccurrence);
    }

    /**
     * Initializes a new recurrence iterator for a specific recurrence rule, optionally advancing to the first occurrence. The latter
     * option ensures that the first date delivered by the iterator matches the start-date of the first occurrence.
     *
     * @param rule The recurrence rule
     * @param seriesStart The series start date, usually the date of the first occurrence
     * @param recurrenceDates The list of recurrence dates to include in the recurrence set, or <code>null</code> if there are none
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @return The recurrence rule iterator
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    private static RecurrenceSetIterator getRecurrenceIterator(RecurrenceRule rule, DateTime seriesStart, long[] recurrenceDates, boolean forwardToOccurrence) throws OXException {
        DateTime start = seriesStart;
        try {
            if (forwardToOccurrence && false == isPotentialOccurrence(start, rule)) {
                /*
                 * supplied start does not match recurrence rule, forward to first "real" occurrence
                 */
                DateTime firstOccurrence = null;
                Integer originalCount = rule.getCount();
                try {
                    if (null != originalCount) {
                        rule.setUntil(null);
                    }
                    for (RecurrenceRuleIterator iterator = rule.iterator(start); null == firstOccurrence && iterator.hasNext(); iterator.nextMillis()) {
                        // TODO: max_recurrences guard?

                        DateTime peekedDateTime = iterator.peekDateTime();
                        if (peekedDateTime.after(seriesStart)) {
                            firstOccurrence = peekedDateTime;
                        }
                    }
                    if (null != firstOccurrence) {
                        start = firstOccurrence;
                    } else {
                        throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(new DefaultRecurrenceId(seriesStart), rule);
                    }
                } finally {
                    if (null != originalCount) {
                        rule.setCount(originalCount.intValue());
                    }
                }
            }
            RecurrenceSet recurrenceSet = new RecurrenceSet();
            recurrenceSet.addInstances(new RecurrenceRuleAdapter(rule));
            if (null != recurrenceDates && 0 < recurrenceDates.length) {
                recurrenceSet.addInstances(new RecurrenceList(recurrenceDates));
            }
            return recurrenceSet.iterator(start.getTimeZone(), start.getTimestamp());
        } catch (IllegalArgumentException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, rule);
        }
    }

    /**
     * Gets a value indicating whether a specific date-time is (or could be) part of a recurrence rule or not.
     * <p/>
     * This is always <code>true</code> for <i>relative</i> recurrences that can usually begin on any date and the continue with a
     * specific interval, e.g. as in a simple <code>FREQ=DAILY</code> rule.
     * <p/>
     * Otherwise, the occurrences are derived from the rule directly, and a specific date is not necessarily part of the rule, e.g. a
     * <code>FREQ=WEEKLY;BYDAY=WE</code> or a <code>FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=8</code> event series.
     *
     * @param dateTime The date-time to check
     * @param recurrenceRule The recurrence rule to match against
     * @return <code>true</code> if the date-time is (or could be) an actual occurrence of the recurrence rule, <code>false</code>, otherwise
     */
    private static boolean isPotentialOccurrence(DateTime dateTime, RecurrenceRule recurrenceRule) {
        List<WeekdayNum> byDayPart = recurrenceRule.getByDayPart();
        List<Integer> byMonthPart = recurrenceRule.getByPart(Part.BYMONTH);
        List<Integer> byMonthDayPart = recurrenceRule.getByPart(Part.BYMONTHDAY);
        List<Integer> bySetPosPart = recurrenceRule.getByPart(Part.BYSETPOS);
        switch (recurrenceRule.getFreq()) {
            case SECONDLY:
            case MINUTELY:
            case HOURLY:
            case DAILY:
                return true;
            case WEEKLY:
                if (null != byDayPart && 0 < byDayPart.size()) {
                    return matchesDayOfWeek(dateTime, byDayPart);
                }
                return true;
            case MONTHLY:
                if (null != byMonthDayPart && 0 < byMonthDayPart.size()) {
                    /*
                     * ~ "monthly 1"
                     */
                    return matchesDayOfMonth(dateTime, byMonthDayPart);
                }
                if (null != byDayPart && 0 < byDayPart.size() && null != bySetPosPart && 0 < bySetPosPart.size()) {
                    /*
                     * ~ "monthly 2"
                     */
                    return matchesDayOfWeekInMonth(dateTime, byDayPart, bySetPosPart);
                }
                break;
            case YEARLY:
                if (null != byMonthDayPart && 0 < byMonthDayPart.size() && null != byMonthPart && 0 < byMonthPart.size()) {
                    /*
                     * ~ "yearly 1"
                     */
                    return matchesMonth(dateTime, byMonthPart) && matchesDayOfMonth(dateTime, byMonthDayPart);
                }
                if (null != byMonthPart && 0 < byMonthPart.size() && null != byDayPart && 0 < byDayPart.size() && null != bySetPosPart && 0 < bySetPosPart.size()) {
                    /*
                     * ~ "yearly 2"
                     */
                    return matchesMonth(dateTime, byMonthPart) && matchesDayOfWeekInMonth(dateTime, byDayPart, bySetPosPart);
                }
                return true;
            default:
                return false;
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific date matches a certain weekday in the month as given through the supplied recurrence
     * rule fragments.
     *
     * @param dateTime The date-time to check
     * @param byDayPart The possible weekday numbers to match
     * @param bySetPosPart The possible <i>set</i> positions of the week in the month to match
     * @return <code>true</code> if the date-time matches the day of week in month, <code>false</code>, otherwise
     */
    private static boolean matchesDayOfWeekInMonth(DateTime dateTime, List<WeekdayNum> byDayPart, List<Integer> bySetPosPart) {
        for (Integer bySetPos : bySetPosPart) {
            Calendar calendar = initCalendar(null != dateTime.getTimeZone() ? dateTime.getTimeZone() : TimeZones.UTC, dateTime.getTimestamp());
            if (0 < bySetPos.intValue()) {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                for (int matched = 0; dateTime.getMonth() == calendar.get(Calendar.MONTH); calendar.add(Calendar.DAY_OF_MONTH, 1)) {
                    if (matchesDayOfWeek(calendar, byDayPart) && ++matched == bySetPos.intValue() && dateTime.getTimestamp() == calendar.getTimeInMillis()) {
                        return true;
                    }
                }
            } else if (0 > bySetPos.intValue()) {
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                for (int matched = 0; dateTime.getMonth() == calendar.get(Calendar.MONTH); calendar.add(Calendar.DAY_OF_MONTH, -1)) {
                    if (matchesDayOfWeek(calendar, byDayPart) && ++matched == -1 * bySetPos.intValue() && dateTime.getTimestamp() == calendar.getTimeInMillis()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific date matches a certain weekday as given through the supplied recurrence rule fragments.
     *
     * @param dateTime The date-time to check
     * @param byDayPart The possible weekdays to match
     * @return <code>true</code> if the date-time matches the weekday, <code>false</code>, otherwise
     */
    private static boolean matchesDayOfWeek(DateTime dateTime, List<WeekdayNum> byDayPart) {
        for (WeekdayNum weekdayNum : byDayPart) {
            if (weekdayNum.weekday.ordinal() == dateTime.getDayOfWeek()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the {@link Calendar#DAY_OF_WEEK} field of a specific calendar instance matches a certain weekday as
     * given through the supplied recurrence rule fragments.
     *
     * @param calendar The calendar to check
     * @param byDayPart The possible weekdays to match
     * @return <code>true</code> if the date-time matches the weekday, <code>false</code>, otherwise
     */
    private static boolean matchesDayOfWeek(Calendar calendar, List<WeekdayNum> byDayPart) {
        int weekDayOrdinal = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        for (WeekdayNum weekdayNum : byDayPart) {
            if (weekdayNum.weekday.ordinal() == weekDayOrdinal) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific date matches a certain day of the month as given through the supplied recurrence rule
     * fragments.
     *
     * @param dateTime The date-time to check
     * @param byMonthDayPart The possible month days to match
     * @return <code>true</code> if the date-time matches the day of month, <code>false</code>, otherwise
     */
    private static boolean matchesDayOfMonth(DateTime dateTime, List<Integer> byMonthDayPart) {
        Calendar calendar = initCalendar(null != dateTime.getTimeZone() ? dateTime.getTimeZone() : TimeZones.UTC, dateTime.getTimestamp());
        int dayMonth = dateTime.getDayOfMonth();
        int maximumDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (Integer byMonthDay : byMonthDayPart) {
            if (byMonthDay.intValue() == dayMonth || dayMonth - maximumDayOfMonth - 1 == byMonthDay.intValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific date matches a certain month as given through the supplied recurrence rule fragments.
     *
     * @param dateTime The date-time to check
     * @param byMonthPart The possible months to match
     * @return <code>true</code> if the date-time matches the month, <code>false</code>, otherwise
     */
    private static boolean matchesMonth(DateTime dateTime, List<Integer> byMonthPart) {
        for (Integer byMonth : byMonthPart) {
            if (byMonth.intValue() == dateTime.getMonth()) {
                return true;
            }
        }
        return false;
    }


}
