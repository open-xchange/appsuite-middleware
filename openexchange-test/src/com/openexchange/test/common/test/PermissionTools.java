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

import static com.openexchange.java.Autoboxing.i;
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
import java.util.ArrayList;
import java.util.List;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PermissionTools}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PermissionTools {

    public static final String ADMIN = "arawada/a";

    public static final String ADMIN_GROUP = "arawada/ag";

    /**
     * Creates an OCLPermission out of a String definition. The definition consists of two parts separated by a '/'
     * The first part is the permission specification, the second part are options. [permissions]/[options]
     *
     * The permissions consist of:
     *
     * Folder Permission:
     * 'a' -> Admin
     * 'v' -> View Folder
     * 'c' -> Create Objects in Folder
     * 's' -> Create Subfolders
     * omitted -> no permissions
     *
     * Read Permission
     * 'r' -> read all
     * 'ro'-> read own
     * 'ra' -> Admin
     * omitted -> no permissions
     *
     * Write permissions
     * 'w' -> write all
     * 'wo' -> write own
     * 'wa' -> Admin
     * omitted -> no permissions
     *
     * Delete Permissions:
     * 'd' -> delete all
     * 'do' -> delete own
     * 'da' -> Admin
     * omitted -> no permissions
     *
     * The options are:
     * 'a' -> Admin Flag
     * 'g' -> Group Flag
     */
    public static OCLPermission OCLP(int entity, String permissionDef) {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(entity);
        PermissionScanner scanner = new PermissionScanner(permissionDef);

        int[] permissions = scanner.getPermissions();
        permission.setFolderPermission(permissions[0]);
        permission.setReadObjectPermission(permissions[1]);
        permission.setWriteObjectPermission(permissions[2]);
        permission.setDeleteObjectPermission(permissions[3]);

        boolean[] options = scanner.getOptions();
        permission.setFolderAdmin(options[0]);
        permission.setGroupPermission(options[1]);

        return permission;
    }

    /**
     * Creates OCLPermissions out of a String definitions. The arguments must be entity ids and permission definitions alternately.
     *
     * The definition consists of two parts separated by a '/'
     * The first part is the permission specification, the second part are options. [permissions]/[options]
     *
     * The permissions consist of:
     *
     * Folder Permission:
     * 'a' -> Admin
     * 'v' -> View Folder
     * 'c' -> Create Objects in Folder
     * 's' -> Create Subfolders
     * omitted -> no permissions
     *
     * Read Permission
     * 'r' -> read all
     * 'ro'-> read own
     * 'ra' -> Admin
     * omitted -> no permissions
     *
     * Write permissions
     * 'w' -> write all
     * 'wo' -> write own
     * 'wa' -> Admin
     * omitted -> no permissions
     *
     * Delete Permissions:
     * 'd' -> delete all
     * 'do' -> delete own
     * 'da' -> Admin
     * omitted -> no permissions
     *
     * The options are:
     * 'a' -> Admin Flag
     * 'g' -> Group Flag
     *
     * @param entity
     * @param permissionDef
     * @return
     */
    public static List<OCLPermission> P(Object... permDefs) {
        if (permDefs.length % 2 != 0) {
            throw new IllegalArgumentException("Expecting alternating ints and Strings");
        }

        List<OCLPermission> retval = new ArrayList<OCLPermission>();

        for (int i = 0; i < permDefs.length; i++) {
            int entity = i((Integer) permDefs[i++]);
            String permissionDef = (String) permDefs[i];

            retval.add(OCLP(entity, permissionDef));
        }

        return retval;
    }

    private static final class PermissionScanner {

        private static final int OPTIONS_MODE = 1;

        private static final int PERMISSIONS_MODE = 0;

        private final String permissionString;

        private int index;

        private final int[] permissions = new int[] { NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS };

        private int mode;

        private final boolean[] options = new boolean[2];

        public PermissionScanner(String permission) {
            this.permissionString = permission;
            while (!eol()) {
                lookupNext();
            }
        }

        private boolean eol() {
            if (index >= permissionString.length()) {
                return true;
            }
            return false;
        }

        private char lookahead() {
            return permissionString.charAt(index);
        }

        private char consume() {
            return permissionString.charAt(index++);
        }

        private void lookupNext() {

            switch (mode) {
                case PERMISSIONS_MODE:
                    parsePermission();
                    break;
                case OPTIONS_MODE:
                    parseOptions();
                    break;
            }

        }

        private void parseOptions() {
            switch (lookahead()) {
                case 'a':
                    consume();
                    options[0] = true;
                    break;
                case 'g':
                    consume();
                    options[1] = true;
                    break;
                default:
                    consume();
            }
        }

        private void parsePermission() {
            switch (lookahead()) {
                case 'a':
                    consume();
                    permissions[0] = ADMIN_PERMISSION;
                    break;
                case 'r':
                    consume();
                    permissions[1] = (own()) ? READ_OWN_OBJECTS : (admin()) ? ADMIN_PERMISSION : READ_ALL_OBJECTS;
                    break;
                case 'w':
                    consume();
                    permissions[2] = (own()) ? WRITE_OWN_OBJECTS : (admin()) ? ADMIN_PERMISSION : WRITE_ALL_OBJECTS;
                    break;
                case 'd':
                    consume();
                    permissions[3] = (own()) ? DELETE_OWN_OBJECTS : (admin()) ? ADMIN_PERMISSION : DELETE_ALL_OBJECTS;
                    break;
                case 'v':
                    consume();
                    permissions[0] = READ_FOLDER;
                    break;
                case 'c':
                    consume();
                    permissions[0] = CREATE_OBJECTS_IN_FOLDER;
                    break;
                case 's':
                    consume();
                    permissions[0] = CREATE_SUB_FOLDERS;
                    break;
                case '/':
                    consume();
                    mode = OPTIONS_MODE;
                    break;
                default:
                    consume();
                    break;
            }
        }

        private boolean own() {
            return isNextLetter('o');
        }

        private boolean admin() {
            return isNextLetter('a');
        }

        /**
         * @param c
         * @return
         */
        private boolean isNextLetter(char c) {
            if (!eol() && c == lookahead()) {
                consume();
                return true;
            }
            return false;

        }

        public int[] getPermissions() {
            return permissions;
        }

        public boolean[] getOptions() {
            return options;
        }
    }
}
