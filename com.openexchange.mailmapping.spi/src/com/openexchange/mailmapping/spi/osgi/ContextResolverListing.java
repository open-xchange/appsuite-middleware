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

package com.openexchange.mailmapping.spi.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.spi.ContextResolver;
import com.openexchange.mailmapping.spi.impl.MailResolverImpl;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ContextResolverListing}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ContextResolverListing extends RankingAwareNearRegistryServiceTracker<ContextResolver> {

    private final ServiceLookup services;
    private ServiceRegistration<MailResolver> serviceRegistration;

    /**
     * Initializes a new {@link ContextResolverListing}.
     *
     * @param context The bundle context
     */
    public ContextResolverListing(BundleContext context, ServiceLookup services) {
        super(context, ContextResolver.class);
        this.services = services;
        serviceRegistration = null;
    }

    @Override
    protected void onServiceAdded(ContextResolver service) {
        register();
    }

    @Override
    protected void onServiceRemoved(ContextResolver service) {
        if (false == hasAnyServices()) {
            unregister();
        }
    }

    private synchronized void register() {
        if (null != serviceRegistration) {
            return;
        }

        BundleContext context = this.context;
        serviceRegistration = context.registerService(MailResolver.class, new MailResolverImpl(this, services), withRanking(Integer.valueOf(Integer.MAX_VALUE)));
    }

    private synchronized void unregister() {
        if (null == serviceRegistration) {
            return;
        }

        serviceRegistration.unregister();
        serviceRegistration = null;
    }
}
