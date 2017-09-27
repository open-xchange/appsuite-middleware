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

package com.openexchange.calendar.json.compat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.java.Strings;

/**
 * moved from com.openexchange.calendar.api.CalendarCollection
 *
 * {@link CalendarCollection} - Provides calculation routines for recurring calendar items.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarCollection {

    /**
     * 'UTC' time zone
     */
    public static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");

    /**
     * The maximum supported number of occurrences.
     */
    public final int MAX_OCCURRENCESE = 999;

    /**
     * @deprecated use {@link Constants#MILLI_DAY}.
     */
    @Deprecated
    public final long MILLI_DAY = Constants.MILLI_DAY;

    public boolean inBetween(final long check_start, final long check_end, final long range_start, final long range_end) {
        return (check_start < range_end) && (check_end > range_start);

        /*
         * if (check_start <= range_start && check_start >= range_start) {
         * return true;
         * } else if (check_start >= range_start && check_end <= range_end) {
         * return true;
         * } else if (check_start > range_start && check_end > range_end && check_start < range_end) {
         * return true;
         * } else if (check_start < range_start && check_end > range_start && check_start < range_end) {
         * return true;
         * }
         * return false;
         */
    }

    public Date[] convertString2Dates(final String s) {
        if (s == null) {
            return null;
        } else if (s.length() == 0) {
            return new Date[0];
        }
        final String[] sa = Strings.splitByComma(s);
        final Date dates[] = new Date[sa.length];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = new Date(Long.parseLong(sa[i]));
        }
        return dates;
    }

    public String convertDates2String(final Date[] d) {
        if (d == null || d.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(d.length << 4);
        Arrays.sort(d);
        sb.append(d[0].getTime());
        for (int i = 1; i < d.length; i++) {
            sb.append(',').append(d[i].getTime());
        }
        return sb.toString();
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
    public boolean exceedsHourOfDay(final long millis, final String timeZoneID) {
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
    public boolean exceedsHourOfDay(final long millis, final TimeZone zone) {
        final Calendar cal = Calendar.getInstance(CalendarCollection.ZONE_UTC);
        cal.setTimeInMillis(millis);
        final long hours = cal.get(Calendar.HOUR_OF_DAY) + (zone.getOffset(millis) / Constants.MILLI_HOUR);
        return hours >= 24 || hours < 0;
    }

    public void checkRecurring(final CalendarObject cdao) throws OXException {
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

}
