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

package com.openexchange.websockets.monitoring;

import java.util.List;
import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;


/**
 * {@link WebSocketMBean} - The MBean for Web Sockets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketMBean {

    /** The MBean's domain */
    public static final String DOMAIN = "com.openexchange.websockets";

    /**
     * Gets the number of open Web Sockets on this node
     *
     * @return The number of open Web Sockets
     * @throws MBeanException If number of open Web Sockets cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of open Web Sockets on this node", parameters={}, parameterDescriptions={})
    long getNumberOfWebSockets() throws MBeanException;

    /**
     * Gets the number of buffered messages that are supposed to be sent to remote cluster members.
     *
     * @return The number of buffered messages
     * @throws MBeanException If number of buffered messages cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of buffered messages that are supposed to be sent to remote cluster members.", parameters={}, parameterDescriptions={})
    long getNumberOfBufferedMessages() throws MBeanException;

    /**
     * Lists all available Web Socket information from whole cluster.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;"><b>Expensive operation!</b></div>
     * <p>
     *
     * @return All available Web Socket information
     * @throws MBeanException If Web Socket information cannot be returned
     */
    @MBeanMethodAnnotation (description="Lists all available Web Socket information from whole cluster; each row provides context identifier, user identifier, member address/port, the path used when the socket was created, and connection identifier", parameters={}, parameterDescriptions={})
    List<List<String>> listClusterWebSocketInfo() throws MBeanException;

    /**
     * Lists Web Sockets opened on this node; each row provides:
     * <ul>
     * <li>context identifier,
     * <li>user identifier,
     * <li>the path used when the socket was created and
     * <li>connection identifier
     * </ul>
     *
     * @return The Web Sockets opened on this node
     * @throws MBeanException If Web Sockets cannot be returned
     */
    @MBeanMethodAnnotation (description="Lists Web Sockets opened on this node; each row provides context identifier, user identifier, the path used when the socket was created, and connection identifier", parameters={}, parameterDescriptions={})
    List<List<String>> listWebSockets() throws MBeanException;

    /**
     * Closes all locally available Web Sockets matching specified path filter expression (if any).
     * <p>
     * In case no path filter expression is given (<code>pathFilter == null</code>), all user-associated Web Sockets are closed.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param pathFilter The optional path filter expression or <code>null</code>
     * @throws MBeanException If closing Web Sockets fails
     */
    @MBeanMethodAnnotation (description="Closes all locally available Web Sockets matching specified path filter expression (if any).", parameters={"userId", "contextId", "pathFilter"}, parameterDescriptions={"The user identifier", "The context identifier", "The optional path filter expression; e.g. \"/socket.io/*\""})
    void closeWebSockets(int userId, int contextId, String pathFilter) throws MBeanException;

}
