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
