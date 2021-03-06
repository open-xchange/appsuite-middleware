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

import java.io.Serializable;
import java.util.List;
import com.openexchange.i18n.I18nService;


/**
 * Defines the type of a facet. Every type must be static and must be registered
 * in {@link Facets}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public interface FacetType extends Serializable {

    /**
     * Gets the id of this type, that uniquely identifies
     * a facet within a module.
     * @return The id, never <code>null</code>.
     */
    String getId();

    /**
     * Gets the display name of this type, that might be shown in
     * a user interface. Should be localizable and therefore a be valid
     * key for {@link I18nService}.
     * @return The display name. Should be <code>null</code> if and
     * only if this facet is a field facet.
     */
    String getDisplayName();

    /**
     * Gets a list of facet types that must not be used together with this one in
     * autocomplete and query requests because they are mutually exclusive
     * (e.g. folder and folder type).
     *
     * @return A list of conflicting types; never <code>null</code>.
     */
    List<FacetType> getConflictingFacets();

}
