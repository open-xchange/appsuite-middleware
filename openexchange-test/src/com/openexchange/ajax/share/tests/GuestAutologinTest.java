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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import org.apache.commons.lang.StringUtils;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.actions.AutologinRequest;
import com.openexchange.ajax.session.actions.AutologinRequest.AutologinParameters;
import com.openexchange.ajax.session.actions.AutologinResponse;
import com.openexchange.ajax.session.actions.StoreRequest;
import com.openexchange.ajax.session.actions.StoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link GuestAutologinTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.2
 */
public class GuestAutologinTest extends ShareTest {

    private final String EMAIL = randomUID() +"@example.org";
    private final String PASSWORD = "secret";
    private AJAXSession sharedSession;
    private ParsedShare share;

    /**
     * Initializes a new {@link GuestAutologinTest}.
     *
     * @param name
     */
    public GuestAutologinTest(String name) {
        super(name);
    }

    public void testGuestAutologin() throws Exception {
        create();
        String oldSessionID = sharedSession.getId();
        try {
            sharedSession.setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, share.getShareURL(), EMAIL, PASSWORD, AJAXClient.class.getName(),  true, false);
            StoreRequest storeRequest = new StoreRequest(sharedSession.getId(), false);
            StoreResponse storeResponse = guestClient.execute(storeRequest);
            assertFalse(storeResponse.getErrorMessage(), storeResponse.hasError());
            AutologinRequest autologin = new AutologinRequest(new AutologinParameters(randomUID(), AJAXClient.class.getName(), AJAXClient.VERSION, share.getToken()), false);
            AutologinResponse response = guestClient.execute(autologin);
            assertFalse(response.getErrorMessage(), response.hasError());
            assertEquals(guestClient.getSession().getId(), response.getSessionId());
            assertEquals(EMAIL, response.getUser());
        } finally {
            sharedSession.setId(oldSessionID);
        }
    }

    public void testGuestAutologinWithoutStore() throws Exception {
        create();
        String oldSessionID = sharedSession.getId();
        try {
            sharedSession.setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, share.getShareURL(), EMAIL, PASSWORD, AJAXClient.class.getName(),  true, false);
            AutologinRequest autologin = new AutologinRequest(new AutologinParameters(randomUID(), AJAXClient.class.getName(), AJAXClient.VERSION, share.getToken()), false);
            AutologinResponse response = guestClient.execute(autologin);
            assertTrue("Autologin worked without store request", response.hasError());
            assertEquals(OXJSONExceptionCodes.INVALID_COOKIE.getNumber(), response.getException().getCode());
        } finally {
            sharedSession.setId(oldSessionID);
        }
    }

    public void testInvalidSharetoken() throws Exception {
        create();
        String oldSessionID = sharedSession.getId();
        try {
            sharedSession.setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, share.getShareURL(), EMAIL, PASSWORD, AJAXClient.class.getName(),  true, false);
            StoreRequest storeRequest = new StoreRequest(sharedSession.getId(), false);
            StoreResponse storeResponse = guestClient.execute(storeRequest);
            assertFalse(storeResponse.getErrorMessage(), storeResponse.hasError());
            AutologinRequest autologin = new AutologinRequest(new AutologinParameters(randomUID(), AJAXClient.class.getName(), AJAXClient.VERSION, StringUtils.reverse(share.getToken())), false);
            AutologinResponse response = guestClient.execute(autologin);
            assertTrue("Autologin worked with invalid share token", response.hasError());
            assertEquals(ShareExceptionCodes.INVALID_TOKEN.getNumber(), response.getException().getCode());
        } finally {
            sharedSession.setId(oldSessionID);
        }
    }

    private void create() throws Exception {
        OCLGuestPermission guestPermission = createNamedGuestPermission(EMAIL, "Test Guest", PASSWORD);

        int module = randomModule();
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, module, getDefaultFolder(module));
        /*
         * update folder, add permission for guest
         */
        folder.addPermission(guestPermission);
        folder = updateFolder(EnumAPI.OX_NEW, folder);
        remember(folder);
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
        share = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(guestPermission, folder, share);
        /*
         * check access to share, using the same ajax session as the sharing user
         */
        sharedSession = getSession();
    }

}
