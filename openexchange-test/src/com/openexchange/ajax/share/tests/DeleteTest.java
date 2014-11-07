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

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.AllResponse;
import com.openexchange.ajax.share.actions.DeleteRequest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link DeleteTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DeleteTest extends ShareTest {

    /**
     * Initializes a new {@link DeleteTest}.
     *
     * @param name The test name
     */
    public DeleteTest(String name) {
        super(name);
    }

    public void testDeleteShareRandomly() throws Exception {
        testDeleteShare(randomFolderAPI(), randomModule(), randomGuestPermission());
    }

    public void noTestDeleteShareExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (OCLGuestPermission guestPermission : TESTED_PERMISSIONS) {
                for (int module : TESTED_MODULES) {
                    testDeleteShare(api, module, guestPermission);
                }
            }
        }
    }

    private void testDeleteShare(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testDeleteShare(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testDeleteShare(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
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
        AllResponse allResponse = client.execute(new AllRequest());
        List<ParsedShare> allShares = allResponse.getParsedShares();
        ParsedShare share = discoverShare(allShares, folder.getObjectID(), matchingPermission.getEntity());
        checkShare(guestPermission, share);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(share, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * delete share
         */
        CommonDeleteResponse deleteResponse = client.execute(new DeleteRequest(share, allResponse.getTimestamp().getTime()));
        assertNotNull("no response", deleteResponse);
        assertFalse("errors in response", deleteResponse.hasError());
        /*
         * check shares list
         */
        allResponse = client.execute(new AllRequest());
        allShares = allResponse.getParsedShares();
        ParsedShare discoveredShare = discoverShare(allShares, matchingPermission.getEntity(), folder.getObjectID());
        assertTrue("share still found", null == discoveredShare);
        /*
         * check folder permissions
         */
        folder = getFolder(api, folder.getObjectID());
        for (OCLPermission permission : folder.getPermissions()) {
            assertFalse("Guest permission still found", permission.getEntity() == share.getGuest());
        }
        /*
         * check guest access to share
         */
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            /*
             * for anonymous guest user, check access with previous guest session (after waiting some time until background operations took place)
             */
            Thread.sleep(CLEANUP_DELAY);
            guestClient.checkSessionAlive(true);
            /*
             * check if share link still accessible
             */
            GuestClient revokedGuestClient = new GuestClient(share.getShareURL(), guestPermission.getRecipient(), false);
            ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
            assertEquals("Status code wrong", HttpServletResponse.SC_NOT_FOUND, shareResolveResponse.getStatusCode());
        } else {
            /*
             * check if share target no longer accessible for non-anonymous guest user, since session may still be alive
             */
            guestClient.checkFolderNotAccessible(share.getTarget().getFolder());
            guestClient.logout();
        }
    }

}
