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

package com.openexchange.pns.transport.websocket;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.WebSocket;

/**
 * {@link WebSocketToClientResolver} - Resolves an open Web Socket to a certain client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketToClientResolver {

    /**
     * Gets the set containing the identifiers of all clients that are supported by this resolver
     *
     * @return The supported clients
     */
    Map<String, WebSocketClient> getSupportedClients();

    /**
     * Resolves the given open Web Socket to a client identifier that is associated with it.
     *
     * @param socket The Web Socket to resolve for
     * @return The client identifier or <code>null</code> if given Web Socket cannot be resolved
     * @throws OXException If client identifier cannot be resolved
     */
    String getClientFor(WebSocket socket) throws OXException;

    /**
     * Gets the applicable path filter expression for given client identifier.
     *
     * @param client The client identifier
     * @return The path filter expression or <code>null</code>
     * @throws OXException If path filter expression cannot be returned
     */
    String getPathFilterFor(String client) throws OXException;

}
