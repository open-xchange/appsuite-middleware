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

import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.InviteRequest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link TestGuestContact}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class TestGuestContact extends ShareTest {

    private static final int FOLDER_READ_PERMISSION = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    private ShareTarget target;
    private final long now = System.currentTimeMillis();
    private final String GUEST_DISPLAYNAME = "Test Guest Contact " + now;
    private final String GUEST_MAIL = "testGuestContact@" + now + ".invalid";
    private final String GUEST_PASSWORD = String.valueOf(now);


    /**
     * Initializes a new {@link TestGuestContact}.
     * @param name
     */
    public TestGuestContact(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        InfostoreTestManager itm = new InfostoreTestManager(client);
        FolderObject infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder());

        FolderObject parent = infostore;
        DefaultFile file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        file.setTitle("Test Create Guest Contact " + now);
        file.setDescription(file.getTitle());
        itm.newAction(file);

        target = new ShareTarget(Module.INFOSTORE.getFolderConstant(), file.getFolderId(), file.getId());
        GuestRecipient guest = new GuestRecipient();
        guest.setDisplayName(GUEST_DISPLAYNAME);
        guest.setEmailAddress(GUEST_MAIL);
        guest.setPassword(GUEST_PASSWORD);
        guest.setBits(FOLDER_READ_PERMISSION);

        client.execute(new InviteRequest(Collections.<ShareTarget>singletonList(target), Collections.<ShareRecipient>singletonList(guest)));
    }

    public void testGuestContact() throws Exception {
        List<ParsedShare> allShares = client.execute(new AllRequest()).getParsedShares();
        int guestId = -1;
        ParsedShare parsedShare = null;
        for (ParsedShare share : allShares) {
            if (share.getTarget().equals(target)) {
                guestId = share.getGuest();
                parsedShare = share;
                break;
            }
        }
        assertTrue("Guest id must not be -1", guestId > -1);
        GuestClient guestClient = new GuestClient(parsedShare.getShareURL(), GUEST_MAIL, GUEST_PASSWORD);
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

}
