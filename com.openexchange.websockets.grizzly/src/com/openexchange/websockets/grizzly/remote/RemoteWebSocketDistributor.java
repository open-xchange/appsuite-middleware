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

import java.util.Collection;
import java.util.List;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketInfo;

/**
 * {@link RemoteWebSocketDistributor} - Sends text messages to remote nodes having an open Web Socket connection for associated user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface RemoteWebSocketDistributor  {

    /**
     * Lists all available Web Socket information from whole cluster.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;"><b>Expensive operation!</b></div>
     * <p>
     *
     * @return All available Web Socket information or <code>null</code> if operation failed
     */
    List<WebSocketInfo> listClusterWebSocketInfo();

    /**
     * Gets the number of buffered messages that are supposed to be sent to remote cluster members.
     *
     * @return The number of buffered messages
     */
    long getNumberOfBufferedMessages();

    /**
     * Sends the given text message to remote nodes having an open Web Socket connection for specified user.
     *
     * @param message The text message to send
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void sendRemote(String message, String pathFilter, int userId, int contextId);

    /**
     * Checks if there is any filter-satisfying Web Socket connection associated with specified user on a remote cluster member.
     *
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a filter-satisfying Web Socket exists; otherwise <code>false</code>
     */
    boolean existsAnyRemote(String pathFilter, int userId, int contextId);

    /**
     * Adds a connected Web Socket.
     *
     * @param socket The Web Socket
     */
    void addWebSocket(WebSocket socket);

    /**
     * Adds a connected Web Socket.
     *
     * @param socketInfo The Web Socket info
     */
    void addWebSocket(WebSocketInfo socketInfo);

    /**
     * Adds connected Web Sockets.
     *
     * @param sockets The Web Sockets
     */
    void addWebSocket(Collection<WebSocket> sockets);

    /**
     * Removes a closed Web Socket.
     *
     * @param socket The Web Socket
     */
    void removeWebSocket(WebSocket socket);

    /**
     * Removes a closed Web Socket.
     *
     * @param socketInfo The Web Socket info
     */
    void removeWebSocket(WebSocketInfo socketInfo);

    /**
     * Starts the cleaner task for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void startCleanerTaskFor(int userId, int contextId);

    /**
     * Stops the cleaner task for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void stopCleanerTaskFor(int userId, int contextId);

}
