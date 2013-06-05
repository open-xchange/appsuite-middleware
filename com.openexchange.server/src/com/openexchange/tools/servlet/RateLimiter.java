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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.tools.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.javacodegeeks.concurrent.LRUPolicy;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.Rate.Result;

/**
 * {@link RateLimiter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RateLimiter {

    /**
     * Initializes a new {@link RateLimiter}.
     */
    private RateLimiter() {
        super();
    }

    private static interface KeyPartProvider {

        String getValue(HttpServletRequest servletRequest);
    }

    private static final KeyPartProvider HTTP_SESSION_KEY_PART_PROVIDER = new KeyPartProvider() {

        @Override
        public String getValue(final HttpServletRequest servletRequest) {
            final Cookie[] cookies = servletRequest.getCookies();
            if (null == cookies) {
                return null;
            }
            for (final Cookie cookie : cookies) {
                if ("jsessionid".equals(toLowerCase(cookie.getName()))) {
                    return cookie.getValue();
                }
            }
            return null;
        }
    };

    private static final class CookieKeyPartProvider implements KeyPartProvider {

        private final String cookieName;

        CookieKeyPartProvider(final String cookieName) {
            super();
            this.cookieName = toLowerCase(cookieName);
        }

        @Override
        public String getValue(final HttpServletRequest servletRequest) {
            final Cookie[] cookies = servletRequest.getCookies();
            if (null == cookies) {
                return null;
            }
            for (final Cookie cookie : cookies) {
                if (cookieName.equals(toLowerCase(cookie.getName()))) {
                    return cookie.getValue();
                }
            }
            return null;
        }

    }

    private static final class HeaderKeyPartProvider implements KeyPartProvider {

        private final String headerName;

        HeaderKeyPartProvider(final String headerName) {
            super();
            this.headerName = headerName;
        }

        @Override
        public String getValue(final HttpServletRequest servletRequest) {
            return servletRequest.getHeader(headerName);
        }

    }

    private static final class ParameterKeyPartProvider implements KeyPartProvider {

        private final String paramName;

        ParameterKeyPartProvider(final String paramName) {
            super();
            this.paramName = paramName;
        }

        @Override
        public String getValue(final HttpServletRequest servletRequest) {
            return servletRequest.getParameter(paramName);
        }

    }

    private static volatile List<KeyPartProvider> keyPartProviders;

    static List<KeyPartProvider> keyPartProviders() {
        List<KeyPartProvider> tmp = keyPartProviders;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = keyPartProviders;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final String sProviders = service.getProperty("com.openexchange.servlet.maxRateKeyPartProviders");
                    if (isEmpty(sProviders)) {
                        tmp = Collections.emptyList();
                    } else {
                        final List<KeyPartProvider> list = new LinkedList<KeyPartProvider>();
                        for (final String sProvider : Strings.splitByComma(sProviders)) {
                            final String s = toLowerCase(sProvider);
                            if ("http-session".equals(s)) {
                                list.add(HTTP_SESSION_KEY_PART_PROVIDER);
                            } else if (s.startsWith("cookie-")) {
                                list.add(new CookieKeyPartProvider(s.substring(7)));
                            } else if (s.startsWith("header-")) {
                                list.add(new HeaderKeyPartProvider(s.substring(7)));
                            } else if (s.startsWith("parameter-")) {
                                list.add(new ParameterKeyPartProvider(s.substring(10)));
                            }
                        }
                        tmp = Collections.unmodifiableList(new ArrayList<KeyPartProvider>(list));
                    }
                    keyPartProviders = tmp;
                }
            }
        }
        return tmp;
    }

    private static volatile Boolean considerRemotePort;

    static boolean considerRemotePort() {
        Boolean tmp = considerRemotePort;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = considerRemotePort;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null == service ? "false" : service.getProperty("com.openexchange.servlet.maxRateConsiderRemotePort", "false"));
                    considerRemotePort = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    // ----------------------------------------------------------------------------------- //

    private static final class Key {

        final int remotePort;
        final String remoteAddr;
        final String userAgent;
        final List<String> parts;
        private final int hash;

        Key(final HttpServletRequest servletRequest, final String userAgent) {
            super();
            remotePort = considerRemotePort() ? servletRequest.getRemotePort() : 0;
            remoteAddr = servletRequest.getRemoteAddr();
            this.userAgent = userAgent;

            final List<String> parts;
            {
                final List<KeyPartProvider> keyPartProviders = keyPartProviders();
                if (null == keyPartProviders || keyPartProviders.isEmpty()) {
                    parts = null;
                } else {
                    parts = new ArrayList<String>(keyPartProviders.size());
                    for (final KeyPartProvider keyPartProvider : keyPartProviders) {
                        parts.add(keyPartProvider.getValue(servletRequest));
                    }
                }
            }
            this.parts = parts;

            final int prime = 31;
            int result = 1;
            result = prime * result + ((remoteAddr == null) ? 0 : remoteAddr.hashCode());
            result = prime * result + remotePort;
            result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
            result = prime * result + ((parts == null) ? 0 : parts.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            final Key other = (Key) obj;
            if (remotePort != other.remotePort) {
                return false;
            }
            if (remoteAddr == null) {
                if (other.remoteAddr != null) {
                    return false;
                }
            } else if (!remoteAddr.equals(other.remoteAddr)) {
                return false;
            }
            if (userAgent == null) {
                if (other.userAgent != null) {
                    return false;
                }
            } else if (!userAgent.equals(other.userAgent)) {
                return false;
            }
            if (parts == null) {
                if (other.parts != null) {
                    return false;
                }
            } else if (!parts.equals(other.parts)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringAllocator builder = new StringAllocator(256);
            builder.append("Key [");
            if (remotePort > 0) {
                builder.append("remotePort=").append(remotePort).append(", ");
            }
            if (remoteAddr != null) {
                builder.append("remoteAddr=").append(remoteAddr).append(", ");
            }
            if (userAgent != null) {
                builder.append("userAgent=").append(userAgent).append(", ");
            }
            if (parts != null) {
                builder.append("parts=").append(parts).append(", ");
            }
            builder.append("hash=").append(hash).append("]");
            return builder.toString();
        }


    } // End of class Key

    // ----------------------------------------------------------------------------------- //

    private static volatile ScheduledTimerTask timerTask;
    private static volatile ConcurrentMap<Key, Rate> bucketMap;

    private static ConcurrentMap<Key, Rate> bucketMap() {
        ConcurrentMap<Key, Rate> tmp = bucketMap;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = bucketMap;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final int maxCapacity = null == service ? 250000 : service.getIntProperty("com.openexchange.servlet.maxActiveSessions", 250000);
                    final ConcurrentLinkedHashMap<Key, Rate> tmp2 = new ConcurrentLinkedHashMap<Key, Rate>(256, 0.75F, 16, maxCapacity, new LRUPolicy());
                    tmp = tmp2;
                    bucketMap = tmp;

                    final TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
                    final int delay = 300000; // Delay of 5 minutes
                    final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final long mark = System.currentTimeMillis() - (delay << 1); // Older than doubled delay -- 10 minutes
                                // Iterator obeys LRU policy therefore stop after first non-elapsed entry
                                for (final Iterator<Entry<Key, Rate>> iterator = tmp2.entrySet().iterator(); iterator.hasNext();) {
                                    final Entry<Key, Rate> entry = iterator.next();
                                    if (!entry.getValue().markDeprecatedIfElapsed(mark)) {
                                        break;
                                    }
                                    iterator.remove();
                                }
                            } catch (final Exception e) {
                                // Ignore
                            }
                        }
                    };
                    timerTask = timerService.scheduleWithFixedDelay(r, delay, delay);
                }
            }
        }
        return tmp;
    }

    private static volatile Integer maxRatePerMinute;

    private static int maxRatePerMinute() {
        Integer tmp = maxRatePerMinute;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = maxRatePerMinute;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? "120" : service.getProperty("com.openexchange.servlet.maxRatePerMinute", "120"));
                    maxRatePerMinute = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Boolean omitLocals;

    private static boolean omitLocals() {
        Boolean tmp = omitLocals;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = omitLocals;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null == service ? "false" : service.getProperty("com.openexchange.servlet.maxRateOmitLocals", "false"));
                    omitLocals = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    // ----------------------------------------------------------------------------------- //

    /**
     * Stops scheduled time task.
     */
    public static void stop() {
        final ScheduledTimerTask t = timerTask;
        if (null != t) {
            t.cancel();
            timerTask = null;
        }
    }

    // ----------------------------------------------------------------------------------- //

    private static final Set<String> LOCALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("localhost", "127.0.0.1", "::1")));
    private static final String PARAMETER_SESSION = AJAXServlet.PARAMETER_SESSION;

    /**
     * Checks given request if possibly rate limited.
     *
     * @param servletRequest The request to check
     * @return <code>true</code> if not rate limited; otherwise <code>false</code> if rate limited
     */
    public static boolean checkRequest(final HttpServletRequest servletRequest) {
        int maxRatePerMinute = maxRatePerMinute();
        if (maxRatePerMinute <= 0) {
            return true;
        }
        if (omitLocals() && LOCALS.contains(servletRequest.getServerName())) {
            return true;
        }
        final String userAgent = servletRequest.getHeader("User-Agent");
        if (lenientCheckForUserAgent(userAgent)) {
            maxRatePerMinute <<= 2;
        }
        final ConcurrentMap<Key, Rate> bucketMap = bucketMap();
        final Key key = new Key(servletRequest, userAgent);
        while (true) {
            Rate rate = bucketMap.get(key);
            if (null == rate) {
                final Rate newRate = new Rate(maxRatePerMinute, 60, TimeUnit.SECONDS);
                rate = bucketMap.putIfAbsent(key, newRate);
                if (null == rate) {
                    rate = newRate;
                }
            }
            // Acquire or fails to do so
            final Rate.Result res = rate.consume(System.currentTimeMillis());
            if (Result.DEPRECATED == res) {
                // Deprecated
                bucketMap.remove(key, rate);
            } else {
                return (Result.SUCCESS == res);
            }
            // Otherwise retry
        }
    }

    private static boolean lenientCheckForUserAgent(final String userAgent) {
        if (null != userAgent) {
            final String lc = toLowerCase(userAgent);
            if (lc.startsWith("open-xchange .net http client")) {
                return true;
            }
            if (lc.startsWith("open-xchange usm http client")) {
                return true;
            }
            if (lc.startsWith("jakarta commons-httpclient")) {
                return true;
            }
        }
        return false;
    }

    /** Check for an empty string */
    static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /** ASCII-wise to lower-case */
    static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
