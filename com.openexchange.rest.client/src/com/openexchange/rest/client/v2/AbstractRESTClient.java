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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
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

    private final RESTResponseParser parser;

    protected ServiceLookup services;

    private final String httpClientId;

    /**
     * Initialises a new {@link AbstractRESTClient}.
     *
     * @param services The service lookup to get the {@link HttpClientService} from
     * @param httpClientId The service ID to get the HTTP client for
     * @param parser The {@link RESTResponseParser} to use when parsing the responses
     */
    public AbstractRESTClient(ServiceLookup services, String httpClientId, RESTResponseParser parser) {
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
}
