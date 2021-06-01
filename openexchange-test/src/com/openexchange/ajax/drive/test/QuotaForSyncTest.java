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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.file.storage.Quota;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.DriveAction;
import com.openexchange.testing.httpclient.models.DriveExtendedActionsResponse;
import com.openexchange.testing.httpclient.models.DriveQuota;
import com.openexchange.testing.httpclient.models.DriveQuotaResponse;
import com.openexchange.testing.httpclient.models.DriveSubfoldersResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFilesBody;
import com.openexchange.testing.httpclient.models.DriveSyncFolderResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFoldersBody;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.DriveApi;

/**
 *
 * {@link QuotaForSyncTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class QuotaForSyncTest extends AbstractAPIClientSession {

    private DriveApi driveApi;
    private String infostoreFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        driveApi = new DriveApi(getApiClient());
        infostoreFolder = getPrivateInfostoreFolder();
    }

    private String getPrivateInfostoreFolder() throws Exception {
        ConfigApi configApi = new ConfigApi(getApiClient());
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
        return (configNode.getData()).toString();
    }

    @Test
    public void testSyncFolders_quotaNotSent() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse syncFolders = driveApi.syncFolders(getSessionId(), infostoreFolder, body, I(2), "2", null, null, null);

        assertNull(syncFolders.getError());
        for (DriveAction action : syncFolders.getData().getActions()) {
            assertTrue(action.getAction().equals("sync"));
        }
        assertTrue(syncFolders.getData().getActions().size() > 2);
    }

    @Test
    public void testSyncFolders_quotaFalse() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse syncFolders = driveApi.syncFolders(getSessionId(), infostoreFolder, body, I(2), "2", null, Boolean.FALSE, null);

        assertNull(syncFolders.getError());
        for (DriveAction action : syncFolders.getData().getActions()) {
            assertTrue(action.getAction().equals("sync"));
        }
        assertTrue(syncFolders.getData().getActions().size() > 2);
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFolders_quotaRequested() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse syncFolders = driveApi.syncFolders(getSessionId(), infostoreFolder, body, I(2), "2", null, Boolean.TRUE, null);
        assertNull(syncFolders.getError());
        //        assertNotNull(syncFolders.getData().getQuota());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFolders_quotaRequestedButWrongVersion_noQuotaReturned() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveSyncFolderResponse syncFolders = driveApi.syncFolders(getSessionId(), infostoreFolder, body, null, null, null, Boolean.TRUE, null);
        assertNull(syncFolders.getError());
        //        assertNull(syncFolders.getData().getQuota());
    }

    @Test
    public void testSyncFiles_quotaSent() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(getSessionId(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(getSessionId(), infostoreFolder, path, body, I(2), null, null, Boolean.TRUE, null, null, null);

        assertNull(syncFiles.getError());
        assertFalse(syncFiles.getData().getQuota().isEmpty());
        assertTrue(syncFiles.getData().getActions().isEmpty());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaNotSent() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(getSessionId(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(getSessionId(), infostoreFolder, path, body, I(2), null, null, null, null, null, null);

        assertNull(syncFiles.getError());
        assertTrue(syncFiles.getData().getActions().isEmpty());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaFalse() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(getSessionId(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(getSessionId(), infostoreFolder, path, body, I(2), null, null, Boolean.FALSE, null, null, null);

        assertNull(syncFiles.getError());
        assertTrue(syncFiles.getData().getActions().isEmpty());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaRequested() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(getSessionId(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(getSessionId(), infostoreFolder, path, body, I(2), null, null, Boolean.TRUE, null, null, null);
        assertNull(syncFiles.getError());
        //        assertNotNull(syncFiles.getData().getQuota());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaRequestedButWrongVersion_noQuotaReturned() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(getSessionId(), infostoreFolder, null, body, null, null, null, Boolean.TRUE, null, null, null);
        assertNull(syncFiles.getError());
        //      assertNull(syncFiles.getData().getQuota());
    }

    @Test
    public void testQuota() throws ApiException {
        DriveQuotaResponse quotaResponse = driveApi.getQuota(getSessionId(), infostoreFolder);

        List<DriveQuota> quota = quotaResponse.getData().getQuota();
        assertEquals(2, quota.size());
        boolean foundStorage = false;
        boolean foundFile = false;
        for (DriveQuota driveQuota : quota) {
            if (driveQuota.getType().equalsIgnoreCase(Quota.Type.FILE.toString())) {
                foundFile = true;
            } else if (driveQuota.getType().equalsIgnoreCase(Quota.Type.STORAGE.toString())) {
                foundStorage = true;
            }
        }
        assertTrue(foundStorage);
        assertTrue(foundFile);

        String manageLink = quotaResponse.getData().getManageLink();
        assertTrue(Strings.isNotEmpty(manageLink));
    }
}
