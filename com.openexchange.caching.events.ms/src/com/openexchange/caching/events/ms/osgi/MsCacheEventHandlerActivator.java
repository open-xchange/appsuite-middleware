/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.caching.events.ms.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.ms.internal.MsCacheEventHandler;
import com.openexchange.caching.events.ms.internal.PortableCacheEventFactory;
import com.openexchange.caching.events.ms.internal.PortableCacheKey;
import com.openexchange.caching.events.ms.internal.PortableCacheKeyFactory;
import com.openexchange.exception.OXException;
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
        return new Class<?>[] { };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MsCacheEventHandlerActivator.class);
        logger.info("starting bundle: {}", context.getBundle().getSymbolicName());
        registerService(CustomPortableFactory.class, new PortableCacheKeyFactory());
        registerService(CustomPortableFactory.class, new PortableCacheEventFactory());

        final BundleContext context = this.context;

        MultipleServiceTracker tracker = new MultipleServiceTracker(context, CacheEventService.class, PortableMsService.class) {

            private volatile MsCacheEventHandler eventHandler;

            @Override
            protected boolean serviceRemoved(Object service) {
                logger.debug("Stopping messaging service cache event handler");
                MsCacheEventHandler eventHandler = this.eventHandler;
                if (null != eventHandler) {
                    eventHandler.stop();
                    this.eventHandler = null;
                }
                return true;
            }

            @Override
            protected void onAllAvailable() {
                logger.debug("Initializing messaging service cache event handler");
                try {
                    this.eventHandler = new MsCacheEventHandler(getTrackedService(PortableMsService.class), getTrackedService(CacheEventService.class));
                } catch (OXException e) {
                    throw new IllegalStateException(
                        e.getMessage(), new BundleException(e.getMessage(), BundleException.ACTIVATOR_ERROR, e));
                }
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
