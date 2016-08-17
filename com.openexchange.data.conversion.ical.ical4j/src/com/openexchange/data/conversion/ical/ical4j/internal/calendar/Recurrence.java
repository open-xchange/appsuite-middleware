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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Recurrence<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    private static final Map<String, Integer> weekdays = new HashMap<String, Integer>();

    private static final Map<Integer, String> reverseDays = new HashMap<Integer, String>();

    private static final List<Integer> allDays = new LinkedList<Integer>();

    private static final SimpleDateFormat date;
    static {
        weekdays.put("MO", Integer.valueOf(CalendarObject.MONDAY));
        weekdays.put("TU", Integer.valueOf(CalendarObject.TUESDAY));
        weekdays.put("WE", Integer.valueOf(CalendarObject.WEDNESDAY));
        weekdays.put("TH", Integer.valueOf(CalendarObject.THURSDAY));
        weekdays.put("FR", Integer.valueOf(CalendarObject.FRIDAY));
        weekdays.put("SA", Integer.valueOf(CalendarObject.SATURDAY));
        weekdays.put("SU", Integer.valueOf(CalendarObject.SUNDAY));

        for (final Map.Entry<String, Integer> entry : weekdays.entrySet()) {
            allDays.add(entry.getValue());
            reverseDays.put(entry.getValue(), entry.getKey());
        }
        Collections.sort(allDays); // nicer order in BYDAYS
        date = new SimpleDateFormat("yyyyMMdd");
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Recurrence() {
        super();
    }

    @Override
    public boolean hasProperty(final T component) {
        return null != component.getProperty("RRULE") || null != component.getProperty("X-MOZ-FAKED-MASTER");
    }

    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsRecurrenceType() && calendar.getRecurrenceDatePosition() == null && calendar.getRecurrencePosition() == 0;
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendar, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) throws ConversionError {
        if (calendar.isException()) {
            return;
        }
        switch (calendar.getRecurrenceType()) {
        case CalendarObject.DAILY:
            addDailyRecurrence(calendar, component);
            break;
        case CalendarObject.WEEKLY:
            addWeeklyRecurrence(index, calendar, component);
            break;
        case CalendarObject.MONTHLY:
            addMonthlyRecurrence(index, calendar, component);
            break;
        case CalendarObject.YEARLY:
            addYearlyRecurrence(index, calendar, component);
            break;
        default:
            return;
        }
    }

    private void addYearlyRecurrence(final int index, final U calendar, final T component) throws ConversionError {
        final StringBuilder recur = getRecurBuilder("YEARLY", calendar);
        if (calendar.containsDays()) {
            addDays("BYDAY", calendar.getDays(), recur);
            recur.append(";BYMONTH=").append(calendar.getMonth() + 1);
            recur.append(";BYSETPOS=").append(calendar.getDayInMonth());
        } else {
            recur.append(";BYMONTH=").append(calendar.getMonth() + 1).append(";BYMONTHDAY=").append(calendar.getDayInMonth());
        }
        addRRule(index, recur, component);
    }

    private void addMonthlyRecurrence(final int index, final U calendar, final T component) throws ConversionError {
        final StringBuilder recur = getRecurBuilder("MONTHLY", calendar);
        if (calendar.containsDays()) {
            addDays("BYDAY", calendar.getDays(), recur);
            int weekNo = calendar.getDayInMonth();
            if (5 == weekNo) {
                weekNo = -1;
            }
            recur.append(";BYSETPOS=").append(weekNo);
        } else if (calendar.containsDayInMonth()) {
            recur.append(";BYMONTHDAY=").append(calendar.getDayInMonth());
        }
        addRRule(index, recur, component);
    }

    private void addWeeklyRecurrence(final int index, final U calendar, final T component) throws ConversionError {
        final StringBuilder recur = getRecurBuilder("WEEKLY", calendar);
        if (calendar.containsDays()) {
            final int days = calendar.getDays();
            addDays("BYDAY", days, recur);
        }
        addRRule(index, recur, component);
    }

    private void addRRule(final int index, final StringBuilder recur, final T component) throws ConversionError {
        try {
            final RRule rrule = new RRule(new Recur(recur.toString()));
            component.getProperties().add(rrule);
        } catch (final ParseException e) {
            throw new ConversionError(index, ConversionError.Code.CANT_CREATE_RRULE, e, recur.toString());
        }
    }

    private void addDays(final String attr, final int days, final StringBuilder recur) {
        recur.append(';').append(attr).append('=');
        for (final int day : allDays) {
            if (day == (day & days)) {
                recur.append(reverseDays.get(Integer.valueOf(day))).append(',');
            }
        }
        recur.setLength(recur.length() - 1);
    }

    private StringBuilder getRecurBuilder(final String frequency, final U calendar) {
        final StringBuilder recur = new StringBuilder("FREQ=").append(frequency).append(";INTERVAL=").append(calendar.getInterval());
        if (calendar.containsOccurrence()) {
            recur.append(";COUNT=").append(calendar.getOccurrence());
        } else if (calendar.containsUntil()) {
            recur.append(";UNTIL=").append(getUntil(calendar).toString());
        }
        return recur;
    }

    private void addDailyRecurrence(final U calendar, final T component) {
        final Recur recur = getRecur("DAILY", calendar);
        recur.setInterval(calendar.getInterval());
        final RRule rrule = new RRule(recur);
        component.getProperties().add(rrule);
    }

    private Recur getRecur(final String frequency, final U calendar) {
        final Recur retval;
        if (calendar.containsOccurrence()) {
            retval = new Recur(frequency, calendar.getOccurrence());
        } else if (calendar.containsUntil()) {
            retval = new Recur(frequency, getUntil(calendar));
        } else {
            retval = new Recur(frequency, null);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        /*
         * check & preserve X-MOZ-FAKED-MASTER marker
         */
        if (null != component.getProperty("X-MOZ-FAKED-MASTER") && "1".equals(component.getProperty("X-MOZ-FAKED-MASTER").getValue())) {
            cObj.setProperty("com.openexchange.data.conversion.ical.recurrence.mozFakedMaster", Boolean.TRUE);
        }
        if (null == cObj.getStartDate()) {
            return;
        }
        final Calendar startDate = new GregorianCalendar();
        startDate.setTime(cObj.getStartDate());

        final PropertyList list = component.getProperties("RRULE");
        if (list.isEmpty()) {
            return;
        }
        if (list.size() > 1) {
            warnings.add(new ConversionWarning(index, "Only converting first recurrence rule, additional recurrence rules will be ignored."));
        }
        final Recur rrule = ((RRule) list.get(0)).getRecur();
        if ("DAILY".equalsIgnoreCase(rrule.getFrequency())) {
            if (null != rrule.getDayList() && 0 < rrule.getDayList().size()) {
                // used as "each weekday" by some clients: FREQ=DAILY;INTERVAL=1;WKST=SU;BYDAY=MO,TU,WE,TH,FR
                // save as 'weekly' type with daymask
                cObj.setRecurrenceType(CalendarObject.WEEKLY);
                setDays(index, cObj, rrule, startDate);
            } else {
                cObj.setRecurrenceType(CalendarObject.DAILY);
            }
            if (!rrule.getMonthList().isEmpty()) {
                throw new ConversionError(index, Code.BYMONTH_NOT_SUPPORTED);
            }
        } else if ("WEEKLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(CalendarObject.WEEKLY);
            setDays(index, cObj, rrule, startDate);
        } else if ("MONTHLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(CalendarObject.MONTHLY);
            setMonthDay(index, cObj, rrule, startDate);
        } else if ("YEARLY".equalsIgnoreCase(rrule.getFrequency())) {
            cObj.setRecurrenceType(CalendarObject.YEARLY);
            final NumberList monthList = rrule.getMonthList();
            if (!monthList.isEmpty()) {
                cObj.setMonth(((Integer) monthList.get(0)).intValue() - 1);
                setMonthDay(index, cObj, rrule, startDate);
            } else {
                cObj.setMonth(startDate.get(Calendar.MONTH));
                setMonthDay(index, cObj, rrule, startDate);
            }
        } else {
            warnings.add(new ConversionWarning(index, "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences"));
        }
        int interval = rrule.getInterval();
        if (interval == -1) {
            interval = 1;
        }
        cObj.setInterval(interval);
        final int count = rrule.getCount();
        if (-1 != count) {
            final int recurrenceCount = rrule.getCount();
            cObj.setRecurrenceCount(recurrenceCount);
            setOccurrenceIfNeededRecoveryFIXME(cObj, recurrenceCount);
        } else if (null != rrule.getUntil()) {
            cObj.setUntil(getUntil(rrule, component));
        }
    }

    private void setOccurrenceIfNeededRecoveryFIXME(final U cObj, final int recurrenceCount) {
        if (Appointment.class.isAssignableFrom(cObj.getClass())) {
            cObj.setOccurrence(recurrenceCount);
        }
    }

    private void setMonthDay(final int index, final CalendarObject cObj, final Recur rrule, final Calendar startDate) throws ConversionError {
        final NumberList monthDayList = rrule.getMonthDayList();
        if (monthDayList.isEmpty()) {
            final NumberList weekNoList = rrule.getWeekNoList();
            if (!weekNoList.isEmpty()) {
                int week = ((Integer) weekNoList.get(0)).intValue();
                if (week == -1) {
                    week = 5;
                }
                cObj.setDayInMonth(week); // Day in month stores week
                setDays(index, cObj, rrule, startDate);
            } else if (!rrule.getDayList().isEmpty()) {
                setWeekdayInMonth(index, cObj, rrule);
                setDayInMonthFromSetPos(index, cObj, rrule);
            } else {
                // Default to monthly series on specific day of month
                cObj.setDayInMonth(startDate.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            cObj.setDayInMonth(((Integer) monthDayList.get(0)).intValue());
        }
    }

    private void setDayInMonthFromSetPos(final int index, final CalendarObject obj, final Recur rrule) {
        if (!rrule.getSetPosList().isEmpty()) {
            int firstPos = (Integer) rrule.getSetPosList().get(0);
            if (firstPos == -1) {
                firstPos = 5;
            }
            obj.setDayInMonth(firstPos);
        }
    }

    private void setWeekdayInMonth(final int index, final CalendarObject cObj, final Recur rrule) throws ConversionError {
        final WeekDayList weekdayList = rrule.getDayList();
        if (!weekdayList.isEmpty()) {
            int days = 0;
            final int size = weekdayList.size();
            for (int i = 0; i < size; i++) {
                final WeekDay weekday = (WeekDay) weekdayList.get(i);
                final Integer day = weekdays.get(weekday.getDay());
                if (null == day) {
                    throw new ConversionError(index, "Unknown day: %s", weekday.getDay());
                }
                int offset = weekday.getOffset();
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

    private void setDays(final int index, final CalendarObject cObj, final Recur rrule, final Calendar startDate) throws ConversionError {
        final WeekDayList weekdayList = rrule.getDayList();
        if (weekdayList.isEmpty()) {
            final int day_of_week = startDate.get(Calendar.DAY_OF_WEEK);
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
            final int size = weekdayList.size();
            for (int i = 0; i < size; i++) {
                final WeekDay weekday = (WeekDay) weekdayList.get(i);

                final Integer day = weekdays.get(weekday.getDay());
                if (null == day) {
                    throw new ConversionError(index, "Unknown day: %s", weekday.getDay());
                }
                days |= day.intValue();
            }
            cObj.setDays(days);
        }
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
    private net.fortuna.ical4j.model.Date getUntil(U calendarObject) {
        if (calendarObject.containsUntil()) {
            /*
             * OX stores series end as date without time (00:00 UTC).
             */
            if (isWholeDay(calendarObject)) {
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
            effectiveUntilCalendar.set(
                utcUntilCalendar.get(Calendar.YEAR),
                utcUntilCalendar.get(Calendar.MONTH),
                utcUntilCalendar.get(Calendar.DAY_OF_MONTH),
                23, 59, 59);
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
     * Extracts the {@link java.util.Date} from the <code>UNTIL</code>
     * parameter of the supplied {@link Recur} instance, ready-to-use in
     * calendar objects. <p/>
     * The value is transformed to an UTC date (without time) implicitly,
     * based on the parent component's timezone. Doing so, this date represents
     * the date of the last occurrence, compatible with the server's internal
     * handling of the "until" property for calendar objects.
     *
     * @param recur the recurrence rule
     * @param component the parent component
     * @return the extracted until date
     * @see http://tools.ietf.org/html/rfc5545#section-3.3.10
     */
    private java.util.Date getUntil(Recur recur, T component) {
        if (null != recur && null != recur.getUntil()) {
            net.fortuna.ical4j.model.Date until = recur.getUntil();
            if (net.fortuna.ical4j.model.DateTime.class.isInstance(until)) {
                /*
                 * consider DATE-TIME value type with UTC time - determine date
                 * of the last occurrence in the appointment's timezone
                 */
                Calendar effectiveUntilCalendar = Calendar.getInstance(getTimeZone(component));
                effectiveUntilCalendar.setTime(until);
                /*
                 * determine OX until date based on the effective until date
                 */
                Calendar utcUntilCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcUntilCalendar.set(
                    effectiveUntilCalendar.get(Calendar.YEAR),
                    effectiveUntilCalendar.get(Calendar.MONTH),
                    effectiveUntilCalendar.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
                utcUntilCalendar.set(Calendar.MILLISECOND, 0);
                return utcUntilCalendar.getTime();
            } else {
                /*
                 * consider DATE value type - already in OX format
                 */
                return until;
            }
        }
        return null;
    }

    private boolean isWholeDay(U calendarObject) {
        return null != calendarObject && (Task.class.isInstance(calendarObject) ||
            (Appointment.class.isInstance(calendarObject) && ((Appointment)calendarObject).getFullTime()));
    }

    private TimeZone getTimeZone(U calendarObject) {
        String timeZoneID = null;
        if (null != calendarObject && Appointment.class.isInstance(calendarObject)) {
            timeZoneID = ((Appointment)calendarObject).getTimezone();
        }
        return java.util.TimeZone.getTimeZone(null != timeZoneID ? timeZoneID : "UTC");
    }

    private TimeZone getTimeZone(T component) {
        TimeZone timeZone = null;
        if (null != component) {
            DtStart dtStart = (DtStart)component.getProperty(Property.DTSTART);
            if (null != dtStart) {
                timeZone = dtStart.getTimeZone();
            }
        }
        return null != timeZone ? timeZone : TimeZone.getTimeZone("UTC");
    }

}
