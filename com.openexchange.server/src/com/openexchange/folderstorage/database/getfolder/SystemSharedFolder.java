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
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Collators;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMap;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * {@link SystemSharedFolder} - Gets the system shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemSharedFolder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SystemSharedFolder.class);

    /**
     * Initializes a new {@link SystemSharedFolder}.
     */
    private SystemSharedFolder() {
        super();
    }

    /**
     * Gets the database folder representing system shared folder for given user.
     *
     * @param fo The folder object fetched from database
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system shared folder for given user
     * @throws OXException
     */
    public static DatabaseFolder getSystemSharedFolder(final FolderObject fo, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system shared folder
         */
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo, false);
        retval.setName(FolderStrings.SYSTEM_SHARED_FOLDER_NAME);
        // Enforce getSubfolders() from storage if at least one shared folder is accessible for user
        ConditionTreeMap treeMap;
        try {
            treeMap = ConditionTreeMapManagement.getInstance().optMapFor(ctx.getContextId());
        } catch (final OXException e) {
            treeMap = null;
        }
        if (null != treeMap) {
            /*
             * Set to null if a shared folder exists otherwise to empty array to indicate no subfolders
             */
            final boolean hasNext = treeMap.hasSharedFolder(user.getId(), user.getGroups(), userPerm.getAccessibleModules());
            retval.setSubfolderIDs(hasNext ? null : new String[0]);
            retval.setSubscribedSubfolders(hasNext);
            return retval;
        }
        /*
         * Query database
         */
        final SearchIterator<FolderObject> searchIterator;
        try {
            searchIterator =
                OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                    FolderObject.SYSTEM_SHARED_FOLDER_ID,
                    user.getId(),
                    user.getGroups(),
                    ctx,
                    userPerm,
                    null,
                    con);
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
        try {
            /*
             * Set to null if a shared folder exists otherwise to empty array to indicate no subfolders
             */
            final boolean hasNext = searchIterator.hasNext();
            retval.setSubfolderIDs(hasNext ? null : new String[0]);
            retval.setSubscribedSubfolders(hasNext);
        } finally {
            SearchIterators.close(searchIterator);
        }
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system shared folder for given user.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The subfolder identifiers of database folder representing system shared folder for given user
     * @throws OXException If the database folder cannot be returned
     */
    public static List<String[]> getSystemSharedFolderSubfolder(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        /*
         * The system shared folder
         */
        final Map<String, Integer> displayNames;
        {
            final Queue<FolderObject> q;
            try {
                q =
                    ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                        FolderObject.SYSTEM_SHARED_FOLDER_ID,
                        user.getId(),
                        user.getGroups(),
                        ctx,
                        userPerm,
                        null,
                        con)).asQueue();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            }
            /*
             * Gather all display names
             */
            final StringHelper strHelper = StringHelper.valueOf(user.getLocale());
            final int size = q.size();
            displayNames = new HashMap<String, Integer>(size);
            final UserStorage us = UserStorage.getInstance();
            final Iterator<FolderObject> iter = q.iterator();
            for (int i = 0; i < size; i++) {
                final FolderObject sharedFolder = iter.next();
                String creatorDisplayName;
                try {
                    creatorDisplayName = us.getUser(sharedFolder.getCreatedBy(), ctx).getDisplayName();
                } catch (final OXException e) {
                    if (sharedFolder.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                        throw e;
                    }
                    creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                }
                if (displayNames.containsKey(creatorDisplayName)) {
                    continue;
                }
                displayNames.put(creatorDisplayName, Integer.valueOf(sharedFolder.getCreatedBy()));
            }
        }
        /*
         * Sort display names and write corresponding virtual owner folder
         */
        final List<String> sortedDisplayNames = new ArrayList<String>(displayNames.keySet());
        Collections.sort(sortedDisplayNames, new DisplayNameComparator(user.getLocale()));
        final StringBuilder sb = new StringBuilder(8);
        final List<String[]> subfolderIds = new ArrayList<String[]>(displayNames.size());
        for (final String displayName : sortedDisplayNames) {
            sb.setLength(0);
            final int createdBy = displayNames.get(displayName).intValue();
            subfolderIds.add(new String[] {sb.append(FolderObject.SHARED_PREFIX).append(createdBy).toString(), displayName});
        }
        return subfolderIds;
    }

    /**
     * {@link DisplayNameComparator} - Sorts display names with respect to a certain locale.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class DisplayNameComparator implements Comparator<String> {

        private final Collator collator;

        public DisplayNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final String displayName1, final String displayName2) {
            return collator.compare(displayName1, displayName2);
        }

    }

}
