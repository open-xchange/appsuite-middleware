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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package org.quartz.service.osgi;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.service.QuartzService;
import org.quartz.service.internal.QuartzProperties;
import org.quartz.service.internal.QuartzServiceImpl;
import org.quartz.service.internal.Services;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link QuartzActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class QuartzActivator extends HousekeepingActivator {

    private volatile ServiceRegistration<QuartzService> quartzServiceRegistration;

    private volatile Scheduler localScheduler;

    private volatile Scheduler clusteredScheduler;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastInstance.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final Log log = LogFactory.getLog(QuartzActivator.class);
        log.info("Starting bundle: org.quartz");
        try {
            System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
            
            ConfigurationService config = getService(ConfigurationService.class);
            boolean startLocalScheduler = config.getBoolProperty(QuartzProperties.START_LOCAL_SCHEDULER, true);
            boolean startClusteredScheduler = config.getBoolProperty(QuartzProperties.START_CLUSTERED_SCHEDULER, true);
            int localThreads = config.getIntProperty(QuartzProperties.LOCAL_THREADS, 3);
            int clusteredThreads = config.getIntProperty(QuartzProperties.CLUSTERED_THREADS, 3);
            
            // Specify properties
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
            
            Properties clusteredProperties = new Properties();
            clusteredProperties.put("org.quartz.scheduler.instanceName", "OX-Clustered-Scheduler");
            clusteredProperties.put("org.quartz.scheduler.instanceId", getService(HazelcastInstance.class).getCluster().getLocalMember().getUuid());
            clusteredProperties.put("org.quartz.scheduler.rmi.export", false);
            clusteredProperties.put("org.quartz.scheduler.rmi.proxy", false);
            clusteredProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
            clusteredProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            clusteredProperties.put("org.quartz.threadPool.threadCount", String.valueOf(clusteredThreads));
            clusteredProperties.put("org.quartz.threadPool.threadPriority", "5");
            clusteredProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", true);
            clusteredProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
            clusteredProperties.put("org.quartz.jobStore.class", "org.quartz.service.internal.HazelcastJobStore");
            clusteredProperties.put("org.quartz.scheduler.jmx.export", true);

            // Create scheduler
            SchedulerFactory lsf = new StdSchedulerFactory(localProperties);
            SchedulerFactory csf = new StdSchedulerFactory(clusteredProperties);
            localScheduler = lsf.getScheduler();
            clusteredScheduler = csf.getScheduler();
            if (startLocalScheduler) {
                localScheduler.start();
            }
            
            if (startClusteredScheduler) {
                clusteredScheduler.start();
            }

            quartzServiceRegistration = context.registerService(QuartzService.class, new QuartzServiceImpl(localScheduler, clusteredScheduler), null);
            log.info("Bundle successfully started: org.quartz");
        } catch (final Exception e) {
            log.error("Failed starting bundle: org.quartz", e);
            throw e;
        }        
    }
    
    @Override
    protected void stopBundle() throws Exception {
        final Log log = LogFactory.getLog(QuartzActivator.class);
        log.info("Stopping bundle: org.quartz");
        try {
            Scheduler scheduler = localScheduler;
            if (null != scheduler) {
                scheduler.shutdown();
                localScheduler = null;
            }
            
            scheduler = clusteredScheduler;
            if (null != scheduler) {
                scheduler.shutdown();
                clusteredScheduler = null;
            }
            
            final ServiceRegistration<QuartzService> quartzServiceRegistration = this.quartzServiceRegistration;
            if (null != quartzServiceRegistration) {
                quartzServiceRegistration.unregister();
                this.quartzServiceRegistration = null;
            }
            log.info("Bundle successfully stopped: org.quartz");
        } catch (final Exception e) {
            log.error("Failed stopping bundle: org.quartz", e);
            throw e;
        }
    }

}
