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

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.FallbackSubscriptionService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link DeleteSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class DeleteSubscriptionAction extends AbstractSubscribeAction {

    /**
     * Initializes a new {@link DeleteSubscriptionAction}.
     *
     * @param services
     */
    public DeleteSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        JSONArray ids = (JSONArray) subscribeRequest.getRequestData().requireData();
        final Context context = subscribeRequest.getServerSession().getContext();
        for (int i = 0, size = ids.length(); i < size; i++) {
            final int id = ids.getInt(i);
            final SubscriptionSource s = getDiscovery(subscribeRequest.getServerSession()).getSource(context, id);
            final Subscription subscription = new Subscription();
            subscription.setContext(context);
            subscription.setId(id);
            if (null == subscription.getSession()) {
                subscription.setSession(subscribeRequest.getServerSession());
            }
            if (s == null) {
                FallbackSubscriptionService.getInstance().unsubscribe(subscription);
                continue;
            }
            final SubscribeService subscribeService = s.getSubscribeService();
            subscribeService.unsubscribe(subscription);
        }
        return new AJAXRequestResult(Integer.valueOf(1), "json");
    }

}
