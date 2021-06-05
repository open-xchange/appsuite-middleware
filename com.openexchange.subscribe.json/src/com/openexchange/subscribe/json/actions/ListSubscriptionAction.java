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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.FallbackSubscriptionService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link ListSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ListSubscriptionAction extends AbstractSubscribeAction {

	/**
	 * Initializes a new {@link ListSubscriptionAction}.
	 * @param services
	 */
	public ListSubscriptionAction(ServiceLookup services) {
		super(services);
	}

	@Override
	public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
		final JSONArray ids = (JSONArray) subscribeRequest.getRequestData().requireData();
		final JSONObject parameters = new JSONObject(subscribeRequest.getRequestData().getParameters());
        final Context context = subscribeRequest.getServerSession().getContext();
        final List<Subscription> subscriptions = new ArrayList<Subscription>(ids.length());
        for (int i = 0, size = ids.length(); i < size; i++) {
            int id;
			{
				id = ids.getInt(i);
				final SubscriptionSource source = getDiscovery(subscribeRequest.getServerSession()).getSource(context, id);
	            if (source != null) {
	                final SubscribeService subscribeService = source.getSubscribeService();
	                final Subscription subscription = subscribeService.loadSubscription(context, id, services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
	                if (subscription != null && checkPermission(subscribeRequest.getServerSession(), subscription)) {
	                    subscriptions.add(subscription);
	                }
                } else {
                    final Subscription subscription = FallbackSubscriptionService.getInstance().getSubscription(context, id);
                    if (subscription != null && checkPermission(subscribeRequest.getServerSession(), subscription)) {
                        subscriptions.add(subscription);
                    }
	            }
			}

        }
		{
		    final String[] basicColumns = getBasicColumns(parameters);
		    Map<String, String[]> dynamicColumns = getDynamicColumns(parameters);
			final List<String> dynamicColumnOrder = getDynamicColumnOrder(parameters);
			JSONValue res = createResponse(subscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, subscribeRequest.getTimeZone());
            return new AJAXRequestResult(res, "json");
		}
	}



}
