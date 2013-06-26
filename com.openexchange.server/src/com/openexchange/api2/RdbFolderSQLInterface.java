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

package com.openexchange.api2;

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RdbFolderSQLInterface} - The (relational) database implementation of {@link FolderSQLInterface}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbFolderSQLInterface implements FolderSQLInterface {

    private static enum FolderQuery {

        NON_TREE_VISIBLE_CALENDAR(1), NON_TREE_VISIBLE_TASK(2), NON_TREE_VISIBLE_CONTACT(3), NON_TREE_VISIBLE_INFOSTORE(4), ROOT_FOLDERS(5);

        final int queryNum;

        private FolderQuery(final int queryNum) {
            this.queryNum = queryNum;
        }

    }

    private static final int getNonTreeVisibleNum(final int module) {
        switch (module) {
        case FolderObject.CALENDAR:
            return FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum;
        case FolderObject.TASK:
            return FolderQuery.NON_TREE_VISIBLE_TASK.queryNum;
        case FolderObject.CONTACT:
            return FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum;
        case FolderObject.INFOSTORE:
            return FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum;
        default:
            return -1;
        }
    }

    /**
     * Creates a new {@link Set set} containing the modules of those folders which might not be visible in hierarchical tree view.
     *
     * @return A new {@link Set set} containing the modules.
     */
    private static final TIntSet newNonTreeVisibleModules() {
        final TIntSet retval = new TIntHashSet(4);
        retval.add(FolderObject.CALENDAR);
        retval.add(FolderObject.TASK);
        retval.add(FolderObject.CONTACT);
        retval.add(FolderObject.INFOSTORE);
        return retval;
    }

    private static final int[] VIRTUAL_IDS = {
        FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
        FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID };

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(RdbFolderSQLInterface.class));

    private final int userId;
    private final int[] groups;
    private final ServerSession session;
    private final Context ctx;
    private final User user;
    private final UserConfiguration userConfiguration;
    private final OXFolderAccess oxfolderAccess;

    /**
     * @param session
     */
    public RdbFolderSQLInterface(final ServerSession session) {
        this(session, null);
    }

    public RdbFolderSQLInterface(final ServerSession session, final OXFolderAccess oxfolderAccess) {
        super();
        this.session = session;
        this.ctx = session.getContext();
        userConfiguration = session.getUserConfiguration();
        user = session.getUser();
        this.userId = user.getId();
        this.groups = user.getGroups();
        this.oxfolderAccess = oxfolderAccess == null ? new OXFolderAccess(session.getContext()) : oxfolderAccess;
    }

    @Override
    public FolderObject getUsersInfostoreFolder() throws OXException {
        if (!userConfiguration.hasInfostore()) {
            throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                getUserName(session),
                folderModule2String(FolderObject.INFOSTORE),
                Integer.valueOf(ctx.getContextId()));
        }
        return oxfolderAccess.getDefaultFolder(userId, FolderObject.INFOSTORE);
    }

    @Override
    public FolderObject getFolderById(final int id) throws OXException {
        final int pos = Arrays.binarySearch(VIRTUAL_IDS, id);
        if (pos >= 0) {
            final FolderObject fo =
                FolderObject.createVirtualFolderObject(
                    id,
                    FolderObject.getFolderString(id, session.getUser().getLocale()),
                    FolderObject.SYSTEM_MODULE,
                    true,
                    FolderObject.SYSTEM_TYPE);
            if (3 == pos) {
                fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
            } else {
                fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            }
            if (FolderCacheManager.isInitialized()) {
                FolderCacheManager.getInstance().putFolderObject(fo, ctx);
            }
            return fo;
        }
        try {
            final FolderObject fo = oxfolderAccess.getFolderObject(id);
            final EffectivePermission perm = fo.getEffectiveUserPermission(userId, userConfiguration);
            if (!perm.isFolderVisible()) {
                if (!perm.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(
                        Integer.valueOf(id),
                        getUserName(session),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(
                    Integer.valueOf(id),
                    getUserName(session),
                    Integer.valueOf(ctx.getContextId()));
            } else if (fo.isShared(session.getUserId()) && !userConfiguration.hasFullSharedFolderAccess()) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_ACCESS.create(
                    getUserName(session),
                    getFolderName(id, ctx),
                    Integer.valueOf(ctx.getContextId()));
            } else if (Arrays.binarySearch(userConfiguration.getAccessibleModules(), fo.getModule()) < 0) {
                throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                    getUserName(session),
                    folderModule2String(fo.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
            return fo;
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

    @Override
    public FolderObject saveFolderObject(final FolderObject folderobjectArg, final Date clientLastModified) throws OXException {
        if (folderobjectArg.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess() && (!folderobjectArg.containsModule() || folderobjectArg.getModule() != FolderObject.INFOSTORE)) {
            throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(
                getUserName(session),
                (folderobjectArg.containsObjectID() && folderobjectArg.getObjectID() > 0 ? getFolderName(folderobjectArg) : ""),
                Integer.valueOf(ctx.getContextId()));
        }
        final FolderObject folderobject = folderobjectArg;
        final boolean insert = (!folderobject.containsObjectID() || folderobject.getObjectID() == -1);
        final OXFolderManager manager = OXFolderManager.getInstance(session, oxfolderAccess);
        try {
            if (insert) {
                if (!folderobject.containsParentFolderID()) {
                    throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(FolderChildFields.FOLDER_ID, Integer.valueOf(ctx.getContextId()));
                }
                final int parentFolderID = folderobject.getParentFolderID();
                if (parentFolderID == FolderObject.SYSTEM_PUBLIC_FOLDER_ID && !userConfiguration.hasFullPublicFolderAccess()) {
                    throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(
                        getUserName(session),
                        (folderobjectArg.containsObjectID() && folderobjectArg.getObjectID() > 0 ? getFolderName(folderobjectArg) : ""),
                        Integer.valueOf(ctx.getContextId()));
                }
                final int[] virtualIDs =
                    new int[] {
                        FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
                        FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID };
                if (Arrays.binarySearch(virtualIDs, parentFolderID) > -1) {
                    throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(
                        getUserName(session),
                        getFolderName(parentFolderID, ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                final FolderObject parentFolder = oxfolderAccess.getFolderObject(parentFolderID);
                final EffectivePermission parentalEffectivePerm = parentFolder.getEffectiveUserPermission(userId, userConfiguration);
                if (!parentalEffectivePerm.hasModuleAccess(folderobject.getModule())) {
                    throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                        getUserName(session),
                        folderModule2String(folderobject.getModule()),
                        Integer.valueOf(ctx.getContextId()));
                }
                if (!parentalEffectivePerm.isFolderVisible()) {
                    if (!parentalEffectivePerm.getUnderlyingPermission().isFolderVisible()) {
                        throw OXFolderExceptionCode.NOT_VISIBLE.create(
                            Integer.valueOf(parentFolderID),
                            getUserName(session),
                            Integer.valueOf(ctx.getContextId()));
                    }
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(
                        Integer.valueOf(parentFolderID),
                        getUserName(session),
                        Integer.valueOf(ctx.getContextId()));
                }
                if (!parentalEffectivePerm.canCreateSubfolders()) {
                    if (!parentalEffectivePerm.getUnderlyingPermission().canCreateSubfolders()) {
                        throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(
                            getUserName(session),
                            getFolderName(parentFolderID, ctx),
                            Integer.valueOf(ctx.getContextId()));
                    }
                    throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(
                        getUserName(session),
                        getFolderName(parentFolderID, ctx),
                        Integer.valueOf(ctx.getContextId()));
                }
                folderobject.setType(getFolderType(parentFolderID));
                final long createTime = System.currentTimeMillis();
                manager.createFolder(folderobject, false, createTime);
            } else {
                folderobject.fill(oxfolderAccess.getFolderObject(folderobject.getObjectID()), false);
                if (!folderobject.exists(ctx)) {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderobject.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
                if (clientLastModified != null && oxfolderAccess.getFolderLastModified(folderobject.getObjectID()).after(clientLastModified)) {
                    throw OXFolderExceptionCode.CONCURRENT_MODIFICATION.create(Integer.valueOf(folderobject.getObjectID()), Integer.valueOf(ctx.getContextId()));
                }
                final EffectivePermission effectivePerm =
                    oxfolderAccess.getFolderPermission(folderobject.getObjectID(), userId, userConfiguration);
                if (!effectivePerm.hasModuleAccess(folderobject.getModule())) {
                    throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                        getUserName(session),
                        folderModule2String(folderobject.getModule()),
                        Integer.valueOf(ctx.getContextId()));
                }
                if (!effectivePerm.isFolderVisible()) {
                    if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                        throw OXFolderExceptionCode.NOT_VISIBLE.create(
                            Integer.valueOf(folderobject.getObjectID()),
                            getUserName(session),
                            Integer.valueOf(ctx.getContextId()));
                    }
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(
                        Integer.valueOf(folderobject.getObjectID()),
                        getUserName(session),
                        Integer.valueOf(ctx.getContextId()));
                }
                if (!effectivePerm.isFolderAdmin()) {
                    if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
                        throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                            getUserName(session),
                            getFolderName(folderobject),
                            Integer.valueOf(ctx.getContextId()));
                    }
                    throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                        getUserName(session),
                        getFolderName(folderobject),
                        Integer.valueOf(ctx.getContextId()));
                }
                folderobject.getObjectID();
                final long lastModfified = System.currentTimeMillis();
                manager.updateFolder(folderobject, false, false, lastModfified);
                {
                    CacheFolderStorage.getInstance().removeSingleFromCache(Integer.toString(folderobject.getObjectID()), FolderStorage.REAL_TREE_ID, userId, session, false);
                    CacheFolderStorage.getInstance().removeSingleFromCache(Integer.toString(folderobject.getParentFolderID()), FolderStorage.REAL_TREE_ID, userId, session, false);
                }
            }
            return folderobject;
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

    @Override
    public int deleteFolderObject(final FolderObject folderobject, final Date clientLastModified) throws OXException {
        try {
            final int folderId = folderobject.getObjectID();
            final int pos = Arrays.binarySearch(VIRTUAL_IDS, folderId);
            if (pos >= 0) {
                final FolderObject fo =
                    FolderObject.createVirtualFolderObject(
                        folderId,
                        FolderObject.getFolderString(folderId, session.getUser().getLocale()),
                        FolderObject.SYSTEM_MODULE,
                        true,
                        FolderObject.SYSTEM_TYPE);
                if (3 == pos) {
                    fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
                } else {
                    fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
                }
                if (FolderCacheManager.isInitialized()) {
                    FolderCacheManager.getInstance().putFolderObject(fo, ctx);
                }
                folderobject.fill(fo);
            } else {
                folderobject.fill(oxfolderAccess.getFolderObject(folderId), false);
            }
            final int module = folderobject.getModule();
            if (FolderObject.PUBLIC == folderobject.getType() && FolderObject.INFOSTORE != module && !userConfiguration.hasFullPublicFolderAccess()) {
                throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(
                    getUserName(session),
                    getFolderName(folderobject),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!folderobject.exists(ctx)) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
            }
            if (clientLastModified != null && oxfolderAccess.getFolderLastModified(folderId).after(clientLastModified)) {
                throw OXFolderExceptionCode.CONCURRENT_MODIFICATION.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
            }
            final EffectivePermission effectivePerm = folderobject.getEffectiveUserPermission(userId, userConfiguration);
            if (!effectivePerm.hasModuleAccess(module)) {
                throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                    getUserName(session),
                    folderModule2String(module),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!effectivePerm.isFolderVisible()) {
                if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(
                        Integer.valueOf(folderId),
                        getUserName(session),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(
                    Integer.valueOf(folderId),
                    getUserName(session),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!effectivePerm.isFolderAdmin()) {
                if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
                    throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                        getUserName(session),
                        getFolderName(folderobject),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                    getUserName(session),
                    getFolderName(folderobject),
                    Integer.valueOf(ctx.getContextId()));
            }
            final long lastModified = System.currentTimeMillis();
            OXFolderManager.getInstance(session, oxfolderAccess).deleteFolder(folderobject, false, lastModified);
            {
                CacheFolderStorage.getInstance().removeSingleFromCache(Integer.toString(folderId), FolderStorage.REAL_TREE_ID, userId, session, true);
                CacheFolderStorage.getInstance().removeSingleFromCache(Integer.toString(folderobject.getParentFolderID()), FolderStorage.REAL_TREE_ID, userId, session, false);
            }
            return folderId;
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Fills folder query cache with "non-tree-visible" folders which applies to given session
     *
     * @param session The session to which the query applies
     * @throws SearchIteratorException If iterator fails
     * @throws OXException If a caching error occurs
     */
    private static final void loadNonTreeVisibleFoldersIntoQueryCache(final ServerSession session, final Context ctx, final UserConfiguration userConfiguration) throws SearchIteratorException, OXException {
        /*
         * Fetch queue from iterator (which implicitly puts referenced objects into cache!)
         */
        final int userId;
        final int[] groups;
        {
            final User u = session.getUser();
            userId = u.getId();
            groups = u.getGroups();
        }
        final Queue<FolderObject> q =
            ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersNotSeenInTreeView(userId, groups, userConfiguration, ctx)).asQueue();
        final int size = q.size();
        final Iterator<FolderObject> iter = q.iterator();
        final TIntSet stdModules = newNonTreeVisibleModules();
        /*
         * Iterate result queue
         */
        int prevModule = -1;
        final LinkedList<Integer> cacheQueue = new LinkedList<Integer>();
        for (int i = 0; i < size; i++) {
            final FolderObject f = iter.next();
            if (prevModule != f.getModule()) {
                FolderQueryCacheManager.getInstance().putFolderQuery(getNonTreeVisibleNum(prevModule), cacheQueue, session, false);
                prevModule = f.getModule();
                stdModules.remove(prevModule);
                cacheQueue.clear();
            }
            cacheQueue.add(Integer.valueOf(f.getObjectID()));
        }
        FolderQueryCacheManager.getInstance().putFolderQuery(getNonTreeVisibleNum(prevModule), cacheQueue, session, false);
        final int setSize = stdModules.size();
        final TIntIterator iter2 = stdModules.iterator();
        for (int i = setSize; i-- > 0;) {
            FolderQueryCacheManager.getInstance().putFolderQuery(
                getNonTreeVisibleNum(iter2.next()),
                new LinkedList<Integer>(),
                session,
                false);
        }
    }

    @Override
    public SearchIterator<FolderObject> getNonTreeVisiblePublicCalendarFolders() throws OXException {
        if (!userConfiguration.hasCalendar()) {
            throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                getUserName(session),
                folderModule2String(FolderObject.CALENDAR),
                Integer.valueOf(ctx.getContextId()));
        }
        LinkedList<Integer> result;
        if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum, session)) == null) {
            loadNonTreeVisibleFoldersIntoQueryCache(session, ctx, userConfiguration);
            result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum, session);
        }
        return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
    }

    @Override
    public SearchIterator<FolderObject> getNonTreeVisiblePublicTaskFolders() throws OXException {
        if (!userConfiguration.hasTask()) {
            throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                getUserName(session),
                folderModule2String(FolderObject.TASK),
                Integer.valueOf(ctx.getContextId()));
        }
        LinkedList<Integer> result;
        if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_TASK.queryNum, session)) == null) {
            loadNonTreeVisibleFoldersIntoQueryCache(session, ctx, userConfiguration);
            result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_TASK.queryNum, session);
        }
        return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
    }

    @Override
    public SearchIterator<FolderObject> getNonTreeVisiblePublicContactFolders() throws OXException {
        if (!userConfiguration.hasContact()) {
            throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                getUserName(session),
                folderModule2String(FolderObject.CONTACT),
                Integer.valueOf(ctx.getContextId()));
        }
        LinkedList<Integer> result;
        if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum, session)) == null) {
            loadNonTreeVisibleFoldersIntoQueryCache(session, ctx, userConfiguration);
            result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum, session);
        }
        return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
    }

    @Override
    public SearchIterator<FolderObject> getNonTreeVisiblePublicInfostoreFolders() throws OXException {
        if (!userConfiguration.hasInfostore()) {
            throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                getUserName(session),
                folderModule2String(FolderObject.INFOSTORE),
                Integer.valueOf(ctx.getContextId()));
        }
        LinkedList<Integer> result;
        if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum, session)) == null) {
            loadNonTreeVisibleFoldersIntoQueryCache(session, ctx, userConfiguration);
            result = FolderQueryCacheManager.getInstance().getFolderQuery(FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum, session);
        }
        return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
    }

    @Override
    public SearchIterator<FolderObject> getRootFolderForUser() throws OXException {
        return OXFolderIteratorSQL.getUserRootFoldersIterator(userId, groups, userConfiguration, ctx);
    }

    /**
     * @param folderIdArg
     * @return
     */
    private final int getFolderType(final int folderIdArg) throws OXException {
        int type = -1;
        int folderId = folderIdArg;
        /*
         * Special treatment for system folders
         */
        if (folderId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
            folderId = FolderObject.SYSTEM_PRIVATE_FOLDER_ID;
            type = FolderObject.SHARED;
        } else if (folderId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
            type = FolderObject.PRIVATE;
        } else if (folderId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID || folderId == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID || folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            type = FolderObject.PUBLIC;
        } else if (folderId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
            type = FolderObject.PROJECT;
        } else {
            type = oxfolderAccess.getFolderType(folderId);
        }
        return type;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.FolderSQLInterface#getSubfolders(int, java.sql.Timestamp)
     */
    @Override
    public SearchIterator<FolderObject> getSubfolders(final int parentId, final Timestamp since) throws OXException {
        try {
            if (parentId == FolderObject.SYSTEM_SHARED_FOLDER_ID && !userConfiguration.hasFullSharedFolderAccess()) {
                throw OXFolderExceptionCode.NO_SHARED_FOLDER_ACCESS.create(
                    getUserName(session),
                    FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, user.getLocale()),
                    Integer.valueOf(ctx.getContextId()));
            } else if (oxfolderAccess.isFolderShared(parentId, userId)) {
                return FolderObjectIterator.EMPTY_FOLDER_ITERATOR;
            }
            return OXFolderIteratorSQL.getVisibleSubfoldersIterator(parentId, userId, groups, ctx, userConfiguration, since);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchIterator<FolderObject> getSharedFoldersFrom(final int owner, final Timestamp since) throws OXException {
        if (!userConfiguration.hasFullSharedFolderAccess()) {
            throw OXFolderExceptionCode.NO_SHARED_FOLDER_ACCESS.create(getUserName(session), Integer.valueOf(ctx.getContextId()));
        }
        return OXFolderIteratorSQL.getVisibleSharedFolders(
            userId,
            groups,
            userConfiguration.getAccessibleModules(),
            owner,
            ctx,
            since,
            null);
    }

    @Override
    public SearchIterator<FolderObject> getPathToRoot(final int folderId) throws OXException {
        return OXFolderIteratorSQL.getFoldersOnPathToRoot(folderId, userId, userConfiguration, user.getLocale(), ctx);
    }

    @Override
    public SearchIterator<FolderObject> getDeletedFolders(final Date since) throws OXException {
        return OXFolderIteratorSQL.getDeletedFoldersSince(since, userId, groups, userConfiguration.getAccessibleModules(), ctx);
    }

    @Override
    public SearchIterator<FolderObject> getModifiedUserFolders(final Date since) throws OXException {
        return OXFolderIteratorSQL.getModifiedFoldersSince(
            since == null ? new Date(0) : since,
            userId,
            groups,
            userConfiguration.getAccessibleModules(),
            false,
            ctx);
    }

    @Override
    public SearchIterator<FolderObject> getAllModifiedFolders(final Date since) throws OXException {
        return OXFolderIteratorSQL.getAllModifiedFoldersSince(since == null ? new Date(0) : since, ctx);
    }

    @Override
    public int clearFolder(final FolderObject folderobject, final Date clientLastModified) throws OXException {
        try {
            final int objectID = folderobject.getObjectID();
            final int pos = Arrays.binarySearch(VIRTUAL_IDS, objectID);
            if (pos >= 0) {
                final FolderObject fo =
                    FolderObject.createVirtualFolderObject(
                        objectID,
                        FolderObject.getFolderString(objectID, session.getUser().getLocale()),
                        FolderObject.SYSTEM_MODULE,
                        true,
                        FolderObject.SYSTEM_TYPE);
                if (3 == pos) {
                    fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
                } else {
                    fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
                }
                if (FolderCacheManager.isInitialized()) {
                    FolderCacheManager.getInstance().putFolderObject(fo, ctx);
                }
                folderobject.fill(fo);
            }
            if (folderobject.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess()) {
                throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(
                    getUserName(session, user),
                    getFolderName(folderobject),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!folderobject.exists(ctx)) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(objectID), Integer.valueOf(ctx.getContextId()));
            }
            if (clientLastModified != null && oxfolderAccess.getFolderLastModified(objectID).after(clientLastModified)) {
                throw OXFolderExceptionCode.CONCURRENT_MODIFICATION.create(Integer.valueOf(objectID), Integer.valueOf(ctx.getContextId()));
            }
            final EffectivePermission effectivePerm = folderobject.getEffectiveUserPermission(userId, userConfiguration);
            if (!effectivePerm.hasModuleAccess(folderobject.getModule())) {
                throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(
                    getUserName(session, user),
                    folderModule2String(folderobject.getModule()),
                    Integer.valueOf(ctx.getContextId()));
            }
            if (!effectivePerm.isFolderVisible()) {
                if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(
                        Integer.valueOf(objectID),
                        getUserName(session, user),
                        Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(
                    Integer.valueOf(objectID),
                    getUserName(session, user),
                    Integer.valueOf(ctx.getContextId()));
            }
            final long lastModified = System.currentTimeMillis();
            OXFolderManager.getInstance(session, oxfolderAccess).clearFolder(folderobject, false, lastModified);
            return objectID;
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

    private static final Queue<FolderObject> int2folder(final Queue<Integer> iq, final OXFolderAccess oxfolderAccess) {
        final Queue<FolderObject> retval = new LinkedList<FolderObject>();
        final int size = iq.size();
        final Iterator<Integer> iter = iq.iterator();
        for (int i = 0; i < size; i++) {
            try {
                retval.add(oxfolderAccess.getFolderObject(iter.next().intValue()));
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
                continue;
            }
        }
        return retval;
    }

}
