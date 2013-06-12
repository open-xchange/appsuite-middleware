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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link SystemInfostoreFolder} - Gets the system infostore folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemInfostoreFolder {

    /**
     * Initializes a new {@link SystemInfostoreFolder}.
     */
    private SystemInfostoreFolder() {
        super();
    }

    /**
     * Gets the database folder representing system infostore folder.
     *
     * @param fo The folder object fetched from database
     * @param altNames <code>true</code> to use alternative names for former InfoStore folders; otherwise <code>false</code>
     * @param translate Whether to translate
     * @return The database folder representing system infostore folder
     */
    public static DatabaseFolder getSystemInfostoreFolder(final FolderObject fo, final boolean altNames, final boolean translate) {
        /*
         * The system infostore folder
         */
        final DatabaseFolder retval = translate ? new LocalizedDatabaseFolder(fo) : new DatabaseFolder(fo);
        retval.setName(altNames ? FolderStrings.SYSTEM_FILES_FOLDER_NAME : FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME);
        retval.setContentType(InfostoreContentType.getInstance());
        // Enforce getSubfolders() on storage
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system infostore folder. <code>false</code>.
     *
     * @param user The user
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system infostore folder
     * @throws OXException If the database folder cannot be returned
     */
    public static int[] getSystemInfostoreFolderSubfoldersAsInt(final User user, final UserConfiguration userConfiguration, final Context ctx, final Connection con) throws OXException {
        try {
            /*
             * The system infostore folder
             */
            final List<FolderObject> l;
            final int size;
            {
                final Queue<FolderObject> q =
                    ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                        FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
                        user.getId(),
                        user.getGroups(),
                        ctx,
                        userConfiguration,
                        null,
                        con)).asQueue();
                size = q.size();
                /*
                 * Write UserStore first
                 */
                final Iterator<FolderObject> iter = q.iterator();
                l = new ArrayList<FolderObject>(size);
                for (int j = 0; j < size; j++) {
                    final FolderObject fobj = iter.next();
                    if (fobj.getObjectID() == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                        l.add(0, fobj);
                    } else {
                        l.add(fobj);
                    }
                }
            }
            final TIntList subfolderIds = new TIntArrayList(size);
            final Iterator<FolderObject> iter = l.iterator();
            for (int i = 0; i < size; i++) {
                subfolderIds.add(iter.next().getObjectID());
            }
            /*
             * Check if user has non-tree-visible folders
             */
            final boolean hasNonTreeVisibleFolders = OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                FolderObject.INFOSTORE,
                user.getId(),
                user.getGroups(),
                userConfiguration,
                ctx,
                con);
            if (hasNonTreeVisibleFolders) {
                subfolderIds.add(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
            }
            return subfolderIds.toArray();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the subfolder identifiers of database folder representing system infostore folder. <code>false</code>.
     *
     * @param user The user
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param altNames TODO
     * @param con The connection
     * @return The database folder representing system infostore folder
     * @throws OXException If the database folder cannot be returned
     */
    public static List<String[]> getSystemInfostoreFolderSubfolders(final User user, final UserConfiguration userConfiguration, final Context ctx, final boolean altNames, final boolean translate, final Connection con) throws OXException {
        try {
            /*
             * The system infostore folder
             */
            final List<FolderObject> l;
            final int size;
            {
                final Queue<FolderObject> q =
                    ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                        FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
                        user.getId(),
                        user.getGroups(),
                        ctx,
                        userConfiguration,
                        null,
                        con)).asQueue();
                size = q.size();
                /*
                 * Write UserStore first
                 */
                final Iterator<FolderObject> iter = q.iterator();
                l = new ArrayList<FolderObject>(size);
                for (int j = 0; j < size; j++) {
                    final FolderObject fobj = iter.next();
                    if (fobj.getObjectID() == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                        l.add(0, fobj);
                    } else {
                        l.add(fobj);
                    }
                }
            }
            final StringHelper sh = translate ? StringHelper.valueOf(user.getLocale()) : null;
            final List<String[]> subfolderIds = new ArrayList<String[]>(size);
            final Iterator<FolderObject> iter = l.iterator();
            for (int i = 0; i < size; i++) {
                final FolderObject fo = iter.next();
                final int fuid = fo.getObjectID();
                if (fuid == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                    subfolderIds.add(toArray(String.valueOf(fuid), null == sh ? (altNames ? FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME : FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME) : (sh.getString(altNames ? FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME : FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME))));
                } else if (fuid == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID) {
                    subfolderIds.add(toArray(String.valueOf(fuid), null == sh ? (altNames ? FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME : FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME) : (sh.getString(altNames ? FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME : FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME))));
                } else {
                    subfolderIds.add(toArray(String.valueOf(fuid), fo.getFolderName()));
                }
            }
            /*
             * Check if user has non-tree-visible folders
             */
            final boolean hasNonTreeVisibleFolders = OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                FolderObject.INFOSTORE,
                user.getId(),
                user.getGroups(),
                userConfiguration,
                ctx,
                con);
            if (hasNonTreeVisibleFolders) {
                subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID), null == sh ? (altNames ? FolderStrings.VIRTUAL_LIST_FILES_FOLDER_NAME: FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME) : (sh.getString(altNames ? FolderStrings.VIRTUAL_LIST_FILES_FOLDER_NAME: FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME))));
            }
            return subfolderIds;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static String[] toArray(final String... values) {
        final int length = values.length;
        final String[] ret = new String[length];
        System.arraycopy(values, 0, ret, 0, length);
        return values;
    }

}
