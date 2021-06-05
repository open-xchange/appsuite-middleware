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

package com.openexchange.subscribe.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.json.SubscriptionJSONErrorMessages;
import com.openexchange.subscribe.json.SubscriptionJSONWriter;

/**
 * {@link GetSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GetSubscriptionAction extends AbstractSubscribeAction {

    public GetSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        JSONObject parameters = new JSONObject(subscribeRequest.getRequestData().getParameters());

        final int id;
        try {
            id = parameters.getInt("id");
        } catch (JSONException e) {
            if (!parameters.hasAndNotNull("id")) {
                throw e;
            }
            final Object obj = parameters.get("id");
            throw new JSONException("JSONObject[\"id\"] is not a number: " + obj);
        }

        String source = "";
        if (parameters.has("source")) {
            source = parameters.getString("source");
        }

        final Subscription subscription = loadSubscription(
            id,
            subscribeRequest.getServerSession(),
            source,
            services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
        if (subscription == null || checkPermission(subscribeRequest.getServerSession(), subscription) == false) {
            throw SubscriptionJSONErrorMessages.UNKNOWN_SUBSCRIPTION.create();
        }

        String urlPrefix = "";
        if (subscribeRequest.getRequestData().getParameter("__serverURL") != null) {
            urlPrefix = subscribeRequest.getRequestData().getParameter("__serverURL");
        }

        JSONObject json = new SubscriptionJSONWriter().write(
            subscription,
            subscription.getSource().getFormDescription(),
            urlPrefix,
            subscribeRequest.getTimeZone());
        return new AJAXRequestResult(json, "json");
    }

}
