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

package com.openexchange.messaging;

/**
 * {@link DefaultMessagingPermission} - The default messaging permission granting full access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class DefaultMessagingPermission implements MessagingPermission {

    private static final long serialVersionUID = -7004880825414752511L;

    /**
     * Creates a new instance of {@link DefaultMessagingPermission} with {@link #setMaxPermissions()} invoked.
     *
     * @return A new instance of {@link DefaultMessagingPermission} with {@link #setMaxPermissions()} invoked
     */
    public static DefaultMessagingPermission newInstance() {
        final DefaultMessagingPermission dmp = new DefaultMessagingPermission();
        dmp.setMaxPermissions();
        return dmp;
    }

    /*-
     * ----------------------------------------- Member section -----------------------------------------
     */

    private int system;

    private MessagingFolderPermissionType type;

    private int deletePermission;

    private int folderPermission;

    private int readPermission;

    private int writePermission;

    private boolean admin;

    private int entity;

    private boolean group;

    /**
     * Initializes an {@link DefaultMessagingPermission}.
     */
    private DefaultMessagingPermission() {
        super();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (admin ? 1231 : 1237);
        result = prime * result + deletePermission;
        result = prime * result + entity;
        result = prime * result + folderPermission;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + readPermission;
        result = prime * result + system;
        result = prime * result + type.getTypeNumber();
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
        if (!(obj instanceof MessagingPermission)) {
            return false;
        }
        final MessagingPermission other = (MessagingPermission) obj;
        if (admin != other.isAdmin()) {
            return false;
        }
        if (deletePermission != other.getDeletePermission()) {
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
    public int getEntity() {
        return entity;
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
    public void setEntity(final int entity) {
        this.entity = entity;
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
        folderPermission = MessagingPermission.MAX_PERMISSION;
        readPermission = MessagingPermission.MAX_PERMISSION;
        deletePermission = MessagingPermission.MAX_PERMISSION;
        writePermission = MessagingPermission.MAX_PERMISSION;
        admin = true;
    }

    @Override
    public void setNoPermissions() {
        folderPermission = MessagingPermission.NO_PERMISSIONS;
        readPermission = MessagingPermission.NO_PERMISSIONS;
        deletePermission = MessagingPermission.NO_PERMISSIONS;
        writePermission = MessagingPermission.NO_PERMISSIONS;
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
    public MessagingFolderPermissionType getType() {
        return type;
    }

    @Override
    public void setType(MessagingFolderPermissionType type) {
        this.type = type;
    }

}
