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

package com.openexchange.ajax.folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.ClearRequest;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PermissionsCascadeTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PermissionsCascadeTest extends AbstractAJAXSession {

    private List<FolderObject> testFolders = new ArrayList<FolderObject>();

    private FolderObject rootFolder;

    private static final boolean CLEANUP = true;

    /**
     * Initializes a new {@link PermissionsCascadeTest}.
     * 
     * @param name
     */
    public PermissionsCascadeTest(String name) {
        super(name);
    }

    @Override
    public void tearDown() throws Exception {
        if (CLEANUP) {
            client.execute(new DeleteRequest(EnumAPI.OUTLOOK, rootFolder.getObjectID(), rootFolder.getLastModified()));
            client.execute(new ClearRequest(EnumAPI.OUTLOOK, client.getValues().getInfostoreTrashFolder()));
        }

        super.tearDown();
    }

    /**
     * Test simple permissions cascade.
     * 
     * Creates a simple folder tree, assigns permissions and asserts
     * 
     * @throws Exception
     */
    public void testCascadePermissionsInChildrenFolders() throws Exception {
        // Create a simple folder tree
        rootFolder = createSimpleTree("testCascadePermissionsInChildrenFolders", 5);
        assertCascadePermissions();
    }

    /**
     * Test cascading in sibling folders
     * 
     * @throws Exception
     */
    public void testCasccadePermissionsInSiblingFolders() throws Exception {
        rootFolder = createRandomTree("testCasccadePermissionsInSiblingFolders", 20);
        assertCascadePermissions();
    }

    /**
     * Assert cascaded permissions in tree, starting by root node
     * 
     * @throws Exception
     */
    private void assertCascadePermissions() throws Exception {
        // Fetch that folder
        GetResponse response = client.execute(new GetRequest(EnumAPI.OUTLOOK, rootFolder.getObjectID()));
        JSONObject data = (JSONObject) response.getData();
        long timestamp = data.getLong("last_modified");

        // User to share the folder with
        AJAXClient client2 = new AJAXClient(User.User2);

        // Apply permissions 
        rootFolder.addPermission(Create.ocl(
            client2.getValues().getUserId(),
            false,
            false,
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS));
        rootFolder.setLastModified(new Date(timestamp));
        client.execute(new UpdateRequest(EnumAPI.OUTLOOK, rootFolder).setCascadePermissions(true));

        // Fetch all folders of the tree and make sure that the permissions are cascaded
        for (int i = 2; i < testFolders.size(); i++) {
            FolderObject fo = testFolders.get(i);
            response = client.execute(new GetRequest(EnumAPI.OUTLOOK, fo.getObjectID(), new int[] { 300, 306 }));
            List<OCLPermission> permissions = response.getFolder().getPermissions();
            boolean found = false;
            for (int p = 0; !found && p < permissions.size(); p++) {
                found = permissions.get(p).getEntity() == client2.getValues().getUserId();
            }
            assertTrue("Second user not found in permissions of folder '" + response.getFolder().getFolderName() + "'", found);
        }
    }

    /**
     * Test roll-back functionality.
     * 
     * Create a random tree and find a folder in the tree (say (A)) that has at least one sub-folder.
     * Select that sub-folder (A) and create a new folder (B) under that sub-folder (A) with a second client
     * Remove the permissions from the new folder (B) for client 1 and make client 2 the administrator
     * for that folder.
     * 
     * Apply new permissions to folder (A) and assert that the permissions are not cascaded.
     * Then apply the new permissions again and ignore the warnings. Assert that the permissions are cascaded.
     * 
     * @throws Exception
     */
    public void testCascadePermissionsInTreeRollbackAndThenIgnore() throws Exception {
        rootFolder = createRandomTree("testCascadePermissionsInTreeRollback", 20);

        // Pick one folder that has at least one sub-folder
        int rootNodeIdOfSubTree = -1;
        for (int f = 1; f < testFolders.size(); f++) {
            GetResponse response = client.execute(new GetRequest(EnumAPI.OUTLOOK, testFolders.get(f).getObjectID(), new int[] { 304 }));
            if (response.getFolder().hasSubfolders()) {
                rootNodeIdOfSubTree = response.getFolder().getObjectID();
                break;
            }
        }
        // If none found, then use the root folder
        if (rootNodeIdOfSubTree < 0) {
            rootNodeIdOfSubTree = testFolders.get(0).getObjectID();
        }

        // Fetch all of its sub-folders.
        List<FolderObject> tree = new ArrayList<FolderObject>();
        fetchAllSubfolders(rootNodeIdOfSubTree, tree);

        // Assert that there is a tree
        assertTrue("No tree", tree.size() > 0);

        // Fetch information about the leaf folder
        FolderObject leaf = tree.get(tree.size() - 1);
        GetResponse response = client.execute(new GetRequest(EnumAPI.OUTLOOK, leaf.getObjectID()));
        JSONObject data = (JSONObject) response.getData();
        long timestamp = data.getLong("last_modified");
        leaf.setPermissions(response.getFolder().getPermissions());

        // User to share the folder with
        AJAXClient client2 = new AJAXClient(User.User2);

        // Make second user an admin
        leaf.addPermission(Create.ocl(
            client2.getValues().getUserId(),
            false,
            true,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION));
        leaf.setLastModified(new Date(timestamp));
        client.execute(new UpdateRequest(EnumAPI.OUTLOOK, leaf));

        // Create a folder under leaf
        int lol = createFolder("Leaf", leaf.getObjectID(), client2).getObjectID();

        // Fetch permissions of the newly created folder
        response = client2.execute(new GetRequest(EnumAPI.OUTLOOK, lol, new int[] { 5, 306 }));
        FolderObject leafOfLeaf = response.getFolder();
        timestamp = ((JSONObject) response.getData()).getLong("last_modified");

        // Apply administrative permissions to the leaf of leaf folder.
        leafOfLeaf.removePermissions();
        leafOfLeaf.addPermission(Create.ocl(
            client2.getValues().getUserId(),
            false,
            true,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION));
        leafOfLeaf.setLastModified(new Date(timestamp));
        client2.execute(new UpdateRequest(EnumAPI.OUTLOOK, leafOfLeaf));

        // Try to apply permissions to the rootNodeOfSubTree and cascade (should fail and roll-back)
        response = client.execute(new GetRequest(EnumAPI.OUTLOOK, rootNodeIdOfSubTree, new int[] { 5, 306 }));
        FolderObject rootNode = response.getFolder();
        timestamp = ((JSONObject) response.getData()).getLong("last_modified");

        AJAXClient client3 = new AJAXClient(User.User3);
        rootNode.addPermission(Create.ocl(
            client3.getValues().getUserId(),
            false,
            false,
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS));
        rootNode.setLastModified(new Date(timestamp));
        UpdateRequest setCascadePermissions = new UpdateRequest(EnumAPI.OUTLOOK, rootNode, false).setCascadePermissions(true);
        client.execute(setCascadePermissions);

        int owner = client.getValues().getUserId();
        int guest = client2.getValues().getUserId();
        int doe = client3.getValues().getUserId();

        // Assert permissions
        assertPermissions(rootNode, new int[] { owner, doe }, new int[] { guest }, client);
        assertPermissions(leaf, new int[] { owner, guest }, new int[] { doe }, client);
        assertPermissions(leafOfLeaf, new int[] { guest }, new int[] { owner, doe }, client2);

        // Ignore warnings and apply permissions to the rootNodeOfSubTree again (should succeed)
        setCascadePermissions.setIgnoreWarnings(true);
        client.execute(setCascadePermissions);

        // Assert permissions
        assertPermissions(rootNode, new int[] { owner }, new int[] { guest, doe }, client);
        assertPermissions(leaf, new int[] { owner, doe }, new int[] { guest }, client);
        assertPermissions(leafOfLeaf, new int[] { guest }, new int[] { owner, doe }, client2);
    }

    /**
     * Assert permissions
     * 
     * @param folderObject The folder objects
     * @param includedUsers The users that should be included in the permission bits
     * @param excludedUsers The users that should be excluded from the permission bits
     * @param client The client
     * @throws Exception
     */
    private void assertPermissions(FolderObject folderObject, int[] includedUsers, int[] excludedUsers, AJAXClient client) throws Exception {
        final GetResponse getResponse = client.execute(new GetRequest(EnumAPI.OUTLOOK, folderObject.getObjectID()));
        final List<OCLPermission> permissions = getResponse.getFolder().getPermissions();

        final int pSize = permissions.size();
        assertEquals("Unexpected number of permissions for folder '" + folderObject.getObjectID() + "': ", includedUsers.length, pSize);

        assertUserInPermissions(includedUsers, permissions, folderObject.getObjectID(), true);
        assertUserInPermissions(excludedUsers, permissions, folderObject.getObjectID(), false);
    }

    /**
     * Assert users in permissions
     * 
     * @param userIds The user identifiers
     * @param permissions The permission bits
     * @param folderId The folder identifier
     * @param isContained true to assert if the users are included; false to assert if users are excluded
     */
    private void assertUserInPermissions(int[] userIds, List<OCLPermission> permissions, int folderId, boolean isContained) {
        boolean found = false;
        for (Integer userId : userIds) {
            for (int i = 0; !found && i < permissions.size(); i++) {
                found = permissions.get(i).getEntity() == userId;
            }
            assertEquals("User " + ((!isContained) ? "not" : "") + " found in permissions for folder '" + folderId + "'", isContained, found);
        }
    }

    /**
     * Fetch all sub-folders of the specified folder
     * 
     * @param folderId The folder identifier
     * @param tree The tree
     * @throws Exception
     */
    private void fetchAllSubfolders(int folderId, List<FolderObject> tree) throws Exception {
        ListResponse listResponse = client.execute(new ListRequest(EnumAPI.OUTLOOK, Integer.toString(folderId), new int[] { 1, 304, 306 }, false));
        Iterator<FolderObject> iterator = listResponse.getFolder();
        while (iterator.hasNext()) {
            FolderObject fo = iterator.next();
            tree.add(fo);
            if (fo.hasSubfolders()) {
                fetchAllSubfolders(fo.getObjectID(), tree);
            }
        }
    }

    /**
     * Create a folder and add it to the delete list for later cleanup (Helper method)
     * 
     * @param folderName The folder name
     * @param parent The parent
     * @return The folder identifier
     * @throws Exception
     */
    private FolderObject createFolder(String folderName, int parent) throws Exception {
        return createFolder(folderName, parent, client);
    }

    /**
     * Create a folder and add it to the delete list for later cleanup
     * 
     * @param folderName The folder name
     * @param parent The parent
     * @param client The client
     * @return The folder identifier
     * @throws Exception
     */
    private FolderObject createFolder(String folderName, int parent, AJAXClient client) throws Exception {
        FolderObject folder = Create.createPrivateFolder(folderName, FolderObject.INFOSTORE, client.getValues().getUserId());
        folder.setParentFolderID(parent);
        InsertResponse response = client.execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
        //String data = (String) response.getData();
        response.fillObject(folder);
        testFolders.add(folder);
        return folder;
    }

    /**
     * Creates a simple folder tree
     * 
     * @param rootName The name of the root node
     * @throws Exception
     */
    private FolderObject createSimpleTree(String rootName, int levels) throws Exception {
        FolderObject rootFolder = createFolder(rootName, client.getValues().getPrivateInfostoreFolder());
        int parent = rootFolder.getObjectID();
        for (int i = 1; i <= levels; i++) {
            parent = createFolder("Level " + i, parent).getObjectID();
        }
        return rootFolder;
    }

    /**
     * Creates a folder tree with folders placed randomly in the hierarchy.
     * 
     * @param rootName The name of the root node
     * @throws Exception
     */
    private FolderObject createRandomTree(String rootName, int folderCount) throws Exception {
        FolderObject rootFolder = createFolder(rootName, client.getValues().getPrivateInfostoreFolder());
        int parentId = rootFolder.getObjectID();
        for (int i = 0; i < folderCount; i++) {
            int r = (int) (Math.random() * (testFolders.size() - 1));
            parentId = testFolders.get(r).getObjectID();
            createFolder(UUID.randomUUID().toString(), parentId);
        }
        return rootFolder;
    }
}
