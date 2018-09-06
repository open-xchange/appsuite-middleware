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

package com.openexchange.microsoft.graph.api;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRequest;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphAPIExceptionCodes;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.entity.InputStreamEntity;
import com.openexchange.rest.client.v2.entity.JSONObjectEntity;

/**
 * {@link AbstractMicrosoftGraphAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractMicrosoftGraphAPI {

    final MicrosoftGraphRESTClient client;
    static final String APPLICATION_JSON = "application/json";

    /**
     * Initialises a new {@link AbstractMicrosoftGraphAPI}.
     */
    public AbstractMicrosoftGraphAPI(MicrosoftGraphRESTClient client) {
        super();
        this.client = client;
    }

    //////////////////////////////// GET ///////////////////////////////////////

    /**
     * Gets the REST resource from the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The resource's path
     * @return A {@link JSONObject} with the resource
     * @throws OXException if an error is occurred
     */
    JSONObject getResource(String accessToken, String path) throws OXException {
        return getResource(accessToken, path, Collections.emptyMap());
    }

    /**
     * Gets the REST resource from the specified path and the specified query
     * parameters
     * 
     * @param accessToken The oauth access token
     * @param path The resource's path
     * @param queryParams the request's query parameters
     * @return A {@link JSONObject} with the resource
     * @throws OXException if an error is occurred
     */
    JSONObject getResource(String accessToken, String path, Map<String, String> queryParams) throws OXException {
        return executeRequest(createRequest(RESTMethod.GET, accessToken, path, queryParams));
    }

    /**
     * Gets the binary resource from the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The resource's path
     * @return The byte array with the resource's contents
     * @throws OXException if an error is occurred
     */
    byte[] getBinaryResource(String accessToken, String path) throws OXException {
        RESTResponse restResponse = client.execute(createRequest(RESTMethod.GET, accessToken, path));
        // TODO: Check for errors
        if (!(restResponse.getResponseBody() instanceof byte[])) {
            throw new OXException(666, "binary resournce not found");
        }
        return (byte[]) restResponse.getResponseBody();
    }

    /**
     * Gets the stream from the specified path. Use to stream data from the
     * remote end-point to the client, i.e. download.
     * 
     * @param path The path
     * @return The {@link InputStream}
     * @throws OXException if an error is occurred
     */
    InputStream getStream(String path) throws OXException {
        return client.download(new HttpGet(path));
    }

    //////////////////////////////// POST ///////////////////////////////////////

    /**
     * Posts the specified {@link JSONObject} body to the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The path
     * @param body The body to post
     * @return A {@link JSONObject} with the resource metadata
     * @throws OXException if an error is occurred
     */
    JSONObject postResource(String accessToken, String path, JSONObject body) throws OXException {
        MicrosoftGraphRequest request = createRequest(RESTMethod.POST, accessToken, path);
        request.sethBodyEntity(new JSONObjectEntity(body));
        return executeRequest(request);
    }

    //////////////////////////////// PUT ///////////////////////////////////////

    /**
     * Puts the specified {@link JSONObject} body to the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The path
     * @param body The body to post
     * @return A {@link JSONObject} with the resource metadata
     * @throws OXException if an error is occurred
     */
    JSONObject putResource(String accessToken, String path, String contentType, long contentLength, InputStream body) throws OXException {
        MicrosoftGraphRequest request = createRequest(RESTMethod.POST, accessToken, path, contentType);
        request.sethBodyEntity(new InputStreamEntity(body, contentLength, contentType));
        return executeRequest(request);
    }

    //////////////////////////////// DELETE ///////////////////////////////////////

    /**
     * Deletes a resource
     * 
     * @param accessToken The oauth access token
     * @param path The path
     * @throws OXException if an error is occurred
     */
    void deleteResource(String accessToken, String path) throws OXException {
        executeRequest(createRequest(RESTMethod.DELETE, accessToken, path));
    }

    //////////////////////////////// PATCH ///////////////////////////////////////

    /**
     * Patches the specified {@link JSONObject} body to the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The path
     * @param body The body to post
     * @return A {@link JSONObject} with the resource metadata
     * @throws OXException if an error is occurred
     */
    JSONObject patchResource(String accessToken, String path, JSONObject body) throws OXException {
        MicrosoftGraphRequest request = createRequest(RESTMethod.PATCH, accessToken, path);
        request.sethBodyEntity(new JSONObjectEntity(body));
        return executeRequest(request);
    }

    //////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Creates a {@link MicrosoftGraphRequest} with the specified {@link RESTMethod} and access token
     * for the specified end-point
     * 
     * @param method the {@link RESTMethod}
     * @param accessToken The oauth access token
     * @param path The path
     * @return The {@link MicrosoftGraphRequest}
     */
    private MicrosoftGraphRequest createRequest(RESTMethod method, String accessToken, String path) {
        return createRequest(method, accessToken, path, APPLICATION_JSON, Collections.emptyMap());
    }

    /**
     * Creates a {@link MicrosoftGraphRequest} with the specified {@link RESTMethod}, access token
     * and content type for the specified end-point
     * 
     * @param method the {@link RESTMethod}
     * @param accessToken The oauth access token
     * @param path The path
     * @param contentType The Contenty-Type
     * @return The {@link MicrosoftGraphRequest}
     */
    private MicrosoftGraphRequest createRequest(RESTMethod method, String accessToken, String path, String contentType) {
        return createRequest(method, accessToken, path, contentType, Collections.emptyMap());
    }

    /**
     * Creates a {@link MicrosoftGraphRequest} with the specified {@link RESTMethod}, access token
     * and query parameters for the specified end-point
     * 
     * @param method the {@link RESTMethod}
     * @param accessToken The oauth access token
     * @param path The path
     * @param queryParameters The query parameters
     * @return The {@link MicrosoftGraphRequest}
     */
    private MicrosoftGraphRequest createRequest(RESTMethod method, String accessToken, String path, Map<String, String> queryParameters) {
        return createRequest(method, accessToken, path, APPLICATION_JSON, queryParameters);
    }

    /**
     * Creates a {@link MicrosoftGraphRequest} with the specified {@link RESTMethod}, access token, content type
     * and query parameters for the specified end-point
     * 
     * @param method the {@link RESTMethod}
     * @param accessToken The oauth access token
     * @param path The path
     * @param contentType The Contenty-Type
     * @param queryParameters The query parameters
     * @return The {@link MicrosoftGraphRequest}
     */
    private MicrosoftGraphRequest createRequest(RESTMethod method, String accessToken, String path, String contentType, Map<String, String> queryParameters) {
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(method, path);
        request.setAccessToken(accessToken);
        request.setHeader(HttpHeaders.CONTENT_TYPE, Strings.isEmpty(contentType) ? APPLICATION_JSON : contentType);
        for (Entry<String, String> queryParam : queryParameters.entrySet()) {
            request.setQueryParameter(queryParam.getKey(), queryParam.getValue());
        }
        return request;
    }

    /**
     * Executes the specified {@link MicrosoftGraphRequest}
     * 
     * @param request the request to execute
     * @return The response body as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    private JSONObject executeRequest(MicrosoftGraphRequest request) throws OXException {
        RESTResponse restResponse = client.execute(request);
        Object responseBody = restResponse.getResponseBody();
        if (responseBody == null) {
            throw new OXException(666, "no response body");
        }
        //FIXME: Check the returned entity type
        JSONObject response = ((JSONValue) restResponse.getResponseBody()).toObject();
        checkForErrors(response);
        return response;
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
}
