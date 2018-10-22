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

import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.net.IPTools;
import com.openexchange.session.Session;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.grizzly.http.WebsocketServletRequestWrapper;

/**
 * {@link SessionBoundWebSocket} - The Web Socket bound to a certain session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SessionBoundWebSocket extends DefaultWebSocket {

    private static final Logger LOG = LoggerFactory.getLogger(SessionBoundWebSocket.class);

    private final AtomicReference<SessionInfo> sessionInfoReference;
    private final ConnectionId connectionId;
    private final String path;
    private final HttpServletRequest wrappedRequest;
    private final GrizzlyConfig config;

    /**
     * Initializes a new {@link SessionBoundWebSocket}.
     */
    public SessionBoundWebSocket(ConnectionId connectionId, String path, ProtocolHandler protocolHandler, HttpRequestPacket request, GrizzlyConfig config, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        this.sessionInfoReference = new AtomicReference<SessionInfo>(null);
        this.connectionId = connectionId;
        this.path = path;
        this.config = config;
        this.wrappedRequest = buildHttpServletRequestWrapper(this.servletRequest);
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
    public HttpServletRequest getHttpRequest() {
        return wrappedRequest;
    }


    private WebsocketServletRequestWrapper buildHttpServletRequestWrapper(HttpServletRequest httpRequest) {
        if (!config.isConsiderXForwards()) {
            return new WebsocketServletRequestWrapper(httpRequest);
        }

        // Determine remote IP address
        String forHeaderValue = httpRequest.getHeader(config.getForHeader());
        String remoteAddress = IPTools.getRemoteIP(forHeaderValue, config.getKnownProxies());
        if (null == remoteAddress) {
            LOG.debug("Could not detect a valid remote IP address in {}: [{}], falling back to default", config.getForHeader(), forHeaderValue == null ? "" : forHeaderValue);
            remoteAddress = httpRequest.getRemoteAddr();
        }

        // Determine protocol/scheme of the incoming request
        String protocol = httpRequest.getHeader(config.getProtocolHeader());
        if (!isValidProtocol(protocol)) {
            LOG.debug("Could not detect a valid protocol header value in {}, falling back to default", protocol);
            protocol = httpRequest.getScheme();
        }

        return new WebsocketServletRequestWrapper(protocol, remoteAddress, httpRequest.getServerPort(), httpRequest);
    }

    private boolean isValidProtocol(String protocolHeaderValue) {
        return WebsocketServletRequestWrapper.WS_SCHEME.equals(protocolHeaderValue) || WebsocketServletRequestWrapper.WSS_SCHEME.equals(protocolHeaderValue);
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
     * Sets the basic information extracted from given session.
     *
     * @param session The session to extract from
     */
    public void setSessionInfo(Session session) {
        this.sessionInfoReference.set(SessionInfo.newInstance(session));
    }

    /**
     * Sets the basic information for the session currently associated with this Web Socket.
     *
     * @param sessionInfo The session information to set
     */
    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfoReference.set(sessionInfo);
    }

    /**
     * Gets the basic information for the session currently associated with this Web Socket.
     *
     * @return The session information
     */
    public SessionInfo getSessionInfo() {
        return sessionInfoReference.get();
    }

    /**
     * Gets the identifier of the session currently associated with this Web Socket.
     *
     * @return The session identifier
     */
    public String getSessionId() {
        SessionInfo sessionInfo = sessionInfoReference.get();
        return sessionInfo.getSessionId();
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        SessionInfo sessionInfo = sessionInfoReference.get();
        return sessionInfo.getUserId();
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        SessionInfo sessionInfo = sessionInfoReference.get();
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
        SessionInfo sessionInfo = sessionInfoReference.get();
        builder.append("userId=").append(sessionInfo.getUserId());
        builder.append(", contextId=").append(sessionInfo.getContextId());
        String sessionId = sessionInfo.getSessionId();
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
