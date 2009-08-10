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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.Queue;
import com.openexchange.api2.OXException;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link VirtualListFolder} - Gets a virtual list folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualListFolder {

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
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param con The connection
     * @return <code>true</code> if specified virtual folder identifier exists; otherwise <code>false</code>
     * @throws FolderException If checking existence fails
     */
    public static boolean existsVirtualListFolder(final int folderId, final User user, final UserConfiguration userConfiguration, final Context ctx, final Connection con) throws FolderException {
        try {
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
            return !(((FolderObjectIterator) OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(
                module,
                user.getId(),
                user.getGroups(),
                userConfiguration,
                ctx,
                con)).asQueue().isEmpty());
        } catch (final SearchIteratorException e) {
            throw new FolderException(e);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
    }

    /**
     * Gets the database folder representing given virtual folder.
     * 
     * @param folderId The virtual folder identifier
     * @return The database folder representing given virtual folder
     */
    public static DatabaseFolder getVirtualListFolder(final int folderId) {
        /*
         * A virtual database folder
         */
        final FolderObject fo;
        if (FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID == folderId) {
            // Task
            fo = FolderObject.createVirtualFolderObject(
                folderId,
                FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME,
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else if (FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID == folderId) {
            // Calendar
            fo = FolderObject.createVirtualFolderObject(
                folderId,
                FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME,
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else if (FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID == folderId) {
            // Contact
            fo = FolderObject.createVirtualFolderObject(
                folderId,
                FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME,
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        } else {
            // Infostore
            fo = FolderObject.createVirtualFolderObject(
                folderId,
                FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME,
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
        }
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
        retval.setSubfolderIDs(null);
        retval.setGlobal(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing given virtual folder.
     * 
     * @param folderId The virtual folder identifier
     * @param user The user
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param con The connection to use
     * @return The subfolder identifiers of database folder representing given virtual folder
     * @throws FolderException If returning database folder fails
     */
    public static String[] getVirtualListFolderSubfolders(final int folderId, final User user, final UserConfiguration userConfiguration, final Context ctx, final Connection con) throws FolderException {
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
        final Queue<FolderObject> q;
        try {
            q = ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(
                module,
                user.getId(),
                user.getGroups(),
                userConfiguration,
                ctx,
                con)).asQueue();
        } catch (final SearchIteratorException e) {
            throw new FolderException(e);
        } catch (final OXException e) {
            throw new FolderException(e);
        }
        final String[] subfolderIds = new String[q.size()];
        int i = 0;
        for (final FolderObject folderObject : q) {
            subfolderIds[i++] = String.valueOf(folderObject.getObjectID());
        }
        return subfolderIds;
    }

}
