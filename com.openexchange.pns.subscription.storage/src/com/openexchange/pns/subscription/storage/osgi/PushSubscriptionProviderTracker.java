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

package com.openexchange.pns.subscription.storage.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.pns.PushSubscriptionListener;
import com.openexchange.pns.PushSubscriptionProvider;

/**
 * {@link PushSubscriptionProviderTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushSubscriptionProviderTracker extends RankingAwareNearRegistryServiceTracker<PushSubscriptionProvider> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PushSubscriptionProviderTracker.class);

    private final ServiceListing<PushSubscriptionListener> listeners;

    /**
     * Initializes a new {@link PushSubscriptionProviderTracker}.
     */
    public PushSubscriptionProviderTracker(ServiceListing<PushSubscriptionListener> listeners, BundleContext context) {
        super(context, PushSubscriptionProvider.class, 0);
        this.listeners = listeners;
    }

    @Override
    protected boolean onServiceAppeared(PushSubscriptionProvider provider) {
        for (PushSubscriptionListener listener : listeners) {
            try {
                if (!listener.addingProvider(provider)) {
                    return false;
                }
            } catch (OXException e) {
                LOG.error("'{}' failed to handle appeared subscription provider", listener.getClass().getName(), e);
            }
        }

        return true;
    }

    @Override
    protected void onServiceAdded(PushSubscriptionProvider provider) {
        for (PushSubscriptionListener listener : listeners) {
            try {
                listener.addedProvider(provider);
            } catch (OXException e) {
                LOG.error("'{}' failed to handle added subscription provider", listener.getClass().getName(), e);
            }
        }
    }

    @Override
    protected void onServiceDisappeared(PushSubscriptionProvider provider) {
        for (PushSubscriptionListener listener : listeners) {
            try {
                listener.removedProvider(provider);
            } catch (OXException e) {
                LOG.error("'{}' failed to handle disappeared subscription provider", listener.getClass().getName(), e);
            }
        }
    }

}
