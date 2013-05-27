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

package com.openexchange.tools.oxfolder;

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.contact.ContactService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.TaskStorage;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

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
        } catch (final OXException e) {
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
            } catch (final OXException e) {
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
            } catch (final OXException e) {
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
        } catch (final SQLException e) {
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
    public final FolderObject getDefaultFolder(final int userId, final int module) throws OXException {
        try {
            final int folderId = OXFolderSQL.getUserDefaultFolder(userId, module, readCon, ctx);
            if (folderId == -1) {
                if (FolderObject.INFOSTORE != module) {
                    throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(
                        folderModule2String(module),
                        getUserName(userId, ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                /*
                 * Re-Create default infostore folder
                 */
                final Connection wc = DBPool.pickupWriteable(ctx);
                try {
                    wc.setAutoCommit(false);
                    final String displayName = UserStorage.getStorageUser(userId, ctx).getDisplayName();
                    final int fuid = new OXFolderAdminHelper().addUserToInfoStore(userId, displayName, ctx.getContextId(), wc);
                    wc.commit();
                    return getFolderObject(fuid);
                } catch (final SQLException e) {
                    DBUtils.rollback(wc);
                    throw e;
                } catch (final Exception e) {
                    DBUtils.rollback(wc);
                    throw e;
                } finally {
                    DBUtils.autocommit(wc);
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
            return getFolderObject(folderId);
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
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
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
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
            final int userId = session.getUserId();
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
                final AppointmentSQLInterface calSql =
                    ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
                if (readCon == null) {
                    return calSql.checkIfFolderContainsForeignObjects(userId, folder.getObjectID());
                }
                return calSql.checkIfFolderContainsForeignObjects(userId, folder.getObjectID(), readCon);
            } else if (module == FolderObject.CONTACT) {
                if (readCon == null) {
                    return Contacts.containsForeignObjectInFolder(folder.getObjectID(), userId, session);
                }
                return Contacts.containsForeignObjectInFolder(folder.getObjectID(), userId, session, readCon);
            } else if (module == FolderObject.PROJECT) {
                return false;
            } else if (module == FolderObject.INFOSTORE) {
                final InfostoreFacade db =
                    new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(userId, ctx);
                return db.hasFolderForeignObjects(
                    folder.getObjectID(),
                    ctx,
                    UserStorage.getStorageUser(session.getUserId(), ctx),
                    userConfig);
            } else {
                throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(module), Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
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
            final int userId = session.getUserId();
            final int module = folder.getModule();
            switch (module) {
            case FolderObject.TASK: {
                final Tasks tasks = Tasks.getInstance();
                return readCon == null ? tasks.isFolderEmpty(ctx, folder.getObjectID()) : tasks.isFolderEmpty(
                    ctx,
                    readCon,
                    folder.getObjectID());
            }
            case FolderObject.CALENDAR: {
                final AppointmentSQLInterface calSql =
                    ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
                return readCon == null ? calSql.isFolderEmpty(userId, folder.getObjectID()) : calSql.isFolderEmpty(
                    userId,
                    folder.getObjectID(),
                    readCon);
            }
            case FolderObject.CONTACT: {
                return readCon == null ? !Contacts.containsAnyObjectInFolder(folder.getObjectID(), ctx) : !Contacts.containsAnyObjectInFolder(
                    folder.getObjectID(),
                    readCon,
                    ctx);
            }
            case FolderObject.PROJECT:
                return true;
            case FolderObject.INFOSTORE: {
                final InfostoreFacade db =
                    new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                return db.isFolderEmpty(folder.getObjectID(), ctx);
            }
            default:
                throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(module), Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
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
            final int userId = session.getUserId();
            switch (folder.getModule()) {
            case FolderObject.TASK: {
                return new TasksSQLImpl(session).countTasks(folder);
            }
            case FolderObject.CALENDAR: {
                final AppointmentSqlFactoryService service =
                    ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
                final AppointmentSQLInterface calSql = service.createAppointmentSql(session);
                return calSql.countObjectsInFolder(folder.getObjectID());
            }
            case FolderObject.CONTACT:
                try {
                    final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
                    return contactService.countContacts(session, Integer.toString(folder.getObjectID()));
                } catch (final OXException e) {
                    if (ContactExceptionCodes.NO_ACCESS_PERMISSION.equals(e)) {
                        return 0;
                    }
                    throw e;
                }
            case FolderObject.PROJECT:
                return 0;
            case FolderObject.INFOSTORE:
                try {
                    final InfostoreFacade db =
                        new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
                    final User user =
                        session instanceof ServerSession ? ((ServerSession) session).getUser() : UserStorage.getStorageUser(userId, ctx);
                    final UserConfiguration userConf =
                        session instanceof ServerSession ? ((ServerSession) session).getUserConfiguration() : UserConfigurationStorage.getInstance().getUserConfiguration(
                            userId,
                            user.getGroups(),
                            ctx);
                    return db.countDocuments(folder.getObjectID(), ctx, user, userConf);
                } catch (final OXException e) {
                    if (InfostoreExceptionCodes.NO_READ_PERMISSION.equals(e)) {
                        return 0;
                    }
                    throw e;
                }
            default:
                return -1;
            }
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

}
