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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.openexchange.find.SearchRequest;

/**
 *
 * A {@link FacetValue} is a possible value for a given {@link Facet}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FacetValue implements Serializable {

    private static final long serialVersionUID = -7719065379433828901L;

    public static final int UNKNOWN_COUNT = -1;

    private final String id;

    private final DisplayItem displayItem;

    private final int count;

    private final List<Filter> filters;


    /**
     * Initializes a new {@link FacetValue}.
     *
     * @param id
     *   The values id which identifies it uniquely within all values of a facet.
     * @param displayItem
     *   The display item. May be {@link FacetValue#NO_DISPLAY_ITEM} if
     *   and only if this value is the only one for its facet.
     * @param count
     *   The number of result documents that apply to the given filter.
     *   {@link FacetValue#UNKNOWN_COUNT} if unknown.
     * @param filter
     *   The filter.
     */
    public FacetValue(String id, DisplayItem displayItem, int count, Filter filter) {
        this(id, displayItem, count, Collections.singletonList(filter));
    }

    /**
     * Initializes a new {@link FacetValue}.
     *
     * @param id
     *   The values id which identifies it uniquely within all values of a facet.
     * @param displayItem
     *   The display item. May be {@link FacetValue#NO_DISPLAY_ITEM} if
     *   and only if this value is the only one for its facet.
     * @param count
     *   The number of result documents that apply to the given filter.
     *   {@link FacetValue#UNKNOWN_COUNT} if unknown.
     * @param filters
     *   The filters.
     */
    public FacetValue(String id, DisplayItem displayItem, int count, List<Filter> filters) {
        super();
        checkNotNull(id);
        checkNotNull(displayItem);
        checkNotNull(filters);
        checkArgument(filters.size() > 0);
        this.id = id;
        this.displayItem = displayItem;
        this.count = count;
        this.filters = filters;
    }

    /**
     * Gets the values id which identifies it uniquely within all values of a facet.
     * @return The id, never <code>null</code>.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The display item. Never <code>null</code>.
     */
    public DisplayItem getDisplayItem() {
        return displayItem;
    }

    /**
     * @return The number of results to which this value applies.
     * May be {@link FacetValue#UNKNOWN_COUNT} if unknown. if unknown.
     */
    public int getCount() {
        return count;
    }

    /**
     * @return The filters of which one has to be applied to
     * {@link SearchRequest}s to filter on this value.
     * Never <code>null</code>.
     */
    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + count;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((filters == null) ? 0 : filters.hashCode());
        result = prime * result + ((displayItem == null) ? 0 : displayItem.hashCode());
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
        FacetValue other = (FacetValue) obj;
        if (!id.equals(other.id))
            return false;
        if (count != other.count)
            return false;
        if (filters == null) {
            if (other.filters != null)
                return false;
        } else if (!filters.equals(other.filters))
            return false;
        if (displayItem == null) {
            if (other.displayItem != null)
                return false;
        } else if (!displayItem.equals(other.displayItem))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FacetValue [id=" + id + ", name=" + displayItem + ", count=" + count + ", filters=" + filters + "]";
    }
}
