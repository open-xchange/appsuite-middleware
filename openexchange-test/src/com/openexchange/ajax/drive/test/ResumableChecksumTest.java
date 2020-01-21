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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.DriveDownloadBody;
import com.openexchange.testing.httpclient.models.DriveUploadResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.DriveApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import jonelo.jacksum.algorithm.MD;

/**
 * {@link ResumableChecksumTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public class ResumableChecksumTest extends AbstractAPIClientSession {

    private DriveApi driveApi;
    private String folderId;
    private String privateInfostoreFolder;
    private List<String> folders = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        driveApi = new DriveApi(getApiClient());

        String folderTitle = "ResumableChecksumFolder_" + UUID.randomUUID().toString();
        folderId = createFolderForTest(folderTitle);
        rememberFolder(folderId);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (!folders.isEmpty()) {
                FoldersApi folderApi = new FoldersApi(getApiClient());
                folderApi.deleteFolders(getApiClient().getSession(), folders, "1", L(new Date().getTime()), null, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null);
            }
        } finally {
            super.tearDown();
        }
    }

    /**
     * 
     * Uploads an empty byte array in three chunks, downloads the uploaded data,
     * and tests the total length and checksum of the data before and after the upload / download.
     *
     * @throws NoSuchAlgorithmException if algorithm 'MD5' is not known in jonelo.jacksum.algorithm.MD.MD
     * @throws ApiException if fails to make API call
     * @throws IOException if an I/O error occurs by parsing the download file the byte array
     */
    @Test
    public void testResumableChecksum_UploadWith3EmptyChunks_Successful() throws NoSuchAlgorithmException, ApiException, IOException {
        String newName = "testResumableChecksum_3chunks.txt";
        int chunksize = 2000;
        byte[] body = new byte[chunksize];
        Long totalLength = L(3 * chunksize);
        Long offset = L(0);
        String newChecksum = getChecksum(new byte[3 * chunksize]);

        DriveUploadResponse uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body, null, null, null, null, offset, totalLength, null, null, null, null, null);
        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());
        offset = L(chunksize);
        uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body, null, null, null, null, offset, totalLength, null, null, null, null, null);
        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());
        offset = L(2 * chunksize);
        uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body, null, null, null, null, offset, totalLength, null, null, null, null, null);

        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());

        /*
         * downloading the file to assert that checksum and length are equal with the uploaded file
         */
        DriveDownloadBody downloadBody = new DriveDownloadBody();
        File downloadFile = driveApi.downloadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, null, L(0), L(-1), downloadBody);
        try (FileInputStream in = new FileInputStream(downloadFile)) {
            byte[] downloadArray = IOUtils.toByteArray(in);
            assertEquals(l(totalLength), downloadFile.length());
            assertEquals(newChecksum, getChecksum(downloadArray));
        }
    }

    /**
     * 
     * Uploads a byte array filled with random bytes in two chunks, downloads the uploaded data,
     * and tests the total length and checksum of the data before and after the upload/download.
     *
     * @throws NoSuchAlgorithmException if algorithm 'MD5' is not known in jonelo.jacksum.algorithm.MD.MD
     * @throws ApiException if fails to make API call
     * @throws IOException if an I/O error occurs by parsing the download file the byte array
     */
    @Test
    public void testResumableChecksum_UploadWithFilledChunks_Successful() throws NoSuchAlgorithmException, ApiException, IOException {
        String newName = "testResumableChecksum_filledChunks.txt";
        int chunksize = 3000;
        byte[] body1 = new byte[chunksize];
        SecureRandom.getInstanceStrong().nextBytes(body1);
        byte[] body2 = new byte[chunksize];
        SecureRandom.getInstanceStrong().nextBytes(body2);
        byte[] bodyComplete = new byte[2 * chunksize];
        System.arraycopy(body1, 0, bodyComplete, 0, body1.length);
        System.arraycopy(body2, 0, bodyComplete, body1.length, body2.length);
        Long totalLength = L(bodyComplete.length);
        String newChecksum = getChecksum(bodyComplete);
        Long offset = L(0);

        DriveUploadResponse uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body1, null, null, null, null, offset, totalLength, null, null, null, null, null);
        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());
        offset = L(chunksize);
        uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body2, null, null, null, null, offset, totalLength, null, null, null, null, null);

        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());

        /*
         * downloading the file to assert that checksum and length are equal with the uploaded file
         */
        DriveDownloadBody downloadBody = new DriveDownloadBody();
        File downloadFile = driveApi.downloadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, null, L(0), L(-1), downloadBody);
        try (FileInputStream in = new FileInputStream(downloadFile)) {
            byte[] downloadArray = IOUtils.toByteArray(in);
            assertEquals(l(totalLength), downloadFile.length());
            assertEquals(newChecksum, getChecksum(downloadArray));
        }
    }

    private String getChecksum(byte[] bytes) throws NoSuchAlgorithmException {
        MD md5 = new MD("MD5");
        md5.update(bytes);
        return md5.getFormattedValue();
    }

    private String createFolderForTest(String title) throws ApiException {
        final String parent = getPrivateInfostoreFolder();
        FoldersApi folderApi = new FoldersApi(getApiClient());
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary(title);
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, getApiClient().getSession(), body, "1", null, null);
        return checkResponse(folderUpdateResponse);
    }

    private void rememberFolder(String folder) {
        folders.add(folder);
    }

    private String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(getApiClient());
            ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), getApiClient().getSession());
            Object data = checkResponse(configNode);
            if (data != null && !data.toString().equalsIgnoreCase("null")) {
                privateInfostoreFolder = String.valueOf(data);
            } else {
                org.junit.Assert.fail("It seems that the user doesn't support drive.");
            }

        }
        return privateInfostoreFolder;
    }

    private Object checkResponse(ConfigResponse resp) {
        org.junit.Assert.assertNull(resp.getErrorDesc(), resp.getError());
        org.junit.Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private String checkResponse(FolderUpdateResponse resp) {
        org.junit.Assert.assertNull(resp.getErrorDesc(), resp.getError());
        org.junit.Assert.assertNotNull(resp.getData());
        return resp.getData();
    }
}
