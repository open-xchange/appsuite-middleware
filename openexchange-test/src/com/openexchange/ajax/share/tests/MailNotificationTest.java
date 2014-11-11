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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.GetMailsRequest;
import com.openexchange.ajax.share.actions.GetMailsResponse.Message;
import com.openexchange.ajax.share.actions.InviteRequest;
import com.openexchange.ajax.share.actions.NotifyRequest;
import com.openexchange.ajax.share.actions.StartSMTPRequest;
import com.openexchange.ajax.share.actions.StopSMTPRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.tools.PasswordUtility;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link MailNotificationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotificationTest extends ShareTest {

    private FolderObject testFolder1;
    private FolderObject testFolder2;

    /**
     * Initializes a new {@link MailNotificationTest}.
     * @param name
     */
    public MailNotificationTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        UserValues userValues = client.getValues();
        testFolder1 = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userValues.getPrivateInfostoreFolder());
        testFolder2 = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userValues.getPrivateInfostoreFolder());
        client.execute(new StartSMTPRequest());
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new StopSMTPRequest());
        super.tearDown();
    }

    /**
     * Invite a user and expect that he gets his personal link as email, together with the necessary credentials.
     * Afterwards share a second folder with the same guest and expect another email without password.
     */
    public void testCreatedNotificationForGuestWithPassword() throws Exception {
        /*
         * First invitation
         */
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null);
        InviteRequest inviteRequest = new InviteRequest();
        inviteRequest.addTarget(new ShareTarget(testFolder1.getModule(), Integer.toString(testFolder1.getObjectID())));
        inviteRequest.addRecipient(guestPermission.getRecipient());
        client.execute(inviteRequest);

        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);

        /*
         * assert magic headers and content
         */
        Map<String, String> headers = message.getHeaders();
        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
        String url = headers.get("X-Open-Xchange-Share-URL");
        assertNotNull(url);
        String access = headers.get("X-Open-Xchange-Share-Access");
        assertNotNull(access);
        String[] credentials = new String(Base64.decode(access), Charset.forName("UTF-8")).split(":");
        assertEquals(2, credentials.length);
        String username = credentials[0];
        String password = credentials[1];

        String plainText = message.getPlainText();
        assertNotNull(plainText);
        assertTrue(plainText.contains(username));
        assertTrue(plainText.contains(password));

        /*
         * check received link and credentials
         */
        GuestClient guestClient = new GuestClient(url, username, password);
        guestClient.checkFolderAccessible(Integer.toString(testFolder1.getObjectID()), guestPermission);
        guestClient.logout();

        /*
         * second invitation
         */
        inviteRequest = new InviteRequest();
        inviteRequest.addTarget(new ShareTarget(testFolder2.getModule(), Integer.toString(testFolder2.getObjectID())));
        inviteRequest.addRecipient(guestPermission.getRecipient());
        client.execute(inviteRequest);

        messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        message = messages.get(0);

        /*
         * assert magic headers and content
         */
        headers = message.getHeaders();
        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
        url = headers.get("X-Open-Xchange-Share-URL");
        assertNotNull(url);
        assertNull(headers.get("X-Open-Xchange-Share-Access"));

        /*
         * re-login with existing credentials
         */
        guestClient = new GuestClient(url, username, password);
        guestClient.checkFolderAccessible(Integer.toString(testFolder2.getObjectID()), guestPermission);
        guestClient.logout();
    }

    /**
     * Get a password-secured link and distribute it via notify action
     */
    public void testNotifyAnonymousWithPassword() throws Exception {
        /*
         * get link
         */
        String password = PasswordUtility.generate();
        OCLGuestPermission permission = createAnonymousAuthorPermission(password);
        GetLinkRequest getLinkRequest = new GetLinkRequest(Collections.singletonList(new ShareTarget(testFolder1.getModule(), Integer.toString(testFolder1.getObjectID()))));
        getLinkRequest.setBits(permission.getPermissionBits());
        getLinkRequest.setPassword(password);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);

        /*
         * notify
         */
        String textMessage = randomUID();
        NotifyRequest notifyRequest = new NotifyRequest(getLinkResponse.getToken(), textMessage + "@example.com");
        notifyRequest.setMessage(textMessage);
        client.execute(notifyRequest);
        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);

        /*
         * assert magic headers and content
         */
        Map<String, String> headers = message.getHeaders();
        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
        String url = headers.get("X-Open-Xchange-Share-URL");
        assertNotNull(url);
        String access = headers.get("X-Open-Xchange-Share-Access");
        assertNotNull(access);
        String receivedPassword = new String(Base64.decode(access), Charset.forName("UTF-8"));
        assertEquals(password, receivedPassword);

        String plainText = message.getPlainText();
        assertNotNull(plainText);
        assertTrue(plainText.contains(receivedPassword));
        assertTrue(plainText.contains(textMessage));

        /*
         * check received link and credentials
         */
        GuestClient guestClient = new GuestClient(url, null, receivedPassword);
        guestClient.checkFolderAccessible(Integer.toString(testFolder1.getObjectID()), permission);
        guestClient.logout();
    }

}
