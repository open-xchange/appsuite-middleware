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

package com.openexchange.ajax.folder.api_client;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

/**
 * {@link PermissionLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
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
        folderApi = new FoldersApi(getApiClient());
        UserApi userApi = new UserApi(apiClient);
        UsersResponse resp = userApi.getAllUsers(getSessionId(), "1", null, null);
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allUsers = (ArrayList<ArrayList<Object>>) resp.getData();
        allEntities = allUsers.parallelStream().map((list) -> (Integer) list.get(0)).collect(Collectors.toList());
        validPerms = allEntities.stream().limit(2).toArray(Integer[]::new);
        invalidPerms = allEntities.stream().toArray(Integer[]::new);
        defaultFolder = getDefaultFolder(getSessionId());
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
        FolderUpdateResponse resp = folderApi.createFolder(defaultFolder, getSessionId(), body, null, null, null, null);
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
     * @param session The session of the user
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultFolder(String session) throws Exception {
        FoldersVisibilityResponse visibleFolders = folderApi.getVisibleFolders(session, "infostore", "1,308", "0", null, null);
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
        FolderUpdateResponse resp = folderApi.updateFolder(getSessionId(), id, body, Boolean.FALSE, timestamp, null, null, B(cascade), null, null);
        if(errorCode.isPresent()) {
            assertEquals(errorCode.get(), resp.getCode());
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

    @Override
    public void tearDown() throws Exception {
        try {
            folderApi.deleteFolders(getApiClient().getSession(), createdFolders, "1", timestamp, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
        } finally {
            super.tearDown();
        }
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
        CONFIG.put("com.openexchange.folderstorage.maxPermissionEntities", String.valueOf(3));
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
