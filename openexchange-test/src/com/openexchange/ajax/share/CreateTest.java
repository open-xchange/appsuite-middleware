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

package com.openexchange.ajax.share;

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link CreateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateTest extends ShareTest {

    private static final OCLGuestPermission[] TESTED_PERMISSIONS = new OCLGuestPermission[] {
        createNamedAuthorPermission("otto@example.com", "Otto Example", "secret", AuthenticationMode.DIGEST),
        createNamedGuestPermission("horst@example.com", "Horst Example", "secret", AuthenticationMode.BASIC),
        createAnonymousAuthorPermission(),
        createAnonymousGuestPermission()
    };

    /**
     * Initializes a new {@link CreateTest}.
     *
     * @param name The test name
     */
    public CreateTest(String name) {
        super(name);
    }

    public void testCreateSharedContactFolders() throws Exception {
        testCreateSharedFolders(FolderObject.CONTACT, client.getValues().getPrivateContactFolder());
    }

    public void testCreateSharedInfostoreFolders() throws Exception {
        testCreateSharedFolders(FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
    }

    public void testCreateSharedTaskFolders() throws Exception {
        testCreateSharedFolders(FolderObject.TASK, client.getValues().getPrivateTaskFolder());
    }

    public void testCreateSharedCalendarFolders() throws Exception {
        testCreateSharedFolders(FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder());
    }

    private void testCreateSharedFolders(int module, int parent) throws Exception {
        for (EnumAPI api : new EnumAPI[] { EnumAPI.OX_OLD, EnumAPI.OX_NEW, EnumAPI.OUTLOOK }) {
            for (OCLGuestPermission guestPermission : TESTED_PERMISSIONS) {
                testCreateSharedFolder(api, module, parent, guestPermission);
            }
        }
    }

    private void testCreateSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder shared to guest user
         */
        FolderObject sharedFolder = super.insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : sharedFolder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        assertEquals("Permission wrong", guestPermission.getDeletePermission(), matchingPermission.getDeletePermission());
        assertEquals("Permission wrong", guestPermission.getFolderPermission(), matchingPermission.getFolderPermission());
        assertEquals("Permission wrong", guestPermission.getReadPermission(), matchingPermission.getReadPermission());
        assertEquals("Permission wrong", guestPermission.getWritePermission(), matchingPermission.getWritePermission());
        /*
         * discover share
         */
        ParsedShare share = discoverShare(sharedFolder.getObjectID(), matchingPermission.getEntity());
        assertNotNull("No matching share found", share);
        assertEquals("Authentication mode wrong", guestPermission.getAuthenticationMode(), share.getAuthentication());
        if (AuthenticationMode.ANONYMOUS != guestPermission.getAuthenticationMode()) {
            assertEquals("E-Mail address wrong", guestPermission.getEmailAddress(), share.getGuestMailAddress());
//TODO            assertEquals("Display name wrong", guestPermission.getDisplayName(), share.getGuestDisplayName());
            assertEquals("Password wrong", guestPermission.getPassword(), share.getGuestPassword());
        }
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(share);
        guestClient.checkShareAccessible(guestPermission);
    }

}
