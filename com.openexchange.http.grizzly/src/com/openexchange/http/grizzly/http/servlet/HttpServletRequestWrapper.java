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

package com.openexchange.http.grizzly.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.glassfish.grizzly.http.server.OXRequest;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.servlet.ServletUtils;
import com.openexchange.dispatcher.Parameterizable;

public class HttpServletRequestWrapper implements HttpServletRequest, Parameterizable {

    public final static String HTTP_SCHEME = "http";

    public final static String HTTPS_SCHEME = "https";

    private final HttpServletRequest delegate;

    private final String requestScheme;

    private final int serverPort;

    private final boolean isSecure;

    private final String remoteAddress;

        /**
         * Initializes a new {@link HttpServletRequestWrapper}.
         *
         * @param requestScheme The scheme of the incoming request: http or https
         * @param remoteAddress
         * @param serverPort The serverPort of the incoming request
         * @param httpServletRequest The incoming request
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

            OXRequest internalRequest = (OXRequest) ServletUtils.getInternalRequest(delegate);
            internalRequest.setXForwardPort(serverPort);
            internalRequest.setxForwardProto(requestScheme);
            internalRequest.setAttribute("com.openexchange.http.isForcedSecurity", true);
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
    }

    @Override
    public Object getAttribute(String arg0) {
        return delegate.getAttribute(arg0);
    }

    @Override
    public Enumeration getAttributeNames() {
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
    public long getDateHeader(String arg0) {
        return delegate.getDateHeader(arg0);
    }

    @Override
    public String getHeader(String arg0) {
        return delegate.getHeader(arg0);
    }

    @Override
    public Enumeration getHeaderNames() {
        return delegate.getHeaderNames();
    }

    @Override
    public Enumeration getHeaders(String arg0) {
        return delegate.getHeaders(arg0);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public int getIntHeader(String arg0) {
        return delegate.getIntHeader(arg0);
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
    public Enumeration getLocales() {
        return delegate.getLocales();
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public String getParameter(String arg0) {
        return delegate.getParameter(arg0);
    }

    @Override
    public Map getParameterMap() {
        return delegate.getParameterMap();
    }

    @Override
    public Enumeration getParameterNames() {
        return delegate.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String arg0) {
        return delegate.getParameterValues(arg0);
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
     * com.openexchange.ajax.Multiple accesses reequest objects concurrently
     * so we have to prevent concurrent modifications
     */
    @Override
    public synchronized void removeAttribute(String arg0) {
        delegate.removeAttribute(arg0);
    }

    @Override
    public synchronized void setAttribute(String arg0, Object arg1) {
        delegate.setAttribute(arg0, arg1);
    }

    @Override
    public synchronized void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        delegate.setCharacterEncoding(arg0);
    }

    @Override
    public synchronized void putParameter(String name, String value) {
        Request internalRequest = ServletUtils.getInternalRequest(delegate);
        internalRequest.putParameter(name, value);
    }

}
