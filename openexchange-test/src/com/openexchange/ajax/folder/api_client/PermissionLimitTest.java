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

package com.openexchange.ajax.folder.api_client;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.UsersResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.UserApi;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;

/**
 * {@link PermissionLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class PermissionLimitTest extends AbstractConfigAwareAPIClientSession {

    private Long timestamp = L(0);
    private FoldersApi folderApi;
    private final List<String> createdFolders = new ArrayList<>();
    List<Integer> allEntities;
    Integer[] validPerms = null;
    Integer[] invalidPerms = null;
    private String defaultFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        super.setUpConfiguration();
        // add third user necessary for testing
        this.testContext.acquireUser();
        folderApi = new FoldersApi(getApiClient());
        UserApi userApi = new UserApi(getApiClient());
        UsersResponse resp = userApi.getAllUsers("1", null, null);
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allUsers = (ArrayList<ArrayList<Object>>) resp.getData();
        allEntities = allUsers.parallelStream().map((list) -> (Integer) list.get(0)).collect(Collectors.toList());
        assertTrue("Not enough users to perform this test.", allEntities.size() > 3);
        validPerms = allEntities.stream().limit(2).toArray(Integer[]::new);
        invalidPerms = allEntities.stream().toArray(Integer[]::new);
        defaultFolder = getDefaultFolder();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(4).build();
    }

    /**
     * Creates a folder with the given title
     *
     * @param title The folder title
     * @param perms The permissions to use
     * @return The folder id
     * @throws ApiException
     */
    private String createFolder(String title, List<FolderPermission> perms) throws ApiException {
        return createFolder(title, perms, Optional.empty());
    }

    /**
     * Creates a folder with the given title and permissions
     *
     * @param title The folder title
     * @param perms The folder permissions
     * @param errorCode The suspected error code. Return null in case the proper error is thrown
     * @return The folder id or null
     * @throws ApiException
     */
    private String createFolder(String title, List<FolderPermission> perms, Optional<String> errorCode) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setPermissions(perms);
        folder.setModule(Module.INFOSTORE.getName());
        folder.setTitle(title);
        body.setFolder(folder);
        FolderUpdateResponse resp = folderApi.createFolder(defaultFolder, body, null, null, null, null);
        if(errorCode.isPresent()) {
            assertEquals(errorCode.get(), resp.getCode());
            return null;
        }
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        this.timestamp = resp.getTimestamp();
        createdFolders.add(resp.getData());
        return resp.getData();
    }

    /**
     * Retrieves the default contact folder of the user with the specified session
     *
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultFolder() throws Exception {
        FoldersVisibilityResponse visibleFolders = folderApi.getVisibleFolders("infostore", "1,308", "0", null, null);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object folders = visibleFolders.getData().getPublic();
        ArrayList<ArrayList<?>> folderList = (ArrayList<ArrayList<?>>) folders;
        if (folderList.size() == 1) {
            return (String) folderList.get(0).get(0);
        }
        for (ArrayList<?> folder : folderList) {
            if (((Boolean) folder.get(1)).booleanValue()) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default contact folder!");
    }

    /**
     * Updates permissions of the folder with the given id
     *
     * @param id The id of the folder
     * @param perms The new permissions
     * @param cascade Whether to cascade permissions or not
     * @param errorCode The error code to expect. Return null in case a proper error is thrown
     * @return The folder id
     * @throws ApiException
     */
    private String updatePermissions(String id, List<FolderPermission> perms, boolean cascade, Optional<String> errorCode) throws ApiException {
        FolderBody body = new FolderBody();
        FolderData folder = new FolderData();
        folder.setPermissions(perms);
        folder.setModule(Module.INFOSTORE.getName());
        folder.setId(id);
        body.folder(folder);
        FolderUpdateResponse resp = folderApi.updateFolder(id, body, Boolean.FALSE, timestamp, null, null, B(cascade), null, null, null);
        if(errorCode.isPresent()) {
            assertNotNull("Response didn't contain an exception.", resp.getError());
            assertEquals("Unexpected error: " + resp.getErrorDesc(), errorCode.get(), resp.getCode());
            return null;
        }
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        this.timestamp = resp.getTimestamp();
        return resp.getData();
    }

    /**
     * Creates a permissions list with default permissions from the given list of entities
     *
     * @param entities The entities
     * @return A list of permissions for all given entities
     */
    private List<FolderPermission> createPermissionsList(Integer... entities){
        List<FolderPermission> permissions = new ArrayList<>();
        for(Integer entity: entities) {
            FolderPermission perm = new FolderPermission();
            perm.setEntity(entity);
            perm.setGroup(Boolean.FALSE);
            perm.setBits(I(403710016));
            permissions.add(perm);
        }
        return permissions;
    }

    /**
     * Test that the middleware denies folders with too many permissions
     *
     * @throws ApiException
     */
    @Test
    public void testPermissionLimit() throws ApiException {

        // Create valid folder
        String folderId = createFolder(PermissionLimitTest.class.getSimpleName()+"_"+new UID().toString().hashCode(), createPermissionsList(validPerms));
        // Update with too many permissions and expect an error
        updatePermissions(folderId, createPermissionsList(invalidPerms), false, Optional.of(OXFolderExceptionCode.TOO_MANY_PERMISSIONS.create().getErrorCode()));
        // reduce number of permissions
        Integer[] reducedPerms = Arrays.copyOf(validPerms, validPerms.length - 1);
        updatePermissions(folderId, createPermissionsList(reducedPerms), false, Optional.empty());

        // Try to create folder with too many permissions
        createFolder(PermissionLimitTest.class.getSimpleName() + "_" + new UID().toString().hashCode(), createPermissionsList(invalidPerms), Optional.of(OXFolderExceptionCode.TOO_MANY_PERMISSIONS.create().getErrorCode()));

    }

    // -------------------------   prepare config --------------------------------------

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        CONFIG.put(OXFolderManagerImpl.MAX_FOLDER_PERMISSIONS.getFQPropertyName(), String.valueOf(3));
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }

}
