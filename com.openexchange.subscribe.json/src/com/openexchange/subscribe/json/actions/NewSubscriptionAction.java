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
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class NewSubscriptionAction extends AbstractSubscribeAction {

    /**
     * Initializes a new {@link NewSubscriptionAction}.
     */
    public NewSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        Subscription subscription = getSubscription(subscribeRequest.getRequestData(), subscribeRequest.getServerSession(), services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
        subscription.setId(-1);

        SubscriptionSource subscriptionSource = subscription.getSource();
        if (null == subscriptionSource) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("source");
        }
        checkAllowed(subscription);
        subscriptionSource.getSubscribeService().subscribe(subscription);

        return new AJAXRequestResult(Integer.valueOf(subscription.getId()), "json");
    }
}
