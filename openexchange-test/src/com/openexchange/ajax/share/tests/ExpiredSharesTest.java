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

import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link ExpiredSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExpiredSharesTest extends ShareTest {

    /**
     * Initializes a new {@link ExpiredSharesTest}.
     *
     * @param name The test name
     */
    public ExpiredSharesTest(String name) {
        super(name);
    }

    public void testAccessExpiredShareRandomly() throws Exception {
        testAccessExpiredShare(randomFolderAPI(), randomModule(), randomGuestPermission());
    }

    public void noTestAccessExpiredShareExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (OCLGuestPermission guestPermission : TESTED_PERMISSIONS) {
                for (int module : TESTED_MODULES) {
                    testAccessExpiredShare(api, module, guestPermission);
                }
            }
        }
    }

    private void testAccessExpiredShare(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testAccessExpiredShare(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testAccessExpiredShare(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * apply expiration time to permission
         */
        long expirationTime = 10000L; // 10 seconds
        Date expires = new Date(System.currentTimeMillis() + expirationTime);
        guestPermission = (OCLGuestPermission) guestPermission.clone();
        guestPermission.setExpires(expires);
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
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
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(share, guestPermission.getEmailAddress(), guestPermission.getPassword());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * wait some time until the share is expired
         */
        Thread.sleep(expirationTime);
        /*
         * check if share link still accessible
         */
        GuestClient revokedGuestClient = new GuestClient(share.getShareURL(), guestPermission.getEmailAddress(), guestPermission.getPassword(), false);
        ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
        assertEquals("Status code wrong", HttpServletResponse.SC_NOT_FOUND, shareResolveResponse.getStatusCode());
        /*
         * check permissions of previously shared folder
         */
        folder = getFolder(api, folder.getObjectID());
        for (OCLPermission permission : folder.getPermissions()) {
            assertTrue("Guest permission still present", permission.getEntity() != matchingPermission.getEntity());
        }
        /*
         * check if previous session is still alive
         */
        guestClient.checkSessionAlive(true);
    }

}
