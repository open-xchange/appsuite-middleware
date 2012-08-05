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

package com.openexchange.caching.hazelcast.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.hazelcast.HazelcastCacheService;
import com.openexchange.caching.hazelcast.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link HazelcastCacheActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastCacheActivator extends HousekeepingActivator {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastCacheActivator.class);

    private HazelcastCacheService hazelcastCacheService;

    private ServiceRegistration<CacheService> cacheServiceRegistration;

    /**
     * Initializes a new {@link HazelcastCacheActivator}.
     */
    public HazelcastCacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HazelcastInstance.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        // Register Hazelcast cache service
        startUp();

        track(CacheService.class, new ServiceTrackerCustomizer<CacheService, CacheService>() {
            final AtomicBoolean stopped = new AtomicBoolean(false);

            @Override
            public CacheService addingService(final ServiceReference<CacheService> reference) {
                final Integer ranking = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
                if (null != ranking && ranking.intValue() > 0 && stopped.compareAndSet(false, true)) {
                    LOG.warn("Found higher-ranked cache service.");
                    shutdown();
                }
                return null;
            }

            @Override
            public void modifiedService(final ServiceReference<CacheService> reference, final CacheService service) {
                // Ignore
            }

            @Override
            public void removedService(final ServiceReference<CacheService> reference, final CacheService service) {
                try {
                    final Integer ranking = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
                    if (null != ranking && ranking.intValue() > 0 && stopped.compareAndSet(true, false)) {
                        startUp();
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        openTrackers();
    }

    protected void startUp() throws OXException {
        synchronized (this) {
            HazelcastCacheService hazelcastCacheService = this.hazelcastCacheService;
            if (null != hazelcastCacheService) {
                return;
            }
            hazelcastCacheService = new HazelcastCacheService(getService(HazelcastInstance.class));
            hazelcastCacheService.loadDefaultConfiguration();
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            cacheServiceRegistration = context.registerService(CacheService.class, hazelcastCacheService, props);
            this.hazelcastCacheService = hazelcastCacheService;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        shutdown();
        super.stopBundle();
    }

    protected void shutdown() {
        synchronized (this) {
            final ServiceRegistration<CacheService> serviceRegistration = cacheServiceRegistration;
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
                cacheServiceRegistration = null;
            }
            final HazelcastCacheService hazelcastCacheService = this.hazelcastCacheService;
            if (null != hazelcastCacheService) {
                hazelcastCacheService.shutdown(false);
                this.hazelcastCacheService = null;
            }
            Services.setServiceLookup(null);
        }
    }

}
