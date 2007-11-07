/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
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
 *     Copyright (C) 2004-2007 Open-Xchange, Inc.
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


package com.openexchange.groupware.calendar.recurrence;

import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.CalendarObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * RecurringCalculation
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class RecurringCalculation {
    
    private int recurring_type = -1; // cdao.getRecurrenceType()
    private int recurring_interval = -1; // cdao.getInterval()
    private int recurring_days = -1; //  cdao.getDays()
    private int recurring_day_in_month = -1 ; // cdao.getDayInMonth()
    private int recurring_month = -1; // cdao.getMonth()
    
    private String change_exceptions = null;
    private String delete_exceptions = null;
    
    private boolean contains_occurrence = false;
    private int occurrence_value; // occurrence
    
    private boolean contains_days = false;
    private boolean contains_day_in_month = false;
    private boolean contains_month = false;
    private boolean contains_until = false;
    
    private int recurrence_calculator;
    
    private long diff;
    
    private long s; // recurring_start - aka timestampfield01
    private long sr; // real recurring_start (normalized)
    private long e; // recurring_end - aka timestampfield02
    private long until; // real recurring_end (normalized)
    
    private long range_start = 0;
    private long range_end = 0;
    private int pos = 0;
    
    private String calc_timezone = "UTC";
    
    private int PMAXTC = 999;
    
    /* Internal */
    
    private static final int days_int[] = { CalendarObject.SUNDAY, CalendarObject.MONDAY, CalendarObject.TUESDAY, CalendarObject.WEDNESDAY, CalendarObject.THURSDAY, CalendarObject.FRIDAY, CalendarObject.SATURDAY };
    
    private int first_day_of_week = Calendar.MONDAY;
    private boolean calc_until = false; // what the hell is this for?
    
    /**
     * <code>RecurringCalculation</code>
     *
     * @param recurring_type a <code>int</code>
     * @param recurring_interval a <code>int</code>
     * @param diff a <code>long</code>
     */
    public RecurringCalculation(int recurring_type, int recurring_interval, int recurrence_calculator) {
        this.recurring_type = recurring_type;
        this.recurring_interval = recurring_interval;
        this.recurrence_calculator = recurrence_calculator;
    }
    
    /**
     * <code>setDays</code>
     * Sets the days for the recurring calculation.
     *
     * @param recurring_days a <code>int</code>
     */
    public void setDays(int recurring_days) {
        this.recurring_days = recurring_days;
        contains_days = true;
    }
    
    /**
     * <code>setDayInMonth</code>
     * Sets the day_in_month for the recurring calculation.
     *
     * @param recurring_day_in_month a <code>int</code>
     */
    public void setDayInMonth(int recurring_day_in_month) {
        this.recurring_day_in_month = recurring_day_in_month;
        contains_day_in_month = true;
    }
    
    /**
     * <code>setMonth</code>
     * Sets the month for the recurring calculation.
     *
     * @param recurring_month a <code>int</code>
     */
    public void setMonth(int recurring_month) {
        this.recurring_month = recurring_month;
        contains_month = true;
    }
    
    /**
     * <code>setFirstDayOfWeek</code>
     * Sets the first day of week. Standard value is Calendar.MONDAY
     *
     * @param first_day_of_week a <code>int</code>
     */
    public void setFirstDayOfWeek(int first_day_of_week) {
        this.first_day_of_week = first_day_of_week;
    }
    
    /**
     * <code>setMaxCalculations</code>
     * Sets the max. numbers of results. If this number is reached
     * the calculation ends even more results can be calculated.
     * The default value is 999.
     *
     * @param max_calc_value a <code>int</code>
     */
    public void setMaxCalculation(int max_calc_value) {
        this.PMAXTC = max_calc_value;
    }
    
    /**
     * <code>setCalculationTimeZone</code>
     * Sets the timezone for calculation. Standard value is UTC.
     * All normal appointments should be calculated in the users
     * timezone. For whole day appointments the standard should be
     * used.
     *
     * @param calc_timezone a <code>int</code>
     */
    public void setCalculationTimeZone(String calc_timezone) {
        this.calc_timezone = calc_timezone;
    }
    
    /**
     * <code>setOccurrence</code>
     * If an occurrence should be used this value must be set here.
     * An intetrnal flag indicates that an occurrence was set. This
     * internal flag can not be removed/changes. If you have set an
     * occurrence the calculation with this value!.
     *
     * @param occurrence_value a <code>int</code>
     */
    public void setOccurrence(int occurrence_value) {
        this.occurrence_value = occurrence_value;
        contains_occurrence = true;
    }
    
    /**
     * <code>setCalculationPosition</code>
     * To calculate a specific position inside of recurring event
     * this position can be defined with this method.
     * Please make sure that you did not define a range
     * for the calculationto get travceable results.
     *
     * @param pos a <code>int</code>
     */
    public void setCalculationPosition(int pos) {
        this.pos = pos;
    }
    
    /**
     * <code>setRange</code>
     * To calculate a specific range (between) you can
     * define this range with this method.
     * Please make sure that you did not define a position
     * for the calculation to get travceable results.
     *
     * @param range_start a <code>long</code>
     * @param range_end a <code>long</code>
     */
    public void setRange(long range_start, long range_end) {
        this.range_start = range_start;
        this.range_end = range_end;
    }
    
    /**
     * <code>setExceptions</code>
     * If exceptions should be considered a comma
     * separated String is expected here. Ignoring
     * exceptions can be achieved by setting null values.
     *
     * @param change_exceptions a <code>String</code>
     * @param delete_exceptions a <code>String</code>
     */
    public void setExceptions(String change_exceptions, String delete_exceptions) {
        this.change_exceptions = change_exceptions;
        this.delete_exceptions = delete_exceptions;
    }
    
    /**
     * <code>setStartAndEndTime</code>
     * Sets the start and end time (aka timestampfield01
     * and timestampfield02).
     *
     * @param start a <code>long</code>
     * @param end a <code>long</code>
     */
    public void setStartAndEndTime(long start, long end) {
        this.s = start;
        this.e = end;
        final long c1 = start % CalendarRecurringCollection.MILLI_DAY;
        final long c2 = end % CalendarRecurringCollection.MILLI_DAY;
        diff = Math.abs(c2-c1);
    }
    
    /**
     * <code>setRecurringStart</code>
     * Sets the recurring start (UTC long without time)
     *
     * @param rec_start a <code>long</code>
     */
    public void setRecurringStart(long rec_start) {
        this.sr = rec_start;
    }
    
    /**
     * <code>setUntil</code>
     * Sets the recurring end (UTC long without time)
     *
     * @param until a <code>long</code>
     */
    public void setUntil(long until) {
        this.e = until;
        this.until = until;
        contains_until = true;
    }
    
    private void checkValues() throws RecurringException {
        if (!contains_until)  {
            e = (e + (CalendarRecurringCollection.MILLI_YEAR * 99));
            this.until = e;
        }
    }
    
    public RecurringResults calculateRecurrence() throws RecurringException {
        
        checkValues();
        
        if (recurring_type == CalendarObject.DAILY) {
            return calculateDaily();
        } else if (recurring_type == CalendarObject.WEEKLY) {
            return calculateWeekly();
        } else if (recurring_type == CalendarObject.MONTHLY) {
            return calculateMonthly();
        } else if (recurring_type == CalendarObject.YEARLY) {
            return calculateYearly();
        }
        return null; // TODO: throw error
    }
    
    
    private final RecurringResults calculateDaily() throws RecurringException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_INTERVAL, recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        long sst = sr;
        
        rs = new RecurringResults();
        calc.setTimeInMillis(s);
        while (sr <= e) {
            if (s >= sst && sr <= e) {
                if (((range_start == 0 && range_end == 0 && pos == 0) || (s >= range_start && s <= range_end) || pos == ds_count)
                && (!CalendarRecurringCollection.isException(sr, change_exceptions, delete_exceptions))) {
                    if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                        CalendarRecurringCollection.fillMap(rs, calc.getTimeInMillis(), diff, recurrence_calculator, ds_count);
                    }
                    if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                        break;
                    }
                }
                ds_count++;
            }
            s += recurring_interval*CalendarRecurringCollection.MILLI_DAY;
            sr += recurring_interval*CalendarRecurringCollection.MILLI_DAY;
            calc.add(Calendar.DAY_OF_MONTH, recurring_interval);
        }
        return rs;
    }
    
    private final RecurringResults calculateWeekly() throws RecurringException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_INTERVAL, recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        long sst = sr;
        
        
        calc.setTimeInMillis(s);
        calc.setFirstDayOfWeek(Calendar.MONDAY);
        calc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Set to first day of week for calculation
        s = calc.getTimeInMillis();
        final Calendar calc_weekly = (Calendar)calc.clone();
        
        final int days[] = new int[7];
        int c = 0;
        int u = recurring_days;
        for (int x = days_int.length-1; x >= 0; x--) {
            if (u >= days_int[x]) {
                switch (days_int[x]) {
                    case CalendarObject.SATURDAY:
                        days[c++] = 5;
                        break;
                    case CalendarObject.FRIDAY:
                        days[c++] = 4;
                        break;
                    case CalendarObject.THURSDAY:
                        days[c++] = 3;
                        break;
                    case CalendarObject.WEDNESDAY:
                        days[c++] = 2;
                        break;
                    case CalendarObject.TUESDAY:
                        days[c++] = 1;
                        break;
                    case CalendarObject.MONDAY:
                        days[c++] = 0;
                        break;
                    case CalendarObject.SUNDAY:
                        days[c++] = 6;
                        break;
                    default:
                        throw new RecurringException(RecurringException.UNKOWN_DAYS_VALUE, days_int[x]);
                }
                u-=days_int[x];
            }
        }
        rs = new RecurringResults();
        long range = 0;
        final int r[] = new int[c];
        System.arraycopy(days, 0, r, 0, c);
        Arrays.sort(r);
        if (contains_occurrence) {
            // find highes value and compare if sst.getDayOfWeek >= highest value. If not, we need to change e !!!
            int hi = r[c-1];
            calc.setTimeInMillis(sst);
            int sd = calc.get(Calendar.DAY_OF_WEEK);
            if (hi < sd) {
                e = (e+(CalendarRecurringCollection.MILLI_DAY*(sd-hi)));
            }
        }
        loop: while (sr <= e) {
            for (int a = 0; a < c; a++) {
                calc.setTimeInMillis(s);
                calc.add(Calendar.DAY_OF_MONTH, r[a]);
                range = calc.getTimeInMillis();
                if (range >= sst && sr <= e) {
                    if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                    && (!CalendarRecurringCollection.isException(range, change_exceptions, delete_exceptions))) {
                        //if (!isException(range, change_exceptions, delete_exceptions)) {
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            CalendarRecurringCollection.fillMap(rs, range, diff, recurrence_calculator, ds_count);
                        }
                        if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break loop;
                        }
                        //}
                    }
                    ds_count++;
                }
            }
            calc_weekly.add(Calendar.WEEK_OF_YEAR, recurring_interval);
            s = calc_weekly.getTimeInMillis();
            sr += (CalendarRecurringCollection.MILLI_WEEK*recurring_interval);
            calc.add(Calendar.WEEK_OF_YEAR, recurring_interval);
        }
        
        return rs;
    }
    
    private final RecurringResults calculateMonthly() throws RecurringException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_INTERVAL, recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        long sst = sr;
        
        int a = recurring_days;
        final int monthly = recurring_interval;
        final int day_or_type = recurring_day_in_month;
        
        rs = new RecurringResults();
        
        if (day_or_type == 0) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_MONTLY_INTERVAL, day_or_type);
        }
        if (monthly <= 0) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_MONTLY_INTERVAL_2, monthly);
        }
        if (!contains_days) {
            if (contains_occurrence) {
                e += CalendarRecurringCollection.MILLI_MONTH;
            }
            while (s <= e) {
                calc.setTimeInMillis(s);
                final int month = calc.get(Calendar.MONTH);
                final int year = calc.get(Calendar.YEAR);
                calc.set(Calendar.YEAR, year);
                calc.set(Calendar.MONTH, month);
                if (calc.getActualMaximum(Calendar.DAY_OF_MONTH) >= day_or_type) {
                    calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                    s = calc.getTimeInMillis();
                    calc.setFirstDayOfWeek(2); // TODO: Make this configurable
                    final long range = calc.getTimeInMillis();
                    if (range >= sst && range <= e) {
                        if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                        && (!CalendarRecurringCollection.isException(s, change_exceptions, delete_exceptions))) {
                            //if (!isException(s, change_exceptions, delete_exceptions)) {
                            if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                                CalendarRecurringCollection.fillMap(rs, s, diff, recurrence_calculator, ds_count);
                            }
                            if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                                break;
                            }
                            //}
                        }
                        ds_count++;
                    }
                }
                calc.add(Calendar.MONTH, monthly);
                s = calc.getTimeInMillis();
            }
        } else {
            a = getDay(a);
            
                /*
                 * MONDAY - SUNDAY
                 * WEEKDAY
                 * WEEKENDDAY
                 * DAY
                 *
                 * day_or_type
                 *
                 * 1 == first
                 * 2 == second
                 * 3 = third
                 * 4 = forth
                 * 5 = last
                 *
                 */
            
            if (a == -1) {
                throw new RecurringException(RecurringException.RECURRING_MISSING_MONTLY_DAY, a);
            }
            if (day_or_type < 1 || day_or_type > 5) {
                throw new RecurringException(RecurringException.RECURRING_MISSING_MONTLY_DAY_2, day_or_type);
            }
            
            final Calendar helper = (Calendar) calc.clone();
            if (contains_occurrence) {
                e += CalendarRecurringCollection.MILLI_MONTH;
            }
            while (s <= e) {
                calc.setTimeInMillis(s);
                helper.setTimeInMillis(s);
                
                if (day_or_type < 5) {
                    // first until forth
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        for (int x = 0; x < 13; x++) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                break;
                            }
                            calc.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calc.add(Calendar.WEEK_OF_MONTH, day_or_type-1);
                    } else {
                        // DAY, WEEKDAY OR WEEKENDDAY
                        if (a == CalendarObject.DAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                        } else if (a == CalendarObject.WEEKDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, 2 + (day_or_type-1));
                            } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, 1 + (day_or_type-1));
                            }
                        } else if (a == CalendarObject.WEEKENDDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            final int x  = Calendar.DAY_OF_WEEK;
                            if (x != Calendar.SATURDAY || x == Calendar.SUNDAY) {
                                calc.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // TODO: Check if we need to calculate this by outself
                                calc.add(Calendar.DAY_OF_MONTH, + (((day_or_type-1) * day_or_type) * 7));
                            }
                        }
                    }
                } else {
                    // everything with LAST
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        calc.add(Calendar.MONTH, 1);
                        calc.add(Calendar.DAY_OF_MONTH, -1);
                        for (int x = 0; x < 7; x++) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                break;
                            }
                            calc.add(Calendar.DAY_OF_MONTH, -1);
                        }
                    } else {
                        // DAY, WEEKDAY OR WEEKENDDAY
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        calc.add(Calendar.MONTH, 1);
                        calc.add(Calendar.DAY_OF_MONTH, -1); // coverage of DAY
                        if (a == CalendarObject.WEEKDAY) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, -2);
                            } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        } else if (a == CalendarObject.WEEKENDDAY) {
                            for (int x = 0; x < 7; x++) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        }
                    }
                }
                final  long range = calc.getTimeInMillis();
                s = calc.getTimeInMillis();
                if (range >= sst && range <= e) {
                    if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                    && (!CalendarRecurringCollection.isException(s, change_exceptions, delete_exceptions))) {
                        //if (!isException(s, change_exceptions, delete_exceptions)) {
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            CalendarRecurringCollection.fillMap(rs, s, diff, recurrence_calculator, ds_count);
                        }
                        if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break;
                        }
                        //}
                    }
                    ds_count++;
                }
                helper.add(Calendar.MONTH, recurring_interval);
                s = helper.getTimeInMillis();
            }
        }
        
        
        return rs;
    }
    
    private final RecurringResults calculateYearly() throws RecurringException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_INTERVAL, recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        long sst = sr;
        
        rs = new RecurringResults();
        
        int a = recurring_days;
        final int day_or_type = recurring_day_in_month;
        final int month = recurring_month;
        
        if (day_or_type == 0) {
            throw new RecurringException(RecurringException.RECURRING_MISSING_YEARLY_INTERVAL, day_or_type);
        }
        
        if (!contains_days) {
            while (s <= e) {
                calc.setTimeInMillis(s);
                calc.set(Calendar.YEAR, calc.get(Calendar.YEAR));
                calc.set(Calendar.MONTH, month);
                if (calc.getActualMaximum(Calendar.DAY_OF_MONTH) >= day_or_type) {
                    calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                    s = calc.getTimeInMillis();
                    calc.setFirstDayOfWeek(2); // TODO: Make this configurable
                    final long range = calc.getTimeInMillis();
                    if (range >= sst && range <= e) {
                        if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                        && (!CalendarRecurringCollection.isException(s, change_exceptions, delete_exceptions))) {
                            //if (!isException(s, change_exceptions, delete_exceptions)) {
                            if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                                CalendarRecurringCollection.fillMap(rs, s, diff, recurrence_calculator, ds_count);
                            }
                            if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                                break;
                            }
                            //}
                        }
                        ds_count++;
                    }
                }
                calc.add(Calendar.YEAR, recurring_interval);
                s = calc.getTimeInMillis();
            }
        } else {
            a = getDay(a);
            
                /*
                 * MONDAY - SUNDAY
                 * WEEKDAY
                 * WEEKENDDAY
                 * DAY
                 *
                 * day_or_type
                 *
                 * 1 == first
                 * 2 == second
                 * 3 = third
                 * 4 = forth
                 * 5 = last
                 *
                 */
            
            if (a == -1) {
                throw new RecurringException(RecurringException.RECURRING_MISSING_YEARLY_DAY, a);
            }
            if (day_or_type < 1 || day_or_type > 5) {
                throw new RecurringException(RecurringException.RECURRING_MISSING_YEARLY_TYPE, day_or_type);
            }
            
            final Calendar helper = (Calendar) calc.clone();
            helper.setTimeInMillis(s);
            helper.set(Calendar.MONTH, month);
            helper.set(Calendar.WEEK_OF_MONTH, 1);
            helper.set(Calendar.DAY_OF_MONTH, 1);
            while (s <= e) {
                calc.setTimeInMillis(s);
                if (day_or_type < 5) {
                    // first until forth
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        for (int x = 0; x < 13; x++) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                break;
                            }
                            calc.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        calc.add(Calendar.WEEK_OF_MONTH, day_or_type-1);
                    } else {
                        // DAY, WEEKDAY OR WEEKENDDAY
                        if (a == CalendarObject.DAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.add(Calendar.DAY_OF_MONTH, 0-day_or_type);
                        } else if (a == CalendarObject.WEEKDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, 2 + (day_or_type-1));
                            } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, 1 + (day_or_type-1));
                            }
                        } else if (a == CalendarObject.WEEKENDDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            final int x  = Calendar.DAY_OF_WEEK;
                            if (x != Calendar.SATURDAY || x == Calendar.SUNDAY) {
                                calc.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // TODO: Check if we need to calculate this by outself
                                calc.add(Calendar.DAY_OF_MONTH, + (((day_or_type-1) * day_or_type) * 7));
                            }
                        }
                    }
                } else {
                    // everything with LAST
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        calc.add(Calendar.MONTH, 1);
                        calc.add(Calendar.DAY_OF_MONTH, -1);
                        for (int x = 0; x < 7; x++) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                break;
                            }
                            calc.add(Calendar.DAY_OF_MONTH, -1);
                        }
                    } else {
                        // DAY, WEEKDAY OR WEEKENDDAY
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        calc.add(Calendar.MONTH, 1);
                        calc.add(Calendar.DAY_OF_MONTH, -1); // coverage of DAY
                        if (a == CalendarObject.WEEKDAY) {
                            if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, -2);
                            } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        } else if (a == CalendarObject.WEEKENDDAY) {
                            for (int x = 0; x < 7; x++) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        }
                    }
                }
                final long range = calc.getTimeInMillis();
                s = calc.getTimeInMillis();
                
                if (range >= sst && range <= e) {
                    if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                    && (!CalendarRecurringCollection.isException(s, change_exceptions, delete_exceptions))) {
                        //if (!isException(s, change_exceptions, delete_exceptions)) {
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            CalendarRecurringCollection.fillMap(rs, s, diff, recurrence_calculator, ds_count);
                        }
                        if (ds_count > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break;
                        }
                        //}
                    }
                    ds_count++;
                }
                helper.add(Calendar.YEAR, recurring_interval);
                s = helper.getTimeInMillis();
            }
        }
        
        
        return rs;
    }
    
    private static int getDay(final int cd) throws RecurringException {
        int ret = -1;
        switch (cd) {
            case CalendarObject.SATURDAY:
                ret = Calendar.SATURDAY;
                break;
            case CalendarObject.FRIDAY:
                ret = Calendar.FRIDAY;
                break;
            case CalendarObject.THURSDAY:
                ret = Calendar.THURSDAY;
                break;
            case CalendarObject.WEDNESDAY:
                ret = Calendar.WEDNESDAY;
                break;
            case CalendarObject.TUESDAY:
                ret = Calendar.TUESDAY;
                break;
            case CalendarObject.MONDAY:
                ret = Calendar.MONDAY;
                break;
            case CalendarObject.SUNDAY:
                ret = Calendar.SUNDAY;
                break;
            case CalendarObject.DAY:
                ret = CalendarObject.DAY;
                break;
            case CalendarObject.WEEKDAY:
                ret = CalendarObject.WEEKDAY;
                break;
            case CalendarObject.WEEKENDDAY:
                ret = CalendarObject.WEEKENDDAY;
                break;
            default:
                throw new RecurringException(RecurringException.UNKOWN_DAYS_VALUE, cd);
        }
        return ret;
    }
    
}
