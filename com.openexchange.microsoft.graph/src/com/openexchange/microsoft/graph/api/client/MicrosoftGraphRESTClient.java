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

package com.openexchange.microsoft.graph.api.client;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphAPIExceptionCodes;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.v2.AbstractRESTClient;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link MicrosoftGraphRESTClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphRESTClient extends AbstractRESTClient {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftGraphRESTClient.class);

    private static final String USER_AGENT = "Open-Xchange Microsoft Graph Client";
    private static final String API_URL = "graph.microsoft.com";
    private static final String API_VERSION = "v1.0";
    private static final String SCHEME = "https";
    private static final String APPLICATION_JSON = "application/json";
    private static final int TIMEOUT = 30000;

    /**
     * Initialises a new {@link MicrosoftGraphRESTClient}.
     */
    public MicrosoftGraphRESTClient() {
        super(USER_AGENT, TIMEOUT, new MicrosoftGraphRESTResponseParser());
    }

    /**
     * Executes the specified {@link MicrosoftGraphRequest} and examines the response body.
     * 
     * @param request The {@link MicrosoftGraphRequest} to execute
     * @return The {@link RESTResponse}
     * @throws OXException if an error is occurred
     */
    public RESTResponse execute(MicrosoftGraphRequest request) throws OXException {
        return execute(prepareRequest(request));
    }

    /**
     * Executes the specified {@link HttpRequest} and examines the response body.
     * 
     * @param request The {@link HttpRequest} to execute
     * @return The {@link RESTResponse}
     * @throws OXException if an error is occurred
     */
    public RESTResponse execute(HttpRequestBase httpRequest) throws OXException {
        RESTResponse restResponse = executeRequest(httpRequest);
        Object responseBody = restResponse.getResponseBody();
        if (responseBody == null) {
            // Huh? OK, we assert the status code and act accordingly
            assertStatusCode(restResponse);
            return restResponse;
        }
        // OK, let's check the Content-Type
        JSONValue responseBodyCandidate = null;
        String contentType = restResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        if (APPLICATION_JSON.equals(contentType)) {
            // We got 'application/json'. We know it's a JSONValue
            // since it is already parsed via the JsonRESTResponseBodyParser
            responseBodyCandidate = ((JSONValue) responseBody);
        } else if (Strings.isEmpty(contentType)) {
            // Fine, we try to see if the response contains any JSON body
            if (responseBody instanceof String) {
                responseBodyCandidate = tryParseAsJSON((String) responseBody);
                if (responseBodyCandidate != null) {
                    // After type correction, explicitly set it as JSONObject.
                    restResponse.setResponseBody(responseBodyCandidate);
                } else {
                    // Response body other than JSONObject
                    assertStatusCode(restResponse);
                    return restResponse;
                }
            } else if (responseBody instanceof JSONObject) {
                assertStatusCode(restResponse);
                checkForErrors((JSONObject) responseBody);
                return restResponse;
            } else if (responseBody instanceof JSONValue) {
                responseBodyCandidate = (JSONValue) responseBody;
            }
        } else {
            // Response body other than JSONObject
            assertStatusCode(restResponse);
            return restResponse;
        }

        // Did we manage to extract it?
        if (responseBodyCandidate == null || false == responseBodyCandidate.isObject() || responseBodyCandidate.isEmpty()) {
            assertStatusCode(restResponse);
            return restResponse;
        }
        // Hooray, we made it! Check for errors and return the response
        assertStatusCode(restResponse);
        checkForErrors(responseBodyCandidate.toObject());
        return restResponse;
    }

    /**
     * Try and parse the specified response body as a {@link JSONValue}
     * 
     * @param responseBody The response body to parse
     * @return The response body as a {@link JSONValue} or <code>null</code> if the body could not be parsed
     */
    private JSONValue tryParseAsJSON(String responseBody) {
        try {
            char c = responseBody.charAt(0);
            switch (c) {
                case '{':
                    return new JSONObject(responseBody);
                case '[':
                    return new JSONArray(responseBody);
            }
            return null;
        } catch (JSONException e) {
            LOG.debug("", e);
            return null;
        }
    }

    /**
     * Prepares an {@link HttpRequestBase} from the specified {@link MicrosoftGraphRequest}
     * 
     * @param request The {@link MicrosoftGraphRequest} to prepare
     * @return The prepared {@link HttpRequestBase} ready for execution
     * @throws OXException if an invalid URI is specified
     */
    private HttpRequestBase prepareRequest(MicrosoftGraphRequest request) throws OXException {
        HttpRequestBase httpRequest = createRequest(request.getMethod());
        try {
            httpRequest.setURI(new URI(SCHEME, API_URL, "/" + API_VERSION + request.getEndPoint(), prepareQuery(request.getQueryParameters()), null));
            httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + request.getAccessToken());
            httpRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
            addAdditionalHeaders(httpRequest, request.getHeaders());
            addOptionalBody(httpRequest, request);
            return httpRequest;
        } catch (URISyntaxException e) {
            throw RESTExceptionCodes.INVALID_URI_PATH.create(e, API_VERSION + request.getEndPoint());
        }
    }

    /**
     * Checks whether the specified response contains any API errors and if
     * it does throws the appropriate exception.
     * 
     * @param response the {@link JSONObject} response body
     * @throws OXException The appropriate API exception if an error is detected
     */
    private void checkForErrors(JSONObject response) throws OXException {
        if (!response.hasAndNotNull("error")) {
            return;
        }
        JSONObject error = response.optJSONObject("error");
        if (error == null || error.isEmpty()) {
            throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create("Unexpected error");
        }
        String message = error.optString("message");
        String code = error.optString("code");
        throw MicrosoftGraphAPIExceptionCodes.parse(code).create(message);
    }

    /**
     * Asserts the status code of the specified {@link RESTResponse}
     * and throws the appropriate exception if necessary.
     * 
     * @param restResponse The {@link RESTResponse}
     * @throws OXException if an error is detected
     */
    private void assertStatusCode(RESTResponse restResponse) throws OXException {
        int statusCode = restResponse.getStatusCode();
        LOG.debug("Check for status code '{}'", statusCode);
        // All good
        if (statusCode < 400) {
            return;
        }
        // Assert the 4xx codes
        switch (statusCode) {
            case 400:
                throw MicrosoftGraphAPIExceptionCodes.INVALID_REQUEST.create(restResponse.getStatusLine());
            case 401:
                throw MicrosoftGraphAPIExceptionCodes.UNAUTHENTICATED.create(restResponse.getStatusLine());
            case 403:
                throw MicrosoftGraphAPIExceptionCodes.ACCESS_DENIED.create(restResponse.getStatusLine());
            case 404:
                throw MicrosoftGraphAPIExceptionCodes.ITEM_NOT_FOUND.create(restResponse.getStatusLine());
            case 409:
                throw MicrosoftGraphAPIExceptionCodes.NAME_ALREADY_EXISTS.create(restResponse.getStatusLine());
            case 416:
                throw MicrosoftGraphAPIExceptionCodes.INVALID_RANGE.create(restResponse.getStatusLine());
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create(restResponse.getStatusLine());
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 507:
                throw MicrosoftGraphAPIExceptionCodes.QUOTA_LIMIT_REACHED.create(restResponse.getStatusLine());
            case 509:
                throw MicrosoftGraphAPIExceptionCodes.ACTIVITY_LIMIT_REACHED.create(restResponse.getStatusLine());
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw MicrosoftGraphAPIExceptionCodes.SERVICE_NOT_AVAILABLE.create(restResponse.getStatusLine());
        }
    }
}
