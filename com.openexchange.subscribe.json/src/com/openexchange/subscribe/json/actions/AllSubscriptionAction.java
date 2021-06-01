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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.FallbackSubscriptionService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AllSubscriptionAction extends AbstractSubscribeAction {

    /**
     * Initializes a new {@link AllSubscriptionAction}.
     *
     * @param services
     */
    public AllSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        String folderId = null;
        boolean containsFolder = false;

        if (subscribeRequest.getRequestData().getParameter("folder") != null) {
            folderId = subscribeRequest.getRequestData().getParameter("folder");
            containsFolder = true;
        }

        List<Subscription> allSubscriptions = null;
        if (containsFolder) {
            SecretService secretService = services.getService(SecretService.class);
            allSubscriptions = getSubscriptionsInFolder(subscribeRequest.getServerSession(), folderId, secretService.getSecret(subscribeRequest.getServerSession()));
        } else {
            allSubscriptions = getAllSubscriptions(subscribeRequest.getServerSession(), services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
        }

        JSONObject parameters = new JSONObject(subscribeRequest.getRequestData().getParameters());
        final String[] basicColumns = getBasicColumns(parameters);
        Map<String, String[]> dynamicColumns = getDynamicColumns(parameters);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(parameters);
        JSONArray jsonArray = (JSONArray) createResponse(allSubscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, subscribeRequest.getTimeZone());
        return new AJAXRequestResult(jsonArray, "json");
    }

    private List<Subscription> getAllSubscriptions(final ServerSession session, final String secret) throws OXException {
        final List<SubscriptionSource> sources = getDiscovery(session).getSources();
        final List<Subscription> allSubscriptions = new ArrayList<Subscription>();
        for (final SubscriptionSource subscriptionSource : sources) {
            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            final Collection<Subscription> subscriptions = subscribeService.loadSubscriptions(session.getContext(), session.getUserId(), secret);
            allSubscriptions.addAll(subscriptions);
        }
        return FallbackSubscriptionService.getInstance().addSubscriptionsFromMissingSource(session.getContext(), session.getUserId(), allSubscriptions);
    }

}
