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

package javax.servlet.http.sim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * {@link SimHttpServletRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SimHttpServletRequest implements HttpServletRequest {

    private String charset;
    private final Map<String, Object> attributes;
    private final Map<String, String> headers;
    private final Map<String, String[]> parameters;
    private ServletInputStream inputStream;
    private String protocol;
    private String scheme;
    private String serverName;
    private int serverPort;
    private String remoteAddr;
    private String remoteHost;
    private Locale locale;
    private boolean secure;
    private RequestDispatcher requestDispatcher;
    private String realPath;
    private int remotePort;
    private String localName;
    private String localAddr;
    private int localPort;
    private String authType;
    private List<Cookie> cookies;
    private long dateHeader;
    private String pathInfo;
    private String method;
    private Principal principal;
    private String requestedSessionId;
    private String requestURI;
    private String requestURL;
    private String remoteUser;
    private String queryString;
    private String contextPath;
    private String pathTranslated;
    private String servletPath;
    private HttpSession httpSession;

    /**
     * Initializes a new {@link SimHttpServletRequest}.
     */
    public SimHttpServletRequest() {
        super();
        attributes = new HashMap<String, Object>(4);
        headers = new HashMap<String, String>(8);
        parameters = new HashMap<String, String[]>(8);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        final Iterator<String> iterator = attributes.keySet().iterator();
        return new Enumeration() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public String getCharacterEncoding() {
        return charset;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        charset = env;
    }

    @Override
    public int getContentLength() {
        final String string = headers.get("content-length");
        if (null == string) {
            return -1;
        }

        try {
            final int ret = (int) Long.parseLong(string);
            return ret < 0 ? -1 : ret;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Sets the contentLength
     *
     * @param contentLength The contentLength to set
     */
    public void setContentLength(long contentLength) {
        headers.put("content-length", Long.toString(contentLength));
    }

    @Override
    public String getContentType() {
        return headers.get("content-type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * Sets the inputStream
     *
     * @param inputStream The inputStream to set
     */
    public void setInputStream(ServletInputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Sets given parameter.
     *
     * @param name The name
     * @param value The value
     */
    public void setParameter(String name, String value) {
        parameters.put(name, new String[] { value });
    }

    @Override
    public String getParameter(String name) {
        String[] strings = parameters.get(name);
        if (strings == null || strings.length == 0) {
            return null;
        }

        return strings[0];
    }

    @Override
    public Enumeration getParameterNames() {
        final Iterator<String> iterator = parameters.keySet().iterator();
        return new Enumeration() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Map getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol
     *
     * @param protocol The protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the scheme
     *
     * @param scheme The scheme to set
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets the serverName
     *
     * @param serverName The serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Sets the serverPort
     *
     * @param serverPort The serverPort to set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Sets the remoteAddr
     *
     * @param remoteAddr The remoteAddr to set
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Sets the remoteHost
     *
     * @param remoteHost The remoteHost to set
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    /**
     * Sets the header
     *
     * @param name The name
     * @param value The value
     */
    public void setHeader(String name, String value) {
        headers.put(toLowerCase(name), value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale
     *
     * @param locale The locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets the secure
     *
     * @param secure The secure to set
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return requestDispatcher;
    }

    /**
     * Sets the requestDispatcher
     *
     * @param requestDispatcher The requestDispatcher to set
     */
    public void setRequestDispatcher(RequestDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public String getRealPath(String path) {
        return realPath;
    }

    /**
     * Sets the realPath
     *
     * @param realPath The realPath to set
     */
    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Sets the remotePort
     *
     * @param remotePort The remotePort to set
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    /**
     * Sets the localName
     *
     * @param localName The localName to set
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    /**
     * Sets the localAddr
     *
     * @param localAddr The localAddr to set
     */
    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Sets the localPort
     *
     * @param localPort The localPort to set
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public String getAuthType() {
        return authType;
    }

    /**
     * Sets the authType
     *
     * @param authType The authType to set
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[0]);
    }

    /**
     * Sets the cookies
     *
     * @param cookies The cookies to set
     */
    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    @Override
    public long getDateHeader(String name) {
        return dateHeader;
    }

    /**
     * Sets the dateHeader
     *
     * @param dateHeader The dateHeader to set
     */
    public void setDateHeader(long dateHeader) {
        this.dateHeader = dateHeader;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(toLowerCase(name));
    }

    @Override
    public Enumeration getHeaders(String name) {
        final Iterator<String[]> iterator = parameters.values().iterator();
        return new Enumeration<String[]>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String[] nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public Enumeration getHeaderNames() {
        final Iterator<String> iterator = headers.keySet().iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public int getIntHeader(String name) {
        final String string = headers.get(name);
        if (null == string) {
            return -1;
        }

        try {
            final int ret = Integer.parseInt(string);
            return ret < 0 ? -1 : ret;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method
     *
     * @param method The method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Sets the pathInfo
     *
     * @param pathInfo The pathInfo to set
     */
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return pathTranslated;
    }

    /**
     * Sets the pathTranslated
     *
     * @param pathTranslated The pathTranslated to set
     */
    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath
     *
     * @param contextPath The contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    /**
     * Sets the queryString
     *
     * @param queryString The queryString to set
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRemoteUser() {
        return remoteUser;
    }

    /**
     * Sets the remoteUser
     *
     * @param remoteUser The remoteUser to set
     */
    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    /**
     * Sets the principal
     *
     * @param principal The principal to set
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    /**
     * Sets the requestedSessionId
     *
     * @param requestedSessionId The requestedSessionId to set
     */
    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Sets the requestURI
     *
     * @param requestURI The requestURI to set
     */
    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(requestURL);
    }

    /**
     * Sets the requestURL
     *
     * @param requestURL The requestURL to set
     */
    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    /**
     * Sets the servletPath
     *
     * @param servletPath The servletPath to set
     */
    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (httpSession == null) {
            httpSession = new SimHttpSession();
        }
        return httpSession;
    }

    /**
     * Sets the httpSession
     *
     * @param httpSession The httpSession to set
     */
    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
