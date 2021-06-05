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

package com.openexchange.subscribe.oauth;

import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.subscribe.Subscription;

/**
 * {@link AbstractSubscribeOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractSubscribeOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    private final Subscription subscription;
    private final String displayName;

    /**
     * Initialises a new {@link MSLiveContactsOAuthAccountAssociation}.
     */
    public AbstractSubscribeOAuthAccountAssociation(int accountId, int userId, int contextId, String displayName, Subscription subscription) {
        super(accountId, userId, contextId);
        this.displayName = displayName;
        this.subscription = subscription;
    }

    @Override
    public String getServiceId() {
        return subscription.getSource().getId();
    }

    @Override
    public String getId() {
        return Integer.toString(subscription.getId());
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getFolder() {
        return subscription.getFolderId();
    }

    /**
     * Returns the {@link Subscription}'s metadata
     * 
     * @return the {@link Subscription}'s metadata
     */
    protected Subscription getSubscription() {
        return subscription;
    }
}
