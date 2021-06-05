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

package com.openexchange.reseller.data;

import java.io.Serializable;

/**
 * {@link ResellerConfigProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ResellerConfigProperty implements Serializable {

    private static final long serialVersionUID = -1880273509202898478L;

    private final String key;
    private final String value;
    private final int resellerId;
    private final int hashCode;

    /**
     * Initializes a new {@link ResellerConfigProperty}.
     * 
     * @param key The property's key
     * @param value The property's value
     * @param resellerId The reseller identifier
     */
    public ResellerConfigProperty(String key, String value, int resellerId) {
        super();
        this.key = key;
        this.value = value;
        this.resellerId = resellerId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + resellerId;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        hashCode = result;
    }

    /**
     * Gets the key
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the resellerId
     *
     * @return The resellerId
     */
    public int getResellerId() {
        return resellerId;
    }

    @Override
    public int hashCode() {
        return hashCode;
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
        ResellerConfigProperty other = (ResellerConfigProperty) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (resellerId != other.resellerId) {
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
        StringBuilder builder = new StringBuilder();
        builder.append("ResellerConfigProperty [key=").append(key).append(", value=").append(value).append(", resellerId=").append(resellerId).append("]");
        return builder.toString();
    }
}
