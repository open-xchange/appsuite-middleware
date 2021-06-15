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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertNotNull;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;
import com.openexchange.testing.httpclient.models.DriveDownloadBody;
import com.openexchange.testing.httpclient.modules.DriveApi;

/**
 * {@link MWB1058Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v8.0.0
 */
public class MWB1058Test extends AbstractAPIClientSession {

    private final long FILE_SIZE = 1680771L;
    private final String FILE_NAME = "testData/oxlogo.png";
    private final String FILE_MD5 = "ab2c2da86c805c5be7165072626aff92";

    private DriveApi driveApi;
    private FolderTestManager folderTestManager;
    private InfostoreTestManager infostoreTestManager;
    private FolderObject testFolder;
    private File testFile;
    private int userId;
    private int driveRootFolderId;

    @Test
    public void testSyncFileWithDashes() throws Exception {
        testFile = InfostoreTestManager.createFile(testFolder.getObjectID(), "MWB-1058-Test-File" + UUID.randomUUID().toString(), "text/plain");
        infostoreTestManager.newAction(testFile, new java.io.File(FILE_NAME));
        DriveDownloadBody body = new DriveDownloadBody();
        java.io.File file = driveApi.downloadFile(getSessionId(), String.valueOf(driveRootFolderId), "/" + testFolder.getFolderName(), testFile.getFileName(), FILE_MD5, I(8), L(0), L(FILE_SIZE), Boolean.TRUE, body);
        assertNotNull(file);
    }

    @Test
    public void testSyncFileWithSpaces() throws Exception {
        testFile = InfostoreTestManager.createFile(testFolder.getObjectID(), "MWB 1058 Test File" + UUID.randomUUID().toString(), "text/plain");
        infostoreTestManager.newAction(testFile, new java.io.File(FILE_NAME));
        DriveDownloadBody body = new DriveDownloadBody();
        java.io.File file = driveApi.downloadFile(getSessionId(), String.valueOf(driveRootFolderId), "/" + testFolder.getFolderName(), testFile.getFileName(), FILE_MD5, I(8), L(0), L(FILE_SIZE), Boolean.TRUE, body);
        assertNotNull(file);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userId = getClient().getValues().getUserId();
        driveRootFolderId = getClient().getValues().getPrivateInfostoreFolder();
        this.driveApi = new DriveApi(getApiClient());
        folderTestManager = new FolderTestManager(getClient());
        infostoreTestManager = new InfostoreTestManager(getClient());
        testFolder = folderTestManager.generatePrivateFolder("MWB1058Test_" + UUID.randomUUID().toString(), FolderObject.INFOSTORE, driveRootFolderId, userId);
        testFolder = folderTestManager.insertFolderOnServer(testFolder);
    }

    @Override
    public void tearDown() throws Exception {
        if (null != folderTestManager && null != testFolder) {
            folderTestManager.deleteFolderOnServer(testFolder);
        }
        super.tearDown();
    }

}
