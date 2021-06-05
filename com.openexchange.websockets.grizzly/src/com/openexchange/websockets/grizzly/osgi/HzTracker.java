/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
