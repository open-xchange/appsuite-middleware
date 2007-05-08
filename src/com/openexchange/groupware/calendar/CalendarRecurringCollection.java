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
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    
    static int MAXTC = 999;
    private static int NO_END_YEARS = 4;
    
    private static final int days_int[] = { CalendarObject.SUNDAY, CalendarObject.MONDAY, CalendarObject.TUESDAY, CalendarObject.WEDNESDAY, CalendarObject.THURSDAY, CalendarObject.FRIDAY, CalendarObject.SATURDAY };
    
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
            }  else {
                rada = RECURRING_NO_ACTION;
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
    
    private static boolean isException(long t, final String ce, final String de) {
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
        
        RecurringResults rs = null;
        int ds_count = 1;
        long s = cdao.getStartDate().getTime();
        long sr = 0;
        if (cdao instanceof CalendarDataObject) {
            sr = ((CalendarDataObject)cdao).getRecurringStart();
        } else {
            sr = ((s/MILLI_DAY)*MILLI_DAY); // normalization
        }
        final long sst = sr;
        long e = cdao.getEndDate().getTime();
        final long c1 = s % MILLI_DAY;
        final  long c2 = e % MILLI_DAY;
        final  long diff = Math.abs(c2-c1);
        e = cdao.getUntil().getTime();
        
        String change_exceptions = null;
        String delete_exceptions = null;
        String calc_timezone = "UTC";
        if (!ignore_exceptions && cdao instanceof CalendarDataObject) {
            change_exceptions = ((CalendarDataObject)cdao).getExceptions();
            delete_exceptions = ((CalendarDataObject)cdao).getDelExceptions();
            calc_timezone = ((CalendarDataObject)cdao).getTimezone();
        }
        
        final  Calendar calc = Calendar.getInstance(TimeZone.getTimeZone(calc_timezone));
        calc.setFirstDayOfWeek(Calendar.MONDAY); // Make this configurable
        if (cdao.getRecurrenceType() == CalendarObject.DAILY) {
            if (cdao.getInterval() < 1) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_DAILY_INTERVAL, Integer.valueOf(cdao.getInterval()));
            }
            rs = new RecurringResults();
            calc.setTimeInMillis(s);
            while (sr <= e) {
                if (s >= sst && sr <= e) {
                    if (((range_start == 0 && range_end == 0 && pos == 0) || (s >= range_start && s <= range_end) || pos == ds_count)
                    		&& (!isException(sr, change_exceptions, delete_exceptions))) {
                        //if (!isException(sr, change_exceptions, delete_exceptions)) {
                            if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                fillMap(rs, calc.getTimeInMillis(), diff, cdao.getRecurrenceCalculator(), ds_count);   
                            } 
                            if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
                                break;
                            }
                        //}
                    }
                    ds_count++;
                }
                s += cdao.getInterval()*MILLI_DAY;
                sr += cdao.getInterval()*MILLI_DAY;
                calc.add(Calendar.DAY_OF_MONTH, cdao.getInterval());
            }
        }  else if (cdao.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_WEEKLY_INTERVAL, Integer.valueOf(cdao.getInterval()));
            }
            
            calc.setTimeInMillis(s);
            calc.setFirstDayOfWeek(Calendar.MONDAY);
            calc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Set to first day of week for calculation
            s = calc.getTimeInMillis();
            final Calendar calc_weekly = (Calendar)calc.clone();
            
            final int days[] = new int[7];
            int c = 0;
            int u = cdao.getDays();
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
                            LOG.warn("Unusable recurring parameter (days) :"+days_int[x]);
                    }
                    u-=days_int[x];
                }
            }
            rs = new RecurringResults();
            long range = 0;
            final int r[] = new int[c];
            System.arraycopy(days, 0, r, 0, c);
            Arrays.sort(r);
            loop: while (sr <= e) {
                for (int a = 0; a < c; a++) {
                    calc.setTimeInMillis(s);
                    calc.add(Calendar.DAY_OF_MONTH, r[a]);
                    range = calc.getTimeInMillis();
                    if (range >= sst && sr <= e) {
                        if (((range_start == 0 && range_end == 0 && pos == 0) || (range >= range_start && range <= range_end) || pos == ds_count)
                        		&& (!isException(range, change_exceptions, delete_exceptions))) {
                            //if (!isException(range, change_exceptions, delete_exceptions)) {
                                if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                    fillMap(rs, range, diff, cdao.getRecurrenceCalculator(), ds_count);
                                }
                                if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
                                    break loop;
                                }
                            //}
                        }
                        ds_count++;
                    }
                }
                calc_weekly.add(Calendar.WEEK_OF_YEAR, cdao.getInterval());
                s = calc_weekly.getTimeInMillis();
                sr += (MILLI_WEEK*cdao.getInterval());
                calc.add(Calendar.WEEK_OF_YEAR, cdao.getInterval());
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.MONTHLY) {
            rs = new RecurringResults();
            int a = cdao.getDays();
            final int monthly = cdao.getInterval();
            final int day_or_type = cdao.getDayInMonth();
            if (day_or_type == 0) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL,Integer.valueOf(day_or_type));
            }
            if (monthly <= 0) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_INTERVAL_2,Integer.valueOf(monthly));
            }
            if (!cdao.containsDays()) {
                if (cdao.containsOccurrence()) {
                    e += MILLI_MONTH;
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
                            		&& (!isException(s, change_exceptions, delete_exceptions))) {
                                //if (!isException(s, change_exceptions, delete_exceptions)) {
                                    if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                        fillMap(rs, s, diff, cdao.getRecurrenceCalculator(), ds_count);
                                    }
                                    if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
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
                 **/
                
                if (a == -1) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY,Integer.valueOf(a));
                }
                if (day_or_type < 1 || day_or_type > 5) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_MONTLY_DAY_2,Integer.valueOf(day_or_type));
                }
                
                final Calendar helper = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                if (cdao.containsOccurrence()) {
                    e += MILLI_MONTH;
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
                            for (int x = 0; x < 7; x++) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, 1);
                            }                          
                            int counter = 1;
                            for (int y = 0; y < 5; y++) {
                                if (counter >= day_or_type) {
                                    break;
                                }
                                calc.add(Calendar.WEEK_OF_MONTH, 1);
                                counter++;
                            }                            
                        } else {
                            // DAY, WEEKDAY OR WEEKENDDAY
                            if (a == AppointmentObject.DAY) {
                                calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                                calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                                calc.set(Calendar.DAY_OF_MONTH, day_or_type);
                            } else if (a == AppointmentObject.WEEKDAY) {
                                calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                                calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                                calc.set(Calendar.DAY_OF_MONTH, 1);
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, 2 + (day_or_type-1));
                                } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, 1 + (day_or_type-1));
                                }
                            } else if (a == AppointmentObject.WEEKENDDAY) {
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
                            if (a == AppointmentObject.WEEKDAY) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, -2);
                                } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, -1);
                                }
                            } else if (a == AppointmentObject.WEEKENDDAY) {
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
                        		&& (!isException(s, change_exceptions, delete_exceptions))) {
                            //if (!isException(s, change_exceptions, delete_exceptions)) {
                                if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                    fillMap(rs, s, diff, cdao.getRecurrenceCalculator(), ds_count);
                                }
                                if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
                                    break;
                                }
                            //}
                        }
                        ds_count++;
                    }
                    helper.add(Calendar.MONTH, cdao.getInterval());
                    s = helper.getTimeInMillis();
                }
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            rs = new RecurringResults();
            int a = cdao.getDays();
            final int day_or_type = cdao.getDayInMonth();
            final int month = cdao.getMonth();
            
            if (day_or_type == 0) {
                throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL, Integer.valueOf(day_or_type));
            }
            
            if (!cdao.containsDays()) {
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
                            		&& (!isException(s, change_exceptions, delete_exceptions))) {
                                //if (!isException(s, change_exceptions, delete_exceptions)) {
                                    if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                        fillMap(rs, s, diff, cdao.getRecurrenceCalculator(), ds_count);
                                    }
                                    if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
                                        break;
                                    }
                                //}
                            }
                            ds_count++;
                        }
                    }
                    calc.add(Calendar.YEAR, 1);
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
                 **/
                
                if (a == -1) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_DAY,Integer.valueOf(a));
                }
                if (day_or_type < 1 || day_or_type > 5) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_TYPE,Integer.valueOf(day_or_type));
                }
                
                if (cdao.getInterval() < 1) {
                    throw new OXCalendarException(OXCalendarException.Code.RECURRING_MISSING_YEARLY_INTERVAL_2,Integer.valueOf(cdao.getInterval()));
                }
                
                final Calendar helper = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
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
                            for (int x = 0; x < 7; x++) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == a) {
                                    break;
                                }
                                calc.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            for (int y = 0; y < 5; y++) {
                                if (calc.get(Calendar.WEEK_OF_MONTH) >= day_or_type) {
                                    break;
                                }
                                calc.add(Calendar.WEEK_OF_MONTH, 1);
                            }
                        } else {
                            // DAY, WEEKDAY OR WEEKENDDAY
                            if (a == AppointmentObject.DAY) {
                                calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                                calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                                calc.add(Calendar.DAY_OF_MONTH, 0-day_or_type);
                            } else if (a == AppointmentObject.WEEKDAY) {
                                calc.set(Calendar.YEAR, helper.get(Calendar.YEAR));
                                calc.set(Calendar.MONTH, helper.get(Calendar.MONTH));
                                calc.set(Calendar.DAY_OF_MONTH, 1);
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, 2 + (day_or_type-1));
                                } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, 1 + (day_or_type-1));
                                }
                            } else if (a == AppointmentObject.WEEKENDDAY) {
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
                            if (a == AppointmentObject.WEEKDAY) {
                                if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, -2);
                                } else if (calc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                    calc.add(Calendar.DAY_OF_MONTH, -1);
                                }
                            } else if (a == AppointmentObject.WEEKENDDAY) {
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
                        		&& (!isException(s, change_exceptions, delete_exceptions))) {
                            //if (!isException(s, change_exceptions, delete_exceptions)) {
                                if (!cdao.containsOccurrence() || calc_until ||(cdao.containsOccurrence() && ds_count <= cdao.getOccurrence())) {
                                    fillMap(rs, s, diff, cdao.getRecurrenceCalculator(), ds_count);
                                }
                                if (ds_count > PMAXTC || pos == ds_count || (cdao.containsOccurrence() && ds_count == cdao.getOccurrence())) {
                                    break;
                                }
                            //}
                        }
                        ds_count++;
                    }
                    helper.add(Calendar.YEAR, cdao.getInterval());
                    s = helper.getTimeInMillis();
                }
            }
        }
        
        return rs;
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
    
    
    private static void fillMap(final RecurringResults rss, final long s, final long diff, final int d, final int counter) {
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
