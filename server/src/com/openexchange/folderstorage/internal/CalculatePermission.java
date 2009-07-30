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

package com.openexchange.folderstorage.internal;

import java.util.Arrays;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalculatePermission} - Utility class to obtain an effective permission.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalculatePermission {

    /**
     * Initializes a new {@link CalculatePermission}.
     */
    private CalculatePermission() {
        super();
    }

    /**
     * Calculates the effective permission for given user in given folder.
     * 
     * @param folder The folder
     * @param user The user
     * @param context The context
     * @return The effective permission for given user in given folder
     * @throws FolderException If calculating the effective permission fails
     */
    public static Permission calculate(final Folder folder, final User user, final Context context) throws FolderException {
        final UserConfiguration userConfiguration;
        try {
            userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), context);
        } catch (final UserConfigurationException e) {
            throw new FolderException(e);
        }
        return new EffectivePermission(
            getMaxPermission(folder.getPermissions(), userConfiguration),
            folder.getID(),
            folder.getType(),
            folder.getContentType(),
            userConfiguration);
    }

    /**
     * Calculates the effective permission for given session's user in given folder.
     * 
     * @param folder The folder
     * @param session The session
     * @return The effective permission for given session's user in given folder
     */
    public static Permission calculate(final Folder folder, final ServerSession session) {
        final UserConfiguration userConfiguration = session.getUserConfiguration();
        return new EffectivePermission(
            getMaxPermission(folder.getPermissions(), userConfiguration),
            folder.getID(),
            folder.getType(),
            folder.getContentType(),
            userConfiguration);
    }

    private static Permission getMaxPermission(final Permission[] permissions, final UserConfiguration userConfig) {
        final DummyPermission p = new DummyPermission();
        p.setNoPermissions();

        final int[] idArr;
        {
            final int[] groups = userConfig.getGroups();
            idArr = new int[groups.length + 1];
            idArr[0] = userConfig.getUserId();
            System.arraycopy(groups, 0, idArr, 1, groups.length);
            Arrays.sort(idArr);
        }

        for (int i = 0; i < permissions.length; i++) {
            final Permission cur = permissions[i];
            if (Arrays.binarySearch(idArr, cur.getEntity()) >= 0) {
                if (cur.getFolderPermission() > p.getFolderPermission()) {
                    p.setFolderPermission(cur.getFolderPermission());
                }
                if (cur.getReadPermission() > p.getReadPermission()) {
                    p.setReadPermission(cur.getReadPermission());
                }
                if (cur.getWritePermission() > p.getWritePermission()) {
                    p.setWritePermission(cur.getWritePermission());
                }
                if (cur.getDeletePermission() > p.getDeletePermission()) {
                    p.setDeletePermission(cur.getDeletePermission());
                }
                if (!p.isAdmin() && cur.isAdmin()) {
                    p.setAdmin(true);
                }
            }
        }

        return p;
    }

    private static final class DummyPermission implements Permission {

        private int system;

        private int deletePermission;

        private int folderPermission;

        private int readPermission;

        private int writePermission;

        private boolean admin;

        private int entity;

        private boolean group;

        /**
         * Initializes an empty {@link DummyPermission}.
         */
        public DummyPermission() {
            super();
        }

        public int getDeletePermission() {
            return deletePermission;
        }

        public int getEntity() {
            return entity;
        }

        public int getFolderPermission() {
            return folderPermission;
        }

        public int getReadPermission() {
            return readPermission;
        }

        public int getSystem() {
            return system;
        }

        public int getWritePermission() {
            return writePermission;
        }

        public boolean isAdmin() {
            return admin;
        }

        public boolean isGroup() {
            return group;
        }

        public void setAdmin(final boolean admin) {
            this.admin = admin;
        }

        public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
            this.folderPermission = folderPermission;
            this.readPermission = readPermission;
            this.deletePermission = deletePermission;
            this.writePermission = writePermission;
        }

        public void setDeletePermission(final int permission) {
            this.deletePermission = permission;
        }

        public void setEntity(final int entity) {
            this.entity = entity;
        }

        public void setFolderPermission(final int permission) {
            this.folderPermission = permission;
        }

        public void setGroup(final boolean group) {
            this.group = group;
        }

        public void setMaxPermissions() {
            this.folderPermission = Permission.MAX_PERMISSION;
            this.readPermission = Permission.MAX_PERMISSION;
            this.deletePermission = Permission.MAX_PERMISSION;
            this.writePermission = Permission.MAX_PERMISSION;
            this.admin = true;
        }

        public void setNoPermissions() {
            this.folderPermission = Permission.NO_PERMISSIONS;
            this.readPermission = Permission.NO_PERMISSIONS;
            this.deletePermission = Permission.NO_PERMISSIONS;
            this.writePermission = Permission.NO_PERMISSIONS;
            this.admin = false;
        }

        public void setReadPermission(final int permission) {
            this.readPermission = permission;
        }

        public void setSystem(final int system) {
            this.system = system;
        }

        public void setWritePermission(final int permission) {
            this.writePermission = permission;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }

    } // End of DummyPermission

}
