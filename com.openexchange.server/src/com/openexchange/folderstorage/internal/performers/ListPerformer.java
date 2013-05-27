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

package com.openexchange.folderstorage.internal.performers;

import static com.openexchange.server.services.ServerServiceRegistry.getInstance;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import org.apache.commons.logging.Log;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.internal.RdbMailAccountStorage;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListPerformer} - Serves the <code>LIST</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListPerformer extends AbstractUserizedFolderPerformer {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ListPerformer.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    protected static final FolderType FOLDER_TYPE_MAIL = MailFolderType.getInstance();

    /**
     * Initializes a new {@link ListPerformer} from given session.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     */
    public ListPerformer(final ServerSession session, final FolderServiceDecorator decorator) {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link ListPerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public ListPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link ListPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public ListPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link ListPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public ListPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>LIST</code> request.
     *
     * @param treeId The tree identifier
     * @param parentId The parent folder identifier
     * @param all <code>true</code> to get all subfolders regardless of their subscription status; otherwise <code>false</code> to only get
     *            subscribed ones
     * @return The user-sensitive subfolders
     * @throws OXException If a folder error occurs
     */
    public UserizedFolder[] doList(final String treeId, final String parentId, final boolean all) throws OXException {
        return doList(treeId, parentId, all, false);
    }

    /**
     * Performs the <code>LIST</code> request.
     *
     * @param treeId The tree identifier
     * @param parentId The parent folder identifier
     * @param all <code>true</code> to get all subfolders regardless of their subscription status; otherwise <code>false</code> to only get
     *            subscribed ones
     * @return The user-sensitive subfolders
     * @throws OXException If a folder error occurs
     */
    protected UserizedFolder[] doList(final String treeId, final String parentId, final boolean all, final boolean checkOnly) throws OXException {
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        final long start = DEBUG ? System.currentTimeMillis() : 0L;
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (folderStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(folderStorage);
        }
        try {
            final UserizedFolder[] ret = doList(treeId, parentId, all, openedStorages, checkOnly);
            for (final UserizedFolder userizedFolder : ret) {
                userizedFolder.setParentID(parentId);
            }
            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
            if (DEBUG) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new com.openexchange.java.StringAllocator().append("List.doList() took ").append(duration).append("msec for parent folder: ").append(
                    parentId).toString());
            }
            return ret;
        } catch (final OXException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw e;
        } catch (final RuntimeException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs the <code>LIST</code> request.
     *
     * @param treeId The tree identifier
     * @param parentId The parent folder identifier
     * @param all <code>true</code> to get all subfolders regardless of their subscription status; otherwise <code>false</code> to only get
     *            subscribed ones
     * @param startTransaction <code>true</code> to start own transaction; otherwise <code>false</code> if invoked from another action class
     * @return The user-sensitive subfolders
     * @throws OXException If a folder error occurs
     */
    UserizedFolder[] doList(final String treeId, final String parentId, final boolean all, final java.util.Collection<FolderStorage> openedStorages, final boolean checkOnly) throws OXException {
        final FolderStorage folderStorage = getOpenedStorage(parentId, treeId, storageParameters, openedStorages);
        final UserizedFolder[] ret;
        try {
            final Folder parent = folderStorage.getFolder(treeId, parentId, storageParameters);
            {
                /*
                 * Check folder permission for parent folder
                 */
                final Permission parentPermission = CalculatePermission.calculate(parent, this, getAllowedContentTypes());
                if (!parentPermission.isVisible()) {
                    throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(
                        getFolderInfo4Error(parent),
                        getUserInfo4Error(),
                        getContextInfo4Error());
                }
            }
            /*
             * Get subfolder identifiers from folder itself
             */
            final String[] subfolderIds = parent.getSubfolderIDs();
            if (null == subfolderIds) {
                /*
                 * Need to get user-visible subfolders from appropriate storage
                 */
                ret = getSubfoldersFromStorages(treeId, parentId, all, checkOnly);
            } else {
                if (0 == subfolderIds.length) {
                    return new UserizedFolder[0];
                }
                /*
                 * The subfolders can be completely fetched from already opened parent's folder storage
                 */
                if (MailProperties.getInstance().isHidePOP3StorageFolders() && FOLDER_TYPE_MAIL.servesFolderId(parentId)) {
                    final FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(parentId);
                    if (MailAccount.DEFAULT_ID == argument.getAccountId()) {
                        final List<String> l = new ArrayList<String>(Arrays.asList(subfolderIds));
                        final Set<String> pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
                        for (final Iterator<String> it = l.iterator(); it.hasNext();) {
                            if (pop3StorageFolders.contains(it.next())) {
                                it.remove();
                            }
                        }
                    }
                }
                /*
                 * Collect by folder storage
                 */
                final Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
                for (int i = 0; i < subfolderIds.length; i++) {
                    final String id = subfolderIds[i];
                    final FolderStorage tmp = folderStorageDiscoverer.getFolderStorage(treeId, id);
                    if (null == tmp) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
                    }
                    TIntList list = map.get(tmp);
                    if (null == list) {
                        list = new TIntArrayList();
                        map.put(tmp, list);
                    }
                    list.add(i);
                }
                /*
                 * Process by folder storage
                 */
                final UserizedFolder[] subfolders = new UserizedFolder[subfolderIds.length];
                final CompletionService<Object> completionService;
                final StorageParametersProvider paramsProvider;
                if (1 == map.size()) {
                    completionService = new CallerRunsCompletionService<Object>();
                    paramsProvider = new InstanceStorageParametersProvider(storageParameters);
                } else {
                    completionService = new ThreadPoolCompletionService<Object>(getInstance().getService(ThreadPoolService.class, true));
                    paramsProvider = null == session ? new SessionStorageParametersProvider(user, context) : new SessionStorageParametersProvider(session);
                }
                final AbstractPerformer performer = this;
                int taskCount = 0;
                for (final Entry<FolderStorage, TIntList> entry : map.entrySet()) {
                    final FolderStorage tmp = entry.getKey();
                    final int[] indexes = entry.getValue().toArray();
                    final Log log = LOG;
                    completionService.submit(new ThreadPools.TrackableCallable<Object>() {

                        @Override
                        public Object call() throws OXException {
                            final StorageParameters newParameters = paramsProvider.getStorageParameters();
                            final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                            if (tmp.startTransaction(newParameters, false)) {
                                openedStorages.add(tmp);
                            }
                            try {
                                /*
                                 * Try to batch-load the folders
                                 */
                                List<Folder> folders;
                                try {
                                    final List<String> ids = new ArrayList<String>(indexes.length);
                                    for (final int index : indexes) {
                                        ids.add(subfolderIds[index]);
                                    }
                                    folders = tmp.getFolders(treeId, ids, newParameters);
                                    final Set<OXException> warnings = newParameters.getWarnings();
                                    if (!warnings.isEmpty()) {
                                        addWarning(warnings.iterator().next());
                                    }
                                } catch (final OXException e) {
                                    /*
                                     * Batch-load failed...
                                     */
                                    if (log.isWarnEnabled()) {
                                        log.warn("Batch loading of folder failed. Fall-back to one-by-one loading.", e);
                                    }
                                    folders = null;
                                }
                                if (null == folders) {
                                    /*
                                     * Load them one-by-one
                                     */
                                    NextIndex: for (final int index : indexes) {
                                        final String id = subfolderIds[index];
                                        /*
                                         * Get subfolder from appropriate storage
                                         */
                                        final Folder subfolder;
                                        try {
                                            subfolder = tmp.getFolder(treeId, id, newParameters);
                                        } catch (final OXException e) {
                                            log.warn(
                                                new com.openexchange.java.StringAllocator(128).append("The folder with ID \"").append(id).append("\" in tree \"").append(treeId).append(
                                                    "\" could not be fetched from storage \"").append(tmp.getClass().getSimpleName()).append('"').toString(),
                                                e);
                                            addWarning(e);
                                            continue NextIndex;
                                        }
                                        /*
                                         * Check for subscribed status dependent on parameter "all"
                                         */
                                        if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                            final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                            if (userPermission.isVisible()) {
                                                subfolders[index] =
                                                    getUserizedFolder(subfolder, userPermission, treeId, all, true, newParameters, openedStorages, checkOnly);
                                            }
                                        }
                                    }
                                } else {
                                    /*
                                     * Convert to userized folders and put into array
                                     */
                                    final int size = folders.size();
                                    int j = 0;
                                    for (final int index : indexes) {
                                        if (j < size) {
                                            final Folder subfolder = folders.get(j++);
                                            /*
                                             * Check for subscribed status dependent on parameter "all"
                                             */
                                            if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                                final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                                if (userPermission.isVisible()) {
                                                    subfolders[index] =
                                                        getUserizedFolder(subfolder, userPermission, treeId, all, true, newParameters, openedStorages, checkOnly);
                                                }
                                            }
                                        }
                                    }
                                }
                                /*
                                 * Commit
                                 */
                                for (final FolderStorage fs : openedStorages) {
                                    fs.commitTransaction(newParameters);
                                }
                                return null;
                            } catch (final OXException e) {
                                for (final FolderStorage fs : openedStorages) {
                                    fs.rollback(newParameters);
                                }
                                throw e;
                            } catch (final RuntimeException e) {
                                for (final FolderStorage fs : openedStorages) {
                                    fs.rollback(newParameters);
                                }
                                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                            }
                        }
                    });
                    taskCount++;
                }
                /*
                 * Wait for completion
                 */
                ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
                ret = trimArray(subfolders);
            }
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return ret;
    }

    private UserizedFolder[] getSubfoldersFromStorages(final String treeId, final String parentId, final boolean all, final boolean checkOnly) throws OXException {
        /*
         * Determine needed storages for given parent
         */
        final FolderStorage[] neededStorages = folderStorageDiscoverer.getFolderStoragesForParent(treeId, parentId);
        if (null == neededStorages || 0 == neededStorages.length) {
            return new UserizedFolder[0];
        }
        final List<SortableId> allSubfolderIds;
        if (1 == neededStorages.length) {
            final FolderStorage neededStorage = neededStorages[0];
            final boolean started = neededStorage.startTransaction(storageParameters, false);
            try {
                allSubfolderIds = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                if (started) {
                    neededStorage.commitTransaction(storageParameters);
                }
            } catch (final OXException e) {
                if (started) {
                    neededStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final RuntimeException e) {
                if (started) {
                    neededStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else {
            allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);
            final CompletionService<List<SortableId>> completionService =
                    new ThreadPoolCompletionService<List<SortableId>>(getInstance().getService(ThreadPoolService.class, true));
            /*
             * Get all visible subfolders from each storage
             */
            for (final FolderStorage neededStorage : neededStorages) {
                completionService.submit(new ThreadPools.TrackableCallable<List<SortableId>>() {

                    @Override
                    public List<SortableId> call() throws OXException {
                        final StorageParameters newParameters = newStorageParameters();
                        final boolean started = neededStorage.startTransaction(newParameters, false);
                        try {
                            final List<SortableId> l;
                            if (MailProperties.getInstance().isHidePOP3StorageFolders() && FOLDER_TYPE_MAIL.servesFolderId(parentId)) {
                                l = new ArrayList<SortableId>(Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters)));
                                final FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(parentId);
                                if (MailAccount.DEFAULT_ID == argument.getAccountId()) {
                                    final Set<String> pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
                                    for (final Iterator<SortableId> it = l.iterator(); it.hasNext();) {
                                        if (pop3StorageFolders.contains(it.next().getId())) {
                                            it.remove();
                                        }
                                    }
                                }
                            } else {
                                l = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
                            }
                            if (started) {
                                neededStorage.commitTransaction(newParameters);
                            }
                            return l;
                        } catch (final OXException e) {
                            if (started) {
                                neededStorage.rollback(newParameters);
                            }
                            addWarning(e);
                            return Collections.<SortableId> emptyList();
                        } catch (final RuntimeException e) {
                            if (started) {
                                neededStorage.rollback(newParameters);
                            }
                            final OXException OXException = FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                            addWarning(OXException);
                            return Collections.<SortableId> emptyList();
                        }
                    }
                });
            }
            /*
             * Wait for completion
             */
            final List<List<SortableId>> results =
                ThreadPools.takeCompletionService(completionService, neededStorages.length, FACTORY);
            for (final List<SortableId> result : results) {
                allSubfolderIds.addAll(result);
            }
            /*
             * All failed with a warning?
             */
            if (!results.isEmpty() && (results.size() == getNumOfWarnings())) {
                /*
                 * Throw first warning in set
                 */
                final OXException e = getWarnings().iterator().next();
                e.addCategory(Category.CATEGORY_ERROR);
                throw e;
            }
        }
        /*
         * Sort them
         */
        Collections.sort(allSubfolderIds);
        final int size = allSubfolderIds.size();
        final UserizedFolder[] subfolders = new UserizedFolder[size];
        /*
         * Get corresponding user-sensitive folders
         */

        /*
         * Collect by folder storage
         */
        final Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
        for (int i = 0; i < size; i++) {
            final String id = allSubfolderIds.get(i).getId();
            final FolderStorage tmp = folderStorageDiscoverer.getFolderStorage(treeId, id);
            if (null == tmp) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
            }
            TIntList list = map.get(tmp);
            if (null == list) {
                list = new TIntArrayList();
                map.put(tmp, list);
            }
            list.add(i);
        }
        /*
         * Process by folder storage
         */
        final CompletionService<Object> completionService;
        final StorageParametersProvider paramsProvider;
        if (1 == map.size()) {
            completionService = new CallerRunsCompletionService<Object>();
            paramsProvider = new InstanceStorageParametersProvider(storageParameters);
        } else {
            completionService = new ThreadPoolCompletionService<Object>(getInstance().getService(ThreadPoolService.class, true));
            paramsProvider = null == session ? new SessionStorageParametersProvider(user, context) : new SessionStorageParametersProvider(session);
        }
        final AbstractPerformer performer = this;
        int taskCount = 0;
        for (final Entry<FolderStorage, TIntList> entry : map.entrySet()) {
            final FolderStorage tmp = entry.getKey();
            final int[] indexes = entry.getValue().toArray();
            final Log log = LOG;
            completionService.submit(new ThreadPools.TrackableCallable<Object>() {

                @Override
                public Object call() throws Exception {
                    final StorageParameters newParameters = paramsProvider.getStorageParameters();
                    final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                    if (tmp.startTransaction(newParameters, false)) {
                        openedStorages.add(tmp);
                    }
                    try {
                        /*
                         * Try to batch-load the folders
                         */
                        List<Folder> folders;
                        try {
                            final List<String> ids = new ArrayList<String>(indexes.length);
                            for (final int index : indexes) {
                                ids.add(allSubfolderIds.get(index).getId());
                            }
                            folders = tmp.getFolders(treeId, ids, newParameters);
                            final Set<OXException> warnings = newParameters.getWarnings();
                            if (!warnings.isEmpty()) {
                                addWarning(warnings.iterator().next());
                            }
                        } catch (final OXException e) {
                            /*
                             * Batch-load failed...
                             */
                            if (log.isWarnEnabled()) {
                                log.warn("Batch loading of folder failed. Fall-back to one-by-one loading.", e);
                            }
                            folders = null;
                        }
                        if (null == folders) {
                            NextIndex: for (final int index : indexes) {
                                final String id = allSubfolderIds.get(index).getId();
                                /*
                                 * Get subfolder from appropriate storage
                                 */
                                final Folder subfolder;
                                try {
                                    subfolder = tmp.getFolder(treeId, id, newParameters);
                                } catch (final OXException e) {
                                    log.warn(
                                        new com.openexchange.java.StringAllocator(128).append("The folder with ID \"").append(id).append("\" in tree \"").append(treeId).append(
                                            "\" could not be fetched from storage \"").append(tmp.getClass().getSimpleName()).append('"').toString(),
                                        e);
                                    addWarning(e);
                                    continue NextIndex;
                                }
                                /*
                                 * Check for subscribed status dependent on parameter "all"
                                 */
                                if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                    final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                    if (userPermission.isVisible()) {
                                        subfolders[index] =
                                            getUserizedFolder(subfolder, userPermission, treeId, all, true, newParameters, openedStorages, checkOnly);
                                    }
                                }
                            }
                        } else {
                            /*
                             * Convert to userized folders and put into array
                             */
                            final int size = folders.size();
                            int j = 0;
                            for (final int index : indexes) {
                                if (j < size) {
                                    final Folder subfolder = folders.get(j++);
                                    if (null != subfolder) {
                                        /*
                                         * Check for subscribed status dependent on parameter "all"
                                         */
                                        if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                            final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                            if (userPermission.isVisible()) {
                                                subfolders[index] =
                                                    getUserizedFolder(
                                                        subfolder,
                                                        userPermission,
                                                        treeId,
                                                        all,
                                                        true,
                                                        newParameters,
                                                        openedStorages,
                                                        checkOnly);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        /*
                         * Commit
                         */
                        for (final FolderStorage fs : openedStorages) {
                            fs.commitTransaction(newParameters);
                        }
                        return null;
                    } catch (final OXException e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw e;
                    } catch (final RuntimeException e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }

                }
            });
            taskCount++;
        }
        /*
         * Wait for completion
         */
        ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
        return trimArray(subfolders);
    }

    private static final ThreadPools.ExpectedExceptionFactory<OXException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<OXException>() {

            @Override
            public Class<OXException> getType() {
                return OXException.class;
            }

            @Override
            public OXException newUnexpectedError(final Throwable t) {
                return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        };

    /**
     * Creates a newly allocated array containing all elements of specified array in the same order except <code>null</code> values.
     *
     * @param userizedFolders The array to trim
     * @return A newly allocated copy-array with <code>null</code> elements removed
     */
    private static UserizedFolder[] trimArray(final UserizedFolder[] userizedFolders) {
        if (null == userizedFolders) {
            return new UserizedFolder[0];
        }
        final List<UserizedFolder> l = new ArrayList<UserizedFolder>(userizedFolders.length);
        for (final UserizedFolder uf : userizedFolders) {
            if ((null != uf) && (null != uf.getID())) {
                l.add(uf);
            }
        }
        return l.toArray(new UserizedFolder[l.size()]);
    }

}
