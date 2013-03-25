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

package com.openexchange.service.indexing.impl.internal.nonclustered;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.service.QuartzService;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MockServiceLookup}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockServiceLookup implements ServiceLookup {

    private HazelcastInstance hazelcast = null;

    private QuartzService quartz = null;

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        if (clazz.equals(HazelcastInstance.class)) {
            return (S) getHazelcast();
        } else if (clazz.equals(QuartzService.class)) {
            return (S) getQuartz();
        }

        return null;
    }

    private QuartzService getQuartz() {
        if (quartz == null) {
            quartz = new QuartzServiceImplementation();
        }

        return quartz;
    }

    private HazelcastInstance getHazelcast() {
        if (hazelcast == null) {
            hazelcast = Hazelcast.getDefaultInstance();
        }

        return hazelcast;
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    private static final class QuartzServiceImplementation implements QuartzService {

        private final Map<String, Scheduler> schedulers = new HashMap<String, Scheduler>();

        @Override
        public Scheduler getScheduler(String name, boolean start, int threads) throws OXException {
            Scheduler scheduler = schedulers.get(name);
            if (scheduler == null) {
                Properties localProperties = new Properties();
                localProperties.put("org.quartz.scheduler.instanceName", name);
                localProperties.put("org.quartz.scheduler.rmi.export", false);
                localProperties.put("org.quartz.scheduler.rmi.proxy", false);
                localProperties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", false);
                localProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                localProperties.put("org.quartz.threadPool.threadCount", 3);
                localProperties.put("org.quartz.threadPool.threadPriority", "5");
                localProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", true);
                localProperties.put("org.quartz.jobStore.misfireThreshold", "60000");
                localProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
                localProperties.put("org.quartz.scheduler.jmx.export", true);

                try {
                    SchedulerFactory csf = new StdSchedulerFactory(localProperties);
                    scheduler = csf.getScheduler();
                    schedulers.put(name, scheduler);
                    if (start) {
                        scheduler.start();
                    }
                } catch (SchedulerException e) {
                    throw new OXException(e);
                }
            }

            return scheduler;
        }

        @Override
        public Scheduler getDefaultScheduler() throws OXException {
            // TODO Auto-generated method stub
            return null;
        }
    }

}
