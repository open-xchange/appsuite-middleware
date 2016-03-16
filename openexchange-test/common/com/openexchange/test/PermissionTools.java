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
     *  omitted -> no permissions
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
     *  omitted -> no permissions
     *
     *  The options are:
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
     *  omitted -> no permissions
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
     *  omitted -> no permissions
     *
     *  The options are:
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
            int entity = (Integer) permDefs[i++];
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
