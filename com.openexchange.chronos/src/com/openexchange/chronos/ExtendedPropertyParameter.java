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

/**
 * {@link ExtendedPropertyParameter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedPropertyParameter {

    private final String name;
    private final String value;

    /**
     * Initializes a new {@link ExtendedPropertyParameter}.
     *
     * @param name The parameter name, or <code>null</code> for a value-only parameter
     * @param value The value
     */
    public ExtendedPropertyParameter(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * Initializes a new {@link ExtendedPropertyParameter} based on another parameter.
     *
     * @param other The to copy over the name and value from
     */
    public ExtendedPropertyParameter(ExtendedPropertyParameter other) {
        this(other.name, other.value);
    }

    /**
     * Gets the parameter name.
     *
     * @return The parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parameter value.
     *
     * @return The parameter value
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ExtendedPropertyParameter other = (ExtendedPropertyParameter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
        return "ExtendedPropertyParameter [name=" + name + ", value=" + value + "]";
    }

}
