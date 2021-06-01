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

package com.openexchange.ajax.find.actions;

import java.util.List;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.find.facet.Facet;

/**
 * {@link AutocompleteResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AutocompleteResponse extends AbstractAJAXResponse {

    private final List<Facet> facets;

    /**
     * Initializes a new {@link AutocompleteResponse}.
     *
     * @param response The response
     * @param facets The factets
     */
    public AutocompleteResponse(final Response response, final List<Facet> facets) {
        super(response);
        this.facets = facets;
    }

    /**
     * Gets the facets
     *
     * @return The facets
     */
    public List<Facet> getFacets() {
        return facets;
    }

}
