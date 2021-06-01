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

import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.ActiveFacet;

/**
 * Encapsulates an autocomplete request.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class AutocompleteRequest extends AbstractFindRequest {

    private static final long serialVersionUID = -5927763265822552092L;

    private final String prefix;

    private final int limit;


    /**
     * Initializes a new {@link AutocompleteRequest}.
     *
     * @param prefix The prefix to autocomplete on. Must not end with a wildcard character.
     * Must never be <code>null</code>, but may be empty.
     * @param activeFacets The list of currently active facets; must not be <code>null</code>
     * @param options A map containing client and module specific options; must not be <code>null</code>
     * @param limit The maximum number of values per facet to return, or <code>0</code> if unlimited
     */
    public AutocompleteRequest(final String prefix, final List<ActiveFacet> activeFacets, final Map<String, String> options, final int limit) {
        super(activeFacets, options);
        this.prefix = prefix;
        this.limit = limit;
    }

    /**
     * The prefix to autocomplete on.
     *
     * @return The (possibly empty) prefix string.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the maximum number of values per facet to return.
     *
     * @return The limit, or <code>0</code> if unlimited
     */
    public int getLimit() {
        return limit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + limit;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AutocompleteRequest other = (AutocompleteRequest) obj;
        if (limit != other.limit) {
            return false;
        }
        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equals(other.prefix)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AutocompleteRequest [prefix=" + prefix + ", limit=" + limit + ", activeFacets=" + getActiveFacets() + ", options=" + getOptions() + "]";
    }

}
