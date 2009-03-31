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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.calendar.OXCalendarException.Code;
import com.openexchange.groupware.calendar.recurrence.RecurringCalculation;
import com.openexchange.groupware.calendar.recurrence.RecurringException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link CalendarRecurringCollection} - Provides calculation routines for recurring calendar items. 
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarRecurringCollection {
    
    private static final char DELIMITER_PIPE = '|';

    public static final String NO_DS = null;
    
    /**
     * @deprecated use {@link Constants#MILLI_HOUR}.
     */
    @Deprecated
    public static final long MILLI_HOUR = Constants.MILLI_HOUR;
    /**
     * @deprecated use {@link Constants#MILLI_DAY}.
     */
    @Deprecated
    public static final long MILLI_DAY = Constants.MILLI_DAY;
    /**
     * @deprecated use {@link Constants#MILLI_WEEK}.
     */
    @Deprecated
    public static final long MILLI_WEEK = Constants.MILLI_WEEK;
    /**
     * @deprecated use {@link Constants#MILLI_MONTH}.
     */
    @Deprecated
    public static final long MILLI_MONTH = Constants.MILLI_MONTH;
    /**
     * @deprecated use {@link Constants#MILLI_YEAR}.
     */
    @Deprecated
    public static final long MILLI_YEAR = Constants.MILLI_YEAR;

    /**
     * Constant to indicate that no recurring action was detected
     */
    public static final int RECURRING_NO_ACTION = 0;

    /**
     * Constant to indicate that a delete exception shall be created through a
     * <b>delete</b> operation
     */
    public static final int RECURRING_VIRTUAL_ACTION = 1;

    /**
     * Constant to indicate that a formerly created change exception shall be
     * deleted through a <b>delete</b> operation; meaning it's turned into a
     * delete exception
     */
    public static final int RECURRING_EXCEPTION_ACTION = 2;

    /**
     * Constant to indicate that a former recurring appointment is turned to a
     * normal non-recurring appointment through an <b>update</b> operation
     */
    public static final int RECURRING_EXCEPTION_DELETE = 3;

    /**
     * Constant to indicate that whole recurring appointment shall be deleted
     * through a <b>delete</b> operation
     */
    public static final int RECURRING_FULL_DELETE = 4;

    /**
     * Constant to indicate that a change exception shall be created through an
     * <b>update</b> operation
     */
    public static final int RECURRING_CREATE_EXCEPTION = 5;

    /**
     * Constant to indicate that the recurring pattern of a recurring
     * appointment shall be changed through an <b>update</b> operation
     */
    public static final int CHANGE_RECURRING_TYPE = 6;

    /**
     * Constant to indicate that a formerly created change exception shall be
     * deleted through an <b>update</b> operation; meaning it's turned into a
     * delete exception
     */
    public static final int RECURRING_EXCEPTION_DELETE_EXISTING = 7;

    /**
     * The maximum supported number of occurrences.
     */
    public static final int MAXTC = 999;

    private static int NO_END_YEARS = 4;

    /**
     * 'UTC' time zone
     */
    public static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");
    
    
    private static final Log LOG = LogFactory.getLog(CalendarRecurringCollection.class);
    
    /**
     * This constructor for the class <code>CalendarRecurringCollection</code> is private
     * because the developer should work with the getInstance() method. This seems to
     * be more flexible in the future if we need to rewrite this ...
     *
     */
    private CalendarRecurringCollection() { super(); }
    
    /**
     * <code>getMAX_END_YEARS</code> returns NO_END_YEARS.
     * NO_END_YEARS means if no end date is given we calculate the start date
     * PLUS NO_END_YEARS to have an end date ...
     *
     * @return an <code>int</code> value
     */
    public static int getMAX_END_YEARS() {
        return NO_END_YEARS;
    }
    
    /**
     * <code>setMAX_END_YEARS</code> sets the max
     * number of years a sequence can run if no end date
     * is given
     *
     * @return an <code>int</code> value
     */
    public static void setMAX_END_YEARS(final int MAX_END_YEARS) {
        NO_END_YEARS = MAX_END_YEARS;
    }
    
    /**
     * Converts the recurring pattern from specified recurring appointment
     * into its corresponding recurring fields to properly reflect pattern's
     * recurrence.
     * 
     * @param cdao The recurring appointment
     * @throws OXCalendarException If recurring appointment's pattern is invalid
     */
    private static void convertDSString(final CalendarDataObject cdao) throws OXCalendarException {
        char name;
        String value;
        final String ds = cdao.getRecurrence();
        if (ds != null) {
            int s = 0;
            int f = 0;
            while ((f = ds.indexOf(DELIMITER_PIPE, s)) != -1) {
                name = ds.charAt(s);
                s = f+1;
                f = ds.indexOf(DELIMITER_PIPE, s);
                if (f == -1) {
                    value = ds.substring(s, ds.length());
                    encodeNameValuePair(name, value, cdao);
                    break;
                }
                value = ds.substring(s, f);
                encodeNameValuePair(name, value, cdao);
                s = f+1;
            }
        }
        checkAndCorrectErrors(cdao);
    }
    
    private static void checkAndCorrectErrors(final CalendarDataObject cdao) {
        if (cdao.getInterval() > CalendarRecurringCollection.MAXTC) {
            final OXCalendarException exc = new OXCalendarException(
                    OXCalendarException.Code.RECURRING_VALUE_CONSTRAINT, Integer.valueOf(cdao.getInterval()), Integer
                            .valueOf(CalendarRecurringCollection.MAXTC));
            if (LOG.isWarnEnabled()) {
                LOG.warn(exc.getMessage() + " Auto-corrected to " + CalendarRecurringCollection.MAXTC, exc);
            }
            cdao.setInterval(CalendarRecurringCollection.MAXTC);
        }
        if (cdao.getOccurrence() > CalendarRecurringCollection.MAXTC) {
            final OXCalendarException exc = new OXCalendarException(
                    OXCalendarException.Code.RECURRING_VALUE_CONSTRAINT, Integer.valueOf(cdao.getOccurrence()), Integer
                            .valueOf(CalendarRecurringCollection.MAXTC));
            if (LOG.isWarnEnabled()) {
                LOG.warn(exc.getMessage() + " Auto-corrected to " + CalendarRecurringCollection.MAXTC, exc);
            }
            cdao.setOccurrence(CalendarRecurringCollection.MAXTC);
        }
        if (cdao.getRecurrenceType() == CalendarDataObject.DAILY) {
            if (cdao.getInterval() < 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (daily), set interval to 1, the given interval was: "+cdao.getInterval());
                }
                cdao.setInterval(1);
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (weekly), set interval to 1, the given interval was: "+cdao.getInterval());
                }
                cdao.setInterval(1);
            }
            if (cdao.getDays() < 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (weekly), set day to CalendarDataObject.MONDAY, the given day was: "+cdao.getDays());
                }
                cdao.setDays(CalendarDataObject.MONDAY);
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.MONTHLY) {
            if (cdao.getInterval() < 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (montly), set interval to 1, the given interval was: "+cdao.getInterval());
                }
                cdao.setInterval(1);
            }
            if (cdao.containsDays() && (getDay(cdao.getDays()) == -1)) {
                //if (getDay(cdao.getDays()) == -1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (monthly), set day to CalendarDataObject.MONDAY, the given day was: "+cdao.getDays());
                }
                cdao.setDays(CalendarDataObject.MONDAY);
                //}
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.YEARLY) {
            if (cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (monthy), set month to 1, the given interval was: "+cdao.getMonth());
                }
                cdao.setMonth(Calendar.JANUARY);
            }
            if (cdao.containsDays() && (getDay(cdao.getDays()) == -1)) {
                //if (getDay(cdao.getDays()) == -1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Auto correction (yearly), set day to CalendarDataObject.MONDAY, the given day was: "+cdao.getDays());
                }
                cdao.setDays(CalendarDataObject.MONDAY);
                //}
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
     * @throws OXCalendarException
     *             If an unknown name-value-pair occurs
     */
    private static void encodeNameValuePair(final char name, final String value, final CalendarDataObject cdao)
            throws OXCalendarException {
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
            throw new OXCalendarException(OXCalendarException.Code.UNKNOWN_NVP_IN_REC_STR, Character.valueOf(name),
                    value);
        }
    }

    /**
     * Checks if given calendar data object denotes a recurring master.
     * 
     * @param edao
     *            The calendar data object to check
     * @return <code>true</code> if given calendar data object denotes a
     *         recurring master; otherwise <code>false</code>
     */
    public static boolean isRecurringMaster(final CalendarDataObject edao) {
        if (edao == null) {
            return false;
        }
        return (edao.containsRecurrenceID() && edao.containsObjectID() && (edao.getRecurrenceID() > 0)
                && (edao.getObjectID() > 0) && (edao.getRecurrenceID() == edao.getObjectID()));
    }
    
    /**
     * <code>getRecurringAppointmentDeleteAction</code> detects and returns
     * the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (tranfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    public static int getRecurringAppointmentDeleteAction(final CalendarDataObject cdao, final CalendarDataObject edao) {
        int rada = RECURRING_NO_ACTION;
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0) {
            if (edao.getRecurrenceID() == edao.getObjectID() && edao.getRecurrencePosition() == 0) {
                if (cdao.containsRecurrencePosition()) {
                    // virtual delete
                    rada = RECURRING_VIRTUAL_ACTION;
                } else if (cdao.containsRecurrenceDatePosition()) {
                    rada = RECURRING_VIRTUAL_ACTION;
                }  else {
                    rada = RECURRING_FULL_DELETE;
                }
            } else if (edao.getRecurrenceID() != edao.getObjectID()) {
                // real exception delete
                rada = RECURRING_EXCEPTION_ACTION;
            }
        }
        return rada;
    }
    
    /**
     * <code>getRecurringAppoiontmentUpdateAction</code> detects and returns
     * the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (transfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    public static int getRecurringAppoiontmentUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao) {
        int rada = RECURRING_NO_ACTION;
        /*
         * Check if edao denotes the main recurring appointment
         */
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() == edao.getObjectID()) {
            /*
             * Check if cdao denotes a change exception of a recurring appointment
             */
            if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
                rada = RECURRING_CREATE_EXCEPTION;
            } else if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null) {
                rada = RECURRING_CREATE_EXCEPTION;
            }
            if (cdao.containsDeleteExceptions() && edao.containsChangeExceptions()) {
                if (CalendarCommonCollection.checkIfArrayKeyExistInArray(cdao.getDeleteException(), edao
                        .getChangeException())) {
                    rada = RECURRING_EXCEPTION_DELETE_EXISTING;
                }
            }
        }
        return rada;
    }
    
    /**
     * <code>getLongByPosition</code> return the long value
     * for the given CalendarDataObject and the given recurring
     * position. The method return 0 if the long can not be calculated.
     *
     * @param cdao a <code>CalendarDataObject</code>
     * @param pos a <code>int</code>
     * @return a <code>long</code> value
     */
    public static long getLongByPosition(final CalendarDataObject cdao, final int pos) throws OXException {
        fillDAO(cdao);
        final RecurringResults rrs  = calculateRecurring(cdao, 0, 0, pos);
        if (rrs.size() == 1) {
            final RecurringResult rr = rrs.getRecurringResult(0);
            if (pos == rr.getPosition()) {
                return rr.getNormalized();
            }
            return 0;
        }
        return 0;
    }

    static Date[] mergeExceptions(final Date[] new_dates, final Date[] old_dates) {
        if (new_dates!= null && old_dates == null) {
            return new_dates;
        } else if (new_dates != null && old_dates != null) {
            final Date dates[] = new Date[new_dates.length + old_dates.length];
            System.arraycopy(old_dates, 0, dates, 0, old_dates.length);
            System.arraycopy(new_dates, 0, dates, old_dates.length, new_dates.length);
            return dates;
        }
        return null;
    }

    static void setRecurrencePositionOrDateInDAO(final CalendarDataObject cdao) throws OXException {
        if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
            /*
             * Determine recurrence date position from recurrence position
             */
            fillDAO(cdao);
            final RecurringResults rrs = calculateRecurring(cdao, 0, 0, cdao.getRecurrencePosition());
            final RecurringResult rr = rrs.getRecurringResult(0);
            if (rr != null) {
                cdao.setRecurrenceDatePosition(new Date(rr.getNormalized()));
                return;
            }
        } else if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null) {
            /*
             * Determine recurrence position from recurrence date position
             */
            fillDAO(cdao);

            final long normalized = normalizeLong(cdao.getRecurrenceDatePosition().getTime());
            final long rangeStart = normalized - Constants.MILLI_WEEK;
            final long rangeEnd = normalized + Constants.MILLI_WEEK;

            final RecurringResults rrs = calculateRecurring(cdao, rangeStart, rangeEnd, 0);
            final int x = rrs.getPositionByLong(cdao.getRecurrenceDatePosition().getTime());
            if (x > 0) {
                cdao.setRecurrencePosition(x);
                return;
            }
        }
        throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT);
    }

    /**
     * Removes hours and minutes for the given date.
     * 
     * @param millis
     *            milliseconds since January 1, 1970, 00:00:00 GMT not to exceed
     *            the milliseconds representation for the year 8099. A negative
     *            number indicates the number of milliseconds before January 1,
     *            1970, 00:00:00 GMT.
     * @return The normalized <code>long</code> value
     */
    public static long normalizeLong(final long millis) {
        return millis - (millis % Constants.MILLI_DAY);
    }

    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     * 
     * @param millis
     *            The time millis
     * @param timeZoneID
     *            The time zone ID
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    public static boolean exceedsHourOfDay(final long millis, final String timeZoneID) {
        return exceedsHourOfDay(millis, Tools.getTimeZone(timeZoneID));
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
    public static boolean exceedsHourOfDay(final long millis, final TimeZone zone) {
        final Calendar cal = GregorianCalendar.getInstance(CalendarRecurringCollection.ZONE_UTC);
        cal.setTimeInMillis(millis);
        long hours = cal.get(Calendar.HOUR_OF_DAY) + (zone.getOffset(millis) / CalendarRecurringCollection.MILLI_HOUR);
        return hours >= 24 || hours < 0;
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
    public static boolean fillDAO(final CalendarDataObject cdao) throws OXException {
        if (cdao.getRecurrence() == null || cdao.getRecurrence().indexOf(DELIMITER_PIPE) == -1) {
            if (cdao.getRecurrenceType() == 0) {
                // No recurring appointment
                return false;
            }
            if ((cdao.getInterval() == 0 && cdao.getMonth() == 0) || cdao.getStartDate() == null
                    || cdao.getEndDate() == null) {
                // Insufficient information
                return false;
            }
            cdao.setRecurrence(createDSString(cdao));
        }
        try {
            convertDSString(cdao);
            return true;
        } catch (final OXCalendarException e) {
            LOG.error("fillDAO:convertDSString error: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Creates the recurring string for specified recurring appointment
     * 
     * @param cdao The recurring appointment whose recurring string shall be created
     * @return The recurring string for specified recurring appointment
     * @throws OXException If recurring appointment contains insufficient or invalid recurring information
     */
    public static String createDSString(final CalendarDataObject cdao) throws OXException {
        if (cdao.containsStartDate()) {
            checkRecurring(cdao);
            final StringBuilder recStrBuilder = new StringBuilder(64);
            final int recurrenceType = cdao.getRecurrenceType();
            int interval = cdao.getInterval(); // i
            if (interval > MAXTC) {
                final OXCalendarException exc = new OXCalendarException(Code
                    .RECURRING_VALUE_CONSTRAINT, Integer.valueOf(interval), Integer
                    .valueOf(MAXTC));
                LOG.warn(exc.getMessage() + " Auto-corrected to " + MAXTC, exc);
                interval = MAXTC;
            }
            final int weekdays = cdao.getDays();
            final int monthday = cdao.getDayInMonth();
            final int month = cdao.getMonth();
            int occurrences = cdao.getOccurrence();
            if (occurrences > MAXTC) {
                final OXCalendarException exc = new OXCalendarException(Code
                    .RECURRING_VALUE_CONSTRAINT, Integer.valueOf(occurrences),
                    Integer.valueOf(MAXTC));
                LOG.warn(exc.getMessage() + " Auto-corrected to " + MAXTC, exc);
                occurrences = MAXTC;
            }
            if (!cdao.containsUntil() && !cdao.containsOccurrence()) {
                occurrences = -1;
            }
            if (recurrenceType == CalendarObject.DAILY) {
                dsf(recStrBuilder, 1);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                } else if (occurrences > 0) {
                    cdao.setUntil(getOccurenceDate(cdao));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                }
            } else if (recurrenceType == CalendarObject.WEEKLY) {
                dsf(recStrBuilder, 2);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 'a', weekdays);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                } else if (occurrences > 0) {
                    cdao.setUntil(getOccurenceDate(cdao));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                }
            } else if (recurrenceType == CalendarObject.MONTHLY) {
                if (monthday <= 0) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, Integer.valueOf(monthday));
                }
                if (weekdays <= 0) {
                    if (monthday > 31) {
                        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 3);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    } else if (occurrences > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    }
                } else {
                    if (monthday > 5) {
                        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2, Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 5);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    } else if (occurrences > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    }
                }
            } else if (recurrenceType == CalendarObject.YEARLY) {
                if (weekdays <= 0) {
                    if (monthday <= 0 || monthday > 31) {
                        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 4);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    } else if (occurrences > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    }
                } else {
                    if (monthday < 1 || monthday > 5) {
                        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_TYPE, Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 6);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    } else if (occurrences > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    }
                }
            } else {
                recStrBuilder.append(NO_DS);
            }
            return recStrBuilder.toString();
        }
        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_START_DATE);
    }

    /**
     * Gets the specified occurrence's end date within recurring appointment.
     * 
     * @param cdao The recurring appointment
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    static Date getOccurenceDate(final CalendarDataObject cdao) throws OXException {
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
    static Date getOccurenceDate(final CalendarDataObject cdao, final int occurrence) throws OXException {
        final RecurringResults rss = calculateRecurring(cdao, 0, 0, occurrence, 1, true, true);
        final RecurringResult rs = rss.getRecurringResult(0);
        if (rs != null) {
            return new Date(rs.getEnd());
        }
        LOG.warn("Unable to calculate until date :" + cdao.toString());
        return new Date(cdao.getStartDate().getTime() + Constants.MILLI_YEAR);
    }

    /**
     * Checks if normalized date of given time millis is contained in either specified change exceptions or delete exceptions.
     * 
     * @param t The time millis to check
     * @param ce The change exceptions
     * @param de The delete exceptions
     * @return <code>true</code>if normalized date of given time millis denotes an exception; otherwise <code>false</code>
     */
    public static boolean isException(final long t, final Set<Long> ce, final Set<Long> de) {
        final Long check = Long.valueOf(normalizeLong(t));
        return (null == ce ? false : ce.contains(check)) || (null == de ? false : de.contains(check));
    }

    /**
     * Tests if specified date is covered by any occurrence of given recurring appointment ignoring second specified date.
     * <p>
     * This method is useful when creating a new change exception within specified recurring appointment and checking that change
     * exception's destination date is not already occupied by either a regular recurrence's occurrence. or an existing change exception
     * 
     * @param date The date to check
     * @param ignoreDate The date to ignore
     * @param cdao The recurring appointment to check against
     * @param changeExceptions The recurring appointment's change exception dates
     * @return <code>true</code> if specified time millis is covered by any occurrence; otherwise <code>false</code>
     * @throws OXException If calculating the occurrences fails
     */
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
        final RecurringResults rss = calculateRecurring(cdao, check - Constants.MILLI_WEEK, check + Constants.MILLI_WEEK, 0);
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

    /**
     * This method calculates the first occurrence and stores it within the returned {@link RecurringResults} collection.
     * 
     * @param cdao The recurring appointment whose first occurrence shall be calculated
     * @return The calculated first occurrence kept in a {@link RecurringResults} collection
     * @throws OXException If calculating the first occurrence fails
     */
    public static RecurringResults calculateFirstRecurring(final CalendarObject cdao) throws OXException {
        return calculateRecurring(cdao, 0, 0, 1, MAXTC, true, true);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResults} collection.
     * <p>
     * <b>! This method returns max. {@link #MAXTC} results AND ignores exceptions !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     * 
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences including change/delete exceptions kept in a {@link RecurringResults} collection
     * @throws OXException If calculating the occurrences fails
     */
    public static RecurringResults calculateRecurringIgnoringExceptions(final CalendarObject cdao, final long range_start,final  long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAXTC, true, false);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResults} collection.
     * <p>
     * <b>! This method returns max. {@link #MAXTC} results !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     * 
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences without change/delete exceptions kept in a {@link RecurringResults} collection
     * @throws OXException If calculating the occurrences fails
     */
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAXTC, false, false);
    }
    
    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResults} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     * 
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAXTC}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding occurrences do not appear in returned {@link RecurringResults} collection; otherwise <code>false</code>
     * @return The calculated occurrences kept in a {@link RecurringResults} collection
     * @throws OXException If calculating the occurrences fails
     */
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, PMAXTC, ignore_exceptions, false);
    }
 
    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResults} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     * 
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAXTC}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding occurrences do not appear in returned {@link RecurringResults} collection; otherwise <code>false</code>
     * @param calc_until This parameter is not used, yet
     * @return The calculated occurrences kept in a {@link RecurringResults} collection
     * @throws OXException If calculating the occurrences fails
     */
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions, final boolean calc_until) throws OXException {
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
                    final OXCalendarException e = new OXCalendarException(Code.TIMEZONE_MISSING);
                    LOG.warn(e.getMessage(), e);
                }
            }
            recurringStart = calDataObject.getRecurringStart();
        } else {
            recurringStart = ((cdao.getStartDate().getTime() / Constants.MILLI_DAY) * Constants.MILLI_DAY);
        }

        final RecurringCalculation rc = new RecurringCalculation(cdao.getRecurrenceType(), cdao.getInterval(), cdao
                .getRecurrenceCalculator());
        rc.setCalculationTimeZone(calc_timezone);
        rc.setCalculationPosition(pos);
        rc.setRange(range_start, range_end);
        rc.setMaxCalculation(PMAXTC);
        rc.setMaxOperations(CalendarConfig.getMaxOperationsInRecurrenceCalculations());
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
        try {
            return rc.calculateRecurrence();
        } catch (final RecurringException re) {
            if (re.getCode() == RecurringException.RECURRING_MISSING_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_DAILY_INTERVAL, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_WEEKLY_INTERVAL, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_INTERVAL_2) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL_2, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_DAY) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_DAY_2) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_DAY) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_DAY, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_TYPE) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_TYPE, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.UNEXPECTED_ERROR) {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.UNKOWN_DAYS_VALUE) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_DAY, re, Integer.valueOf(re.getValue()));
            } else if (re.getCode() == RecurringException.PATTERN_TOO_COMPLEX) {
                LOG.error("Pattern too complex for "+cdao);
                throw new OXCalendarException(OXCalendarException.Code.RECURRENCE_PATTERN_TOO_COMPLEX, re);
            } else {
                throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, re, Integer.valueOf(re.getValue()));
            }
        }
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

    /**
     * Maps given day constant from {@link CalendarObject} to the corresponding
     * day from {@link Calendar}.
     * 
     * @param cd
     *            The day constant from {@link CalendarObject}:
     *            {@link CalendarObject#SUNDAY}, {@link CalendarObject#MONDAY},
     *            {@link CalendarObject#TUESDAY},
     *            {@link CalendarObject#WEDNESDAY},
     *            {@link CalendarObject#THURSDAY},{@link CalendarObject#FRIDAY},
     *            {@link CalendarObject#SATURDAY},{@link CalendarObject#DAY},
     *            {@link CalendarObject#WEEKDAY}, or
     *            {@link CalendarObject#WEEKENDDAY}
     * @return The corresponding day from {@link Calendar} or <code>-1</code>.
     */
    private static int getDay(final int cd) {
        final Integer retval = DAY_MAP.get(Integer.valueOf(cd));
        if (retval == null) {
            LOG.error("Unusable getDay parameter (days) :" + cd, new Throwable());
            return -1;
        }
        return retval.intValue();
    }
    
    
    public static void fillMap(final RecurringResults rss, final long s, final long diff, final int d, final int counter) {
        final RecurringResult rs = new RecurringResult(s, diff, d, counter);
        rss.add(rs);
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
    
    static Date calculateRecurringDate(final long date, final long time) {
        return new Date((date - (date % Constants.MILLI_DAY)) + time);
    }

    /**
     * Checks if recurring information provided in specified calendar object is complete.<br>
     * Fields <b>Until</b> and <b>Occurrence</b> may be ignored since an infinite recurring appointment may omit this information.<br>
     * This is the dependency table as defined by {@link #createDSString(CalendarDataObject)}:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Recurrence type</th>
     * <th>Interval</th>
     * <th>Until or Occurrence</th>
     * <th>Weekday</th>
     * <th>Monthday</th>
     * <th>Month</th>
     * </tr>
     * <tr>
     * <td align="center">DAILY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">WEEKLY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * </table>
     * 
     * @param cdao The calendar object to check
     * @param ignoreUntilAndOccurrence <code>true</code> to ignore whether until or occurrence is contained in specified calendar object;
     *            otherwise <code>false</code>
     * @throws OXCalendarException If check fails
     */
    static void checkRecurringCompleteness(final CalendarObject cdao, final boolean ignoreUntilAndOccurrence) throws OXCalendarException {
        if (!cdao.containsRecurrenceType()) {
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_TYPE);
        }
        final int recType = cdao.getRecurrenceType();
        if (CalendarObject.NO_RECURRENCE == recType) {
            return;
        }
        if (!cdao.containsInterval()) {
            /*
             * Every recurrence type needs interval information
             */
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_INTERVAL);
        }
        if (!ignoreUntilAndOccurrence && !cdao.containsOccurrence() && !cdao.containsUntil()) {
            /*
             * Every recurrence type needs at least an until or occurrence information
             */
            throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_UNTIL_OR_OCCUR);
        }
        if (CalendarObject.DAILY == recType) {
            /*
             * Interval and until or occurrence information is sufficient for daily
             */
            return;
        }
        if (CalendarObject.WEEKLY == recType) {
            if (!cdao.containsDays()) {
                /*
                 * Weekday needed for weekly recurrence
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_WEEKDAY);
            }
        } else if (CalendarObject.MONTHLY == recType) {
            if (!cdao.containsDayInMonth()) {
                /*
                 * Monthday needed for monthly recurrence
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_MONTHDAY);
            }
            if (cdao.getDays() > 0 && !cdao.containsDays()) {
                /*
                 * Weekday needed for monthly2 recurrence
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_WEEKDAY);
            }
        } else if (CalendarObject.YEARLY == recType) {
            if (!cdao.containsDayInMonth()) {
                /*
                 * Monthday needed for yearly recurrence
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_MONTHDAY);
            }
            if (!cdao.containsMonth()) {
                /*
                 * Month needed for yearly recurrence
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_MONTH);
            }
            if (cdao.getDays() > 0 && !cdao.containsDays()) {
                /*
                 * Weekday needed for yearly recurrence with weekdays greater than zero
                 */
                throw new OXCalendarException(OXCalendarException.Code.INCOMPLETE_REC_INFOS_WEEKDAY);
            }
        }
    }
    
    public static void checkRecurring(final CalendarObject cdao) throws OXException {
        if (cdao.getInterval() > CalendarRecurringCollection.MAXTC) {
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_VALUE_CONSTRAINT, Integer.valueOf(cdao
                    .getInterval()), Integer.valueOf(CalendarRecurringCollection.MAXTC));
        }
        if (cdao.getOccurrence() > CalendarRecurringCollection.MAXTC) {
            throw new OXCalendarException(OXCalendarException.Code.RECURRING_VALUE_CONSTRAINT, Integer.valueOf(cdao
                    .getOccurrence()), Integer.valueOf(CalendarRecurringCollection.MAXTC));
        }
        if (cdao.getRecurrenceType() == CalendarDataObject.DAILY) {
            if (cdao.getInterval() < 1) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL, Integer.valueOf(cdao.getInterval()));
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL, Integer.valueOf(cdao.getInterval()));
            }
            if (cdao.getDays() < 1) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_DAYS, Integer.valueOf(cdao.getDays()));
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.MONTHLY) {
            if (cdao.containsDays()) {
                if (cdao.getInterval() < 1) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL, Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2, Integer.valueOf(cdao.getDayInMonth()));
                }
            } else {
                if (cdao.getInterval() < 1) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL, Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 999) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, Integer.valueOf(cdao.getDayInMonth()));
                }
            }
        } else if (cdao.getRecurrenceType() == CalendarDataObject.YEARLY) {
            if (cdao.containsDays()) {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_TYPE, Integer.valueOf(cdao.getDayInMonth()));
                }
                if (cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL_2, Integer.valueOf(cdao.getMonth()));
                }
            } else {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 32) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, Integer.valueOf(cdao.getDayInMonth()));
                }
                if (cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_MONTH, Integer.valueOf(cdao.getMonth()));
                }
            }
        }
    }
    
    /**
     * Creates a cloned version from given calendar object ready for being used to create the denoted change exception
     * 
     * @param cdao The current calendar object denoting the change exception
     * @param edao The calendar object's storage version
     * @param sessionUser The session user performing the operation
     * @return A cloned version ready for being used to create the denoted change exception
     * @throws OXException If cloned version cannot be created
     */
    static CalendarDataObject cloneObjectForRecurringException(final CalendarDataObject cdao, final CalendarDataObject edao, final int sessionUser) throws OXException {
        final CalendarDataObject clone = (CalendarDataObject) edao.clone();
        // Recurrence exceptions MUST contain the position and date position.
        // This is necessary for further handling of the series.
        if (cdao.containsRecurrencePosition()) {
            clone.setRecurrencePosition(cdao.getRecurrencePosition());
        }
        if (cdao.containsRecurrenceDatePosition()) {
            clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
        }
        CalendarRecurringCollection.setRecurrencePositionOrDateInDAO(clone);
        /*
         * Check that change exception's date is contained in recurring
         * appointment's range
         */
        if (!CalendarCommonCollection.checkIfDateOccursInRecurrence(clone.getRecurrenceDatePosition(), edao)) {
            throw new OXCalendarException(OXCalendarException.Code.FOREIGN_EXCEPTION_DATE);
        }
        {
            final Date[] newChangeExcs = CalendarCommonCollection.addException(edao.getChangeException(), clone
                    .getRecurrenceDatePosition());
            /*
             * Check that no other change exception exists on specified date;
             * meaning another user already created a change exception on this
             * date in the meantime
             */
            if (Arrays.equals(edao.getChangeException(), newChangeExcs)) {
                throw new OXConcurrentModificationException(EnumComponent.APPOINTMENT,
                        OXConcurrentModificationException.ConcurrentModificationCode.CONCURRENT_MODIFICATION);
            }
            cdao.setChangeExceptions(newChangeExcs);
        }
        CalendarCommonCollection.fillObject(cdao, clone);
        if (cdao.containsModifiedBy()) {
            clone.setCreatedBy(cdao.getModifiedBy());
        } else {
            clone.setCreatedBy(sessionUser);
        }
        if (!cdao.containsUserParticipants() && checkForReconfirmation(cdao, edao)) {
            /*
             * Turn cloned appointment's confirmation information to initial
             * status since obviously no confirmation information were set in
             * cdao that tells us what to do.
             */
            final UserParticipant[] users = clone.getUsers();
            for (final UserParticipant userParticipant : users) {
                if (userParticipant.getIdentifier() == sessionUser) {
                    userParticipant.setConfirm(CalendarDataObject.ACCEPT);
                } else {
                    userParticipant.setConfirm(CalendarDataObject.NONE);
                }
            }
        }
        clone.removeObjectID();
        clone.removeDeleteExceptions();
        clone.removeChangeExceptions();
        // We store the date_position in the exception field
        clone.setChangeExceptions(new java.util.Date[] { clone.getRecurrenceDatePosition() });
        if (!cdao.containsStartDate()  || !cdao.containsEndDate()) {
            // Calculate real times !!!!
            CalendarRecurringCollection.fillDAO(edao);
            final RecurringResults rss = CalendarRecurringCollection.calculateRecurring(edao, 0, 0, clone.getRecurrencePosition());
            if (rss == null) {
                throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION);
            }
            final RecurringResult rs = rss.getRecurringResult(0);
            clone.setStartDate(new Date(rs.getStart()));
            clone.setEndDate(new Date(rs.getEnd()));
        }
        return clone;
    }

    /**
     * Checks if changes reflected through given {@code cdao} enforce a
     * re-confirmation for participants.
     * 
     * @param cdao The calendar data object containing the changes to apply
     * @param edao The storage's calendar data object
     * @return <code>true</code> if changes reflected through given {@code cdao}
     *         enforce a re-confirmation for participants; otherwise
     *         <code>false</code>
     * @throws OXException If calculating occurrence's start/end date fails
     */
    private static boolean checkForReconfirmation(final CalendarDataObject cdao, final CalendarDataObject edao)
            throws OXException {
        final long startDate;
        final long endDate;
        if ((cdao.getRecurrencePosition() > 0 || cdao.getRecurrenceDatePosition() != null)
                && (edao.getObjectID() == edao.getRecurrenceID())) {
            // Calculate occurrence's start/end in recurring appointment
            final RecurringResult rs;
            if (cdao.getRecurrencePosition() > 0) {
                final RecurringResults rrs = CalendarRecurringCollection.calculateRecurringIgnoringExceptions(edao, 0,
                        0, cdao.getRecurrencePosition());
                rs = rrs.getRecurringResult(0);
            } else {
                final long normalized = CalendarRecurringCollection.normalizeLong(cdao.getStartDate().getTime());

                final RecurringResults rrs = CalendarRecurringCollection.calculateRecurringIgnoringExceptions(
                    edao,
                    normalized - Constants.MILLI_WEEK,
                    normalized + Constants.MILLI_WEEK,
                    0);
                final int pos = rrs.getPositionByLong(normalized);
                rs = rrs.getRecurringResult(pos - 1);
            }
            startDate = rs.getStart();
            endDate = rs.getEnd();
        } else {
            startDate = edao.getStartDate().getTime();
            endDate = edao.getEndDate().getTime();
        }

        if (cdao.containsStartDate()
                && CalendarCommonCollection.check(Long.valueOf(cdao.getStartDate().getTime()), Long.valueOf(startDate))) {
            // Start date changed
            return true;
        }
        if (cdao.containsEndDate()
                && CalendarCommonCollection.check(Long.valueOf(cdao.getEndDate().getTime()), Long.valueOf(endDate))) {
            // End date changed
            return true;
        }
        if (cdao.containsFullTime()
                && CalendarCommonCollection.check(Boolean.valueOf(cdao.getFullTime()), Boolean.valueOf(edao
                        .getFullTime()))) {
            // Full-time changed
            return true;
        }
        if (cdao.containsLocation() && CalendarCommonCollection.check(cdao.getLocation(), edao.getLocation())) {
            // Location changed
            return true;
        }
        if (cdao.containsRecurrenceString()
                && CalendarCommonCollection.check(cdao.getRecurrence(), edao.getRecurrence())) {
            // Recurring pattern/type changed
            return true;
        }
        return false;
    }

    /**
     * Replaces the start date and end date of specified recurring appointment
     * with the start date and end date of its first occurrence.
     * <p>
     * <b>Note</b> that neither <i>recurrence position</i> nor <i>recurrence
     * date position</i> is set.
     * 
     * @param appointment The recurring appointment whose start date and end
     *            date shall be replaced
     * @throws OXException If calculating the first occurrence fails
     */
    public static void replaceDatesWithFirstOccurence(final AppointmentObject appointment) throws OXException {
        final RecurringResults results = calculateFirstRecurring(appointment);
        if (0 == results.size()) {
            throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_FIRST_RECURRING);
        }
        final RecurringResult result = results.getRecurringResult(0);
        appointment.setStartDate(new Date(result.getStart()));
        appointment.setEndDate(new Date(result.getEnd()));
    }

    /**
     * Sets the start/end date of specified recurring appointment to its first occurrence. A possible exception is swallowed and recurring
     * information is removed.
     * 
     * @param cdao The recurring appointment whose start/end date shall be set to its first occurrence
     */
    public static void safelySetStartAndEndDateForRecurringAppointment(final CalendarDataObject cdao) {
        if (cdao.getRecurrenceType() != AppointmentObject.NO_RECURRENCE) {
            try {
                final RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(
                    cdao,
                    0,
                    0,
                    1,
                    CalendarRecurringCollection.MAXTC,
                    true);
                final RecurringResult rr = rrs.getRecurringResultByPosition(1);
                if (rr != null) {
                    cdao.setStartDate(new Date(rr.getStart()));
                    cdao.setEndDate(new Date(rr.getEnd()));
                }
            } catch (final OXException x) {
                LOG.error(
                    "Can not load appointment '" + cdao.getTitle() + "' with id " + cdao.getObjectID() + ":" + cdao.getContextID() + " due to invalid recurrence pattern",
                    x);
                CalendarCommonCollection.recoverForInvalidPattern(cdao);
            }
        }
    }
}
