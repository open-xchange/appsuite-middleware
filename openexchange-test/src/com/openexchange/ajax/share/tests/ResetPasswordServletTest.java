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

import java.net.URLEncoder;
import org.junit.Assert;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResetPasswordServletRequest;
import com.openexchange.ajax.share.actions.ResetPasswordServletResponse;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;

/**
 * {@link ResetPasswordServletTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public final class ResetPasswordServletTest extends ShareTest {

    private OCLGuestPermission guestPermission;

    private ParsedShare share;

    /**
     * Initializes a new {@link ResetPasswordServletTest}
     *
     * @param name The test name
     */
    public ResetPasswordServletTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        OCLGuestPermission lGuestPermission = createNamedAuthorPermission(randomUID() + "@example.com", "Test Guest", "secret");
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        FolderObject folder = insertSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), lGuestPermission);
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
        checkPermissions(lGuestPermission, matchingPermission);
        /*
         * discover & check share
         */
        ParsedShare lShare = discoverShare(matchingPermission.getEntity(), folder.getObjectID());
        checkShare(lGuestPermission, lShare);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(lShare, ((GuestRecipient) lGuestPermission.getRecipient()).getEmailAddress(), ((GuestRecipient) lGuestPermission.getRecipient()).getPassword());
        guestClient.checkShareModuleAvailable();
        this.share = lShare;
        this.guestPermission = lGuestPermission;
    }

    public void testResetPassword_retrievedRedirectLocation() throws Exception {
        ResetPasswordServletResponse response = Executor.execute(getSession(), new ResetPasswordServletRequest(share.getToken(), false));
        String location = response.getLocation();

        Assert.assertNotNull("Redirect URL cannot be null", location);
        int lastIndexOf = share.getToken().lastIndexOf("/");
        Assert.assertTrue("Redirect URL does not contain a token", location.contains(share.getToken().substring(0, lastIndexOf)));
        String encode = URLEncoder.encode(((GuestRecipient) share.getRecipient()).getEmailAddress());
        Assert.assertTrue("Redirect URL does not contain email address of the guest. Expected: " + encode + " within redirect URL: " + location, location.contains(encode));
    }

    public void testResetPassword_loginNotPossibleAnyMore() throws Exception {
        Executor.execute(getSession(), new ResetPasswordServletRequest(share.getToken(), false));

        // Try to get share with obsolete password
        GuestClient guestClient = resolveShare(share, ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress(), ((GuestRecipient) guestPermission.getRecipient()).getPassword());
        Assert.assertNotNull("LoginException not avaiable! Login still possible. Password reset did not happen!", guestClient.getLoginResponse().getException());
        Assert.assertEquals("Login still possible; password reset did not happen!", LoginExceptionCodes.INVALID_CREDENTIALS.getNumber(), guestClient.getLoginResponse().getException().getCode());
    }

}
