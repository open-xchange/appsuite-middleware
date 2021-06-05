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

import java.io.Serializable;

/**
 * {@link MessagingPermission} - Represents a messaging permission.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingPermission extends Cloneable, Serializable {

    /**
     * The constant for no permission at all.
     */
    public static final int NO_PERMISSIONS = 0;

    /**
     * The constant for maximum permission.
     */
    public static final int MAX_PERMISSION = 128;

    /**
     * The permission constant granting folder visibility.
     */
    public static final int READ_FOLDER = 2;

    /**
     * The permission constant granting folder visibility and allowing to create objects in folder.
     */
    public static final int CREATE_OBJECTS_IN_FOLDER = 4;

    /**
     * The permission constant granting folder visibility, allowing to create objects in folder, and allowing to create subfolders below
     * folder.
     */
    public static final int CREATE_SUB_FOLDERS = 8;

    /**
     * The permission constant granting visibility for own objects.
     */
    public static final int READ_OWN_OBJECTS = 2;

    /**
     * The permission constant granting visibility for all objects.
     */
    public static final int READ_ALL_OBJECTS = 4;

    /**
     * The permission constant allowing to edit own objects.
     */
    public static final int WRITE_OWN_OBJECTS = 2;

    /**
     * The permission constant allowing to edit all objects.
     */
    public static final int WRITE_ALL_OBJECTS = 4;

    /**
     * The permission constant allowing to remove own objects.
     */
    public static final int DELETE_OWN_OBJECTS = 2;

    /**
     * The permission constant allowing to remove all objects.
     */
    public static final int DELETE_ALL_OBJECTS = 4;

    /*-
     * #################### METHODS ####################
     */

    /**
     * Creates and returns a copy of this object.
     *
     * @return A clone of this instance.
     */
    public Object clone();

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Gets this folder permission's system bit mask.
     *
     * @return This folder permission's system bit mask
     */
    public int getSystem();

    /**
     * Sets this folder permission's system bit mask.
     *
     * @param system This folder permission's system bit mask
     */
    public void setSystem(int system);

    /**
     * Gets this folder permission's type.
     *
     * @return This folder permission's type.
     */
    public MessagingFolderPermissionType getType();

    /**
     * Sets this folder permission's type.
     *
     * @param type This folder permission's type.
     */
    public void setType(MessagingFolderPermissionType type);

    /**
     * Checks if this folder permission's entity is a group.
     *
     * @return <code>true</code> if this folder permission's entity is a group; otherwise <code>false</code>
     */
    public boolean isGroup();

    /**
     * Sets if this folder permission's entity is a group.
     *
     * @param group <code>true</code> if this folder permission's entity is a group; otherwise <code>false</code>
     */
    public void setGroup(boolean group);

    /**
     * Gets this folder permission's entity identifier.
     *
     * @return This folder permission's entity identifier
     */
    public int getEntity();

    /**
     * Sets this folder permission's entity identifier.
     *
     * @param entity The entity identifier
     */
    public void setEntity(int entity);

    /**
     * Checks if this folder permission denotes its entity as a folder administrator.
     *
     * @return <code>true</code> if this folder permission's entity is a folder administrator; otherwise <code>false</code>
     */
    public boolean isAdmin();

    /**
     * Sets if this folder permission denotes its entity as a folder administrator.
     *
     * @param admin <code>true</code> if this folder permission's entity is a folder administrator; otherwise <code>false</code>
     */
    public void setAdmin(boolean admin);

    /**
     * Gets the folder permission.
     * <p>
     * Returned value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #READ_FOLDER}</li>
     * <li>{@link #CREATE_OBJECTS_IN_FOLDER}</li>
     * <li>{@link #CREATE_SUB_FOLDERS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @return The folder permission
     */
    public int getFolderPermission();

    /**
     * Sets the folder permission.
     * <p>
     * Passed value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #READ_FOLDER}</li>
     * <li>{@link #CREATE_OBJECTS_IN_FOLDER}</li>
     * <li>{@link #CREATE_SUB_FOLDERS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @param permission The folder permission
     */
    public void setFolderPermission(int permission);

    /**
     * Gets the read permission.
     * <p>
     * Returned value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #READ_OWN_OBJECTS}</li>
     * <li>{@link #READ_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @return The read permission
     */
    public int getReadPermission();

    /**
     * Sets the read permission.
     * <p>
     * Passed value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #READ_OWN_OBJECTS}</li>
     * <li>{@link #READ_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @param permission The read permission
     */
    public void setReadPermission(int permission);

    /**
     * Gets the write permission.
     * <p>
     * Returned value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #WRITE_OWN_OBJECTS}</li>
     * <li>{@link #WRITE_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @return The write permission
     */
    public int getWritePermission();

    /**
     * Sets the write permission.
     * <p>
     * Passed value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #WRITE_OWN_OBJECTS}</li>
     * <li>{@link #WRITE_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @param permission The write permission
     */
    public void setWritePermission(int permission);

    /**
     * Gets the delete permission.
     * <p>
     * Returned value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #DELETE_OWN_OBJECTS}</li>
     * <li>{@link #DELETE_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @return The delete permission
     */
    public int getDeletePermission();

    /**
     * Sets the delete permission.
     * <p>
     * Passed value is one of:
     * <ul>
     * <li>{@link #NO_PERMISSIONS}</li>
     * <li>{@link #DELETE_OWN_OBJECTS}</li>
     * <li>{@link #DELETE_ALL_OBJECTS}</li>
     * <li>{@link #MAX_PERMISSION}</li>
     * </ul>
     *
     * @param permission The delete permission
     */
    public void setDeletePermission(int permission);

    /**
     * Convenience method to set all permissions at once.
     *
     * @param folderPermission The folder permission
     * @param readPermission The read permission
     * @param writePermission The write permission
     * @param deletePermission The delete permission
     * @see #setFolderPermission(int)
     * @see #setReadPermission(int)
     * @see #setWritePermission(int)
     * @see #setDeletePermission(int)
     */
    public void setAllPermissions(int folderPermission, int readPermission, int writePermission, int deletePermission);

    /**
     * Convenience method which passes {@link #MAX_PERMISSION} to all permissions and sets folder administrator flag to <code>true</code>.
     */
    public void setMaxPermissions();

    /**
     * Convenience method which passes {@link #NO_PERMISSIONS} to all permissions and sets folder administrator flag to <code>false</code>.
     */
    public void setNoPermissions();

}
