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

package com.openexchange.websockets.grizzly.impl;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSocketInfo;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.remote.RemoteWebSocketDistributor;

/**
 * {@link WebSocketServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketServiceImpl implements WebSocketService {

    private final DefaultGrizzlyWebSocketApplication localApp;
    private final RemoteWebSocketDistributor remoteDistributor;

    /**
     * Initializes a new {@link WebSocketServiceImpl}.
     */
    public WebSocketServiceImpl(DefaultGrizzlyWebSocketApplication app, RemoteWebSocketDistributor remoteDistributor) {
        super();
        this.localApp = app;
        this.remoteDistributor = remoteDistributor;
    }

    @Override
    public boolean exists(int userId, int contextId) throws OXException {
        return exists(null, userId, contextId);
    }

    @Override
    public boolean exists(String pathFilter, int userId, int contextId) throws OXException {
        if (false == WebSockets.validatePath(pathFilter)) {
            throw WebSocketExceptionCodes.INVALID_PATH_FILTER.create(pathFilter);
        }

        if (localApp.existsAny(pathFilter, userId, contextId)) {
            return true;
        }

        return remoteDistributor.existsAnyRemote(pathFilter, userId, contextId);
    }

    @Override
    public void sendMessage(String message, String sourceToken, int userId, int contextId) throws OXException {
        sendMessage(message, sourceToken, null, userId, contextId);
    }

    // -------------------------------------------------------------------------------------------------------------

    @Override
    public void sendMessage(String message, String sourceToken, String pathFilter, int userId, int contextId) throws OXException {
        if (false == WebSockets.validatePath(pathFilter)) {
            throw WebSocketExceptionCodes.INVALID_PATH_FILTER.create(pathFilter);
        }

        localApp.sendToUser(message, sourceToken, pathFilter, false, userId, contextId);
        remoteDistributor.sendRemote(message, pathFilter, userId, contextId);
    }

    // -------------------------------------------------------------------------------------------------------------

    @Override
    public List<WebSocketInfo> listClusterWebSocketInfo() throws OXException {
        List<WebSocketInfo> infos = remoteDistributor.listClusterWebSocketInfo();
        if (null == infos) {
            // Only locally available...
            infos = localApp.listWebSocketInfo();
        }
        return infos;
    }

    @Override
    public List<WebSocket> listLocalWebSockets() throws OXException {
        return localApp.listLocalWebSockets();
    }

    @Override
    public long getNumberOfBufferedMessages() throws OXException {
        return remoteDistributor.getNumberOfBufferedMessages();
    }

    @Override
    public void closeWebSockets(int userId, int contextId, String pathFilter) throws OXException {
        if (false == WebSockets.validatePath(pathFilter)) {
            throw WebSocketExceptionCodes.INVALID_PATH_FILTER.create(pathFilter);
        }

        localApp.closeWebSockets(userId, contextId, pathFilter);
    }

    @Override
    public long getNumberOfWebSockets() throws OXException {
        return localApp.getNumberOfWebSockets();
    }

}
