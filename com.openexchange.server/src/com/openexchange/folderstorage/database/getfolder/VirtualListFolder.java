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
import java.util.List;
import java.util.Queue;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;

/**
 * {@link VirtualListFolder} - Gets a virtual list folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualListFolder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VirtualListFolder.class);

    /**
     * Initializes a new {@link VirtualListFolder}.
     */
    private VirtualListFolder() {
        super();
    }

    /**
     * Checks if specified virtual folder identifier exists; meaning corresponding non-tree-visible folders exist.
     *
     * @param folderId The folder identifier
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return <code>true</code> if specified virtual folder identifier exists; otherwise <code>false</code>
     * @throws OXException If checking existence fails
     */
    public static boolean existsVirtualListFolder(final int folderId, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        final int module;
        if (FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID == folderId) {
            // Task
            module = FolderObject.TASK;
        } else if (FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID == folderId) {
            // Calendar
            module = FolderObject.CALENDAR;
        } else if (FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID == folderId) {
            // Contact
            module = FolderObject.CONTACT;
        } else {
            // Infostore
            module = FolderObject.INFOSTORE;
        }
        // Return non-isEmpty()
        return OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(module, user.getId(), user.getGroups(), userPerm, ctx, con);
    }

    /**
     * Gets the database folder representing given virtual folder.
     *
     * @param folderId The virtual folder identifier
     * @param altNames <code>true</code> to use alternative names for former InfoStore folders; otherwise <code>false</code>
     * @return The database folder representing given virtual folder
     */
    public static DatabaseFolder getVirtualListFolder(final int folderId, final boolean altNames) {
        /*
         * A virtual database folder
         */
        final FolderObject fo;
        if (FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID == folderId) {
            // Task
            fo =
                FolderObject.createVirtualFolderObject(
                    folderId,
                    FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME,
                    FolderObject.SYSTEM_MODULE,
                    true,
                    FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else if (FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID == folderId) {
            // Calendar
            fo =
                FolderObject.createVirtualFolderObject(
                    folderId,
                    FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME,
                    FolderObject.SYSTEM_MODULE,
                    true,
                    FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else if (FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID == folderId) {
            // Contact
            fo =
                FolderObject.createVirtualFolderObject(
                    folderId,
                    FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME,
                    FolderObject.SYSTEM_MODULE,
                    true,
                    FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else {
            // Infostore
            fo =
                FolderObject.createVirtualFolderObject(
                    folderId,
                    altNames ? FolderStrings.VIRTUAL_LIST_FILES_FOLDER_NAME : FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME,
                    FolderObject.INFOSTORE,
                    true,
                    FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
        }
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        retval.setGlobal(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing given virtual folder.
     *
     * @param folderId The virtual folder identifier
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection to use
     * @return The subfolder identifiers of database folder representing given virtual folder
     * @throws OXException If returning database folder fails
     */
    public static int[] getVirtualListFolderSubfoldersAsInt(final int folderId, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * Get subfolders
         */
        final int module;
        if (FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID == folderId) {
            // Task
            module = FolderObject.TASK;
        } else if (FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID == folderId) {
            // Calendar
            module = FolderObject.CALENDAR;
        } else if (FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID == folderId) {
            // Contact
            module = FolderObject.CONTACT;
        } else {
            // Infostore
            module = FolderObject.INFOSTORE;
        }
        final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(
                    module,
                    user.getId(),
                    user.getGroups(),
                    userPerm,
                    ctx,
                    con)).asQueue();
        final int[] subfolderIds = new int[q.size()];
        int i = 0;
        for (final FolderObject folderObject : q) {
            subfolderIds[i++] = folderObject.getObjectID();
        }
        return subfolderIds;
    }

    /**
     * Gets the subfolder identifiers of database folder representing given virtual folder.
     *
     * @param folderId The virtual folder identifier
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection to use
     * @return The subfolder identifiers of database folder representing given virtual folder
     * @throws OXException If returning database folder fails
     */
    public static List<String[]> getVirtualListFolderSubfolders(final int folderId, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * Get subfolders
         */
        final int module;
        if (FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID == folderId) {
            // Task
            module = FolderObject.TASK;
        } else if (FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID == folderId) {
            // Calendar
            module = FolderObject.CALENDAR;
        } else if (FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID == folderId) {
            // Contact
            module = FolderObject.CONTACT;
        } else {
            // Infostore
            module = FolderObject.INFOSTORE;
        }
        final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(
                    module,
                    user.getId(),
                    user.getGroups(),
                    userPerm,
                    ctx,
                    con)).asQueue();
        final List<String[]> ret = new ArrayList<String[]>(q.size());
        for (final FolderObject folderObject : q) {
            ret.add(new String[] { String.valueOf(folderObject.getObjectID()), folderObject.getFolderName()});
        }
        return ret;
    }

}
