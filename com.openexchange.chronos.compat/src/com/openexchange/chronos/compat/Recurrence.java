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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;

/**
 * {@link Recurrence}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class Recurrence {

    private static final Logger LOG = LoggerFactory.getLogger(Recurrence.class);

    private static final Map<String, Integer> weekdays = new HashMap<String, Integer>();

    private static final Map<Integer, String> reverseDays = new HashMap<Integer, String>();

    private static final List<Integer> allDays = new LinkedList<Integer>();

    private static final SimpleDateFormat date;

    private static final char DELIMITER_PIPE = '|';

    static {
        weekdays.put("SU", 1);
        weekdays.put("MO", 2);
        weekdays.put("TU", 4);
        weekdays.put("WE", 8);
        weekdays.put("TH", 16);
        weekdays.put("FR", 32);
        weekdays.put("SA", 64);

        for (final Map.Entry<String, Integer> entry : weekdays.entrySet()) {
            allDays.add(entry.getValue());
            reverseDays.put(entry.getValue(), entry.getKey());
        }
        Collections.sort(allDays); // nicer order in BYDAYS
        date = new SimpleDateFormat("yyyyMMdd");
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Gets the recurrence rule appropriate for the supplied series pattern.
     *
     * @param seriesPattern The legacy, pipe-separated series pattern, e.g. <code>t|1|i|1|s|1313388000000|e|1313625600000|o|4|</code>
     * @return The corresponding recurrence rule
     */
    public static String getRecurrenceRule(String seriesPattern, TimeZone tz, Boolean fullTime) {
        SeriesPattern pattern = SeriesPattern.parse(seriesPattern, tz, fullTime);
        return generateRRule(pattern);
    }

    public static String generateRRule(SeriesPattern pattern) {
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
            // TODO Auto-generated catch block
        }
        return null;
    }

    public static String generatePattern(String recur, Calendar startDate) {
        RecurrenceRule rrule = null;
        try {
            rrule = new RecurrenceRule(recur);
        } catch (InvalidRecurrenceRuleException e) {
            // TODO:
        }
        CalendarDataObject cObj = new CalendarDataObject();
        cObj.setTimezone(startDate.getTimeZone().getID());
        cObj.setStartDate(startDate.getTime());
        switch (rrule.getFreq()) {
            case DAILY:
                if (rrule.getByDayPart() != null && rrule.getByDayPart().size() > 0) {
                    // used as "each weekday" by some clients: FREQ=DAILY;INTERVAL=1;WKST=SU;BYDAY=MO,TU,WE,TH,FR
                    // save as 'weekly' type with daymask
                    cObj.setRecurrenceType(CalendarObject.WEEKLY);
                    setDays(cObj, rrule, startDate);
                } else {
                    cObj.setRecurrenceType(CalendarObject.DAILY);
                }
                if (rrule.getByPart(Part.BYMONTH) != null && !rrule.getByPart(Part.BYMONTH).isEmpty()) {
                    // TODO:
                }
                break;
            case WEEKLY:
                cObj.setRecurrenceType(CalendarObject.WEEKLY);
                setDays(cObj, rrule, startDate);
                break;
            case MONTHLY:
                cObj.setRecurrenceType(CalendarObject.MONTHLY);
                setMonthDay(cObj, rrule, startDate);
                break;
            case YEARLY:
                cObj.setRecurrenceType(CalendarObject.YEARLY);
                List<Integer> monthList = rrule.getByPart(Part.BYMONTH);
                if (!monthList.isEmpty()) {
                    cObj.setMonth(((Integer) monthList.get(0)).intValue() - 1);
                    setMonthDay(cObj, rrule, startDate);
                } else {
                    cObj.setMonth(startDate.get(Calendar.MONTH));
                    setMonthDay(cObj, rrule, startDate);
                }
                break;
            default:
                break;
        }

        int interval = rrule.getInterval();
        if (interval == -1) {
            interval = 1;
        }
        cObj.setInterval(interval);
        Integer count = rrule.getCount();
        if (count != null) {
            final int recurrenceCount = rrule.getCount();
            cObj.setRecurrenceCount(recurrenceCount);
            setOccurrenceIfNeededRecoveryFIXME(cObj, recurrenceCount);
        } else if (rrule.getUntil() != null) {
            cObj.setUntil(getUntil(rrule, startDate.getTimeZone()));
        }

        return createDSString(cObj, rrule);
    }

    private static String daily(SeriesPattern pattern) {
        return getRecurBuilder(Freq.DAILY, pattern).toString();
    }

    private static String weekly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.WEEKLY, pattern);
        int days = pattern.getDaysOfWeek();
        addDays(days, recur);
        return recur.toString();
    }

    private static String monthly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.MONTHLY, pattern);
        if (pattern.getType() == 5) {
            addDays(pattern.getDaysOfWeek(), recur);
            int weekNo = pattern.getDayOfMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.setByPart(Part.BYSETPOS, weekNo);
        } else if (pattern.getType() == 3) {
            recur.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
        } else {
            return null;
        }
        return recur.toString();
    }

    private static String yearly(SeriesPattern pattern) throws InvalidRecurrenceRuleException {
        RecurrenceRule recur = getRecurBuilder(Freq.YEARLY, pattern);
        if (pattern.getType() == 6) {
            addDays(pattern.getDaysOfWeek(), recur);
            recur.setByPart(Part.BYMONTH, pattern.getMonth());
            int weekNo = pattern.getDayOfMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.setByPart(Part.BYSETPOS, weekNo);
        } else if (pattern.getType() == 4) {
            recur.setByPart(Part.BYMONTH, pattern.getMonth());
            recur.setByPart(Part.BYMONTHDAY, pattern.getDayOfMonth());
        } else {
            return null;
        }
        return recur.toString();
    }

    private static void addDays(int days, final RecurrenceRule recur) throws InvalidRecurrenceRuleException {
        List<WeekdayNum> weekdays = new ArrayList<WeekdayNum>();
        for (int day : allDays) {
            if (day == (day & days)) {
                weekdays.add(WeekdayNum.valueOf(reverseDays.get(Integer.valueOf(day))));
            }
        }
        recur.setByDayPart(weekdays);
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
        Calendar utcUntilCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcUntilCalendar.setTimeInMillis(pattern.getSeriesEnd());
        /*
         * iCal wants a correct inclusive until value. With time zone and time. So extract time from the start date.
         */
        Calendar effectiveUntilCalendar = Calendar.getInstance(pattern.getTimeZone());
        Calendar seriesStart = pattern.getSeriesStartCalendar();
        effectiveUntilCalendar.set(utcUntilCalendar.get(Calendar.YEAR), utcUntilCalendar.get(Calendar.MONTH), utcUntilCalendar.get(Calendar.DAY_OF_MONTH), seriesStart.get(Calendar.HOUR_OF_DAY), seriesStart.get(Calendar.MINUTE), seriesStart.get(Calendar.SECOND));
        /*
         * finally, build an ical4j date-time
         */
        DateTime dt = new DateTime(effectiveUntilCalendar.getTimeInMillis());
        return dt;
    }

    private static void setDays(CalendarObject cObj, RecurrenceRule rrule, Calendar startDate) {
        List<WeekdayNum> weekdayList = rrule.getByDayPart();
        if (weekdayList.isEmpty()) {
            int day_of_week = startDate.get(Calendar.DAY_OF_WEEK);
            int days = -1;
            switch (day_of_week) {
                case Calendar.MONDAY:
                    days = CalendarObject.MONDAY;
                    break;
                case Calendar.TUESDAY:
                    days = CalendarObject.TUESDAY;
                    break;
                case Calendar.WEDNESDAY:
                    days = CalendarObject.WEDNESDAY;
                    break;
                case Calendar.THURSDAY:
                    days = CalendarObject.THURSDAY;
                    break;
                case Calendar.FRIDAY:
                    days = CalendarObject.FRIDAY;
                    break;
                case Calendar.SATURDAY:
                    days = CalendarObject.SATURDAY;
                    break;
                case Calendar.SUNDAY:
                    days = CalendarObject.SUNDAY;
                    break;
                default:
            }
            cObj.setDays(days);
        } else {
            int days = 0;
            for (WeekdayNum weekday : weekdayList) {
                Integer day = weekdays.get(weekday.weekday.name());
                if (null == day) {
                    // TODO:
                }
                days |= day.intValue();
            }
            cObj.setDays(days);
        }
    }

    private static void setMonthDay(CalendarObject cObj, RecurrenceRule rrule, Calendar startDate) {
        List<Integer> monthDayList = rrule.getByPart(Part.BYMONTHDAY);
        if (monthDayList.isEmpty()) {
            List<Integer> weekNoList = rrule.getByPart(Part.BYWEEKNO);
            if (!weekNoList.isEmpty()) {
                int week = ((Integer) weekNoList.get(0)).intValue();
                if (week == -1) {
                    week = 5;
                }
                cObj.setDayInMonth(week); // Day in month stores week
                setDays(cObj, rrule, startDate);
            } else if (!rrule.getByDayPart().isEmpty()) {
                setWeekdayInMonth(cObj, rrule);
                setDayInMonthFromSetPos(cObj, rrule);
            } else {
                // Default to monthly series on specific day of month
                cObj.setDayInMonth(startDate.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            cObj.setDayInMonth(((Integer) monthDayList.get(0)).intValue());
        }
    }

    private static void setDayInMonthFromSetPos(CalendarObject obj, RecurrenceRule rrule) {
        if (!rrule.getByPart(Part.BYSETPOS).isEmpty()) {
            int firstPos = (Integer) rrule.getByPart(Part.BYSETPOS).get(0);
            if (firstPos == -1) {
                firstPos = 5;
            }
            obj.setDayInMonth(firstPos);
        }
    }

    private static void setWeekdayInMonth(CalendarObject cObj, RecurrenceRule rrule) {
        List<WeekdayNum> weekdayList = rrule.getByDayPart();
        if (!weekdayList.isEmpty()) {
            int days = 0;
            for (WeekdayNum weekday : weekdayList) {
                Integer day = weekdays.get(weekday.weekday.name());
                if (null == day) {
                    // TODO:
                }
                int offset = weekday.pos;
                if (offset != 0) {
                    if (offset == -1) {
                        offset = 5;
                    }
                    cObj.setDayInMonth(offset);
                }
                days |= day.intValue();
            }
            cObj.setDays(days);
        }
    }

    private static void setOccurrenceIfNeededRecoveryFIXME(CalendarDataObject cObj, int recurrenceCount) {
        if (Appointment.class.isAssignableFrom(cObj.getClass())) {
            cObj.setOccurrence(recurrenceCount);
        }
    }

    private static Date getUntil(RecurrenceRule recur, TimeZone tz) {
        if (recur != null && recur.getUntil() != null) {
            DateTime until = recur.getUntil();
            if (until.isAllDay()) {
                /*
                 * consider DATE value type - already in OX format
                 */
                return new Date(until.getTimestamp());
            } else {
                /*
                 * consider DATE-TIME value type with UTC time - determine date
                 * of the last occurrence in the appointment's timezone
                 */
                Calendar effectiveUntilCalendar = Calendar.getInstance(tz);
                effectiveUntilCalendar.setTimeInMillis(until.getTimestamp());
                /*
                 * determine OX until date based on the effective until date
                 */
                Calendar utcUntilCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcUntilCalendar.set(effectiveUntilCalendar.get(Calendar.YEAR), effectiveUntilCalendar.get(Calendar.MONTH), effectiveUntilCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                utcUntilCalendar.set(Calendar.MILLISECOND, 0);
                return utcUntilCalendar.getTime();
            }
        }
        return null;
    }

    private static String createDSString(CalendarDataObject cdao, RecurrenceRule rrule) {
        if (cdao.containsStartDate()) {
            StringBuilder recStrBuilder = new StringBuilder(64);
            int recurrenceType = cdao.getRecurrenceType();
            int interval = cdao.getInterval(); // i
            int weekdays = cdao.getDays();
            int monthday = cdao.getDayInMonth();
            int month = cdao.getMonth();
            int occurrences = cdao.getOccurrence();
            if (!cdao.containsUntil() && !cdao.containsOccurrence()) {
                occurrences = -1;
            }
            if (recurrenceType == CalendarObject.DAILY) {
                dsf(recStrBuilder, 1);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (occurrences > 0) {
                    cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                }
            } else if (recurrenceType == CalendarObject.WEEKLY) {
                dsf(recStrBuilder, 2);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 'a', weekdays);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (occurrences > 0) {
                    cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                }
            } else if (recurrenceType == CalendarObject.MONTHLY) {
                if (monthday <= 0) {
                    // TODO:
                }
                if (weekdays <= 0) {
                    if (monthday > 31) {
                        // TODO:
                    }
                    dsf(recStrBuilder, 3);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday > 5) {
                        // TODO:
                    }
                    dsf(recStrBuilder, 5);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                }
            } else if (recurrenceType == CalendarObject.YEARLY) {
                if (weekdays <= 0) {
                    if (monthday <= 0 || monthday > 31) {
                        // TODO:
                    }
                    dsf(recStrBuilder, 4);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday < 1 || monthday > 5) {
                        // TODO:
                    }
                    dsf(recStrBuilder, 6);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilForUnlimited(cdao, rrule));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                }
            } else {
                recStrBuilder = null;
            }
            return recStrBuilder == null ? null : recStrBuilder.toString();
        }
        // TODO:
        return null;
    }

    private static Date calculateUntilForUnlimited(CalendarDataObject cObj, RecurrenceRule rrule) {
        DateTime start = new DateTime(cObj.getRecurringStart());
        RecurrenceRuleIterator iterator = rrule.iterator(start);
        int count = 0;
        long millis = 0L;
        while (iterator.hasNext() && count++ <= 999) { // TODO: implicit limit
            millis = iterator.nextMillis();
        }
        return new Date(millis);
    }

    private static void dsf(StringBuilder sb, char c, int v) {
        if (v >= 0) {
            sb.append(c);
            sb.append(DELIMITER_PIPE);
            sb.append(v);
            sb.append(DELIMITER_PIPE);
        }
    }

    private static void dsf(StringBuilder sb, char c, long l) {
        sb.append(c);
        sb.append(DELIMITER_PIPE);
        sb.append(l);
        sb.append(DELIMITER_PIPE);
    }

    private static void dsf(StringBuilder sb, int type) {
        dsf(sb, 't', type);
    }
}
