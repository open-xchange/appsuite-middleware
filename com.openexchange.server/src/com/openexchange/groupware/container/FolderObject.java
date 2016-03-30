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

package com.openexchange.groupware.container;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.OXCloneable;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.OXFolderUtility;

/**
 * {@link FolderObject} - Represents a folder.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.org">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> - generic methods
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - added fields to generic methods
 */
public class FolderObject extends FolderChildObject implements Cloneable {

    private static final long serialVersionUID = 1019652520335292041L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderObject.class);

    /**
     * Gets the locale-specific folder name
     *
     * @param id The folder ID
     * @param locale The locale
     * @return The locale-specific folder name or <code>null</code> if no locale-specific folder name is available
     */
    public static String getFolderString(final int id, final Locale locale) {
        final StringHelper strHelper = StringHelper.valueOf(locale);
        switch (id) {
        case SYSTEM_PRIVATE_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME);
        case SYSTEM_PUBLIC_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME);
        case SYSTEM_SHARED_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME);
        case SYSTEM_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_FOLDER_NAME);
        case SYSTEM_GLOBAL_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_GLOBAL_FOLDER_NAME);
        case SYSTEM_LDAP_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
        case SYSTEM_OX_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_OX_FOLDER_NAME);
        case SYSTEM_INFOSTORE_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME);
        case SYSTEM_USER_INFOSTORE_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME);
        case SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID:
            return strHelper.getString(FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME);
        case VIRTUAL_LIST_TASK_FOLDER_ID:
            return strHelper.getString(FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME);
        case VIRTUAL_LIST_CALENDAR_FOLDER_ID:
            return strHelper.getString(FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME);
        case VIRTUAL_LIST_CONTACT_FOLDER_ID:
            return strHelper.getString(FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME);
        case VIRTUAL_LIST_INFOSTORE_FOLDER_ID:
            return strHelper.getString(FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME);
        default:
            return null;
        }
    }

    // Constants for system folders per context
    public static final int SYSTEM_ROOT_FOLDER_ID = 0;

    public static final int SYSTEM_PRIVATE_FOLDER_ID = 1;

    public static final int SYSTEM_PUBLIC_FOLDER_ID = 2;

    public static final int SYSTEM_SHARED_FOLDER_ID = 3;

    public static final int SYSTEM_FOLDER_ID = 4;

    public static final int SYSTEM_GLOBAL_FOLDER_ID = 5;

    public static final int SYSTEM_LDAP_FOLDER_ID = 6;

    public static final int SYSTEM_OX_FOLDER_ID = 7;

    public static final int SYSTEM_INFOSTORE_FOLDER_ID = 9;

    public static final int SYSTEM_USER_INFOSTORE_FOLDER_ID = 10;

    public static final int SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID = 15;

    public static final int VIRTUAL_LIST_TASK_FOLDER_ID = 11;

    public static final int VIRTUAL_LIST_CALENDAR_FOLDER_ID = 12;

    public static final int VIRTUAL_LIST_CONTACT_FOLDER_ID = 13;

    public static final int VIRTUAL_LIST_INFOSTORE_FOLDER_ID = 14;

    public static final int VIRTUAL_GUEST_CONTACT_FOLDER_ID = 16;

    public static final int MIN_FOLDER_ID = 20;

    // Constant identifier for system folders
    public static final String SYSTEM_PRIVATE_FOLDER_NAME = "private";

    public static final String SYSTEM_PUBLIC_FOLDER_NAME = "public";

    public static final String SYSTEM_SHARED_FOLDER_NAME = "shared";

    public static final String SYSTEM_FOLDER_NAME = "system";

    public static final String SYSTEM_GLOBAL_FOLDER_NAME = "system_global";

    public static final String SYSTEM_LDAP_FOLDER_NAME = "system_ldap";

    public static final String SYSTEM_OX_FOLDER_NAME = "user";

    public static final String SYSTEM_INFOSTORE_FOLDER_NAME = "infostore";

    public static final String SYSTEM_USER_INFOSTORE_FOLDER_NAME = "userstore";

    public static final String SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME = "public_infostore";

    /**
     * The UID prefix of a virtual shared folder
     */
    public static final String SHARED_PREFIX = "u:";

    // Constants for folder fields
    public static final int FOLDER_NAME = 300;

    public static final int MODULE = 301;

    public static final int TYPE = 302;

    public static final int SUBFOLDERS = 304;

    public static final int OWN_RIGHTS = 305;

    public static final int PERMISSIONS_BITS = 306;

    public static final int SUMMARY = 307;

    public static final int STANDARD_FOLDER = 308;

    public static final int TOTAL = 309;

    public static final int NEW = 310;

    public static final int UNREAD = 311;

    public static final int DELETED = 312;

    public static final int CAPABILITIES = 313;

    public static final int SUBSCRIBED = 314;

    public static final int SUBSCR_SUBFLDS = 315;

    public static final int[] ALL_COLUMNS =
    {
        // From FolderObject itself
        FOLDER_NAME, MODULE, TYPE, SUBFOLDERS, OWN_RIGHTS, PERMISSIONS_BITS, SUMMARY, STANDARD_FOLDER, TOTAL, NEW, UNREAD, DELETED,
        CAPABILITIES, SUBSCRIBED, SUBSCR_SUBFLDS,
        // From FolderChildObject
        FOLDER_ID,
        // From DataObject
        OBJECT_ID, CREATED_BY, MODIFIED_BY, CREATION_DATE, LAST_MODIFIED, LAST_MODIFIED_UTC };

    // Modules
    public static final int TASK = 1;

    public static final int CALENDAR = 2;

    public static final int CONTACT = 3;

    public static final int UNBOUND = 4;

    public static final int SYSTEM_MODULE = 5;

    public static final int MAIL = 7;

    public static final int INFOSTORE = 8;

    public static final int MESSAGING = 13;

    public static final int FILE = 14;

    // Types
    public static final int PRIVATE = 1;

    public static final int PUBLIC = 2;

    public static final int SHARED = 3;

    public static final int TRASH = 16;

    public static final int PICTURES = 20;

    public static final int DOCUMENTS = 21;

    public static final int MUSIC = 22;

    public static final int VIDEOS = 23;

    public static final int TEMPLATES = 24;

    public static final int SYSTEM_TYPE = SYSTEM_MODULE; // Formerly 6;

    private static final int[] SORTED_TYPES = { PRIVATE, PUBLIC, SHARED };

    /**
     * Tests if specified type is valid; meaning either {@link #PRIVATE}, {@link #PUBLIC}, or {@link #SHARED}.
     *
     * @param type The folder type to test
     * @return <code>true</code> if specified type is valid; otherwise <code>false</code>
     */
    public static final boolean isValidFolderType(final int type) {
        return (Arrays.binarySearch(SORTED_TYPES, type) >= 0);
    }

    // SQL string for standard modules
    public static final String SQL_IN_STR_STANDARD_MODULES =
        new StringBuilder().append('(').append(TASK).append(',').append(CALENDAR).append(',').append(CONTACT).append(',').append(UNBOUND).append(
            ',').append(INFOSTORE).append(')').toString();

    // SQL string for standard modules including system module
    public static final String SQL_IN_STR_STANDARD_MODULES_ALL =
        new StringBuilder().append('(').append(TASK).append(',').append(CALENDAR).append(',').append(CONTACT).append(',').append(UNBOUND).append(
            ',').append(SYSTEM_MODULE).append(',').append(INFOSTORE).append(')').toString();

    // Permissions
    public static final int PRIVATE_PERMISSION = 1;

    public static final int PUBLIC_PERMISSION = 2;

    public static final int CUSTOM_PERMISSION = 3;

    // Variables
    protected String folderName;

    protected boolean b_folderName;

    protected int module;

    protected boolean b_module;

    protected int type;

    protected boolean b_type;

    protected boolean defaultFolder;

    protected boolean b_defaultFolder;

    protected int permissionFlag;

    protected boolean b_permissionFlag;

    protected ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>();

    protected boolean b_permissions;

    protected boolean subfolderFlag;

    protected boolean b_subfolderFlag;

    protected ArrayList<Integer> subfolderIds = new ArrayList<Integer>();

    protected boolean b_subfolderIds;

    protected String fullName;

    protected boolean b_fullName;

    /**
     * Initializes a new {@link FolderObject}
     */
    public FolderObject() {
        super();
        topic = "ox/common/folder";
    }

    /**
     * Initializes a new {@link FolderObject}
     *
     * @param objectId The folder's object ID
     */
    public FolderObject(final int objectId) {
        super();
        this.objectId = objectId;
        b_objectId = true;
        topic = "ox/common/folder";
    }

    /**
     * Initializes a new {@link FolderObject}
     *
     * @param folderName The folder name
     * @param objectId The object ID
     * @param module The module; {@link #TASK}, {@link #CALENDAR}, {@link #CONTACT} , {@link #UNBOUND}, {@link #SYSTEM_MODULE},
     *            {@link #MAIL}, or {@link #INFOSTORE}
     * @param type The type; {@link #PRIVATE}, {@link #PUBLIC}, or {@link #SYSTEM_TYPE}
     * @param creator The folder creator
     */
    public FolderObject(final String folderName, final int objectId, final int module, final int type, final int creator) {
        super();
        this.folderName = folderName;
        b_folderName = true;
        this.module = module;
        b_module = true;
        this.type = type;
        b_type = true;
        this.objectId = objectId;
        b_objectId = true;
        createdBy = creator;
        b_createdBy = true;
        topic = "ox/common/folder";
    }

    /**
     * Checks if this folder denotes a user's default folder
     *
     * @return <code>true</code> if this folder denotes a user's default folder; otherwise <code>false</code>
     */
    public boolean isDefaultFolder() {
        return defaultFolder;
    }

    /**
     * Checks if default folder status has been set in this folder object
     *
     * @return <code>true</code> if default folder status has been set in this folder object; otherwise <code>false</code>
     */
    public boolean containsDefaultFolder() {
        return b_defaultFolder;
    }

    /**
     * Sets the default folder status for this folder object
     *
     * @param defaultFolder <code>true</code> if this folder denotes a user's default folder; otherwise <code>false</code>
     */
    public void setDefaultFolder(final boolean defaultFolder) {
        this.defaultFolder = defaultFolder;
        b_defaultFolder = true;
    }

    /**
     * Removes the default folder status from this folder object
     */
    public void removeDefaultFolder() {
        defaultFolder = false;
        b_defaultFolder = false;
    }

    /**
     * Gets the folder name
     *
     * @return The folder name
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * Checks if folder name has been set in this folder object
     *
     * @return <code>true</code> if folder name has been set in this folder object; otherwise <code>false</code>
     */
    public boolean containsFolderName() {
        return b_folderName;
    }

    /**
     * Sets the folder name
     *
     * @param folderName The folder name to set
     */
    public void setFolderName(final String folderName) {
        this.folderName = folderName;
        b_folderName = true;
    }

    /**
     * Removes the folder name
     */
    public void removeFolderName() {
        folderName = null;
        b_folderName = false;
    }

    /**
     * Gets the module; either {@link #TASK}, {@link #CALENDAR}, {@link #CONTACT} , {@link #UNBOUND}, {@link #SYSTEM_MODULE}, {@link #MAIL},
     * or {@link #INFOSTORE}
     *
     * @return The module; either {@link #TASK}, {@link #CALENDAR}, {@link #CONTACT} , {@link #UNBOUND}, {@link #SYSTEM_MODULE},
     *         {@link #MAIL}, or {@link #INFOSTORE}
     */
    public int getModule() {
        return module;
    }

    /**
     * Checks if module has been set in this folder object
     *
     * @return <code>true</code> if module has been set in this folder object; otherwise <code>false</code>
     */
    public boolean containsModule() {
        return b_module;
    }

    /**
     * Sets the module
     *
     * @param module The module to set; either {@link #TASK}, {@link #CALENDAR}, {@link #CONTACT} , {@link #UNBOUND}, {@link #SYSTEM_MODULE}
     *            , {@link #MAIL}, or {@link #INFOSTORE}
     */
    public void setModule(final int module) {
        this.module = module;
        b_module = true;
    }

    /**
     * Removes the module
     */
    public void removeModule() {
        module = 0;
        b_module = false;
    }

    /**
     * Gets the permission flag; either {@link #PRIVATE_PERMISSION}, {@link #PUBLIC_PERMISSION} or {@link #CUSTOM_PERMISSION}
     *
     * @return The permission flag; either {@link #PRIVATE_PERMISSION}, {@link #PUBLIC_PERMISSION} or {@link #CUSTOM_PERMISSION}
     */
    public int getPermissionFlag() {
        return permissionFlag;
    }

    /**
     * Checks if permission flag has been set in this folder object
     *
     * @return <code>true</code> if permission flag has been set in this folder object; otherwise <code>false</code>
     */
    public boolean containsPermissionFlag() {
        return b_permissionFlag;
    }

    /**
     * Sets the permission flag
     *
     * @param permissionFlag The permission flag to set; either {@link #PRIVATE_PERMISSION} , {@link #PUBLIC_PERMISSION} or
     *            {@link #CUSTOM_PERMISSION}
     */
    public void setPermissionFlag(final int permissionFlag) {
        this.permissionFlag = permissionFlag;
        b_permissionFlag = true;
    }

    /**
     * Removes the permission flag
     */
    public void removePermissionFlag() {
        permissionFlag = 0;
        b_permissionFlag = false;
    }

    /**
     * Gets all permissions
     *
     * @return All permissions
     */
    public List<OCLPermission> getPermissions() {
        return permissions;
    }

    /**
     * Gets all permissions
     *
     * @return All permissions
     */
    public OCLPermission[] getPermissionsAsArray() {
        final OCLPermission[] perms = new OCLPermission[permissions.size()];
        System.arraycopy(permissions.toArray(), 0, perms, 0, perms.length);
        return perms;
    }

    /**
     * Gets the non-system permissions
     *
     * @return The non-system permissions
     */
    public OCLPermission[] getNonSystemPermissionsAsArray() {
        final List<OCLPermission> retval = new ArrayList<OCLPermission>(permissions.size());
        for (final OCLPermission permission : permissions) {
            if (!permission.isSystem()) {
                retval.add(permission);
            }
        }
        return retval.toArray(new OCLPermission[retval.size()]);
    }

    /**
     * Gets the non-system permission this folder grants to specified entity
     *
     * @param entity The entity ID; either a group or user ID
     * @return The non-system permission or <code>null</code> if none granted
     */
    public OCLPermission getNonSystemPermission(final int entity) {
        for (final OCLPermission permission : permissions) {
            if (!permission.isSystem() && permission.getEntity() == entity) {
                return permission.deepClone();
            }
        }
        return null;
    }

    /**
     * Checks if this folder grants a non-system permission to specified entity which allows at least folder visibility
     *
     * @param entity The entity ID; either a group or user ID
     * @return <code>true</code> if this folder grants a non-system permission to specified entity which allows at least folder visibility;
     *         otherwise <code>false</code>
     * @see #isVisible(int)
     */
    public boolean isNonSystemVisible(final int entity) {
        for (final OCLPermission permission : permissions) {
            if (!permission.isSystem() && permission.getEntity() == entity && permission.isFolderVisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this folder is visible for any of specified entities.
     * <p>
     * <b>Note</b>: This method only checks <b><small>ALL</small></b> basic permissions and does not consider any configuration settings.
     * Use {@link #isVisible(int, UserConfiguration)} for a detailed check if this folder is visible to a certain user.
     *
     * @param entities The entity identifiers (either user or group identifiers)
     * @return <code>true</code> if this folder is visible for any of entities; otherwise <code>false</code>
     * @see #isVisible(int, UserConfiguration)
     */
    public boolean isVisibleForAny(final int[] entities) {
        if (null == entities) {
            return false;
        }
        final int length = entities.length;
        if (0 == length) {
            return false;
        }
        if (1 == length) {
            return isVisible(entities[0]);
        }
        // Sort
        Arrays.sort(entities);
        for (final OCLPermission cur : permissions) {
            if (Arrays.binarySearch(entities, cur.getEntity()) >= 0 && cur.isFolderVisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this folder is visible to specified entity
     * <p>
     * <b>Note</b>: This method only checks <b><small>ALL</small></b> basic permissions and does not consider any configuration settings.
     * Use {@link #isVisible(int, UserConfiguration)} for a detailed check if this folder is visible to a certain user.
     *
     * @param entity The entity ID (either a user or a group ID)
     * @return <code>true</code> if this folder is visible to specified entity; otherwise <code>false</code>
     * @see #isVisible(int, UserConfiguration)
     */
    public boolean isVisible(final int entity) {
        if (entity < 0) {
            return false;
        }
        for (final OCLPermission cur : permissions) {
            if (cur.getEntity() == entity && cur.isFolderVisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this folder has permissions set
     *
     * @return <code>true</code> if this folder has permissions set; otherwise <code>false</code>
     */
    public boolean containsPermissions() {
        return b_permissions;
    }

    /**
     * Applies given permissions to this folder.
     *
     * @param permissions The permissions to set
     */
    public void setPermissionsNoClone(final List<OCLPermission> permissions) {
        this.permissions = new ArrayList<OCLPermission>(permissions);
        b_permissions = true;
    }

    /**
     * Applies given permissions to this folder.
     * <p>
     * <b>NOTE</b>: A <b><small>DEEP</small></b> copy of specified permissions is passed to this folder not a reference.
     *
     * @param permissions The permissions to set
     */
    public void setPermissions(final List<OCLPermission> permissions) {
        if (null != permissions) {
            final int size = permissions.size();
            this.permissions = new ArrayList<OCLPermission>(size);
            for (final OCLPermission permission : permissions) {
                this.permissions.add(permission.deepClone());
            }
            b_permissions = true;
        }
    }

    /**
     * Applies given permissions to this folder.
     * <p>
     * <b>NOTE</b>: A <b><small>DEEP</small></b> copy of specified permissions is passed to this folder, not a reference.
     *
     * @param permissions The permissions to set
     */
    public void setPermissionsAsArray(final OCLPermission[] permissions) {
        if (null == permissions) {
            return;
        }
        if (this.permissions == null) {
            this.permissions = new ArrayList<OCLPermission>();
        } else {
            this.permissions.clear();
        }
        for (final OCLPermission permission : permissions) {
            this.permissions.add(permission.deepClone());
        }
        b_permissions = true;
    }

    /**
     * Adds given permission to this folder object.
     * <p>
     * <b>NOTE</b>: A <b><small>DEEP</small></b> copy of the permission is passed to this folder, not a reference.
     *
     * @param permission The permission to add
     */
    public void addPermission(final OCLPermission permission) {
        if (null == permission) {
            return;
        }
        if (this.permissions == null) {
            this.permissions = new ArrayList<OCLPermission>(4);
        }
        b_permissions = true;
        this.permissions.add(permission.deepClone());
    }

    /**
     * Removes the permissions from this folder
     */
    public void removePermissions() {
        permissions = null;
        b_permissions = false;
    }

    /**
     * Gets this folder's type
     * <p>
     * <b>NOTE</b>: To check if this folder is shared call {@link #isShared(int)} or {@link #getType(int)}
     *
     * @return The type which is either {@link #PUBLIC} or {@link #PRIVATE}.
     */
    public int getType() {
        return type;
    }

    /**
     * Gets this folder's type with respect to specified user
     * <p>
     * <b>NOTE</b>: This method does not check if specified used holds at least read-folder permission but only checks against its type and
     * owner values.
     *
     * @param userId The user ID
     * @return The type which is either {@link #PUBLIC}, {@link #PRIVATE} or {@link #SHARED}.
     */
    public int getType(final int userId) {
        return isShared(userId) ? SHARED : type;
    }

    /**
     * Checks if this folder has its type set
     *
     * @return <code>true</code> if this folder has its type set; otherwise <code>false</code>
     */
    public boolean containsType() {
        return b_type;
    }

    /**
     * Sets this folder's type
     *
     * @param type The type which is either {@link #PUBLIC} or {@link #PRIVATE}
     */
    public void setType(final int type) {
        this.type = type;
        b_type = true;
    }

    /**
     * Removes this folder's type
     */
    public void removeType() {
        type = 0;
        b_type = false;
    }

    /**
     * Gets the creator
     *
     * @return The creator
     */
    public int getCreator() {
        return createdBy;
    }

    /**
     * Checks if creator is set
     *
     * @return <code>true</code> if creator is set; otherwise <code>false</code>
     */
    public boolean containsCreator() {
        return b_createdBy;
    }

    /**
     * Sets the creator
     *
     * @param creator The creator
     */
    public void setCreator(final int creator) {
        createdBy = creator;
        b_createdBy = true;
    }

    /**
     * Removes the creator
     */
    public void removeCreator() {
        createdBy = 0;
        b_createdBy = false;
    }

    /**
     * Checks if this folder has subfolders
     *
     * @return <code>true</code> if this folder has subfolders; otherwise <code>false</code>
     */
    public boolean hasSubfolders() {
        return subfolderFlag;
    }

    /**
     * Checks if this folder has subfolders visible to specified user
     *
     * @param user The user
     * @param userConfig The user's configuration
     * @param ctx The context
     * @return <code>true</code> if this folder has subfolders visible to specified user; otherwise <code>false</code>
     * @throws OXException If user-visible subfolders cannot be checked
     */
    public final boolean hasVisibleSubfolders(final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
        return hasVisibleSubfolders(user.getId(), user.getGroups(), userConfig, ctx);
    }

    private static final int[] MODULES = { TASK, CALENDAR, CONTACT };

    /**
     * Checks if this folder has subfolders visible to specified user
     *
     * @param userId The user ID
     * @param groups The user's group IDs
     * @param userConfig The user configuration
     * @param ctx The context
     * @return <code>true</code> if this folder has subfolders visible to specified user; otherwise <code>false</code>
     * @throws OXException If user-visible subfolders cannot be checked
     */
    public final boolean hasVisibleSubfolders(final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx) throws OXException {
        SearchIterator<FolderObject> iter = null;
        try {
            if (SYSTEM_ROOT_FOLDER_ID == objectId) {
                return true;
            } else if (SYSTEM_PUBLIC_FOLDER_ID == objectId) {
                /*
                 * Search for visible subfolders
                 */
                if (new OXFolderAccess(ctx).getFolderPermission(SYSTEM_LDAP_FOLDER_ID, userId, userConfig).isFolderVisible()) {
                    return true;
                }
                return (iter =
                    OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(
                        userId,
                        groups,
                        userConfig.getAccessibleModules(),
                        FolderObject.PUBLIC,
                        MODULES,
                        SYSTEM_PUBLIC_FOLDER_ID,
                        ctx)).hasNext();
            } else if (SYSTEM_INFOSTORE_FOLDER_ID == objectId) {
                return userConfig.hasInfostore();
                // return (iter =
                // OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType
                // (userId, groups, userConfig
                // .getAccessibleModules(), FolderObject.PUBLIC, new int[] {
                // INFOSTORE },
                // SYSTEM_INFOSTORE_FOLDER_ID, ctx)).hasNext();
            } else if (!subfolderFlag) {
                /*
                 * Folder has no subfolder(s)
                 */
                return false;
            } else if (objectId == SYSTEM_USER_INFOSTORE_FOLDER_ID || objectId == SYSTEM_PRIVATE_FOLDER_ID || objectId == VIRTUAL_LIST_CALENDAR_FOLDER_ID || objectId == VIRTUAL_LIST_CONTACT_FOLDER_ID || objectId == VIRTUAL_LIST_TASK_FOLDER_ID || objectId == VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
                return subfolderFlag;
            }
            return (iter = OXFolderIteratorSQL.getVisibleSubfoldersIterator(objectId, userId, groups, ctx, userConfig.getUserPermissionBits(), null)).hasNext();
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            SearchIterators.close(iter);
        }
    }

    /**
     * Checks if this folder has the sub-folder flag set
     *
     * @return <code>true</code> if this folder has the sub-folder flag set; otherwise <code>false</code>
     */
    public boolean containsSubfolderFlag() {
        return b_subfolderFlag;
    }

    /**
     * Sets the sub-folder flag
     *
     * @param subfolderFlag The sub-folder flag
     */
    public void setSubfolderFlag(final boolean subfolderFlag) {
        this.subfolderFlag = subfolderFlag;
        b_subfolderFlag = true;
    }

    /**
     * Removes the sub-folder flag
     */
    public void removeSubfolderFlag() {
        subfolderFlag = false;
        b_subfolderFlag = false;
    }

    /**
     * Gets a list of user-visible subfolders
     *
     * @param userObj The user
     * @param userConfig The user configuration
     * @param ctx The context
     * @return A list of user-visible subfolders
     * @throws OXException If a pooling error occurs
     * @throws OXException If an error occurs
     * @throws SQLException If a SQL error occurs
     * @throws SearchIteratorException If an iterator error occurs
     */
    public final List<FolderObject> getVisibleSubfolders(final User userObj, final UserConfiguration userConfig, final Context ctx) throws SQLException, OXException {
        return getVisibleSubfolders(userObj.getId(), userObj.getGroups(), userConfig, ctx);
    }

    /**
     * Returns a <code>java.util.List</code> containing all user-visible subfolders
     */
    public final List<FolderObject> getVisibleSubfolders(final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx) throws OXException, SQLException {
        if (b_subfolderFlag && !subfolderFlag) {
            return new ArrayList<FolderObject>(0);
        }
        final List<FolderObject> retval;
        SearchIterator<FolderObject> iter = null;
        try {
            if (objectId == VIRTUAL_LIST_TASK_FOLDER_ID) {
                iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(FolderObject.TASK, userId, groups, userConfig.getUserPermissionBits(), ctx, null);
            } else if (objectId == VIRTUAL_LIST_CALENDAR_FOLDER_ID) {
                iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(FolderObject.CALENDAR, userId, groups, userConfig.getUserPermissionBits(), ctx, null);
            } else if (objectId == VIRTUAL_LIST_CONTACT_FOLDER_ID) {
                iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(FolderObject.CONTACT, userId, groups, userConfig.getUserPermissionBits(), ctx, null);
            } else if (objectId == VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
                iter =
                    OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(FolderObject.INFOSTORE, userId, groups, userConfig.getUserPermissionBits(), ctx, null);
            } else {
                iter = OXFolderIteratorSQL.getVisibleSubfoldersIterator(objectId, userId, groups, ctx, userConfig.getUserPermissionBits(), null);
            }
            if (iter.size() != -1) {
                final int size = iter.size();
                retval = new ArrayList<FolderObject>(size);
                for (int i = 0; i < size; i++) {
                    retval.add(iter.next());
                }
            } else {
                retval = new ArrayList<FolderObject>();
                while (iter.hasNext()) {
                    retval.add(iter.next());
                }
            }
            return retval;
        } finally {
            SearchIterators.close(iter);
        }
    }

    public final List<Integer> getSubfolderIds() throws OXException {
        if (!b_subfolderIds) {
            throw OXFolderExceptionCode.ATTRIBUTE_NOT_SET.create("subfolderIds", Integer.toString(getObjectID()), "");
        }
        return subfolderIds;
    }

    /**
     * Returns a list of subfolder IDs. If <code>enforce</code> is set and list has not been already loaded, their IDs are going to be
     * loaded from storage. Otherwise a exception is thrown that no subfolder IDs are present in this folder object.
     */
    public final List<Integer> getSubfolderIds(final boolean enforce, final Context ctx) throws OXException, SQLException, OXException {
        return getSubfolderIds(enforce, null, ctx);
    }

    public final List<Integer> getSubfolderIds(final boolean enforce, final Connection readCon, final Context ctx) throws OXException, SQLException, OXException {
        if (!b_subfolderIds) {
            /*
             * Subfolder list not set, yet
             */
            if (b_subfolderFlag && !subfolderFlag) {
                /*
                 * Flag indicates no present subfolders
                 */
                return new ArrayList<Integer>(0);
            }
            if (!enforce) {
                throw OXFolderExceptionCode.ATTRIBUTE_NOT_SET.create("subfolderIds", Integer.toString(getObjectID()), "");
            }
            subfolderIds = getSubfolderIds(objectId, ctx, readCon);
            b_subfolderIds = true;
        }
        return subfolderIds;
    }

    public boolean containsSubfolderIds() {
        return b_subfolderIds;
    }

    public void setSubfolderIds(final ArrayList<Integer> subfolderIds) {
        this.subfolderIds = new ArrayList<Integer>(subfolderIds);
        b_subfolderIds = true;
    }

    public void removeSubfolderIds() {
        subfolderIds = null;
        b_subfolderIds = false;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean containsFullName() {
        return b_fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
        b_fullName = true;
    }

    public void removeFullName() {
        fullName = null;
        b_fullName = false;
    }

    @Override
    public final void reset() {
        super.reset();
        removeCreator();
        removeType();
        removePermissions();
        removePermissionFlag();
        removeModule();
        removeFolderName();
        removeSubfolderFlag();
        removeSubfolderIds();
        removeFullName();
    }

    /**
     * Fills this folder with all available values from given folder and returns itself.
     *
     * @return filled folder
     */
    public final FolderObject fill(final FolderObject other) {
        return fill(other, true);
    }

    /**
     * Fills this folder with all availbable values from given folder and returns itself.
     *
     * @param other The other instance of <code>{@link FolderObject}</code> serving as source
     * @param overwrite <code>true</code> to overwrite even if value is already present; <code>false</code> to only fill value if not
     *            present
     * @return filled folder
     */
    public final FolderObject fill(final FolderObject other, final boolean overwrite) {
        if (overwrite) {
            reset();
        }
        if (other.containsObjectID() && (overwrite || !containsObjectID())) {
            setObjectID(other.getObjectID());
        }
        if (other.containsCreatedBy() && (overwrite || !containsCreatedBy())) {
            setCreatedBy(other.getCreatedBy());
        }
        if (other.containsCreationDate() && (overwrite || !containsCreationDate())) {
            setCreationDate(other.getCreationDate());
        }
        if (other.containsDefaultFolder() && (overwrite || !containsDefaultFolder())) {
            setDefaultFolder(other.isDefaultFolder());
        }
        if (other.containsFolderName() && (overwrite || !containsFolderName())) {
            setFolderName(other.getFolderName());
        }
        if (other.containsFullName() && (overwrite || !containsFullName())) {
            setFullName(other.getFullName());
        }
        if (other.containsLastModified() && (overwrite || !containsLastModified())) {
            setLastModified(other.getLastModified());
        }
        if (other.containsModifiedBy() && (overwrite || !containsModifiedBy())) {
            setModifiedBy(other.getModifiedBy());
        }
        if (other.containsModule() && (overwrite || !containsModule())) {
            setModule(other.getModule());
        }
        if (other.containsParentFolderID() && (overwrite || !containsParentFolderID())) {
            setParentFolderID(other.getParentFolderID());
        }
        if (other.containsPermissionFlag() && (overwrite || !containsPermissionFlag())) {
            setPermissionFlag(other.getPermissionFlag());
        }
        if (other.containsPermissions() && (overwrite || !containsPermissions())) {
            setPermissions(other.getPermissions());
        }
        if (other.containsSubfolderFlag() && (overwrite || !containsSubfolderFlag())) {
            setSubfolderFlag(other.hasSubfolders());
        }
        if (other.containsSubfolderIds() && (overwrite || !containsSubfolderIds())) {
            try {
                setSubfolderIds((ArrayList<Integer>) other.getSubfolderIds());
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        if (other.containsType() && (overwrite || !containsType())) {
            setType(other.getType());
        }
        return this;
    }

    /**
     * Checks if this folder exists in underlying storage by checking its object ID or (if object ID is not present) by its folder name,
     * parent and module. An <code>OXException</code> is thrown if folder does not hold sufficient information to verify existence.
     *
     * @return <code>true</code> if a corresponding folder can be detected, otherwise <code>false</code>
     */
    public final boolean exists(final Context ctx) throws OXException {
        if (containsObjectID()) {
            try {
                return OXFolderSQL.exists(getObjectID(), null, ctx);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        } else if (containsParentFolderID() && containsFolderName() && containsModule()) {
            try {
                final int fuid = OXFolderSQL.lookUpFolder(getParentFolderID(), getFolderName(), getModule(), null, ctx);
                if (fuid == -1) {
                    return false;
                }
                setObjectID(fuid);
                return true;
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        throw OXFolderExceptionCode.UNSUFFICIENT_FOLDER_INFORMATION.create();
    }

    /**
     * <b>NOTE:</b> This method does not check user's permissions on this folder, but only checks if this folder is of type
     * <code>PRIVATE</code> and user is not folder's creator
     *
     * @return <code>true</code> if this folder is of type PRIVATE and user is not folder's creator, <code>false</code> otherwise
     */
    public final boolean isShared(final int userId) {
        return (type == PRIVATE && createdBy != userId);
    }

    /**
     * @return <code>true</code> if given user has READ access to this folder, <code>false</code> otherwise
     */
    public final boolean isVisible(final int userId, final UserPermissionBits userPerm) throws OXException {
        return (getEffectiveUserPermission(userId, userPerm).isFolderVisible());
    }

    /**
     * @return <code>true</code> if given user has READ access to this folder, <code>false</code> otherwise
     */
    public final boolean isVisible(final int userId, final UserConfiguration userConfig) throws OXException {
        return (getEffectiveUserPermission(userId, userConfig).isFolderVisible());
    }

    /**
     * This methods yields the effective OCL permission for the currently logged in user by determining the max. OCL permission which the
     * user has on folder and applying the user configuration profile.
     */
    public final EffectivePermission getEffectiveUserPermission(final int userId, final UserPermissionBits userPerm) throws OXException {
        if (!containsPermissions()) {
            try {
                setPermissionsAsArray(FolderObject.getFolderPermissions(getObjectID(), ContextStorage.getStorageContext(userPerm.getContextId()), null));
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        return calcEffectiveUserPermission(userId, userPerm);
    }

    /**
     * This methods yields the effective OCL permission for the currently logged in user by determining the max. OCL permission which the
     * user has on folder and applying the user configuration profile.
     */
    public final EffectivePermission getEffectiveUserPermission(final int userId, final UserConfiguration userConfig) throws OXException {
        if (!containsPermissions()) {
            try {
                setPermissionsAsArray(FolderObject.getFolderPermissions(getObjectID(), userConfig.getContext(), null));
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        return calcEffectiveUserPermission(userId, userConfig);
    }

    /**
     * This methods yields the effective OCL permission for the currently logged in user by determining the max. OCL permission which the
     * user has on folder and applying the user configuration profile.
     */
    public final EffectivePermission getEffectiveUserPermission(final int userId, final UserPermissionBits upb, final Connection readConArg) throws SQLException, OXException {
        if (!containsPermissions()) {
            setPermissionsAsArray(FolderObject.getFolderPermissions(getObjectID(), upb.getContext(), readConArg));
        }
        return calcEffectiveUserPermission(userId, upb);
    }

    /**
     * This methods yields the effective OCL permission for the currently logged in user by determining the max. OCL permission which the
     * user has on folder and applying the user configuration profile.
     */
    public final EffectivePermission getEffectiveUserPermission(final int userId, final UserConfiguration userConfig, final Connection readConArg) throws SQLException, OXException {
        if (!containsPermissions()) {
            setPermissionsAsArray(FolderObject.getFolderPermissions(getObjectID(), userConfig.getContext(), readConArg));
        }
        return calcEffectiveUserPermission(userId, userConfig);
    }

    private final EffectivePermission calcEffectiveUserPermission(final int userId, final UserPermissionBits userPermissionBits) {
        return calcEffectiveUserPermission(userId, userPermissionBits, true);
    }

    private final EffectivePermission calcEffectiveUserPermission(final int userId, final UserPermissionBits userPermissionBits, final boolean considerSystemPermissions) {
        final EffectivePermission maxPerm = new EffectivePermission(userId, getObjectID(), getType(userId), getModule(), getCreatedBy(), userPermissionBits);
        final int[] idArr;
        {
            final int[] groups = userPermissionBits.getGroups();
            idArr = new int[groups.length + 1];
            idArr[0] = userId;
            System.arraycopy(groups, 0, idArr, 1, groups.length);
            Arrays.sort(idArr);
        }
        int fp = 0;
        int orp = 0;
        int owp = 0;
        int odp = 0;
        boolean admin = false;
        for (final OCLPermission oclPerm : getPermissions()) {
            if ((considerSystemPermissions || !oclPerm.isSystem()) && Arrays.binarySearch(idArr, oclPerm.getEntity()) >= 0) {
                // Folder permission
                int cur = oclPerm.getFolderPermission();
                if (cur > fp) {
                    fp = cur;
                }
                // Read permission
                cur = oclPerm.getReadPermission();
                if (cur > orp) {
                    orp = cur;
                }
                // Write permission
                cur = oclPerm.getWritePermission();
                if (cur > owp) {
                    owp = cur;
                }
                // Delete permission
                cur = oclPerm.getDeletePermission();
                if (cur > odp) {
                    odp = cur;
                }
                // Admin flag
                admin |= oclPerm.isFolderAdmin();
            }
        }
        maxPerm.setAllPermission(fp, orp, owp, odp);
        maxPerm.setFolderAdmin(admin);
        return maxPerm;
    }

    private final EffectivePermission calcEffectiveUserPermission(final int userId, final UserConfiguration userConfig) {
        final EffectivePermission maxPerm = new EffectivePermission(userId, getObjectID(), getType(userId), getModule(), getCreatedBy(), userConfig);
        final int[] idArr;
        {
            final int[] groups = userConfig.getGroups();
            idArr = new int[groups.length + 1];
            idArr[0] = userId;
            System.arraycopy(groups, 0, idArr, 1, groups.length);
            Arrays.sort(idArr);
        }
        int fp = 0;
        int orp = 0;
        int owp = 0;
        int odp = 0;
        boolean admin = false;
        for (final OCLPermission oclPerm : getPermissions()) {
            if (Arrays.binarySearch(idArr, oclPerm.getEntity()) >= 0) {
                // Folder permission
                int cur = oclPerm.getFolderPermission();
                if (cur > fp) {
                    fp = cur;
                }
                // Read permission
                cur = oclPerm.getReadPermission();
                if (cur > orp) {
                    orp = cur;
                }
                // Write permission
                cur = oclPerm.getWritePermission();
                if (cur > owp) {
                    owp = cur;
                }
                // Delete permission
                cur = oclPerm.getDeletePermission();
                if (cur > odp) {
                    odp = cur;
                }
                // Admin flag
                admin |= oclPerm.isFolderAdmin();
            }
        }
        maxPerm.setAllPermission(fp, orp, owp, odp);
        maxPerm.setFolderAdmin(admin);
        return maxPerm;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append('\n');
        if (containsObjectID()) {
            sb.append("ObjectID=").append(getObjectID());
        } else {
            sb.append("<no-object-id>");
        }
        if (containsFullName()) {
            sb.append(" Full Name=").append(getFullName());
        }
        if (containsParentFolderID()) {
            sb.append(" Parent Folder ID=").append(getParentFolderID());
        }
        if (containsFolderName()) {
            sb.append(" Folder Name=").append(getFolderName());
        }
        if (containsType()) {
            sb.append(" Type=").append(getType());
        }
        if (containsModule()) {
            sb.append(" Module=").append(getModule());
        }
        if (containsCreatedBy()) {
            sb.append(" Created By=").append(getCreatedBy());
        }
        if (containsCreationDate()) {
            sb.append(" Creation Date=").append(getCreationDate());
        }
        if (containsModifiedBy()) {
            sb.append(" Modified By=").append(getModifiedBy());
        }
        if (containsLastModified()) {
            sb.append(" Last Modified=").append(getLastModified());
        }
        if (containsDefaultFolder()) {
            sb.append(" Default Folder=").append(isDefaultFolder());
        }
        if (containsSubfolderFlag()) {
            sb.append(" Has Subfolders=").append(hasSubfolders());
        }
        if (containsPermissions()) {
            sb.append(" permissions=");
            final int size = getPermissions().size();
            final Iterator<OCLPermission> iter = getPermissions().iterator();
            for (int i = 0; i < size; i++) {
                sb.append(iter.next().toString());
                if (i < size - 1) {
                    sb.append('|');
                }
            }
        }
        if (containsSubfolderIds()) {
            try {
                sb.append(" subfolder IDs=");
                final int size = getSubfolderIds().size();
                final Iterator<Integer> iter = getSubfolderIds().iterator();
                for (int i = 0; i < size; i++) {
                    sb.append(iter.next().toString());
                    if (i < size - 1) {
                        sb.append('|');
                    }
                }
            } catch (final OXException e) {
                sb.append("");
            }
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public FolderObject clone() {
        try {
            final FolderObject clone = (FolderObject) super.clone();
            if (b_createdBy) {
                clone.setCreatedBy(createdBy);
            }
            if (b_creationDate) {
                clone.setCreationDate(creationDate);
            }
            if (b_defaultFolder) {
                clone.setDefaultFolder(defaultFolder);
            }
            if (b_objectId) {
                clone.setObjectID(objectId);
            }
            if (b_folderName) {
                clone.setFolderName(folderName);
            }
            if (b_fullName) {
                clone.setFullName(fullName);
            }
            if (b_lastModified) {
                clone.setLastModified(lastModified);
            }
            if (b_modifiedBy) {
                clone.setModifiedBy(modifiedBy);
            }
            if (b_module) {
                clone.setModule(module);
            }
            if (b_parent_folder_id) {
                clone.setParentFolderID(parentFolderId);
            }
            if (b_permissionFlag) {
                clone.setPermissionFlag(permissionFlag);
            }
            if (b_subfolderFlag) {
                clone.setSubfolderFlag(subfolderFlag);
            }
            if (b_subfolderIds) {
                clone.setSubfolderIds(copyIntArrayList(subfolderIds));
            }
            if (b_permissions) {
                clone.setPermissions(copyArrayList(permissions));
            }
            if (b_type) {
                clone.setType(type);
            }
            if (b_map) {
                clone.setMap(copyMap(map));
            }
            return clone;
        } catch (final CloneNotSupportedException exc) {
            return null;
        }
    }

    @Override
    public void set(final int field, final Object value) {
        switch (field) {
        case MODULE:
            setModule(((Integer) value).intValue());
            break;
        case FOLDER_NAME:
            setFolderName((String) value);
            break;
        case TYPE:
            setType(((Integer) value).intValue());
            break;
        case SUBFOLDERS:
            setSubfolderFlag(((Boolean) value).booleanValue());
            break;
        case PERMISSIONS_BITS:
            if (value.getClass().isInstance(new OCLPermission[0])) {
                setPermissionsAsArray((OCLPermission[]) value);
            } else {
                setPermissions((List<OCLPermission>) value);
            }
            break;
        default:
            super.set(field, value);
        }
    }

    @Override
    public Object get(final int field) {
        switch (field) {
        case MODULE:
            return Integer.valueOf(getModule());
        case FOLDER_NAME:
            return getFolderName();
        case TYPE:
            return Integer.valueOf(getType());
        case SUBFOLDERS:
            return Boolean.valueOf(hasSubfolders());
        case PERMISSIONS_BITS:
            return getPermissions();
        default:
            return super.get(field);
        }
    }

    @Override
    public boolean contains(final int field) {
        switch (field) {
        case MODULE:
            return containsModule();
        case FOLDER_NAME:
            return containsFolderName();
        case TYPE:
            return containsType();
        case SUBFOLDERS:
            return containsSubfolderFlag();
        case PERMISSIONS_BITS:
            return containsPermissions();
        default:
            return super.contains(field);
        }
    }

    @Override
    public void remove(final int field) {
        switch (field) {
        case MODULE:
            removeModule();
            break;
        case FOLDER_NAME:
            removeFolderName();
            break;
        case TYPE:
            removeType();
            break;
        case SUBFOLDERS:
            removeSubfolderIds();
            removeSubfolderFlag();
            break;
        case PERMISSIONS_BITS:
            removePermissions();
            break;
        default:
            super.remove(field);
            break;
        }
    }

    private static final <T extends OXCloneable<T>> ArrayList<T> copyArrayList(final ArrayList<T> original) {
        final int size = original.size();
        final ArrayList<T> copy = new ArrayList<T>(original.size());
        final Iterator<T> iter = original.iterator();
        for (int i = 0; i < size; i++) {
            copy.add(iter.next().deepClone());
        }
        return copy;
    }

    private static final Map<String, Object> copyMap(Map<String, Object> original) throws CloneNotSupportedException {
        InputStream meta = null;
        try {
             meta = OXFolderUtility.serializeMeta(original);
             return OXFolderUtility.deserializeMeta(meta);
        } catch (JSONException e) {
            throw new CloneNotSupportedException(e.getMessage());
        } finally {
            Streams.close(meta);
        }
    }

    private static final ArrayList<Integer> copyIntArrayList(final ArrayList<Integer> original) {
        final int size = original.size();
        final ArrayList<Integer> copy = new ArrayList<Integer>(original.size());
        for (int i = 0; i < size; i++) {
            copy.add(Integer.valueOf(original.get(i).intValue()));
        }
        return copy;
    }

    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx) throws OXException {
        return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx);
    }

    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, readCon);
    }

    /**
     * Loads specified folder from database.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param loadPermissions <code>true</code> to load folder's permissions, otherwise <code>false</code>
     * @param loadSubfolderList <code>true</code> to load subfolders, otherwise <code>false</code>
     * @return The loaded folder object from database
     * @throws OXException If folder cannot be loaded
     */
    public static final FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList) throws OXException {
        return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, readConArg, loadPermissions, loadSubfolderList);
    }

    /**
     * Loads specified folder from database.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param loadPermissions <code>true</code> to load folder's permissions, otherwise <code>false</code>
     * @param loadSubfolderList <code>true</code> to load subfolders, otherwise <code>false</code>
     * @param table The folder's working or backup table name
     * @param permTable The folder permissions' working or backup table name
     * @return The loaded folder object from database
     * @throws OXException If folder cannot be loaded
     */
    public static final FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList, final String table, final String permTable) throws OXException {
        return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, readConArg, loadPermissions, loadSubfolderList, table, permTable);
    }

    /**
     * Loads folder permissions from database. Creates a new connection if <code>null</code> is given.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The folder's permissions
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final OCLPermission[] getFolderPermissions(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return OXFolderLoader.getFolderPermissions(folderId, ctx, readConArg);
    }

    /**
     * Loads folder permissions from database. Creates a new connection if <code>null</code> is given.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readCon A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table Either folder permissions working or backup table name
     * @return The folder's permissions
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final OCLPermission[] getFolderPermissions(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        return OXFolderLoader.getFolderPermissions(folderId, ctx, readConArg, table);
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return OXFolderLoader.getSubfolderIds(folderId, ctx, readConArg);
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table The folder's working or backup table name
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        return OXFolderLoader.getSubfolderIds(folderId, ctx, readConArg, table);
    }

    public static final OCLPermission VIRTUAL_FOLDER_PERMISSION = new OCLPermission();
    public static final OCLPermission VIRTUAL_GUEST_PERMISSION = new OCLPermission();
    static {
        VIRTUAL_FOLDER_PERMISSION.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        VIRTUAL_FOLDER_PERMISSION.setFolderAdmin(false);
        VIRTUAL_FOLDER_PERMISSION.setGroupPermission(true);
        VIRTUAL_FOLDER_PERMISSION.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);

        VIRTUAL_GUEST_PERMISSION.setEntity(OCLPermission.ALL_GUESTS);
        VIRTUAL_GUEST_PERMISSION.setFolderAdmin(false);
        VIRTUAL_GUEST_PERMISSION.setGroupPermission(true);
        VIRTUAL_GUEST_PERMISSION.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
    }

    /**
     * Creates a folder instance representing a virtual folder.
     *
     * @param objectID The virtual object ID
     * @param name The name
     * @param module The module
     * @param hasSubfolders Whether the folder is supposed to contain subfolders
     * @param type The type
     * @return A folder instance representing a virtual folder
     */
    public static final FolderObject createVirtualFolderObject(final int objectID, final String name, final int module, final boolean hasSubfolders, final int type) {
        return createVirtualFolderObject(objectID, name, module, hasSubfolders, type, (OCLPermission) null);
    }

    /**
     * Creates a folder instance representing a virtual folder.
     *
     * @param objectID The virtual object ID
     * @param name The name
     * @param module The module
     * @param hasSubfolders Whether the folder is supposed to contain subfolders
     * @param type The type
     * @param virtualPerms The folder's permissions
     * @return A folder instance representing a virtual folder
     */
    public static final FolderObject createVirtualFolderObject(final int objectID, final String name, final int module, final boolean hasSubfolders, final int type, final OCLPermission... virtualPerms) {
        final FolderObject virtualFolder = new FolderObject(objectID);
        virtualFolder.setFolderName(name);
        virtualFolder.setModule(module);
        virtualFolder.setSubfolderFlag(hasSubfolders);
        virtualFolder.setType(type);

        final List<OCLPermission> permissions = prepareVirtualPermissions(virtualPerms);
        for (OCLPermission permission : permissions) {
            permission.setFuid(objectID);
            virtualFolder.addPermission(permission);
        }

        return virtualFolder;
    }

    /**
     * Creates a folder instance representing a virtual folder.
     *
     * @param fullName The folder's fullname
     * @param name The name
     * @param module The module
     * @param hasSubfolders Whether the folder is supposed to contain subfolders
     * @param type The type
     * @return A folder instance representing a virtual folder
     */
    public static final FolderObject createVirtualFolderObject(final String fullName, final String name, final int module, final boolean hasSubfolders, final int type) {
        return createVirtualFolderObject(fullName, name, module, hasSubfolders, type, VIRTUAL_FOLDER_PERMISSION);
    }

    /**
     * Creates a folder instance representing a virtual folder.
     *
     * @param fullName The folder's fullname
     * @param name The name
     * @param module The module
     * @param hasSubfolders Whether the folder is supposed to contain subfolders
     * @param type The type
     * @param virtualPerms The folder's permissions
     * @return A folder instance representing a virtual folder
     */
    public static final FolderObject createVirtualFolderObject(final String fullName, final String name, final int module, final boolean hasSubfolders, final int type, final OCLPermission... virtualPerms) {
        final FolderObject virtualFolder = new FolderObject();
        virtualFolder.setFullName(fullName);
        virtualFolder.setFolderName(name);
        virtualFolder.setModule(module);
        virtualFolder.setSubfolderFlag(hasSubfolders);
        virtualFolder.setType(type);

        final List<OCLPermission> permissions = prepareVirtualPermissions(virtualPerms);
        for (OCLPermission permission : permissions) {
            virtualFolder.addPermission(permission);
        }

        return virtualFolder;
    }

    /**
     * Creates a folder instance representing a shared folder.
     *
     * @param createdBy The user ID of shared folder's owner
     * @param creatorDisplayName The display name of shared folder's owner
     * @return A folder instance representing a shared folder.
     */
    public static final FolderObject createVirtualSharedFolderObject(final int createdBy, final String creatorDisplayName) {
        return createVirtualFolderObject(
            new StringBuilder(20).append(SHARED_PREFIX).append(createdBy).toString(),
            creatorDisplayName,
            FolderObject.SYSTEM_MODULE,
            true,
            FolderObject.SYSTEM_TYPE,
            VIRTUAL_FOLDER_PERMISSION,
            VIRTUAL_GUEST_PERMISSION);
    }

    /**
     * Sets the meta map with arbitrary properties.
     * <p>
     * Delegates to {@link #setMap(Map)}
     *
     * @param map The meta map
     */
    public void setMeta(Map<String, Object> meta) {
        setMap(meta);
    }

    /**
     * Gets (optionally) the meta map with arbitrary properties.
     * <p>
     * Delegates to {@link #getMap()}
     *
     * @return The meta map or <code>null</code>
     */
    public Map<String, Object> getMeta() {
        return getMap();
    }

    /**
     * Checks if this object contains a map.
     * <p>
     * Delegates to {@link #containsMap()}
     *
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsMeta() {
        return containsMap();
    }

    private static List<OCLPermission> prepareVirtualPermissions(final OCLPermission[] virtualPerms) {
        if (virtualPerms == null) {
            return Collections.singletonList(VIRTUAL_FOLDER_PERMISSION.deepClone());
        }

        final List<OCLPermission> permissions = new ArrayList<OCLPermission>(virtualPerms.length);
        for (OCLPermission permission : virtualPerms) {
            if (permission != null) {
                permissions.add(permission);
            }
        }

        if (permissions.isEmpty()) {
            permissions.add(VIRTUAL_FOLDER_PERMISSION.deepClone());
        }

        return permissions;
    }

}
