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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
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

    /**
     * Gets the subfolder identifiers of database folder representing system root folder for given user.
     *
     * @return The subfolder identifiers of database folder representing system root folder for given user
     */
    public static List<String[]> getSystemRootFolderSubfolder(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        return getSystemRootFolderSubfolder(user.getLocale());
    }

    private static final ConcurrentMap<Locale, List<String[]>> CACHED_SUBFOLDERS = new ConcurrentHashMap<Locale, List<String[]>>(16);

    /**
     * Gets the subfolder identifiers of database folder representing system root folder for given user.
     *
     * @return The subfolder identifiers of database folder representing system root folder for given user
     */
    private static List<String[]> getSystemRootFolderSubfolder(final Locale locale) {
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
