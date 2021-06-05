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

package com.openexchange.config.lean;


/**
 * {@link DefaultProperty} - The default property implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class DefaultProperty implements Property {

    /**
     * Creates the appropriate property for specified arguments.
     *
     * @param fqPropertyName The fully qualifying property name; e.g. <code>"com.openexchange.module.option"</code>
     * @param def The default value to return in case such a property is not available/existent
     * @return The appropriate <code>DefaultProperty</code> instance
     */
    public static DefaultProperty valueOf(String fqPropertyName, Object def) {
        return new DefaultProperty(fqPropertyName, def);
    }

    // ----------------------------------------------------------------------------------------

    private final String propertyName;
    private final Object def;
    private int hash;

    /**
     * Initializes a new {@link DefaultProperty}.
     */
    private DefaultProperty(String propertyName, Object def) {
        super();
        this.propertyName = propertyName;
        this.def = def;
        hash = 0;
    }

    @Override
    public String getFQPropertyName() {
        return propertyName;
    }

    @Override
    public Object getDefaultValue() {
        return def;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = prime * 1 + ((propertyName == null) ? 0 : propertyName.hashCode());
            result = prime * result + ((def == null) ? 0 : def.hashCode());
            hash = result;
        }
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
        DefaultProperty other = (DefaultProperty) obj;
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        if (def == null) {
            if (other.def != null) {
                return false;
            }
        } else if (!def.equals(other.def)) {
            return false;
        }
        return true;
    }

}
