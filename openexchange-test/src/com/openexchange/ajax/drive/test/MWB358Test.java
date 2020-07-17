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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.infostore.apiclient.InfostoreApiClientTest;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.DriveAction;
import com.openexchange.testing.httpclient.models.DriveDirectoryVersion;
import com.openexchange.testing.httpclient.models.DriveSyncFolderBody;
import com.openexchange.testing.httpclient.models.DriveSyncFolderResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.TrashContent;
import com.openexchange.testing.httpclient.models.TrashFolderResponse;
import com.openexchange.testing.httpclient.models.TrashTargetsBody;
import com.openexchange.testing.httpclient.modules.DriveApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * 
 * {@link MWB358Test}
 * 
 * Tests for MWB358: Deletion of shared file was not detected during OX Drive synchronization.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public class MWB358Test extends InfostoreApiClientTest {

    private static final String DEFAULT_CHECKSUM = "341833daf98ddcb454279c4975ff0419";

    private static final String SHARED_FOLDER_ID = "10";

    private static final String TESTFOLDER_CHECKSUM = "612caec919864455f4664160425550a8";

    private DriveApi driveApiUser1;

    private DriveApi driveApiUser2;

    private FoldersApi foldersApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        driveApiUser1 = new DriveApi(getApiClient());
        driveApiUser2 = new DriveApi(getApiClient2());
        foldersApi = new FoldersApi(getApiClient());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * 
     * Tests if a sync folder action indicates an action to sync the shared folder,
     * when a shared file is deleted and removed from trash.
     *
     * @throws Exception
     */
    @Test
    public void testMWB358DeleteSharedFileAndTrash() throws Exception {
        String file1 = createShareAndSyncTwoFiles();
        deleteFile(file1);
        emptyTrash();
        checkForSyncAction();
    }

    /**
     * 
     * Tests if a sync folder action indicates an action to sync the shared folder,
     * when the folder with a shared file inside is deleted.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteFolderWithSharedFile() throws ApiException {
        createShareAndSyncTwoFiles();
        FoldersCleanUpResponse deleteFoldersResponse = foldersApi.deleteFolders(getApiClient().getSession(), Collections.singletonList(folderId), "1", L(new Date().getTime()), null, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null);
        assertNull(deleteFoldersResponse.getErrorDesc(), deleteFoldersResponse.getError());
        checkForSyncAction();
    }

    /**
     * 
     * Tests if a sync folder action indicates an action to sync the shared folder,
     * when a shared file is deleted without removing it from trash.
     * 
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     * @throws Exception
     */
    @Test
    public void testMoveSharedDocumentToTrash() throws ApiException, OXException, IOException, JSONException {
        String file1 = createShareAndSyncTwoFiles();
        deleteFile(file1);
        rememberFile(file1, getClient().getValues().getTrashFolder());
        checkForSyncAction();
    }

    /**
     * 
     * Tests if a sync folder action indicates an action to sync the shared folder,
     * when the sharing permission of a shared file is removed.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveSharePermission() throws ApiException {
        String file1 = createShareAndSyncTwoFiles();
        removeSharingPermission(file1);
        checkForSyncAction();
    }

    /**
     * 
     * Checks, if the sync folder action of shared folder from user 2 delivers a sync action.
     *
     * @throws ApiException
     */
    private void checkForSyncAction() throws ApiException {
        List<DriveAction> actions = syncFolder(getApiClient2(), SHARED_FOLDER_ID, TESTFOLDER_CHECKSUM, TESTFOLDER_CHECKSUM);
        assertEquals(1, actions.size());
        assertEquals("sync", actions.get(0).getAction());
    }

    /**
     * 
     * Creates a file with the given file name, owner and shared user.
     *
     * @param sharedToUserId The if of the user for whom the file is shared.
     * @param fileName The name of the file.
     * @return An entry with the object id of the file.
     * @throws ApiException
     */
    private String createFile(Integer sharedToUserId, String fileName) throws ApiException {
        InfoItemUpdateResponse uploadResponse = infostoreApi.uploadInfoItem(getApiClient().getSession(), folderId, fileName, new byte[] { 34, 45, 35, 23 }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertNotNull(uploadResponse);
        assertNull(uploadResponse.getErrorDesc(), uploadResponse.getError());
        String id = uploadResponse.getData();
        timestamp = uploadResponse.getTimestamp();
        if (sharedToUserId != null) {
            InfoItemPermission objectPermission = new InfoItemPermission();
            objectPermission.setEntity(sharedToUserId);
            objectPermission.setGroup(Boolean.FALSE);
            objectPermission.setBits(InfoItemPermission.BitsEnum.NUMBER_1);
            updatePermissions(id, Collections.singletonList(objectPermission));
        }
        return id;
    }

    /**
     * 
     * Creates two file for user 1, that are shared with user 2, and checks the sync of shared folder from user 2.
     *
     * @return An entry containing the object id of file 1.
     * @throws ApiException
     */
    private String createShareAndSyncTwoFiles() throws ApiException {
        /*
         * Share two files to user 2
         */
        String file1 = createFile(getApiClient2().getUserId(), "MWB358Test1.txt");
        createFile(getApiClient2().getUserId(), "MWB358Test2.txt");

        /*
         * sync folder 10 of user 2
         */
        List<DriveAction> actions = syncFolder(getApiClient2(), SHARED_FOLDER_ID, DEFAULT_CHECKSUM, DEFAULT_CHECKSUM);
        assertEquals(1, actions.size());
        assertEquals("sync", actions.get(0).getAction());

        actions = syncFolder(getApiClient2(), SHARED_FOLDER_ID, TESTFOLDER_CHECKSUM, DEFAULT_CHECKSUM);
        assertEquals(2, actions.size());
        assertEquals("acknowledge", actions.get(0).getAction());

        actions = syncFolder(getApiClient2(), SHARED_FOLDER_ID, TESTFOLDER_CHECKSUM, TESTFOLDER_CHECKSUM);
        assertEquals(0, actions.size());
        return file1;
    }

    /**
     * 
     * Deletes a file with the given object id.
     *
     * @param objectId The object id of the file.
     * @throws ApiException
     */
    private void deleteFile(String objectId) throws ApiException {
        InfoItemListElement item = new InfoItemListElement();
        item.setId(objectId);
        item.setFolder(folderId);
        deleteInfoItems(Collections.singletonList(item), Boolean.FALSE);
    }

    /**
     * 
     * Empties the trash folder.
     *
     * @throws ApiException
     */
    private void emptyTrash() throws ApiException {
        String infostoreFolder = getPrivateInfostoreFolder();

        TrashFolderResponse trashContent = driveApiUser1.getTrashContent(getApiClient().getSession(), infostoreFolder);
        TrashContent data = trashContent.getData();

        TrashTargetsBody body = new TrashTargetsBody();
        if (data != null && data.getFiles() != null && data.getFiles().isEmpty() == false) {
            body.addFilesItem(data.getFiles().get(0).getName());
            TrashFolderResponse removeFromTrash = driveApiUser1.deleteFromTrash(getApiClient().getSession(), infostoreFolder, body);
            assertNull(removeFromTrash.getErrorDesc(), removeFromTrash.getError());
        }
    }

    /**
     * 
     * Removes the permissions of the given file.
     *
     * @param objectId the id of the file.
     * @throws ApiException
     */
    private void removeSharingPermission(String objectId) throws ApiException {
        updatePermissions(objectId, new ArrayList<InfoItemPermission>());
    }

    /**
     * 
     * Calls the drive sync folder action with the given parameters.
     *
     * @param apiClient The api client of the user for whom the sync action should be executed.
     * @param folderId The folder id of the folder to sync.
     * @param clientChecksum The actual checksum of the folder (calculated by drive client).
     * @param originalChecksum The former checksum of the folder (calculated by drive client).
     * @return A list of the returned drive actions.
     * @throws ApiException
     */
    private List<DriveAction> syncFolder(ApiClient apiClient, String folderId, String clientChecksum, String originalChecksum) throws ApiException {
        DriveSyncFolderBody driveSyncFolderBody = new DriveSyncFolderBody();

        DriveDirectoryVersion originalVersion = new DriveDirectoryVersion();
        originalVersion.setPath("/");
        originalVersion.setChecksum(originalChecksum);
        driveSyncFolderBody.setOriginalVersion(originalVersion);

        DriveDirectoryVersion clientVersion = new DriveDirectoryVersion();
        clientVersion.setPath("/");
        clientVersion.setChecksum(clientChecksum);
        driveSyncFolderBody.setClientVersion(clientVersion);

        DriveSyncFolderResponse syncFolderResponse = driveApiUser2.syncFolder(apiClient.getSession(), folderId, driveSyncFolderBody, I(2), null, null, null, null);
        assertNotNull(syncFolderResponse);
        assertNull(syncFolderResponse.getErrorDesc(), syncFolderResponse.getError());
        assertNotNull(syncFolderResponse.getData());
        assertNotNull(syncFolderResponse.getData().getActions());
        return syncFolderResponse.getData().getActions();
    }
}
