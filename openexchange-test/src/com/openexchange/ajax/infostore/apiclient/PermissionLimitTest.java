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

package com.openexchange.ajax.infostore.apiclient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.validation.PermissionSizeValidator;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemPermission.BitsEnum;
import com.openexchange.testing.httpclient.models.UsersResponse;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link PermissionLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class PermissionLimitTest extends InfostoreApiClientTest {

    List<Integer> allEntities;
    Integer[] validPerms = null;
    Integer[] invalidPerms = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        super.setUpConfiguration();

        UserApi userApi = new UserApi(getApiClient());
        UsersResponse resp = userApi.getAllUsers("1", null, null);
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allUsers = (ArrayList<ArrayList<Object>>) resp.getData();
        allEntities = allUsers.stream().limit(4).map((list) -> (Integer) list.get(0)).collect(Collectors.toList());
        validPerms = allEntities.stream().limit(2).toArray(Integer[]::new);
        invalidPerms = allEntities.stream().toArray(Integer[]::new);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(4).build();
    }

    /**
     * Creates a permissions list with default permissions from the given list of entities
     *
     * @param entities The entities
     * @return A list of permissions for all given entities
     */
    private List<InfoItemPermission> createPermissionsList(Integer... entities){
        List<InfoItemPermission> permissions = new ArrayList<>();
        for(Integer entity: entities) {
            InfoItemPermission perm = new InfoItemPermission();
            perm.setEntity(entity);
            perm.setGroup(Boolean.FALSE);
            perm.setBits(BitsEnum.NUMBER_2);
            permissions.add(perm);
        }
        return permissions;
    }

    /**
     * Test that the middleware denies folders with too many permissions
     *
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void testPermissionLimit() throws ApiException, IOException {
        final File file = File.createTempFile("infostore-permission-limit-test", ".txt");
        // Create a valid file
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);
        // Try to add too many permissions
        updatePermissions(id, createPermissionsList(invalidPerms), Optional.of(InfostoreExceptionCodes.TOO_MANY_PERMISSIONS.create().getErrorCode()));
        // Try with a valid amount
        updatePermissions(id, createPermissionsList(validPerms));
    }

    // -------------------------   prepare config --------------------------------------

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        CONFIG.put(PermissionSizeValidator.MAX_OBJECT_PERMISSIONS.getFQPropertyName(), String.valueOf(3));
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
