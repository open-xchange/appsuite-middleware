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

package com.openexchange.pns.transport.websocket.internal;

import java.util.Collections;
import java.util.List;
import com.openexchange.pns.Hit;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushMatch;

/**
 * {@link WebSocketHit}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class WebSocketHit implements Hit {

    /** The topic for all */
    private static final String ALL = KnownTopic.ALL.getName();

    private final int userId;
    private final int contextId;
    private final String client;

    /**
     * Initializes a new {@link WebSocketHit}.
     *
     * @param client The Web Socket client
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    WebSocketHit(String client, int userId, int contextId) {
        super();
        this.userId = userId;
        this.client = client;
        this.contextId = contextId;
    }

    @Override
    public String getTransportId() {
        return WebSocketPushNotificationTransport.ID;
    }

    @Override
    public List<PushMatch> getMatches() {
        return Collections.singletonList(newMatch());
    }

    private PushMatch newMatch() {
        return new PushMatchImpl(userId, contextId, client, WebSocketPushNotificationTransport.ID, WebSocketPushNotificationTransport.createTokenFor(client, userId, contextId), ALL);
    }

    @Override
    public String getClient() {
        return client;
    }
}