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

package com.openexchange.caching.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.Dictionary;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.internal.AbstractCache;
import com.openexchange.caching.internal.JCSCacheInformation;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link CacheActivator} - The {@link DeferredActivator} implementation for cache bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CacheActivator}.
     */
    public CacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CacheEventService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (ConfigurationService.class.equals(clazz)) {
            JCSCacheServiceInit.getInstance().setConfigurationService(null);
        }
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        /*
         * TODO: Reconfigure with newly available configuration service?
         */
        if (ConfigurationService.class.equals(clazz)) {
            JCSCacheServiceInit.getInstance().setConfigurationService(getService(ConfigurationService.class));
            JCSCacheServiceInit.getInstance().reconfigureByPropertyFile();
        }
    }

    @Override
    protected void startBundle() throws Exception {
        JCSCacheServiceInit.initInstance();
        final ConfigurationService service = getService(ConfigurationService.class);
        JCSCacheServiceInit.getInstance().start(service);
        JCSCacheServiceInit.getInstance().setCacheEventService(getService(CacheEventService.class));
        registerService(CacheKeyService.class, new DefaultCacheKeyService());
        /*
         * Register service
         */
        final JCSCacheService jcsCacheService = JCSCacheService.getInstance();
        {
            final Dictionary<String, Object> dictionary = withRanking(10);
            dictionary.put("name", "oxcache");
            registerService(CacheService.class, jcsCacheService, dictionary);
        }

        track(ManagementService.class, new HousekeepingManagementTracker(context, JCSCacheInformation.class.getName(), CacheInformationMBean.CACHE_DOMAIN, new JCSCacheInformation(jcsCacheService)));
        track(EventAdmin.class, new SimpleRegistryListener<EventAdmin>() {

            @Override
            public void added(final ServiceReference<EventAdmin> ref, final EventAdmin service) {
                AbstractCache.setEventAdmin(service);
            }

            @Override
            public void removed(final ServiceReference<EventAdmin> ref, final EventAdmin service) {
                AbstractCache.setEventAdmin(null);
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        /*
         * Stop cache
         */
        final JCSCacheServiceInit instance = JCSCacheServiceInit.getInstance();
        if (null != instance) {
            instance.stop();
        }
        JCSCacheServiceInit.releaseInstance();
    }
}
