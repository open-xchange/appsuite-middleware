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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.java.Strings;

/**
 * {@link RESTResponse}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.1
 */
public class RESTResponse {

    private final int statusCode;
    private final String statusLine;
    private Object responseBody;
    private final Map<String, String> headers;

    /**
     * Initialises a new {@link MicrosoftGraphResponse}.
     */
    public RESTResponse(int statusCode, String statusLine) {
        super();
        this.statusCode = statusCode;
        this.statusLine = statusLine;
        headers = new HashMap<>(4);
    }

    /**
     * Returns the status code of the response
     * 
     * @return the status code of the response
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the response body (if any)
     * 
     * @return the response body or <code>null</code>
     */
    public Object getResponseBody() {
        return responseBody;
    }

    /**
     * Returns an unmodifiable {@link Map} with the headers
     * 
     * @return an unmodifiable {@link Map} with the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the value of a specific header or <code>null</code>
     * if that header is absent.
     * 
     * @param headerName the header's name
     * @return the value of a specific header or <code>null</code>
     *         if that header is absent.
     */
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    /**
     * Gets the statusLine
     *
     * @return The statusLine
     */
    public String getStatusLine() {
        return statusLine;
    }

    //////////////////////////// SETTERS //////////////////////////////

    /**
     * Sets the responseBody
     *
     * @param responseBody The responseBody to set
     */
    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * The response's headers
     * 
     * @param headers the headers to set
     */
    public void addHeader(String key, String value) {
        if (Strings.isEmpty(key)) {
            return;
        }
        if (Strings.isEmpty(value)) {
            return;
        }
        headers.put(key, value);
    }
}
