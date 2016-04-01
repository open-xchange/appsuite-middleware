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

import static com.openexchange.groupware.container.FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
import static com.openexchange.groupware.container.FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID;
import static com.openexchange.groupware.container.FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
import static com.openexchange.groupware.container.FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID;
import static com.openexchange.groupware.i18n.FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME;
import static com.openexchange.groupware.i18n.FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME;
import static com.openexchange.groupware.i18n.FolderStrings.SYSTEM_TRASH_INFOSTORE_FOLDER_NAME;
import static com.openexchange.groupware.i18n.FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME;
import static com.openexchange.groupware.i18n.FolderStrings.VIRTUAL_LIST_FILES_FOLDER_NAME;
import static com.openexchange.groupware.i18n.FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME;
import gnu.trove.list.TIntList;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.AltNameLocalizedDatabaseFolder;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link SystemInfostoreFolder} - Gets the system infostore folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemInfostoreFolder {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SystemInfostoreFolder.class);

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
     * @return The database folder representing system infostore folder
     */
    public static DatabaseFolder getSystemInfostoreFolder(final FolderObject fo, final boolean altNames) {
        /*
         * The system infostore folder
         */
        final DatabaseFolder retval = new AltNameLocalizedDatabaseFolder(fo, FolderStrings.SYSTEM_FILES_FOLDER_NAME);
        retval.setName(altNames ? FolderStrings.SYSTEM_FILES_FOLDER_NAME : FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME);
        retval.setContentType(InfostoreContentType.getInstance());
        // Enforce getSubfolders() on storage
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        // Don't cache if altNames enabled -- "Shared files" is supposed NOT to be displayed if no shared files exist
        if (altNames) {
            retval.setCacheable(false);
        }
        return retval;
    }

    /**
     * Gets a list of folder identifier and -name tuples of database folders below the system infostore folder.
     *
     * @param user The user to get the subfolders for
     * @param permissionBits The user's permission bits
     * @param context The context
     * @param altNames <code>true</code> to prefer alternative names for infostore folders, <code>false</code>, otherwise
     * @param connection The database connection to use
     * @return A list of folder identifier and -name tuples of the subfolders below the system infostore folder
     */
    public static List<String[]> getSystemInfostoreFolderSubfolders(User user, UserPermissionBits permissionBits, Context context, boolean altNames, Session session, Connection connection) throws OXException {
        StringHelper stringHelper = StringHelper.valueOf(user.getLocale());
        List<String[]> subfolderIDs = new ArrayList<String[]>();
        /*
         * add subfolders from database
         */
        SearchIterator<FolderObject> searchIterator = null;
        try {
            searchIterator = OXFolderIteratorSQL.getVisibleSubfoldersIterator(SYSTEM_INFOSTORE_FOLDER_ID, user.getId(), user.getGroups(), context, permissionBits, null, connection);
            while (searchIterator.hasNext()) {
                FolderObject folder = searchIterator.next();
                if (SYSTEM_USER_INFOSTORE_FOLDER_ID == folder.getObjectID()) {
                    if (showPersonalBelowInfoStore(session, altNames)) {
                        /*
                         * personal folder is shown elsewhere - include "shared files" only if shared contents are actually available
                         */
                        TIntList visibleSubfolderIDs = OXFolderIteratorSQL.getVisibleSubfolders(
                            folder.getObjectID(), user.getId(), user.getGroups(), permissionBits.getAccessibleModules(), context, connection);
                        if (1 < visibleSubfolderIDs.size() ||
                            1 == visibleSubfolderIDs.size() && false == visibleSubfolderIDs.remove(getDefaultInfoStoreFolderId(session, context, connection)) ||
                            0 < new OXFolderAccess(connection, context).getItemCount(folder, session, context)) {
                            subfolderIDs.add(0, new String[] { String.valueOf(folder.getObjectID()), stringHelper.getString(SYSTEM_USER_FILES_FOLDER_NAME) });
                        }
                    } else {
                        String name = stringHelper.getString(altNames ? SYSTEM_USER_FILES_FOLDER_NAME : FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME);
                        subfolderIDs.add(0, new String[] { String.valueOf(folder.getObjectID()), name });
                    }
                } else if (SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folder.getObjectID()) {
                    /*
                     * only include public infostore root if there are visible subfolders, or user has full public folder access and is able to create subfolders
                     */
                    if (permissionBits.hasFullPublicFolderAccess() && folder.getEffectiveUserPermission(user.getId(), permissionBits, connection).canCreateSubfolders() ||
                        0 < OXFolderIteratorSQL.getVisibleSubfolders(folder.getObjectID(), user.getId(), user.getGroups(), permissionBits.getAccessibleModules(), context, connection).size()) {
                        String name = stringHelper.getString(altNames ? SYSTEM_PUBLIC_FILES_FOLDER_NAME : FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME);
                        subfolderIDs.add(new String[] { String.valueOf(folder.getObjectID()), name });
                    }
                } else if (FolderObject.TRASH == folder.getType() && folder.isDefaultFolder()) {
                    String name = stringHelper.getString(altNames ? SYSTEM_TRASH_FILES_FOLDER_NAME : SYSTEM_TRASH_INFOSTORE_FOLDER_NAME);
                    subfolderIDs.add(new String[] { String.valueOf(folder.getObjectID()), name });
                } else {
                    subfolderIDs.add(new String[] { String.valueOf(folder.getObjectID()), folder.getFolderName() });
                }
            }
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            SearchIterators.close(searchIterator);
        }
        /*
         * also include the virtual root if there are any non-tree-visible folders for the user
         */
        if (OXFolderIteratorSQL.hasVisibleFoldersNotSeenInTreeView(FolderObject.INFOSTORE, user.getId(), user.getGroups(), permissionBits, context, connection)) {
            String name = stringHelper.getString(altNames ? VIRTUAL_LIST_FILES_FOLDER_NAME : VIRTUAL_LIST_INFOSTORE_FOLDER_NAME);
            subfolderIDs.add(new String[] { String.valueOf(VIRTUAL_LIST_INFOSTORE_FOLDER_ID), name });
        }
        return subfolderIDs;
    }

    private static boolean showPersonalBelowInfoStore(final Session session, final boolean altNames) {
        if (!altNames) {
            return false;
        }
        final String paramName = "com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore";
        final Boolean tmp = (Boolean) session.getParameter(paramName);
        if (null != tmp) {
            return tmp.booleanValue();
        }
        final ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            return false;
        }
        try {
            final ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            final Boolean b = view.opt(paramName, boolean.class, Boolean.FALSE);
            if (session instanceof PutIfAbsent) {
                ((PutIfAbsent) session).setParameterIfAbsent(paramName, b);
            } else {
                session.setParameter(paramName, b);
            }
            return b.booleanValue();
        } catch (final OXException e) {
            LOGGER.debug("", e);
            return false;
        }
    }

    /**
     * Gets the identifier of the user's default infostore folder.
     *
     * @param session The session
     * @param ctx The context
     * @param connection The database connection to use
     * @return The identifier of the user's default infostore folder, or <code>-1</code> if there is none
     */
    private static int getDefaultInfoStoreFolderId(Session session, Context ctx, Connection connection) {
        String parameterName = "com.openexchange.folderstorage.defaultInfoStoreFolderId";
        String parameterValue = (String) session.getParameter(parameterName);
        if (null != parameterValue) {
            try {
                return Integer.parseInt(parameterValue);
            } catch (NumberFormatException e) {
                LOGGER.warn("Error parsing folder ID from session parameter", e);
            }
        }
        int folderID = -1;
        try {
            folderID = new OXFolderAccess(connection, ctx).getDefaultFolderID(session.getUserId(), FolderObject.INFOSTORE);
        } catch (OXException e) {
            if (false == OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.equals(e)) {
                LOGGER.warn("Error gettting default infostore folder ID", e);
                return folderID;
            }
        }
        if (PutIfAbsent.class.isInstance(session)) {
            ((PutIfAbsent) session).setParameterIfAbsent(parameterName, String.valueOf(folderID));
        } else {
            session.setParameter(parameterName, String.valueOf(folderID));
        }
        return folderID;
    }

}
