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

package com.openexchange.ajax.chronos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.models.DateTimeData;

/**
 * {@link DateTimeUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class DateTimeUtil {

    /**
     * Thread local {@link SimpleDateFormat} using <code>yyyyMMdd'T'HHmmss</code> as pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> BASIC_FORMATER = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            dateFormat.setTimeZone(TimeZones.UTC);
            return dateFormat;
        }
    };

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
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} in ZULU format
     * 
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    static DateTimeData getZuluDateTime(long millis) {
        DateTimeData result = new DateTimeData();
        result.setTzid("UTC");

        Date date = new Date(millis);
        result.setValue(ZULU_FORMATER.get().format(date));

        return result;
    }

    /**
     * Parses the specified millisecond timestamp into a proper {@link DateTimeData} format
     * 
     * @param millis The millisecond timestamp
     * @return The {@link DateTimeData}
     */
    static DateTimeData getDateTime(long millis) {
        return getDateTime(TimeZone.getDefault().getID(), millis);
    }

    /**
     * Parses the specified {@link Calendar} into a proper {@link DateTimeData} format
     * 
     * @param cal The {@link Calendar}
     * @return The {@link DateTimeData}
     */
    static DateTimeData getDateTime(Calendar cal) {
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
    static DateTimeData getDateTime(String timezoneId, long millis) {
        DateTimeData result = new DateTimeData();
        result.setTzid(timezoneId);

        Date date = new Date(millis);
        result.setValue(BASIC_FORMATER.get().format(date));

        return result;
    }
}
