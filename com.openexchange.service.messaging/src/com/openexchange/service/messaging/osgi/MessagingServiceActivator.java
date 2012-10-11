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

package com.openexchange.service.messaging.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.service.messaging.MessageHandler;
import com.openexchange.service.messaging.MessagingService;
import com.openexchange.service.messaging.MessagingServiceConstants;
import com.openexchange.service.messaging.internal.DelegateEventHandler;
import com.openexchange.service.messaging.internal.DelegateMessageHandler;
import com.openexchange.service.messaging.internal.MessageHandlerTracker;
import com.openexchange.service.messaging.internal.MessagingConfig;
import com.openexchange.service.messaging.internal.MessagingRemoteServerProvider;
import com.openexchange.service.messaging.internal.MessagingServiceImpl;
import com.openexchange.service.messaging.internal.receipt.MessagingServer;
import com.openexchange.service.messaging.services.ServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link MessagingServiceActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingServiceActivator extends HousekeepingActivator {

    private List<ServiceRegistration<?>> registrations;

    private List<ServiceTracker<?,?>> trackers;

    private MessagingServer server;

    /**
     * Initializes a new {@link MessagingServiceActivator}.
     */
    public MessagingServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ThreadPoolService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingServiceActivator.class));
        if (logger.isInfoEnabled()) {
            logger.info("Re-available service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingServiceActivator.class));
        if (logger.isWarnEnabled()) {
            logger.warn("Absent service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    public void startBundle() throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingServiceActivator.class));
        try {
            if (log.isInfoEnabled()) {
                log.info("starting bundle: com.openexchange.service.messaging");
            }
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = ServiceRegistry.getInstance();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            /*
             * Initialize
             */
            final MessageHandlerTracker handlers = new MessageHandlerTracker(context);
            MessagingConfig.initInstance(getService(ConfigurationService.class));
            final MessagingConfig config = MessagingConfig.getInstance();
            MessagingRemoteServerProvider.initInstance(context);
            final DelegateMessageHandler dmh = new DelegateMessageHandler(context);
            /*
             * Start other trackers
             */
            trackers = new ArrayList<ServiceTracker<?,?>>(4);
            trackers.add(handlers);
            trackers.add(MessagingRemoteServerProvider.getInstance());
            trackers.add(dmh);
            for (final ServiceTracker<?,?> tracker : trackers) {
                tracker.open();
            }
            /*
             * Register
             */
            registrations = new ArrayList<ServiceRegistration<?>>(3);
            server = new MessagingServer(handlers);
            server.startServer(config);
            {
                final Dictionary<String, String> serviceProperties = new Hashtable<String, String>(1);
                serviceProperties.put(MessagingServiceConstants.MESSAGE_TOPIC, "*");
                registrations.add(context.registerService(MessageHandler.class, dmh, serviceProperties));
            }
            final MessagingServiceImpl serviceImpl = new MessagingServiceImpl(server.getServerSocket());
            registrations.add(context.registerService(MessagingService.class, serviceImpl, null));
            {
                final Dictionary<String, String> serviceProperties = new Hashtable<String, String>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, "remote/*");
                registrations.add(context.registerService(
                    EventHandler.class,
                    new DelegateEventHandler(serviceImpl),
                    serviceProperties));
            }
        } catch (final Exception e) {
            log.error("Starting bundle \"com.openexchange.service.messaging\" failed.", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingServiceActivator.class));
        try {
            if (log.isInfoEnabled()) {
                log.info("stopping bundle: com.openexchange.service.messaging");
            }
            if (server != null) {
                server.stopServer();
                server = null;
            }
            if (null != trackers) {
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                trackers = null;
            }
            if (null != registrations) {
                while (!registrations.isEmpty()) {
                    registrations.remove(0).unregister();
                }
                registrations = null;
            }
            MessagingRemoteServerProvider.dropInstance();
            MessagingConfig.dropInstance();
        } catch (final Exception e) {
            log.error("Stopping bundle \"com.openexchange.service.messaging\" failed.", e);
            throw e;
        }
    }

}
