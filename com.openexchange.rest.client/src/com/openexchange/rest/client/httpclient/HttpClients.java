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

package com.openexchange.rest.client.httpclient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

/**
 * {@link HttpClients} - Utility class for HTTP client.
 * <p>
 * See <a href="http://svn.apache.org/repos/asf/httpcomponents/httpclient/branches/4.0.x/httpclient/src/examples/org/apache/http/examples/client/">here</a> for several examples.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class HttpClients {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpClients.class);

    /**
     * Initializes a new {@link HttpClients}.
     */
    private HttpClients() {
        super();
    }

    /** The default timeout for client connections. */
    private static final int DEFAULT_TIMEOUT_MILLIS = 30000;

    /**
     * Applies the default timeout of 30sec to given HTTP request.
     *
     * @param request The HTTP request
     */
    public static void setDefaultRequestTimeout(HttpRequestBase request) {
        if (null == request) {
            return;
        }
        request.setConfig(RequestConfig.custom().setConnectTimeout(DEFAULT_TIMEOUT_MILLIS).setSocketTimeout(DEFAULT_TIMEOUT_MILLIS).build());
    }

    /**
     * Applies the specified timeout to given HTTP request.
     *
     * @param timeoutMillis The timeout in milliseconds to apply
     * @param request The HTTP request
     */
    public static void setRequestTimeout(int timeoutMillis, HttpRequestBase request) {
        if (null == request || timeoutMillis <= 0) {
            return;
        }
        request.setConfig(RequestConfig.custom().setConnectTimeout(timeoutMillis).setSocketTimeout(timeoutMillis).build());
    }

    /**
     * Closes the supplied HTTP request / response resources silently.
     * <p>
     * <ul>
     * <li>Resets internal state of the HTTP request making it reusable.</li>
     * <li>Ensures that the response's content is fully consumed and the content stream, if exists, is closed</li>
     * <li>Checks if HTTP response is an instance of {@link CloseableHttpResponse}. If so <code>close()</code> is invoked</li>
     * </ul>
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    public static void close(HttpRequestBase request, HttpResponse response) {
        close(response, true);
        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                LOG.trace("Failed to reset request for making it reusable.", e);
            }
        }
    }

    /**
     * Closes the supplied HTTP response resource silently.
     * <p>
     * <ul>
     * <li>Ensures that the response's content is fully consumed and the content stream, if exists, is closed (provided that <code>consumeEntity</code> is set to <code>true</code>)</li>
     * <li>Checks if HTTP response is an instance of {@link CloseableHttpResponse}. If so <code>close()</code> is invoked</li>
     * </ul>
     *
     * @param response The HTTP response to consume and close
     * @param consumeEntity Whether to consume the message entity of given HTTP response
     */
    public static void close(HttpResponse response, boolean consumeEntity) {
        if (null != response) {
            if (consumeEntity) {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    try {
                        EntityUtils.consumeQuietly(entity);
                    } catch (Exception e) {
                        LOG.trace("Failed to ensure that the entity content is fully consumed and the content stream, if exists, is closed.", e);
                    }
                }
            }
            if (response instanceof CloseableHttpResponse) {
                try {
                    ((CloseableHttpResponse) response).close();
                } catch (Exception e) {
                    LOG.debug("Error closing HTTP response", e);
                }
            }
        }
    }

    /**
     * Aborts the supplied HTTP request silently.
     * <p>
     * Any active execution of this method should return immediately. If the request has not started, it will abort after the next execution.
     * Aborting this request will cause all subsequent executions with this request to fail.
     *
     * @param request The HTTP request to abort
     */
    public static void abort(HttpRequestBase request) {
        if (null != request) {
            try {
                request.abort();
            } catch (Exception e) {
                LOG.trace("Failed to reset request for making it reusable.", e);
            }
        }
    }

    // ----------------------------------------------- Response entity stream --------------------------------------------------------------

    /**
     * Initializes a new {@link HttpResponseStream} for given HTTP response.
     *
     * @param response The HTTP response to create the stream for
     * @return The newly created response stream
     * @throws IOException If initialization fails
     */
    public static HttpResponseStream createHttpResponseStreamFor(HttpResponse response) throws IOException {
        if (response == null) {
            return null;
        }

        HttpEntity entity = response.getEntity();
        if (null == entity) {
            throw new IOException("No response entity");
        }

        long contentLength = entity.getContentLength();
        if (contentLength < 0) {
            // No content length advertised
            return new HttpResponseStream(entity.getContent(), response);
        }

        /*-
         * Content length advertised. Hence, an instance of `org.apache.http.impl.io.ContentLengthInputStream` represents actual entity's
         * content stream. Closing such an instance results in unnecessarily all remaining data being read from stream:
         *
         * public void close() throws IOException {
         *  ...
         *  if (pos < contentLength) {
         *      final byte buffer[] = new byte[BUFFER_SIZE];
         *      while (read(buffer) >= 0) { <------------------ Read data until EOF
         *        // do nothing.
         *      }
         *  }
         *  ...
         * }
         */
        return new ContentLengthAwareHttpResponseStream(contentLength, entity.getContent(), response);
    }

    private static class HttpResponseStream extends FilterInputStream {

        /** The HTTP response whose entity's content is read from */
        protected final HttpResponse response;

        /**
         * Initializes a new {@link HttpResponseStream}.
         *
         * @param entityStream The response entity's input stream
         * @param response The HTTP response whose entity stream shall be read from
         */
        HttpResponseStream(InputStream entityStream, HttpResponse response) {
            super(entityStream);
            this.response = response;
        }

        @Override
        public void close() throws IOException {
            try {
                HttpClients.close(response, true);
            } finally {
                super.close();
            }
        }
    } // End of class HttpResponseStream

    private static class ContentLengthAwareHttpResponseStream extends HttpResponseStream {

        /** The current position */
        private long pos = 0;

        /** The marked position */
        private long mark = -1;

        /** The length of the content on bytes */
        private final long contentLength;

        /**
         * Initializes a new {@link HttpResponseStream}.
         *
         * @param contentLength The length of the content, which is the number of bytes of the content, or a negative number if unknown
         * @param entityStream The response entity's input stream
         * @param response The HTTP response whose entity stream shall be read from
         */
        ContentLengthAwareHttpResponseStream(long contentLength, InputStream entityStream, HttpResponse response) {
            super(entityStream, response);
            this.contentLength = contentLength;
        }

        @Override
        public void close() throws IOException {
            try {
                if (0 < contentLength && contentLength > pos) {
                    // Invoke with consumeEntity=false since stream is closed in finally block
                    HttpClients.close(response, false);
                } else {
                    HttpClients.close(response, true);
                }
            } finally {
                in.close();
            }
        }

        @Override
        public int read() throws IOException {
            int read = super.read();
            if (read >= 0) {
                pos++;
            }
            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int readLen = super.read(b, off, len);
            if (readLen > 0) {
                pos += readLen;
            }
            return readLen;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipLen = super.skip(n);
            if (skipLen > 0) {
                pos += skipLen;
            }
            return skipLen;
        }

        @Override
        public synchronized void mark(int readlimit) {
            super.mark(readlimit);
            mark = pos;
        }

        @Override
        public synchronized void reset() throws IOException {
            super.reset();
            pos = mark;
        }
    } // End of class ContentLengthAwareHttpResponseStream

}
