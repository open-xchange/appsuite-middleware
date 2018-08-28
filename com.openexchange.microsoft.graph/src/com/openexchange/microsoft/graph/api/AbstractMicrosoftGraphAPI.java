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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRequest;
import com.openexchange.rest.client.v2.RESTMethod;

/**
 * {@link AbstractMicrosoftGraphAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractMicrosoftGraphAPI {

    final MicrosoftGraphRESTClient client;

    /**
     * Initialises a new {@link AbstractMicrosoftGraphAPI}.
     */
    public AbstractMicrosoftGraphAPI(MicrosoftGraphRESTClient client) {
        super();
        this.client = client;
    }

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
     * Gets the binary resource from the specified path
     * 
     * @param accessToken The oauth access token
     * @param path The resource's path
     * @return The byte array with the resource's contents
     * @throws OXException if an error is occurred
     */
    byte[] getBinaryResource(String accessToken, String path) throws OXException {
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(RESTMethod.GET, path);
        request.setAccessToken(accessToken);
        return (byte[]) client.execute(request).getResponseBody();
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
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(RESTMethod.GET, path);
        request.setAccessToken(accessToken);
        for (Entry<String, String> queryParam : queryParams.entrySet()) {
            request.withQueryParameter(queryParam.getKey(), queryParam.getValue());
        }
        return ((JSONValue) client.execute(request).getResponseBody()).toObject();
    }

    /**
     * 
     * @param accessToken
     * @param path
     * @return
     * @throws OXException
     */
    JSONObject postResource(String accessToken, String path, JSONObject body) throws OXException {
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(RESTMethod.POST, path);
        request.setAccessToken(accessToken);
        request.withHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.withBody(body);
        return ((JSONValue) client.execute(request).getResponseBody()).toObject();
    }
}
