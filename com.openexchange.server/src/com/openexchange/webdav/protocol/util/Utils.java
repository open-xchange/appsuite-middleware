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

    private static final SimpleDateFormat output_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    static {
        output_format.setTimeZone(TimeZoneUtils.getTimeZone("UTC"));
    }

	public static Date convert(final String s) {
		if(s == null) {
			return null;
		}
		Date date = null;
        // Parsing the HTTP Date
        for (int i = 0; (date == null) && (i < formats.length); i++) {
            try {
                synchronized (formats[i]) {
                    date = formats[i].parse(s);
                }
            } catch (final ParseException e) {
            	// Ignore and try the others
            	LOG.debug("", e);
            }
        }
        return date;
	}

	public static String convert(final Date d) {
		if(d == null) {
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
            default:
                return "Unknown";
        }
	}
}
