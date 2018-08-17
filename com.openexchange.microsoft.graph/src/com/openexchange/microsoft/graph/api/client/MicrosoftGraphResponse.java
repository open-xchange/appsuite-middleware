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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.RESTResponse;

/**
 * {@link MicrosoftGraphResponse}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphResponse implements RESTResponse {

    private final int statusCode;
    private InputStream stream;
    private Object responseBody;
    private final Map<String, String> headers;

    /**
     * Initialises a new {@link MicrosoftGraphResponse}.
     */
    public MicrosoftGraphResponse(int statusCode) {
        super();
        this.statusCode = statusCode;
        headers = new HashMap<>(4);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponse#getStream()
     */
    @Override
    public InputStream getStream() {
        return stream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponse#getStatusCode()
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponse#getResponseBody()
     */
    @Override
    public Object getResponseBody() {
        return responseBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponse#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    //////////////////////////// SETTERS //////////////////////////////

    /**
     * Sets the responseBody
     *
     * @param responseBody The responseBody to set
     */
    void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * Sets the stream
     *
     * @param stream The stream to set
     */
    void setStream(InputStream stream) {
        this.stream = stream;
    }

    /**
     * The response's headers
     * 
     * @param headers the headers to set
     */
    void addHeader(String key, String value) {
        if (Strings.isEmpty(key)) {
            return;
        }
        if (Strings.isEmpty(value)) {
            return;
        }
        headers.put(key, value);
    }
}
