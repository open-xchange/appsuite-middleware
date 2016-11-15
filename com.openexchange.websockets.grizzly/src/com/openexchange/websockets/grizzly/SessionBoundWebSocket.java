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

package com.openexchange.websockets.grizzly;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import com.openexchange.websockets.ConnectionId;

/**
 * {@link SessionBoundWebSocket} - The Web Socket bound to a certain session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SessionBoundWebSocket extends DefaultWebSocket {

    private final SessionInfo sessionInfo;
    private final ConnectionId connectionId;
    private final String path;

    /**
     * Initializes a new {@link SessionBoundWebSocket}.
     */
    public SessionBoundWebSocket(SessionInfo sessionInfo, ConnectionId connectionId, String path, ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        this.sessionInfo = sessionInfo;
        this.connectionId = connectionId;
        this.path = path;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the associated protocol handler on socket creation
     *
     * @return The protocol handler
     */
    public ProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    /**
     * Gets the associated HTTP request on socket creation
     *
     * @return The HTTP request
     */
    public HttpRequestPacket getRequest() {
        return request;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the connection identifier
     *
     * @return The connection identifier
     */
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the basic information for the session currently associated with this Web Socket.
     *
     * @return The session information
     */
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    /**
     * Gets the identifier of the session currently associated with this Web Socket.
     *
     * @return The session identifier
     */
    public String getSessionId() {
        return sessionInfo.getSessionId();
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return sessionInfo.getUserId();
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return sessionInfo.getContextId();
    }

    /**
     * Gets the path that was used while this Web Socket was created; e.g. <code>"/websockets/foo/bar"</code>.
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

    @Override
    public void onConnect() {
        try {
            super.onConnect();
        } catch (HandshakeException e) {
            throw e;
        } catch (Exception e) {
            HandshakeException hndshkExc = new HandshakeException(e.getMessage());
            hndshkExc.initCause(e);
            throw hndshkExc;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("{");
        builder.append("userId=").append(getUserId());
        builder.append(", contextId=").append(getContextId());
        String sessionId = getSessionId();
        if (sessionId != null) {
            builder.append(", sessionId=").append(sessionId);
        }
        if (connectionId != null) {
            builder.append(", connectionId=").append(connectionId);
        }
        builder.append("}");
        return builder.toString();
    }

}
