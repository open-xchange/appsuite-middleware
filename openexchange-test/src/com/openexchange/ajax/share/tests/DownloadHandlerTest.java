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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link DownloadHandlerTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadHandlerTest extends ShareTest {

    /**
     * Initializes a new {@link DownloadHandlerTest}.
     *
     * @param name The test name
     */
    public DownloadHandlerTest(String name) {
        super(name);
    }

    public void testDownloadSharedFileRandomly() throws Exception {
        testDownloadSharedFile(randomFolderAPI(), randomGuestObjectPermission());
    }

    public void noTestDownloadSharedFileExtensively() throws Exception {
        for (FileStorageGuestObjectPermission guestPermission : TESTED_OBJECT_PERMISSIONS) {
            testDownloadSharedFile(EnumAPI.OX_NEW, guestPermission);
        }
    }

    private void testDownloadSharedFile(EnumAPI api, FileStorageGuestObjectPermission guestPermission) throws Exception {
        testDownloadSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
    }

    private void testDownloadSharedFile(EnumAPI api, int parent, FileStorageGuestObjectPermission guestPermission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share (via guest client)
         */
        String shareURL = discoverShareURL(guest);
        GuestClient guestClient = resolveShare(shareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * prepare basic http client to access file directly
         */
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        String password = getPassword(guestPermission.getRecipient());
        if (null != password) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                getUsername(guestPermission.getRecipient()), getPassword(guestPermission.getRecipient()));
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
            httpClient.setCredentialsProvider(credentialsProvider);
        }
        /*
         * check direct download
         */
        for (String queryParameter : new String[] { "delivery=download", "dl=1", "dl=true" }) {
            HttpGet httpGet = new HttpGet(shareURL + '?' + queryParameter);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            assertEquals("Wrong HTTP status", 200, httpResponse.getStatusLine().getStatusCode());
            Header disposition = httpResponse.getFirstHeader("Content-Disposition");
            assertTrue("Wrong content disposition",
                null != disposition && null != disposition.getValue() && disposition.getValue().startsWith("attachment"));
            HttpEntity entity = httpResponse.getEntity();
            assertNotNull("No file downloaded", entity);
            byte[] downloadedFile = EntityUtils.toByteArray(entity);
            Assert.assertArrayEquals("Different contents downloaded", contents, downloadedFile);
        }
        /*
         * check inline delivery
         */
        for (String queryParameter : new String[] { "delivery=view", "raw=1", "raw=true" }) {
            HttpGet httpGet = new HttpGet(shareURL + '?' + queryParameter);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            assertEquals("Wrong HTTP status", 200, httpResponse.getStatusLine().getStatusCode());
            Header disposition = httpResponse.getFirstHeader("Content-Disposition");
            assertTrue("Wrong content disposition",
                null != disposition && null != disposition.getValue() && disposition.getValue().startsWith("inline"));
            HttpEntity entity = httpResponse.getEntity();
            assertNotNull("No file downloaded", entity);
            byte[] downloadedFile = EntityUtils.toByteArray(entity);
            Assert.assertArrayEquals("Different contents downloaded", contents, downloadedFile);
        }

    }

}
