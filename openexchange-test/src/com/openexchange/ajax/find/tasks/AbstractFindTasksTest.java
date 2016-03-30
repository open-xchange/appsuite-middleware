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

package com.openexchange.ajax.find.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.tasks.Task;


/**
 * {@link AbstractFindTasksTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractFindTasksTest extends AbstractFindTest {

    /**
     * Initializes a new {@link AbstractFindTasksTest}.
     */
    public AbstractFindTasksTest(String name) {
        super(name);

    }
    
    /**
     * Fetch the results from the QueryResponse
     * @param qr the QueryResponse
     * @return the results as a JSONArray, or null if the respond does not contain a results payload
     */
    protected static final JSONArray getResults(QueryResponse qr) {
        JSONArray ret = null;
        if (qr.getData() != null && qr.getData() instanceof JSONObject)
            ret = ((JSONObject) qr.getData()).optJSONArray("results");
        return ret;
    }
    
    /**
     * Helper method to assert the query response (no paging)
     * 
     * @param expectedResultCount
     * @param f
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected final void assertResults(int expectedResultCount, List<ActiveFacet> f) throws OXException, IOException, JSONException {
        assertResults(expectedResultCount, f, -1, -1);
    }
    
    /**
     * Helper method to assert the query response (with paging)
     * 
     * @param expectedResultCount
     * @param f
     * @param start
     * @param size
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected final void assertResults(int expectedResultCount, List<ActiveFacet> f, int start, int size) throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(FindTasksTestEnvironment.createGlobalFacet());
        facets.addAll(f);
        final QueryResponse queryResponse = client.execute(new QueryRequest(start, size, facets, "tasks"));
        assertNotNull(queryResponse);
        JSONArray results  = getResults(queryResponse);
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
        
        for(Object o : results.asList()) {
            Map<String, Object> m = (Map<String, Object>) o;
            Task t = FindTasksTestEnvironment.getInstance().getTask((Integer) m.get("id"));
            assertNotNull("Expected object not found", t);
            assertEquals("Not the same", t.getTitle(), m.get("title"));
        }
    }
}
