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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link SystemRootFolder} - Gets the system shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemRootFolder {

    /**
     * Initializes a new {@link SystemRootFolder}.
     */
    private SystemRootFolder() {
        super();
    }

    /**
     * Gets the database folder representing system root folder.
     *
     * @param fo The folder object fetched from database
     * @return The database folder representing system root folder
     */
    public static DatabaseFolder getSystemRootFolder() {
        /*
         * The system root folder
         */
        final OCLPermission guestPermission = new OCLPermission(GroupStorage.GUEST_GROUP_IDENTIFIER, FolderObject.SYSTEM_ROOT_FOLDER_ID);
        guestPermission.setFolderAdmin(false);
        guestPermission.setGroupPermission(true);
        guestPermission.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        final FolderObject fo = FolderObject.createVirtualFolderObject(
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            "root",
            FolderObject.SYSTEM_MODULE,
            true,
            FolderObject.SYSTEM_TYPE,
            FolderObject.VIRTUAL_FOLDER_PERMISSION,
            guestPermission);
        final DatabaseFolder retval = new DatabaseFolder(fo);
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);

        return retval;
    }

    private static final ConcurrentMap<Locale, List<String[]>> CACHED_SUBFOLDERS = new ConcurrentHashMap<Locale, List<String[]>>(16);

    /**
     * Gets the subfolder identifiers of database folder representing system root folder for given user.
     *
     * @return The subfolder identifiers of database folder representing system root folder for given user
     */
    public static List<String[]> getSystemRootFolderSubfolder(final Locale locale) {
        /*
         * The system root folder
         */
        List<String[]> list = CACHED_SUBFOLDERS.get(locale);
        if (null == list) {
            final StringHelper sh = StringHelper.valueOf(locale);
            final List<String[]> newList = new ArrayList<String[]>(4);
            newList.add(new String[] { String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), sh.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME) });
            newList.add(new String[] { String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID), sh.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME) });
            newList.add(new String[] { String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID), sh.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME) });
            newList.add(new String[] { String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID), sh.getString(FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME) });
            list = CACHED_SUBFOLDERS.putIfAbsent(locale, newList);
            if (null == list) {
                list = newList;
            }
        }
        return list;
    }

}
