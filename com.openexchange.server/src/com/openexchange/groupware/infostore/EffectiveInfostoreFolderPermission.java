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

package com.openexchange.groupware.infostore;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link EffectiveInfostoreFolderPermission}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class EffectiveInfostoreFolderPermission {

    private final EffectivePermission permission;
    private final int folderOwner;

    /**
     * Initializes a new {@link EffectiveInfostoreFolderPermission}.
     *
     * @param permission The permission
     * @param folderOwner The folder owner
     */
    public EffectiveInfostoreFolderPermission(EffectivePermission permission, int folderOwner) {
        super();
        this.permission = permission;
        this.folderOwner = folderOwner;
    }

    /**
     * Gets the permission
     *
     * @return The permission
     */
    public EffectivePermission getPermission() {
        return permission;
    }

    /**
     * Gets the optional folder owner identifier
     *
     * @return The folder owner identifier or <code>-1</code>
     */
    public int getFolderOwner() {
        return folderOwner;
    }

    public boolean hasModuleAccess(int folderModule) {
        return permission.hasModuleAccess(folderModule);
    }

    public boolean isFolderAdmin() {
        return permission.isFolderAdmin();
    }

    public void setFolderAdmin(boolean folderAdmin) {
        permission.setFolderAdmin(folderAdmin);
    }

    public void reset() {
        permission.reset();
    }

    public void setGroupPermission(boolean groupPermission) {
        permission.setGroupPermission(groupPermission);
    }

    public void setSystem(int system) {
        permission.setSystem(system);
    }

    public int getFolderPermission() {
        return permission.getFolderPermission();
    }

    public void setName(String name) {
        permission.setName(name);
    }

    public boolean setFolderPermission(int fp) {
        return permission.setFolderPermission(fp);
    }

    public int getReadPermission() {
        return permission.getReadPermission();
    }

    public boolean setReadObjectPermission(int p) {
        return permission.setReadObjectPermission(p);
    }

    public int getWritePermission() {
        return permission.getWritePermission();
    }

    public boolean setWriteObjectPermission(int p) {
        return permission.setWriteObjectPermission(p);
    }

    public int getDeletePermission() {
        return permission.getDeletePermission();
    }

    public boolean setAllObjectPermission(int pr, int pw, int pd) {
        return permission.setAllObjectPermission(pr, pw, pd);
    }

    public boolean setDeleteObjectPermission(int p) {
        return permission.setDeleteObjectPermission(p);
    }

    public boolean setAllPermission(int fp, int opr, int opw, int opd) {
        return permission.setAllPermission(fp, opr, opw, opd);
    }

    public void setEntity(int entity) {
        permission.setEntity(entity);
    }

    public void setFuid(int fuid) {
        permission.setFuid(fuid);
    }

    public boolean isGroupPermission() {
        return permission.isGroupPermission();
    }

    public boolean isFolderVisible() {
        return permission.isFolderVisible();
    }

    public boolean canCreateObjects() {
        return permission.canCreateObjects();
    }

    public boolean canCreateSubfolders() {
        return permission.canCreateSubfolders();
    }

    public boolean canReadOwnObjects() {
        return permission.canReadOwnObjects();
    }

    public boolean canReadAllObjects() {
        return permission.canReadAllObjects();
    }

    public boolean canWriteOwnObjects() {
        return permission.canWriteOwnObjects();
    }

    public boolean canWriteAllObjects() {
        return permission.canWriteAllObjects();
    }

    public boolean canDeleteOwnObjects() {
        return permission.canDeleteOwnObjects();
    }

    public boolean canDeleteAllObjects() {
        return permission.canDeleteAllObjects();
    }

    /**
     * Gets a value indicating whether the permissions are sufficient to share "own" items in the folder or not.
     *
     * @return <code>true</code> if "own" items can be shared, <code>false</code>, otherwise
     */
    public boolean canShareOwnObjects() {
        return permission.canWriteOwnObjects() && FolderObject.TRASH != permission.getFolderType();
    }

    /**
     * Gets a value indicating whether the permissions are sufficient to share all items in the folder or not.
     *
     * @return <code>true</code> if all items can be shared, <code>false</code>, otherwise
     */
    public boolean canShareAllObjects() {
        return permission.canWriteAllObjects() && FolderObject.TRASH != permission.getFolderType();
    }

    public OCLPermission getUnderlyingPermission() {
        return permission.getUnderlyingPermission();
    }

    @Override
    public String toString() {
        return permission.toString();
    }

    public String getName() {
        return permission.getName();
    }

    public int getEntity() {
        return permission.getEntity();
    }

    public int getFuid() {
        return permission.getFuid();
    }

    public int getSystem() {
        return permission.getSystem();
    }

    public boolean isSystem() {
        return permission.isSystem();
    }

    public boolean equalsPermission(OCLPermission op) {
        return permission.equalsPermission(op);
    }



}
