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

package com.openexchange.tools.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.servlet.StatusKnowing;

/**
 * {@link StatusKnowingHttpServletResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class StatusKnowingHttpServletResponse implements StatusKnowing {

    private final HttpServletResponse delegate;
    private int status;

    /**
     * Initializes a new {@link StatusKnowingHttpServletResponse}.
     */
    public StatusKnowingHttpServletResponse(HttpServletResponse delegate) {
        super();
        this.delegate = delegate;
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
        return status;
    }

    @Override
    public void addCookie(Cookie cookie) {
        delegate.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return delegate.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return delegate.encodeURL(url);
    }

    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    @Override
    public String encodeRedirectURL(String url) {
        return delegate.encodeRedirectURL(url);
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String encodeUrl(String url) {
        return delegate.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return delegate.encodeRedirectUrl(url);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        delegate.sendError(sc, msg);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return delegate.getWriter();
    }

    @Override
    public void sendError(int sc) throws IOException {
        delegate.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        delegate.sendRedirect(location);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        delegate.setCharacterEncoding(charset);
    }

    @Override
    public void setDateHeader(String name, long date) {
        delegate.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        delegate.addDateHeader(name, date);
    }

    @Override
    public void setContentLength(int len) {
        delegate.setContentLength(len);
    }

    @Override
    public void setHeader(String name, String value) {
        delegate.setHeader(name, value);
    }

    @Override
    public void setContentType(String type) {
        delegate.setContentType(type);
    }

    @Override
    public void addHeader(String name, String value) {
        delegate.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        delegate.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        delegate.addIntHeader(name, value);
    }

    @Override
    public void setBufferSize(int size) {
        delegate.setBufferSize(size);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
        delegate.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
        delegate.setStatus(sc, sm);
    }

    @Override
    public int getBufferSize() {
        return delegate.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        delegate.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        delegate.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return delegate.isCommitted();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        delegate.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }


}
