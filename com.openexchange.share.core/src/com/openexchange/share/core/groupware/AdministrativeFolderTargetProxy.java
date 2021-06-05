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

package com.openexchange.share.core.groupware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.i18n.Translator;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.KnownTargetProxyType;
import com.openexchange.share.groupware.SubfolderAwareTargetPermission;
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
    private List<OCLPermission> appliedPermissions;
    private List<OCLPermission> removedPermissions;

    /**
     * Initializes a new {@link AdministrativeFolderTargetProxy}.
     *
     * @param folder The underlying folder object
     */
    public AdministrativeFolderTargetProxy(FolderObject folder) {
        this(folder, new ShareTarget(folder.getModule(), Integer.toString(folder.getObjectID())));
    }

    /**
     * Initializes a new {@link AdministrativeFolderTargetProxy}.
     *
     * @param folder The underlying folder object
     * @param target The share target
     */
    public AdministrativeFolderTargetProxy(FolderObject folder, ShareTarget target) {
        super();
        this.folder = folder;
        appliedPermissions = new ArrayList<>();
        removedPermissions = new ArrayList<>();
        this.affectedUsers = new HashSet<Integer>();
        this.target = target;
        this.targetPath = new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
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
        appliedPermissions = mergePermissions(appliedPermissions, permissions, new OCLPermissionConverter(folder));
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
        removedPermissions = mergePermissions(removedPermissions, permissions, new OCLPermissionConverter(folder));
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
        if (Module.CALENDAR.getFolderConstant() == folder.getModule()) {
            return KnownTargetProxyType.CALENDAR;
        }
        return KnownTargetProxyType.FOLDER;
    }

    public static class OCLPermissionConverter implements PermissionConverter<OCLPermission> {

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
            if (permission instanceof SubfolderAwareTargetPermission) {
                oclPermission.setType(FolderPermissionType.getType(((SubfolderAwareTargetPermission) permission).getType()));
                oclPermission.setPermissionLegator(((SubfolderAwareTargetPermission) permission).getPermissionLegator());
            }
            return oclPermission;
        }

        @Override
        public TargetPermission convert(OCLPermission permission) {
            return new SubfolderAwareTargetPermission(permission.getEntity(), permission.isGroupPermission(), getBits(permission), permission.getType().getTypeNumber(), permission.getPermissionLegator(), permission.getSystem());
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

    public List<OCLPermission> getAppliedPermissions(){
        return appliedPermissions;
    }

    public List<OCLPermission> getRemovedPermissions() {
        return removedPermissions;
    }

    @Override
    public String getLocalizedTitle(Translator translator) {
        if (folder.isDefaultFolder() && null != translator) {
            return translator.translate(getTitle());
        }
        return getTitle();
    }

}
