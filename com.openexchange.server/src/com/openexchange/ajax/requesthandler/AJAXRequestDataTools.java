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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedPushbackReader;
import com.openexchange.java.UnsynchronizedStringReader;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXRequestDataTools} - Tools for parsing AJAX requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestDataTools {

    private static final String PARAMETER_ACTION = AJAXServlet.PARAMETER_ACTION;

    private static final AJAXRequestDataTools INSTANCE = new AJAXRequestDataTools();

    /**
     * Gets the default instance
     *
     * @return The default instance
     */
    public static AJAXRequestDataTools getInstance() {
        return INSTANCE;
    }

    /*-
     * ----------------------- Member stuff -----------------------
     */

    /**
     * Initializes a new {@link AJAXRequestDataTools}.
     */
    protected AJAXRequestDataTools() {
        super();
    }

    /**
     * The pattern to split by commas.
     */
    protected static final Pattern SPLIT_CSV = Pattern.compile("\\s*,\\s*");

    /**
     * Parses an appropriate {@link AJAXRequestData} instance from specified arguments.
     *
     * @param req The HTTP Servlet request
     * @param preferStream Whether to prefer request's stream instead of parsing its body data to an appropriate (JSON) object
     * @param isFileUpload Whether passed request is considered as a file upload
     * @param session The associated session
     * @param prefix The part of request's URI considered as prefix; &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt;
     * @return An appropriate {@link AJAXRequestData} instance
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public final AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session, final String prefix) throws IOException, OXException {
        return parseRequest(req, preferStream, isFileUpload, session, prefix, null);
    }

    /**
     * Parses an appropriate {@link AJAXRequestData} instance from specified arguments.
     *
     * @param req The HTTP Servlet request
     * @param preferStream Whether to prefer request's stream instead of parsing its body data to an appropriate (JSON) object
     * @param isFileUpload Whether passed request is considered as a file upload
     * @param session The associated session
     * @param prefix The part of request's URI considered as prefix; &lt;prefix&gt; + <code>'/'</code> + &lt;module&gt;
     * @param optResp The optional HTTP Servlet response
     * @return An appropriate {@link AJAXRequestData} instance
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session, final String prefix, final HttpServletResponse optResp) throws IOException, OXException {
        final AJAXRequestData retval = new AJAXRequestData().setHttpServletResponse(optResp);
        retval.setUserAgent(req.getHeader("user-agent"));
        parseHostName(retval, req, session);
        retval.setMultipart(isFileUpload);
        /*
         * Set HTTP Servlet request instance
         */
        retval.setHttpServletRequest(req);
        /*
         * Set the module
         */
        retval.setModule(getModule(prefix, req));
        /*
         * Set request URI
         */
        retval.setServletRequestURI(AJAXServlet.getServletSpecificURI(req));
        retval.setPathInfo(req.getPathInfo());
        retval.setAction(getAction(req));
        retval.setPrefix(prefix);
        /*
         * Set the format
         */
        retval.setFormat(req.getParameter("format"));
        /*
         * Pass all parameters to AJAX request object
         */
        {
            @SuppressWarnings("unchecked") final Set<Entry<String, String[]>> entrySet = req.getParameterMap().entrySet();
            for (final Entry<String, String[]> entry : entrySet) {
                retval.putParameter(entry.getKey(), entry.getValue()[0]);
            }
        }
        /*
         * Check for ETag header to support client caching
         */
        {
            final String eTag = req.getHeader("If-None-Match");
            if (null != eTag) {
                retval.setETag(eTag);
            }
        }
        /*
         * Check for decorators
         */
        {
            final String parameter = req.getParameter("decorators");
            if (null != parameter) {
                for (final String id : SPLIT_CSV.split(parameter, 0)) {
                    retval.addDecoratorId(id.trim());
                }
            }
        }
        /*
         * Set request body
         */
        if (preferStream || parseBoolParameter("binary", req)) {
            /*
             * Pass request's stream
             */
            retval.setUploadStreamProvider(new HTTPRequestInputStreamProvider(req));
        } else {
            /*
             * Guess an appropriate body object (if the request indicates a body object)
             */
            if (hasBody(req)) {
                UnsynchronizedPushbackReader reader = null;
                try {
                    reader = new UnsynchronizedPushbackReader(AJAXServlet.getReaderFor(req));
                    int read = reader.read();
                    if (read < 0) {
                        trySetDataByParameter(req, retval);
                    } else {
                        // Skip whitespaces
                        while (isWhitespace((char) read)) {
                            read = reader.read();
                            if (read < 0) {
                                trySetDataByParameter(req, retval);
                                Streams.close(reader);
                                reader = null;
                                return retval;
                            }
                        }
                        // Check first non-whitespace character
                        final char c = (char) read;
                        reader.unread(c);
                        if ('[' == c || '{' == c) {
                            try {
                                retval.setData(JSONObject.parse(reader));
                            } catch (JSONException e) {
                                retval.setData(AJAXServlet.readFrom(reader));
                            }
                        } else {
                            retval.setData(AJAXServlet.readFrom(reader));
                        }
                    }
                } finally {
                    Streams.close(reader);
                }
            } else {
                trySetDataByParameter(req, retval);
            }
        }
        return retval;
    }

    private void trySetDataByParameter(final HttpServletRequest req, final AJAXRequestData retval) {
        retval.setData(null);
        final String data = req.getParameter("data");
        if (data != null && data.length() > 0) {
            try {
                final char c = data.charAt(0);
                if ('[' == c || '{' == c) {
                    retval.setData(JSONObject.parse(new UnsynchronizedStringReader(data)));
                } else {
                    retval.setData(data);
                }
            } catch (final JSONException e) {
                retval.setData(data);
            }
        }
    }

    /**
     * High speed test for whitespace!  Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    private boolean isWhitespace(char c) {
        switch (c) {
            case 9:  //'unicode: 0009
            case 10: //'unicode: 000A'
            case 11: //'unicode: 000B'
            case 12: //'unicode: 000C'
            case 13: //'unicode: 000D'
            case 28: //'unicode: 001C'
            case 29: //'unicode: 001D'
            case 30: //'unicode: 001E'
            case 31: //'unicode: 001F'
            case ' ': // Space
                //case Character.SPACE_SEPARATOR:
                //case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
        }
        return false;
    }

    /**
     * Check if the incoming request has a body. From RFC 2616: An entity-body is only present when a message-body is present (section 7.2),
     * the presence of a message-body is signaled by the inclusion of a Content-Length or Transfer-Encoding header (section 4.3)
     *
     * @param httpServletRequest The incoming request
     * @return <code>true</code> if the incoming request includes a body, <code>false</code> otherwise
     */
    public static boolean hasBody(final HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getContentLength() > 0) || (httpServletRequest.getHeader("Transfer-Encoding") != null);
    }

    private static boolean parseBoolParameter(final String name, final HttpServletRequest req) {
        return parseBoolParameter(req.getParameter(name));
    }

    /**
     * Parses denoted <tt>boolean</tt> value from specified request data.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param name The parameter's name
     * @param requestData The request data to parse from
     * @return The parsed <tt>boolean</tt> value (<code>false</code> on absence)
     */
    public static boolean parseBoolParameter(final String name, final AJAXRequestData requestData) {
        return parseBoolParameter(requestData.getParameter(name));
    }

    private static final Set<String> BOOL_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "true",
        "1",
        "yes",
        "y",
        "on")));

    /**
     * Parses denoted <tt>boolean</tt> value from specified <tt>String</tt> parameter.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param parameter The parameter
     * @return The parsed <tt>boolean</tt> value (<code>false</code> on absence)
     */
    public static boolean parseBoolParameter(final String parameter) {
        return (null != parameter) && BOOL_VALS.contains(toLowerCase(parameter.trim()));
    }

    /**
     * Parses host name, secure and AJP route.
     *
     * @param request The AJAX request data
     * @param req The HTTP Servlet request
     * @param session The associated session
     */
    public static void parseHostName(final AJAXRequestData request, final HttpServletRequest req, final ServerSession session) {
        request.setSecure(Tools.considerSecure(req));
        {
            final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
            if (null == hostnameService) {
                request.setHostname(req.getServerName());
            } else {
                final String hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                request.setHostname(null == hn ? req.getServerName() : hn);
            }
        }
        request.setRemoteAddress(req.getRemoteAddr());
        request.setRoute(Tools.getRoute(req.getSession(true).getId()));
    }

    private static boolean startsWith(final char startingChar, final String toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        int i = 0;
        if (Character.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Character.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    /**
     * Gets the module from specified HTTP request.
     *
     * @param prefix The dispatcher's default prefix to strip from request's {@link HttpServletRequest#getPathInfo() path info}.
     * @param req The HTTP request
     * @return The determined module
     */
    public String getModule(final String prefix, final HttpServletRequest req) {
        String pathInfo = req.getRequestURI();
        final int lastIndex = pathInfo.lastIndexOf(';');
        if (lastIndex > 0) {
            pathInfo = pathInfo.substring(0, lastIndex);
        }
        String module = pathInfo.substring(prefix.length());
        final int mlen = module.length()-1;
        if ('/' == module.charAt(mlen)) {
            module = module.substring(0, mlen);
        }
        return module;
    }

    /**
     * Gets the action from specified HTTP request.
     *
     * @param req The HTTP request
     * @return The determined action
     */
    public String getAction(final HttpServletRequest req) {
        final String action = req.getParameter(PARAMETER_ACTION);
        if (null == action) {
            return toUpperCase(req.getMethod());
        }
        return action;

    }

    /** ASCII-wise upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

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
