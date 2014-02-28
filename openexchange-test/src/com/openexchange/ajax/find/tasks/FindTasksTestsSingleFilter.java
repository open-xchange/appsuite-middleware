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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;


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
public class FindTasksTestsSingleFilter {
    
    /** UserA's private test folder */
    private static FolderObject userAprivateTestFolder;
    
    /** UserA's public test folder */ 
    private static FolderObject userApublicTestFolder;
    
    /** UserB's private shared folder, Read-Only for UserA */
    private static FolderObject userBsharedTestFolderRO;
    
    /** UserB's private shared folder, Read-Write for UserA */
    private static FolderObject userBsharedTestFolderRW;
    
    /** UserB's private folder, No Access for UserA */
    private static FolderObject userBprivateTestFolder;
    
    /** UserB's public folder */
    private static FolderObject userBpublicTestFolder; 
    
    private static UserValues userA;
    
    private static UserValues userB;
    
    private static AJAXClient clientA;
    
    private static AJAXClient clientB;
    
    /** List of Lists with filters */
    private static List<List<Filter>> lolFilters = new ArrayList<List<Filter>>();
    
    private static enum FolderType {PRIVATE, PUBLIC, SHARED};
    
    private static enum Status {NOT_STARTED, IN_PROGRESS, DONE, WAITING, DEFERRED};
    
    private static boolean cleanup = false;
    
    private AJAXClient client;
    
    /**
     * Initializes a new {@link FindTasksTests}.
     */
    public FindTasksTestsSingleFilter() {
        super();
    }
    
    /**
     * Setup the test case
     * @throws Exception 
     */
    @BeforeClass
    public static void init() throws Exception {
        initUsers();
        createFolderStructure();
        createAndInsertTasks();
        createFilters();
        logout();
    }
    
    /**
     * Login before each test
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        client = new AJAXClient(User.User1);
    }

    /**
     * Logout after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.logout();
        }
    }
    
    /**
     * Initialize the users
     */
    private static final void initUsers() throws Exception {
        clientA = new AJAXClient(User.User1);
        userA = clientA.getValues();
        
        clientB = new AJAXClient(User.User2);
        userB = clientB.getValues();
    }
    
    /**
     * Logout
     * @throws Exception
     */
    private static final void logout() throws Exception {
        if (clientA != null)
            clientA.logout();
        if (clientB != null)
            clientB.logout();
    }
    
    /**
     * Create the test folder structure
     * @throws Exception
     */
    private static final void createFolderStructure() throws Exception {
        //create private test folder
        userAprivateTestFolder = Create.createPrivateFolder("UserA - findAPIPrivateTaskFolder", FolderObject.TASK, userA.getUserId());
        userAprivateTestFolder.setParentFolderID(userA.getPrivateTaskFolder());
        InsertRequest insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userAprivateTestFolder);
        InsertResponse insertResponseResp = clientA.execute(insertRequestReq);
        insertResponseResp.fillObject(userAprivateTestFolder);
        
        //create public test folder
        userApublicTestFolder = Create.createPublicFolder(clientA, "UserA - findAPIPublicTaskFolder", FolderObject.TASK);
        
        //create shared folder, read-only
        userBsharedTestFolderRO = Create.createPrivateFolder("UserB - findAPIPrivateSharedTaskFolder - RO", FolderObject.TASK, userB.getUserId());
        userBsharedTestFolderRO.setParentFolderID(userB.getPrivateTaskFolder());
        insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRO);
        insertResponseResp = clientB.execute(insertRequestReq);
        insertResponseResp.fillObject(userBsharedTestFolderRO);
        
        //share read only folder to userA
        FolderTools.shareFolder(clientB, EnumAPI.OX_NEW, userBsharedTestFolderRO.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, 
                                                                                                                   OCLPermission.READ_ALL_OBJECTS, 
                                                                                                                   OCLPermission.NO_PERMISSIONS, 
                                                                                                                   OCLPermission.NO_PERMISSIONS);
        
        //create shared folder, read/write
        userBsharedTestFolderRW = Create.createPrivateFolder("UserB - findAPIPrivateSharedTaskFolder - RW", FolderObject.TASK, userB.getUserId());
        userBsharedTestFolderRW.setParentFolderID(userB.getPrivateTaskFolder());
        insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRW);
        insertResponseResp = clientB.execute(insertRequestReq);
        insertResponseResp.fillObject(userBsharedTestFolderRW);
        
        //share read/write folder to userA
        FolderTools.shareFolder(clientB, EnumAPI.OX_NEW, userBsharedTestFolderRW.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, 
                                                                                                                   OCLPermission.WRITE_ALL_OBJECTS, 
                                                                                                                   OCLPermission.WRITE_ALL_OBJECTS, 
                                                                                                                   OCLPermission.WRITE_ALL_OBJECTS);
        
        //create userB's private folder
        userBprivateTestFolder = Create.createPrivateFolder("UserB - findAPIPrivateTaskFolder - NA", FolderObject.TASK, userB.getUserId());
        userBprivateTestFolder.setParentFolderID(userB.getPrivateTaskFolder());
        insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBprivateTestFolder);
        insertResponseResp = clientB.execute(insertRequestReq);
        insertResponseResp.fillObject(userBprivateTestFolder);
        
        //create public test folder for user B
        userBpublicTestFolder = Create.createPublicFolder(clientB, "UserB - findAPIPublicTaskFolder", FolderObject.TASK);
    }
    
    /**
     * Create and insert tasks to the previously created folder structure
     * @throws Exception 
     */
    private static final void createAndInsertTasks() throws Exception {
        //insert some tasks
        insertTask(clientA, FolderType.PRIVATE, Status.NOT_STARTED, userAprivateTestFolder.getObjectID());
        insertTask(clientA, FolderType.PRIVATE, Status.DEFERRED, userAprivateTestFolder.getObjectID());
        insertTask(clientA, FolderType.PRIVATE, Status.DONE, userApublicTestFolder.getObjectID());
        insertTask(clientA, FolderType.PUBLIC, Status.DONE, userApublicTestFolder.getObjectID());
    }
    
    /**
     * Helper method to insert tasks
     * @param client the AJAXClient
     * @param ft FolderType 
     * @param status Task's status
     * @param folder parent folder
     * @throws Exception
     */
    private static final void insertTask(AJAXClient client, FolderType ft, Status status, int folder) throws Exception {
        Task t = com.openexchange.groupware.tasks.Create.createWithDefaults("Find me, I am in " + ft + " - Hint User " + client.getValues().getDefaultAddress(), 
                      "User " + client.getValues().getDefaultAddress()+ "'s private task in his " + ft + " folder and " + status, status.ordinal() + 1, folder);
        client.execute(new com.openexchange.ajax.task.actions.InsertRequest(t, client.getValues().getTimeZone()));
    }
    
    /**
     * Create the filters
     * @throws Exception
     */
    private static final void createFilters() throws Exception {
        //create single filters
        //participants
        List<Filter> l = new ArrayList<Filter>(2);
        l.add(createFilter("participant", Integer.toString(userB.getUserId()))); //internal
        l.add(createFilter("participant", "foo@bar.org"));                       //external
        lolFilters.add(l);
        
        //status
        l = new ArrayList<Filter>(5);
        l.add(createFilter("status", Integer.toString(Task.NOT_STARTED)));
        l.add(createFilter("status", Integer.toString(Task.IN_PROGRESS)));
        l.add(createFilter("status", Integer.toString(Task.DONE)));
        l.add(createFilter("status", Integer.toString(Task.WAITING)));
        l.add(createFilter("status", Integer.toString(Task.DEFERRED)));
        lolFilters.add(l);
        
        //folder type
        l = new ArrayList<Filter>(3);
        l.add(createFilter("folder_type", Integer.toString(FolderObject.PRIVATE)));
        l.add(createFilter("folder_type", Integer.toString(FolderObject.PUBLIC)));
        l.add(createFilter("folder_type", Integer.toString(FolderObject.SHARED)));
        lolFilters.add(l);
        
        //type
        l = new ArrayList<Filter>(2);
        l.add(createFilter("type", Integer.toString(0))); //single
        l.add(createFilter("type", Integer.toString(1))); //series
        lolFilters.add(l);
    }
    
    /**
     * Cleanup
     * @throws Exception 
     */
    @AfterClass
    public static void cleanup() throws Exception {
        if (cleanup) {
            if (clientA != null)
                clientA.execute(new DeleteRequest(EnumAPI.OX_NEW, userAprivateTestFolder, userApublicTestFolder));
            
            if (clientB != null) {
                clientB.execute(new DeleteRequest(EnumAPI.OX_NEW, userBsharedTestFolderRO, userBsharedTestFolderRW, userBprivateTestFolder, userBpublicTestFolder));
                clientB.logout();
            }
        }
    }
    
    /**
     * Create a single filter
     * @param name
     * @param value
     * @return
     */
    private static final Filter createFilter(String name, String value) {
        return new Filter(Collections.singletonList(name), value);
    }
    
    /**
     * Get all relevant filters for the given combination
     * @param combination as char array
     * @return all relevant filters for that combination
     */
    private static final List<Filter> getRelevantFilters(char[] combination) {
        List<Filter> filters = new ArrayList<Filter>();
        for (int i = 0; i < combination.length; i++) {
            if (combination[i] == 1)
                filters.addAll(lolFilters.get(i));
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
     */
    @Test
    public void testWithSimpleQuery() {
        try {

            List<String> queries = Collections.singletonList("task");
            List<Filter> filters = Collections.emptyList();
            final QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, filters, "tasks"));

            assertNotNull(queryResponse);

            //assert the response
            //JSONArray results  = getResults(queryResponse);
            
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Test filter combination 1, i.e. with Participant (internal)
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testWithInternalParticipant() throws OXException, IOException, JSONException {
        List<Filter> f = getRelevantFilters(Integer.toBinaryString(1).toCharArray());
        List<String> queries = Collections.singletonList("task");
        final QueryResponse queryResponse = client.execute(new QueryRequest(0, 10, queries, Collections.singletonList(f.get(0)), "tasks"));
        //assert the response
        //JSONArray results  = getResults(queryResponse);
    }
    
    /**
     * Test filter combination 1, i.e. with Participant (external)
     */
    @Test
    public void testWithExternalParticipant() {
        
    }
    
    /**
     * Test filter combination 1, i.e. with mixed Participants (both internal and external)
     */
    @Test
    public void testWithMixedParticipants() {
        
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
