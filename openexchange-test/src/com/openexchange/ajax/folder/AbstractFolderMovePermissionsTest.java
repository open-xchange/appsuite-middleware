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

package com.openexchange.ajax.folder;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.junit.Assert;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link AbstractFolderMovePermissionsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.4
 */
public abstract class AbstractFolderMovePermissionsTest extends AbstractConfigAwareAPIClientSession {

    protected final String type;
    protected final String TREE = "0";
    protected final Integer BITS_ADMIN = new Integer(403710016);
    protected final Integer BITS_AUTHOR = new Integer(4227332);
    protected final Integer BITS_REVIEWER = new Integer(33025);
    protected final Integer BITS_VIEWER = new Integer(257);
    protected final Integer BITS_OWNER = new Integer(272662788);
    private final List<String> createdFolders;

    protected FoldersApi api;
    protected FoldersApi api2;
    protected Integer userId1;
    protected Integer userId2;
    protected String privateFolderId;
    protected String publicFolderId;
    protected String sharedFolderId;

    protected AbstractFolderMovePermissionsTest(String type) {
        super();
        this.type = type;
        createdFolders = new ArrayList<String>();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();
        ApiClient apiClient2 = testUser2.getApiClient();
        userId1 = I(testUser.getUserId());
        userId2 = I(testUser2.getUserId());
        api = new FoldersApi(getApiClient());
        api2 = new FoldersApi(apiClient2);
        switch (type) {
            case "keep":
            case "inherit":
                privateFolderId = createNewFolder(true, BITS_REVIEWER, false, true);
                publicFolderId = createNewFolder(true, BITS_REVIEWER, true, false);
                sharedFolderId = createSharedFolder();
                break;
            case "merge":
                privateFolderId = createNewFolder(false, I(0), true, true);
                publicFolderId = createNewFolder(false, I(0), true, false);
                sharedFolderId = createSharedFolder();
                break;
            default:
                fail("Unexpected type: " + type);
        }
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).build();
    }

    @Override
    protected String getScope() {
        return "user";
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> configs = new HashMap<>();
        configs.put("com.openexchange.folderstorage.permissions.moveToPublic", type);
        configs.put("com.openexchange.folderstorage.permissions.moveToShared", type);
        configs.put("com.openexchange.folderstorage.permissions.moveToPrivate", type);
        return configs;
    }

    protected String getPrivateInfostoreFolder(ApiClient apiClient) throws ApiException {
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
        Object data = checkResponse(configNode);
        if (data != null && !data.toString().equalsIgnoreCase("null")) {
            return String.valueOf(data);
        }
        Assert.fail("It seems that the user doesn't support drive.");
        return null;
    }

    protected Object checkResponse(ConfigResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    protected String createNewFolder(boolean additionalPermissions, Integer additionalBits, boolean addGroup, boolean privateTree) throws Exception {
        return createNewFolder(additionalPermissions ? userId2 : null, additionalBits, addGroup, privateTree);
    }

    protected String createNewFolder(Integer userIdToShare, Integer additionalBits, boolean addGroup, boolean privateTree) throws Exception {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderPermissionTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p1 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        if (userIdToShare != null && userIdToShare.intValue() > 0) {
            FolderPermission p = createPermissionFor(userIdToShare, additionalBits, Boolean.FALSE);
            perm.add(p);
        }
        if (addGroup) {
            FolderPermission p = createPermissionFor(I(0), BITS_VIEWER, Boolean.TRUE);
            perm.add(p);
        }
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = api.createFolder(privateTree ? getPrivateInfostoreFolder(getApiClient()) : "15", body, TREE, null, null, null);
        String folderId = response.getData();
        createdFolders.add(folderId);
        return folderId;
    }

    protected String createChildFolder(String parentFolderId) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderPermissionTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse response = api.createFolder(parentFolderId, body, TREE, null, null, null);
        String folderId = response.getData();
        createdFolders.add(folderId);
        return folderId;
    }

    protected String createChildFolder(String parentFolderId, Integer userIdToShare) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderPermissionTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p1 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        if (userIdToShare != null && userIdToShare.intValue() > 0) {
            FolderPermission p = createPermissionFor(userIdToShare, BITS_VIEWER, Boolean.FALSE);
            perm.add(p);
        }
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = api.createFolder(parentFolderId, body, TREE, null, null, null);
        String folderId = response.getData();
        createdFolders.add(folderId);
        return folderId;
    }

    protected FolderPermission createPermissionFor(Integer entity, Integer bits, Boolean isGroup) {
        FolderPermission p = new FolderPermission();
        p.setEntity(entity);
        p.setGroup(isGroup);
        p.setBits(bits);
        return p;
    }

    private String createSharedFolder() throws Exception {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderPermissionTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p1 = createPermissionFor(userId2, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        FolderPermission p2 = createPermissionFor(userId1, BITS_AUTHOR, Boolean.FALSE);
        perm.add(p2);
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = api2.createFolder(getPrivateInfostoreFolder(api2.getApiClient()), body, TREE, null, null, null);
        return response.getData();
    }

}
