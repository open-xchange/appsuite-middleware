
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
     * Set whether to enable blocking access to input stream or not.
     * 
     * @param block <code>true</code> to enable blocking access; otherwise <code>false</code>
     */
    public void blockInputStream(boolean block);

    /**
     * Gets the output stream to AJP client
     * 
     * @return The output stream to AJP client
     * @throws IOException If output stream cannot be returned
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Set whether to enable blocking access to output stream or not.
     * 
     * @param block <code>true</code> to enable blocking access; otherwise <code>false</code>
     */
    public void blockOutputStream(boolean block);

    /**
     * Closes this AJP connection.<br>
     * Closes both input and output stream and associated socket as well.
     */
    public void close();

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
