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
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;

/**
 * {@link SystemPublicFolder} - Gets the system shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemPublicFolder {

    /**
     * Initializes a new {@link SystemPublicFolder}.
     */
    private SystemPublicFolder() {
        super();
    }

    /**
     * Gets the database folder representing system public folder.
     *
     * @param fo The folder object fetched from database
     * @return The database folder representing system public folder
     */
    public static DatabaseFolder getSystemPublicFolder(final FolderObject fo) {
        /*
         * The system public folder
         */
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
        retval.setName(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME);
        // Enforce getSubfolders() on storage
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system public folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system public folder
     * @throws OXException If the database folder cannot be returned
     */
    public static int[] getSystemPublicFolderSubfoldersAsInt(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system public folder
         */
        final Queue<FolderObject> q =
            ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                user.getId(),
                user.getGroups(),
                ctx,
                userPerm,
                null,
                con)).asQueue();
        final TIntList subfolderIds = new TIntArrayList(q.size());
        /*
         * Add global address book and subfolders
         */
        final CapabilityService capsService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        if (null == capsService || capsService.getCapabilities(user.getId(), ctx.getContextId()).contains("gab")) {
            subfolderIds.add(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        }
        for (final FolderObject folderObject : q) {
            subfolderIds.add(folderObject.getObjectID());
        }
        /*
         * Check for presence of virtual folders
         */
        TIntSet containedModules = OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(new int[] { FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.TASK }, user.getId(), user.getGroups(), userPerm, ctx, con);
        if (containedModules.contains(FolderObject.CALENDAR)) {
            subfolderIds.add(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
        }
        if (containedModules.contains(FolderObject.CONTACT)) {
            subfolderIds.add(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
        }
        if (containedModules.contains(FolderObject.TASK)) {
            subfolderIds.add(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
        }
        return subfolderIds.toArray();
    }

    /**
     * Gets the subfolder identifiers of database folder representing system public folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system public folder
     * @throws OXException If the database folder cannot be returned
     */
    public static List<String[]> getSystemPublicFolderSubfolders(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system public folder
         */
        final Queue<FolderObject> q =
            ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
                user.getId(),
                user.getGroups(),
                ctx,
                userPerm,
                null,
                con)).asQueue();
        final List<String[]> subfolderIds = new ArrayList<String[]>(q.size());
        final StringHelper sh = StringHelper.valueOf(user.getLocale());
        /*
         * Add global address book and subfolders
         */
        final CapabilityService capsService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        if (null == capsService || capsService.getCapabilities(user.getId(), ctx.getContextId()).contains("gab")) {
            subfolderIds.add(toArray(String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID), sh.getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME)));
        }
        for (final FolderObject folderObject : q) {
            subfolderIds.add(toArray(String.valueOf(folderObject.getObjectID()), folderObject.getFolderName()));
        }
        /*
         * Check for presence of virtual folders
         */
        TIntSet containedModules = OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(new int[] { FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.TASK }, user.getId(), user.getGroups(), userPerm, ctx, con);
        if (containedModules.contains(FolderObject.CALENDAR)) {
            subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME)));
        }
        if (containedModules.contains(FolderObject.CONTACT)) {
            subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME)));
        }
        if (containedModules.contains(FolderObject.TASK)) {
            subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME)));
        }
        return subfolderIds;
    }

    private static String[] toArray(final String... values) {
        final int length = values.length;
        final String[] ret = new String[length];
        System.arraycopy(values, 0, ret, 0, length);
        return values;
    }

}
