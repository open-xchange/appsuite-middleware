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

package com.openexchange.caching.events.ms.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.ms.internal.MsCacheEventHandler;
import com.openexchange.caching.events.ms.internal.PortableCacheEventFactory;
import com.openexchange.caching.events.ms.internal.PortableCacheKey;
import com.openexchange.caching.events.ms.internal.PortableCacheKeyFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.ms.PortableMsService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.MultipleServiceTracker;

/**
 * {@link MsCacheEventHandlerActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class MsCacheEventHandlerActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MsCacheEventHandlerActivator}.
     */
    public MsCacheEventHandlerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MsCacheEventHandlerActivator.class);
        logger.info("starting bundle: {}", context.getBundle().getSymbolicName());
        registerService(CustomPortableFactory.class, new PortableCacheKeyFactory());
        registerService(CustomPortableFactory.class, new PortableCacheEventFactory());

        final BundleContext context = this.context;

        MultipleServiceTracker tracker = new MultipleServiceTracker(context, CacheEventService.class, PortableMsService.class, ConfigurationService.class) {

            private MsCacheEventHandler eventHandler;

            @Override
            protected synchronized boolean serviceRemoved(Object service) {
                logger.debug("Stopping messaging service cache event handler");
                MsCacheEventHandler eventHandler = this.eventHandler;
                if (null != eventHandler) {
                    eventHandler.stop();
                    this.eventHandler = null;
                }
                return true;
            }

            @Override
            protected synchronized void onAllAvailable() {
                logger.debug("Initializing messaging service cache event handler");
                this.eventHandler = new MsCacheEventHandler(getTrackedService(PortableMsService.class), getTrackedService(CacheEventService.class), getTrackedService(ConfigurationService.class));
            }
        };
        rememberTracker(tracker.createTracker());

        track(CacheKeyService.class, new ServiceTrackerCustomizer<CacheKeyService, CacheKeyService>() {

            @Override
            public CacheKeyService addingService(ServiceReference<CacheKeyService> reference) {
                CacheKeyService cacheKeyService = context.getService(reference);
                PortableCacheKey.setCacheKeyService(cacheKeyService);
                return cacheKeyService;
            }

            @Override
            public void modifiedService(ServiceReference<CacheKeyService> reference, CacheKeyService service) {
                // Ignored
            }

            @Override
            public void removedService(ServiceReference<CacheKeyService> reference, CacheKeyService service) {
                PortableCacheKey.setCacheKeyService(null);
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MsCacheEventHandlerActivator.class);
        logger.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }

}
