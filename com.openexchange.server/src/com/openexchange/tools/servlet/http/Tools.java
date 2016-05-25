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

package com.openexchange.tools.servlet.http;

import static com.openexchange.java.Strings.asciiLowerCase;
import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.HttpStatus;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.annotation.NonNull;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.notify.hostname.internal.HostDataImpl;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.systemname.SystemNameService;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * Convenience methods for servlets.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    public static final String COM_OPENEXCHANGE_CHECK_URL_PARAMS = "com.openexchange.check.url.params";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tools.class);

    /**
     * DateFormat for HTTP header.
     */
    private static final DateFormat HEADER_DATEFORMAT;

    /*-
     * ------------------ Header names -------------------------
     */

    /**
     * <code>"Cache-Control"</code> HTTP header name.
     */
    private static final String NAME_CACHE_CONTROL = "Cache-Control";

    /**
     * <code>"Expires"</code> HTTP header name.
     */
    private static final String NAME_EXPIRES = "Expires";

    /**
     * <code>"ETag"</code> HTTP header name.
     */
    private static final String NAME_ETAG = "ETag";

    /*-
     * ------------------ Header values -------------------------
     */

    /** The <code>"Accept"</code> header */
    private static final @NonNull String ACCEPT = "Accept";

    /**
     * Expires HTTP header value.
     */
    private static final String EXPIRES_DATE;

    /**
     * Cache-Control value.
     */
    private static final String CACHE_VALUE = "no-store, no-cache, must-revalidate, post-check=0, pre-check=0";

    /**
     * Pragma HTTP header key.
     */
    private static final String PRAGMA_KEY = "Pragma";

    /**
     * Pragma HTTP header value.
     */
    private static final String PRAGMA_VALUE = "no-cache";

    private static final AtomicReference<ConfigurationService> CONFIG_SERVICE_REF = new AtomicReference<ConfigurationService>();

    static {
        /*
         * Pattern for the HTTP header date format.
         */
        HEADER_DATEFORMAT = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        HEADER_DATEFORMAT.setTimeZone(getTimeZone("GMT"));
        EXPIRES_DATE = HEADER_DATEFORMAT.format(new Date(799761600000L));
    }

    // ------------------------------------------------------------------------------

    /**
     * Prevent instantiation
     */
    private Tools() {
        super();
    }

    /**
     * Converts a unicode representation of an internet address to ASCII using the procedure in RFC3490 section 4.1. Unassigned characters
     * are not allowed and STD3 ASCII rules are enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@m&uuml;ller.de"</code> is converted to <code>"someone@xn--mller-kva.de"</code>
     *
     * @param idnAddress The unicode representation of an internet address
     * @return The ASCII-encoded (punycode) of given internet address or <code>null</code> if argument is <code>null</code>
     * @throws OXException If ASCII representation of given internet address cannot be created
     */
    public static String toACE(final String idnAddress) throws OXException {
        try {
            return IDNA.toACE(idnAddress);
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Converts an ASCII-encoded address to its unicode representation. Unassigned characters are not allowed and STD3 hostnames are
     * enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@xn--mller-kva.de"</code> is converted to <code>"someone@m&uuml;ller.de"</code>
     *
     * @param aceAddress The ASCII-encoded (punycode) address
     * @return The unicode representation of given internet address or <code>null</code> if argument is <code>null</code>
     */
    public static String toIDN(final String aceAddress) {
        return IDNA.toIDN(aceAddress);
    }

    /**
     * Sets specified ETag header (and implicitly removes/replaces any existing cache-controlling header: <i>Expires</i>,
     * <i>Cache-Control</i>, and <i>Pragma</i>)
     *
     * @param eTag The ETag value
     * @param resp The HTTP response to apply to
     */
    public static void setETag(final String eTag, final HttpServletResponse resp) {
        setETag(eTag, -1L, resp);
    }

    private static final long MILLIS_5MIN = 300000L;

    private static final long MILLIS_HOUR = 3600000L;

    private static final long MILLIS_WEEK = 604800000L;

    private static final long MILLIS_YEAR = 52 * MILLIS_WEEK;

    /**
     * Sets specified ETag header (and implicitly removes/replaces any existing cache-controlling header: <i>Expires</i>,
     * <i>Cache-Control</i>, and <i>Pragma</i>)
     *
     * @param eTag The ETag value
     * @param expiry The optional expiry milliseconds, pass <code>-1</code> to set default expiry (+ 5 minutes)
     * @param resp The HTTP servlet response to apply to
     */
    public static void setETag(final String eTag, final long expiry, final HttpServletResponse resp) {
        removeCachingHeader(resp);
        if (null != eTag) {
            resp.setHeader(NAME_ETAG, eTag);// ETag
        }
        if (expiry <= 0) {
            synchronized (HEADER_DATEFORMAT) {
                resp.setHeader(NAME_EXPIRES, HEADER_DATEFORMAT.format(new Date(System.currentTimeMillis() + MILLIS_5MIN)));
            }
            resp.setHeader(NAME_CACHE_CONTROL, "private, max-age=300");
        } else {
            synchronized (HEADER_DATEFORMAT) {
                resp.setHeader(NAME_EXPIRES, HEADER_DATEFORMAT.format(new Date(System.currentTimeMillis() + expiry)));
            }
            resp.setHeader(NAME_CACHE_CONTROL, "private, max-age=" + (expiry / 1000));
        }
    }

    /**
     * Gets the default of 5 minutes for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @return The default of 5 minutes for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information
     */
    public static long getDefaultExpiry() {
        return MILLIS_5MIN;
    }

    /**
     * Gets the default of 1 hour for image resources for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @return The default of 1 hour for image resources for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information
     */
    public static long getDefaultImageExpiry() {
        return MILLIS_HOUR;
    }

    /**
     * Sets the given date for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @param expiry The optional expiry milliseconds, pass <code>-1</code> to set default expiry (+ 5 minutes)
     * @param resp The HTTP response to apply to
     */
    public static void setExpires(final long expiry, final HttpServletResponse resp) {
        setETag(null, expiry, resp);
    }

    /**
     * Sets the default of 5 minutes for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @param resp The HTTP response to apply to
     */
    public static void setDefaultExpiry(final HttpServletResponse resp) {
        synchronized (HEADER_DATEFORMAT) {
            resp.setHeader(NAME_EXPIRES, HEADER_DATEFORMAT.format(new Date(System.currentTimeMillis() + MILLIS_5MIN)));
        }
        resp.setHeader(NAME_CACHE_CONTROL, "private, max-age=300");
    }

    /**
     * Sets the default of 1 hour for image resources for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @param resp The HTTP response to apply to
     */
    public static void setDefaultImageExpiry(final HttpServletResponse resp) {
        synchronized (HEADER_DATEFORMAT) {
            resp.setHeader(NAME_EXPIRES, HEADER_DATEFORMAT.format(new Date(System.currentTimeMillis() + MILLIS_HOUR)));
        }
        resp.setHeader(NAME_CACHE_CONTROL, "private, max-age=3600");// 1 hour
    }

    /**
     * Sets the amount of 1 year for <tt>Expires</tt> and <tt>Cache-Control</tt>'s <tt>max-age</tt> header information.
     *
     * @param resp The HTTP response to apply to
     */
    public static void setExpiresInOneYear(final HttpServletResponse resp) {
        synchronized (HEADER_DATEFORMAT) {
            resp.setHeader(NAME_EXPIRES, HEADER_DATEFORMAT.format(new Date(System.currentTimeMillis() + MILLIS_YEAR)));
        }
        resp.setHeader(NAME_CACHE_CONTROL, "private, max-age=31521018");// 1 year
    }

    /**
     * The magic spell to disable caching. Do not use these headers if response is directly written into servlet's output stream to initiate
     * a download.
     *
     * @param resp the servlet response.
     * @see #removeCachingHeader(HttpServletResponse)
     */
    public static void disableCaching(final HttpServletResponse resp) {
        resp.setHeader(NAME_EXPIRES, EXPIRES_DATE);
        resp.setHeader(NAME_CACHE_CONTROL, CACHE_VALUE);
        resp.setHeader(PRAGMA_KEY, PRAGMA_VALUE);
    }

    /**
     * Remove <tt>Pragma</tt> response header value if we are going to write directly into servlet's output stream cause then some browsers
     * do not allow this header.
     *
     * @param resp the servlet response.
     */
    public static void removeCachingHeader(final HttpServletResponse resp) {
        resp.setHeader(PRAGMA_KEY, null);
        resp.setHeader(NAME_CACHE_CONTROL, null);
        resp.setHeader(NAME_EXPIRES, null);
    }

    /**
     * Formats a date for HTTP headers.
     *
     * @param date date to format.
     * @return the string with the formated date.
     */
    public static String formatHeaderDate(final Date date) {
        synchronized (HEADER_DATEFORMAT) {
            return HEADER_DATEFORMAT.format(date);
        }
    }

    /**
     * Parses a date from a HTTP date header.
     *
     * @param str The HTTP date header value
     * @return The parsed <code>java.util.Date</code> object
     * @throws ParseException If the date header value cannot be parsed
     */
    public static Date parseHeaderDate(final String str) throws ParseException {
        synchronized (HEADER_DATEFORMAT) {
            return HEADER_DATEFORMAT.parse(str);
        }
    }

    /**
     * Optionally parses a date from a HTTP date header.
     *
     * @param str The HTTP date header value
     * @return The parsed time or <code>-1</code>
     */
    public static long optHeaderDate(final String str) {
        if (null == str) {
            return -1L;
        }
        synchronized (HEADER_DATEFORMAT) {
            try {
                return HEADER_DATEFORMAT.parse(str).getTime();
            } catch (ParseException e) {
                return -1L;
            }
        }
    }

    /**
     * HTTP header name containing the user agent.
     */
    public static final String HEADER_AGENT = "User-Agent";

    /**
     * HTTP header name containing the content type of the body.
     */
    public static final String HEADER_TYPE = "Content-Type";

    /**
     * HTTP header name containing the site that caused the request.
     */
    public static final String HEADER_REFERER = "Referer";

    /**
     * HTTP header name containing the length of the body.
     */
    public static final String HEADER_LENGTH = "Content-Length";

    /**
     * This method integrates interesting HTTP header values into a string for logging purposes. This is usefull if a client sent an illegal
     * request for discovering the cause of the illegal request.
     *
     * @param req the servlet request.
     * @return a string containing interesting HTTP headers.
     */
    public static String logHeaderForError(final HttpServletRequest req) {
        final StringBuilder message = new StringBuilder();
        final String sep = System.getProperty("line.separator");
        message.append("|").append(sep);
        message.append(HEADER_AGENT);
        message.append(": ");
        message.append(req.getHeader(HEADER_AGENT));
        message.append(sep);
        message.append(HEADER_TYPE);
        message.append(": ");
        message.append(req.getHeader(HEADER_TYPE));
        message.append(sep);
        message.append(HEADER_REFERER);
        message.append(": ");
        message.append(req.getHeader(HEADER_REFERER));
        message.append(sep);
        message.append(HEADER_LENGTH);
        message.append(": ");
        message.append(req.getHeader(HEADER_LENGTH));
        return message.toString();
    }

    /**
     * The name of the JSESSIONID cookie
     */
    public static final String JSESSIONID_COOKIE = "JSESSIONID";

    private static final CookieNameMatcher OX_COOKIE_MATCHER = new CookieNameMatcher() {

        @Override
        public boolean matches(final String cookieName) {
            return (null != cookieName && (cookieName.startsWith(LoginServlet.SESSION_PREFIX) || JSESSIONID_COOKIE.equals(cookieName)));
        }
    };

    /**
     * Deletes all OX specific cookies.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     */
    public static void deleteCookies(final HttpServletRequest req, final HttpServletResponse resp) {
        deleteCookies(req, resp, OX_COOKIE_MATCHER);
    }

    /**
     * Deletes all cookies which satisfy specified matcher.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @param matcher The cookie name matcher determining which cookie shall be deleted
     */
    public static void deleteCookies(final HttpServletRequest req, final HttpServletResponse resp, final CookieNameMatcher matcher) {
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                final String cookieName = cookie.getName();
                if (matcher.matches(cookieName)) {
                    final Cookie respCookie = new Cookie(cookieName, cookie.getValue());
                    respCookie.setPath("/");
                    // A zero value causes the cookie to be deleted.
                    respCookie.setMaxAge(0);
                    resp.addCookie(respCookie);
                }
            }
        }
    }

    public static void setHeaderForFileDownload(final String userAgent, final HttpServletResponse resp, final String fileName) throws UnsupportedEncodingException {
        setHeaderForFileDownload(userAgent, resp, fileName, null);
    }

    public static void setHeaderForFileDownload(final String userAgent, final HttpServletResponse resp, final String fileName, final String contentDisposition) throws UnsupportedEncodingException {
        final BrowserDetector detector = BrowserDetector.detectorFor(userAgent);
        String cd = contentDisposition;
        if (cd == null) {
            cd = "attachment";
        }
        String filename = null;

        if (detector.isMSIE()) {
            filename = Helper.encodeFilenameForIE(fileName, Charsets.UTF_8);
        } else if (detector.isSafari5()) {
            /*-
             * On socket layer characters are casted to byte values.
             *
             * sink.write((byte) chars[i]);
             *
             * Therefore ensure we have a one-character-per-byte charset, as it is with ISO-5589-1
             */
            filename = new String(fileName.getBytes(Charsets.UTF_8), Charsets.ISO_8859_1);
        } else {
            filename = Helper.escape(Helper.encodeFilename(fileName, "UTF-8"));
        }

        if (cd.indexOf(';') < 0 && filename != null) {
            cd = new StringBuilder(64).append(cd).append("; filename=\"").append(filename).append('"').toString();
        }

        resp.setHeader("Content-Disposition", cd);
    }

    public static interface CookieNameMatcher {

        /**
         * Indicates if specified cookie name matches.
         *
         * @param cookieName The cookie name to check
         * @return <code>true</code> if specified cookie name matches; otherwise <code>false</code>
         */
        boolean matches(String cookieName);
    }

    /**
     * Tries to determine the best protocol used for accessing this server instance. If the configuration property
     * com.openexchange.forceHTTPS is set to true, this will always be https://, otherwise the request will be used to determine the
     * protocol. https:// if it was a secure request, http:// otherwise
     *
     * @param req The HttpServletRequest used to contact this server
     * @return "http://" or "https://" depending on what was deemed more appropriate
     */
    public static String getProtocol(final HttpServletRequest req) {
        return (considerSecure(req)) ? "https://" : "http://";
    }

    public static boolean considerSecure(final HttpServletRequest req) {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configurationService != null && configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false) && !Cookies.isLocalLan(req)) {
            // HTTPS is enforced by configuration
            return true;
        }
        return req.isSecure();
    }

    public static boolean considerSecure(HttpServletRequest req, boolean force) {
        if (force && !Cookies.isLocalLan(req)) {
            return true;
        }
        return req.isSecure();
    }

    /**
     * Gets the route for specified HTTP session identifier to be used along with <i>";jsessionid"</i> URL part.
     *
     * @param httpSessionId The HTTP session identifier
     * @return The route
     */
    public static String getRoute(final String httpSessionId) {
        if (null == httpSessionId) {
            return null;
        }
        final int pos = httpSessionId.indexOf('.');
        return pos > 0 ? httpSessionId : httpSessionId + '.' + ServerServiceRegistry.getInstance().getService(SystemNameService.class).getSystemName();
    }

    private static final String NAME_ACCEPT_LANGUAGE = "Accept-Language".intern();

    /**
     * Gets the locale by <i>Accept-Language</i> header.
     *
     * @param request The request
     * @param defaultLocale The default locale to return if absent
     * @return The parsed locale
     */
    public static Locale getLocaleByAcceptLanguage(final HttpServletRequest request, final Locale defaultLocale) {
        if (null == request) {
            return defaultLocale;
        }
        String header = request.getHeader(NAME_ACCEPT_LANGUAGE);
        if (com.openexchange.java.Strings.isEmpty(header)) {
            return defaultLocale;
        }
        int pos = header.indexOf(';');
        if (pos > 0) {
            header = header.substring(0, pos);
        }
        header = header.trim();
        pos = header.indexOf(',');
        final Locale l = LocaleTools.getLocale(pos > 0 ? header.substring(0, pos) : header);
        return null == l ? defaultLocale : l;
    }

    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";

    /**
     * Utility method that determines whether the request contains multipart content
     *
     * @param request The request to be evaluated.
     * @return <code>true</code> if the request is multipart; <code>false</code> otherwise.
     */
    public static final boolean isMultipartContent(HttpServletRequest request) {
        if (null == request) {
            return false;
        }
        String contentType = request.getContentType();
        return null != contentType && asciiLowerCase(contentType).startsWith(MULTIPART);
    }

    /**
     * Copy headers from specified request to a newly generated map.
     *
     * @param req The request to copy headers from
     * @return The map containing the headers
     */
    public static final Map<String, List<String>> copyHeaders(final HttpServletRequest req) {
        if (null == req) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (final Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
            final String name = (String) e.nextElement();
            List<String> values = headers.get(name);
            if (null == values) {
                values = new LinkedList<String>();
                headers.put(name, values);
            }
            for (final Enumeration<?> valueEnum = req.getHeaders(name); valueEnum.hasMoreElements();) {
                values.add((String) valueEnum.nextElement());
            }
        }
        return headers;
    }

    /**
     * Gets the authentication Cookies from passed request.
     *
     * @param req The request
     * @return The authentication Cookies
     */
    public static com.openexchange.authentication.Cookie[] getCookieFromHeader(final HttpServletRequest req) {
        if (null == req) {
            return new com.openexchange.authentication.Cookie[0];
        }
        final Cookie[] cookies = req.getCookies();
        if (null == cookies) {
            return new com.openexchange.authentication.Cookie[0];
        }
        final com.openexchange.authentication.Cookie[] retval = new com.openexchange.authentication.Cookie[cookies.length];
        for (int i = 0; i < cookies.length; i++) {
            retval[i] = getCookie(cookies[i]);
        }
        return retval;
    }

    private static com.openexchange.authentication.Cookie getCookie(final Cookie cookie) {
        return new AuthCookie(cookie);
    }

    /**
     * Creates a new {@link HostData} instance based on the given servlet request and an optional
     * user information.
     *
     * @param servletRequest The servlet request
     * @param contextId The context id or <code>-1</code> if none is available
     * @param userId The user id or <code>-1</code> if none is available
     * @param isGuest <code>true</code> to determine the hostname for guest users, <code>false</code>, otherwise
     * @return The host data
     */
    public static HostData createHostData(HttpServletRequest request, int contextId, int userId, boolean isGuest) {
        String host = determineHostname(request, contextId, userId, isGuest);
        int port = request.getServerPort();
        String dispatcherPrefix = determineServletPrefix();
        boolean secure = considerSecure(request);
        HttpSession httpSession = request.getSession(false);
        String httpSessionID = null != httpSession ? httpSession.getId() : null;
        String route = extractRoute(httpSessionID);
        return new HostDataImpl(secure, host, port, httpSessionID, route, dispatcherPrefix);
    }

    /**
     * Extracts a request's backend "route" based on the supplied HTTP session ID, falling back to this server's default backend route as
     * configured via <code>com.openexchange.server.backendRoute</code>.
     *
     * @param httpSessionID The request's HTTP session ID, or <code>null</code> if unknown
     * @return The route, e.g. <code>OX1</code>
     */
    public static String extractRoute(String httpSessionID) {
        if (null != httpSessionID) {
            int index = httpSessionID.indexOf('.');
            if (0 < index && index < httpSessionID.length() - 1) {
                return httpSessionID.substring(index + 1);
            }
        }
        return determineBackendRoute();
    }

    /**
     * Sends an error response having a JSON body using given HTTP response
     *
     * @param httpResponse The HTTP response to use
     * @param statusCode The HTTP status code
     * @param body The associated JSON body
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If the response has already been committed
     */
    public static void sendErrorResponse(HttpServletResponse httpResponse, int statusCode, String body) throws IOException {
        sendErrorResponse(httpResponse, statusCode, Collections.<String, String> emptyMap(), body);
    }

    /**
     * Sends an error response having a JSON body using given HTTP response
     *
     * @param httpResponse The HTTP response to use
     * @param statusCode The HTTP status code
     * @param additionalHeaders Optional additional headers to apply to HTTP response
     * @param body The associated JSON body
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If the response has already been committed
     */
    public static void sendErrorResponse(HttpServletResponse httpResponse, int statusCode, Map<String, String> additionalHeaders, String body) throws IOException {
        for (Entry<String, String> header : additionalHeaders.entrySet()) {
            httpResponse.setHeader(header.getKey(), header.getValue());
        }

        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setStatus(statusCode);
        PrintWriter writer = httpResponse.getWriter();
        writer.write(body);
        writer.flush();
    }

    /**
     * Sends an error response w/o body data using given HTTP response
     *
     * @param httpResponse The HTTP response
     * @param statusCode The HTTP status code
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If the response has already been committed
     */
    public static void sendEmptyErrorResponse(HttpServletResponse httpResponse, int statusCode) throws IOException {
        sendEmptyErrorResponse(httpResponse, statusCode, Collections.<String, String> emptyMap());
    }

    /**
     * Sends an error response w/o body data using given HTTP response
     *
     * @param httpResponse The HTTP response
     * @param statusCode The HTTP status code
     * @param additionalHeaders Optional additional headers to apply to HTTP response
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If the response has already been committed
     */
    public static void sendEmptyErrorResponse(HttpServletResponse httpResponse, int statusCode, Map<String, String> additionalHeaders) throws IOException {
        httpResponse.setContentType(null);
        for (Entry<String, String> header : additionalHeaders.entrySet()) {
            httpResponse.setHeader(header.getKey(), header.getValue());
        }

        httpResponse.sendError(statusCode);
    }

    /**
     * Sends an HTML error page to HTTP response.
     *
     * @param httpResponse The HTTP response
     * @param statusCode The HTTP status code
     * @param desc The error description
     *
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If the response has already been committed
     */
    public static void sendErrorPage(HttpServletResponse httpResponse, int statusCode, String desc) throws IOException {
        httpResponse.setContentType("text/html; charset=UTF-8");
        httpResponse.setHeader("Content-Disposition", "inline");
        httpResponse.setStatus(statusCode);
        PrintWriter writer = httpResponse.getWriter();
        writer.write(getErrorPage(statusCode, null, desc));
        writer.flush();
    }

    private static final List<String> JSON_TYPES = Arrays.asList("application/json", "text/javascript");

    /**
     * Checks if the <code>"Accept"</code> header of specified HTTP request signals to expect JSON data.
     *
     * @param request The HTTP request
     * @param interpretMissingAsTrue <code>true</code> to interpret a missing/empty <code>"Accept"</code> header as <code>true</code>; otherwise <code>false</code>
     * @return <code>true</code> if JSON data is expected; otherwise <code>false</code>
     */
    public static boolean isJsonResponseExpected(HttpServletRequest request, boolean interpretMissingAsTrue) {
        if (null == request) {
            return false;
        }

        // Explicitly requested by client
        if (AJAXRequestDataTools.parseBoolParameter(request.getParameter("force_json_response"))) {
            return true;
        }

        // E.g. "Accept: application/json, text/javascript, ..."
        String acceptHdr = request.getHeader(ACCEPT);
        if (Strings.isEmpty(acceptHdr)) {
            return interpretMissingAsTrue;
        }

        float[] qualities = MIMEParse.qualities(JSON_TYPES, acceptHdr);
        return qualities[0] == 1.0f || qualities[1] == 1.0f;
    }

    /**
     * Generates a simple error page for given arguments.
     *
     * @param statusCode The status code; e.g. <code>404</code>
     * @param msg The optional status message; e.g. <code>"Not Found"</code>
     * @param desc The optional status description; e.g. <code>"The requested URL was not found on this server."</code>
     * @return A simple error page
     */
    public static String getErrorPage(int statusCode, String msg, String desc) {
        String msg0 = null == msg ? HttpStatus.getStatusText(statusCode) : msg;

        StringBuilder sb = new StringBuilder(512);
        String lineSep = System.getProperty("line.separator");
        sb.append("<!DOCTYPE html>").append(lineSep);
        sb.append("<html><head>").append(lineSep);
        {
            sb.append("<title>").append(statusCode);
            if (null != msg0) {
                sb.append(' ').append(filter(msg0));
            }
            sb.append("</title>").append(lineSep);
        }

        sb.append("</head><body>").append(lineSep);

        sb.append("<h1>");
        if (null == msg0) {
            sb.append(statusCode);
        } else {
            sb.append(filter(msg0));
        }
        sb.append("</h1>").append(lineSep);

        String desc0 = null == desc ? msg0 : desc;
        if (null != desc0) {
            sb.append("<p>").append(filter(desc0)).append("</p>").append(lineSep);
        }

        sb.append("</body></html>").append(lineSep);
        return sb.toString();
    }

    /**
     * Generates an empty HTML page for given arguments.
     *
     * @return An empty HTML page
     */
    public static String getEmptyPage() {
        StringBuilder sb = new StringBuilder(128);
        String lineSep = System.getProperty("line.separator");
        sb.append("<!DOCTYPE html>").append(lineSep);
        sb.append("<html><head>").append(lineSep);
        sb.append("<title>A blank HTML page</title>").append(lineSep);
        sb.append("<meta charset=\"utf-8\" />").append(lineSep);
        sb.append("</head><body>").append(lineSep);
        sb.append("</body></html>").append(lineSep);
        return sb.toString();
    }

    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML. This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     * @return The filtered message
     */
    public static String filter(String message) {
        HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
        return null == htmlService ? filter0(message) : htmlService.encodeForHTML(message);
    }

    private static String filter0(String message) {
        if (message == null) {
            return null;
        }

        int length = message.length();
        if (length <= 0) {
            return message;
        }

        int i = 0;
        for (int k = length; k-- > 0;) {
            char c = message.charAt(i);
            switch (c) {
            case '<':
                k = 0;
                break;
            case '>':
                k = 0;
                break;
            case '&':
                k = 0;
                break;
            case '"':
                k = 0;
                break;
            default:
                i++;
            }
        }

        if (i >= length) {
            // Nothing to escape
            return message;
        }

        StringBuilder result = new StringBuilder(length + 50);
        if (i > 0) {
            result.append(message, 0, i);
        }

        for (int k = length - i; k-- > 0;) {
            char c = message.charAt(i++);
            switch (c) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(c);
            }
        }
        return (result.toString());
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final Pattern PATTERN_BYTE_RANGES = Pattern.compile("^bytes=\\d*-\\d*(,\\d*-\\d*)*$");

    /**
     * Checks if given HTTP request provides a "Range" header, whose value matches format "bytes=n-n,n-n,n-n..."
     *
     * @param req The HTTP request to check
     * @return <code>true</code> if request queries a byte range; otherwise <code>false</code>
     */
    public static boolean hasRangeHeader(HttpServletRequest req) {
        if (null == req) {
            return false;
        }
        return isByteRangeHeader(req.getHeader("Range"));
    }

    /**
     * Checks if given "Range" header matches format "bytes=n-n,n-n,n-n..."
     *
     * @param range The "Range" header
     * @return <code>true</code> for a byte range; otherwise <code>false</code>
     */
    public static boolean isByteRangeHeader(String range) {
        // Range header should match format "bytes=n-n,n-n,n-n...".
        return ((null != range) && PATTERN_BYTE_RANGES.matcher(range).matches());
    }

    private static String determineServletPrefix() {
        try {
            return ServiceCallWrapper.tryServiceCall(Tools.class, DispatcherPrefixService.class, new ServiceUser<DispatcherPrefixService, String>() {

                @Override
                public String call(DispatcherPrefixService service) throws Exception {
                    return service.getPrefix();
                }
            }, DispatcherPrefixService.DEFAULT_PREFIX);
        } catch (ServiceException e) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }
    }

    private static String determineBackendRoute() {
        try {
            return ServiceCallWrapper.tryServiceCall(Tools.class, SystemNameService.class, new ServiceUser<SystemNameService, String>() {

                @Override
                public String call(SystemNameService service) throws Exception {
                    return service.getSystemName();
                }
            }, "OX1");
        } catch (ServiceException e) {
            return "OX1";
        }
    }

    private static String determineHostname(HttpServletRequest servletRequest, final int contextId, final int userId, final boolean isGuest) {
        String hostname = null;
        try {
            hostname = ServiceCallWrapper.tryServiceCall(Tools.class, HostnameService.class, new ServiceUser<HostnameService, String>() {

                @Override
                public String call(HostnameService service) throws Exception {
                    if (isGuest) {
                        return service.getGuestHostname(userId, contextId);
                    } else {
                        return service.getHostname(userId, contextId);
                    }
                }
            }, null);
        } catch (ServiceException e) {
            // ignore
        }

        if (hostname == null) {
            hostname = servletRequest.getServerName();
        }

        return hostname;
    }

    /**
     * Checks the existence of the given parameters within the request URL and throws an exception if at least one param hurts this rule. If the {@link ConfigurationService} is <code>null</code> no check is performed
     *
     * @param req - the request to look for URL
     * @param parameters - the URL parameter that should not occur
     * @throws OXException - if the parameters was sent by the client
     */
    public static void checkNonExistence(final HttpServletRequest req, String... parameters) throws OXException {
        ConfigurationService configService = CONFIG_SERVICE_REF.get();
        if ((configService == null) || (req == null) || (parameters == null)) {
            LOG.debug("One of the provided parameters is null. Return without checking parameters.");
            return;
        }
        if (!configService.getBoolProperty(COM_OPENEXCHANGE_CHECK_URL_PARAMS, true)) {
            LOG.debug(COM_OPENEXCHANGE_CHECK_URL_PARAMS + " configured to false. return without checking parameters.");
            return;
        }

        String queryString = req.getQueryString();
        if ((queryString == null) || (queryString.isEmpty())) {
            return;
        }

        Map<String, List<String>> parameterMap = null;
        try {
            parameterMap = splitQuery(queryString);
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Unable to analyze query string. Will not check for undesired URI params.", e);
            return;
        }
        if (parameterMap == null) {
            return;
        }

        List<String> notAllowed = new ArrayList<>();
        for (String parameter : parameters) {
            if (parameterMap.containsKey(parameter)) {
                notAllowed.add(parameter);
            }
        }

        if (!notAllowed.isEmpty()) {
            throw AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.create(Strings.concat(", ", notAllowed));
        }
    }

    private static Map<String, List<String>> splitQuery(String queryString) throws UnsupportedEncodingException {
        final Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), Charsets.UTF_8_NAME) : pair;
            if (!queryPairs.containsKey(key)) {
                queryPairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), Charsets.UTF_8_NAME) : null;
            queryPairs.get(key).add(value);
        }
        return queryPairs;
    }

    public static void setConfigurationService(final ConfigurationService configurationService) {
        CONFIG_SERVICE_REF.set(configurationService);
    }
}
