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

package com.openexchange.file.storage.dropbox.access;

import static com.openexchange.file.storage.dropbox.DropboxServices.getService;
import static com.openexchange.file.storage.dropbox.http.DropboxHttpClientConfiguration.HTTP_CLIENT_DROPBOX;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import com.dropbox.core.http.HttpRequestor;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;

/**
 * {@link ApacheHttpClientHttpRequestor} - Implements the <code>HttpRequestor</code> from Dropbox SDK utilizing Apache HttpClient.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApacheHttpClientHttpRequestor extends HttpRequestor {

    /**
     * Initializes a new {@link ApacheHttpClientHttpRequestor}.
     *
     */
    public ApacheHttpClientHttpRequestor() {
        super();
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
            httpResponse = getHttpClient().execute(request);

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
            } else {
                HttpClients.close(httpResponse, false);
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
        return new HttpClientUploader(request, headers);
    }

    private static class HttpClientUploader extends HttpRequestor.Uploader {

        private static final int BUFFER_SIZE = 5 << 20; // 5MiB, the max size for JSON requests on server

        final HttpEntityEnclosingRequestBase httpRequest;
        private final long contentLength;
        private boolean closed;
        private boolean cancelled;
        private PipedStream body;
        private Future<HttpResponse> execution;

        HttpClientUploader(HttpEntityEnclosingRequestBase httpRequest, Iterable<Header> headers) {
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
                    return getHttpClient().execute(httpRequest);
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
                    httpResponse = getHttpClient().execute(httpRequest);
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

    static HttpClient getHttpClient() {
        return getService(HttpClientService.class).getHttpClient(HTTP_CLIENT_DROPBOX);
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
