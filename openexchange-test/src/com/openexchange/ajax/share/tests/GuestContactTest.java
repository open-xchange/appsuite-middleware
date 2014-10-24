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
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.DeleteRequest;
import com.openexchange.ajax.share.actions.NewRequest;
import com.openexchange.ajax.share.actions.NewResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link GuestContactTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class GuestContactTest extends ShareTest {

    private static final int FOLDER_READ_PERMISSION = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    private ShareTarget target;
    private InfostoreTestManager itm;
    private DefaultFile file;
    private int guestId;
    private ParsedShare share;
    private List<ParsedShare> shares;
    private final long now = System.currentTimeMillis();
    private final String GUEST_DISPLAYNAME = "Test Guest Contact " + now;
    private final String GUEST_MAIL = "testGuestContact@" + now + ".invalid";
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
        itm = new InfostoreTestManager(client);
        FolderObject infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder());

        FolderObject parent = infostore;
        file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        file.setTitle("Test Create Guest Contact " + now);
        file.setDescription(file.getTitle());
        itm.newAction(file);

        target = new ShareTarget(Module.INFOSTORE.getFolderConstant(), file.getFolderId(), file.getId());
        target.setOwnedBy(client.getValues().getUserId());
        GuestRecipient guest = new GuestRecipient();
        guest.setDisplayName(GUEST_DISPLAYNAME);
        guest.setEmailAddress(GUEST_MAIL);
        guest.setPassword(GUEST_PASSWORD);
        guest.setBits(FOLDER_READ_PERMISSION);

        NewRequest newRequest = new NewRequest(Collections.<ShareTarget>singletonList(target), Collections.<ShareRecipient>singletonList(guest));
        NewResponse newResponse = client.execute(newRequest);
        JSONArray jsonArray = (JSONArray) newResponse.getData();
        shares = new ArrayList<ParsedShare>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            //TODO: shares from new response
            //tokens.add(jsonArray.getString(i));
        }
        List<ParsedShare> allShares = client.execute(new AllRequest()).getParsedShares();
        guestId = -1;
        share = null;
        for (ParsedShare parsedShare : allShares) {
            if (parsedShare.getTarget().equals(target)) {
                guestId = parsedShare.getGuest();
                share = parsedShare;
                break;
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(shares, System.currentTimeMillis(), false);
        client.execute(deleteRequest);
        itm.deleteAction(file);
        super.tearDown();
    }

    public void testCreateGuestContact() throws Exception {
        assertTrue("Guest id must not be -1", guestId > -1);
        GuestClient guestClient = new GuestClient(share.getShareURL(), GUEST_MAIL, GUEST_PASSWORD);
        GetRequest guestGetRequest = new GetRequest(guestId, guestClient.getValues().getTimeZone());
        GetResponse guestGetResponse = guestClient.execute(guestGetRequest);
        Contact guestContact = guestGetResponse.getContact();
        GetRequest getRequest = new GetRequest(guestId, client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        assertEquals("Contacts does not match", contact, guestContact);
        assertNotNull("Contact is null.", contact);
        assertEquals("Wrong display name.", GUEST_DISPLAYNAME, contact.getDisplayName());
        assertEquals("Wrong email address.", GUEST_MAIL, contact.getEmail1());
        assertTrue("Contact id is 0.", contact.getObjectID() != 0);
    }

    public void testUpdateGuestContact() throws Exception {
        assertTrue("Guest id must not be -1", guestId > -1);
        GuestClient guestClient = new GuestClient(share.getShareURL(), GUEST_MAIL, GUEST_PASSWORD);
        GetRequest guestGetRequest = new GetRequest(guestId, guestClient.getValues().getTimeZone());
        GetResponse guestGetResponse = guestClient.execute(guestGetRequest);
        Contact guestContact = guestGetResponse.getContact();
        User guestUser = guestGetResponse.getUser();
        guestContact.setDisplayName(GUEST_DISPLAYNAME + "modified");
        UpdateRequest updateRequest = new UpdateRequest(guestContact, guestUser);
        UpdateResponse updateResponse = guestClient.execute(updateRequest);
        assertFalse(updateResponse.getErrorMessage(), updateResponse.hasError());

        GetRequest getRequest = new GetRequest(guestId, client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        assertEquals("Display name was not updated", GUEST_DISPLAYNAME + "modified", contact.getDisplayName());
        contact.setDisplayName(GUEST_DISPLAYNAME);
        UpdateRequest updateRequest2 = new UpdateRequest(guestContact, guestUser, false);
        UpdateResponse updateResponse2 = client.execute(updateRequest2);
        assertTrue("Client was able to update foreign contact.", updateResponse2.hasError());
        assertEquals(ContactExceptionCodes.NO_CHANGE_PERMISSION.getNumber(), updateResponse2.getException().getCode());
    }

    public void testDeleteGuestContact() throws Exception {
        assertTrue("Guest id must not be -1", guestId > -1);
        DeleteRequest deleteRequest = new DeleteRequest(shares, System.currentTimeMillis());
        CommonDeleteResponse deleteResponse = client.execute(deleteRequest);
        assertFalse(deleteResponse.getErrorMessage(), deleteResponse.hasError());
        GetRequest getRequest = new GetRequest(guestId, client.getValues().getTimeZone(), false);
        GetResponse getResponse = client.execute(getRequest);
        assertTrue("Contact was not deleted.", getResponse.hasError());
        assertEquals(ContactExceptionCodes.CONTACT_NOT_FOUND.getNumber(), getResponse.getException().getCode());
    }

    public void testOtherUser() throws Exception {
        assertTrue("Guest id must not be -1", guestId > -1);
        AJAXClient secondClient = new AJAXClient(AJAXClient.User.User2);
        GetRequest getRequest = new GetRequest(guestId, secondClient.getValues().getTimeZone());
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
        DeleteRequest deleteRequest = new DeleteRequest(shares, System.currentTimeMillis(), false);
        CommonDeleteResponse deleteResponse = secondClient.execute(deleteRequest);
        assertTrue("Any user can delete any shares.", deleteResponse.hasError());
        assertEquals(ShareExceptionCodes.NO_DELETE_PERMISSIONS.getNumber(), deleteResponse.getException().getCode());
    }

}
