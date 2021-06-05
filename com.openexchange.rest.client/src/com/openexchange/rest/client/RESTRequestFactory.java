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

package com.openexchange.rest.client;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.rest.client.RESTRequest.Method;
import com.openexchange.rest.client.session.Session;

/**
 * {@link RESTRequestFactory}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @deprecated
 */
public class RESTRequestFactory {

    /**
     * Create a complete {@link RESTRequest} with all values filled
     * 
     * @param method The HTTP {@link Method}
     * @param session The {@link Session} to use for this request.
     * @param host The host on which resides the REST API
     * @param apiVersion The API version to use.
     * @param path The URL path starting with a '/'.
     * @param expectedStatusCodes The expected status code(s) on successful response
     * @param body The request's JSON object
     * @param params Any URL parameters in a String array. with the even numbered elements the parameter names and odd numbered elements the
     *            values, e.g. <code>new String[] {"path", "/Public", "locale", "en"}</code>.
     * @param expectedStatusCodes
     * @return A {@link RESTRequest}
     */
    public static RESTRequest completeRequest(final Method method, final Session session, final String host, final int apiVersion, final String path, final List<String> params, final JSONObject body, final List<Integer> expectedStatusCodes) {
        final RESTRequest req = new RESTRequest(session, method, host, path, expectedStatusCodes);
        req.setAPIVersion(apiVersion).setParameters(params).setRequestBody(body);
        return req;
    }

    /**
     * Create a basic {@link RESTRequest}
     * 
     * @param method The HTTP {@link Method}
     * @param session The {@link Session} to use for this request.
     * @param host The host on which resides the REST API
     * @param path The URL path starting with a '/'.
     * @param expectedStatusCodes The expected status code(s) on successful response
     * @return A {@link RESTRequest}
     */
    public static RESTRequest basicRequest(final Method method, final Session session, final String host, final String path, final List<Integer> expectedStatusCodes) {
        return new RESTRequest(session, method, host, path, expectedStatusCodes);
    }
}
