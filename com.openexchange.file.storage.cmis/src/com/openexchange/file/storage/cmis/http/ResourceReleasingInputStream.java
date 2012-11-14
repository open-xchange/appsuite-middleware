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

package com.openexchange.file.storage.cmis.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import com.openexchange.java.Streams;

/**
 * {@link ResourceReleasingInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceReleasingInputStream extends BufferedInputStream {

    private static final int defaultBufferSize = 8192;

    private final DefaultHttpClient httpClient;

    private final HttpRequestBase httpRequest;

    /**
     * Initializes a new {@link ResourceReleasingInputStream}.
     * 
     * @param in The input stream
     * @param httpRequest The associated HTTP request
     * @param httpClient The associated HTTP client
     */
    public ResourceReleasingInputStream(InputStream in, HttpRequestBase httpRequest, DefaultHttpClient httpClient) {
        this(in, defaultBufferSize, httpRequest, httpClient);
    }

    /**
     * Initializes a new {@link ResourceReleasingInputStream}.
     * 
     * @param in The input stream
     * @param size The initial buffer size (greater than zero)
     * @param httpRequest The associated HTTP request
     * @param httpClient The associated HTTP client
     */
    public ResourceReleasingInputStream(InputStream in, final int size, HttpRequestBase httpRequest, DefaultHttpClient httpClient) {
        super(toBufferedInputStream(in, size), 1);
        this.httpRequest = httpRequest;
        this.httpClient = httpClient;
    }

    private static BufferedInputStream toBufferedInputStream(InputStream in, final int size) {
        if (in instanceof BufferedInputStream) {
            return (BufferedInputStream) in;
        }
        return new BufferedInputStream(in, size);
    }

    @Override
    public int read() throws IOException {
        try {
            return in.read();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        try {
            return read(b, 0, b.length);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        try {
            return in.read(b, off, len);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return in.skip(n);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return in.available();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public void close() {
        // Safely close stream and...
        Streams.close(in);
        // ... release HTTP resources immediately
        if (null != httpRequest) {
            httpRequest.reset();
        }
        if (null != httpClient) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
}