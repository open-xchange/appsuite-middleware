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
package com.openexchange.find;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.Filter;

/**
 * Encapsulates a search request.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class SearchRequest extends AbstractFindRequest {

    private static final long serialVersionUID = -3958179907725259325L;

    private final int start;

    private final int size;

    private final Columns columns;

    private List<Filter> filters;

    private List<String> queries;


    /**
     * Initializes a new {@link SearchRequest}.
     *
     * @param start The start index for pagination
     * @param size The max. number of documents to return
     * @param activeFacets The list of currently active facets; must not be <code>null</code>
     * @param options A map containing client and module specific options; must not be <code>null</code>
     * @param columns The columns that shall be returned in the response items or <code>null</code> to use the modules default
     */
    public SearchRequest(final int start, final int size, final List<ActiveFacet> activeFacets, final Map<String, String> options, final String[] columns) {
        super(activeFacets, options);
        this.start = start;
        this.size = size;
        this.columns = new Columns(columns);
    }

    /**
     * The start index for pagination.
     *
     * @return The start index within the set of total results.
     * Never negative.
     */
    public int getStart() {
        return start;
    }

    /**
     * The maximum size of the result set for pagination.
     *
     * @return The max. number of documents to return.
     * Never negative.
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the columns that shall be returned in the response items.
     *
     * @return The columns; never <code>null</code>
     */
    public Columns getColumns() {
        return columns;
    }

    /**
     * A list of queries to search for.
     *
     * @return Never <code>null</code>. May be empty to denote no query at all.
     */
    public List<String> getQueries() {
        if (queries == null) {
            List<ActiveFacet> globals = facetMap.get(CommonFacetType.GLOBAL);
            if (globals == null) {
                queries = Collections.emptyList();
            } else {
                queries = new LinkedList<String>();
                for (ActiveFacet facet : globals) {
                    queries.addAll(facet.getFilter().getQueries());
                }
            }
        }

        return queries;
    }

    /**
     * A list of filters to be applied on the search results based
     * on the currently active facets. {@link CommonFacetType#GLOBAL},
     * {@link CommonFacetType#FOLDER}, {@link CommonFacetType#FOLDER_TYPE}
     * and
     * are always ignored when constructing the filters.
     *
     * @return May be empty but never <code>null</code>.
     */
    public List<Filter> getFilters() {
        if (filters == null) {
            filters = new LinkedList<Filter>();
            Set<FacetType> exclude = new HashSet<FacetType>(2);
            exclude.add(CommonFacetType.GLOBAL);
            exclude.add(CommonFacetType.FOLDER);
            exclude.add(CommonFacetType.FOLDER_TYPE);
            exclude.add(CommonFacetType.DATE);
            for (Entry<FacetType, List<ActiveFacet>> entry : facetMap.entrySet()) {
                FacetType type = entry.getKey();
                if (!exclude.contains(type)) {
                    for (ActiveFacet facet : entry.getValue()) {
                        Filter filter = facet.getFilter();
                        if (filter != Filter.NO_FILTER) {
                            filters.add(filter);
                        }
                    }
                }
            }
        }

        return filters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((facetMap == null) ? 0 : facetMap.hashCode());
        result = prime * result + size;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchRequest other = (SearchRequest) obj;
        if (facetMap == null) {
            if (other.facetMap != null)
                return false;
        } else if (!facetMap.equals(other.facetMap))
            return false;
        if (size != other.size)
            return false;
        if (start != other.start)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest [start=" + start + ", size=" + size + ", queries=" + getQueries() + ", filters=" + getFilters() + ", options=" + getOptions() + "]";
    }

}
