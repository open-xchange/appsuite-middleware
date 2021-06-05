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

import java.util.Map;
import com.openexchange.pns.transport.websocket.WebSocketClient;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;

/**
 * {@link WebSocketToClientResolverRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketToClientResolverRegistry extends Iterable<WebSocketToClientResolver> {

    /**
     * Gets the set containing the identifiers of all supported clients.
     *
     * @return The identifiers of all supported clients
     */
    Map<String, WebSocketClient> getAllSupportedClients();

    /**
     * Checks whether there is a resolver for specified client identifier.
     *
     * @param client The client identifier to check
     * @return <code>true</code> if there is a resolver associated with given client identifier; otherwise <code>false</code>
     */
    boolean containsClient(String client);

}
