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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link FileStorageFolder} - Represents a file storage folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageFolder extends FileStorageConstants {

    /**
     * The constant for full name of an account's root folder.
     */
    public static final String ROOT_FULLNAME = "";

    /**
     * The capability identifier for permissions support.
     */
    public static final String CAPABILITY_PERMISSIONS = "permissions";

    /**
     * The capability identifier for quota support.
     */
    public static final String CAPABILITY_QUOTA = "quota";

    /**
     * The capability identifier for sort support.
     */
    public static final String CAPABILITY_SORT = "sort";

    /**
     * The capability identifier for subscription support.
     */
    public static final String CAPABILITY_SUBSCRIPTION = "subscription";

    /**
     * All known capabilities in a set.
     */
    public static final Set<String> ALL_CAPABILITIES = ImmutableSet.of(
        CAPABILITY_PERMISSIONS, CAPABILITY_QUOTA, CAPABILITY_SORT, CAPABILITY_SUBSCRIPTION
    );

    /**
     * Gets the capabilities of this folder; e.g <code>"QUOTA"</code>, <code>"PERMISSIONS"</code>, etc.
     *
     * @return The list of capabilities
     */
    Set<String> getCapabilities();

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the name.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the locale-sensitive display name of folder.
     *
     * @param locale The locale
     * @return The localized name, or <code>null</code> if not available
     */
    String getLocalizedName(Locale locale);

    /**
     * Gets the permission for currently logged-in user accessing this folder
     * <p>
     * The returned permission should reflect user's permission regardless if file storage system supports permissions or not. An instance
     * of {@link DefaultFileStoragePermission} is supposed to be returned on missing permissions support except for the root folder. The
     * root folder should indicate no object permissions in any case, but the folder permission varies if file storage system allows
     * subfolder creation below root folder or not. The returned permission must reflect the allowed behavior.
     *
     * @return The own permission
     */
    FileStoragePermission getOwnPermission();

    /**
     * Gets the parent identifier or <code>null</code> if this file storage folder denotes the root folder.
     *
     * @return The parent identifier or <code>null</code> if this file storage folder denotes the root folder
     */
    String getParentId();

    /**
     * Gets the permissions associated with this file storage folder.
     *
     * @return The permissions as a collection of {@link FileStoragePermission}
     */
    List<FileStoragePermission> getPermissions();

    /**
     * Checks if this file storage folder has subfolders.
     *
     * @return <code>true</code> if this file storage folder has subfolders; otherwise <code>false</code>
     */
    boolean hasSubfolders();

    /**
     * Checks if this file storage folder has subscribed subfolders.
     *
     * @return <code>true</code> if this file storage folder has subscribed subfolders; otherwise <code>false</code>
     */
    boolean hasSubscribedSubfolders();

    /**
     * Checks whether the denoted file storage folder is subscribed or not.
     * <p>
     * If file storage system does not support subscription, <code>true</code> is supposed to be returned.
     *
     * @return Whether the denoted file storage folder is subscribed or not
     */
    boolean isSubscribed();

    /**
     * Gets the creation date.
     *
     * @return The creation date
     */
    Date getCreationDate();

    /**
     * Gets the last modified date.
     *
     * @return The last modified date
     */
    Date getLastModifiedDate();

    /**
     * Checks if this folder is able to hold folders.
     *
     * @return <code>true</code> if this folder is able to hold folders; otherwise <code>false</code>
     */
    boolean isHoldsFolders();

    /**
     * Checks if this folder is able to hold files.
     *
     * @return <code>true</code> if this folder is able to hold files; otherwise <code>false</code>
     */
    boolean isHoldsFiles();

    /**
     * Checks if this folder denotes the root folder
     *
     * @return <code>true</code> if this folder denotes the root folder; otherwise <code>false</code>
     */
    boolean isRootFolder();

    /**
     * Checks if this folder denotes a default folder.
     *
     * @return <code>true</code> if this folder denotes a default folder; otherwise <code>false</code>
     */
    boolean isDefaultFolder();

    /**
     * Gets the number of files.
     *
     * @return The number of files or <code>-1</code> if this folder does not hold files
     * @see #isHoldsFiles()
     */
    int getFileCount();

    /**
     * Gets the properties associated with this folder.
     *
     * @return The properties
     */
    Map<String, Object> getProperties();

    /**
     * Gets dynamic metadata
     */
    Map<String, Object> getMeta();

    /**
     * Gets the identifier of the entity who created this folder.
     *
     * @return The entity, or <code>-1</code> if not available
     */
    int getCreatedBy();

    /**
     * Gets the identifier of the entity who updated this folder.
     *
     * @return The entity, or <code>-1</code> if not available
     */
    int getModifiedBy();

    /**
     * Gets the entity info from folder creator
     *
     * @return The entity info
     */
    EntityInfo getCreatedFrom();

    /**
     * Gets the entity info from latest modificator
     *
     * @return The entity info
     */
    EntityInfo getModifiedFrom();

    /**
     * Gets an account error related to the folder.
     *
     * @return The account error of the folder
     */
    default OXException getAccountError() {
       return null;
    }

    /**
     * Gets the possible delegate object.
     *
     * @return The delegate object or <code>null</code>
     */
    default Object getDelegate() {
        return null;
    }
}
