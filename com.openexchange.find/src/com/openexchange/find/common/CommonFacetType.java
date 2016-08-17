/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
