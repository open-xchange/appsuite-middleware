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
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link MultipleAdapterServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class MultipleAdapterServlet extends PermissionServlet {

    private static final Log LOG = LogFactory.getLog(MultipleAdapterServlet.class);
    private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(MultipleAdapterServlet.class, LOG);
    

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
        handle(req, resp);
    }
    
    
    protected void handle(HttpServletRequest req, HttpServletResponse resp) {
        if(handleOverride(req, resp)) {
            return;
        }
        try {
            String action = req.getParameter(PARAMETER_ACTION);
            JSONObject request = toJSON(req, action);
            MultipleHandler handler = createMultipleHandler();

            if(action == null) {
                throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_ACTION);
            }
            Object response = handler.performRequest(action, request, getSessionObject(req));
            Date timestamp = handler.getTimestamp();
            writeResponseSafely(response, timestamp, resp);       
        } catch (AbstractOXException x) {
            writeException(x, resp);
        } catch (Throwable t) {
            writeException(wrap(t), resp);
        }
    }

    private AbstractOXException wrap(Throwable t) {
        AbstractOXException x = new AbstractOXException(EnumComponent.NONE, Category.INTERNAL_ERROR, 1, "Caught Exception: %s", t);
        x.setMessageArgs(t.getMessage());
        return x;
    }

    protected boolean handleOverride(HttpServletRequest req, HttpServletResponse resp) {
        return false;
    }

    private void writeResponseSafely(Object data, Date timestamp, HttpServletResponse resp) {
        Response response = new Response();
        response.setData(data);
        if(null != timestamp) {
            response.setTimestamp(timestamp);
        }
        try {
            writeResponse(response, resp);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    private void writeException(AbstractOXException x, HttpServletResponse resp) {
        LL.log(x);
        Response response = new Response();
        response.setException(x);
        try {
            writeResponse(response, resp);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private JSONObject toJSON(HttpServletRequest req, String action) throws JSONException, IOException {
        JSONObject request = new JSONObject();
        Enumeration parameterNames = req.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            String parameter = req.getParameter(parameterName);
            request.put(parameterName, parameter);
        }
        if(requiresBody(action)) {
            String body = getBody(req);
            Object value = toJSONConformantValue(body);
            request.put(ResponseFields.DATA, value);
        }
        return modify(req, action, request);
    }

    protected JSONObject modify(HttpServletRequest req, String action, JSONObject request) throws JSONException {
        return request;
    }

    private Object toJSONConformantValue(String body) throws JSONException {
        return new JSONObject("{ body : "+ body+" }").get("body");
    }

    protected abstract boolean requiresBody(String action);
    protected abstract MultipleHandler createMultipleHandler();

}
