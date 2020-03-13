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

package com.openexchange.chronos.itip.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.service.CalendarParameters;

/**
 * {@link ITipUtils}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipUtils {

    public static long startOfTheDay(Date recurrenceDatePosition) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(recurrenceDatePosition);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long endOfTheDay(Date recurrenceDatePosition) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(recurrenceDatePosition);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    public static CalendarUser getPrincipal(CalendarParameters session) {
        if (session == null) {
            return null;
        }
        // ID is the only value used, so we are fine only checking for this
        if (session.contains(CalendarParameters.PARAMETER_PRINCIPAL_ID)) {
            CalendarUser principal = new CalendarUser();
            principal.setEntity(session.get(CalendarParameters.PARAMETER_PRINCIPAL_ID, Integer.class).intValue());
            return principal;
        }
        return null;
    }
    
    /**
     * Build the value for a header
     * 
     * @param uid The event unique identifier
     * @param timestamp <code>true></code> to add a timestamp to the value, <code>false</code> otherwise
     * @return The header value
     */
    public static String generateHeaderValue(String uid, boolean timestamp) {
        StringBuilder builder = new StringBuilder("<Appointment.");
        builder.append(uid);
        if (timestamp) {
            builder.append(".");
            builder.append(String.valueOf(System.currentTimeMillis()));
        }
        builder.append("@");
        builder.append("open-xchange.com");
        builder.append(">");
        return builder.toString();
    }
}
