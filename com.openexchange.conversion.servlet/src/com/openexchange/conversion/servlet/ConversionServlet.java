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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.conversion.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link ConversionServlet} - The conversion servlet
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionServlet extends SessionServlet {

    private static final long serialVersionUID = 2192713156202101696L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ConversionServlet.class);

    private static final String ACTION_CONVERT = "convert";

    private static final String JSON_ARGS = "args";

    private static final String JSON_IDENTIFIER = "identifier";

    private static final String JSON_DATAHANDLER = "datahandler";

    private static final String JSON_DATASOURCE = "datasource";

    /**
     * Initializes a new {@link ConversionServlet}
     */
    public ConversionServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            throw new ConversionServletException(ConversionServletException.Code.UNSUPPORTED_METHOD, "GET");
        } catch (final AbstractOXException e) {
            LOG.error("doGet", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            final Response response = doAction(req);
            ResponseWriter.write(response, resp.getWriter());
        } catch (final AbstractOXException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(new ConversionServletException(ConversionServletException.Code.JSON_ERROR, e, e.getMessage()));
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            final Response response = doAction(req);
            ResponseWriter.write(response, resp.getWriter());
        } catch (final AbstractOXException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(new ConversionServletException(ConversionServletException.Code.JSON_ERROR, e, e.getMessage()));
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        }
    }

    private Response doAction(final HttpServletRequest req) throws ConversionServletException, JSONException, IOException, DataException {
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        if (actionStr.equalsIgnoreCase(ACTION_CONVERT)) {
            return actionConvert(new JSONObject(getBody(req)), getSessionObject(req));
        }
        throw new ConversionServletException(ConversionServletException.Code.UNSUPPORTED_PARAM, PARAMETER_ACTION, actionStr);
    }

    private Response actionConvert(final JSONObject jsonBody, final Session session) throws ConversionServletException, JSONException, DataException {
        /*
         * Check for data source in JSON body
         */
        if (!jsonBody.has(JSON_DATASOURCE) || jsonBody.isNull(JSON_DATASOURCE)) {
            throw new ConversionServletException(ConversionServletException.Code.MISSING_PARAM, JSON_DATASOURCE);
        }
        final JSONObject jsonDataSource = jsonBody.getJSONObject(JSON_DATASOURCE);
        checkDataSourceOrHandler(jsonDataSource);
        /*
         * Check for data handler in JSON body
         */
        if (!jsonBody.has(JSON_DATAHANDLER) || jsonBody.isNull(JSON_DATAHANDLER)) {
            throw new ConversionServletException(ConversionServletException.Code.MISSING_PARAM, JSON_DATAHANDLER);
        }
        final JSONObject jsonDataHandler = jsonBody.getJSONObject(JSON_DATAHANDLER);
        checkDataSourceOrHandler(jsonDataHandler);
        /*
         * Convert with conversion service
         */
        final ConversionService conversionService;
        try {
            conversionService = ConversionServletServiceRegistry.getServiceRegistry().getService(ConversionService.class, true);
        } catch (final ServiceException e) {
            throw new ConversionServletException(e);
        }
        final Object result = conversionService.convert(
            jsonDataSource.getString(JSON_IDENTIFIER),
            parseDataSourceOrHandlerArguments(jsonDataSource),
            jsonDataHandler.getString(JSON_IDENTIFIER),
            parseDataSourceOrHandlerArguments(jsonDataHandler),
            session);
        /*
         * Compose response
         */
        final Response response = new Response();
        response.setTimestamp(null);
        response.setData(result);
        return response;
    }

    private static String checkStringParam(final HttpServletRequest req, final String paramName) throws ConversionServletException {
        final String paramVal = req.getParameter(paramName);
        if ((paramVal == null) || (paramVal.length() == 0) || "null".equals(paramVal)) {
            throw new ConversionServletException(ConversionServletException.Code.MISSING_PARAM, paramName);
        }
        return paramVal;
    }

    private static void checkDataSourceOrHandler(final JSONObject json) throws ConversionServletException {
        if (!json.has(JSON_IDENTIFIER) || json.isNull(JSON_IDENTIFIER)) {
            throw new ConversionServletException(ConversionServletException.Code.MISSING_PARAM, JSON_IDENTIFIER);
        }
    }

    private static DataArguments parseDataSourceOrHandlerArguments(final JSONObject json) throws JSONException {
        if (!json.has(JSON_ARGS) || json.isNull(JSON_ARGS)) {
            return DataArguments.EMPTY_ARGS;
        }
        final JSONArray jsonArray = json.getJSONArray(JSON_ARGS);
        final int len = jsonArray.length();
        final DataArguments dataArguments = new DataArguments(len);
        for (int i = 0; i < len; i++) {
            final JSONObject elem = jsonArray.getJSONObject(i);
            if (elem.length() == 1) {
                final String key = elem.keys().next();
                dataArguments.put(key, elem.getString(key));
            } else {
                LOG.warn("Corrupt data argument in JSON object: " + elem.toString());
            }
        }
        return dataArguments;
    }

}
