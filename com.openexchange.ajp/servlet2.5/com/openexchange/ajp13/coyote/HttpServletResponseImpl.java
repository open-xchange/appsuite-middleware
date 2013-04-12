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

package com.openexchange.ajp13.coyote;

import static com.openexchange.ajp13.coyote.CookieWrapper.wrapper;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.AJPv13ServletOutputStream;
import com.openexchange.ajp13.servlet.ServletResponseWrapper;
import com.openexchange.ajp13.servlet.http.HttpDateFormatRegistry;
import com.openexchange.ajp13.util.CharsetValidator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.AsciiWriter;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.version.Version;

/**
 * {@link HttpServletResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpServletResponseImpl implements HttpServletResponse {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HttpServletResponseImpl.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    public static final int OUTPUT_NOT_SELECTED = -1;

    public static final int OUTPUT_STREAM = 1;

    public static final int OUTPUT_WRITER = 2;

    private static volatile String defaultCharset;

    private static String getDefaultCharset() {
        String tmp = defaultCharset;
        if (tmp == null) {
            synchronized (ServletResponseWrapper.class) {
                tmp = defaultCharset;
                if (tmp == null) {
                    final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return "UTF-8";
                    }
                    defaultCharset = tmp = service.getProperty("DefaultEncoding", "UTF-8");
                }
            }
        }
        return tmp;
    }

    private static final String ERROR_PAGE_TEMPL =
        "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<html><head>\r\n" + "<title>#STATUS_CODE# #STATUS_MSG#</title>\r\n" + "</head><body>\r\n" + "<h1>#STATUS_CODE# #STATUS_MSG#</h1>\r\n" + "<p>#STATUS_DESC#</p>\r\n" + "<hr>\r\n" + "<address>#DATE#,&nbsp;Open-Xchange v#VERSION#</address>\r\n" + "</body></html>";

    public static final TIntObjectMap<String> STATUS_MSGS;

    public static final TIntObjectMap<String> STATUS_DESC;

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

    /**
     * A set for known single-value headers. Those headers which occur only once in HTTP headers.
     */
    private static final Set<String> SINGLE_VALUE_HEADERS = Constants.SINGLE_VALUE_HEADERS;

    private final Set<CookieWrapper> cookies;

    private String statusMsg;

    private final AjpProcessor ajpProcessor;

    private final boolean httpOnly;

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String CONTENT_LENGTH = "Content-Length";

    private String characterEncoding;

    private int status;

    private final Map<String, List<String>> headers;

    private Locale locale;

    private boolean committed;

    private int bufferSize;

    private ActionAwareServletOutputStream servletOutputStream;

    private int outputSelection;

    private PrintWriter writer;

    //private OutputBuffer outputBuffer;

    /**
     * Initializes a new {@link ServletResponseWrapper}
     */
    public HttpServletResponseImpl(final AjpProcessor ajpProcessor) {
        super();
        headers = new HashMap<String, List<String>>(16);
        outputSelection = OUTPUT_NOT_SELECTED;
        cookies = new LinkedHashSet<CookieWrapper>(8);
        status = HttpServletResponse.SC_OK;
        statusMsg = "OK";
        this.ajpProcessor = ajpProcessor;
        final ConfigurationService cs = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
        httpOnly = (null != cs && cs.getBoolProperty(ServerConfig.Property.COOKIE_HTTP_ONLY.getPropertyName(), true));
    }

    /**
     * Sets the output buffer
     *
     * @param outputBuffer The output buffer to set
     */
    public void setOutputBuffer(final OutputBuffer outputBuffer) {
        servletOutputStream = null == outputBuffer ? null : new ActionAwareServletOutputStream(outputBuffer);
    }

    /**
     * Gets the AJP processor.
     *
     * @return The AJP processor
     */
    public AjpProcessor getAjpProcessor() {
        return ajpProcessor;
    }

    /**
     * Recycles this response.
     */
    public void recycle() {
        servletOutputStream.recycle();
        headers.clear();
        outputSelection = OUTPUT_NOT_SELECTED;
        writer = null;
        cookies.clear();
        status = HttpServletResponse.SC_OK;
        characterEncoding = null;
        statusMsg = "OK";
        committed = false;
        locale = null;
        bufferSize = 0;
    }

    private static final Pattern CONTENT_TYPE_CHARSET_PARAM = Pattern.compile("(;\\s*charset=)([^\\s|^;]+)");

    @Override
    public void setContentType(final String contentType) {
        if (contentType == null) {
            return;
        }
        final Matcher matcher = CONTENT_TYPE_CHARSET_PARAM.matcher(contentType);
        if (matcher.find()) {
            /*
             * Check if getWriter() was already called
             */
            if (outputSelection == OUTPUT_WRITER && !characterEncoding.equalsIgnoreCase(matcher.group(2))) {
                throw new IllegalStateException("\"getWriter()\" has already been called. Not allowed to change its encoding afterwards");
            }
            do {
                setCharacterEncoding(matcher.group(2));
            } while (matcher.find());
        } else if (characterEncoding == null) {
            /*
             * Corresponding to rfc
             */
            setCharacterEncoding(getDefaultCharset());
        }
        headers.put(CONTENT_TYPE, Collections.singletonList(contentType));
    }

    @Override
    public String getContentType() {
        final List<String> list = headers.get(CONTENT_TYPE);
        return null == list ? null : list.get(0);
    }

    @Override
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setContentLength(final int contentLength) {
        headers.put(CONTENT_LENGTH, Collections.singletonList(Integer.toString(contentLength)));
    }

    public int getContentLength() {
        return headers.containsKey(CONTENT_LENGTH) ? Integer.parseInt((headers.get(CONTENT_LENGTH)).get(0)) : 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        /*-
         * Since PrintWriter simply delegates flush() invocation to underlying OutputStream,
         * we can safely call ServletOutputStream.flush() directly.
         *
         * See implementation of flush() inside PrintWriter:
         *    public void flush() {
         *        try {
         *        synchronized (lock) {
         *        ensureOpen()
         *        out.flush();
         *        }
         *        }
         *        catch (IOException x) {
         *        trouble = true;
         *        }
         *    }
         */
        Streams.flush(writer);
        Streams.flush(servletOutputStream);
    }

    /**
     * Sets the character encoding
     *
     * @param characterEncoding
     */
    @Override
    public void setCharacterEncoding(final String characterEncoding) {
        /*
         * Check if getWriter() was already called
         */
        if (outputSelection == OUTPUT_WRITER && !characterEncoding.equalsIgnoreCase(characterEncoding)) {
            throw new IllegalStateException("\"getWriter()\" has already been called. " + "Not allowed to change its encoding afterwards");
        }
        setCharacterEncoding(characterEncoding, true);
    }

    /**
     * Sets the character encoding
     *
     * @param characterEncoding
     * @param checkContentType
     */
    private void setCharacterEncoding(final String characterEncoding, final boolean checkContentType) {
        this.characterEncoding = characterEncoding;
        if (checkContentType && headers.containsKey(CONTENT_TYPE)) {
            final String contentType = headers.get(CONTENT_TYPE).get(0);
            final Matcher m = CONTENT_TYPE_CHARSET_PARAM.matcher(contentType);
            if (m.find()) {
                /*
                 * Charset argument set in content type and differs from new charset
                 */
                if (!characterEncoding.equalsIgnoreCase(m.group(2))) {
                    final StringBuilder newContentType = new StringBuilder();
                    final MatcherReplacer mr = new MatcherReplacer(m, contentType);
                    mr.appendLiteralReplacement(newContentType, new com.openexchange.java.StringAllocator().append(m.group(1)).append(characterEncoding).toString());
                    while (m.find()) {
                        mr.appendLiteralReplacement(
                            newContentType,
                            new com.openexchange.java.StringAllocator().append(m.group(1)).append(characterEncoding).toString());
                    }
                    mr.appendTail(newContentType);
                    headers.put(CONTENT_TYPE, Collections.singletonList(newContentType.toString()));
                }
            } else {
                /*
                 * No charset argument set in content type, yet
                 */
                final String newCT = contentType + "; charset=" + characterEncoding;
                headers.put(CONTENT_TYPE, Collections.singletonList(newCT));
            }
        }
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding == null ? (characterEncoding = getDefaultCharset()) : characterEncoding;
    }

    @Override
    public void resetBuffer() {
        if (committed) {
            throw new IllegalStateException("resetBuffer(): The response has already been committed");
        }
        if (null == writer) {
            servletOutputStream.resetBuffer();
        } else {
            // TODO:
        }
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException, IOException {
        if (false && outputSelection == OUTPUT_STREAM) {
            throw new IllegalStateException("Servlet's OutputStream has already been selected as output");
        }
        if (null != writer) {
            return writer;
        }
        if (characterEncoding == null) {
            /*
             * Method setContentType() has not been called prior to call getWriter()
             */
            characterEncoding = getDefaultCharset();
        }
        /*
         * Check Charset Encoding
         */
        CharsetValidator.getInstance().checkCharset(characterEncoding);
        /*
         * Check if getOutputSteam hasn't been called before
         */
        if (false && outputSelection == OUTPUT_STREAM) {
            throw new IllegalStateException("Servlet's OutputStream has already been selected as output");
        }
        if (outputSelection == OUTPUT_NOT_SELECTED) {
            outputSelection = OUTPUT_WRITER;
        }
        if (bufferSize > 0) {
            final Writer w = Charsets.isAsciiCharset(characterEncoding) ? new AsciiWriter(servletOutputStream) : new OutputStreamWriter(servletOutputStream, characterEncoding);
            writer = new PrintWriter(w, false);
        } else {
            final Writer w = Charsets.isAsciiCharset(characterEncoding) ? new AsciiWriter(servletOutputStream) : new OutputStreamWriter(servletOutputStream, characterEncoding);
            writer = new PrintWriter(w, false);
        }
        return writer;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Sets the committed flag
     *
     * @param committed
     */
    public void setCommitted(final boolean committed) {
        this.committed = committed;
    }

    @Override
    public void setBufferSize(final int bufferSize) {
        if (outputSelection != OUTPUT_NOT_SELECTED) {
            throw new IllegalStateException("Buffer size MUSTN'T be altered when body content has already been written/selected.");
        }
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @return the underlying {@link AJPv13ServletOutputStream} reference
     */
    public ActionAwareServletOutputStream getServletOutputStream() {
        return servletOutputStream;
    }

    /**
     * Removes the underlying {@link AJPv13ServletOutputStream} reference by setting it to <code>null</code>
     */
    public void removeServletOutputStream() {
        servletOutputStream = null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            throw new IOException("no ServletOutputStream found!");
        }
        /*
         * Check if getOutputSteam hasn't been called before
         */
        if (outputSelection == OUTPUT_WRITER) {
            throw new IllegalStateException("Servlet's Writer has already been selected as output");
        }
        if (outputSelection == OUTPUT_NOT_SELECTED) {
            outputSelection = OUTPUT_STREAM;
        }
        return servletOutputStream;
    }

    public int getOutputSelection() {
        return outputSelection;
    }

    /**
     * Gets the associated HTTP request.
     *
     * @return The associated HTTP request
     */
    public HttpServletRequestImpl getRequest() {
        return ajpProcessor.getRequest();
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
        if (committed) {
            throw new IllegalStateException("Servlet can not be resetted cause it has already been committed");
        }
        headers.clear();
        status = 0;
        if (writer == null) {
            servletOutputStream.resetBuffer();
        } else {
            // TODO:assa
        }
        cookies.clear();
    }

    @Override
    public String encodeURL(final String url) {
        final HttpServletRequestImpl request = ajpProcessor.getRequest();
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
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(path);
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
        cookies.add(wrapper(cookie));
    }

    /**
     * Removes specified cookie from cookie set
     *
     * @param cookie The cookie to remove
     */
    public void removeCookie(final Cookie cookie) {
        if (null == cookie) {
            return;
        }
        cookies.remove(wrapper(cookie));
    }

    private static final List<List<String>> EMPTY_COOKIES = Collections.emptyList();

    private static volatile Boolean filterByName;

    private static boolean filterByName() {
        Boolean tmp = filterByName;
        if (null == tmp) {
            synchronized (AjpProcessor.class) {
                tmp = filterByName;
                if (null == tmp) {
                    final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null != service && service.getBoolProperty("com.openexchange.cookie.filterByName", false));
                    filterByName = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * Generates a two dimensional array of {@link String} containing the <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers of this HTTP
     * response's cookies.
     * <p>
     * For each cookie its HTTP header format is generated and added to corresponding array of {@link String}
     *
     * @return A two dimensional array of {@link String} containing the <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers
     */
    public List<List<String>> getFormatedCookies() {
        final int cookiesSize = cookies.size();
        if (cookiesSize <= 0) {
            return EMPTY_COOKIES;
        }
        // Write cookies
        final Collection<CookieWrapper> iterateMe;
        if (filterByName()) {
            // Check for duplicate named cookies
            final Map<String, CookieWrapper> checkedCookies = new LinkedHashMap<String, CookieWrapper>(cookiesSize);
            for (final CookieWrapper wrapper : cookies) {
                /*final Cookie prev = */checkedCookies.put(wrapper.getCookie().getName(), wrapper);
                // Already existing; decide which one to keep or to merge
                // By now: Keep the newer one (cookies is a LinkedHashSet that keeps order)
                /*-
                 *
                if (null != prev) {
                    if (0 == prev.getMaxAge()) {
                        // First indeciates delete
                        if (0 == cookie.getMaxAge()) {
                            // Keep previous one
                            names.put(cookie.getName(), prev);
                        }
                    } else {
                        // First i

                    }
                }
                */
            }
            iterateMe = checkedCookies.values();
        } else {
            // Write to list
            iterateMe = cookies;
        }
        // Write to list
        final List<String> list = new ArrayList<String>(cookiesSize);
        final String userAgent = ajpProcessor.getRequest().getHeader("User-Agent");
        final StringBuilder strBuilder = new StringBuilder(32);
        for (final CookieWrapper wrapper : iterateMe) {
            list.add(getFormattedCookie(wrapper.getCookie(), userAgent, strBuilder, httpOnly));
        }
        final List<List<String>> retval = new ArrayList<List<String>>(1);
        retval.add(list);
        return retval;
    }

    private static volatile String[] cookieParams;

    private static String[] cookieParams() {
        String[] tmp = cookieParams;
        if (null == tmp) {
            synchronized (HttpServletResponseImpl.class) {
                tmp = cookieParams;
                if (null == tmp) {
                    final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        tmp = new String[] { "; expires=", "; version=", "; path=", "; domain=", "; secure" };
                    } else {
                        tmp = new String[5];
                        final StringBuilder sb = new StringBuilder(16).append("; ");
                        int pos = 0;

                        String name = service.getProperty("com.openexchange.cookie.expires.name", "expires");
                        tmp[pos++] = sb.append(name).append('=').toString();

                        name = service.getProperty("com.openexchange.cookie.version.name", "version");
                        sb.setLength(2);
                        tmp[pos++] = sb.append(name).append('=').toString();

                        name = service.getProperty("com.openexchange.cookie.path.name", "path");
                        sb.setLength(2);
                        tmp[pos++] = sb.append(name).append('=').toString();

                        name = service.getProperty("com.openexchange.cookie.domain.name", "domain");
                        sb.setLength(2);
                        tmp[pos++] = sb.append(name).append('=').toString();

                        name = service.getProperty("com.openexchange.cookie.secure.name", "secure");
                        sb.setLength(2);
                        tmp[pos++] = sb.append(name).toString();
                    }
                    cookieParams = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the HTTP header format for specified instance of {@link Cookie}
     *
     * @param cookie The cookie whose HTTP header format shall be returned
     * @param strBuilder A string builder instance
     * @return A string representing the HTTP header format
     */
    private static final String getFormattedCookie(final Cookie cookie, final String userAgent, final StringBuilder strBuilder, final boolean httpOnly) {
        strBuilder.setLength(0);
        strBuilder.append(cookie.getName()).append('=');
        strBuilder.append(cookie.getValue());
        final int maxAge = cookie.getMaxAge();
        if (maxAge >= 0) {
            HttpDateFormatRegistry.getInstance().appendCookieMaxAge(maxAge, userAgent, strBuilder);
        }
        final String[] cookieParams = cookieParams();
        if (cookie.getVersion() > 0) {
            strBuilder.append(cookieParams[1]).append(cookie.getVersion());
        }
        {
            final String path = cookie.getPath();
            if (!isEmpty(path)) {
                strBuilder.append(cookieParams[2]).append(path);
            }
        }
        {
            final String domain = cookie.getDomain();
            if (!isEmpty(domain)) {
                strBuilder.append(cookieParams[3]).append(domain);
            }
        }
        if (cookie.getSecure()) {
            strBuilder.append(cookieParams[4]);
        }
        /*-
         * TODO: HttpOnly currently cannot be set in Cookie class, thus we do it hard-coded here.
         *       This is available with Java Servlet Specification v3.0.
         *
         * Append HttpOnly flag
         */
        if (httpOnly /* && maxAge > 0 */) {
            strBuilder.append("; HttpOnly");
        }
        if (DEBUG) {
            LOG.debug("Cookie: " + strBuilder.toString());
        }
        return strBuilder.toString();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    @Override
    public void addHeader(final String name, final String value) {
        if (SINGLE_VALUE_HEADERS.contains(name)) {
            headers.put(name, Collections.singletonList(value));
        } else {
            /*
             * Header may carry multiple values
             */
            final List<String> prevValues = headers.get(name);
            if (null == prevValues) {
                headers.put(name, newLinkedList(value));
            } else {
                prevValues.add(value);
            }
        }
    }

    private static List<String> newLinkedList(final String initialValue) {
        final List<String> list = new LinkedList<String>();
        list.add(initialValue);
        return list;
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
        headers.put(name, newLinkedList(value));
    }

    public final int getNumOfHeaders() {
        return headers.size();
    }

    public final Iterator<String> getHeaderNames() {
        return headers.keySet().iterator();
    }

    public final Set<Map.Entry<String, List<String>>> getHeaderEntrySet() {
        return headers.entrySet();
    }

    public Enumeration<?> getHeaders(final String name) {
        return makeEnumeration(headers.get(name));
    }

    public final String getHeader(final String name) {
        if (!containsHeader(name)) {
            return null;
        }
        final StringBuilder retval = new StringBuilder(128);
        final List<String> list = headers.get(name);
        retval.append(list.get(0));
        final int len = list.size();
        for (int i = 1; i < len; i++) {
            retval.append(',').append(list.get(i));
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
            final HttpServletRequestImpl request = ajpProcessor.getRequest();
            desc = String.format(desc, request.getServletPath());
        }
        String errorMsgStr = ERROR_PAGE_TEMPL;
        errorMsgStr = errorMsgStr.replaceAll("#STATUS_CODE#", String.valueOf(this.status)).replaceAll(
            "#STATUS_MSG#",
            com.openexchange.java.Strings.quoteReplacement(this.statusMsg)).replaceFirst("#STATUS_DESC#", com.openexchange.java.Strings.quoteReplacement(desc));
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
            final HttpServletRequestImpl request = ajpProcessor.getRequest();
            desc = String.format(desc, request.getServletPath());
        }
        String errorMsgStr = ERROR_PAGE_TEMPL;
        errorMsgStr =
            errorMsgStr.replaceAll("#STATUS_CODE#", Integer.toString(this.status)).replaceAll("#STATUS_MSG#", this.statusMsg).replaceFirst(
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

    private Enumeration<?> makeEnumeration(final List<String> list) {
        return (new Enumeration<String>() {

            private final Iterator<String> iter = list.iterator();

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public String nextElement() {
                return iter.next();
            }
        });
    }

}
