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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.osgi.BodyParserRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXRequestDataTools} - Tools for parsing AJAX requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestDataTools {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AJAXRequestDataTools.class);

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

    private static final AtomicReference<BodyParserRegistry> REGISTRY = new AtomicReference<BodyParserRegistry>();

    /**
     * Sets the registry.
     *
     * @param registry The registry
     */
    public static void setBodyParserRegistry(final BodyParserRegistry registry) {
        REGISTRY.set(registry);
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
        if (null != prefix) {
            retval.setModule(getModule(prefix, req));
        }
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
                retval.setHeader("If-None-Match", eTag);
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
             * Guess an appropriate body object
             */
            final BodyParserRegistry registry = REGISTRY.get();
            if (null == registry) {
                DefaultBodyParser.getInstance().setBody(retval, req);
            } else {
                final BodyParser bodyParser = registry.getParserFor(retval);
                if (null == bodyParser) {
                    DefaultBodyParser.getInstance().setBody(retval, req);
                } else {
                    bodyParser.setBody(retval, req);
                }
            }
        }
        return retval;
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

    /**
     * Parses denoted <tt>boolean</tt> value from specified request data.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param name The parameter's name
     * @param requestData The request data to parse from
     * @param defaultValue The default value to return if parameter is absent
     * @return The parsed <tt>boolean</tt> value (<code>defaultValue</code> on absence)
     */
    public static boolean parseBoolParameter(final String name, final AJAXRequestData requestData, final boolean defaultValue) {
        final String value = requestData.getParameter(name);
        if (null == value) {
            return defaultValue;
        }
        return TRUE_VALS.contains(com.openexchange.java.Strings.toLowerCase(value.trim()));
    }

    private static final Set<String> TRUE_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("true", "1", "yes", "y", "on")));

    /**
     * Parses denoted <tt>boolean</tt> value from specified <tt>String</tt> parameter value.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param parameter The parameter value
     * @return The parsed <tt>boolean</tt> value (<code>false</code> on absence)
     */
    public static boolean parseBoolParameter(final String parameter) {
        return (null != parameter) && TRUE_VALS.contains(com.openexchange.java.Strings.toLowerCase(parameter.trim()));
    }

    private static final Set<String> FALSE_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("false", "0", "no", "n", "off")));

    /**
     * Parses denoted <tt>boolean</tt> value from specified <tt>String</tt> parameter value.
     * <p>
     * <code>Boolean.FALSE</code> if given value is not <code>null</code> and equals ignore-case to one of the values "false", "no", "n",
     * "off", or "0".
     *
     * @param parameter The parameter value
     * @return The parsed <tt>Boolean.FALSE</tt> value (<code>null</code> on absence or mismatch)
     */
    public static Boolean parseFalseBoolParameter(final String parameter) {
        if (null == parameter) {
            return null;
        }
        return FALSE_VALS.contains(com.openexchange.java.Strings.toLowerCase(parameter.trim())) ? Boolean.FALSE : null;
    }

    /**
     * Parses denoted <tt>int</tt> value from specified <tt>String</tt> parameter.
     *
     * @param parameter The parameter value
     * @param defaultInt The default <tt>int</tt> to return if absent or unparseable
     * @return The parsed <tt>int</tt> value or <tt>defaultInt</tt>
     */
    public static int parseIntParameter(final String parameter, final int defaultInt) {
        if (null == parameter) {
            return defaultInt;
        }
        try {
            return Integer.parseInt(parameter);
        } catch (final NumberFormatException e) {
            return defaultInt;
        }
    }

    /**
     * Parses the "from"/"to" indexes from specified {@code AJAXRequestData} instance
     *
     * @param requestData The request data
     * @return The parsed "from"/"to" indexes or <code>null</code>
     * @throws OXException If parsing fails; e.g. NaN
     */
    public static int[] parseFromToIndexes(AJAXRequestData requestData) throws OXException {
        String sLimit = requestData.getParameter(AJAXServlet.PARAMETER_LIMIT);
        if (null == sLimit) {
            int from = parseIntParameter(requestData.getParameter(AJAXServlet.LEFT_HAND_LIMIT), -1);
            if (from < 0) {
                return null;
            }
            int to = parseIntParameter(requestData.getParameter(AJAXServlet.RIGHT_HAND_LIMIT), -1);
            if (to < 0) {
                return null;
            }

            return new int[] { from, to };
        }

        int start;
        int end;
        try {
            int pos = sLimit.indexOf(',');
            if (pos < 0) {
                start = 0;
                int i = Integer.parseInt(sLimit.trim());
                end = i < 0 ? 0 : i;
            } else {
                int i = Integer.parseInt(sLimit.substring(0, pos).trim());
                start = i < 0 ? 0 : i;
                i = Integer.parseInt(sLimit.substring(pos+1).trim());
                end = i < 0 ? 0 : i;
            }
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "limit", sLimit);
        }
        return new int[] {start, end};
    }

    /**
     * Parses host name, secure and route.
     *
     * @param request The AJAX request data
     * @param req The HTTP Servlet request
     * @param session The associated session
     */
    public static void parseHostName(AJAXRequestData request, HttpServletRequest req, ServerSession session) {
        if (null == session) {
            parseHostName(request, req, -1, -1, false);
        } else {
            parseHostName(request, req, session.getUserId(), session.getContextId(), session.getUser().isGuest());
        }
    }

    /**
     * Parses host name, secure and route.
     *
     * @param request The AJAX request data
     * @param req The HTTP Servlet request
     * @param userId The user identifier
     * @param isGuest <code>true</code> if the guest hostname should be preferred, <code>false</code>, otherwise
     * @param contextId The context identifier
     */
    private static void parseHostName(AJAXRequestData request, HttpServletRequest req, int userId, int contextId, boolean isGuest) {
        request.setSecure(Tools.considerSecure(req));
        {
            final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
            if (null == hostnameService) {
                request.setHostname(req.getServerName());
            } else {
                final String hn;
                if (isGuest) {
                    hn = hostnameService.getGuestHostname(userId, contextId);
                } else {
                    hn = hostnameService.getHostname(userId, contextId);
                }
                request.setHostname(null == hn ? req.getServerName() : hn);
            }
        }
        request.setRemoteAddress(req.getRemoteAddr());
        request.setRoute(Tools.getRoute(req.getSession(true).getId()));
    }

    /**
     * Gets the module from specified HTTP request.
     *
     * @param prefix The dispatcher's default prefix to strip from request's {@link HttpServletRequest#getPathInfo() path info}.
     * @param req The HTTP request
     * @return The determined module
     */
    public String getModule(final String prefix, final HttpServletRequest req) {
        String module = (String) req.getAttribute("__module");
        if (null != module) {
            return module;
        }

        String pathInfo = req.getRequestURI();
        try {
            pathInfo = URLDecoder.decode(pathInfo, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            LOG.error(e.getMessage(), e);
        }
        final int lastIndex = pathInfo.lastIndexOf(';');
        if (lastIndex > 0) {
            pathInfo = pathInfo.substring(0, lastIndex);
        }

        module = null != prefix ? pathInfo.substring(prefix.length()) : pathInfo;
        final int mlen = module.length() - 1;
        if ('/' == module.charAt(mlen)) {
            module = module.substring(0, mlen);
        }
        req.setAttribute("__module", module);
        return module;
    }

    /**
     * Gets the action from specified HTTP request.
     *
     * @param req The HTTP request
     * @return The determined action
     */
    public String getAction(final HttpServletRequest req) {
        String action = req.getParameter(PARAMETER_ACTION);
        return null == action ? Strings.toUpperCase(req.getMethod()) : action;
    }

    /**
     * Gets the value for the <code>"User-Agent"</code> header from passed AJAX request data<br>
     * (while preferring the one from {@link HttpServletRequest}).
     *
     * @param request The AJAX request data
     * @return The value for the <code>"User-Agent"</code> header or <code>null</code>
     */
    public static String getUserAgent(AJAXRequestData request) {
        if (null == request) {
            return null;
        }

        String userAgent = null;

        HttpServletRequest servletRequest = request.optHttpServletRequest();
        if (null != servletRequest) {
            userAgent = servletRequest.getHeader("User-Agent");
        }

        if (null == userAgent) {
            userAgent = request.getUserAgent();
        }

        return userAgent;
    }

}
