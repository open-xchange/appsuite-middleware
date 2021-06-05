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

package com.openexchange.pns.transport.websocket.osgi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.pns.transport.websocket.WebSocketClient;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;
import com.openexchange.pns.transport.websocket.internal.WebSocketToClientResolverRegistry;

/**
 * {@link WebSocketToClientResolverTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketToClientResolverTracker extends RankingAwareNearRegistryServiceTracker<WebSocketToClientResolver> implements WebSocketToClientResolverRegistry {

    private final ConcurrentMap<String, WebSocketClient> supportedClients;

    /**
     * Initializes a new {@link WebSocketToClientResolverTracker}.
     */
    public WebSocketToClientResolverTracker(BundleContext context) {
        super(context, WebSocketToClientResolver.class, 0);
        supportedClients = new ConcurrentHashMap<>(16, 0.9F, 1);
    }

    @Override
    protected boolean onServiceAppeared(WebSocketToClientResolver resolver) {
        List<String> toRemove = new LinkedList<>();
        boolean invalid = true;
        try {
            Map<String, WebSocketClient> clients = resolver.getSupportedClients();
            for (Map.Entry<String, WebSocketClient> clientToAdd : clients.entrySet()) {
                if (null != supportedClients.putIfAbsent(clientToAdd.getKey(), clientToAdd.getValue())) {
                    // There is already such a client...
                    return false;
                }
            }
            invalid = false;
            return true;
        } finally {
            if (invalid) {
                for (String clientToRemove : toRemove) {
                    supportedClients.remove(clientToRemove);
                }
            }
        }
    }

    @Override
    protected void onServiceRemoved(WebSocketToClientResolver resolver) {
        Map<String, WebSocketClient> clients = resolver.getSupportedClients();
        for (String clientToRemove : clients.keySet()) {
            supportedClients.remove(clientToRemove);
        }
    }

    @Override
    public Map<String, WebSocketClient> getAllSupportedClients() {
        return Collections.unmodifiableMap(supportedClients);
    }

    @Override
    public boolean containsClient(String client) {
        return null != client && supportedClients.containsKey(client);
    }

}
