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

package com.openexchange.osgi;

import static com.openexchange.osgi.util.RankedService.getRanking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link RankingAwareNearRegistryServiceTracker} - A {@link NearRegistryServiceTracker} that sorts tracked services by their ranking
 * (highest ranking first).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RankingAwareNearRegistryServiceTracker<S> extends ServiceTracker<S, S> implements ServiceListing<S> {

    private final SortableConcurrentList<RankedService<S>> services;
    private final int defaultRanking;
    private volatile boolean empty;

    /**
     * Initializes a new {@link RankingAwareNearRegistryServiceTracker} with <tt>0</tt> (zero) as default ranking.
     *
     * @param context The bundle context
     * @param clazz The service's class
     */
    public RankingAwareNearRegistryServiceTracker(final BundleContext context, final Class<S> clazz) {
        this(context, clazz, 0);
    }

    /**
     * Initializes a new {@link RankingAwareNearRegistryServiceTracker}.
     *
     * @param context The bundle context
     * @param clazz The service's class
     * @param defaultRanking The default ranking
     */
    public RankingAwareNearRegistryServiceTracker(final BundleContext context, final Class<S> clazz, final int defaultRanking) {
        super(context, clazz, null);
        services = new SortableConcurrentList<RankedService<S>>();
        this.defaultRanking = defaultRanking;
        empty = true; // Initially empty
    }

    /**
     * Called when a service appeared, but is not yet added.
     *
     * @param service The appeared service
     * @return <code>true</code> to proceed; otherwise <code>false</code> to discard the appeared service
     */
    protected boolean onServiceAppeared(S service) {
        return true;
    }

    /**
     * Called when a service gets added.
     *
     * @param service The added service
     */
    protected void onServiceAdded(S service) {
        // Nothing
    }

    /**
     * Called when a service disappeared, but is not yet removed.
     *
     * @param service The disappeared service
     */
    protected void onServiceDisappeared(S service) {
        // Nothing
    }

    /**
     * Called when a service gets removed.
     *
     * @param service The removed service
     */
    protected void onServiceRemoved(S service) {
        // Nothing
    }

    /**
     * Checks if this service list has any services
     *
     * @return <code>true</code> if at least one service exists; otherwise <code>false</code>
     */
    protected boolean hasAnyServices() {
        return !empty;
    }

    /**
     * Gets the currently highest-ranked service from this service listing
     *
     * @return The highest-ranked service or <code>null</code> (if service listing is empty)
     */
    protected S getHighestRanked() {
        return empty ? null : services.get(0).service;
    }

    /**
     * Gets the rank-wise sorted service list
     *
     * @return The rank-wise sorted service list
     */
    @Override
    public List<S> getServiceList() {
        return empty ? Collections.<S>emptyList() : asList();
    }

    @Override
    public Iterator<S> iterator() {
        return empty ? Collections.<S> emptyIterator() : new Iter<S>(services.getSnapshot());
    }

    /**
     * Gets the list view for tracked services.
     *
     * @return The list view
     */
    private List<S> asList() {
        List<RankedService<S>> snapshot = services.getSnapshot();
        List<S> ret = new ArrayList<S>(snapshot.size());
        for (RankedService<S> rs : snapshot) {
            ret.add(rs.service);
        }
        return ret;
    }

    @Override
    public synchronized S addingService(final ServiceReference<S> reference) {
        final S service = context.getService(reference);
        if (false == onServiceAppeared(service)) {
            context.ungetService(reference);
            return null;
        }

        final RankedService<S> rankedService = new RankedService<S>(service, getRanking(service, reference, defaultRanking));
        if (services.addAndSort(rankedService)) { // Append
            empty = false;
            onServiceAdded(service);
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public synchronized void removedService(final ServiceReference<S> reference, final S service) {
        onServiceDisappeared(service);
        if (services.remove(new RankedService<S>(service, getRanking(service, reference, defaultRanking)))) {
            empty = services.isEmpty();
            onServiceRemoved(service);
        }
        context.ungetService(reference);
    }

    // -----------------------------------------------------------------------------------------------------------------------

    /** The iterator implementation */
    private static final class Iter<S> implements Iterator<S> {

        private final Iterator<RankedService<S>> iterator;

        Iter(List<RankedService<S>> snapshot) {
            super();
            iterator = snapshot.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public S next() {
            return iterator.next().service;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("RankingAwareNearRegistryServiceTracker.Iter.remove()");
        }
    }

}
