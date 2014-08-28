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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.tools.oxfolder.OXFolderUtility.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.folder.internal.FolderDeleteListenerRegistry;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnInsert;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnRemove;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OXFolderManagerImpl} - The {@link OXFolderManager} implementation
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class OXFolderManagerImpl extends OXFolderManager implements OXExceptionConstants {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderManagerImpl.class);

    /**
     * No options.
     */
    static final int OPTION_NONE = 0;

    /**
     * The option to deny updating a folder's module (provided that folder is empty).
     */
    static final int OPTION_DENY_MODULE_UPDATE = 1;

    /**
     * Allows changing the "created by" property.
     */
    private static final int OPTION_OVERRIDE_CREATED_BY = 2;

    private static final String TABLE_OXFOLDER_TREE = "oxfolder_tree";

    private final Connection readCon;
    private final Connection writeCon;
    private final Context ctx;
    private final UserPermissionBits userPerms;
    private final User user;
    private final Session session;
    private final List<OXException> warnings;
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
     * @throws OXException If instantiation fails
     */
    OXFolderManagerImpl(final Session session) throws OXException {
        this(session, null, null);
    }

    /**
     * Constructor which only uses <code>Session</code> and <code>OXFolderAccess</code>. Optional connection are going to be set to
     * <code>null</code>.
     *
     * @throws OXException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final OXFolderAccess oxfolderAccess) throws OXException {
        this(session, oxfolderAccess, null, null);
    }

    /**
     * Constructor which uses <code>Session</code> and also uses a readable and a writable <code>Connection</code>.
     *
     * @throws OXException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final Connection readCon, final Connection writeCon) throws OXException {
        this(session, null, readCon, writeCon);
    }

    /**
     * Constructor which uses <code>Session</code>, <code>OXFolderAccess</code> and also uses a readable and a writable
     * <code>Connection</code>.
     *
     * @throws OXException If instantiation fails
     */
    OXFolderManagerImpl(final Session session, final OXFolderAccess oxfolderAccess, final Connection readCon, final Connection writeCon) throws OXException {
        super();
        this.session = session;
        if (session instanceof ServerSession) {
            final ServerSession serverSession = (ServerSession) session;
            ctx = serverSession.getContext();
            userPerms = serverSession.getUserPermissionBits();
            user = serverSession.getUser();
        } else {
            ctx = ContextStorage.getStorageContext(session.getContextId());
            userPerms = UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), ctx);
            user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
        }
        this.readCon = readCon;
        this.writeCon = writeCon;
        this.oxfolderAccess = oxfolderAccess;
        final AppointmentSqlFactoryService factory = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
        if (factory != null) {
            this.cSql = factory.createAppointmentSql(session);
        } else {
            this.cSql = null;
        }
        warnings = new LinkedList<OXException>();
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
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
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TITLE, "", Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsParentFolderID()) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderChildFields.FOLDER_ID,
                "",
                Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsModule()) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.MODULE, "", Integer.valueOf(ctx.getContextId()));
        }
        if (!folderObj.containsType()) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TYPE, "", Integer.valueOf(ctx.getContextId()));
        } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderObj.getParentFolderID()) {
            folderObj.setType(FolderObject.PUBLIC);
        }
        if (folderObj.getPermissions() == null || folderObj.getPermissions().size() == 0) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.PERMISSIONS,
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
                final EffectivePermission p = parentFolder.getEffectiveUserPermission(user.getId(), userPerms, readCon);
                if (!p.canCreateSubfolders()) {
                    final OXException fe = OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(parentFolder),
                        Integer.valueOf(ctx.getContextId()));
                    if (p.getUnderlyingPermission().canCreateSubfolders()) {
                        fe.setCategory(CATEGORY_PERMISSION_DENIED);
                    }
                    throw fe;
                }
                if (!userPerms.hasModuleAccess(folderObj.getModule())) {
                    throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(CATEGORY_PERMISSION_DENIED, OXFolderUtility.getUserName(
                        user.getId(),
                        ctx), OXFolderUtility.folderModule2String(folderObj.getModule()), Integer.valueOf(ctx.getContextId()));
                }
                if ((parentFolder.getType() == FolderObject.PUBLIC) && !userPerms.hasFullPublicFolderAccess() && (folderObj.getModule() != FolderObject.INFOSTORE)) {
                    throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(parentFolder),
                        Integer.valueOf(ctx.getContextId()));
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Check folder types
         */
        if (!OXFolderUtility.checkFolderTypeAgainstParentType(parentFolder, folderObj.getType())) {
            throw OXFolderExceptionCode.INVALID_TYPE.create(OXFolderUtility.getFolderName(parentFolder),
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
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(OXFolderUtility.folderModule2String(folderObj.getModule()),
                Integer.valueOf(ctx.getContextId()));
        }
        if (!OXFolderUtility.checkFolderModuleAgainstParentModule(
            parentFolder.getObjectID(),
            parentFolder.getModule(),
            folderObj.getModule(),
            ctx.getContextId())) {
            throw OXFolderExceptionCode.INVALID_MODULE.create(OXFolderUtility.getFolderName(parentFolder),
                OXFolderUtility.folderModule2String(folderObj.getModule()),
                Integer.valueOf(ctx.getContextId()));
        }
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(folderObj, userPerms, ctx);
        /*
         * Check if admin exists and permission structure
         */
        OXFolderUtility.checkFolderPermissions(folderObj, user.getId(), ctx, warnings);
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
            final int parentFolderID = folderObj.getParentFolderID();
            final String folderName = folderObj.getFolderName();
            boolean throwException = false;

            if (parentFolderID != 1) {
                if (OXFolderSQL.lookUpFolder(parentFolderID, folderName, folderObj.getModule(), readCon, ctx) != -1) {
                    /*
                     * A duplicate folder exists
                     */
                    throwException = true;
                }
            } else {
                final TIntList folders = OXFolderSQL.lookUpFolders(parentFolderID, folderName, folderObj.getModule(), readCon, ctx);
                /*
                 * Check if the user is owner of one of these folders. In this case throw a duplicate folder exception
                 */
                for (final int fuid : folders.toArray()) {
                    final FolderObject toCheck = getOXFolderAccess().getFolderObject(fuid);
                    if (toCheck.getCreatedBy() == (folderObj.containsCreatedBy() ? folderObj.getCreatedBy() : user.getId())) {
                        /*
                         * User is already owner of a private folder with the same name located below system's private folder
                         */
                        throwException = true;
                        break;
                    }
                }
            }

            if (throwException) {
                throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(OXFolderUtility.getFolderName(parentFolder),
                    Integer.valueOf(ctx.getContextId()), folderName);
            }

            OXFolderUtility.checki18nString(parentFolderID, folderName, user.getLocale(), ctx);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * This folder shall be shared to other users
         */
        if (folderObj.getType() == FolderObject.PRIVATE && folderObj.getPermissions().size() > 1) {
            final TIntSet diff = OXFolderUtility.getShareUsers(null, folderObj.getPermissions(), user.getId(), ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final TIntCollection fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    final int length = fuids.size();
                    allSharedFolders = new FolderObject[length];
                    final TIntIterator iter = fuids.iterator();
                    for (int i = 0; i < length; i++) {
                        allSharedFolders[i] = getOXFolderAccess().getFolderObject(iter.next());
                    }
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
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
        int fuid = generateFolderID();

        /*
         * Call SQL insert
         */
        try {
            OXFolderSQL.insertFolderSQL(fuid, user.getId(), folderObj, createTime, ctx, writeCon);
            folderObj.setObjectID(fuid);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Update cache with writable connection!
         */
        final Date creatingDate = new Date(createTime);
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
        {
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            Connection wc = writeCon;
            final boolean create = (wc == null);
            try {
                if (create) {
                    wc = DBPool.pickupWriteable(ctx);
                }
                if (FolderCacheManager.isInitialized()) {
                    final FolderCacheManager manager = FolderCacheManager.getInstance();
                    manager.removeFolderObject(parentFolder.getObjectID(), ctx);
                    // manager.putFolderObject(parentFolder, ctx);
                    folderObj.fill(manager.getFolderObject(fuid, false, ctx, wc));
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
                    if (FolderObject.INFOSTORE == folderObj.getModule()) {
                        new EventClient(session).create(folderObj, parentFolder, getFolderPath(folderObj, folderObj, wc));
                    } else {
                        new EventClient(session).create(folderObj, parentFolder);
                    }
                } catch (final OXException e) {
                    LOG.warn("Create event could not be enqueued", e);
                }
                return folderObj;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterAfterReading(ctx, wc);
                    wc = null;
                }
            }
        }
    }

    private int generateFolderID() throws OXException {
        int fuid = -1;
        boolean created = false;
        boolean transactionStarted = false;
        Connection wc = writeCon;
        if (wc == null) {
            wc = DBPool.pickupWriteable(ctx);
            created = true;
        }

        try {
            if (created) {
                Databases.startTransaction(wc);
            } else if (wc.getAutoCommit()) {
                Databases.startTransaction(wc);
                transactionStarted = true;
            }

            fuid = IDGenerator.getId(ctx, Types.FOLDER, wc);

            if (created || transactionStarted) {
                wc.commit();
            }
        } catch (final SQLException e) {
            if (created) {
                Databases.rollback(wc);
            }
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (created) {
                Databases.autocommit(wc);
                DBPool.closeWriterSilent(ctx, wc);
            } else if (transactionStarted) {
                Databases.autocommit(wc);
            }
        }

        if (fuid < FolderObject.MIN_FOLDER_ID) {
            throw OXFolderExceptionCode.INVALID_SEQUENCE_ID.create(Integer.valueOf(fuid),
                Integer.valueOf(FolderObject.MIN_FOLDER_ID),
                Integer.valueOf(ctx.getContextId()));
        }

        return fuid;
    }

    @Override
    public FolderObject updateFolder(final FolderObject fo, final boolean checkPermissions, final boolean handDown, final long lastModified) throws OXException {
        return updateFolder(fo, checkPermissions, handDown, lastModified, OPTION_NONE);
    }

    private FolderObject updateFolder(final FolderObject fo, final boolean checkPermissions, final boolean handDown, final long lastModified, int options) throws OXException {
        if (checkPermissions) {
            if (fo.containsType() && fo.getType() == FolderObject.PUBLIC &&
                fo.getModule() != FolderObject.INFOSTORE && !userPerms.hasFullPublicFolderAccess()) {
                throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(
                    getUserName(session, user), getFolderName(fo), Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Fetch effective permission from storage
             */
            final EffectivePermission perm = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userPerms);
            if (!perm.isFolderVisible() || !perm.getUnderlyingPermission().isFolderVisible()) {
                throw OXFolderExceptionCode.NOT_VISIBLE.create(
                    Integer.valueOf(fo.getObjectID()), getUserName(session, user), Integer.valueOf(ctx.getContextId()));
            }
            if (!perm.isFolderAdmin() || !perm.getUnderlyingPermission().isFolderAdmin()) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                    getUserName(session, user), getFolderName(fo), Integer.valueOf(ctx.getContextId()));
            }
            if (fo.getObjectID() == getPublishedMailAttachmentsFolder(session)) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                    getUserName(session, user), getFolderName(fo), Integer.valueOf(ctx.getContextId()));
            }
        }
        final boolean performMove = fo.containsParentFolderID();
        FolderObject originalFolder = getFolderFromMaster(fo.getObjectID());
        int oldParentId = originalFolder.getParentFolderID();
        FolderObject storageObject = originalFolder.clone();
        if (fo.containsPermissions() || fo.containsModule() || fo.containsMeta()) {
            final int newParentFolderID = fo.getParentFolderID();
            if (performMove && newParentFolderID > 0 && newParentFolderID != storageObject.getParentFolderID()) {
                move(fo.getObjectID(), newParentFolderID, fo.getCreatedBy(), fo.getFolderName(), storageObject, lastModified);
                // Reload storage's folder for following update
                storageObject = getFolderFromMaster(fo.getObjectID());
            }
            update(fo, options, storageObject, lastModified, handDown);
        } else if (fo.containsFolderName()) {
            final int newParentFolderID = fo.getParentFolderID();
            if (performMove && newParentFolderID > 0 && newParentFolderID != storageObject.getParentFolderID()) {
                move(fo.getObjectID(), newParentFolderID, fo.getCreatedBy(), fo.getFolderName(), storageObject, lastModified);
            } else {
                rename(fo, storageObject, lastModified);
            }
        } else if (performMove) {
            /*
             * Perform move
             */
            move(fo.getObjectID(), fo.getParentFolderID(), fo.getCreatedBy(), null, storageObject, lastModified);
        }
        /*
         * Finally update cache
         */
        {
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            Connection wc = writeCon;
            final boolean create = (wc == null);
            try {
                if (create) {
                    wc = DBPool.pickupWriteable(ctx);
                }
                if (FolderCacheManager.isEnabled()) {
                    final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
                    cacheManager.removeFolderObject(fo.getObjectID(), ctx);
                    fo.fill(cacheManager.getFolderObject(fo.getObjectID(), false, ctx, wc));
                    final int parentFolderID = fo.getParentFolderID();
                    if (parentFolderID > 0) {
                        /*
                         * Update parent, too
                         */
                        cacheManager.removeFolderObject(parentFolderID, ctx);
                        cacheManager.loadFolderObject(parentFolderID, ctx, wc);
                    }
                    if (0 < oldParentId && oldParentId != parentFolderID) {
                        /*
                         * Update old parent, too
                         */
                        cacheManager.removeFolderObject(oldParentId, ctx);
                        cacheManager.loadFolderObject(oldParentId, ctx, wc);
                    }
                } else {
                    fo.fill(FolderObject.loadFolderObjectFromDB(fo.getObjectID(), ctx, wc));
                }
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
                }
                if (CalendarCache.isInitialized()) {
                    CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                }
                if (FolderObject.SYSTEM_MODULE != fo.getModule()) {
                    try {
                        FolderObject newParentFolder = FolderObject.loadFolderObjectFromDB(fo.getParentFolderID(), ctx, wc, true, false);
                        if (FolderObject.INFOSTORE == fo.getModule()) {
                            new EventClient(session).modify(originalFolder, fo, newParentFolder, getFolderPath(fo, newParentFolder, wc));
                        } else {
                            new EventClient(session).modify(originalFolder, fo, newParentFolder);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Update event could not be enqueued", e);
                    }
                }
                return fo;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterAfterReading(ctx, wc);
                    wc = null;
                }
            }
        }
    }

    protected void update(final FolderObject fo, final int options, final FolderObject storageObj, final long lastModified, final boolean handDown) throws OXException {
        if (fo.getObjectID() <= 0) {
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(OXFolderUtility.getFolderName(fo));
        }
        /*
         * Get storage version (and thus implicitly check existence)
         */
        final boolean containsPermissions = fo.containsPermissions();
        if (fo.getPermissions() == null || fo.getPermissions().isEmpty()) {
            if (containsPermissions) {
                /*
                 * Deny to set empty permissions
                 */
                throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.PERMISSIONS,
                    OXFolderUtility.getFolderName(fo),
                    Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Pass storage's permissions
             */
            fo.setPermissionsAsArray(storageObj.getPermissionsAsArray());
        }
        /*
         * Check if a move is done here
         */
        if (fo.containsParentFolderID() && fo.getParentFolderID() > 0 && storageObj.getParentFolderID() != fo.getParentFolderID()) {
            throw OXFolderExceptionCode.NO_MOVE_THROUGH_UPDATE.create(OXFolderUtility.getFolderName(fo));
        }
        /*
         * Check folder name
         */
        if (fo.containsFolderName()) {
            if (fo.getFolderName() == null || fo.getFolderName().trim().length() == 0) {
                throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TITLE,
                    OXFolderUtility.getFolderName(fo),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageObj.isDefaultFolder() && !fo.getFolderName().equals(storageObj.getFolderName())) {
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_RENAME.create(OXFolderUtility.getFolderName(fo),
                    Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check if folder module is supposed to be updated
         */
        if (fo.containsModule() && fo.getModule() != storageObj.getModule() && FolderObject.SYSTEM_MODULE != storageObj.getModule()) {
            /*
             * Module update only allowed if known and folder is empty
             */
            if (!isKnownModule(fo.getModule())) {
                throw OXFolderExceptionCode.UNKNOWN_MODULE.create(OXFolderUtility.folderModule2String(fo.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (storageObj.isDefaultFolder()) {
                /*
                 * A default folder's module must not be changed
                 */
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_MODULE_UPDATE.create();
            } else if (!isFolderEmpty(storageObj.getObjectID(), storageObj.getModule())) {
                /*
                 * Module cannot be updated since folder already contains elements
                 */
                throw OXFolderExceptionCode.DENY_FOLDER_MODULE_UPDATE.create();
            } else if ((options & OPTION_DENY_MODULE_UPDATE) > 0) {
                /*
                 * Folder module must not be updated
                 */
                throw OXFolderExceptionCode.NO_FOLDER_MODULE_UPDATE.create();
            }
            final FolderObject parent = getFolderFromMaster(storageObj.getParentFolderID());
            if (!OXFolderUtility.checkFolderModuleAgainstParentModule(
                parent.getObjectID(),
                parent.getModule(),
                fo.getModule(),
                ctx.getContextId())) {
                throw OXFolderExceptionCode.INVALID_MODULE.create(OXFolderUtility.getFolderName(parent),
                    OXFolderUtility.folderModule2String(fo.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
        } else {
            fo.setModule(storageObj.getModule());
        }
        /*
         * Check if shared
         */
        if (storageObj.isShared(user.getId())) {
            throw OXFolderExceptionCode.NO_SHARED_FOLDER_UPDATE.create(OXFolderUtility.getFolderName(fo),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check Permissions
         */
        fo.setType(storageObj.getType());
        if (options != OPTION_OVERRIDE_CREATED_BY) {
            fo.setCreatedBy(storageObj.getCreatedBy());
        }
        fo.setDefaultFolder(storageObj.isDefaultFolder());
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(fo, userPerms, ctx);
        OXFolderUtility.checkFolderPermissions(fo, user.getId(), ctx, warnings);
        OXFolderUtility.checkPermissionsAgainstUserConfigs(readCon, fo, ctx);
        OXFolderUtility.checkSystemFolderPermissions(fo.getObjectID(), fo.getNonSystemPermissionsAsArray(), user, ctx);
        if (FolderObject.PUBLIC == fo.getType()) {
            {
                final OCLPermission[] removedPerms = OXFolderUtility.getPermissionsWithoutFolderAccess(
                    fo.getNonSystemPermissionsAsArray(),
                    storageObj.getNonSystemPermissionsAsArray());
                if (removedPerms.length > 0) {
                    new CheckPermissionOnRemove(session, writeCon, ctx).checkPermissionsOnUpdate(
                        fo.getObjectID(),
                        removedPerms,
                        lastModified);
                }
            }
            new CheckPermissionOnInsert(session, writeCon, ctx).checkParentPermissions(
                storageObj.getParentFolderID(),
                fo.getNonSystemPermissionsAsArray(),
                lastModified);
        }
        boolean rename = false;
        if (fo.containsFolderName() && !storageObj.getFolderName().equals(fo.getFolderName())) {
            rename = true;
            /*
             * Check for invalid characters
             */
            OXFolderUtility.checkFolderStringData(fo);
            /*
             * Rename: Check if duplicate folder exists
             */
            try {
                final String folderName = fo.getFolderName();
                final int folderId = OXFolderSQL.lookUpFolderOnUpdate(
                    fo.getObjectID(),
                    storageObj.getParentFolderID(),
                    folderName,
                    fo.getModule(),
                    readCon,
                    ctx);
                if (folderId != -1 && folderId != fo.getObjectID()) {
                    /*
                     * A duplicate folder exists
                     */
                    throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(OXFolderUtility.getFolderName(new OXFolderAccess(
                        readCon,
                        ctx).getFolderObject(storageObj.getParentFolderID())), Integer.valueOf(ctx.getContextId()), folderName);
                }
                /*
                 * Check i18n strings, too
                 */
                final int parentFolderId = storageObj.getParentFolderID();
                OXFolderUtility.checki18nString(parentFolderId, folderName, user.getLocale(), ctx);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * This folder shall be shared to other users
         */
        if (fo.getType() == FolderObject.PRIVATE && fo.getPermissions().size() > 1) {
            final TIntSet diff = OXFolderUtility.getShareUsers(
                rename ? null : storageObj.getPermissions(),
                fo.getPermissions(),
                user.getId(),
                ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final TIntCollection fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    final int size = fuids.size();
                    allSharedFolders = new FolderObject[size];
                    final TIntIterator iter = fuids.iterator();
                    for (int i = 0; i < size; i++) {
                        /*
                         * Remove currently updated folder
                         */
                        final int fuid = iter.next();
                        if (fuid != fo.getObjectID()) {
                            allSharedFolders[i] = getOXFolderAccess().getFolderObject(fuid);
                        }
                    }
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, fo, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(
                    diff,
                    allSharedFolders,
                    rename ? fo.getFolderName() : storageObj.getFolderName(),
                    ctx);
            }
        }
        /*
         * Check duplicate permissions
         */
        OXFolderUtility.checkForDuplicateNonSystemPermissions(fo, ctx);
        /*
         * Call SQL update
         */
        try {
            OXFolderSQL.updateFolderSQL(user.getId(), fo, lastModified, ctx, writeCon);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, fo, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        if (handDown) {
            /*
             * Check if permissions are supposed to be handed down to subfolders
             */
            try {
                if (containsPermissions) {
                    final List<OCLPermission> permissions = fo.getPermissions();
                    if (permissions != null && !permissions.isEmpty()) {
                        handDown(fo.getObjectID(), options, permissions, lastModified, FolderCacheManager.isEnabled() ? FolderCacheManager.getInstance() : null);
                    }
                }
            } catch (final DataTruncation e) {
                throw parseTruncated(e, fo, TABLE_OXFOLDER_TREE);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } catch (final ProcedureFailedException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                if (cause instanceof SQLException) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(cause, cause.getMessage());
                }
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(cause, cause.getMessage());
            }
        }
    }

    protected void handDown(final int folderId, final int options, final List<OCLPermission> permissions, final long lastModified, final FolderCacheManager cacheManager) throws OXException, SQLException {
        final Context ctx = this.ctx;
        final TIntList subfolders = OXFolderSQL.getSubfolderIDs(folderId, writeCon, ctx);
        if (!subfolders.isEmpty()) {
            final Session session = this.session;
            subfolders.forEach(new TIntProcedure() {

                @Override
                public boolean execute(final int subfolderId) {
                    try {
                        final FolderObject tmp = new FolderObject(subfolderId);
                        tmp.setPermissions(permissions);
                        update(tmp, options, getFolderFromMaster(subfolderId), lastModified, true);
                        if (null != cacheManager) {
                            cacheManager.removeFolderObject(subfolderId, ctx);
                        }
                        CacheFolderStorage.getInstance().removeFromCache(Integer.toString(subfolderId), FolderStorage.REAL_TREE_ID, true, session);
                        handDown(subfolderId, options, permissions, lastModified, cacheManager);
                        return true;
                    } catch (final OXException e) {
                        throw new ProcedureFailedException(e);
                    } catch (final SQLException e) {
                        throw new ProcedureFailedException(e);
                    } catch (final RuntimeException e) {
                        throw new ProcedureFailedException(e);
                    }
                }
            });
        }
    }

    protected FolderObject getFolderFromMaster(final int folderId) throws OXException {
        return getFolderFromMaster(folderId, false);
    }

    private FolderObject getFolderFromMaster(final int folderId, final boolean withSubfolders) throws OXException {
        // Use writable connection to ensure to fetch from master database
        Connection wc = writeCon;
        if (wc != null) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
        }

        // Fetch new writable connection
        wc = DBPool.pickupWriteable(ctx);
        try {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
        } finally {
            DBPool.closeWriterAfterReading(ctx, wc);
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
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
            }
            try {
                return calSql.isFolderEmpty(user.getId(), folderId, readCon);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        } else if (module == FolderObject.CONTACT) {
            ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
            return contactService.isFolderEmpty(session, String.valueOf(folderId));
        } else if (module == FolderObject.INFOSTORE) {
            final InfostoreFacade db = new InfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
            return db.isFolderEmpty(folderId, ctx);
        } else if (module == FolderObject.SYSTEM_MODULE) {
            return true;
        } else {
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(OXFolderUtility.folderModule2String(module),
                Integer.valueOf(ctx.getContextId()));
        }
    }

    private static boolean isKnownModule(final int module) {
        return ((module == FolderObject.TASK) || (module == FolderObject.CALENDAR) || (module == FolderObject.CONTACT) || (module == FolderObject.INFOSTORE));
    }

    private void rename(final FolderObject folderObj, final FolderObject storageObj, final long lastModified) throws OXException {
        if (folderObj.getObjectID() <= 0) {
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(OXFolderUtility.getFolderName(folderObj));
        } else if (!folderObj.containsFolderName() || folderObj.getFolderName() == null || folderObj.getFolderName().trim().length() == 0) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TITLE, "", Integer.valueOf(ctx.getContextId()));
        }
        OXFolderUtility.checkFolderStringData(folderObj);
        /*
         * Check if rename can be avoided (cause new name equals old one) and prevent default folder rename
         */
        if (storageObj.getFolderName().equals(folderObj.getFolderName())) {
            return;
        } else if (storageObj.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_RENAME.create(OXFolderUtility.getFolderName(folderObj),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check for duplicate folder
         */
        try {
            final String folderName = folderObj.getFolderName();
            final int parentFolderID = storageObj.getParentFolderID();
            final int folderId = folderObj.getObjectID();
            boolean throwException = false;

            /*
             * When the private folder is parent, we have to check if folders with the same name in the same module can be seen by the user.
             */
            if (parentFolderID != 1) {
                if (OXFolderSQL.lookUpFolderOnUpdate(folderId, parentFolderID, folderName, storageObj.getModule(), readCon, ctx) != -1) {
                    /*
                     * A duplicate folder exists
                     */
                    throwException = true;
                }
            } else {
                final TIntList folders = OXFolderSQL.lookUpFolders(parentFolderID, folderName, storageObj.getModule(), readCon, ctx);
                /*
                 * Check if the user is owner of one of these folders. In this case throw a duplicate folder exception
                 */
                for (final int fuid : folders.toArray()) {
                    final FolderObject toCheck = getOXFolderAccess().getFolderObject(fuid);
                    if (toCheck.getCreatedBy() == (folderObj.containsCreatedBy() ? folderObj.getCreatedBy() : user.getId())) {
                        /*
                         * User is already owner of a private folder with the same name located below system's private folder
                         */
                        throwException = true;
                        break;
                    }
                }
            }

            if (throwException) {
                throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(OXFolderUtility.getFolderName(new OXFolderAccess(readCon, ctx).getFolderObject(storageObj.getParentFolderID())),
                    Integer.valueOf(ctx.getContextId()), folderName);
            }

            /*
             * Check i18n strings, too
             */
            OXFolderUtility.checki18nString(parentFolderID, folderName, user.getLocale(), ctx);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * This folder shall be shared to other users
         */
        if (storageObj.getType() == FolderObject.PRIVATE && storageObj.getPermissions().size() > 1) {
            final TIntSet diff = OXFolderUtility.getShareUsers(null, storageObj.getPermissions(), user.getId(), ctx);
            if (!diff.isEmpty()) {
                final FolderObject[] allSharedFolders;
                try {
                    /*
                     * Check duplicate folder names
                     */
                    final TIntCollection fuids = OXFolderSQL.getSharedFoldersOf(user.getId(), readCon, ctx);
                    final int size = fuids.size();
                    allSharedFolders = new FolderObject[size];
                    final TIntIterator iter = fuids.iterator();
                    for (int i = 0; i < size; i++) {
                        /*
                         * Remove currently renamed folder
                         */
                        final int fuid = iter.next();
                        if (fuid != folderObj.getObjectID()) {
                            allSharedFolders[i] = getOXFolderAccess().getFolderObject(fuid);
                        }
                    }
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
                } catch (final SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(diff, allSharedFolders, folderObj.getFolderName(), ctx);
            }
        }
        /*
         * Call SQL rename
         */
        try {
            OXFolderSQL.renameFolderSQL(user.getId(), folderObj, lastModified, ctx, writeCon);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private final int[] SYSTEM_PUBLIC_FOLDERS = { FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    private static boolean isInArray(final int key, final int[] a) {
        Arrays.sort(a);
        return Arrays.binarySearch(a, key) >= 0;
    }

    private void move(final int folderId, final int targetFolderId, final int createdBy, String newName, final FolderObject storageSrc, final long lastModified) throws OXException {
        /*
         * Folder is already in target folder and does not need to be renamed
         */
        if (storageSrc.getParentFolderID() == targetFolderId && (null == newName || newName.equals(storageSrc.getFolderName()))) {
            return;
        }
        /*
         * Default folder must not be moved
         */
        if (storageSrc.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
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
            final int parentFolderID = storageDest.getObjectID();
            final String folderName = null == newName ? storageSrc.getFolderName() : newName;
            boolean throwException = false;

            /*
             * When the private folder is parent, we have to check if folders with the same name in the same module can be seen by the user.
             */
            if (parentFolderID != 1) {
                if (OXFolderSQL.lookUpFolderOnUpdate(folderId, parentFolderID, folderName, storageSrc.getModule(), readCon, ctx) != -1) {
                    /*
                     * A duplicate folder exists
                     */
                    throwException = true;
                }
            } else {
                final TIntList folders = OXFolderSQL.lookUpFolders(parentFolderID, folderName, storageSrc.getModule(), readCon, ctx);
                /*
                 * Check if the user is owner of one of these folders. In this case throw a duplicate folder exception
                 */
                for (final int fuid : folders.toArray()) {
                    final FolderObject toCheck = getOXFolderAccess().getFolderObject(fuid);
                    if (toCheck.getCreatedBy() == (createdBy > 0 ? createdBy : user.getId())) {
                        /*
                         * User is already owner of a private folder with the same name located below system's private folder
                         */
                        throwException = true;
                        break;
                    }
                }
            }

            if (throwException) {
                throw OXFolderExceptionCode.NO_DUPLICATE_FOLDER.create(OXFolderUtility.getFolderName(storageDest),
                    Integer.valueOf(ctx.getContextId()), folderName);
            }

            /*
             * Check i18n strings, too
             */
            OXFolderUtility.checki18nString(targetFolderId, folderName, user.getLocale(), ctx);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Check a bunch of possible errors
         */
        try {
            if (storageSrc.isShared(user.getId())) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageDest.isShared(user.getId())) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_TARGET.create(OXFolderUtility.getFolderName(storageDest),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.SYSTEM_TYPE) {
                throw OXFolderExceptionCode.NO_SYSTEM_FOLDER_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PRIVATE && ((storageDest.getType() == FolderObject.PUBLIC || (storageDest.getType() == FolderObject.SYSTEM_TYPE && targetFolderId != FolderObject.SYSTEM_PRIVATE_FOLDER_ID)))) {
                throw OXFolderExceptionCode.ONLY_PRIVATE_TO_PRIVATE_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PUBLIC && ((storageDest.getType() == FolderObject.PRIVATE || (storageDest.getType() == FolderObject.SYSTEM_TYPE && !isInArray(
                targetFolderId,
                SYSTEM_PUBLIC_FOLDERS))))) {
                throw OXFolderExceptionCode.ONLY_PUBLIC_TO_PUBLIC_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
                    Integer.valueOf(ctx.getContextId()));
            } else if (storageSrc.getModule() == FolderObject.INFOSTORE && storageDest.getModule() != FolderObject.INFOSTORE && targetFolderId != FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                throw OXFolderExceptionCode.INCOMPATIBLE_MODULES.create(OXFolderUtility.folderModule2String(storageSrc.getModule()),
                    OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageSrc.getModule() != FolderObject.INFOSTORE && storageDest.getModule() == FolderObject.INFOSTORE) {
                throw OXFolderExceptionCode.INCOMPATIBLE_MODULES.create(OXFolderUtility.folderModule2String(storageSrc.getModule()),
                    OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageDest.getEffectiveUserPermission(user.getId(), userPerms).getFolderPermission() < OCLPermission.CREATE_SUB_FOLDERS) {
                throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(OXFolderUtility.getUserName(
                    user.getId(),
                    ctx), OXFolderUtility.getFolderName(storageDest), Integer.valueOf(ctx.getContextId()));
            } else if (folderId == targetFolderId) {
                throw OXFolderExceptionCode.NO_EQUAL_MOVE.create(Integer.valueOf(ctx.getContextId()));
            }
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if source folder has subfolders
         */
        try {
            if (storageSrc.hasSubfolders()) {
                /*
                 * Check if target is a descendant folder
                 */
                final TIntList parentIDList = new TIntArrayList(1);
                parentIDList.add(storageSrc.getObjectID());
                if (OXFolderUtility.isDescendentFolder(parentIDList, targetFolderId, readCon, ctx)) {
                    throw OXFolderExceptionCode.NO_SUBFOLDER_MOVE.create(OXFolderUtility.getFolderName(storageSrc),
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
                    throw OXFolderExceptionCode.NO_SUBFOLDER_MOVE_ACCESS.create(OXFolderUtility.getUserName(session, user),
                        OXFolderUtility.getFolderName(storageSrc),
                        Integer.valueOf(ctx.getContextId()));
                }
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * First treat as a delete prior to actual move
         */
        try {
            processDeletedFolderThroughMove(storageSrc, new CheckPermissionOnRemove(session, writeCon, ctx), lastModified);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Call SQL move
         */
        try {
            storageSrc.setFolderName(newName);
            OXFolderSQL.moveFolderSQL(user.getId(), storageSrc, storageDest, lastModified, ctx, readCon, writeCon);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, storageSrc, TABLE_OXFOLDER_TREE);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Now treat as an insert after actual move if not moved below trash
         */
        if (FolderObject.TRASH != storageDest.getType()) {
            try {
                processInsertedFolderThroughMove(
                    getFolderFromMaster(folderId), new CheckPermissionOnInsert(session, writeCon, ctx), lastModified);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Inherit folder type recursively if move from or to trash folder
         */
        if (FolderObject.TRASH == storageDest.getType() && FolderObject.TRASH != storageSrc.getType() ||
            FolderObject.TRASH != storageDest.getType() && FolderObject.TRASH == storageSrc.getType()) {
            try {
                List<Integer> folderIDs;
                if (false == storageSrc.hasSubfolders()) {
                    folderIDs = Collections.singletonList(Integer.valueOf(folderId));
                } else {
                    folderIDs = new ArrayList<Integer>();
                    folderIDs.add(Integer.valueOf(folderId));
                    folderIDs.addAll(OXFolderSQL.getSubfolderIDs(folderId, readCon, ctx, true));
                }
                OXFolderSQL.updateFolderType(writeCon, ctx, storageDest.getType(), folderIDs);
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Update last-modified time stamps
         */
        try {
            OXFolderSQL.updateLastModified(storageSrc.getParentFolderID(), lastModified, user.getId(), writeCon, ctx);
            OXFolderSQL.updateLastModified(storageSrc.getObjectID(), lastModified, user.getId(), writeCon, ctx);
            OXFolderSQL.updateLastModified(storageDest.getObjectID(), lastModified, user.getId(), writeCon, ctx);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Update OLD parent in cache, cause this can only be done here
         */
        ConditionTreeMapManagement.dropFor(ctx.getContextId());
        if (FolderCacheManager.isEnabled()) {
            Connection wc = writeCon;
            final boolean create = (wc == null);
            if (create) {
                wc = DBPool.pickupWriteable(ctx);
            }
            try {
                final int srcParentId = storageSrc.getParentFolderID();
                if (srcParentId > 0) {
                    FolderCacheManager.getInstance().loadFolderObject(srcParentId, ctx, wc);
                }
                final int destParentId = storageDest.getParentFolderID();
                if (destParentId > 0) {
                    FolderCacheManager.getInstance().loadFolderObject(destParentId, ctx, wc);
                }
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
        }
    }

    private void processDeletedFolderThroughMove(final FolderObject folder, final CheckPermissionOnRemove checker, final long lastModified) throws OXException, SQLException, OXException {
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

    private void processInsertedFolderThroughMove(final FolderObject folder, final CheckPermissionOnInsert checker, final long lastModified) throws OXException, SQLException, OXException {
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
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(OXFolderUtility.getFolderName(fo));
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
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        if (checkPermissions) {
            /*
             * Check permissions
             */
            final EffectivePermission p = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userPerms);
            if (!p.isFolderVisible()) {
                if (p.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(fo.getObjectID()),
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED,
                    Integer.valueOf(fo.getObjectID()),
                    OXFolderUtility.getUserName(user.getId(), ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(fo, session, ctx)) {
            throw OXFolderExceptionCode.NOT_ALL_OBJECTS_DELETION.create(OXFolderUtility.getUserName(user.getId(), ctx),
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
        default:
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(Integer.valueOf(module), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * delete subfolders, too, when clearing the trash folder
         */
        if (FolderObject.TRASH == fo.getType()) {
            /*
             * Gather all deletable subfolders recursively
             */
            TIntObjectMap<TIntObjectMap<?>> deleteableFolders = new TIntObjectHashMap<TIntObjectMap<?>>();
            try {
                TIntList subfolders = OXFolderSQL.getSubfolderIDs(fo.getObjectID(), readCon, ctx);
                for (int i = 0; i < subfolders.size(); i++) {
                    deleteableFolders.putAll(gatherDeleteableFolders(
                        subfolders.get(i), user.getId(), userPerms, StringCollection.getSqlInString(user.getId(), user.getGroups())));
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            /*
             * Delete subfolders
             */
            if (0 < deleteableFolders.size()) {
                deleteValidatedFolders(deleteableFolders, lastModified, fo.getType());
            }
        }
        return fo;
    }

    @Override
    public FolderObject deleteFolder(final FolderObject fo, final boolean checkPermissions, final long lastModified, boolean hardDelete) throws OXException {
        final int folderId = fo.getObjectID();
        if (folderId <= 0) {
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(OXFolderUtility.getFolderName(fo));
        }
        if (folderId < FolderObject.MIN_FOLDER_ID) {
            throw OXFolderExceptionCode.NO_SYSTEM_FOLDER_MOVE.create(OXFolderUtility.getFolderName(fo), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * reload original folder
         */
        OXFolderAccess folderAccess = getOXFolderAccess();
        FolderObject folder = folderAccess.getFolderObject(folderId);
        if (checkPermissions) {
            /*
             * Check permissions
             */
            final EffectivePermission p = folder.getEffectiveUserPermission(user.getId(), userPerms);
            if (!p.isFolderVisible()) {
                if (p.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderId),
                        OXFolderUtility.getUserName(user.getId(), ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED,
                    Integer.valueOf(folderId),
                    OXFolderUtility.getUserName(user.getId(), ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!p.isFolderAdmin()) {
                if (!p.getUnderlyingPermission().isFolderAdmin()) {
                    throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(OXFolderUtility.getUserName(user.getId(), ctx),
                        OXFolderUtility.getFolderName(folder),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(CATEGORY_PERMISSION_DENIED, OXFolderUtility.getUserName(
                    user.getId(),
                    ctx), OXFolderUtility.getFolderName(folder), Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * get parent
         */
        final FolderObject parentFolder = folderAccess.getFolderObject(folder.getParentFolderID());
        /*
         * check if folder can be soft-deleted
         */
        if (false == hardDelete) {
            FolderObject trashFolder = getTrashFolder(folder.getModule());
            if (null != trashFolder) {
                /*
                 * trash folder available, check if folder already located below trash
                 */
                int trashFolderID = trashFolder.getObjectID();
                boolean belowTrash;
                if (parentFolder.getObjectID() == trashFolderID || parentFolder.getParentFolderID() == trashFolderID) {
                    belowTrash = true;
                } else {
                    FolderObject p;
                    for (p = parentFolder; p.getParentFolderID() != trashFolderID && FolderObject.MIN_FOLDER_ID < p.getParentFolderID();
                        p = folderAccess.getFolderObject(p.getParentFolderID())) {
                        ;
                    }
                    belowTrash = p.getParentFolderID() == trashFolderID;
                }
                if (false == belowTrash) {
                    /*
                     * move to trash possible, append sequence number to folder name in case of conflicts
                     */
                    String name = folder.getFolderName();
                    try {
                        while (-1 != OXFolderSQL.lookUpFolderOnUpdate(folderId, trashFolderID, name, folder.getModule(), readCon, ctx)) {
                            name = incrementSequenceNumber(name);
                        }
                    } catch (SQLException e) {
                        throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                    }
                    /*
                     * remove any subscriptions and publications recursively
                     */
                    deleteSubscriptionsAndPublications(folder.getModule(), folderId, true);
                    /*
                     * perform move to trash
                     */
                    FolderObject toUpdate = new FolderObject(name, folderId, folder.getModule(), folder.getType(), user.getId());
                    toUpdate.setParentFolderID(trashFolderID);
                    toUpdate.setPermissions(trashFolder.getPermissions());
                    // when deleting a folder, the permissions should always be inherited from the parent trash folder
                    // in order to do so, "created by" is overridden intentionally here to not violate permission restrictions,
                    // and to prevent synthetic system permissions to get inserted implicitly
                    int options = user.getId() != folder.getCreatedBy() ? OPTION_OVERRIDE_CREATED_BY : OPTION_NONE;
                    return updateFolder(toUpdate, false, true, lastModified, options);
                }
            }
        }
        /*
         * perform hard delete of folder and all subfolders
         */
        final TIntObjectMap<TIntObjectMap<?>> deleteableFolders;
        try {
            deleteableFolders = gatherDeleteableFolders(
                folderId,
                user.getId(),
                userPerms,
                StringCollection.getSqlInString(user.getId(), user.getGroups()));
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Remember folder type
         */
        final int type = getOXFolderAccess().getFolderType(folderId);
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
        ConditionTreeMapManagement.dropFor(ctx.getContextId());
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
                final boolean hasSubfolders = !OXFolderSQL.getSubfolderIDs(parentFolder.getObjectID(), wc, ctx).isEmpty();
                OXFolderSQL.updateSubfolderFlag(parentFolder.getObjectID(), hasSubfolders, lastModified, wc, ctx);
                /*
                 * Update cache
                 */
                if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
                    FolderCacheManager.getInstance().loadFolderObject(parentFolder.getObjectID(), ctx, wc);
                }
                /*
                 * Load return value
                 */
                fo.fill(FolderObject.loadFolderObjectFromDB(
                    folderId,
                    ctx,
                    wc,
                    true,
                    false,
                    "del_oxfolder_tree",
                    "del_oxfolder_permissions"));
                return fo;
            } finally {
                if (create && wc != null) {
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Deletes the validated folders.
     *
     * @param deleteableIDs The gathered IDs of validated folders
     * @param lastModified The last-modified time stamp
     * @param type The folder type
     * @throws OXException If deletion fails for any folder
     */
    void deleteValidatedFolders(final TIntObjectMap<TIntObjectMap<?>> deleteableIDs, final long lastModified, final int type) throws OXException {
        final DeleteValidatedFoldersProcedure procedure = new DeleteValidatedFoldersProcedure(lastModified, type);
        if (!deleteableIDs.forEachEntry(procedure)) {
            final OXException error = procedure.error;
            if (null != error) {
                throw error;
            }
        }
    }

    private final class DeleteValidatedFoldersProcedure implements TIntObjectProcedure<TIntObjectMap<?>> {

        public OXException error;

        private final long lastModified;

        private final int type;

        public DeleteValidatedFoldersProcedure(final long lastModified, final int type) {
            super();
            this.lastModified = lastModified;
            this.type = type;
        }

        @Override
        public boolean execute(final int folderId, final TIntObjectMap<?> hashMap) {
            if (null == error) {
                try {
                    if (null != hashMap) {
                        final @SuppressWarnings("unchecked") TIntObjectMap<TIntObjectMap<?>> tmp = (TIntObjectMap<TIntObjectMap<?>>) hashMap;
                        deleteValidatedFolders(tmp, lastModified, type);
                    }
                    deleteValidatedFolder(folderId, lastModified, type, false);
                    /*
                     * Allow further executions
                     */
                    return true;
                } catch (final OXException e) {
                    error = e;
                    /*
                     * Deny further executions
                     */
                    return false;
                }
            }
            /*
             * Deny further executions
             */
            return false;
        }

    }

    /**
     * Deletes any existing subscriptions and publications for the supplied folder ID.
     *
     * @param module The module of the folder
     * @param folderID The folder ID
     * @param handDown <code>true</code> to also remove the subscriptions and publications of any nested subfolder, <code>false</code>,
     *                 otherwise
     * @return The number of removed subscriptions and publications
     * @throws OXException
     */
    private int deleteSubscriptionsAndPublications(int module, int folderID, boolean handDown) throws OXException {
        Connection wc = writeCon;
        final boolean create = (wc == null);
        try {
            if (create) {
                wc = DBPool.pickupWriteable(ctx);
            }
            return deleteSubscriptionsAndPublications(wc, module, folderID, handDown);
        } finally {
            if (create && wc != null) {
                DBPool.closeWriterSilent(ctx, wc);
            }
        }
    }

    /**
     * Deletes any existing subscriptions and publications for the supplied folder ID.
     *
     * @param con A "write" connection to the database
     * @param module The module of the folder
     * @param folderID The folder ID
     * @param handDown <code>true</code> to also remove the subscriptions and publications of any nested subfolder, <code>false</code>,
     *                 otherwise
     * @return The number of removed subscriptions and publications
     * @throws OXException
     */
    private int deleteSubscriptionsAndPublications(Connection con, int module, int folderID, boolean handDown) throws OXException {
        int updated = 0;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            /*
             * prepare clause for folder IDs
             */
            TIntList subfolderIDs = handDown ? OXFolderSQL.getSubfolderIDs(folderID, con, ctx) : null;
            String whereFolderID;
            if (null == subfolderIDs || 0 == subfolderIDs.size()) {
                whereFolderID = "=?;";
            } else {
                StringBuilder StringBuilder = new StringBuilder(" IN (?");
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    StringBuilder.append(",?");
                }
                StringBuilder.append(");");
                whereFolderID = StringBuilder.toString();
            }
            /*
             * delete publications
             */
            stmt1 = con.prepareStatement("DELETE FROM publications WHERE cid=? AND module=? AND entity" + whereFolderID);
            stmt1.setInt(1, ctx.getContextId());
            stmt1.setString(2, Module.getModuleString(module, folderID));
            stmt1.setInt(3, folderID);
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    stmt1.setInt(i + 4, subfolderIDs.get(i));
                }
            }
            /*
             * delete subscriptions
             */
            updated += stmt1.executeUpdate();
            stmt2 = con.prepareStatement("DELETE FROM subscriptions WHERE cid=? AND folder_id" + whereFolderID);
            stmt2.setInt(1, ctx.getContextId());
            stmt2.setInt(2, folderID);
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    stmt2.setInt(i + 3, subfolderIDs.get(i));
                }
            }
            updated += stmt2.executeUpdate();
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt1);
            DBUtils.closeSQLStuff(stmt2);
        }
        return updated;
    }

    /**
     * Increments or appends an initial sequence number to the supplied folder name to avoid conflicting names, e.g. the string
     * <code>test</code> will get enhanced to <code>test (1)</code>, while the string <code>test (3)</code> will get enhanced to
     * <code>test (4)</code>.
     *
     * @param folderName The name to increment the sequence number in
     * @return The name with an incremented or initially appended sequence number
     */
    private static String incrementSequenceNumber(final String folderName) {
        String name = folderName;
        Pattern regex = Pattern.compile("\\((\\d+)\\)\\z");
        Matcher matcher = regex.matcher(name);
        if (false == matcher.find()) {
            /*
             * append new initial sequence number
             */
            name += " (1)";
        } else if (0 == matcher.groupCount() || 0 < matcher.groupCount() && Strings.isEmpty(matcher.group(1))) {
            /*
             * append new initial sequence number
             */
            name = name.substring(0, matcher.start()) + " (1)";
        } else {
            /*
             * incremented existing sequence number
             */
            int number = 0;
            try {
                number = Integer.valueOf(matcher.group(1).trim()).intValue();
            } catch (NumberFormatException e) {
                // should not get here
            }
            name = name.substring(0, matcher.start()) + '(' + String.valueOf(1 + number) + ')';
        }
        return name;
    }

    /**
     * Gets the user's trash folder for the supplied module.
     *
     * @param module The module to get the trash folder for
     * @return The folder, or <code>null</code> if no trash folder was found
     * @throws OXException
     */
    private FolderObject getTrashFolder(int module) throws OXException {
        try {
            return getOXFolderAccess().getDefaultFolder(user.getId(), module, FolderObject.TRASH);
        } catch (OXException e) {
            if (false == OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.equals(e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Deletes the validated folder.
     *
     * @param folderID The folder ID
     * @param lastModified The last-modified time stamp
     * @param type The folder type
     * @throws OXException If deletion fails
     */
    @Override
    public void deleteValidatedFolder(final int folderID, final long lastModified, final int type, final boolean hardDelete) throws OXException {
        final FolderObject storageFolder;
        try {
            storageFolder = getFolderFromMaster(folderID, false);
        } catch (final OXException e) {
            if (!OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                throw e;
            }
            // Already deleted
            return;
        }
        if (hardDelete) {
            /*
             * Delete contained items
             */
            deleteContainedItems(folderID);
            /*
             * Call SQL delete
             */
            try {
                OXFolderSQL.delOXFolder(folderID, session.getUserId(), lastModified, true, false, ctx, writeCon);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        } else {
            /*
             * Iterate possibly listening folder delete listeners
             */
            for (final Iterator<FolderDeleteListenerService> iter = FolderDeleteListenerRegistry.getInstance().getDeleteListenerServices(); iter.hasNext();) {
                final FolderDeleteListenerService next = iter.next();
                try {
                    next.onFolderDelete(folderID, ctx);
                } catch (final OXException e) {
                    LOG.error("Folder delete listener \"{}\" failed for folder {} int context {}", next.getClass().getName(), folderID, ctx.getContextId(),
                        e);
                    throw e;
                }
            }
            /*
             * Delete contained items
             */
            deleteContainedItems(folderID);
            /*
             * Remember values
             */
            final OCLPermission[] perms = getOXFolderAccess().getFolderObject(folderID).getPermissionsAsArray();
            final int parent = getOXFolderAccess().getParentFolderID(folderID);
            /*
             * Call SQL delete
             */
            try {
                OXFolderSQL.delWorkingOXFolder(folderID, session.getUserId(), lastModified, ctx, writeCon);
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            /*
             * Process system permissions
             */
            if (FolderObject.PUBLIC == type) {
                new CheckPermissionOnRemove(session, writeCon, ctx).checkPermissionsOnDelete(parent, folderID, perms, lastModified);
            }
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
        ConditionTreeMapManagement.dropFor(ctx.getContextId());
        if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
            try {
                FolderCacheManager.getInstance().removeFolderObject(folderID, ctx);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        /*
         * Remove remaining links & deactivate contact collector if necessary
         */
        Connection wc = writeCon;
        boolean closeWriter = false;
        if (wc == null) {
            wc = DBPool.pickupWriteable(ctx);
            closeWriter = true;
        }
        try {
            {
                final ServerUserSetting sus = ServerUserSetting.getInstance(wc);
                final Integer collectFolder = sus.getContactCollectionFolder(ctx.getContextId(), user.getId());
                if (null != collectFolder && folderID == collectFolder.intValue()) {
                    sus.setContactCollectOnMailAccess(ctx.getContextId(), user.getId(), false);
                    sus.setContactCollectOnMailTransport(ctx.getContextId(), user.getId(), false);
                    sus.setContactCollectionFolder(ctx.getContextId(), user.getId(), null);
                }
            }
            /*
             * Subscriptions & Publications
             */
            deleteSubscriptionsAndPublications(wc, storageFolder.getModule(), folderID, false);
            /*
             * Propagate
             */
            if (!hardDelete) {
                final FolderObject fo = FolderObject.loadFolderObjectFromDB(
                    folderID,
                    ctx,
                    wc,
                    true,
                    false,
                    "del_oxfolder_tree",
                    "del_oxfolder_permissions");
                try {
                    if (FolderObject.INFOSTORE == fo.getModule()) {
                        FolderObject parentFolder = FolderObject.loadFolderObjectFromDB(fo.getParentFolderID(), ctx, wc, true, false);
                        new EventClient(session).delete(fo, parentFolder, getFolderPath(fo, parentFolder, wc));
                    } else {
                        new EventClient(session).delete(fo);
                    }
                } catch (final OXException e) {
                    LOG.warn("Delete event could not be enqueued", e);
                }
            }
        } finally {
            if (closeWriter) {
                DBPool.closeWriterSilent(ctx, wc);
            }
        }
    }

    private void deleteContainedItems(final int folderID) throws OXException {
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
        default:
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(Integer.valueOf(module), Integer.valueOf(ctx.getContextId()));
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
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void deleteContainedTasks(final int folderID) throws OXException {
        final Tasks tasks = Tasks.getInstance();
        if (null == writeCon) {
            Connection wc = null;
            try {
                wc = DBPool.pickupWriteable(ctx);
                tasks.deleteTasksInFolder(session, wc, folderID);
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
        ServerServiceRegistry.getInstance().getService(ContactService.class).deleteContacts(session, String.valueOf(folderID));
    }

    private void deleteContainedDocuments(final int folderID) throws OXException {
        final InfostoreFacade infostoreFacade;
        if (writeCon == null) {
            infostoreFacade = new InfostoreFacadeImpl(new DBPoolProvider());
        } else {
            infostoreFacade = new InfostoreFacadeImpl(new StaticDBPoolProvider(writeCon));
            infostoreFacade.setCommitsTransaction(false);
        }
        infostoreFacade.setTransactional(true);
        infostoreFacade.startTransaction();
        try {
            infostoreFacade.removeDocument(folderID, FileStorageFileAccess.DISTANT_FUTURE, ServerSessionAdapter.valueOf(session, ctx));
            infostoreFacade.commit();
        } catch (final OXException x) {
            infostoreFacade.rollback();
            if (InfostoreExceptionCodes.ALREADY_LOCKED.equals(x)) {
                throw OXFolderExceptionCode.DELETE_FAILED_LOCKED_DOCUMENTS.create(x,
                    OXFolderUtility.getFolderName(folderID, ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            throw x;
        } catch (final RuntimeException x) {
            infostoreFacade.rollback();
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(x, Integer.valueOf(ctx.getContextId()));
        } finally {
            infostoreFacade.finish();
        }
    }

    private static final int SPECIAL_CONTACT_COLLECT_FOLDER = 0;

    /**
     * Gathers all folders which are allowed to be deleted
     */
    private TIntObjectMap<TIntObjectMap<?>> gatherDeleteableFolders(final int folderID, final int userId, final UserPermissionBits userPerms, final String permissionIDs) throws OXException, OXException, SQLException {
        final TIntObjectMap<TIntObjectMap<?>> deleteableIDs = new TIntObjectHashMap<TIntObjectMap<?>>();
        final Integer[] specials = new Integer[1];
        // Initialize special folders that must not be deleted
        {
            Integer i = null;
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null != service && service.getBoolProperty("com.openexchange.contactcollector.folder.deleteDenied", false)) {
                i = ServerUserSetting.getInstance(writeCon).getContactCollectionFolder(ctx.getContextId(), userId);
            }
            specials[SPECIAL_CONTACT_COLLECT_FOLDER] = i;
        }
        gatherDeleteableSubfoldersRecursively(folderID, userId, userPerms, permissionIDs, deleteableIDs, folderID, specials);
        return deleteableIDs;
    }

    /**
     * Gathers all folders which are allowed to be deleted in a recursive manner
     * @param specials
     */
    private void gatherDeleteableSubfoldersRecursively(final int folderID, final int userId, final UserPermissionBits userPerms, final String permissionIDs, final TIntObjectMap<TIntObjectMap<?>> deleteableIDs, final int initParent, final Integer[] specials) throws OXException, OXException, SQLException {
        final FolderObject delFolder = getOXFolderAccess().getFolderObject(folderID);
        /*
         * Check if shared
         */
        if (delFolder.isShared(userId)) {
            throw OXFolderExceptionCode.NO_SHARED_FOLDER_DELETION.create(OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if marked as default folder
         */
        if (delFolder.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_DELETION.create(OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check user's effective permission
         */
        final EffectivePermission effectivePerm = getOXFolderAccess().getFolderPermission(folderID, userId, userPerms);
        if (!effectivePerm.isFolderVisible()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                if (initParent == folderID) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderID), OXFolderUtility.getUserName(
                        userId,
                        ctx), Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.HIDDEN_FOLDER_ON_DELETION.create(OXFolderUtility.getFolderName(initParent, ctx),
                    Integer.valueOf(ctx.getContextId()),
                    OXFolderUtility.getUserName(userId, ctx));
            }
            if (initParent == folderID) {
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED,
                    Integer.valueOf(folderID),
                    OXFolderUtility.getUserName(userId, ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            throw OXFolderExceptionCode.HIDDEN_FOLDER_ON_DELETION.create(CATEGORY_PERMISSION_DENIED, OXFolderUtility.getFolderName(
                initParent,
                ctx), Integer.valueOf(ctx.getContextId()), OXFolderUtility.getUserName(userId, ctx));
        }
        if (!effectivePerm.isFolderAdmin()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(OXFolderUtility.getUserName(userId, ctx),
                    OXFolderUtility.getFolderName(folderID, ctx),
                    Integer.valueOf(ctx.getContextId()));
            }
            throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(CATEGORY_PERMISSION_DENIED,
                OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(delFolder, session, ctx)) {
            throw OXFolderExceptionCode.NOT_ALL_OBJECTS_DELETION.create(OXFolderUtility.getUserName(userId, ctx),
                OXFolderUtility.getFolderName(folderID, ctx),
                Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check for special folder
         */
        for (final Integer special : specials) {
            if (null != special && special.intValue() == folderID) {
                throw OXFolderExceptionCode.DELETE_DENIED.create(OXFolderUtility.getFolderName(folderID, ctx), Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check, if folder has subfolders
         */
        final TIntList subfolders = OXFolderSQL.getSubfolderIDs(delFolder.getObjectID(), readCon, ctx);
        if (subfolders.isEmpty()) {
            deleteableIDs.put(folderID, null);
            return;
        }
        final TIntObjectMap<TIntObjectMap<?>> subMap = new TIntObjectHashMap<TIntObjectMap<?>>();
        final int size = subfolders.size();
        for (int i = 0; i < size; i++) {
            gatherDeleteableSubfoldersRecursively(subfolders.get(i), userId, userPerms, permissionIDs, subMap, initParent, specials);
        }
        deleteableIDs.put(folderID, subMap);
    }

    /**
     * Gets a folder's path down to the root folder, ready to be used in events.
     *
     * @param folder The folder to get the path for
     * @param parentFolder The parent folder if known, or <code>null</code> if not
     * @param connection A connection to use
     * @return The folder path
     * @throws OXException
     */
    private String[] getFolderPath(FolderObject folder, FolderObject parentFolder, Connection connection) throws OXException {
        List<String> folderPath = new ArrayList<String>();
        folderPath.add(String.valueOf(folder.getObjectID()));
        int startID;
        if (null == parentFolder) {
            startID = folder.getParentFolderID();
            folderPath.add(String.valueOf(startID));
        } else {
            folderPath.add(String.valueOf(parentFolder.getObjectID()));
            startID = parentFolder.getParentFolderID();
            folderPath.add(String.valueOf(startID));
        }
        if (FolderObject.SYSTEM_ROOT_FOLDER_ID != startID) {
            try {
                List<Integer> pathToRoot = OXFolderSQL.getPathToRoot(startID, connection, ctx);
                for (Integer id : pathToRoot) {
                    folderPath.add(String.valueOf(id));
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        return folderPath.toArray(new String[folderPath.size()]);
    }

    /**
     * This routine is called through AJAX' folder tests!
     */
    @Override
    public void cleanUpTestFolders(final int[] fuids, final Context ctx) {
        for (int fuid : fuids) {
            try {
                OXFolderSQL.hardDeleteOXFolder(fuid, ctx, null);
                ConditionTreeMapManagement.dropFor(ctx.getContextId());
                if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
                    try {
                        FolderCacheManager.getInstance().removeFolderObject(fuid, ctx);
                    } catch (final OXException e) {
                        LOG.warn("", e);
                    }
                }
            } catch (final Exception e) {
                LOG.error("", e);
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

    private OXException parseTruncated(final DataTruncation exc, final FolderObject folder, final String tableName) {
        final String[] fields = DBUtils.parseTruncatedFields(exc);
        final OXException.Truncated[] truncateds = new OXException.Truncated[fields.length];
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
                } catch (final OXException e) {
                    LOG.error("A readable connection could not be fetched from pool", e);
                    return OXFolderExceptionCode.SQL_ERROR.create(exc, exc.getMessage());
                }
                closeReadCon = true;
            }
            try {
                tmp2 = DBUtils.getColumnSize(readCon, tableName, fields[i]);
            } catch (final SQLException e) {
                LOG.error("", e);
                tmp2 = -1;
            } finally {
                if (closeReadCon) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            }
            final int length = -1 == tmp2 ? 0 : tmp2;
            truncateds[i] = new OXException.Truncated() {

                @Override
                public int getId() {
                    return fieldId;
                }

                @Override
                public int getLength() {
                    return valueLength;
                }

                @Override
                public int getMaxSize() {
                    return length;
                }
            };
        }
        sFields.setLength(sFields.length() - 2);
        final OXException fe;
        if (truncateds.length > 0) {
            final OXException.Truncated truncated = truncateds[0];
            if (1 == truncateds.length && FolderObject.FOLDER_NAME == truncated.getId()) {
                fe =  OXFolderExceptionCode.TRUNCATED_FOLDERNAME.create(exc,
                    sFields.toString(),
                    Integer.valueOf(truncated.getMaxSize()),
                    Integer.valueOf(truncated.getLength()));
            } else {
                fe = OXFolderExceptionCode.TRUNCATED.create(exc,
                    sFields.toString(),
                    Integer.valueOf(truncated.getMaxSize()),
                    Integer.valueOf(truncated.getLength()));
            }
        } else {
            fe = OXFolderExceptionCode.TRUNCATED.create(exc,
                sFields.toString(),
                Integer.valueOf(0),
                Integer.valueOf(0));
        }
        for (final OXException.Truncated truncated : truncateds) {
            fe.addProblematic(truncated);
        }
        return fe;
    }

    private static final class ProcedureFailedException extends RuntimeException {

        private static final long serialVersionUID = 1821041261492515385L;

        public ProcedureFailedException(final Throwable cause) {
            super(cause);
        }

    }

    private static int getPublishedMailAttachmentsFolder(final Session session) {
        if (null == session) {
            return -1;
        }
        final Integer i = (Integer) session.getParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID());
        return null == i ? -1 : i.intValue();
    }

}
