
package com.openexchange.ajp13;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;

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
     * Set whether to enable synchronized access to input stream or not.
     * 
     * @param synchronize <code>true</code> to enable synchronized access; otherwise <code>false</code>
     */
    public void synchronizeInputStream(boolean synchronize);

    /**
     * Gets the output stream to AJP client
     * 
     * @return The output stream to AJP client
     * @throws IOException If output stream cannot be returned
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Set whether to enable synchronized access to output stream or not.
     * 
     * @param synchronize <code>true</code> to enable synchronized access; otherwise <code>false</code>
     */
    public void synchronizeOutputStream(boolean synchronize);

    /**
     * Sets both input and output stream to <code>null</code> and closes associated socket.
     */
    public void discardAll();

    /**
     * Sets the SO_TIMEOUT with the specified timeout, in milliseconds.
     * 
     * @param millis The timeout in milliseconds
     * @throws AJPv13Exception If there is an error in the underlying protocol, such as a TCP error.
     */
    public void setSoTimeout(final int millis) throws AJPv13Exception;

    /**
     * Gets the number of current AJP package.
     * 
     * @return The number of current AJP package.
     */
    public int getPackageNumber();

    /**
     * Gets the current AJP connection's state
     * 
     * @return Current AJP connection's state
     */
    public int getState();
}
