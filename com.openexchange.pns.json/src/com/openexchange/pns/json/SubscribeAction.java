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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.PushSubscriptionResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscribeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SubscribeAction extends AbstractPushJsonAction {

    /**
     * Initializes a new {@link SubscribeAction}.
     */
    public SubscribeAction(ServiceLookup services) {
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
        JSONArray jTopics = requireArrayField("topics", jRequestBody);

        List<String> topics = new ArrayList<>(jTopics.length());
        for (Object topicObj : jTopics) {
            String topic = topicObj.toString();
            try {
                PushNotifications.validateTopicName(topic);
            } catch (IllegalArgumentException e) {
                throw PushExceptionCodes.INVALID_TOPIC.create(e, topic);
            }
            topics.add(topic);
        }

        DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
            .client(client)
            .topics(topics)
            .contextId(session.getContextId())
            .token(token)
            .transportId(transportId)
            .userId(session.getUserId());
        DefaultPushSubscription subscription = builder.build();

        boolean retry;
        do {
            retry = doSubscribe(subscription, subscriptionRegistry);
        } while (retry);

        return new AJAXRequestResult(new JSONObject(2).put("success", true), "json");
    }

    private boolean doSubscribe(DefaultPushSubscription subscription, PushSubscriptionRegistry subscriptionRegistry) throws OXException {
        PushSubscriptionResult result = subscriptionRegistry.registerSubscription(subscription);
        switch (result.getStatus()) {
            case CONFLICT:
                {
                    // Unsubscribe conflicting subscription
                    DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
                        .client(subscription.getClient())
                        .contextId(result.getTokenUsingContextId())
                        .token(subscription.getToken())
                        .transportId(subscription.getTransportId())
                        .userId(result.getTokenUsingUserId());
                    DefaultPushSubscription subscriptionToDrop = builder.build();
                    LogProperties.put(LogProperties.Name.PNS_NO_RECONNECT, "true");
                    try {
                        subscriptionRegistry.unregisterSubscription(subscriptionToDrop);
                    } finally {
                        LogProperties.remove(LogProperties.Name.PNS_NO_RECONNECT);
                    }
                    return true;
                }
            case FAIL:
                throw result.getError();
            case OK:
                /* fall-through */
            default:
                break;
        }

        // Success
        return false;
    }

    @Override
    public String getAction() {
        return "subscribe";
    }

}
