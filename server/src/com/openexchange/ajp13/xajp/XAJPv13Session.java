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

package com.openexchange.ajp13.xajp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.xsocket.connection.INonBlockingConnection;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.xajp.http.HttpServletRequestWrapper;
import com.openexchange.ajp13.xajp.http.HttpServletResponseWrapper;
import com.openexchange.ajp13.xajp.http.XAJPv13ServletInputStream;
import com.openexchange.ajp13.xajp.request.XAJPv13ForwardRequest;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.tools.servlet.http.HttpErrorServlet;
import com.openexchange.tools.servlet.http.HttpServletManager;

/**
 * {@link XAJPv13Session} - An AJP session holding several status information about the on-going AJP cycle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XAJPv13Session {

    /**
     * The value for a missing <i>Content-Length</i> header
     * 
     * @value -1
     */
    public static final int NOT_SET = -1;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(XAJPv13Session.class);

    private final XAJPv13ProtocolHandler protocolHandler;

    private int packageNumber;

    private HttpServlet servlet;

    private final StringBuilder servletId;

    private HttpServletRequestWrapper request;

    private HttpServletResponseWrapper response;

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

    /**
     * Initializes a new {@link XAJPv13Session}.
     */
    public XAJPv13Session(final XAJPv13ProtocolHandler protocolHandler) {
        super();
        servletId = new StringBuilder(16);
        this.protocolHandler = protocolHandler;
    }

    /**
     * Resets this AJP session.
     */
    public void reset() {
        packageNumber = 0;
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
    }

    /**
     * Gets the protocol handler.
     * 
     * @return The protocol handler.
     */
    public XAJPv13ProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    /**
     * Releases the servlet instance.
     */
    private void releaseServlet() {
        if (servletId.length() > 0) {
            HttpServletManager.putServlet(servletId.toString(), servlet);
        }
        servletId.setLength(0);
        servlet = null;
    }

    /**
     * Gets the package number.
     * 
     * @return The package number.
     */
    public int getPackageNumber() {
        return packageNumber;
    }

    /**
     * Increases the package number.
     */
    public void incrementPackageNumber() {
        packageNumber++;
    }

    /**
     * Sets this request hander's servlet reference to the one bound to given path argument
     * 
     * @param pathArg The request path
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

    private void supplyRequestWrapperWithServlet() {
        if (request != null && servlet != null) {
            request.setServletInstance(servlet);
        }
    }

    private static String preparePath(final String path) {
        final int start = path.charAt(0) == '/' ? 1 : 0;
        final int end = path.charAt(path.length() - 1) == '/' ? path.length() - 1 : path.length();
        return path.substring(start, end);
    }

    /**
     * Triggers the servlet's service method to start processing the request and flushes the response to output stream.
     * <p>
     * This request handler is then marked to have the service() method called; meaning {@link #isServiceMethodCalled()} will return
     * <code>true</code>.
     * 
     * @throws IOException If an I/O error occurs
     * @throws ServletException If a servlet error occurs
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

    /**
     * Writes the HTTP headers to specified connection if not already written.
     * 
     * @param out The connection
     * @throws AJPv13Exception If composing the <code>SEND_HEADERS</code> package fails
     * @throws IOException If an I/O error occurs
     */
    public void doWriteHeaders(final INonBlockingConnection out) throws AJPv13Exception, IOException {
        if (!headersSent) {
            out.write(AJPv13Response.getSendHeadersBytes(response));
            out.flush();
            response.setCommitted(true);
            headersSent = true;
        }
    }

    /**
     * Gets the response output stream's data and clears it
     * 
     * @return The response output stream's data
     * @throws IOException If an I/O error occurs
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
        return ((XAJPv13ServletInputStream) request.getInputStream()).peekData();
    }

    /**
     * Parses given form's data into servlet request
     * 
     * @param contentBytes The content bytes representing a form's data
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public void doParseQueryString(final byte[] contentBytes) throws UnsupportedEncodingException {
        String charEnc = request.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        XAJPv13ForwardRequest.parseQueryString(request, new String(contentBytes, charEnc));
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

    /**
     * Gets the content length
     * 
     * @return The content length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * @return <code>true</code> if content length has been set; otherwise <code>false</code>
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

    /**
     * Gets the total requested content length
     * 
     * @return The total requested content length
     */
    public long getTotalRequestedContentLength() {
        return totalRequestedContentLength;
    }

    /**
     * Increases the total requested content length by specified argument
     * 
     * @param increaseBy The value by which the total requested content length is increased
     */
    public void increaseTotalRequestedContentLength(final long increaseBy) {
        totalRequestedContentLength += increaseBy;
    }

    /**
     * Checks if the <code>service()</code> method has already been called
     * 
     * @return <code>true</code> if <code>service()</code> method has already been called; otherwise <code>false</code>
     */
    public boolean isServiceMethodCalled() {
        return serviceMethodCalled;
    }

    /**
     * Checks if AJP's end response package has been sent to web server
     * 
     * @return <code>true</code> if AJP's end response package has been sent to web server; otherwise <code>false</code>
     */
    public boolean isEndResponseSent() {
        return endResponseSent;
    }

    /**
     * Sets the end response flag
     */
    public void setEndResponseSent() {
        endResponseSent = true;
        reset();
    }

    /**
     * Indicates if request content type equals <code>application/x-www-form-urlencoded</code>
     * 
     * @return <code>true</code> if request content type equals <code>application/x-www-form-urlencoded</code>; otherwise <code>false</code>
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

    /**
     * Gets the number of bytes that are left for being requested from web server
     * 
     * @return The number of bytes that are left for being requested
     */
    public int getNumOfBytesToRequestFor() {
        final long retval = contentLength - totalRequestedContentLength;
        if (retval > AJPv13Response.MAX_INT_VALUE || retval < 0) {
            return AJPv13Response.MAX_INT_VALUE;
        }
        return (int) retval;
    }

    /**
     * Checks if amount of received data is equal to value of header 'Content-Length'.
     * <p>
     * This method will always return false if content-length is not set unless method {@link #makeEqual()} is invoked
     * 
     * @return <code>true</code> if amount of received data is equal to value of header 'Content-Length'; otherwise <code>false</code>
     */
    public boolean isAllDataRead() {
        /*
         * This method will always return false if content-length is not set unless method makeEqual() is invoked
         */
        return (totalRequestedContentLength == contentLength);
    }

    /**
     * Indicates if servlet container still expects data from web server that is amount of received data is less than value of header
     * 'Content-Length'.
     * <p>
     * No empty data package received AND requested data length is still less than header 'Content-Length'.
     * 
     * @return <code>true</code> if servlet container still expects data from web server; otherwise <code>false</code>
     */
    public boolean isMoreDataExpected() {
        /*
         * No empty data package received AND requested data length is still less than header Content-Length
         */
        return (contentLength != NOT_SET && totalRequestedContentLength < contentLength);
    }

    /**
     * Checks if header 'Content-Length' has not been set
     * 
     * @return <code>true</code> if header 'Content-Length' has not been set; otherwise <code>false</code>
     */
    public boolean isNotSet() {
        return (contentLength == NOT_SET);
    }

    /**
     * Indicates if amount of received data exceeds value of header "content-length"
     * 
     * @return
     */
    public boolean isMoreDataReadThanExpected() {
        return (contentLength != NOT_SET && totalRequestedContentLength > contentLength);
    }

    /**
     * Total requested content length is made equal to header "content-length" which has the effect that no more data is going to be
     * requested from web server cause <code>AJPv13RequestHandler.isAllDataRead()</code> will return <code>true</code>.
     */
    public void makeEqual() {
        totalRequestedContentLength = contentLength;
    }

    /**
     * Gets the HTTP session ID
     * 
     * @return The HTTP session ID
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

    /**
     * Checks if the HTTP session has joined a previous HTTP session
     * 
     * @return <code>true</code> if the HTTP session has joined a previous HTTP session; otherwise <code>false</code>
     */
    public boolean isHttpSessionJoined() {
        return httpSessionJoined;
    }

    /**
     * Gets the servlet path (which is not the request path). The servlet path is defined in servlet mapping configuration.
     * 
     * @return The servlet path
     */
    public String getServletPath() {
        return servletPath;
    }

}
