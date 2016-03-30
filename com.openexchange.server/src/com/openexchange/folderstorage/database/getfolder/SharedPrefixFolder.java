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

package com.openexchange.folderstorage.database.getfolder;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

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
        } catch (final NumberFormatException exc) {
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
        } catch (final NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        String creatorDisplayName;
        try {
            creatorDisplayName = UserStorage.getInstance().getUser(sharedOwner, ctx).getDisplayName();
        } catch (final OXException e) {
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
        } catch (final NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSharedFolders(
                    user.getId(),
                    user.getGroups(),
                    userPerm.getAccessibleModules(),
                    sharedOwner,
                    ctx,
                    null,
                    con)).asQueue();
        if (q.isEmpty()) {
            return new int[0];
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
        } catch (final NumberFormatException exc) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(exc, exc.getMessage());
        }
        final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSharedFolders(
                    user.getId(),
                    user.getGroups(),
                    userPermissionBits.getAccessibleModules(),
                    sharedOwner,
                    ctx,
                    null,
                    con)).asQueue();
        if (q.isEmpty()) {
            return Collections.<FolderIdNamePair> emptyList();
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
