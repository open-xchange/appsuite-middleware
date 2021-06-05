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

package com.openexchange.subscribe.osgi;

import java.util.Collection;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceCollector;


/**
 * {@link OSGiSubscriptionSourceCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiSubscriptionSourceCollector extends SubscriptionSourceCollector implements ServiceTrackerCustomizer<SubscribeService, SubscribeService> {

    private final ServiceTracker<SubscribeService, SubscribeService> tracker;
    private final BundleContext context;
    private volatile boolean grabbedAll;

    public OSGiSubscriptionSourceCollector(final BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker<>(context, SubscribeService.class, this);
        tracker.open();
    }

    private void grabAll() {
        if (grabbedAll) {
            return;
        }
        synchronized (this) {
            if (grabbedAll) {
                return;
            }
            try {
                final Collection<ServiceReference<SubscribeService>> serviceReferences = context.getServiceReferences(SubscribeService.class, null);
                if (serviceReferences != null) {
                    for (final ServiceReference<SubscribeService> reference : serviceReferences) {
                        addingService(reference);
                    }
                }
                grabbedAll = true;
            } catch (@SuppressWarnings("unused") InvalidSyntaxException x) {
                // IGNORE, we didn't specify a filter, so won't happen
            }
        }
    }

    public void close() {
        this.tracker.close();
    }

    @Override
    public SubscribeService addingService(final ServiceReference<SubscribeService> reference) {
        final SubscribeService subscribeService = context.getService(reference);
        addSubscribeService(subscribeService);
        return subscribeService;
    }

    @Override
    public void modifiedService(final ServiceReference<SubscribeService> reference, final SubscribeService service) {
        // IGNORE
    }

    @Override
    public void removedService(final ServiceReference<SubscribeService> reference, final SubscribeService service) {
        removeSubscribeService(service.getSubscriptionSource().getId());
    }

    @Override
    public SubscriptionSource getSource(final com.openexchange.groupware.contexts.Context context, final int subscriptionId) throws OXException {
        grabAll();
        return super.getSource(context, subscriptionId);
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        grabAll();
        return super.getSource(identifier);
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        grabAll();
        return super.getSources(folderModule);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        grabAll();
        return super.knowsSource(identifier);
    }
}
