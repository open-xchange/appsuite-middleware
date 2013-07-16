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

import static com.openexchange.tools.servlet.RateLimiter.checkRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.Parameterizable;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.stream.CountingInputStream;

/**
 * {@link CountingHttpServletRequest} - The HTTP Servlet request wrapper aware of <code>"com.openexchange.servlet.maxBodySize"</code>
 * property.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CountingHttpServletRequest implements HttpServletRequest, Parameterizable {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CountingHttpServletRequest.class);
    private static final boolean INFO = LOG.isInfoEnabled();

    private static final String LINE_SEP = System.getProperty("line.separator");

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
            if (INFO) {
                LOG.info(new StringAllocator("Request with IP '").append(servletRequest.getRemoteAddr()).append("' to path '").append(servletRequest.getServletPath()).append("' has been rate limited.").append(LINE_SEP).toString());
            }
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
    public Enumeration<String> getAttributeNames() {
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
    public Enumeration<String> getHeaders(final String name) {
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
    public Enumeration<String> getHeaderNames() {
        return servletRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(final String name) {
        return servletRequest.getIntHeader(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
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
    public Map<String, String[]> getParameterMap() {
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
    public Enumeration<Locale> getLocales() {
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

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
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

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

}
