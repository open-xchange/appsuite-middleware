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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
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
     */
    public AbstractRESTClient(String userAgent, RESTResponseParser parser) {
        super();
        this.parser = parser;
        this.httpClient = initializeHttpClient(userAgent);
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
     * This is the lower layer of the RESTClient stack before data leaves the middleware's
     * premises.
     * 
     * @param httpRequest The HTTP request to execute
     * @return The parsed HTTP REST response
     * @throws OXException if a client protocol error or an I/O error occurs
     */
    public RESTResponse executeRequest(HttpRequestBase httpRequest) throws OXException {
        CloseableHttpResponse httpResponse = null;
        try {
            LOGGER.debug("Executing request: '{}'", httpRequest.getURI());
            httpResponse = httpClient.execute(httpRequest);
            LOGGER.debug("Request '{}' completed with status code '{}'", httpRequest.getURI(), httpResponse.getStatusLine().getStatusCode());

            return parser.parse(httpResponse);
        } catch (ClientProtocolException e) {
            throw RESTExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } finally {
            consume(httpResponse);
            reset(httpRequest);
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
            default:
                throw RESTExceptionCodes.UNSUPPORTED_METHOD.create(httpMethod);
        }
        return httpRequest;
    }

    /**
     * Initialises the HTTP client
     *
     * @param userAgent The user agent
     * @return The initialised {@link CloseableHttpClient}
     */
    private CloseableHttpClient initializeHttpClient(String userAgent) {
        ClientConfig clientConfig = ClientConfig.newInstance();
        clientConfig.setUserAgent(userAgent);

        return HttpClients.getHttpClient(clientConfig);
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

    /**
     * Resets the specified {@link HttpRequestBase}
     *
     * @param httpRequest The {@link HttpRequestBase} to reset
     */
    private void reset(HttpRequestBase httpRequest) {
        if (httpRequest == null) {
            return;
        }
        try {
            httpRequest.reset();
        } catch (final Throwable e) {
            LOGGER.debug("Error while resetting the HTTP request {}", e.getMessage(), e);
        }
    }
}
