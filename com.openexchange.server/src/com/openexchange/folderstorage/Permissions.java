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

package com.openexchange.folderstorage;

import com.openexchange.server.impl.OCLPermission;
import gnu.trove.map.hash.TIntIntHashMap;


/**
 * A helper class with useful methods for folder permissions.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Permissions {

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static final int[] MAPPING_0 = { 0, 2, 4, -1, 8 };

    private static final TIntIntHashMap MAPPING_1 = new TIntIntHashMap(6) {
        { // Unnamed Block.
            put(Permission.MAX_PERMISSION, MAX_PERMISSION);
            put(MAX_PERMISSION, MAX_PERMISSION);
            put(0, 0);
            put(2, 1);
            put(4, 2);
            put(8, 4);
        }
    };

    /**
     * Parses the given permission bit mask into an array with 5 elements:
     * <ul>
     * <li>0: folder permission</li>
     * <li>1: read permission</li>
     * <li>2: write permission</li>
     * <li>3: delete permission</li>
     * <li>4: admin permission (<code>true</code> if > 0)</li>
     * </ul>
     *
     * @param permissionBits The bit mask
     * @return An array containing the partial permissions.
     */
    public static final int[] parsePermissionBits(final int permissionBits) {
        int bits = permissionBits;
        final int[] retval = new int[5];
        for (int i = retval.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            retval[i] = bits >> shiftVal;
            bits -= (retval[i] << shiftVal);
            if (retval[i] == MAX_PERMISSION) {
                retval[i] = Permission.MAX_PERMISSION;
            } else if (i < (retval.length - 1)) {
                retval[i] = MAPPING_0[retval[i]];
            } else {
                retval[i] = retval[i];
            }
        }
        return retval;
    }

    /**
     * Creates a permission bit mask from the given {@link Permission} instance.
     *
     * @param permission The permission
     * @return The bit mask
     */
    public static int createPermissionBits(final Permission permission) {
        return createPermissionBits(
            permission.getFolderPermission(),
            permission.getReadPermission(),
            permission.getWritePermission(),
            permission.getDeletePermission(),
            permission.isAdmin());
    }

    /**
     * Creates a read-only permission bit mask.
     *
     * @return The bit mask
     */
    public static int createReadOnlyPermissionBits() {
        return createPermissionBits(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, false);
    }

    /**
     * Creates a permission bit mask from the given partial permissions.
     *
     * @param fp The folder permission
     * @param rp The read permission
     * @param wp The write permission
     * @param dp The delete permission
     * @param adminFlag The folder admin flag
     * @return The bit mask
     */
    public static int createPermissionBits(int fp, int rp, int wp, int dp, boolean adminFlag) {
        int retval = 0;
        int i = 4;
        retval += (adminFlag ? 1 : 0) << (i-- * 7)/* Number of bits to be shifted */;
        retval += MAPPING_1.get(dp) << (i-- * 7);
        retval += MAPPING_1.get(wp) << (i-- * 7);
        retval += MAPPING_1.get(rp) << (i-- * 7);
        retval += MAPPING_1.get(fp) << (i * 7);
        return retval;
    }

    /**
     * Creates a new {@link Permission} instance based on the given entity and permission bit mask.
     *
     * @param entity The entity
     * @param isGroup Whether the entity is a group (<code>true</code>) or a user (<code>false</code>)
     * @param permissionBits The permission bit mask
     * @return The permission instance
     */
    public static Permission createPermission(int entity, boolean isGroup, int permissionBits) {
        return new DefaultPermission(entity, isGroup, permissionBits);
    }


}
