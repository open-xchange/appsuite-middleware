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

package com.openexchange.folderstorage;

import java.util.Objects;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link BasicPermission}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class BasicPermission implements Permission, Cloneable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7760512838927816558L;

    protected String identifier;

    protected int entity = -1;

    protected EntityInfo entityInfo;

    protected boolean group;

    protected int system;

    protected FolderPermissionType type;

    protected String legator;

    protected boolean admin;

    protected int folderPermission;

    protected int readPermission;

    protected int writePermission;

    protected int deletePermission;

    /**
     * Initializes an empty {@link DefaultPermission}.
     */
    public BasicPermission() {
        super();
        setNoPermissionInternal();
    }

    /**
     * Initializes a new {@link DefaultPermission} set to the given
     * entity and permission bits.
     *
     * @param entity
     * @param isGroup
     * @param permissionBits
     */
    public BasicPermission(int entity, boolean isGroup, int permissionBits) {
        super();
        this.identifier = String.valueOf(entity);
        this.entity = entity;
        this.group = isGroup;
        int[] permissions = Permissions.parsePermissionBits(permissionBits);
        folderPermission = permissions[0];
        readPermission = permissions[1];
        writePermission = permissions[2];
        deletePermission = permissions[3];
        admin = permissions[4] > 0;
    }

    /**
     * Copy constructor
     */
    public BasicPermission(Permission permission) {
        super();
        identifier = permission.getIdentifier();
        entity = permission.getEntity();
        entityInfo = permission.getEntityInfo();
        group = permission.isGroup();
        system = permission.getSystem();
        type = permission.getType();
        legator = permission.getPermissionLegator();
        admin = permission.isAdmin();
        folderPermission = permission.getFolderPermission();
        readPermission = permission.getReadPermission();
        writePermission = permission.getWritePermission();
        deletePermission = permission.getDeletePermission();
    }

    @Override
    public boolean isVisible() {
        return isAdmin() || getFolderPermission() > NO_PERMISSIONS;
    }

    @Override
    public int getDeletePermission() {
        return deletePermission;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getEntity() {
        return entity;
    }

    @Override
    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    @Override
    public int getFolderPermission() {
        return folderPermission;
    }

    @Override
    public int getReadPermission() {
        return readPermission;
    }

    @Override
    public int getSystem() {
        return system;
    }

    @Override
    public int getWritePermission() {
        return writePermission;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public boolean isGroup() {
        return group;
    }

    @Override
    public void setAdmin(final boolean admin) {
        this.admin = admin;
    }

    @Override
    public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
        this.folderPermission = folderPermission;
        this.readPermission = readPermission;
        this.deletePermission = deletePermission;
        this.writePermission = writePermission;
    }

    @Override
    public void setDeletePermission(final int permission) {
        deletePermission = permission;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void setEntity(final int entity) {
        this.entity = entity;
    }

    @Override
    public void setEntityInfo(EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
    }

    @Override
    public void setFolderPermission(final int permission) {
        folderPermission = permission;
    }

    @Override
    public void setGroup(final boolean group) {
        this.group = group;
    }

    @Override
    public void setMaxPermissions() {
        folderPermission = Permission.MAX_PERMISSION;
        readPermission = Permission.MAX_PERMISSION;
        deletePermission = Permission.MAX_PERMISSION;
        writePermission = Permission.MAX_PERMISSION;
        admin = true;
    }

    @Override
    public void setNoPermissions() {
        setNoPermissionInternal();
    }

    /**
     * Initializes the permissions with no permission
     */
    private final void setNoPermissionInternal() {
        folderPermission = Permission.NO_PERMISSIONS;
        readPermission = Permission.NO_PERMISSIONS;
        deletePermission = Permission.NO_PERMISSIONS;
        writePermission = Permission.NO_PERMISSIONS;
        admin = false;
    }

    @Override
    public void setReadPermission(final int permission) {
        readPermission = permission;
    }

    @Override
    public void setSystem(final int system) {
        this.system = system;
    }

    @Override
    public void setWritePermission(final int permission) {
        writePermission = permission;
    }

    @Override
    public Object clone() {
        try {
            BasicPermission clone = (BasicPermission) super.clone();
            clone.identifier = getIdentifier();
            clone.entity = getEntity();
            clone.entityInfo = getEntityInfo();
            clone.group = isGroup();
            clone.system = getSystem();
            clone.type = getType();
            clone.legator = getPermissionLegator();
            clone.admin = isAdmin();
            clone.folderPermission = getFolderPermission();
            clone.readPermission = getReadPermission();
            clone.writePermission = getWritePermission();
            clone.deletePermission = getDeletePermission();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Error although Cloneable is implemented");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (admin ? 1231 : 1237);
        result = prime * result + deletePermission;
        result = prime * result + (null != identifier ? identifier.hashCode() : 0);
        result = prime * result + entity;
        result = prime * result + folderPermission;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + readPermission;
        result = prime * result + system;
        result = prime * result + (null != type ? type : FolderPermissionType.NORMAL).getTypeNumber();
        result = prime * result + (legator == null ? 0 : legator.hashCode());
        result = prime * result + writePermission;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Permission)) {
            return false;
        }

        final Permission other = (Permission) obj;
        if (admin != other.isAdmin()) {
            return false;
        }
        if (deletePermission != other.getDeletePermission()) {
            return false;
        }
        if (false == Objects.equals(identifier, other.getIdentifier())) {
            return false;
        }
        if (entity != other.getEntity()) {
            return false;
        }
        if (folderPermission != other.getFolderPermission()) {
            return false;
        }
        if (group != other.isGroup()) {
            return false;
        }
        if (readPermission != other.getReadPermission()) {
            return false;
        }
        if (system != other.getSystem()) {
            return false;
        }
        if ((null != type ? type : FolderPermissionType.NORMAL) != (null != other.getType() ? other.getType() : FolderPermissionType.NORMAL)) {
            return false;
        }
        if ((legator==null && other.getPermissionLegator()!=null) || (legator!=null && !legator.equals(other.getPermissionLegator()))) {
            return false;
        }
        if (writePermission != other.getWritePermission()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Permission [identifier=" + identifier + ", entity=" + entity + ", group=" + group + ", admin=" + admin + ", system=" + system + ", folderPermission=" + folderPermission + ", readPermission=" + readPermission + ", writePermission=" + writePermission + ", deletePermission=" + deletePermission + "]";
    }

    @Override
    public FolderPermissionType getType() {
        return type;
    }

    @Override
    public void setType(FolderPermissionType type) {
        this.type = type;
    }

    @Override
    public String getPermissionLegator() {
        return legator;
    }

    @Override
    public void setPermissionLegator(String legator) {
        this.legator = legator;
    }
}
