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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

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
        try {
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
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.CALENDAR,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                }
            }
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.CONTACT,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                }
            }
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.TASK,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
                }
            }
            return subfolderIds.toArray();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
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
        try {
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
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.CALENDAR,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME)));
                }
            }
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.CONTACT,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME)));
                }
            }
            {
                final boolean tmp =
                    OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(
                        FolderObject.TASK,
                        user.getId(),
                        user.getGroups(),
                        userPerm,
                        ctx,
                        con);
                if (tmp) {
                    subfolderIds.add(toArray(String.valueOf(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID), sh.getString(FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME)));
                }
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
