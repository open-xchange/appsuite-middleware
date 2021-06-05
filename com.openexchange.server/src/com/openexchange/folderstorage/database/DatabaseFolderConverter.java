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

package com.openexchange.folderstorage.database;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.database.getfolder.SystemInfostoreFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPrivateFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPublicFolder;
import com.openexchange.folderstorage.database.getfolder.SystemSharedFolder;
import com.openexchange.folderstorage.database.getfolder.VirtualListFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderProperties;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link DatabaseFolderConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderConverter {

    private static interface FolderConverter {

        DatabaseFolder convert(FolderObject fo, boolean altNames) throws OXException;
    }

    private static final TIntObjectMap<FolderConverter> SYSTEM_CONVERTERS;

    private static final TIntObjectMap<FolderConverter> CONVERTERS;

    static {
        TIntObjectMap<FolderConverter> m = new TIntObjectHashMap<FolderConverter>(4);
        m.put(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                return SystemPublicFolder.getSystemPublicFolder(fo);
            }
        });
        m.put(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                return SystemInfostoreFolder.getSystemInfostoreFolder(fo, altNames);
            }
        });
        m.put(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                return SystemPrivateFolder.getSystemPrivateFolder(fo);
            }
        });
        SYSTEM_CONVERTERS = m;

        m = new TIntObjectHashMap<FolderConverter>(4);
        m.put(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                final DatabaseFolder retval = new AltNameLocalizedDatabaseFolder(fo, FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME);
                retval.setName(altNames ? FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME : FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME);
                return retval;
            }
        });
        m.put(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                final DatabaseFolder retval = new AltNameLocalizedDatabaseFolder(fo, FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME);
                retval.setName(altNames ? FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME : FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME);
                return retval;
            }
        });
        m.put(FolderObject.SYSTEM_LDAP_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
                retval.setParentID(FolderStorage.PUBLIC_ID);
                return retval;
            }
        });
        /*-
         *  // finally dropped
        m.put(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, new FolderConverter() {

            @Override
            public DatabaseFolder convert(final FolderObject fo, final boolean altNames) throws OXException {
                final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
                retval.setName(FolderStrings.SYSTEM_GLOBAL_FOLDER_NAME);
                retval.setParentID(FolderStorage.PUBLIC_ID);
                return retval;
            }
        });
        */
        CONVERTERS = m;
    }

    /**
     * Initializes a new {@link DatabaseFolderConverter}.
     */
    private DatabaseFolderConverter() {
        super();
    }

    private static int getContactCollectorFolder(final Session session, final Connection con) throws OXException {
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final String key = "__ccf#";
        Integer ret = (Integer) session.getParameter(key);
        if (null == ret) {
            Integer folderId = Integer.valueOf(getContactCollectorFolder(userId, contextId, con));
            session.setParameter(key, folderId);
            ret = folderId;
        }
        return ret.intValue();
    }

    private static int getContactCollectorFolder(final int userId, final int contextId, final Connection con) throws OXException {
        Integer folderId = ServerUserSetting.getInstance(con).getContactCollectionFolder(contextId, userId);
        return null == folderId ? Integer.valueOf(-1) : folderId;
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

    private static volatile Boolean preferDisplayName;
    private static boolean preferDisplayName() {
        Boolean tmp = preferDisplayName;
        if (null == tmp) {
            synchronized (DatabaseFolderConverter.class) {
                tmp = preferDisplayName;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = null == service ? Boolean.FALSE : Boolean.valueOf(service.getBoolProperty("com.openexchange.folderstorage.database.preferDisplayName", false));
                    preferDisplayName = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * Converts specified {@link FolderObject} instance to a {@link DatabaseFolder} instance.
     *
     * @param fo The {@link FolderObject} instance
     * @param user The user
     * @param userPermissionBits The user permission bits
     * @param ctx The context
     * @param session The user session
     * @param altNames <code>true</code> to use alternative names for former InfoStore folders; otherwise <code>false</code>
     * @param con The connection
     * @return The converted {@link DatabaseFolder} instance
     * @throws OXException If conversion fails
     */
    public static DatabaseFolder convert(final FolderObject fo, final User user, final UserPermissionBits userPermissionBits, final Context ctx, final Session session, final boolean altNames, final Connection con) throws OXException {
        try {
            final int folderId = fo.getObjectID();
            if (folderId < FolderObject.MIN_FOLDER_ID) { // Possibly a system folder
                if (FolderObject.SYSTEM_SHARED_FOLDER_ID == folderId) {
                    /*
                     * The system shared folder
                     */
                    return SystemSharedFolder.getSystemSharedFolder(fo, user, userPermissionBits, ctx, con);
                }
                /*
                 * Look-up a system converter
                 */
                FolderConverter folderConverter = SYSTEM_CONVERTERS.get(folderId);
                if (null != folderConverter) {
                    /*
                     * Return immediately
                     */
                    final DatabaseFolder databaseFolder = folderConverter.convert(fo, altNames);
                    if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId && !InfostoreFacades.isInfoStoreAvailable()) {
                        if (session == null) {
                            throw FolderExceptionErrorMessage.MISSING_SESSION.create();
                        }
                        final FileStorageAccount defaultAccount = getDefaultFileStorageAccess(session);
                        if (null != defaultAccount) {
                            // Rename to default account name
                            databaseFolder.setName(defaultAccount.getDisplayName());
                        }
                    }
                    return databaseFolder;
                }
                /*
                 * Look-up a converter
                 */
                folderConverter = CONVERTERS.get(folderId);
                if (null != folderConverter) {
                    final DatabaseFolder databaseFolder = folderConverter.convert(fo, altNames);
                    return handleDatabaseFolder(databaseFolder, folderId, fo, session, user, userPermissionBits, ctx, con);
                }
            }
            /*
             * Converter database folder
             */
            final DatabaseFolder retval;
            if (fo.isDefaultFolder()) {
                /*
                 * A default folder: set locale-sensitive name
                 */
                String localizableName = null;
                switch (fo.getModule()) {
                    case FolderObject.TASK:
                        localizableName = FolderStrings.DEFAULT_TASK_FOLDER_NAME;
                        break;
                    case FolderObject.CONTACT:
                        localizableName = FolderStrings.DEFAULT_CONTACT_FOLDER_NAME;
                        break;
                    case FolderObject.CALENDAR:
                        localizableName = FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME;
                        break;
                    case FolderObject.INFOSTORE:
                        switch (fo.getType()) {
                            case FolderObject.PUBLIC:
                                if (preferDisplayName()) {
                                    if (fo.getCreatedBy() == user.getId()) {
                                        localizableName = user.getDisplayName();
                                    } else {
                                        try {
                                            localizableName = UserStorage.getInstance().getUser(fo.getCreatedBy(), ctx).getDisplayName();
                                        } catch (Exception e) {
                                            org.slf4j.LoggerFactory.getLogger(DatabaseFolderConverter.class).error(
                                                "error getting owner for folder {}", I(fo.getObjectID()), e);
                                        }
                                    }
                                } else if (altNames && fo.getCreatedBy() == user.getId()) {
                                    localizableName = FolderStrings.DEFAULT_FILES_FOLDER_NAME;
                                }
                                break;
                            case FolderObject.TRASH:
                                localizableName = FolderStrings.SYSTEM_TRASH_INFOSTORE_FOLDER_NAME;
                                break;
                            case FolderObject.DOCUMENTS:
                                localizableName = FolderStrings.SYSTEM_USER_DOCUMENTS_FOLDER_NAME;
                                break;
                            case FolderObject.PICTURES:
                                localizableName = FolderStrings.SYSTEM_USER_PICTURES_FOLDER_NAME;
                                break;
                            case FolderObject.VIDEOS:
                                localizableName = FolderStrings.SYSTEM_USER_VIDEOS_FOLDER_NAME;
                                break;
                            case FolderObject.MUSIC:
                                localizableName = FolderStrings.SYSTEM_USER_MUSIC_FOLDER_NAME;
                                break;
                            case FolderObject.TEMPLATES:
                                localizableName = FolderStrings.SYSTEM_USER_TEMPLATES_FOLDER_NAME;
                                break;
                        }
                        break;
                }
                if (null != localizableName) {
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(localizableName);
                } else {
                    retval = new DatabaseFolder(fo);
                }
            } else {
                if (isContactCollectorFolder(ctx.getContextId(), fo, session, con)) {
                    // "Collected addresses" folder
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
                    retval.setDefault(true);
                    retval.setCacheable(true);
                    retval.setGlobal(false);
                } else if (folderId == getPublishedMailAttachmentsFolder(session)) {
                    retval = new LocalizedDatabaseFolder(fo);
                    retval.setName(FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME);
                } else {
                    retval = new DatabaseFolder(fo);
                    /*-
                     * If enabled performance need to be improved for:
                     *
                     * VirtualListFolder.getVirtualListFolderSubfolders(int, User, UserConfiguration, Context, Connection)
                     */
                    final boolean checkIfVirtuallyReachable = false;
                    if (checkIfVirtuallyReachable) {
                        /*-
                         * Does it appear below virtual folder?:
                         *
                         * FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
                         * FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID
                         */
                        final int virtualParent = getPossibleVirtualParent(fo);
                        if (virtualParent > 0) {
                            final String sFolderId = Integer.toString(folderId);
                            for (final String[] arr : VirtualListFolder.getVirtualListFolderSubfolders(virtualParent, user, userPermissionBits, ctx, con)) {
                                if (sFolderId.equals(arr[0])) {
                                    retval.setParentID(Integer.toString(virtualParent));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Handle database folder
            return handleDatabaseFolder(retval, folderId, fo, session, user, userPermissionBits, ctx, con);
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets a value indicating whether the supplied folder represents the special "contact collector" folder (of the folder's creator).
     *
     * @param contextId The context identifier
     * @param folder The folder to check
     * @param optSession The current session, or <code>null</code> if not available
     * @param optConnection An optional connection to the database, or <code>null</code> if not available
     * @return <code>true</code> if the folder represents the special "contact collector" folder, <code>false</code>, otherwise
     */
    private static boolean isContactCollectorFolder(int contextId, FolderObject folder, Session optSession, Connection optConnection) throws OXException {
        if (null == folder || FolderObject.CONTACT != folder.getModule() || FolderObject.PUBLIC == folder.getType()) {
            return false;
        }
        /*
         * check via flag in folder metadata
         */
        if (null != folder.getMeta() && Boolean.TRUE.equals(folder.getMeta().get("__ccf#"))) {
            return true;
        }
        /*
         * check via cached identifier in session if session user is owner
         */
        if (null != optSession && optSession.getUserId() == folder.getCreatedBy()) {
            return folder.getObjectID() == getContactCollectorFolder(optSession, optConnection);
        }
        /*
         * check via server user setting of owner
         */
        return folder.getObjectID() == getContactCollectorFolder(folder.getCreatedBy(), contextId, optConnection);
    }

    private static DatabaseFolder handleDatabaseFolder(final DatabaseFolder databaseFolder, final int folderId, final FolderObject fo, final Session session, final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException, SQLException {
        final int userId = user.getId();
        if (FolderObject.PRIVATE == fo.getType() && userId != databaseFolder.getCreatedBy()) { // Shared
            /*
             * A shared folder
             */
            databaseFolder.setType(SharedType.getInstance());
            databaseFolder.setGlobal(false); // user-sensitive!
            databaseFolder.setCacheable(OXFolderProperties.isEnableSharedFolderCaching()); // cacheable
            databaseFolder.setDefault(false);
            /*
             * Determine user-visible subfolders
             */
            final TIntList visibleSubfolders = OXFolderIteratorSQL.getVisibleSubfolders(folderId, userId, user.getGroups(), userPerm.getAccessibleModules(), ctx, con);
            if (visibleSubfolders.isEmpty()) {
                databaseFolder.setSubfolderIDs(new String[0]);
                databaseFolder.setSubscribedSubfolders(false);
            } else {
                final String[] tmp = new String[visibleSubfolders.size()];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = Integer.toString(visibleSubfolders.get(i), 10);
                }
                databaseFolder.setSubfolderIDs(tmp);
                databaseFolder.setSubscribedSubfolders(true);
            }
            /*
             * Determine parent
             */
            final int parent = fo.getParentFolderID();
            if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == parent || !OXFolderIteratorSQL.isVisibleFolder(parent, userId, user.getGroups(), userPerm.getAccessibleModules(), ctx, con)) {
                /*
                 * Either located below private folder or parent not visible
                 */
                databaseFolder.setParentID(new StringBuilder(8).append(FolderObject.SHARED_PREFIX).append(databaseFolder.getCreatedBy()).toString());
            } else {
                /*
                 * Parent is visible
                 */
                databaseFolder.setParentID(Integer.toString(parent, 10));
            }
        } else if (FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == folderId) {
            /*
             * Enforce subfolders are retrieved from appropriate file storage & mark for user-sensitive cache
             */
            databaseFolder.setSubfolderIDs(null);
            databaseFolder.setSubscribedSubfolders(true);
            databaseFolder.setCacheable(true);
            databaseFolder.setGlobal(false);
        } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
            /*
             * Enforce subfolders are retrieved from appropriate file storage
             */
            databaseFolder.setSubfolderIDs(null);
            databaseFolder.setSubscribedSubfolders(true);
            /*
             * Mark for user-sensitive cache if alternative default account present
             */
            if (!InfostoreFacades.isInfoStoreAvailable()) {
                if (session == null) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create();
                }
                if (null != getDefaultFileStorageAccess(session)) {
                    databaseFolder.setCacheable(true);
                    databaseFolder.setGlobal(false);
                }
            }
        } else {
            /*
             * Set subfolders for folder.
             */
            List<Integer> subfolderIds = fo.containsSubfolderIds() ? fo.getSubfolderIds() : OXFolderLoader.getSubfolderIds(folderId, ctx, con);
            if (subfolderIds.isEmpty()) {
                databaseFolder.setSubfolderIDs(new String[0]);
                databaseFolder.setSubscribedSubfolders(false);
            } else {
                String[] ids = new String[subfolderIds.size()];
                for (int i = 0; i < subfolderIds.size(); i++) {
                    ids[i] = Integer.toString(subfolderIds.get(i), 10);
                }
                databaseFolder.setSubfolderIDs(ids);
                databaseFolder.setSubscribedSubfolders(true);
            }
            /*
             * Mark default infostore folders of users for user-sensitive cache (possible altNames)
             */
            if (FolderObject.INFOSTORE == fo.getModule() && FolderObject.PUBLIC == fo.getType() && fo.isDefaultFolder()) {
                databaseFolder.setCacheable(true);
                databaseFolder.setGlobal(false);
            }
        }
        /*
         * assume all supported capabilities for database folders
         */
        databaseFolder.addSupportedCapabilities(FileStorageFolder.ALL_CAPABILITIES);
        return databaseFolder;
    }

    private static int getPossibleVirtualParent(final FolderObject fo) {
        switch (fo.getModule()) {
        case FolderObject.TASK:
            return FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID;
        case FolderObject.CONTACT:
            return FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID;
        case FolderObject.CALENDAR:
            return FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID;
        case FolderObject.INFOSTORE:
            return FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID;
        default:
            return 0;
        }
    }

}
