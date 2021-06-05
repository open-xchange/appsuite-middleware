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

package com.openexchange.http.grizzly.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import org.glassfish.grizzly.http.server.OXRequest;
import org.glassfish.grizzly.http.server.util.SimpleDateFormats;
import org.glassfish.grizzly.http.util.FastHttpDateFormat;
import org.glassfish.grizzly.servlet.ServletUtils;
import com.google.common.collect.ImmutableMap;
import com.openexchange.dispatcher.Headerizable;
import com.openexchange.dispatcher.Parameterizable;

public class HttpServletRequestWrapper implements HttpServletRequest, Parameterizable, Headerizable {

    private static final String ABSENT = new String(new char[] {'_','_','a', 'b', 's', 'e', 'n', 't'});

    /** The protocol scheme for HTTP */
    public final static String HTTP_SCHEME = "http";

    /** The protocol scheme for HTTPS */
    public final static String HTTPS_SCHEME = "https";

    private final HttpServletRequest delegate;
    private final String requestScheme;
    private final int serverPort;
    private final boolean isSecure;
    private final String remoteAddress;
    private final ConcurrentMap<String, String> additionalParams;
    private final ConcurrentMap<String, String> additionalHeaders;

    /**
     * Initializes a new {@link HttpServletRequestWrapper}.
     *
     * @param requestScheme The scheme of the request: http or https
     * @param remoteAddress The remote IP address of the request
     * @param serverPort The server port of the request
     * @param httpServletRequest The request (to delegate to)
     * @throws IllegalArgumentException If the port is smaller than 1
     */
    public HttpServletRequestWrapper(String requestScheme, String remoteAddress, int serverPort, HttpServletRequest httpServletRequest) {
        if (serverPort < 1) {
            throw new IllegalArgumentException("Port is out of valid range: " + serverPort);
        }
        if (requestScheme.equalsIgnoreCase(HTTPS_SCHEME)) {
            this.requestScheme = HTTPS_SCHEME;
            this.isSecure = true;
        } else {
            this.requestScheme = HTTP_SCHEME;
            this.isSecure = false;
        }
        this.remoteAddress = remoteAddress;
        this.serverPort = serverPort;
        this.delegate = httpServletRequest;
        this.additionalParams = new ConcurrentHashMap<>(6, 0.9F, 1);
        this.additionalHeaders = new ConcurrentHashMap<>(6, 0.9F, 1);

        OXRequest internalRequest = (OXRequest) ServletUtils.getInternalRequest(httpServletRequest);
        internalRequest.setXForwardPort(serverPort);
        internalRequest.setXForwardProto(requestScheme);
        internalRequest.setAttribute("com.openexchange.http.isForcedSecurity", Boolean.TRUE);
    }

    /**
     * Initializes a new {@link HttpServletRequestWrapper}.
     *
     * @param httpServletRequest the delegate to use with this wrapper
     */
    public HttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        this.delegate = httpServletRequest;
        if (HTTPS_SCHEME.equals(httpServletRequest.getScheme())) {
            this.requestScheme = HTTPS_SCHEME;
            this.isSecure = true;
        } else {
            this.requestScheme = HTTP_SCHEME;
            this.isSecure = false;
        }
        this.serverPort = httpServletRequest.getServerPort();
        this.remoteAddress = httpServletRequest.getRemoteAddr();
        this.additionalParams = new ConcurrentHashMap<>(6, 0.9F, 1);
        this.additionalHeaders = new ConcurrentHashMap<>(6, 0.9F, 1);
    }

    @Override
    public Object getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public String getAuthType() {
        return delegate.getAuthType();
    }

    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContextPath() {
        return delegate.getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return delegate.getCookies();
    }

    @Override
    public void setHeader(String name, String value) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }

        additionalHeaders.put(name, null == value ? ABSENT : value);
    }

    @Override
    public long getDateHeader(String name) {
        if (name == null || 0 == name.length()) {
            return -1L;
        }

        String value = additionalHeaders.get(name);
        if (null != value) {
            if (ABSENT == value) {
                // Explicitly removed
                return -1L;
            }

            SimpleDateFormats formats = SimpleDateFormats.create();
            try {
                // Attempt to convert the date header in a variety of formats
                long result = FastHttpDateFormat.parseDate(value, formats.getFormats());
                if (result != (-1L)) {
                    return result;
                }
                throw new IllegalArgumentException(value);
            } finally {
                formats.recycle();
            }
        }

        return delegate.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        if (name == null || 0 == name.length()) {
            return null;
        }

        String value = additionalHeaders.get(name);
        if (null != value) {
            return ABSENT == value ? null /*Explicitly removed*/ : value;
        }

        return delegate.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (additionalParams.isEmpty()) {
            return delegate.getHeaderNames();
        }

        // Create copy from request's parameters
        List<String> headerNames = new ArrayList<String>();
        for (Enumeration<String> e = delegate.getHeaderNames(); e.hasMoreElements();) {
            headerNames.add(e.nextElement());
        }

        // Overwrite/clean by the ones from additional parameters
        for (Map.Entry<String,String> entry : additionalHeaders.entrySet()) {
            String value = entry.getValue();
            if (ABSENT == value) {
                headerNames.remove(entry.getKey());
            } else {
                headerNames.add(entry.getKey());
            }
        }

        return new IteratorEnumeration<String>(headerNames.iterator());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (additionalParams.isEmpty()) {
            return delegate.getHeaders(name);
        }

        String value = additionalHeaders.get(name);
        if (null != value) {
            return ABSENT == value ? null /*Explicitly removed*/ : new IteratorEnumeration<String>(Collections.singletonList(value).iterator());
        }

        return delegate.getHeaders(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public int getIntHeader(String name) {
        if (name == null || 0 == name.length()) {
            return -1;
        }

        String value = additionalHeaders.get(name);
        if (null != value) {
            if (ABSENT == value) {
                // Explicitly removed
                return -1;
            }

            return Integer.parseInt(value);
        }

        return delegate.getIntHeader(name);
    }

    @Override
    public String getLocalAddr() {
        return delegate.getLocalAddr();
    }

    @Override
    public String getLocalName() {
        return delegate.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return delegate.getLocalPort();
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return delegate.getLocales();
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public String getParameter(String name) {
        String value = additionalParams.get(name);
        if (null != value) {
            return ABSENT == value ? null /*Explicitly removed*/ : value;
        }

        return delegate.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (additionalParams.isEmpty()) {
            return delegate.getParameterMap();
        }

        // Create copy from request's parameters
        Map<String, String[]> parameters = delegate.getParameterMap();
        if (null != parameters && !parameters.isEmpty()) {
            parameters = new LinkedHashMap<>(parameters);
        } else {
            parameters = new LinkedHashMap<>(4, 0.9F);
        }

        // Overwrite/clean by the ones from additional parameters
        for (Map.Entry<String,String> entry : additionalParams.entrySet()) {
            String value = entry.getValue();
            if (ABSENT == value) {
                parameters.remove(entry.getKey());
            } else {
                parameters.put(entry.getKey(), new String[] { value });
            }
        }

        return ImmutableMap.copyOf(parameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (additionalParams.isEmpty()) {
            return delegate.getParameterNames();
        }

        // Create copy from request's parameters
        Set<String> names = new LinkedHashSet<>(6);
        for (Enumeration<String> e = delegate.getParameterNames(); e.hasMoreElements();) {
            names.add(e.nextElement());
        }

        // Enhance/clean by the ones from additional parameters
        for (Map.Entry<String,String> entry : additionalParams.entrySet()) {
            String value = entry.getValue();
            if (ABSENT == value) {
                names.remove(entry.getKey());
            } else {
                names.add(entry.getKey());
            }
        }

        return new IteratorEnumeration<String>(names.iterator());
    }

    @Override
    public String[] getParameterValues(String name) {
        String value = additionalParams.get(name);
        if (null != value) {
            return ABSENT == value ? null /*Explicitly removed*/ : new String[] { value };
        }

        return delegate.getParameterValues(name);
    }

    @Override
    public String getPathInfo() {
        return delegate.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return delegate.getPathTranslated();
    }

    @Override
    public String getProtocol() {
        return delegate.getProtocol();
    }

    @Override
    public String getQueryString() {
        return delegate.getQueryString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return delegate.getReader();
    }

    @Override
    @Deprecated
    public String getRealPath(String arg0) {
        return delegate.getRealPath(arg0);
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    @Override
    public String getRemoteHost() {
        return delegate.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return delegate.getRemotePort();
    }

    @Override
    public String getRemoteUser() {
        return delegate.getRemoteUser();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return delegate.getRequestDispatcher(arg0);
    }

    @Override
    public String getRequestURI() {
        return delegate.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return delegate.getRequestURL();
    }

    @Override
    public String getRequestedSessionId() {
        return delegate.getRequestedSessionId();
    }

    @Override
    public String getScheme() {
        return requestScheme;
    }

    @Override
    public String getServerName() {
        return delegate.getServerName();
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getServletPath() {
        return delegate.getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return delegate.getSession();
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        return delegate.getSession(arg0);
    }

    @Override
    public Principal getUserPrincipal() {
        return delegate.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return delegate.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return delegate.isRequestedSessionIdFromURL();
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return delegate.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return delegate.isRequestedSessionIdValid();
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return delegate.isUserInRole(arg0);
    }

    /**
     * com.openexchange.ajax.Multiple accesses request objects concurrently
     * so we have to prevent concurrent modifications
     */
    @Override
    public synchronized void removeAttribute(String name) {
        delegate.removeAttribute(name);
    }

    @Override
    public synchronized void setAttribute(String name, Object o) {
        delegate.setAttribute(name, o);
    }

    @Override
    public synchronized void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        delegate.setCharacterEncoding(env);
    }

    @Override
    public void putParameter(String name, String value) {
        if (null == name) {
           throw new NullPointerException("name is null");
        }

        additionalParams.put(name, null == value ? ABSENT : value);
    }

    @Override
    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return delegate.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return delegate.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return delegate.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return delegate.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return delegate.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return delegate.getDispatcherType();
    }

    @Override
    public String changeSessionId() {
        return delegate.changeSessionId();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return delegate.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        delegate.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        delegate.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return delegate.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return delegate.getPart(name);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return delegate.upgrade(handlerClass);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private static class IteratorEnumeration<E> implements Enumeration<E> {

        /** The iterator being decorated. */
        private final Iterator<E> iterator;

        /**
         * Constructs a new <code>IteratorEnumeration</code> that will use the given iterator.
         *
         * @param iterator  the iterator to use
         */
        public IteratorEnumeration(Iterator<E> iterator ) {
            super();
            this.iterator = iterator;
        }

        // Iterator interface
        //-------------------------------------------------------------------------

        /**
         *  Returns true if the underlying iterator has more elements.
         *
         *  @return true if the underlying iterator has more elements
         */
        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        /**
         *  Returns the next element from the underlying iterator.
         *
         *  @return the next element from the underlying iterator.
         *  @throws java.util.NoSuchElementException  if the underlying iterator has no
         *    more elements
         */
        @Override
        public E nextElement() {
            return iterator.next();
        }
    }

}
