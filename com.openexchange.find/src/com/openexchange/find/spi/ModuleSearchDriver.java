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

package com.openexchange.find.spi;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.tools.session.ServerSession;

/**
 * A {@link ModuleSearchDriver} has to be implemented for every module that enables searching via the find API.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public interface ModuleSearchDriver {

    /** The superior ranking that must not be exceeded by a registered <code>ModuleSearchDriver</code> */
    public static final int RANKING_SUPERIOR = 100000;

    /**
     * Gets the module supported by this driver.
     *
     * @return The module supported by this driver.
     */
    Module getModule();

    /**
     * Checks if this driver applies to a given {@link ServerSession}.
     *
     * @param session The associated session
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session) throws OXException;

    /**
     * Checks if this driver applies to a given {@link ServerSession} and concrete find request.
     *
     * @param session The associated session
     * @param findRequest The current find request
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException;

    /**
     * Checks if this driver applies to a given {@link ServerSession} and concrete find request.
     *
     * @param session The associated session
     * @param facetInfos The current facet information
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    boolean isValidFor(ServerSession session, List<FacetInfo> facetInfos) throws OXException;

    /**
     * Gets the driver-specific {@link SearchConfiguration}. May be individual for the
     * given session.
     *
     * @param session The associated session
     * @return The configuration; never <code>null</code>.
     */
    SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException;

    /**
     * Performs an auto-complete request.
     *
     * @param autocompleteRequest The associated request
     * @param session The associated session
     * @return The {@link AutocompleteResult}. Never <code>null</code>.
     */
    AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;

    /**
     * Performs a search request.
     *
     * @param searchRequest The associated request
     * @param session The associated session
     * @return The {@link SearchResult}. Never <code>null</code>.
     */
    SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException;

}
