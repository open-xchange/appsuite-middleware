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
 * {@link FileStorageObjectPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileStorageObjectPermission {

    /**
     * The numerical value indicating no object permissions.
     */
    static final int NONE = 0;

    /**
     * The numerical value indicating read object permissions.
     */
    static final int READ = 1;

    /**
     * The numerical value indicating write object permissions. This implicitly includes the {@link #READ} permission (this is no bitmask).
     */
    static final int WRITE = 2;

    /**
     * The numerical value indicating delete object permissions. This implicitly includes the {@link #READ} and {@link #WRITE} permission
     * (this is no bitmask).
     */
    static final int DELETE = 4;

    /**
     * Gets the qualified identifier of the entity associated with this permission.
     * 
     * @return The identifier
     */
    String getIdentifier();

    /**
     * Gets the entity associated with this permission, i.e. either the user ID in case this permission is mapped to a user, or the group
     * ID if it is mapped to a group.
     *
     * @return The permission entity
     */
    int getEntity();

    /**
     * Gets additional information about the entity associated with this permission.
     * 
     * @return The entity info, or <code>null</code> if not available
     */
    EntityInfo getEntityInfo();

    /**
     * Gets a value indicating whether this permission entity represents a group or not.
     *
     * @return <code>true</code> if this permission represents a group, <code>false</code>, otherwise
     */
    boolean isGroup();

    /**
     * Gets the numerical permission value (also known as permission bits).
     *
     * @return The permissions, usually one of {@link #NONE}, {@link #READ}, {@link #WRITE} or {@link #DELETE}.
     */
    int getPermissions();

    /**
     * Gets a value indicating whether it's permitted to view and access an item or not.
     *
     * @return <code>true</code> if it's permitted to read, <code>false</code>, otherwise
     */
    boolean canRead();

    /**
     * Gets a value indicating whether it's permitted to update/change an item or not.
     *
     * @return <code>true</code> if it's permitted to write, <code>false</code>, otherwise
     */
    boolean canWrite();

    /**
     * Gets a value indicating whether it's permitted to delete an item or not.
     *
     * @return <code>true</code> if it's permitted to delete, <code>false</code>, otherwise
     */
    boolean canDelete();

}
