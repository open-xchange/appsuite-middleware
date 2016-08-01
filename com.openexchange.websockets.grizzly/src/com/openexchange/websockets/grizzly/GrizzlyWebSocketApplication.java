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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.websockets.grizzly;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolError;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;
import com.openexchange.session.Session;


/**
 * {@link GrizzlyWebSocketApplication}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

    /**
     * Initializes a new {@link GrizzlyWebSocketApplication}.
     */
    public GrizzlyWebSocketApplication() {
        super();
    }

    /**
     * Gets a listing of identifiers for sessions currently bound to an active Web Socket connection.
     *
     * @return The session identifier listing
     */
    public Map<String, WebSocket> getActiveSessions() {
        Map<String, WebSocket> sessions = new HashMap<>(32, 0.9F);
        Set<WebSocket> webSockets = getWebSockets();
        for (WebSocket socket : webSockets) {
            if (socket instanceof SessionBoundWebSocket) {
                String sessionId = ((SessionBoundWebSocket) socket).getSessionId();
                if (null != sessionId) {
                    sessions.put(sessionId, socket);
                }
            }
        }
        return sessions;
    }

    /**
     * Closes specified Web Socket connection.
     *
     * @param socket The Web Socket
     */
    public void close(WebSocket socket) {
        remove(socket);
        socket.close();
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        return new SessionBoundWebSocket(handler, requestPacket, listeners);
    }

    @Override
    public void onConnect(WebSocket socket) {
        // Override this method to take control over socket collection
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        super.onClose(socket, frame);
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        if (!(socket instanceof SessionBoundWebSocket)) {
            throw new ProtocolError("Invalid socket: " + (null == socket ? "null" : socket.getClass().getName()));
        }

        SessionBoundWebSocket sessionBoundWebSocket = (SessionBoundWebSocket) socket;
        if (isLoginMessage(message)) {
            Session session = login(message);
            if (null == session || false == sessionBoundWebSocket.bindSession(session) || false == add(sessionBoundWebSocket)) {
                throw new ProtocolError("Invalid login message");
            }
        } else {
            String sessionId = sessionBoundWebSocket.getSessionId();
            if (null == sessionId) {
                throw new ProtocolError("Not logged-in");
            }

            // Hm... No broadcast support in current Grizzly version
        }
    }

    /**
     * Performs a login using given login message.
     *
     * @param message The login message
     * @return The resulting session or <code>null</code>
     */
    private Session login(String loginMessage) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Checks if specified message attempts to perform a login
     *
     * @param message The message to examine
     * @return <code>true</code> if message attempts to perform a login; otherwise <code>false</code>
     */
    private boolean isLoginMessage(String message) {
        return false;
    }

}
