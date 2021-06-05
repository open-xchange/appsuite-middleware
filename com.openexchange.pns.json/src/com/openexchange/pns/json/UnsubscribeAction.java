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

package com.openexchange.pns.json;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UnsubscribeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class UnsubscribeAction extends AbstractPushJsonAction {

    /**
     * Initializes a new {@link UnsubscribeAction}.
     */
    public UnsubscribeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        JSONObject jRequestBody = (JSONObject) requestData.requireData();

        PushSubscriptionRegistry subscriptionRegistry = services.getOptionalService(PushSubscriptionRegistry.class);
        if (null == subscriptionRegistry) {
            throw ServiceExceptionCode.absentService(PushSubscriptionRegistry.class);
        }

        String client = jRequestBody.optString("client", null);
        if (null == client) {
            client = session.getClient();
        } else if (null != session.getClient() && !client.equals(session.getClient())) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("client", client);
        }
        if (Strings.isEmpty(client)) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("client");
        }

        String token = requireStringField("token", jRequestBody);
        String transportId = requireStringField("transport", jRequestBody);

        DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
            .client(client)
            .contextId(session.getContextId())
            .token(token)
            .transportId(transportId)
            .userId(session.getUserId());
        DefaultPushSubscription subscription = builder.build();

        subscriptionRegistry.unregisterSubscription(subscription);

        return new AJAXRequestResult(new JSONObject(2).put("success", true), "json");
    }

    @Override
    public String getAction() {
        return "unsubscribe";
    }

}
