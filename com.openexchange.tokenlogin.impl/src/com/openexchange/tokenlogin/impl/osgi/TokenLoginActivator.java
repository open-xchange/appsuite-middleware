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

package com.openexchange.tokenlogin.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.lock.LockService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tokenlogin.TokenLoginService;
import com.openexchange.tokenlogin.impl.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.tokenlogin.impl.Services;
import com.openexchange.tokenlogin.impl.TokenLoginServiceImpl;

/**
 * {@link TokenLoginActivator} - Activator for token-login implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TokenLoginActivator extends HousekeepingActivator implements HazelcastInstanceNotActiveExceptionHandler {

    /** The logger */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TokenLoginActivator.class);

    /**
     * Initializes a new {@link TokenLoginActivator}.
     */
    public TokenLoginActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HazelcastConfigurationService.class, SessiondService.class, ContextService.class, LockService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        final BundleContext context = this.context;

        // Get configuration service
        final ConfigurationService configService = getService(ConfigurationService.class);

        // Check if disabled
        if (!configService.getBoolProperty("com.openexchange.tokenlogin", true)) {
            LOG.info("Bundle \"com.openexchange.tokenlogin\" per configuration.");
            return;
        }

        // Max. idle time for a token
        final int maxIdleTime = configService.getIntProperty("com.openexchange.tokenlogin.maxIdleTime", 300000);

        // Create service instance
        final TokenLoginServiceImpl serviceImpl = new TokenLoginServiceImpl(maxIdleTime, configService, this);

        // Check Hazelcast stuff
        {
            final HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
            if (hazelcastConfig.isEnabled()) {
                // Track HazelcastInstance service
                final ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> customizer = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    @Override
                    public void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        removeService(HazelcastInstance.class);
                        serviceImpl.changeBackingMapToLocalMap();
                        context.ungetService(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        // Ignore
                    }

                    @Override
                    public HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                        final HazelcastInstance hazelcastInstance = context.getService(reference);
                        try {
                            final String sessionId2tokenMapName = discoverHzMapName(hazelcastConfig.getConfig(),"sessionId2token");
                            final String token2sessionIdMapName = discoverHzMapName(hazelcastConfig.getConfig(),"token2sessionId");
                            if (null == sessionId2tokenMapName || null == token2sessionIdMapName) {
                                context.ungetService(reference);
                                return null;
                            }
                            addService(HazelcastInstance.class, hazelcastInstance);
                            serviceImpl.setSessionId2tokenHzMapName(sessionId2tokenMapName);
                            serviceImpl.setToken2sessionIdMapNameHzMapName(token2sessionIdMapName);
                            serviceImpl.changeBackingMapToHz();
                            return hazelcastInstance;
                        } catch (final OXException e) {
                            LOG.warn("Couldn't initialize distributed token-login map.", e);
                        } catch (final RuntimeException e) {
                            LOG.warn("Couldn't initialize distributed token-login map.", e);
                        }
                        context.ungetService(reference);
                        return null;
                    }
                };
                track(HazelcastInstance.class, customizer);
            }
        }

        // Event handler to detect sessions that have been removed/trimmed in the meantime.
        {
            final String propSession = SessiondEventConstants.PROP_SESSION;
            final String propContainer = SessiondEventConstants.PROP_CONTAINER;
            final EventHandler eventHandler = new EventHandler() {

                @SuppressWarnings("unchecked")
                @Override
                public void handleEvent(final Event event) {
                    final String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                        for (final Session session : ((Map<String, Session>) event.getProperty(propContainer)).values()) {
                            handleSession(session);
                        }
                    } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                        final Session session = (Session) event.getProperty(propSession);
                        handleSession(session);
                    } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                        for (final Session session : ((Map<String, Session>) event.getProperty(propContainer)).values()) {
                            handleSession(session);
                        }
                    }
                }

                private void handleSession(final Session session) {
                    serviceImpl.removeTokenFor(session);
                }
            };
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, eventHandler, serviceProperties);
        }

        // Open trackers
        openTrackers();

        // Register service instance
        registerService(TokenLoginService.class, serviceImpl);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * @param config
     * @return
     * @throws IllegalStateException
     */
    private String discoverHzMapName(final Config config, String mapPrefix) throws IllegalStateException {
        final Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && !mapConfigs.isEmpty()) {
            for (final String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith(mapPrefix)) {
                    LOG.info("Using distributed token-login '{}'.", mapName);
                    return mapName;
                }
            }
        }
        LOG.info("No distributed token-login map with mapPrefix {} in hazelcast configuration", mapPrefix);
        return null;
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        final BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

}
