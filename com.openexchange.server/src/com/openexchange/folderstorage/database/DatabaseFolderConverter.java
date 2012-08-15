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

package com.openexchange.folderstorage.database;

import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.database.getfolder.SystemInfostoreFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPrivateFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPublicFolder;
import com.openexchange.folderstorage.database.getfolder.SystemSharedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderProperties;

/**
 * {@link DatabaseFolderConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderConverter {

    private static interface FolderConverter {

        DatabaseFolder convert(FolderObject fo) throws OXException;
    }

    private static final TIntObjectMap<FolderConverter> SYSTEM_CONVERTERS;

    private static final TIntObjectMap<FolderConverter> CONVERTERS;

    static {
        TIntObjectMap<FolderConverter> m = new TIntObjectHashMap<FolderConverter>(4);
        m.put(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                return SystemPublicFolder.getSystemPublicFolder(fo);
            }
        });
        m.put(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                return SystemInfostoreFolder.getSystemInfostoreFolder(fo);
            }
        });
        m.put(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                return SystemPrivateFolder.getSystemPrivateFolder(fo);
            }
        });
        SYSTEM_CONVERTERS = m;

        m = new TIntObjectHashMap<FolderConverter>(4);
        m.put(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME);
                return retval;
            }
        });
        m.put(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME);
                return retval;
            }
        });
        m.put(FolderObject.SYSTEM_LDAP_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
                retval.setParentID(FolderStorage.PUBLIC_ID);
                return retval;
            }
        });
        m.put(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_GLOBAL_FOLDER_NAME);
                retval.setParentID(FolderStorage.PUBLIC_ID);
                return retval;
            }
        });
        CONVERTERS = m;
    }

    /**
     * Initializes a new {@link DatabaseFolderConverter}.
     */
    private DatabaseFolderConverter() {
        super();
    }

    private static int getContactCollectorFolder(final int userId, final int contextId, final Connection con) throws OXException {
        final Integer folderId = ServerUserSetting.getInstance(con).getContactCollectionFolder(contextId, userId);
        return null == folderId ? -1 : folderId.intValue();
    }

    private static int getPublishedMailAttachmentsFolder(final Session session) {
        if (null == session) {
            return -1;
        }
        final Integer i = (Integer) session.getParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID());
        return null == i ? -1 : i.intValue();
    }

    /**
     * The identifier for default/primary file storage account.
     */
    private static final String DEFAULT_ID = FileStorageAccount.DEFAULT_ID;

    /**
     * Look-up of default file storage account.
     * 
     * @param session The session
     * @return The default file storage account or <code>null</code>
     * @throws OXException If look-up attempt fails
     */
    public static FileStorageAccount getDefaultFileStorageAccess(final Session session) throws OXException {
        final FileStorageAccountManagerLookupService lookupService = ServerServiceRegistry.getInstance().getService(
            FileStorageAccountManagerLookupService.class);
        if (null == lookupService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
        }
        final FileStorageAccountManager defaultAccountManager = lookupService.getAccountManager(DEFAULT_ID, session);
        if (null != defaultAccountManager) {
            return defaultAccountManager.getAccount(DEFAULT_ID, session);
        }
        return null;
    }

    /**
     * Converts specified {@link FolderObject} instance to a {@link DatabaseFolder} instance.
     *
     * @param fo The {@link FolderObject} instance
     * @param user The user
     * @param userConfiguration The user configuration
     * @param ctx The context
     * @param session The user session
     * @param con The connection
     * @return The converted {@link DatabaseFolder} instance
     * @throws OXException If conversion fails
     */
    public static DatabaseFolder convert(final FolderObject fo, final User user, final UserConfiguration userConfiguration, final Context ctx, final Session session, final Connection con) throws OXException {
        try {
            final int folderId = fo.getObjectID();
            if (FolderObject.SYSTEM_SHARED_FOLDER_ID == folderId) {
                /*
                 * The system shared folder
                 */
                return SystemSharedFolder.getSystemSharedFolder(fo, user, userConfiguration, ctx, con);
            }
            /*
             * Look-up a system converter
             */
            FolderConverter folderConverter = SYSTEM_CONVERTERS.get(folderId);
            if (null != folderConverter) {
                /*
                 * Return immediately
                 */
                final DatabaseFolder databaseFolder = folderConverter.convert(fo);
                if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId && !InfostoreFacades.isInfoStoreAvailable()) {
                    final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                    if (null != defaultAccount) {
                        // Rename to default account name
                        databaseFolder.setName(defaultAccount.getDisplayName());
                    }
                }
                return databaseFolder;
            }
            final DatabaseFolder retval;
            /*
             * Look-up a converter
             */
            folderConverter = CONVERTERS.get(folderId);
            if (null != folderConverter) {
                retval = folderConverter.convert(fo);
            } else if (fo.isDefaultFolder()) {
                /*
                 * A default folder: set locale-sensitive name
                 */
                final int module = fo.getModule();
                if (module == FolderObject.TASK) {
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(FolderStrings.DEFAULT_TASK_FOLDER_NAME);
                } else if (module == FolderObject.CONTACT) {
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME);
                } else if (module == FolderObject.CALENDAR) {
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME);
                } else {
                    retval = new DatabaseFolder(fo);
                }
            } else if (folderId == getContactCollectorFolder(user.getId(), ctx.getContextId(), con)) {
                retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
            } else if (folderId == getPublishedMailAttachmentsFolder(session)) {
                retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME);
            } else {
                retval = new DatabaseFolder(fo);
            }
            final int userId = user.getId();
            final int[] groups = user.getGroups();
            final int[] modules = userConfiguration.getAccessibleModules();
            if (PrivateType.getInstance().equals(retval.getType()) && userId != retval.getCreatedBy()) {
                /*
                 * A shared folder
                 */
                retval.setType(SharedType.getInstance());
                retval.setGlobal(false); // user-sensitive!
                retval.setCacheable(OXFolderProperties.isEnableSharedFolderCaching()); // cacheable
                retval.setDefault(false);
                /*
                 * Determine user-visible subfolders
                 */
                final TIntList visibleSubfolders = OXFolderIteratorSQL.getVisibleSubfolders(folderId, userId, groups, modules, ctx, con);
                if (visibleSubfolders.isEmpty()) {
                    retval.setSubfolderIDs(new String[0]);
                    retval.setSubscribedSubfolders(false);
                } else {
                    final String[] tmp = new String[visibleSubfolders.size()];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = String.valueOf(visibleSubfolders.get(i));
                    }
                    retval.setSubfolderIDs(tmp);
                    retval.setSubscribedSubfolders(true);
                }
                /*
                 * Determine parent
                 */
                final int parent = fo.getParentFolderID();
                if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == parent || !OXFolderIteratorSQL.isVisibleFolder(parent, userId, groups, modules, ctx, con)) {
                    /*
                     * Either located below private folder or parent not visible
                     */
                    retval.setParentID(new StringBuilder(8).append(FolderObject.SHARED_PREFIX).append(retval.getCreatedBy()).toString());
                } else {
                    /*
                     * Parent is visible
                     */
                    retval.setParentID(String.valueOf(parent));
                }
            } else {
                /*
                 * Set subfolders for folder.
                 */
                if (FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == folderId) {
                    boolean lookupDb = true;
                    if (!InfostoreFacades.isInfoStoreAvailable()) {
                        final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                        if (null != defaultAccount) {
                            /*
                             * Enforce subfolders are retrieved from appropriate file storage
                             */
                            retval.setSubfolderIDs(null);
                            lookupDb = false;
                        }
                    }
                    if (lookupDb) {
                        /*
                         * User-sensitive loading of user infostore folder
                         */
                        final TIntList subfolders = OXFolderIteratorSQL.getVisibleSubfolders(folderId, userId, groups, modules, ctx, null);
                        if (subfolders.isEmpty()) {
                            retval.setSubfolderIDs(new String[0]);
                            retval.setSubscribedSubfolders(false);
                        } else {
                            final int len = subfolders.size();
                            final String[] arr = new String[len];
                            for (int i = 0; i < len; i++) {
                                arr[i] = String.valueOf(subfolders.get(i));
                            }
                            retval.setSubfolderIDs(arr);
                            retval.setSubscribedSubfolders(true);
                        }
                    }
                    /*
                     * Mark for user-sensitive cache
                     */
                    retval.setCacheable(true);
                    retval.setGlobal(false);
                } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
                    if (!InfostoreFacades.isInfoStoreAvailable()) {
                        final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                        if (null != defaultAccount) {
                            /*
                             * Enforce subfolders are retrieved from appropriate file storage
                             */
                            retval.setSubfolderIDs(null);
                            /*
                             * Mark for user-sensitive cache
                             */
                            retval.setCacheable(true);
                            retval.setGlobal(false);
                        }
                    }
                } else {
                    if (fo.containsSubfolderIds()) {
                        final List<Integer> subfolderIds = fo.getSubfolderIds();
                        if (subfolderIds.isEmpty()) {
                            retval.setSubfolderIDs(new String[0]);
                            retval.setSubscribedSubfolders(false);
                        } else {
                            final List<String> tmp = new ArrayList<String>(subfolderIds.size());
                            for (final Integer id : subfolderIds) {
                                tmp.add(id.toString());
                            }
                            retval.setSubfolderIDs(tmp.toArray(new String[tmp.size()]));
                            retval.setSubscribedSubfolders(true);
                        }
                    } else {
                        final TIntList subfolderIds = OXFolderLoader.getSubfolderInts(folderId, ctx, con);
                        if (subfolderIds.isEmpty()) {
                            retval.setSubfolderIDs(new String[0]);
                            retval.setSubscribedSubfolders(false);
                        } else {
                            final List<String> tmp = new ArrayList<String>(subfolderIds.size());
                            subfolderIds.forEach(new TIntProcedure() {

                                @Override
                                public boolean execute(final int id) {
                                    tmp.add(String.valueOf(id));
                                    return true;
                                }
                            });
                            retval.setSubfolderIDs(tmp.toArray(new String[tmp.size()]));
                            retval.setSubscribedSubfolders(true);
                        }
                    }
                }
            }
            return retval;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

}
