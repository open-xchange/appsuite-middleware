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

package org.quartz.service.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.service.QuartzService;
import org.quartz.service.internal.QuartzServiceImpl;


/**
 * {@link QuartzActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuartzActivator implements BundleActivator {

    private volatile Scheduler scheduler;

    private volatile ServiceRegistration<QuartzService> quartzServiceRegistration;

    /**
     * Initializes a new {@link QuartzActivator}.
     */
    public QuartzActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final Log log = LogFactory.getLog(QuartzActivator.class);
        log.info("Starting bundle: org.quartz");
        try {
            System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
            // Create scheduler
            final SchedulerFactory sf = new StdSchedulerFactory();
            final Scheduler scheduler = sf.getScheduler();
            // Configure scheduler
            
            // Start scheduler
            scheduler.start();
            this.scheduler = scheduler;
            // Initialize appropriate service
            quartzServiceRegistration = context.registerService(QuartzService.class, new QuartzServiceImpl(scheduler), null);
            log.info("Bundle successfully started: org.quartz");
        } catch (final Exception e) {
            log.error("Failed starting bundle: org.quartz", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final Log log = LogFactory.getLog(QuartzActivator.class);
        log.info("Stopping bundle: org.quartz");
        try {
            final Scheduler scheduler = this.scheduler;
            if (null != scheduler) {
                scheduler.shutdown();
                this.scheduler = null;
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
