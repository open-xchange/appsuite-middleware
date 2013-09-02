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

package com.openexchange.sessiond.osgi;

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.sessiond.SessionCounter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.event.SessiondEventHandler;
import com.openexchange.sessiond.impl.SessionControl;
import com.openexchange.sessiond.impl.SessionHandler;
import com.openexchange.sessiond.impl.SessionImpl;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessiond.impl.SessiondSessionSpecificRetrievalService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link SessiondActivator} - Activator for sessiond bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessiondActivator.class));

    private volatile ServiceRegistration<EventHandler> eventHandlerRegistration;

    /**
     * Initializes a new {@link SessiondActivator}.
     */
    public SessiondActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, EventAdmin.class, CryptoService.class, ThreadPoolService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        // Don't stop the sessiond
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            // (Re-)Initialize service registry with available services
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("starting bundle: com.openexchange.sessiond");
            }
            final BundleContext context = this.context;
            SessiondInit.getInstance().start();
            final SessiondService serviceImpl = /*new InvalidatedAwareSessiondService*/(new SessiondServiceImpl());
            SessiondService.SERVICE_REFERENCE.set(serviceImpl);
            registerService(SessiondService.class, serviceImpl);
            registerService(SessionCounter.class, SessionHandler.SESSION_COUNTER);

            track(ManagementService.class, new ManagementRegisterer(context));
            track(ThreadPoolService.class, new ThreadPoolTracker(context));
            track(TimerService.class, new TimerServiceTracker(context));
            track(SessionStorageService.class, new SessionStorageServiceTracker(context));
            track(ContextService.class, new ServiceTrackerCustomizer<ContextService, ContextService>() {

                @Override
                public ContextService addingService(final ServiceReference<ContextService> reference) {
                    final ContextService service = context.getService(reference);
                    addService(ContextService.class, service);
                    return service;
                }

                @Override
                public void modifiedService(final ServiceReference<ContextService> reference, final ContextService service) {
                    // Nothing to do
                }

                @Override
                public void removedService(final ServiceReference<ContextService> reference, final ContextService service) {
                    removeService(ContextService.class);
                    context.ungetService(reference);
                }
            });
            openTrackers();

            final SessiondSessionSpecificRetrievalService retrievalService = new SessiondSessionSpecificRetrievalService();
            final SessiondEventHandler eventHandler = new SessiondEventHandler();
            eventHandler.addListener(retrievalService);
            eventHandlerRegistration = eventHandler.registerSessiondEventHandler(context);

            registerService(SessionSpecificContainerRetrievalService.class, retrievalService);
        } catch (final Exception e) {
            LOG.error("SessiondActivator: start: ", e);
            // Try to stop what already has been started.
            SessiondInit.getInstance().stop();
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("stopping bundle: com.openexchange.sessiond");
        }
        try {
            final SessionStorageService storageService = getServiceRegistry().getOptionalService(SessionStorageService.class);
            final ServiceRegistration<EventHandler> eventHandlerRegistration = this.eventHandlerRegistration;
            if (null != eventHandlerRegistration) {
                eventHandlerRegistration.unregister();
                this.eventHandlerRegistration = null;
            }
            cleanUp();
            SessiondService.SERVICE_REFERENCE.set(null);
            // Put remaining sessions into cache for remote distribution, if no session storage exist
            final List<SessionControl> sessions = SessionHandler.getSessions();
            if (null != storageService) {
                try {
                    final EventAdmin eventAdmin = getServiceRegistry().getService(EventAdmin.class);
                    for (final SessionControl sessionControl : sessions) {
                        if (null != sessionControl) {
                            final SessionImpl session = sessionControl.getSession();
                            try {
                                if (storageService.addSessionIfAbsent(session)) {
                                    SessionHandler.postSessionStored(session, eventAdmin);
                                }
                            } catch (final Exception e) {
                                LOG.warn("Active session " + session.getSessionID() + " could not be put into session storage.", e);
                            }
                        }
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info("stopping bundle: com.openexchange.sessiond.\nRemaining active sessions were put into central session storage\n");
                    }
                } catch (final RuntimeException e) {
                    LOG.warn("Remaining active sessions could not be put into central session storage.", e);
                }
            }
            // Stop sessiond
            SessiondInit.getInstance().stop();
            // Clear service registry
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error("SessiondActivator: stop: ", e);
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
