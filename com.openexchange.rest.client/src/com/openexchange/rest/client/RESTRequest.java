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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.rest.client.session.Session;

/**
 * {@link RESTRequest}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @deprecated Use {@link com.openexchange.rest.client.v2.RESTRequest} instead.
 */
public class RESTRequest {

    private final Session session;

    private final Method method;

    private final String host;

    /** The optional API version to use. Or <code>-1</code> to ignore */
    private int apiVersion = -1;

    private final String path;

    /**
     * Any URL parameters in a String array. with the even numbered elements the parameter names and odd numbered elements the values, e.g.
     * <code>new String[] {"path", "/Public", "locale", "en"}</code>.
     */
    private List<String> params;

    /** The request's JSON object */
    private JSONObject body;

    private final List<Integer> expectedStatusCodes;

    public static enum Method {
        PUT, GET, POST, DELETE;
    }

    /**
     * Initializes a new {@link RESTRequest}.
     * 
     * @param session The {@link Session} to use for this request.
     * @param method The HTTP {@link Method}
     * @param host The host on which resides the REST API
     * @param path The URL path starting with a '/'.
     * @param expectedStatusCodes The expected status code(s) on successful response
     */
    public RESTRequest(final Session session, final Method method, final String host, final String path, final List<Integer> expectedStatusCodes) {
        super();
        this.session = session;
        this.method = method;
        this.host = host;
        this.path = path;
        this.expectedStatusCodes = expectedStatusCodes;
        params = new ArrayList<String>();
    }

    /**
     * Gets the session
     * 
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the method
     * 
     * @return The method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the host
     * 
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the API version for this request
     * 
     * @param version The API version for this request
     */
    public RESTRequest setAPIVersion(final int version) {
        apiVersion = version;
        return this;
    }

    /**
     * Gets the API version
     * 
     * @return apiVersion The API version for this request
     */
    public int getAPIVersion() {
        return apiVersion;
    }

    /**
     * Gets the path
     * 
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Add a parameter to this request
     * 
     * @param name The parameter's name
     * @param value The parameter's value
     * @return thiss
     */
    public RESTRequest addParameter(final String name, final String value) {
        params.add(name);
        params.add(value);
        return this;
    }

    /**
     * Get the request parameters
     * 
     * @param The request parameters
     */
    public String[] getParams() {
        return (params != null) ? params.toArray(new String[params.size()]) : new String[0];
    }

    public RESTRequest setParameters(final List<String> params) {
        this.params = params;
        return this;
    }

    /**
     * Sets the request body
     * 
     * @param body The request body
     */
    public RESTRequest setRequestBody(final JSONObject json) {
        body = json;
        return this;
    }

    /**
     * Gets the request body
     * 
     * @return The request body
     */
    public JSONObject getRequestBody() {
        return body;
    }

    /**
     * Gets the expectedStatusCodes
     * 
     * @return The expectedStatusCodes
     */
    public List<Integer> getExpectedStatusCodes() {
        return expectedStatusCodes;
    }

}
