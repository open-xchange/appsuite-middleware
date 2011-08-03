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

package com.openexchange.push.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.internal.PushEventHandler;
import com.openexchange.push.internal.PushManagerRegistry;
import com.openexchange.push.internal.ServiceRegistry;
import com.openexchange.server.osgiservice.RegistryServiceTrackerCustomizer;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link PushActivator} - The activator for push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushActivator implements BundleActivator {

    private ServiceRegistration eventHandlerRegistration;

    private List<ServiceTracker> trackers;

    /**
     * Initializes a new {@link PushActivator}.
     */
    public PushActivator() {
        super();
    }

    public void start(final BundleContext context) throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(PushActivator.class))));
        try {
            if (log.isInfoEnabled()) {
                log.info("starting bundle: com.openexchange.push");
            }
            /*
             * Initialize and open service tracker for push manager services
             */
            trackers = new ArrayList<ServiceTracker>(4);
            PushManagerRegistry.init();
            trackers.add(new ServiceTracker(context, PushManagerService.class.getName(), new PushManagerServiceTracker(context)));
            trackers.add(new ServiceTracker(context, ConfigurationService.class.getName(), new WhitelistServiceTracker(context)));
            /*
             * Thread pool service tracker
             */
            trackers.add(new ServiceTracker(context, ConfigurationService.class.getName(), new RegistryServiceTrackerCustomizer<ConfigurationService>(
                context,
                ServiceRegistry.getInstance(),
                ConfigurationService.class)));
            trackers.add(new ServiceTracker(context, EventFactoryService.class.getName(), new RegistryServiceTrackerCustomizer<EventFactoryService>(
                context,
                ServiceRegistry.getInstance(),
                EventFactoryService.class)));
            trackers.add(new ServiceTracker(context, ThreadPoolService.class.getName(), new RegistryServiceTrackerCustomizer<ThreadPoolService>(
                    context,
                    ServiceRegistry.getInstance(),
                    ThreadPoolService.class)));
            trackers.add(new ServiceTracker(context, EventAdmin.class.getName(), new RegistryServiceTrackerCustomizer<EventAdmin>(
                context,
                ServiceRegistry.getInstance(),
                EventAdmin.class)));
            for (final ServiceTracker tracker : trackers) {
                tracker.open();
            }
            /*
             * Register event handler to detect removed sessions
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            eventHandlerRegistration = context.registerService(EventHandler.class.getName(), new PushEventHandler(), serviceProperties);
        } catch (final Exception e) {
            log.error("Failed start-up of bundle com.openexchange.push: " + e.getMessage(), e);
            throw e;
        }
    }

    public void stop(final BundleContext context) throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(PushActivator.class))));
        try {
            if (log.isInfoEnabled()) {
                log.info("stopping bundle: com.openexchange.push");
            }
            /*
             * Unregister event handler
             */
            if (null != eventHandlerRegistration) {
                eventHandlerRegistration.unregister();
                eventHandlerRegistration = null;
            }
            /*
             * Drop service tracker
             */
            if (null != trackers) {
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                trackers = null;
            }
            PushManagerRegistry.shutdown();
        } catch (final Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.push: " + e.getMessage(), e);
            throw e;
        }
    }

}
