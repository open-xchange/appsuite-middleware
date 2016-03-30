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

package com.openexchange.find.facet;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An {@link ActiveFacet} is a facet that is currently selected to
 * filter search results.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ActiveFacet {

    private final FacetType type;

    private final String valueId;

    private final Filter filter;

    /**
     * Initializes a new {@link ActiveFacet}.
     * @param type The facets type.
     * @param valueId The id of the selected value.
     * @param filter The filter according to the value.
     * Use {@link Filter#NO_FILTER} if constructing a real filter makes no sense.
     */
    public ActiveFacet(FacetType type, String valueId, Filter filter) {
        super();
        checkNotNull(type);
        checkNotNull(valueId);
        checkNotNull(filter);
        this.type = type;
        this.valueId = valueId;
        this.filter = filter;
    }

    /**
     * The type of this facet.
     *
     * @return The type; never <code>null</code>.
     */
    public FacetType getType() {
        return type;
    }

    /**
     * The id of the selected value. Generally corresponds to the id attribute
     * of a {@link FacetValue} that was present in a previous autocomplete response.
     * In some special cases the value can be used to realize custom filters.
     *
     * @return The id; never <code>null</code>.
     */
    public String getValueId() {
        return valueId;
    }

    /**
     * The filter for the according value. This should always be the
     * unmodified filter object that was written out for the referenced
     * value within a previous autocomplete response.
     *
     * @return The filter. Can be {@link Filter#NO_FILTER} but never <code>null</code>.
     */
    public Filter getFilter() {
        return filter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((valueId == null) ? 0 : valueId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActiveFacet other = (ActiveFacet) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (valueId == null) {
            if (other.valueId != null)
                return false;
        } else if (!valueId.equals(other.valueId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ActiveFacet [type=" + type + ", valueId=" + valueId + ", filter=" + filter + "]";
    }

}
