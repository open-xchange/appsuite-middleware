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

package com.openexchange.tools.oxfolder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.openexchange.database.IncorrectStringSQLException;
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
import com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlResult;
import com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlUtil;
import com.openexchange.groupware.infostore.facade.impl.EventFiringInfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.i18n.tools.StringHelper;
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
import com.openexchange.tools.exceptions.SimpleIncorrectStringAttribute;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnInsert;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnRemove;
import com.openexchange.tools.oxfolder.treeconsistency.CheckPermissionOnUpdate;
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

    private static final int[] SYSTEM_PUBLIC_FOLDERS = { FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    private static volatile Boolean setAdminAsCreatorForPublicDriveFolder;
    private static boolean setAdminAsCreatorForPublicDriveFolder() {
        Boolean tmp = setAdminAsCreatorForPublicDriveFolder;
        if (null == tmp) {
            synchronized (OXFolderManagerImpl.class) {
                tmp = setAdminAsCreatorForPublicDriveFolder;
                if (null == tmp) {
                    boolean defaultValue = false;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.infostore.setAdminAsCreatorForPublicDriveFolder", defaultValue));
                    setAdminAsCreatorForPublicDriveFolder = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

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

    /**
     * Signals a move to trash through a delete.
     */
    private static final int OPTION_TRASHING = 4;

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
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderChildFields.FOLDER_ID, "", Integer.valueOf(ctx.getContextId()));
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
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.PERMISSIONS, "", Integer.valueOf(ctx.getContextId()));
        }
        final FolderObject parentFolder = getOXFolderAccess().getFolderObject(folderObj.getParentFolderID());
        if (checkPermissions) {
            /*
             * Check, if user holds right to create a sub-folder in given parent folder
             */
            try {
                final EffectivePermission p = parentFolder.getEffectiveUserPermission(user.getId(), userPerms, readCon);
                if (!p.canCreateSubfolders()) {
                    final OXException fe = OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(user.getId(), Integer.valueOf(parentFolder.getObjectID()), Integer.valueOf(ctx.getContextId()));
                    if (p.getUnderlyingPermission().canCreateSubfolders()) {
                        fe.setCategory(CATEGORY_PERMISSION_DENIED);
                    }
                    throw fe;
                }
                if (!userPerms.hasModuleAccess(folderObj.getModule())) {
                    throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(CATEGORY_PERMISSION_DENIED, user.getId(), OXFolderUtility.folderModule2String(folderObj.getModule()), Integer.valueOf(ctx.getContextId()));
                }
                if ((parentFolder.getType() == FolderObject.PUBLIC) && !userPerms.hasFullPublicFolderAccess() && (folderObj.getModule() != FolderObject.INFOSTORE)) {
                    throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(user.getId(), Integer.valueOf(parentFolder.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Check folder types
         */
        if (!OXFolderUtility.checkFolderTypeAgainstParentType(parentFolder, folderObj.getType())) {
            throw OXFolderExceptionCode.INVALID_TYPE.create(Integer.valueOf(parentFolder.getObjectID()), OXFolderUtility.folderType2String(folderObj.getType()), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check folder module
         */
        if (!isKnownModule(folderObj.getModule())) {
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(OXFolderUtility.folderModule2String(folderObj.getModule()), Integer.valueOf(ctx.getContextId()));
        }
        if (!OXFolderUtility.checkFolderModuleAgainstParentModule(parentFolder.getObjectID(), parentFolder.getModule(), folderObj.getModule(), ctx.getContextId())) {
            throw OXFolderExceptionCode.INVALID_MODULE.create(Integer.valueOf(parentFolder.getObjectID()), OXFolderUtility.folderModule2String(folderObj.getModule()), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if parent folder is a shared folder OR
         * if folder is a public InfoStore folder and setting context admin as creator is desired
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
        } else if (FolderObject.INFOSTORE == folderObj.getModule() && setAdminAsCreatorForPublicDriveFolder() && isPublicInfoStoreFolder(parentFolder)) {
            folderObj.setCreatedBy(ctx.getMailadmin());
        }
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(session, folderObj, parentFolder.getNonSystemPermissionsAsArray());
        /*
         * Check if admin exists and permission structure
         */
        OXFolderUtility.checkFolderPermissions(folderObj, user.getId(), ctx, warnings);
        OXFolderUtility.checkPermissionsAgainstUserConfigs(folderObj, ctx);
        if (FolderObject.PUBLIC == folderObj.getType()) {
            new CheckPermissionOnInsert(session, writeCon, ctx).checkParentPermissions(parentFolder.getObjectID(), folderObj.getNonSystemPermissionsAsArray(), createTime);
        }
        /*
         * Check against reserved / duplicate / invalid folder names in target folder
         */
        OXFolderUtility.checkTargetFolderName(readCon, ctx, user, -1, folderObj.getModule(), folderObj.getParentFolderID(), folderObj.getFolderName(), user.getId());
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
                } catch (final IncorrectStringSQLException e) {
                    throw handleIncorrectStringError(e, session);
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
        boolean created = false;
        try {
            OXFolderSQL.insertFolderSQL(fuid, user.getId(), folderObj, createTime, ctx, writeCon);
            created = true;
            folderObj.setObjectID(fuid);
        } catch (final DataTruncation e) {
            throw parseTruncated(e, folderObj, TABLE_OXFOLDER_TREE);
        } catch (final IncorrectStringSQLException e) {
            throw handleIncorrectStringError(e, session);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (false == created) {
                FolderCacheManager manager = FolderCacheManager.getInstance();
                manager.removeFolderObject(parentFolder.getObjectID(), ctx);
            }
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
                    manager.loadFolderObject(parentFolder.getObjectID(), ctx, wc);
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
                        new EventClient(session).create(folderObj, parentFolder, getFolderPath(folderObj, parentFolder, wc));
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

    private boolean isPublicInfoStoreFolder(FolderObject parentFolder) throws OXException {
        int fuid = parentFolder.getObjectID();
        if (fuid <= FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            return false;
        }
        if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == fuid) {
            return true;
        }

        // Recursive check with grand parent
        return isPublicInfoStoreFolder(getOXFolderAccess().getFolderObject(parentFolder.getParentFolderID()));
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
            throw OXFolderExceptionCode.INVALID_SEQUENCE_ID.create(Integer.valueOf(fuid), Integer.valueOf(FolderObject.MIN_FOLDER_ID), Integer.valueOf(ctx.getContextId()));
        }

        return fuid;
    }

    @Override
    public FolderObject updateFolder(final FolderObject fo, final boolean checkPermissions, final boolean handDown, final long lastModified) throws OXException {
        return updateFolder(fo, checkPermissions, handDown, lastModified, OPTION_NONE);
    }

    private FolderObject updateFolder(final FolderObject fo, final boolean checkPermissions, final boolean handDown, final long lastModified, int options) throws OXException {
        if (checkPermissions) {
            if (fo.containsType() && fo.getType() == FolderObject.PUBLIC && fo.getModule() != FolderObject.INFOSTORE && !userPerms.hasFullPublicFolderAccess()) {
                throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(session.getUserId(), Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Fetch effective permission from storage
             */
            final EffectivePermission perm = getOXFolderAccess().getFolderPermission(fo.getObjectID(), user.getId(), userPerms);
            if (!perm.isFolderVisible() || !perm.getUnderlyingPermission().isFolderVisible()) {
                throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(fo.getObjectID()), session.getUserId(), Integer.valueOf(ctx.getContextId()));
            }
            if (!perm.isFolderAdmin() || !perm.getUnderlyingPermission().isFolderAdmin()) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(session.getUserId(), Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            if (fo.getObjectID() == getPublishedMailAttachmentsFolder(session)) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(session.getUserId(), Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
        }

        boolean performMove = fo.containsParentFolderID();
        FolderObject originalFolder = getFolderFromMaster(fo.getObjectID());
        int oldParentId = originalFolder.getParentFolderID();
        FolderObject storageObject = originalFolder.clone();

        int optionz = options;
        if (((optionz & OPTION_TRASHING) <= 0) && performMove) {
            int newParentFolderID = fo.getParentFolderID();
            if (newParentFolderID > 0 && newParentFolderID != storageObject.getParentFolderID()) {
                if ((FolderObject.TRASH == getFolderTypeFromMaster(newParentFolderID)) && (FolderObject.TRASH != getFolderTypeFromMaster(storageObject.getParentFolderID()))) {
                    // Move to trash
                    int folderId = fo.getObjectID();
                    String name = fo.containsFolderName() && !Strings.isEmpty(fo.getFolderName()) ? fo.getFolderName() : storageObject.getFolderName();
                    try {
                        while (-1 != OXFolderSQL.lookUpFolderOnUpdate(folderId, newParentFolderID, name, storageObject.getModule(), readCon, ctx)) {
                            name = incrementSequenceNumber(name);
                        }
                    } catch (SQLException e) {
                        throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                    }
                    /*
                     * remove any folder-dependent entities
                     */
                    deleteDependentEntities(writeCon, storageObject, true);
                    /*
                     * perform move to trash
                     */
                    fo.setFolderName(name);
                    fo.setPermissions(getFolderFromMaster(newParentFolderID).getPermissions());
                    // when deleting a folder, the permissions should always be inherited from the parent trash folder
                    // in order to do so, "created by" is overridden intentionally here to not violate permission restrictions,
                    // and to prevent synthetic system permissions to get inserted implicitly
                    optionz |= user.getId() != storageObject.getCreatedBy() ? OPTION_OVERRIDE_CREATED_BY : OPTION_NONE;
                }
            }
        }

        Map<Integer, Integer> folderId2OldOwner = null;
        try {
            if (fo.containsPermissions() || fo.containsModule() || fo.containsMeta()) {
                int newParentFolderID = fo.getParentFolderID();
                if (performMove && newParentFolderID > 0 && newParentFolderID != storageObject.getParentFolderID()) {
                    folderId2OldOwner = determineCurrentOwnerships(originalFolder);
                    move(fo.getObjectID(), newParentFolderID, fo.getCreatedBy(), fo.getFolderName(), storageObject, lastModified);
                    // Reload storage's folder for following update
                    storageObject = getFolderFromMaster(fo.getObjectID());
                } else {
                    // Check if permissions of a trash folder are supposed to be changed
                    if (fo.containsPermissions()) {
                        checkTrashFolderPermissionChange(fo, storageObject);
                    }
                }
                update(fo, optionz, storageObject, lastModified, handDown);
            } else if (fo.containsFolderName()) {
                int newParentFolderID = fo.getParentFolderID();
                if (performMove && newParentFolderID > 0 && newParentFolderID != storageObject.getParentFolderID()) {
                    // Perform move
                    folderId2OldOwner = determineCurrentOwnerships(originalFolder);
                    move(fo.getObjectID(), newParentFolderID, fo.getCreatedBy(), fo.getFolderName(), storageObject, lastModified);
                } else {
                    rename(fo, storageObject, lastModified);
                }
            } else if (performMove) {
                // Perform move
                folderId2OldOwner = determineCurrentOwnerships(originalFolder);
                move(fo.getObjectID(), fo.getParentFolderID(), fo.getCreatedBy(), null, storageObject, lastModified);
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Possibly changed file storage?
         */
        if (null != folderId2OldOwner) {
            adjustFileStorageLocations(folderId2OldOwner);
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
                         * it is needed to do this by removing the folder object so the invalidation is distributed every time also in the
                         * event that it is not in the local cache
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

    private void checkTrashFolderPermissionChange(final FolderObject fo, FolderObject storageObject) throws OXException {
        FolderObject trashFolder = getTrashFolder(storageObject.getModule());
        if (null != trashFolder) {
            boolean belowTrash;
            int trashFolderID = trashFolder.getObjectID();
            if (storageObject.getObjectID() == trashFolderID || storageObject.getParentFolderID() == trashFolderID) {
                belowTrash = true;
            } else {
                OXFolderAccess folderAccess = getOXFolderAccess();
                FolderObject p = storageObject;
                while (p.getParentFolderID() != trashFolderID && FolderObject.MIN_FOLDER_ID < p.getParentFolderID()) {
                    p = folderAccess.getFolderObject(p.getParentFolderID());
                }
                belowTrash = p.getParentFolderID() == trashFolderID;
            }

            if (belowTrash && !OXFolderUtility.equalPermissions(fo.getNonSystemPermissionsAsArray(), storageObject.getNonSystemPermissionsAsArray())) {
                throw OXFolderExceptionCode.NO_TRASH_PERMISSIONS_CHANGE_ALLOWED.create(Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
        }
    }

    private Map<Integer, Integer> determineCurrentOwnerships(FolderObject folder) throws OXException {
        Map<Integer, Integer> folderId2OldOwner;
        List<Integer> folderIds = new ArrayList<Integer>(8);
        folderIds.add(Integer.valueOf(folder.getObjectID()));
        if (folder.hasSubfolders()) {
            try {
                folderIds.addAll(OXFolderSQL.getSubfolderIDs(folder.getObjectID(), writeCon, ctx, true));
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        folderId2OldOwner = new LinkedHashMap<Integer, Integer>(folderIds.size());
        for (Integer folderId : folderIds) {
            folderId2OldOwner.put(folderId, Integer.valueOf(getFolderFromMaster(folderId.intValue()).getCreatedBy()));
        }
        return folderId2OldOwner;
    }

    private void adjustFileStorageLocations(Map<Integer, Integer> folderId2OldOwner) throws OXException {
        List<Map<Integer, List<VersionControlResult>>> results = new LinkedList<Map<Integer, List<VersionControlResult>>>();
        boolean error = true;
        try {
            for (Entry<Integer, Integer> f2o : folderId2OldOwner.entrySet()) {
                int folderId = f2o.getKey().intValue();
                int newOwner = getFolderOwnerFromMaster(folderId);
                int oldOwner = f2o.getValue().intValue();
                if (oldOwner != newOwner) {
                    // File storage location might be changed due to changed ownership
                    Connection wc = writeCon;
                    boolean create = (wc == null);
                    Map<Integer, List<VersionControlResult>> modified = null;
                    try {
                        if (create) {
                            wc = DBPool.pickupWriteable(ctx);
                        }
                        modified = VersionControlUtil.changeFileStoreLocationsIfNecessary(oldOwner, newOwner, folderId, ctx, wc);
                        if (!modified.isEmpty()) {
                            results.add(modified);
                        }
                    } finally {
                        if (create && wc != null) {
                            if (null != modified && !modified.isEmpty()) {
                                DBPool.closeWriterSilent(ctx, wc);
                            } else {
                                DBPool.closeWriterAfterReading(ctx, wc);
                            }
                            wc = null;
                        }
                    }
                }
            }
            error = false;
        } finally {
            if (error) {
                Connection wc = writeCon;
                boolean create = (wc == null);
                try {
                    for (Map<Integer,List<VersionControlResult>> resultMap : results) {
                        for (Map.Entry<Integer, List<VersionControlResult>> documentEntry : resultMap.entrySet()) {
                            Integer documentId = documentEntry.getKey();
                            List<VersionControlResult> versionInfo = documentEntry.getValue();

                            try {
                                VersionControlUtil.restoreVersionControl(Collections.singletonMap(documentId, versionInfo), ctx, wc);
                            } catch (Exception e) {
                                LOG.error("Failed to restore InfoStore/Drive files for document {} in context {}", documentId, ctx.getContextId(), e);
                            }
                        }
                    }
                } finally {
                    if (create && wc != null) {
                        DBPool.closeWriterSilent(ctx, wc);
                        wc = null;
                    }
                }
            }
        }
    }

    protected void update(FolderObject fo, int options, FolderObject storageObj, long lastModified, boolean handDown) throws OXException {
        doUpdate(fo, options, storageObj, lastModified, handDown, new TIntHashSet());
    }

    void doUpdate(FolderObject fo, int options, FolderObject storageObj, long lastModified, boolean handDown, TIntSet alreadyCheckedParents) throws OXException {
        if (fo.getObjectID() <= 0) {
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(Integer.valueOf(fo.getObjectID()));
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
                throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.PERMISSIONS, Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
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
            throw OXFolderExceptionCode.NO_MOVE_THROUGH_UPDATE.create(Integer.valueOf(fo.getObjectID()));
        }
        /*
         * Check folder name
         */
        if (fo.containsFolderName()) {
            if (fo.getFolderName() == null || fo.getFolderName().trim().length() == 0) {
                throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TITLE, Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
            } else if (storageObj.isDefaultFolder() && !fo.getFolderName().equals(storageObj.getFolderName())) {
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_RENAME.create(Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
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
                throw OXFolderExceptionCode.UNKNOWN_MODULE.create(OXFolderUtility.folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
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
            FolderObject parent = getFolderFromMaster(storageObj.getParentFolderID());
            if (!OXFolderUtility.checkFolderModuleAgainstParentModule(parent.getObjectID(), parent.getModule(), fo.getModule(), ctx.getContextId())) {
                throw OXFolderExceptionCode.INVALID_MODULE.create(Integer.valueOf(parent.getObjectID()), OXFolderUtility.folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
            }
        } else {
            fo.setModule(storageObj.getModule());
        }
        /*
         * Check if shared
         */
        if (storageObj.isShared(user.getId())) {
            throw OXFolderExceptionCode.NO_SHARED_FOLDER_UPDATE.create(Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check Permissions
         */
        fo.setType(storageObj.getType());
        if ((options & OPTION_OVERRIDE_CREATED_BY) <= 0) {
            fo.setCreatedBy(storageObj.getCreatedBy());
        }
        fo.setDefaultFolder(storageObj.isDefaultFolder());
        OXFolderUtility.checkPermissionsAgainstSessionUserConfig(session, fo, storageObj.getNonSystemPermissionsAsArray());
        OXFolderUtility.checkFolderPermissions(fo, user.getId(), ctx, warnings);
        OXFolderUtility.checkPermissionsAgainstUserConfigs(readCon, fo, ctx);
        OXFolderUtility.checkSystemFolderPermissions(fo.getObjectID(), fo.getNonSystemPermissionsAsArray(), user, ctx);
        if (FolderObject.PUBLIC == fo.getType() || FolderObject.INFOSTORE == storageObj.getModule()) {
            {
                final OCLPermission[] removedPerms = OXFolderUtility.getPermissionsWithoutFolderAccess(fo.getNonSystemPermissionsAsArray(), storageObj.getNonSystemPermissionsAsArray());
                if (removedPerms.length > 0) {
                    new CheckPermissionOnRemove(session, writeCon, ctx).checkPermissionsOnUpdate(fo.getObjectID(), removedPerms, lastModified);
                }
            }
            if (alreadyCheckedParents.add(storageObj.getParentFolderID())) {
                new CheckPermissionOnUpdate(session, writeCon, ctx).checkParentPermissions(storageObj.getParentFolderID(), fo.getNonSystemPermissionsAsArray(), storageObj.getNonSystemPermissionsAsArray(), lastModified, alreadyCheckedParents);
            }
        }

        boolean rename = false;
        if (fo.containsFolderName() && !storageObj.getFolderName().equals(fo.getFolderName())) {
            rename = true;
            /*
             * Check against reserved / duplicate / invalid folder names in target folder
             */
            OXFolderUtility.checkTargetFolderName(readCon, ctx, user, fo.getObjectID(), fo.getModule(), storageObj.getParentFolderID(), fo.getFolderName(), storageObj.getCreatedBy());
        }
        /*
         * This folder shall be shared to other users
         */
        if (fo.getType() == FolderObject.PRIVATE && fo.getPermissions().size() > 1) {
            final TIntSet diff = OXFolderUtility.getShareUsers(rename ? null : storageObj.getPermissions(), fo.getPermissions(), user.getId(), ctx);
            if (!diff.isEmpty()) {
                OXFolderAccess folderAccess = getOXFolderAccess();
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
                            allSharedFolders[i] = folderAccess.getFolderObject(fuid);
                        }
                    }
                } catch (final DataTruncation e) {
                    throw parseTruncated(e, fo, TABLE_OXFOLDER_TREE);
                } catch (final IncorrectStringSQLException e) {
                    throw handleIncorrectStringError(e, session);
                } catch (final SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
                OXFolderUtility.checkSimilarNamedSharedFolder(diff, allSharedFolders, rename ? fo.getFolderName() : storageObj.getFolderName(), ctx);
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
        } catch (final IncorrectStringSQLException e) {
            throw handleIncorrectStringError(e, session);
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
                        handDown(fo.getObjectID(), options, permissions, lastModified, alreadyCheckedParents, FolderCacheManager.isEnabled() ? FolderCacheManager.getInstance() : null);
                    }
                }
            } catch (final DataTruncation e) {
                throw parseTruncated(e, fo, TABLE_OXFOLDER_TREE);
            } catch (final IncorrectStringSQLException e) {
                throw handleIncorrectStringError(e, session);
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

    protected void handDown(final int folderId, final int options, final List<OCLPermission> permissions, final long lastModified, final TIntSet alreadyCheckedParents, final FolderCacheManager cacheManager) throws OXException, SQLException {
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
                        doUpdate(tmp, options, getFolderFromMaster(subfolderId), lastModified, true, alreadyCheckedParents);  // Calls handDown() for subfolder, as well
                        if (null != cacheManager) {
                            cacheManager.removeFolderObject(subfolderId, ctx);
                        }
                        CacheFolderStorage.getInstance().removeFromCache(Integer.toString(subfolderId), FolderStorage.REAL_TREE_ID, true, session);
                        return true;
                    } catch (final OXException e) {
                        throw new ProcedureFailedException(e);
                    } catch (final RuntimeException e) {
                        throw new ProcedureFailedException(e);
                    }
                }
            });
        }
    }

    private int getFolderTypeFromMaster(int folderId) throws OXException {
        return getFolderFromMaster(folderId, false, false).getType();
    }

    private int getFolderOwnerFromMaster(int folderId) throws OXException {
        return getFolderFromMaster(folderId, false, false).getCreatedBy();
    }

    private boolean hasSubfoldersFromMaster(int folderId) throws OXException {
        return getFolderFromMaster(folderId, false, false).hasSubfolders();
    }

    protected FolderObject getFolderFromMaster(int folderId) throws OXException {
        return getFolderFromMaster(folderId, false);
    }

    private FolderObject getFolderFromMaster(int folderId, boolean withSubfolders) throws OXException {
        return getFolderFromMaster(folderId, true, withSubfolders);
    }

    private FolderObject getFolderFromMaster(int folderId, boolean loadPermissions, boolean withSubfolders) throws OXException {
        // Use writable connection to ensure to fetch from master database
        Connection wc = writeCon;
        if (wc != null) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, loadPermissions, withSubfolders);
        }

        // Fetch new writable connection
        wc = DBPool.pickupWriteable(ctx);
        try {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, loadPermissions, withSubfolders);
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
            final InfostoreFacade db = new EventFiringInfostoreFacadeImpl(readCon == null ? new DBPoolProvider() : new StaticDBPoolProvider(readCon));
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
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(Integer.valueOf(folderObj.getObjectID()));
        } else if (!folderObj.containsFolderName() || folderObj.getFolderName() == null || folderObj.getFolderName().trim().length() == 0) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderFields.TITLE, "", I(ctx.getContextId()));
        }
        /*
         * Check if rename can be avoided (cause new name equals old one) and prevent default folder rename
         */
        if (storageObj.getFolderName().equals(folderObj.getFolderName())) {
            return;
        }
        if (storageObj.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_RENAME.create(Integer.valueOf(folderObj.getObjectID()), I(ctx.getContextId()));
        }
        /*
         * Check against reserved / duplicate / invalid folder names in target folder
         */
        OXFolderUtility.checkTargetFolderName(readCon, ctx, user, folderObj.getObjectID(), storageObj.getModule(), storageObj.getParentFolderID(), folderObj.getFolderName(), storageObj.getCreatedBy());
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
                } catch (final IncorrectStringSQLException e) {
                    throw handleIncorrectStringError(e, session);
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
        } catch (final IncorrectStringSQLException e) {
            throw handleIncorrectStringError(e, session);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Moves and/or renames a folder in the database.
     *
     * @param folderId The identifier of the folder to move and/or rename
     * @param targetFolderId The identifier of the target folder
     * @param createdBy The user who created the folder
     * @param newName The new folder name, or <code>null</code> if unchanged
     * @param storageSrc The folder being moved, reloaded from the storage
     * @param lastModified The new time stamp to store as last modification date
     */
    private void move(int folderId, int targetFolderId, int createdBy, String newName, FolderObject storageSrc, long lastModified) throws OXException, SQLException {
        /*
         * Folder is already in target folder and does not need to be renamed
         */
        int oldParentId = storageSrc.getParentFolderID();
        if (oldParentId == targetFolderId && (null == newName || newName.equals(storageSrc.getFolderName()))) {
            return;
        }
        /*
         * Default folder must not be moved
         */
        if (storageSrc.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), I(ctx.getContextId()));
        }
        /*
         * Check if duplicate / reserved folder exists in target folder
         */
        String folderName = null == newName ? storageSrc.getFolderName() : newName;
        OXFolderUtility.checkTargetFolderName(readCon, ctx, user, folderId, storageSrc.getModule(), targetFolderId, folderName, createdBy);
        /*
         * For further checks we need to load destination folder
         */
        final FolderObject storageDest = getOXFolderAccess().getFolderObject(targetFolderId);
        /*
         * Check a bunch of possible errors
         */
        try {
            if (storageSrc.isShared(user.getId())) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), I(ctx.getContextId()));
            } else if (storageDest.isShared(user.getId())) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_TARGET.create(Integer.valueOf(storageDest.getObjectID()), I(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.SYSTEM_TYPE) {
                throw OXFolderExceptionCode.NO_SYSTEM_FOLDER_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), I(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PRIVATE && ((storageDest.getType() == FolderObject.PUBLIC || (storageDest.getType() == FolderObject.SYSTEM_TYPE && targetFolderId != FolderObject.SYSTEM_PRIVATE_FOLDER_ID)))) {
                throw OXFolderExceptionCode.ONLY_PRIVATE_TO_PRIVATE_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), I(ctx.getContextId()));
            } else if (storageSrc.getType() == FolderObject.PUBLIC && ((storageDest.getType() == FolderObject.PRIVATE || (storageDest.getType() == FolderObject.SYSTEM_TYPE && false == com.openexchange.tools.arrays.Arrays.contains(SYSTEM_PUBLIC_FOLDERS, targetFolderId))))) {
                throw OXFolderExceptionCode.ONLY_PUBLIC_TO_PUBLIC_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), I(ctx.getContextId()));
            } else if (storageSrc.getModule() == FolderObject.INFOSTORE && storageDest.getModule() != FolderObject.INFOSTORE && targetFolderId != FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                throw OXFolderExceptionCode.INCOMPATIBLE_MODULES.create(OXFolderUtility.folderModule2String(storageSrc.getModule()), OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageSrc.getModule() != FolderObject.INFOSTORE && storageDest.getModule() == FolderObject.INFOSTORE) {
                throw OXFolderExceptionCode.INCOMPATIBLE_MODULES.create(OXFolderUtility.folderModule2String(storageSrc.getModule()), OXFolderUtility.folderModule2String(storageDest.getModule()));
            } else if (storageDest.getEffectiveUserPermission(user.getId(), userPerms).getFolderPermission() < OCLPermission.CREATE_SUB_FOLDERS) {
                throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(user.getId(), Integer.valueOf(storageDest.getObjectID()), I(ctx.getContextId()));
            } else if (folderId == targetFolderId) {
                throw OXFolderExceptionCode.NO_EQUAL_MOVE.create(I(ctx.getContextId()));
            }
        } catch (RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, I(ctx.getContextId()));
        }
        /*
         * Check if source folder has subfolders
         */
        if (storageSrc.hasSubfolders()) {
            /*
             * Check if target is a descendant folder
             */
            final TIntList parentIDList = new TIntArrayList(1);
            parentIDList.add(storageSrc.getObjectID());
            if (OXFolderUtility.isDescendentFolder(parentIDList, targetFolderId, readCon, ctx)) {
                throw OXFolderExceptionCode.NO_SUBFOLDER_MOVE.create(Integer.valueOf(storageSrc.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Count all moveable subfolders: TODO: Recursive check???
             */
            final int numOfMoveableSubfolders = OXFolderSQL.getNumOfMoveableSubfolders(storageSrc.getObjectID(), user.getId(), user.getGroups(), readCon, ctx);
            if (numOfMoveableSubfolders != storageSrc.getSubfolderIds(true, ctx).size()) {
                throw OXFolderExceptionCode.NO_SUBFOLDER_MOVE_ACCESS.create(session.getUserId(), Integer.valueOf(storageSrc.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * First treat as a delete prior to actual move
         */
        processDeletedFolderThroughMove(storageSrc, new CheckPermissionOnRemove(session, writeCon, ctx), lastModified);
        /*
         * Call SQL move
         */
        try {
            storageSrc.setFolderName(newName);
            OXFolderSQL.moveFolderSQL(user.getId(), storageSrc, storageDest, lastModified, ctx, readCon, writeCon);
        } catch (DataTruncation e) {
            throw parseTruncated(e, storageSrc, TABLE_OXFOLDER_TREE);
        } catch (final IncorrectStringSQLException e) {
            throw handleIncorrectStringError(e, session);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Now treat as an insert after actual move if not moved below trash
         */
        if (FolderObject.TRASH != storageDest.getType()) {
            processInsertedFolderThroughMove(getFolderFromMaster(folderId), new CheckPermissionOnInsert(session, writeCon, ctx), lastModified);
        }
        /*
         * Inherit folder type recursively if move from or to special folder
         */
        {
            int optNewOwner = FolderObject.TRASH == storageDest.getType() ? user.getId() : 0;
            adjustFolderTypeOnMove(storageSrc, storageDest, true, optNewOwner);
        }
        /*
         * Adjust owner (if necessary)
         */
        changeOwnerOnMove(storageSrc, targetFolderId, oldParentId, true);
        /*
         * Update last-modified time stamps
         */
        OXFolderSQL.updateLastModified(storageSrc.getParentFolderID(), lastModified, user.getId(), writeCon, ctx);
        OXFolderSQL.updateLastModified(storageSrc.getObjectID(), lastModified, user.getId(), writeCon, ctx);
        OXFolderSQL.updateLastModified(storageDest.getObjectID(), lastModified, user.getId(), writeCon, ctx);
        /*
         * Update OLD parent in cache, cause this can only be done here
         */
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

    /**
     * Adjusts the folder's type as needed after moving a folder to a new parent folder.
     *
     * @param sourceFolder The folder being moved
     * @param destinationFolder The destination folder, i.e. the new parent folder
     * @param recursive <code>true</code> to inherit the new folder type to any subfolders recursively, <code>false</code>, otherwise
     * @param optNewOwner The optional new owner or less than/equal to <code>0</code> (zero)
     * @return <code>true</code> if the folder type was adjusted, <code>false</code>, otherwise
     */
    private boolean adjustFolderTypeOnMove(FolderObject sourceFolder, FolderObject destinationFolder, boolean recursive, int optNewOwner) throws OXException, SQLException {
        if (sourceFolder.getType() != destinationFolder.getType()) {
            int[] inheritingTypes = { FolderObject.TRASH, FolderObject.DOCUMENTS, FolderObject.PICTURES, FolderObject.MUSIC, FolderObject.VIDEOS, FolderObject.TEMPLATES };
            if (contains(inheritingTypes, destinationFolder.getType()) || contains(inheritingTypes, sourceFolder.getType())) {
                List<Integer> folderIDs;
                List<Integer> children = null;
                if (false == recursive || false == sourceFolder.hasSubfolders()) {
                    folderIDs = Collections.singletonList(Integer.valueOf(sourceFolder.getObjectID()));
                } else {
                    folderIDs = new ArrayList<Integer>();
                    folderIDs.add(Integer.valueOf(sourceFolder.getObjectID()));
                    children = OXFolderSQL.getSubfolderIDs(sourceFolder.getObjectID(), readCon, ctx, true);
                    folderIDs.addAll(children);
                }
                int type = FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == destinationFolder.getObjectID() ? FolderObject.PUBLIC : destinationFolder.getType();
                boolean result = 0 < OXFolderSQL.updateFolderType(writeCon, ctx, type, optNewOwner, folderIDs);
                if (recursive && children != null && !children.isEmpty() && FolderCacheManager.isEnabled()) {
                    Connection wc = writeCon;
                    final boolean create = (wc == null);
                    if (create) {
                        wc = DBPool.pickupWriteable(ctx);
                    }
                    try {
                        final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
                        for (int i : children) {
                            cacheManager.loadFolderObject(i, ctx, wc);
                        }
                    } finally {
                        if (create && wc != null) {
                            DBPool.closeWriterSilent(ctx, wc);
                        }
                    }
                }
                return result;
            }
        }
        return false;
    }

    private boolean changeOwnerOnMove(FolderObject fo, int newParentId, int oldParentId, boolean recursive) throws OXException, SQLException {
        if (FolderObject.INFOSTORE == fo.getModule() && (newParentId > 0 && newParentId != oldParentId) && setAdminAsCreatorForPublicDriveFolder()) {
            FolderObject newParent = getFolderFromMaster(newParentId);
            boolean isPublicInfoStoreFolder = isPublicInfoStoreFolder(newParent);

            FolderObject oldParent = getFolderFromMaster(oldParentId);
            boolean wasPublicInfoStoreFolder = isPublicInfoStoreFolder(oldParent);

            if (isPublicInfoStoreFolder != wasPublicInfoStoreFolder) {
                List<Integer> folderIDs;
                List<Integer> children = null;
                if (false == recursive || false == fo.hasSubfolders()) {
                    folderIDs = Collections.singletonList(Integer.valueOf(fo.getObjectID()));
                } else {
                    folderIDs = new ArrayList<Integer>();
                    folderIDs.add(Integer.valueOf(fo.getObjectID()));
                    children = OXFolderSQL.getSubfolderIDs(fo.getObjectID(), readCon, ctx, true);
                    folderIDs.addAll(children);

                }

                int newOwner = isPublicInfoStoreFolder ? ctx.getMailadmin() : newParent.getCreatedBy();
                boolean result = 0 < OXFolderSQL.updateFolderOwner(writeCon, ctx, newOwner, folderIDs);
                if (recursive && children != null && !children.isEmpty() && FolderCacheManager.isEnabled()) {
                    Connection wc = writeCon;
                    final boolean create = (wc == null);
                    if (create) {
                        wc = DBPool.pickupWriteable(ctx);
                    }
                    try {
                        for (int i : children) {
                            FolderCacheManager.getInstance().loadFolderObject(i, ctx, wc);
                        }
                    } finally {
                        if (create && wc != null) {
                            DBPool.closeWriterSilent(ctx, wc);
                        }
                    }
                }
                return result;
            }
        }
        return false;
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
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(Integer.valueOf(fo.getObjectID()));
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
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(fo.getObjectID()), user.getId(), Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED, Integer.valueOf(fo.getObjectID()), user.getId(), Integer.valueOf(ctx.getContextId()));
            }
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(fo, session, ctx)) {
            throw OXFolderExceptionCode.NOT_ALL_OBJECTS_DELETION.create(user.getId(), Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
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
                    deleteableFolders.putAll(gatherDeleteableFolders(subfolders.get(i), user.getId(), userPerms, StringCollection.getSqlInString(user.getId(), user.getGroups())));
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
            throw OXFolderExceptionCode.INVALID_OBJECT_ID.create(Integer.valueOf(fo.getObjectID()));
        }
        if (folderId < FolderObject.MIN_FOLDER_ID) {
            throw OXFolderExceptionCode.NO_SYSTEM_FOLDER_MOVE.create(Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Reload original folder
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
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderId), user.getId(), Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED, Integer.valueOf(folderId), user.getId(), Integer.valueOf(ctx.getContextId()));
            }
            if (!p.isFolderAdmin()) {
                if (!p.getUnderlyingPermission().isFolderAdmin()) {
                    throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(user.getId(), Integer.valueOf(folder.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(CATEGORY_PERMISSION_DENIED, user.getId(), Integer.valueOf(folder.getObjectID()), Integer.valueOf(ctx.getContextId()));
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
                boolean belowTrash;
                int trashFolderID = trashFolder.getObjectID();
                if (parentFolder.getObjectID() == trashFolderID || parentFolder.getParentFolderID() == trashFolderID) {
                    belowTrash = true;
                } else {
                    FolderObject p = parentFolder;
                    while (p.getParentFolderID() != trashFolderID && FolderObject.MIN_FOLDER_ID < p.getParentFolderID()) {
                        p = folderAccess.getFolderObject(p.getParentFolderID());
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
                     * remove any folder-dependent entities
                     */
                    deleteDependentEntities(writeCon, folder, true);
                    /*
                     * perform move to trash
                     */
                    FolderObject toUpdate = new FolderObject(name, folderId, folder.getModule(), folder.getType(), user.getId());
                    toUpdate.setParentFolderID(trashFolderID);
                    toUpdate.setPermissions(trashFolder.getPermissions());
                    // when deleting a folder, the permissions should always be inherited from the parent trash folder
                    // in order to do so, "created by" is overridden intentionally here to not violate permission restrictions,
                    // and to prevent synthetic system permissions to get inserted implicitly
                    int options = OPTION_TRASHING;
                    options |= (user.getId() != folder.getCreatedBy() ? OPTION_OVERRIDE_CREATED_BY : OPTION_NONE);
                    return updateFolder(toUpdate, false, true, lastModified, options);
                }
            }
        }
        /*
         * perform hard delete of folder and all subfolders
         */
        final TIntObjectMap<TIntObjectMap<?>> deleteableFolders;
        try {
            deleteableFolders = gatherDeleteableFolders(folderId, user.getId(), userPerms, StringCollection.getSqlInString(user.getId(), user.getGroups()));
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
                    FolderCacheManager.getInstance().removeFolderObject(parentFolder.getObjectID(), ctx);
                    FolderCacheManager.getInstance().loadFolderObject(parentFolder.getObjectID(), ctx, wc);
                }
                /*
                 * Load return value
                 */
                fo.fill(FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, false, "del_oxfolder_tree", "del_oxfolder_permissions"));
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
        final Set<Integer> validatedFolders = new LinkedHashSet<Integer>();
        TIntObjectProcedure<TIntObjectMap<?>> procedure = new TIntObjectProcedure<TIntObjectMap<?>>() {

            @Override
            public boolean execute(int folderId, TIntObjectMap<?> hashMap) {
                if (null != hashMap) {
                    final @SuppressWarnings("unchecked") TIntObjectMap<TIntObjectMap<?>> tmp = (TIntObjectMap<TIntObjectMap<?>>) hashMap;
                    tmp.forEachEntry(this);
                }
                validatedFolders.add(I(folderId));
                return true;
            }
        };
        deleteableIDs.forEachEntry(procedure);
        for (Integer validatedFolder : validatedFolders) {
            deleteValidatedFolder(validatedFolder.intValue(), lastModified, type, false);
        }
    }

    /**
     * Deletes any existing dependent entities (e.g. subscriptions, publications, shares) for the supplied folder ID.
     *
     * @param wcon A "write" connection to the database
     * @param folder The deleted folder. Must be fully initialized.
     * @param handDown <code>true</code> to also remove the subscriptions and publications of any nested subfolder, <code>false</code>,
     *            otherwise
     * @return The number of removed subscriptions and publications
     * @throws OXException
     */
    private void deleteDependentEntities(Connection wcon, FolderObject folder, boolean handDown) throws OXException {
        if (null == wcon) {
            Connection wc = null;
            try {
                wc = DBPool.pickupWriteable(ctx);
                OXFolderDependentDeleter.folderDeleted(wc, session, folder, handDown);
            } finally {
                if (null != wc) {
                    DBPool.closeWriterSilent(ctx, wc);
                }
            }
        } else {
            OXFolderDependentDeleter.folderDeleted(wcon, session, folder, handDown);
        }
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
                    LOG.error("Folder delete listener \"{}\" failed for folder {} int context {}", next.getClass().getName(), folderID, ctx.getContextId(), e);
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
            deleteDependentEntities(wc, storageFolder, false);
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
            infostoreFacade = new EventFiringInfostoreFacadeImpl(new DBPoolProvider());
        } else {
            infostoreFacade = new EventFiringInfostoreFacadeImpl(new StaticDBPoolProvider(writeCon));
            infostoreFacade.setCommitsTransaction(false);
        }
        infostoreFacade.setTransactional(true);
        infostoreFacade.startTransaction();
        try {
            infostoreFacade.removeDocument(folderID, FileStorageFileAccess.DISTANT_FUTURE, ServerSessionAdapter.valueOf(session, ctx));
            infostoreFacade.commit();
        } catch (final OXException x) {
            infostoreFacade.rollback();
            if (InfostoreExceptionCodes.CURRENTLY_LOCKED.equals(x)) {
                throw OXFolderExceptionCode.DELETE_FAILED_LOCKED_DOCUMENTS.create(x, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
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
            throw OXFolderExceptionCode.NO_SHARED_FOLDER_DELETION.create(userId, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check if marked as default folder
         */
        if (delFolder.isDefaultFolder()) {
            throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_DELETION.create(userId, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check user's effective permission
         */
        final EffectivePermission effectivePerm = getOXFolderAccess().getFolderPermission(folderID, userId, userPerms);
        if (!effectivePerm.isFolderVisible()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                if (initParent == folderID) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderID), userId, Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.HIDDEN_FOLDER_ON_DELETION.create(Integer.valueOf(initParent), Integer.valueOf(ctx.getContextId()), userId);
            }
            if (initParent == folderID) {
                throw OXFolderExceptionCode.NOT_VISIBLE.create(CATEGORY_PERMISSION_DENIED, Integer.valueOf(folderID), userId, Integer.valueOf(ctx.getContextId()));
            }
            throw OXFolderExceptionCode.HIDDEN_FOLDER_ON_DELETION.create(CATEGORY_PERMISSION_DENIED, Integer.valueOf(initParent), Integer.valueOf(ctx.getContextId()), userId);
        }
        if (!effectivePerm.isFolderAdmin()) {
            if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(userId, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
            }
            throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(CATEGORY_PERMISSION_DENIED, userId, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check delete permission on folder's objects
         */
        if (!getOXFolderAccess().canDeleteAllObjectsInFolder(delFolder, session, ctx)) {
            throw OXFolderExceptionCode.NOT_ALL_OBJECTS_DELETION.create(userId, Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
        }
        /*
         * Check for special folder
         */
        for (final Integer special : specials) {
            if (null != special && special.intValue() == folderID) {
                throw OXFolderExceptionCode.DELETE_DENIED.create(Integer.valueOf(folderID), Integer.valueOf(ctx.getContextId()));
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

    private static User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
    }

    private static OXException handleIncorrectStringError(IncorrectStringSQLException e, Session session) throws OXException {
        String column = e.getColumn();
        if (null == column) {
            return OXFolderExceptionCode.INVALID_CHARACTER_SIMPLE.create(e);
        }

        String displayName = OXFolderUtility.column2Field(column);
        if (null == displayName) {
            return OXFolderExceptionCode.INVALID_CHARACTER.create(e, e.getIncorrectString(), e.getColumn());
        }
        if (null == session) {
            return OXFolderExceptionCode.INVALID_CHARACTER.create(e, e.getIncorrectString(), displayName);
        }

        String translatedName = StringHelper.valueOf(getUser(session).getLocale()).getString(displayName);
        OXException oxe = OXFolderExceptionCode.INVALID_CHARACTER.create(e, e.getIncorrectString(), translatedName);
        oxe.addProblematic(new SimpleIncorrectStringAttribute(fieldMapping.get(column).intValue(), e.getIncorrectString()));
        return oxe;
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
