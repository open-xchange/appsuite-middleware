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

package com.openexchange.subscribe.google.oauth;

import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.google.GoogleContactsSubscribeService;
import com.openexchange.subscribe.oauth.AbstractSubscribeOAuthAccountAssociationProvider;

/**
 * {@link GoogleContactsOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleContactsOAuthAccountAssociationProvider extends AbstractSubscribeOAuthAccountAssociationProvider {

    private final GoogleContactsSubscribeService subscriptionService;

    /**
     * Initialises a new {@link GoogleContactsOAuthAccountAssociationProvider}.
     */
    public GoogleContactsOAuthAccountAssociationProvider(ServiceLookup services, GoogleContactsSubscribeService subscriptionService) {
        super(GoogleContactsSubscribeService.SOURCE_ID, services);
        this.subscriptionService = subscriptionService;
    }

    @Override
    public OAuthAccountAssociation createAssociation(int accountId, int userId, int contextId, String folderName, Subscription subscription) {
        return new GoogleContactsOAuthAccountAssociation(accountId, userId, contextId, folderName, subscription, subscriptionService);
    }
}
