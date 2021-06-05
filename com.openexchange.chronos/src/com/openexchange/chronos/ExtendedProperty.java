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

package com.openexchange.chronos;

import java.util.List;

/**
 * {@link ExtendedProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedProperty {

    private final String name;
    private final Object value;
    private final List<ExtendedPropertyParameter> parameters;

    /**
     * Initializes a new {@link ExtendedProperty}.
     *
     * @param name The property name
     * @param value The value
     * @param parameters The parameters, or <code>null</code> if there are none
     */
    public ExtendedProperty(String name, Object value, List<ExtendedPropertyParameter> parameters) {
        super();
        this.name = name;
        this.value = value;
        this.parameters = parameters;
    }

    /**
     * Initializes a new {@link ExtendedProperty}, without further parameters.
     *
     * @param name The property name
     * @param value The value
     */
    public ExtendedProperty(String name, Object value) {
        this(name, value, null);
    }

    /**
     * Gets the property name.
     *
     * @return The property name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the property value.
     *
     * @return The property value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets a map of additional property parameters.
     *
     * @return The property parameters, or <code>null</code> if not set
     */
    public List<ExtendedPropertyParameter> getParameters() {
        return parameters;
    }

    /**
     * Gets the (first) property parameter matching the supplied name.
     *
     * @param name The name of the parameter to get
     * @return The parameter, or <code>null</code> if not found
     */
    public ExtendedPropertyParameter getParameter(String name) {
        if (null != parameters) {
            for (ExtendedPropertyParameter parameter : parameters) {
                if (name.equals(parameter.getName())) {
                    return parameter;
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExtendedProperty other = (ExtendedProperty) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        if (null != parameters && 0 < parameters.size()) {
            for (ExtendedPropertyParameter parameter : parameters) {
                stringBuilder.append(';').append(parameter.getName()).append('=').append(parameter.getValue());
            }
        }
        stringBuilder.append(':').append(value);
        return stringBuilder.toString();
    }

}
