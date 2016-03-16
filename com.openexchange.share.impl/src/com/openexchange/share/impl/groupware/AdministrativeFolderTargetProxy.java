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

package com.openexchange.share.impl.groupware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.DriveTargetProxyType;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxyType;


/**
 * {@link AdministrativeFolderTargetProxy}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class AdministrativeFolderTargetProxy extends AbstractTargetProxy {

    private final FolderObject folder;
    private final Set<Integer> affectedUsers;
    private final ShareTarget target;
    private final ShareTargetPath targetPath;

    /**
     * Initializes a new {@link AdministrativeFolderTargetProxy}.
     *
     * @param folder The underlying folder object
     */
    public AdministrativeFolderTargetProxy(FolderObject folder) {
        super();
        this.folder = folder;
        affectedUsers = new HashSet<Integer>();
        target = new ShareTarget(folder.getModule(), Integer.toString(folder.getObjectID()), null);
        targetPath = new ShareTargetPath(folder.getModule(), Integer.toString(folder.getObjectID()), null);
    }

    @Override
    public String getID() {
        return targetPath.getFolder();
    }

    @Override
    public String getFolderID() {
        return Integer.toString(folder.getParentFolderID());
    }

    public int getOwner() {
        return folder.getCreatedBy();
    }

    @Override
    public String getTitle() {
        return folder.getFolderName();
    }

    @Override
    public List<TargetPermission> getPermissions() {
        List<OCLPermission> permissions = folder.getPermissions();
        if (null == permissions) {
            return Collections.emptyList();
        }
        OCLPermissionConverter converter = new OCLPermissionConverter(folder);
        List<TargetPermission> targetPermissions = new ArrayList<TargetPermission>(permissions.size());
        for (OCLPermission permission : permissions) {
            targetPermissions.add(converter.convert(permission));
        }
        return targetPermissions;
    }

    @Override
    public void applyPermissions(List<TargetPermission> permissions) {
        List<OCLPermission> originalPermissions = folder.getPermissions();
        if (null == originalPermissions) {
            originalPermissions = new ArrayList<OCLPermission>();
        }
        List<OCLPermission> newPermissions = mergePermissions(originalPermissions, permissions, new OCLPermissionConverter(folder));
        for (OCLPermission permission : newPermissions) {
            if (false == permission.isSystem() && false == permission.isGroupPermission()) {
                affectedUsers.add(Integer.valueOf(permission.getEntity()));
            }
        }
        folder.setPermissions(newPermissions);
        setModified();
    }

    @Override
    public void removePermissions(List<TargetPermission> permissions) {
        List<OCLPermission> originalPermissions = folder.getPermissions();
        if (null == originalPermissions || 0 == originalPermissions.size()) {
            return;
        }
        for (OCLPermission permission : originalPermissions) {
            if (false == permission.isSystem() && false == permission.isGroupPermission()) {
                affectedUsers.add(Integer.valueOf(permission.getEntity()));
            }
        }
        List<OCLPermission> newPermissions = removePermissions(originalPermissions, permissions, new OCLPermissionConverter(folder));
        folder.setPermissions(newPermissions);
        setModified();
    }

    @Override
    public ShareTarget getTarget() {
        return target;
    }

    @Override
    public ShareTargetPath getTargetPath() {
        return targetPath;
    }

    /**
     * Gets the underlying folder.
     *
     * @return The folder
     */
    public FolderObject getFolder() {
        return folder;
    }

    /**
     * Gets the identifiers of all potentially affected users that had or will have access to this folder based on the underlying
     * permissions.
     *
     * @return The identifiers of all affected users
     */
    public Collection<Integer> getAffectedUsers() {
        return affectedUsers;
    }

    @Override
    public String toString() {
        return "AdministrativeFolderTargetProxy [folder=" + folder + "]";
    }

    @Override
    public TargetProxyType getProxyType() {
        return DriveTargetProxyType.FOLDER;
    }

    private static class OCLPermissionConverter implements PermissionConverter<OCLPermission> {

        private final FolderObject folder;

        /**
         * Initializes a new {@link OCLPermissionConverter}.
         *
         * @param folder
         */
        public OCLPermissionConverter(FolderObject folder) {
            super();
            this.folder = folder;
        }

        @Override
        public int getEntity(OCLPermission permission) {
            return permission.getEntity();
        }

        @Override
        public boolean isGroup(OCLPermission permission) {
            return permission.isGroupPermission();
        }

        @Override
        public boolean isSystem(OCLPermission permission) {
            return permission.isSystem();
        }

        @Override
        public int getBits(OCLPermission permission) {
            return Permissions.createPermissionBits(permission.getFolderPermission(), permission.getReadPermission(),
                permission.getWritePermission(), permission.getDeletePermission(), permission.isFolderAdmin());
        }

        @Override
        public OCLPermission convert(TargetPermission permission) {
            OCLPermission oclPermission = new OCLPermission(permission.getEntity(), folder.getObjectID());
            oclPermission.setGroupPermission(permission.isGroup());
            int[] bits = Permissions.parsePermissionBits(permission.getBits());
            oclPermission.setAllPermission(bits[0], bits[1], bits[2], bits[3]);
            return oclPermission;
        }

        @Override
        public TargetPermission convert(OCLPermission permission) {
            return new TargetPermission(permission.getEntity(), permission.isGroupPermission(), getBits(permission));
        }

    }

    @Override
    public boolean mayAdjust() {
        return true;
    }

    @Override
    public Date getTimestamp() {
        return folder.getLastModified();
    }

}
