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

package com.openexchange.reseller.impl;

import java.io.Serializable;

/**
 * {@link ResellerValue}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
class ResellerValue implements Serializable {

    private static final long serialVersionUID = -1113412646692632746L;
    private final Integer resellerId;
    private final Integer parentId;
    private final int hashCode;

    /**
     * Initializes a new {@link ResellerValue}.
     * 
     * @param resellerId The reseller identifier
     * @param parentId The parent identifier for the reseller
     */
    ResellerValue(Integer resellerId, Integer parentId) {
        this.resellerId = resellerId;
        this.parentId = parentId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((resellerId == null) ? 0 : resellerId.hashCode());
        hashCode = result;
    }

    /**
     * Gets the resellerId
     *
     * @return The resellerId
     */
    Integer getResellerId() {
        return resellerId;
    }

    /**
     * Gets the parentId
     *
     * @return The parentId
     */
    Integer getParentId() {
        return parentId;
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
        ResellerValue other = (ResellerValue) obj;
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (resellerId == null) {
            if (other.resellerId != null) {
                return false;
            }
        } else if (!resellerId.equals(other.resellerId)) {
            return false;
        }
        return true;
    }
}
