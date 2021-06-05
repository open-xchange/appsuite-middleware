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

package com.openexchange.webdav.protocol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.tools.TimeZoneUtils;

public class Utils {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utils.class);

	// Taken from slide
	/**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     */
    private static final SimpleDateFormat formats[] = {
    	 new SimpleDateFormat("EEE, d MMM yyyy kk:mm:ss z", Locale.US),
         new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
         new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
         new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
         new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
         new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
         new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)
    };

    private static final SimpleDateFormat output_format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    static {
        output_format.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

	public static Date convert(final String s) {
		if (s == null) {
			return null;
		}
		Date date = null;
        // Parsing the HTTP Date
        for (int i = 0; (date == null) && (i < formats.length); i++) {
            try {
                synchronized (formats[i]) {
                    date = formats[i].parse(s);
                }
            } catch (ParseException e) {
            	// Ignore and try the others
            	LOG.debug("", e);
            }
        }
        return date;
	}

	public static String convert(final Date d) {
		if (d == null) {
			return null;
		}
		synchronized(output_format) {
			return output_format.format(d);
		}
	}

	public static String getStatusString(final int s) {
        switch (s) {
            case HttpServletResponse.SC_OK:
                return "OK";
            case HttpServletResponse.SC_NOT_FOUND:
                return "NOT FOUND";
            case HttpServletResponse.SC_FORBIDDEN:
                return "FORBIDDEN";
            case HttpServletResponse.SC_CONFLICT:
                return "CONFLICT";
            case 507:
                return "Insufficient Storage";
            default:
                return "Unknown";
        }
	}
}
