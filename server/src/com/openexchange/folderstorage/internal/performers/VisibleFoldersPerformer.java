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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import gnu.trove.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceException;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VisibleFoldersPerformer} - Serves the request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VisibleFoldersPerformer extends AbstractUserizedFolderPerformer {

    private static final Log LOG = LogFactory.getLog(VisibleFoldersPerformer.class);

    /**
     * Initializes a new {@link VisibleFoldersPerformer} from given session.
     * 
     * @param session The session
     * @param decorator The optional folder service decorator
     */
    public VisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator) {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link VisibleFoldersPerformer} from given user-context-pair.
     * 
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public VisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link VisibleFoldersPerformer}.
     * 
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public VisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link VisibleFoldersPerformer}.
     * 
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public VisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
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
     * @throws FolderException If a folder error occurs
     */
    public UserizedFolder[] doVisibleFolders(final String treeId, final ContentType contentType, final Type type, final boolean all) throws FolderException {
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final long start = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final List<SortableId> allSubfolderIds =
                Arrays.asList(folderStorage.getVisibleFolders(treeId, contentType, type, storageParameters));
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
            final Map<FolderStorage, TIntArrayList> map = new HashMap<FolderStorage, TIntArrayList>(4);
            for (int i = 0; i < size; i++) {
                final String id = allSubfolderIds.get(i).getId();
                final FolderStorage tmp = folderStorageDiscoverer.getFolderStorage(treeId, id);
                if (null == tmp) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
                }
                TIntArrayList list = map.get(tmp);
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
                try {
                    completionService = new ThreadPoolCompletionService<Object>(getInstance().getService(ThreadPoolService.class, true));
                } catch (final ServiceException e) {
                    throw new FolderException(e);
                }
                paramsProvider = null == session ? new SessionStorageParametersProvider(user, context) : new SessionStorageParametersProvider(session);
            }
            int taskCount = 0;
            for (final Entry<FolderStorage, TIntArrayList> entry : map.entrySet()) {
                final FolderStorage tmp = entry.getKey();
                final int[] indexes = entry.getValue().toNativeArray();
                final Log log = LOG;
                completionService.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        final StorageParameters newParameters = paramsProvider.getStorageParameters();
                        final boolean started = tmp.startTransaction(newParameters, false);
                        try {
                            final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                            openedStorages.add(tmp);
                            for (final int index : indexes) {
                                final String id = allSubfolderIds.get(index).getId();
                                /*
                                 * Get subfolder from appropriate storage
                                 */
                                final Folder subfolder;
                                try {
                                    subfolder = tmp.getFolder(treeId, id, newParameters);
                                } catch (final FolderException e) {
                                    log.warn(
                                        new StringBuilder(128).append("The folder with ID \"").append(id).append("\" in tree \"").append(treeId).append(
                                            "\" could not be fetched from storage \"").append(tmp.getClass().getSimpleName()).append("\"").toString(),
                                        e);
                                    addWarning(e);
                                    return null;
                                }
                                /*
                                 * Check for subscribed status dependent on parameter "all"
                                 */
                                if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                    final Permission userPermission;
                                    if (null == getSession()) {
                                        userPermission =
                                            CalculatePermission.calculate(subfolder, getUser(), getContext(), getAllowedContentTypes());
                                    } else {
                                        userPermission = CalculatePermission.calculate(subfolder, getSession(), getAllowedContentTypes());
                                    }
                                    if (userPermission.isVisible()) {
                                        subfolders[index] =
                                            getUserizedFolder(subfolder, userPermission, treeId, all, true, newParameters, openedStorages);
                                    }
                                }
                            }
                            if (started) {
                                tmp.commitTransaction(newParameters);
                            }
                            return null;
                        } catch (final FolderException e) {
                            if (started) {
                                tmp.rollback(newParameters);
                            }
                            throw e;
                        } catch (final Exception e) {
                            if (started) {
                                tmp.rollback(newParameters);
                            }
                            throw FolderException.newUnexpectedException(e);
                        }
                        
                    }
                });
                taskCount++;
            }
            /*
             * Wait for completion
             */
            ThreadPools.pollCompletionService(completionService, taskCount, getMaxRunningMillis(), FACTORY);
            final UserizedFolder[] ret = trimArray(subfolders);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            if (LOG.isDebugEnabled()) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("VisibleSubfoldersPerformer.doVisibleSubfolders() took ").append(duration).append(
                    "msec").toString());
            }
            return ret;
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final int DEFAULT_MAX_RUNNING_MILLIS = 120000;

    private int getMaxRunningMillis() {
        final ConfigurationService confService = getInstance().getService(ConfigurationService.class);
        if (null == confService) {
            // Default of 2 minutes
            return DEFAULT_MAX_RUNNING_MILLIS;
        }
        // 2 * AJP_WATCHER_MAX_RUNNING_TIME
        return confService.getIntProperty("AJP_WATCHER_MAX_RUNNING_TIME", DEFAULT_MAX_RUNNING_MILLIS) * 2;
    }

    private static final ThreadPools.ExpectedExceptionFactory<FolderException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<FolderException>() {

            public Class<FolderException> getType() {
                return FolderException.class;
            }

            public FolderException newUnexpectedError(final Throwable t) {
                return FolderException.newUnexpectedException(t);
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
            if (null != uf) {
                l.add(uf);
            }
        }
        return l.toArray(new UserizedFolder[l.size()]);
    }

}
