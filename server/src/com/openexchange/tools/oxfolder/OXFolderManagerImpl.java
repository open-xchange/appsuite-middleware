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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.database.DBPoolingException;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.internal.FolderDeleteListenerRegistry;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.links.Links;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.StaticDBPoolProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.encoding.Charsets;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnInsert;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnRemove;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OXFolderManagerImpl} - The {@link OXFolderManager} implementation
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class OXFolderManagerImpl extends OXFolderManager {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OXFolderManagerImpl.class);

    private static final String TABLE_OXFOLDER_TREE = "oxfolder_tree";

    private final Connection readCon;

    private final Connection writeCon;

    private final Context ctx;

    private final UserConfiguration userConfig;

    private final User user;

    private final Session session;

    private OXFolderAccess oxfolderAccess;

    private AppointmentSQLInterface cSql;

    /**
     * Getter for testing purposes.
     * 
     * @return
     */
    public AppointmentSQLInterface getCSql() {
        return cSql;
    }

    /**
     * Setter for testing purposes.
     * 
     * @param sql
     */
    public void setCSql(final AppointmentSQLInterface sql) {
        cSql = sql;
    }

    /**
     * Constructor which only uses <code>Session</code>. Optional connections are going to be set to <code>null</code>.
     * 
     * @param session The session providing needed user data
     * @throws OXFolderException If instantiation fails
     */
    OXFolderManagerImpl(final Session session) throws OXFolderException {
        this(session, null, null);
    }

    /**
     * Constructor which only uses <code>Session</code> and <code>OXFolderAccess</code>. Optional connection are going to be set to
     * <code>null</code>.
     * 
     * @throws OXFolderException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final OXFolderAccess oxfolderAccess) throws OXFolderException {
        this(session, oxfolderAccess, null, null);
    }

    /**
     * Constructor which uses <code>Session</code> and also uses a readable and a writable <code>Connection</code>.
     * 
     * @throws OXFolderException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final Connection readCon, final Connection writeCon) throws OXFolderException {
        this(session, null, readCon, writeCon);
    }

    /**
     * Constructor which uses <code>Session</code>, <code>OXFolderAccess</code> and also uses a readable and a writable
     * <code>Connection</code>.
     * 
     * @throws OXFolderException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final OXFolderAccess oxfolderAccess, final Connection readCon, final Connection writeCon) throws OXFolderException {
        super();
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new OXFolderException(e);
        }
        userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
        user = UserStorage.getStorageUser(session.getUserId(), ctx);
        this.readCon = readCon;
        this.writeCon = writeCon;
        this.oxfolderAccess = oxfolderAccess;
        final AppointmentSqlFactoryService factory = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
        if (factory != null) {
            this.cSql = factory.createAppointmentSql(session);
        } else {
            this.cSql = null;
        }
    }

    private OXFolderAccess getOXFolderAccess() {
        if (oxfolderAccess != null) {
            return oxfolderAccess;
        }
        return (oxfolderAccess = new OXFolderAccess(writeCon, ctx));
    }

    @Override
    public FolderObject createFolder(final FolderObject folderObj, final boolean checkPermissions, final long createTime) throws OXException {
        /*
         * No need to synchronize here since new folder IDs are unique
         */
        if (!folderObj.containsFolderName() || folderObj.getFolderName() == null || folderObj.getFolderName().length() == 0) {
            throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.TITLE, "", Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsParentFolderID()) {
            throw new OXFolderException(
                FolderCode.MISSING_FOLDER_ATTRIBUTE,
                FolderChildFields.FOLDER_ID,
                "",
                Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsModule()) {
            throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.MODULE, "", Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsType()) {
            throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.TYPE, "", Integer.valueOf(ctx.getContextId()));
        }
        if (folderObj.getPermissions() == null || folderObj.getPermissions().size() == 0) {
            throw new OXFolderException(
                FolderCode.MISSING_FOLDER_ATTRIBUTE,
                FolderFields.PERMISSIONS,
                "",
                Integer.valueOf(ctx.getContextId()));
        }
        OXFolderUtility.checkFolderStringData(folderObj);
        final FolderObject parentFolder = getOXFolderAccess().getFolderObject(folderObj.getParentFolderID());
        if (checkPermissions) {
            /*
             * Check, if user holds right to create a sub-folder in given parent folder
             */
            try {
                final EffectivePermission p = parentFolder.getEffectiveUserPermission(user.getId(), userConfig, readCon);
                if (!p.canCreateSubfolders()) {
                    final OXFolderException fe = new OXFolderException(
                        FolderCode.NO_CREATE_SUBFOLDER_PERMISSION,
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(parentFolder),
                        Integer.valueOf(ctx.getContextId()));
                    if (p.getUnderlyingPermission().canCreateSubfolders()) {
                        fe.setCategory(Category.USER_CONFIGURATION);
                    }
                    throw fe;
                }
                if (!userConfig.hasModuleAccess(folderObj.getModule())) {
                    throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, Category.USER_CONFIGURATION, OXFolderUtility.getUserName(
                        user.getId(),
                        ctx), OXFolderUtility.folderModule2String(folderObj.getModule()), Integer.valueOf(ctx.getContextId()));
                }
                if (parentFolder.getType() == FolderObject.PUBLIC && !userConfig.hasFullPublicFolderAccess()) {
                    throw new OXFolderException(
                        FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS,
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(parentFolder),
                        Integer.valueOf(ctx.getContextId()));
                }
            } catch (final SQLException e) {
                throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check folder types
         */
        if (!OXFolderUtility.checkFolderTypeAgainstParentType(parentFolder, folderObj.getType())) {
            throw new OXFolderException(
                FolderCode.INVALID_TYPE,
                OXFolderUtility.getFolderName(parentFolder),
                OXFolderUtility.folderType2String(folderObj.getType()),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if parent folder is a shared folder
         */
        if (parentFolder.isShared(user.getId())) {
            /*
             * Current user wants to create a subfolder underneath a shared folder
             */
            OXFolderUtility.checkSharedSubfolderOwnerPermission(parentFolder, folderObj, user.getId(), ctx);
            /*
             * Set folder creator for next permission check and for proper insert value
             */
            folderObj.setCreatedBy(parentFolder.getCreatedBy());
        }
        /*
         * Check folder module
         */
        if (!isKnownModule(folderObj.getModule())) {
            throw new OXFolderException(
                FolderCode.UNKNOWN_MODULE,
                OXFolderUtility.folderModule2String(folderObj.getModule()),
                Integer.valueOf(ctx.getContextId()));
        }
        if (!OXFolderUtility.checkFolderModuleAgainstParentModule(
            parentFolder.getObjectID(),
            parentFolder.getModule(),
            folderObj.getModule(),
            ctx.getContextId())) {
            throw new OXFolderLogicException(
                FolderCode.INVALID_MODULE,
                OXFolderUtility.getFolderName(parentFolder),
                OXFolderUtility.folderModule2String(folderObj.getModule()),
                Integer.valueOf(ctx.getContextId()));
        }
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(folderObj, userConfig, ctx);
        /*
         * Check if admin exists and permission structure
         */
        OXFolderUtility.checkFolderPermissions(folderObj, user.getId(), ctx);
        OXFolderUtility.checkPermissionsAgainstUserConfigs(folderObj, ctx);
        if (FolderObject.PUBLIC == folderObj.getType()) {
            new CheckPermissionOnInsert(session, writeCon, ctx).checkParentPermissions(
                parentFolder.getObjectID(),
                folderObj.getNonSystemPermissionsAsArray(),
                createTime);
        }
        /*
         * Check if duplicate folder exists
         */
        try {
            if (OXFolderSQL.lookUpFolder(folderObj.getParentFolderID(), folderObj.getFolderName(), folderObj.getModule(), readCon, ctx) != -1) {
                /*
                 * A duplicate folder exists
                 */
                throw new OXFolderException(
                    FolderCode.NO_DUPLICATE_FOLDER,
                    OXFolderUtility.getFolderName(parentFolder),
                    Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * This folder shall be shared to other users
         */
        if (folderObj.getType() == FolderObject.PRIVATE && folderObj.getPermissions().size() > 1) {
            final Set<Integer> diff = OXFolderUtility.getShareUsers(null, folderObj.getPermissions(), user.getId(), ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final int[] fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    allSharedFolders = new FolderObject[fuids.length];
                    for (int i = 0; i < fuids.length; i++) {
                        allSharedFolders[i] = getOXFolderAccess().getFolderObject(fuids[i]);
                    }
                } catch (final DBPoolingException e) {
                    throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(diff, allSharedFolders, folderObj.getFolderName(), ctx);
            }
        }
        /*
         * Check duplicate permissions
         */
        OXFolderUtility.checkForDuplicateNonSystemPermissions(folderObj, ctx);
        /*
         * Get new folder ID
         */
        int fuid = -1;
        try {
            fuid = OXFolderSQL.getNextSerial(ctx, writeCon);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        if (fuid < FolderObject.MIN_FOLDER_ID) {
            throw new OXFolderException(
                FolderCode.INVALID_SEQUENCE_ID,
                Integer.valueOf(fuid),
                Integer.valueOf(FolderObject.MIN_FOLDER_ID),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Call SQL insert
         */
        try {
            OXFolderSQL.insertFolderSQL(fuid, user.getId(), folderObj, createTime, ctx, writeCon);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Update cache with writable connection!
         */
        final Date creatingDate = new Date(createTime);
        folderObj.setObjectID(fuid);
        folderObj.setCreationDate(creatingDate);
        if (!folderObj.containsCreatedBy()) {
            folderObj.setCreatedBy(user.getId());
        }
        folderObj.setLastModified(creatingDate);
        folderObj.setModifiedBy(user.getId());
        folderObj.setSubfolderFlag(false);
        folderObj.setDefaultFolder(false);
        parentFolder.setSubfolderFlag(true);
        parentFolder.setLastModified(creatingDate);
        try {
            Connection wc = writeCon;
            final boolean create = (wc == null);
            try {
                if (create) {
                    wc = DBPool.pickupWriteable(ctx);
                }
                if (FolderCacheManager.isInitialized()) {
                    FolderCacheManager.getInstance().putFolderObject(parentFolder, ctx);
                    folderObj.fill(FolderCacheManager.getInstance().getFolderObject(fuid, false, ctx, wc));
                } else {
                    folderObj.fill(FolderObject.loadFolderObjectFromDB(fuid, ctx, wc));
                }
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
                }
                if (CalendarCache.isInitialized()) {
                    CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                }
                try {
                    new EventClient(session).create(folderObj);
                } catch (final EventException e) {
                    LOG.warn("Create event could not be enqueued", e);
                } catch (final ContextException e) {
                    LOG.warn("Create event could not be enqueued", e);
                }
                return folderObj;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterSilent(ctx, wc);
                    wc = null;
                }
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    @Override
    public FolderObject updateFolder(final FolderObject fo, final boolean checkPermissions, final long lastModified) throws OXException {
        if (checkPermissions) {
            if (fo.containsType() && fo.getType() == FolderObject.PUBLIC && !UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                session.getUserId(),
                ctx).hasFullPublicFolderAccess()) {
                throw new OXFolderException(
                    FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS,
                    OXFolderUtility.getUserName(session, user),
                    OXFolderUtility.getFolderName(fo),
                    Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Fetch effective permission from storage
             */
            final EffectivePermission perm = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userConfig);
            if (!perm.isFolderVisible()) {
                if (!perm.getUnderlyingPermission().isFolderVisible()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NOT_VISIBLE,
                        OXFolderUtility.getFolderName(fo),
                        OXFolderUtility.getUserName(session, user),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderException(
                    FolderCode.NOT_VISIBLE,
                    Category.USER_CONFIGURATION,
                    OXFolderUtility.getFolderName(fo),
                    OXFolderUtility.getUserName(session, user),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!perm.isFolderAdmin()) {
                if (!perm.getUnderlyingPermission().isFolderAdmin()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NO_ADMIN_ACCESS,
                        OXFolderUtility.getUserName(session, user),
                        OXFolderUtility.getFolderName(fo),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, Category.USER_CONFIGURATION, OXFolderUtility.getUserName(
                    session,
                    user), OXFolderUtility.getFolderName(fo), Integer.valueOf(ctx.getContextId()));
            }
        }
        final FolderObject storageVersion = getFolderFromMaster(fo.getObjectID());
        final boolean performMove = fo.containsParentFolderID();
        if (fo.containsPermissions() || fo.containsModule()) {
            if (performMove) {
                move(fo.getObjectID(), fo.getParentFolderID(), lastModified);
            }
            update(fo, lastModified);
        } else if (fo.containsFolderName()) {
            if (performMove) {
                move(fo.getObjectID(), fo.getParentFolderID(), lastModified);
            }
            rename(fo, lastModified);
        } else if (performMove) {
            /*
             * Perform move
             */
            move(fo.getObjectID(), fo.getParentFolderID(), lastModified);
        }
        /*
         * Finally update cache
         */
        try {
            Connection wc = writeCon;
            final boolean create = (wc == null);
            try {
                if (create) {
                    wc = DBPool.pickupWriteable(ctx);
                }
                if (FolderCacheManager.isEnabled()) {
                    fo.fill(FolderCacheManager.getInstance().getFolderObject(fo.getObjectID(), false, ctx, wc));
                    /*
                     * Update parent, too
                     */
                    FolderCacheManager.getInstance().loadFolderObject(fo.getParentFolderID(), ctx, wc);
                } else {
                    fo.fill(FolderObject.loadFolderObjectFromDB(fo.getObjectID(), ctx, wc));
                }
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
                }
                if (CalendarCache.isInitialized()) {
                    CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                }
                try {
                    new EventClient(session).modify(storageVersion, fo, getFolderFromMaster(fo.getParentFolderID()));
                } catch (final EventException e) {
                    LOG.warn("Update event could not be enqueued", e);
                }
                return fo;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterSilent(ctx, wc);
                    wc = null;
                }
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private void update(final FolderObject folderObj, final long lastModified) throws OXException {
        if (folderObj.getObjectID() <= 0) {
            throw new OXFolderException(FolderCode.INVALID_OBJECT_ID, OXFolderUtility.getFolderName(folderObj));
        }
        /*
         * Get storage version (and thus implicitly check existence)
         */
        final FolderObject storageObj = getFolderFromMaster(folderObj.getObjectID());
        if (folderObj.getPermissions() == null || folderObj.getPermissions().size() == 0) {
            if (folderObj.containsPermissions()) {
                /*
                 * Deny to set empty permissions
                 */
                throw new OXFolderException(
                    FolderCode.MISSING_FOLDER_ATTRIBUTE,
                    FolderFields.PERMISSIONS,
                    OXFolderUtility.getFolderName(folderObj),
                    Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Pass storage's permissions
             */
            folderObj.setPermissionsAsArray(storageObj.getPermissionsAsArray());
        }
        /*
         * Check if a move is done here
         */
        if (folderObj.containsParentFolderID() && storageObj.getParentFolderID() != folderObj.getParentFolderID()) {
            throw new OXFolderLogicException(FolderCode.NO_MOVE_THROUGH_UPDATE, OXFolderUtility.getFolderName(folderObj));
        }
        /*
         * Check folder name
         */
        if (folderObj.containsFolderName()) {
            if (folderObj.getFolderName() == null || folderObj.getFolderName().trim().length() == 0) {
                throw new OXFolderException(
                    FolderCode.MISSING_FOLDER_ATTRIBUTE,
                    FolderFields.TITLE,
                    OXFolderUtility.getFolderName(folderObj),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageObj.isDefaultFolder() && !folderObj.getFolderName().equals(storageObj.getFolderName())) {
                throw new OXFolderException(
                    FolderCode.NO_DEFAULT_FOLDER_RENAME,
                    OXFolderUtility.getFolderName(folderObj),
                    Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check if folder module is supposed to be updated
         */
        if (folderObj.containsModule() && folderObj.getModule() != storageObj.getModule()) {
            /*
             * Module update only allowed if known and folder is empty
             */
            if (!isKnownModule(folderObj.getModule())) {
                throw new OXFolderException(
                    FolderCode.UNKNOWN_MODULE,
                    OXFolderUtility.folderModule2String(folderObj.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!isFolderEmpty(storageObj.getObjectID(), storageObj.getModule())) {
                throw new OXFolderException(FolderCode.NO_FOLDER_MODULE_UPDATE);
            }
            final FolderObject parent = getFolderFromMaster(storageObj.getParentFolderID());
            if (!OXFolderUtility.checkFolderModuleAgainstParentModule(
                parent.getObjectID(),
                parent.getModule(),
                folderObj.getModule(),
                ctx.getContextId())) {
                throw new OXFolderLogicException(
                    FolderCode.INVALID_MODULE,
                    OXFolderUtility.getFolderName(parent),
                    OXFolderUtility.folderModule2String(folderObj.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
        } else {
            folderObj.setModule(storageObj.getModule());
        }
        /*
         * Check if shared
         */
        if (storageObj.isShared(user.getId())) {
            throw new OXFolderException(
                FolderCode.NO_SHARED_FOLDER_UPDATE,
                OXFolderUtility.getFolderName(folderObj),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check Permissions
         */
        folderObj.setType(storageObj.getType());
        folderObj.setCreatedBy(storageObj.getCreatedBy());
        folderObj.setDefaultFolder(storageObj.isDefaultFolder());
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(folderObj, userConfig, ctx);
        OXFolderUtility.checkFolderPermissions(folderObj, user.getId(), ctx);
        OXFolderUtility.checkPermissionsAgainstUserConfigs(folderObj, ctx);
        if (FolderObject.PUBLIC == folderObj.getType()) {
            {
                final OCLPermission[] removedPerms = OXFolderUtility.getPermissionsWithoutFolderAccess(
                    folderObj.getNonSystemPermissionsAsArray(),
                    storageObj.getNonSystemPermissionsAsArray());
                if (removedPerms.length > 0) {
                    new CheckPermissionOnRemove(session, writeCon, ctx).checkPermissionsOnUpdate(
                        folderObj.getObjectID(),
                        removedPerms,
                        lastModified);
                }
            }
            new CheckPermissionOnInsert(session, writeCon, ctx).checkParentPermissions(
                storageObj.getParentFolderID(),
                folderObj.getNonSystemPermissionsAsArray(),
                lastModified);
        }
        boolean rename = false;
        if (folderObj.containsFolderName() && !storageObj.getFolderName().equals(folderObj.getFolderName())) {
            rename = true;
            /*
             * Check for invalid characters
             */
            OXFolderUtility.checkFolderStringData(folderObj);
            /*
             * Rename: Check if duplicate folder exists
             */
            try {
                final int folderId = OXFolderSQL.lookUpFolderOnUpdate(
                    folderObj.getObjectID(),
                    storageObj.getParentFolderID(),
                    folderObj.getFolderName(),
                    folderObj.getModule(),
                    readCon,
                    ctx);
                if (folderId != -1 && folderId != folderObj.getObjectID()) {
                    /*
                     * A duplicate folder exists
                     */
                    throw new OXFolderException(FolderCode.NO_DUPLICATE_FOLDER, OXFolderUtility.getFolderName(new OXFolderAccess(
                        readCon,
                        ctx).getFolderObject(storageObj.getParentFolderID())), Integer.valueOf(ctx.getContextId()));
                }
            } catch (final SQLException e) {
                throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * This folder shall be shared to other users
         */
        if (folderObj.getType() == FolderObject.PRIVATE && folderObj.getPermissions().size() > 1) {
            final Set<Integer> diff = OXFolderUtility.getShareUsers(
                rename ? null : storageObj.getPermissions(),
                folderObj.getPermissions(),
                user.getId(),
                ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final int[] fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    allSharedFolders = new FolderObject[fuids.length];
                    for (int i = 0; i < fuids.length; i++) {
                        /*
                         * Remove currently updated folder
                         */
                        if (fuids[i] != folderObj.getObjectID()) {
                            allSharedFolders[i] = getOXFolderAccess().getFolderObject(fuids[i]);
                        }
                    }
                } catch (final DBPoolingException e) {
                    throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(
                    diff,
                    allSharedFolders,
                    rename ? folderObj.getFolderName() : storageObj.getFolderName(),
                    ctx);
            }
        }
        /*
         * Check duplicate permissions
         */
        OXFolderUtility.checkForDuplicateNonSystemPermissions(folderObj, ctx);
        /*
         * Call SQL update
         */
        try {
            OXFolderSQL.updateFolderSQL(user.getId(), folderObj, lastModified, ctx, writeCon);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private FolderObject getFolderFromMaster(final int folderId) throws OXException {
        return getFolderFromMaster(folderId, false);
    }

    private FolderObject getFolderFromMaster(final int folderId, final boolean withSubfolders) throws OXException {
        try {
            /*
             * Use writable connection to ensure to fetch from master database
             */
            Connection wc = writeCon;
            if (wc == null) {
                try {
                    wc = DBPool.pickupWriteable(ctx);
                    return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
                } finally {
                    if (wc != null) {
                        DBPool.closeWriterSilent(ctx, wc);
                    }
                }
            }
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
        } catch (final OXFolderNotFoundException e) {
            throw e;
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private boolean isFolderEmpty(final int folderId, final int module) throws OXException {
        if (module == FolderObject.TASK) {
            final Tasks tasks = Tasks.getInstance();
            return readCon == null ? tasks.isFolderEmpty(ctx, folderId) : tasks.isFolderEmpty(ctx, readCon, folderId);
        } else if (module == FolderObject.CALENDAR) {
            final AppointmentSQLInterface calSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(
                session);
            if (readCon == null) {
                try {
                    return calSql.isFolderEmpty(user.getId(), folderId);
                } catch (final SQLException e) {
                    throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, e.getMessage());
                }
            }
            try {
                return calSql.isFolderEmpty(user.getId(), folderId, readCon);
            } catch (final SQLException e) {
                throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, e.getMessage());
            }
        } else if (module == FolderObject.CONTACT) {
            return readCon == null ? !Contacts.containsAnyObjectInFolder(folderId, ctx) : !Contacts.containsAnyObjectInFolder(
                folderId,
                readCon,
                ctx);
        } else if (module == FolderObject.INFOSTORE) {
            final InfostoreFacade db = new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
            return db.isFolderEmpty(folderId, ctx);
        } else {
            throw new OXFolderException(
                FolderCode.UNKNOWN_MODULE,
                OXFolderUtility.folderModule2String(module),
                Integer.valueOf(ctx.getContextId()));
        }
    }

    private static boolean isKnownModule(final int module) {
        return ((module == FolderObject.TASK) || (module == FolderObject.CALENDAR) || (module == FolderObject.CONTACT) || (module == FolderObject.INFOSTORE));
    }

    private void rename(final FolderObject folderObj, final long lastModified) throws OXException {
        if (folderObj.getObjectID() <= 0) {
            throw new OXFolderException(FolderCode.INVALID_OBJECT_ID, OXFolderUtility.getFolderName(folderObj));
        } else if (!folderObj.containsFolderName() || folderObj.getFolderName() == null || folderObj.getFolderName().trim().length() == 0) {
            throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.TITLE, "", Integer.valueOf(ctx.getContextId()));
        }
        OXFolderUtility.checkFolderStringData(folderObj);
        /*
         * Get storage version (and thus implicitly check existence)
         */
        final FolderObject storageObj = getFolderFromMaster(folderObj.getObjectID());
        /*
         * Check if rename can be avoided (cause new name equals old one) and prevent default folder rename
         */
        if (storageObj.getFolderName().equals(folderObj.getFolderName())) {
            return;
        } else if (storageObj.isDefaultFolder()) {
            throw new OXFolderException(
                FolderCode.NO_DEFAULT_FOLDER_RENAME,
                OXFolderUtility.getFolderName(folderObj),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check for duplicate folder
         */
        try {
            final int folderId = OXFolderSQL.lookUpFolderOnUpdate(
                folderObj.getObjectID(),
                storageObj.getParentFolderID(),
                folderObj.getFolderName(),
                storageObj.getModule(),
                readCon,
                ctx);
            if (folderId != -1 && folderId != folderObj.getObjectID()) {
                /*
                 * A duplicate folder exists
                 */
                throw new OXFolderException(
                    FolderCode.NO_DUPLICATE_FOLDER,
                    OXFolderUtility.getFolderName(new OXFolderAccess(readCon, ctx).getFolderObject(storageObj.getParentFolderID())),
                    Integer.valueOf(ctx.getContextId()));
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * This folder shall be shared to other users
         */
        if (folderObj.getType() == FolderObject.PRIVATE && folderObj.getPermissions().size() > 1) {
            final Set<Integer> diff = OXFolderUtility.getShareUsers(null, folderObj.getPermissions(), user.getId(), ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final int[] fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    allSharedFolders = new FolderObject[fuids.length];
                    for (int i = 0; i < fuids.length; i++) {
                        /*
                         * Remove currently renamed folder
                         */
                        if (fuids[i] != folderObj.getObjectID()) {
                            allSharedFolders[i] = getOXFolderAccess().getFolderObject(fuids[i]);
                        }
                    }
                } catch (final DBPoolingException e) {
                    throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(diff, allSharedFolders, folderObj.getFolderName(), ctx);
            }
        }
        /*
         * Call SQL rename
         */
        try {
            OXFolderSQL.renameFolderSQL(user.getId(), folderObj, lastModified, ctx, writeCon);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private final int[] SYSTEM_PUBLIC_FOLDERS = { FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID };

    private static boolean isInArray(final int key, final int[] a) {
        Arrays.sort(a);
        return Arrays.binarySearch(a, key) >= 0;
    }

    private void move(final int folderId, final int targetFolderId, final long lastModified) throws OXException {
        /*
         * Load source folder
         */
        final FolderObject storageSrc = getFolderFromMaster(folderId);
        /*
         * Folder is already in target folder
         */
        if (storageSrc.getParentFolderID() == targetFolderId) {
            return;
        }
        /*
         * Default folder must not be moved
         */
        if (storageSrc.isDefaultFolder()) {
            throw new OXFolderException(
                FolderCode.NO_DEFAULT_FOLDER_MOVE,
                OXFolderUtility.getFolderName(storageSrc),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * For further checks we need to load destination folder
         */
        final FolderObject storageDest = getOXFolderAccess().getFolderObject(targetFolderId);
        /*
         * Check for a duplicate folder in target folder
         */
        try {
            if (OXFolderSQL.lookUpFolder(targetFolderId, storageSrc.getFolderName(), storageSrc.getModule(), readCon, ctx) != -1) {
                throw new OXFolderException(
                    FolderCode.TARGET_FOLDER_CONTAINS_DUPLICATE,
                    OXFolderUtility.getFolderName(storageDest),
                    Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check a bunch of possible errors
         */
        try {
            if (storageSrc.isShared(user.getId())) {
                throw new OXFolderException(
                    FolderCode.NO_SHARED_FOLDER_MOVE,
                    OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageDest.isShared(user.getId())) {
                throw new OXFolderException(
                    FolderCode.NO_SHARED_FOLDER_TARGET,
                    OXFolderUtility.getFolderName(storageDest),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.SYSTEM_TYPE) {
                throw new OXFolderException(
                    FolderCode.NO_SYSTEM_FOLDER_MOVE,
                    OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PRIVATE && ((storageDest.getType() == FolderObject.PUBLIC || (storageDest.getType() == FolderObject.SYSTEM_TYPE && targetFolderId != FolderObject.SYSTEM_PRIVATE_FOLDER_ID)))) {
                throw new OXFolderException(
                    FolderCode.ONLY_PRIVATE_TO_PRIVATE_MOVE,
                    OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PUBLIC && ((storageDest.getType() == FolderObject.PRIVATE || (storageDest.getType() == FolderObject.SYSTEM_TYPE && !isInArray(
                targetFolderId,
                SYSTEM_PUBLIC_FOLDERS))))) {
                throw new OXFolderException(
                    FolderCode.ONLY_PUBLIC_TO_PUBLIC_MOVE,
                    OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getModule() == FolderObject.INFOSTORE && storageDest.getModule() != FolderObject.INFOSTORE && targetFolderId != FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                throw new OXFolderException(
                    FolderCode.INCOMPATIBLE_MODULES,
                    OXFolderUtility.folderModule2String(storageSrc.getModule()),
                    OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageSrc.getModule() != FolderObject.INFOSTORE && storageDest.getModule() == FolderObject.INFOSTORE) {
                throw new OXFolderException(
                    FolderCode.INCOMPATIBLE_MODULES,
                    OXFolderUtility.folderModule2String(storageSrc.getModule()),
                    OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageDest.getEffectiveUserPermission(user.getId(), userConfig).getFolderPermission() < OCLPermission.CREATE_SUB_FOLDERS) {
                throw new OXFolderPermissionException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, OXFolderUtility.getUserName(
                    user.getId(),
                    ctx), OXFolderUtility.getFolderName(storageDest), Integer.valueOf(ctx.getContextId()));
            } else if (folderId == targetFolderId) {
                throw new OXFolderPermissionException(FolderCode.NO_EQUAL_MOVE, Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if source folder has subfolders
         */
        try {
            if (storageSrc.hasSubfolders()) {
                /*
                 * Check if target is a descendant folder
                 */
                final List<Integer> parentIDList = new ArrayList<Integer>(1);
                parentIDList.add(Integer.valueOf(storageSrc.getObjectID()));
                if (OXFolderUtility.isDescendentFolder(parentIDList, targetFolderId, readCon, ctx)) {
                    throw new OXFolderException(
                        FolderCode.NO_SUBFOLDER_MOVE,
                        OXFolderUtility.getFolderName(storageSrc),
                        Integer.valueOf(ctx.getContextId()));
                }
                /*
                 * Count all moveable subfolders: TODO: Recursive check???
                 */
                final int numOfMoveableSubfolders = OXFolderSQL.getNumOfMoveableSubfolders(
                    storageSrc.getObjectID(),
                    user.getId(),
                    user.getGroups(),
                    readCon,
                    ctx);
                if (numOfMoveableSubfolders != storageSrc.getSubfolderIds(true, ctx).size()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NO_SUBFOLDER_MOVE_ACCESS,
                        OXFolderUtility.getUserName(session, user),
                        OXFolderUtility.getFolderName(storageSrc),
                        Integer.valueOf(ctx.getContextId()));
                }
            }
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * First treat as a delete prior to actual move
         */
        try {
            processDeletedFolderThroughMove(storageSrc, new CheckPermissionOnRemove(session, writeCon, ctx), lastModified);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Call SQL move
         */
        try {
            OXFolderSQL.moveFolderSQL(user.getId(), storageSrc, storageDest, lastModified, ctx, readCon, writeCon);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, storageSrc, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Now treat as an insert after actual move
         */
        try {
            processInsertedFolderThroughMove(
                getFolderFromMaster(folderId),
                new CheckPermissionOnInsert(session, writeCon, ctx),
                lastModified);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Update last-modified time stamps
         */
        try {
            OXFolderSQL.updateLastModified(storageSrc.getParentFolderID(), lastModified, user.getId(), writeCon, ctx);
            OXFolderSQL.updateLastModified(storageSrc.getObjectID(), lastModified, user.getId(), writeCon, ctx);
            OXFolderSQL.updateLastModified(storageDest.getObjectID(), lastModified, user.getId(), writeCon, ctx);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Update OLD parent in cache, cause this can only be done here
         */
        if (FolderCacheManager.isEnabled()) {
            try {
                Connection wc = writeCon;
                final boolean create = (wc == null);
                try {
                    if (create) {
                        wc = DBPool.pickupWriteable(ctx);
                    }
                    FolderCacheManager.getInstance().loadFolderObject(storageSrc.getParentFolderID(), ctx, wc);
                    FolderCacheManager.getInstance().loadFolderObject(storageDest.getParentFolderID(), ctx, wc);
                } finally {
                    if (create && wc != null) {
                        DBPool.closeWriterSilent(ctx, wc);
                    }
                }
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
    }

    private void processDeletedFolderThroughMove(final FolderObject folder, final CheckPermissionOnRemove checker, final long lastModified) throws DBPoolingException, SQLException, OXException {
        final int folderId = folder.getObjectID();
        final List<Integer> subflds = FolderObject.getSubfolderIds(folderId, ctx, writeCon);
        for (final Integer subfld : subflds) {
            processDeletedFolderThroughMove(getOXFolderAccess().getFolderObject(subfld.intValue()), checker, lastModified);
        }
        checker.checkPermissionsOnDelete(folder.getParentFolderID(), folderId, folder.getNonSystemPermissionsAsArray(), lastModified);
        /*
         * Now strip all system permissions
         */
        OXFolderSQL.deleteAllSystemPermission(folderId, writeCon, ctx);
    }

    private void processInsertedFolderThroughMove(final FolderObject folder, final CheckPermissionOnInsert checker, final long lastModified) throws DBPoolingException, SQLException, OXException {
        final int folderId = folder.getObjectID();
        checker.checkParentPermissions(folder.getParentFolderID(), folder.getNonSystemPermissionsAsArray(), lastModified);
        final List<Integer> subflds = FolderObject.getSubfolderIds(folderId, ctx, writeCon);
        for (final Integer subfld : subflds) {
            processInsertedFolderThroughMove(getOXFolderAccess().getFolderObject(subfld.intValue()), checker, lastModified);
        }
    }

    @Override
    public FolderObject clearFolder(final FolderObject fo, final boolean checkPermissions, final long lastModified) throws OXException {
        if (fo.getObjectID() <= 0) {
            throw new OXFolderException(FolderCode.INVALID_OBJECT_ID, OXFolderUtility.getFolderName(fo));
        }
        if (!fo.containsParentFolderID() || fo.getParentFolderID() <= 0) {
            /*
             * Incomplete, whereby its existence is checked
             */
            fo.setParentFolderID(getOXFolderAccess().getParentFolderID(fo.getObjectID()));
        } else {
            /*
             * Check existence
             */
            try {
                if (!OXFolderSQL.exists(fo.getObjectID(), readCon, ctx)) {
                    throw new OXFolderNotFoundException(fo.getObjectID(), ctx);
                }
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            } catch (final SQLException e) {
                throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
        if (checkPermissions) {
            /*
             * Check permissions
             */
            final EffectivePermission p = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userConfig);
            if (!p.isFolderVisible()) {
                if (p.getUnderlyingPermission().isFolderVisible()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NOT_VISIBLE,
                        OXFolderUtility.getFolderName(fo),
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderException(
                    FolderCode.NOT_VISIBLE,
                    Category.USER_CONFIGURATION,
                    OXFolderUtility.getFolderName(fo),
                    OXFolderUtility.getUserName(user.getId(), ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(fo, session, ctx)) {
            throw new OXFolderPermissionException(
                FolderCode.NOT_ALL_OBJECTS_DELETION,
                OXFolderUtility.getUserName(user.getId(), ctx),
                OXFolderUtility.getFolderName(fo.getObjectID(), ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Finally, delete folder content
         */
        final int module = fo.getModule();
        switch (module) {
        case FolderObject.CALENDAR:
            deleteContainedAppointments(fo.getObjectID());
            break;
        case FolderObject.TASK:
            deleteContainedTasks(fo.getObjectID());
            break;
        case FolderObject.CONTACT:
            deleteContainedContacts(fo.getObjectID());
            break;
        case FolderObject.UNBOUND:
            break;
        case FolderObject.INFOSTORE:
            deleteContainedDocuments(fo.getObjectID());
            break;
        case FolderObject.PROJECT:
            // TODO: Delete all projects in project folder
            break;
        default:
            throw new OXFolderException(FolderCode.UNKNOWN_MODULE, Integer.valueOf(module), Integer.valueOf(ctx.getContextId()));
        }
        return fo;
    }

    @Override
    public FolderObject deleteFolder(final FolderObject fo, final boolean checkPermissions, final long lastModified) throws OXException {
        if (fo.getObjectID() <= 0) {
            throw new OXFolderException(FolderCode.INVALID_OBJECT_ID, OXFolderUtility.getFolderName(fo));
        }
        if (!fo.containsParentFolderID() || fo.getParentFolderID() <= 0) {
            /*
             * Incomplete, whereby its existence is checked
             */
            fo.setParentFolderID(getOXFolderAccess().getParentFolderID(fo.getObjectID()));
        } else {
            /*
             * Check existence
             */
            try {
                if (!OXFolderSQL.exists(fo.getObjectID(), readCon, ctx)) {
                    throw new OXFolderNotFoundException(fo.getObjectID(), ctx);
                }
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            } catch (final SQLException e) {
                throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
        if (checkPermissions) {
            /*
             * Check permissions
             */
            final EffectivePermission p = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userConfig);
            if (!p.isFolderVisible()) {
                if (p.getUnderlyingPermission().isFolderVisible()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NOT_VISIBLE,
                        OXFolderUtility.getFolderName(fo),
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderException(
                    FolderCode.NOT_VISIBLE,
                    Category.USER_CONFIGURATION,
                    OXFolderUtility.getFolderName(fo),
                    OXFolderUtility.getUserName(user.getId(), ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!p.isFolderAdmin()) {
                if (!p.getUnderlyingPermission().isFolderAdmin()) {
                    throw new OXFolderPermissionException(
                        FolderCode.NO_ADMIN_ACCESS,
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(fo),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, Category.USER_CONFIGURATION, OXFolderUtility.getUserName(
                    user.getId(),
                    ctx), OXFolderUtility.getFolderName(fo), Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Get parent
         */
        final FolderObject parentObj = getOXFolderAccess().getFolderObject(fo.getParentFolderID());
        /*
         * Gather all deletable subfolders
         */
        final HashMap<Integer, HashMap<?, ?>> deleteableFolders;
        try {
            deleteableFolders = gatherDeleteableFolders(fo.getObjectID(), user.getId(), userConfig, StringCollection.getSqlInString(
                user.getId(),
                user.getGroups()));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Remember folder type
         */
        final int type = getOXFolderAccess().getFolderType(fo.getObjectID());
        /*
         * Delete folders
         */
        deleteValidatedFolders(deleteableFolders, lastModified, type);
        /*
         * Invalidate query caches
         */
        if (FolderQueryCacheManager.isInitialized()) {
            FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
        }
        if (CalendarCache.isInitialized()) {
            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
        }
        /*
         * Continue
         */
        try {
            Connection wc = writeCon;
            final boolean create = (wc == null);
            try {
                if (create) {
                    wc = DBPool.pickupWriteable(ctx);
                }
                /*
                 * Check parent subfolder flag
                 */
                final boolean hasSubfolders = (OXFolderSQL.getSubfolderIDs(parentObj.getObjectID(), wc, ctx).size() > 0);
                OXFolderSQL.updateSubfolderFlag(parentObj.getObjectID(), hasSubfolders, lastModified, wc, ctx);
                /*
                 * Update cache
                 */
                if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
                    FolderCacheManager.getInstance().loadFolderObject(parentObj.getObjectID(), ctx, wc);
                }
                /*
                 * Load return value
                 */
                fo.fill(FolderObject.loadFolderObjectFromDB(
                    fo.getObjectID(),
                    ctx,
                    wc,
                    true,
                    false,
                    "del_oxfolder_tree",
                    "del_oxfolder_permissions"));
                try {
                    new EventClient(session).delete(fo);
                } catch (final EventException e) {
                    LOG.warn("Delete event could not be enqueued", e);
                } catch (final ContextException e) {
                    LOG.warn("Delete event could not be enqueued", e);
                }
                return fo;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    @SuppressWarnings("unchecked")
    private void deleteValidatedFolders(final HashMap<Integer, HashMap<?, ?>> deleteableIDs, final long lastModified, final int type) throws OXException {
        final int deleteableIDsSize = deleteableIDs.size();
        final Iterator<Map.Entry<Integer, HashMap<?, ?>>> iter = deleteableIDs.entrySet().iterator();
        for (int i = 0; i < deleteableIDsSize; i++) {
            final Map.Entry<Integer, HashMap<?, ?>> entry = iter.next();
            final Integer folderID = entry.getKey();
            final HashMap<Integer, HashMap<?, ?>> hashMap = (HashMap<Integer, HashMap<?, ?>>) entry.getValue();
            /*
             * Delete subfolders first, if any exist
             */
            if (hashMap != null) {
                deleteValidatedFolders(hashMap, lastModified, type);
            }
            deleteValidatedFolder(folderID.intValue(), lastModified, type);
        }
    }

    private void deleteValidatedFolder(final int folderID, final long lastModified, final int type) throws OXException {
        /*
         * Iterate possibly listening folder delete listeners
         */
        for (final Iterator<FolderDeleteListenerService> iter = FolderDeleteListenerRegistry.getInstance().getDeleteListenerServices(); iter.hasNext();) {
            final FolderDeleteListenerService next = iter.next();
            try {
                next.onFolderDelete(folderID, ctx);
            } catch (final FolderException e) {
                LOG.error(new StringBuilder(128).append("Folder delete listener \"").append(next.getClass().getName()).append(
                    "\" failed for folder ").append(folderID).append(" int context ").append(ctx.getContextId()), e);
                throw new OXFolderException(e);
            }
        }
        /*
         * Delete folder
         */
        final int module = getOXFolderAccess().getFolderModule(folderID);
        switch (module) {
        case FolderObject.CALENDAR:
            deleteContainedAppointments(folderID);
            break;
        case FolderObject.TASK:
            deleteContainedTasks(folderID);
            break;
        case FolderObject.CONTACT:
            deleteContainedContacts(folderID);
            break;
        case FolderObject.UNBOUND:
            break;
        case FolderObject.INFOSTORE:
            deleteContainedDocuments(folderID);
            break;
        case FolderObject.PROJECT:
            // TODO: Delete all projects in project folder
            break;
        default:
            throw new OXFolderException(FolderCode.UNKNOWN_MODULE, Integer.valueOf(module), Integer.valueOf(ctx.getContextId()));
        }
        final OCLPermission[] perms = getOXFolderAccess().getFolderObject(folderID).getPermissionsAsArray();
        final int parent = getOXFolderAccess().getParentFolderID(folderID);
        /*
         * Call SQL delete
         */
        try {
            OXFolderSQL.delWorkingOXFolder(folderID, session.getUserId(), lastModified, ctx, writeCon);
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Process system permissions
         */
        if (FolderObject.PUBLIC == type) {
            new CheckPermissionOnRemove(session, writeCon, ctx).checkPermissionsOnDelete(parent, folderID, perms, lastModified);
        }
        /*
         * Remove from cache
         */
        if (FolderQueryCacheManager.isInitialized()) {
            FolderQueryCacheManager.getInstance().invalidateContextQueries(ctx.getContextId());
        }
        if (CalendarCache.isInitialized()) {
            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
        }
        if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
            try {
                FolderCacheManager.getInstance().removeFolderObject(folderID, ctx);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        /*
         * Remove remaining links & deactivate contact collector if necessary
         */
        Connection wc = writeCon;
        boolean closeWriter = false;
        if (wc == null) {
            try {
                wc = DBPool.pickupWriteable(ctx);
                closeWriter = true;
            } catch (final DBPoolingException e) {
                throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
            }
        }
        try {
            Links.deleteAllFolderLinks(folderID, ctx.getContextId(), wc);

            final ServerUserSetting sus = ServerUserSetting.getInstance(wc);
            final Integer collectFolder = sus.getIContactCollectionFolder(ctx.getContextId(), user.getId());
            if (null != collectFolder && folderID == collectFolder.intValue()) {
                sus.setIContactColletion(ctx.getContextId(), user.getId(), false);
                sus.setIContactCollectionFolder(ctx.getContextId(), user.getId(), null);
            }
        } catch (final SettingException e) {
            throw new OXFolderException(e);
        } finally {
            if (closeWriter) {
                DBPool.closeWriterSilent(ctx, wc);
            }
        }
    }

    private void deleteContainedAppointments(final int folderID) throws OXException {
        try {
            if (null == writeCon) {
                cSql.deleteAppointmentsInFolder(folderID);
            } else {
                cSql.deleteAppointmentsInFolder(folderID, writeCon);
            }
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private void deleteContainedTasks(final int folderID) throws OXException {
        final Tasks tasks = Tasks.getInstance();
        if (null == writeCon) {
            Connection wc = null;
            try {
                wc = DBPool.pickupWriteable(ctx);
                tasks.deleteTasksInFolder(session, wc, folderID);
            } catch (final DBPoolingException e) {
                throw new OXException(e);
            } finally {
                if (null != wc) {
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
        } else {
            tasks.deleteTasksInFolder(session, writeCon, folderID);
        }
    }

    private void deleteContainedContacts(final int folderID) throws OXException {
        try {
            Connection readCon = this.readCon;
            Connection writeCon = this.writeCon;
            final boolean createReadCon = (readCon == null);
            final boolean createWriteCon = (writeCon == null);
            if (createReadCon) {
                readCon = DBPool.pickup(ctx);
            }
            if (createWriteCon) {
                writeCon = DBPool.pickupWriteable(ctx);
            }
            try {
                Contacts.trashContactsFromFolder(folderID, session, readCon, writeCon, false);
            } finally {
                if (createReadCon && readCon != null) {
                    DBPool.push(ctx, readCon);
                }
                if (createWriteCon && writeCon != null) {
                    DBPool.pushWrite(ctx, writeCon);
                }
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private void deleteContainedDocuments(final int folderID) throws OXException {
        final InfostoreFacade db;
        if (writeCon == null) {
            db = new InfostoreFacadeImpl(new DBPoolProvider());
        } else {
            db = new InfostoreFacadeImpl(new StaticDBPoolProvider(writeCon));
            db.setCommitsTransaction(false);
        }
        db.setTransactional(true);
        db.startTransaction();
        try {
            db.removeDocument(folderID, System.currentTimeMillis(), new ServerSessionAdapter(session, ctx));
            db.commit();
        } catch (final OXException x) {
            db.rollback();
            throw x;
        } finally {
            db.finish();
        }
    }

    /**
     * Gathers all folders which are allowed to be deleted
     */
    private HashMap<Integer, HashMap<?, ?>> gatherDeleteableFolders(final int folderID, final int userId, final UserConfiguration userConfig, final String permissionIDs) throws OXException, DBPoolingException, SQLException {
        final HashMap<Integer, HashMap<?, ?>> deleteableIDs = new HashMap<Integer, HashMap<?, ?>>();
        gatherDeleteableSubfoldersRecursively(folderID, userId, userConfig, permissionIDs, deleteableIDs, folderID);
        return deleteableIDs;
    }

    /**
     * Gathers all folders which are allowed to be deleted in a recursive manner
     */
    private void gatherDeleteableSubfoldersRecursively(final int folderID, final int userId, final UserConfiguration userConfig, final String permissionIDs, final HashMap<Integer, HashMap<?, ?>> deleteableIDs, final int initParent) throws OXException, DBPoolingException, SQLException {
        final FolderObject delFolder = getOXFolderAccess().getFolderObject(folderID);
        /*
         * Check if shared
         */
        if (delFolder.isShared(userId)) {
            throw new OXFolderPermissionException(
                FolderCode.NO_SHARED_FOLDER_DELETION,
                OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if marked as default folder
         */
        if (delFolder.isDefaultFolder()) {
            throw new OXFolderPermissionException(
                FolderCode.NO_DEFAULT_FOLDER_DELETION,
                OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check user's effective permission
         */
        final EffectivePermission effectivePerm = getOXFolderAccess().getFolderPermission(folderID, userId, userConfig);
        if (!effectivePerm.isFolderVisible()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                if (initParent == folderID) {
                    throw new OXFolderPermissionException(
                        FolderCode.NOT_VISIBLE,
                        OXFolderUtility.getFolderName(folderID, ctx),
                        OXFolderUtility.getUserName(userId, ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw new OXFolderPermissionException(
                    FolderCode.HIDDEN_FOLDER_ON_DELETION,
                    OXFolderUtility.getFolderName(initParent, ctx),
                    Integer.valueOf(ctx.getContextId()),
                    OXFolderUtility.getUserName(userId, ctx));
            }
            if (initParent == folderID) {
                throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION, OXFolderUtility.getFolderName(
                    folderID,
                    ctx), OXFolderUtility.getUserName(userId, ctx), Integer.valueOf(ctx.getContextId()));
            }
            throw new OXFolderException(FolderCode.HIDDEN_FOLDER_ON_DELETION, Category.USER_CONFIGURATION, OXFolderUtility.getFolderName(
                initParent,
                ctx), Integer.valueOf(ctx.getContextId()), OXFolderUtility.getUserName(userId, ctx));
        }
        if (!effectivePerm.isFolderAdmin()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
                throw new OXFolderPermissionException(
                    FolderCode.NO_ADMIN_ACCESS,
                    OXFolderUtility.getUserName(userId, ctx),
                    OXFolderUtility.getFolderName(folderID, ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            throw new OXFolderException(
                FolderCode.NO_ADMIN_ACCESS,
                Category.USER_CONFIGURATION,
                OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(delFolder, session, ctx)) {
            throw new OXFolderPermissionException(
                FolderCode.NOT_ALL_OBJECTS_DELETION,
                OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check, if folder has subfolders
         */
        if (!delFolder.hasSubfolders()) {
            deleteableIDs.put(Integer.valueOf(folderID), null);
            return;
        }
        /*
         * No subfolders detected
         */
        final List<Integer> subfolders = OXFolderSQL.getSubfolderIDs(delFolder.getObjectID(), readCon, ctx);
        if (subfolders.isEmpty()) {
            deleteableIDs.put(Integer.valueOf(folderID), null);
            return;
        }
        final HashMap<Integer, HashMap<?, ?>> subMap = new HashMap<Integer, HashMap<?, ?>>();
        final int size = subfolders.size();
        final Iterator<Integer> it = subfolders.iterator();
        for (int i = 0; i < size; i++) {
            final int fuid = it.next().intValue();
            gatherDeleteableSubfoldersRecursively(fuid, userId, userConfig, permissionIDs, subMap, initParent);
        }
        deleteableIDs.put(Integer.valueOf(folderID), subMap);
    }

    /**
     * This routine is called through AJAX' folder tests!
     */
    @Override
    public void cleanUpTestFolders(final int[] fuids, final Context ctx) {
        for (int i = 0; i < fuids.length; i++) {
            try {
                OXFolderSQL.hardDeleteOXFolder(fuids[i], ctx, null);
                if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
                    try {
                        FolderCacheManager.getInstance().removeFolderObject(fuids[i], ctx);
                    } catch (final OXException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /*-
     * ----------------- STATIC HELPER METHODS ---------------------
     */

    private static Map<String, Integer> fieldMapping;

    static {
        final Map<String, Integer> fieldMapping = new HashMap<String, Integer>(9);
        fieldMapping.put("fuid", Integer.valueOf(DataObject.OBJECT_ID));
        fieldMapping.put("parent", Integer.valueOf(FolderChildObject.FOLDER_ID));
        fieldMapping.put("fname", Integer.valueOf(FolderObject.FOLDER_NAME));
        fieldMapping.put("module", Integer.valueOf(FolderObject.MODULE));
        fieldMapping.put("type", Integer.valueOf(FolderObject.TYPE));
        fieldMapping.put("creating_date", Integer.valueOf(DataObject.CREATION_DATE));
        fieldMapping.put("created_from", Integer.valueOf(DataObject.CREATED_BY));
        fieldMapping.put("changing_date", Integer.valueOf(DataObject.LAST_MODIFIED));
        fieldMapping.put("changed_from", Integer.valueOf(DataObject.MODIFIED_BY));
        OXFolderManagerImpl.fieldMapping = Collections.unmodifiableMap(fieldMapping);
    }

    private static Object getFolderValue(final int folderField, final FolderObject folder) {
        if (FolderObject.FOLDER_NAME == folderField) {
            return folder.getFolderName();
        } else if (DataObject.OBJECT_ID == folderField) {
            return Integer.valueOf(folder.getObjectID());
        } else if (FolderChildObject.FOLDER_ID == folderField) {
            return Integer.valueOf(folder.getParentFolderID());
        } else if (FolderObject.MODULE == folderField) {
            return Integer.valueOf(folder.getModule());
        } else if (FolderObject.TYPE == folderField) {
            return Integer.valueOf(folder.getType());
        } else if (DataObject.CREATION_DATE == folderField) {
            return folder.getCreationDate();
        } else if (DataObject.CREATED_BY == folderField) {
            return Integer.valueOf(folder.getCreatedBy());
        } else if (DataObject.LAST_MODIFIED == folderField) {
            return folder.getLastModified();
        } else if (DataObject.MODIFIED_BY == folderField) {
            return Integer.valueOf(folder.getModifiedBy());
        } else {
            throw new IllegalStateException("Unknown folder field ID: " + folder);
        }
    }

    private OXFolderException parseTruncated(final DataTruncation exc, final FolderObject folder, final String tableName) {
        final String[] fields = DBUtils.parseTruncatedFields(exc);
        final OXFolderException.Truncated[] truncateds = new OXFolderException.Truncated[fields.length];
        final StringBuilder sFields = new StringBuilder(fields.length << 3);
        for (int i = 0; i < fields.length; i++) {
            sFields.append(fields[i]);
            sFields.append(", ");
            final int valueLength;
            final int fieldId = fieldMapping.get(fields[i]).intValue();
            final Object tmp = getFolderValue(fieldId, folder);
            if (tmp instanceof String) {
                valueLength = Charsets.getBytes((String) tmp, Charsets.UTF_8).length;
            } else {
                valueLength = 0;
            }
            int tmp2 = -1;
            boolean closeReadCon = false;
            Connection readCon = this.readCon;
            if (readCon == null) {
                try {
                    readCon = DBPool.pickup(ctx);
                } catch (final DBPoolingException e) {
                    LOG.error("A readable connection could not be fetched from pool", e);
                    return new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, exc, Integer.valueOf(ctx.getContextId()));
                }
                closeReadCon = true;
            }
            try {
                tmp2 = DBUtils.getColumnSize(readCon, tableName, fields[i]);
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
                tmp2 = -1;
            } finally {
                if (closeReadCon) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            }
            final int length = -1 == tmp2 ? 0 : tmp2;
            truncateds[i] = new OXFolderException.Truncated() {

                public int getId() {
                    return fieldId;
                }

                public int getLength() {
                    return valueLength;
                }

                public int getMaxSize() {
                    return length;
                }
            };
        }
        sFields.setLength(sFields.length() - 2);
        final OXFolderException fe;
        if (truncateds.length > 0) {
            final OXFolderException.Truncated truncated = truncateds[0];
            fe = new OXFolderException(
                OXFolderException.FolderCode.TRUNCATED,
                exc,
                sFields.toString(),
                Integer.valueOf(truncated.getMaxSize()),
                Integer.valueOf(truncated.getLength()));
        } else {
            fe = new OXFolderException(
                OXFolderException.FolderCode.TRUNCATED,
                exc,
                sFields.toString(),
                Integer.valueOf(0),
                Integer.valueOf(0));
        }
        for (final OXFolderException.Truncated truncated : truncateds) {
            fe.addProblematic(truncated);
        }
        return fe;
    }

}
