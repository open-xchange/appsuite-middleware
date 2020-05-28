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

        UserApi userApi = new UserApi(apiClient);
        UsersResponse resp = userApi.getAllUsers(getSessionId(), "1", null, null);
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allUsers = (ArrayList<ArrayList<Object>>) resp.getData();
        allEntities = allUsers.parallelStream().map((list) -> (Integer) list.get(0)).collect(Collectors.toList());
        validPerms = allEntities.stream().limit(2).toArray(Integer[]::new);
        invalidPerms = allEntities.stream().toArray(Integer[]::new);
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
        CONFIG.put("com.openexchange.infostore.maxPermissionEntities", String.valueOf(3));
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
