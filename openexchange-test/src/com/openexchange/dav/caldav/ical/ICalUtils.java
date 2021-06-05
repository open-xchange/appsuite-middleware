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

package com.openexchange.dav.caldav.ical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;

/**
 * {@link ICalUtils}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ICalUtils {

    public static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String fold(String content) {
        return content.replaceAll(".{75}", "$0\r\n ");
    }

    public static String unfold(String content) {
        return content.replaceAll("(?:\\r\\n?|\\n)[ \t]", "");
    }

    public static Date parseDate(Property property) throws ParseException {
        if (null == property || null == property.getValue()) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        if ("DATE".equals(property.getAttribute("VALUE"))) {
            dateFormat.applyPattern("yyyyMMdd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        } else {
            String tzid = property.getAttribute("TZID");
            if (null != tzid) {
                dateFormat.setTimeZone(TimeZone.getTimeZone(tzid));
                dateFormat.applyPattern("yyyyMMdd'T'HHmmss");
            } else if (property.getValue().endsWith("Z")) {
                dateFormat.applyPattern("yyyyMMdd'T'HHmmss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                dateFormat.applyPattern("yyyyMMdd'T'HHmmss");
            }
        }
        return dateFormat.parse(property.getValue());
    }

    public static String format(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    public static String format(Date date, String timeZoneID) {
        return format(date, TimeZone.getTimeZone(timeZoneID));
    }

    public static List<Date[]> parsePeriods(Property property) throws ParseException {
        List<Date[]> periods = new ArrayList<Date[]>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String[] splitted = property.getValue().split(",");
        for (String value : splitted) {
            if (0 < value.trim().length()) {
                String from = value.substring(0, value.indexOf('/'));
                String until = value.substring(value.indexOf('/') + 1);
                if (until.startsWith("PT")) {
                    throw new UnsupportedOperationException("durations not implemented");
                }
                Date[] period = new Date[2];
                period[0] = dateFormat.parse(from);
                period[1] = dateFormat.parse(until);
                periods.add(period);
            }
        }
        return periods;
    }

    private ICalUtils() {
        // 
    }

}
