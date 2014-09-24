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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

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
        final List<FolderObject> list = ((FolderObjectIterator) OXFolderIteratorSQL.getUserRootFoldersIterator(user.getId(), user.getGroups(), userPerm, ctx)).asList();
        final List<String[]> ret = new ArrayList<String[]>(list.size());
        for (final FolderObject folderObject : list) {
            int folderId = folderObject.getObjectID();
            String displayName = FolderObject.getFolderString(folderId, user.getLocale());
            if (displayName == null) {
                displayName = folderObject.getFolderName();
            }

            ret.add(new String[] { String.valueOf(folderId), displayName });
        }

        return ret;
    }

}
