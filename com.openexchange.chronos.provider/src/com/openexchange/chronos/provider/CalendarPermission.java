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

package com.openexchange.chronos.provider;

import com.openexchange.groupware.EntityInfo;

/**
 * {@link CalendarPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarPermission extends Cloneable {

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
    Object clone();

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Gets this folder permission's system bit mask.
     *
     * @return This folder permission's system bit mask
     */
    int getSystem();

    /**
     * Sets this folder permission's system bit mask.
     *
     * @param system This folder permission's system bit mask
     */
    void setSystem(int system);

    /**
     * Checks if this folder permission's entity is a group.
     *
     * @return <code>true</code> if this folder permission's entity is a group; otherwise <code>false</code>
     */
    boolean isGroup();

    /**
     * Sets if this folder permission's entity is a group.
     *
     * @param group <code>true</code> if this folder permission's entity is a group; otherwise <code>false</code>
     */
    void setGroup(boolean group);

    /**
     * Gets the qualified identifier of the entity associated with this permission.
     * 
     * @return The identifier
     */
    String getIdentifier();

    /**
     * Sets the qualified identifier of the entity associated with this permission.
     * 
     * @param identifier The identifier to set
     */
    void setIdentifier(String identifier);

    /**
     * Gets this folder permission's entity identifier.
     *
     * @return This folder permission's entity identifier
     */
    int getEntity();

    /**
     * Sets this folder permission's entity identifier.
     *
     * @param entity The entity identifier
     */
    void setEntity(int entity);

    /**
     * Gets additional information about the entity associated with this permission.
     * 
     * @return The entity info, or <code>null</code> if not available
     */
    EntityInfo getEntityInfo();

    /**
     * Sets additional information about the entity associated with this permission.
     * 
     * @param entityInfo The entity info to set
     */
    void setEntityInfo(EntityInfo entityInfo);

    /**
     * Checks if this folder permission denotes its entity as a folder administrator.
     *
     * @return <code>true</code> if this folder permission's entity is a folder administrator; otherwise <code>false</code>
     */
    boolean isAdmin();

    /**
     * Sets if this folder permission denotes its entity as a folder administrator.
     *
     * @param admin <code>true</code> if this folder permission's entity is a folder administrator; otherwise <code>false</code>
     */
    void setAdmin(boolean admin);

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
    int getFolderPermission();

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
    void setFolderPermission(int permission);

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
    int getReadPermission();

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
    void setReadPermission(int permission);

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
    int getWritePermission();

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
    void setWritePermission(int permission);

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
    int getDeletePermission();

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
    void setDeletePermission(int permission);

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
    void setAllPermissions(int folderPermission, int readPermission, int writePermission, int deletePermission);

    /**
     * Convenience method which passes {@link #MAX_PERMISSION} to all permissions and sets folder administrator flag to <code>true</code>.
     */
    void setMaxPermissions();

    /**
     * Convenience method which passes {@link #NO_PERMISSIONS} to all permissions and sets folder administrator flag to <code>false</code>.
     */
    void setNoPermissions();

}
