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

package com.openexchange.push.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.internal.PushEventHandler;
import com.openexchange.push.internal.PushManagerRegistry;
import com.openexchange.push.internal.ServiceRegistry;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link PushActivator} - The activator for push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PushActivator}.
     */
    public PushActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final org.apache.commons.logging.Log log =
            com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PushActivator.class))));
        try {
            if (log.isInfoEnabled()) {
                log.info("starting bundle: com.openexchange.push");
            }
            /*
             * Initialize and open service tracker for push manager services
             */
            PushManagerRegistry.init();
            track(PushManagerService.class, new PushManagerServiceTracker(context));
            track(ConfigurationService.class, new WhitelistServiceTracker(context));
            /*
             * Thread pool service tracker
             */
            track(ConfigurationService.class, new RegistryServiceTrackerCustomizer<ConfigurationService>(context, ServiceRegistry.getInstance(), ConfigurationService.class));
            track(EventFactoryService.class, new RegistryServiceTrackerCustomizer<EventFactoryService>(context, ServiceRegistry.getInstance(), EventFactoryService.class));
            track(ThreadPoolService.class, new RegistryServiceTrackerCustomizer<ThreadPoolService>(context, ServiceRegistry.getInstance(), ThreadPoolService.class));
            track(EventAdmin.class, new RegistryServiceTrackerCustomizer<EventAdmin>(context, ServiceRegistry.getInstance(), EventAdmin.class));

            openTrackers();
            /*
             * Register event handler to detect removed sessions
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new PushEventHandler(), serviceProperties);
        } catch (final Exception e) {
            log.error("Failed start-up of bundle com.openexchange.push: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.apache.commons.logging.Log log =
            com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PushActivator.class))));
        try {
            if (log.isInfoEnabled()) {
                log.info("stopping bundle: com.openexchange.push");
            }
            /*
             * Unregister event handler
             */
            unregisterServices();
            /*
             * Drop service tracker
             */
            closeTrackers();
            PushManagerRegistry.shutdown();
        } catch (final Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.push: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // TODO Auto-generated method stub
        return null;
    }

}
