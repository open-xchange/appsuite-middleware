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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package javax.servlet.http.sim;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * {@link SimHttpServletResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SimHttpServletResponse implements HttpServletResponse {

    private final Map<String, String> headers;
    private String characterEncoding;
    private ServletOutputStream outputStream;
    private boolean committed;
    private Locale locale;
    private final List<Cookie> cookies = new LinkedList<Cookie>();
    private int status;
    private String statusMessage;

    /**
     * Initializes a new {@link SimHttpServletResponse}.
     */
    public SimHttpServletResponse() {
        super();
        headers = new HashMap<String, String>(8);
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return headers.get("content-type");
    }

    public int getContentLength() {
        final String string = headers.get("content-length");
        if (null == string) {
            return -1;
        }

        try {
            final int ret = (int) Long.parseLong(string);
            return ret < 0 ? -1 : ret;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    /**
     * Sets the outputStream
     *
     * @param outputStream The outputStream to set
     */
    public void setOutputStream(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(outputStream, characterEncoding));
    }

    @Override
    public void setCharacterEncoding(String charset) {
        characterEncoding = charset;
    }

    @Override
    public void setContentLength(int len) {
        headers.put("content-length", Integer.toString(len));
    }

    @Override
    public void setContentType(String type) {
        headers.put("content-type", type);
    }

    @Override
    public void setBufferSize(int size) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Sets the committed
     *
     * @param committed The committed to set
     */
    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * Gets the headers
     *
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    @Override
    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        status = sc;
        statusMessage = msg;
    }

    @Override
    public void sendError(int sc) throws IOException {
        status = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(HttpServletResponse.SC_FOUND);
        setHeader("location", location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.put(toLowerCase(name), Long.toString(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.put(toLowerCase(name), Long.toString(date));
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(toLowerCase(name), value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(toLowerCase(name), value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.put(toLowerCase(name), Integer.toString(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.put(toLowerCase(name), Integer.toString(value));
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    /**
     * Gets the status
     *
     * @return The status
     */
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
        this.statusMessage = sm;
    }

    /**
     * Gets the statusMessage
     *
     * @return The statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
