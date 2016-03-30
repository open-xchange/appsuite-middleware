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

package com.openexchange.groupware.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TimeTools {

    /**
     * Prevent instantiation.
     */
    private TimeTools() {
        super();
    }

    /**
     * @deprecated use {@link #getHour(int, TimeZone)}
     */
    @Deprecated
    public static long getHour(final int diff) {
        return (System.currentTimeMillis() / 3600000 + diff) * 3600000;
    }

    public static long getHour(final int diff, final TimeZone tz) {
        final Calendar calendar = new GregorianCalendar(tz);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, diff);
        return calendar.getTimeInMillis();
    }

    /**
     * Creates a new calendar and sets it to the last current full hour.
     *
     * @param tz TimeZone.
     * @return a calendar set to last full hour.
     */
    public static Calendar createCalendar(final TimeZone tz) {
        final Calendar calendar = new GregorianCalendar(tz);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private static final String[] patterns = { "dd/MM/yyyy HH:mm", "dd.MM.yyyy HH:mm", "yyyy-MM-dd HH:mm" };

    public static Calendar createCalendar(TimeZone tz, int year, int month, int day, int hour) {
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Date D(final String value, TimeZone timeZone) {
        for (String fallbackPattern : patterns) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(fallbackPattern);
                if (null != timeZone) {
                    sdf.setTimeZone(timeZone);
                }
                return sdf.parse(value);
            } catch (ParseException e) {
                // let Chronic have a try then
            }
        }

        Date date = null;
        final Span span = Chronic.parse(value);
        if (null == span) {
            return null;
        }

        date = span.getBeginCalendar().getTime();

        if (null != timeZone) {
            date = applyTimeZone(timeZone, date);
        }

        return date;

    }

    public static Date D(final String date) {
        return D(date, TimeZone.getTimeZone("UTC"));
    }


    public static Date applyTimeZone(final TimeZone timeZone, final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        final String dateString = sdf.format(date);
        sdf.setTimeZone(timeZone);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date removeMilliseconds(final Date roundme) {
        long timestamp = roundme.getTime();
        timestamp /= 1000;
        timestamp *= 1000;
        return new Date(timestamp);
    }
}
