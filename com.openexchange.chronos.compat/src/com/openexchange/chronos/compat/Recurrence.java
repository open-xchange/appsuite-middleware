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

package com.openexchange.chronos.compat;

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Recurrence}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class Recurrence {

    /**
     * Gets a value indicating whether the supplied recurrence data is supported or not, throwing an appropriate exception if not.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE} {@link CalendarExceptionCodes#UNSUPPORTED_RRULE}
     */
    public static void checkIsSupported(RecurrenceService recurrenceService, RecurrenceData recurrenceData) throws OXException {
        /*
         * check unsupported parts
         */
        RecurrenceRule rule = getRecurrenceRule(recurrenceData.getRecurrenceRule());
        //TODO: further checks
        switch (rule.getFreq()) {
            case DAILY:
                List<Integer> byMonthPart = rule.getByPart(Part.BYMONTH);
                if (null != byMonthPart && 0 < byMonthPart.size()) {
                    // bug #9840
                    throw CalendarExceptionCodes.UNSUPPORTED_RRULE.create(
                        recurrenceData.getRecurrenceRule(), "BYMONTH", "BYMONTH not supported in DAILY");
                }
                break;
            case MONTHLY:
                break;
            case WEEKLY:
                break;
            case YEARLY:
                break;
            default:
                // no BYSECOND, BYMINUTE, BYHOUR, ...
                throw CalendarExceptionCodes.UNSUPPORTED_RRULE.create(
                    recurrenceData.getRecurrenceRule(), "FREQ", rule.getFreq() + " not supported");
        }
        /*
         * validate via recurrence service
         */
        recurrenceService.validate(recurrenceData);
    }

    /**
     * Initializes a new recurrence rule for the supplied recurrence rule string.
     *
     *
     * @param rrule The recurrence rule string
     * @return The recurrence rule
     * @throws OXException {@link CalendarExceptionCodes#INVALID_RRULE}
     */
    private static RecurrenceRule getRecurrenceRule(String rrule) throws OXException {
        try {
            return new RecurrenceRule(rrule);
        } catch (InvalidRecurrenceRuleException | IllegalArgumentException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, rrule);
        }
    }

    /**
     * Gets the recurrence rule appropriate for the supplied series pattern.
     *
     * @param seriesPattern The legacy, pipe-separated series pattern, e.g. <code>t|1|i|1|s|1313388000000|e|1313625600000|o|4|</code>
     * @return The corresponding recurrence rule
     */
    public static String getRecurrenceRule(String seriesPattern, TimeZone tz, Boolean fullTime) throws OXException {
        return getRecurrenceRule(new SeriesPattern(seriesPattern, tz.getID(), fullTime.booleanValue()));
    }

    public static String getRecurrenceRule(SeriesPattern pattern) throws OXException {
        if (pattern.isFullTime() == null) {
            throw new IllegalArgumentException("Fulltime value must be set.");
        }
        if (pattern.getTimeZone() == null) {
            throw new IllegalArgumentException("TimeZone must be set.");
        }
        try {
            switch (pattern.getType()) {
                case 1:
                    return daily(pattern);
                case 2:
                    return weekly(pattern);
                case 3:
                case 5:
                    return monthly(pattern);
                case 4:
                case 6:
                    return yearly(pattern);
                default:
                    return null;
            }
        } catch (InvalidRecurrenceRuleException e) {
            throw CalendarExceptionCodes.INVALID_RRULE.create(e, pattern);
        }
    }

    /**
     * Gets the legacy series pattern for the supplied recurrence data.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data to construct the series pattern for
     * @return The series pattern
     */
    public static SeriesPattern getSeriesPattern(RecurrenceService recurrenceService, RecurrenceData recurrenceData) throws OXException {
        /*
         * take over common attributes
         */
        TimeZone timeZone = null != recurrenceData.getTimeZoneID() ? TimeZone.getTimeZone(recurrenceData.getTimeZoneID()) : TimeZones.UTC;
        Calendar seriesStartCalendar = initCalendar(timeZone, recurrenceData.getSeriesStart());
        RecurrenceRule rrule = getRecurrenceRule(recurrenceData.getRecurrenceRule());
        SeriesPattern pattern = new SeriesPattern();
        pattern.setInterval(I(rrule.getInterval()));
        pattern.setSeriesStart(L(recurrenceData.getSeriesStart()));
        pattern.setFullTime(B(recurrenceData.isAllDay())); //TODO: needed?
        pattern.setTz(timeZone); //TODO: needed?
        if (null != rrule.getCount()) {
            pattern.setOccurrences(rrule.getCount());
            pattern.setSeriesEnd(L(getUntilForUnlimited(recurrenceService, recurrenceData).getTime()));
        } else if (null != rrule.getUntil()) {
            pattern.setSeriesEnd(L(getSeriesEnd(rrule, recurrenceService, recurrenceData).getTime()));
        }
        /*
         * apply specific parts based on rule's FREQ
         */
        switch (rrule.getFreq()) {
            case DAILY:
                if (null != rrule.getByDayPart() && 0 < rrule.getByDayPart().size()) {
                    // used as "each weekday" by some clients: FREQ=DAILY;INTERVAL=1;WKST=SU;BYDAY=MO,TU,WE,TH,FR
                    // save as 'weekly' type with daymask
                    setDays(pattern, rrule, seriesStartCalendar);
                    pattern.setType(SeriesPattern.WEEKLY);
                } else {
                    pattern.setType(SeriesPattern.DAILY);
                }
                return pattern;
            case WEEKLY:
                setDays(pattern, rrule, seriesStartCalendar);
                pattern.setType(SeriesPattern.WEEKLY);
                return pattern;
            case MONTHLY:
                setMonthDay(pattern, rrule, seriesStartCalendar);
                if (null == rrule.getByDayPart() || 0 == rrule.getByDayPart().size()) {
                    pattern.setType(SeriesPattern.MONTHLY_1);
                } else {
                    pattern.setType(SeriesPattern.MONTHLY_2);
                }
                return pattern;
            case YEARLY:
                List<Integer> monthList = rrule.getByPart(Part.BYMONTH);
                if (null != monthList && !monthList.isEmpty()) {
                    pattern.setMonth(monthList.get(0));
                    setMonthDay(pattern, rrule, seriesStartCalendar);
                } else {
                    pattern.setMonth(I(seriesStartCalendar.get(Calendar.MONTH))); //TODO: which timezone for floating events?
                    setMonthDay(pattern, rrule, seriesStartCalendar);
                }
                if (null == rrule.getByDayPart() || 0 == rrule.getByDayPart().size()) {
                    pattern.setType(SeriesPattern.YEARLY_1);
                } else {
                    pattern.setType(SeriesPattern.YEARLY_2);
                }
                return pattern;
            default:
                throw CalendarExceptionCodes.UNSUPPORTED_RRULE.create(recurrenceData.getRecurrenceRule(), Part.FREQ, "Unsupported frequency");
        }
    }

    /**
     * Calculates the (legacy) series end date from the <code>UNTIL</code> part of a specific recurrence rule, which is the date in UTC
     * (without time fraction) of the last occurrence, as used in the legacy series pattern.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data
     * @return The series end date, or <code>null</code> if not set in the recurrence rule
     */
    private static Date getSeriesEnd(RecurrenceRule rrule, RecurrenceService recurrenceService, RecurrenceData recurrenceData) throws OXException {
        if (null == rrule || null == rrule.getUntil()) {
            return null;
        }
        DateTime until = rrule.getUntil();
        if (until.isAllDay()) {
            /*
             * consider DATE value type - already in legacy format
             */
            return new Date(until.getTimestamp());
        }
        /*
         * consider DATE-TIME value type - determine start date of first occurrence outside range
         */
        DateTime nextOccurrenceStart = null;
        if (until.getTimestamp() > recurrenceData.getSeriesStart()) {
            try {
                rrule.setUntil(null);
                DefaultRecurrenceData unlimitedRecurrenceData = new DefaultRecurrenceData(
                    rrule.toString(), recurrenceData.isAllDay(), recurrenceData.getTimeZoneID(), recurrenceData.getSeriesStart());
                RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(unlimitedRecurrenceData, null, null);
                while (iterator.hasNext()) {
                    long occurrenceStart = iterator.next().getValue();
                    if (occurrenceStart > until.getTimestamp()) {
                        nextOccurrenceStart = new DateTime(until.getTimeZone(), occurrenceStart);
                        break;
                    }
                }
            } finally {
                /*
                 * ensure to reset UNTIL to previous value in any case
                 */
                rrule.setUntil(until);
            }
        }
        /*
         * check if the client-defined UNTIL is at least one day prior next occurrence (as observed in the timezone)
         */
        DateTime localUntil = null != recurrenceData.getTimeZoneID() ? new DateTime(TimeZone.getTimeZone(recurrenceData.getTimeZoneID()), until.getTimestamp()) : until;
        if (null == nextOccurrenceStart || nextOccurrenceStart.getYear() > localUntil.getYear() ||
            nextOccurrenceStart.getMonth() > localUntil.getMonth() || nextOccurrenceStart.getDayOfMonth() > localUntil.getDayOfMonth()) {
            /*
             * take over series end from UNTIL
             */
        } else {
            /*
             * shift series end one day earlier to prevent generation of an additional occurrence
             */
            localUntil = localUntil.addDuration(new Duration(-1, 1, 0));
        }
        return initCalendar(TimeZones.UTC, localUntil.getYear(), localUntil.getMonth(), localUntil.getDayOfMonth()).getTime();
    }

    private static String daily(SeriesPattern pattern) {
        return getRecurBuilder(Freq.DAILY, pattern).toString();
    }

    private static String weekly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.WEEKLY, pattern);
        recur.setByDayPart(getByDayPart(pattern.getDaysOfWeek()));
        return recur.toString();
    }

    private static String monthly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.MONTHLY, pattern);
        if (SeriesPattern.MONTHLY_2 == pattern.getType()) {
            recur.setByDayPart(getByDayPart(pattern.getDaysOfWeek()));
            int weekNo = pattern.getDayOfMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.setByPart(Part.BYSETPOS, weekNo);
        } else if (SeriesPattern.MONTHLY_1 == pattern.getType()) {
            recur.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
        } else {
            return null;
        }
        return recur.toString();
    }

    private static String yearly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.YEARLY, pattern);
        if (SeriesPattern.YEARLY_2 == pattern.getType()) {
            recur.setByDayPart(getByDayPart(pattern.getDaysOfWeek()));
            recur.setByPart(Part.BYMONTH, pattern.getMonth());
            int weekNo = pattern.getDayOfMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.setByPart(Part.BYSETPOS, weekNo);
        } else if (SeriesPattern.YEARLY_1 == pattern.getType()) {
            recur.setByPart(Part.BYMONTH, pattern.getMonth());
            recur.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
        } else {
            return null;
        }
        return recur.toString();
    }

    private static List<WeekdayNum> getByDayPart(Integer daysOfWeek) throws InvalidRecurrenceRuleException {
        List<WeekdayNum> byDayPart = new ArrayList<WeekdayNum>();
        if (null != daysOfWeek) {
            for (Weekday weekday : Weekday.values()) {
                int day = 1 << weekday.ordinal();
                if (day == (daysOfWeek.intValue() & day)) {
                    byDayPart.add(new WeekdayNum(0, weekday));
                }
            }
        }
        return byDayPart;
    }

    private static RecurrenceRule getRecurBuilder(Freq frequency, SeriesPattern pattern) {
        RecurrenceRule recur = new RecurrenceRule(frequency);
        recur.setInterval(pattern.getInterval());
        if (pattern.getOccurrences() != null) {
            recur.setCount(pattern.getOccurrences());
        } else if (pattern.getSeriesEnd() != null) {
            recur.setUntil(getUntil(pattern));
        }
        return recur;
    }

    /**
     * Determines the {@link net.fortuna.ical4j.model.Date} from the supplied
     * recurring calendar object, ready-to-use in ical4j components. <p/>
     * While date-only until dates are used as is (for tasks and whole day
     * appointments), date-time specific until dates are calculated based on
     * the appointments timezone to include the last-possible start-time of
     * the last occurrence.
     *
     * @param calendarObject the recurring calendar object
     * @return the calculated until date
     * @see http://tools.ietf.org/html/rfc5545#section-3.3.10
     */
    private static DateTime getUntil(SeriesPattern pattern) {
        if (pattern.getSeriesEnd() == null) {
            return null;
        }

        if (pattern.isFullTime()) {
            return new DateTime(pattern.getSeriesEnd()).toAllDay();
        }

        /*
         * "OX" model defines until as 00:00:00 utc if the day of the last occurrence.
         */
        Calendar utcUntilCalendar = initCalendar(TimeZones.UTC, pattern.getSeriesEnd().longValue());
        /*
         * iCal wants a correct inclusive until value. With time zone and time. So extract time from the start date.
         */
        Calendar effectiveUntilCalendar = Calendar.getInstance(pattern.getTimeZone());
//        Calendar seriesStart = pattern.getSeriesStartCalendar();
        Calendar seriesStart = Calendar.getInstance(pattern.getTimeZone());
        seriesStart.setTimeInMillis(pattern.getSeriesStart().longValue());
        effectiveUntilCalendar.set(utcUntilCalendar.get(Calendar.YEAR), utcUntilCalendar.get(Calendar.MONTH), utcUntilCalendar.get(Calendar.DAY_OF_MONTH), seriesStart.get(Calendar.HOUR_OF_DAY), seriesStart.get(Calendar.MINUTE), seriesStart.get(Calendar.SECOND));
        /*
         * finally, build an ical4j date-time
         */
//        DateTime dt = new DateTime(pattern.getTimeZone(), effectiveUntilCalendar.getTimeInMillis());
        DateTime dt = new DateTime(effectiveUntilCalendar.getTimeInMillis());
        return dt;
    }

    private static void setDays(SeriesPattern pattern, RecurrenceRule rrule, Calendar seriesStart) {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        if (null == byDayPart || byDayPart.isEmpty()) {
            pattern.setDaysOfWeek(I(1 << (seriesStart.get(Calendar.DAY_OF_WEEK) - 1)));
        } else {
            int days = 0;
            for (WeekdayNum weekday : byDayPart) {
                days |= 1 << weekday.weekday.ordinal();
            }
            pattern.setDaysOfWeek(I(days));
        }
    }

    private static void setMonthDay(SeriesPattern pattern, RecurrenceRule rrule, Calendar seriesStart) {
        List<Integer> byMonthDayPart = rrule.getByPart(Part.BYMONTHDAY);
        if (null != byMonthDayPart && 0 < byMonthDayPart.size()) {
            pattern.setDayOfMonth(byMonthDayPart.get(0));
        } else {
            List<Integer> byWeekNoPart = rrule.getByPart(Part.BYWEEKNO);
            if (null != byWeekNoPart && 0 < byWeekNoPart.size()) {
                Integer firstWeek = byWeekNoPart.get(0);
                pattern.setDaysOfWeek(-1 == firstWeek.intValue() ? I(5) : firstWeek); // Day in month stores week
                setDays(pattern, rrule, seriesStart);
            } else if (null != rrule.getByDayPart() && 0 < rrule.getByDayPart().size()) {
                setWeekdayInMonth(pattern, rrule);
                setDayInMonthFromSetPos(pattern, rrule);
            } else {
                // Default to monthly series on specific day of month
                pattern.setDayOfMonth(I(seriesStart.get(Calendar.DAY_OF_MONTH)));
            }
        }
    }

    private static void setDayInMonthFromSetPos(SeriesPattern pattern, RecurrenceRule rrule) {
        List<Integer> bySetPosPart = rrule.getByPart(Part.BYSETPOS);
        if (null != bySetPosPart && 0 < bySetPosPart.size()) {
            Integer firstPos = bySetPosPart.get(0);
            pattern.setDayOfMonth(-1 == firstPos.intValue() ? I(5) : firstPos);
        }
    }

    private static void setWeekdayInMonth(SeriesPattern pattern, RecurrenceRule rrule) {
        List<WeekdayNum> byDayPart = rrule.getByDayPart();
        if (null != byDayPart && 0 < byDayPart.size()) {
            int days = 0;
            for (WeekdayNum weekday : byDayPart) {
                days |= 1 << weekday.weekday.ordinal();
                if (0 != weekday.pos) {
                    pattern.setDayOfMonth(I(-1 == weekday.pos ? 5 : weekday.pos));
                }
            }
            pattern.setDaysOfWeek(I(days));
        }
    }

    /**
     * Gets the date of the "last" calculated occurrence for a specific recurrence (either based on the real end of the recurrence, or
     * the latest possible occurrence based on the legacy recurrence calculation limit.
     *
     * @param recurrenceService A reference to the recurrence service
     * @param recurrenceData The recurrence data
     * @return The calculated until date (in UTC timezone, with truncated time fraction)
     */
    private static Date getUntilForUnlimited(RecurrenceService recurrenceService, RecurrenceData recurrenceData) throws OXException {
        RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(recurrenceData, null, null);
        long millis = recurrenceData.getSeriesStart();
        for (int i = 0; i <= SeriesPattern.MAX_OCCURRENCESE && iterator.hasNext(); millis = iterator.next().getValue(), i++)
            ;
        DateTime localUntil;
        if (recurrenceData.isAllDay()) {
            localUntil = new DateTime(TimeZones.UTC, millis).toAllDay();
        } else if (null != recurrenceData.getTimeZoneID()) {
            localUntil = new DateTime(TimeZone.getTimeZone(recurrenceData.getTimeZoneID()), millis);
        } else {
            localUntil = new DateTime(millis);
        }
        return initCalendar(TimeZones.UTC, localUntil.getYear(), localUntil.getMonth(), localUntil.getDayOfMonth()).getTime();
    }

}
