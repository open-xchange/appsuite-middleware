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

package com.openexchange.websockets.grizzly.remote;

import static com.openexchange.java.Autoboxing.I;
import org.slf4j.Logger;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.MapEvent;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.grizzly.impl.DefaultGrizzlyWebSocketApplication;

/**
 * {@link WebSocketClosingEntryListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketClosingEntryListener implements com.hazelcast.core.EntryListener<String, String> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketClosingEntryListener.class);

    private final DefaultGrizzlyWebSocketApplication app;

    /**
     * Initializes a new {@link WebSocketClosingEntryListener}.
     */
    public WebSocketClosingEntryListener(DefaultGrizzlyWebSocketApplication app) {
        super();
        this.app = app;
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        if (event == null || event.getKey() == null) {
            return;
        }
        // Manually close associated Web Socket (if any available) to enforce re-establishing a new one
        MapKey key = MapKey.parseFrom(event.getKey());
        MapValue value = MapValue.parseFrom(event.getValue());
        if (app.closeWebSockets(key.getUserId(), key.getContextId(), ConnectionId.newInstance(value.getConnectionId()))) {
            LOG.info("Closed Web Socket ({}) due to entry eviction for user {} in context {}.", value.getConnectionId(), I(key.getUserId()), I(key.getContextId()));
        }
    }

    @Override
    public void entryExpired(EntryEvent<String, String> event) {
        if (event == null || event.getKey() == null) {
            return;
        }
        // Manually close associated Web Socket (if any available) to enforce re-establishing a new one
        MapKey key = MapKey.parseFrom(event.getKey());
        MapValue value = MapValue.parseFrom(event.getValue());
        if (app.closeWebSockets(key.getUserId(), key.getContextId(), ConnectionId.newInstance(value.getConnectionId()))) {
            LOG.info("Closed Web Socket ({}) due to entry expiration for user {} in context {}.", value.getConnectionId(), I(key.getUserId()), I(key.getContextId()));
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void mapCleared(MapEvent event) {
        // Nothing
    }

    @Override
    public void mapEvicted(MapEvent event) {
        // Nothing
    }

}
