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

package com.openexchange.test;

import static com.openexchange.server.impl.OCLPermission.*;
import static com.openexchange.test.PermissionTools.OCLP;
import static com.openexchange.test.PermissionTools.P;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PermissionToolsTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PermissionToolsTest extends TestCase {

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

    public void testP() {
        List<OCLPermission> oclps = P(  12, "arwd",
                                        13, "arwd",
                                        14, "arwd/g" );

        assertEquals(3, oclps.size());
        assertEquals(12, oclps.get(0).getEntity());
        assertEquals(13, oclps.get(1).getEntity());
        assertEquals(14, oclps.get(2).getEntity());


    }

    private static void assertPermissions(String permissionString, int entity, boolean group, boolean folderAdmin, int folderPermission, int readPermission, int writePermission, int deletePermission) {
        OCLPermission oclp = OCLP(entity, permissionString);

        assertNotNull(oclp);
        assertEquals(entity, oclp.getEntity());
        assertEquals(group, oclp.isGroupPermission());

        assertEquals(folderAdmin, oclp.isFolderAdmin());
        assertEquals(folderPermission, oclp.getFolderPermission());
        assertEquals(readPermission, oclp.getReadPermission());
        assertEquals(writePermission, oclp.getWritePermission());
        assertEquals(deletePermission, oclp.getDeletePermission());
    }

}
