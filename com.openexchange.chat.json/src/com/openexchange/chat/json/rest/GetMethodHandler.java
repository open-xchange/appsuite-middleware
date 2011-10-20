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

package com.openexchange.chat.json.rest;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chat.json.rest.RestServlet.MethodHandler;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MethodHandlerImplementation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetMethodHandler implements MethodHandler {

    /**
     * Initializes a new {@link GetMethodHandler}.
     */
    public GetMethodHandler() {
        super();
    }

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
            final String[] pathElements = RestServlet.SPLIT_PATH.split(pathInfo);
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
                final String element = pathElements[0];
                if (element.indexOf(',') < 0) {
                    retval.setAction("get");
                    retval.putParameter("id", element);
                } else {
                    retval.setAction("list");
                    final JSONArray array = new JSONArray();
                    for (final String id : RestServlet.SPLIT_CSV.split(element)) {
                        array.put(id);
                    }
                    retval.setData(array);
                }
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
                    retval.putParameter("id", pathElements[0]);
                    final String element = pathElements[2];
                    if (element.indexOf(',') < 0) {
                        retval.setAction("getMessage");
                        retval.putParameter("messageId", element);
                    } else {
                        retval.setAction("listMessages");
                        final JSONArray array = new JSONArray();
                        for (final String id : RestServlet.SPLIT_CSV.split(element)) {
                            array.put(id);
                        }
                        retval.setData(array);
                    }
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
                final String name = entry.getKey();
                if (!retval.containsParameter(name)) {
                    retval.putParameter(name, entry.getValue()[0]);
                }
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
}