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

package com.openexchange.tools.oxfolder;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.i18n.LocalizableArgument;
import com.openexchange.java.AsciiReader;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link OXFolderUtility} - Provides utility methods for folder operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderUtility.class);

    private static final String STR_EMPTY = "";

    /**
     * Initializes a new {@link OXFolderUtility}.
     */
    private OXFolderUtility() {
        super();
    }

    /**
     * Checks equality of specified permissions.
     *
     * @param permissions1 The first permissions
     * @param permissions2 The second permissions
     * @return <code>true</code> if both permissions are equal; otherwise <code>false</code>
     */
    public static boolean equalPermissions(OCLPermission[] permissions1, OCLPermission[] permissions2) {
        if (null == permissions1) {
            return null == permissions2;
        }
        if (null == permissions2) {
            return false;
        }
        if (permissions1.length != permissions2.length) {
            return false;
        }
        for (OCLPermission permission : permissions1) {
            boolean found = false;
            for (int i = 0; !found && i < permissions2.length; i++) {
                OCLPermission other = permissions2[i];
                if (permission.getEntity() == other.getEntity() && permission.equalsPermission(other)) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the appropriate accessible module for DB folder queries.
     *
     * @param accessibleModules The accessible modules as indicated by user configuration
     * @return The appropriate accessible module for DB folder queries
     */
    public static int[] getAccessibleModulesForFolders(final int[] accessibleModules) {
        if (null == accessibleModules) {
            return null;
        }
        if (!InfostoreFacades.isInfoStoreAvailable()) {
            final int pos = Arrays.binarySearch(accessibleModules, FolderObject.INFOSTORE);
            if (pos >= 0) {
                // Remove Infostore module identifier to ignore infostore folders
                final int mlen = accessibleModules.length - 1;
                final int[] modules = new int[mlen];
                if (0 == pos) {
                    System.arraycopy(accessibleModules, 1, modules, 0, modules.length);
                } else {
                    System.arraycopy(accessibleModules, 0, modules, 0, pos);
                    final int len = mlen - pos;
                    if (len > 0) {
                        System.arraycopy(accessibleModules, pos + 1, modules, pos, len);
                    }
                }
                return modules;
            }
        }
        return accessibleModules;
    }

    /**
     * Checks for duplicate folder name considering locale-sensitive folder names.
     *
     * @param parentFolderId The parent folder ID
     * @param folderName The folder name to check
     * @param locale The user's locale
     * @param ctx The context
     * @throws OXException If a duplicate folder exists
     */
    private static void checki18nString(final int parentFolderId, final String folderName, final Locale locale, final Context ctx) throws OXException {
        if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == parentFolderId) {
            if (FolderObject.getFolderString(FolderObject.SYSTEM_LDAP_FOLDER_ID, locale).equalsIgnoreCase(folderName)) {
                final String parentFolderName = new StringBuilder(FolderObject.getFolderString(parentFolderId, locale)).append(" (").append(parentFolderId).append(')').toString();
                /*
                 * A duplicate folder exists
                 */
                throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(parentFolderName, Integer.valueOf(ctx.getContextId()), folderName);
            }
            if (!OXFolderProperties.isIgnoreSharedAddressbook() && FolderObject.getFolderString(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, locale).equalsIgnoreCase(folderName)) {
                final String parentFolderName = new StringBuilder(FolderObject.getFolderString(parentFolderId, locale)).append(" (").append(parentFolderId).append(')').toString();
                /*
                 * A duplicate folder exists
                 */
                throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(parentFolderName, Integer.valueOf(ctx.getContextId()), folderName);
            }
        }
    }

    /**
     * Performs various checks against conflicting duplicate folders on the same level or reserved folder names before saving a folder in
     * the target folder.
     *
     * @param connection A (readable) database connection
     * @param context The context
     * @param user The user
     * @param folderID The identifier of the folder being saved (to avoid conflicts with the folder itself), or <code>-1</code> for new folders
     * @param module The groupware module of the folder
     * @param parentFolderID The identifier of the parent folder to perform the checks in
     * @param folderName The target folder name
     * @param createdBy The identifier of the user who created the folder
     * @throws OXException If check fails
     */
    public static void checkTargetFolderName(Connection connection, Context context, User user, int folderID, int module, int parentFolderID, String folderName, int createdBy) throws OXException {
        /*
         * check folder name string for invalid data
         */
        String result = Check.containsInvalidChars(folderName);
        if (null != result) {
            throw OXFolderExceptionCode.INVALID_DATA.create(result);
        }
        try {
            if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == parentFolderID) {
                TIntList folders = OXFolderSQL.lookUpFolders(parentFolderID, folderName, module, connection, context);
                /*
                 * Check if the user is owner of one of these folders. In this case throw a duplicate folder exception
                 */
                OXFolderAccess folderAccess = new OXFolderAccess(connection, context);
                for (int fuid : folders.toArray()) {
                    if (folderID == fuid) {
                        continue;
                    }
                    FolderObject toCheck = folderAccess.getFolderObject(fuid);
                    if (toCheck.getCreatedBy() == createdBy) {
                        /*
                         * User is already owner of a private folder with the same name located below system's private folder
                         */
                        throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(Integer.valueOf(parentFolderID), I(context.getContextId()), folderName);
                    }
                }
            } else if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == parentFolderID) {
                /*
                 * check localized names below public folder
                 */
                checki18nString(parentFolderID, folderName, user.getLocale(), context);
            } else {
                /*
                 * by default, check for equally named folder on same level
                 */
                int existingFolderID = OXFolderSQL.lookUpFolderOnUpdate(folderID, parentFolderID, folderName, module, connection, context);
                if (-1 != existingFolderID) {
                    /*
                     * double-check if cached parent folder lists the existing folder as subfolder, otherwise invalidate
                     */
                    FolderCacheManager manager = FolderCacheManager.getInstance();
                    FolderObject cachedParentFolder = manager.getFolderObject(parentFolderID, context);
                    if (null != cachedParentFolder) {
                        List<Integer> cachedSubfolderIDs = null;
                        try {
                            cachedSubfolderIDs = cachedParentFolder.getSubfolderIds();
                        } catch (OXException e) {
                            if (false == OXFolderExceptionCode.ATTRIBUTE_NOT_SET.equals(e)) {
                                throw e;
                            }
                        }
                        if (null != cachedSubfolderIDs && false == cachedSubfolderIDs.contains(Integer.valueOf(existingFolderID))) {
                            manager.removeFolderObject(parentFolderID, context);
                        }
                    }
                    throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(Integer.valueOf(parentFolderID), I(context.getContextId()), folderName);
                }
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Tests if specified folder contains contains duplicate permissions.
     *
     * @param folder The folder whose permissions shall be checked.
     * @param ctx The context
     * @throws OXException If specified folder contains contains duplicate permissions.
     */
    public static void checkForDuplicateNonSystemPermissions(final FolderObject folder, final Context ctx) throws OXException {
        final OCLPermission[] permissions = folder.getNonSystemPermissionsAsArray();
        if (permissions.length == 0) {
            return;
        }
        final TIntSet set = new TIntHashSet(permissions.length);
        set.add(permissions[0].getEntity());
        for (int i = 1; i < permissions.length; i++) {
            final OCLPermission permission = permissions[i];
            final int key = permission.getEntity();
            if (set.contains(key)) {
                if (permission.isGroupPermission()) {
                    throw OXFolderExceptionCode.DUPLICATE_GROUP_PERMISSION.create(Integer.valueOf(permission.getEntity()));
                }
                throw OXFolderExceptionCode.DUPLICATE_USER_PERMISSION.create(Integer.valueOf(permission.getEntity()));
            }
            set.add(key);
        }
    }

    /**
     * Checks for similar named shared folder
     *
     * @param diff The user IDs
     * @param allSharedFolders The shared folders
     * @param folderName The folder name of the folder that shall be shared
     * @param ctx The context
     * @throws OXException If a similar named shared folder is already shared to a user
     */
    public static void checkSimilarNamedSharedFolder(final TIntSet diff, final FolderObject[] allSharedFolders, final String folderName, final Context ctx) throws OXException {
        final TIntList affectedUsers = new TIntLinkedList();
        for (final FolderObject f : allSharedFolders) {
            if (null == f) {
                continue;
            }
            final List<OCLPermission> l = f.getPermissions();
            for (final OCLPermission permission : l) {
                if (permission.isGroupPermission()) {
                    /*
                     * Check against group members
                     */
                    try {
                        final int[] members = GroupStorage.getInstance().getGroup(permission.getEntity(), ctx).getMember();
                        for (final int cur : members) {
                            if (diff.contains(cur) && f.getFolderName().equals(folderName)) {
                                affectedUsers.add(cur);
                                // TODO: Throw exception if bug #9111 says
                                // so
                            }
                        }
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                } else {
                    /*
                     * Check against entity itself
                     */
                    final int cur = permission.getEntity();
                    if (diff.contains(cur) && f.getFolderName().equals(folderName)) {
                        affectedUsers.add(cur);
                        // TODO: Throw exception if bug #9111 says so
                    }
                }
            }
        }
        if (!affectedUsers.isEmpty()) {
            throw OXFolderExceptionCode.SIMILAR_NAMED_SHARED_FOLDER.create(folderName);
        }
    }

    /**
     * Checks if permissions from given folder specify (at least) one folder admin and if creating user is the folder admin for his default
     * folders.
     *
     * @param folderObj The folder
     * @param userId The user ID
     * @param ctx The context
     * @param warnings
     * @throws OXException If specified folder's permissions do not pass the checks
     */
    public static void checkFolderPermissions(final FolderObject folderObj, final int userId, final Context ctx, final List<OXException> warnings) throws OXException {
        final boolean isPrivate = (folderObj.getType() == FolderObject.PRIVATE || folderObj.getType() == FolderObject.SHARED);
        final TIntList adminEntities = new TIntArrayList(isPrivate ? 1 : 4);
        final int permissionsSize = folderObj.getPermissions().size();
        Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
        final int creator = folderObj.containsCreatedBy() ? folderObj.getCreatedBy() : userId;
        final boolean isDefaultFolder = folderObj.containsDefaultFolder() ? folderObj.isDefaultFolder() : false;
        boolean creatorIsAdmin = false;
        for (int i = 0; i < permissionsSize; i++) {
            final OCLPermission oclPerm = iter.next();
            if (oclPerm.getEntity() < 0) {
                throw OXFolderExceptionCode.INVALID_ENTITY.create(I(oclPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            if (oclPerm.isGroupPermission() && GroupStorage.GUEST_GROUP_IDENTIFIER == oclPerm.getEntity()) {
                throw OXFolderExceptionCode.INVALID_ENTITY_FROM_USER.create(I(oclPerm.getEntity()), new LocalizableArgument(Groups.GUEST_GROUP), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            if (oclPerm.isFolderAdmin()) {
                adminEntities.add(oclPerm.getEntity());
                if (isPrivate && folderObj.getModule() != FolderObject.SYSTEM_MODULE) {
                    checkPrivateAdminPerm(adminEntities.size(), creator, oclPerm);
                }
                if (isDefaultFolder && !creatorIsAdmin) {
                    creatorIsAdmin = (oclPerm.getEntity() == creator);
                }
            }
        }
        if (!isPrivate && !adminEntities.contains(creator)) {
            iter = folderObj.getPermissions().iterator();
            boolean found = false;
            for (int i = 0; !found && i < permissionsSize; i++) {
                final OCLPermission oclPerm = iter.next();
                if (oclPerm.getEntity() == creator) {
                    if (!oclPerm.isFolderAdmin()) {
                        warnings.add(OXFolderExceptionCode.CREATOR_STAYS_ADMIN.create(Integer.valueOf(creator), Integer.valueOf(folderObj.getObjectID())));
                    }
                    oclPerm.setFolderAdmin(true);
                    found = true;
                }
            }
            if (!found) {
                final OCLPermission oclPerm = new OCLPermission();
                oclPerm.setEntity(creator);
                oclPerm.setGroupPermission(false);
                oclPerm.setFolderAdmin(true);
                oclPerm.setAllPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                oclPerm.setSystem(0);
                final List<OCLPermission> nList = new ArrayList<OCLPermission>(folderObj.getPermissions());
                nList.add(oclPerm);
                adminEntities.add(creator);
                folderObj.setPermissions(nList);
            }
        }
        if (adminEntities.isEmpty()) {
            throw OXFolderExceptionCode.NO_FOLDER_ADMIN.create();
        } else if (isDefaultFolder && !creatorIsAdmin) {
            throw OXFolderExceptionCode.CREATOR_IS_NOT_ADMIN.create(Integer.valueOf(creator), Integer.valueOf(folderObj.getObjectID()));
        }
    }

    private static void checkPrivateAdminPerm(final int adminPermissionCount, final int creator, final OCLPermission oclPerm) throws OXException {
        if (adminPermissionCount > 1) {
            throw OXFolderExceptionCode.ONLY_ONE_PRIVATE_FOLDER_ADMIN.create();
        }
        if (oclPerm.isGroupPermission()) {
            throw OXFolderExceptionCode.NO_PRIVATE_FOLDER_ADMIN_GROUP.create();
        }
        if (creator != oclPerm.getEntity()) {
            throw OXFolderExceptionCode.ONLY_PRIVATE_FOLDER_OWNER_ADMIN.create();
        }
    }

    /**
     * Checks that any permission-related modifications to a folder are allowed based on the session user's module access permissions and
     * capabilities.
     *
     * @param session The session
     * @param folder The folder being saved
     * @param originalPermissions The non-system permissions of the original folder in case of updates, or the parent folder's non-system permissions for new folders
     */
    public static void checkPermissionsAgainstSessionUserConfig(Session session, FolderObject folder, OCLPermission[] originalPermissions) throws OXException {
        /*
         * check for added, removed or modified permissions
         */
        OCLPermission[] newPermissions = folder.getNonSystemPermissionsAsArray();
        List<OCLPermission> touchedPermissions = getTouchedPermissions(newPermissions, originalPermissions);
        if (null == touchedPermissions || 0 == touchedPermissions.size()) {
            return; // no changes
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ShareService shareService = ServerServiceRegistry.getServize(ShareService.class);
        CapabilityService capabilityService = ServerServiceRegistry.getServize(CapabilityService.class);
        CapabilitySet capabilities = null != capabilityService ? capabilityService.getCapabilities(session) : null;
        for (OCLPermission permission : touchedPermissions) {
            GuestInfo guestInfo = null == shareService || permission.isGroupPermission() ? null : shareService.getGuestInfo(session, permission.getEntity());
            if (null == guestInfo) {
                /*
                 * internal permission entity, check "readcreatesharedfolders" / "editpublicfolders" flag for non-infostore PIM folders
                 */
                if (permission.getEntity() != session.getUserId() && FolderObject.INFOSTORE != folder.getModule()) {
                    if (FolderObject.PRIVATE == folder.getType() && false == serverSession.getUserConfiguration().hasFullSharedFolderAccess() ||
                        FolderObject.PUBLIC == folder.getType() && false == serverSession.getUserConfiguration().hasFullPublicFolderAccess()) {
                        throw OXFolderExceptionCode.SHARE_FORBIDDEN.create(I(session.getUserId()), I(folder.getObjectID()), I(session.getContextId()));
                    }
                }
            } else if (RecipientType.ANONYMOUS.equals(guestInfo.getRecipientType())) {
                /*
                 * anonymous link permission entity, check if link was added, modified or removed
                 */
                OCLPermission originalLinkPermission = getPermissionByEntity(originalPermissions, permission.getEntity());
                OCLPermission newLinkPermission = getPermissionByEntity(newPermissions, permission.getEntity());
                if (null == originalLinkPermission) {
                    /*
                     * added link, check link target, assigned permissions & "share_links" capability
                     */
                    if (false == matches(guestInfo.getLinkTarget(), folder)) {
                        throw ShareExceptionCodes.NO_MULTIPLE_TARGETS_LINK.create();
                    }
                    if (null == capabilities || false == capabilities.contains("share_links")) {
                        throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
                    }
                    if (false == isAllowedLinkPermission(newLinkPermission)) {
                        throw ShareExceptionCodes.INVALID_LINK_PERMISSION.create();
                    }
                } else if (null == newLinkPermission) {
                    if (matches(guestInfo.getLinkTarget(), folder)) {
                        /*
                         * removed link during update, check "share_links" capability
                         */
                        if (null == capabilities || false == capabilities.contains("share_links")) {
                            throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
                        }
                    } else {
                        /*
                         * another share target's link is not inherited during creation, okay
                         */
                    }
                } else if (false == originalLinkPermission.equals(newLinkPermission)) {
                    /*
                     * modified link, re-check assigned permissions & "share_links" capability
                     */
                    if (null == capabilities || false == capabilities.contains("share_links")) {
                        throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
                    }
                    if (false == isAllowedLinkPermission(newLinkPermission)) {
                        throw ShareExceptionCodes.INVALID_LINK_PERMISSION.create();
                    }
                }
            } else if (RecipientType.GUEST.equals(guestInfo.getRecipientType())) {
                /*
                 * external guest permission entity, check "readcreatesharedfolders" / "editpublicfolders" flag for non-infostore PIM folders,
                 * but require "invite_guests" capability for all folders
                 */
                if (permission.getEntity() != session.getUserId()) {
                    if (FolderObject.INFOSTORE != folder.getModule() && (
                        FolderObject.PRIVATE == folder.getType() && false == serverSession.getUserConfiguration().hasFullSharedFolderAccess() ||
                        FolderObject.PUBLIC == folder.getType() && false == serverSession.getUserConfiguration().hasFullPublicFolderAccess())) {
                        throw OXFolderExceptionCode.SHARE_FORBIDDEN.create(I(session.getUserId()), I(folder.getObjectID()), I(session.getContextId()));
                    }
                    if (null == capabilities || false == capabilities.contains("invite_guests")) {
                        throw ShareExceptionCodes.NO_INVITE_GUEST_PERMISSION.create();
                    }
                }
            }
        }
    }

    /**
     * Gets a list of permissions that have been either been added, removed, or modified based on the supplied new- and original permissions.
     *
     * @param newPermissions The new permissions, or <code>null</code> if not passed
     * @param originalPermissions The original permissions, or <code>null</code> if there are none
     * @return A list of permissions that have been added, modified or removed, or <code>null</code> or an empty list if there are none
     */
    private static List<OCLPermission> getTouchedPermissions(OCLPermission[] newPermissions, OCLPermission[] originalPermissions) {
        if (null == newPermissions) {
            return null; // no changes
        }
        /*
         * check for added, removed or modified permissions
         */
        List<OCLPermission> touchedPermissions = new ArrayList<OCLPermission>();
        for (OCLPermission permission : newPermissions) {
            OCLPermission originalPermission = getPermissionByEntity(originalPermissions, permission.getEntity());
            if (null == originalPermission || false == originalPermission.equals(permission)) {
                touchedPermissions.add(permission);
            }
        }
        if (null != originalPermissions) {
            for (OCLPermission permission : originalPermissions) {
                if (null == getPermissionByEntity(newPermissions, permission.getEntity())) {
                    touchedPermissions.add(permission);
                }
            }
        }
        return touchedPermissions;
    }

    /**
     * Searches an array of permissions for a specific entity.
     *
     * @param permissions The permissions to search in
     * @param entity The identifier of the entity to lookup
     * @return The entity's permission, or <code>null</code> if not present in the supplied permissions
     */
    private static OCLPermission getPermissionByEntity(OCLPermission[] permissions, int entity) {
        if (null != permissions) {
            for (OCLPermission permission : permissions) {
                if (permission.getEntity() == entity) {
                    return permission;
                }
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether a share target points to a specific folder or not.
     *
     * @param shareTarget The share target to check
     * @param folder The folder to match
     * @return <code>true</code> if the share target matches the folder, <code>false</code>, otherwise
     */
    private static boolean matches(ShareTarget shareTarget, FolderObject folder) {
        if (null != shareTarget && shareTarget.isFolder() && shareTarget.getModule() == folder.getModule()) {
            try {
                return folder.getObjectID() == Integer.parseInt(shareTarget.getFolder());
            } catch (NumberFormatException e) {
                LOG.warn("Unexpected database folder ID", e);
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the set permissions bits for an an anonymous link allowed or not.
     *
     * @param permission The permission to check
     * @return <code>true</code> if the permission is allowed, <code>false</code>, otherwise
     */
    private static boolean isAllowedLinkPermission(OCLPermission permission) {
        if (permission.isFolderAdmin() || permission.isGroupPermission() || permission.getFolderPermission() != Permission.READ_FOLDER ||
            permission.getReadPermission() != Permission.READ_ALL_OBJECTS || permission.getWritePermission() != Permission.NO_PERMISSIONS ||
            permission.getDeletePermission() != Permission.NO_PERMISSIONS) {
            return false;
        }
        return true;
    }

    /**
     * Checks system folder permissions.
     *
     * @param folderId The folder ID
     * @param newPerms The update-operation permissions
     * @param ctx The context
     * @throws OXException If changing system folder's permission is denied
     */
    public static void checkSystemFolderPermissions(final int folderId, final OCLPermission[] newPerms, final User user, final Context ctx) throws OXException {
        if (folderId >= FolderObject.MIN_FOLDER_ID) {
            return;
        }
        final int[] allowedObjectPermissions = maxAllowedObjectPermissions(folderId);
        final int allowedFolderPermission = maxAllowedFolderPermission(folderId);
        final int admin = ctx.getMailadmin();
        if (FolderObject.SYSTEM_LDAP_FOLDER_ID == folderId) {
            for (final OCLPermission newPerm : newPerms) {
                if (newPerm.isGroupPermission()) {
                    throw OXFolderExceptionCode.NO_GROUP_PERMISSION.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                /*
                 * Only context admin may hold administer right and folder visibility change only
                 */
                checkSystemFolderObjectPermissions(folderId, newPerm, admin, allowedObjectPermissions, allowedFolderPermission, user, ctx);
            }
        } else {
            for (OCLPermission newPerm2 : newPerms) {
                final OCLPermission newPerm = newPerm2;
                if (!newPerm.isGroupPermission() && newPerm.getEntity() != admin) {
                    throw OXFolderExceptionCode.NO_INDIVIDUAL_PERMISSION.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                /*
                 * Only context admin may hold administer right and folder visibility change only
                 */
                checkSystemFolderObjectPermissions(folderId, newPerm, admin, allowedObjectPermissions, allowedFolderPermission, user, ctx);
            }
        }
    }

    private static void checkSystemFolderObjectPermissions(final int folderId, final OCLPermission toCheck, final int admin, final int[] allowedObjectPermissions, final int maxAllowedFolderPermission, final User user, final Context ctx) throws OXException {
        /*
         * Only context admin may hold administer right and folder visibility change only
         */
        if ((toCheck.getEntity() == admin ? !toCheck.isFolderAdmin() : toCheck.isFolderAdmin()) || !checkObjectPermissions(toCheck, allowedObjectPermissions) || toCheck.getFolderPermission() > maxAllowedFolderPermission) {
            throw OXFolderExceptionCode.FOLDER_VISIBILITY_PERMISSION_ONLY.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
        }
    }

    private static boolean checkObjectPermissions(final OCLPermission p, final int[] allowedObjectPermissions) {
        return (p.getReadPermission() == allowedObjectPermissions[0]) && (p.getWritePermission() == allowedObjectPermissions[1]) && (p.getDeletePermission() == allowedObjectPermissions[2]);
    }

    private static final int[] NO_OBJECT_PERMISSIONS = { OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS };

    private static int[] maxAllowedObjectPermissions(final int folderId) {
        if (FolderObject.SYSTEM_LDAP_FOLDER_ID == folderId) {
            return new int[] { OCLPermission.READ_ALL_OBJECTS, OXFolderProperties.isEnableInternalUsersEdit() ? OCLPermission.WRITE_OWN_OBJECTS : OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS };
        } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
            return NO_OBJECT_PERMISSIONS;
        } else {
            return NO_OBJECT_PERMISSIONS;
        }
    }

    private static int maxAllowedFolderPermission(final int folderId) {
        if (FolderObject.SYSTEM_LDAP_FOLDER_ID == folderId) {
            return OCLPermission.READ_FOLDER;
        } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
            return OCLPermission.CREATE_SUB_FOLDERS;
        } else if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
            return OCLPermission.CREATE_SUB_FOLDERS;
        } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
            return OCLPermission.READ_FOLDER;
        } else {
            return OCLPermission.NO_PERMISSIONS;
        }
    }

    /**
     * Gets the permissions without folder access by comparing specified storage-version permissions with update-operation permissions
     *
     * @param newPerms The update-operation permissions
     * @param storagePerms The storage-version permissions
     * @return The permissions without folder access
     */
    public static OCLPermission[] getPermissionsWithoutFolderAccess(final OCLPermission[] newPerms, final OCLPermission[] storagePerms) {
        final List<OCLPermission> removed = new ArrayList<OCLPermission>(4);
        final TIntSet entities = new TIntHashSet(4);
        for (final OCLPermission storagePerm : storagePerms) {
            boolean found = false;
            for (int i = 0; i < newPerms.length && !found; i++) {
                if (newPerms[i].getEntity() == storagePerm.getEntity()) {
                    /*
                     * Still present in new permissions
                     */
                    found = (newPerms[i].getFolderPermission() > OCLPermission.NO_PERMISSIONS);
                }
            }
            if (!found) {
                removed.add(storagePerm);
                entities.add(storagePerm.getEntity());
            }
        }
        for (final OCLPermission newPerm : newPerms) {
            boolean found = false;
            for (int i = 0; i < storagePerms.length && !found; i++) {
                if (newPerm.getEntity() == storagePerms[i].getEntity()) {
                    found = true;
                }
            }
            if (!found && newPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS && !entities.contains(newPerm.getEntity())) {
                /*
                 * A newly added permission which grants no folder-read access
                 */
                removed.add(newPerm);
            }
        }
        return removed.toArray(new OCLPermission[removed.size()]);
    }

    /**
     * Checks every <b>user permission</b> against user configuration settings
     *
     * @param folderObj The folder object
     * @param ctx The context
     * @throws OXException If a composed permission does not obey user's configuration
     */
    public static void checkPermissionsAgainstUserConfigs(final FolderObject folderObj, final Context ctx) throws OXException {
        final int size = folderObj.getPermissions().size();
        final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
        final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
        for (int i = 0; i < size; i++) {
            final OCLPermission assignedPerm = iter.next();
            if (!assignedPerm.isGroupPermission()) {
                final OCLPermission maxApplicablePerm = getMaxApplicablePermission(folderObj, userConfigStorage.getUserConfiguration(assignedPerm.getEntity(), ctx));
                if (!isApplicable(maxApplicablePerm, assignedPerm)) {
                    throw OXFolderExceptionCode.UNAPPLICABLE_FOLDER_PERM.create(Integer.valueOf(assignedPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
            }
        }
    }

    /**
     * Checks every <b>user permission</b> against user configuration settings
     *
     * @param con The current db connection
     * @param folderObj The folder object
     * @param ctx The context
     * @throws OXException If a composed permission does not obey user's configuration
     */
    public static void checkPermissionsAgainstUserConfigs(final Connection con, final FolderObject folderObj, final Context ctx) throws OXException {
        final int size = folderObj.getPermissions().size();
        final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
        UserPermissionBitsStorage permissionBitsStorage = UserPermissionBitsStorage.getInstance();
        for (int i = 0; i < size; i++) {
            final OCLPermission assignedPerm = iter.next();
            if (!assignedPerm.isGroupPermission()) {
                final UserPermissionBits userPermissionBits = permissionBitsStorage.getUserPermissionBits(con, assignedPerm.getEntity(), ctx);
                final OCLPermission maxApplicablePerm = getMaxApplicablePermission(folderObj, userPermissionBits);
                if (!isApplicable(maxApplicablePerm, assignedPerm)) {
                    UserService service = ServerServiceRegistry.getServize(UserService.class);
                    if (service == null) {
                        throw OXFolderExceptionCode.UNAPPLICABLE_FOLDER_PERM.create(Integer.valueOf(assignedPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
                    }
                    User usr = null;
                    try {
                        usr = service.getUser(Integer.valueOf(assignedPerm.getEntity()), ctx);
                    } catch (OXException ex) {
                        throw OXFolderExceptionCode.UNAPPLICABLE_FOLDER_PERM.create(Integer.valueOf(assignedPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
                    }
                    if (usr == null) {
                        throw OXFolderExceptionCode.UNAPPLICABLE_FOLDER_PERM.create(Integer.valueOf(assignedPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
                    }
                    throw OXFolderExceptionCode.UNAPPLICABLE_FOLDER_PERM_EXTENDED.create(usr.getDisplayName(), Integer.valueOf(assignedPerm.getEntity()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
            }
        }
    }

    private static OCLPermission getMaxApplicablePermission(final FolderObject folderObj, final UserPermissionBits permissionBits) {
        final int userId = permissionBits.getUserId();
        final EffectivePermission retval = new EffectivePermission(userId, folderObj.getObjectID(), folderObj.getType(userId), folderObj.getModule(), folderObj.getCreatedBy(), permissionBits);
        retval.setFolderAdmin(true);
        retval.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        return retval;
    }

    private static OCLPermission getMaxApplicablePermission(final FolderObject folderObj, final UserConfiguration userConfig) {
        final EffectivePermission retval = new EffectivePermission(userConfig.getUserId(), folderObj.getObjectID(), folderObj.getType(userConfig.getUserId()), folderObj.getModule(), folderObj.getCreatedBy(), userConfig);
        retval.setFolderAdmin(true);
        retval.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        return retval;
    }

    private static boolean isApplicable(final OCLPermission maxApplicablePerm, final OCLPermission assignedPerm) {
        if (!maxApplicablePerm.isFolderAdmin() && assignedPerm.isFolderAdmin()) {
            return false;
        }
        return (maxApplicablePerm.getFolderPermission() >= assignedPerm.getFolderPermission() && maxApplicablePerm.getReadPermission() >= assignedPerm.getReadPermission() && maxApplicablePerm.getWritePermission() >= assignedPerm.getWritePermission() && maxApplicablePerm.getDeletePermission() >= assignedPerm.getDeletePermission());
    }

    /**
     * This routine ensures that owner of parental shared folder gets full access (incl. folder admin) to shared subfolder
     *
     * @param parentOwner The user ID of parent folder owner
     * @param folderObj The shared subfolder
     * @param userId The user ID
     * @param ctx The context
     * @throws OXException If permission check fails
     */
    public static void checkSharedSubfolderOwnerPermission(final FolderObject parent, final FolderObject folderObj, final int userId, final Context ctx) throws OXException {
        final List<OCLPermission> ocls = folderObj.getPermissions();
        final int size = ocls.size();
        /*
         * Look for existing permissions for parent owner
         */
        boolean pownerFound = false;
        for (int i = 0; i < size; i++) {
            final OCLPermission cur = ocls.get(i);
            if (cur.getEntity() == parent.getCreatedBy()) {
                /*
                 * In any case grant full access
                 */
                cur.setFolderAdmin(true);
                cur.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                pownerFound = true;
            } else if (cur.isFolderAdmin()) {
                throw OXFolderExceptionCode.INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION.create(Integer.valueOf(userId), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderObj.getObjectID()), Integer.valueOf(ctx.getContextId()), Integer.valueOf(parent.getObjectID()));
            }
        }
        if (!pownerFound) {
            /*
             * Add full permission for parent folder owner
             */
            final OCLPermission pownerPerm = new OCLPermission();
            pownerPerm.setEntity(parent.getCreatedBy());
            pownerPerm.setFolderAdmin(true);
            pownerPerm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocls.add(pownerPerm);
        }
        folderObj.setPermissionsNoClone(ocls);
    }

    private static final int[] SORTED_STD_MODULES = { FolderObject.TASK, FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.UNBOUND };

    /**
     * Checks specified new folder module against parent folder
     *
     * @param parentId The parent folder ID
     * @param parentModule The parent module
     * @param newFolderModule The new folder module
     * @param cid The context ID
     * @return <code>true</code> if parent folder allows specified new folder module; otherwise <code>false</code>
     * @throws OXException If specified module is unknown
     */
    public static boolean checkFolderModuleAgainstParentModule(final int parentId, final int parentModule, final int newFolderModule, final int cid) throws OXException {
        if (parentModule == FolderObject.TASK || parentModule == FolderObject.CALENDAR || parentModule == FolderObject.CONTACT) {
            return (Arrays.binarySearch(SORTED_STD_MODULES, newFolderModule) >= 0);
        } else if (parentModule == FolderObject.SYSTEM_MODULE) {
            if (parentId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID || parentId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
                return (Arrays.binarySearch(SORTED_STD_MODULES, newFolderModule) >= 0);
            } else if (parentId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                return (newFolderModule == FolderObject.INFOSTORE);
            }
        } else if (parentModule == FolderObject.INFOSTORE) {
            return (newFolderModule == FolderObject.INFOSTORE);
        } else {
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(Integer.valueOf(parentModule), Integer.valueOf(cid));
        }
        return true;
    }

    /**
     * Checks specified folder type against parent folder
     *
     * @param parentFolder The parent folder
     * @param newFolderType The folder type
     * @return <code>true</code> if parent allows specified folder type; otherwise <code>false</code>
     */
    public static boolean checkFolderTypeAgainstParentType(final FolderObject parentFolder, final int newFolderType) {
        final int enforcedType;
        switch (parentFolder.getObjectID()) {
            case FolderObject.SYSTEM_PRIVATE_FOLDER_ID:
                enforcedType = FolderObject.PRIVATE;
                break;
            case FolderObject.SYSTEM_PUBLIC_FOLDER_ID:
                enforcedType = FolderObject.PUBLIC;
                break;
            case FolderObject.SYSTEM_INFOSTORE_FOLDER_ID:
                enforcedType = FolderObject.PUBLIC;
                break;
            case FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID:
                enforcedType = FolderObject.PUBLIC;
                break;
            case FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID:
                enforcedType = FolderObject.PUBLIC;
                break;
            default:
                enforcedType = parentFolder.getType();
                break;
        }
        return (newFolderType == enforcedType);
    }

    /**
     * Checks if specified folder ID is a descendant folder
     *
     * @param parentIDList The parent IDs
     * @param possibleDescendant The ID of possible descendant folder
     * @param readCon A connection with read-only capability
     * @param ctx The context
     * @return <code>true</code> if specified folder ID is a descendant folder; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static boolean isDescendentFolder(final TIntList parentIDList, final int possibleDescendant, final Connection readCon, final Context ctx) throws SQLException, OXException {
        final int size = parentIDList.size();
        boolean isDescendant = false;
        for (int i = 0; i < size && !isDescendant; i++) {
            final TIntList subfolderIDs = OXFolderSQL.getSubfolderIDs(parentIDList.get(i), readCon, ctx);
            final int subsize = subfolderIDs.size();
            for (int j = 0; j < subsize && !isDescendant; j++) {
                final int current = subfolderIDs.get(j);
                isDescendant |= (current == possibleDescendant);
            }
            if (isDescendant) {
                /*
                 * Matching descendant found
                 */
                return true;
            }
            /*
             * Recursive call with collected subfolder ids
             */
            isDescendant = isDescendentFolder(subfolderIDs, possibleDescendant, readCon, ctx);
        }
        return isDescendant;
    }

    /**
     * Checks if given update object indicates to perform a rename-only operation compared to storage object.
     *
     * @param updateObject The update object
     * @param storageObject The storage object
     * @return <code>true</code> if given update object indicates to perform a rename-only operation; otherwise <code>false</code>
     */
    public static boolean isRenameOnly(final FolderObject updateObject, final FolderObject storageObject) {
        if (!updateObject.containsFolderName()) {
            return false;
        }
        final String newName = updateObject.getFolderName();
        if (null == newName || newName.equals(storageObject.getFolderName())) {
            return false;
        }
        /*-
         * Ok, folder name differs. Check other fields.
         *
         * Permissions
         */
        if (updateObject.containsPermissions()) {
            final List<OCLPermission> updateList = Arrays.asList(updateObject.getNonSystemPermissionsAsArray());
            if (null != updateList && isDifferent(Arrays.asList(storageObject.getNonSystemPermissionsAsArray()), updateList)) {
                return false;
            }
        }
        /*
         * Parent
         */
        if (updateObject.containsParentFolderID()) {
            final int updateParent = updateObject.getParentFolderID();
            if (updateParent > 0 && storageObject.getParentFolderID() != updateParent) {
                return false;
            }
        }
        /*
         * Module
         */
        if (updateObject.containsModule()) {
            final int updateModule = updateObject.getModule();
            if (updateModule > 0 && storageObject.getModule() != updateModule) {
                return false;
            }
        }
        /*
         * Rename only
         */
        return true;
    }

    /**
     * Checks if specified permission lists are different.
     *
     * @param storageList The storage-version permissions
     * @param updateList The update-version permissions
     * @return <code>true</code> if different; otherwise <code>false</code>
     */
    public static boolean isDifferent(final List<OCLPermission> storageList, final List<OCLPermission> updateList) {
        if (updateList.isEmpty()) {
            return false;
        }
        final int ssize = storageList.size();
        for (final OCLPermission update : updateList) {
            boolean found = false;
            for (int i = 0; i < ssize && !found; i++) {
                final OCLPermission storage = storageList.get(i);
                if (storage.getEntity() == update.getEntity()) {
                    found = true;
                    if (!update.equalsPermission(storage)) {
                        return true;
                    }

                }
            }
            if (!found) {
                return true;
            }
        }
        /*
         * Same
         */
        return false;
    }

    /**
     * Gets those entities which are new or updated in given update list compared to given storage list or whole update list if storage list
     * is <code>null</code>.
     *
     * @param storageList The storage list of permissions (if <code>null</code> whole update list entities are added)
     * @param updateList The update list of permissions
     * @param user The user ID
     * @return A set of <code>int</code> containing share user IDs.
     */
    public static TIntSet getShareUsers(final List<OCLPermission> storageList, final List<OCLPermission> updateList, final int user, final Context ctx) {
        final TIntSet retval = new TIntHashSet();
        if (null == storageList) {
            for (final OCLPermission update : updateList) {
                addPermissionUsers(update, retval, ctx);
            }
        } else {
            final int ssize = storageList.size();
            for (final OCLPermission update : updateList) {
                boolean found = false;
                for (int i = 0; i < ssize && !found; i++) {
                    final OCLPermission storage = storageList.get(i);
                    if (storage.getEntity() == update.getEntity()) {
                        found = true;
                        if (!update.equalsPermission(storage)) {
                            addPermissionUsers(update, retval, ctx);
                        }

                    }
                }
                if (!found) {
                    addPermissionUsers(update, retval, ctx);
                }
            }
        }
        /*
         * Remove user ID...
         */
        retval.remove(user);
        /*
         * ... and return set
         */
        return retval;
    }

    /**
     * Adds permission-associated user ID or group member IDs to specified set dependent on whether given permission denotes an user or a
     * group permission.
     *
     * @param permission The permission
     * @param retval The set of user IDs
     * @param ctx The context (possibly needed to resolve group)
     */
    private static void addPermissionUsers(final OCLPermission permission, final TIntSet retval, final Context ctx) {
        if (permission.isGroupPermission()) {
            /*
             * Resolve group
             */
            try {
                for (int member : GroupStorage.getInstance().getGroup(permission.getEntity(), ctx).getMember()) {
                    retval.add(member);
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }
        } else {
            retval.add(permission.getEntity());
        }
    }

    /**
     * Gets the folder name for logging/messaging purpose
     *
     * @param folder The folder
     * @param context The context
     * @return The folder name for logging/messaging purpose
     */
    public static String getFolderName(FolderObject folder, Context context) {
        final String folderName = folder.getFolderName();
        if (null == folderName) {
            return getFolderName(folder.getObjectID(), context);
        }
        return new StringBuilder().append(folderName).append(" (").append(folder.getObjectID()).append(')').toString();
    }

    /**
     * Gets the folder name for logging/messaging purpose
     *
     * @param fo The folder
     * @return The folder name for logging/messaging purpose
     */
    public static String getFolderName(final FolderObject fo) {
        final String folderName = fo.getFolderName();
        if (null == folderName) {
            return Integer.toString(fo.getObjectID());
        }
        return new StringBuilder().append(folderName).append(" (").append(fo.getObjectID()).append(')').toString();
    }

    /**
     * Gets the folder name for logging/messaging purpose
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @return The folder name for logging/messaging purpose
     */
    public static String getFolderName(final int folderId, final Context ctx) {
        try {
            return new StringBuilder().append(new OXFolderAccess(ctx).getFolderName(folderId)).append(" (").append(folderId).append(')').toString();
        } catch (final OXException e) {
            return String.valueOf(folderId);
        }
    }

    /**
     * Gets the user name for logging/messaging purpose.
     *
     * @param session The session
     * @param u The user
     * @return The user name for logging/messaging purpose.
     */
    public static String getUserName(final Session session, final User u) {
        if (session == null) {
            return STR_EMPTY;
        }
        if (u.getDisplayName() == null) {
            return new StringBuilder().append(u.getGivenName()).append(' ').append(u.getSurname()).append(" (").append(u.getId()).append(')').toString();
        }
        return new StringBuilder().append(u.getDisplayName()).append(" (").append(u.getId()).append(')').toString();
    }

    /**
     * Gets the user name for logging/messaging purpose.
     *
     * @param userId The user ID
     * @param ctx The context
     * @return The user name for logging/messaging purpose.
     */
    public static String getUserName(final int userId, final Context ctx) {
        final User u;
        try {
            u = UserStorage.getInstance().getUser(userId, ctx);
        } catch (final OXException e) {
            return String.valueOf(userId);
        }
        if (u == null) {
            return String.valueOf(userId);
        } else if (u.getDisplayName() == null) {
            return new StringBuilder().append(u.getGivenName()).append(' ').append(u.getSurname()).append(" (").append(userId).append(')').toString();
        }
        return new StringBuilder().append(u.getDisplayName()).append(" (").append(userId).append(')').toString();
    }

    /**
     * Gets the user name for logging/messaging purpose.
     *
     * @param session The server session
     * @return The user name for logging/messaging purpose.
     */
    public static String getUserName(final ServerSession session) {
        final User u = session.getUser();
        if (u.getDisplayName() == null) {
            return new StringBuilder().append(u.getGivenName()).append(' ').append(u.getSurname()).append(" (").append(u.getId()).append(')').toString();
        }
        return new StringBuilder().append(u.getDisplayName()).append(" (").append(u.getId()).append(')').toString();
    }

    /**
     * Gets the group name for logging/messaging purpose.
     *
     * @param groupId The group ID
     * @param ctx The context
     * @return The group name for logging/messaging purpose.
     */
    public static String getGroupName(final int groupId, final Context ctx) {
        final Group g;
        try {
            g = GroupStorage.getInstance().getGroup(groupId, ctx);
        } catch (final OXException e) {
            return String.valueOf(groupId);
        }
        if (g == null) {
            return String.valueOf(groupId);
        } else if (g.getDisplayName() == null) {
            return new StringBuilder().append(g.getSimpleName()).append(" (").append(groupId).append(')').toString();
        }
        return new StringBuilder().append(g.getDisplayName()).append(" (").append(groupId).append(')').toString();
    }

    private static final String STR_TYPE_PRIVATE = "'private'";

    private static final String STR_TYPE_PUBLIC = "'public'";

    private static final String STR_SYSTEM = "'system'";

    private static final String STR_UNKNOWN = "'unknown'";

    /**
     * Gets the given type's string representation
     *
     * @param type The type
     * @return The type's string representation
     */
    public static String folderType2String(final int type) {
        switch (type) {
            case FolderObject.PRIVATE:
                return STR_TYPE_PRIVATE;
            case FolderObject.PUBLIC:
                return STR_TYPE_PUBLIC;
            case FolderObject.SYSTEM_TYPE:
                return STR_SYSTEM;
            default:
                return STR_UNKNOWN;
        }
    }

    private static final String STR_MODULE_CALENDAR = "'calendar'";

    private static final String STR_MODULE_TASK = "'task'";

    private static final String STR_MODULE_CONTACT = "'contact'";

    private static final String STR_MODULE_UNBOUND = "'unbound'";

    private static final String STR_MODULE_INFOSTORE = "'infostore'";

    /**
     * Gets the given module's string representation
     *
     * @param module The module
     * @return The module's string representation
     */
    public static String folderModule2String(final int module) {
        switch (module) {
            case FolderObject.CALENDAR:
                return STR_MODULE_CALENDAR;
            case FolderObject.TASK:
                return STR_MODULE_TASK;
            case FolderObject.CONTACT:
                return STR_MODULE_CONTACT;
            case FolderObject.UNBOUND:
                return STR_MODULE_UNBOUND;
            case FolderObject.SYSTEM_MODULE:
                return STR_SYSTEM;
            case FolderObject.INFOSTORE:
                return STR_MODULE_INFOSTORE;
            default:
                return STR_UNKNOWN;
        }
    }

    private static final String COLUMN_FNAME = "fname";

    /**
     * Gets the appropriate localizable field name for specified column
     *
     * @param column The column
     * @return The field name or <code>null</code>
     */
    public static String column2Field(String column) {
        if (null == column) {
            return null;
        }
        if (COLUMN_FNAME.equalsIgnoreCase(column)) {
            return FolderStrings.FIELD_FOLDER_NAME;
        }
        return null;
    }

    /**
     * Deserializes a an arbitrary meta map (as used in a folder's meta field) from the supplied input stream
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized map
     */
    public static Map<String, Object> deserializeMeta(InputStream inputStream) throws JSONException {
        return new JSONObject(new AsciiReader(inputStream)).asMap();
    }

    /**
     * Serializes an arbitrary meta map (as used in a folder's meta field) to an input stream.
     *
     * @param meta The map to serialize, or <code>null</code>
     * @return The serialized map data, or <code>null</code> if the map is empty
     */
    public static InputStream serializeMeta(Map<String, Object> meta) throws JSONException {
        if (null == meta || meta.isEmpty()) {
            return null;
        }
        Object coerced = JSONCoercion.coerceToJSON(meta);
        if (null == coerced || JSONObject.NULL.equals(coerced)) {
            return null;
        }
        return new JSONInputStream((JSONValue) coerced, "US-ASCII");
    }

}
