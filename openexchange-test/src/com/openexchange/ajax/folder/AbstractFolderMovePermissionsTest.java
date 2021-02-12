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

package com.openexchange.ajax.folder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.junit.Assert;
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
        ApiClient apiClient2 = generateApiClient(testUser2);
        userId1 = getApiClient().getUserId();
        userId2 = apiClient2.getUserId();
        api = new FoldersApi(getApiClient());
        api2 = new FoldersApi(apiClient2);
        switch (type) {
            case "keep":
            case "inherit":
                privateFolderId = createNewFolder(true, BITS_REVIEWER, false, true);
                publicFolderId = createNewFolder(true, BITS_REVIEWER, false, false);
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
    public void tearDown() throws Exception {
        api.deleteFolders(getApiClient().getSession(), createdFolders, TREE, L(System.currentTimeMillis()), null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        if (Strings.isNotEmpty(sharedFolderId)) {
            api2.deleteFolders(api2.getApiClient().getSession(), Collections.singletonList(sharedFolderId), TREE, L(System.currentTimeMillis()), null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        }
        super.tearDown();
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
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), apiClient.getSession());
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
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary("FolderPermissionTest_" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        List<FolderPermission> perm = new ArrayList<FolderPermission>();
        FolderPermission p1 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        if (additionalPermissions) {
            FolderPermission p = createPermissionFor(userId2, additionalBits, Boolean.FALSE);
            perm.add(p);
        }
        if (addGroup) {
            FolderPermission p = createPermissionFor(I(0), BITS_VIEWER, Boolean.TRUE);
            perm.add(p);
        }
        folder.setPermissions(perm);
        body.setFolder(folder);
        FolderUpdateResponse response = api.createFolder(privateTree ? getPrivateInfostoreFolder(apiClient) : "15", getApiClient().getSession(), body, TREE, null, null, null);
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
        FolderUpdateResponse response = api2.createFolder(getPrivateInfostoreFolder(api2.getApiClient()), api2.getApiClient().getSession(), body, TREE, null, null, null);
        return response.getData();
    }

}
