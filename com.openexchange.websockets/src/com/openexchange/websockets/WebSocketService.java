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

package com.openexchange.websockets;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link WebSocketService} - The service for sending/receiving data to/from established Web Socket connections.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
@SingletonService
public interface WebSocketService {

    /**
     * Checks if there is any open Web Socket connection associated with specified user; either locally or on any remote cluster member.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if there is an open Web Socket; otherwise <code>false</code>
     * @throws OXException If availability check fails
     */
    boolean exists(int userId, int contextId) throws OXException;

    /**
     * Checks if there is any filter-satisfying Web Socket connection associated with specified user on a remote cluster member.
     *
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>) or <code>null</code> to not filter at all
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a filter-satisfying Web Socket exists; otherwise <code>false</code>
     * @throws OXException If availability check fails
     */
    boolean exists(String pathFilter, int userId, int contextId) throws OXException;

    // --------------------------------------------------------------------------------------------------------------

    /**
     * Sends a text message to denoted user's remote end-points, blocking until all of the message has been transmitted. End-points whose
     * connection identifier matches the optional source token are excluded implicitly.
     *
     * @param message The message to be sent
     * @param sourceToken The push token of the client triggering the update, or <code>null</code> if not available
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The handler which will be notified of progress.
     * @throws OXException If there is a problem delivering the message.
     */
    void sendMessage(String message, String sourceToken, int userId, int contextId) throws OXException;

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Sends a text message to those end-points of the denoted user having its {@link WebSocket#getPath() path} starting with given filter. 
     * End-points whose connection identifier matches the optional source token are excluded implicitly.
     * <p>
     * This method blocks until all of the message has been transmitted.
     *
     * <pre>
     * Examples:
     *   Filter "/websockets/push"
     *    matches:
     *     "/websockets/push"
     *    does not match:
     *     "/websockets/push/foo"
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "/websockets/push/*"
     *    matches:
     *     "/websockets/push"
     *     "/websockets/push/foo"
     *    does not match:
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "*"
     *    matches all
     * </pre>
     *
     * @param message The message to be sent
     * @param sourceToken The push token of the client triggering the update, or <code>null</code> if not available
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>); if <code>null</code> it is the same behavior as {@link #sendMessage(String, int, int)}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The handler which will be notified of progress
     * @throws OXException If there is a problem delivering the message.
     */
    void sendMessage(String message, String sourceToken, String pathFilter, int userId, int contextId) throws OXException;

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Gets the number of open Web Sockets on this node
     *
     * @return The number of open Web Sockets
     * @throws OXException If number of open Web Sockets cannot be returned
     */
    long getNumberOfWebSockets() throws OXException;

    /**
     * Gets the number of buffered messages that are supposed to be sent to remote cluster members.
     *
     * @return The number of buffered messages
     * @throws OXException If number of buffered messages cannot be returned
     */
    long getNumberOfBufferedMessages() throws OXException;

    /**
     * Lists all currently locally available Web Sockets.
     *
     * @return Locally available Web Sockets
     * @throws OXException If Web Sockets cannot be returned
     */
    List<WebSocket> listLocalWebSockets() throws OXException;

    /**
     * Lists all available Web Socket information from whole cluster.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;"><b>Expensive operation!</b></div>
     * <p>
     *
     * @return All available Web Socket information
     * @throws OXException If Web Socket information cannot be returned
     */
    List<WebSocketInfo> listClusterWebSocketInfo() throws OXException;

    /**
     * Closes all locally available Web Sockets matching specified path filter expression (if any).
     * <p>
     * In case no path filter expression is given (<code>pathFilter == null</code>), all user-associated Web Sockets are closed.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param pathFilter The optional path filter expression or <code>null</code>
     * @throws OXException If closing Web Sockets fails
     */
    void closeWebSockets(int userId, int contextId, String pathFilter) throws OXException;

}
