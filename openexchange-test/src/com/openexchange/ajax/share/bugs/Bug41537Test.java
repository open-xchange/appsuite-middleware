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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
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

    private static final byte[] CONTACT_IMAGE = {
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 100, 0, 0, 0, 100, 8, 2, 0, 0, 0, -1, -128, 2, 3, 0, 0, 0,
        -30, 73, 68, 65, 84, 120, -38, -19, -40, 73, 14, -124, 32, 16, 64, 81, 19, -18, 127, 102, 76, 92, 58, 20, -72, 64, 9, -11, -2,
        -82, -23, -87, -6, 69, 13, -19, -74, -87, -81, 82, 10, 4, 73, -54, 83, -43, 17, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63,
        -126, 5, 75, -80, 96, -63, -126, 5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, 22, 44, 88, -126, 5, 11, 22, 44, 88,
        -80, 4, 11, 22, 44, 88, -80, 96, 13, -99, -32, 52, -54, -85, 23, 92, -97, 26, 52, -10, 44, 88, -63, -105, 54, 71, -6, 108, -20,
        -119, -80, -98, 86, 94, 61, -68, 93, 89, -16, 52, 124, 90, 12, 78, -52, -98, -73, -61, -126, 5, 11, -42, 15, 23, -8, 24, 43, -41,
        5, -66, -71, 117, 8, 14, -100, 116, 91, -121, -98, 77, 105, 115, 37, -53, -90, 116, -2, -49, 92, 4, -85, 121, -36, -63, -86, -3,
        -1, -121, 96, 69, 63, -61, 45, 26, -73, 104, 96, -63, 18, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63, -126, 5, 75, -80, 96,
        -63, -126, 5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, -42, 122, 88, -110, 36, 37, 107, 7, 58, -3, 68, -102, -42,
        88, 97, 118, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };

    /**
     * Initializes a new {@link Bug41537Test}.
     *
     * @param name
     */
    public Bug41537Test() {
        super();
    }

    @Test
    public void testGuestCanUpdateHisContactImage() throws Exception {
        /*
         * Create share
         */
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", randomUID());
        FolderObject folder = insertSharedFolder(EnumAPI.OUTLOOK, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), guestPermission);
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
        contact.setImage1(CONTACT_IMAGE);
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
        assertTrue("Wrong image set in contact", Arrays.equals(CONTACT_IMAGE, reloadedImageBytes));

        /*
         * Try reload as sharing user
         */
        reloadResponse = getClient().execute(new GetRequest(guestUserId, timeZone, true));
        reloaded = reloadResponse.getContact();
        assertEquals("No image set in contact", 1, reloaded.getNumberOfImages());
        assertEquals("Wrong image content type set in contact", "image/png", reloaded.getImageContentType());
        assertNotNull("No image set in contact", reloadResponse.getImageUrl());
        reloadedImageBytes = AbstractContactTest.loadImageByURL(getClient(), reloadResponse.getImageUrl());
        assertTrue("Wrong image set in contact", Arrays.equals(CONTACT_IMAGE, reloadedImageBytes));
    }

}
