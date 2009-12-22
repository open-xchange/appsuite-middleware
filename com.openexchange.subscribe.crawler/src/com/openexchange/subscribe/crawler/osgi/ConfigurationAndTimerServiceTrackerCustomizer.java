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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.subscribe.crawler.CrawlerUpdateTask;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * This is meant to initiate and cancel the Task for crawler-auto-updates when necessary
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ConfigurationAndTimerServiceTrackerCustomizer implements ServiceTrackerCustomizer {

    private final BundleContext context;

    private ScheduledTimerTask scheduledTimerTask;

    private Activator activator;
    
    private TimerService timerService;

    public ConfigurationAndTimerServiceTrackerCustomizer(BundleContext context, Activator activator, TimerService timerService, ScheduledTimerTask scheduledTimerTask) {
        super();
        this.context = context;
        this.scheduledTimerTask = scheduledTimerTask;
        this.activator = activator;
        this.timerService = timerService;
    }

    public Object addingService(ServiceReference reference) {
        ConfigurationService configurationService = (ConfigurationService) context.getService(reference);
        CrawlerUpdateTask crawlerUpdateTask = new CrawlerUpdateTask(configurationService, activator);
        // Start the job 30 seconds after this and repeat it as often as configured (default:daily)
        final long updateInterval = Integer.parseInt(configurationService.getProperty(activator.UPDATE_INTERVAL));
        // Insert daily TimerTask to look for updates
        scheduledTimerTask = timerService.scheduleWithFixedDelay(crawlerUpdateTask, 30 * 1000, updateInterval);
        System.out.println("***** Crawler-Updatetask started !!!");
        return configurationService;
    }

    public void modifiedService(ServiceReference reference, Object service) {
        // nothing to do here
    }

    public void removedService(ServiceReference reference, Object service) {
        scheduledTimerTask.cancel();
        context.ungetService(reference);
    }

}
