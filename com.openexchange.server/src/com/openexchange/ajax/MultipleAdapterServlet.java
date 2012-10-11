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

package com.openexchange.ajax;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.exception.OXException;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MultipleAdapterServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class MultipleAdapterServlet extends PermissionServlet {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MultipleAdapterServlet.class));


    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }


    protected void handle(final HttpServletRequest req, final HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(resp);

        if (handleOverride(req, resp)) {
            return;
        }
        final ServerSession session = getSessionObject(req);
        try {
            final String action = req.getParameter(PARAMETER_ACTION);
            final JSONObject request = toJSON(req, action);
            final MultipleHandler handler = createMultipleHandler();

            if (action == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( PARAMETER_ACTION);
            }
            final Object response = handler.performRequest(action, request, session, Tools.considerSecure(req));
            final Date timestamp = handler.getTimestamp();
            writeResponseSafely(response, session.getUser().getLocale(), timestamp, handler.getWarnings(), resp);
        } catch (final OXException x) {
            writeException(x, session.getUser().getLocale(), resp);
        } catch (final Throwable t) {
            writeException(wrap(t), session.getUser().getLocale(), resp);
        }
    }

    private OXException wrap(final Throwable t) {
        return AjaxExceptionCodes.UNEXPECTED_ERROR.create(t, t.getMessage());
    }

    protected boolean handleOverride(final HttpServletRequest req, final HttpServletResponse resp) {
        return false;
    }

    private void writeResponseSafely(final Object data, final Locale locale, final Date timestamp, final Collection<OXException> warnings, final HttpServletResponse resp) {
        final Response response = new Response(locale);
        response.setData(data);
        if(null != timestamp) {
            response.setTimestamp(timestamp);
        }
        if (null != warnings && !warnings.isEmpty()) {
            response.addWarnings(warnings);
        }
        try {
            writeResponse(response, resp);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void writeException(final OXException x, final Locale locale, final HttpServletResponse resp) {
        x.log(LOG);
        final Response response = new Response(locale);
        response.setException(x);
        try {
            writeResponse(response, resp);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private JSONObject toJSON(final HttpServletRequest req, final String action) throws JSONException, IOException {
        final JSONObject request = new JSONObject();
        final Enumeration parameterNames = req.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            final String parameterName = (String) parameterNames.nextElement();
            final String parameter = req.getParameter(parameterName);
            request.put(parameterName, parameter);
        }
        if(requiresBody(action)) {
            final String body = getBody(req);
            if(body != null && ! body.equals("")) {
                final Object value = toJSONConformantValue(body);
                request.put(ResponseFields.DATA, value);
            }
        }
        return modify(req, action, request);
    }

    protected JSONObject modify(final HttpServletRequest req, final String action, final JSONObject request) throws JSONException {
        return request;
    }

    private Object toJSONConformantValue(final String body) throws JSONException {
        return new JSONObject("{ body : "+ body+" }").get("body");
    }

    protected abstract boolean requiresBody(String action);
    protected abstract MultipleHandler createMultipleHandler();

}
