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

package com.openexchange.find.internal;

import java.util.List;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.facet.FacetInfo;

/**
 * {@link LookUpInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class LookUpInfo {

    /** The empty look-up info */
    public static final LookUpInfo EMPTY = new LookUpInfo(null, null);

    // -----------------------------------------------------------------------

    private final AbstractFindRequest findRequest;
    private final List<FacetInfo> facetInfos;

    /**
     * Initializes a new {@link LookUpInfo}.
     *
     * @param findRequest The associated find request (if any)
     * @param facetInfos The basic facet information (if any)
     */
    public LookUpInfo(AbstractFindRequest findRequest, List<FacetInfo> facetInfos) {
        super();
        this.findRequest = findRequest;
        this.facetInfos = facetInfos;
    }

    /**
     * Gets the basic facet information
     *
     * @return The basic facet information or <code>null</code>
     */
    public List<FacetInfo> getFacetInfos() {
        return facetInfos;
    }

    /**
     * Gets the associated find request
     *
     * @return The associated find request or <code>null</code>
     */
    public AbstractFindRequest getFindRequest() {
        return findRequest;
    }

}
