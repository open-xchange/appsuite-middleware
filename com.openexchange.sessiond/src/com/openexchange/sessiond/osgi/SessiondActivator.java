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

package com.openexchange.sessiond.osgi;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSerializationInterceptor;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.event.SessiondEventHandler;
import com.openexchange.sessiond.impl.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.sessiond.impl.SessionHandler;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessiond.impl.SessiondSessionSpecificRetrievalService;
import com.openexchange.sessiond.impl.TokenSessionContainer;
import com.openexchange.sessiond.portable.PortableTokenSessionControlFactory;
import com.openexchange.sessiond.serialization.PortableContextSessionsCleanerFactory;
import com.openexchange.sessiond.serialization.PortableSessionFilterApplierFactory;
import com.openexchange.sessiond.serialization.PortableUserSessionsCleanerFactory;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link SessiondActivator} - Activator for sessiond bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondActivator extends HousekeepingActivator implements HazelcastInstanceNotActiveExceptionHandler {

    /** The logger instance */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessiondActivator.class);

    // ------------------------------------------------------------------------------------------------------------------------

    private static class HazelcastConfTracker implements ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> {

        final BundleContext context;
        final SessiondActivator activator;
        private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker;

        HazelcastConfTracker(BundleContext context, SessiondActivator activator) {
            super();
            this.context = context;
            this.activator = activator;
        }

        @Override
        public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
            final HazelcastConfigurationService hzConfig = context.getService(reference);

            try {
                if (hzConfig.isEnabled()) {
                    // Track HazelcastInstance service
                    ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> customizer = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                        @Override
                        public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                            HazelcastInstance hzInstance = context.getService(reference);
                            try {
                                String hzMapName = discoverHzMapName(hzConfig.getConfig(), TokenSessionContainer.getInstance().getServerTokenMapName());
                                if (null == hzMapName) {
                                    context.ungetService(reference);
                                    return null;
                                }
                                activator.addService(HazelcastInstance.class, hzInstance);
                                TokenSessionContainer.getInstance().changeBackingMapToHz();
                                return hzInstance;
                            } catch (OXException e) {
                                LOG.warn("Couldn't initialize distributed token-session map.", e);
                            } catch (RuntimeException e) {
                                LOG.warn("Couldn't initialize distributed token-session map.", e);
                            }
                            context.ungetService(reference);
                            return null;
                        }

                        @Override
                        public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
                            // Ignore
                        }

                        @Override
                        public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
                            activator.removeService(HazelcastInstance.class);
                            TokenSessionContainer.getInstance().changeBackingMapToLocalMap();
                            context.ungetService(reference);
                        }
                    };
                    ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, customizer);
                    this.hzInstanceTracker = hzInstanceTracker;
                    hzInstanceTracker.open();
                }

                return hzConfig;
            } catch (Exception e) {
                // Failed
                LOG.error("SessiondActivator: start: ", e);
            }

            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            // Ignore
        }

        @Override
        public void removedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = this.hzInstanceTracker;
            if (null != hzInstanceTracker) {
                hzInstanceTracker.close();
                this.hzInstanceTracker = null;
            }

            context.ungetService(reference);
        }

        String discoverHzMapName(final Config config, String mapPrefix) throws IllegalStateException {
            final Map<String, MapConfig> mapConfigs = config.getMapConfigs();
            if (null != mapConfigs && !mapConfigs.isEmpty()) {
                for (final String mapName : mapConfigs.keySet()) {
                    if (mapName.startsWith(mapPrefix)) {
                        LOG.info("Using distributed token-session map '{}'.", mapName);
                        return mapName;
                    }
                }
            }
            LOG.info("No distributed token-session map with mapPrefix {} in hazelcast configuration", mapPrefix);
            return null;
        }
    } // End of class HazelcastConfTracker

    // ------------------------------------------------------------------------------------------------------------------------

    private static class HazelcastInstanceTracker implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

        final BundleContext context;

        final SessiondActivator activator;

        public HazelcastInstanceTracker(BundleContext context, SessiondActivator activator) {
            super();
            this.context = context;
            this.activator = activator;
        }

        @Override
        public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
            HazelcastInstance hzInstance = context.getService(reference);
            try {
                activator.addService(HazelcastInstance.class, hzInstance);
                return hzInstance;
            } catch (RuntimeException e) {
                LOG.warn("Couldn't initialize distributed token-session map.", e);
            }
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
            // Ignore
        }

        @Override
        public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
            activator.removeService(HazelcastInstance.class);
            context.ungetService(reference);
        }
    }

    private volatile ServiceRegistration<EventHandler> eventHandlerRegistration;

    /**
     * Initializes a new {@link SessiondActivator}.
     */
    public SessiondActivator() {
        super();
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, EventAdmin.class, CryptoService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.sessiond");
            Services.setServiceLookup(this);
            final BundleContext context = this.context;
            SessiondInit.getInstance().start();

            // Create & register portable factories
            registerService(CustomPortableFactory.class, new PortableContextSessionsCleanerFactory());
            registerService(CustomPortableFactory.class, new PortableTokenSessionControlFactory());

            // Initialize token session container
            TokenSessionContainer.getInstance().setNotActiveExceptionHandler(this);

            // Track Hazelcast
            {
                // Check if distributed token-sessions are enabled
                ConfigurationService configService = getService(ConfigurationService.class);
                if (configService.getBoolProperty("com.openexchange.sessiond.useDistributedTokenSessions", false)) {
                    // Start tracking
                    track(HazelcastConfigurationService.class, new HazelcastConfTracker(context, this));
                }
            }

            // Initialize service instance
            final SessiondService serviceImpl = /*new InvalidatedAwareSessiondService*/(new SessiondServiceImpl());
            SessiondService.SERVICE_REFERENCE.set(serviceImpl);
            registerService(SessiondService.class, serviceImpl);
            registerService(SessionCounter.class, SessionHandler.SESSION_COUNTER);
            
            registerService(ObfuscatorService.class, SessionHandler.getObfuscator());

            registerService(ObfuscatorService.class, SessionHandler.getObfuscator());

            registerService(CustomPortableFactory.class, new PortableUserSessionsCleanerFactory());
            registerService(CustomPortableFactory.class, new PortableSessionFilterApplierFactory());

            track(HazelcastInstance.class, new HazelcastInstanceTracker(context, this));
            track(ManagementService.class, new ManagementRegisterer(context));
            track(ThreadPoolService.class, new ThreadPoolTracker(context));
            track(TimerService.class, new TimerServiceTracker(context));
            track(SessionStorageService.class, new SessionStorageServiceTracker(this, context));
            trackService(ContextService.class);
            track(SessionSerializationInterceptor.class, new SessionSerializationInterceptorTracker(context));
            openTrackers();

            final SessiondSessionSpecificRetrievalService retrievalService = new SessiondSessionSpecificRetrievalService();
            final SessiondEventHandler eventHandler = new SessiondEventHandler();
            eventHandler.addListener(retrievalService);
            eventHandlerRegistration = eventHandler.registerSessiondEventHandler(context);

            registerService(SessionSpecificContainerRetrievalService.class, retrievalService);

            // Clear other sessions for a user on (remote) password change event
            {
                Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange");
                EventHandler passwordChangeEventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(Event event) {
                        if (event.containsProperty(CommonEvent.REMOTE_MARKER)) {
                            // Received from remote node
                            int contextId = ((Integer) event.getProperty("com.openexchange.passwordchange.contextId")).intValue();
                            int userId = ((Integer) event.getProperty("com.openexchange.passwordchange.userId")).intValue();
                            Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");
                            if (null != session && false == Strings.isEmpty(session.getSessionID())) {
                                Collection<Session> sessions = serviceImpl.getSessions(userId, contextId);
                                for (Session userSession : sessions) {
                                    if (false == session.getSessionID().equals(userSession.getSessionID())) {
                                        serviceImpl.removeSession(userSession.getSessionID());
                                    }
                                }
                            }
                        }
                    }
                };
                registerService(EventHandler.class, passwordChangeEventHandler, serviceProperties);
            }
        } catch (final Exception e) {
            LOG.error("SessiondActivator: start: ", e);
            // Try to stop what already has been started.
            SessiondInit.getInstance().stop();
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.sessiond");
        try {
            final ServiceRegistration<EventHandler> eventHandlerRegistration = this.eventHandlerRegistration;
            if (null != eventHandlerRegistration) {
                eventHandlerRegistration.unregister();
                this.eventHandlerRegistration = null;
            }
            cleanUp();
            SessiondService.SERVICE_REFERENCE.set(null);
            TokenSessionContainer.getInstance().setNotActiveExceptionHandler(null);
            // Stop sessiond
            SessiondInit.getInstance().stop();
            // Clear service registry
            Services.setServiceLookup(null);
        } catch (final Exception e) {
            LOG.error("SessiondActivator: stop", e);
            throw e;
        }
    }

    @Override
    public <S> boolean addService(final Class<S> clazz, final S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(final Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
