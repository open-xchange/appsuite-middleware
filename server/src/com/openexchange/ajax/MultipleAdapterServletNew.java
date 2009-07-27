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

package com.openexchange.ajax;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link MultipleAdapterServletNew} is a rewrite of the really good {@link MultipleAdapterServlet} with smarter handling of the request
 * parameters.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class MultipleAdapterServletNew extends PermissionServlet {

    private static final long serialVersionUID = -8060034833311074781L;

    private static final Log LOG = LogFactory.getLog(MultipleAdapterServletNew.class);

    private final AJAXActionServiceFactory factory;

    protected MultipleAdapterServletNew(AJAXActionServiceFactory factory) {
        super();
        this.factory = factory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO this method may need some extra handling due to uploads.
        handle(req, resp);
    }

    protected void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        try {
            String action = req.getParameter(PARAMETER_ACTION);
            if (action == null) {
                throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_ACTION);
            }
            AJAXRequestData data = parseRequest(req);
            AJAXActionService actionService = factory.createActionService(action);
            AJAXRequestResult result = actionService.perform(data, getSessionObject(req));
            response.setData(result.getResultObject());
            response.setTimestamp(result.getTimestamp());
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(resp);
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (JSONException e) {
            final OXJSONException e1 = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
            LOG.error(e1.getMessage(), e1);
            sendError(resp);
        }
    }

    private AJAXRequestData parseRequest(HttpServletRequest req) {
        AJAXRequestData retval = new AJAXRequestData();
        Enumeration<?> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            String value = req.getParameter(name);
            retval.putParameter(name, value);
        }
        // FIXME add body parsing here.
        return retval;
    }
}
