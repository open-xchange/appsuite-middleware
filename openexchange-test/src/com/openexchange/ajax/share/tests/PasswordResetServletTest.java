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

import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import org.junit.Assert;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.PasswordResetConfirmServletRequest;
import com.openexchange.ajax.share.actions.PasswordResetConfirmServletResponse;
import com.openexchange.ajax.share.actions.PasswordResetServletRequest;
import com.openexchange.ajax.share.actions.StartSMTPRequest;
import com.openexchange.ajax.share.actions.StopSMTPRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;

/**
 * {@link PasswordResetServletTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public final class PasswordResetServletTest extends ShareTest {

    private OCLGuestPermission guestPermission;

    private ParsedShare share;

    private FolderObject folder;

    /**
     * Initializes a new {@link PasswordResetServletTest}
     *
     * @param name The test name
     */
    public PasswordResetServletTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        OCLGuestPermission lGuestPermission = createNamedAuthorPermission(randomUID() + "@example.com", "Test Guest", "secret");
        /*
         * \u00b0 create folder shared to guest user
         */
        int module = randomModule();
        folder = insertSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), lGuestPermission);
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
        checkShare(lGuestPermission, folder, lShare);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(lShare, ((GuestRecipient) lGuestPermission.getRecipient()).getEmailAddress(), ((GuestRecipient) lGuestPermission.getRecipient()).getPassword());
        guestClient.checkShareModuleAvailable();
        this.share = lShare;
        this.guestPermission = lGuestPermission;

        /*
         * start dummy smtp to catch password-reset mail
         */
        try {
            StartSMTPRequest startSMTPReqeuest = new StartSMTPRequest(true);
            startSMTPReqeuest.setUpdateNoReplyForContext(client.getValues().getContextId());
            client.execute(startSMTPReqeuest);
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new StopSMTPRequest());
        super.tearDown();
    }

    public void testResetPassword_confirmPasswordReset() throws Exception {
        String confirm = getConfirmationToken();
        PasswordResetConfirmServletResponse response = Executor.execute(getSession(), new PasswordResetConfirmServletRequest(share.getToken(), confirm, false));
        String location = response.getLocation();

        Assert.assertNotNull("Redirect URL cannot be null", location);
        Assert.assertEquals("Unexpected location", share.getShareURL(), location);
    }

    public void testResetPassword_passwordReset() throws Exception {
        String confirm = getConfirmationToken();
        Executor.execute(getSession(), new PasswordResetConfirmServletRequest(share.getToken(), confirm, false));
        String newPW = UUIDs.getUnformattedStringFromRandom();
        // Set the new password
        GuestClient guestClient = resolveShare(share, ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress(), newPW);
        guestClient.logout();
        // Login again to verify
        guestClient = resolveShare(share, ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress(), newPW);
        guestClient.logout();
    }

    private String getConfirmationToken() throws Exception {
        PasswordResetServletRequest request = new PasswordResetServletRequest(share.getToken());
        Executor.execute(getSession(), request);

        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        String url = message.getHeaders().get("X-Open-Xchange-Share-Reset-PW-URL");
        String query = new URI(url).getRawQuery();
        String[] kvPairs = query.split("&");
        for (String kvPair : kvPairs) {
            String[] kv = kvPair.split("=");
            if (kv.length == 2 && "confirm".equals(kv[0])) {
                return URLDecoder.decode(kv[1], "UTF-8");
            }
        }

        fail("Confirmation token was not set in URL: " + url);
        return null;
    }

}
