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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.caching.events.hazelcast.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.hazelcast.internal.HzCacheEventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HzCacheEventHandlerActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class HzCacheEventHandlerActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HzCacheEventHandlerActivator.class);
    
    /**
     * Initializes a new {@link HzCacheEventHandlerActivator}.
     */
    public HzCacheEventHandlerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CacheEventService.class, HazelcastConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: " + context.getBundle().getSymbolicName());
        if (false == getService(HazelcastConfigurationService.class).isEnabled()) {
            LOG.info("Aborting startup since Hazelcast is disabled by configuration.");
        }
        final BundleContext context = this.context;
        final CacheEventService cacheEventService = getService(CacheEventService.class);
        track(HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

            private volatile HzCacheEventHandler eventHandler;

            @Override
            public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                HazelcastInstance hazelcastInstance = context.getService(reference);
                HzCacheEventHandler.setHazelcastInstance(hazelcastInstance);
                LOG.debug("Initializing Hazelcast cache event handler");
                try {
                    this.eventHandler = new HzCacheEventHandler(cacheEventService);
                } catch (OXException e) {
                    throw new IllegalStateException(
                        e.getMessage(), new BundleException(e.getMessage(), BundleException.ACTIVATOR_ERROR, e));
                }
                return hazelcastInstance;
            }

            @Override
            public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                // Ignored
            }

            @Override
            public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                LOG.debug("Stopping Hazelcast cache event handler");
                this.eventHandler.stop();
                HzCacheEventHandler.setHazelcastInstance(null);
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: " + context.getBundle().getSymbolicName());
        super.stopBundle();
    }

}
