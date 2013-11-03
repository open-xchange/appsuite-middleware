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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

/**
 * {@link HttpServletResponseWrapper} - Wraps an HttpServletResponse and delegates all calls that we don't need to
 * modify to the response object. Other methods are modified to keep compatibility to the old
 * {@link com.openexchange.http.grizzly.wrapper.ajp13.servlet.http.HttpServletResponseWrapper} as good as we can using Grizzly.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HttpServletResponseWrapper implements HttpServletResponse {

    private final HttpServletResponse httpServletResponse;

    public HttpServletResponseWrapper(final HttpServletResponse httpServletResponse) {
        super();
        this.httpServletResponse = httpServletResponse;
    }

    /**
     * Set a header to a specified value or remove a header from this response.
     *
     * @param headerName The name of the header that should be set or removed
     * @param headerValue If the value is null then the header will be removed, otherwise the header will be set to the specified value
     * @throws IllegalStateException if the underlying Response is already committed
     */
    @Override
    public void setHeader(final String headerName, final String headerValue) {
        final Response internalResponse = ServletUtils.getInternalResponse(httpServletResponse);

        if (httpServletResponse.isCommitted()) {
            throw new IllegalStateException("Respone is already committed");
        }

        if (headerValue == null) {
            final HttpResponsePacket httpResponsePacket = internalResponse.getResponse();
            final MimeHeaders responseMimeHeaders = httpResponsePacket.getHeaders();
            responseMimeHeaders.removeHeader(headerName);
        } else { // delegate to the original response implementation
            httpServletResponse.setHeader(headerName, headerValue);
        }
    }

    @Override
    public void addCookie(final Cookie cookie) {
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(final String name) {
        return httpServletResponse.containsHeader(name);
    }

    @Override
    public String encodeURL(final String url) {
        return httpServletResponse.encodeURL(url);
    }

    @Override
    public String getCharacterEncoding() {
        return httpServletResponse.getCharacterEncoding();
    }

    @Override
    public String encodeRedirectURL(final String url) {
        return httpServletResponse.encodeRedirectURL(url);
    }

    @Override
    public String getContentType() {
        return httpServletResponse.getContentType();
    }

    @Override
    public String encodeUrl(final String url) {
        return httpServletResponse.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(final String url) {
        return httpServletResponse.encodeRedirectUrl(url);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return httpServletResponse.getOutputStream();
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException {
        httpServletResponse.sendError(sc, msg);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return httpServletResponse.getWriter();
    }

    @Override
    public void sendError(final int sc) throws IOException {
        httpServletResponse.sendError(sc);
    }

    @Override
    public void setCharacterEncoding(final String charset) {
        httpServletResponse.setCharacterEncoding(charset);
    }

    @Override
    public void sendRedirect(final String location) throws IOException {
        httpServletResponse.sendRedirect(location);
    }

    @Override
    public void setDateHeader(final String name, final long date) {
        httpServletResponse.setDateHeader(name, date);
    }

    @Override
    public void setContentLength(final int len) {
        httpServletResponse.setContentLength(len);
    }

    @Override
    public void addDateHeader(final String name, final long date) {
        httpServletResponse.addDateHeader(name, date);
    }

    @Override
    public void setContentType(final String type) {
        httpServletResponse.setContentType(type);
    }

    @Override
    public void addHeader(final String name, final String value) {
        httpServletResponse.addHeader(name, value);
    }

    @Override
    public void setBufferSize(final int size) {
        httpServletResponse.setBufferSize(size);
    }

    @Override
    public void setIntHeader(final String name, final int value) {
        httpServletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(final String name, final int value) {
        httpServletResponse.addIntHeader(name, value);
    }

    @Override
    public void setStatus(final int sc) {
        httpServletResponse.setStatus(sc);
    }

    @Override
    public int getBufferSize() {
        return httpServletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        httpServletResponse.flushBuffer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setStatus(final int sc, final String sm) {
        httpServletResponse.setStatus(sc, sm);
    }

    @Override
    public void resetBuffer() {
        httpServletResponse.resetBuffer();
    }

    @Override
    public int getStatus() {
        return httpServletResponse.getStatus();
    }

    @Override
    public boolean isCommitted() {
        return httpServletResponse.isCommitted();
    }

    @Override
    public String getHeader(final String name) {
        return httpServletResponse.getHeader(name);
    }

    @Override
    public void reset() {
        httpServletResponse.reset();
    }

    @Override
    public Collection<String> getHeaders(final String name) {
        return httpServletResponse.getHeaders(name);
    }

    @Override
    public void setLocale(final Locale loc) {
        httpServletResponse.setLocale(loc);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return httpServletResponse.getHeaderNames();
    }

    @Override
    public Locale getLocale() {
        return httpServletResponse.getLocale();
    }

}
