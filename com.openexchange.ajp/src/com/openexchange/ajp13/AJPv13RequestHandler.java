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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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


package com.openexchange.ajp13;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.servlet.http.HttpServletRequestWrapper;
import com.openexchange.ajp13.servlet.http.HttpServletResponseWrapper;

public interface AJPv13RequestHandler {

    public static enum State {
        IDLE, ASSIGNED
    }

    /**
     * First magic byte indicating a packet from Web Server to Servlet Container.
     */
    public static final int MAGIC1_SERVER_TO_CONTAINER = 0x12;

    /**
     * Second magic byte indicating a packet from Web Server to Servlet Container.
     */
    public static final int MAGIC2_SERVER_TO_CONTAINER = 0x34;

    /**
     * Starts the request handle cycle with following data.
     *
     * @value 2
     */
    public static final int FORWARD_REQUEST_PREFIX_CODE = 2;

    /**
     * Web Server asks to shut down the Servlet Container.
     *
     * @value 7
     */
    public static final int SHUTDOWN_PREFIX_CODE = 7;

    /**
     * Web Server asks the Servlet Container to take control (secure login phase).
     *
     * @value 8
     */
    public static final int PING_PREFIX_CODE = 8;

    /**
     * Web Server asks the Servlet Container to respond quickly with a CPong.
     *
     * @value 10
     */
    public static final int CPING_PREFIX_CODE = 10;

    /**
     * The name of the JSESSIONID cookie
     */
    public static final String JSESSIONID_COOKIE = "JSESSIONID";

    /**
     * The JSESSIONID URI parameter
     */
    public static final String JSESSIONID_URI = ";jsessionid=";

    /**
     * The value for a missing <i>Content-Length</i> header
     *
     * @value -1
     */
    public static final int NOT_SET = -1;

    /**
     * Processes an incoming AJP package from web server. If first package of an AJP cycle is processed its prefix code determines further
     * handling. Any subsequent packages are treated as data-only packages.
     *
     * @throws AJPv13Exception If package processing fails
     */
    public void processPackage() throws AJPv13Exception;

    /**
     * Creates and writes the AJP response package corresponding to formerly received AJP package.
     *
     * @throws AJPv13Exception If an AJP error occurs
     * @throws ServletException If processing the request fails
     */
    public void createResponse() throws AJPv13Exception, ServletException;

    /**
     * Gets the AJP connection of this request handler
     *
     * @return The AJP connection of this request handler
     */
    public AJPv13Connection getAJPConnection();

    /**
     * Releases associated servlet instance and resets request handler to hold initial values
     */
    public void reset();

    /**
     * Sets this request handler's servlet reference to the one bound to given path argument
     *
     * @param pathArg The request path
     */
    public void setServletInstance(final String pathArg);

    /**
     * Triggers the servlet's service method to start processing the request and flushes the response to output stream.
     * <p>
     * This request handler is then marked to have the service() method called; meaning {@link #isServiceMethodCalled()} will return
     * <code>true</code>.
     *
     * @throws IOException If an I/O error occurs
     * @throws ServletException If a servlet error occurs
     */
    public void doServletService() throws ServletException, IOException;

    /**
     * Writes the HTTP headers to specified output stream if not already written.
     *
     * @param out The output stream
     * @throws AJPv13Exception If composing the <code>SEND_HEADERS</code> package fails
     * @throws IOException If an I/O error occurs
     */
    public void doWriteHeaders(final BlockableBufferedOutputStream out) throws AJPv13Exception, IOException;

    /**
     * Gets the response output stream's data and clears it
     *
     * @return The response output stream's data
     * @throws IOException If an I/O error occurs
     */
    public byte[] getAndClearResponseData() throws IOException;

    /**
     * Parses given form's data into servlet request
     *
     * @param contentBytes The content bytes representing a form's data
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public void doParseQueryString(final byte[] contentBytes) throws UnsupportedEncodingException;

    /**
     * Gets the content length
     *
     * @return The content length
     */
    public long getContentLength();

    /**
     * @return <code>true</code> if content length has been set; otherwise <code>false</code>
     */
    public boolean containsContentLength();

    /**
     * Gets the total requested content length
     *
     * @return The total requested content length
     */
    public long getTotalRequestedContentLength();

    /**
     * Increases the total requested content length by specified argument
     *
     * @param increaseBy The value by which the total requested content length is increased
     */
    public void increaseTotalRequestedContentLength(final long increaseBy);

    /**
     * Checks if the <code>service()</code> method has already been called
     *
     * @return <code>true</code> if <code>service()</code> method has already been called; otherwise <code>false</code>
     */
    public boolean isServiceMethodCalled();

    /**
     * Checks if AJP's end response package has been sent to web server
     *
     * @return <code>true</code> if AJP's end response package has been sent to web server; otherwise <code>false</code>
     */
    public boolean isEndResponseSent();

    /**
     * Indicates if request content type equals <code>application/x-www-form-urlencoded</code>
     *
     * @return <code>true</code> if request content type equals <code>application/x-www-form-urlencoded</code>; otherwise <code>false</code>
     */
    public boolean isFormData();

    /**
     * Gets the number of bytes that are left for being requested from web server
     *
     * @return The number of bytes that are left for being requested
     */
    public int getNumOfBytesToRequestFor();

    /**
     * Checks if amount of received data is equal to value of header 'Content-Length'.
     * <p>
     * This method will always return false if content-length is not set unless method {@link #makeEqual()} is invoked
     *
     * @return <code>true</code> if amount of received data is equal to value of header 'Content-Length'; otherwise <code>false</code>
     */
    public boolean isAllDataRead();

    /**
     * Indicates if servlet container still expects data from web server that is amount of received data is less than value of header
     * 'Content-Length'.
     * <p>
     * No empty data package received AND requested data length is still less than header 'Content-Length'.
     *
     * @return <code>true</code> if servlet container still expects data from web server; otherwise <code>false</code>
     */
    public boolean isMoreDataExpected();

    /**
     * Checks if header 'Content-Length' has not been set
     *
     * @return <code>true</code> if header 'Content-Length' has not been set; otherwise <code>false</code>
     */
    public boolean isNotSet();

    /**
     * Indicates if amount of received data exceeds value of header "content-length"
     *
     * @return
     */
    public boolean isMoreDataReadThanExpected();

    /**
     * Total requested content length is made equal to header "content-length" which has the effect that no more data is going to be
     * requested from web server cause <code>AJPv13RequestHandler.isAllDataRead()</code> will return <code>true</code>.
     */
    public void makeEqual();

    /**
     * Gets the HTTP session cookie.
     *
     * @return The HTTP session cookie
     */
    public Cookie getHttpSessionCookie();

    /**
     * Checks if the HTTP session has joined a previous HTTP session
     *
     * @return <code>true</code> if the HTTP session has joined a previous HTTP session; otherwise <code>false</code>
     */
    public boolean isHttpSessionJoined();

    /**
     * Checks if headers have been sent already.
     *
     * @return <code>true</code> if headers have been sent; otherwise <code>false</code>
     */
    public boolean isHeadersSent();

    /**
     * Gets the servlet path (which is not the request path). The servlet path is defined in servlet mapping configuration.
     *
     * @return The servlet path
     */
    public String getServletPath();

    /**
     * Sets the end response flag
     */
    public void setEndResponseSent();

    /**
     * Sets the request's content length
     *
     * @param contentLength The content length
     */
    public void setContentLength(long contentLength);

    /**
     * Marks that requests content type equals <code>application/x-www-form-urlencoded</code>
     *
     * @param isFormData <code>true</code> if request content type equals <code>application/x-www-form-urlencoded</code>; otherwise
     *            <code>false</code>
     */
    public void setFormData(boolean isFormData);

    /**
     * Sets the HTTP session cookie.
     *
     * @param httpSessionCookie The HTTP session cookie
     * @param join <code>true</code> if the HTTP session has joined a previous HTTP session; otherwise <code>false</code>
     */
    public void setHttpSessionCookie(Cookie httpSessionCookie, boolean join);

    /**
     * Sets the servlet request.
     *
     * @param request The servlet request
     */
    public void setServletRequest(HttpServletRequestWrapper request);

    /**
     * Gets the servlet request.
     *
     * @return The servlet request
     */
    public HttpServletRequest getServletRequest();

    /**
     * Sets the servlet response.
     *
     * @param response The servlet response
     */
    public void setServletResponse(HttpServletResponseWrapper response);

    /**
     * Gets the servlet response.
     *
     * @return The servlet response
     */
    public HttpServletResponse getServletResponse();

    /**
     * Gets the forward request's bytes as a formatted string or "&lt;not enabled&gt;" if not enabled via configuration.
     *
     * @return The forward request's bytes as a formatted string
     */
    public String getForwardRequest();

    /**
     * Sets/appends new data to servlet request's input stream.
     *
     * @param newData The new data to set
     * @throws IOException If an I/O error occurs
     */
    public void setData(final byte[] newData) throws IOException;

    /**
     * Peeks available data from servlet request's input stream.
     *
     * @return Available data from servlet request's input stream.
     * @throws IOException If an I/O error occurs
     */
    public byte[] peekData() throws IOException;
}
