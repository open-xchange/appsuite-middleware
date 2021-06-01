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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.test.FolderTestManager;
import com.openexchange.testing.httpclient.models.DriveExtendedActionsResponse;
import com.openexchange.testing.httpclient.models.DriveSettingsResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFilesBody;
import com.openexchange.testing.httpclient.models.DriveSyncFolderResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFoldersBody;
import com.openexchange.testing.httpclient.modules.DriveApi;


/**
 * {@link PathToRootTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.3
 */
public class PathToRootTest extends AbstractAPIClientSession {

    private DriveApi driveApi;
    private FolderTestManager folderTestManager;
    private int realRootFolderId;
    private int userId;
    private String realRootPath;

    @Test
    public void testGetPathToRoot_syncFilesRequest() throws Exception {
        String folderName = "PathToRootTest_syncFilesRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveExtendedActionsResponse response = driveApi.syncFiles(getSessionId(), folderId, "/", body, I(8), null, null, null, null, null, null);
        assertNotNull(response);
        assertNotNull(response.getData());
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_syncFoldersRequest() throws Exception {
        String folderName = "PathToRootTest_syncFoldersRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse response = driveApi.syncFolders(getSessionId(), folderId, body, I(8), null, null, null, null);
        assertNotNull(response);
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_syncFolderRequest() throws Exception {
        String folderName = "PathToRootTest_syncFolderRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse response = driveApi.syncFolders(getSessionId(), folderId, body, I(8), null, null, null, null);
        assertNotNull(response);
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_settingsRequest() throws Exception {
        String folderName = "PathToRootTest_settingsRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSettingsResponse response = driveApi.getSettings(getSessionId(), folderId, I(8), null);
        assertNotNull(response);
        String pathToRoot = response.getData().getPathToRoot();
        assertTrue(Strings.isNotEmpty(pathToRoot));
        assertTrue(pathToRoot.startsWith(realRootPath));
        assertEquals(folderName, pathToRoot.substring(realRootPath.length() + 1));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.driveApi = new DriveApi(getApiClient());
        folderTestManager = new FolderTestManager(getClient());
        this.realRootFolderId = getClient().getValues().getPrivateInfostoreFolder();
        this.userId = getClient().getValues().getUserId();
        this.realRootPath = getRealRootPath();
    }

    private String getRealRootPath() throws Exception {
        DriveSettingsResponse resp = driveApi.getSettings(getSessionId(), String.valueOf(realRootFolderId), I(8), null);
        assertNotNull(resp);
        assertNotNull(resp.getData());
        return resp.getData().getPathToRoot();
    }

    private String stripSlash(String path) {
        if (Strings.isNotEmpty(path)) {
            if (path.startsWith("/")) {
                return path.substring(1);
            }
        }
        return path;
    }

    private void checkResponse(DriveSyncFolderResponse response, String expectedFolderName) {
        assertNotNull(response.getData());
        String pathToRoot = response.getData().getPathToRoot();
        assertTrue(Strings.isNotEmpty(pathToRoot));
        assertTrue(pathToRoot.startsWith(realRootPath));
        String subPath = stripSlash(pathToRoot.substring(realRootPath.length()));
        assertEquals(expectedFolderName, subPath);
    }

    private void checkResponse(DriveExtendedActionsResponse response, String expectedFolderName) {
        assertNotNull(response.getData());
        String pathToRoot = response.getData().getPathToRoot();
        assertTrue(Strings.isNotEmpty(pathToRoot));
        assertTrue(pathToRoot.startsWith(realRootPath));
        String subPath = stripSlash(pathToRoot.substring(realRootPath.length()));
        assertEquals(expectedFolderName, subPath);
    }

    private String createFolderForTest(String folderName) {
        FolderObject generated = folderTestManager.generatePublicFolder(folderName, FolderObject.INFOSTORE, realRootFolderId, userId);
        generated = folderTestManager.insertFolderOnServer(generated);
        assertNotNull(generated);
        return String.valueOf(generated.getObjectID());
    }

}
