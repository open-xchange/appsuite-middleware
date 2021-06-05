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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link HigherRankedObservator} - An observator for possibly appearing higher-ranked service(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see HigherRankedObservatorCallback
 */
public class HigherRankedObservator<S> implements ServiceTrackerCustomizer<S, S> {

    /**
     * The callback for {@link HigherRankedObservator observator}.
     */
    public static interface HigherRankedObservatorCallback<S> {

        /**
         * Invoked if first service appeared with a ranking higher than observator's one.
         *
         * @param reference The service reference
         * @param service The service
         */
        void onFirstHigherRankedAvailable(final ServiceReference<S> reference, final S service);

        /**
         * Invoked if last service disappeared with a ranking higher than observator's one.
         *
         * @param reference The service reference
         * @param service The service
         */
        void onLastHigherRankedDisappeared(final ServiceReference<S> reference, final S service);
    }

    private final AtomicInteger higherRankedCounter;

    private final List<HigherRankedObservatorCallback<S>> callbacks;

    private final int myRanking;

    private final BundleContext context;

    /**
     * Initializes a new {@link HigherRankedObservator}.
     *
     * @param ranking This observator's ranking
     * @param context The bundle context to acquire higher-ranked service(s)
     */
    public HigherRankedObservator(final int ranking, final BundleContext context) {
        super();
        callbacks = new CopyOnWriteArrayList<HigherRankedObservatorCallback<S>>();
        this.context = context;
        higherRankedCounter = new AtomicInteger(0);
        this.myRanking = ranking;
    }

    /**
     * Adds specified callback to this observator.
     *
     * @param callback The callback to add
     * @return This observator with callback added
     */
    public HigherRankedObservator<S> addCallback(final HigherRankedObservatorCallback<S> callback) {
        if (null != callback) {
            callbacks.add(callback);
        }
        return this;
    }

    /**
     * Removes specified callback from this observator.
     *
     * @param callback The callback to remove
     * @return This observator with callback removed
     */
    public HigherRankedObservator<S> removeCallback(final HigherRankedObservatorCallback<S> callback) {
        if (null != callback) {
            callbacks.remove(callback);
        }
        return this;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        if ((getRanking(reference) > myRanking) && (1 == higherRankedCounter.incrementAndGet())) {
            final S service = context.getService(reference);
            for (final HigherRankedObservatorCallback<S> callback : callbacks) {
                callback.onFirstHigherRankedAvailable(reference, service);
            }
            return service;
        }
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        if (null == service) {
            return;
        }
        if ((getRanking(reference) > myRanking) && (0 == higherRankedCounter.decrementAndGet())) {
            try {
                for (final HigherRankedObservatorCallback<S> callback : callbacks) {
                    callback.onLastHigherRankedDisappeared(reference, service);
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

    private int getRanking(final ServiceReference<S> reference) {
        final Object property = reference.getProperty(Constants.SERVICE_RANKING);
        if (null == property) {
            return 0;
        }
        return ((Integer) property).intValue();
    }

}
