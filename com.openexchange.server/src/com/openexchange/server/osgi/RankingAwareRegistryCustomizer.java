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

package com.openexchange.server.osgi;

import static com.openexchange.osgi.util.RankedService.getRanking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link RankingAwareRegistryCustomizer} - Registers/unregisters a certain service in/from {@link ServerServiceRegistry}, keeping the one with highest ranking.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class RankingAwareRegistryCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

    private final BundleContext context;
    private final Class<S> clazz;

    private final List<RankedService<S>> candidates;
    private RankedService<S> current;

    /**
     * Initializes a new {@link RankingAwareRegistryCustomizer}.
     *
     * @param context The bundle context
     * @param clazz The class of the service to register
     */
    public RankingAwareRegistryCustomizer(final BundleContext context, final Class<S> clazz) {
        super();
        this.context = context;
        this.clazz = clazz;
        candidates = new ArrayList<RankedService<S>>(4);
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        S service = context.getService(reference);
        if (clazz.isInstance(service)) {
            RankedService<S> rankedService = new RankedService<S>(service, getRanking(service, reference, 0));
            if (false == candidates.contains(rankedService) && candidates.add(rankedService)) {
                Collections.sort(candidates);
                if (null == current || current.ranking < rankedService.ranking) {
                    // Not set before OR has higher ranking than current
                    current = rankedService;
                    ServerServiceRegistry.getInstance().addService(clazz, service);
                }
                return service;
            }
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        if (null == service) {
            return;
        }

        if (candidates.remove(new RankedService<S>(service, getRanking(service, reference, 0)))) {
            if (candidates.isEmpty()) {
                // No other candidate available
                current = null;
                ServerServiceRegistry.getInstance().removeService(clazz);
            } else {
                RankedService<S> next = candidates.get(0);
                if (null == current || current.service == service) {
                    // Replace service in registry
                    current = next;
                    ServerServiceRegistry.getInstance().addService(clazz, next.service);
                }
            }
            context.ungetService(reference);
        }
    }
}
