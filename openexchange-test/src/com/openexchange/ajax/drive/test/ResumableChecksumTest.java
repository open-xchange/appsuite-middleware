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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.drive.DriveProperty;
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
@RunWith(BlockJUnit4ClassRunner.class)
public class ResumableChecksumTest extends AbstractConfigAwareAPIClientSession {

    @Rule
    public Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

    private DriveApi driveApi;
    private String folderId;
    private String privateInfostoreFolder;
    private final List<String> folders = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * prepare config
         */
        CONFIG.put(DriveProperty.OPTIMISTIC_SAVE_THRESHOLD_DESKTOP.getFQPropertyName(), "1kB");
        CONFIG.put(DriveProperty.OPTIMISTIC_SAVE_THRESHOLD_MOBILE.getFQPropertyName(), "1kB");
        super.setUpConfiguration();
        /*
         * set up
         */
        driveApi = new DriveApi(getApiClient());
        String folderTitle = "ResumableChecksumFolder_" + UUID.randomUUID().toString();
        folderId = createFolderForTest(folderTitle);
        rememberFolder(folderId);
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
        String newName = "testResumableChecksum_UploadWith3EmptyChunks_Successful.txt";
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
        File downloadFile = driveApi.downloadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, null, L(0), L(-1), null, downloadBody);
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
        String newName = "testResumableChecksum_UploadWithFilledChunks_Successful.txt";
        byte[] testData1 = { 76, 111, 114, 101, 109, 32, 105, 112, 115, 117, 109, 32, 100, 111, 108, 111, 114, 32, 115, 105, 116, 32, 97, 109, 101 };
        byte[] testData2 = { 83, 101, 100, 32, 117, 116, 32, 112, 101, 114, 115, 112, 105, 99, 105, 97, 116, 105, 115, 32, 117, 110, 100, 101, 32 };
        int testDataLength = 25;
        int chunksize = 1500;
        byte[] body1 = new byte[chunksize];
        byte[] body2 = new byte[chunksize];
        for (int i = 0; i < chunksize; i = i + testDataLength) {
            System.arraycopy(testData1, 0, body1, i, testDataLength);
            System.arraycopy(testData2, 0, body2, i, testDataLength);
        }
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
        File downloadFile = driveApi.downloadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, null, L(0), L(-1), null, downloadBody);
        try (FileInputStream in = new FileInputStream(downloadFile)) {
            byte[] downloadArray = IOUtils.toByteArray(in);
            assertEquals(l(totalLength), downloadFile.length());
            assertEquals(newChecksum, getChecksum(downloadArray));
        }
    }

    /**
     *
     * Tries to upload an array in two chunks, that are smaller then {@link DriveProperty#OPTIMISTIC_SAVE_THRESHOLD_DESKTOP}
     * or {@link DriveProperty#OPTIMISTIC_SAVE_THRESHOLD_MOBILE}. After every try an error is expected.
     *
     * @throws NoSuchAlgorithmException if algorithm 'MD5' is not known in jonelo.jacksum.algorithm.MD.MD
     * @throws ApiException if fails to make API call
     */
    @Test
    public void testResumableChecksum_TooSmallChunkSize_Failure() throws NoSuchAlgorithmException, ApiException {
        String newName = "testResumableChecksum_TooSmallChunkSize_Failure.txt";
        int chunksize = 500;
        byte[] body = new byte[chunksize];
        Long totalLength = L(2 * chunksize);
        Long offset = L(0);
        String newChecksum = getChecksum(new byte[2 * chunksize]);

        DriveUploadResponse uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body, null, null, null, null, offset, totalLength, null, null, null, null, null);
        assertThat("Checksum for the first chunk must not be equal total checksum", uploadFile.getError(), startsWith("Integrity checks failed"));
        offset = L(chunksize);
        uploadFile = driveApi.uploadFile(getApiClient().getSession(), folderId, "/", newName, newChecksum, body, null, null, null, null, offset, totalLength, null, null, null, null, null);
        assertThat("Upload must not be resumed", uploadFile.getError(), startsWith("Unable to access the file at the requested position"));
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
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, body, "1", null, null, null);
        return checkResponse(folderUpdateResponse);
    }

    private void rememberFolder(String folder) {
        folders.add(folder);
    }

    private String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(getApiClient());
            ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
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

    // -------------------------   prepare config --------------------------------------

    private static final Map<String, String> CONFIG = new HashMap<String, String>();

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }
}
