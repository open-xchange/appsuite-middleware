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

import static org.junit.Assert.assertEquals;
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
import com.openexchange.testing.httpclient.models.DriveActionsResponse;
import com.openexchange.testing.httpclient.models.DriveExtendedActionsResponse;
import com.openexchange.testing.httpclient.models.DriveQuota;
import com.openexchange.testing.httpclient.models.DriveQuotaResponse;
import com.openexchange.testing.httpclient.models.DriveSubfoldersResponse;
import com.openexchange.testing.httpclient.models.DriveSyncFilesBody;
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
        driveApi = new DriveApi(apiClient);
        infostoreFolder = getPrivateInfostoreFolder();
    }

    private String getPrivateInfostoreFolder() throws Exception {
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), apiClient.getSession());
        return ((Integer) configNode.getData()).toString();
    }

    @Test
    public void testSyncFolders_quotaNotSent() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse syncFolders = driveApi.syncFolders(apiClient.getSession(), infostoreFolder, body, "2", 2, null, null, null);

        assertNull(syncFolders.getError());
        for (DriveAction action : syncFolders.getData()) {
            assertTrue(action.getAction().equals("sync"));
        }
        assertTrue(syncFolders.getData().size() > 2);
    }

    @Test
    public void testSyncFolders_quotaFalse() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse syncFolders = driveApi.syncFolders(apiClient.getSession(), infostoreFolder, body, "2", 2, null, Boolean.FALSE, null);

        assertNull(syncFolders.getError());
        for (DriveAction action : syncFolders.getData()) {
            assertTrue(action.getAction().equals("sync"));
        }
        assertTrue(syncFolders.getData().size() > 2);
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFolders_quotaRequested() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse syncFolders = driveApi.syncFolders(apiClient.getSession(), infostoreFolder, body, "2", 2, null, Boolean.TRUE, null);

        //        assertNotNull(syncFolders.getData().getQuota());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFolders_quotaRequestedButWrongVersion_noQuotaReturned() throws ApiException {
        DriveSyncFoldersBody body = new DriveSyncFoldersBody();
        DriveActionsResponse syncFolders = driveApi.syncFolders(apiClient.getSession(), infostoreFolder, body, null, null, null, Boolean.TRUE, null);

        assertNull(syncFolders.getError());
        //        assertNull(syncFolders.getData().getQuota());
    }

    @Test
    public void testSyncFiles_quotaNotSent() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(apiClient.getSession(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(apiClient.getSession(), infostoreFolder, path, body, null, 2, null, null, null, null, null);

        assertNull(syncFiles.getError());
        assertTrue(syncFiles.getData().isEmpty());
    }

    @Test
    public void testSyncFiles_quotaFalse() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(apiClient.getSession(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(apiClient.getSession(), infostoreFolder, path, body, null, 2, null, Boolean.FALSE, null, null, null);

        assertNull(syncFiles.getError());
        assertTrue(syncFiles.getData().isEmpty());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaRequested() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveSubfoldersResponse synchronizableFolders = driveApi.getSynchronizableFolders(apiClient.getSession(), infostoreFolder);
        String path = synchronizableFolders.getData().get(0).getPath();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(apiClient.getSession(), infostoreFolder, path, body, null, 2, null, Boolean.TRUE, null, null, null);

        //        assertNotNull(syncFiles.getData().getQuota());
    }

    @Ignore("Unfortunately the client isn't able to deserialize this response schema design.")
    @Test
    public void testSyncFiles_quotaRequestedButWrongVersion_noQuotaReturned() throws ApiException {
        DriveSyncFilesBody body = new DriveSyncFilesBody();
        DriveExtendedActionsResponse syncFiles = driveApi.syncFiles(apiClient.getSession(), infostoreFolder, null, body, null, null, null, Boolean.TRUE, null, null, null);

        assertNull(syncFiles.getError());
        //      assertNull(syncFiles.getData().getQuota());
    }

    @Test
    public void testQuota() throws ApiException {
        DriveQuotaResponse quotaResponse = driveApi.getQuota(apiClient.getSession(), infostoreFolder);

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
