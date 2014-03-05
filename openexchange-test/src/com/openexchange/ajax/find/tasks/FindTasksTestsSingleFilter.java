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

package com.openexchange.ajax.find.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;


/**
 * {@link FindTasksTests}
 * <p style="font-weight:bold; font-size:14px;">Matrix with Filter Combinations</p>
 * <table border="1">
 *  <tr style="font-weight: bold;"><td>&nbsp;</td><td>Type</td><td>Folder Type</td><td>Status</td><td>Participant</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">1</td><td>0</td><td>0</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">2</td><td>0</td><td>0</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">3</td><td>0</td><td>0</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">4</td><td>0</td><td>1</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">5</td><td>0</td><td>1</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">6</td><td>0</td><td>1</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">7</td><td>0</td><td>1</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">8</td><td>1</td><td>0</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">9</td><td>1</td><td>0</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">10</td><td>1</td><td>0</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">11</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">12</td><td>1</td><td>1</td><td>0</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">13</td><td>1</td><td>1</td><td>0</td><td>1</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">14</td><td>1</td><td>1</td><td>1</td><td>0</td></tr>
 *  <tr style="text-align: center"><td style="font-weight:bold;">15</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * </table>
 *
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksTestsSingleFilter extends AbstractFindTest {

    /**
     * Initializes a new {@link FindTasksTests}.
     */
    public FindTasksTestsSingleFilter(String name) {
        super(name);
    }

    /**
     * Get all relevant filters for the given combination
     * @param combination as char array
     * @return all relevant filters for that combination
     */
    private static final List<ActiveFacet> getRelevantActiveFacets(char[] combination) {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        ArrayUtils.reverse(combination);
        for (int i = 0; i < combination.length; i++) {
            if (combination[i] == '1')
                facets.addAll(FindTasksTestEnvironment.getInstance().getLoActiveFacets().get(i));
        }
        return facets;
    }

    /**
     * Fetch the results from the QueryResponse
     * @param qr the QueryResponse
     * @return the results as a JSONArray, or null if the respond does not contain a results payload
     */
    private static final JSONArray getResults(QueryResponse qr) {
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
    private final void assertResults(int expectedResultCount, List<ActiveFacet> f) throws OXException, IOException, JSONException {
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
    private final void assertResults(int expectedResultCount, List<ActiveFacet> f, int start, int size) throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(FindTasksTestEnvironment.createGlobalFacet());
        facets.addAll(f);
        final QueryResponse queryResponse = client.execute(new QueryRequest(start, size, facets, "tasks"));
        assertNotNull(queryResponse);
        JSONArray results  = getResults(queryResponse);
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////// TEST CASES BEGIN //////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test with simple query with no filters
     * Should find 30 tasks.
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     *
     * @see {@link FindTasksTestEnvironment.createAndInsertTasks}
     */
    @Test
    public void testWithSimpleQuery() throws OXException, IOException, JSONException {
        assertResults(30, Collections.<ActiveFacet>emptyList(), -1, 30);
    }

    /**
     * Test filter combination 1, i.e. with Participant (internal)
     *
     * 3 requests:
     *  - find all tasks with userA as participant (should find 5 tasks)
     *  - find all tasks with userB as participant (should find 2 tasks)
     *  - find all tasks with userA and userB as participant (should find 5 tasks)
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithInternalParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(1).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>(3);
        facets.add(f.get(0));
        assertResults(5, facets);
        
        facets.clear();
        facets.add(f.get(1));
        assertResults(4, facets);

        facets.clear();
        facets.add(f.get(0));
        facets.add(f.get(1));
        assertResults(5, facets);
    }

    /**
     * Test filter combination 1, i.e. with Participant (external)
     *
     * Should find 2 tasks
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithExternalParticipant() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(1).toCharArray());
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>(3);
        facets.add(f.get(2));
        facets.add(FindTasksTestEnvironment.createGlobalFacet());
        assertResults(2, facets);
    }

    /**
     * Test filter combination 1, i.e. with mixed Participants (both internal and external)
     *
     * should find 2 tasks
     * - both in user's a private folder, 1 with a+b+ext and 1 with a+ext
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @Test
    public void testWithMixedParticipants() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(1).toCharArray());
        assertResults(2, f);
    }

    /**
     * Test filter combination 2, i.e. with status
     * - NOT STARTED: 9
     * - IN PROGRESS: 6
     * - DONE:        6
     * - WAITING:     5
     * - DEFERRED:    5
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     */
    @Test
    public void testWithStatus() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(2).toCharArray());
        assertResults(9, Collections.singletonList(f.get(0)));
        assertResults(6, Collections.singletonList(f.get(1)));
        assertResults(6, Collections.singletonList(f.get(2)));
        assertResults(5, Collections.singletonList(f.get(3)));
        assertResults(5, Collections.singletonList(f.get(4)));
    }
    
    /**
     * Test filter combination 4, i.e. with folder type
     * - in PRIVATE: 10
     * - in PUBLIC: 10
     * - in SHARED: 11
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     */
    @Test
    public void testWithFolderType() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(4).toCharArray());
        assertResults(10, Collections.singletonList(f.get(0))); //private
        assertResults(10, Collections.singletonList(f.get(1))); //public
        assertResults(11, Collections.singletonList(f.get(2))); //shared
    }
    
    /**
     * Test filter combination 8, i.e. with task type
     * - SINGLE: 29
     * - SERIES:  2
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     */
    @Test
    public void testWithType() throws OXException, IOException, JSONException {
        List<ActiveFacet> f = getRelevantActiveFacets(Integer.toBinaryString(8).toCharArray());
        assertResults(29, Collections.singletonList(f.get(0)), -1, 30);
        assertResults(2, Collections.singletonList(f.get(1)));
    }
}
