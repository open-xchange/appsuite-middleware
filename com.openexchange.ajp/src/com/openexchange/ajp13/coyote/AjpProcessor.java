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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote;

import static com.openexchange.ajp13.AJPv13Response.writeHeaderSafe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.coyote.util.ByteChunk;
import com.openexchange.ajp13.coyote.util.HexUtils;
import com.openexchange.ajp13.coyote.util.MessageBytes;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.servlet.http.HttpErrorServlet;
import com.openexchange.ajp13.servlet.http.HttpServletManager;
import com.openexchange.ajp13.servlet.http.HttpSessionManagement;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.log.Log;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AjpProcessor}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AjpProcessor {

    private static final org.apache.commons.logging.Log log = Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AjpProcessor.class));

    private static final boolean DEBUG = log.isDebugEnabled();

    private static final int STAGE_PARSE = 1;

    private static final int STAGE_PREPARE = 2;

    private static final int STAGE_SERVICE = 4;

    private static final int STAGE_KEEPALIVE = 8;

    private static final int STAGE_ENDED = 16;

    /**
     * The current processor stage.
     */
    private int stage;

    /**
     * Associated servlet.
     */
    private HttpServlet servlet = null;

    /**
     * Request object.
     */
    private final HttpServletRequestImpl request;

    /**
     * Response object.
     */
    protected final HttpServletResponseImpl response;

    /**
     * The socket timeout used when reading the first block of the request header.
     */
    protected final int packetSize;

    /**
     * Header message. Note that this header is merely the one used during the processing of the first message of a "request", so it might
     * not be a request header. It will stay unchanged during the processing of the whole request.
     */
    private final AjpMessage requestHeaderMessage;

    /**
     * Message used for response header composition.
     */
    protected final AjpMessage responseHeaderMessage;

    /**
     * Body message.
     */
    private final AjpMessage bodyMessage;

    /**
     * Body message.
     */
    protected final MessageBytes bodyBytes = MessageBytes.newInstance();

    /**
     * State flag.
     */
    private boolean started = false;

    /**
     * Error flag.
     */
    protected boolean error = false;

    /**
     * Socket associated with the current connection.
     */
    private Socket socket;

    /**
     * Input stream.
     */
    private InputStream input;

    /**
     * Output stream.
     */
    protected OutputStream output;

    /**
     * The socket timeout used when reading the first block of the request header.
     */
    private long readTimeout;

    /**
     * The byte sink used for processing.
     */
    private final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(Constants.MAX_PACKET_SIZE);

    /**
     * Byte chunk for certs.
     */
    private final MessageBytes certificates = MessageBytes.newInstance();

    /**
     * End of stream flag.
     */
    protected boolean endOfStream = false;

    /**
     * Body empty flag.
     */
    protected boolean empty = true;

    /**
     * First read.
     */
    protected boolean first = true;

    /**
     * Replay read.
     */
    private boolean replay = false;

    /**
     * Finished response.
     */
    private boolean finished = false;

    /**
     * The HTTP session (JSESSIONID) cookie.
     */
    private Cookie httpSessionCookie;

    /**
     * Whether client joined an existing HTTP session.
     */
    private boolean httpSessionJoined;

    /**
     * The identifier of the currently associated servlet.
     */
    private final StringBuilder servletId;

    /**
     * The servlet path (which is not the request path). The servlet path is defined in servlet mapping configuration.
     */
    private String servletPath;

    /**
     * Direct buffer used for sending right away a get body message.
     */
    private final byte[] getBodyMessageArray;

    /**
     * Direct buffer used for sending right away a pong message.
     */
    private static final byte[] pongMessageArray;

    /**
     * End message array.
     */
    private static final byte[] endMessageArray;

    /**
     * Flush message array.
     */
    private static final byte[] flushMessageArray;

    // ----------------------------------------------------- Static Initializer

    static {

        // Set the read body message buffer
        final AjpMessage pongMessage = new AjpMessage(16);
        pongMessage.reset();
        pongMessage.appendByte(Constants.JK_AJP13_CPONG_REPLY);
        pongMessage.end();
        pongMessageArray = new byte[pongMessage.getLen()];
        System.arraycopy(pongMessage.getBuffer(), 0, pongMessageArray, 0, pongMessage.getLen());

        // Allocate the end message array
        final AjpMessage endMessage = new AjpMessage(16);
        endMessage.reset();
        endMessage.appendByte(Constants.JK_AJP13_END_RESPONSE);
        endMessage.appendByte(1);
        endMessage.end();
        endMessageArray = new byte[endMessage.getLen()];
        System.arraycopy(endMessage.getBuffer(), 0, endMessageArray, 0, endMessage.getLen());

        // Allocate the flush message array
        final AjpMessage flushMessage = new AjpMessage(16);
        flushMessage.reset();
        flushMessage.appendByte(Constants.JK_AJP13_SEND_BODY_CHUNK);
        flushMessage.appendInt(0);
        flushMessage.appendByte(0);
        flushMessage.end();
        flushMessageArray = new byte[flushMessage.getLen()];
        System.arraycopy(flushMessage.getBuffer(), 0, flushMessageArray, 0, flushMessage.getLen());

    }

    public AjpProcessor(final int packetSize) {
        super();
        servletId = new StringBuilder(16);
        /*
         * Create request/response
         */
        response = new HttpServletResponseImpl(this);
        request = new HttpServletRequestImpl(response);
        /*
         * Apply input/outout
         */
        request.setInputBuffer(new SocketInputBuffer());
        response.setOutputBuffer(new SocketOutputBuffer());
        /*
         * Initialize rest
         */
        this.packetSize = packetSize;
        requestHeaderMessage = new AjpMessage(packetSize);
        responseHeaderMessage = new AjpMessage(packetSize);
        bodyMessage = new AjpMessage(packetSize);
        /*
         * Set the get body message buffer
         */
        final AjpMessage getBodyMessage = new AjpMessage(16);
        getBodyMessage.reset();
        getBodyMessage.appendByte(Constants.JK_AJP13_GET_BODY_CHUNK);
        /*
         * Adjust allowed size if packetSize != default (Constants.MAX_PACKET_SIZE)
         */
        getBodyMessage.appendInt(Constants.MAX_READ_SIZE + packetSize - Constants.MAX_PACKET_SIZE);
        getBodyMessage.end();
        getBodyMessageArray = new byte[getBodyMessage.getLen()];
        System.arraycopy(getBodyMessage.getBuffer(), 0, getBodyMessageArray, 0, getBodyMessage.getLen());
        /*
         * Cause loading of HexUtils
         */
        final int foo = HexUtils.DEC[0];
    }

    // ------------------------------------------------------------- Properties

    /**
     * Use Tomcat authentication ?
     */
    protected boolean tomcatAuthentication = true;

    public boolean getTomcatAuthentication() {
        return tomcatAuthentication;
    }

    public void setTomcatAuthentication(final boolean tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
    }

    /**
     * Required secret.
     */
    protected String requiredSecret = null;

    public void setRequiredSecret(final String requiredSecret) {
        this.requiredSecret = requiredSecret;
    }

    /**
     * The number of milliseconds Tomcat will wait for a subsequent request before closing the connection. The default is the same as for
     * Apache HTTP Server (15 000 milliseconds).
     */
    protected int keepAliveTimeout = -1;

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(final int timeout) {
        keepAliveTimeout = timeout;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Get the request associated with this processor.
     * 
     * @return The request
     */
    public HttpServletRequestImpl getRequest() {
        return request;
    }

    /**
     * Gets the HTTP session cookie.
     * 
     * @return The HTTP session cookie
     */
    public Cookie getHttpSessionCookie() {
        return httpSessionCookie;
    }

    /**
     * Checks if the client joined a previously existing HTTP session.
     * 
     * @return <code>true</code> if the client joined a previously existing HTTP session; otherwise <code>false</code>
     */
    public boolean isHttpSessionJoined() {
        return httpSessionJoined;
    }

    /**
     * Process pipelined HTTP requests using the specified input and output streams.
     * 
     * @throws IOException If an error occurs during an I/O operation
     */
    public void process(final Socket socket) throws IOException {
        stage = STAGE_PARSE;
        // Setting up the socket
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
        int soTimeout = -1;
        if (keepAliveTimeout > 0) {
            soTimeout = socket.getSoTimeout();
        }

        // Error flag
        error = false;

        while (started && !error) {

            // Parsing the request header
            try {
                // Set keep alive timeout if enabled
                if (keepAliveTimeout > 0) {
                    socket.setSoTimeout(keepAliveTimeout);
                }
                // Get first message of the request
                if (!readMessage(requestHeaderMessage)) {
                    // This means a connection timeout
                    stage = STAGE_ENDED;
                    break;
                }
                // Set back timeout if keep alive timeout is enabled
                if (keepAliveTimeout > 0) {
                    socket.setSoTimeout(soTimeout);
                }
                // Check message type, process right away and break if
                // not regular request processing
                final int type = requestHeaderMessage.getByte();
                if (type == Constants.JK_AJP13_CPING_REQUEST) {
                    try {
                        output.write(pongMessageArray);
                        output.flush();
                    } catch (final IOException e) {
                        error = true;
                    }
                    continue;
                } else if (type != Constants.JK_AJP13_FORWARD_REQUEST) {
                    // Usually the servlet didn't read the previous request body
                    if (log.isDebugEnabled()) {
                        log.debug("Unexpected message: " + type);
                    }
                    continue;
                }
                request.setStartTime(System.currentTimeMillis());
            } catch (final IOException e) {
                log.debug("ajpprocessor.io.error", e);
                error = true;
                break;
            } catch (final Throwable t) {
                log.debug("ajpprocessor.header.error", t);
                // 400 - Bad Request
                response.setStatus(400);
                error = true;
            }
            /*
             * Setting up filters, and parse some request headers
             */
            stage = STAGE_PREPARE;
            try {
                /*
                 * Parse AJP FORWARD-REQUEST package
                 */
                prepareRequest();
            } catch (final Throwable t) {
                log.debug("ajpprocessor.request.prepare", t);
                /*
                 * 400 - Internal Server Error
                 */
                response.setStatus(400);
                error = true;
            }
            /*
             * Process the request in the servlet
             */
            if (!error) {
                try {
                    stage = STAGE_SERVICE;
                    /*
                     * Form data?
                     */
                    if (request.isFormData()) {
                        /*
                         * Read all data from servlet input stream...
                         */
                        final int buflen = 2048;
                        final byte[] buf = new byte[buflen];
                        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(8192);
                        final ServletInputStream inputStream = request.getInputStream();
                        for (int read = inputStream.read(buf, 0, buflen); read > 0; read = inputStream.read(buf, 0, buflen)) {
                            baos.write(buf, 0, read);
                        }
                        String charEnc = request.getCharacterEncoding();
                        if (charEnc == null) {
                            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                        }
                        final byte[] bytes = baos.toByteArray();
                        parseQueryString(new String(bytes, charEnc));
                        request.setServletInputStream(new ByteArrayServletInputStream(bytes));
                    }
                    servlet.service(request, response);
                    response.flushBuffer();
                    // response.getServletOutputStream().flush();
                } catch (final InterruptedIOException e) {
                    error = true;
                } catch (final Throwable t) {
                    log.error("ajpprocessor.request.process", t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    error = true;
                }
            }
            /*
             * Finish the response if not done yet
             */
            if (!finished) {
                try {
                    finish();
                } catch (final Throwable t) {
                    error = true;
                }
            }

            // If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (error) {
                response.setStatus(500);
            }
            // TODO:
            // request.updateCounters();
            stage = STAGE_KEEPALIVE;
            recycle();
        }
        /*
         * Terminate AJP connection
         */
        stage = STAGE_ENDED;
        recycle();
        input = null;
        output = null;
    }

    // ----------------------------------------------------- ActionHook Methods

    /**
     * Send an action to the connector.
     * 
     * @param actionCode The action type
     * @param param The action parameter
     */
    public void action(final ActionCode actionCode, final Object param) {
        if (actionCode == ActionCode.ACTION_COMMIT) {
            if (response.isCommitted()) {
                return;
            }

            // Validate and write response headers
            try {
                prepareResponse();
            } catch (final IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_CLIENT_FLUSH) {
            if (!response.isCommitted()) {
                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                    return;
                }
            }
            try {
                /*
                 * Write empty SEND-BODY-CHUNK package
                 */
                flush();
            } catch (final IOException e) {
                // Set error flag
                error = true;
            }
        } else if (actionCode == ActionCode.ACTION_CLOSE) {
            // Close

            // End the processing of the current request, and stop any further
            // transactions with the client
            try {
                /*
                 * Write END-RESPONSE package
                 */
                finish();
            } catch (final IOException e) {
                // Set error flag
                error = true;
            }
        } else if (actionCode == ActionCode.ACTION_START) {
            started = true;
        } else if (actionCode == ActionCode.ACTION_STOP) {
            started = false;
        }
        /*-
         * 
        else if (actionCode == ActionCode.ACTION_REQ_SSL_ATTRIBUTE) {

            if (!certificates.isNull()) {
                final ByteChunk certData = certificates.getByteChunk();
                X509Certificate jsseCerts[] = null;
                final ByteArrayInputStream bais = new ByteArrayInputStream(certData.getBytes(), certData.getStart(), certData.getLength());
                // Fill the elements.
                try {
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    while (bais.available() > 0) {
                        final X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
                        if (jsseCerts == null) {
                            jsseCerts = new X509Certificate[1];
                            jsseCerts[0] = cert;
                        } else {
                            final X509Certificate[] temp = new X509Certificate[jsseCerts.length + 1];
                            System.arraycopy(jsseCerts, 0, temp, 0, jsseCerts.length);
                            temp[jsseCerts.length] = cert;
                            jsseCerts = temp;
                        }
                    }
                } catch (final java.security.cert.CertificateException e) {
                    log.error(sm.getString("ajpprocessor.certs.fail"), e);
                    return;
                }
                request.setAttribute(JIoEndpoint.CERTIFICATE_KEY, jsseCerts);
            }

        }
         * 
         */
        else if (actionCode == ActionCode.ACTION_REQ_HOST_ATTRIBUTE) {
            // Get remote host name using a DNS resolution
            if (request.getRemoteHost() == null) {
                try {
                    request.setRemoteHost(InetAddress.getByName(request.getRemoteAddr().toString()).getHostName());
                } catch (final IOException iex) {
                    // Ignore
                }
            }
        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_ADDR_ATTRIBUTE) {
            // Copy from local name for now, which should simply be an address
            request.setLocalAddr(request.getLocalName().toString());
        } else if (actionCode == ActionCode.ACTION_REQ_SET_BODY_REPLAY) {
            // Set the given bytes as the content
            final ByteChunk bc = (ByteChunk) param;
            final int length = bc.getLength();
            bodyBytes.setBytes(bc.getBytes(), bc.getStart(), length);
            request.setContentLength(length);
            first = false;
            empty = false;
            replay = true;
        }
    }

    // ------------------------------------------------------ Connector Methods

    /**
     * Get the associated adapter.
     * 
     * @return the associated adapter
     */
    public HttpServlet getAdapter() {
        return servlet;
    }

    // ------------------------------------------------------ Protected Methods

    private static final String JSESSIONID_URI = AJPv13RequestHandler.JSESSIONID_URI;

    /**
     * After reading the request headers, we have to setup the request filters.
     */
    protected void prepareRequest() {
        // Translate the HTTP method code to a String.
        final byte methodCode = requestHeaderMessage.getByte();
        if (methodCode != Constants.SC_M_JK_STORED) {
            final String methodName = Constants.methodTransArray[methodCode - 1];
            request.setMethod(methodName);
        }
        request.setProtocol(requestHeaderMessage.getString());
        String jsessionID = null;
        {
            final String requestURI = requestHeaderMessage.getString();
            final int pos = requestURI.toLowerCase(Locale.ENGLISH).indexOf(JSESSIONID_URI);
            if (pos > -1) {
                jsessionID = requestURI.substring(pos + JSESSIONID_URI.length());
                request.setRequestedSessionIdFromURL(true);
                request.setRequestedSessionIdFromCookie(false);
            }
            request.setRequestURI(requestURI);
        }
        request.setRemoteAddr(requestHeaderMessage.getString());
        request.setRemoteHost(requestHeaderMessage.getString());
        {
            final String serverName = requestHeaderMessage.getString();
            request.setLocalName(serverName);
            request.setServerName(serverName);
        }
        {
            final int serverPort = requestHeaderMessage.getInt();
            request.setLocalPort(serverPort);
            request.setServerPort(serverPort);
        }

        final boolean isSSL = requestHeaderMessage.getByte() != 0;
        if (isSSL) {
            request.setSecure(isSSL);
            request.setScheme("https");
        }

        // Decode headers
        final int hCount = requestHeaderMessage.getInt();
        for (int i = 0; i < hCount; i++) {
            final String hName;
            // Header names are encoded as either an integer code starting
            // with 0xA0, or as a normal string (in which case the first
            // two bytes are the length).
            int isc = requestHeaderMessage.peekInt();
            int hId = isc & 0xFF;

            isc &= 0xFF00;
            if (0xA000 == isc) {
                requestHeaderMessage.getInt(); // To advance the read position
                hName = Constants.headerTransArray[hId - 1];
            } else {
                // reset hId -- if the header currently being read
                // happens to be 7 or 8 bytes long, the code below
                // will think it's the content-type header or the
                // content-length header - SC_REQ_CONTENT_TYPE=7,
                // SC_REQ_CONTENT_LENGTH=8 - leading to unexpected
                // behaviour. see bug 5861 for more information.
                hId = -1;
                hName = requestHeaderMessage.getString();
            }
            final String hValue = requestHeaderMessage.getString();
            /*
             * Check for "Content-Length" and "Content-Type" headers
             */
            if (hId == Constants.SC_REQ_CONTENT_LENGTH || (hId == -1 && hName.equalsIgnoreCase("Content-Length"))) {
                // just read the content-length header, so set it
                final long cl = Long.parseLong(hValue);
                if (cl < Integer.MAX_VALUE) {
                    request.setContentLength((int) cl);
                }
            } else if (hId == Constants.SC_REQ_CONTENT_TYPE || (hId == -1 && hName.equalsIgnoreCase("Content-Type"))) {
                // just read the content-type header, so set it
                try {
                    request.setContentType(hValue);
                } catch (final AJPv13Exception e) {
                    response.setStatus(403);
                    error = true;
                }
            } else {
                try {
                    request.setHeader(hName, hValue, false);
                } catch (final AJPv13Exception e) {
                    // Cannot occur
                }
            }
        }
        /*
         * Decode extra attributes
         */
        boolean secret = false;
        byte attributeCode;
        while ((attributeCode = requestHeaderMessage.getByte()) != Constants.SC_A_ARE_DONE) {

            if (attributeCode == Constants.SC_A_REQ_ATTRIBUTE) {
                final String n = requestHeaderMessage.getString();
                final String v = requestHeaderMessage.getString();
                /*
                 * AJP13 misses to forward the remotePort. Allow the AJP connector to add this info via a private request attribute. We will
                 * accept the forwarded data as the remote port, and remove it from the public list of request attributes.
                 */
                if (n.equals(Constants.SC_A_REQ_REMOTE_PORT)) {
                    try {
                        request.setRemotePort(Integer.parseInt(v));
                    } catch (final NumberFormatException nfe) {
                        // Ignore
                    }
                } else {
                    request.setAttribute(n, v);
                }
            } else if (attributeCode == Constants.SC_A_CONTEXT) {
                final String context = requestHeaderMessage.getString();
                request.setAttribute(Constants.attributeNameArray[attributeCode - 1], context);
                request.setContextPath(context);
            } else if (attributeCode == Constants.SC_A_SERVLET_PATH) {
                final String servletPath = requestHeaderMessage.getString();
                request.setAttribute(Constants.attributeNameArray[attributeCode - 1], servletPath);
                request.setServletPath(servletPath);
            } else if (attributeCode == Constants.SC_A_REMOTE_USER) {
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getString();
                } else {
                    request.setRemoteUser(requestHeaderMessage.getString());
                }
            } else if (attributeCode == Constants.SC_A_AUTH_TYPE) {
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getString();
                } else {
                    request.setAuthType(requestHeaderMessage.getString());
                }
            } else if (attributeCode == Constants.SC_A_QUERY_STRING) {
                final String queryString = requestHeaderMessage.getString();
                request.setQueryString(queryString);
                try {
                    parseQueryString(queryString);
                } catch (final UnsupportedEncodingException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            } else if (attributeCode == Constants.SC_A_JVM_ROUTE) {
                final String jvmRoute = requestHeaderMessage.getString();
                if (!AJPv13Config.getJvmRoute().equals(jvmRoute)) {
                    log.error("JVM route mismatch. Expected \"" + AJPv13Config.getJvmRoute() + "\", but is \"" + jvmRoute + "\".");
                }
                request.setInstanceId(jvmRoute);
            } else if (attributeCode == Constants.SC_A_SSL_CERT) {
                request.setScheme("https");
                // SSL certificate extraction is lazy, moved to JkCoyoteHandler
                requestHeaderMessage.getBytes(certificates);
            } else if (attributeCode == Constants.SC_A_SSL_CIPHER) {
                request.setScheme("https");
                request.setAttribute("javax.servlet.request.cipher_suite", requestHeaderMessage.getString());
            } else if (attributeCode == Constants.SC_A_SSL_SESSION) {
                request.setScheme("https");
                request.setAttribute("javax.servlet.request.ssl_session", requestHeaderMessage.getString());
            } else if (attributeCode == Constants.SC_A_SSL_KEY_SIZE) {
                request.setAttribute("javax.servlet.request.key_size", new Integer(requestHeaderMessage.getInt()));
            } else if (attributeCode == Constants.SC_A_STORED_METHOD) {
                request.setMethod(requestHeaderMessage.getString());
            } else if (attributeCode == Constants.SC_A_SECRET) {
                final String value = requestHeaderMessage.getString();
                if (requiredSecret != null) {
                    secret = true;
                    if (!value.equals(requiredSecret)) {
                        response.setStatus(403);
                        error = true;
                    }
                }
            } else {
                // Nothing to do
            }

        }
        /*
         * Check if secret was submitted if required
         */
        if ((requiredSecret != null) && !secret) {
            response.setStatus(403);
            error = true;
        }
        /*
         * Check for a full URI (including protocol://host:port/)
         */
        final String requestURI = request.getRequestURI();
        if (requestURI.regionMatches(true, 0, "http", 0, 4)) {
            final int pos = requestURI.indexOf("://", 4);
            int slashPos = -1;
            if (pos != -1) {
                slashPos = requestURI.indexOf('/', pos + 3);
                if (slashPos == -1) {
                    slashPos = requestURI.length();
                    /*
                     * Set URI as "/"
                     */
                    request.setRequestURI("/");
                } else {
                    request.setRequestURI(requestURI.substring(slashPos));
                }
                final String host = request.getHeader("host");
                if (null != host) {
                    try {
                        request.setHeader("host", host.substring(pos + 3, slashPos), false);
                    } catch (final AJPv13Exception e) {
                        // Cannot occur
                    }
                }
            }
        }
        /*
         * Parse host header
         */
        final String host = request.getHeader("host");
        if (null != host) {
            parseHost(host);
        }
        /*-
         * Parsing done
         * 
         * Determine addressed servlet instance
         */
        setServletInstance(request.getRequestURI());
        /*
         * Set proper JSESSIONID cookie and pre-create associated HTTP session
         */
        if (jsessionID == null) {
            /*
             * Look for JSESSIONID cookie, if request URI does not contain session id
             */
            checkJSessionIDCookie();
        } else {
            final int dot = jsessionID.lastIndexOf('.');
            if ((dot == -1) || (AJPv13Config.getJvmRoute().equals(jsessionID.substring(dot + 1)))) {
                addJSessionIDCookie(jsessionID);
            } else {
                /*
                 * JVM route does not match
                 */
                createJSessionIDCookie();
            }
        }
    }

    /**
     * Parse host.
     */
    public void parseHost(final String host) {
        if (null == host || 0 == host.length()) {
            /*
             * HTTP/1.0; server port and server name are equal to local ones.
             */
            request.setServerPort(request.getLocalPort());
            request.setServerName(request.getLocalName());
            return;
        }
        /*
         * Build host string
         */
        final boolean ipv6 = '[' == host.charAt(0);
        final int length = host.length();
        final StringBuilder hostBuilder = new StringBuilder(length);
        int colonPos = -1;
        boolean bracketClosed = false;
        for (int i = 0; i < length; i++) {
            final char c = host.charAt(i);
            hostBuilder.append(c);
            if (']' == c) {
                bracketClosed = true;
            } else if (':' == c) {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }
        /*
         * Colon detected?
         */
        if (colonPos < 0) {
            if (request.getScheme().equalsIgnoreCase("https")) {
                /*
                 * 443 - Default HTTPS port
                 */
                request.setServerPort(443);
            } else {
                // 80 - Default HTTTP port
                request.setServerPort(80);
            }
            request.setServerName(hostBuilder.toString());
        } else {
            request.setServerName(hostBuilder.substring(0, colonPos));
            int port = 0;
            int mult = 1;
            for (int i = length - 1; i > colonPos; i--) {
                final int charValue = host.charAt(i);
                if (charValue == -1) {
                    // Invalid character
                    error = true;
                    // 400 - Bad request
                    response.setStatus(400);
                    break;
                }
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
            request.setServerPort(port);
        }
    }

    private static final java.util.regex.Pattern PATTERN_SPLIT = java.util.regex.Pattern.compile("&");

    /**
     * Parses a query string and puts resulting parameters into given servlet request.
     * 
     * @param queryStr The query string to be parsed
     * @throws UnsupportedEncodingException If charset provided by servlet request is not supported
     */
    private void parseQueryString(final String queryStr) throws UnsupportedEncodingException {
        final String[] paramsNVPs = PATTERN_SPLIT.split(queryStr, 0);
        final String charEnc = request.getCharacterEncoding();
        for (final String paramsNVP2 : paramsNVPs) {
            final String paramsNVP = paramsNVP2.trim();
            if (paramsNVP.length() > 0) {
                // Look-up character '='
                final int pos = paramsNVP.indexOf('=');
                if (pos >= 0) {
                    request.setParameter(paramsNVP.substring(0, pos), decodeQueryStringValue(charEnc, paramsNVP.substring(pos + 1)));
                } else {
                    request.setParameter(paramsNVP, "");
                }
            }
        }
    }

    private static String decodeQueryStringValue(final String charEnc, final String queryStringValue) throws UnsupportedEncodingException {
        return URLDecoder.decode(queryStringValue, charEnc == null ? AJPv13Config.getServerProperty(Property.DefaultEncoding) : charEnc);
    }

    /**
     * Sets this request hander's servlet reference to the one bound to given path argument
     * 
     * @param requestURI The request URI
     */
    private void setServletInstance(final String requestURI) {
        /*
         * Remove leading slash character
         */
        final String path = removeFromPath(requestURI, '/');
        /*
         * Lookup path in available servlet paths
         */
        if (servletId.length() > 0) {
            servletId.setLength(0);
        }
        HttpServlet servlet = HttpServletManager.getServlet(path, servletId);
        if (servlet == null) {
            servlet = new HttpErrorServlet("No servlet bound to path/alias: " + requestURI);
        }
        this.servlet = servlet;
        // servletId = pathStorage.length() > 0 ? pathStorage.toString() : null;
        if (servletId.length() > 0) {
            servletPath = removeFromPath(servletId.toString(), '*');
        }
        request.setServletInstance(servlet);
        request.setServletPath(servletPath);
        if (null != servletPath) {
            /*
             * Apply the servlet path with leading "/" character
             */
            final int servletPathLen = servletPath.length();
            if ((1 == servletPathLen) && ('*' == servletPath.charAt(0))) {
                /*
                 * Set an empty string ("") if the servlet used to process this request was matched using the "/*" pattern.
                 */
                request.setServletPath("");
                /*
                 * Set complete request URI as path info
                 */
                request.setPathInfo(requestURI);
            } else {
                /*
                 * The path starts with a "/" character and includes either the servlet name or a path to the servlet, but does not include
                 * any extra path information or a query string.
                 */
                request.setServletPath(servletPath);
                /*
                 * Set path info: The extra path information follows the servlet path but precedes the query string and will start with a
                 * "/" character.
                 */
                if ((requestURI.length() > servletPathLen) /* && requestURI.startsWith(servletPath) */) {
                    request.setPathInfo(requestURI.substring(servletPathLen));
                } else {
                    request.setPathInfo(null);
                }
            }
        }
    }

    /**
     * Removes specified character if given path ends with such a character.
     * 
     * @param path The path to prepare
     * @param c The (trailing) character to remove
     * @return The path possibly with ending character removed
     */
    private static String removeFromPath(final String path, final char c) {
        final int len = path.length();
        if (c == path.charAt(len - 1)) {
            // Ends with "/"
            return path.substring(0, len - 1);
        }
        return path;
    }

    private void checkJSessionIDCookie() {
        final Cookie[] cookies = request.getCookies();
        Cookie jsessionIDCookie = null;
        if (cookies != null) {
            NextCookie: for (int i = 0; (i < cookies.length) && (jsessionIDCookie == null); i++) {
                final Cookie current = cookies[i];
                if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(current.getName())) {
                    /*
                     * Check JVM route
                     */
                    final String id = current.getValue();
                    final int pos = id.lastIndexOf('.');
                    final String jvmRoute = AJPv13Config.getJvmRoute();
                    if (pos > -1) {
                        if ((jvmRoute != null) && !jvmRoute.equals(id.substring(pos + 1))) {
                            /*
                             * Different JVM route detected -> Discard
                             */
                            if (DEBUG) {
                                log.debug(new StringBuilder("\n\tDifferent JVM route detected. Removing JSESSIONID cookie: ").append(id));
                            }
                            current.setMaxAge(0); // delete
                            response.addCookie(current);
                            continue NextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                log.debug(new StringBuilder("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setMaxAge(0); // delete
                            response.addCookie(current);
                            continue NextCookie;
                        }
                        jsessionIDCookie = current;
                        httpSessionCookie = jsessionIDCookie;
                        httpSessionJoined = true;
                    } else {
                        /*
                         * Value does not apply to pattern [UID].[JVM-ROUTE], hence only UID is given through special cookie JSESSIONID.
                         */
                        if (jvmRoute != null) {
                            /*
                             * But this host defines a JVM route
                             */
                            if (DEBUG) {
                                log.debug(new StringBuilder("\n\tMissing JVM route in JESSIONID cookie").append(current.getValue()));
                            }
                            current.setMaxAge(0); // delete
                            response.addCookie(current);
                            continue NextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                log.debug(new StringBuilder("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setMaxAge(0); // delete
                            response.addCookie(current);
                            continue NextCookie;
                        }
                        jsessionIDCookie = current;
                        httpSessionCookie = jsessionIDCookie;
                        httpSessionJoined = true;
                    }
                }
            }
        }
        if (jsessionIDCookie == null) {
            createJSessionIDCookie();
        }
    }

    private void createJSessionIDCookie() {
        /*
         * Create a new unique id
         */
        final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
        final String jvmRoute = AJPv13Config.getJvmRoute();
        if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
            jsessionIDVal.append('.').append(jvmRoute);
        }
        final Cookie jsessionIDCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, jsessionIDVal.toString());
        httpSessionCookie = jsessionIDCookie;
        httpSessionJoined = false;
        /*
         * HttpServletRequestWrapper.getSession() adds the JSESSIONID cookie
         */
        request.getSession(true);
    }

    private void addJSessionIDCookie(final String id) {
        final String jsessionIdVal;
        final boolean join;
        /*
         * Check known JSESSIONIDs and corresponding HTTP session
         */
        if (HttpSessionManagement.isHttpSessionValid(id)) {
            jsessionIdVal = id;
            join = true;
        } else {
            /*
             * Invalid cookie. Create a new unique id
             */
            final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
            final String jvmRoute = AJPv13Config.getJvmRoute();
            if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
                jsessionIDVal.append('.').append(jvmRoute);
            }
            jsessionIdVal = jsessionIDVal.toString();
            join = false;
        }
        final Cookie jsessionIDCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, jsessionIdVal);
        httpSessionCookie = jsessionIDCookie;
        httpSessionJoined = join;
        /*
         * HttpServletRequestWrapper.getSession() adds the JSESSIONID cookie
         */
        request.getSession(true);
    }

    /**
     * If true, custom HTTP status messages will be used in headers.
     */
    private static final boolean USE_CUSTOM_STATUS_MSG_IN_HEADER = Boolean.valueOf(
        System.getProperty("org.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER", "false")).booleanValue();

    private static final String STR_SET_COOKIE = "Set-Cookie";

    /**
     * When committing the response, we have to validate the set of headers, as well as setup the response filters.
     */
    protected void prepareResponse() throws IOException {
        response.setCommitted(true);
        /*
         * Reset AJP header message and append SEND_HEADERS prefix code
         */
        responseHeaderMessage.reset();
        responseHeaderMessage.appendByte(Constants.JK_AJP13_SEND_HEADERS);
        /*
         * HTTP header contents
         */
        responseHeaderMessage.appendInt(response.getStatus());
        String message = null;
        if (USE_CUSTOM_STATUS_MSG_IN_HEADER && HttpMessages.isSafeInHttpHeader(response.getStatusMsg())) {
            message = response.getStatusMsg();
        }
        if (message == null) {
            message = HttpMessages.getMessage(response.getStatus());
        }
        if (message == null) {
            // mod_jk + httpd 2.x fails with a null status message - bug 45026
            message = Integer.toString(response.getStatus());
        }
        responseHeaderMessage.appendString(message);
        /*-
         * Check for echo header presence
         */
        final String echoHeaderName = AJPv13Response.getEchoHeaderName();
        if (null != echoHeaderName) {
            final String echoValue = request.getHeader(echoHeaderName);
            if (null != echoValue) {
                response.setHeader(echoHeaderName, echoValue);
            }
        }
        /*
         * Write headers to SEND_HEADERS message
         */
        final int numHeaders = response.getNumOfHeaders();
        final byte[] headers;
        {
            sink.reset();
            for (final Entry<String, List<String>> entry : response.getHeaderEntrySet()) {
                writeHeaderSafe(entry.getKey(), toValue(entry.getValue()), sink);
            }
            headers = sink.toByteArray();
        }
        final byte[] cookies;
        final int numCookies;
        {
            sink.reset();
            final String[][] formattedCookies = response.getFormatedCookies();
            if (formattedCookies.length > 0) {
                for (int j = 0; j < formattedCookies[0].length; j++) {
                    writeHeaderSafe(STR_SET_COOKIE, formattedCookies[0][j], sink);
                }
                if (formattedCookies.length > 1) {
                    final StringBuilder sb = new StringBuilder(STR_SET_COOKIE.length() + 1);
                    for (int i = 1; i < formattedCookies.length; i++) {
                        sb.setLength(0);
                        final String hdrName = sb.append(STR_SET_COOKIE).append(i + 1).toString();
                        for (int j = 0; j < formattedCookies[i].length; j++) {
                            writeHeaderSafe(hdrName, formattedCookies[i][j], sink);
                        }
                    }
                }
            }
            cookies = sink.toByteArray();
            numCookies = getNumOfCookieHeader(formattedCookies);
        }
        /*
         * Write total number of headers (headers + cookies)
         */
        responseHeaderMessage.appendInt(numHeaders + numCookies);
        /*
         * Write headers' and cookies' bytes.
         */
        responseHeaderMessage.appendBytes(headers, 0, headers.length);
        responseHeaderMessage.appendBytes(cookies, 0, cookies.length);
        /*
         * Write to buffer
         */
        responseHeaderMessage.end();
        output.write(responseHeaderMessage.getBuffer(), 0, responseHeaderMessage.getLen());
        output.flush();
    }

    private static final int getNumOfCookieHeader(final String[][] formattedCookies) {
        int retval = 0;
        for (final String[] formattedCookie : formattedCookies) {
            retval += formattedCookie.length;
        }
        return retval;
    }

    private static String toValue(final List<String> values) {
        if (null == values || values.isEmpty()) {
            return "";
        }
        final StringBuilder retval = new StringBuilder(128);
        retval.append(values.get(0));
        for (int i = 1, len = values.size(); i < len; i++) {
            retval.append(',').append(values.get(i));
        }
        return retval.toString();
    }

    /**
     * Finish AJP response.
     */
    protected void finish() throws IOException {

        if (!response.isCommitted()) {
            // Validate and write response headers
            try {
                prepareResponse();
            } catch (final IOException e) {
                // Set error flag
                error = true;
            }
        }

        if (finished) {
            return;
        }

        finished = true;

        // Add the end message
        output.write(endMessageArray);
        output.flush();
    }

    /**
     * Read at least the specified amount of bytes, and place them in the input buffer.
     */
    protected boolean read(final byte[] buf, final int pos, final int n) throws IOException {
        int read = 0;
        int res = 0;
        while (read < n) {
            res = input.read(buf, read + pos, n - read);
            if (res <= 0) {
                throw new IOException("ajpprotocol.failedread");
            }
            read += res;
        }
        return true;
    }

    /**
     * Receive a chunk of data. Called to implement the 'special' packet in ajp13 and to receive the data after we send a GET_BODY packet
     */
    public boolean receive() throws IOException {
        first = false;
        bodyMessage.reset();
        readMessage(bodyMessage);
        // No data received.
        if (bodyMessage.getLen() == 0) {
            // just the header
            // Don't mark 'end of stream' for the first chunk.
            return false;
        }
        final int blen = bodyMessage.peekInt();
        if (blen == 0) {
            return false;
        }
        bodyMessage.getBytes(bodyBytes);
        empty = false;
        return true;
    }

    /**
     * Get more request body data from the web server and store it in the internal buffer.
     * 
     * @return true if there is more data, false if not.
     */
    protected boolean refillReadBuffer() throws IOException {
        // If the server returns an empty packet, assume that that end of
        // the stream has been reached (yuck -- fix protocol??).
        // FORM support
        if (replay) {
            endOfStream = true; // we've read everything there is
        }
        if (endOfStream) {
            return false;
        }

        // Request more data immediately
        output.write(getBodyMessageArray);
        output.flush();

        final boolean moreData = receive();
        if (!moreData) {
            endOfStream = true;
        }
        return moreData;
    }

    /**
     * Read an AJP message.
     * 
     * @return true if the message has been read, false if the short read didn't return anything
     * @throws IOException any other failure, including incomplete reads
     */
    protected boolean readMessage(final AjpMessage message) throws IOException {
        final byte[] buf = message.getBuffer();

        read(buf, 0, message.getHeaderLength());

        message.processHeader();
        read(buf, message.getHeaderLength(), message.getLen());

        return true;
    }

    /**
     * Recycle the processor.
     */
    public void recycle() {
        // Recycle Request object
        first = true;
        endOfStream = false;
        empty = true;
        replay = false;
        finished = false;
        httpSessionCookie = null;
        httpSessionJoined = false;
        servlet = null;
        servletPath = null;
        servletId.setLength(0);
        request.recycle();
        response.recycle();
        certificates.recycle();
    }

    /**
     * Callback to write data from the buffer.
     */
    protected void flush() throws IOException {
        // Send the flush message
        output.write(flushMessageArray);
        output.flush();
    }

    // ------------------------------------- InputStreamInputBuffer Inner Class

    /**
     * This class is an input buffer which will read its data from an input stream.
     */
    protected class SocketInputBuffer implements InputBuffer {

        /**
         * Read bytes into the specified chunk.
         */
        @Override
        public int doRead(final ByteChunk chunk, final HttpServletRequestImpl req) throws IOException {
            if (endOfStream) {
                return -1;
            }
            if (first && req.getContentLengthLong() > 0) {
                /*
                 * Handle special first-body-chunk
                 */
                if (!receive()) {
                    return 0;
                }
            } else if (empty) {
                if (!refillReadBuffer()) {
                    return -1;
                }
            }
            final ByteChunk bc = bodyBytes.getByteChunk();
            chunk.setBytes(bc.getBuffer(), bc.getStart(), bc.getLength());
            empty = true;
            return chunk.getLength();
        }
    }

    // ----------------------------------- OutputStreamOutputBuffer Inner Class

    /**
     * This class is an output buffer which will write data to an output stream.
     */
    protected class SocketOutputBuffer implements OutputBuffer {

        /**
         * Write chunk.
         */
        @Override
        public int doWrite(final ByteChunk chunk) throws IOException {
            if (!response.isCommitted()) {
                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                }
            }

            int len = chunk.getLength();
            // 4 - hardcoded, byte[] marshalling overhead
            // Adjust allowed size if packetSize != default (Constants.MAX_PACKET_SIZE)
            final int chunkSize = Constants.MAX_SEND_SIZE + packetSize - Constants.MAX_PACKET_SIZE;
            int off = 0;
            while (len > 0) {
                int thisTime = len;
                if (thisTime > chunkSize) {
                    thisTime = chunkSize;
                }
                len -= thisTime;
                responseHeaderMessage.reset();
                responseHeaderMessage.appendByte(Constants.JK_AJP13_SEND_BODY_CHUNK);
                responseHeaderMessage.appendBytes(chunk.getBytes(), chunk.getOffset() + off, thisTime);
                // mod_proxy: Terminating 0 (zero) byte
                responseHeaderMessage.appendByte(0);
                responseHeaderMessage.end();
                output.write(responseHeaderMessage.getBuffer(), 0, responseHeaderMessage.getLen());
                output.flush();
                off += thisTime;
            }

            return chunk.getLength();
        }
    }

}
