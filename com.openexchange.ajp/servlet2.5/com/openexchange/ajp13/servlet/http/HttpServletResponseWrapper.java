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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.servlet.ServletResponseWrapper;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.Charsets;
import com.openexchange.java.StringAllocator;
import com.openexchange.version.Version;
import com.openexchange.session.Session;

/**
 * HttpServletResponseWrapper
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletResponseWrapper extends ServletResponseWrapper implements HttpServletResponse {

    private static final String ERROR_PAGE_TEMPL = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<html><head>\r\n" + "<title>#STATUS_CODE# #STATUS_MSG#</title>\r\n" + "</head><body>\r\n" + "<h1>#STATUS_CODE# #STATUS_MSG#</h1>\r\n" + "<p>#STATUS_DESC#</p>\r\n" + "<hr>\r\n" + "<address>#DATE#,&nbsp;Open-Xchange v#VERSION#</address>\r\n" + "</body></html>";

    private static final TIntObjectMap<String> STATUS_MSGS;

    private static final TIntObjectMap<String> STATUS_DESC;

    private static final DateFormat HEADER_DATE_FORMAT = HttpDateFormatRegistry.getInstance().getDefaultDateFormat();

    static {
        STATUS_MSGS = new TIntObjectHashMap<String>(46);
        STATUS_MSGS.put(100, "Continue");
        STATUS_MSGS.put(101, "Switching Protocols");
        STATUS_MSGS.put(200, "OK");
        STATUS_MSGS.put(201, "Created");
        STATUS_MSGS.put(202, "Accepted");
        STATUS_MSGS.put(203, "Non-Authoritative Information");
        STATUS_MSGS.put(204, "No Content");
        STATUS_MSGS.put(205, "Reset Content");
        STATUS_MSGS.put(206, "Partial Content");
        STATUS_MSGS.put(207, "Multistatus");
        STATUS_MSGS.put(300, "Multiple Choices");
        STATUS_MSGS.put(301, "Moved Permanently");
        STATUS_MSGS.put(302, "Found");
        STATUS_MSGS.put(303, "See Other");
        STATUS_MSGS.put(304, "Not Modified");
        STATUS_MSGS.put(305, "Use Proxy");
        STATUS_MSGS.put(306, "");
        STATUS_MSGS.put(307, "Temporary Redirect");
        STATUS_MSGS.put(400, "Bad Request");
        STATUS_MSGS.put(401, "Unauthorized");
        STATUS_MSGS.put(402, "Payment Required");
        STATUS_MSGS.put(403, "Forbidden");
        STATUS_MSGS.put(404, "Not Found");
        STATUS_MSGS.put(405, "Method Not Allowed");
        STATUS_MSGS.put(406, "Not Acceptable");
        STATUS_MSGS.put(407, "Proxy Authentication Required");
        STATUS_MSGS.put(408, "Request Timeout");
        STATUS_MSGS.put(409, "Conflict");
        STATUS_MSGS.put(410, "Gone");
        STATUS_MSGS.put(411, "Length Required");
        STATUS_MSGS.put(412, "Precondition Failed");
        STATUS_MSGS.put(413, "Request Entity Too Large");
        STATUS_MSGS.put(414, "Request-URI Too Long");
        STATUS_MSGS.put(415, "Unsupported Media Type");
        STATUS_MSGS.put(416, "Requested Range Not Satisfiable");
        STATUS_MSGS.put(417, "Expectation Failed");
        STATUS_MSGS.put(500, "Internal Server Error");
        STATUS_MSGS.put(501, "Not Implemented");
        STATUS_MSGS.put(502, "Bad Gateway");
        STATUS_MSGS.put(503, "Service Unavailable");
        STATUS_MSGS.put(504, "Gateway Timeout");
        STATUS_MSGS.put(505, "HTTP Version Not Supported");
        /*
         * Status descriptions
         */
        STATUS_DESC = new TIntObjectHashMap<String>(2);
        STATUS_DESC.put(404, "The requested URL %s was not found on this server.");
        STATUS_DESC.put(
            503,
            "The server is temporarily unable to service your request due to" + " maintenance downtime or capacity problems. Please try again later.");
    }

    private final Set<Cookie> cookies;

    private String statusMsg;

    private final HttpServletRequestWrapper request;

    private final boolean httpOnly;

    /**
     * Initializes a new {@link HttpServletResponseWrapper}
     *
     * @param request The corresponding servlet request to this servlet response
     */
    public HttpServletResponseWrapper(final HttpServletRequestWrapper request) {
        super();
        cookies = new LinkedHashSet<Cookie>();
        status = HttpServletResponse.SC_OK;
        this.request = request;
        final ConfigurationService cs = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
        httpOnly = (null != cs && cs.getBoolProperty(ServerConfig.Property.COOKIE_HTTP_ONLY.getPropertyName(), true));
    }

    /**
     * Gets the associated HTTP request.
     *
     * @return The associated HTTP request
     */
    public HttpServletRequestWrapper getRequest() {
        return request;
    }

    @Override
    public String encodeRedirectUrl(final String url) {
        return encodeURL(url);
    }

    @Override
    public boolean containsHeader(final String name) {
        return headers.containsKey(name);
    }

    @Override
    public void reset() {
        super.reset();
        cookies.clear();
    }

    @Override
    public String encodeURL(final String url) {
        if (null == request) {
            return url;
        }
        /*
         * Retrieve groupware session, if user is logged in
         */
        final Session groupwareSession = (Session) request.getAttribute(SessionServlet.SESSION_KEY);
        /*
         * Check for HTTP session: First look for JSESSIONID cookie, if none found check if HTTP session was created.
         */
        boolean foundInCookie = false;
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length && !foundInCookie; i++) {
                if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(cookies[i].getName())) {
                    foundInCookie = true;
                }
            }
        }
        final HttpSession httpSession;
        if (foundInCookie) {
            /*
             * Set to null, cause obviously cookies are used
             */
            httpSession = null;
        } else {
            httpSession = request.getSession(false);
        }
        return appendSessionID(
            url,
            groupwareSession == null ? null : groupwareSession.getSecret(),
            httpSession == null ? null : httpSession.getId());
    }

    @Override
    public String encodeRedirectURL(final String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeUrl(final String url) {
        return encodeURL(url);
    }

    private static final String appendSessionID(final String url, final String groupwareSessionId, final String httpSessionId) {
        if (url == null) {
            return null;
        } else if (groupwareSessionId == null && httpSessionId == null) {
            return url.indexOf('?') == -1 ? new com.openexchange.java.StringAllocator(url).append("?jvm=").append(AJPv13Config.getJvmRoute()).toString() : url;
        }
        String path = url;
        String query = "";
        String anchor = "";
        final int question = url.indexOf('?');
        if (question >= 0) {
            path = url.substring(0, question);
            query = url.substring(question + 1);
        }
        final int pound = path.indexOf('#');
        if (pound >= 0) {
            anchor = path.substring(pound);
            path = path.substring(0, pound);
        }
        final StringAllocator sb = new StringAllocator(path);
        if (httpSessionId != null && sb.length() > 0) {
            sb.append('/');
            sb.append(AJPv13RequestHandler.JSESSIONID_URI);
            sb.append(httpSessionId);
        }
        sb.append(anchor);
        boolean first = true;
        if (groupwareSessionId != null) {
            sb.append('?').append(AJAXServlet.PARAMETER_SESSION).append('=');
            sb.append(groupwareSessionId);
            first = false;
        }
        if (query.length() > 0) {
            sb.append(first ? '?' : '&').append(query);
            first = false;
        }
        if (first) {
            sb.append("?jvm=").append(AJPv13Config.getJvmRoute());
        }
        return (sb.toString());
    }

    @Override
    public void addDateHeader(final String name, final long l) {
        synchronized (HEADER_DATE_FORMAT) {
            addHeader(name, HEADER_DATE_FORMAT.format(new Date(l)));
        }
    }

    @Override
    public void addIntHeader(final String name, final int i) {
        addHeader(name, Integer.toString(i));
    }

    @Override
    public void addCookie(final Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * Removes specified cookie from cookie set
     *
     * @param cookie The cookie to remove
     */
    public void removeCookie(final Cookie cookie) {
        cookies.remove(cookie);
    }

    /**
     * Generates a two dimensional array of {@link String} containing the <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers of this HTTP
     * response's cookies.
     * <p>
     * For each cookie its HTTP header format is generated and added to corresponding array of {@link String}
     *
     * @return A two dimensional array of {@link String} containing the <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers
     */
    public String[][] getFormatedCookies() {
        final String[][] retval = new String[1][];
        final int cookiesSize = cookies.size();
        final String[] list = new String[cookiesSize];
        if (cookiesSize > 0) {
            final String userAgent = request.getHeader("User-Agent");
            final Iterator<Cookie> iter = cookies.iterator();
            final StringBuilder composer = new StringBuilder(32);
            list[0] = getFormattedCookie(iter.next(), userAgent, composer, httpOnly);
            for (int i = 1; i < cookiesSize; i++) {
                composer.setLength(0);
                list[i] = getFormattedCookie(iter.next(), userAgent, composer, httpOnly);
            }
        }
        retval[0] = list;
        return retval;
    }

    private static final String[] COOKIE_PARAMS = { "; expires=", "; version=", "; path=", "; domain=", "; secure" };

    /**
     * Gets the HTTP header format for specified instance of {@link Cookie}
     *
     * @param cookie The cookie whose HTTP header format shall be returned
     * @param composer A string builder used for composing
     * @return A string representing the HTTP header format
     */
    private static final String getFormattedCookie(final Cookie cookie, final String userAgent, final StringBuilder composer, final boolean httpOnly) {
        composer.append(cookie.getName()).append('=');
        composer.append(cookie.getValue());
        final int maxAge = cookie.getMaxAge();
        if (maxAge >= 0) {
            HttpDateFormatRegistry.getInstance().appendCookieMaxAge(maxAge, userAgent, composer);
        }
        if (cookie.getVersion() > 0) {
            composer.append(COOKIE_PARAMS[1]).append(cookie.getVersion());
        }
        {
            final String path = cookie.getPath();
            if (!isEmpty(path)) {
                composer.append(COOKIE_PARAMS[2]).append(path);
            }
        }
        {
            final String domain = cookie.getDomain();
            if (!isEmpty(domain)) {
                composer.append(COOKIE_PARAMS[3]).append(domain);
            }
        }
        if (cookie.getSecure()) {
            composer.append(COOKIE_PARAMS[4]);
        }
        /*-
         * TODO: HttpOnly currently cannot be set in Cookie class, thus we do it hard-coded here.
         *       This is available with Java Servlet Specification v3.0.
         *
         * Append HttpOnly flag
         */
        if (httpOnly /*&& maxAge > 0*/) {
            composer.append("; HttpOnly");
        }
        return composer.toString();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    @Override
    public void addHeader(final String name, final String value) {
        if (!headers.containsKey(name)) {
            headers.put(name, new String[] { value });
            return;
        }
        final String[] tmp = headers.get(name);
        final String[] val = new String[tmp.length + 1];
        System.arraycopy(tmp, 0, val, 0, tmp.length);
        val[val.length - 1] = value;
        headers.put(name, val);
    }

    public int getStatus() {
        return status;
    }

    public String getStatusMsg() {
        // System.out.println("STATUS: " + status + " - " + (statusMsg != null ?
        // statusMsg : statusMsgs.get(new Integer(status))));
        return statusMsg == null ? STATUS_MSGS.get(status) : statusMsg;
    }

    @Override
    public void setStatus(final int status) {
        this.status = status;
        statusMsg = STATUS_MSGS.get(status);
    }

    @Override
    public void setStatus(final int status, final String statusMsg) {
        this.status = status;
        this.statusMsg = statusMsg == null ? STATUS_MSGS.get(status) : statusMsg;
    }

    @Override
    public void setDateHeader(final String name, final long l) {
        synchronized (HEADER_DATE_FORMAT) {
            setHeader(name, HEADER_DATE_FORMAT.format(new Date(l)));
        }
    }

    @Override
    public void setIntHeader(final String name, final int i) {
        setHeader(name, Integer.toString(i));
    }

    @Override
    public final void setHeader(final String name, final String value) {
        if (value == null) {
            /*
             * Treat as a remove
             */
            headers.remove(name);
            return;
        }
        headers.put(name, new String[] { value });
    }

    public final int getHeadersSize() {
        return headers.size();
    }

    public final Iterator<String> getHeaderNames() {
        return headers.keySet().iterator();
    }

    public final Set<Map.Entry<String, String[]>> getHeaderEntrySet() {
        return headers.entrySet();
    }

    public Enumeration<?> getHeaders(final String name) {
        return makeEnumeration(headers.get(name));
    }

    public final String getHeader(final String name) {
        if (!containsHeader(name)) {
            return null;
        }
        final StringBuilder retval = new StringBuilder(150);
        final String[] sa = headers.get(name);
        retval.append(sa[0]);
        for (int i = 1; i < sa.length; i++) {
            retval.append(',');
            retval.append(sa[i]);
        }
        return retval.toString();
    }

    @Override
    public final void sendRedirect(final String location) {
        status = HttpServletResponse.SC_MOVED_TEMPORARILY;
        statusMsg = STATUS_MSGS.get(HttpServletResponse.SC_MOVED_TEMPORARILY);
        addHeader("Location", location);
    }

    private static final String ERR_DESC_NOT_AVAILABLE = "[no description available]";

    /**
     * Composes and sets appropriate error in this HTTP servlet response wrapper.
     *
     * @param status The status to set
     * @param statusMsg The (optional) status message or <code>null</code>
     * @return The error message in bytes
     */
    public final byte[] composeAndSetError(final int status, final String statusMsg) {
        this.status = status;
        this.statusMsg = statusMsg == null ? STATUS_MSGS.get(status) : statusMsg;
        String desc = STATUS_DESC.containsKey(this.status) ? STATUS_DESC.get(this.status) : ERR_DESC_NOT_AVAILABLE;
        if (HttpServletResponse.SC_NOT_FOUND == status) {
            desc = String.format(desc, request.getServletPath());
        }
        String errorMsgStr = ERROR_PAGE_TEMPL;
        errorMsgStr = errorMsgStr.replaceAll("#STATUS_CODE#", Integer.toString(this.status)).replaceAll("#STATUS_MSG#", this.statusMsg).replaceFirst(
            "#STATUS_DESC#",
            desc);
        synchronized (HEADER_DATE_FORMAT) {
            errorMsgStr = errorMsgStr.replaceFirst("#DATE#", HEADER_DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        }
        errorMsgStr = errorMsgStr.replaceFirst("#VERSION#", Version.getInstance().getVersionString());
        setContentType(new com.openexchange.java.StringAllocator("text/html; charset=").append(getCharacterEncoding()).toString());
        final byte[] errormessage = errorMsgStr.getBytes(Charsets.forName(getCharacterEncoding()));
        setContentLength(errormessage.length);
        return errormessage;
    }

    @Override
    public final void sendError(final int status, final String statusMsg) throws IOException {
        this.status = status;
        this.statusMsg = statusMsg == null ? STATUS_MSGS.get(status) : statusMsg;
        servletOutputStream.write(getErrorMessage());
    }

    /**
     * Gets the default error page.
     *
     * @return The default error page
     */
    public byte[] getErrorMessage() {
        String desc = STATUS_DESC.containsKey(this.status) ? STATUS_DESC.get(this.status) : ERR_DESC_NOT_AVAILABLE;
        if (HttpServletResponse.SC_NOT_FOUND == status) {
            desc = String.format(desc, request.getServletPath());
        }
        String errorMsgStr = ERROR_PAGE_TEMPL;
        errorMsgStr = errorMsgStr.replaceAll("#STATUS_CODE#", Integer.toString(this.status)).replaceAll("#STATUS_MSG#", this.statusMsg).replaceFirst(
            "#STATUS_DESC#",
            desc);
        synchronized (HEADER_DATE_FORMAT) {
            errorMsgStr = errorMsgStr.replaceFirst("#DATE#", HEADER_DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        }
        errorMsgStr = errorMsgStr.replaceFirst("#VERSION#", Version.getInstance().getVersionString());
        String encoding = getCharacterEncoding();
        if (null == encoding) {
            encoding = "UTF-8";
        }
        setContentType(new com.openexchange.java.StringAllocator("text/html; charset=").append(encoding).toString());
        final byte[] errormessage = errorMsgStr.getBytes(Charsets.forName(encoding));
        setContentLength(errormessage.length);
        return errormessage;
    }

    @Override
    public void sendError(final int status) throws IOException {
        sendError(status, STATUS_MSGS.get(status));
    }

    private Enumeration<?> makeEnumeration(final Object obj) {
        final Class<?> type = obj.getClass();
        if (!type.isArray()) {
            throw new IllegalArgumentException(obj.getClass().toString());
        }
        return (new Enumeration<Object>() {

            int size = Array.getLength(obj);

            int cursor;

            @Override
            public boolean hasMoreElements() {
                return (cursor < size);
            }

            @Override
            public Object nextElement() {
                return Array.get(obj, cursor++);
            }
        });
    }

}
