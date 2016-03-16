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

package com.openexchange.ajax.share.tests;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
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
    private ExtendedPermissionEntity guest;
    private FolderObject folder;
    private String shareURL;

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

        OCLGuestPermission lGuestPermission = createNamedGuestPermission(randomUID() + "@example.com", "Test Guest", "secret");
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        EnumAPI api = randomFolderAPI();
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
        ExtendedPermissionEntity lGuest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(lGuestPermission, lGuest);
        /*
         * check access to share
         */
        shareURL = discoverShareURL(lGuest);
        GuestClient guestClient = resolveShare(shareURL, ((GuestRecipient) lGuestPermission.getRecipient()).getEmailAddress(), ((GuestRecipient) lGuestPermission.getRecipient()).getPassword());
        guestClient.checkShareModuleAvailable();
        this.guest = lGuest;
        this.guestPermission = lGuestPermission;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testResetPassword_passwordReset() throws Exception {
        // http://localhost/ajax/share/1100ba1e0f0652b8849d7f3f066049e390589313a77026ef
        URI shareUrl = new URI(shareURL);
        String[] pathSegments = shareUrl.getPath().split("/");
        String token = null;
        for (String segment : pathSegments) {
            Matcher matcher = Pattern.compile("[a-f0-9]{48}", Pattern.CASE_INSENSITIVE).matcher(segment);
            if (matcher.matches()) {
                token = matcher.group();
                break;
            }
        }
        if (token == null) {
            fail("got no token from share link");
        }
        DefaultHttpClient httpClient = getSession().getHttpClient();
        // http://localhost/ajax/share/reset/password?share=1100ba1e0f0652b8849d7f3f066049e390589313a77026ef&confirm=FIMvTtnmQ7Dv_N97CRENJy6rTYw
        HttpGet getConfirmationMail = new HttpGet(new URIBuilder()
            .setScheme(client.getProtocol())
            .setHost(client.getHostname())
            .setPath("/ajax/share/reset/password")
            .setParameter("share", token)
            .build());
        HttpResponse getConfirmationMailResponse = httpClient.execute(getConfirmationMail);
        EntityUtils.consume(getConfirmationMailResponse.getEntity());

        PWResetData resetData = getConfirmationToken();
        HttpPost confirmPWReset = new HttpPost(new URIBuilder()
            .setScheme(client.getProtocol())
            .setHost(client.getHostname())
            .setPath("/ajax/share/reset/password")
            .build());
        String newPW = UUIDs.getUnformattedStringFromRandom();
        List<BasicNameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("share", resetData.shareToken));
        params.add(new BasicNameValuePair("confirm", resetData.confirmationToken));
        params.add(new BasicNameValuePair("password", newPW));
        confirmPWReset.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse confirmResponse = httpClient.execute(confirmPWReset);
        EntityUtils.consume(confirmResponse.getEntity());
        assertEquals("Response was no redirect", 302, confirmResponse.getStatusLine().getStatusCode());
        Header locationHeader = confirmResponse.getFirstHeader(HttpHeaders.LOCATION);
        assertNotNull("Missing location header", locationHeader);
        URI location = new URI(locationHeader.getValue());
        String[] kvPairs = location.getRawFragment().split("&");
        String sessionId = null;
        for (String pair : kvPairs) {
            String[] splitted = pair.split("=");
            if (splitted.length == 2) {
                if ("session".equals(splitted[0])) {
                    sessionId = URLDecoder.decode(splitted[1], "UTF-8");
                }
            }
        }

        assertNotNull("Missing session ID in redirect location", sessionId);

        // Login again to verify
        GuestClient guestClient = resolveShare(shareURL, ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress(), newPW);
        guestClient.checkShareAccessible(guestPermission);
        guestClient.logout();
    }

    private PWResetData getConfirmationToken() throws Exception {
        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        String url = message.getHeaders().get("X-Open-Xchange-Share-Reset-PW-URL");
        assertNotNull("Missing X-Open-Xchange-Share-Reset-PW-URL in confirmation mail", url);
        String query = new URI(url).getRawQuery();
        String[] kvPairs = query.split("&");
        PWResetData pwResetData = new PWResetData();
        for (String kvPair : kvPairs) {
            String[] kv = kvPair.split("=");
            if (kv.length == 2) {
                if ("confirm".equals(kv[0])) {
                    pwResetData.confirmationToken = URLDecoder.decode(kv[1], "UTF-8");
                } else if ("share".equals(kv[0])) {
                    pwResetData.shareToken = URLDecoder.decode(kv[1], "UTF-8");
                }
            }
        }

        assertNotNull("Cannot extract share token from URL: " + url, pwResetData.shareToken);
        assertNotNull("Cannot extract confirmation token from URL: " + url, pwResetData.confirmationToken);
        return pwResetData;
    }

    private static final class PWResetData {
        private String shareToken;
        private String confirmationToken;
    }

}
