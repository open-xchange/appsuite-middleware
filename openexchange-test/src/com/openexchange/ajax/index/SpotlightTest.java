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

package com.openexchange.ajax.index;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.index.actions.GeneralIndexRequest;
import com.openexchange.ajax.index.actions.GeneralIndexResponse;
import com.openexchange.ajax.index.actions.SpotlightRequest;

/**
 * {@link SpotlightTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SpotlightTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link SpotlightTest}.
     * 
     * @param name
     */
    public SpotlightTest() {
        super();
    }

    @Test
    public void testSpotlight() throws Exception {
        SpotlightRequest req = new SpotlightRequest("ubun*", 10, 10);
        //        long start = System.currentTimeMillis();
        GeneralIndexResponse resp = getClient().execute(req);
        //        long diff = System.currentTimeMillis() - start;
        JSONValue json = resp.getJSON();
        System.out.println("Spotligh Results:");
        System.out.println(json.toString(2, 0));
        System.out.println("==============");
        System.out.println();

        //        System.out.println("Duration: " + diff);
        String[] searchTerms = new String[2];
        JSONObject object = json.toObject();
        JSONArray persons = object.getJSONArray("persons");
        searchTerms[0] = (String) persons.getJSONObject(0).get("value");
        JSONArray topics = object.getJSONArray("topics");
        searchTerms[1] = (String) topics.getJSONObject(0).get("value");

        Parameter[] parameters = new Parameter[] { new Parameter("action", "persons"), new Parameter("searchTerm", searchTerms[0]) };
        GeneralIndexRequest searchReq = new GeneralIndexRequest(parameters);
        resp = getClient().execute(searchReq);
        json = resp.getJSON();
        System.out.println("Persons Search Results (" + searchTerms[0] + "):");
        System.out.println(json.toString(2, 0));
        System.out.println("==============");
        System.out.println();

        parameters = new Parameter[] { new Parameter("action", "topics"), new Parameter("searchTerm", searchTerms[1]) };
        searchReq = new GeneralIndexRequest(parameters);
        resp = getClient().execute(searchReq);
        json = resp.getJSON();
        System.out.println("Topics Search Results (" + searchTerms[1] + "):");
        System.out.println(json.toString(2, 0));
    }

}
