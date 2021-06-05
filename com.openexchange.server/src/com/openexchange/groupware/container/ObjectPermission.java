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

package com.openexchange.groupware.container;

import java.io.Serializable;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;

/**
 * {@link ObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ObjectPermission implements java.security.acl.Permission, Serializable {

    /**
     * The numerical value indicating no object permissions.
     */
    public static final int NONE = 0;

    /**
     * The numerical value indicating read object permissions.
     */
    public static final int READ = 1;

    /**
     * The numerical value indicating write object permissions. This implicitly includes the {@link #READ} permission (this is no bitmask).
     */
    public static final int WRITE = 2;

    /**
     * The numerical value indicating delete object permissions. This implicitly includes the {@link #READ} and {@link #WRITE} permission
     * (this is no bitmask).
     */
    public static final int DELETE = 4;

    private static final long serialVersionUID = 29807878639680055L;

    private int entity;
    private boolean group;
    private int permissions;

    /**
     * Initializes a new {@link ObjectPermission}.
     */
    public ObjectPermission() {
        super();
    }

    /**
     * Initializes a new {@link ObjectPermission}.
     *
     * @param entity The entity associated with this permission
     * @param <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     * @param permissions The numerical permission value (also known as permission bits)
     *
     */
    public ObjectPermission(int entity, boolean group, int permissions) {
        super();
        this.entity = entity;
        this.group = group;
        this.permissions = permissions;
    }

    /**
     * Gets the entity associated with this permission, i.e. either the user ID in case this permission is mapped to a user, or the group
     * ID if it is mapped to a group.
     *
     * @return The permission entity
     */
    public int getEntity() {
        return entity;
    }

    /**
     * Sets the permission entity.
     *
     * @param entity The entity to set, i.e. either the user ID in case this permission is mapped to a user, or the group ID if it is
     *               mapped to a group
     */
    public void setEntity(int entity) {
        this.entity = entity;
    }

    /**
     * Gets a value indicating whether this permission entity represents a group or not.
     *
     * @return <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * Sets the group permission flag.
     *
     * @param group <code>true</code> to indicate a group permission, <code>false</code>, otherwise
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    /**
     * Gets the numerical permission value (also known as permission bits).
     *
     * @return The permissions, usually one of {@link #NONE}, {@link #READ}, {@link #WRITE} or {@link #DELETE}.
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Sets the numerical permission value (also known as permission bits).
     *
     * @param permissions The permissions, usually one of {@link #NONE}, {@link #READ}, {@link #WRITE} or {@link #DELETE}.
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    /**
     * Gets a value indicating whether it's permitted to view and access an item or not.
     *
     * @return <code>true</code> if it's permitted to read, <code>false</code>, otherwise
     */
    public boolean canRead() {
        return permissions >= READ;
    }

    /**
     * Gets a value indicating whether it's permitted to update/change an item or not.
     *
     * @return <code>true</code> if it's permitted to write, <code>false</code>, otherwise
     */
    public boolean canWrite() {
        return permissions >= WRITE;
    }

    /**
     * Gets a value indicating whether it's permitted to delete an item or not.
     *
     * @return <code>true</code> if it's permitted to delete, <code>false</code>, otherwise
     */
    public boolean canDelete() {
        return permissions >= DELETE;
    }

    /**
     * Takes a object permission and converts it into an equivalent folder permission bit mask.
     *
     * @param permission The object permission
     * @return The folder permission bit mask
     */
    public static int toFolderPermissionBits(ObjectPermission permission) {
        BasicPermission fp = new BasicPermission();
        fp.setEntity(permission.entity);
        fp.setGroup(false);
        fp.setAdmin(false);
        fp.setFolderPermission(Permission.READ_FOLDER);
        fp.setReadPermission(permission.canRead() ? Permission.READ_ALL_OBJECTS : Permission.NO_PERMISSIONS);
        fp.setWritePermission(permission.canWrite() ? Permission.WRITE_ALL_OBJECTS : Permission.NO_PERMISSIONS);
        fp.setDeletePermission(permission.canDelete() ? Permission.DELETE_ALL_OBJECTS : Permission.NO_PERMISSIONS);
        return Permissions.createPermissionBits(fp);
    }

    /**
     * Takes a folder permission bit mask and deduces the according object permissions.
     *
     * @param folderPermissionBits The folder permission bit mask
     * @return The object permission bits
     */
    public static int convertFolderPermissionBits(int folderPermissionBits) {
        int objectBits = ObjectPermission.NONE;
        int[] permissionBits = Permissions.parsePermissionBits(folderPermissionBits);
        int rp = permissionBits[1];
        int wp = permissionBits[2];
        int dp = permissionBits[3];
        if (dp >= Permission.DELETE_ALL_OBJECTS) {
            objectBits = ObjectPermission.DELETE;
        } else if (wp >= Permission.WRITE_ALL_OBJECTS) {
            objectBits = ObjectPermission.WRITE;
        } else if (rp >= Permission.READ_ALL_OBJECTS) {
            objectBits = ObjectPermission.READ;
        }

        return objectBits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entity;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + permissions;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ObjectPermission)) {
            return false;
        }
        ObjectPermission other = (ObjectPermission) obj;
        if (entity != other.entity) {
            return false;
        }
        if (group != other.group) {
            return false;
        }
        if (permissions != other.permissions) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ObjectPermission [entity=" + entity + ", group=" + group + ", permissions=" + permissions + "]";
    }

}
