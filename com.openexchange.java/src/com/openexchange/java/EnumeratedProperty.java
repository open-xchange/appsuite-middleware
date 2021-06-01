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

package com.openexchange.java;

/**
 * {@link EnumeratedProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class EnumeratedProperty {

    private final String value;

    /**
     * Initializes a new {@link EnumeratedProperty}.
     *
     * @param value The property value
     */
    protected EnumeratedProperty(String value) {
        super();
        this.value = value;
    }

    /**
     * Gets the known, standards-compliant values for the property.
     *
     * @return The standard values
     */
    protected abstract String[] getStandardValues();

    /**
     * Gets a string array from the values of the supplied enumerated properties.
     *
     * @param properties The properties to get the values from
     * @return The property values
     */
    protected static String[] getValues(EnumeratedProperty... properties) {
        if (null == properties) {
            return new String[0];
        }
        String[] values = new String[properties.length];
        for (int i = 0; i < properties.length; i++) {
            values[i] = properties[i].getValue();
        }
        return values;
    }

    /**
     * Gets the property value.
     *
     * @return The property value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the property's default value, which should be assumed if not specified.
     *
     * @return The default value for this property, or <code>null</code> if there is none
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * Gets a value indicating whether this property's value represents a known, standards-compliant value or not (which is the case for
     * custom <i>x-name</i> or unknown <i>iana-token</i> values).
     *
     * @return <code>true</code> if the property value is a <i>standard</i> value, <code>false</code>, otherwise
     */
    public boolean isStandard() {
        String value = getValue();
        String[] standardValues = getStandardValues();
        if (null != standardValues && 0 < standardValues.length) {
            for (String standardValue : standardValues) {
                if (standardValue.equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.toUpperCase().hashCode());
        return result;
    }

    /**
     * Gets a value indicating whether this property is equal to another one by comparing their values, ignoring case.
     *
     * @param obj The reference object with which to compare
     * @return <code>true</code> if the supplied object is equal to this one, <code>false</code>, otherwise
     */
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
        EnumeratedProperty other = (EnumeratedProperty) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equalsIgnoreCase(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * Gets a value indicating whether this property matches another one by comparing their values, ignoring case, and additionally,
     * if there is one, also considering the property's default value, which is assumed if passed object is <code>null</code>.
     *
     * @param obj The reference object with which to compare
     * @return <code>true</code> if the supplied object is matches this one, <code>false</code>, otherwise
     */
    public boolean matches(Object obj) {
        if (equals(obj)) {
            return true;
        }
        String defaultValue = getDefaultValue();
        if (null == defaultValue) {
            return false;
        }
        if (defaultValue.equals(value)) {
            // this is the default value, so it matches 'null'
            return null == obj;
        }
        return false;
    }

    @Override
    public String toString() {
        return value;
    }

}
