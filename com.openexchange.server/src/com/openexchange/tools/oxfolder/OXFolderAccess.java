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

package com.openexchange.tools.oxfolder;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.contact.ContactService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link OXFolderAccess} - Provides access to database folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderAccess {

    /**
     * A connection with "read-only" capability.
     */
    private final Connection readCon;

    /**
     * The associated context
     */
    private final Context ctx;

    /**
     * Initializes a new {@link OXFolderAccess}.
     * <p>
     * Since the access is created with a connection with "read-only" capability, an appropriate connection is going to be fetched from DB
     * pool every time when needed.
     *
     * @param ctx The context
     */
    public OXFolderAccess(final Context ctx) {
        this(null, ctx);
    }

    /**
     * Initializes a new {@link OXFolderAccess}.
     *
     * @param readCon A connection with "read-only" capability or <code>null</code> to let the access fetch an appropriate connection from
     *            DB pool every time when needed
     * @param ctx The context
     */
    public OXFolderAccess(final Connection readCon, final Context ctx) {
        super();
        this.readCon = readCon;
        this.ctx = ctx;
    }

    /**
     * Tests if the folder associated with specified folder ID exists.
     *
     * @param folderId The folder ID
     * @return <code>true</code> if the folder associated with specified folder ID exists; otherwise <code>false</code>
     * @throws OXException If an error occurs while checking existence
     */
    public boolean exists(final int folderId) throws OXException {
        try {
            getFolderObject(folderId);
            return true;
        } catch (OXException e) {
            if (OXFolderExceptionCode.isNotFound(e)) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Loads matching <code>com.openexchange.groupware.container.FolderObject</code> instance either from cache (if enabled) or from
     * underlying storage.
     *
     * @param folderId The folder ID
     * @return The matching <code>com.openexchange.groupware.container.FolderObject</code> instance
     * @throws OXException If operation fails
     */
    public final FolderObject getFolderObject(final int folderId) throws OXException {
        return getFolderObject(folderId, true);
    }

    /**
     * Loads matching <code>com.openexchange.groupware.container.FolderObject</code> instance either from cache (if enabled) or from
     * underlying storage.
     *
     * @param folderId The folder ID
     * @param fromCache - <code>true</code> to look-up cache; otherwise <code>false</code>
     * @return The matching <code>com.openexchange.groupware.container.FolderObject</code> instance
     * @throws OXException If operation fails
     */
    public final FolderObject getFolderObject(final int folderId, final boolean fromCache) throws OXException {
        FolderObject virtualFolder = optVirtualFolder(folderId);
        if (null != virtualFolder) {
            return virtualFolder;
        }
        final FolderObject fo;
        if (fromCache && FolderCacheManager.isEnabled()) {
            fo = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
        } else {
            fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
        }
        return fo;
    }

    /**
     * Creates a <code>java.util.List</code> of <code>FolderObject</code> instances which match given folder IDs.
     *
     * @param folderIDs - the folder IDs as an <code>int</code> array
     * @return A <code>java.util.List</code> of <code>FolderObject</code> instances
     * @throws OXException If operation fails
     */
    public final List<FolderObject> getFolderObjects(final int[] folderIDs) throws OXException {
        final List<FolderObject> retval = new ArrayList<FolderObject>(folderIDs.length);
        for (final int fuid : folderIDs) {
            try {
                retval.add(getFolderObject(fuid));
            } catch (OXException e) {
                if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                    continue;
                }
                throw e;
            }
        }
        return retval;
    }

    /**
     * Creates a <code>java.util.List</code> of <code>FolderObject</code> instances fills which match given folder IDs.
     *
     * @param folderIDs - the folder IDs backed by a <code>java.util.Collection</code>
     * @return A <code>java.util.List</code> of <code>FolderObject</code> instances
     * @throws OXException If operation fails
     */
    public final List<FolderObject> getFolderObjects(final Collection<Integer> folderIDs) throws OXException {
        final int size = folderIDs.size();
        final List<FolderObject> retval = new ArrayList<FolderObject>(size);
        final Iterator<Integer> iter = folderIDs.iterator();
        for (int i = 0; i < size; i++) {
            try {
                retval.add(getFolderObject(iter.next().intValue()));
            } catch (OXException e) {
                if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                    continue;
                }
                throw e;
            }
        }
        return retval;
    }

    /**
     * Determines folder type. The returned value is either <code>FolderObject.PRIVATE</code>, <code>FolderObject.PUBLIC</code> or
     * <code>FolderObject.SHARED</code>. <b>NOTE:</b> This method assumes that given user has read access!
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @return The folder type
     * @throws OXException If operation fails
     */
    public final int getFolderType(final int folderId, final int userId) throws OXException {
        return getFolderObject(folderId).getType(userId);
    }

    /**
     * Determines the <b>plain</b> folder type meaning the returned value is either <code>FolderObject.PRIVATE</code> or
     * <code>FolderObject.PUBLIC</code>. <b>NOTE:</b> Do not use this method to check if folder is shared (<code>FolderObject.SHARED</code>
     * ), use {@link #getFolderType(int, int)} instead.
     *
     * @param folderId The folder ID
     * @return The folder type
     * @throws OXException
     * @see <code>getFolderType(int, int)</code>
     */
    public final int getFolderType(final int folderId) throws OXException {
        return getFolderObject(folderId).getType();
    }

    /**
     * Determines folder module.
     *
     * @param folderId The folder ID
     * @return folder module
     * @throws OXException If operation fails
     */
    public final int getFolderModule(final int folderId) throws OXException {
        return getFolderObject(folderId).getModule();
    }

    /**
     * Determines folder owner.
     *
     * @param folderId The folder ID
     * @return The folder owner
     * @throws OXException If operation fails
     */
    public final int getFolderOwner(final int folderId) throws OXException {
        return getFolderObject(folderId).getCreatedBy();
    }

    /**
     * Determines if denoted folder has sub-folders.
     *
     * @param folderId The folder ID
     * @return <code>true</code> if folder has sub-folders; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean hasSubfolders(final int folderId) throws OXException {
        return getFolderObject(folderId).hasSubfolders();
    }

    /**
     * Determines if folder is shared. <b>NOTE:</b> This method assumes that given user has read access!
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @return <code>true</code> if folder is shared, otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isFolderShared(final int folderId, final int userId) throws OXException {
        return (getFolderType(folderId, userId) == FolderObject.SHARED);
    }

    /**
     * Determines if folder is an user's default folder.
     *
     * @param folderId The folder ID
     * @return <code>true</code> if folder is marked as a default folder, otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isDefaultFolder(final int folderId) throws OXException {
        return getFolderObject(folderId).isDefaultFolder();
    }

    /**
     * Determines given folder's name.
     *
     * @param folderId The folder ID
     * @return The folder name
     * @throws OXException If operation fails
     */
    public String getFolderName(final int folderId) throws OXException {
        return getFolderObject(folderId).getFolderName();
    }

    /**
     * Determines given folder's parent ID.
     *
     * @param folderId The folder ID
     * @return The folder parent ID
     * @throws OXException If operation fails
     */
    public int getParentFolderID(final int folderId) throws OXException {
        return getFolderObject(folderId).getParentFolderID();
    }

    /**
     * Determines given folder's last modifies date.
     *
     * @param folderId The folder ID
     * @return The folder's last modifies date
     * @throws OXException If operation fails
     */
    public Date getFolderLastModified(final int folderId) throws OXException {
        return getFolderObject(folderId).getLastModified();
    }

    /**
     * Determines user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param userConfig The user configuration
     * @return The user's effective permission
     * @throws OXException If operation fails
     */
    public final EffectivePermission getFolderPermission(final int folderId, final int userId, final UserConfiguration userConfig) throws OXException {
        try {
            final FolderObject fo = getFolderObject(folderId);
            return fo.getEffectiveUserPermission(userId, userConfig, readCon);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Determines user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param userPermissionBits The user permission bits
     * @return The user's effective permission
     * @throws OXException If operation fails
     */
    public final EffectivePermission getFolderPermission(final int folderId, final int userId, final UserPermissionBits userPermissionBits) throws OXException {
        try {
            final FolderObject fo = getFolderObject(folderId);
            return fo.getEffectiveUserPermission(userId, userPermissionBits, readCon);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Determines if folder is visible (without respect to user configuration).
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param permissions The permission bits
     * @return <code>true</code> if folder is visible; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isVisibleFor(final int folderId, final int userId, final UserPermissionBits permissions) throws OXException {
        return isVisibleFor(folderId, userId, UserStorage.getInstance().getUser(userId, ctx).getGroups(), permissions);
    }

    /**
     * Determines if folder is visible (without respect to user configuration).
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param groups The group identifier
     * @param permissions The permission bits
     * @return <code>true</code> if folder is visible; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isVisibleFor(final int folderId, final int userId, final int[] groups, final UserPermissionBits permissions) throws OXException {
        if (null == groups) {
            return isVisibleFor(folderId, userId, permissions);
        }

        final FolderObject fo = getFolderObject(folderId);
        if (null == readCon) {
            return fo.getEffectiveUserPermission(userId, permissions).isFolderVisible();
        }
        try {
            return fo.getEffectiveUserPermission(userId, permissions, readCon).isFolderVisible();
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Determines if folder permission is at least set to <code>READ_FOLDER</code> (without respect to user configuration).
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param permissions The permission bits
     * @return <code>true</code> if folder permission applies; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isReadFolder(final int folderId, final int userId, final UserPermissionBits permissions) throws OXException {
        return isReadFolder(folderId, userId, UserStorage.getInstance().getUser(userId, ctx).getGroups(), permissions);
    }

    /**
     * Determines if folder permission is at least set to <code>READ_FOLDER</code> (without respect to user configuration).
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param groups The group identifier
     * @param permissions The permission bits
     * @return <code>true</code> if folder permission applies; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean isReadFolder(final int folderId, final int userId, final int[] groups, final UserPermissionBits permissions) throws OXException {
        if (null == groups) {
            return isReadFolder(folderId, userId, permissions);
        }

        final FolderObject fo = getFolderObject(folderId);
        if (null == readCon) {
            return fo.getEffectiveUserPermission(userId, permissions).getFolderPermission() >= OCLPermission.READ_FOLDER;
        }
        try {
            return fo.getEffectiveUserPermission(userId, permissions, readCon).getFolderPermission() >= OCLPermission.READ_FOLDER;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Determines user's default folder of given module.
     *
     * @param userId The user ID
     * @param module The module
     * @return The user's default folder of given module
     * @throws OXException If operation fails
     */
    public FolderObject getDefaultFolder(final int userId, final int module) throws OXException {
        return getDefaultFolder(userId, module, -1);
    }

    /**
     * Determines user's default folder of given module and an optional folder type. Folders of module {@link FolderObject#INFOSTORE}
     * are created implicitly if they not yet exist.
     *
     * @param userId The user ID
     * @param module The module
     * @param type The type, or <code>-1</code> if not applicable
     * @return The user's default folder of given module and type
     * @throws OXException If operation fails
     */
    public FolderObject getDefaultFolder(final int userId, final int module, final int type) throws OXException {
        return getFolderObject(getDefaultFolderID(userId, module, type));
    }

    /**
     * Determines the identifier of a user's default folder of given module.
     *
     * @param userId The user ID
     * @param module The module
     * @return The identifier of the user's default folder of given module
     * @throws OXException If operation fails
     */
    public int getDefaultFolderID(final int userId, final int module) throws OXException {
        return getDefaultFolderID(userId, module, -1);
    }

    /**
     * Determines a user's default folder identifier of given module and an optional folder type. Folders of module
     * {@link FolderObject#INFOSTORE} are created implicitly if they not yet exist.
     *
     * @param userId The user ID
     * @param module The module
     * @param type The type, or <code>-1</code> if not applicable
     * @return The identifier of the user's default folder of given module and type, or -1 if no folder was found
     * @throws OXException If operation fails
     */
    public int getDefaultFolderID(int userId, int module, int type) throws OXException {
        try {
            /*
             * Read out default folder
             */
            int folderId = -1 == type ? OXFolderSQL.getUserDefaultFolder(userId, module, readCon, ctx) : OXFolderSQL.getUserDefaultFolder(userId, module, type, readCon, ctx);
            if (-1 != folderId) {
                return folderId;
            }
            if (FolderObject.INFOSTORE != module) {
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(module), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            }
            User user = UserStorage.getInstance().getUser(userId, ctx);
            if (user.isGuest()) {
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(module), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            }
            return createAndGetFolderId(userId, module, type, user);
        } catch (OXException e) {
            throw e;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private int createAndGetFolderId(int userId, int module, int type, User user) throws OXException, SQLException {
        int folderId = -1;
        Connection wc = DBPool.pickupWriteable(ctx);
        boolean rollback = true;
        boolean created = false;
        int contextId = ctx.getContextId();
        Locale userLocale = user.getLocale();
        try {
            wc.setAutoCommit(false);
            /*
            * Check existence again within this transaction to avoid race conditions
            */
            folderId = -1 == type ? OXFolderSQL.getUserDefaultFolder(userId, module, wc, ctx) : OXFolderSQL.getUserDefaultFolder(userId, module, type, wc, ctx);
            if (-1 == folderId) {
                /*
                * Not found, create default folder, either trash or public
                */
                int folderType = -1 == type ? FolderObject.PUBLIC : type;
                if (folderType == FolderObject.TRASH) {
                    ConditionTreeMapManagement.dropFor(contextId);
                    folderId = InfoStoreFolderAdminHelper.addDefaultFolder(wc, contextId, userId, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, folderType, userLocale, Optional.empty());
                } else if (folderType == FolderObject.PUBLIC){
                    ConditionTreeMapManagement.dropFor(contextId);
                    folderId = InfoStoreFolderAdminHelper.addDefaultFolder(wc, contextId, userId, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, folderType, userLocale, Optional.empty());
                }
                created = true;
            }
            wc.commit();
            rollback = false;
        } finally {
            if (rollback) {
                Databases.rollback(wc);
            }
            Databases.autocommit(wc);
            if (created) {
                DBPool.closeWriterSilent(ctx, wc);
            } else {
                DBPool.closeWriterAfterReading(ctx, wc);
            }
        }
        return folderId;
    }

    /**
     * Determines if session's user is allowed to delete all objects located in given folder.
     * <p>
     * <b>Note</b>: This method checks only by contained items and does <small><b>NOT</b></small> check by the user's effective folder
     * permission itself. Thus the user is supposed to hold sufficient folder permissions on specified folder.
     *
     * @param folder The folder object
     * @param session The current user session
     * @param ctx The context
     * @return <code>true</code> if user can delete all objects in folder; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public final boolean canDeleteAllObjectsInFolder(final FolderObject folder, final Session session, final Context ctx) throws OXException {
        final int userId = session.getUserId();
        final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(userId, ctx);
        try {
            /*
             * Check user permission on folder
             */
            final OCLPermission oclPerm = folder.getEffectiveUserPermission(userId, userConfig, readCon);
            if (!oclPerm.isFolderVisible()) {
                /*
                 * Folder is not visible to user
                 */
                return false;
            }
            if (oclPerm.canDeleteAllObjects()) {
                /*
                 * Can delete all objects
                 */
                return true;
            }
            if (oclPerm.canDeleteOwnObjects()) {
                /*
                 * User may only delete own objects. Check if folder contains foreign objects which must not be deleted.
                 */
                return !containsForeignObjects(folder, session, ctx);
            }
            /*
             * No delete permission: Return true if folder is empty
             */
            return isEmpty(folder, session, ctx);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Checks if given folder contains session-user-foreign objects.
     *
     * @param folder The folder to check
     * @param session The session
     * @param ctx The context
     * @return <code>true</code> if given folder contains session-user-foreign objects; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public final boolean containsForeignObjects(final FolderObject folder, final Session session, final Context ctx) throws OXException {
        try {
            final int module = folder.getModule();
            if (module == FolderObject.TASK) {
                final Tasks tasks = Tasks.getInstance();
                if (null == readCon) {
                    Connection rc = null;
                    try {
                        rc = DBPool.pickup(ctx);
                        return tasks.containsNotSelfCreatedTasks(session, rc, folder.getObjectID());
                    } finally {
                        if (null != rc) {
                            DBPool.closeReaderSilent(ctx, rc);
                        }
                    }
                }
                return tasks.containsNotSelfCreatedTasks(session, readCon, folder.getObjectID());
            } else if (module == FolderObject.CALENDAR) {
                CalendarSession calendarSession = ServerServiceRegistry.getInstance().getService(CalendarService.class, true).init(session);
                calendarSession.set(PARAMETER_CONNECTION(), readCon);
                return calendarSession.getCalendarService().getUtilities().containsForeignEvents(calendarSession, String.valueOf(folder.getObjectID()));
            } else if (module == FolderObject.CONTACT) {
                final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
                return contactService.containsForeignObjectInFolder(session, String.valueOf(folder.getObjectID()));
            } else if (module == FolderObject.INFOSTORE) {
                final InfostoreFacade db = new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                return db.hasFolderForeignObjects(folder.getObjectID(), ServerSessionAdapter.valueOf(session, ctx));
            } else {
                throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(module), Integer.valueOf(ctx.getContextId()));
            }
        } catch (RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Checks if given folder is empty.
     *
     * @param folder The folder to check
     * @param session The session
     * @param ctx The context
     * @return <code>true</code> if given folder is empty; otherwise <code>false</code>
     * @throws OXException If checking emptiness fails
     */
    public final boolean isEmpty(final FolderObject folder, final Session session, final Context ctx) throws OXException {
        try {
            final int module = folder.getModule();
            switch (module) {
                case FolderObject.TASK: {
                    final Tasks tasks = Tasks.getInstance();
                    return readCon == null ? tasks.isFolderEmpty(ctx, folder.getObjectID()) : tasks.isFolderEmpty(ctx, readCon, folder.getObjectID());
                }
                case FolderObject.CALENDAR: {
                    CalendarSession calendarSession = ServerServiceRegistry.getInstance().getService(CalendarService.class, true).init(session);
                    calendarSession.set(PARAMETER_CONNECTION(), readCon);
                    return 0 == calendarSession.getCalendarService().getUtilities().countEvents(calendarSession, String.valueOf(folder.getObjectID()));
                }
                case FolderObject.CONTACT: {
                    final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
                    return contactService.isFolderEmpty(session, String.valueOf(folder.getObjectID()));
                }
                case FolderObject.INFOSTORE: {
                    final InfostoreFacade db = new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                    return db.isFolderEmpty(folder.getObjectID(), ctx);
                }
                default:
                    throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(module), Integer.valueOf(ctx.getContextId()));
            }
        } catch (RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Gets the folder's item count.
     *
     * @param folder The folder
     * @param session The session
     * @param ctx The context
     * @return The item count or <code>-1</code> if unknown
     * @throws OXException If item count cannot be returned
     */
    public long getItemCount(final FolderObject folder, final Session session, final Context ctx) throws OXException {
        try {
            session.getUserId();
            switch (folder.getModule()) {
                case FolderObject.TASK: {
                    return new TasksSQLImpl(session).countTasks(folder);
                }
                case FolderObject.CALENDAR: {
                    CalendarSession calendarSession = ServerServiceRegistry.getInstance().getService(CalendarService.class, true).init(session);
                    calendarSession.set(PARAMETER_CONNECTION(), readCon);
                    return calendarSession.getCalendarService().getUtilities().countEvents(calendarSession, String.valueOf(folder.getObjectID()));
                }
                case FolderObject.CONTACT:
                    try {
                        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                        return contactService.countContacts(session, Integer.toString(folder.getObjectID()));
                    } catch (OXException e) {
                        if (ContactExceptionCodes.NO_ACCESS_PERMISSION.equals(e)) {
                            return 0;
                        }
                        throw e;
                    }
                case FolderObject.INFOSTORE:
                    try {
                        final InfostoreFacade db = new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                        return db.countDocuments(folder.getObjectID(), ServerSessionAdapter.valueOf(session, ctx));
                    } catch (OXException e) {
                        if (InfostoreExceptionCodes.NO_READ_PERMISSION.equals(e)) {
                            return 0;
                        }
                        throw e;
                    }
                default:
                    return -1;
            }
        } catch (RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Checks whether the given folder has a subscription.
     *
     * @param folderId The folder id
     * @param contextId The context id
     * @return <code>true</code> if the folder has a subscription, <code>false</code> otherwise
     */
    public boolean isSubscriptionFolder(String folderId, int contextId) throws OXException {
        if (null != readCon) {
            return OXFolderDependentUtil.hasSubscription(readCon, contextId, folderId);
        }

        Connection con = DBPool.pickup(ctx);
        try {
            return OXFolderDependentUtil.hasSubscription(con, contextId, folderId);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Optionally gets a <i>virtual</i> folder object representing one of the virtual list folders of the different modules where folders
     * not seen in the tree view are bundled.
     * 
     * @param folderId The identifier of the folder to optionally get the virtual folder object representation for
     * @return The virtual folder with the supplied folder identifier, or <code>null</code> if the identifier refers to a non-virtual folder
     */
    private static FolderObject optVirtualFolder(int folderId) {
        switch (folderId) {
            case FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID:
                return FolderObject.createVirtualFolderObject(folderId, FolderObject.getFolderString(folderId, LocaleTools.DEFAULT_LOCALE), FolderObject.INFOSTORE, true, FolderObject.SYSTEM_TYPE);
            case FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID:
                return FolderObject.createVirtualFolderObject(folderId, FolderObject.getFolderString(folderId, LocaleTools.DEFAULT_LOCALE), FolderObject.TASK, true, FolderObject.SYSTEM_TYPE);
            case FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID:
                return FolderObject.createVirtualFolderObject(folderId, FolderObject.getFolderString(folderId, LocaleTools.DEFAULT_LOCALE), FolderObject.CALENDAR, true, FolderObject.SYSTEM_TYPE);
            case FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID:
                return FolderObject.createVirtualFolderObject(folderId, FolderObject.getFolderString(folderId, LocaleTools.DEFAULT_LOCALE), FolderObject.CONTACT, true, FolderObject.SYSTEM_TYPE);
            default:
                return null;
        }
    }

}
