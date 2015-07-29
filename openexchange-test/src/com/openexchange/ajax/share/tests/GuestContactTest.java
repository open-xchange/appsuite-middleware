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

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;


/**
 * {@link GuestContactTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class GuestContactTest extends ShareTest {

    private File file;
    private ExtendedPermissionEntity guest;
    private final long now = System.currentTimeMillis();
    private final String GUEST_DISPLAYNAME = "Test Guest Contact " + now;
    private final String GUEST_MAIL = "testGuestContact@" + now + ".example.org";
    private final String GUEST_PASSWORD = String.valueOf(now);


    /**
     * Initializes a new {@link GuestContactTest}.
     * @param name
     */
    public GuestContactTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FolderObject infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder());
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createNamedGuestPermission(GUEST_MAIL, GUEST_DISPLAYNAME, GUEST_PASSWORD));
        file = insertSharedFile(infostore.getObjectID(), guestPermission);
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateGuestContact() throws Exception {
        assertTrue("Guest id must not be -1", guest.getEntity() > -1);
        GuestClient guestClient = new GuestClient(guest.getShareURL(), GUEST_MAIL, GUEST_PASSWORD);
        GetRequest guestGetRequest = new GetRequest(guest.getEntity(), guestClient.getValues().getTimeZone());
        GetResponse guestGetResponse = guestClient.execute(guestGetRequest);
        Contact guestContact = guestGetResponse.getContact();
        GetRequest getRequest = new GetRequest(guest.getEntity(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        assertEquals("Contacts does not match", contact, guestContact);
        assertNotNull("Contact is null.", contact);
        assertEquals("Wrong display name.", GUEST_DISPLAYNAME, contact.getDisplayName());
        assertEquals("Wrong email address.", GUEST_MAIL, contact.getEmail1());
        assertTrue("Contact id is 0.", contact.getObjectID() != 0);
    }

    public void testUpdateGuestContact() throws Exception {
        assertTrue("Guest id must not be -1", guest.getEntity() > -1);
        GuestClient guestClient = new GuestClient(guest.getShareURL(), GUEST_MAIL, GUEST_PASSWORD);
        GetRequest guestGetRequest = new GetRequest(guest.getEntity(), guestClient.getValues().getTimeZone());
        GetResponse guestGetResponse = guestClient.execute(guestGetRequest);
        Contact guestContact = guestGetResponse.getContact();
        User guestUser = guestGetResponse.getUser();
        guestContact.setDisplayName(GUEST_DISPLAYNAME + "modified");
        UpdateRequest updateRequest = new UpdateRequest(guestContact, guestUser);
        UpdateResponse updateResponse = guestClient.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());

        GetRequest getRequest = new GetRequest(guest.getEntity(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        assertEquals("Display name was not updated", GUEST_DISPLAYNAME + "modified", contact.getDisplayName());
        contact.setDisplayName(GUEST_DISPLAYNAME);
        UpdateRequest updateRequest2 = new UpdateRequest(guestContact, guestUser, false);
        UpdateResponse updateResponse2 = client.execute(updateRequest2);
        assertTrue("Client was able to update foreign contact.", updateResponse2.hasError());
        assertEquals(ContactExceptionCodes.NO_CHANGE_PERMISSION.getNumber(), updateResponse2.getException().getCode());
    }

    public void testOtherUser() throws Exception {
        assertTrue("Guest id must not be -1", guest.getEntity() > -1);
        AJAXClient secondClient = new AJAXClient(AJAXClient.User.User2);
        GetRequest getRequest = new GetRequest(guest.getEntity(), secondClient.getValues().getTimeZone());
        GetResponse getResponse = secondClient.execute(getRequest);
        assertFalse("Contact could not be loaded.", getResponse.hasError());
        Contact contact = getResponse.getContact();
        User user = getResponse.getUser();
        assertEquals("Wrong contact loaded.", GUEST_DISPLAYNAME, contact.getDisplayName());
        assertEquals("Wrong contact loaded.", GUEST_MAIL, contact.getEmail1());
        contact.setDisplayName("This should not work");
        UpdateRequest updateRequest = new UpdateRequest(contact, user, false);
        UpdateResponse updateResponse = secondClient.execute(updateRequest);
        assertTrue("Any user can change contact data.", updateResponse.hasError());
        assertEquals(ContactExceptionCodes.NO_CHANGE_PERMISSION.getNumber(), updateResponse.getException().getCode());
        //TODO
//        DeleteRequest deleteRequest = new DeleteRequest(shares, System.currentTimeMillis(), false);
//        CommonDeleteResponse deleteResponse = secondClient.execute(deleteRequest);
//        assertTrue("Any user can delete any shares.", deleteResponse.hasError());
//        assertEquals(FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.getNumber(), deleteResponse.getException().getCode());
    }

}
