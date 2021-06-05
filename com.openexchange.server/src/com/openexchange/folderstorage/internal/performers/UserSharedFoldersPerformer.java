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

package com.openexchange.folderstorage.internal.performers;

import static com.openexchange.server.services.ServerServiceRegistry.getInstance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link UserSharedFoldersPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UserSharedFoldersPerformer extends AbstractUserizedFolderPerformer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserSharedFoldersPerformer.class);

    /**
     * Initializes a new {@link UserSharedFoldersPerformer} from given session.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public UserSharedFoldersPerformer(ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link UserSharedFoldersPerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public UserSharedFoldersPerformer(User user, Context context, FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link UserSharedFoldersPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public UserSharedFoldersPerformer(ServerSession session, FolderServiceDecorator decorator, FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link UserSharedFoldersPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public UserSharedFoldersPerformer(User user, Context context, FolderServiceDecorator decorator, FolderStorageDiscoverer folderStorageDiscoverer) {
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
    public UserizedFolder[] doSharedFolders(final String treeId, ContentType contentType) throws OXException {
        FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final List<SortableId> allFolderIds;
            try {
                SortableId[] userSharedFolders = folderStorage.getUserSharedFolders(treeId, contentType, storageParameters);
                allFolderIds = null != userSharedFolders ? Arrays.asList(userSharedFolders) : Collections.emptyList();
            } catch (UnsupportedOperationException e) {
                LOG.warn("Operation is not supported for folder storage {} (content-type={})", folderStorage.getClass().getSimpleName(), contentType, e);
                return new UserizedFolder[0];
            }
            /*
             * Sort them
             */
            Collections.sort(allFolderIds);
            final int size = allFolderIds.size();
            final UserizedFolder[] subfolders = new UserizedFolder[size];
            /*
             * Get corresponding user-sensitive folders
             */

            /*
             * Collect by folder storage
             */
            final Map<FolderStorage, TIntList> map = new HashMap<>(4);
            for (int i = 0; i < size; i++) {
                final String id = allFolderIds.get(i).getId();
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
                completionService = new CallerRunsCompletionService<>();
                paramsProvider = new InstanceStorageParametersProvider(storageParameters);
            } else {
                completionService = new ThreadPoolCompletionService<>(getInstance().getService(ThreadPoolService.class, true));
                paramsProvider = null == session ? new SessionStorageParametersProvider(user, context) : new SessionStorageParametersProvider(session);
            }
            int taskCount = 0;
            final AbstractPerformer performer = this;
            for (final Entry<FolderStorage, TIntList> entry : map.entrySet()) {
                final FolderStorage tmp = entry.getKey();
                final int[] indexes = entry.getValue().toArray();
                final org.slf4j.Logger log = LOG;
                completionService.submit(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        final StorageParameters newParameters = paramsProvider.getStorageParameters();
                        final List<FolderStorage> openedStorages = new ArrayList<>(2);
                        if (tmp.startTransaction(newParameters, false)) {
                            openedStorages.add(tmp);
                        }
                        try {
                            /*
                             * Try to batch-load the folders
                             */
                            List<Folder> folders;
                            try {
                                final List<String> ids = new ArrayList<>(indexes.length);
                                for (final int index : indexes) {
                                    ids.add(allFolderIds.get(index).getId());
                                }
                                folders = tmp.getFolders(treeId, ids, newParameters);
                                final Set<OXException> warnings = newParameters.getWarnings();
                                if (!warnings.isEmpty()) {
                                    addWarning(warnings.iterator().next());
                                }
                            } catch (OXException e) {
                                log.warn("Batch loading of folder failed. Fall-back to one-by-one loading.", e);
                                folders = null;
                            }
                            if (null == folders) {
                                /*
                                 * Load them one-by-one
                                 */
                                NextIndex: for (final int index : indexes) {
                                    final String id = allFolderIds.get(index).getId();
                                    /*
                                     * Get subfolder from appropriate storage
                                     */
                                    final Folder subfolder;
                                    try {
                                        subfolder = tmp.getFolder(treeId, id, newParameters);
                                    } catch (OXException e) {
                                        log.warn("The folder with ID \"{}\" in tree \"{}\" could not be fetched from storage \"{}\"", id, treeId, tmp.getClass().getSimpleName(), e);
                                        addWarning(e);
                                        continue NextIndex;
                                    }
                                    /*
                                     * Check for subscribed status dependent on parameter "all"
                                     */
                                    final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                    if (userPermission.isVisible()) {
                                        subfolders[index] =
                                            getUserizedFolder(subfolder, userPermission, treeId, true, true, newParameters, openedStorages);
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
                                        final Permission userPermission = CalculatePermission.calculate(subfolder, performer, getAllowedContentTypes());
                                        if (userPermission.isVisible()) {
                                            subfolders[index] =
                                                getUserizedFolder(subfolder, userPermission, treeId, true, true, newParameters, openedStorages);
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
                        } catch (OXException e) {
                            for (final FolderStorage fs : openedStorages) {
                                fs.rollback(newParameters);
                            }
                            throw e;
                        } catch (RuntimeException e) {
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
            callAndWait(completionService, taskCount);
            final UserizedFolder[] ret = trimArray(subfolders);
            /*
             * Commit
             */
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return ret;
        } catch (OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            if (FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e)) {
                Thread.currentThread().interrupt();
                return new UserizedFolder[0];
            }
            throw e;
        } catch (RuntimeException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
