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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.rest.client.v2;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.rest.client.v2.parser.RESTResponseParser;

/**
 * {@link AbstractRESTClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractRESTClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRESTClient.class);

    private final CloseableHttpClient httpClient;
    private final RESTResponseParser parser;

    /**
     * Initialises a new {@link AbstractRESTClient}.
     * 
     * @param userAgent The user agent to use for this RESTClient
     * @param timeout The timeout for socket read and connections
     * @param parser The {@link RESTResponseParser} to use when parsing the responses
     */
    public AbstractRESTClient(String userAgent, int timeout, RESTResponseParser parser) {
        super();
        this.parser = parser;
        this.httpClient = initializeHttpClient(userAgent, timeout);
    }

    /**
     * Shuts down the REST client.
     */
    @Override
    public void close() {
        if (httpClient == null) {
            return;
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing the http client: {}", e.getMessage(), e);
        }
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
            consume(httpResponse);
        }
    }

    /**
     * Executes the specified {@link HttpRequestBase} and returns the {@link InputStream}
     * of the response. Use to stream data to client.
     * 
     * @param httpRequest The HTTP request to execute
     * @return The {@link InputStream} of the response
     * @throws OXException if a client protocol error or an I/O error occurs
     */
    public InputStream download(HttpRequestBase httpRequest) throws OXException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                return httpResponse.getEntity().getContent();
            }
            throw RESTExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        } catch (ClientProtocolException e) {
            throw RESTExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
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
     * @throws OXException if the default HTTP charset is not supported
     */
    protected void addOptionalBody(HttpRequestBase httpRequest, RESTRequest request) throws OXException {
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
     * Initialises the HTTP client
     *
     * @param userAgent The user agent
     * @param timeout The timeout to use for connections and socket reads
     * @return The initialised {@link CloseableHttpClient}
     */
    private CloseableHttpClient initializeHttpClient(String userAgent, int timeout) {
        ClientConfig clientConfig = ClientConfig.newInstance();
        clientConfig.setUserAgent(userAgent);
        clientConfig.setConnectionTimeout(timeout);
        clientConfig.setSocketReadTimeout(timeout);

        return HttpClients.getHttpClient(clientConfig);
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
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        LOGGER.debug("Request '{}' completed with status code '{}'", httpRequest.getURI(), httpResponse.getStatusLine().getStatusCode());
        return httpResponse;
    }

    /**
     * Consumes the specified {@link HttpResponse}
     *
     * @param response the {@link HttpResponse} to consume
     */
    private void consume(HttpResponse response) {
        if (null == response) {
            return;
        }
        HttpEntity entity = response.getEntity();
        if (null == entity) {
            return;
        }
        try {
            EntityUtils.consume(entity);
        } catch (Throwable e) {
            LOGGER.debug("Error while consuming the entity of the HTTP response {}", e.getMessage(), e);
        }
    }
}
