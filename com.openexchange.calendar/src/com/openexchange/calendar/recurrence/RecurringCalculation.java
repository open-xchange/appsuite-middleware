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


package com.openexchange.calendar.recurrence;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.calendar.RecurringResults;
import com.openexchange.calendar.Tools;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.java.Strings;

/**
 * RecurringCalculation
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class RecurringCalculation {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RecurringCalculation.class);

    private final int recurring_type; // cdao.getRecurrenceType()
    private final int recurring_interval; // cdao.getInterval()
    private int recurring_days = -1; //  cdao.getDays()
    private int recurring_day_in_month = -1 ; // cdao.getDayInMonth()
    private int recurring_month = -1; // cdao.getMonth()

    private Set<Long> changeExceptions;
    private Set<Long> deleteExceptions;

    private boolean contains_occurrence;
    private int occurrence_value; // occurrence

    private boolean contains_days;
    private boolean contains_day_in_month;
    private boolean contains_month;
    private boolean contains_until;

    private final int recurrence_calculator;

    private long diff;

    private long start_of_series; // recurring_start - aka timestampfield01
    private long normalized_start_of_series; // real recurring_start (normalized)
    private long end_of_series; // recurring_end - aka timestampfield02
    private long until; // real recurring_end (normalized)

    private long range_start;
    private long range_end;
    private int pos;

    private String calc_timezone = "UTC";

    private int PMAXTC = 999;

    private final CalendarCollection recColl;

    /**
     * set true if a recurrence calculation should find also appointments which are not completely surrounded by the given range.
     * Example:
     * an occurrence which starts at 08:00 and ends at 12:00 will be found, if the range is 10:00 - 11:00
     */
    private boolean greedy = false;

    // Maximum number of calculations PMAXTC (maximum number of ocurrences) * 100 ops per ourrence sounds appropriate
    private int TTL = 999 * 100;
    private int operationCounter;

    /* Internal */

    private static final int days_int[] = { CalendarObject.SUNDAY, CalendarObject.MONDAY, CalendarObject.TUESDAY, CalendarObject.WEDNESDAY, CalendarObject.THURSDAY, CalendarObject.FRIDAY, CalendarObject.SATURDAY };

    private int first_day_of_week = Calendar.MONDAY;

    private final boolean calc_until = false; // what the hell is this for?

    private final String getState() {
        final StringBuilder builder = new StringBuilder(1024);
        builder.append("Recurring Calculation State:\n");
        builder.append("recurring_type: ").append(recurring_type).append('\n');
        builder.append("recurring_interval: ").append(recurring_interval).append('\n');
        builder.append("recurring_days: ").append(recurring_days).append('\n');
        builder.append("recurring_day_in_month: ").append(recurring_day_in_month).append('\n');
        builder.append("recurring_month: ").append(recurring_month).append('\n');
        builder.append('\n');
        builder.append("change_exceptions: ").append(changeExceptions).append('\n');
        builder.append("delete_exceptions: ").append(deleteExceptions).append('\n');
        builder.append('\n');
        builder.append("contains_occurrence: ").append(contains_occurrence).append('\n');
        builder.append("occurrence_value: ").append(occurrence_value).append('\n');
        builder.append('\n');
        builder.append("contains_days: ").append(contains_days).append('\n');
        builder.append("contains_day_in_month: ").append(contains_day_in_month).append('\n');
        builder.append("contains_month: ").append(contains_month).append('\n');
        builder.append("contains_until: ").append(contains_until).append('\n');
        builder.append('\n');
        builder.append("recurrence_calculator: ").append(recurrence_calculator).append('\n');
        builder.append('\n');
        builder.append("diff: ").append(diff).append('\n');
        builder.append('\n');
        builder.append("start_of_series: ").append(start_of_series).append('\n');
        builder.append("normalized_start_of_series: ").append(normalized_start_of_series).append('\n');
        builder.append("end_of_series: ").append(end_of_series).append('\n');
        builder.append("until: ").append(until).append('\n');
        builder.append('\n');
        builder.append("range_start: ").append(range_start).append('\n');
        builder.append("range_end: ").append(range_end).append('\n');
        builder.append("pos: ").append(pos).append('\n');
        builder.append('\n');
        builder.append("calc_timezone: ").append(calc_timezone).append('\n');
        builder.append('\n');
        builder.append("PMAXTC: ").append(PMAXTC).append('\n');
        builder.append('\n');
        builder.append("TTL: ").append(TTL).append('\n');
        builder.append("operationCounter: ").append(operationCounter).append('\n');
        return builder.toString();
    }

    /**
     * <code>RecurringCalculation</code>
     *
     * @param recurring_type a <code>int</code>
     * @param recurring_interval a <code>int</code>
     * @param diff a <code>long</code>
     */
    public RecurringCalculation(final int recurring_type, final int recurring_interval, final int recurrence_calculator) {
        super();
        this.recurring_type = recurring_type;
        this.recurring_interval = recurring_interval;
        this.recurrence_calculator = recurrence_calculator;
        this.recColl = new CalendarCollection();
    }

    /**
     * <code>setDays</code>
     * Sets the days for the recurring calculation.
     *
     * @param recurring_days a <code>int</code>
     */
    public void setDays(final int recurring_days) {
        if (recurring_days == 0) {
            return;
        }
        this.recurring_days = recurring_days;
        contains_days = true;
    }

    /**
     * <code>setDayInMonth</code>
     * Sets the day_in_month for the recurring calculation.
     *
     * @param recurring_day_in_month a <code>int</code>
     */
    public void setDayInMonth(final int recurring_day_in_month) {
        this.recurring_day_in_month = recurring_day_in_month;
        contains_day_in_month = true;
    }

    /**
     * <code>setMonth</code>
     * Sets the month for the recurring calculation.
     *
     * @param recurring_month a <code>int</code>
     */
    public void setMonth(final int recurring_month) {
        this.recurring_month = recurring_month;
        contains_month = true;
    }

    /**
     * <code>setFirstDayOfWeek</code>
     * Sets the first day of week. Standard value is Calendar.MONDAY
     *
     * @param first_day_of_week a <code>int</code>
     */
    public void setFirstDayOfWeek(final int first_day_of_week) {
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
    public void setMaxCalculation(final int max_calc_value) {
        this.PMAXTC = max_calc_value;
    }

    public void setGreedy(final boolean greedy) {
        this.greedy = greedy;
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
    public void setCalculationTimeZone(final String calc_timezone) {
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
    public void setOccurrence(final int occurrence_value) {
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
    public void setCalculationPosition(final int pos) {
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
    public void setRange(final long range_start, final long range_end) {
        this.range_start = range_start;
        this.range_end = range_end;
    }

    /**
     * Specified strings should be comma-separated strings of <code>long</code>
     * values. Ignoring exceptions can be achieved by setting <code>null</code>
     * values.
     *
     * @param changeExceptions The comma-separated strings of change exceptions
     * @param deleteExceptions The comma-separated strings of delete exceptions
     */
    public void setExceptions(final String changeExceptions, final String deleteExceptions) {
        if (null == changeExceptions || changeExceptions.length() == 0) {
            this.changeExceptions = Collections.emptySet();
        } else {
            final String[] sa = Strings.splitByComma(changeExceptions);
            this.changeExceptions = new HashSet<Long>(sa.length);
            for (final String sLong : sa) {
                this.changeExceptions.add(Long.valueOf(sLong));
            }
        }
        if (null == deleteExceptions || deleteExceptions.length() == 0) {
            this.deleteExceptions = Collections.emptySet();
        } else {
            final String[] sa = Strings.splitByComma(deleteExceptions);
            this.deleteExceptions = new HashSet<Long>(sa.length);
            for (final String sLong : sa) {
                this.deleteExceptions.add(Long.valueOf(sLong));
            }
        }
    }

    /**
     * <code>setStartAndEndTime</code>
     * Sets the start and end time (aka timestampfield01
     * and timestampfield02).
     *
     * @param start a <code>long</code>
     * @param end a <code>long</code>
     */
    public void setStartAndEndTime(final long start, final long end) {
        this.start_of_series = start;
        this.end_of_series = end;
        diff = Math.abs((end - start) % Constants.MILLI_DAY);
    }

    /**
     * <code>setRecurringStart</code>
     * Sets the recurring start (UTC long without time)
     *
     * @param rec_start a <code>long</code>
     */
    public void setRecurringStart(final long rec_start) {
        this.normalized_start_of_series = rec_start;
    }

    /**
     * <code>setUntil</code>
     * Sets the recurring end (UTC long without time)
     *
     * @param until a <code>long</code>
     */
    public void setUntil(final long until) {
    	if (end_of_series > 0) {
    		/*
    		 * Add day offset
    		 */
    		end_of_series = until + (end_of_series % Constants.MILLI_DAY);
    	} else {
    		end_of_series = until;
    	}
        //this.end_of_series = until;
        this.until = until;
        contains_until = true;
    }

    private void checkValues() {
		if (!contains_until) {
			Calendar tmp = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
			tmp.setTimeInMillis(normalized_start_of_series);
			tmp.add(Calendar.YEAR, recurring_type == CalendarObject.YEARLY ? CalendarCollectionService.MAX_OCCURRENCESE : 99);
			end_of_series = tmp.getTimeInMillis();
			this.until = end_of_series;
		}
	}

    private final void increaseCalculationCounter() throws OXException {
        if (TTL > 0) {
            operationCounter++;
            if (operationCounter > TTL) {
                OXException oxException = OXCalendarExceptionCodes.RECURRENCE_PATTERN_TOO_COMPLEX.create();
                Throwable t = oxException.fillInStackTrace();
                LOG.error(getState(), t);
                throw oxException;
            }
        }
    }

    /**
     * Checks if this recurring calculation cycle has boundaries; meaning range set and/or calculation position as well.
     * <p>
     * If boundaries are set, the calculation cycle proceeds until any boundary is violated. Otherwise the calculation cycle proceeds until
     * the recurrence's (virtual) end is reached
     *
     * @return <code>true</code> if this recurring calculation cycle has no boundaries; otherwise <code>false</code>
     */
    private boolean hasBoundaries() {
        return (range_start != 0 || range_end != 0 || pos != 0);
    }

    /**
     * Starts the recurring calculation cycle providing its results in returned instance of {@link RecurringResults}.
     *
     * @return The calculated occurrences kept by returned instance of {@link RecurringResults} or <code>null</code> if recurrence type is
     *         unknown.
     * @throws OXException
     */
    public RecurringResults calculateRecurrence() throws OXException {

        checkValues();
        switch (recurring_type) {
        case CalendarObject.DAILY:
            return calculateDaily();
        case CalendarObject.WEEKLY:
            return calculateWeekly();
        case CalendarObject.MONTHLY:
            return calculateMonthly();
        case CalendarObject.YEARLY:
            return calculateYearly();
        case CalendarObject.NO_RECURRENCE:
            return null;
        default:
            throw OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_TYPE.create(recurring_type);
        }
    }

    private final RecurringResults calculateDaily() throws OXException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_DAILY_INTERVAL.create(recurring_interval);
        }

        int ds_count = 1;
        final long sst = normalized_start_of_series;

        rs = new RecurringResults();

        final Calendar calc = Calendar.getInstance(Tools.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        calc.setTimeInMillis(start_of_series);

        {
			final int zoneHourOffset = (Tools.getTimeZone(calc_timezone).getOffset(start_of_series) / (int) Constants.MILLI_HOUR);
			if (zoneHourOffset != 0) {
				final int compare = (calc.get(Calendar.HOUR_OF_DAY) - (zoneHourOffset));
				if (compare >= 24) {
					/*
					 * Zone offset causes to increment day in month; therefore
					 * add one day to end
					 */
					end_of_series = end_of_series + Constants.MILLI_DAY;
				} else if (compare < 0) {
					/*
					 * Zone offset causes to decrement day in month; therefore
					 * subtract one day from end
					 */
					end_of_series = end_of_series - Constants.MILLI_DAY;
				}
			}
		}

        long end_of_calculation = end_of_series;
        if(range_end  != 0) {
            end_of_calculation = Math.min(end_of_series, range_end);
        }

        final boolean boundaries = hasBoundaries();

        int hoursJumped = 0; // This is to detect jumps in the time. Happens during timezone offset transitions.
        while (normalized_start_of_series <= end_of_calculation) {
            increaseCalculationCounter();
            if (start_of_series >= sst && normalized_start_of_series <= end_of_series) {
                final long start_of_occurrence = calc.getTimeInMillis();
                final long end_of_occurrence = start_of_occurrence + diff + recurrence_calculator * Constants.MILLI_DAY;
                if (((!boundaries) || (start_of_occurrence < range_end && end_of_occurrence > range_start) || pos == ds_count)
                    && (!recColl.isException(normalized_start_of_series, changeExceptions, deleteExceptions))) {
                    if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                        recColl.fillMap(rs, calc.getTimeInMillis(), diff, recurrence_calculator, ds_count);
                        if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break;
                        }
                    }
                }
                ds_count++;
            }
            start_of_series += recurring_interval*Constants.MILLI_DAY;
            normalized_start_of_series += recurring_interval*Constants.MILLI_DAY;
            int tmp = calc.get(Calendar.HOUR_OF_DAY);
            calc.add(Calendar.DAY_OF_MONTH, recurring_interval);
            if (hoursJumped != 0) {
                calc.add(Calendar.HOUR_OF_DAY, -hoursJumped);
                hoursJumped = 0;
            } else {
                hoursJumped = calc.get(Calendar.HOUR_OF_DAY) - tmp;
            }
        }
        return rs;
    }

    private final RecurringResults calculateWeekly() throws OXException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_DAILY_INTERVAL.create(recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(Tools.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        final long sst = normalized_start_of_series;


        calc.setTimeInMillis(start_of_series);
        calc.setFirstDayOfWeek(Calendar.MONDAY);
        calc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Set to first day of week for calculation
        start_of_series = calc.getTimeInMillis();
        final Calendar calc_weekly = (Calendar)calc.clone();

        final int days[] = new int[7];
        int c = 0;
        int u = recurring_days;
        for (int x = days_int.length-1; x >= 0; x--) {
            increaseCalculationCounter();
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
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_DAY.create(days_int[x]);
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
            // bishoph: find highest value and compare if sst.getDayOfWeek >= highest value. If not, we need to change end_of_series !!!
            // marcus: the calculated until date must be shifted in some cases.
            // look at a weekly recurrence. start day may be a tuesday but you
            // want this recurrence to be on thursday. the until has been calculated
            // to a tuesday and is now shifted to the thursday.
            final int hi = r[c-1];
            calc.setTimeInMillis(sst);
            final int sd = calc.get(Calendar.DAY_OF_WEEK);
            if (hi < sd) {
                end_of_series = (end_of_series +(Constants.MILLI_DAY*(sd-hi)));
            }
        }
        final boolean exceeds = recColl.exceedsHourOfDay(end_of_series, calc_timezone);
        long end_of_calculation = end_of_series;
        if(range_end  != 0) {
            end_of_calculation = Math.min(end_of_series, range_end);
        }

        final boolean boundaries = hasBoundaries();

        loop: while (normalized_start_of_series <= end_of_calculation) {
            increaseCalculationCounter();
            for (int a = 0; a < c; a++) {
                increaseCalculationCounter();
                calc.setTimeInMillis(start_of_series);
                calc.add(Calendar.DAY_OF_MONTH, r[a]);
                range = calc.getTimeInMillis();
                if (range >= sst && normalized_start_of_series <= end_of_series) {
                    final long end_of_occurrence = range + diff + recurrence_calculator * Constants.MILLI_DAY;
                    if (((!boundaries) || (end_of_occurrence > range_start && range < range_end) || pos == ds_count) && (!recColl.isException(range, changeExceptions, deleteExceptions))) {
                        //if (!isException(range, change_exceptions, delete_exceptions)) {
						if (exceeds ? ((recColl.normalizeLong(range) + Constants.MILLI_DAY) > end_of_series)
								: (recColl.normalizeLong(range) > end_of_series)) {
							break loop;
						}
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            recColl.fillMap(rs, range, diff, recurrence_calculator, ds_count);
                        }
                        if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break loop;
                        }
                        //}
                    }
                    ds_count++;
                }
            }
            calc_weekly.add(Calendar.WEEK_OF_YEAR, recurring_interval);
            start_of_series = calc_weekly.getTimeInMillis();

            normalized_start_of_series = start_of_series;
            //normalized_start_of_series += (Constants.MILLI_WEEK*recurring_interval);

            // ???
            //calc.add(Calendar.WEEK_OF_YEAR, recurring_interval);
        }

        return rs;
    }

    private final RecurringResults calculateMonthly() throws OXException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_DAILY_INTERVAL.create(recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(Tools.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        final long sst = normalized_start_of_series;

        int a = recurring_days;
        final int monthly = recurring_interval;
        final int day_or_type = recurring_day_in_month;

        rs = new RecurringResults();

        if (day_or_type == 0) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(day_or_type);
        }
        if (monthly <= 0) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL_2.create(monthly);
        }

        final boolean boundaries = hasBoundaries();

        if (!contains_days) {
            if (contains_occurrence) {
                end_of_series += Constants.MILLI_MONTH;
            }
            long end_of_calculation = end_of_series;
            if (range_end != 0) {
                end_of_calculation = Math.min(end_of_series, range_end);
            }

            // TODO: Remove on failure
            // ///////////////////
            // MARKER-START
//            if (range_start != 0) {
//                calc.setTimeInMillis(start_of_series);
//                calc.add(Calendar.MONTH, recurring_interval);
//                while (calc.getTimeInMillis() < range_start) {
//                    start_of_series = calc.getTimeInMillis();
//                    calc.add(Calendar.MONTH, recurring_interval);
//                }
//            }
            // MARKER-END
            // ///////////////////

            while (start_of_series <= end_of_calculation) {
                increaseCalculationCounter();
                calc.setTimeInMillis(start_of_series);
                final int month = calc.get(Calendar.MONTH);
                final int year = calc.get(Calendar.YEAR);
                calc.set(Calendar.YEAR, year);
                calc.set(Calendar.MONTH, month);
                if (calc.getActualMaximum(Calendar.DAY_OF_MONTH) >= day_or_type) {
                    calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                    start_of_series = calc.getTimeInMillis();
                    calc.setFirstDayOfWeek(Calendar.MONDAY); // TODO: Make this configurable
                    final long range = calc.getTimeInMillis();
                    if (range >= sst && range <= end_of_series) {
                        final long end_of_occurrence = range + diff + recurrence_calculator * Constants.MILLI_DAY;
                        if (((!boundaries) || (end_of_occurrence > range_start && range < range_end) || pos == ds_count)
                        && (!recColl.isException(start_of_series, changeExceptions, deleteExceptions))) {
                            //if (!isException(start_of_series, change_exceptions, delete_exceptions)) {
                            if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                                recColl.fillMap(rs, start_of_series, diff, recurrence_calculator, ds_count);
                            }
                            if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                                break;
                            }
                            //}
                        }
                        ds_count++;
                    }
                }
                calc.add(Calendar.MONTH, monthly);
                start_of_series = calc.getTimeInMillis();
            }
        } else {
            a = getDay(a);

                /*-
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
                throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY.create(a);
            }
            if (day_or_type < 1 || day_or_type > 5) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2.create(day_or_type);
            }

            final Calendar helper = (Calendar) calc.clone();
            if (contains_occurrence) {
                end_of_series += Constants.MILLI_MONTH;
            }
            // Reset to first day in month to properly detect all occurrences
            calc.setTimeInMillis(start_of_series);
            calc.set(Calendar.DAY_OF_MONTH, 1);
            start_of_series = calc.getTimeInMillis();

            long end_of_calculation = end_of_series;
            if(range_end  != 0) {
                end_of_calculation = Math.min(end_of_series, range_end);
            }

            // TODO: Remove on failure
            // ///////////////////
            // MARKER-START
//            if (range_start != 0) {
//                calc.setTimeInMillis(start_of_series);
//                calc.add(Calendar.MONTH, recurring_interval);
//                while (calc.getTimeInMillis() < range_start) {
//                    start_of_series = calc.getTimeInMillis();
//                    calc.add(Calendar.MONTH, recurring_interval);
//                }
//            }
            // MARKER-END
            // ///////////////////

            while (start_of_series <= end_of_calculation) {
                increaseCalculationCounter();
                calc.setTimeInMillis(start_of_series);
                helper.setTimeInMillis(start_of_series);

                if (day_or_type < 5) {
                    // first until forth
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        for (int x = 0; x < 13; x++) {
                            increaseCalculationCounter();
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
                            int neededWorkdays = day_or_type;
                            int day = 1;
                            while(neededWorkdays > 0) {
                                increaseCalculationCounter();
                                calc.set(Calendar.DAY_OF_MONTH, day);
                                if(calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    // No Workday, so we still need them all
                                } else {
                                    // Found a workday
                                    neededWorkdays--;
                                }
                                day++;
                            }

                        } else if (a == CalendarObject.WEEKENDDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            int neededWeekenddays = day_or_type;
                            int day = 1;
                            while(neededWeekenddays > 0) {
                                increaseCalculationCounter();
                                calc.set(Calendar.DAY_OF_MONTH, day);
                                if(calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    // Found a weekendday
                                    neededWeekenddays--;
                                } else {
                                    // Found a workday
                                }
                                day++;
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
                            increaseCalculationCounter();
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
                                increaseCalculationCounter();
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        }
                    }
                }
                final  long range = calc.getTimeInMillis();
                start_of_series = calc.getTimeInMillis();
                if (range >= sst && range <= end_of_series) {
                    final long end_of_occurrence = range + diff + recurrence_calculator * Constants.MILLI_DAY;
                    if (((!boundaries) || (end_of_occurrence > range_start && range < range_end) || pos == ds_count)
                    && (!recColl.isException(start_of_series, changeExceptions, deleteExceptions))) {
                        //if (!isException(start_of_series, change_exceptions, delete_exceptions)) {
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            recColl.fillMap(rs, start_of_series, diff, recurrence_calculator, ds_count);
                        }
                        if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break;
                        }
                        //}
                    }
                    ds_count++;
                }
                helper.add(Calendar.MONTH, recurring_interval);
                start_of_series = helper.getTimeInMillis();
            }
        }


        return rs;
    }

    private final RecurringResults calculateYearly() throws OXException {
        RecurringResults rs = null;
        if (recurring_interval < 1) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_DAILY_INTERVAL.create(recurring_interval);
        }
        final Calendar calc = Calendar.getInstance(Tools.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(first_day_of_week);
        int ds_count = 1;
        final long sst = normalized_start_of_series;

        rs = new RecurringResults();

        int a = recurring_days;
        final int day_or_type = recurring_day_in_month;
        final int month = recurring_month;

        if (day_or_type == 0) {
            throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL.create(day_or_type);
        }

        final boolean boundaries = hasBoundaries();

        if (!contains_days) {
            long end_of_calculation = end_of_series;
            if(range_end  != 0) {
                end_of_calculation = Math.min(end_of_series, range_end);
            }

            while (start_of_series <= end_of_calculation) {
                Calendar sos = Calendar.getInstance();
                sos.setTimeInMillis(start_of_series);
                increaseCalculationCounter();
                calc.setTimeInMillis(start_of_series);
                calc.set(Calendar.YEAR, calc.get(Calendar.YEAR));
                calc.set(Calendar.MONTH, month);
                if (calc.getActualMaximum(Calendar.DAY_OF_MONTH) >= day_or_type) {
                    calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                    start_of_series = calc.getTimeInMillis();
                    calc.setFirstDayOfWeek(Calendar.MONDAY); // TODO: Make this configurable
                    final long range = calc.getTimeInMillis();
                    if (range >= sst && range <= end_of_series) {
                        final long end_of_occurrence = range + diff + recurrence_calculator * Constants.MILLI_DAY;
                        if (((!boundaries) || (end_of_occurrence > range_start && range < range_end) || pos == ds_count)
                        && (!recColl.isException(start_of_series, changeExceptions, deleteExceptions))) {
                            //if (!isException(start_of_series, change_exceptions, delete_exceptions)) {
                            if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                                recColl.fillMap(rs, start_of_series, diff, recurrence_calculator, ds_count);
                            }
                            if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                                break;
                            }
                            //}
                        }
                        ds_count++;
                    }
                }
                calc.add(Calendar.YEAR, recurring_interval);
                start_of_series = calc.getTimeInMillis();
            }
        } else {
            a = getDay(a);

                /*-
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
                throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_DAY.create(a);
            }
            if (day_or_type < 1 || day_or_type > 5) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE.create(day_or_type);
            }

            final Calendar helper = (Calendar) calc.clone();
            helper.setTimeInMillis(start_of_series);
            helper.set(Calendar.MONTH, month);
            helper.set(Calendar.WEEK_OF_MONTH, 1);
            helper.set(Calendar.DAY_OF_MONTH, 1);

            long end_of_calculation = end_of_series;
            if(range_end  != 0) {
                end_of_calculation = Math.min(end_of_series, range_end);
            }

            // TODO: Remove on failure
            // ///////////////////
            // MARKER-START
//            if (range_start != 0) {
//                calc.setTimeInMillis(start_of_series);
//                calc.add(Calendar.YEAR, recurring_interval);
//                while (calc.getTimeInMillis() < range_start) {
//                    start_of_series = calc.getTimeInMillis();
//                    calc.add(Calendar.YEAR, recurring_interval);
//                }
//            }
            // MARKER-END
            // ///////////////////

            while (start_of_series <= end_of_calculation) {
                increaseCalculationCounter();
                calc.setTimeInMillis(start_of_series);
                if (day_or_type < 5) {
                    // first until forth
                    if (a <= Calendar.SATURDAY) {
                        // normal operation
                        calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                        calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                        calc.set(Calendar.DAY_OF_MONTH, 1);
                        for (int x = 0; x < 13; x++) {
                            increaseCalculationCounter();
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
                            //calc.add(Calendar.DAY_OF_MONTH, 0-day_or_type);
                        } else if (a == CalendarObject.WEEKDAY) {
                            calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                            calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                            calc.set(Calendar.DAY_OF_MONTH, 1);
                            addWorkdays(calc, day_or_type);
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
                            increaseCalculationCounter();
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
                                increaseCalculationCounter();
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, -1);
                            }
                        }
                    }
                }
                final long range = calc.getTimeInMillis();
                start_of_series = calc.getTimeInMillis();

                if (range >= sst && range <= end_of_series) {
                    final long end_of_occurrence = range + diff + recurrence_calculator * Constants.MILLI_DAY;
                    if (((!boundaries) || (end_of_occurrence > range_start && range < range_end) || pos == ds_count)
                    && (!recColl.isException(start_of_series, changeExceptions, deleteExceptions))) {
                        //if (!isException(start_of_series, change_exceptions, delete_exceptions)) {
                        if (!contains_occurrence || calc_until ||(contains_occurrence && ds_count <= occurrence_value)) {
                            recColl.fillMap(rs, start_of_series, diff, recurrence_calculator, ds_count);
                        }
                        if (rs.size() > PMAXTC || pos == ds_count || (contains_occurrence && ds_count == occurrence_value)) {
                            break;
                        }
                        //}
                    }
                    ds_count++;
                }
                helper.add(Calendar.YEAR, recurring_interval);
                start_of_series = helper.getTimeInMillis();
            }
        }


        return rs;
    }

    /**
     * Adds the ammount of workdays to the given Calendar, which should be the first of a month.
     *
     * @param cal
     * @param days
     */
    private void addWorkdays(final Calendar cal, int days) {
        while (days > 1 || isWeekend(cal)) {
            if (!isWeekend(cal)) {
                days--;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isWeekend(final Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
    }

    private static int getDay(final int cd) throws OXException {
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
                throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_DAY.create(cd);
        }
        return ret;
    }

    /**
     * Sets the max. number of operation steps which are allowed to be performed during recurring calculation.
     *
     * @param calculations The max. number of operation steps which are allowed to be performed during recurring calculation.
     */
    public void setMaxOperations(final int calculations) {
        TTL = calculations;
    }
}
