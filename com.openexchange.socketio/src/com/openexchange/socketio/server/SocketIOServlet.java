/**
 * The MIT License
 * Copyright (c) 2010 Tad Glines
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
 * <p/>
 * Contributors: Ovea.com, Mycila.com
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

package com.openexchange.socketio.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.io.ByteStreams;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.protocol.SocketIOProtocol;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;

public abstract class SocketIOServlet extends SessionServlet {

    private static final long serialVersionUID = 3773065767366606255L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SocketIOServlet.class);

    private final SocketIOManager socketIOManager;

    /**
     * Initializes a new {@link SocketIOServlet}.
     */
    protected SocketIOServlet(TimerService timerService) {
        super();
        socketIOManager = new SocketIOManager(timerService);
    }

    /**
     * Gets the Socket.IO manager
     *
     * @return The Socket.IO manager
     */
    public SocketIOManager getSocketIOManager() {
        return socketIOManager;
    }

    /**
     * Initializes and retrieves the given Namespace by its pathname identifier {@code id}.
     *
     * If the namespace was already initialized it returns it right away.
     *
     * @param id namespace id
     * @return namespace object
     */
    public Namespace of(String id) {
        return namespace(id);
    }

    /**
     * Initializes and retrieves the given Namespace by its pathname identifier {@code id}.
     *
     * If the namespace was already initialized it returns it right away.
     *
     * @param id namespace id
     * @return namespace object
     */
    public Namespace namespace(String id) {
        Namespace ns = socketIOManager.getNamespace(id);
        if (ns == null) {
            ns = socketIOManager.createNamespace(id);
        }

        return ns;
    }

    /**
     * Sets the transport provider.
     *
     * @param transportProvider The provider to set
     */
    protected void setTransportProvider(TransportProvider transportProvider) {
        socketIOManager.setTransportProvider(transportProvider);
    }

    @Override
    public void init() throws ServletException {
        of(SocketIOProtocol.DEFAULT_NAMESPACE);

        LOGGER.info("Socket.IO server stated.");
    }

    @Override
    public void destroy() {
        socketIOManager.getTransportProvider().destroy();
        super.destroy();
    }

    @Override
    protected void doService(HttpServletRequest req, HttpServletResponse resp, boolean checkRateLimit) throws ServletException, IOException {
        if (resp.isCommitted()) {
            return;
        }
        super.doService(req, resp, checkRateLimit);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.startsWith("socket.io.js")) {
            resp.setContentType("text/javascript");
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("socket.io.js");
            OutputStream os = resp.getOutputStream();
            ByteStreams.copy(is, os);
            return;
        }

        serve(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serve(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serve(req, resp);
    }

    /**
     * Serves given HTTP request
     *
     * @param request The HTTP request
     * @param response The associated HTTP response
     * @throws ServletException If serving fails
     * @throws IOException If an I/O error occurs
     */
    protected void serve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServerSession session = SessionUtility.getSessionObject(request, false);
        if (session == null) {
            LOGGER.warn("No session available");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No session available");
            return;
        }

        TransportProvider transportProvider = socketIOManager.getTransportProvider();
        if (null == transportProvider) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            LOGGER.debug("Request from {}:{}, transport: {}, EIO protocol version:{}", request.getRemoteHost(), request.getRemotePort(), request.getParameter(EngineIOProtocol.TRANSPORT), request.getParameter(EngineIOProtocol.VERSION));

            transportProvider.getTransport(request).handle(request, response, socketIOManager);
        } catch (UnsupportedTransportException | SocketIOProtocolException e) {
            LOGGER.warn("Socket IO error", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
