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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link DateTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DateTool {

    /**
     * US locale - all HTTP dates are in English
     */
    private final static Locale LOCALE_US = Locale.US;

    /**
     * GMT time zone - all HTTP dates are on GMT
     */
    public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
     */
    public final static String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    // format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
    public final static String rfc1036Pattern = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    // format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
    public final static String asctimePattern = "EEE MMM d HH:mm:ss yyyy";

    /**
     * Pattern used for old cookies
     */
    private final static String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

    /**
     * DateFormat to be used to format dates. Called from MessageBytes
     */
    private final static DateFormat rfc1123Format = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);

    /**
     * DateFormat to be used to format old netscape cookies Called from ServerCookie
     */
    private final static DateFormat oldCookieFormat = new SimpleDateFormat(OLD_COOKIE_PATTERN, LOCALE_US);

    private final static DateFormat rfc1036Format = new SimpleDateFormat(rfc1036Pattern, LOCALE_US);

    private final static DateFormat asctimeFormat = new SimpleDateFormat(asctimePattern, LOCALE_US);

    static {
        rfc1123Format.setTimeZone(GMT_ZONE);
        oldCookieFormat.setTimeZone(GMT_ZONE);
        rfc1036Format.setTimeZone(GMT_ZONE);
        asctimeFormat.setTimeZone(GMT_ZONE);
    }

    private static String rfc1123DS;

    private static long rfc1123Sec;

    // Called from MessageBytes.getTime()
    static long parseDate(final MessageBytes value) {
        return parseDate(value.toString());
    }

    // Called from MessageBytes.setTime
    /**
     */
    public static String format1123(final Date d) {
        String dstr = null;
        synchronized (rfc1123Format) {
            dstr = format1123(d, rfc1123Format);
        }
        return dstr;
    }

    public static String format1123(final Date d, final DateFormat df) {
        final long dt = d.getTime() / 1000;
        if ((rfc1123DS != null) && (dt == rfc1123Sec)) {
            return rfc1123DS;
        }
        rfc1123DS = df.format(d);
        rfc1123Sec = dt;
        return rfc1123DS;
    }

    // Called from ServerCookie
    /**
     */
    public static void formatOldCookie(final Date d, final StringBuffer sb, final FieldPosition fp) {
        synchronized (oldCookieFormat) {
            oldCookieFormat.format(d, sb, fp);
        }
    }

    // Called from ServerCookie
    public static String formatOldCookie(final Date d) {
        String ocf = null;
        synchronized (oldCookieFormat) {
            ocf = oldCookieFormat.format(d);
        }
        return ocf;
    }

    /**
     * Called from HttpServletRequest.getDateHeader(). Not efficient - but not very used.
     */
    public static long parseDate(final String dateString) {
        final DateFormat[] format = { rfc1123Format, rfc1036Format, asctimeFormat };
        return parseDate(dateString, format);
    }

    public static long parseDate(final String dateString, final DateFormat[] format) {
        Date date = null;
        for (int i = 0; i < format.length; i++) {
            try {
                date = format[i].parse(dateString);
                return date.getTime();
            } catch (final ParseException e) {
                // Ignore
            } catch (final StringIndexOutOfBoundsException e) {
                // Ignore
            }
        }
        final String msg = "httpDate.pe: " + dateString;
        throw new IllegalArgumentException(msg);
    }

}
