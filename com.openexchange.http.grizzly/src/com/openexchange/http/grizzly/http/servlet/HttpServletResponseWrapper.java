/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.http.grizzly.http.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.servlet.ServletUtils;
import com.openexchange.java.Strings;
import com.openexchange.servlet.StatusKnowing;

/**
 * {@link HttpServletResponseWrapper} - Wraps an HttpServletResponse and delegates all calls that we don't need to modify to the response
 * object.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
@SuppressWarnings("deprecation")
public class HttpServletResponseWrapper implements StatusKnowing {

    private final HttpServletResponse delegate;
    private final String echoHeaderName;
    private final String echoHeaderValue;
    private int statusCode;

    /**
     * Initializes a new {@link HttpServletResponseWrapper}. Incorporates the echo header when using KippData's mod_id in case somebody
     * calls {@link HttpServletResponse#reset}.
     *
     * @param httpServletResponse The response to wrap
     * @param echoHeaderName The name of the echo header when using KippData's mod_id
     * @param echoHeaderValue The value of the echo header when using KippData's mod_id.
     */
    public HttpServletResponseWrapper(HttpServletResponse httpServletResponse, String echoHeaderName, String echoHeaderValue) {
        super();
        this.delegate = httpServletResponse;
        this.echoHeaderName = echoHeaderName;
        this.echoHeaderValue = echoHeaderValue;
    }

    @Override
    public void setContentLengthLong(long len) {
        delegate.setContentLengthLong(len);
    }

    @Override
    public String getHeader(String name) {
        return delegate.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return delegate.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return delegate.getHeaderNames();
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public void addCookie(Cookie arg0) {
        delegate.addCookie(arg0);
    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
        delegate.addDateHeader(arg0, arg1);
    }

    @Override
    public void addHeader(String arg0, String arg1) {
        delegate.addHeader(arg0, arg1);
    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
        delegate.addIntHeader(arg0, arg1);
    }

    @Override
    public boolean containsHeader(String arg0) {
        return delegate.containsHeader(arg0);
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        return delegate.encodeRedirectURL(arg0);
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        return delegate.encodeRedirectUrl(arg0);
    }

    @Override
    public String encodeURL(String arg0) {
        return delegate.encodeURL(arg0);
    }

    @Override
    public String encodeUrl(String arg0) {
        return delegate.encodeUrl(arg0);
    }

    @Override
    public void flushBuffer() throws IOException {
        delegate.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return delegate.getBufferSize();
    }

    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return delegate.getWriter();
    }

    @Override
    public boolean isCommitted() {
        return delegate.isCommitted();
    }

    @Override
    public void reset() {
        delegate.reset();
        //Don't reset the com.openexchange.servlet.echoHeaderName if present
        if (Strings.isNotEmpty(echoHeaderValue)) {
            setHeader(echoHeaderName, echoHeaderValue);
        }
    }

    @Override
    public void resetBuffer() {
        delegate.resetBuffer();
    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        delegate.sendError(arg0, arg1);
    }

    @Override
    public void sendError(int arg0) throws IOException {
        delegate.sendError(arg0);
    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
        delegate.sendRedirect(arg0);
    }

    @Override
    public void setBufferSize(int arg0) {
        delegate.setBufferSize(arg0);
    }

    @Override
    public void setCharacterEncoding(String arg0) {
        delegate.setCharacterEncoding(arg0);
    }

    @Override
    public void setContentLength(int arg0) {
        delegate.setContentLength(arg0);
    }

    @Override
    public void setContentType(String arg0) {
        delegate.setContentType(arg0);
    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
        delegate.setDateHeader(arg0, arg1);
    }

    /**
     * Set a header to a specified value or remove a header from this response.
     *
     * @param headerName The name of the header that should be set or removed
     * @param headerValue If the value is null then the header will be removed, otherwise the header will be set to the specified value
     * @throws IllegalStateException if the underlying Response is already committed
     */
    @Override
    public void setHeader(String headerName, String headerValue) {
        if (delegate.isCommitted()) {
            throw new IllegalStateException("Response is already committed");
        }

        if (headerValue == null) {
            Response internalResponse = ServletUtils.getInternalResponse(delegate);
            HttpResponsePacket httpResponsePacket = internalResponse.getResponse();
            MimeHeaders responseMimeHeaders = httpResponsePacket.getHeaders();
            responseMimeHeaders.removeHeader(headerName);
        } else {
            // Delegate to the original response implementation
            delegate.setHeader(headerName, headerValue);
        }
    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
        delegate.setIntHeader(arg0, arg1);
    }

    @Override
    public void setLocale(Locale arg0) {
        delegate.setLocale(arg0);
    }

    @Override
    public void setStatus(int arg0, String arg1) {
        this.statusCode = arg0;
        delegate.setStatus(arg0, arg1);
    }

    @Override
    public void setStatus(int arg0) {
        this.statusCode = arg0;
        delegate.setStatus(arg0);
    }
}
