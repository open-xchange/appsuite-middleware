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
package com.openexchange.find;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates a search request.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class SearchRequest implements Serializable {

    private static final long serialVersionUID = -3958179907725259325L;

    private final int start;

    private final int size;

    private final List<String> queries;

    private final List<Filter> filters;


    public SearchRequest(int start, int size, List<String> queries, List<Filter> filters) {
        super();
        this.start = start;
        this.size = size;
        this.queries = queries;
        this.filters = filters;
    }

    /**
     * Used for pagination.
     * @return The start index within the set of total results.
     * Never negative.
     */
    public int getStart() {
        return start;
    }

    /**
     * Used for pagination.
     * @return The max. number of documents to return.
     * Never negative.
     */
    public int getSize() {
        return size;
    }

    /**
     * A list of queries to search for.
     * @return Never <code>null</code>.
     * May be empty to denote no query at all.
     */
    public List<String> getQueries() {
        return queries;
    }

    /**
     * A list of filters to be applied on the search results.
     * @return May be empty but never <code>null</code>.
     */
    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filters == null) ? 0 : filters.hashCode());
        result = prime * result + ((queries == null) ? 0 : queries.hashCode());
        result = prime * result + size;
        result = prime * result + start;
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
        SearchRequest other = (SearchRequest) obj;
        if (filters == null) {
            if (other.filters != null)
                return false;
        } else if (!filters.equals(other.filters))
            return false;
        if (queries == null) {
            if (other.queries != null)
                return false;
        } else if (!queries.equals(other.queries))
            return false;
        if (size != other.size)
            return false;
        if (start != other.start)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest [start=" + start + ", size=" + size + ", queries=" + queries + ", filters=" + filters + "]";
    }

}
