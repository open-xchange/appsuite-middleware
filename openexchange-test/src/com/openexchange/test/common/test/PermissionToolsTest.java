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

package com.openexchange.test.common.test;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.server.impl.OCLPermission.ADMIN_PERMISSION;
import static com.openexchange.server.impl.OCLPermission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.server.impl.OCLPermission.CREATE_SUB_FOLDERS;
import static com.openexchange.server.impl.OCLPermission.DELETE_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.DELETE_OWN_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.NO_PERMISSIONS;
import static com.openexchange.server.impl.OCLPermission.READ_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_FOLDER;
import static com.openexchange.server.impl.OCLPermission.READ_OWN_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.WRITE_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.WRITE_OWN_OBJECTS;
import static com.openexchange.test.common.test.PermissionTools.OCLP;
import static com.openexchange.test.common.test.PermissionTools.P;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Test;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PermissionToolsTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PermissionToolsTest {

    @Test
    public void testOCLP() {

        assertPermissions("arwd", 12, false, false, ADMIN_PERMISSION, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);

        // Nothing means no permission
        assertPermissions("rwd", 12, false, false, NO_PERMISSIONS, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("awd", 12, false, false, ADMIN_PERMISSION, NO_PERMISSIONS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("ard", 12, false, false, ADMIN_PERMISSION, READ_ALL_OBJECTS, NO_PERMISSIONS, DELETE_ALL_OBJECTS);
        assertPermissions("arw", 12, false, false, ADMIN_PERMISSION, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);

        // Own
        assertPermissions("arowodo", 12, false, false, ADMIN_PERMISSION, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, DELETE_OWN_OBJECTS);

        // Admin
        assertPermissions("arawada", 12, false, false, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION);

        assertPermissions("vrwd", 12, false, false, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("crwd", 12, false, false, CREATE_OBJECTS_IN_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("srwd", 12, false, false, CREATE_SUB_FOLDERS, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);

        assertPermissions("arwd/ag", 12, true, true, ADMIN_PERMISSION, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("arwd/g", 12, true, false, ADMIN_PERMISSION, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        assertPermissions("arwd/a", 12, false, true, ADMIN_PERMISSION, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);

    }

    @Test
    public void testP() {
        List<OCLPermission> oclps = P(I(12), "arwd", I(13), "arwd", I(14), "arwd/g");

        assertEquals(3, oclps.size());
        assertEquals(12, oclps.get(0).getEntity());
        assertEquals(13, oclps.get(1).getEntity());
        assertEquals(14, oclps.get(2).getEntity());

    }

    private static void assertPermissions(String permissionString, int entity, boolean group, boolean folderAdmin, int folderPermission, int readPermission, int writePermission, int deletePermission) {
        OCLPermission oclp = OCLP(entity, permissionString);

        assertNotNull(oclp);
        assertEquals(entity, oclp.getEntity());
        assertEquals(B(group), B(oclp.isGroupPermission()));

        assertEquals(B(folderAdmin), B(oclp.isFolderAdmin()));
        assertEquals(folderPermission, oclp.getFolderPermission());
        assertEquals(readPermission, oclp.getReadPermission());
        assertEquals(writePermission, oclp.getWritePermission());
        assertEquals(deletePermission, oclp.getDeletePermission());
    }

}
