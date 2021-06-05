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
 * {@link ResellerTaxonomy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ResellerTaxonomy implements Serializable {

    private static final long serialVersionUID = -6069196027590972948L;

    private final String taxonomy;
    private final int resellerId;
    private final int hashCode;

    /**
     * Initializes a new {@link ResellerTaxonomy}.
     * 
     * @param taxonomy The taxonomy
     * @param resellerId The reseller identifier
     */
    public ResellerTaxonomy(String taxonomy, int resellerId) {
        super();
        this.taxonomy = taxonomy;
        this.resellerId = resellerId;

        final int prime = 31;
        int result = 1;
        result = prime * result + resellerId;
        result = prime * result + ((taxonomy == null) ? 0 : taxonomy.hashCode());
        hashCode = result;
    }

    /**
     * Gets the taxonomy
     *
     * @return The taxonomy
     */
    public String getTaxonomy() {
        return taxonomy;
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
        ResellerTaxonomy other = (ResellerTaxonomy) obj;
        if (resellerId != other.resellerId) {
            return false;
        }
        if (taxonomy == null) {
            if (other.taxonomy != null) {
                return false;
            }
        } else if (!taxonomy.equals(other.taxonomy)) {
            return false;
        }
        return true;
    }
}
