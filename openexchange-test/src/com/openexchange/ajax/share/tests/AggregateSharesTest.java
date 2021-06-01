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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link AggregateSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregateSharesTest extends Abstract2UserShareTest {

    private java.util.Map<AJAXClient, List<Integer>> clientsAndFolders;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clientsAndFolders = new HashMap<AJAXClient, List<Integer>>();
        clientsAndFolders.put(client1, new ArrayList<Integer>());
        clientsAndFolders.put(client2, new ArrayList<Integer>());
    }

    private AJAXClient randomClient() {
        AJAXClient[] ajaxClients = clientsAndFolders.keySet().toArray(new AJAXClient[clientsAndFolders.size()]);
        return ajaxClients[random.nextInt(ajaxClients.length)];
    }

    @Test
    @TryAgain
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

    @Test
    @TryAgain
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
        OCLGuestPermission guestPermission = createNamedGuestPermission();
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        FolderObject folderA = Create.createPrivateFolder(randomUID(), module1, client1.getValues().getUserId(), guestPermission);
        folderA.setParentFolderID(getDefaultFolder(client1, module1));
        InsertRequest insertRequest1 = new InsertRequest(api, folderA);
        insertRequest1.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse1 = client1.execute(insertRequest1);
        insertResponse1.fillObject(folderA);
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
        String shareURLA = discoverShareURL(guestPermission.getApiClient(), guestA);

        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = Create.createPrivateFolder(randomUID(), module2, client2.getValues().getUserId(), guestPermission);
        folderB.setParentFolderID(getDefaultFolder(client2, module2));
        InsertRequest insertRequest2 = new InsertRequest(api, folderB);
        insertRequest2.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse2 = client2.execute(insertRequest2);
        insertResponse2.fillObject(folderB);
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
        String shareURLB = discoverShareURL(guestPermission.getApiClient(), guestB);
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
        OCLGuestPermission guestPermission = createNamedGuestPermission();
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        FolderObject folderA = insertSharedFolder(client1, api, module1, getDefaultFolder(client1, module1), guestPermission);
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
        String shareURLA = discoverShareURL(guestPermission.getApiClient(), guestA);
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        FolderObject folderB = insertSharedFolder(client2, api, module2, getDefaultFolder(client2, module2), guestPermission);
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
        String shareURLB = discoverShareURL(guestPermission.getApiClient(), guestB);
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
        guestClientA.getShareResolveResponse().getTarget();
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
        folderA = updateFolder(client1, api, folderA);
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
        assertNotNull("No target", shareResolveResponse.getTarget());
        ShareTargetPath folderBShareTarget = ShareTargetPath.parse(folderBTarget);
        ShareTargetPath responseTarget = ShareTargetPath.parse(shareResolveResponse.getTarget());
        assertTrue("Target wrong. Expected: " + folderBTarget.toString() + " but got: " + responseTarget.toString(), folderBShareTarget.matches(responseTarget));
        /*
         * check if share link to folder A still accessible
         */
        shareResolveResponse = new GuestClient(shareURLB, guestPermission.getRecipient()).getShareResolveResponse();
        assertEquals("Status code wrong", HttpServletResponse.SC_MOVED_TEMPORARILY, shareResolveResponse.getStatusCode());
    }

}
