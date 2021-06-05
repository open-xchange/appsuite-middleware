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

package com.openexchange.folderstorage.database.getfolder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.FolderIdNamePair;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

/**
 * {@link SharedPrefixFolder} - Gets the folder whose identifier starts with shared prefix.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SharedPrefixFolder {

    private static final class RemovingProcedure implements TIntProcedure {

        private final TIntObjectMap<FolderObject> map;

        public RemovingProcedure(final TIntObjectMap<FolderObject> map) {
            this.map = map;
        }

        @Override
        public boolean execute(final int folderId) {
            map.remove(folderId);
            return true;
        }
    }

    private static final class DetectDirectSubfoldersProcedure implements TObjectProcedure<FolderObject> {

        private final TIntObjectMap<FolderObject> map;

        private final TIntList toRemove;

        public DetectDirectSubfoldersProcedure(final TIntObjectMap<FolderObject> map, final TIntList toRemove) {
            this.map = map;
            this.toRemove = toRemove;
        }

        @Override
        public boolean execute(final FolderObject folder) {
            /*
             * Check if current folder's parent is contained in map.
             */
            final int parent = folder.getParentFolderID();
            if (map.containsKey(parent)) {
                toRemove.add(folder.getObjectID());
            }
            return true;
        }
    }

    /**
     * Initializes a new {@link SharedPrefixFolder}.
     */
    private SharedPrefixFolder() {
        super();
    }

    /**
     * Checks existence of specified folder identifier starting with shared prefix; meaning folder's owner shared at least one folder to
     * given user.
     *
     * @param folderIdentifier The folder identifier starting with shared prefix
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return <code>true</code> if specified folder identifier starting with shared prefix exists; otherwise <code>false</code>
     * @throws OXException If checking existence fails
     */
    public static boolean existsSharedPrefixFolder(final String folderIdentifier, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        final int sharedOwner;
        try {
            sharedOwner = Integer.parseInt(folderIdentifier.substring(2));
        } catch (NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        return OXFolderIteratorSQL.hasVisibleSharedFolders(
            user.getId(),
            user.getGroups(),
            userPerm.getAccessibleModules(),
            sharedOwner,
            ctx,
            null,
            con);
    }

    /**
     * Gets the folder whose identifier starts with shared prefix.
     *
     * @param folderIdentifier The folder identifier starting with shared prefix
     * @param user The user
     * @param ctx The context
     * @return The corresponding database folder with subfolders set
     * @throws OXException If returning corresponding database folder fails
     */
    public static DatabaseFolder getSharedPrefixFolder(final String folderIdentifier, final User user, final Context ctx) throws OXException {
        final int sharedOwner;
        try {
            sharedOwner = Integer.parseInt(folderIdentifier.substring(2));
        } catch (NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        String creatorDisplayName;
        try {
            creatorDisplayName = UserStorage.getInstance().getUser(sharedOwner, ctx).getDisplayName();
        } catch (OXException e) {
            if (sharedOwner != OCLPermission.ALL_GROUPS_AND_USERS) {
                throw e;
            }
            creatorDisplayName = StringHelper.valueOf(user.getLocale()).getString(Groups.ALL_USERS);
        }
        final FolderObject virtualOwnerFolder = FolderObject.createVirtualSharedFolderObject(sharedOwner, creatorDisplayName);
        /*
         * This highly user-specific folder is NOT cacheable
         */
        final DatabaseFolder retval = new DatabaseFolder(virtualOwnerFolder, false);
        retval.setID(folderIdentifier);
        retval.setParentID(String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID));
        retval.setGlobal(false);
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        return retval;
    }

    /**
     * Gets the folder whose identifier starts with shared prefix.
     *
     * @param folderIdentifier The folder identifier starting with shared prefix
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The corresponding database folder with subfolders set
     * @throws OXException If returning corresponding database folder fails
     */
    public static int[] getSharedPrefixFolderSubfoldersAsInt(final String folderIdentifier, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        final int sharedOwner;
        try {
            sharedOwner = Integer.parseInt(folderIdentifier.substring(2));
        } catch (NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        Queue<FolderObject> q;
        try (FolderObjectIterator foi = (FolderObjectIterator) OXFolderIteratorSQL.getVisibleSharedFolders(user.getId(), user.getGroups(), userPerm.getAccessibleModules(), sharedOwner, ctx, null, con)) {
            q = foi.asQueue();
            if (q.isEmpty()) {
                return new int[0];
            }
        }
        /*
         * Get first level shared folders
         */
        final int size = q.size();
        final TIntObjectMap<FolderObject> set = getFirstLevelSharedFolders(q, size);
        /*
         * Return filtered list
         */
        final TIntList ret = new TIntArrayList(q.size());
        for (final FolderObject fo : q) {
            final int folderId = fo.getObjectID();
            if (set.containsKey(folderId)) {
                ret.add(folderId);
            }
        }
        return ret.toArray();
    }

    /**
     * Gets the folder whose identifier starts with shared prefix.
     *
     * @param folderIdentifier The folder identifier starting with shared prefix
     * @param user The user
     * @param userPermissionBits The user configuration
     * @param ctx The context
     * @param con The connection
     * @return The corresponding database folder with subfolders set
     * @throws OXException If returning corresponding database folder fails
     */
    public static List<FolderIdNamePair> getSharedPrefixFolderSubfolders(final String folderIdentifier, final User user, final UserPermissionBits userPermissionBits, final Context ctx, final Connection con) throws OXException {
        final int sharedOwner;
        try {
            sharedOwner = Integer.parseInt(folderIdentifier.substring(2));
        } catch (NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        Queue<FolderObject> q;
        try (FolderObjectIterator foi = (FolderObjectIterator) OXFolderIteratorSQL.getVisibleSharedFolders(user.getId(), user.getGroups(), userPermissionBits.getAccessibleModules(), sharedOwner, ctx, null, con)) {
            q = foi.asQueue();
            if (q.isEmpty()) {
                return Collections.<FolderIdNamePair> emptyList();
            }
        }
        /*
         * Get first level shared folders
         */
        final int size = q.size();
        final TIntObjectMap<FolderObject> set = getFirstLevelSharedFolders(q, size);
        /*
         * Return filtered list
         */
        final List<FolderIdNamePair> ret = new ArrayList<FolderIdNamePair>(size);
        for (final FolderObject fo : q) {
            final int folderId = fo.getObjectID();
            if (set.containsKey(folderId)) {
                ret.add(new FolderIdNamePair(folderId, fo.getFolderName()));
            }
        }
        return ret;
    }

    private static TIntObjectMap<FolderObject> getFirstLevelSharedFolders(final Queue<FolderObject> q, final int size) {
        /*
         * Generate mapping
         */
        final TIntObjectMap<FolderObject> map = new TIntObjectHashMap<FolderObject>(size);
        for (final FolderObject fo : q) {
            map.put(fo.getObjectID(), fo);
        }
        /*
         * Strip direct subfolders
         */
        {
            final TIntList toRemove = new TIntArrayList(size >> 1);
            map.forEachValue(new DetectDirectSubfoldersProcedure(map, toRemove));
            toRemove.forEach(new RemovingProcedure(map));
        }
        /*
         * Return set view
         */
        return map;
    }

}
