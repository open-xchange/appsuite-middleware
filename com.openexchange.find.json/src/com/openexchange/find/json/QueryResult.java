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

package com.openexchange.find.json;

import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;


/**
 * Encapsulates a {@link SearchResult} and additional attributes that are needed
 * to create a JSON response object.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class QueryResult {

    private final SearchRequest searchRequest;

    private final SearchResult searchResult;

    /**
     * Initializes a new {@link QueryResult}.
     * @param searchRequest The search reguest object; never <code>null</code>.
     * @param searchResult The search result object; never <code>null</code>.
     */
    public QueryResult(SearchRequest searchRequest, SearchResult searchResult) {
        super();
        this.searchRequest = searchRequest;
        this.searchResult = searchResult;
    }

    /**
     * @return The search request object; never <code>null</code>.
     */
    public SearchRequest getSearchRequest() {
        return searchRequest;
    }

    /**
     * @return The search result object; never <code>null</code>.
     */
    public SearchResult getSearchResult() {
        return searchResult;
    }

}
