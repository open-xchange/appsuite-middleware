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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesProvider;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.models.DateTimeData;

/**
 * {@link DateTimeUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public final class DateTimeUtil {

    /**
     * Thread local {@link SimpleDateFormat} using <code>yyyyMMdd'T'HHmmss'Z'</code> as pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> ZULU_FORMATER = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateFormat.setTimeZone(TimeZones.UTC);
            return dateFormat;
        }
    };

    /**
     * Thread local {@link SimpleDateFormat} using <code>yyyyMMdd</code> as pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_FORMATER = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    
    /**
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} in ZULU format
     *
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    public static DateTimeData getZuluDateTime(long millis) {
        DateTimeData result = new DateTimeData();
        result.setTzid("UTC");

        Date date = new Date(millis);
        result.setValue(formatZuluDate(date));

        return result;
    }

    /**
     * Gets the Zulu date from the specified timestamp
     *
     * @param millis The timestamp
     * @return The Zulu {@link Date}
     * @throws ParseException if a parsing error occurs
     */
    public static Date getZuluDate(long millis) throws ParseException {
        return DateTimeUtil.parseZuluDateTime(DateTimeUtil.formatZuluDate(new Date(millis)));
    }

    /**
     * Formats the specified {@link Date} into ZULU format
     *
     * @param date The date to format
     * @return The string representation of the date
     */
    public static String formatZuluDate(Date date) {
        return ZULU_FORMATER.get().format(date);
    }

    /**
     * Parses the specified time string into a {@link Date} object using the ZULU format
     *
     * @param time The time
     * @return The {@link Date}
     * @throws ParseException if a parsing error occurs
     */
    public static Date parseZuluDateTime(String time) throws ParseException {
        return ZULU_FORMATER.get().parse(time);
    }

    /**
     * Parses the specified {@link DateTimeData} into a {@link Date} object using the ZULU format
     *
     * @param time The time
     * @return The {@link Date}
     * @throws ParseException if a parsing error occurs
     */
    public static Date parseZuluDateTime(DateTimeData time) throws ParseException {
        return ZULU_FORMATER.get().parse(time.getValue());
    }

    /**
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} format
     * without the time information
     * 
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    public static DateTimeData getDateTimeWithoutTimeInformation(long millis) {
        DateTimeData result = new DateTimeData();
        Date date = new Date(millis);
        result.setValue(SIMPLE_FORMATER.get().format(date));
        return result;
    }

    /**
     * Parses the specified {@link DateTimeData} object to a {@link Date} object
     *
     * @param time The {@link DateTimeData} object
     * @return The {@link Date} object
     * @throws ParseException if a parsing error occurs
     */
    public static Date parseDateTime(DateTimeData time) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        if (time.getTzid() != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(time.getTzid()));
        }
        return dateFormat.parse(time.getValue());
    }

    /**
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} format
     *
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    public static DateTimeData getDateTime(long millis) {
        return getDateTime(TimeZone.getDefault().getID(), millis);
    }

    /**
     * Parses the specified {@link Calendar} into a proper {@link DateTimeData} format
     *
     * @param cal The {@link Calendar}
     * @return The {@link DateTimeData}
     */
    public static DateTimeData getDateTime(Calendar cal) {
        return getDateTime(cal.getTimeZone().getID(), cal.getTimeInMillis());
    }

    /**
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} format
     * in the specified time-zone.
     *
     * @param timezoneId The time-zone identifier
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    public static DateTimeData getDateTime(String timezoneId, long millis) {
        DateTimeData result = new DateTimeData();
        result.setTzid(timezoneId);

        Date date = new Date(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        if (timezoneId != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezoneId));
        }
        result.setValue(dateFormat.format(date));

        return result;
    }

    /**
     * Adds the specified millisecond timestamp to the specified {@link DateTimeData}
     *
     * @param data The {@link DateTimeData}
     * @param millis The timestamp to add
     * @return The new {@link DateTimeData}
     * @throws ParseException if a parsing error occurs
     */
    public static DateTimeData incrementDateTimeData(DateTimeData data, long millis) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        if (data.getTzid() != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(data.getTzid()));
        }
        Date date = dateFormat.parse(data.getValue());
        return getDateTime(data.getTzid(), date.getTime() + millis);
    }

    /**
     * Adds the specified amount of days in the specified {@link Date} in the specified {@link TimeZone}
     *
     * @param localtimeZone the {@link TimeZone}
     * @param localDate The {@link Date}
     * @param datesToAdd The amount of days to add
     * @return The new {@link Date}
     */
    public static Date incrementDateTimeData(TimeZone localtimeZone, Date localDate, int datesToAdd) {
        Calendar localCalendar = GregorianCalendar.getInstance(localtimeZone);
        localCalendar.setTime(localDate);
        localCalendar.add(Calendar.DAY_OF_YEAR, datesToAdd);

        Calendar utcCalendar = GregorianCalendar.getInstance(TimeZones.UTC);
        utcCalendar.set(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DATE), 0, 0, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        return utcCalendar.getTime();
    }

    /**
     * Returns a {@link Calendar} object with time set to today 12 o clock and timezone set to 'utc'
     *
     * @return The calendar
     */
    public static Calendar getUTCCalendar() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
    
    public static DateTimeData getDateTimeData(String timestamp, String timezoneId) {
        DateTimeData result = new DateTimeData();
        result.setTzid(timezoneId);
        result.setValue(timestamp);
        return result;
    }

    /**
     * Gets the daylight saving date from the specified {@link TimeZone} and the specified year
     *
     * @param tz The {@link TimeZone}
     * @param year The year
     * @return The {@link Calendar} with the daylight saving date
     */
    public static Calendar getDaylightSavingDate(TimeZone tz, int year) {
        Calendar cal = Calendar.getInstance(tz);
        cal.set(year, 1, 1, 0, 0);

        ZoneRules rules = ZoneRulesProvider.getRules(tz.getID(), true);
        ZoneOffsetTransition nextTransition = rules.nextTransition(Instant.ofEpochMilli(cal.getTimeInMillis()));

        cal.setTimeInMillis(nextTransition.getInstant().getEpochSecond() * 1000);
        return cal;
    }

    /**
     * Strips the time information from the specified {@link DateTimeData} and returns
     * a new version of the data
     * 
     * @param data The {@link DateTimeData}
     * @return The new {@link DateTimeData} without the time information
     * @throws ParseException if a parsing error is occurred
     */
    public static DateTimeData stripTimeInformation(DateTimeData data) throws ParseException {
        long timestamp = parseDateTime(data).getTime();
        return getDateTimeWithoutTimeInformation(timestamp);

    }
}
