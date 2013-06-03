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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.stream.CountingInputStream;

/**
 * {@link CountingHttpServletRequest} - The HTTP Servlet request wrapper aware of <code>"com.openexchange.servlet.maxBodySize"</code>
 * property.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CountingHttpServletRequest implements HttpServletRequest, Parameterizable {

    private static final class Key {

        final int remotePort;
        final String remoteAddr;
        final String userAgent;
        private final int hash;

        Key(final HttpServletRequest servletRequest) {
            super();
            this.remotePort = servletRequest.getRemotePort();
            this.remoteAddr = servletRequest.getRemoteAddr();
            this.userAgent = servletRequest.getHeader("User-Agent");

            final int prime = 31;
            int result = 1;
            result = prime * result + ((remoteAddr == null) ? 0 : remoteAddr.hashCode());
            result = prime * result + remotePort;
            result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
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
            return true;
        }

    }

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
                    final ConcurrentMap<Key, Rate> tmp2 = new ConcurrentLinkedHashMap.Builder<Key, Rate>().maximumWeightedCapacity(maxCapacity).weigher(Weighers.entrySingleton()).build();
                    tmp = tmp2;
                    bucketMap = tmp;

                    final TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
                    final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final long mark = System.currentTimeMillis() - 600000; // Older than 10 minutes
                                final Iterator<Entry<Key, Rate>> iterator = tmp2.entrySet().iterator();
                                while (iterator.hasNext()) {
                                    final Entry<Key, Rate> entry = iterator.next();
                                    if (entry.getValue().lastAccessTime() < mark) {
                                        iterator.remove();
                                    }
                                }
                            } catch (final Exception e) {
                                // Ignore
                            }
                        }
                    };
                    timerTask = timerService.scheduleWithFixedDelay(r, 300000, 300000);
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
                    tmp = Integer.valueOf(null == service ? "300" : service.getProperty("com.openexchange.servlet.maxRatePerMinute", "300"));
                    maxRatePerMinute = tmp;
                }
            }
        }
        return tmp.intValue();
    }

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

    private static boolean checkRequest(final HttpServletRequest servletRequest) {
        final int maxRatePerMinute = maxRatePerMinute();
        if (maxRatePerMinute <= 0) {
            return true;
        }
        final ConcurrentMap<Key, Rate> bucketMap = bucketMap();
        final Key key = new Key(servletRequest);
        Rate rate = bucketMap.get(key);
        if (null == rate) {
            final Rate newLeakyBucket = new Rate(maxRatePerMinute, 60, TimeUnit.SECONDS);
            rate = bucketMap.putIfAbsent(key, newLeakyBucket);
            if (null == rate) {
                rate = newLeakyBucket;
            }
        }
        // Acquire or fails to do so
        return rate.consume(System.currentTimeMillis());
    }

    // ---------------------------------------------------------------------------------- //

    private final HttpServletRequest servletRequest;
    private final long max;
    private final Parameterizable parameterizable;
    private volatile ServletInputStream servletInputStream;

    /**
     * Initializes a new {@link CountingHttpServletRequest}.
     *
     * @throws RateLimitedException If associated request is rate limited
     */
    public CountingHttpServletRequest(final HttpServletRequest servletRequest) {
        this(servletRequest, ConfigTools.getLongProperty(
            "com.openexchange.servlet.maxBodySize",
            104857600L,
            ServerServiceRegistry.getInstance().getService(ConfigurationService.class)));
    }

    /**
     * Initializes a new {@link CountingHttpServletRequest}.
     *
     * @throws RateLimitedException If associated request is rate limited
     */
    public CountingHttpServletRequest(final HttpServletRequest servletRequest, final long max) {
        super();
        if (!checkRequest(servletRequest)) {
            throw new RateLimitedException("429 Too Many Requests");
        }
        this.max = max;
        this.servletRequest = servletRequest;
        parameterizable = servletRequest instanceof Parameterizable ? (Parameterizable) servletRequest : null;
    }

    @Override
    public void putParameter(final String name, final String value) {
        if (null != parameterizable) {
            parameterizable.putParameter(name, value);
        }
    }

    @Override
    public Object getAttribute(final String name) {
        return servletRequest.getAttribute(name);
    }

    @Override
    public String getAuthType() {
        return servletRequest.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return servletRequest.getCookies();
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return servletRequest.getAttributeNames();
    }

    @Override
    public long getDateHeader(final String name) {
        return servletRequest.getDateHeader(name);
    }

    @Override
    public String getCharacterEncoding() {
        return servletRequest.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
        servletRequest.setCharacterEncoding(env);
    }

    @Override
    public String getHeader(final String name) {
        return servletRequest.getHeader(name);
    }

    @Override
    public int getContentLength() {
        return servletRequest.getContentLength();
    }

    @Override
    public String getContentType() {
        return servletRequest.getContentType();
    }

    @Override
    public Enumeration<?> getHeaders(final String name) {
        return servletRequest.getHeaders(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (max <= 0) {
            return servletRequest.getInputStream();
        }
        ServletInputStream tmp = servletInputStream;
        if (null == tmp) {
            synchronized (servletRequest) {
                tmp = servletInputStream;
                if (null == tmp) {
                    servletInputStream = tmp = new DelegateServletInputStream(new CountingInputStream(servletRequest.getInputStream(), max));
                }
            }
        }
        return tmp;
    }

    @Override
    public String getParameter(final String name) {
        return servletRequest.getParameter(name);
    }

    @Override
    public Enumeration<?> getHeaderNames() {
        return servletRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(final String name) {
        return servletRequest.getIntHeader(name);
    }

    @Override
    public Enumeration<?> getParameterNames() {
        return servletRequest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(final String name) {
        return servletRequest.getParameterValues(name);
    }

    @Override
    public String getMethod() {
        return servletRequest.getMethod();
    }

    @Override
    public String getPathInfo() {
        return servletRequest.getPathInfo();
    }

    @Override
    public Map<?, ?> getParameterMap() {
        return servletRequest.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return servletRequest.getProtocol();
    }

    @Override
    public String getPathTranslated() {
        return servletRequest.getPathTranslated();
    }

    @Override
    public String getScheme() {
        return servletRequest.getScheme();
    }

    @Override
    public String getServerName() {
        return servletRequest.getServerName();
    }

    @Override
    public String getContextPath() {
        return servletRequest.getContextPath();
    }

    @Override
    public int getServerPort() {
        return servletRequest.getServerPort();
    }

    @Override
    public String getQueryString() {
        return servletRequest.getQueryString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return servletRequest.getReader();
    }

    @Override
    public String getRemoteUser() {
        return servletRequest.getRemoteUser();
    }

    @Override
    public String getRemoteAddr() {
        return servletRequest.getRemoteAddr();
    }

    @Override
    public boolean isUserInRole(final String role) {
        return servletRequest.isUserInRole(role);
    }

    @Override
    public String getRemoteHost() {
        return servletRequest.getRemoteHost();
    }

    @Override
    public Principal getUserPrincipal() {
        return servletRequest.getUserPrincipal();
    }

    @Override
    public void setAttribute(final String name, final Object o) {
        servletRequest.setAttribute(name, o);
    }

    @Override
    public String getRequestedSessionId() {
        return servletRequest.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return servletRequest.getRequestURI();
    }

    @Override
    public void removeAttribute(final String name) {
        servletRequest.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return servletRequest.getLocale();
    }

    @Override
    public StringBuffer getRequestURL() {
        return servletRequest.getRequestURL();
    }

    @Override
    public Enumeration<?> getLocales() {
        return servletRequest.getLocales();
    }

    @Override
    public String getServletPath() {
        return servletRequest.getServletPath();
    }

    @Override
    public boolean isSecure() {
        return servletRequest.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return servletRequest.getRequestDispatcher(path);
    }

    @Override
    public HttpSession getSession(final boolean create) {
        return servletRequest.getSession(create);
    }

    @Override
    public String getRealPath(final String path) {
        return servletRequest.getRealPath(path);
    }

    @Override
    public HttpSession getSession() {
        return servletRequest.getSession();
    }

    @Override
    public int getRemotePort() {
        return servletRequest.getRemotePort();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return servletRequest.isRequestedSessionIdValid();
    }

    @Override
    public String getLocalName() {
        return servletRequest.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return servletRequest.getLocalAddr();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return servletRequest.isRequestedSessionIdFromCookie();
    }

    @Override
    public int getLocalPort() {
        return servletRequest.getLocalPort();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return servletRequest.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return servletRequest.isRequestedSessionIdFromUrl();
    }

}
