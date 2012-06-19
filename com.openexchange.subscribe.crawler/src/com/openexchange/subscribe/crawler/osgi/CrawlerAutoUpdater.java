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

package com.openexchange.subscribe.crawler.osgi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.subscribe.crawler.internal.CrawlerUpdateTask;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CrawlerAutoUpdater}
 * This class is meant to create and cancel the TimerTask scheduled to check for daily updates to the crawler-configurations.
 * It is needed because otherwise the TimerTask would be saved and a new one would be created each time the crawler-bundle is
 * restarted, resulting in multiple Tasks where only one is needed.
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerAutoUpdater implements ServiceTrackerCustomizer<Object, Object> {

    private final BundleContext context;

    private ScheduledTimerTask scheduledTimerTask;

    private final Activator activator;

    private final Lock lock = new ReentrantLock();

    private TimerService timerService;

    private ConfigurationService configurationService;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CrawlerAutoUpdater.class));

    public CrawlerAutoUpdater(final BundleContext context, final Activator activator) {
        super();
        this.context = context;
        this.activator = activator;
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean taskSchedulingPossible;
        lock.lock();
        try {
            if (obj instanceof TimerService) {
                timerService = (TimerService) obj;
            }
            if (obj instanceof ConfigurationService) {
                configurationService = (ConfigurationService) obj;
            }
            taskSchedulingPossible = null != timerService && null != configurationService && scheduledTimerTask == null;
        } finally {
            lock.unlock();
        }
        // only activate the auto-update if both services are available and it is enabled via config-file
        if (taskSchedulingPossible && Boolean.parseBoolean(configurationService.getProperty(Activator.ENABLE_AUTO_UPDATE))) {
            final CrawlerUpdateTask crawlerUpdateTask = new CrawlerUpdateTask(configurationService, activator);
            // Start the job 30 seconds after this and repeat it as often as configured (default:daily)
            final long updateInterval = Integer.parseInt(configurationService.getProperty(Activator.UPDATE_INTERVAL));
            // Insert daily TimerTask to look for updates
            scheduledTimerTask = timerService.scheduleWithFixedDelay(crawlerUpdateTask, 30 * 1000, updateInterval);
            LOG.info("Task for crawler auto-update initialised");
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        ScheduledTimerTask cancel = null;
        lock.lock();
        try {
            if (service instanceof TimerService) {
                timerService = null;
            }
            if (service instanceof ConfigurationService) {
                configurationService = null;
            }
            if (scheduledTimerTask != null) {
                cancel = scheduledTimerTask;
                scheduledTimerTask = null;
            }
        } finally {
            lock.unlock();
        }
        if (cancel != null) {
            // cancel the TimerTask before either service (ConfigurationService or TimerService) or the bundle itself is going down
            cancel.cancel();
            LOG.info("Task for crawler auto-update cancelled");
        }
        context.ungetService(reference);
    }
}
