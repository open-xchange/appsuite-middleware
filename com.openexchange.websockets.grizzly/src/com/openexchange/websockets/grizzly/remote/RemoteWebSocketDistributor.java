/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
     * @param async Whether to send asynchronously or with blocking behavior
     */
    void sendRemote(String message, String pathFilter, int userId, int contextId, boolean async);

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
