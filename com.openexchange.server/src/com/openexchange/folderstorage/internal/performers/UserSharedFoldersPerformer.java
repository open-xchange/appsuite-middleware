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
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

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
                allFolderIds = Arrays.asList(folderStorage.getUserSharedFolders(treeId, contentType, storageParameters));
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
            final Map<FolderStorage, TIntList> map = new HashMap<FolderStorage, TIntList>(4);
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
                completionService = new CallerRunsCompletionService<Object>();
                paramsProvider = new InstanceStorageParametersProvider(storageParameters);
            } else {
                completionService = new ThreadPoolCompletionService<Object>(getInstance().getService(ThreadPoolService.class, true));
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
                                    ids.add(allFolderIds.get(index).getId());
                                }
                                folders = tmp.getFolders(treeId, ids, newParameters);
                                final Set<OXException> warnings = newParameters.getWarnings();
                                if (!warnings.isEmpty()) {
                                    addWarning(warnings.iterator().next());
                                }
                            } catch (final OXException e) {
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
                                    } catch (final OXException e) {
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
            final UserizedFolder[] ret = trimArray(subfolders);
            /*
             * Commit
             */
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return ret;
        } catch (final OXException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final RuntimeException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
