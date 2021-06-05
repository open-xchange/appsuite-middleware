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
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.session.ServerSession;

/**
 * The {@link SearchService} is the entry point to utilize the Find API.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @since 7.6.0
 */
@SingletonService
public interface SearchService {

    /**
     * Performs an auto-complete request for a given module.
     *
     * @param autocompleteRequest The auto-complete search request to execute
     * @param module The module in which to perform the auto-complete search
     * @param session The associated session
     * @return An {@link AutocompleteResult}. Never <code>null</code>.
     */
    AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, Module module, ServerSession session) throws OXException;

    /**
     * Performs a search request for a given module.
     *
     * @param searchRequest The search request to execute
     * @param module The module in which to perform the search
     * @param session The associated session
     * @return A {@link SearchResult}. Never <code>null</code>.
     */
    SearchResult search(SearchRequest searchRequest, Module module, ServerSession session) throws OXException;

    /**
     * Gets the appropriate driver for given module.
     *
     * @param facetInfos The basic facet information
     * @param module The module
     * @param session The associated session
     * @return The driver
     * @throws OXException If no suitable driver exists
     */
    ModuleSearchDriver getDriver(List<FacetInfo> facetInfos, Module module, ServerSession session) throws OXException;

}
