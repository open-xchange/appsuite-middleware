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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.chat.json;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link RestServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RestServlet extends DispatcherServlet {

    private static enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        
        ;

        private static final Map<String, Method> MAP;

        static {
            final Method[] values = Method.values();
            final Map<String, Method> m = new HashMap<String, Method>(values.length);
            for (final Method method : values) {
                m.put(method.name(), method);
            }
            MAP = Collections.unmodifiableMap(m);
        }

        /**
         * Gets the method appropriate for specified HTTP Servlet request.
         * 
         * @param req The HTTP Servlet request
         * @return The appropriate method or <code>null</code>
         */
        public static Method valueOf(final HttpServletRequest req) {
            return MAP.get(req.getMethod().toUpperCase(Locale.US));
        }
    }

    private static interface MethodHandler {
        
        /**
         * Parses REST-like HTTP Servlet request to an appropriate {@link AJAXRequestData} instance.
         * 
         * @param req The HTTP Servlet request
         * @param session The session
         * @param servlet The dispatcher Servlet
         * @return An appropriate {@link AJAXRequestData} instance
         * @throws IOException If an I/O error occurs
         * @throws OXException If an OX error occurs
         */
        AJAXRequestData parseRequest(HttpServletRequest req, ServerSession session, RestServlet servlet) throws IOException, OXException;

    }

    private static final MethodHandler GET_METHOD_HANDLER = new MethodHandler() {

        @Override
        public AJAXRequestData parseRequest(final HttpServletRequest req, final ServerSession session, final RestServlet servlet) throws IOException, OXException {
            final AJAXRequestData retval = new AJAXRequestData();
            servlet.parseHostName(retval, req, session);
            /*
             * Set the module
             */
            retval.setModule("conversation");
            /*
             * Set request URI
             */
            retval.setServletRequestURI(AJAXServlet.getServletSpecificURI(req));
            /*
             * Determine action by path information (extra path information follows the Servlet path but precedes the query string and
             * will start with a "/" character)
             */
            final String pathInfo = req.getPathInfo();
            if (null == pathInfo) {
                retval.setAction("all");
            } else {
                final String[] pathElements = SPLIT_PATH.split(pathInfo);
                final int length = pathElements.length;
                if (0 == length) {
                    /*-
                     * "Get all conversations"
                     *  GET /conversation
                     */
                    retval.setAction("all");
                } else if (1 == length) {
                    /*-
                     * "Get specific conversation"
                     *  GET /conversation/11
                     */
                    retval.setAction("get");
                    retval.putParameter("id", pathElements[0]);
                } else if ("message".equals(pathElements[1])) {
                    if (2 == length) {
                        /*-
                         * "Get all messages"
                         *  GET /conversation/11/message?since=<long:timestamp>
                         */
                        retval.setAction("allMessages");
                        retval.putParameter("id", pathElements[0]);
                    } else {
                        /*-
                         * "Get specific message"
                         *  GET /conversation/11/message/1234
                         */
                        retval.setAction("getMessages");
                        retval.putParameter("id", pathElements[0]);
                        retval.putParameter("messageId", pathElements[2]);
                    }
                } else {
                    throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
                }
            }
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
             * No need to parse body for GET method.
             */
            return retval;
        }
    };

    protected static final Pattern SPLIT_PATH = Pattern.compile(Pattern.quote("/"));

    private static final Map<Method, MethodHandler> HANDLER_MAP;

    static {
        final EnumMap<Method, MethodHandler> m = new EnumMap<RestServlet.Method, RestServlet.MethodHandler>(Method.class);
        m.put(Method.GET, GET_METHOD_HANDLER);
        
        HANDLER_MAP = Collections.unmodifiableMap(m);
    }

    /**
     * Initializes a new {@link RestServlet}.
     */
    public RestServlet() {
        super();
    }

    @Override
    protected AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session) throws IOException, OXException {
        if (isFileUpload) {
            return super.parseRequest(req, preferStream, isFileUpload, session);
        }
        /*
         * Parse dependent on HTTP method and/or servlet path
         */
        final Method method = Method.valueOf(req);
        if (null == method) {
            return super.parseRequest(req, preferStream, isFileUpload, session);
        }
        final MethodHandler methodHandler = HANDLER_MAP.get(method);
        if (null == methodHandler) {
            return super.parseRequest(req, preferStream, isFileUpload, session);
        }
        return methodHandler.parseRequest(req, session, this);
    }

}
