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

package com.openexchange.passwordchange.servlet;

import static com.openexchange.passwordchange.servlet.services.PasswordChangeServletServiceRegistry.getServiceRegistry;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PasswordChangeServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordChangeServlet extends SessionServlet {

    private static final long serialVersionUID = 3129607149739575803L;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PasswordChangeServlet.class));

    private static final String PARAM_OLD_PASSWORD = "old_password";

    private static final String PARAM_NEW_PASSWORD = "new_password";

    /**
     * Initializes a new {@link PasswordChangeServlet}
     */
    public PasswordChangeServlet() {
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
            actionGet(req, resp);
        } catch (final OXException e) {
            LOG.error("PasswordChangeServlet.doGet()", e);
            final ServerSession session = getSessionObject(req);
            final Response response = new Response(session);
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
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
            actionPut(req, resp);
        } catch (final OXException e) {
            LOG.error("PasswordChangeServlet.doPut()", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(req)));
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("PasswordChangeServlet.doPut()", e);
            final Response response = new Response();
            response.setException(
                PasswordChangeServletExceptionCode.JSON_ERROR.create(
                e,
                e.getMessage()));
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(req)));
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        }
    }

    private void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, JSONException, IOException {
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        if (actionStr.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            actionPutUpdate(req, resp);
        } else {
            throw PasswordChangeServletExceptionCode.UNSUPPORTED_ACTION.create(actionStr, "PUT");
        }
    }

    private void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        throw PasswordChangeServletExceptionCode.UNSUPPORTED_ACTION.create(actionStr, "GET");
    }

    private void actionPutUpdate(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final Session session = getSessionObject(req);
        try {
            /*
             * Construct JSON object from request's body data and check mandatory fields
             */
            final JSONObject requestObject = new JSONObject(getBody(req));
            if (!requestObject.has(PARAM_NEW_PASSWORD) || requestObject.isNull(PARAM_NEW_PASSWORD)) {
                throw PasswordChangeServletExceptionCode.MISSING_PARAM.create(PARAM_NEW_PASSWORD);
            }
            if (!requestObject.has(PARAM_OLD_PASSWORD) || requestObject.isNull(PARAM_OLD_PASSWORD)) {
                throw PasswordChangeServletExceptionCode.MISSING_PARAM.create(PARAM_OLD_PASSWORD);
            }
            /*
             * Perform password change
             */
            final PasswordChangeService passwordChangeService = getServiceRegistry().getService(PasswordChangeService.class);
            if (passwordChangeService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(
                    PasswordChangeService.class.getName());
            }
            final ContextService contextService = getServiceRegistry().getService(ContextService.class);
            if (contextService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(
                    ContextService.class.getName());
            }
            passwordChangeService.perform(new PasswordChangeEvent(
                session,
                contextService.getContext(session.getContextId()),
                requestObject.getString(PARAM_NEW_PASSWORD),
                requestObject.getString(PARAM_OLD_PASSWORD)));

        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(PasswordChangeServletExceptionCode.PW_CHANGE_SUCCEEDED.create().getDisplayMessage(localeFrom(session)));
        // response.addWarning(PasswordChangeServletExceptionCode.PW_CHANGE_SUCCEEDED.create());
        response.setTimestamp(null);
        ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
    }

    private static String checkStringParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String paramVal = req.getParameter(paramName);
        if ((paramVal == null) || (paramVal.length() == 0) || "null".equals(paramVal)) {
            throw PasswordChangeServletExceptionCode.MISSING_PARAM.create(paramName);
        }
        return paramVal;
    }

}
