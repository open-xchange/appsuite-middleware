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

package com.openexchange.file.storage;

import com.openexchange.groupware.EntityInfo;

/**
 * {@link DefaultFileStorageObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultFileStorageObjectPermission implements FileStorageObjectPermission {

    private String identifier;
    private int entity;
    private EntityInfo entityInfo;
    private boolean group;
    private int permissions;

    /**
     * Initializes a new {@link DefaultFileStorageObjectPermission}.
     */
    public DefaultFileStorageObjectPermission() {
        super();
    }

    /**
     * Initializes a new {@link DefaultFileStorageObjectPermission}.
     * <p/>
     * The passed entity is implicitly applied as qualified <i>identifier</i>, too.
     *
     * @param entity The entity associated with this permission
     * @param group <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     * @param permissions The numerical permission value (also known as permission bits)
     */
    public DefaultFileStorageObjectPermission(int entity, boolean group, int permissions) {
        this(String.valueOf(entity), entity, group, permissions);
    }

    /**
     * Initializes a new {@link DefaultFileStorageObjectPermission}.
     *
     * @param identifier The qualified identifier of the entity associated with this permission
     * @param entity The entity associated with this permission
     * @param group <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     * @param permissions The numerical permission value (also known as permission bits)
     */
    public DefaultFileStorageObjectPermission(String identifier, int entity, boolean group, int permissions) {
        super();
        this.identifier = identifier;
        this.entity = entity;
        this.group = group;
        this.permissions = permissions;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier
     * 
     * @param identifier The qualified identifier of the entity associated with this permission
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
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

    @Override
    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    /**
     * Sets the entity info.
     * 
     * @param entityInfo The additional information about the entity associated with this permission
     */
    public void setEntityInfo(EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
    }

    @Override
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

    @Override
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

    @Override
    public boolean canRead() {
        return permissions >= READ;
    }

    @Override
    public boolean canWrite() {
        return permissions >= WRITE;
    }

    @Override
    public boolean canDelete() {
        return permissions >= DELETE;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultFileStorageObjectPermission)) {
            return false;
        }
        DefaultFileStorageObjectPermission other = (DefaultFileStorageObjectPermission) obj;
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
        return "DefaultFileStorageObjectPermission [entity=" + entity + ", group=" + group + ", permissions=" + permissions + "]";
    }

}
