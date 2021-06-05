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

import java.util.Iterator;
import java.util.Set;
import com.openexchange.pns.Hit;
import com.openexchange.pns.Hits;
import com.openexchange.pns.transport.websocket.WebSocketClient;

/**
 * {@link WebSocketHits}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class WebSocketHits implements Hits {

    private final int userId;
    private final int contextId;
    private final Set<WebSocketClient> clients;

    /**
     * Initializes a new {@link WebSocketHits}.
     *
     * @param clients The set of clients having an open Web Socket
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    WebSocketHits(Set<WebSocketClient> clients, int userId, int contextId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.clients = clients;
    }

    @Override
    public Iterator<Hit> iterator() {
        return new Iter(clients.iterator(), userId, contextId);
    }

    @Override
    public boolean isEmpty() {
        return clients.isEmpty();
    }

    // ------------------------------------------------------------------------------

    private static final class Iter implements Iterator<Hit> {

        private final Iterator<WebSocketClient> iterator;
        private final int userId;
        private final int contextId;

        /**
         * Initializes a new {@link IteratorImplementation}.
         */
        Iter(Iterator<WebSocketClient> iterator, int userId, int contextId) {
            super();
            this.iterator = iterator;
            this.userId = userId;
            this.contextId = contextId;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Hit next() {
            WebSocketClient client = iterator.next();
            return new WebSocketHit(client.getClient(), userId, contextId);
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}