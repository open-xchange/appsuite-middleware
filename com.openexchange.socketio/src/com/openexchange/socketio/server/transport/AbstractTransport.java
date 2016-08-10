/**
 * The MIT License
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import com.openexchange.socketio.server.Config;
import com.openexchange.socketio.server.ServletBasedConfig;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.server.Transport;
import com.openexchange.socketio.server.TransportConnection;

/**
 * @author Alexander Sova (bird@codeminders.com)
 * @author Mathieu Carbou
 */
public abstract class AbstractTransport implements Transport {

    private volatile Config config;

    /**
     * Initializes a new {@link AbstractTransport}.
     */
    protected AbstractTransport() {
        super();
    }

    @Override
    public void destroy() {
        // Nothing...
    }

    @Override
    public void init(ServletConfig config, ServletContext context) throws ServletException {
        this.config = new ServletBasedConfig(config, getType().toString());
    }

    protected final Config getConfig() {
        return config;
    }

    protected TransportConnection createConnection(Session session) {
        TransportConnection connection = createConnection();
        connection.setSession(session);
        connection.init(getConfig());
        return connection;
    }

    protected TransportConnection getConnection(String sessionId, SocketIOManager sessionManager) {
        Session session = null;
        if (sessionId != null && sessionId.length() > 0) {
            session = sessionManager.getSession(sessionId);
        }

        if(session == null) {
            return createConnection(sessionManager.createSession());
        }

        TransportConnection activeConnection = session.getConnection();

        if(activeConnection.getTransport() == this) {
            return activeConnection;
        }

        // this is new connection considered for an upgrade
        return createConnection(session);
    }

    @Override
    public String toString() {
        return getType().toString();
    }

}
