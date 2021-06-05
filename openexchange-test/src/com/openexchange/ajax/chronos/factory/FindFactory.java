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

package com.openexchange.ajax.chronos.factory;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.testing.httpclient.models.FindActiveFacet;
import com.openexchange.testing.httpclient.models.FindActiveFacetFilter;
import com.openexchange.testing.httpclient.models.FindOptionsData;
import com.openexchange.testing.httpclient.models.FindQueryBody;

/**
 * {@link FindFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindFactory {

    /**
     * Creates a {@link FindQueryBody} with the specified query and folder identifier
     * 
     * @param query The query
     * @param folderId The folder identifier
     * @return The {@link FindQueryBody}
     */
    public static final FindQueryBody createFindBody(String query, String folderId) {
        FindActiveFacet facetsItem = new FindActiveFacet();
        facetsItem.setFacet("folder");
        facetsItem.setValue(folderId);

        FindActiveFacetFilter filter = new FindActiveFacetFilter();
        filter.addFieldsItem("global");
        filter.addQueriesItem(query);

        FindActiveFacet q = new FindActiveFacet();
        q.setFilter(filter);
        q.setFacet("global");
        q.setValue(query);

        FindOptionsData options = new FindOptionsData();
        options.setAdmin(Boolean.FALSE);
        options.setTimezone("UTC");

        FindQueryBody queryBody = new FindQueryBody();
        queryBody.addFacetsItem(facetsItem);
        queryBody.addFacetsItem(q);
        queryBody.setStart(I(0));
        queryBody.setSize(I(101));
        queryBody.setOptions(options);

        return queryBody;
    }
}
