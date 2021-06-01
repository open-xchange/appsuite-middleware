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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug41622Test}
 *
 * Sharing User is displayed with User id
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41622Test extends Abstract2UserShareTest {

    /**
     * Initializes a new {@link Bug41622Test}.
     *
     * @param name The test name
     */
    public Bug41622Test() {
        super();
    }

    @Test
    @TryAgain
    public void testShowSharingUsers() throws Exception {
        /*
         * prepare guest permission
         */
        EnumAPI api = randomFolderAPI();
        OCLGuestPermission guestPermission = createNamedGuestPermission();
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        int module1 = randomModule();
        FolderObject folderA = Create.createPrivateFolder(randomUID(), module1, getClient().getValues().getUserId(), guestPermission);
        folderA.setParentFolderID(getDefaultFolder(getClient(), module1));
        InsertRequest insertRequest1 = new InsertRequest(api, folderA);
        insertRequest1.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse1 = getClient().execute(insertRequest1);
        insertResponse1.fillObject(folderA);
        GetResponse getResponse1 = getClient().execute(new GetRequest(api, folderA.getObjectID()));
        folderA = getResponse1.getFolder();
        folderA.setLastModified(getResponse1.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionA = null;
        for (OCLPermission permission : folderA.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermissionA = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionA);
        checkPermissions(guestPermission, matchingPermissionA);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guestA = discoverGuestEntity(getClient(), api, module1, folderA.getObjectID(), matchingPermissionA.getEntity());
        checkGuestPermission(guestPermission, guestA);
        String shareURLA = discoverShareURL(guestPermission.getApiClient(), guestA);
        /*
         * as user 2 with client 2, create folder B shared to guest user
         */
        int module2 = randomModule();
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
        String shareURLB = discoverShareURL((guestPermission).getApiClient(), guestB);
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
         * check if both sharing users can be resolved
         */
        com.openexchange.user.User expectedUser1 = getClient().execute(new com.openexchange.ajax.user.actions.GetRequest(getClient().getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.user.User expectedUser2 = client2.execute(new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.user.User actualUser1 = guestClientA.execute(new com.openexchange.ajax.user.actions.GetRequest(getClient().getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.user.User actualUser2 = guestClientA.execute(new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        assertEquals(expectedUser1.getDisplayName(), actualUser1.getDisplayName());
        assertEquals(expectedUser1.getGivenName(), actualUser1.getGivenName());
        assertEquals(expectedUser1.getSurname(), actualUser1.getSurname());
        assertEquals(expectedUser2.getDisplayName(), actualUser2.getDisplayName());
        assertEquals(expectedUser2.getGivenName(), actualUser2.getGivenName());
        assertEquals(expectedUser2.getSurname(), actualUser2.getSurname());
        /*
         * check access to shares via link to folder B
         */
        GuestClient guestClientB = resolveShare(shareURLB, guestPermission.getRecipient());
        guestClientB.checkModuleAvailable(module1);
        guestClientB.checkModuleAvailable(module2);
        guestClientB.checkFolderAccessible(String.valueOf(folderA.getObjectID()), guestPermission);
        guestClientB.checkFolderAccessible(String.valueOf(folderB.getObjectID()), guestPermission);
        /*
         * check if both sharing users can be resolved
         */
        actualUser1 = guestClientB.execute(new com.openexchange.ajax.user.actions.GetRequest(getClient().getValues().getUserId(), TimeZones.UTC, true)).getUser();
        actualUser2 = guestClientB.execute(new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        assertEquals(expectedUser1.getDisplayName(), actualUser1.getDisplayName());
        assertEquals(expectedUser1.getGivenName(), actualUser1.getGivenName());
        assertEquals(expectedUser1.getSurname(), actualUser1.getSurname());
        assertEquals(expectedUser2.getDisplayName(), actualUser2.getDisplayName());
        assertEquals(expectedUser2.getGivenName(), actualUser2.getGivenName());
        assertEquals(expectedUser2.getSurname(), actualUser2.getSurname());
    }

}
