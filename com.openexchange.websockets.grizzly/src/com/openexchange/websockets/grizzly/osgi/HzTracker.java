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

package com.openexchange.websockets.grizzly.osgi;

import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.grizzly.impl.DefaultGrizzlyWebSocketApplication;
import com.openexchange.websockets.grizzly.remote.HzRemoteWebSocketDistributor;

/**
 * {@link HzTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class HzTracker implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HzTracker.class);

    private final BundleContext context;
    private final HzRemoteWebSocketDistributor remoteDistributor;
    private final GrizzlyWebSocketActivator activator;

    /**
     * Initializes a new {@link HzTracker}.
     */
    public HzTracker(HzRemoteWebSocketDistributor remoteDistributor, GrizzlyWebSocketActivator activator, BundleContext context) {
        super();
        this.remoteDistributor = remoteDistributor;
        this.activator = activator;
        this.context = context;
    }

    @Override
    public synchronized HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
        HazelcastInstance hzInstance = context.getService(reference);
        String mapName = discoverMapName(hzInstance.getConfig());
        if (null == mapName) {
            LOG.warn("No distributed Grizzly Web Sockets map found in Hazelcast configuration. Hazelcast will not be used for remote Web Socket communication!");
            context.ungetService(reference);
            return null;
        }

        LOG.info("Using distributed Grizzly Web Sockets map '{}'.", mapName);
        activator.addService(HazelcastInstance.class, hzInstance);
        remoteDistributor.setHazelcastResources(hzInstance, mapName);

        DefaultGrizzlyWebSocketApplication app = DefaultGrizzlyWebSocketApplication.getGrizzlyWebSocketApplication();
        if (null != app) {
            List<WebSocket> sockets = app.listLocalWebSockets();
            int size = sockets.size();
            if (size > 0) {
                remoteDistributor.addWebSocket(sockets);
                LOG.info("Added {} existing Web Socket(s) to distributed Grizzly Web Sockets map '{}'.", Integer.valueOf(size), mapName);
            }
        }

        return hzInstance;
    }

    @Override
    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        remoteDistributor.unsetHazelcastResources();
        activator.removeService(HazelcastInstance.class);
        context.ungetService(reference);
    }

    /**
     * Discovers the map name from the supplied Hazelcast configuration.
     *
     * @param config The config object
     * @return The sessions map name
     * @throws IllegalStateException If no such map is available
     */
    private String discoverMapName(Config config) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("grizzlyws-")) {
                    return mapName;
                }
            }
        }
        return null;
    }

}
