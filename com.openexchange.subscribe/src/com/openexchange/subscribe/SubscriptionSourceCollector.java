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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.helpers.FilteredSubscriptionSourceDiscoveryService;

/**
 * {@link SubscriptionSourceCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionSourceCollector implements SubscriptionSourceDiscoveryService {

    private final ConcurrentMap<String, SubscribeService> services;
    private final ConcurrentMap<String, SortedSet<SubscribeService>> shelvedServices;

    /**
     * Initializes a new {@link SubscriptionSourceCollector}.
     */
    public SubscriptionSourceCollector() {
        super();
        services = new ConcurrentHashMap<String, SubscribeService>();
        shelvedServices = new ConcurrentHashMap<String, SortedSet<SubscribeService>>();
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        final SubscribeService subscribeService = services.get(identifier);
        return null == subscribeService ? null : subscribeService.getSubscriptionSource();
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        final List<SubscriptionSource> sources = new LinkedList<SubscriptionSource>();
        for (final SubscribeService subscriber : services.values()) {
            if (folderModule == -1 || subscriber.handles(folderModule)) {
                sources.add(subscriber.getSubscriptionSource());
            }
        }
        return sources;
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return getSources(-1);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return services.containsKey(identifier);
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return new FilteredSubscriptionSourceDiscoveryService(user, context, this);
    }

    public void addSubscribeService(final SubscribeService service) {
        final SubscribeService oldService = services.putIfAbsent(service.getSubscriptionSource().getId(), service);
        if (oldService != null) {
            if (oldService.getSubscriptionSource().getPriority() < service.getSubscriptionSource().getPriority()) {
                shelfService(oldService);
                services.put(service.getSubscriptionSource().getId(), service);
            } else {
                shelfService(service);
            }
        }
    }

    // FIXME: This is not unique anymore
    public void removeSubscribeService(final String identifier) {
        services.remove(identifier);
        resurrectFromShelf(identifier);
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        for (final SubscribeService source : services.values()) {
            if (source.knows(context, subscriptionId)) {
                return source.getSubscriptionSource();
            }
        }
        return null;
    }

    private void shelfService(final SubscribeService service) {
        final String identifier = service.getSubscriptionSource().getId();
        SortedSet<SubscribeService> set = shelvedServices.get(identifier);
        if (set == null) {
            final SortedSet<SubscribeService> newset = new TreeSet<SubscribeService>(new Comparator<SubscribeService>() {

                @Override
                public int compare(final SubscribeService o1, final SubscribeService o2) {
                    return o1.getSubscriptionSource().getPriority() - o2.getSubscriptionSource().getPriority();
                }

            });
            set = shelvedServices.putIfAbsent(identifier, newset);
            if (null == set) {
                set = newset;
            }
        }
        set.add(service);
    }

    private void resurrectFromShelf(final String identifier) {
        final SortedSet<SubscribeService> set = shelvedServices.get(identifier);
        if (set != null && !set.isEmpty()) {
            services.put(identifier, set.first());
            set.remove(set.first());
        }
    }

}
