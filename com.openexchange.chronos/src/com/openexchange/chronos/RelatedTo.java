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
 * {@link RelatedTo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.5">RFC 5545, section 3.8.4.5</a>
 */
public class RelatedTo {

    private final String relType;
    private final String value;

    /**
     * Initializes a new {@link RelatedTo}.
     *
     * @param relType The relationship type, or <code>null</code> for the default <code>PARENT</code> relationship
     * @param value The value, i.e. the unique identifier of the referenced component
     */
    public RelatedTo(String relType, String value) {
        super();
        this.relType = relType;
        this.value = value;
    }

    /**
     * Gets the relType
     *
     * @return The relType
     */
    public String getRelType() {
        return relType;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((relType == null) ? 0 : relType.hashCode());
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
        RelatedTo other = (RelatedTo) obj;
        if (relType == null) {
            if (other.relType != null) {
                return false;
            }
        } else if (!relType.equals(other.relType)) {
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
        return "RelatedTo [relType=" + relType + ", value=" + value + "]";
    }

}
