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

package com.openexchange.jslob.json.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONUtil;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSONUpdate;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public final class UpdateAction extends JSlobAction {

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link UpdateAction}.
     *
     * @param services The service look-up
     */
    public UpdateAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super(services, actions);
        restMethods = Collections.singletonList(Method.POST);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException, JSONException {
        String serviceId = jslobRequest.getParameter("serviceId", String.class, true);
        if (null == serviceId) {
            serviceId = DEFAULT_SERVICE_ID;
        }
        final JSlobService jslobService = getJSlobService(serviceId);

        final String id = jslobRequest.checkParameter("id");

        JSlob jslob;
        {
            final AJAXRequestData requestData = jslobRequest.getRequestData();
            final String serlvetRequestURI = requestData.getSerlvetRequestURI();
            final Object data = requestData.requireData();
            if (!isEmpty(serlvetRequestURI)) {
                /*
                 * Update by request path
                 */
                final JSONUpdate jsonUpdate = new JSONUpdate(serlvetRequestURI, data instanceof String ? JSONUtil.toObject(data.toString()) : data);
                /*
                 * Update...
                 */
                jslobService.update(id, jsonUpdate, jslobRequest.getSession());
                return new AJAXRequestResult();
            }
            /*
             * Update by JSON data
             */
            final JSONObject jsonData = (JSONObject) data;
            if (null != jsonData && jsonData.hasAndNotNull("path")) {
                final JSONUpdate jsonUpdate = new JSONUpdate(jsonData.getString("path"), jsonData.get("value"));
                /*
                 * Update...
                 */
                jslobService.update(id, jsonUpdate, jslobRequest.getSession());
                return new AJAXRequestResult();
            }
            /*
             * Perform merge
             */
            final JSONObject merged = JSONUtil.merge(jslobService.get(id, jslobRequest.getSession()).getJsonObject(), jsonData);
            jslob = new DefaultJSlob(merged);
            jslobService.set(id, jslob, jslobRequest.getSession());
            return new AJAXRequestResult();
        }
    }

    @Override
    protected AJAXRequestResult performREST(final JSlobRequest jslobRequest, final Method method) throws OXException, JSONException {
        if (!Method.POST.equals(method)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        /*
         * REST style access
         */
        final AJAXRequestData requestData = jslobRequest.getRequestData();
        final String pathInfo = requestData.getPathInfo();
        // E.g. pathInfo="11" (preceding "jslob" removed)
        if (isEmpty(pathInfo)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        final String[] pathElements = SPLIT_PATH.split(pathInfo);
        final int length = pathElements.length;
        if (0 == length) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        if (1 == length) {
            /*-
             *  PUT /jslob/11
             */
            requestData.setAction("update");
            requestData.putParameter("id", pathElements[0]);
        } else if (2 == length) {
            /*-
             *  PUT /jslob/11/<path>
             */
            requestData.setAction("update");
            requestData.putParameter("id", pathElements[0]);
            try {
                final JSONObject jObject = new JSONObject();
                jObject.put("path", pathElements[1]);
                jObject.put("value", requestData.getData());
                requestData.setData(jObject, "json");
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
        }
        return perform(jslobRequest);
    }

    @Override
    public String getAction() {
        return "update";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
