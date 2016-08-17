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
                localProperties.put("org.quartz.scheduler.rmi.export", false);
                localProperties.put("org.quartz.scheduler.rmi.proxy", false);
                localProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
                localProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                localProperties.put("org.quartz.threadPool.threadCount", String.valueOf(localThreads));
                localProperties.put("org.quartz.threadPool.threadPriority", "5");
                localProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", true);
                localProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
                localProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
                localProperties.put("org.quartz.scheduler.jmx.export", true);

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
                clusteredProperties.put("org.quartz.scheduler.rmi.export", false);
                clusteredProperties.put("org.quartz.scheduler.rmi.proxy", false);
                clusteredProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
                clusteredProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                clusteredProperties.put("org.quartz.threadPool.threadCount", String.valueOf(threads <= 0 ? 1 : threads));
                clusteredProperties.put("org.quartz.threadPool.threadPriority", "5");
                clusteredProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", true);
                clusteredProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
                clusteredProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
                clusteredProperties.put("org.quartz.scheduler.jmx.export", true);
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
            for (String name : namedSchedulers.keySet()) {
                Scheduler scheduler = namedSchedulers.get(name);
                try {
                    if (scheduler.isStarted()) {
                        scheduler.shutdown();
                    }
                } catch (SchedulerException e) {
                    LOG.warn("Could not stop clustered scheduler '{}'.", name, e);
                }
            }
            namedSchedulers.clear();
        }
    }
}
