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

import java.util.List;


/**
 * {@link Facet}s are used to refine a search by filtering results based
 * on categories.<br>
 * <br>
 * Example:<br>
 * You are searching in the mail module. A possible facet here can be "contacts".
 * A possible {@link FacetValue} might be "John Doe".
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public interface Facet {

    /**
     * Gets the facets style.
     *
     * @return The style, never <code>null</code>.
     */
    String getStyle();

    /**
     * Gets the facet type.
     *
     * @return The type, never <code>null</code>.
     */
    FacetType getType();

    /**
     * Gets the facets flags.
     *
     * @return A list of flags; never <code>null</code> but possibly empty.
     */
    List<String> getFlags();

    /**
     * Accepts a {@link FacetVisitor}.
     *
     * @param visitor The visitor, never <code>null</code>.
     */
    void accept(FacetVisitor visitor);

}
