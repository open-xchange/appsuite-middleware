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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public final class ListAction extends JSlobAction {

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services The service look-up
     */
    public ListAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super(services, actions);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException, JSONException {
        try {
            String serviceId = jslobRequest.getParameter("serviceId", String.class, true);
            if (null == serviceId) {
                serviceId = DEFAULT_SERVICE_ID;
            }
            final JSlobService jslobService = getJSlobService(serviceId);
            Object obj = jslobRequest.getRequestData().requireData();
            if (!(obj instanceof JSONArray)) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            final JSONArray ids = (JSONArray) jslobRequest.getRequestData().requireData();
            final int length = ids.length();
            // Check length
            if (length <= 1) {
                if (0 == length) {
                    return new AJAXRequestResult(Collections.<JSlob> emptyList(), "jslob");
                }
                return new AJAXRequestResult(Collections.<JSlob> singletonList(jslobService.get(ids.getString(0), jslobRequest.getSession())), "jslob");
            }
            // More than one to load
            final List<String> sIds = new ArrayList<String>(length);
            for (int i = 0; i < length; i++) {
                sIds.add(ids.getString(i));
            }
            return new AJAXRequestResult(jslobService.get(sIds, jslobRequest.getSession()), "jslob");
        } catch (RuntimeException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getAction() {
        return "list";
    }

}
