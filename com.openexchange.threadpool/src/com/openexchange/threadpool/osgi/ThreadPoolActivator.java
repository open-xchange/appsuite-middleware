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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.threadpool.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogPropertyName;
import com.openexchange.log.LogPropertyName.LogLevel;
import com.openexchange.log.LogService;
import com.openexchange.log.internal.LogServiceImpl;
import com.openexchange.management.ManagementService;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.internal.QueueProvider;
import com.openexchange.threadpool.internal.ThreadPoolProperties;
import com.openexchange.threadpool.internal.ThreadPoolServiceImpl;
import com.openexchange.timer.TimerService;
import com.openexchange.timer.internal.CustomThreadPoolExecutorTimerService;

/**
 * {@link ThreadPoolActivator} - The {@link BundleActivator activator} for thread pool bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolActivator extends HousekeepingActivator {

    public static final AtomicReference<ThreadPoolService> REF = new AtomicReference<ThreadPoolService>();

    private ThreadPoolServiceImpl threadPool;

    private LogServiceImpl logService;

    /**
     * Initializes a new {@link ThreadPoolActivator}.
     */
    public ThreadPoolActivator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ThreadPoolActivator.class));
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("starting bundle: com.openexchange.threadpool");
            }
            /*
             * Proper synchronous queue
             */
            String property = System.getProperty("java.specification.version");
            if (null == property) {
                property = System.getProperty("java.runtime.version");
                if (null == property) {
                    // JRE not detectable, use fallback
                    QueueProvider.initInstance(false);
                } else {
                    // "java.runtime.version=1.6.0_0-b14" OR "java.runtime.version=1.5.0_18-b02"
                    QueueProvider.initInstance(!property.startsWith("1.5"));
                }
            } else {
                // "java.specification.version=1.5" OR "java.specification.version=1.6"
                QueueProvider.initInstance("1.5".compareTo(property) < 0);
            }
            configureLogProperties();
            /*
             * Initialize thread pool
             */
            final ThreadPoolProperties init = new ThreadPoolProperties().init(getService(ConfigurationService.class));
            threadPool = ThreadPoolServiceImpl.newInstance(init);
            if (init.isPrestartAllCoreThreads()) {
                threadPool.prestartAllCoreThreads();
            }
            logService = new LogServiceImpl(threadPool);
            Log.set(logService);
            /*
             * Service trackers
             */
            track(ManagementService.class, new ManagementServiceTrackerCustomizer(context, threadPool));
            openTrackers();
            /*
             * Register
             */
            registerService(ThreadPoolService.class, threadPool, null);
            REF.set(threadPool);
            registerService(TimerService.class, new CustomThreadPoolExecutorTimerService(threadPool.getThreadPoolExecutor()), null);
            registerService(LogService.class, logService, null);
        } catch (final Exception e) {
            LOG.error("Failed start-up of bundle com.openexchange.threadpool: " + e.getMessage(), e);
            throw e;
        }
    }

    private void configureLogProperties() {
        final ConfigurationService service = getService(ConfigurationService.class);
        final String property = service.getProperty("com.openexchange.log.propertyNames");
        if (null == property) {
            LogProperties.configuredProperties(Collections.<LogPropertyName> emptyList());
        } else {
            final List<String> list = Arrays.asList(property.split(" *, *"));
            final List<LogPropertyName> names = new ArrayList<LogPropertyName>(list.size());
            for (final String configuredName : list) {
                if (!isEmpty(configuredName)) {
                    final int pos = configuredName.indexOf('(');
                    if (pos < 0) {
                        names.add(new LogPropertyName(configuredName, LogLevel.ALL));
                    } else {
                        final String propertyName = configuredName.substring(0, pos);
                        if (!isEmpty(propertyName)) {
                            final int closing = configuredName.indexOf(')', pos + 1);
                            if (closing < 0) { // No closing parenthesis
                                names.add(new LogPropertyName(propertyName, LogLevel.ALL));
                            } else {
                                names.add(new LogPropertyName(
                                    propertyName,
                                    LogLevel.logLevelFor(configuredName.substring(pos + 1, closing))));
                            }
                        }
                    }
                }
            }
            LogProperties.configuredProperties(names);
        }
    }

    private static boolean isEmpty(final String s) {
        if (s.length() == 0) {
            return true;
        }
        boolean retval = true;
        final char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length && retval; i++) {
            retval = Character.isWhitespace(chars[i]);
        }
        return retval;
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ThreadPoolActivator.class));
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("stopping bundle: com.openexchange.threadpool");
            }
            cleanUp();
            REF.set(null);
            /*
             * Stop thread pool
             */
            if (null != logService) {
                logService.stop();
                logService = null;
                Log.set(null);
            }
            if (null != threadPool) {
                try {
                    threadPool.shutdownNow();
                    threadPool.awaitTermination(10000L);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    threadPool = null;
                }
            }
            /*
             * Drop queue provider
             */
            QueueProvider.releaseInstance();
        } catch (final Exception e) {
            LOG.error("Failed shut-down of bundle com.openexchange.threadpool: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ThreadPoolActivator.class));
        if (LOG.isInfoEnabled()) {
            LOG.info("Appeared service: " + clazz.getName());
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ThreadPoolActivator.class));
        if (LOG.isInfoEnabled()) {
            LOG.info("Disappeared service: " + clazz.getName());
        }
    }

}
