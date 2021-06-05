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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link MWB933Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class MWB933Test extends ShareTest {

    /**
     * Initializes a new {@link MWB933Test}.
     *
     * @param name The test name
     */
    public MWB933Test() {
        super();
    }

    @Test
    public void testDeliveryView() throws Exception {
        /*
         * create folder
         */
        EnumAPI api = EnumAPI.OX_NEW;
        int parent = getDefaultFolder(FolderObject.INFOSTORE);
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        /*
         * create shared file in this folder
         */
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createAnonymousGuestPermission());
        DefaultFile metadata = new DefaultFile();
        metadata.setFolderId(String.valueOf(folder.getObjectID()));
        metadata.setFileName("image.html");
        metadata.setTitle("XCF + HTML");
        metadata.setFileMIMEType("image/x-xcf");
        metadata.setObjectPermissions(Collections.singletonList(guestPermission));
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        NewInfostoreResponse newResponse = getClient().execute(new NewInfostoreRequest(metadata, new ByteArrayInputStream(contents)));
        assertFalse(newResponse.getErrorMessage(), newResponse.hasError());
        String id = newResponse.getID();
        metadata.setId(id);
        /*
         * check permissions
         */
        GetInfostoreResponse getResponse = getClient().execute(new GetInfostoreRequest(id));
        File createdFile = getResponse.getDocumentMetadata();
        assertNotNull(createdFile);
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : createdFile.getObjectPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(createdFile.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share (via guest client)
         */
        String shareURL = discoverShareURL(testUser.getApiClient(), guest);
        GuestClient guestClient = resolveShare(shareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * prepare basic http client to access file directly
         */
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        /*
         * check direct download with dl=1 *and* delivery=view (and derivates)
         */
        HttpGet httpGet = new HttpGet(shareURL + "?dl=1&delivery=view");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
            /*
             * check content disposition
             */
            Header disposition = httpResponse.getFirstHeader("Content-Disposition");
            assertTrue("Wrong content disposition", null != disposition && null != disposition.getValue() && disposition.getValue().startsWith("attachment"));
            HttpEntity entity = httpResponse.getEntity();
            assertNotNull("No file downloaded", entity);
            byte[] downloadedFile = EntityUtils.toByteArray(entity);
            Assert.assertArrayEquals("Different contents downloaded", contents, downloadedFile);
        } else {
            /*
             * alternatively, check for redirect to app suite ui
             */
            assertEquals("Wrong HTTP status", HttpStatus.SC_MOVED_TEMPORARILY, httpResponse.getStatusLine().getStatusCode());
        }
    }

}
