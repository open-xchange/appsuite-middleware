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

import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.UpdateRecipientRequest;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.AnonymousRecipient;

/**
 * {@link UpdateAnonymousGuestPasswordTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateAnonymousGuestPasswordTest extends ShareTest {

    /**
     * Initializes a new {@link UpdateAnonymousGuestPasswordTest}.
     *
     * @param name The test name
     */
    public UpdateAnonymousGuestPasswordTest(String name) {
        super(name);
    }

    public void testUpdateAddPasswordForAnonymousGuest() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        FolderObject folder = insertSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check share
         */
        ParsedShare share = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(guestPermission, share);
        assertTrue(AnonymousRecipient.class.isInstance(share.getRecipient()));
        assertNull("Password is set", ((AnonymousRecipient) share.getRecipient()).getPassword());
        /*
         * update recipient, set a password for the anonymous guest
         */
        AnonymousRecipient recipient = new AnonymousRecipient();
        recipient.setPassword("secret");
        recipient.setBits(Permissions.createPermissionBits(guestPermission.getFolderPermission(), guestPermission.getReadPermission(),
            guestPermission.getWritePermission(), guestPermission.getDeletePermission(), guestPermission.isFolderAdmin()));
        getClient().execute(new UpdateRecipientRequest(share.getGuest(), recipient));
        /*
         * discover & check share
         */
        share = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(guestPermission, share);
        assertTrue(AnonymousRecipient.class.isInstance(share.getRecipient()));
        assertNotNull("Password not set", ((AnonymousRecipient) share.getRecipient()).getPassword());
        assertEquals("Password wrong", recipient.getPassword(), ((AnonymousRecipient) share.getRecipient()).getPassword());
        /*
         * update recipient, change password for the anonymous guest
         */
        recipient.setPassword("geheim");
        getClient().execute(new UpdateRecipientRequest(share.getGuest(), recipient));
        /*
         * discover & check share
         */
        share = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(guestPermission, share);
        assertTrue(AnonymousRecipient.class.isInstance(share.getRecipient()));
        assertNotNull("Password not set", ((AnonymousRecipient) share.getRecipient()).getPassword());
        assertEquals("Password wrong", recipient.getPassword(), ((AnonymousRecipient) share.getRecipient()).getPassword());
        /*
         * update recipient remove password for the anonymous guest
         */
        recipient.setPassword(null);
        getClient().execute(new UpdateRecipientRequest(share.getGuest(), recipient));
        /*
         * discover & check share
         */
        share = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(guestPermission, share);
        assertTrue(AnonymousRecipient.class.isInstance(share.getRecipient()));
        assertNull("Password is set", ((AnonymousRecipient) share.getRecipient()).getPassword());
    }

}
