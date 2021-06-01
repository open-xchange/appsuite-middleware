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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.FallbackSubscriptionService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link RefreshSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class RefreshSubscriptionAction extends AbstractSubscribeAction {

    /**
     * Initializes a new {@link RefreshSubscriptionAction}.
     *
     * @param services
     */
    public RefreshSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        final List<Subscription> subscriptionsToRefresh = new LinkedList<Subscription>();
        final TIntSet ids = new TIntHashSet();
        JSONObject parameters = new JSONObject(subscribeRequest.getRequestData().getParameters());
        if (parameters.has("folder")) {
            String folderId;
            folderId = parameters.getString("folder");
            List<Subscription> allSubscriptions = null;
            allSubscriptions = getSubscriptionsInFolder(
                subscribeRequest.getServerSession(),
                folderId,
                services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
            Collections.sort(allSubscriptions, new Comparator<Subscription>() {

                @Override
                public int compare(final Subscription o1, final Subscription o2) {
                    if (o1.getLastUpdate() == o2.getLastUpdate()) {
                        return o2.getId() - o1.getId();
                    }
                    return (int) (o2.getLastUpdate() - o1.getLastUpdate());
                }

            });
            for (final Subscription subscription : allSubscriptions) {
                ids.add(subscription.getId());
                subscriptionsToRefresh.add(subscription);
            }
        }
        if (parameters.has("id")) {
            int id = parameters.getInt("id");
            final Subscription subscription = loadSubscription(
                id,
                subscribeRequest.getServerSession(),
                parameters.optString("source"),
                services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
            if ((subscription != null) && (ids.add(id)) && false == subscription.getSource().getId().equalsIgnoreCase(FallbackSubscriptionService.ID)) {
                subscriptionsToRefresh.add(subscription);
            }
        }

        List<OXException> errors = new LinkedList<OXException>();
        int resultCode = services.getService(SubscriptionExecutionService.class).executeSubscriptions(subscriptionsToRefresh, subscribeRequest.getServerSession(), errors);

        return new AJAXRequestResult(Integer.valueOf(resultCode), "json").addWarnings(errors);
    }

}
