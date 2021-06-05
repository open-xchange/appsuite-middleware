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

package com.openexchange.rest.client.v2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AbstractRESTRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractRESTRequest implements RESTRequest {

    private final String endPoint;
    private final RESTMethod method;
    private RESTBodyEntity entity;
    private final Map<String, String> queryParameters;
    private final Map<String, String> headers;

    /**
     * Initialises a new {@link AbstractRESTRequest}.
     * 
     * @param path
     */
    public AbstractRESTRequest(String path) {
        this(RESTMethod.GET, path);
    }

    /**
     * Initialises a new {@link AbstractRESTRequest}.
     */
    public AbstractRESTRequest(RESTMethod method, String path) {
        super();
        this.method = method;
        this.endPoint = path;
        queryParameters = new HashMap<>();
        headers = new HashMap<>(4);
    }

    /**
     * Gets the endPoint
     *
     * @return The endPoint
     */
    public String getEndPoint() {
        return endPoint;
    }

    /**
     * Gets the method
     *
     * @return The method
     */
    @Override
    public RESTMethod getMethod() {
        return method;
    }

    /**
     * With the specified query parameter
     * 
     * @param name The name of the query parameter
     * @param value the value of the query parameter
     */
    public void setQueryParameter(String name, String value) {
        queryParameters.put(name, value);
    }

    /**
     * Sets the body entity
     * 
     * @param entity The body entity to set
     */
    public void setBodyEntity(RESTBodyEntity entity) {
        this.entity = entity;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public RESTBodyEntity getBodyEntity() {
        return entity;
    }
}
