/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
