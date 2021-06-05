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

package com.openexchange.pns.appsuite;

import java.util.Collections;
import java.util.Map;
import com.openexchange.ajax.Client;
import com.openexchange.exception.OXException;
import com.openexchange.pns.Interest;
import com.openexchange.pns.transport.websocket.WebSocketClient;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSockets;

/**
 * {@link AppSuiteWebSocketToClientResolver}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AppSuiteWebSocketToClientResolver implements WebSocketToClientResolver {

    /** The path filter expression for App Suite UI: <code>"/socket.io/appsuite/*"</code> */
    private static final String PATH_FILTER_APPSUITE_UI = "/socket.io/appsuite/*";

    /**
     * Initializes a new {@link AppSuiteWebSocketToClientResolver}.
     */
    public AppSuiteWebSocketToClientResolver() {
        super();
    }

    @Override
    public String getClientFor(WebSocket socket) throws OXException {
        if (WebSockets.matches(PATH_FILTER_APPSUITE_UI, socket)) {
            return Client.APPSUITE_UI.getClientId();
        }

        return null;
    }

    @Override
    public String getPathFilterFor(String client) throws OXException {
        if (Client.APPSUITE_UI.getClientId().equals(client)) {
            return  PATH_FILTER_APPSUITE_UI;
        }

        return null;
    }

    @Override
    public Map<String, WebSocketClient> getSupportedClients() {
        return Collections.singletonMap(Client.APPSUITE_UI.getClientId(), new WebSocketClient(Client.APPSUITE_UI.getClientId(), PATH_FILTER_APPSUITE_UI, Interest.interestsFor("ox:mail:*", "ox:calendar:*")));
    }

}
