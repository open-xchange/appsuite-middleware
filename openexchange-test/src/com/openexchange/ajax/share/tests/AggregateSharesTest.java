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

package com.openexchange.ajax.share.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link AggregateSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregateSharesTest extends ShareTest {

    private java.util.Map<AJAXClient, List<Integer>> clientsAndFolders;

    /**
     * Initializes a new {@link AggregateSharesTest}.
     *
     * @param name The test name
     */
    public AggregateSharesTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clientsAndFolders = new HashMap<AJAXClient, List<Integer>>();
        clientsAndFolders.put(client, new ArrayList<Integer>());
        clientsAndFolders.put(new AJAXClient(User.User2), new ArrayList<Integer>());
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != clientsAndFolders) {
            for (Map.Entry<AJAXClient, List<Integer>> entry : clientsAndFolders.entrySet()) {
                deleteFoldersSilently(entry.getKey(), entry.getValue());
                entry.getKey().logout();
            }
        }
        super.tearDown();
    }

    private AJAXClient randomClient() {
        AJAXClient[] ajaxClients = clientsAndFolders.keySet().toArray(new AJAXClient[clientsAndFolders.size()]);
        return ajaxClients[random.nextInt(ajaxClients.length)];
    }

    public void testAggregateSharesRandomly() throws Exception {
        testAggregateShares(randomFolderAPI(), randomClient(), randomModule(), randomClient(), randomModule());
    }

    public void noTestAggregateSharesExtensively() throws Exception {
        AJAXClient[] ajaxClients = clientsAndFolders.keySet().toArray(new AJAXClient[clientsAndFolders.size()]);
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (AJAXClient client1 : ajaxClients) {
                for (int module1 : TESTED_MODULES) {
                    for (AJAXClient client2 : ajaxClients) {
                        for (int module2 : TESTED_MODULES) {
                            testAggregateShares(api, client1, module1, client2, module2);
                        }
                    }
                }
            }
        }
    }

    public void testRemoveAggregateSharesRandomly() throws Exception {
        testRemoveAggregateShares(randomFolderAPI(), randomClient(), randomModule(), randomClient(), randomModule());
    }

    public void noTestRemoveAggregateSharesExtensively() throws Exception {
        AJAXClient[] ajaxClients = clientsAndFolders.keySet().toArray(new AJAXClient[clientsAndFolders.size()]);
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (AJAXClient client1 : ajaxClients) {
                for (int module1 : TESTED_MODULES) {
                    for (AJAXClient client2 : ajaxClients) {
                        for (int module2 : TESTED_MODULES) {
                            testRemoveAggregateShares(api, client1, module1, client2, module2);
                        }
                    }
                }
            }
        }
    }

    private void testAggregateShares(EnumAPI api, AJAXClient client1, int module1, AJAXClient client2, int module2) throws Exception {
        /*
         * prepare guest permission
         */
        String name = randomUID();
        OCLGuestPermission guestPermission = createNamedGuestPermission(name + "@example.com", name, "secret");
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        FolderObject folderA = Create.createPrivateFolder(randomUID(), module1, client1.getValues().getUserId(), guestPermission);
        folderA.setParentFolderID(getDefaultFolder(client1, module1));
        InsertResponse insertResponse1 = client1.execute(new InsertRequest(api, folderA));
        insertResponse1.fillObject(folderA);
        clientsAndFolders.get(client1).add(Integer.valueOf(folderA.getObjectID()));
        GetResponse getResponse1 = client1.execute(new GetRequest(api, folderA.getObjectID()));
        folderA = getResponse1.getFolder();
        folderA.setLastModified(getResponse1.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionA = null;
        for (OCLPermission permission : folderA.getPermissions()) {
            if (permission.getEntity() != client1.getValues().getUserId()) {
                matchingPermissionA = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionA);
        checkPermissions(guestPermission, matchingPermissionA);
        /*
         * discover & check share
         */
        ParsedShare shareA = discoverShare(client1, folderA.getObjectID(), matchingPermissionA.getEntity());
        checkShare(guestPermission, shareA);
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = Create.createPrivateFolder(randomUID(), module2, client2.getValues().getUserId(), guestPermission);
        folderB.setParentFolderID(getDefaultFolder(client2, module2));
        InsertResponse insertResponse2 = client2.execute(new InsertRequest(api, folderB));
        insertResponse2.fillObject(folderB);
        clientsAndFolders.get(client2).add(Integer.valueOf(folderB.getObjectID()));
        GetResponse getResponse = client2.execute(new GetRequest(api, folderB.getObjectID()));
        folderB = getResponse.getFolder();
        folderB.setLastModified(getResponse.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionB = null;
        for (OCLPermission permission : folderB.getPermissions()) {
            if (permission.getEntity() != client2.getValues().getUserId()) {
                matchingPermissionB = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionB);
        checkPermissions(guestPermission, matchingPermissionB);
        /*
         * discover & check share
         */
        ParsedShare shareB = discoverShare(client2, folderB.getObjectID(), matchingPermissionB.getEntity());
        checkShare(guestPermission, shareB);
        /*
         * check permission entities
         */
        assertEquals("Permission entities differ", matchingPermissionA.getEntity(), matchingPermissionB.getEntity());
        /*
         * check access to shares via link to folder A
         */
        GuestClient guestClientA = resolveShare(shareA, guestPermission.getPassword());
        guestClientA.checkModuleAvailable(shareA.getTarget().getModule());
        guestClientA.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientA.checkFolderAccessible(shareA.getTarget().getFolder(), guestPermission);
        guestClientA.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
        /*
         * check access to shares via link to folder B
         */
        GuestClient guestClientB = resolveShare(shareB, guestPermission.getPassword());
        guestClientB.checkModuleAvailable(shareA.getTarget().getModule());
        guestClientB.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientB.checkFolderAccessible(shareA.getTarget().getFolder(), guestPermission);
        guestClientB.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
    }

    private void testRemoveAggregateShares(EnumAPI api, AJAXClient client1, int module1, AJAXClient client2, int module2) throws Exception {
        /*
         * prepare guest permission
         */
        String name = randomUID();
        OCLGuestPermission guestPermission = createNamedGuestPermission(name + "@example.com", name, "secret");
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        FolderObject folderA = Create.createPrivateFolder(randomUID(), module1, client1.getValues().getUserId(), guestPermission);
        folderA.setParentFolderID(getDefaultFolder(client1, module1));
        InsertResponse insertResponse1 = client1.execute(new InsertRequest(api, folderA));
        insertResponse1.fillObject(folderA);
        clientsAndFolders.get(client1).add(Integer.valueOf(folderA.getObjectID()));
        GetResponse getResponse1 = client1.execute(new GetRequest(api, folderA.getObjectID()));
        folderA = getResponse1.getFolder();
        folderA.setLastModified(getResponse1.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionA = null;
        for (OCLPermission permission : folderA.getPermissions()) {
            if (permission.getEntity() != client1.getValues().getUserId()) {
                matchingPermissionA = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionA);
        checkPermissions(guestPermission, matchingPermissionA);
        /*
         * discover share
         */
        ParsedShare shareA = discoverShare(client1, folderA.getObjectID(), matchingPermissionA.getEntity());
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = Create.createPrivateFolder(randomUID(), module2, client2.getValues().getUserId(), guestPermission);
        folderB.setParentFolderID(getDefaultFolder(client2, module2));
        InsertResponse insertResponse2 = client2.execute(new InsertRequest(api, folderB));
        insertResponse2.fillObject(folderB);
        clientsAndFolders.get(client2).add(Integer.valueOf(folderB.getObjectID()));
        GetResponse getResponse2 = client2.execute(new GetRequest(api, folderB.getObjectID()));
        folderB = getResponse2.getFolder();
        folderB.setLastModified(getResponse2.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionB = null;
        for (OCLPermission permission : folderB.getPermissions()) {
            if (permission.getEntity() != client2.getValues().getUserId()) {
                matchingPermissionB = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionB);
        checkPermissions(guestPermission, matchingPermissionB);
        /*
         * discover share
         */
        ParsedShare shareB = discoverShare(client2, folderB.getObjectID(), matchingPermissionB.getEntity());
        /*
         * check permission entities
         */
        assertEquals("Permission entities differ", matchingPermissionA.getEntity(), matchingPermissionB.getEntity());
        /*
         * check access to shares via link to folder A
         */
        GuestClient guestClientA = resolveShare(shareA, guestPermission.getPassword());
        guestClientA.checkModuleAvailable(shareA.getTarget().getModule());
        guestClientA.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientA.checkFolderAccessible(shareA.getTarget().getFolder(), guestPermission);
        guestClientA.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
        /*
         * check access to shares via link to folder B
         */
        GuestClient guestClientB = resolveShare(shareB, guestPermission.getPassword());
        guestClientB.checkModuleAvailable(shareA.getTarget().getModule());
        guestClientB.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientB.checkFolderAccessible(shareA.getTarget().getFolder(), guestPermission);
        guestClientB.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
        /*
         * update folder A, revoke guest permissions
         */
        folderA.getPermissions().remove(matchingPermissionA);
        insertResponse1 = client1.execute(new UpdateRequest(api, folderA));
        insertResponse1.fillObject(folderA);
        clientsAndFolders.get(client1).add(Integer.valueOf(folderA.getObjectID()));
        getResponse1 = client1.execute(new GetRequest(api, folderA.getObjectID()));
        folderA = getResponse1.getFolder();
        folderA.setLastModified(getResponse1.getTimestamp());
        /*
         * check permissions
         */
        for (OCLPermission permission : folderA.getPermissions()) {
            assertTrue("Guest permission still present", permission.getEntity() != matchingPermissionA.getEntity());
        }
        /*
         * check access with previous guest sessions
         */
        guestClientA.checkSessionAlive(false);
        // FIXME: capabilities are cached and not invalidated
        // guestClientA.checkModuleNotAvailable(module1);
        guestClientA.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientA.checkFolderNotAccessible(shareA.getTarget().getFolder());
        guestClientA.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
        guestClientB.checkSessionAlive(false);
        // FIXME: capabilities are cached and not invalidated
        // guestClientB.checkModuleNotAvailable(module1);
        guestClientB.checkModuleAvailable(shareB.getTarget().getModule());
        guestClientB.checkFolderNotAccessible(shareA.getTarget().getFolder());
        guestClientB.checkFolderAccessible(shareB.getTarget().getFolder(), guestPermission);
        /*
         * check if share link to folder A still accessible
         */
        ResolveShareResponse shareResolveResponse = new GuestClient(shareA, guestPermission.getPassword(), false).getShareResolveResponse();
        assertEquals("Status code wrong", HttpServletResponse.SC_NOT_FOUND, shareResolveResponse.getStatusCode());
        /*
         * check if share link to folder A still accessible
         */
        shareResolveResponse = new GuestClient(shareB, guestPermission.getPassword()).getShareResolveResponse();
        assertEquals("Status code wrong", HttpServletResponse.SC_MOVED_TEMPORARILY, shareResolveResponse.getStatusCode());
    }

}
