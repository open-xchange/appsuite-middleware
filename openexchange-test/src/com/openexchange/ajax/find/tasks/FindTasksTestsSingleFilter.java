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
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.Filter;


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
    private static final List<Filter> getRelevantFilters(char[] combination) {
        List<Filter> filters = new ArrayList<Filter>();
        for (int i = 0; i < combination.length; i++) {
            if (combination[i] == '1')
                filters.addAll(FindTasksTestEnvironment.getInstance().getLolFilters().get(i));
        }
        return filters;
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
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////// TEST CASES BEGIN //////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Test with simple query with no filters
     * Should find 28 tasks.
     * 
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     *  
     * @see {@link FindTasksTestEnvironment.createAndInsertTasks}
     */
    @Test
    public void testWithSimpleQuery() throws OXException, IOException, JSONException {
        List<String> queries = Collections.singletonList(FindTasksTestEnvironment.getTrackingID());
        List<Filter> filters = Collections.emptyList();
        final QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, filters, "tasks"));

        assertNotNull(queryResponse);
        
        //assert the response
        JSONArray results  = getResults(queryResponse);
        int expectedResultCount = 29;
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
    }
    
    /**
     * Test filter combination 1, i.e. with Participant (internal)
     * 
     * 3 requests: 
     *  - find all tasks with userA as participant (should find 3 tasks)
     *  - find all tasks with userB as participant (should find 2 tasks)
     *  - find all tasks with userA and userB as participant (should find 3 tasks)
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithInternalParticipant() throws OXException, IOException, JSONException {
        List<Filter> f = getRelevantFilters(Integer.toBinaryString(1).toCharArray());
        List<String> queries = Collections.singletonList(FindTasksTestEnvironment.getTrackingID());
        QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, Collections.singletonList(f.get(0)), "tasks"));
        
        //assert the response
        JSONArray results  = getResults(queryResponse);
        int expectedResultCount = 3;
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
        
        queryResponse = client.execute(new QueryRequest(0, 10, queries, Collections.singletonList(f.get(1)), "tasks"));
        
        results  = getResults(queryResponse);
        expectedResultCount = 2;
        actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
        
        List<Filter> filters = new ArrayList<Filter>(2);
        filters.add(f.get(0));
        filters.add(f.get(1));
        queryResponse = client.execute(new QueryRequest(0, 10, queries, filters, "tasks"));
        results  = getResults(queryResponse);
        expectedResultCount = 3;
        actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
    }
    
    /**
     * Test filter combination 1, i.e. with Participant (external)
     * 
     * Should find 1 task
     * 
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     */
    @Test
    public void testWithExternalParticipant() throws OXException, IOException, JSONException {
        List<Filter> f = getRelevantFilters(Integer.toBinaryString(1).toCharArray());
        List<String> queries = Collections.singletonList(FindTasksTestEnvironment.getTrackingID());
        final QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, Collections.singletonList(f.get(2)), "tasks"));
        
        //assert the response
        JSONArray results  = getResults(queryResponse);
        int expectedResultCount = 2;
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
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
        List<Filter> f = getRelevantFilters(Integer.toBinaryString(1).toCharArray());
        List<String> queries = Collections.singletonList(FindTasksTestEnvironment.getTrackingID());
        
        final QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, f, "tasks"));
        JSONArray results  = getResults(queryResponse);
        int expectedResultCount = 2;
        int actualResultCount = results.asList().size();
        assertEquals(expectedResultCount, actualResultCount);
    }
    
    /**
     * Test filter combination 2, i.e. with status
     */
    @Test
    public void testWithStatus() {
        List<Filter> f = getRelevantFilters(Integer.toBinaryString(2).toCharArray());
        List<String> queries = Collections.singletonList(FindTasksTestEnvironment.getTrackingID());
        
    }
    
    
    /*@Test
    public void testSimpleFilter() {
        try {

            List<String> queries = Collections.singletonList("mail");
            List<Filter> filters = new ArrayList<Filter>();
            //filters.add(new Filter(Collections.singletonList("folder_type"), "shared"));
            //filters.add(new Filter(Collections.singletonList("folder_type"), "private"));
            //filters.add(new Filter(Collections.singletonList("type"), "single_task"));
            //filters.add(new Filter(Collections.singletonList("participant"), "5"));
            //filters.add(new Filter(Collections.singletonList("participant"), "foo@bar.org"));
            //filters.add(new Filter(Collections.singletonList("participant"), "bar@foo.org"));
            //filters.add(new Filter(Collections.singletonList("status"), "1"));
            //filters.add(new Filter(Collections.singletonList("status"), "2"));
            //filters.add(new Filter(Collections.singletonList("attachment"), "mail.png"));
            final QueryResponse queryResponse = getClient().execute(new QueryRequest(0, 10, queries, filters, "tasks"));

            assertNotNull(queryResponse);

            JSONObject j = (JSONObject) queryResponse.getData();
            System.err.println("RESULTS: " + j.getJSONArray("results").length());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }*/
}
