
package com.openexchange.ajp13;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface AJPv13Connection {

    public static final int IDLE_STATE = 1;

    public static final int ASSIGNED_STATE = 2;

    /**
     * Gets the associated AJP request handler which processes the AJP data sent over this connection
     * 
     * @return The associated AJP request handler.
     */
    public AJPv13RequestHandler getAjpRequestHandler();

    /**
     * Gets the input stream from AJP client
     * 
     * @return The input stream from AJP client
     * @throws IOException If input stream cannot be returned
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Gets the output stream to AJP client
     * 
     * @return The output stream to AJP client
     * @throws IOException If output stream cannot be returned
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Sets both input and output stream to <code>null</code> and closes associated socket.
     */
    public void discardAll();
}
