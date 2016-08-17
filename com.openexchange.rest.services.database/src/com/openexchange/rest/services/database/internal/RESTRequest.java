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

package com.openexchange.rest.services.database.internal;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link RESTRequest}. Simple POJO that holds information about the headers, the body and the parameters of the request
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RESTRequest {

    private final Object body;
    private final MultivaluedMap<String, String> params;
    private final HttpHeaders headers;

    /**
     * Initializes a new {@link RESTRequest}.
     * 
     * @param headers The request's headers
     * @param params The request's parameters
     * @param body The request's body
     */
    public RESTRequest(HttpHeaders headers, MultivaluedMap<String, String> params, Object body) {
        this.headers = headers;
        this.body = body;
        this.params = params;
    }

    /**
     * Initializes a new {@link RESTRequest}.
     * 
     * @param headers The request's headers
     * @param params The request's parameters
     */
    public RESTRequest(HttpHeaders headers, MultivaluedMap<String, String> params) {
        this(headers, params, null);
    }

    /**
     * Gets the body
     *
     * @return The body
     */
    public Object getBody() {
        return body;
    }

    /**
     * Gets the headers
     *
     * @return The headers
     */
    public String getHeader(String key) {
        return headers.getHeaderString(key);
    }

    /**
     * Determines whether or not the specified URL parameter is set.
     * 
     * @param key The name of the URL parameter
     * @return true if the URL parameter is set; false otherwise
     */
    public boolean isParameterSet(String key) {
        return (params.get(key) != null);
    }

    /**
     * Get the value of the specified URL parameter
     * 
     * @param key The name of the URL parameter
     * @return The value of the URL parameter
     */
    public String getParameter(String key) {
        return params.getFirst(key);
    }

    /**
     * Get the specified parameter from the URL parameters map and parse it as the specified type.
     * 
     * @param key The parameter's name
     * @param clazz The type
     * @return The parsed parameter's value
     * @throws OXException
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> clazz) throws OXException {
        String value = params.getFirst(key);
        if (value == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(key, "null");
        }
        if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return (T) Boolean.valueOf(value);
        } else {
            return (T) value;
        }
    }
}
