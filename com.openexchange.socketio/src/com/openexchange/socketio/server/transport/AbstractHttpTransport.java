/**
 * The MIT License
 * Copyright (c) 2010 Tad Glines
 * <p/>
 * Contributors: Tad Glines, Ovea.com, Mycila.com, Alexander Sova (bird@codeminders.com)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.openexchange.socketio.server.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.socketio.common.ConnectionState;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.server.Config;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.server.TransportConnection;
import com.openexchange.socketio.server.TransportType;

public abstract class AbstractHttpTransport extends AbstractTransport {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractHttpTransport.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, SocketIOManager socketIOManager) throws IOException {
        LOGGER.debug("Handling {} request by {}", request.getMethod(), getClass().getName());

        TransportConnection connection = getConnection(request.getParameter(EngineIOProtocol.SESSION_ID), socketIOManager);
        Session session = connection.getSession();

        if (session.getConnectionState() == ConnectionState.CONNECTING) {

            List<String> upgrades = new ArrayList<>(2);
            if (socketIOManager.getTransportProvider().getTransport(TransportType.WEB_SOCKET) != null) {
                upgrades.add("websocket");
            }

            connection.send(EngineIOProtocol.createHandshakePacket(session.getSessionId(), upgrades.toArray(new String[upgrades.size()]), getConfig().getPingInterval(Config.DEFAULT_PING_INTERVAL), getConfig().getTimeout(Config.DEFAULT_PING_TIMEOUT)));

            // response.addCookie(new Cookie("io", session.getSessionId()));
            connection.handle(request, response); // called to send the handshake packet
            session.onConnect(connection);
        } else if (session.getConnectionState() == ConnectionState.CONNECTED) {
            connection.handle(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_GONE, "Socket.IO session is closed");
        }
    }
}
