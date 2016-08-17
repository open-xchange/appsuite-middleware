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

package com.openexchange.jump.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.jump.Endpoint;

/**
 * {@link EndpointImpl} - The default endpoint implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EndpointImpl implements Endpoint {

    private final Map<String, Object> properties;
    private String url;
    private String systemName;

    /**
     * Initializes a new {@link EndpointImpl}.
     */
    public EndpointImpl() {
        super();
        properties = new LinkedHashMap<String, Object>(2);
    }

    /**
     * Initializes a new {@link EndpointImpl}.
     *
     * @param systemName The system name
     * @param url The URL
     */
    public EndpointImpl(String systemName, String url) {
        this();
        this.url = url;
        this.systemName = systemName;
    }


    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL.
     *
     * @param url The URL to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the system name.
     *
     * @param systemName The system name to set
     */
    public void setSystemName(final String systemName) {
        this.systemName = systemName;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Object getProperty(final String propName) {
        return properties.get(propName);
    }

    /**
     * Puts given property into this endpoint's properties.
     *
     * @param propName The property name
     * @param value The value
     */
    public void put(final String propName, final Object value) {
        properties.put(propName, value);
    }

    /**
     * Puts given properties into this endpoint's properties.
     *
     * @param properties The properties to put
     */
    public void putAll(final Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Removes denoted property from this endpoint's properties.
     *
     * @param propName The name of the property to remove
     */
    public void remove(final String propName) {
        properties.remove(propName);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointImpl [");
        if (systemName != null) {
            builder.append("systemName=").append(systemName).append(", ");
        }
        if (url != null) {
            builder.append("url=\"").append(url).append("\", ");
        }
        if (properties != null) {
            builder.append("properties=").append(properties);
        }
        builder.append("]");
        return builder.toString();
    }

}
