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

package com.openexchange.tools.servlet.http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletResponse;

/**
 * Convenience methods for servlets.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Pattern for the HTTP header date format.
     */
    private static final String DATE_PATTERN =
        "EEE',' dd MMMM yyyy HH:mm:ss z";

    /**
     * DateFormat for HTTP header.
     */
    public static final DateFormat HEADER_DATEFORMAT;

    /**
     * Lock for the HTTP header date format.
     */
    private static final Lock DATEFORMAT_LOCK = new ReentrantLock(true);

    /**
     * Cache-Control HTTP header name.
     */
    private static final String CACHE_CONTROL_KEY = "Cache-Control";

    /**
     * First Cache-Control value.
     */
    private static final String CACHE_VALUE1 =
        "no-store, no-cache, must-revalidate";

    /**
     * Second Cache-Control value.
     */
    private static final String CACHE_VALUE2 =
        "post-check=0, pre-check=0";

    /**
     * Expires HTTP header name.
     */
    private static final String EXPIRES_KEY = "Expires";

    /**
     * Expires HTTP header value.
     */
    private static final String EXPIRES_DATE;

    /**
     * Pragma HTTP header key.
     */
    private static final String PRAGMA_KEY = "Pragma";

    /**
     * Pragma HTTP header value.
     */
    private static final String PRAGMA_VALUE = "no-cache";

    /**
     * Prevent instanciation.
     */
    private Tools() {
        super();
    }

    /**
     * The magic spell to disable caching.
     * @param resp the servlet response.
     */
    public static void disableCaching(final HttpServletResponse resp) {
        resp.addHeader(EXPIRES_KEY, EXPIRES_DATE);
        resp.addHeader(CACHE_CONTROL_KEY, CACHE_VALUE1);
        resp.addHeader(CACHE_CONTROL_KEY, CACHE_VALUE2);
        resp.addHeader(PRAGMA_KEY, PRAGMA_VALUE);
    }

    /**
     * Formats a date for http headers.
     * @param date date to format.
     * @return the string with the formated date.
     */
    public static String formatHeaderDate(final Date date) {
        DATEFORMAT_LOCK.lock();
        try {
            return HEADER_DATEFORMAT.format(date);
        } finally {
            DATEFORMAT_LOCK.unlock();
        }
    }

    static {
        HEADER_DATEFORMAT = new SimpleDateFormat(DATE_PATTERN, Locale
            .ENGLISH);
        HEADER_DATEFORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        EXPIRES_DATE = HEADER_DATEFORMAT.format(new Date());
    }
}
