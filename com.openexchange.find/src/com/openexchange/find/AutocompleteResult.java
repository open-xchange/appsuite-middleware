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
package com.openexchange.find;

import java.io.Serializable;
import java.util.List;
import com.openexchange.find.facet.Facet;

/**
 * The result of an {@link AutocompleteRequest}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class AutocompleteResult implements Serializable {

    private static final long serialVersionUID = -8830406356267375791L;

    private List<Facet> facets;


    public AutocompleteResult(List<Facet> facets) {
        super();
        this.facets = facets;
    }

    /**
     * @return A list of facets based on the search for the requests prefix.
     * May be empty but never <code>null</code>.
     */
    public List<Facet> getFacets() {
        return facets;
    }

    /**
     * Sets the facets.
     */
    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((facets == null) ? 0 : facets.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AutocompleteResult other = (AutocompleteResult) obj;
        if (facets == null) {
            if (other.facets != null)
                return false;
        } else if (!facets.equals(other.facets))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AutocompleteResult [facets=" + facets + "]";
    }

}
