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
 * {@link ResellerCapability}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ResellerCapability implements Serializable {

    private static final long serialVersionUID = -8273111403847462004L;

    private final String id;
    private final int resellerId;
    private final int hashCode;

    /**
     * Initializes a new {@link ResellerCapability}.
     * 
     * @param id The capability identifier
     * @param resellerAdmin The {@link ResellerAdmin}
     */
    public ResellerCapability(String id, int resellerId) {
        super();
        this.id = id;
        this.resellerId = resellerId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + resellerId;
        hashCode = result;
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the reseller identifier
     *
     * @return The reseller identifier
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
        ResellerCapability other = (ResellerCapability) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (resellerId != other.resellerId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ResellerCapability [id=").append(id).append(", resellerId=").append(resellerId).append("]");
        return builder.toString();
    }
}
