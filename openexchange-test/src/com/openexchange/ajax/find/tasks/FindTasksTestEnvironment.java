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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link FindTasksTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksTestEnvironment {
    
    private static final FindTasksTestEnvironment INSTANCE = new FindTasksTestEnvironment();
    
    /** UserA's private test folder */
    private FolderObject userAprivateTestFolder;
    
    /** UserA's public test folder */ 
    private FolderObject userApublicTestFolder;
    
    /** UserB's private shared folder, Read-Only for UserA */
    private FolderObject userBsharedTestFolderRO;
    
    /** UserB's private shared folder, Read-Write for UserA */
    private FolderObject userBsharedTestFolderRW;
    
    /** UserB's private folder, No Access for UserA */
    private FolderObject userBprivateTestFolder;
    
    /** UserB's public folder */
    private FolderObject userBpublicTestFolder; 
    
    private UserValues userA;
    
    private UserValues userB;
    
    private AJAXClient clientA;
    
    private AJAXClient clientB;
    
    /** List of Lists with filters */
    public List<List<Filter>> lolFilters = new ArrayList<List<Filter>>();
    
    private enum FolderType {PRIVATE, PUBLIC, SHARED};
    
    private enum Status {NOT_STARTED, IN_PROGRESS, DONE, WAITING, DEFERRED};
    
    private boolean cleanup = true;

    /**
     * Initializes a new {@link FindTasksTestEnvironment}.
     */
    public FindTasksTestEnvironment() {
        super();
    }
    
    /**
     * Get the instance of the test environment
     * @return the instance of the test environment
     */
    public static FindTasksTestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the test environment for Tasks
     * @return true if the environment was successfully initialized; false otherwise
     * @throws Exception
     */
    public boolean init() {
        try {
            initUsers();
            createFolderStructure();
            createAndInsertTasks();
            createFilters();
            logout();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initialize the users
     */
    private final void initUsers() throws Exception {
        clientA = new AJAXClient(User.User1);
        userA = clientA.getValues();
        
        clientB = new AJAXClient(User.User2);
        userB = clientB.getValues();
    }
    
    /**
     * Logout
     * @throws Exception
     */
    private final void logout() throws Exception {
        if (clientA != null)
            clientA.logout();
        if (clientB != null)
            clientB.logout();
    }
    
    /**
     * Create the test folder structure
     * TODO: handle duplicate folder creation
     * @throws IOException 
     * @throws OXException 
     * @throws Exception
     */
    private final void createFolderStructure() throws OXException, IOException, Exception {
        InsertRequest insertRequestReq;
        InsertResponse insertResponseResp;
        
        //Get current structure
        Map<String, FolderObject> foldersA = getFolderStructure(clientA, userA.getPrivateTaskFolder());
        Map<String, FolderObject> foldersB = getFolderStructure(clientB, userB.getPrivateTaskFolder());
        
        try {
            //create private test folder
            userAprivateTestFolder = Create.createPrivateFolder("UserA - findAPIPrivateTaskFolder", FolderObject.TASK, userA.getUserId());
            userAprivateTestFolder.setParentFolderID(userA.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userAprivateTestFolder, false);
            insertResponseResp = clientA.execute(insertRequestReq);
            insertResponseResp.fillObject(userAprivateTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userAprivateTestFolder.getObjectID() == 0) {
            userAprivateTestFolder = foldersA.get("UserA - findAPIPrivateTaskFolder");
        }
        
        //create public test folder
        try {
            userApublicTestFolder = Create.createPublicFolder(clientA, "UserA - findAPIPublicTaskFolder", FolderObject.TASK, false);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userApublicTestFolder.getObjectID() == 0) {
            userApublicTestFolder = foldersA.get("UserA - findAPIPublicTaskFolder");
        }
        
        try {
            //create shared folder, read-only
            userBsharedTestFolderRO = Create.createPrivateFolder("UserB - findAPIPrivateSharedTaskFolder - RO", FolderObject.TASK, userB.getUserId());
            userBsharedTestFolderRO.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRO, false);
            insertResponseResp = clientB.execute(insertRequestReq);
            insertResponseResp.fillObject(userBsharedTestFolderRO);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userBsharedTestFolderRO.getObjectID() == 0) {
            userBsharedTestFolderRO = foldersB.get("UserB - findAPIPrivateSharedTaskFolder - RO");
        }
        
        try {
            //share read only folder to userA
            FolderTools.shareFolder(clientB, EnumAPI.OX_NEW, userBsharedTestFolderRO.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, 
                                                                                                                       OCLPermission.READ_ALL_OBJECTS, 
                                                                                                                       OCLPermission.NO_PERMISSIONS, 
                                                                                                                       OCLPermission.NO_PERMISSIONS);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            //create shared folder, read/write
            userBsharedTestFolderRW = Create.createPrivateFolder("UserB - findAPIPrivateSharedTaskFolder - RW", FolderObject.TASK, userB.getUserId());
            userBsharedTestFolderRW.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRW, false);
            insertResponseResp = clientB.execute(insertRequestReq);
            insertResponseResp.fillObject(userBsharedTestFolderRW);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userBsharedTestFolderRW.getObjectID() == 0) {
            userBsharedTestFolderRW = foldersB.get("UserB - findAPIPrivateSharedTaskFolder - RW");
        }
        
        try {
            //share read/write folder to userA
            FolderTools.shareFolder(clientB, EnumAPI.OX_NEW, userBsharedTestFolderRW.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, 
                                                                                                                       OCLPermission.WRITE_ALL_OBJECTS, 
                                                                                                                       OCLPermission.WRITE_ALL_OBJECTS, 
                                                                                                                       OCLPermission.WRITE_ALL_OBJECTS);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            //create userB's private folder
            userBprivateTestFolder = Create.createPrivateFolder("UserB - findAPIPrivateTaskFolder - NA", FolderObject.TASK, userB.getUserId());
            userBprivateTestFolder.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBprivateTestFolder, false);
            insertResponseResp = clientB.execute(insertRequestReq);
            insertResponseResp.fillObject(userBprivateTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userBprivateTestFolder.getObjectID() == 0) {
            userBprivateTestFolder = foldersB.get("UserB - findAPIPrivateTaskFolder - NA");
        }
        
        try {
            //create public test folder for user B
            userBpublicTestFolder = Create.createPublicFolder(clientB, "UserB - findAPIPublicTaskFolder", FolderObject.TASK, false);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (userBpublicTestFolder.getObjectID() == 0) {
            userBpublicTestFolder = foldersB.get("UserB - findAPIPublicTaskFolder");
        }
    }
    
    /**
     * Get the folder structure of the specified folder 
     * @param client
     * @param folderID
     * @return
     * @throws JSONException 
     * @throws IOException 
     * @throws OXException 
     */
    private final Map<String, FolderObject> getFolderStructure(AJAXClient client, int folderID) throws Exception {
        Map<String, FolderObject> folders = new HashMap<String, FolderObject>();
        VisibleFoldersRequest lr = new VisibleFoldersRequest(EnumAPI.OX_NEW, "tasks");
        VisibleFoldersResponse response = client.execute(lr);
        Iterator<FolderObject> it = response.getPrivateFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }
        
        it = response.getPublicFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }
        
        it = response.getSharedFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }

        return folders;
    }
    
    /**
     * Create and insert tasks to the previously created folder structure
     * @throws Exception 
     */
    private final void createAndInsertTasks() throws Exception {
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
    private final void insertTask(AJAXClient client, FolderType ft, Status status, int folder) throws Exception {
        Task t = com.openexchange.groupware.tasks.Create.createWithDefaults("Find me, I am in " + ft + " - Hint User " + client.getValues().getDefaultAddress(), 
                      "User " + client.getValues().getDefaultAddress()+ "'s private task in his " + ft + " folder and " + status, status.ordinal() + 1, folder);
        client.execute(new com.openexchange.ajax.task.actions.InsertRequest(t, client.getValues().getTimeZone()));
    }
    
    /**
     * Create the filters
     * @throws Exception
     */
    private final void createFilters() throws Exception {
        //create single filters
        //participants
        List<Filter> l = new ArrayList<Filter>(2);
        l.add(createFilter("participant", Integer.toString(userA.getUserId()))); 
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
     * Create a single filter
     * @param name
     * @param value
     * @return
     */
    private final Filter createFilter(String name, String value) {
        return new Filter(Collections.singletonList(name), value);
    }

    
    /**
     * Cleanup
     * @throws Exception 
     */
    public void cleanup() throws Exception {
        if (cleanup) {
            if (clientA == null || clientB == null)
                initUsers();
            clientA.execute(new DeleteRequest(EnumAPI.OX_NEW, userAprivateTestFolder, userApublicTestFolder));
            clientB.execute(new DeleteRequest(EnumAPI.OX_NEW, userBsharedTestFolderRO, userBsharedTestFolderRW, userBprivateTestFolder, userBpublicTestFolder));
            
            logout();
        }
    }
    
    /**
     * Get the List of Lists Filters
     * @return the List of Lists Filters
     */
    public List<List<Filter>> getLolFilters() {
        return lolFilters;
    }

}
