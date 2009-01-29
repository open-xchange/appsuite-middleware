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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.server.impl.OCLPermission;
import static com.openexchange.server.impl.OCLPermission.*;

/**
 * {@link PermissionTools}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PermissionTools {

    public static OCLPermission OCLP(int entity, String permissionDef) {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(entity);
        PermissionScanner scanner = new PermissionScanner(permissionDef);
        
        int[] permissions = scanner.getPermissions();
        permission.setFolderPermission(permissions[0]);
        permission.setReadObjectPermission(permissions[1]);
        permission.setWriteObjectPermission(permissions[2]);
        permission.setDeleteObjectPermission(permissions[3]);
        
        return permission;
    }

    private static final class PermissionScanner {

        private String permissionString;

        private int index;

        private int[] permissions = new int[]{NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS };

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

            switch (lookahead()) {
            case 'a':
                consume();
                permissions[0] = ADMIN_PERMISSION;
                break;
            case 'r':
                consume();
                permissions[1] = (own())? READ_OWN_OBJECTS : READ_ALL_OBJECTS;
                break;
            case 'w':
                consume();
                permissions[2] = (own())? WRITE_OWN_OBJECTS : WRITE_ALL_OBJECTS;
                break;
            case 'd':
                consume();
                permissions[3] = (own())? DELETE_OWN_OBJECTS : DELETE_ALL_OBJECTS;
                break;
            default:
                consume();
                break;
            }

        }
        
        private boolean own() {
            if(!eol() && 'o' == lookahead()) {
                consume();
                return true;
            }
            return false;
        }

        public int[] getPermissions() {
            return permissions;
        }
    }
}
