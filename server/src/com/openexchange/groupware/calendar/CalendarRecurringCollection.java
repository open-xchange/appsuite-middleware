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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.recurrence.RecurringCalculation;
import com.openexchange.groupware.calendar.recurrence.RecurringException;
import com.openexchange.groupware.container.CalendarObject;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CalendarRecurringCollection
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public final class CalendarRecurringCollection {
    
    public static final String DELIMETER_PIPE = "|";
    public static final String DELIMETER_COMMA = ",";
    public static final String NO_DS = null;
    
    public static final long MILLI_DAY = 86400000L;
    public static final long MILLI_WEEK = 604800000L;
    public static final long MILLI_MONTH = 2678400000L;
    public static final long MILLI_YEAR = 31536000000L;
    
    public static final int RECURRING_NO_ACTION = 0;
    public static final int RECURRING_VIRTUAL_ACTION = 1;
    public static final int RECURRING_EXCEPTION_ACTION = 2;
    public static final int RECURRING_EXCEPTION_DELETE = 3;
    public static final int RECURRING_FULL_DELETE = 4;
    public static final int RECURRING_CREATE_EXCEPTION = 5;
    public static final int CHANGE_RECURRING_TYPE = 6;
    public static final int RECURRING_EXCEPTION_DELETE_EXISTING = 7;    
    
    static int MAXTC = 999;
    private static int NO_END_YEARS = 4;
    
    
    
    private static final Log LOG = LogFactory.getLog(CalendarRecurringCollection.class);
    
    /**
     * This construtor for the class <code>CalendarRecurringCollection</code> is private
     * because the developer should work with the getInstance() method. This seems to
     * be more flexible in the future if we need to rewrite this ...
     *
     */
    private CalendarRecurringCollection() { }
    
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
     * <code>searchNextToken</code> method.
     *
     * @param ds a <code>String</code> value
     * @param s an <code>int</code> value
     * @param delimeter a <code>String</code> value
     * @return an <code>int</code> value
     */
    private static int searchNextToken(final String ds, final int s, final String delimeter) {
        return ds.indexOf(delimeter, s);
    }
    
    private static void convertDSString(final CalendarDataObject cdao) throws Exception {
        String name;
        String value;
        final String ds = cdao.getRecurrence();
        if (ds != null) {
            int s = 0;
            int f = 0;
            while ((f = searchNextToken(ds, s, DELIMETER_PIPE)) != -1) {
                name = ds.substring(s, f);
                s = f+1;
                f = searchNextToken(ds, s, DELIMETER_PIPE);
                if (f != -1) {
                    value = ds.substring(s, f);
                    encodeNameValuePair(name, value, cdao);
                } else {
                    value = ds.substring(s, ds.length());
                    encodeNameValuePair(name, value, cdao);
                    break;
                }
                s = f+1;
            }
        }
        checkAndCorrectErrors(cdao);
    }
    
    private static void checkAndCorrectErrors(final CalendarDataObject cdao) {
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
    
    private static void encodeNameValuePair(final String name, final String value, final CalendarDataObject cdao) throws Exception {
        if (name.equals("t")) {
            int t = Integer.parseInt(value);
            if (t == 5) { t = 3;
            } else if (t == 6) { t = 4; }
            cdao.setRecurrenceType(t);
        } else if (name.equals("i")) {
            cdao.setInterval(Integer.parseInt(value));
        } else if (name.equals("a")) {
            cdao.setDays(Integer.parseInt(value));
        } else if (name.equals("b")) {
            cdao.setDayInMonth(Integer.parseInt(value));
        } else if (name.equals("c")) {
            cdao.setMonth(Integer.parseInt(value));
        } else if (name.equals("e")) {
            final long u = Long.parseLong(value);
            cdao.setUntil(new java.util.Date(u));
        } else if (name.equals("s")) {
            final long s = Long.parseLong(value);
            cdao.setRecurringStart(s);
        } else if (name.equals("o")) {
            cdao.setOccurrence(Integer.parseInt(value));
        } else {
            throw new Exception("encodeNameValuePair : unknown type : "+name + " : "+value);
        }
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
        if (edao.containsRecurrenceID()) {
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
     * @param cdao a <code>CalendarDataObject</code> object (tranfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    public static int getRecurringAppoiontmentUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao) {
        int rada = RECURRING_NO_ACTION;
        if (edao.containsRecurrenceID()) {
            if (cdao.containsRecurrencePosition()) {
                rada = RECURRING_CREATE_EXCEPTION;
            } else if (cdao.containsRecurrenceDatePosition()) {
                rada = RECURRING_CREATE_EXCEPTION;
            }
            if (cdao.containsDeleteExceptions() && edao.containsChangeExceptions()) {
            		if (CalendarCommonCollection.checkIfArrayKeyExistInArray(cdao.getDeleteException(), edao.getChangeException())) {
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
        if (cdao.containsRecurrencePosition()) {
            fillDAO(cdao);
            final RecurringResults rrs  = calculateRecurring(cdao, 0, 0, cdao.getRecurrencePosition());
            final  RecurringResult rr = rrs.getRecurringResult(0);
            if (rr != null) {
                cdao.setRecurrenceDatePosition(new Date(rr.getNormalized()));
                return;
            }
        } else if (cdao.containsRecurrenceDatePosition()) {
            fillDAO(cdao);
            final RecurringResults rrs  = calculateRecurring(cdao, 0, 0, 0);
            final int x = rrs.getPositionByLong(cdao.getRecurrenceDatePosition().getTime());
            if (x > 0) {
                cdao.setRecurrencePosition(x);
                return;
            }
        }
        throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT);
    }
    
    /**
     * <code>normalizeLong</code> removed hours and minutes
     * for the given long
     *
     * @param l a <code>long</code>
     * @return a <code>long</code> value
     */
    public static long normalizeLong(long l) {
        final long mod = l%MILLI_DAY;
        if (mod == 0) {
            return l;
        }
        l -= mod;
        return l;
    }
    
    /**
     * <code>fillDAO</code> creates and fills needed
     * values for the final calculation.
     *
     * @param cdao a <code>CalendarDataObject</code>
     * @return a <code>boolean</code> value
     */
    public static boolean fillDAO(final CalendarDataObject cdao) throws OXException {
        if (cdao.getRecurrence() == null || cdao.getRecurrence().indexOf(DELIMETER_PIPE) == -1) {
            if (cdao.getRecurrenceType() != 0) {
                if ((cdao.getInterval() != 0 || cdao.getMonth() != 0) && cdao.getStartDate() != null && cdao.getEndDate() != null) {
                    cdao.setRecurrence(createDSString(cdao));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        try {
            convertDSString(cdao);
            return true;
        } catch(final Exception e) {
            LOG.error("fillDAO:convertDSString error :", e);
        }
        return false;
    }
    
    public static String createDSString(final CalendarDataObject cdao) throws OXException {
        if (cdao.containsStartDate()) {
            final StringBuilder sb = new StringBuilder();
            final int t = cdao.getRecurrenceType();
            final int i = cdao.getInterval(); // i
            final int a = cdao.getDays();
            final int b = cdao.getDayInMonth();
            final int c = cdao.getMonth();
            int o = cdao.getOccurrence();
            if (!cdao.containsUntil() && !cdao.containsOccurrence()) {
                o = -1;
            }
            if (t == CalendarObject.DAILY) {
                dsf(sb, "1");
                dsf(sb, "i", i);
                dsf(sb, "s", cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (cdao.containsUntil()) {
                    dsf(sb, "e", cdao.getUntil().getTime());
                } else if (o > 0) {
                    cdao.setUntil(getOccurenceDate(cdao));
                    dsf(sb, "e", cdao.getUntil().getTime());
                    dsf(sb, "o", o);
                }
            } else if (t == CalendarObject.WEEKLY) {
                dsf(sb, "2");
                dsf(sb, "i", i);
                dsf(sb, "a", a);
                dsf(sb, "s", cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (cdao.containsUntil()) {
                    dsf(sb, "e", cdao.getUntil().getTime());
                } else if (o > 0) {
                    cdao.setUntil(getOccurenceDate(cdao));
                    dsf(sb, "e", cdao.getUntil().getTime());
                    dsf(sb, "o", o);
                }
            } else if (t == CalendarObject.MONTHLY) {
                if (a == 0) {
                    dsf(sb, "3");
                    dsf(sb, "i", i);
                    dsf(sb, "b", b);
                    dsf(sb, "s", cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil()) {
                        dsf(sb, "e", cdao.getUntil().getTime());
                    } else if (o > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(sb, "e", cdao.getUntil().getTime());
                        dsf(sb, "o", o);
                    }
                } else {
                    dsf(sb, "5");
                    dsf(sb, "i", i);
                    dsf(sb, "a", a);
                    dsf(sb, "b", b);
                    dsf(sb, "s", cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil()) {
                        dsf(sb, "e", cdao.getUntil().getTime());
                    } else if (o > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(sb, "e", cdao.getUntil().getTime());
                        dsf(sb, "o", o);
                    }
                }
            } else if (t == CalendarObject.YEARLY) {
                if (a == 0) {
                    dsf(sb, "4");
                    dsf(sb, "i", i);
                    dsf(sb, "b", b);
                    dsf(sb, "c", c);
                    dsf(sb, "s", cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil()) {
                        dsf(sb, "e", cdao.getUntil().getTime());
                    } else if (o > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(sb, "e", cdao.getUntil().getTime());
                        dsf(sb, "o", o);
                    }
                } else {
                    dsf(sb, "6");
                    dsf(sb, "i", i);
                    dsf(sb, "a", a);
                    dsf(sb, "b", b);
                    dsf(sb, "c", c);
                    dsf(sb, "s", cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (cdao.containsUntil()) {
                        dsf(sb, "e", cdao.getUntil().getTime());
                    } else if (o > 0) {
                        cdao.setUntil(getOccurenceDate(cdao));
                        dsf(sb, "e", cdao.getUntil().getTime());
                        dsf(sb, "o", o);
                    }
                }
            } else {
                sb.append(NO_DS);
            }
            return sb.toString();
        }
        throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_START_DATE);
    }
    
    static Date getOccurenceDate(final CalendarDataObject cdao) throws OXException {
        final RecurringResults rss = calculateRecurring(cdao, 0, 0, cdao.getOccurrence(), 1, true, true);
        final RecurringResult rs = rss.getRecurringResult(0);
        if (rs != null) {
            final long result = rs.getEnd();
            return new Date(result);
        }
        LOG.warn("Unable to calculate until date :"+cdao.toString());
        return new Date(cdao.getStartDate().getTime() + MILLI_YEAR);
    }
    
    public static boolean isException(long t, final String ce, final String de) {
        if (ce == null && de == null)  { return false;
        } else if (ce != null && de != null) {
            t = normalizeLong(t);
            final String check = ""+t;
            if (ce.indexOf(check) != -1 || (de.indexOf(check) != -1)) {
                return true;
            }
        } else if (ce != null) { t = normalizeLong(t); return (ce.indexOf(""+t) != -1);
        } else if (de != null) { t = normalizeLong(t); return (de.indexOf(""+t) != -1); }
        return false;
    }
    
    
    /**
     * <code>calculateFirstRecurring</code>
     * This method calculates the recurring occurrences
     *
     * You can request a range query by giving start and end times
     * by submitting the range_start/range_end parameters
     * and you can also request only one special position by filling the
     * parameter pos
     *
     * ! This method returns max. "MAXTC" results AND ignores exceptions !
     *
     * @param cdao a <code>CalendarDataObject</code> value
     * @return a <code>RecurringResults</code> object that can be iterated
     */
    public static RecurringResults calculateFirstRecurring(final CalendarObject cdao) throws OXException {
        return calculateRecurring(cdao, 0, 0, 1, MAXTC, true, true);
    }
    
    /**
     * <code>calculateRecurringIgnoringExceptions</code>
     * This method calculates the recurring occurrences
     *
     * You can request a range query by giving start and end times
     * by submitting the range_start/range_end parameters
     * and you can also request only one special position by filling the
     * parameter pos
     *
     * ! This method returns max. "MAXTC" results AND ignores exceptions !
     *
     * @param cdao a <code>CalendarDataObject</code> value
     * @param range_start <code>long</code> value
     * @param range_end a <code>long</code> value
     * @param pos a <code>int</code> value
     * @return a <code>RecurringResults</code> object that can be iterated
     */
    public static RecurringResults calculateRecurringIgnoringExceptions(final CalendarObject cdao, final long range_start,final  long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAXTC, true, false);
    }
    
    /**
     * <code>calculateRecurring</code>
     * This method calculates the recurring occurrences
     *
     * You can request a range query by giving start and end times
     * by submitting the range_start/range_end parameters
     * and you can also request only one special position by filling the
     * parameter pos
     *
     * ! This method returns max. "MAXTC" results !
     *
     * @param cdao a <code>CalendarDataObject</code> value
     * @param range_start <code>long</code> value
     * @param range_end a <code>long</code> value
     * @param pos a <code>int</code> value
     * @return a <code>RecurringResults</code> object that can be iterated
     */
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAXTC, false, false);
    }
    
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAXTC, ignore_exceptions, false);
    }
    
    /**
     * <code>calculateRecurring</code>
     * This method calculates the recurring occurrences
     *
     * You can request a range query by giving start and end times
     * by submitting the range_start/range_end parameters
     * and you can also request only one special position by filling the
     * parameter pos
     *
     * ! This method returns max. "PMAXTC" results !
     *
     * @param cdao a <code>CalendarDataObject</code> value
     * @param range_start <code>long</code> value
     * @param range_end a <code>long</code> value
     * @param pos a <code>int</code> value
     * @param PMAXTC a <code>int</code> value
     * @param ignore_exceptions a <code>boolean</code> value
     * @return a <code>RecurringResults</code> object to be iterated
     */
    public static RecurringResults calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions, final boolean calc_until) throws OXException {
        
        String change_exceptions = null;
        String delete_exceptions = null;
        String calc_timezone = "UTC";
        if (cdao instanceof CalendarDataObject) {
            if (!ignore_exceptions) {
                change_exceptions = ((CalendarDataObject)cdao).getExceptions();
                delete_exceptions = ((CalendarDataObject)cdao).getDelExceptions();
            }
            if (((CalendarDataObject)cdao).getFullTime() != true) {
                calc_timezone = ((CalendarDataObject)cdao).getTimezone();
            } 
        }
        
        RecurringCalculation rc = new RecurringCalculation(cdao.getRecurrenceType(), cdao.getInterval(), cdao.getRecurrenceCalculator());
        rc.setCalculationTimeZone(calc_timezone);
        rc.setCalculationPosition(pos);
        rc.setRange(range_start, range_end);
        rc.setMaxCalculation(PMAXTC);
        rc.setExceptions(change_exceptions, delete_exceptions);
        rc.setStartAndEndTime(cdao.getStartDate().getTime(), cdao.getEndDate().getTime());        
        if (cdao instanceof CalendarDataObject) {
           rc.setRecurringStart(((CalendarDataObject)cdao).getRecurringStart());
        } else {
            rc.setRecurringStart(((cdao.getStartDate().getTime()/MILLI_DAY)*MILLI_DAY));
        }

        if (cdao.containsUntil()) {
            rc.setUntil(cdao.getUntil().getTime()); 
        }
        
        if (cdao.containsOccurrence()) {
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
        } catch (RecurringException re) {
            if (re.getCode() == RecurringException.RECURRING_MISSING_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_DAILY_INTERVAL, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_WEEKLY_INTERVAL, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_INTERVAL_2) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL_2, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_DAY) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_MONTLY_DAY_2) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_INTERVAL) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_DAY) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_DAY, re.getValue());
            } else if (re.getCode() == RecurringException.RECURRING_MISSING_YEARLY_TYPE) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_TYPE, re.getValue());
            } else if (re.getCode() == RecurringException.UNEXPECTED_ERROR) {
            	throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, re, re.getValue());
            }
            
        }
        
        return null;

    }
    
    private static int getDay(final int cd) {
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
                LOG.warn("Unusable getDay parameter (days) :"+cd);
        }
        return ret;
    }
    
    
    public static void fillMap(final RecurringResults rss, final long s, final long diff, final int d, final int counter) {
        final RecurringResult rs = new RecurringResult(s, diff, d, counter);
        rss.add(rs);
    }
    
    private static void dsf(final StringBuilder sb, final String s, final int v) {
        if (v >= 0) {
            sb.append(s);
            sb.append(DELIMETER_PIPE);
            sb.append(v);
            sb.append(DELIMETER_PIPE);
        }
    }
    
    private static void dsf(final StringBuilder sb, final String s, final long l) {
        sb.append(s);
        sb.append(DELIMETER_PIPE);
        sb.append(l);
        sb.append(DELIMETER_PIPE);
    }
    
    private static void dsf(final StringBuilder sb, final String s) {
        sb.append('t');
        sb.append(DELIMETER_PIPE);
        sb.append(s);
        sb.append(DELIMETER_PIPE);
    }
    
    static Date calculateRecurringDate(long date, final long time) {
        final long diff = date%MILLI_DAY;
        date -= diff;
        date += time;
        return new Date(date);
    }
    
    public static void checkRecurring(final CalendarObject cdao) throws OXException {
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
    
    static CalendarDataObject cloneObjectForRecurringException(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        final CalendarDataObject clone = (CalendarDataObject)edao.clone();
        if (cdao.containsRecurrencePosition()) {
            clone.setRecurrencePosition(cdao.getRecurrencePosition());
        }
        if (cdao.containsRecurrenceDatePosition()) {
            clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
        }
        CalendarRecurringCollection.setRecurrencePositionOrDateInDAO(clone);
        cdao.setChangeExceptions(new java.util.Date[] { clone.getRecurrenceDatePosition() });
        CalendarCommonCollection.fillObject(cdao, clone);
        clone.removeObjectID();
        clone.removeDeleteExceptions();
        clone.removeChangeExceptions();
        clone.setChangeExceptions(new java.util.Date[] { clone.getRecurrenceDatePosition() }); // We store the date_position in the exception field
        if (!cdao.containsStartDate()  || !cdao.containsEndDate()) {
            // Calculate real times !!!!
            CalendarRecurringCollection.fillDAO(edao);
            final RecurringResults rss = CalendarRecurringCollection.calculateRecurring(edao, 0, 0, clone.getRecurrencePosition());
            if (rss != null) {
                final RecurringResult rs = rss.getRecurringResult(0);
                clone.setStartDate(new Date(rs.getStart()));
                clone.setEndDate(new Date(rs.getEnd()));
            } else {
                throw new OXCalendarException(OXCalendarException.Code.UNABLE_TO_CALCULATE_RECURRING_POSITION);
            }
        }
        return clone;
    }
    
}
