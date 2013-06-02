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

package com.openexchange.ajp13;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.servlet.http.HttpServletRequestWrapper;
import com.openexchange.ajp13.servlet.http.HttpServletResponseWrapper;
import com.openexchange.ajp13.servlet.http.HttpSessionManagement;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.codec.QuotedPrintable;
import com.openexchange.tools.regex.RFC2616Regex;
import com.openexchange.tools.servlet.http.Cookies;

/**
 * AJPv13ForwardRequest - this class' purpose is mainly to fill the http servlet request from AJP's forward request, to identify servlet
 * instance through request path and to apply the load-balancing and http-session-identifying <tt>JSESSIONID</tt> cookie or URL parameter to
 * the http serlvet response
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ForwardRequest extends AbstractAJPv13Request {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13ForwardRequest.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String methods[] =
        {
            "OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "PROPFIND", "PROPPATCH", "MKCOL", "COPY", "MOVE", "LOCK", "UNLOCK",
            "ACL", "REPORT", "VERSION-CONTROL", "CHECKIN", "CHECKOUT", "UNCHECKOUT", "SEARCH", "MKWORKSPACE", "UPDATE", "LABEL", "MERGE",
            "BASELINE_CONTROL", "MKACTIVITY" };

    private static final int BYTE_FIRST = 0xA0;

    private static final int BYTE_CONTENT_TYPE = 0x07;

    private static final int BYTE_COOKIE = 0x09;

    private static final TIntObjectMap<String> httpHeaderMapping;

    private static final TIntObjectMap<String> attributeMapping;

    private static final String DEFAULT_ENCODING = ServerConfig.getProperty(Property.DefaultEncoding);

    private static final String STR_EMPTY = "";

    private static final String MIME_FORM_DATA = "application/x-www-form-urlencoded";

    private static final String HDR_CONTENT_TYPE = "content-type";

    private static final String HDR_CONTENT_LENGTH = "content-length";

    private static final String ATTR_STORED_METHOD = "stored_method";

    private static final String ATTR_QUERY_STRING = "query_string";

    private static final String ATTR_SSL_KEY_SIZE = "ssl_key_size";

    /**
     * This byte value indicates termination of a forward request.
     */
    private static final int REQUEST_TERMINATOR = 0xFF;

    private static final boolean forceHttps;

    static {
        httpHeaderMapping = new TIntObjectHashMap<String>(14);
        httpHeaderMapping.put(0x01, "accept");
        httpHeaderMapping.put(0x02, "accept-charset");
        httpHeaderMapping.put(0x03, "accept-encoding");
        httpHeaderMapping.put(0x04, "accept-language");
        httpHeaderMapping.put(0x05, "authorization");
        httpHeaderMapping.put(0x06, "connection");
        httpHeaderMapping.put(BYTE_CONTENT_TYPE, HDR_CONTENT_TYPE);
        httpHeaderMapping.put(0x08, HDR_CONTENT_LENGTH);
        httpHeaderMapping.put(BYTE_COOKIE, "cookie");
        httpHeaderMapping.put(0x0a, "cookie2");
        httpHeaderMapping.put(0x0b, "host");
        httpHeaderMapping.put(0x0c, "pragma");
        httpHeaderMapping.put(0x0d, "referer");
        httpHeaderMapping.put(0x0e, "user-agent");

        attributeMapping = new TIntObjectHashMap<String>(14);
        attributeMapping.put(0x01, "context");
        attributeMapping.put(0x02, "servlet_path");
        attributeMapping.put(0x03, "remote_user");
        attributeMapping.put(0x04, "auth_type");
        attributeMapping.put(0x05, ATTR_QUERY_STRING);
        attributeMapping.put(0x06, "jvm_route");
        attributeMapping.put(0x07, "ssl_cert");
        attributeMapping.put(0x08, "ssl_cipher");
        attributeMapping.put(0x09, "ssl_session");
        attributeMapping.put(0x0a, "req_attribute");
        attributeMapping.put(0x0b, ATTR_SSL_KEY_SIZE);
        attributeMapping.put(0x0c, "secret_attribute");
        attributeMapping.put(0x0d, ATTR_STORED_METHOD);
        attributeMapping.put(REQUEST_TERMINATOR, "are_done");

        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (configurationService == null) {
            forceHttps = false;
        } else {
            forceHttps = configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false);
        }
    }

    /**
     * Initializes a new {@link AJPv13ForwardRequest}
     *
     * @param payloadData The payload data
     */
    public AJPv13ForwardRequest(final byte[] payloadData, final int dataLength) {
        super(payloadData, dataLength);
    }

    @Override
    public void processRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
        /*
         * Create Servlet Request with its InputStream
         */
        final HttpServletRequestWrapper servletRequest = new HttpServletRequestWrapper(ajpRequestHandler);
        servletRequest.setInputStream(new AJPv13ServletInputStream(ajpRequestHandler.getAJPConnection()));
        /*
         * Create Servlet Response with its OutputStream
         */
        final HttpServletResponseWrapper servletResponse = new HttpServletResponseWrapper(servletRequest);
        servletResponse.setServletOutputStream(new AJPv13ServletOutputStream(ajpRequestHandler.getAJPConnection()));
        /*
         * Determine method: If next byte is equal to 0xff then the method is given by "stored_method" attribute
         */
        final int encodedMethod = nextByte();
        if (encodedMethod != REQUEST_TERMINATOR) {
            servletRequest.setMethod(methods[encodedMethod - 1]);
        }
        /*
         * Determine protocol
         */
        servletRequest.setProtocol(parseString("protocol"));
        /*
         * Determine req_uri
         */
        String requestURI = null;
        String jsessionID = null;
        {
            requestURI = parseString("req_uri");
            final String jsessionIdURI = AJPv13RequestHandler.JSESSIONID_URI;
            final int pos = requestURI.toLowerCase(Locale.ENGLISH).indexOf(jsessionIdURI);
            if (pos > -1) {
                jsessionID = requestURI.substring(pos + jsessionIdURI.length());
//                requestURI = requestURI.substring(0, pos);
                servletRequest.setRequestedSessionIdFromURL(true);
                servletRequest.setRequestedSessionIdFromCookie(false);
            }
            servletRequest.setRequestURI(requestURI);
        }
        /*
         * Determine remote_addr
         */
        servletRequest.setRemoteAddr(parseString("remote_addr"));
        /*
         * Determine remote_host
         */
        servletRequest.setRemoteHost(parseString("remote_host"));
        /*
         * Determine server_name
         */
        servletRequest.setServerName(parseString("server_name"));
        /*
         * Determine server_port
         */
        servletRequest.setServerPort(parseInt());
        /*
         * Determine is_ssl
         */
        servletRequest.setSecure(parseBoolean());
        /*
         * Determine num_headers
         */
        final int numHeaders = parseInt();
        /*
         * Determine Request Headers
         */
        parseRequestHeaders(servletRequest, numHeaders);
        {
            /*
             * Set important header CONTENT_LENGTH which decides whether to further process an upcoming body request from web server or to
             * terminate communication after this forward request. If this header is missing the getIntHeader() method returns -1 which
             * represents a missing content length in request handler, too.
             */
            long contentLength = -1;
            if (servletRequest.containsHeader(HDR_CONTENT_LENGTH)) {
                try {
                    contentLength = Long.parseLong(servletRequest.getHeader(HDR_CONTENT_LENGTH));
                } catch (final NumberFormatException e) {
                    LOG.error("Content-Length header cannot parsed as a number", e);
                    contentLength = -1;
                }
            }
            if (contentLength == -1) {
                ajpRequestHandler.setContentLength(AJPv13RequestHandler.NOT_SET);
            } else {
                ajpRequestHandler.setContentLength(contentLength);
            }
        }
        if (LogProperties.isEnabled()) {
            /*
             * Gather logging info
             */
            final String echoHeaderName = AJPv13Response.getEchoHeaderName();
            if (null != echoHeaderName) {
                final String echoValue = servletRequest.getHeader(echoHeaderName);
                if (null != echoValue) {
                    LogProperties.putLogProperty(LogProperties.Name.AJP_REQUEST_ID, echoValue);
                }
            }
        }
        LogProperties.putLogProperty(LogProperties.Name.AJP_REQUEST_IP, servletRequest.getRemoteAddr());
        LogProperties.putLogProperty(LogProperties.Name.AJP_SERVER_NAME, servletRequest.getServerName());
        /*
         * Determine if content type indicates form data
         */
        if (servletRequest.containsHeader(HDR_CONTENT_TYPE) && MIME_FORM_DATA.regionMatches(
            0,
            servletRequest.getHeader(HDR_CONTENT_TYPE),
            0,
            33)) {
            ajpRequestHandler.setFormData(true);
        }
        /*
         * End of payload data NOT reached
         */
        if (!compareNextByte(REQUEST_TERMINATOR)) {
            /*
             * Determine Attributes
             */
            parseAttributes(servletRequest);
            if ((encodedMethod == -1) && servletRequest.containsAttribute(ATTR_STORED_METHOD)) {
                servletRequest.setMethod((String) servletRequest.getAttribute(ATTR_STORED_METHOD));
            }
            if (servletRequest.containsAttribute(ATTR_QUERY_STRING)) {
                parseQueryString(servletRequest, (String) servletRequest.getAttribute(ATTR_QUERY_STRING), true);
            }
        }
        /*
         * Apply request/response to AJP request handler
         */
        ajpRequestHandler.setServletRequest(servletRequest);
        ajpRequestHandler.setServletResponse(servletResponse);
        /*
         * Create servlet instance dependent on requested URI
         */
        ajpRequestHandler.setServletInstance(requestURI);
        /*
         * Set servlet path and path info
         */
        final String servletPath = ajpRequestHandler.getServletPath();
        if (null != servletPath) {
            /*
             * Apply the servlet path with leading "/" character
             */
            final int servletPathLen = servletPath.length();
            if ((1 == servletPathLen) && ('*' == servletPath.charAt(0))) {
                /*
                 * Set an empty string ("") if the servlet used to process this request was matched using the "/*" pattern.
                 */
                servletRequest.setServletPath(STR_EMPTY);
                /*
                 * Set complete request URI as path info
                 */
                servletRequest.setPathInfo(requestURI);
            } else {
                /*
                 * The path starts with a "/" character and includes either the servlet name or a path to the servlet, but does not include
                 * any extra path information or a query string.
                 */
                servletRequest.setServletPath(servletPath);
                /*
                 * Set path info: The extra path information follows the servlet path but precedes the query string and will start with a
                 * "/" character.
                 */
                if ((requestURI.length() > servletPathLen) /* && requestURI.startsWith(servletPath) */) {
                    servletRequest.setPathInfo(requestURI.substring(servletPathLen));
                } else {
                    servletRequest.setPathInfo(null);
                }
            }
        }
        /*
         * JSESSIONID
         */
        if (jsessionID == null) {
            /*
             * Look for JSESSIONID cookie, if request URI does not contain session id
             */
            checkJSessionIDCookie(servletRequest, servletResponse, ajpRequestHandler);
        } else {
            final int dot = jsessionID.lastIndexOf('.');
            if ((dot == -1) || (AJPv13Config.getJvmRoute().equals(jsessionID.substring(dot + 1)))) {
                addJSessionIDCookie(jsessionID, servletRequest, ajpRequestHandler);
            } else {
                /*
                 * JVM route does not match
                 */
                createJSessionIDCookie(servletRequest, ajpRequestHandler);
            }
        }
    }

    private static final java.util.regex.Pattern PATTERN_SPLIT = java.util.regex.Pattern.compile("&");

    /**
     * Parses a query string and puts resulting parameters into given servlet request
     *
     * @param servletRequest The servlet request
     * @param queryStr The query string to be parsed
     * @throws UnsupportedEncodingException If charset provided by servlet request is not supported
     */
    public static void parseQueryString(final HttpServletRequestWrapper servletRequest, final String queryStr) throws UnsupportedEncodingException {
        parseQueryString(servletRequest, queryStr, false);
    }

    /**
     * Parses a query string and puts resulting parameters into given servlet request.
     *
     * @param servletRequest The servlet request
     * @param queryStr The query string to be parsed
     * @param fromAttribute <code>true</code> if query string comes from request's attributes; otherwise <code>false</code>
     * @throws UnsupportedEncodingException If charset provided by servlet request is not supported
     */
    private static void parseQueryString(final HttpServletRequestWrapper servletRequest, final String queryStr, final boolean fromAttribute) throws UnsupportedEncodingException {
        if (fromAttribute) {
            servletRequest.setQueryString(queryStr);
        }
        final String[] paramsNVPs = PATTERN_SPLIT.split(queryStr, 0);
        for (String paramsNVP : paramsNVPs) {
            paramsNVP = paramsNVP.trim();
            if (paramsNVP.length() > 0) {
                // Look-up character '='
                final int pos = paramsNVP.indexOf('=');
                if (pos >= 0) {
                    servletRequest.setParameter(paramsNVP.substring(0, pos), decodeQueryStringValue(
                        servletRequest.getCharacterEncoding(),
                        paramsNVP.substring(pos + 1)));
                } else {
                    servletRequest.setParameter(paramsNVP, STR_EMPTY);
                }
            }
        }
    }

    private void parseRequestHeaders(final HttpServletRequestWrapper servletRequest, final int numHeaders) throws AJPv13Exception {
        boolean contentTypeSet = false;
        nextHeader: for (int i = numHeaders; i > 0; i--) {
            final String headerName;
            final boolean isCookie;
            {
                final int firstByte = nextByte();
                final int secondByte = nextByte();
                if (firstByte == BYTE_FIRST) {
                    /*
                     * Header name is encoded as an integer value.
                     */
                    headerName = httpHeaderMapping.get(secondByte);
                    if (!contentTypeSet && (BYTE_CONTENT_TYPE == secondByte)) {
                        servletRequest.setContentType(parseString());
                        contentTypeSet = true;
                        continue nextHeader;
                    }
                    isCookie = (BYTE_COOKIE == secondByte);
                } else {
                    headerName = parseString(firstByte, secondByte);
                    if (!contentTypeSet && HDR_CONTENT_TYPE.equalsIgnoreCase(headerName)) {
                        servletRequest.setContentType(parseString());
                        contentTypeSet = true;
                        continue nextHeader;
                    }
                    isCookie = ("cookie".equalsIgnoreCase(headerName));
                }
            }
            final String headerValue = parseString();
            if (isCookie) {
                servletRequest.setCookies(parseCookieHeader(headerValue));
            } else {
                servletRequest.setHeader(headerName, headerValue, false);
            }
        }
    }

    private static final Set<String> COOKIE_PARAMS = new HashSet<String>(Arrays.asList("$Path", "$Domain", "$Port"));

    private static Cookie[] parseCookieHeader(final String headerValue) throws AJPv13Exception {
        final Matcher m = RFC2616Regex.COOKIE.matcher(headerValue);
        final List<Cookie> cookieList = new ArrayList<Cookie>();
        final StringBuilder valueBuilder = new StringBuilder(128);
        int prevEnd = -1;
        while (m.find()) {
            // offset + 1 -> complete single cookie
            int version = -1;
            {
                final String versionStr = m.group(2);
                if (versionStr == null) {
                    version = 0;
                } else {
                    try {
                        version = Integer.parseInt(versionStr);
                    } catch (final NumberFormatException e) {
                        version = 0;
                        if (DEBUG) {
                            LOG.debug("Version set to 0. No number value in $Version cookie: " + versionStr);
                        }
                    }
                }
            }
            final String name = m.group(3);
            if (null == name) {
                // Regex has always at minimum 2 cookies as groups.
                continue;
            }
            if ((name.length() > 0) && (name.charAt(0) == '$')) {
                if (COOKIE_PARAMS.contains(name)) {
                    /*
                     * A wrongly parsed cookie name which is actually a cookie parameter. Force re-parse of previous cookie.
                     */
                    continue;
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder(32).append("Special cookie ").append(name).append(" not handled, yet!"));
                }
            }
            if (prevEnd != -1) {
                final int start = m.start();
                if (start > prevEnd) {
                    // Last cookie skipped some characters
                    final String skipped = prepare(headerValue.substring(prevEnd, start));
                    if (skipped.length() > 0) {
                        reparsePrevCookie(headerValue, skipped, valueBuilder, cookieList);
                    }
                }
            }
            prevEnd = m.end();
            final Cookie c;
            try {
                c = new Cookie(name, prepare(m.group(4)));
            } catch (final RuntimeException e) {
                /*
                 * Cookie name contains illegal characters (for example, a comma, space, or semicolon) or it is one of the tokens reserved
                 * for use by the cookie protocol
                 */
                if (DEBUG) {
                    LOG.debug("Invalid cookie name detected. Ignoring...", e);
                }
                continue;
            }
            c.setVersion(version);
            String attr = m.group(5);
            if (attr != null) {
                /*
                 * Set $Path parameter
                 */
                c.setPath(attr);
            }
            attr = m.group(6);
            if (attr != null) {
                /*
                 * Set $Domain parameter
                 */
                c.setDomain(attr);
            }
            /*
             * Ignore $Port, apply version, and add to list
             */
            cookieList.add(c);
        }
        final int len = headerValue.length();
        if (len > prevEnd && prevEnd >= 0) {
            // Last cookie skipped some characters
            final String skipped = prepare(headerValue.substring(prevEnd, len));
            if (skipped.length() > 0) {
                reparsePrevCookie(headerValue, skipped, valueBuilder, cookieList);
            }
        }
        if ((headerValue.length() > 0) && cookieList.isEmpty()) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.INVALID_COOKIE_HEADER, true, headerValue);
        }
        if (DEBUG) {
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(256).append("Parsed Cookies:\n");
            for (final Cookie cookie : cookieList) {
                sb.append('\'').append(cookie.getName()).append("'='").append(cookie.getValue()).append("'\n");
            }
            LOG.debug(sb.toString());
        }
        return cookieList.toArray(new Cookie[cookieList.size()]);
    }

    /**
     * Re-Parse previous cookie in list
     *
     * @param headerValue The complete cookie header value
     * @param skipped The skipped string
     * @param valueBuilder A string builder needed to compose proper cookie value
     * @param cookieList The cookie list
     */
    private static void reparsePrevCookie(final String headerValue, final String skipped, final StringBuilder valueBuilder, final List<Cookie> cookieList) {
        final String prevValue;
        final Cookie prevCookie = cookieList.get(cookieList.size() - 1);
        valueBuilder.append(prevCookie.getValue());
        String prevAttr = prevCookie.getPath();
        if (null != prevAttr) {
            valueBuilder.append("; $Path=").append(prevAttr);
        }
        prevAttr = prevCookie.getDomain();
        if (null != prevAttr) {
            valueBuilder.append("; $Domain=").append(prevAttr);
        }
        valueBuilder.append(skipped);
        final String complVal = valueBuilder.toString();
        valueBuilder.setLength(0);
        int paramsStart = -1;
        /*
         * Check for parameters except $Port
         */
        Matcher paramMatcher = RFC2616Regex.COOKIE_PARAM_PATH.matcher(complVal);
        if (paramMatcher.find()) {
            paramsStart = paramMatcher.start();
            prevCookie.setPath(paramMatcher.group(1));
        }
        paramMatcher = RFC2616Regex.COOKIE_PARAM_DOMAIN.matcher(complVal);
        if (paramMatcher.find()) {
            paramsStart = Math.min(paramMatcher.start(), paramsStart);
            prevCookie.setDomain(paramMatcher.group(1));
        }
        if (paramsStart == -1) {
            prevValue = complVal;
        } else {
            prevValue = complVal.substring(0, paramsStart);
        }
        prevCookie.setValue(prevValue);
    }

    /**
     * Prepares passed cookie value.
     *
     * @param cookieValue The cookie value to prepare
     * @return The prepared cookie value.
     */
    private static String prepare(final String cookieValue) {
        if (null == cookieValue || cookieValue.length() == 0) {
            return cookieValue;
        }
        String cv = cookieValue;
        int mlen = cv.length() - 1;
        if (cv.charAt(mlen) == ';') {
            if (mlen == 0) {
                return "";
            }
            do {
                mlen--;
            } while ((mlen > 0) && (cv.charAt(mlen) == ';'));
            cv = cv.substring(0, mlen);
        }
        if ((mlen > 0) && (cv.charAt(0) == '"') && (cv.charAt(mlen) == '"')) {
            cv = cv.substring(1, mlen);
        }
        return cv;
    }

    private void parseAttributes(final HttpServletRequestWrapper servletRequest) throws AJPv13Exception {
        int attrNum = REQUEST_TERMINATOR;
        while ((attrNum = nextByte()) != REQUEST_TERMINATOR) {
            if (0x0b == attrNum) {
                servletRequest.setAttribute(ATTR_SSL_KEY_SIZE, Integer.valueOf(parseInt()));
            } else if (0x0a == attrNum) {
                /*-
                 * "req_attribute": This optional attribute has a special syntax.
                 *
                 * (sc_a_req_attribute string)(string) => 0a <attribute-name> <attribute-value>
                 *
                 * Example:
                 * 0a 00 0f 41 4a 50 5f 52 45 4d 4f 54 45 5f 50 4f 52 54 00 00 05 33 33 38 36 36 00
                 *
                 * <req_attribute> AJP_REMOTE_PORT 33866
                 */
                servletRequest.setAttribute(parseString(), parseString());
            } else {
                final String attributeName = attributeMapping.get(attrNum);
                if (attributeName == null) {
                    throw new AJPv13Exception(AJPCode.NO_ATTRIBUTE_NAME, true, Integer.valueOf(attrNum));
                }
                servletRequest.setAttribute(attributeName, parseString());
            }
        }
    }

    private static void checkJSessionIDCookie(final HttpServletRequestWrapper servletRequest, final HttpServletResponseWrapper resp, final AJPv13RequestHandler ajpRequestHandler) {
        final Cookie[] cookies = servletRequest.getCookies();
        Cookie jsessionIDCookie = null;
        if (cookies != null) {
            nextCookie: for (int i = 0; (i < cookies.length) && (jsessionIDCookie == null); i++) {
                final Cookie current = cookies[i];
                if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(current.getName())) {
                    /*
                     * Check JVM route
                     */
                    final String id = current.getValue();
                    final int pos = id.lastIndexOf('.');
                    final String jvmRoute = AJPv13Config.getJvmRoute();
                    if (pos > -1) {
                        if ((jvmRoute != null) && !jvmRoute.equals(id.substring(pos + 1))) {
                            /*
                             * Different JVM route detected -> Discard
                             */
                            if (DEBUG) {
                                LOG.debug(new StringBuilder("\n\tDifferent JVM route detected. Removing JSESSIONID cookie: ").append(id));
                            }
                            current.setPath("/");
                            current.setMaxAge(0); // delete
                            current.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                            resp.addCookie(current);
                            continue nextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                LOG.debug(new StringBuilder("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setPath("/");
                            current.setMaxAge(0); // delete
                            current.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                            resp.addCookie(current);
                            continue nextCookie;
                        }
                        jsessionIDCookie = current;
                        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, id);
                        jsessionIDCookie.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                        ajpRequestHandler.setHttpSessionCookie(jsessionIDCookie, true);
                    } else {
                        /*
                         * Value does not apply to pattern [UID].[JVM-ROUTE], hence only UID is given through special cookie JSESSIONID.
                         */
                        if (jvmRoute != null) {
                            /*
                             * But this host defines a JVM route
                             */
                            if (DEBUG) {
                                LOG.debug(new StringBuilder("\n\tMissing JVM route in JESSIONID cookie").append(current.getValue()));
                            }
                            current.setPath("/");
                            current.setMaxAge(0); // delete
                            current.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                            resp.addCookie(current);
                            continue nextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                LOG.debug(new StringBuilder("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setPath("/");
                            current.setMaxAge(0); // delete
                            current.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                            resp.addCookie(current);
                            continue nextCookie;
                        }
                        jsessionIDCookie = current;
                        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, id);
                        jsessionIDCookie.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
                        ajpRequestHandler.setHttpSessionCookie(jsessionIDCookie, true);
                    }
                }
            }
        }
        if (jsessionIDCookie == null) {
            createJSessionIDCookie(servletRequest, ajpRequestHandler);
        }
    }

    private static void createJSessionIDCookie(final HttpServletRequestWrapper servletRequest, final AJPv13RequestHandler ajpRequestHandler) {
        addJSessionIDCookie(null, servletRequest, ajpRequestHandler);
    }

    private static void addJSessionIDCookie(final String id, final HttpServletRequestWrapper servletRequest, final AJPv13RequestHandler ajpRequestHandler) {
        final String jvmRoute = AJPv13Config.getJvmRoute();
        final String jsessionIdVal;
        final boolean join;
        if (id == null) {
            /*
             * Create a new unique id
             */
            final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
            if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
                jsessionIDVal.append('.').append(jvmRoute);
            }
            jsessionIdVal = jsessionIDVal.toString();
            join = true;
        } else {
            /*
             * Check known JSESSIONIDs and corresponding HTTP session
             */
            if (HttpSessionManagement.isHttpSessionValid(id)) {
                jsessionIdVal = id;
                join = false;
            } else {
                /*
                 * Invalid cookie. Create a new unique id
                 */
                final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
                if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
                    jsessionIDVal.append('.').append(jvmRoute);
                }
                jsessionIdVal = jsessionIDVal.toString();
                join = true;
            }
        }
        final Cookie jsessionIDCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, jsessionIdVal);
        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, jsessionIdVal);
        jsessionIDCookie.setSecure((forceHttps && !Cookies.isLocalLan(servletRequest)) || servletRequest.isSecure());
        ajpRequestHandler.setHttpSessionCookie(jsessionIDCookie, join);
        /*
         * HttpServletRequestWrapper.getSession() adds the JSESSIONID cookie
         */
        servletRequest.getSession(true);
    }

    private String parseString(final String headerName) throws AJPv13Exception {
        try {
            return parseString();
        } catch (final AJPv13Exception e) {
            throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, true, e, headerName);
        }
    }

    private String parseString() throws AJPv13Exception {
        return parseString(nextByte(), nextByte());
    }

    private static final int ASCII_LIMIT = 127;

    private static final int STRING_TERMINATOR = 0x00;

    /**
     * First two bytes, which indicate length of string, already consumed.
     */
    private String parseString(final int firstByte, final int secondByte) throws AJPv13Exception {
        /*
         * Special byte 0xFF indicates absence of current string value.
         */
        if ((firstByte == REQUEST_TERMINATOR) && (secondByte == REQUEST_TERMINATOR)) {
            return STR_EMPTY;
        }
        boolean encoded = false;
        final int strLength = ((firstByte) << 8) + secondByte;
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(strLength);
        for (int strIndex = 0; strIndex < strLength; strIndex++) {
            final int b = nextByte();
            if (b > ASCII_LIMIT) { // non-ascii character
                encoded = true;
                sb.append('=').append(Integer.toHexString(b));
            } else {
                sb.append((char) b);
            }
        }
        if (nextByte() != STRING_TERMINATOR) {
            throw new AJPv13Exception(AJPCode.UNPARSEABLE_STRING, true);
        }
        if (encoded) {
            try {
                return QuotedPrintable.decodeString(sb.toString(), DEFAULT_ENCODING == null ? "UTF-8" : DEFAULT_ENCODING);
            } catch (final IOException e) {
                throw new AJPv13Exception(AJPCode.IO_ERROR, true, e, e.getMessage());
            } catch (final MessagingException e) {
                throw new AJPv13Exception(AJPCode.MESSAGING_ERROR, true, e, e.getMessage());
            }
        }
        return sb.toString();
    }

    private boolean parseBoolean() {
        return (nextByte() > 0);
    }

    private static String decodeQueryStringValue(final String charEnc, final String queryStringValue) {
        return AJPv13Utility.decodeUrl(queryStringValue, charEnc == null ? AJPv13Config.getServerProperty(Property.DefaultEncoding) : charEnc);
    }

}
