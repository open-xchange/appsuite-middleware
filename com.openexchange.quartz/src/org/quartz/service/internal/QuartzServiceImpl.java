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

package org.quartz.service.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.service.QuartzService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link QuartzServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuartzServiceImpl implements QuartzService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuartzServiceImpl.class);

    private final Map<String, Scheduler> namedSchedulers = new HashMap<String, Scheduler>();

    private final ConfigurationService config;

    private Scheduler defaultScheduler = null;


    /**
     * Initializes a new {@link QuartzServiceImpl}.
     */
    public QuartzServiceImpl(final ConfigurationService config) {
        super();
        this.config = config;
    }

    @Override
    public Scheduler getDefaultScheduler() throws OXException {
        synchronized (namedSchedulers) {
            if (defaultScheduler == null) {
                boolean startLocalScheduler = config.getBoolProperty(QuartzProperties.START_LOCAL_SCHEDULER, true);
                int localThreads = config.getIntProperty(QuartzProperties.LOCAL_THREADS, 3);

                Properties localProperties = new Properties();
                localProperties.put("org.quartz.scheduler.instanceName", "OX-Local-Scheduler");
                localProperties.put("org.quartz.scheduler.rmi.export", Boolean.FALSE);
                localProperties.put("org.quartz.scheduler.rmi.proxy", Boolean.FALSE);
                localProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", Boolean.FALSE);
                localProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                localProperties.put("org.quartz.threadPool.threadCount", String.valueOf(localThreads));
                localProperties.put("org.quartz.threadPool.threadPriority", "5");
                localProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", Boolean.TRUE);
                localProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
                localProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
                localProperties.put("org.quartz.scheduler.jmx.export", Boolean.TRUE);

                try {
                    SchedulerFactory csf = new StdSchedulerFactory(localProperties);
                    defaultScheduler = csf.getScheduler();
                    if (startLocalScheduler) {
                        defaultScheduler.start();
                    }
                } catch (SchedulerException e) {
                    throw new OXException(e);
                }
            }
            return defaultScheduler;
        }
    }

    @Override
    public Scheduler getScheduler(String name, boolean start, int threads) throws OXException {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' must not be null!");
        }

        synchronized (namedSchedulers) {
            Scheduler scheduler = namedSchedulers.get(name);
            if (scheduler == null) {
                Properties clusteredProperties = new Properties();
                clusteredProperties.put("org.quartz.scheduler.instanceName", name);
                clusteredProperties.put("org.quartz.scheduler.instanceId", "AUTO");
                clusteredProperties.put("org.quartz.scheduler.rmi.export", Boolean.FALSE);
                clusteredProperties.put("org.quartz.scheduler.rmi.proxy", Boolean.FALSE);
                clusteredProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", Boolean.FALSE);
                clusteredProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                clusteredProperties.put("org.quartz.threadPool.threadCount", String.valueOf(threads <= 0 ? 1 : threads));
                clusteredProperties.put("org.quartz.threadPool.threadPriority", "5");
                clusteredProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", Boolean.TRUE);
                clusteredProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
                clusteredProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
                clusteredProperties.put("org.quartz.scheduler.jmx.export", Boolean.TRUE);
                try {
                    SchedulerFactory csf = new StdSchedulerFactory(clusteredProperties);
                    scheduler = csf.getScheduler();
                } catch (SchedulerException e) {
                    throw new OXException(e);
                }

                namedSchedulers.put(name, scheduler);
            }
            try {
                if (start && !scheduler.isStarted()) {
                    scheduler.start();
                }
            } catch (SchedulerException e) {
                throw new OXException(e);
            }
            return scheduler;
        }
    }

    public void shutdown() {
        synchronized (namedSchedulers) {
            try {
                if (defaultScheduler != null && defaultScheduler.isStarted()) {
                    defaultScheduler.shutdown();
                    defaultScheduler = null;
                }
            } catch (SchedulerException e) {
                LOG.warn("Could not stop local scheduler.", e);
            }
            for (Map.Entry<String, Scheduler> entry : namedSchedulers.entrySet()) {
                Scheduler scheduler = entry.getValue();
                try {
                    if (scheduler.isStarted()) {
                        scheduler.shutdown();
                    }
                } catch (SchedulerException e) {
                    LOG.warn("Could not stop clustered scheduler '{}'.", entry.getKey(), e);
                }
            }
            namedSchedulers.clear();
        }
    }
}
