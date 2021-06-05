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

package com.openexchange.caching.events.osgi;

import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.internal.CacheEventConfigurationImpl;
import com.openexchange.caching.events.internal.CacheEventServiceImpl;
import com.openexchange.caching.events.monitoring.CacheEventMBean;
import com.openexchange.caching.events.monitoring.CacheEventMBeanImpl;
import com.openexchange.caching.events.monitoring.CacheEventMetricHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link CacheEventServiceActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheEventServiceActivator extends HousekeepingActivator {

    private CacheEventServiceImpl cacheEventService;

    /**
     * Initializes a new {@link CacheEventServiceActivator}.
     */
    public CacheEventServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, ConfigurationService.class };
    }

    @Override
    protected synchronized void handleAvailability(Class<?> clazz) {
        if (ThreadPoolService.class.equals(clazz)) {
            CacheEventServiceImpl service = this.cacheEventService;
            if (null != service) {
                service.setThreadPoolService(getService(ThreadPoolService.class));
            }
        }
    }

    @Override
    protected synchronized void handleUnavailability(Class<?> clazz) {
        if (ThreadPoolService.class.equals(clazz)) {
            CacheEventServiceImpl service = this.cacheEventService;
            if (null != service) {
                service.setThreadPoolService(null);
            }
        }
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheEventServiceActivator.class);
        logger.info("starting bundle: {}", context.getBundle().getSymbolicName());

        CacheEventMetricHandler metricHandler = new CacheEventMetricHandler();
        CacheEventServiceImpl service = new CacheEventServiceImpl(new CacheEventConfigurationImpl(getService(ConfigurationService.class)), getService(ThreadPoolService.class), metricHandler);
        this.cacheEventService = service;

        track(ManagementService.class, new HousekeepingManagementTracker(context, CacheEventMBean.NAME, CacheEventMBean.DOMAIN, new CacheEventMBeanImpl(metricHandler)));
        openTrackers();

        registerService(CacheEventService.class, service);
        registerService(Reloadable.class, service);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheEventServiceActivator.class);
        logger.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        CacheEventServiceImpl service = this.cacheEventService;
        if (null != service) {
            service.shutdown();
            this.cacheEventService = null;
        }
        super.stopBundle();
    }

}
