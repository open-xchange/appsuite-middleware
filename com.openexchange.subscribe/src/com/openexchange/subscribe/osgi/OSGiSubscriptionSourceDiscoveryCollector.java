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

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.CompositeSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;


/**
 * {@link OSGiSubscriptionSourceDiscoveryCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiSubscriptionSourceDiscoveryCollector implements ServiceTrackerCustomizer<SubscriptionSourceDiscoveryService, SubscriptionSourceDiscoveryService>, SubscriptionSourceDiscoveryService {

    private final BundleContext context;
    private final ServiceTracker<SubscriptionSourceDiscoveryService, SubscriptionSourceDiscoveryService> tracker;
    private final List<ServiceReference<SubscriptionSourceDiscoveryService>> references = new ArrayList<ServiceReference<SubscriptionSourceDiscoveryService>>();

    private final CompositeSubscriptionSourceDiscoveryService delegate = new CompositeSubscriptionSourceDiscoveryService();

    public OSGiSubscriptionSourceDiscoveryCollector(final BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker<SubscriptionSourceDiscoveryService, SubscriptionSourceDiscoveryService>(context, SubscriptionSourceDiscoveryService.class.getName(), this);
        tracker.open();
    }

    public void close() {
        delegate.clear();
        for (final ServiceReference<SubscriptionSourceDiscoveryService> reference : references) {
            context.ungetService(reference);
        }
        tracker.close();
    }

    @Override
    public SubscriptionSourceDiscoveryService addingService(final ServiceReference<SubscriptionSourceDiscoveryService> reference) {
        final SubscriptionSourceDiscoveryService service = context.getService(reference);
        if (service.getClass() == getClass()) {
            context.ungetService(reference);
            return service;
        }
        delegate.addSubscriptionSourceDiscoveryService(service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<SubscriptionSourceDiscoveryService> reference, final SubscriptionSourceDiscoveryService service) {

    }

    @Override
    public void removedService(final ServiceReference<SubscriptionSourceDiscoveryService> reference, final SubscriptionSourceDiscoveryService service) {
        delegate.removeSubscriptionSourceDiscoveryService(service);
        references.remove(reference);
        context.ungetService(reference);
    }


    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        return delegate.getSource(context, subscriptionId);
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        return delegate.getSource(identifier);
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return delegate.getSources();
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        return delegate.getSources(folderModule);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return delegate.knowsSource(identifier);
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return delegate.filter(user, context);
    }

    public void addSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        delegate.addSubscriptionSourceDiscoveryService(service);
    }

    public void removeSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        delegate.removeSubscriptionSourceDiscoveryService(service);
    }


}
