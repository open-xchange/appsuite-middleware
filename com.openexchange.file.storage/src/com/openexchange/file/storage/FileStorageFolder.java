/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.file.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
     * The capability identifier for publication support.
     */
    public static final String CAPABILITY_PUBLICATION = "publication";

    /**
     * All known capabilities in a set.
     */
    public static final Set<String> ALL_CAPABILITIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
        CAPABILITY_PERMISSIONS, CAPABILITY_PUBLICATION, CAPABILITY_QUOTA, CAPABILITY_SORT, CAPABILITY_SUBSCRIPTION }
    )));

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

}
