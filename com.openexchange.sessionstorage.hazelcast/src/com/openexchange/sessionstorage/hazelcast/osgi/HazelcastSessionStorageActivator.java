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

package com.openexchange.sessionstorage.hazelcast.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageService;
import com.openexchange.sessionstorage.hazelcast.Services;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link HazelcastSessionStorageActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(HazelcastSessionStorageActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, HazelcastConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.sessionstorage.hazelcast");
        Services.setServiceLookup(this);
        final HazelcastConfigurationService configService = getService(HazelcastConfigurationService.class);
        final boolean enabled = configService.isEnabled();
        if (false == enabled) {
            LOG.warn("com.openexchange.sessionstorage.hazelcast will be disabled due to disabled Hazelcast services");
        } else {
            // Track HazelcastInstance
            final BundleContext context = this.context;
            track(HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                private volatile ServiceRegistration<SessionStorageService> sessionStorageRegistration;
                private volatile ServiceRegistration<EventHandler> eventHandlerRegistration;

                @Override
                public HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                    final HazelcastInstance hazelcastInstance = context.getService(reference);
                    HazelcastSessionStorageService.setHazelcastInstance(hazelcastInstance);
                    /*
                     * create & register session storage service
                     */
                    String sessionsMapName = discoverSessionsMapName(hazelcastInstance.getConfig());
                    final HazelcastSessionStorageService sessionStorageService = new HazelcastSessionStorageService(sessionsMapName);
                    sessionStorageRegistration = context.registerService(SessionStorageService.class, sessionStorageService, null);
                    /*
                     * create & register event handler
                     */
                    final EventHandler eventHandler = new EventHandler() {

                        @Override
                        public void handleEvent(Event osgiEvent) {
                            if (null != osgiEvent && SessiondEventConstants.TOPIC_TOUCH_SESSION.equals(osgiEvent.getTopic())) {
                                Session touchedSession = (Session)osgiEvent.getProperty(SessiondEventConstants.PROP_SESSION);
                                if (null != touchedSession && null != touchedSession.getSessionID()) {
                                    try {
                                        sessionStorageService.touch(touchedSession.getSessionID());
                                    } catch (OXException e) {
                                        LOG.warn("error handling OSGi event", e);
                                    } catch (HazelcastException e) {
                                        LOG.warn("error handling OSGi event", e);
                                    }
                                }
                            }
                        }
                    };
                    Dictionary<String, String> properties = new Hashtable<String, String>(1);
                    properties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_TOUCH_SESSION);
                    eventHandlerRegistration = context.registerService(EventHandler.class, eventHandler, properties);
                    return hazelcastInstance;
                }

                @Override
                public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    // Ignore
                }

                @Override
                public void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    /*
                     * remove event handler registration
                     */
                    ServiceRegistration<EventHandler> eventHandlerRegistration = this.eventHandlerRegistration;
                    if (null != eventHandlerRegistration) {
                        eventHandlerRegistration.unregister();
                        this.sessionStorageRegistration = null;
                    }
                    /*
                     * remove session storage registration
                     */
                    ServiceRegistration<SessionStorageService> sessionStorageRegistration = this.sessionStorageRegistration;
                    if (null != sessionStorageRegistration) {
                        sessionStorageRegistration.unregister();
                        this.sessionStorageRegistration = null;
                    }
                    context.ungetService(reference);
                    HazelcastSessionStorageService.setHazelcastInstance(null);
                }
            });
            openTrackers();
        }
    }

    @Override
    public <S> boolean removeService(final Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> boolean addService(final Class<S> clazz, final S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.sessionstorage.hazelcast");
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * Discovers the sessions map name from the supplied hazelcast configuration.
     *
     * @param config The config object
     * @return The sessions map name
     * @throws IllegalStateException
     */
    private static String discoverSessionsMapName(Config config) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("sessions-")) {
                    LOG.info("Using distributed map '" + mapName + "'.");
                    return mapName;
                }
            }
        }
        String msg = "No distributed sessions map found in hazelcast configuration";
        throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
    }

}
