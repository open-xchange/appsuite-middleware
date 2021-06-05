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
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Option implements Serializable{

    private static final long serialVersionUID = 5585827431039480470L;

    private final String id;

    private final String name;

    private final Filter filter;

    /**
     * Convenience method that takes a localizable string to construct an according
     * {@link SimpleDisplayItem}.
     * @param id
     *   The unique id of this option within a {@link FacetValue}.
     * @param name
     *   The localizable display name of this option (shown within a client).
     * @param filter
     *   The filter.
     */
    public static Option newInstance(String id, String name, Filter filter) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(filter);
        return new Option(id, name, filter);
    }

    private Option(String id, String name, Filter filter) {
        super();
        this.id = id;
        this.name = name;
        this.filter = filter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Filter getFilter() {
        return filter;
    }

}
