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

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.contact.AbstractContactTest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link Bug41537Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug41537Test extends ShareTest {

    /**
     * Initializes a new {@link Bug41537Test}.
     * @param name
     */
    public Bug41537Test(String name) {
        super(name);
    }

    public void testGuestCanUpdateHisContactImage() throws Exception {
        /*
         * Create share
         */
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", randomUID());
        FolderObject folder = insertSharedFolder(
            EnumAPI.OUTLOOK,
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            guestPermission);
        OCLPermission guestEntityPermission = findFirstGuestPermission(folder);
        assertNotNull(guestEntityPermission);
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OUTLOOK, FolderObject.INFOSTORE, folder.getObjectID(), guestEntityPermission.getEntity());
        checkGuestPermission(guestPermission, guest);

        /*
         * Init guest session
         */
        String folderShareURL = discoverShareURL(guest);
        GuestClient guestClient = resolveShare(folderShareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);

        /*
         * Update contact image
         */
        TimeZone timeZone = guestClient.getValues().getTimeZone();
        int guestUserId = guestClient.getValues().getUserId();
        User guestUser = guestClient.execute(new GetRequest(guestUserId, timeZone, true)).getUser();
        Contact contact = new Contact();
        contact.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        contact.setObjectID(guestUser.getContactId());
        contact.setInternalUserId(guestUserId);
        contact.setImageContentType("image/png");
        contact.setLastModified(new Date());
        byte[] imageBytes = BaseEncoding.base64().decode(CONTACT_IMAGE);
        contact.setImage1(imageBytes);
        guestClient.execute(new UpdateRequest(contact, null, true));

        /*
         * Reload and check
         */
        GetResponse reloadResponse = guestClient.execute(new GetRequest(guestUserId, timeZone, true));
        Contact reloaded = reloadResponse.getContact();
        assertEquals("No image set in contact", 1, reloaded.getNumberOfImages());
        assertEquals("Wrong image content type set in contact", "image/png", reloaded.getImageContentType());
        assertNotNull("No image set in contact", reloadResponse.getImageUrl());
        byte[] reloadedImageBytes = AbstractContactTest.loadImageByURL(guestClient, reloadResponse.getImageUrl());
        assertTrue("Wrong image set in contact", Arrays.equals(imageBytes, reloadedImageBytes));

        /*
         * Try reload as sharing user
         */
        reloadResponse = client.execute(new GetRequest(guestUserId, timeZone, true));
        reloaded = reloadResponse.getContact();
        assertEquals("No image set in contact", 1, reloaded.getNumberOfImages());
        assertEquals("Wrong image content type set in contact", "image/png", reloaded.getImageContentType());
        assertNotNull("No image set in contact", reloadResponse.getImageUrl());
        reloadedImageBytes = AbstractContactTest.loadImageByURL(client, reloadResponse.getImageUrl());
        assertTrue("Wrong image set in contact", Arrays.equals(imageBytes, reloadedImageBytes));
    }

    private static final String CONTACT_IMAGE =
        "iVBORw0KGgoAAAANSUhEUgAAAEwAAABMCAMAAADwSaEZAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAN" +
        "1wAADdcBQiibeAAAAAd0SU1FB9wEDAgrFQPAJ7YAAAAidEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRo" +
        "IEdJTVAgb24gYSBNYWOHqHdDAAAAUVBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD///9cFzNQAAAA" +
        "GnRSTlMAAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGXTAuKUAAAABYktHRBp1Z+QyAAABNklEQVRY" +
        "w+3X3Y6EIAwF4OLPrAq4KjDlvP+L7tUmsztCLHIzCecBvkCNbSH61KjRHIEBDocZ1T1qcXiJW25w" +
        "4x8KANxYak2Mt/BUaEWcJBZpI+M0XHLTHYnsBZdEMlO9gxUcrUcmvRCbc9gsxNYcttYrmbxoLoc5" +
        "IeZzmBdiIYeFhjWsYbX+zVCza0h/9C2HbUJM5zAtHSgxbUXpQMkNASvfptITvWCvUse5dRRtQfa0" +
        "XgWXTFYtiodmwxrWsGFNPSpW6avi8Z1t21N3vfnM2Y0WAJy+1m077XEhTztcKNUTF8NbvniPDaIc" +
        "X6kWrqYd4rjl7Fso7VAUr/9znfEoTjCvXGcZt8L2l1PmJgUAbBQR0eBRJX4g6gMqJfTno7YslkI9" +
        "LBDXw5hiPSwSKqZhDfs8zNWz3A+fuiRwXiy9mwAAAABJRU5ErkJggg==";

}
