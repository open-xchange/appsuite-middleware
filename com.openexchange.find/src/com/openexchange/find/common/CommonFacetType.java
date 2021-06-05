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

package com.openexchange.find.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.TimeFrame;
import com.openexchange.java.Strings;

/**
 * {@link CommonFacetType}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum CommonFacetType implements FacetType {

    /**
     * The global facet is a field facet, that applies to all modules and is meant
     * to be used as a filter that searches in an implementation-defined set of fields.
     */
    GLOBAL,
    /**
     * The facet type for folders. The presence of this facet is mutually exclusive
     * with {@link CommonFacetType#FOLDER_TYPE}.
     */
    FOLDER(CommonStrings.FACET_TYPE_FOLDER),
    /**
     * The facet type for folder type. The presence of this facet is mutually exclusive
     * with {@link CommonFacetType#FOLDER}.
     */
    FOLDER_TYPE(CommonStrings.FACET_TYPE_FOLDER_TYPE),
    /**
     * The facet type for dates. A module that supports this facet may provide some
     * default values to indicate that the facet exists (e.g. "last week"). It also has to
     * support client-defined time ranges with a special syntax denoting a frame between two
     * timestamps:
     *
     * <ul>
     *   <li><code>[&lt;from&gt; TO &lt;to&gt;]</code></li>
     *   <li><code>{&lt;from&gt; TO &lt;to&gt;}</code></li>
     * </ul>
     *
     * For <code>[]</code> &lt;from&gt; and &lt;to&gt; are inclusive. For <code>{}</code>
     * &lt;from&gt; and &lt;to&gt; are exclusive. Both, &lt;from&gt; and &lt;to&gt; are
     * timestamps in milliseconds since midnight, January 1, 1970 UTC. It is also possible to
     * use the asterisk as a wildcard, e.g. <code>[* TO 1407142786432]</code>.
     *
     * If the filter for an {@link ActiveFacet} of type {@link CommonFacetType#DATE} is {@link Filter#NO_FILTER},
     * its value is expected to be such a custom time range. Modules are advised to use {@link TimeFrame#valueOf(String)}
     * to parse the input string.
     */
    DATE(CommonStrings.DATE),
    ACCOUNT(CommonStrings.ACCOUNT)
    ;

    private static final Map<String, CommonFacetType> typesById = new HashMap<String, CommonFacetType>();
    static {
        for (CommonFacetType type : values()) {
            typesById.put(type.getId(), type);
        }

        FOLDER.conflictingFacets.add(FOLDER_TYPE);
        FOLDER_TYPE.conflictingFacets.add(FOLDER);
    }

    private final String displayName;

    private final List<FacetType> conflictingFacets = new LinkedList<FacetType>();

    private CommonFacetType() {
        this(null);
    }

    private CommonFacetType(final String displayName, final FacetType... conflictingFacets) {
        this.displayName = displayName;
        if (conflictingFacets != null) {
            for (FacetType conflicting : conflictingFacets) {
                this.conflictingFacets.add(conflicting);
            }
        }
    }

    @Override
    public String getId() {
        return toString().toLowerCase();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<FacetType> getConflictingFacets() {
        return conflictingFacets;
    }

    /**
     * Gets a {@link CommonFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static CommonFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        return typesById.get(id);
    }

}
