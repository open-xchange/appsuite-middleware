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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet.http;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link HttpDateFormatRegistry} - The registry for {@link DateFormat}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpDateFormatRegistry {

    private static final HttpDateFormatRegistry singleton = new HttpDateFormatRegistry();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static HttpDateFormatRegistry getInstance() {
        return singleton;
    }

    private final DateFormat defaultDateFormat;

    private final DateFormat netscapeDateFormat;

    /**
     * Initializes a new {@link HttpDateFormatRegistry}.
     */
    private HttpDateFormatRegistry() {
        super();
        {
            final SimpleDateFormat headerDateFormat = new SimpleDateFormat("EEE',' dd MMMM yyyy HH:mm:ss z", Locale.ENGLISH);
            final DateFormatSymbols dfs = headerDateFormat.getDateFormatSymbols();
            final String[] shortWeekdays = new String[8];
            shortWeekdays[Calendar.SUNDAY] = "Sun";
            shortWeekdays[Calendar.MONDAY] = "Mon";
            shortWeekdays[Calendar.TUESDAY] = "Tue";
            shortWeekdays[Calendar.WEDNESDAY] = "Wed";
            shortWeekdays[Calendar.THURSDAY] = "Thu";
            shortWeekdays[Calendar.FRIDAY] = "Fri";
            shortWeekdays[Calendar.SATURDAY] = "Sat";
            dfs.setShortWeekdays(shortWeekdays);
            final String[] shortMonths = new String[12];
            shortMonths[Calendar.JANUARY] = "Jan";
            shortMonths[Calendar.FEBRUARY] = "Feb";
            shortMonths[Calendar.MARCH] = "Mar";
            shortMonths[Calendar.APRIL] = "April";
            shortMonths[Calendar.MAY] = "May";
            shortMonths[Calendar.JUNE] = "June";
            shortMonths[Calendar.JULY] = "July";
            shortMonths[Calendar.AUGUST] = "Aug";
            shortMonths[Calendar.SEPTEMBER] = "Sep";
            shortMonths[Calendar.OCTOBER] = "Oct";
            shortMonths[Calendar.NOVEMBER] = "Nov";
            shortMonths[Calendar.DECEMBER] = "Dec";
            dfs.setShortMonths(shortMonths);
            headerDateFormat.setDateFormatSymbols(dfs);
            headerDateFormat.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
            defaultDateFormat = headerDateFormat;
        }
        /*
         * Taken from org.apache.commons.httpclient.cookie.NetscapeDraftSpec
         */
        netscapeDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
        netscapeDateFormat.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

    /**
     * Gets the default date format as specified in RFC 822: <code>"EEE',' dd MMMM yyyy HH:mm:ss z"</code>
     * <p>
     * <b>Note</b>: Don't forget to exclusively lock returned {@link DateFormat} instance:
     *
     * <pre>
     * DateFormat df = HttpDateFormatRegistry.getInstance().getDefaultDateFormat();
     * synchronized (df) {
     *     // Use the DateFormat instance...
     * }
     * </pre>
     *
     * @return The default date format
     */
    public DateFormat getDefaultDateFormat() {
        return defaultDateFormat;
    }

    /**
     * Appends specified max-age attribute to given string builder dependent on given user agent.
     *
     * @param maxAgeSecs The max-age seconds
     * @param userAgent The user agent
     * @param composer The string builder to append to
     */
    public void appendCookieMaxAge(final int maxAgeSecs, final String userAgent, final StringBuilder composer) {
        if (null == userAgent) {
            appendNetscapeCookieMaxAge(maxAgeSecs, composer);
        }
        /*
         * TODO: Invoke appendNetscapeCookieMaxAge() or appendRFC2109CookieMaxAge() dependent on user agent
         */
        appendNetscapeCookieMaxAge(maxAgeSecs, composer);
    }

    private static volatile Long zeroMaxAgeSecs;

    private static long zeroMaxAgeSecs() {
        Long tmp = zeroMaxAgeSecs;
        if (null == tmp) {
            synchronized (HttpDateFormatRegistry.class) {
                tmp = zeroMaxAgeSecs;
                if (null == tmp) {
                    final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final boolean b = service.getBoolProperty("com.openexchange.ajp.cookie.enableExactZeroMaxAge", true);
                    tmp = Long.valueOf(b ? 0L : 10000); // 10sec after 01/01/1970
                    zeroMaxAgeSecs = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    /**
     * Appends expiry according to Netscape specification; e.g. <code>"expires=Thu, 26-Apr-2012 18:35:06 GMT"</code>.
     *
     * @param maxAgeSecs The max-age seconds
     * @param composer The composing string builder
     */
    private void appendNetscapeCookieMaxAge(final int maxAgeSecs, final StringBuilder composer) {
        synchronized (netscapeDateFormat) {
            /*
             * expires=Sat, 01-Jan-2000 00:00:00 GMT
             */
            final long millis = maxAgeSecs == 0 ? zeroMaxAgeSecs() : System.currentTimeMillis() + (maxAgeSecs * 1000L);
            composer.append("; expires=").append(netscapeDateFormat.format(new Date(millis)));
        }
    }

    /**
     * Appends expiry according to RFC 2109/RFC 2965. The "Expires" attribute is replaced with "Max-Age" attribute.
     *
     * @param maxAgeSecs The max-age seconds
     * @param composer The composing string builder
     */
    private void appendRFC2109CookieMaxAge(final int maxAgeSecs, final StringBuilder composer) {
        composer.append("; max-age=").append(maxAgeSecs);
    }

}
