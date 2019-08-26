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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static com.openexchange.java.Autoboxing.I;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.test.FolderTestManager;
import com.openexchange.testing.httpclient.models.DriveActionsResponse;
import com.openexchange.testing.httpclient.models.DriveExtendedActionsResponse;
import com.openexchange.testing.httpclient.models.DriveSettingsResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFilesBody;
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
        DriveExtendedActionsResponse response = driveApi.syncFiles(apiClient.getSession(), folderId, "/", body, I(8), null, null, null, null, null, null);
        assertNotNull(response);
        assertNotNull(response.getData());
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_syncFoldersRequest() throws Exception {
        String folderName = "PathToRootTest_syncFoldersRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse response = driveApi.syncFolders(apiClient.getSession(), folderId, body, I(8), null, null, null, null);
        assertNotNull(response);
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_syncFolderRequest() throws Exception {
        String folderName = "PathToRootTest_syncFolderRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse response = driveApi.syncFolders(apiClient.getSession(), folderId, body, I(8), null, null, null, null);
        assertNotNull(response);
        checkResponse(response, folderName);
    }

    @Test
    public void testGetPathToRoot_settingsRequest() throws Exception {
        String folderName = "PathToRootTest_settingsRequest_" + UUID.randomUUID().toString();
        String folderId = createFolderForTest(folderName);
        DriveSettingsResponse response = driveApi.getSettings(apiClient.getSession(), folderId, I(8), null);
        assertNotNull(response);
        String pathToRoot = response.getData().getPathToRoot();
        assertTrue(Strings.isNotEmpty(pathToRoot));
        assertTrue(pathToRoot.startsWith(realRootPath));
        assertEquals(folderName, pathToRoot.substring(realRootPath.length() + 1));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.driveApi = new DriveApi(apiClient);
        folderTestManager = new FolderTestManager(getClient());
        this.realRootFolderId = getClient().getValues().getPrivateInfostoreFolder();
        this.userId = getClient().getValues().getUserId();
        this.realRootPath = getRealRootPath();
    }

    @Override
    public void tearDown() throws Exception {
        folderTestManager.cleanUp();
        super.tearDown();
    }

    private String getRealRootPath() throws Exception {
        DriveSettingsResponse resp = driveApi.getSettings(apiClient.getSession(), String.valueOf(realRootFolderId), I(8), null);
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

    private void checkResponse(DriveActionsResponse response, String expectedFolderName) {
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
