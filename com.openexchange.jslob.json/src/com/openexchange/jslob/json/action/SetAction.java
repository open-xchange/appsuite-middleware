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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public final class SetAction extends JSlobAction {

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link SetAction}.
     *
     * @param services The service look-up
     */
    public SetAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super(services, actions);
        restMethods = Arrays.asList(Method.DELETE, Method.PUT);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException {
        String serviceId = jslobRequest.getParameter("serviceId", String.class, true);
        if (null == serviceId) {
            serviceId = DEFAULT_SERVICE_ID;
        }
        final JSlobService jslobService = getJSlobService(serviceId);

        final String id = jslobRequest.checkParameter("id");
        final Object data = jslobRequest.getRequestData().getData();
        /*
         * A null value is considered as a remove operation
         */
        final DefaultJSlob jslob = null == data || JSONObject.NULL.equals(data) ? DefaultJSlob.EMPTY_JSLOB : new DefaultJSlob((JSONObject) data);
        jslobService.set(id, jslob, jslobRequest.getSession());
        return new AJAXRequestResult();
    }

    @Override
    protected AJAXRequestResult performREST(final JSlobRequest jslobRequest, final Method method) throws OXException, JSONException {
        /*
         * REST style access
         */
        final AJAXRequestData requestData = jslobRequest.getRequestData();
        final String pathInfo = requestData.getPathInfo();
        if (Method.DELETE.equals(method)) {
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
                 *  DELETE /jslob/11
                 */
                requestData.setAction("set");
                requestData.putParameter("id", pathElements[0]);
                requestData.setData(null);
            } else {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
            }
            return perform(jslobRequest);
        } else if (Method.PUT.equals(method)) {
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
                requestData.setAction("set");
                requestData.putParameter("id", pathElements[0]);
            } else {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
            }
            return perform(jslobRequest);
        } else {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
    }

    @Override
    public String getAction() {
        return "set";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
