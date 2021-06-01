/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
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
    public DownloadHandlerTest() {
        super();
    }

    @Test
    public void testDownloadSharedFileRandomly() throws Exception {
        testDownloadSharedFile(randomFolderAPI(), randomGuestPermission());
    }

    public void noTestDownloadSharedFileExtensively() throws Exception {
        for (GuestPermissionType permissionType : GuestPermissionType.values()) {
            OCLGuestPermission guestPermission = createGuestPermission(permissionType);
            testDownloadSharedFile(EnumAPI.OX_NEW, guestPermission);
        }
    }

    private void testDownloadSharedFile(EnumAPI api, OCLGuestPermission guestPermission) throws Exception {
        testDownloadSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
    }

    private void testDownloadSharedFile(EnumAPI api, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        FileStorageGuestObjectPermission guestObjectPermission = asObjectPermission(guestPermission);
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), filename, guestObjectPermission, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestObjectPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestObjectPermission, guest);
        /*
         * check access to share (via guest client)
         */
        String shareURL = discoverShareURL(guestPermission.getApiClient(), guest);
        GuestClient guestClient = resolveShare(shareURL, guestObjectPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestObjectPermission);
        /*
         * prepare basic http client to access file directly
         */
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        String password = getPassword(guestObjectPermission.getRecipient());
        if (null != password) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(guestObjectPermission.getRecipient()), getPassword(guestObjectPermission.getRecipient()));
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
            assertTrue("Wrong content disposition", null != disposition && null != disposition.getValue() && disposition.getValue().startsWith("attachment"));
            HttpEntity entity = httpResponse.getEntity();
            assertNotNull("No file downloaded", entity);
            byte[] downloadedFile = EntityUtils.toByteArray(entity);
            Assert.assertArrayEquals("Different contents downloaded", contents, downloadedFile);
        }
    }

}
