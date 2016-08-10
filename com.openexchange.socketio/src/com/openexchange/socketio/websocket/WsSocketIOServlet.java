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
package com.openexchange.socketio.websocket;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.socketio.server.SocketIOServlet;
import com.openexchange.socketio.server.TransportProvider;
import com.openexchange.timer.TimerService;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WsSocketIOServlet extends SocketIOServlet {

    private static final long serialVersionUID = 4367242338228401621L;

    private final WsTransportConnectionRegistry connectionRegistry;

    /**
     * Initializes a new {@link WsSocketIOServlet}.
     */
    public WsSocketIOServlet(WsTransportConnectionRegistry connectionRegistry, TimerService timerService) {
        super(timerService);
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        TransportProvider transportProvider = new WsTransportProvider(connectionRegistry);
        transportProvider.init(config, getServletContext());
        setTransportProvider(transportProvider);
    }

    @Override
    protected void serve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.serve(request, response);
    }

}
