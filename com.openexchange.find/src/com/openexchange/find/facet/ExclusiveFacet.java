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
 * An {@link ExclusiveFacet} is a facet where the contained values are
 * mutually exclusive. That means that the facet must only be present once
 * in search requests.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ExclusiveFacet extends DefaultFacet {

    private static final long serialVersionUID = -8388379773362556244L;

    public ExclusiveFacet(FacetType type) {
        super(type);
    }

    public ExclusiveFacet(FacetType type, List<FacetValue> values) {
        super(type, values);
    }

    ExclusiveFacet() {
        super();
    }

    @Override
    public String getStyle() {
        return "exclusive";
    }

    @Override
    public void accept(FacetVisitor visitor) {
        visitor.visit(this);
    }

}
