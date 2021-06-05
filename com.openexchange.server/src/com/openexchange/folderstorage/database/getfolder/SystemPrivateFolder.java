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
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;

/**
 * {@link SystemPrivateFolder} - Gets the system shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemPrivateFolder {

    /**
     * Initializes a new {@link SystemPrivateFolder}.
     */
    private SystemPrivateFolder() {
        super();
    }

    /**
     * Gets the database folder representing system private folder.
     *
     * @param fo The folder object fetched from database
     * @return The database folder representing system private folder
     */
    public static DatabaseFolder getSystemPrivateFolder(final FolderObject fo) {
        /*
         * The system public folder
         */
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
        retval.setName(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME);
        // Enforce getSubfolders() on storage
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system private folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system private folder
     * @throws OXException If the database folder cannot be returned
     */
    public static int[] getSystemPrivateFolderSubfoldersAsInt(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system private folder
         */
        List<FolderObject> list;
        try (FolderObjectIterator foi = (FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, user.getId(), user.getGroups(), ctx, userPerm, null, con)) {
            list = foi.asList();
        }
        StringHelper stringHelper = null;
        for (final FolderObject folderObject : list) {
            /*
             * Check if folder is user's default folder and set locale-sensitive name
             */
            if (folderObject.isDefaultFolder()) {
                final int module = folderObject.getModule();
                if (FolderObject.CALENDAR == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                    }
                } else if (FolderObject.CONTACT == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                    }
                } else if (FolderObject.TASK == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                    }
                }
            }
        }
        /*
         * Extract IDs
         */
        final int[] ret = new int[list.size()];
        int i = 0;
        for (final FolderObject folderObject : list) {
            ret[i++] = folderObject.getObjectID();
        }
        return ret;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system private folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system private folder
     * @throws OXException If the database folder cannot be returned
     */
    public static List<String[]> getSystemPrivateFolderSubfolders(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system private folder
         */
        List<FolderObject> list;
        try (FolderObjectIterator foi = (FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, user.getId(), user.getGroups(), ctx, userPerm, null, con)) {
            list = foi.asList();
        }
        StringHelper stringHelper = null;
        for (final FolderObject folderObject : list) {
            /*
             * Check if folder is user's default folder and set locale-sensitive name
             */
            if (folderObject.isDefaultFolder()) {
                final int module = folderObject.getModule();
                if (FolderObject.CALENDAR == module) {
                    if (null == stringHelper) {
                        stringHelper = StringHelper.valueOf(user.getLocale());
                    }
                    folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                } else if (FolderObject.CONTACT == module) {
                    if (null == stringHelper) {
                        stringHelper = StringHelper.valueOf(user.getLocale());
                    }
                    folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                } else if (FolderObject.TASK == module) {
                    if (null == stringHelper) {
                        stringHelper = StringHelper.valueOf(user.getLocale());
                    }
                    folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                }
            }
        }
        /*
         * Extract IDs
         */
        final List<String[]> ret = new ArrayList<String[]>(list.size());
        for (final FolderObject folderObject : list) {
            ret.add(new String[] {String.valueOf(folderObject.getObjectID()),folderObject.getFolderName()});
        }
        return ret;
    }

}
