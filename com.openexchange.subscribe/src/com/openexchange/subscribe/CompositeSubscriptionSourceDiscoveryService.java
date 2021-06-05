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

package com.openexchange.subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.ConcurrentList;
import com.openexchange.subscribe.helpers.FilteredSubscriptionSourceDiscoveryService;


/**
 * {@link CompositeSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private final List<SubscriptionSourceDiscoveryService> services = new ConcurrentList<SubscriptionSourceDiscoveryService>();

    @Override
    public SubscriptionSource getSource(final String identifier) {
        SubscriptionSource current = null;
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            if (subDiscoverer.knowsSource(identifier)) {
                final SubscriptionSource source = subDiscoverer.getSource(identifier);
                if (current == null || current.getPriority() < source.getPriority()) {
                    current = source;
                }
            }
        }
        return current;
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        final Map<String, SubscriptionSource> allSources = new HashMap<String, SubscriptionSource>();
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            final List<SubscriptionSource> sources = subDiscoverer.getSources(folderModule);
            for (final SubscriptionSource subscriptionSource : sources) {
                final SubscriptionSource previousSource = allSources.get(subscriptionSource.getId());
                if (previousSource == null || previousSource.getPriority() < subscriptionSource.getPriority()) {
                    allSources.put(subscriptionSource.getId(), subscriptionSource);
                }
            }
        }
        final List<SubscriptionSource> sources = new ArrayList<SubscriptionSource>(allSources.values());
        Collections.sort(sources, new Comparator<SubscriptionSource>() {

            @Override
            public int compare(final SubscriptionSource o1, final SubscriptionSource o2) {
                if (o1.getDisplayName() != null && o2.getDisplayName() != null) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
                return o1.getDisplayName() == null ? -1 : 1;
            }

        });
        return sources;
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return getSources(-1);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            if (subDiscoverer.knowsSource(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void addSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        services.add(service);
    }

    public void removeSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        services.remove(service);
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        for(final SubscriptionSourceDiscoveryService subDiscoverer : services) {
            final SubscriptionSource source = subDiscoverer.getSource(context, subscriptionId);
            if (source != null) {
                return source;
            }
        }
        return null;
    }

    public void clear() {
        services.clear();
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return new FilteredSubscriptionSourceDiscoveryService(user, context, this);
    }

}
