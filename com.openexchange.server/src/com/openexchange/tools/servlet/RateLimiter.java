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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.javacodegeeks.concurrent.LRUPolicy;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.Rate.Result;
import com.openexchange.tools.servlet.http.Cookies;

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
            final Map<String, Cookie> cookies = Cookies.cookieMapFor(servletRequest);
            if (null == cookies) {
                return null;
            }
            final Cookie cookie = cookies.get("JSESSIONID");
            return null == cookie ? null : cookie.getValue();
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
            final Map<String, Cookie> cookies = Cookies.cookieMapFor(servletRequest);
            if (null == cookies) {
                return null;
            }
            final Cookie cookie = cookies.get(cookieName);
            return null == cookie ? null : cookie.getValue();
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
                    if (null == service) {
                        // Service not yet available
                        return Collections.emptyList();
                    }
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
                        tmp = Collections.unmodifiableList(list);
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
                    if (null == service) {
                        return false;
                    }
                    tmp = Boolean.valueOf(service.getProperty("com.openexchange.servlet.maxRateConsiderRemotePort", "false"));
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
            final StringAllocator builder = new StringAllocator(256);
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
                    if (null == service) {
                        return null;
                    }
                    final int maxCapacity = service.getIntProperty("com.openexchange.servlet.maxActiveSessions", 250000);
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

    private static volatile Integer maxRate;

    private static int maxRate() {
        Integer tmp = maxRate;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = maxRate;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 1500;
                    }
                    tmp = Integer.valueOf(service.getProperty("com.openexchange.servlet.maxRate", "1500"));
                    maxRate = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer maxRateTimeWindow;

    private static int maxRateTimeWindow() {
        Integer tmp = maxRateTimeWindow;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = maxRateTimeWindow;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        // Service not yet available
                        return 300000;
                    }
                    tmp = Integer.valueOf(service.getProperty("com.openexchange.servlet.maxRateTimeWindow", "300000"));
                    maxRateTimeWindow = tmp;
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
                    if (null == service) {
                        // Service not yet available
                        return false;
                    }
                    tmp = Boolean.valueOf(service.getProperty("com.openexchange.servlet.maxRateOmitLocals", "false"));
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

    /**
     * Checks given request if possibly rate limited.
     *
     * @param servletRequest The request to check
     * @return <code>true</code> if not rate limited; otherwise <code>false</code> if rate limited
     */
    public static boolean checkRequest(final HttpServletRequest servletRequest) {
        int maxRatePerMinute = maxRate();
        if (maxRatePerMinute <= 0) {
            return true;
        }
        final int maxRateTimeWindow = maxRateTimeWindow();
        if (maxRatePerMinute <= 0) {
            return true;
        }
        if (omitLocals() && LOCALS.contains(servletRequest.getServerName())) {
            return true;
        }
        final String userAgent = servletRequest.getHeader("User-Agent");
        if (lenientCheckForUserAgent(userAgent)) {
            return true;
            //maxRatePerMinute <<= 2;
        }
        final ConcurrentMap<Key, Rate> bucketMap = bucketMap();
        if (null == bucketMap) {
            // Not yet fully initialized
            return true;
        }
        final Key key = new Key(servletRequest, userAgent);
        while (true) {
            Rate rate = bucketMap.get(key);
            if (null == rate) {
                final Rate newRate = new Rate(maxRatePerMinute, maxRateTimeWindow, TimeUnit.MILLISECONDS);
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

    // ------------------------- Lenient clients ----------------------------------------- //

    private static interface UserAgentChecker {

        boolean isLenient(String userAgent);
    }

    private static final class StartsWithUserAgentChecker implements UserAgentChecker {

        private final String[] prefixes;

        StartsWithUserAgentChecker(final List<String> prefixes) {
            super();
            final int size = prefixes.size();
            final String[] newArray = new String[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = toLowerCase(prefixes.get(i));
            }
            this.prefixes = newArray;
        }

        @Override
        public boolean isLenient(final String userAgent) {
            final String lc = toLowerCase(userAgent);
            for (final String prefix : prefixes) {
                if (lc.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static final class IgnoreCaseUserAgentChecker implements UserAgentChecker {

        private final String userAgent;

        IgnoreCaseUserAgentChecker(final String userAgent) {
            super();
            this.userAgent = toLowerCase(userAgent);
        }

        @Override
        public boolean isLenient(final String userAgent) {
            return this.userAgent.equals(toLowerCase(userAgent));
        }

    }

    private static final class PatternUserAgentChecker implements UserAgentChecker {

        private final Pattern pattern;

        PatternUserAgentChecker(final String wildcard) {
            super();
            pattern = Pattern.compile(wildcardToRegex(wildcard), Pattern.CASE_INSENSITIVE);
        }

        @Override
        public boolean isLenient(final String userAgent) {
            return pattern.matcher(userAgent).matches();
        }

    }

    private static volatile List<UserAgentChecker> userAgentCheckers;

    private static List<UserAgentChecker> userAgentCheckers() {
        List<UserAgentChecker> tmp = userAgentCheckers;
        if (null == tmp) {
            synchronized (CountingHttpServletRequest.class) {
                tmp = userAgentCheckers;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return Collections.emptyList();
                    }
                    final String sProviders;
                    {
                        final String defaultValue = "\"Open-Xchange .NET HTTP Client*\", \"Open-Xchange USM HTTP Client*\", \"Jakarta Commons-HttpClient*\"";
                        sProviders = service.getProperty("com.openexchange.servlet.maxRateLenientClients", defaultValue);
                    }
                    if (isEmpty(sProviders)) {
                        tmp = Collections.emptyList();
                    } else {
                        final List<UserAgentChecker> list = new LinkedList<UserAgentChecker>();
                        final List<String> startsWiths = new LinkedList<String>();
                        for (final String sChecker : Strings.splitByComma(sProviders)) {
                            String s = unquote(sChecker);
                            if (!isEmpty(s)) {
                                s = s.trim();
                                if (isStartsWith(s)) {
                                    // Starts-with
                                    startsWiths.add(s.substring(0, s.length() - 1));
                                } else if (s.indexOf('*') >= 0 || s.indexOf('?') >= 0) {
                                    // Pattern
                                    list.add(new PatternUserAgentChecker(s));
                                } else {
                                    list.add(new IgnoreCaseUserAgentChecker(s));
                                }
                            }
                        }
                        if (!startsWiths.isEmpty()) {
                            list.add(0, new StartsWithUserAgentChecker(startsWiths));
                        }
                        tmp = list.isEmpty() ? Collections.<UserAgentChecker> emptyList() : (1 == list.size() ? Collections.singletonList(list.get(0)) : Collections.unmodifiableList(list));
                    }
                    userAgentCheckers = tmp;
                }
            }
        }
        return tmp;
    }

    private static boolean lenientCheckForUserAgent(final String userAgent) {
        if (null != userAgent) {
            for (final UserAgentChecker checker : userAgentCheckers()) {
                if (checker.isLenient(userAgent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Converts specified wild-card string to a regular expression */
    static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
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

    /** Checks for starts-with notation */
    private static boolean isStartsWith(final String s) {
        if (!s.endsWith("*")) {
            return false;
        }
        final int mlen = s.length() - 1;
        int pos = s.indexOf("?");
        if (pos >= 0) {
            return false;
        }
        pos = s.indexOf("*");
        if (pos >= 0 && pos < mlen) {
            return false;
        }
        return true;
    }

    /** Removes single or double quotes from a string if its quoted. */
    private static String unquote(final String s) {
        if (!isEmpty(s) && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}
