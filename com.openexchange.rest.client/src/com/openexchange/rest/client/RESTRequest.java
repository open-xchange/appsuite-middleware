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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.rest.client;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.rest.client.session.Session;

/**
 * {@link RESTRequest}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
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
