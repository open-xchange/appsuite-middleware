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

package com.openexchange.ajax.share.bugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.StartSMTPRequest;
import com.openexchange.ajax.share.actions.StopSMTPRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link Bug41622Test}
 *
 * Sharing User is displayed with User id
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41622Test extends ShareTest {

    private java.util.Map<AJAXClient, List<Integer>> clientsAndFolders;
    private AJAXClient client2;

    /**
     * Initializes a new {@link Bug41622Test}.
     *
     * @param name The test name
     */
    public Bug41622Test(String name) {
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

    public void testShowSharingUsers() throws Exception {
        /*
         * prepare guest permission
         */
        EnumAPI api = randomFolderAPI();
        String name = randomUID();
        OCLGuestPermission guestPermission = createNamedGuestPermission(name + "@example.com", name, "secret");
        /*
         * as user 1 with client 1, create folder A shared to guest user
         */
        int module1 = randomModule();
        FolderObject folderA = Create.createPrivateFolder(randomUID(), module1, client.getValues().getUserId(), guestPermission);
        folderA.setParentFolderID(getDefaultFolder(client, module1));
        InsertRequest insertRequest1 = new InsertRequest(api, folderA);
        insertRequest1.setNotifyPermissionEntities(Transport.MAIL);
        InsertResponse insertResponse1 = client.execute(insertRequest1);
        insertResponse1.fillObject(folderA);
        clientsAndFolders.get(client).add(Integer.valueOf(folderA.getObjectID()));
        GetResponse getResponse1 = client.execute(new GetRequest(api, folderA.getObjectID()));
        folderA = getResponse1.getFolder();
        folderA.setLastModified(getResponse1.getTimestamp());
        /*
         * check permissions
         */
        OCLPermission matchingPermissionA = null;
        for (OCLPermission permission : folderA.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermissionA = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermissionA);
        checkPermissions(guestPermission, matchingPermissionA);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guestA = discoverGuestEntity(client, api, module1, folderA.getObjectID(), matchingPermissionA.getEntity());
        checkGuestPermission(guestPermission, guestA);
        String shareURLA = discoverShareURL(client, guestA);
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
         * check if both sharing users can be resolved
         */
        com.openexchange.groupware.ldap.User expectedUser1 = client.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.groupware.ldap.User expectedUser2 = client2.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.groupware.ldap.User actualUser1 = guestClientA.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        com.openexchange.groupware.ldap.User actualUser2 = guestClientA.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
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
        actualUser1 = guestClientB.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        actualUser2 = guestClientB.execute(
            new com.openexchange.ajax.user.actions.GetRequest(client2.getValues().getUserId(), TimeZones.UTC, true)).getUser();
        assertEquals(expectedUser1.getDisplayName(), actualUser1.getDisplayName());
        assertEquals(expectedUser1.getGivenName(), actualUser1.getGivenName());
        assertEquals(expectedUser1.getSurname(), actualUser1.getSurname());
        assertEquals(expectedUser2.getDisplayName(), actualUser2.getDisplayName());
        assertEquals(expectedUser2.getGivenName(), actualUser2.getGivenName());
        assertEquals(expectedUser2.getSurname(), actualUser2.getSurname());
    }

}
