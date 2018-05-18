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

package com.openexchange.groupware.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.old.RecurringCalculation;
import com.openexchange.groupware.calendar.old.RecurringResult;
import com.openexchange.groupware.container.CalendarObject;

/**
 * {@link CalendarCollectionUtils} - Provides calculation routines for recurring calendar items.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated
 */
@Deprecated
public final class CalendarCollectionUtils {

    private static final char DELIMITER_PIPE = '|';

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarCollectionUtils.class);

    @Deprecated
    public static final int MAX_OCCURRENCESE = 999;

    /**
     * Prevent instantiation.
     */
    private CalendarCollectionUtils() {
        super();
    }

    /**
     * Creates the recurring pattern for given (possibly recurring) appointment
     * if needed and fills its recurring information according to generated
     * pattern.
     *
     * @param cdao
     *            The (possibly recurring) appointment
     * @return <code>true</code> if specified appointment denotes a proper
     *         recurring appointment whose recurring information could be
     *         successfully filled; otherwise <code>false</code> to indicate a failure
     */
    @Deprecated
    public static boolean fillDAO(final CalendarDataObject cdao) throws OXException {
        if (cdao.getRecurrence() == null || cdao.getRecurrence().indexOf(DELIMITER_PIPE) == -1) {
            if (cdao.getRecurrenceType() == 0) {
                // No recurring appointment
                return false;
            }
            if ((cdao.getInterval() == 0 && cdao.getMonth() == 0) || cdao.getStartDate() == null || cdao.getEndDate() == null) {
                // Insufficient information
                return false;
            }
            changeRecurrenceString(cdao);
        }
        try {
            convertDSString(cdao);
            return true;
        } catch (final OXException e) {
            LOG.error("fillDAO:convertDSString error.", e);
        }
        return false;
    }

    private static void changeRecurrenceString(final CalendarDataObject cdao) throws OXException {
        final String recString = createDSString(cdao);
        if (recString == null) {
            cdao.removeRecurrenceID();
        }
        cdao.setRecurrence(recString);
    }

    /**
     * Converts the recurring pattern from specified recurring appointment
     * into its corresponding recurring fields to properly reflect pattern's
     * recurrence.
     *
     * @param cdao The recurring appointment
     * @throws OXException If recurring appointment's pattern is invalid
     */
    private static void convertDSString(final CalendarDataObject cdao) throws OXException {
        char name;
        String value;
        final String ds = cdao.getRecurrence();
        if (ds != null) {
            int s = 0;
            int f = 0;
            while ((f = ds.indexOf(DELIMITER_PIPE, s)) != -1) {
                name = ds.charAt(s);
                s = f + 1;
                f = ds.indexOf(DELIMITER_PIPE, s);
                if (f == -1) {
                    value = ds.substring(s, ds.length());
                    encodeNameValuePair(name, value, cdao);
                    break;
                }
                value = ds.substring(s, f);
                encodeNameValuePair(name, value, cdao);
                s = f + 1;
            }
        }
        checkAndCorrectErrors(cdao);
    }

    private static void checkAndCorrectErrors(final CalendarDataObject cdao) {
        if (cdao.getInterval() > MAX_OCCURRENCESE) {
            final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getInterval()), Integer.valueOf(MAX_OCCURRENCESE));
            LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
            cdao.setInterval(MAX_OCCURRENCESE);
        }
        if (cdao.getOccurrence() > MAX_OCCURRENCESE) {
            final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getOccurrence()), Integer.valueOf(MAX_OCCURRENCESE));
            LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
            cdao.setOccurrence(MAX_OCCURRENCESE);
        }
        if (cdao.getRecurrenceType() == CalendarObject.DAILY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (daily), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (weekly), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
            if (cdao.getDays() < 1) {
                LOG.debug("Auto correction (weekly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.MONTHLY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (montly), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
            if (cdao.containsDays() && cdao.getDays() != 0 && (getDay(cdao.getDays()) == -1)) {
                // if (getDay(cdao.getDays()) == -1) {
                LOG.debug("Auto correction (monthly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
                // }
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            if (cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                LOG.debug("Auto correction (monthy), set month to 1, the given interval was: {}", cdao.getMonth());
                cdao.setMonth(Calendar.JANUARY);
            }
            if (cdao.containsDays() && (getDay(cdao.getDays()) == -1)) {
                // if (getDay(cdao.getDays()) == -1) {
                LOG.debug("Auto correction (yearly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
                // }
            }
        }
    }

    /**
     * Applies the given name-value-pair to specified calendar object
     *
     * @param name
     *            The name identifier
     * @param value
     *            The value
     * @param cdao
     *            The calendar object
     * @throws OXException
     *             If an unknown name-value-pair occurs
     */
    private static void encodeNameValuePair(final char name, final String value, final CalendarDataObject cdao) throws OXException {
        if (name == 't') {
            int t = Integer.parseInt(value);
            if (t == 5) {
                t = 3;
            } else if (t == 6) {
                t = 4;
            }
            cdao.setRecurrenceType(t);
        } else if (name == 'i') {
            cdao.setInterval(Integer.parseInt(value));
        } else if (name == 'a') {
            cdao.setDays(Integer.parseInt(value));
        } else if (name == 'b') {
            cdao.setDayInMonth(Integer.parseInt(value));
        } else if (name == 'c') {
            cdao.setMonth(Integer.parseInt(value));
        } else if (name == 'e') {
            final long u = Long.parseLong(value);
            cdao.setUntil(new java.util.Date(u));
        } else if (name == 's') {
            final long s = Long.parseLong(value);
            cdao.setRecurringStart(s);
        } else if (name == 'o') {
            cdao.setOccurrence(Integer.parseInt(value));
        } else {
            throw OXCalendarExceptionCodes.UNKNOWN_NVP_IN_REC_STR.create(Character.valueOf(name), value);
        }
    }

    private static int NO_END_YEARS = 4;

    @Deprecated
    public static Date getMaxUntilDate(final CalendarDataObject cdao) {
        /*
         * Determine max. end date
         */
        long maxEnd;
        if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            maxEnd = normalizeLong(addYears(cdao.getStartDate().getTime(), MAX_OCCURRENCESE));
        } else {
            maxEnd = normalizeLong(addYears(cdao.getStartDate().getTime(), NO_END_YEARS));
        }

        /*
         * Create a clone for calculation purpose
         */
        final CalendarDataObject clone = cdao.clone();
        final RecurringResultsInterface rresults;
        try {
            rresults = calculateRecurring(clone, 0, 0, MAX_OCCURRENCESE, MAX_OCCURRENCESE, true);
        } catch (final OXException e) {
            LOG.error("", e);
            return new Date(maxEnd);
        }
        if (rresults == null) {
            return new Date(maxEnd);
        }
        final RecurringResultInterface rresult = rresults.getRecurringResult(0);
        if (rresult != null) {
            return new Date(normalizeLong(rresult.getEnd()));
        }
        return new Date(maxEnd);
    }

    @Deprecated
    public static long normalizeLong(final long millis) {
        return millis - (millis % Constants.MILLI_DAY);
    }

    @Deprecated
    public static boolean isException(final long t, final Set<Long> ce, final Set<Long> de) {
        final Long check = Long.valueOf(normalizeLong(t));
        return (null == ce ? false : ce.contains(check)) || (null == de ? false : de.contains(check));
    }

    @Deprecated
    public static RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions) throws OXException {
        String change_exceptions = null;
        String delete_exceptions = null;
        String calc_timezone = "UTC";
        final long recurringStart;
        if (cdao instanceof CalendarDataObject) {
            final CalendarDataObject calDataObject = (CalendarDataObject) cdao;
            if (!ignore_exceptions) {
                change_exceptions = calDataObject.getExceptions();
                delete_exceptions = calDataObject.getDelExceptions();
            }
            if (!calDataObject.getFullTime()) {
                if (calDataObject.containsTimezone()) {
                    calc_timezone = calDataObject.getTimezone();
                } else {
                    final OXException e = OXCalendarExceptionCodes.TIMEZONE_MISSING.create();
                    LOG.warn("", e);
                }
            }
            recurringStart = calDataObject.getRecurringStart();
        } else {
            recurringStart = ((cdao.getStartDate().getTime() / Constants.MILLI_DAY) * Constants.MILLI_DAY);
        }

        final RecurringCalculation rc = new RecurringCalculation(cdao.getRecurrenceType(), cdao.getInterval(), cdao.getRecurrenceCalculator());
        rc.setCalculationTimeZone(calc_timezone);
        rc.setCalculationPosition(pos);
        rc.setRange(range_start, range_end);
        rc.setMaxCalculation(PMAXTC);
        rc.setMaxOperations(49950);
        rc.setExceptions(change_exceptions, delete_exceptions);
        rc.setStartAndEndTime(cdao.getStartDate().getTime(), cdao.getEndDate().getTime());
        rc.setRecurringStart(recurringStart);

        if (cdao.containsUntil() && cdao.getUntil() != null) {
            rc.setUntil(cdao.getUntil().getTime());
        }

        if (cdao.containsOccurrence() && cdao.getOccurrence() > 0) {
            rc.setOccurrence(cdao.getOccurrence());
        }
        if (cdao.containsDays()) {
            rc.setDays(cdao.getDays());
        }
        if (cdao.containsDayInMonth()) {
            rc.setDayInMonth(cdao.getDayInMonth());
        }
        if (cdao.containsMonth()) {
            rc.setMonth(cdao.getMonth());
        }
        return rc.calculateRecurrence();
    }

    private static String createDSString(final CalendarDataObject cdao) throws OXException {
        if (cdao.containsStartDate()) {
            checkRecurring(cdao);
            StringBuilder recStrBuilder = new StringBuilder(64);
            final int recurrenceType = cdao.getRecurrenceType();
            int interval = cdao.getInterval(); // i
            if (interval > MAX_OCCURRENCESE) {
                final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(interval), Integer.valueOf(MAX_OCCURRENCESE));
                LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
                interval = MAX_OCCURRENCESE;
            }
            final int weekdays = cdao.getDays();
            final int monthday = cdao.getDayInMonth();
            final int month = cdao.getMonth();
            int occurrences = cdao.getOccurrence();
            if (occurrences > MAX_OCCURRENCESE) {
                final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(occurrences), Integer.valueOf(MAX_OCCURRENCESE));
                LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
                occurrences = MAX_OCCURRENCESE;
            }
            if (!cdao.containsUntil() && !cdao.containsOccurrence()) {
                occurrences = -1;
            }
            if (recurrenceType == CalendarObject.DAILY) {
                dsf(recStrBuilder, 1);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (occurrences > 0) {
                    cdao.setUntil(calculateUntilOfSequence(cdao));
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
                    cdao.setUntil(calculateUntilOfSequence(cdao));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                }
            } else if (recurrenceType == CalendarObject.MONTHLY) {
                if (monthday <= 0) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(monthday));
                }
                if (weekdays <= 0) {
                    if (monthday > 31) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 3);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday > 5) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 5);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                }
            } else if (recurrenceType == CalendarObject.YEARLY) {
                if (weekdays <= 0) {
                    if (monthday <= 0 || monthday > 31) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 4);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday < 1 || monthday > 5) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 6);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
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
        throw OXCalendarExceptionCodes.RECURRING_MISSING_START_DATE.create();
    }

    private static void dsf(final StringBuilder sb, final char c, final int v) {
        if (v >= 0) {
            sb.append(c);
            sb.append(DELIMITER_PIPE);
            sb.append(v);
            sb.append(DELIMITER_PIPE);
        }
    }

    private static void dsf(final StringBuilder sb, final char c, final long l) {
        sb.append(c);
        sb.append(DELIMITER_PIPE);
        sb.append(l);
        sb.append(DELIMITER_PIPE);
    }

    private static void dsf(final StringBuilder sb, final int type) {
        dsf(sb, 't', type);
    }

    private static Date calculateUntilOfSequence(final CalendarDataObject cdao) throws OXException {
        final Date temp = getOccurenceDate(cdao);
        temp.setTime(temp.getTime() - cdao.getRecurrenceCalculator() * Constants.MILLI_DAY);
        return temp;
    }

    /**
     * Gets the specified occurrence's end date within recurring appointment.
     *
     * @param cdao The recurring appointment
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    private static Date getOccurenceDate(final CalendarDataObject cdao) throws OXException {
        return getOccurenceDate(cdao, cdao.getOccurrence());
    }

    /**
     * Gets the given occurrence's end date within specified recurring appointment.
     *
     * @param cdao The recurring appointment
     * @param occurrence The occurrence
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    private static Date getOccurenceDate(final CalendarDataObject cdao, final int occurrence) throws OXException {
        final RecurringResultsInterface rss = calculateRecurring(cdao, 0, 0, occurrence, 1, true);
        final RecurringResultInterface rs = rss.getRecurringResult(0);
        if (rs != null) {
            return new Date(rs.getEnd());
        }
        LOG.warn("Unable to calculate until date :{}", cdao);
        return new Date(cdao.getStartDate().getTime() + Constants.MILLI_YEAR);
    }

    @Deprecated
    public static void fillMap(final RecurringResultsInterface rss, final long s, final long diff, final int d, final int counter) {
        final RecurringResult rs = new RecurringResult(s, diff, d, counter);
        rss.add(rs);
    }

    @Deprecated
    public static boolean exceedsHourOfDay(final long millis, final String timeZoneID) {
        return exceedsHourOfDay(millis, TimeZone.getTimeZone(timeZoneID));
    }

    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     *
     * @param millis
     *            The time millis
     * @param zone
     *            The time zone
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    private static boolean exceedsHourOfDay(final long millis, final TimeZone zone) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(millis);
        final long hours = cal.get(Calendar.HOUR_OF_DAY) + (zone.getOffset(millis) / Constants.MILLI_HOUR);
        return hours >= 24 || hours < 0;
    }

    private static Map<Integer, Integer> DAY_MAP = new HashMap<Integer, Integer>(10);

    static {
        DAY_MAP.put(Integer.valueOf(CalendarObject.SATURDAY), Integer.valueOf(Calendar.SATURDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.FRIDAY), Integer.valueOf(Calendar.FRIDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.THURSDAY), Integer.valueOf(Calendar.THURSDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEDNESDAY), Integer.valueOf(Calendar.WEDNESDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.TUESDAY), Integer.valueOf(Calendar.TUESDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.MONDAY), Integer.valueOf(Calendar.MONDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.SUNDAY), Integer.valueOf(Calendar.SUNDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.DAY), Integer.valueOf(CalendarObject.DAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEEKDAY), Integer.valueOf(CalendarObject.WEEKDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEEKENDDAY), Integer.valueOf(CalendarObject.WEEKENDDAY));
    }

    private static int getDay(final int cd) {
        final Integer retval = DAY_MAP.get(Integer.valueOf(cd));
        if (retval == null) {
            LOG.error("Unusable getDay parameter (days) :{}", cd, new Throwable());
            return -1;
        }
        return retval.intValue();
    }

    @Deprecated
    public static void checkRecurring(final CalendarObject cdao) throws OXException {
        if (cdao.getInterval() > MAX_OCCURRENCESE) {
            throw OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getInterval()), Integer.valueOf(MAX_OCCURRENCESE));
        }
        if (cdao.getOccurrence() > MAX_OCCURRENCESE) {
            throw OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getOccurrence()), Integer.valueOf(MAX_OCCURRENCESE));
        }
        if (cdao.getRecurrenceType() == CalendarObject.DAILY) {
            if (cdao.getInterval() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
            }
            if (cdao.getDays() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_DAYS.create(Integer.valueOf(cdao.getDays()));
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.MONTHLY) {
            if (cdao.containsDays()) {
                if (cdao.getInterval() < 1) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2.create(Integer.valueOf(cdao.getDayInMonth()));
                }
            } else {
                if (cdao.getInterval() < 1) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 999) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(cdao.getDayInMonth()));
                }
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            if (cdao.containsDays()) {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE.create(Integer.valueOf(cdao.getDayInMonth()));
                }
                if (!cdao.containsMonth() || cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(Integer.valueOf(cdao.getMonth()));
                }
            } else {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 32) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL.create(Integer.valueOf(cdao.getDayInMonth()));
                }
                if (!cdao.containsMonth() || cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(Integer.valueOf(cdao.getMonth()));
                }
            }
        }
    }

    private static long addYears(final long base, final int years) {
        final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(base);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTimeInMillis();
    }

    /**
     * 
     * @param date
     * @param ignoreDate
     * @param cdao
     * @param changeExceptions
     * @return
     * @throws OXException
     */
    @Deprecated
    public static boolean isOccurrenceDate(final long date, final long ignoreDate, final CalendarDataObject cdao, final long[] changeExceptions) throws OXException {
        /*
         * Since we check dates here, normalize given time millis
         */
        final long check = normalizeLong(date);
        final long ign = normalizeLong(ignoreDate);
        if (check == ign) {
            /*
             * Original and new date are equal
             */
            return false;
        }
        final RecurringResultsInterface rss = calculateRecurring(cdao, check - Constants.MILLI_WEEK, check + Constants.MILLI_WEEK, 0, MAX_OCCURRENCESE, false);
        /*
         * Check regular occurrences
         */
        {
            final int size = rss.size();
            for (int i = 0; i < size; i++) {
                final long cur = rss.getRecurringResult(i).getNormalized();
                if (cur != ign && cur == check) {
                    /*
                     * Date already occupied by a regular occurrence
                     */
                    return true;
                }
            }
        }
        /*
         * Check change exceptions
         */
        for (int i = 0; i < changeExceptions.length; i++) {
            if (changeExceptions[i] == check) {
                /*
                 * Date already occupied by a change exception
                 */
                return true;
            }
        }
        return false;
    }

}
