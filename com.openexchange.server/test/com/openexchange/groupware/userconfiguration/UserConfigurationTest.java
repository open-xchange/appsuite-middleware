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

package com.openexchange.groupware.userconfiguration;

import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit Tests for {@link UserConfigurationTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.2.2
 */
public class UserConfigurationTest {

     @Test
     public void testByBits_GetActiveSync_returnPermission() {
        List<Permission> userPermissionsByBit = Permission.byBits(UserConfiguration.ACTIVE_SYNC);
        assertNotNull(userPermissionsByBit);
    }

     @Test
     public void testByBits_GetActiveSync_returnCorrectSize() {
        List<Permission> userPermissionsByBit = Permission.byBits(UserConfiguration.ACTIVE_SYNC);
        Assert.assertEquals(1, userPermissionsByBit.size());
    }

     @Test
     public void testByBits_NoValidBit_returnEmptyPermission() {
        List<Permission> userPermissionsByBit = Permission.byBits(0);
        assertNotNull(userPermissionsByBit);
    }

     @Test
     public void testByBits_NoValidBit_returnEmptyList() {
        List<Permission> userPermissionsByBit = Permission.byBits(0);
        Assert.assertEquals(0, userPermissionsByBit.size());
    }

     @Test
     public void testByBits_GetWithThreePermissions_returnCorrectSize() {
        List<Permission> userPermissionsByBit = Permission.byBits(UserConfiguration.ACTIVE_SYNC + UserConfiguration.CALENDAR + UserConfiguration.CALDAV);
        Assert.assertEquals(3, userPermissionsByBit.size());
    }

     @Test
     public void testByBits_GetWithManyPermissions_returnList() {
        List<Permission> userPermissionsByBit = Permission.byBits(2097157);
        Assert.assertEquals(3, userPermissionsByBit.size());
    }

//    @Test
//     public void testByBits_GetWithManyPermissionsWithServiceChecker_returnList() {
//        List<Permission> userPermissionsByBit = Permission.byBits(2097157, false);
//        Assert.assertEquals(3, userPermissionsByBit.size());
//    }
}
