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

import java.util.Map;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.Parameters;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.MessageTranscoder;
import com.openexchange.websockets.SendControl;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSocketSession;
import com.openexchange.websockets.grizzly.CompletedSendControl;
import com.openexchange.websockets.grizzly.FutureBackedSendControl;
import com.openexchange.websockets.grizzly.SessionBoundWebSocket;

/**
 * {@link DefaultSessionBoundWebSocket} - The Web Socket bound to a certain session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultSessionBoundWebSocket extends SessionBoundWebSocket implements WebSocket {

    private final WebSocketSession webSocketSession;
    private final Parameters parameters;
    private volatile Map<String, String> paramsMap;
    private volatile MessageTranscoder transcoder;

    /**
     * Initializes a new {@link DefaultSessionBoundWebSocket}.
     */
    public DefaultSessionBoundWebSocket(ConnectionId connectionId, String path, Parameters parameters, ProtocolHandler protocolHandler, HttpRequestPacket request, GrizzlyConfig config, WebSocketListener... listeners) {
        super(connectionId, path, protocolHandler, request, config, listeners);
        this.parameters = parameters;
        webSocketSession = new WebSocketSessionImpl();
    }

    /**
     * Gets the Web Socket session
     *
     * @return The Web Socket session
     */
    @Override
    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    /**
     * Gets the available query parameters associated while this Web Socket was created; e.g. <code>"param1=foo&amp;param2=bar"</code>.
     *
     * @return The parameters
     */
    @Override
    public Map<String, String> getParameters() {
        Map<String, String> tmp = this.paramsMap;
        if (null == tmp) {
            // May be concurrently initialized
            Builder<String, String> builder = ImmutableMap.builder();
            for (String parameterName : parameters.getParameterNames()) {
                builder.put(parameterName, parameters.getParameter(parameterName));
            }
            tmp = builder.build();
            this.paramsMap = tmp;
        }
        return tmp;
    }

    /**
     * Gets the value for the denoted query parameter.
     *
     * @param parameterName The parameter name
     * @return The parameters value or <code>null</code> (if no such parameter was available while this Web Socket was created)
     */
    @Override
    public String getParameter(String parameterName) {
        return null == parameterName ? null : parameters.getParameter(parameterName);
    }

    /**
     * Applies a certain message transcoder to this Web Socket.
     * <p>
     * Every inbound and outbound messages are routed through that transcoder.
     *
     * @param transcoder The transcode to set
     */
    @Override
    public void setMessageTranscoder(MessageTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    /**
     * Gets the scheme identifier for the currently active message transcoder.
     *
     * @return The scheme identifier or <code>null</code> if no trancoder is in place
     */
    @Override
    public String getMessageTranscoderScheme() {
        MessageTranscoder transcoder = this.transcoder;
        return null == transcoder ? null : transcoder.getId();
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

    // ---------------------------------------------------------- Outbound ---------------------------------------------------------------

    @Override
    public SendControl sendMessage(String message) throws OXException {
        if (false == isConnected()) {
            throw WebSocketExceptionCodes.NOT_CONNECTED.create();
        }

        MessageTranscoder transcoder = this.transcoder;

        String transcoded = null == transcoder ? message : transcoder.onOutboundMessage(this, message);
        if (null != transcoded) {
            return doSend(transcoded);
        }

        // Don't know better...
        return CompletedSendControl.getInstance();
    }

    @Override
    public SendControl sendMessageRaw(String message) throws OXException {
        if (false == isConnected()) {
            throw WebSocketExceptionCodes.NOT_CONNECTED.create();
        }

        return doSend(message);
    }

    /**
     * Sends specified text message.
     *
     * @param message The text message to send
     * @return The send control
     */
    protected SendControl doSend(String message) {
        // Yield data-frame for given text message
        ProtocolHandler protocolHandler = this.protocolHandler;
        DataFrame frameToSend = protocolHandler.toDataFrame(message);
        frameToSend.getBytes();

        // Perform the send
        return new FutureBackedSendControl(protocolHandler.send(frameToSend));
    }

    // ---------------------------------------------------------- Inbound ----------------------------------------------------------------

    @Override
    public void onMessage(String text) {
        MessageTranscoder transcoder = this.transcoder;

        String transcoded = null == transcoder ? text : transcoder.onInboundMessage(this, text);
        if (null != transcoded) {
            super.onMessage(transcoded);
        }
    }

    @Override
    public void onMessage(byte[] data) {
        super.onMessage(data);
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
        ConnectionId connectionId = getConnectionId();
        if (connectionId != null) {
            builder.append(", connectionId=").append(connectionId);
        }
        String path = getPath();
        if (path != null) {
            builder.append(", path=").append(path);
        }
        Map<String, String> parameters = getParameters();
        if (null != parameters) {
            builder.append(", parameters=").append(parameters);
        }
        if (webSocketSession != null) {
            builder.append(", webSocketSession=").append(webSocketSession);
        }
        builder.append("}");
        return builder.toString();
    }

}
