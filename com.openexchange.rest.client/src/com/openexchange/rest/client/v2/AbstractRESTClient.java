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

package com.openexchange.rest.client.v2;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.v2.parser.RESTResponseParser;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractRESTClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractRESTClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRESTClient.class);

    /** The service look-up providing tracked OSGi services */
    protected final ServiceLookup services;

    private final RESTResponseParser parser;
    private final String httpClientId;

    /**
     * Initializes a new {@link AbstractRESTClient}.
     *
     * @param services The service lookup to get the {@link HttpClientService} from
     * @param httpClientId The service ID to get the HTTP client for
     * @param parser The {@link RESTResponseParser} to use when parsing the responses
     */
    protected AbstractRESTClient(ServiceLookup services, String httpClientId, RESTResponseParser parser) {
        super();
        this.parser = parser;
        this.services = services;
        this.httpClientId = httpClientId;
    }

    /**
     * Executes the specified {@link HttpRequestBase} and returns the response.
     *
     * @param httpRequest The HTTP request to execute
     * @return The parsed HTTP REST response
     * @throws OXException if a client protocol error or an I/O error occurs
     */
    public RESTResponse executeRequest(HttpRequestBase httpRequest) throws OXException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = execute(httpRequest);
            return parser.parse(httpResponse);
        } catch (ClientProtocolException e) {
            throw RESTExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } finally {
            HttpClients.close(httpRequest, httpResponse);
        }
    }

    /**
     * Executes the specified {@link HttpRequestBase} and returns the {@link InputStream}
     * of the response. Use to stream data to client. Clients are obliged to close the
     * returning {@link InputStream}.
     *
     * @param httpRequest The HTTP request to execute
     * @return The {@link InputStream} of the response
     * @throws OXException if a client protocol error or an I/O error occurs
     */
    public InputStream download(HttpRequestBase httpRequest) throws OXException {
        HttpResponse httpResponse = null;
        boolean success = false;
        try {
            httpResponse = execute(httpRequest);
            success = httpResponse.getStatusLine().getStatusCode() == 200;
            if (success) {
                return httpResponse.getEntity().getContent();
            }
            throw RESTExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        } catch (ClientProtocolException e) {
            throw RESTExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } finally {
            if (success) {
                httpRequest.reset();
            } else {
                HttpClients.abort(httpRequest);
                HttpClients.close(httpRequest, httpResponse);
            }
        }
    }

    /**
     * Executes the specified {@link HttpRequestBase} and returns the {@link InputStream}
     * of the response. Use to stream data to client. Clients are obliged to close the
     * returning {@link InputStream}.
     *
     * @param httpRequestProvider The HTTP request provider
     * @return The {@link InputStream} of the response
     * @throws OXException if a client protocol error or an I/O error occurs
     */
    public InputStream download(HttpRequestProvider httpRequestProvider) throws OXException {
        // Create first request
        HttpRequestBase httpRequest = httpRequestProvider.createRequest();

        // Execute it
        HttpResponse httpResponse = null;
        boolean success = false;
        try {
            httpResponse = execute(httpRequest);
            success = httpResponse.getStatusLine().getStatusCode() == 200;
            if (success) {
                InputStream contentStream = httpResponse.getEntity().getContent();
                return new ResumableAbortIfNotConsumedInputStream(contentStream, httpRequest, httpResponse, httpRequestProvider, this);
            }
            throw RESTExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        } catch (ClientProtocolException e) {
            throw RESTExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } finally {
            if (success == false) {
                HttpClients.abort(httpRequest);
                HttpClients.close(httpRequest, httpResponse);
            }
        }
    }

    //////////////////////////////// HELPERS ////////////////////////////

    /**
     * Creates an {@link HttpRequestBase} with the specified {@link HttpMethod}
     *
     * @param httpMethod The {@link HttpMethod}
     * @return The new {@link HttpRequestBase}
     * @throws OXException if an unknown HTTP method is provided
     */
    protected HttpRequestBase createRequest(RESTMethod httpMethod) throws OXException {
        HttpRequestBase httpRequest;
        switch (httpMethod) {
            case GET:
                httpRequest = new HttpGet();
                break;
            case HEAD:
                httpRequest = new HttpHead();
                break;
            case POST:
                httpRequest = new HttpPost();
                break;
            case PUT:
                httpRequest = new HttpPut();
                break;
            case DELETE:
                httpRequest = new HttpDelete();
                break;
            case PATCH:
                httpRequest = new HttpPatch();
                break;
            default:
                throw RESTExceptionCodes.UNSUPPORTED_METHOD.create(httpMethod);
        }
        return httpRequest;
    }

    /**
     * Add any additional headers to the request
     *
     * @param request The request to add the headers to
     * @param headers the headers to add
     */
    protected void addAdditionalHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
        for (Entry<String, String> header : headers.entrySet()) {
            httpRequest.addHeader(header.getKey(), header.getValue());
        }
    }

    /**
     * Adds an optional body to the specified HTTP request
     *
     * @param httpRequest the request to add the body to
     * @param body The body to add to the request
     */
    protected void addOptionalBody(HttpRequestBase httpRequest, RESTRequest request) {
        if (request.getBodyEntity() == null) {
            return;
        }

        switch (request.getMethod()) {
            case PATCH:
            case POST:
            case PUT:
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(request.getBodyEntity().getBodyEntity());
                return;
            default:
                return;
        }
    }

    /**
     * Prepares the query parameters and returns a query string
     *
     * @param queryParams The {@link Map} with the query parameters to prepare
     * @return the query string
     */
    protected String prepareQuery(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        StringBuilder queryParamBuilder = new StringBuilder(32);
        for (Entry<String, String> queryParameter : queryParams.entrySet()) {
            queryParamBuilder.append(queryParameter.getKey()).append('=').append(queryParameter.getValue()).append('&');
        }
        if (queryParamBuilder.length() > 0) {
            queryParamBuilder.setLength(queryParamBuilder.length() - 1);
        }
        return queryParamBuilder.toString();
    }

    /**
     * Executes the specified {@link HttpRequestBase} and returns the response.
     * This is the lower layer of the RESTClient stack before data leaves the middleware's
     * premises.
     *
     * @param httpRequest The HTTP request to execute
     * @return The HTTP response
     * @throws ClientProtocolException if a client protocol error occurs
     * @throws IOException if an I/O error occurs
     */
    private HttpResponse execute(HttpRequestBase httpRequest) throws ClientProtocolException, IOException {
        LOGGER.debug("Executing request: '{}'", httpRequest.getURI());
        try {
            HttpResponse httpResponse = getHttpClient().execute(httpRequest);
            LOGGER.debug("Request '{}' completed with status code '{}'", httpRequest.getURI(), I(httpResponse.getStatusLine().getStatusCode()));
            return httpResponse;
        } catch (OXException e) {
            throw new IOException("Unable to get client", e);
        }
    }

    /**
     * Retrieves the HTTP client
     *
     * @return the HTTP client
     * @throws OXException if the {@link HttpClientService} is absent
     */
    private HttpClient getHttpClient() throws OXException {
        return services.getServiceSafe(HttpClientService.class).getHttpClient(httpClientId);
    }

    // ------------------------------------------------------- Resumable stream ------------------------------------------------------------

    private static class ResumableAbortIfNotConsumedInputStream extends InputStream {

        private final AbstractRESTClient client;
        private final HttpRequestProvider httpRequestProvider;
        private HttpRequestBase httpRequest;
        private HttpResponse httpResponse;
        private InputStream entityContent;
        private boolean closed;
        private long mark;
        private long numberOfReadBytes;
        private long contentLength;

        /**
         * Initializes a new {@link ResumableAbortIfNotConsumedInputStream}.
         *
         * @param initialEntityContent The initial object content
         * @param initialHttpRequest The initial HTTP request
         * @param initialHttpResponse The initial HTTP response
         * @param httpRequestProvider The HTTP request provider
         * @param client The REST client to use
         */
        ResumableAbortIfNotConsumedInputStream(InputStream initialEntityContent, HttpRequestBase initialHttpRequest, HttpResponse initialHttpResponse, HttpRequestProvider httpRequestProvider, AbstractRESTClient client) {
            super();
            this.httpRequestProvider = httpRequestProvider;
            this.httpRequest = initialHttpRequest;
            this.httpResponse = initialHttpResponse;
            this.entityContent = initialEntityContent;
            this.client = client;
            closed = false;
            mark = -1;
            numberOfReadBytes = 0;
            contentLength = -1;
        }

        private long getContentLength() throws IOException {
            long contentLength = this.contentLength;
            if (contentLength < 0) {
                Header clHeader = httpResponse.getFirstHeader("Content-Length");
                if (clHeader == null) {
                    contentLength = -1;
                } else {
                    try {
                        contentLength = Long.parseLong(clHeader.getValue().trim());
                    } catch (NumberFormatException e) {
                        throw new IOException("Content-Length header is not a number: " + clHeader.getValue());
                    }
                }
                this.contentLength = contentLength;
            }
            return contentLength;
        }

        /**
         * Handles given I/O exception that occurred while trying to read from S3 object's content stream.
         *
         * @param e The I/O exception to handle
         * @param errorOnPrematureEof Whether premature EOF should be handled through re-initializing S3 object's content stream
         * @throws IOException If an I/O exception should be advertised to caller
         */
        private void handleIOException(IOException e, boolean errorOnPrematureEof) throws IOException {
            if (errorOnPrematureEof || isNotPrematureEof(e)) {
                throw e;
            }

            // Close existent stream from which -1 was prematurely read
            Streams.close(entityContent);
            entityContent = null;

            // Initialize new object stream after premature EOF
            initNewEntityStreamAfterPrematureEof(e);
        }

        private void initNewEntityStreamAfterPrematureEof(IOException e) throws IOException {
            // Issue request with appropriate range
            boolean success = false;
            try {
                long contentLength = getContentLength();
                if (contentLength < 0) {
                    // Missing or corrupt Content-Length header. Re-throw given I/O exception.
                    throw e;
                }

                long rangeEnd = contentLength - 1;
                long rangeStart = numberOfReadBytes;

                HttpClients.abort(httpRequest);
                HttpClients.close(httpRequest, httpResponse);
                httpRequest = null;
                httpResponse = null;

                httpRequest = httpRequestProvider.createRequest();
                httpRequest.setHeader("Range", "bytes=" + Long.toString(rangeEnd) + "-" + Long.toString(rangeStart));

                httpResponse = client.execute(httpRequest);
                success = httpResponse.getStatusLine().getStatusCode() == 200;
                if (!success) {
                    throw new IOException("Range request failed with: " + httpResponse.getStatusLine());
                }
                entityContent = httpResponse.getEntity().getContent();
            } finally {
                if (!success) {
                    HttpClients.close(httpRequest, httpResponse);
                }
            }
        }

        @Override
        public int read() throws IOException {
            return doRead(false);
        }

        private int doRead(boolean errorOnPrematureEof) throws IOException {
            try {
                int bite = entityContent.read();
                if (bite >= 0) {
                    numberOfReadBytes += 1;
                }
                return bite;
            } catch (IOException e) {
                handleIOException(e, errorOnPrematureEof);

                // Repeat with new InputStream instance
                return doRead(true);
            }
        }

        @Override
        public int read(byte b[]) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            return doRead(b, off, len, false);
        }

        private int doRead(byte b[], int off, int len, boolean errorOnPrematureEof) throws IOException {
            try {
                int result = entityContent.read(b, off, len);
                if (result >= 0) {
                    numberOfReadBytes += result;
                }
                return result;
            } catch (IOException e) {
                handleIOException(e, errorOnPrematureEof);

                // Repeat with new S3ObjectInputStream instance
                return doRead(b, off, len, true);
            }
        }

        @Override
        public long skip(long n) throws IOException {
            long result = entityContent.skip(n);
            numberOfReadBytes += result;
            return result;
        }

        @Override
        public int available() throws IOException {
            return entityContent.available();
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                Streams.close(entityContent);
                HttpClients.abort(httpRequest);
                HttpClients.close(httpRequest, httpResponse);
            }
        }

        @Override
        public void mark(int readlimit) {
            entityContent.mark(readlimit);
            mark = numberOfReadBytes;
        }

        @Override
        public void reset() throws IOException {
            if (!entityContent.markSupported()) {
                throw new IOException("Mark not supported");
            }

            long mark = this.mark;
            if (mark == -1) {
                throw new IOException("Mark not set");
            }

            entityContent.reset();
            numberOfReadBytes = mark;
        }

        @Override
        public boolean markSupported() {
            return entityContent.markSupported();
        }

        // ---------------------------------------------------------------------------------------------------------------------------------

        /**
         * Checks if given I/O exception does <b>not</b> indicate premature EOF.
         *
         * @param e The I/O exception to examine
         * @return <code>true</code> if <b>no</b> premature EOF; otherwise <code>false</code>
         */
        private static boolean isNotPrematureEof(IOException e) {
            return isPrematureEof(e) == false;
        }

        /**
         * Checks if given I/O exception indicates premature EOF.
         *
         * @param e The I/O exception to examine
         * @return <code>true</code> if premature EOF; otherwise <code>false</code>
         */
        private static boolean isPrematureEof(IOException e) {
            if (org.apache.http.ConnectionClosedException.class.isInstance(e)) {
                // HTTP connection has been closed unexpectedly
                String message = e.getMessage();
                if (message != null && message.startsWith("Premature end of Content-Length delimited message body")) {
                    /*-
                     * See org.apache.http.impl.io.ContentLengthInputStream.read(byte[], int, int)
                     *
                     * ...
                     * int readLen = this.in.read(b, off, chunk);
                     * if (readLen == -1 && pos < contentLength) {
                     *     throw new ConnectionClosedException(
                     *         "Premature end of Content-Length delimited message body (expected: %,d; received: %,d)",
                     *         contentLength, pos);
                     * }
                     * ...
                     *
                     * E.g. "Premature end of Content-Length delimited message body (expected: 52,428,800; received: 21,463,040)"
                     */
                    return true;
                }
            }
            return false;
        }
    } // End of class ResumableAbortIfNotConsumedInputStream

}
