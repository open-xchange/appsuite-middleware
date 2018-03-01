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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.dropbox.access;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import com.dropbox.core.http.HttpRequestor;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.version.Version;

/**
 * {@link ApacheHttpClientHttpRequestor} - Implements the <code>HttpRequestor</code> from Dropbox SDK utilizing Apache HttpClient.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApacheHttpClientHttpRequestor extends HttpRequestor {

    /**
     * Builds the default Apache HttpClient instance with the default settings for Dropbox SDK.
     *
     * @return The Apache HttpClient instance
     */
    public static CloseableHttpClient defaultApacheHttpClient() {
        HttpClients.ClientConfig config = HttpClients.ClientConfig.newInstance();
        config.setConnectionTimeout((int) DEFAULT_CONNECT_TIMEOUT_MILLIS);
        config.setSocketReadTimeout((int) DEFAULT_READ_TIMEOUT_MILLIS);
        config.setUserAgent("Open-Xchange Dropbox HttpClient v" + Version.getInstance().getVersionString());
        return HttpClients.getHttpClient(config);
    }

    /**
     * Converts specified Apache HttpClient headers to a mapping.
     *
     * @param headers The Apache HttpClient headers to convert
     * @return The header mapping
     */
    static Map<String, List<String>> fromApacheHttpHeaders(org.apache.http.Header[] headers) {
        if (null == headers || headers.length <= 0) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>(headers.length);
        for (org.apache.http.Header header : headers) {
            List<String> values = responseHeaders.get(header.getName());
            if (null == values) {
                values = new ArrayList<String>(2);
                responseHeaders.put(header.getName(), values);
            }
            values.add(header.getValue());
        }
        return responseHeaders;
    }

    // ------------------------------------------------------------------------------------------------------------

    private final CloseableHttpClient httpClient;

    /**
     * Initializes a new {@link ApacheHttpClientHttpRequestor}.
     */
    public ApacheHttpClientHttpRequestor(CloseableHttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }

    /**
     * Gets the Apache HttpClient instance
     *
     * @return The Apache HttpClient instance
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public Response doGet(String url, Iterable<Header> headers) throws IOException {
        HttpGet request = null;
        HttpResponse httpResponse = null;
        boolean error = true;
        try {
            request = new HttpGet(url);
            for (Header header : headers) {
                request.setHeader(new BasicHeader(header.getKey(), header.getValue()));
            }
            httpResponse = httpClient.execute(request);

            // Response headers
            Map<String, List<String>> responseHeaders = fromApacheHttpHeaders(httpResponse.getAllHeaders());

            // Response entity
            InputStream body;
            {
                HttpEntity entity = httpResponse.getEntity();
                if (null == entity) {
                    body = Streams.EMPTY_INPUT_STREAM;
                } else {
                    body = entity.getContent();
                }
            }

            Response response = new Response(httpResponse.getStatusLine().getStatusCode(), body, responseHeaders);
            error = false;
            return response;
        } finally {
            if (error) {
                HttpClients.close(request, httpResponse);
            }
        }
    }

    @Override
    public Uploader startPost(String url, Iterable<Header> headers) throws IOException {
        return startUpload(headers, new HttpPost(url));
    }

    @Override
    public Uploader startPut(String url, Iterable<Header> headers) throws IOException {
        return startUpload(headers, new HttpPut(url));
    }

    private Uploader startUpload(Iterable<Header> headers, final HttpEntityEnclosingRequestBase request) {
        return new HttpClientUploader(request, headers, httpClient);
    }

    private static class HttpClientUploader extends HttpRequestor.Uploader {

        private static final int BUFFER_SIZE = 5 << 20; // 5MiB, the max size for JSON requests on server

        final CloseableHttpClient httpClient;
        final HttpEntityEnclosingRequestBase httpRequest;
        private final long contentLength;
        private boolean closed;
        private boolean cancelled;
        private PipedStream body;
        private Future<HttpResponse> execution;

        HttpClientUploader(HttpEntityEnclosingRequestBase httpRequest, Iterable<Header> headers, CloseableHttpClient httpClient) {
            super();
            long contentLength = -1L;

            // Apply headers
            for (Header header : headers) {
                String name = header.getKey();
                if ("Content-Length".equals(name)) {
                    contentLength = Strings.getUnsignedLong(header.getValue().trim());
                } else {
                    httpRequest.setHeader(new BasicHeader(name, header.getValue()));
                }
            }

            this.contentLength = contentLength;
            this.httpRequest = httpRequest;
            this.httpClient = httpClient;
            this.closed = false;
            cancelled = false;
        }

        /**
         * Forces the release of an HttpMethod's connection in a way that will
         * perform all the necessary cleanup through the correct use of HttpClient
         * methods.
         */
        private void releaseConnection(boolean abort) {
            if (!closed) {
                if (abort) {
                    httpRequest.abort();
                }
                if (null != body) {
                    body.close();
                }
                if (null != execution) {
                    execution.cancel(true);
                }
                closed = true;
            }
        }

        @Override
        public OutputStream getBody() {
            if (null != body) {
                return body.getOutputStream();
            }

            PipedStream pipedStream = new PipedStream(BUFFER_SIZE);
            body = pipedStream;
            httpRequest.setEntity(new InputStreamEntity(pipedStream.getInputStream(), contentLength));
            execution = Executors.newSingleThreadExecutor().submit(new Callable<HttpResponse>() {

                @Override
                public HttpResponse call() throws Exception {
                    return httpClient.execute(httpRequest);
                }
            });

            return pipedStream.getOutputStream();
        }

        @Override
        public void close() {
            releaseConnection(false);
        }

        @Override
        public void abort() {
            cancelled = true;
            releaseConnection(true);
        }

        @Override
        public Response finish() throws IOException {
            if (cancelled) {
                throw new IllegalStateException("Already aborted");
            }

            HttpResponse httpResponse;
            {
                Future<HttpResponse> execution = this.execution;
                if (null == execution) {
                    // Nothing written to output stream
                    httpResponse = httpClient.execute(httpRequest);
                } else {
                    try {
                        httpResponse = execution.get();
                    } catch (InterruptedException e) {
                        // Keep interrupted state
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException("Interrupted");
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof IOException) {
                            throw (IOException) cause;
                        }
                        throw new IOException(cause.getMessage(), cause);
                    }
                }

            }

            // Response headers
            Map<String, List<String>> responseHeaders = fromApacheHttpHeaders(httpResponse.getAllHeaders());

            // Response entity
            InputStream body;
            {
                HttpEntity entity = httpResponse.getEntity();
                if (null == entity) {
                    body = Streams.EMPTY_INPUT_STREAM;
                } else {
                    body = entity.getContent();
                }
            }

            return new Response(httpResponse.getStatusLine().getStatusCode(), body, responseHeaders);
        }
    }

    private static final class PipedStream implements Closeable {

        private final PipedInputStream in;
        private final PipedOutputStream out;

        PipedStream(int bufferSize) {
            super();
            this.in = new PipedInputStream(bufferSize);
            try {
                this.out = new PipedOutputStream(in);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to create piped stream for async upload request.");
            }
        }

        InputStream getInputStream() {
            return in;
        }

        OutputStream getOutputStream() {
            return out;
        }

        @Override
        public void close() {
            Streams.close(in, out);
        }
    }

}
