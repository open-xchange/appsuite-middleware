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

package com.openexchange.find.facet;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;


/**
 * An {@link ActiveFacet} is a facet that is currently selected to
 * filter search results.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ActiveFacet implements Serializable {

    private static final long serialVersionUID = -7373982187282668862L;

    private final FacetType type;

    private final String valueId;

    private final Filter filter;

    /**
     * Initializes a new {@link ActiveFacet}.
     * @param type The facets type.
     * @param valueId The id of the selected value.
     * @param filter The filter according to the value.
     * Use {@link Filter#NO_FILTER} if constructing a real filter makes no sense.
     */
    public ActiveFacet(FacetType type, String valueId, Filter filter) {
        super();
        checkNotNull(type);
        checkNotNull(valueId);
        checkNotNull(filter);
        this.type = type;
        this.valueId = valueId;
        this.filter = filter;
    }

    /**
     * The type of this facet.
     *
     * @return The type; never <code>null</code>.
     */
    public FacetType getType() {
        return type;
    }

    /**
     * The id of the selected value. Generally corresponds to the id attribute
     * of a {@link FacetValue} that was present in a previous autocomplete response.
     * In some special cases the value can be used to realize custom filters.
     *
     * @return The id; never <code>null</code>.
     */
    public String getValueId() {
        return valueId;
    }

    /**
     * The filter for the according value. This should always be the
     * unmodified filter object that was written out for the referenced
     * value within a previous autocomplete response.
     *
     * @return The filter. Can be {@link Filter#NO_FILTER} but never <code>null</code>.
     */
    public Filter getFilter() {
        return filter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((valueId == null) ? 0 : valueId.hashCode());
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
        ActiveFacet other = (ActiveFacet) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (valueId == null) {
            if (other.valueId != null) {
                return false;
            }
        } else if (!valueId.equals(other.valueId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ActiveFacet [type=" + type + ", valueId=" + valueId + ", filter=" + filter + "]";
    }

}
