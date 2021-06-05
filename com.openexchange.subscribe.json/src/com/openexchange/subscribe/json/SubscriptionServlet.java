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

package com.openexchange.subscribe.json;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.MultipleAdapterServlet;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscriptionServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionServlet extends MultipleAdapterServlet {

    private static final long serialVersionUID = -1033538285268622324L;

    private static SubscriptionMultipleFactory multipleFactory = null;

    public static void setFactory(SubscriptionMultipleFactory factory) {
        multipleFactory = factory;
    }

    @Override
    protected MultipleHandler createMultipleHandler() {
        return multipleFactory.createMultipleHandler();
    }

    @Override
    protected boolean requiresBody(String action) {
        return SubscriptionMultipleHandler.ACTIONS_REQUIRING_BODY.contains(action);
    }
    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return session.getUserPermissionBits().isSubscription();
    }


    @Override
    protected JSONObject modify(HttpServletRequest req, String action, JSONObject request) throws JSONException {
        request.put("__query", req.getQueryString());
        request.put("__serverURL", getServerURL(req));

        return request;
    }

    private String getServerURL(HttpServletRequest req) {
        String protocol = com.openexchange.tools.servlet.http.Tools.getProtocol(req);
        return protocol + req.getServerName();
    }

}
