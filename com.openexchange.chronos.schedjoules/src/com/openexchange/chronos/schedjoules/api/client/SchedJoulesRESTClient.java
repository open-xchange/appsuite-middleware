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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesExceptionCodes;
import com.openexchange.chronos.schedjoules.impl.SchedJoulesProperty;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;

/**
 * {@link SchedJoulesRESTClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesRESTClient {

    private static final String SCHEME = "https";
    private static final String BASE_URL = "api.schedjoules.com";
    private static final String AUTHORIZATION_HEADER = "Token token=\"{{token}}\"";
    private static final String USER_AGENT = "Open-Xchange SchedJoules Client (pre-Alpha)";

    private final CloseableHttpClient httpClient;
    private final String authorizationHeader;

    /**
     * Initialises a new {@link SchedJoulesRESTClient}.
     * 
     * @throws OXException if the apiKey is not configured
     */
    public SchedJoulesRESTClient() throws OXException {
        super();
        authorizationHeader = prepareAuthorizationHeader();
        httpClient = initializeHttpClient();
    }

    /**
     * Prepares the authorisation header
     * 
     * @return The authorisation header
     * @throws OXException if the api key is not configured
     */
    private String prepareAuthorizationHeader() throws OXException {
        LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
        String apiKey = service.getProperty(SchedJoulesProperty.apiKey);
        if (Strings.isEmpty(apiKey)) {
            throw SchedJoulesExceptionCodes.NO_API_KEY_CONFIGURED.create();
        }
        return AUTHORIZATION_HEADER.replaceFirst("\\{\\{token\\}\\}", apiKey);
    }

    /**
     * Initialises the HTTP client
     * 
     * @return The initialised {@link CloseableHttpClient}
     */
    private CloseableHttpClient initializeHttpClient() {
        ClientConfig clientConfig = ClientConfig.newInstance();
        clientConfig.setUserAgent(USER_AGENT);

        return HttpClients.getHttpClient(clientConfig);
    }

    /**
     * Prepares the specified HTTP request
     * 
     * @param request The {@link HttpRequestBase} to prepare
     * @param path The mandatory path
     * @param query The optional query
     * @throws OXException
     */
    private void prepareRequest(HttpRequestBase request, String path, String query) throws OXException {
        prepareRequest(request, SCHEME, BASE_URL, path, query);
    }

    /**
     * Prepares the specified HTTP request
     * 
     * @param request The {@link HttpRequestBase} to prepare
     * @param scheme The mandatory scheme/protocol
     * @param baseUrl The mandatory base URL
     * @param path The mandatory path
     * @param query The optional query string
     * @throws OXException if the path is not valid
     */
    private void prepareRequest(HttpRequestBase request, String scheme, String baseUrl, String path, String query) throws OXException {
        try {
            request.setURI(new URI(scheme, baseUrl, path, query, null));
            request.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        } catch (URISyntaxException e) {
            throw SchedJoulesExceptionCodes.INVALID_URI_PATH.create(path, e);
        }
    }

    /**
     * Prepares the specified {@link SchedJoulesRequest}
     * 
     * @param request The {@link SchedJoulesRequest} to prepare
     * @return an {@link HttpUriRequest}
     * @throws OXException if an unknown HTTP method is defined in the request
     */
    private HttpUriRequest prepareRequest(SchedJoulesRequest request) throws OXException {
        // Prepare the query
        String query = prepareQuery(request.getQueryParameters());

        // Prepare the request
        HttpRequestBase httpRequest = createRequest(request.getMethod());
        prepareRequest(httpRequest, request.getPath(), query);
        return httpRequest;
    }

    /**
     * Creates an {@link HttpRequestBase} with the specifeid {@link HttpMethod}
     * 
     * @param httpMethod The {@link HttpMethod}
     * @return The new {@link HttpRequestBase}
     * @throws OXException if an unknown HTTP method is provided
     */
    private HttpRequestBase createRequest(HttpMethod httpMethod) throws OXException {
        HttpRequestBase httpRequest;
        switch (httpMethod) {
            case GET:
                httpRequest = new HttpGet();
                break;
            default:
                throw SchedJoulesExceptionCodes.UNKNOWN_HTTP_METHOD.create(httpMethod);
        }
        return httpRequest;
    }

    /**
     * Creates an {@link HttpUriRequest} from the specified {@link URL} and the specified {@link HttpMethod}
     * 
     * @param url The {@link URL} to use
     * @param httpMethod The {@link HttpMethod}
     * @return The new {@link HttpUriRequest}
     * @throws OXException if an unknwon HTTP method is provided
     * @throws URISyntaxException If an invalid URL is provided
     */
    private HttpUriRequest prepareRequest(URL url, HttpMethod httpMethod) throws OXException {
        HttpRequestBase httpRequest = createRequest(httpMethod);
        prepareRequest(httpRequest, url.getProtocol(), url.getHost(), url.getPath(), null);
        return httpRequest;
    }

    /**
     * Prepares the query string from the specified query parameters
     * 
     * @param queryParameters The {@link Map} containing the query parameters
     * @return The query string
     */
    private String prepareQuery(Map<String, String> queryParameters) {
        if (queryParameters.isEmpty()) {
            return new String();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : queryParameters.keySet()) {
            stringBuilder.append(name).append("=").append(queryParameters.get(name)).append("&");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    /**
     * Executes the specified {@link SchedJoulesRequest}
     * 
     * @param request The {@link SchedJoulesRequest} to execute
     * @return The {@link SchedJoulesResponse}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesResponse executeRequest(SchedJoulesRequest request) throws OXException {
        return executeRequest(prepareRequest(request));
    }

    /**
     * Executes a GET request to the specified {@link URL}
     * 
     * @param url The {@link URL}
     * @return The {@link SchedJoulesResponse}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesResponse executeRequest(URL url) throws OXException {
        return executeRequest(url, HttpMethod.GET);
    }

    /**
     * Executes a request with the specified method to the specified {@link URL}
     * 
     * @param url The {@link URL}
     * @param httpMethod the {@link HttpMethod} to use
     * @return The {@link SchedJoulesResponse}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesResponse executeRequest(URL url, HttpMethod httpMethod) throws OXException {
        return executeRequest(prepareRequest(url, httpMethod));
    }

    /**
     * Executes the specified {@link HttpUriRequest} and returns a {@link SchedJoulesResponse}
     * 
     * @param httpRequest the {@link HttpUriRequest} to execute
     * @return The {@link SchedJoulesResponse}
     * @throws OXException if an error is occurred
     */
    private SchedJoulesResponse executeRequest(HttpUriRequest httpRequest) throws OXException {
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);

            int statusCode = assertStatusCode(httpResponse);
            SchedJoulesResponse response = new SchedJoulesResponse(statusCode);

            if (statusCode == 304) {
                // Ok, nothing was modified, no response body, return as is
                return response;
            }

            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                throw new OXException(1138, "No body was returned");
            }
            Header ctHeader = httpResponse.getFirstHeader("content-type");
            if (ctHeader != null) {
                String ct = ctHeader.getValue();
                String contentType = ct.substring(0, ct.indexOf(';'));
                response.setContentType(contentType);
            }
            response.setStream(entity.getContent());
            return response;
        } catch (ClientProtocolException e) {
            throw SchedJoulesExceptionCodes.CLIENT_PROTOCOL_ERROR.create(e.getMessage(), e);
        } catch (IOException e) {
            throw SchedJoulesExceptionCodes.IO_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Asserts the status code for any errors
     * 
     * @param httpResponse The {@link HttpResponse}'s status code to assert
     * @return The status code
     * @throws OXException if an HTTP error is occurred (4xx or 5xx)
     */
    private int assertStatusCode(HttpResponse httpResponse) throws OXException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        // Assert the 4xx codes
        switch (statusCode) {
            case 401:
                throw SchedJoulesExceptionCodes.NOT_AUTHORIZED.create(httpResponse.getStatusLine().getReasonPhrase());
            case 404:
                throw SchedJoulesExceptionCodes.PAGE_NOT_FOUND.create();
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw SchedJoulesExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 500:
                throw SchedJoulesExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(httpResponse.getStatusLine().getReasonPhrase());
            case 503:
                throw SchedJoulesExceptionCodes.REMOTE_SERVICE_UNAVAILABLE.create(httpResponse.getStatusLine().getReasonPhrase());
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw SchedJoulesExceptionCodes.REMOTE_SERVER_ERROR.create(httpResponse.getStatusLine());
        }
        return statusCode;
    }
}
