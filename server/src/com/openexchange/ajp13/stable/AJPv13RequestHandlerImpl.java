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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13.stable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.AJPv13CPingRequest;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Connection;
import com.openexchange.ajp13.AJPv13ForwardRequest;
import com.openexchange.ajp13.AJPv13Request;
import com.openexchange.ajp13.AJPv13RequestBody;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.AJPv13ServletInputStream;
import com.openexchange.ajp13.AJPv13ServletOutputStream;
import com.openexchange.ajp13.AJPv13Utility;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13InvalidByteSequenceException;
import com.openexchange.ajp13.exception.AJPv13SocketClosedException;
import com.openexchange.ajp13.exception.AJPv13UnknownPrefixCodeException;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;
import com.openexchange.ajp13.monitoring.Constants;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.tools.servlet.http.HttpErrorServlet;
import com.openexchange.tools.servlet.http.HttpServletManager;
import com.openexchange.tools.servlet.http.HttpServletRequestWrapper;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AJPv13RequestHandlerImpl} - The AJP request handler processes incoming AJP packages dependent on their prefix code and/or
 * associated package number.
 * <p>
 * Whenever an AJP connection delegates processing to a request handler, it waits on AJP connection's input stream for incoming data. The
 * data is then processed and control is returned to AJP connection.
 * <p>
 * Sub-sequential AJP communication may be initiated through {@link AJPv13ServletInputStream} and {@link AJPv13ServletOutputStream} during
 * servlets' processing.
 * 
 * @see AJPv13ServletInputStream
 * @see AJPv13ServletOutputStream
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13RequestHandlerImpl implements AJPv13RequestHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13RequestHandlerImpl.class);

    private static enum State {
        IDLE, ASSIGNED
    }

    /**
     * Byte sequence indicating a packet from Web Server to Servlet Container.
     */
    private static final int[] PACKAGE_FROM_SERVER_TO_CONTAINER = { 0x12, 0x34 };

    /**
     * Starts the request handle cycle with following data.
     * 
     * @value 2
     */
    private static final int FORWARD_REQUEST_PREFIX_CODE = 2;

    /**
     * Web Server asks to shut down the Servlet Container.
     * 
     * @value 7
     */
    private static final int SHUTDOWN_PREFIX_CODE = 7;

    /**
     * Web Server asks the Servlet Container to take control (secure login phase).
     * 
     * @value 8
     */
    private static final int PING_PREFIX_CODE = 8;

    /**
     * Web Server asks the Servlet Container to respond quickly with a CPong.
     * 
     * @value 10
     */
    private static final int CPING_PREFIX_CODE = 10;

    /**
     * The value for a missing <i>Content-Length</i> header
     * 
     * @value -1
     */
    static final int NOT_SET = -1;

    private HttpServlet servlet;

    private final StringBuilder servletId;

    private HttpServletRequestWrapper request;

    private HttpServletResponseWrapper response;

    private AJPv13Request ajpRequest;

    private AJPv13ConnectionImpl ajpCon;

    private long contentLength;

    private boolean bContentLength;

    private long totalRequestedContentLength;

    private boolean headersSent;

    private boolean serviceMethodCalled;

    private boolean endResponseSent;

    private boolean isFormData;

    private String httpSessionId;

    private boolean httpSessionJoined;

    private String servletPath;

    private State state;

    private byte[] clonedForwardPackage;

    /**
     * Initializes a new {@link AJPv13RequestHandlerImpl}
     */
    AJPv13RequestHandlerImpl() {
        super();
        state = State.IDLE;
        servletId = new StringBuilder(16);
    }

    /**
     * Reads the (mandatory) first four bytes of an incoming AJPv13 package which indicate a package from Web Server to Servlet Container
     * with its first two bytes and the payload data size in bytes in the following two bytes.
     */
    private int readInitialBytes(final boolean enableTimeout) throws IOException, AJPv13Exception {
        int dataLength = -1;
        if (enableTimeout) {
            ajpCon.setSoTimeout(AJPv13Config.getAJPListenerReadTimeout());
        }
        /*
         * Read a package from Web Server to Servlet Container.
         */
        Constants.ajpv13ListenerMonitor.incrementNumWaiting();
        try {
            final InputStream ajpInputStream = ajpCon.getInputStream();
            long start = 0L;
            final int[] magic;
            try {
                /*
                 * Read first two bytes
                 */
                ajpCon.markListenerNonProcessing();
                start = System.currentTimeMillis();
                magic = new int[] { ajpInputStream.read(), ajpInputStream.read() };
            } catch (final SocketException e) {
                throw new AJPv13SocketClosedException(
                    AJPCode.SOCKET_CLOSED_BY_WEB_SERVER,
                    e,
                    Integer.valueOf(ajpCon == null ? 1 : ajpCon.getPackageNumber()),
                    Long.valueOf((System.currentTimeMillis() - start)));
            }
            if (checkMagicBytes(magic)) {
                dataLength = (ajpInputStream.read() << 8) + ajpInputStream.read();
            } else if (magic[0] == -1 || magic[1] == -1) {
                throw new AJPv13SocketClosedException(
                    AJPCode.EMPTY_INPUT_STREAM,
                    null,
                    Integer.valueOf(ajpCon == null ? 1 : ajpCon.getPackageNumber()),
                    Long.valueOf(System.currentTimeMillis() - start));
            } else {
                throw new AJPv13InvalidByteSequenceException(ajpCon.getPackageNumber(), magic[0], magic[1], AJPv13Utility.dumpBytes(
                    (byte) magic[0],
                    (byte) magic[1],
                    getPayloadData(-1, ajpInputStream, false)));
            }
            if (enableTimeout) {
                /*
                 * Set an infinite timeout
                 */
                ajpCon.setSoTimeout(0);
            }
            /*
             * Initial bytes have been read, so processing (re-)starts now
             */
            ajpCon.markListenerProcessing();
        } finally {
            Constants.ajpv13ListenerMonitor.decrementNumWaiting();
        }
        return dataLength;
    }

    private static boolean checkMagicBytes(final int[] magic) {
        if (AJPv13Config.getCheckMagicBytesStrict()) {
            return (magic[0] == PACKAGE_FROM_SERVER_TO_CONTAINER[0] && magic[1] == PACKAGE_FROM_SERVER_TO_CONTAINER[1]);
        }
        return (magic[0] == PACKAGE_FROM_SERVER_TO_CONTAINER[0] || magic[1] == PACKAGE_FROM_SERVER_TO_CONTAINER[1]);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#processPackage()
     */
    public void processPackage() throws AJPv13Exception {
        try {
            if (State.IDLE.equals(state)) {
                state = State.ASSIGNED;
            }
            ajpCon.incrementPackageNumber();
            final boolean firstPackage = (ajpCon.getPackageNumber() == 1);
            final int dataLength = readInitialBytes(firstPackage && AJPv13Config.getAJPListenerReadTimeout() > 0);
            /*
             * We received the first package which must contain a prefix code
             */
            int prefixCode = -1;
            if (firstPackage) {
                /*
                 * Read Prefix Code from Input Stream
                 */
                prefixCode = ajpCon.getInputStream().read();
                if (prefixCode == FORWARD_REQUEST_PREFIX_CODE) {
                    if (AJPv13Config.isLogForwardRequest()) {
                        /*
                         * Clone bytes from forward request
                         */
                        final byte[] payload = getPayloadData(dataLength - 1, ajpCon.getInputStream(), true);
                        clonedForwardPackage = new byte[payload.length + 5];
                        clonedForwardPackage[0] = 0x12;
                        clonedForwardPackage[1] = 0x34;
                        clonedForwardPackage[2] = (byte) (dataLength >> 8);
                        clonedForwardPackage[3] = (byte) (dataLength & (255));
                        clonedForwardPackage[4] = FORWARD_REQUEST_PREFIX_CODE;
                        System.arraycopy(payload, 0, clonedForwardPackage, 5, payload.length);
                        /*
                         * Create forward request with payload data
                         */
                        ajpRequest = new AJPv13ForwardRequest(payload);
                    } else {
                        ajpRequest = new AJPv13ForwardRequest(getPayloadData(dataLength - 1, ajpCon.getInputStream(), true));
                    }
                } else if (prefixCode == SHUTDOWN_PREFIX_CODE) {
                    LOG.error("AJPv13 Shutdown command NOT supported");
                    return;
                } else if (prefixCode == PING_PREFIX_CODE) {
                    LOG.error("AJPv13 Ping command NOT supported");
                    return;
                } else if (prefixCode == CPING_PREFIX_CODE) {
                    ajpRequest = new AJPv13CPingRequest(getPayloadData(dataLength - 1, ajpCon.getInputStream(), true));
                } else {
                    /*
                     * Unknown prefix code in first package: Leave routine
                     */
                    if (LOG.isWarnEnabled()) {
                        final AJPv13Exception ajpExc = new AJPv13UnknownPrefixCodeException(prefixCode);
                        LOG.warn(ajpExc.getMessage(), ajpExc);
                        /*
                         * Dump package
                         */
                        final byte[] payload = getPayloadData(dataLength - 1, ajpCon.getInputStream(), true);
                        final byte[] clonedPackage = new byte[payload.length + 5];
                        clonedPackage[0] = 0x12;
                        clonedPackage[1] = 0x34;
                        clonedPackage[2] = (byte) (dataLength >> 8);
                        clonedPackage[3] = (byte) (dataLength & (255));
                        clonedPackage[4] = (byte) prefixCode;
                        System.arraycopy(payload, 0, clonedPackage, 5, payload.length);
                        LOG.warn("Corresponding AJP package:\n" + AJPv13Utility.dumpBytes(clonedPackage));
                    }
                    return;
                }
            } else {
                /*
                 * Any following packages after package #1 have to be a request body package which does not deliver a prefix code
                 */
                ajpRequest = new AJPv13RequestBody(getPayloadData(dataLength, ajpCon.getInputStream(), true));
            }
            ajpRequest.processRequest(this);
            if (prefixCode == FORWARD_REQUEST_PREFIX_CODE) {
                handleContentLength();
            }
        } catch (final IOException e) {
            throw new AJPv13Exception(AJPCode.IO_ERROR, false, e, e.getMessage());
        }
    }

    private void handleContentLength() throws IOException, AJPv13Exception {
        int dataLength;
        if (contentLength == NOT_SET) {
            /*
             * This condition is reached when no content-length header was present in forward request package (transfer-encoding: chunked)
             */
            request.setData(new byte[0]);
        } else if (contentLength == 0) {
            /*
             * This condition is reached when content-length header's value is set to '0'
             */
            request.setData(null);
        } else {
            /*
             * Forward request is immediately followed by a data package
             */
            ajpCon.incrementPackageNumber();
            /*
             * Processed package is an AJP forward request which indicates presence of a following request body package. Create a copy for
             * logging on a possible error.
             */
            dataLength = readInitialBytes(false);
            ajpRequest = new AJPv13RequestBody(getPayloadData(dataLength, ajpCon.getInputStream(), true));
            ajpRequest.processRequest(this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#createResponse()
     */
    public void createResponse() throws AJPv13Exception, ServletException {
        try {
            if (ajpRequest == null) {
                /*
                 * We received an unsupported prefix code before, thus ajpRequest is null. Terminate AJP cycle.
                 */
                final OutputStream out = ajpCon.getOutputStream();
                out.write(AJPv13Response.getEndResponseBytes());
                out.flush();
                endResponseSent = true;
                return;
            }
            ajpRequest.response(this);
        } catch (final IOException e) {
            throw new AJPv13Exception(AJPCode.IO_ERROR, false, e, e.getMessage());
        }
    }

    /**
     * Gets the forward request's bytes as a formatted string or "&lt;not enabled&gt;" if not enabled via configuration
     * 
     * @return The forward request's bytes as a formatted string
     */
    public String getForwardRequest() {
        return AJPv13Config.isLogForwardRequest() ? AJPv13Utility.dumpBytes(clonedForwardPackage) : "<not enabled>";
    }

    /**
     * Gets the AJP connection of this request handler
     * 
     * @return The AJP connection of this request handler
     */
    public AJPv13Connection getAJPConnection() {
        return ajpCon;
    }

    /**
     * Sets the AJP connection of this request handler
     * 
     * @param ajpCon The AJP connection
     */
    void setAJPConnection(final AJPv13ConnectionImpl ajpCon) {
        this.ajpCon = ajpCon;
    }

    /**
     * Reads a certain amount or all data from given <code>InputStream</code> instance dependent on boolean value of <code>strict</code>
     * 
     * @param payloadLength
     * @param in
     * @param strict if <code>true</code> only <code>payloadLength</code> bytes are read, otherwise all data is read
     * @return The read bytes
     * @throws IOException If an I/O error occurs
     */
    private static byte[] getPayloadData(final int payloadLength, final InputStream in, final boolean strict) throws IOException {
        byte[] bytes = null;
        if (strict) {
            /*
             * Read only payloadLength bytes
             */
            bytes = new byte[payloadLength];
            int bytesRead = -1;
            int offset = 0;
            while ((offset < bytes.length) && ((bytesRead = in.read(bytes, offset, bytes.length - offset)) != -1)) {
                offset += bytesRead;
            }
            if (offset < bytes.length) {
                Arrays.fill(bytes, offset, bytes.length, ((byte) -1));
                if (LOG.isWarnEnabled()) {
                    LOG.warn(new StringBuilder().append("Incomplete payload data in AJP package: Should be ").append(payloadLength).append(
                        " but was ").append(offset).toString(), new Throwable());
                }
            }
        } else {
            /*
             * Read all available bytes
             */
            int bytesRead = -1;
            final ByteArrayOutputStream buf = new UnsynchronizedByteArrayOutputStream(8192);
            final byte[] fillMe = new byte[8192];
            while ((bytesRead = in.read(fillMe, 0, fillMe.length)) != -1) {
                buf.write(fillMe, 0, bytesRead);
            }
            bytes = buf.toByteArray();
        }
        return bytes;
    }

    private void releaseServlet() {
        if (servletId.length() > 0) {
            HttpServletManager.putServlet(servletId.toString(), servlet);
        }
        servletId.setLength(0);
        servlet = null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#reset(boolean)
     */
    public void reset(final boolean discardConnection) {
        if (state.equals(State.IDLE)) {
            return;
        }
        releaseServlet();
        try {
            if (request != null && request.getInputStream() != null) {
                request.getInputStream().close();
                request.removeInputStream();
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        try {
            if (!endResponseSent && response != null && response.getServletOutputStream() != null) {
                response.getServletOutputStream().close();
                response.removeServletOutputStream();
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        request = null;
        response = null;
        ajpRequest = null;
        contentLength = 0;
        bContentLength = false;
        totalRequestedContentLength = 0;
        headersSent = false;
        serviceMethodCalled = false;
        endResponseSent = false;
        isFormData = false;
        httpSessionId = null;
        httpSessionJoined = false;
        servletPath = null;
        state = State.IDLE;
        clonedForwardPackage = null;
        if (discardConnection) {
            ajpCon = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String delim = " | ";
        final StringBuilder sb = new StringBuilder(300);
        sb.append("State: ").append(state.equals(State.IDLE) ? "IDLE" : "ASSIGNED").append(delim);
        sb.append("Servlet: ").append(servlet == null ? "null" : servlet.getClass().getName()).append(delim);
        sb.append("Current Request: ").append(ajpRequest.getClass().getName()).append(delim);
        sb.append("Content Length: ").append(bContentLength ? String.valueOf(contentLength) : "Not available").append(delim);
        sb.append("Servlet triggered: ").append(serviceMethodCalled).append(delim);
        sb.append("Headers sent: ").append(headersSent).append(delim);
        sb.append("End Response sent: ").append(endResponseSent).append(delim);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#setServletInstance(java.lang.String)
     */
    public void setServletInstance(final String pathArg) {
        /*
         * Remove leading slash character
         */
        final String path = preparePath(pathArg);
        /*
         * Lookup path in available servlet paths
         */
        if (servletId.length() > 0) {
            servletId.setLength(0);
        }
        HttpServlet servletInst = HttpServletManager.getServlet(path, servletId);
        if (servletInst == null) {
            servletInst = new HttpErrorServlet("No servlet bound to path/alias: " + path);
        }
        servlet = servletInst;
        // servletId = pathStorage.length() > 0 ? pathStorage.toString() : null;
        if (servletId.length() > 0) {
            servletPath = servletId.toString().replaceFirst("\\*", ""); // path;
        }
        supplyRequestWrapperWithServlet();
    }

    private static String preparePath(final String path) {
        final int start = path.charAt(0) == '/' ? 1 : 0;
        final int end = path.charAt(path.length() - 1) == '/' ? path.length() - 1 : path.length();
        return path.substring(start, end);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#doServletService()
     */
    public void doServletService() throws ServletException, IOException {
        servlet.service(request, response);
        doResponseFlush();
        serviceMethodCalled = true;
    }

    /**
     * Flushes the response to output stream
     * 
     * @throws IOException If an I/O error occurs
     */
    private void doResponseFlush() throws IOException {
        if (response != null) {
            response.flushBuffer();
            response.getServletOutputStream().flushByteBuffer();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#doWriteHeaders(java.io.OutputStream)
     */
    public void doWriteHeaders(final OutputStream out) throws AJPv13Exception, IOException {
        if (!headersSent) {
            out.write(AJPv13Response.getSendHeadersBytes(response));
            out.flush();
            response.setCommitted(true);
            headersSent = true;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getAndClearResponseData()
     */
    public byte[] getAndClearResponseData() throws IOException {
        if (null == response) {
            return new byte[0];
        }
        final byte[] retval = response.getServletOutputStream().getData();
        response.getServletOutputStream().clearByteBuffer();
        return retval;
    }

    /**
     * Sets/appends new data to servlet request's input stream
     * 
     * @param newData The new data to set
     * @throws IOException If an I/O error occurs
     */
    public void setData(final byte[] newData) throws IOException {
        request.setData(newData);
    }

    public byte[] peekData() throws IOException {
        return ((AJPv13ServletInputStream) request.getInputStream()).peekData();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#doParseQueryString(byte[])
     */
    public void doParseQueryString(final byte[] contentBytes) throws UnsupportedEncodingException {
        String charEnc = request.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        AJPv13ForwardRequest.parseQueryString(request, new String(contentBytes, charEnc));
    }

    /**
     * Sets the servlet request
     * 
     * @param request The servlet request
     */
    public void setServletRequest(final HttpServletRequestWrapper request) {
        this.request = request;
        supplyRequestWrapperWithServlet();
    }

    /**
     * Sets the servlet response
     * 
     * @param response The servlet response
     */
    public void setServletResponse(final HttpServletResponseWrapper response) {
        this.response = response;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getContentLength()
     */
    public long getContentLength() {
        return contentLength;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#containsContentLength()
     */
    public boolean containsContentLength() {
        return bContentLength;
    }

    /**
     * Sets the request's content length
     * 
     * @param contentLength The content length
     */
    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength;
        bContentLength = true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getTotalRequestedContentLength()
     */
    public long getTotalRequestedContentLength() {
        return totalRequestedContentLength;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#increaseTotalRequestedContentLength(long)
     */
    public void increaseTotalRequestedContentLength(final long increaseBy) {
        totalRequestedContentLength += increaseBy;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isServiceMethodCalled()
     */
    public boolean isServiceMethodCalled() {
        return serviceMethodCalled;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isEndResponseSent()
     */
    public boolean isEndResponseSent() {
        return endResponseSent;
    }

    /**
     * Sets the end response flag
     */
    public void setEndResponseSent() {
        endResponseSent = true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isFormData()
     */
    public boolean isFormData() {
        return isFormData;
    }

    /**
     * Marks that requests content type equals <code>application/x-www-form-urlencoded</code>
     * 
     * @param isFormData <code>true</code> if request content type equals <code>application/x-www-form-urlencoded</code>; otherwise
     *            <code>false</code>
     */
    public void setFormData(final boolean isFormData) {
        this.isFormData = isFormData;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getNumOfBytesToRequestFor()
     */
    public int getNumOfBytesToRequestFor() {
        final long retval = contentLength - totalRequestedContentLength;
        if (retval > AJPv13Response.MAX_INT_VALUE || retval < 0) {
            return AJPv13Response.MAX_INT_VALUE;
        }
        return (int) retval;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isAllDataRead()
     */
    public boolean isAllDataRead() {
        /*
         * This method will always return false if content-length is not set unless method makeEqual() is invoked
         */
        return (totalRequestedContentLength == contentLength);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isMoreDataExpected()
     */
    public boolean isMoreDataExpected() {
        /*
         * No empty data package received AND requested data length is still less than header Content-Length
         */
        return (contentLength != NOT_SET && totalRequestedContentLength < contentLength);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isNotSet()
     */
    public boolean isNotSet() {
        return (contentLength == NOT_SET);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isMoreDataReadThanExpected()
     */
    public boolean isMoreDataReadThanExpected() {
        return (contentLength != NOT_SET && totalRequestedContentLength > contentLength);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#makeEqual()
     */
    public void makeEqual() {
        totalRequestedContentLength = contentLength;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getHttpSessionId()
     */
    public String getHttpSessionId() {
        return httpSessionId;
    }

    /**
     * Sets the HTTP session ID
     * 
     * @param httpSessionId The HTTP session ID
     * @param join <code>true</code> if the HTTP session has joined a previous HTTP session; otherwise <code>false</code>
     */
    public void setHttpSessionId(final String httpSessionId, final boolean join) {
        this.httpSessionId = httpSessionId;
        httpSessionJoined = join;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#isHttpSessionJoined()
     */
    public boolean isHttpSessionJoined() {
        return httpSessionJoined;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajp13.IAJPv13RequestHandler#getServletPath()
     */
    public String getServletPath() {
        return servletPath;
    }

    private void supplyRequestWrapperWithServlet() {
        if (request != null && servlet != null) {
            request.setServletInstance(servlet);
        }
    }

}
