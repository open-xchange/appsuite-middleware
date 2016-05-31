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

package com.openexchange.folderstorage.internal.performers;

import static com.openexchange.server.services.ServerServiceRegistry.getInstance;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.folderstorage.AbstractFolder;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListPerformer.class);

    protected static final FolderType FOLDER_TYPE_MAIL = MailFolderType.getInstance();

    private static final Folder MISSING_FOLDER = new AbstractFolder() {
        private static final long serialVersionUID = -2248191704180825606L;
        @Override
        public boolean isGlobalID() {
            return false;
        }
    };

    /**
     * Initializes a new {@link ListPerformer} from given session.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public ListPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
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
     * @throws OXException If passed session is invalid
     */
    public ListPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
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
     * Gets the parent folder from its storage and checks its visibility against the requesting
     * users permissions.
     *
     * @param treeId The requested folder tree identifier
     * @param parentId The ID of the parent folder
     * @param openedStorages The already opened folder storages
     * @return The parent folder
     * @throws OXException If the users permissions aren't sufficient or an error occurs
     */
    private Folder checkParentFolder(String treeId, String parentId, Collection<FolderStorage> openedStorages) throws OXException {
        FolderStorage folderStorage = getOpenedStorage(parentId, treeId, storageParameters, openedStorages);
        Folder parent = folderStorage.getFolder(treeId, parentId, storageParameters);

        // Check folder permission for parent folder
        Permission parentPermission = CalculatePermission.calculate(parent, this, getAllowedContentTypes());
        if (!parentPermission.isVisible()) {
            throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(parent), getUserInfo4Error(), getContextInfo4Error());
        }
        return parent;
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
        Folder parent = checkParentFolder(treeId, parentId, openedStorages);
        try {
            /*
             * Get subfolder identifiers from folder itself
             */
            String[] subfolderIds = parent.getSubfolderIDs();
            if (null == subfolderIds) {
                /*
                 * Need to get user-visible subfolders from appropriate storage
                 */
                return getSubfoldersFromStorages(treeId, parentId, all, checkOnly);
            }

            subfolderIds = filterPOP3SubfolderIds(parentId, subfolderIds);
            if (0 == subfolderIds.length) {
                return new UserizedFolder[0];
            }

            return loadFolders(treeId, subfolderIds, all, checkOnly);
        } catch (final RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private List<SortableId> filterPOP3SubfolderIds(FolderStorage neededStorage, String treeId, String parentId, SortableId[] subfolderIds, StorageParameters newParameters) throws OXException {
        final List<SortableId> l;
        if (MailProperties.getInstance().isHidePOP3StorageFolders() && FOLDER_TYPE_MAIL.servesFolderId(parentId)) {
            l = new ArrayList<SortableId>(Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters)));
            final FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(parentId);
            if (MailAccount.DEFAULT_ID == argument.getAccountId()) {
                final Set<String> pop3StorageFolders;
                if (session == null) {
                    pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(user, context);
                } else {
                    pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
                }
                for (final Iterator<SortableId> it = l.iterator(); it.hasNext();) {
                    if (pop3StorageFolders.contains(it.next().getId())) {
                        it.remove();
                    }
                }
            }
        } else {
            l = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
        }
        return l;
    }

    private String[] filterPOP3SubfolderIds(String parentId, String[] subfolderIds) throws OXException {
        /*
         * The subfolders can be completely fetched from already opened parent's folder storage
         */
        if (MailProperties.getInstance().isHidePOP3StorageFolders() && FOLDER_TYPE_MAIL.servesFolderId(parentId)) {
            final FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(parentId);
            if (MailAccount.DEFAULT_ID == argument.getAccountId()) {
                final List<String> l = new ArrayList<String>(Arrays.asList(subfolderIds));
                final Set<String> pop3StorageFolders;
                if (session == null) {
                    pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(user, context);
                } else {
                    pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
                }
                for (final Iterator<String> it = l.iterator(); it.hasNext();) {
                    if (pop3StorageFolders.contains(it.next())) {
                        it.remove();
                    }
                }
                return l.toArray(new String[l.size()]);
            }
        }

        return subfolderIds;
    }

    private Map<FolderStorage, TIntList> mapStoragesToIndexes(final String treeId, final String[] subfolderIds) throws OXException {
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

        return map;
    }

    private List<List<SortableId>> getAllVisibleSubfolderIds(FolderStorage[] neededStorages, final String parentId, final String treeId) throws OXException {
        ThreadPoolService threadPool = getInstance().getService(ThreadPoolService.class, true);
        CompletionService<List<SortableId>> completionService = new ThreadPoolCompletionService<List<SortableId>>(threadPool);
        for (final FolderStorage neededStorage : neededStorages) {
            completionService.submit(new ThreadPools.TrackableCallable<List<SortableId>>() {
                @Override
                public List<SortableId> call() throws OXException {
                    StorageParameters newParameters = newStorageParameters();
                    boolean started = neededStorage.startTransaction(newParameters, false);
                    try {
                        final List<SortableId> l = filterPOP3SubfolderIds(neededStorage, treeId, parentId, neededStorage.getSubfolders(treeId, parentId, newParameters), newParameters);
                        if (started) {
                            neededStorage.commitTransaction(newParameters);
                        }
                        return l;
                    } catch (OXException e) {
                        if (started) {
                            neededStorage.rollback(newParameters);
                        }
                        addWarning(e);
                        return Collections.<SortableId> emptyList();
                    } catch (RuntimeException e) {
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
        List<List<SortableId>> results = ThreadPools.takeCompletionService(completionService, neededStorages.length, FACTORY);
        return results;
    }

    private UserizedFolder[] getSubfoldersFromStorages(final String treeId, final String parentId, final boolean all, final boolean checkOnly) throws OXException {
        /*
         * Determine needed storages for given parent
         */
        final String[] subfolderIds = collectAndSortSubfolderIds(treeId, parentId);
        if (subfolderIds.length == 0) {
            return new UserizedFolder[0];
        }

        return loadFolders(treeId, subfolderIds, all, checkOnly);
    }

    private String[] collectAndSortSubfolderIds(final String treeId, final String parentId) throws OXException {
        FolderStorage[] neededStorages = folderStorageDiscoverer.getFolderStoragesForParent(treeId, parentId);
        if (null == neededStorages || 0 == neededStorages.length) {
            return new String[0];
        }

        /*
         * Collect
         */
        final List<SortableId> allSubfolderIds;
        if (1 == neededStorages.length) {
            FolderStorage neededStorage = neededStorages[0];
            boolean started = neededStorage.startTransaction(storageParameters, false);
            try {
                allSubfolderIds = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                if (started) {
                    neededStorage.commitTransaction(storageParameters);
                }
            } catch (OXException e) {
                if (started) {
                    neededStorage.rollback(storageParameters);
                }
                throw e;
            } catch (RuntimeException e) {
                if (started) {
                    neededStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else {
            allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);

            /*
             * Get all visible subfolders from each storage
             */
            List<List<SortableId>> results = getAllVisibleSubfolderIds(neededStorages, parentId, treeId);
            for (List<SortableId> result : results) {
                allSubfolderIds.addAll(result);
            }
            /*
             * All failed with a warning?
             */
            if (!results.isEmpty() && (results.size() == getNumOfWarnings())) {
                /*
                 * Throw first warning in set
                 */
                OXException e = getWarnings().iterator().next();
                e.addCategory(Category.CATEGORY_ERROR);
                throw e;
            }
        }

        /*
         * Sort
         */
        Collections.sort(allSubfolderIds);

        String[] subfolderIds = new String[allSubfolderIds.size()];
        for (int i = 0; i < subfolderIds.length; i++) {
            SortableId subfolderId = allSubfolderIds.get(i);
            subfolderIds[i] = subfolderId.getId();
        }

        return subfolderIds;
    }

    private UserizedFolder[] loadFolders(final String treeId, final String[] subfolderIds, final boolean all, final boolean checkOnly) throws OXException {
        /*
         * Process by folder storage
         */
        final UserizedFolder[] subfolders = new UserizedFolder[subfolderIds.length];
        final Map<FolderStorage, TIntList> map = mapStoragesToIndexes(treeId, subfolderIds);
        final CompletionService<Object> completionService;
        final StorageParametersProvider paramsProvider;
        if (1 == map.size()) {
            completionService = new CallerRunsCompletionService<Object>();
            paramsProvider = new InstanceStorageParametersProvider(storageParameters);
        } else {
            completionService = new ThreadPoolCompletionService<Object>(getInstance().getService(ThreadPoolService.class, true));
            paramsProvider = null == session ? new SessionStorageParametersProvider(user, context) : new SessionStorageParametersProvider(session);
        }
        int taskCount = 0;
        for (final Entry<FolderStorage, TIntList> entry : map.entrySet()) {
            final FolderStorage folderStorage = entry.getKey();
            final int[] indexes = entry.getValue().toArray();
            completionService.submit(new ThreadPools.TrackableCallable<Object>() {
                @Override
                public Object call() throws Exception {
                    loadFoldersFromStorage(folderStorage, paramsProvider, treeId, subfolderIds, all, checkOnly, subfolders, indexes);
                    return null;
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

    void loadFoldersFromStorage(FolderStorage folderStorage, StorageParametersProvider paramsProvider, String treeId, String[] subfolderIds, boolean all, boolean checkOnly, UserizedFolder[] subfolders, int[] indexes) throws Exception {
        StorageParameters newParameters = paramsProvider.getStorageParameters();
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
        if (folderStorage.startTransaction(newParameters, false)) {
            openedStorages.add(folderStorage);
        }
        try {
            /*
             * Try to batch-load the folders
             */
            List<Folder> folders = getFolders(folderStorage, newParameters, treeId, subfolderIds, indexes);
            if (null == folders) {
                /*
                 * Load them one-by-one
                 */
                folders = getFoldersOneByOne(folderStorage, newParameters, treeId, subfolderIds, indexes, openedStorages);
            }

            /*
             * Convert to userized folders and put into array
             */
            final int size = folders.size();
            int j = 0;
            for (final int index : indexes) {
                if (j < size) {
                    final Folder subfolder = folders.get(j++);
                    if (null != subfolder) {
                        setSubfolder(subfolders, subfolder, index, all, checkOnly, treeId, newParameters, openedStorages);
                    }
                }
            }

            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(newParameters);
            }
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

    private List<Folder> getFolders(FolderStorage folderStorage, StorageParameters newParameters, String treeId, String[] subfolderIds, int[] indexes) {
        List<Folder> folders;
        try {
            final List<String> ids = new ArrayList<String>(indexes.length);
            for (final int index : indexes) {
                ids.add(subfolderIds[index]);
            }
            folders = folderStorage.getFolders(treeId, ids, newParameters);
            final Set<OXException> warnings = newParameters.getWarnings();
            if (!warnings.isEmpty()) {
                addWarning(warnings.iterator().next());
            }
        } catch (final OXException e) {
            if (OXExceptions.isUserInput(e) || OXExceptions.isPermissionDenied(e)) {
                LOG.debug("Batch loading of folder failed. Fall-back to one-by-one loading.", e);
            } else {
                LOG.warn("Batch loading of folder failed. Fall-back to one-by-one loading.", e);
            }
            folders = null;
        }
        return folders;
    }

    private List<Folder> getFoldersOneByOne(FolderStorage folderStorage, StorageParameters newParameters, String treeId, String[] subfolderIds, int[] indexes, List<FolderStorage> openedStorages) throws OXException {
        List<Folder> folders = new ArrayList<>(indexes.length);
        for (final int index : indexes) {
            final String id = subfolderIds[index];
            /*
             * Get subfolder from appropriate storage
             */
            Folder subfolder = null;
            try {
                subfolder = folderStorage.getFolder(treeId, id, newParameters);
            } catch (final OXException e) {
                if (OXExceptions.isUserInput(e) || OXExceptions.isPermissionDenied(e)) {
                    LOG.debug("The folder with ID \"{}\" in tree \"{}\" could not be fetched from storage \"{}\"", id, treeId, folderStorage.getClass().getSimpleName(), e);
                } else {
                    LOG.warn("The folder with ID \"{}\" in tree \"{}\" could not be fetched from storage \"{}\"", id, treeId, folderStorage.getClass().getSimpleName());
                }
                addWarning(e);
            }

            if (subfolder == null) {
                folders.add(MISSING_FOLDER);
            } else {
                folders.add(subfolder);
            }
        }

        return folders;
    }

    private void setSubfolder(UserizedFolder[] subfolders, Folder subfolder, int index, boolean all, boolean checkOnly, String treeId, StorageParameters newParameters, List<FolderStorage> openedStorages) throws OXException {
        if (subfolder == MISSING_FOLDER) {
            return;
        }

        /*
         * Check for subscribed status dependent on parameter "all"
         */
        if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
            final Permission userPermission = CalculatePermission.calculate(subfolder, this, getAllowedContentTypes());
            if (userPermission.isVisible()) {
                subfolders[index] = getUserizedFolder(subfolder, userPermission, treeId, all, true, newParameters, openedStorages, checkOnly);
            }
        }
    }

}
