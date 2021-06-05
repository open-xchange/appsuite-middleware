/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
            if (jsonData.hasAndNotNull("path")) {
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
            } catch (JSONException e) {
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
