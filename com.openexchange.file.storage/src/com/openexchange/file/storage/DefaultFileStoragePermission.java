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

import java.util.Objects;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link DefaultFileStoragePermission} - The default file storage permission granting full access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class DefaultFileStoragePermission implements FileStoragePermission {

    /**
     * Creates a new instance of {@link DefaultFileStoragePermission} with {@link #setMaxPermissions()} invoked.
     *
     * @return A new instance of {@link DefaultFileStoragePermission} with {@link #setMaxPermissions()} invoked
     */
    public static DefaultFileStoragePermission newInstance() {
        final DefaultFileStoragePermission retval = new DefaultFileStoragePermission();
        retval.setMaxPermissions();
        return retval;
    }

    /**
     * Initializes a new {@link DefaultFileStoragePermission}, taking over all properties from another permission.
     * 
     * @param permission The permission to take over the property values from
     * @return The new file storage permission
     */
    public static DefaultFileStoragePermission newInstance(FileStoragePermission permission) {
        DefaultFileStoragePermission storagePermission = DefaultFileStoragePermission.newInstance();
        storagePermission.setAllPermissions(permission.getFolderPermission(), permission.getReadPermission(), permission.getWritePermission(), permission.getDeletePermission());
        storagePermission.setGroup(permission.isGroup());
        storagePermission.setAdmin(permission.isAdmin());
        storagePermission.setSystem(permission.getSystem());
        storagePermission.setType(permission.getType());
        storagePermission.setPermissionLegator(permission.getPermissionLegator());
        storagePermission.setIdentifier(permission.getIdentifier());
        storagePermission.setEntity(permission.getEntity());
        storagePermission.setEntityInfo(permission.getEntityInfo());
        return storagePermission;
    }

    /*-
     * ----------------------------------------- Member section -----------------------------------------
     */

    private int system;

    private FileStorageFolderPermissionType type;

    protected String legator;

    private int deletePermission;

    private int folderPermission;

    private int readPermission;

    private int writePermission;

    private boolean admin;

    private String identifier;

    private int entity;

    private EntityInfo entityInfo;

    private boolean group;

    /**
     * Initializes an {@link DefaultFileStoragePermission}.
     */
    protected DefaultFileStoragePermission() {
        super();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (admin ? 1231 : 1237);
        result = prime * result + deletePermission;
        result = prime * result + entity;
        result = prime * result + (null != identifier ? identifier.hashCode() : 0);
        result = prime * result + folderPermission;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + readPermission;
        result = prime * result + system;
        result = prime * result + type.getTypeNumber();
        result = prime * result + (legator==null ? 0 : legator.hashCode());
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
        if (!(obj instanceof FileStoragePermission)) {
            return false;
        }
        final FileStoragePermission other = (FileStoragePermission) obj;
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
        if (type != other.getType()) {
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

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void setEntity(final int entity) {
        this.entity = entity;
    }

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
        folderPermission = FileStoragePermission.MAX_PERMISSION;
        readPermission = FileStoragePermission.MAX_PERMISSION;
        deletePermission = FileStoragePermission.MAX_PERMISSION;
        writePermission = FileStoragePermission.MAX_PERMISSION;
        admin = true;
    }

    @Override
    public void setNoPermissions() {
        folderPermission = FileStoragePermission.NO_PERMISSIONS;
        readPermission = FileStoragePermission.NO_PERMISSIONS;
        deletePermission = FileStoragePermission.NO_PERMISSIONS;
        writePermission = FileStoragePermission.NO_PERMISSIONS;
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
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    @Override
    public FileStorageFolderPermissionType getType() {
        return type;
    }

    @Override
    public void setType(FileStorageFolderPermissionType type) {
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
