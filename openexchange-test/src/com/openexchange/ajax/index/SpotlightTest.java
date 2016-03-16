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
     * @param name
     */
    public SpotlightTest(String name) {
        super(name);
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
