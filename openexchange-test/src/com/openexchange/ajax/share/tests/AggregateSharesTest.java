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
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.ajax.share.actions.StartSMTPRequest;
import com.openexchange.ajax.share.actions.StopSMTPRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link AggregateSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregateSharesTest extends ShareTest {

    private java.util.Map<AJAXClient, List<Integer>> clientsAndFolders;
    private AJAXClient client2;

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
        client2 = new AJAXClient(User.User2);
        client2.execute(new StartSMTPRequest());
        clientsAndFolders = new HashMap<AJAXClient, List<Integer>>();
        clientsAndFolders.put(client, new ArrayList<Integer>());
        clientsAndFolders.put(client2, new ArrayList<Integer>());
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != clientsAndFolders) {
            for (Map.Entry<AJAXClient, List<Integer>> entry : clientsAndFolders.entrySet()) {
                deleteFoldersSilently(entry.getKey(), entry.getValue());
                if (false == entry.getKey().equals(client)) {
                    entry.getKey().execute(new StopSMTPRequest());
                    entry.getKey().logout();
                }
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
//                            System.out.println("AggregateShares API: " + api + ", Client 1: " + client1.getValues().getUserId() + ", Module 1: " + module1 + ", Client 2: " + client2.getValues().getUserId() + ", Module 2: " + module2);
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
//                            System.out.println("RemoveAggregateShares API: " + api + ", Client 1: " + client1.getValues().getUserId() + ", Module 1: " + module1 + ", Client 2: " + client2.getValues().getUserId() + ", Module 2: " + module2);
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
        InsertRequest insertRequest1 = new InsertRequest(api, folderA);
        insertRequest1.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse1 = client1.execute(insertRequest1);
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
         * discover & check guest
         */
        ExtendedPermissionEntity guestA = discoverGuestEntity(client1, api, module1, folderA.getObjectID(), matchingPermissionA.getEntity());
        checkGuestPermission(guestPermission, guestA);
        String shareURLA = discoverShareURL(client1, guestA);
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = Create.createPrivateFolder(randomUID(), module2, client2.getValues().getUserId(), guestPermission);
        folderB.setParentFolderID(getDefaultFolder(client2, module2));
        InsertRequest insertRequest2 = new InsertRequest(api, folderB);
        insertRequest2.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse2 = client2.execute(insertRequest2);
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
         * discover & check guest
         */
        ExtendedPermissionEntity guestB = discoverGuestEntity(client2, api, module2, folderB.getObjectID(), matchingPermissionB.getEntity());
        checkGuestPermission(guestPermission, guestB);
        String shareURLB = discoverShareURL(client2, guestB);
        /*
         * check permission entities
         */
        assertEquals("Permission entities differ", matchingPermissionA.getEntity(), matchingPermissionB.getEntity());
        /*
         * check access to shares via link to folder A
         */
        GuestClient guestClientA = resolveShare(shareURLA, guestPermission.getRecipient());
        guestClientA.checkModuleAvailable(module1);
        guestClientA.checkModuleAvailable(module2);
        guestClientA.checkFolderAccessible(String.valueOf(folderA.getObjectID()), guestPermission);
        guestClientA.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        /*
         * check access to shares via link to folder B
         */
        GuestClient guestClientB = resolveShare(shareURLB, guestPermission.getRecipient());
        guestClientB.checkModuleAvailable(module1);
        guestClientB.checkModuleAvailable(module2);
        guestClientB.checkFolderAccessible(String.valueOf(folderA.getObjectID()), guestPermission);
        guestClientB.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
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
        InsertRequest insertRequest1 = new InsertRequest(api, folderA);
        insertRequest1.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse1 = client1.execute(insertRequest1);
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
         * discover & check guest
         */
        ExtendedPermissionEntity guestA = discoverGuestEntity(client1, api, module1, folderA.getObjectID(), matchingPermissionA.getEntity());
        checkGuestPermission(guestPermission, guestA);
        String shareURLA = discoverShareURL(client1, guestA);
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = Create.createPrivateFolder(randomUID(), module2, client2.getValues().getUserId(), guestPermission);
        folderB.setParentFolderID(getDefaultFolder(client2, module2));
        InsertRequest insertRequest2 = new InsertRequest(api, folderB);
        insertRequest2.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse2 = client2.execute(insertRequest2);
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
         * discover & check guest
         */
        ExtendedPermissionEntity guestB = discoverGuestEntity(client2, api, module2, folderB.getObjectID(), matchingPermissionB.getEntity());
        checkGuestPermission(guestPermission, guestB);
        String shareURLB = discoverShareURL(client2, guestB);
        /*
         * check permission entities
         */
        assertEquals("Permission entities differ", matchingPermissionA.getEntity(), matchingPermissionB.getEntity());
        /*
         * check access to shares via link to folder A
         */
        GuestClient guestClientA = resolveShare(shareURLA, guestPermission.getRecipient());
        guestClientA.checkModuleAvailable(module1);
        guestClientA.checkModuleAvailable(module2);
        guestClientA.checkFolderAccessible(String.valueOf(folderA.getObjectID()), guestPermission);
        guestClientA.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        String folderATarget = guestClientA.getShareResolveResponse().getTarget();
        /*
         * check access to shares via link to folder B
         */
        GuestClient guestClientB = resolveShare(shareURLB, guestPermission.getRecipient());
        guestClientB.checkModuleAvailable(module1);
        guestClientB.checkModuleAvailable(module2);
        guestClientB.checkFolderAccessible(String.valueOf(folderA.getObjectID()), guestPermission);
        guestClientB.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        String folderBTarget = guestClientB.getShareResolveResponse().getTarget();
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
        guestClientA.checkModuleAvailable(module2);
        guestClientA.checkFolderNotAccessible(String.valueOf(folderA.getObjectID()));
        guestClientA.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        guestClientB.checkSessionAlive(false);
        // FIXME: capabilities are cached and not invalidated
        // guestClientB.checkModuleNotAvailable(module1);
        guestClientB.checkModuleAvailable(module2);
        guestClientB.checkFolderNotAccessible(String.valueOf(folderA.getObjectID()));
        guestClientB.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        /*
         * Check if share link to folder A still accessible. The response should result in a message, that the requested share
         * is not available, but that others still are. The contained target must be one of those others - which can only be
         * folder B in this case.
         */
        ResolveShareResponse shareResolveResponse = new GuestClient(shareURLA, guestPermission.getRecipient(), false).getShareResolveResponse();
        assertEquals("Login type wrong", "guest_password", shareResolveResponse.getLoginType());
        assertEquals("Status wrong", "not_found_continue", shareResolveResponse.getStatus());
        assertEquals("Target wrong", folderBTarget,  shareResolveResponse.getTarget());
        /*
         * check if share link to folder A still accessible
         */
        shareResolveResponse = new GuestClient(shareURLB, guestPermission.getRecipient()).getShareResolveResponse();
        assertEquals("Status code wrong", HttpServletResponse.SC_MOVED_TEMPORARILY, shareResolveResponse.getStatusCode());
    }

}
