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
