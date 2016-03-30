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

package com.openexchange.jslob.json.rest;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedPushbackReader;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMethodHandler} - The abstract method handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMethodHandler implements MethodHandler {

    /**
     * Initializes a new {@link AbstractMethodHandler}.
     */
    protected AbstractMethodHandler() {
        super();
    }

    /**
     * Splits a char sequence by comma-separated (<code>','</code>) values.
     */
    protected static final Pattern SPLIT_CSV = Pattern.compile(" *, *");

    /**
     * Splits a char sequence by slash-separated (<code>'/'</code>) values.
     */
    protected static final Pattern SPLIT_PATH = Pattern.compile(Pattern.quote("/"));

    @Override
    public AJAXRequestData parseRequest(final HttpServletRequest req, final ServerSession session, final DispatcherServlet servlet) throws IOException, OXException {
        final AJAXRequestData requestData = new AJAXRequestData();
        AJAXRequestDataTools.parseHostName(requestData, req, session);
        /*
         * Set the module
         */
        requestData.setModule(getModule());
        /*
         * Set request URI
         */
        requestData.setServletRequestURI(AJAXServlet.getServletSpecificURI(req));
        requestData.setPathInfo(req.getPathInfo());
        /*
         * Determine action by path information (extra path information follows the Servlet path but precedes the query string and will
         * start with a "/" character)
         */
        requestData.setServletRequestURI("");
        parseByPathInfo(requestData, req.getPathInfo(), req);
        /*
         * Set the format
         */
        requestData.setFormat(req.getParameter("format"));
        /*
         * Pass all parameters to AJAX request object
         */
        {
            @SuppressWarnings("unchecked") final Set<Entry<String, String[]>> entrySet = req.getParameterMap().entrySet();
            for (final Entry<String, String[]> entry : entrySet) {
                final String name = entry.getKey();
                if (!requestData.containsParameter(name)) {
                    requestData.putParameter(name, entry.getValue()[0]);
                }
            }
            requestData.examineServletRequest(req);
        }
        /*
         * Check for ETag header to support client caching
         */
        {
            final String eTag = req.getHeader("If-None-Match");
            if (null != eTag) {
                requestData.setETag(eTag);
                requestData.setHeader("If-None-Match", eTag);
            }
        }
        /*
         * Body data
         */
        if (shouldApplyBody()) {
            applyBodyObject(requestData, req);
        }
        /*
         * Return parsed AJAX request data
         */
        return requestData;
    }

    /**
     * Gets the module identifier.
     *
     * @return The module identifier
     */
    protected abstract String getModule();

    /**
     * Parses by path info (extra path information follows the Servlet path but precedes the query string and will start with a "/"
     * character)
     *
     * @param requestData The AJAX request data
     * @param pathInfo The path info
     * @param req The HTTP request
     */
    protected abstract void parseByPathInfo(AJAXRequestData requestData, String pathInfo, HttpServletRequest req) throws IOException, OXException;

    /**
     * Whether to apply body data.
     *
     * @return <code>true</code> to apply body data; else <code>false</code>
     */
    protected abstract boolean shouldApplyBody();

    private void applyBodyObject(final AJAXRequestData request, final HttpServletRequest req) throws IOException {
        /*
         * Guess an appropriate body object
         */
        UnsynchronizedPushbackReader reader = null;
        try {
            reader = new UnsynchronizedPushbackReader(AJAXServlet.getReaderFor(req));
            final int read = reader.read();
            if (read < 0) {
                request.setData(null);
            } else {
                final char c = (char) read;
                reader.unread(c);
                if ('[' == c || '{' == c) {
                    try {
                        request.setData(JSONObject.parse(reader));
                    } catch (final JSONException e) {
                        request.setData(AJAXServlet.readFrom(reader));
                    }
                } else {
                    request.setData(AJAXServlet.readFrom(reader));
                }
            }
        } finally {
            Streams.close(reader);
        }
    }
}
