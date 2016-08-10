/**
 * The MIT License
 * Copyright (c) 2015
 *
 * Contributors: Tad Glines, Ovea.com, Mycila.com, Alexander Sova (bird@codeminders.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.openexchange.socketio.server.transport;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import com.openexchange.socketio.protocol.EngineIOProtocol;
import com.openexchange.socketio.server.Session;
import com.openexchange.socketio.server.SocketIOProtocolException;
import com.openexchange.socketio.server.TransportType;

public abstract class JSONPPollingTransport extends AbstractHttpTransport {

    private static final String EIO_PREFIX = "___eio";
    private static final String FRAME_ID = JSONPPollingTransport.class.getName() + ".FRAME_ID";

    protected JSONPPollingTransport() {
        super();
    }

    @Override
    public TransportType getType() {
        return TransportType.JSONP_POLLING;
    }

    public void startSend(Session session, ServletResponse response) throws IOException {
        response.setContentType("text/javascript; charset=UTF-8");
    }

    public void writeData(Session session, ServletResponse response, String data) throws IOException {
        response.getOutputStream().print(EIO_PREFIX);
        response.getOutputStream().print("[" + session.getAttribute(FRAME_ID) + "]('");
        response.getOutputStream().print(data); //TODO: encode data?
        response.getOutputStream().print("');");
    }

    public void finishSend(Session session, ServletResponse response) throws IOException {
        response.flushBuffer();
    }

    public void onConnect(Session session, ServletRequest request, ServletResponse response) throws IOException {
        try {
            //TODO: Use string constant for request parameter name "j"
            //TODO: Do we really need to enforce "j" to be an integer?
            session.setAttribute(FRAME_ID, Integer.parseInt(request.getParameter(EngineIOProtocol.JSONP_INDEX)));
        } catch (NullPointerException | NumberFormatException e) {
            throw new SocketIOProtocolException("Missing or invalid 'j' parameter. It suppose to be integer");
        }

        startSend(session, response);
    }
}
