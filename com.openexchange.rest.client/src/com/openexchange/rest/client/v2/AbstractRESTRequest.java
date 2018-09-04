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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.v2.RESTRequest#getQueryParameters()
     */
    @Override
    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.v2.RESTRequest#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 
     */
    public void sethBodyEntity(RESTBodyEntity entity) {
        this.entity = entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.v2.RESTRequest#getBodyEntity()
     */
    @Override
    public RESTBodyEntity getBodyEntity() {
        return entity;
    }
}
