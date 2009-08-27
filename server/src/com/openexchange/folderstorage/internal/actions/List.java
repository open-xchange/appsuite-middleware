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

package com.openexchange.folderstorage.internal.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link List} - Serves the <code>LIST</code> request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class List extends AbstractUserizedFolderAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(List.class);

    /**
     * Initializes a new {@link List} from given session.
     * 
     * @param session The session
     */
    public List(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link List} from given user-context-pair.
     * 
     * @param user The user
     * @param context The context
     */
    public List(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link List}.
     * 
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public List(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link List}.
     * 
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public List(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
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
    public UserizedFolder[] doList(final String treeId, final String parentId, final boolean all) throws FolderException {
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        final long start = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        folderStorage.startTransaction(storageParameters, false);
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(folderStorage);
        try {
            final UserizedFolder[] ret = doList(treeId, parentId, all, openedStorages);
            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
            if (LOG.isDebugEnabled()) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("List.doList() took ").append(duration).append("msec for parent folder: ").append(
                    parentId).toString());
            }
            return ret;
        } catch (final FolderException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
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
     * @throws FolderException If a folder error occurs
     */
    UserizedFolder[] doList(final String treeId, final String parentId, final boolean all, final java.util.Collection<FolderStorage> openedStorages) throws FolderException {
        final FolderStorage folderStorage = getOpenedStorage(parentId, treeId, storageParameters, openedStorages);
        final UserizedFolder[] ret;
        try {
            final Folder parent = folderStorage.getFolder(treeId, parentId, storageParameters);
            {
                /*
                 * Check folder permission for parent folder
                 */
                final Permission parentPermission;
                if (null == getSession()) {
                    parentPermission = CalculatePermission.calculate(parent, getUser(), getContext());
                } else {
                    parentPermission = CalculatePermission.calculate(parent, getSession());
                }
                if (parentPermission.getFolderPermission() <= Permission.NO_PERMISSIONS) {
                    throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(
                        parentId,
                        getUser().getDisplayName(),
                        Integer.valueOf(getContext().getContextId()));
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
                ret = getSubfoldersFromMultipleStorages(treeId, parentId, all);
            } else {
                /*
                 * The subfolders can be completely fetched from parent's folder storage
                 */
                final CompletionService<Object> completionService = new ExecutorCompletionService<Object>(
                    ServerServiceRegistry.getInstance().getService(com.openexchange.timer.TimerService.class).getExecutor());
                final UserizedFolder[] subfolders = new UserizedFolder[subfolderIds.length];
                for (int i = 0; i < subfolderIds.length; i++) {
                    final int index = i;
                    completionService.submit(new Callable<Object>() {

                        public Object call() throws Exception {
                            final StorageParameters newParameters = newStorageParameters();
                            folderStorage.startTransaction(newParameters, false);
                            final Folder subfolder;
                            try {
                                subfolder = folderStorage.getFolder(treeId, subfolderIds[index], newParameters);
                                folderStorage.commitTransaction(newParameters);
                            } catch (final Exception e) {
                                folderStorage.rollback(newParameters);
                                throw e;
                            }
                            /*
                             * Check for access rights and subscribed status dependent on parameter "all"
                             */
                            final Permission subfolderPermission;
                            if (null == getSession()) {
                                subfolderPermission = CalculatePermission.calculate(subfolder, getUser(), getContext());
                            } else {
                                subfolderPermission = CalculatePermission.calculate(subfolder, getSession());
                            }
                            if (subfolderPermission.getFolderPermission() > Permission.NO_PERMISSIONS && (all ? true : subfolder.isSubscribed())) {
                                final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                                try {
                                    final UserizedFolder userizedFolder = getUserizedFolder(
                                        subfolder,
                                        subfolderPermission,
                                        treeId,
                                        all,
                                        true,
                                        newParameters,
                                        openedStorages);
                                    subfolders[index] = userizedFolder;
                                    for (final FolderStorage openedStorage : openedStorages) {
                                        openedStorage.commitTransaction(newParameters);
                                    }
                                } catch (final Exception e) {
                                    for (final FolderStorage openedStorage : openedStorages) {
                                        openedStorage.rollback(newParameters);
                                    }
                                    throw e;
                                }
                            }
                            return null;
                        }
                    });
                }
                /*
                 * Wait for completion
                 */
                final int maxRunningMillis = getMaxRunningMillis();
                try {
                    for (int i = 0; i < subfolderIds.length; i++) {
                        final Future<Object> f = completionService.poll(maxRunningMillis, TimeUnit.MILLISECONDS);
                        if (null != f) {
                            f.get();
                        }
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } catch (final ExecutionException e) {
                    final Throwable t = e.getCause();
                    if (FolderException.class.isAssignableFrom(t.getClass())) {
                        throw (FolderException) t;
                    } else if (AbstractOXException.class.isAssignableFrom(t.getClass())) {
                        throw new FolderException((AbstractOXException) t);
                    } else if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else if (t instanceof Error) {
                        throw (Error) t;
                    } else {
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
                    }
                }
                ret = subfolders;
            }
        } catch (final FolderException e) {
            throw e;
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return ret;
    }

    private UserizedFolder[] getSubfoldersFromMultipleStorages(final String treeId, final String parentId, final boolean all) throws FolderException {
        /*
         * Determine needed storages for given parent
         */
        final FolderStorage[] neededStorages = folderStorageDiscoverer.getFolderStoragesForParent(treeId, parentId);
        if (null == neededStorages || 0 == neededStorages.length) {
            return new UserizedFolder[0];
        }
        final java.util.List<SortableId> allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);
        {
            final CompletionService<java.util.List<SortableId>> completionService = new ExecutorCompletionService<java.util.List<SortableId>>(
                ServerServiceRegistry.getInstance().getService(com.openexchange.timer.TimerService.class).getExecutor());
            /*
             * Get all visible subfolders from each storage
             */
            for (final FolderStorage neededStorage : neededStorages) {
                completionService.submit(new Callable<java.util.List<SortableId>>() {

                    public java.util.List<SortableId> call() throws Exception {
                        final StorageParameters newParameters = newStorageParameters();
                        neededStorage.startTransaction(newParameters, false);
                        try {
                            final java.util.List<SortableId> l = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
                            neededStorage.commitTransaction(newParameters);
                            return l;
                        } catch (final Exception e) {
                            neededStorage.rollback(newParameters);
                            throw e;
                        }
                    }
                });
            }
            /*
             * Wait for completion
             */
            final int maxRunningMillis = getMaxRunningMillis();
            try {
                for (int i = neededStorages.length; i > 0; i--) {
                    final Future<java.util.List<SortableId>> f = completionService.poll(maxRunningMillis, TimeUnit.MILLISECONDS);
                    if (null != f) {
                        allSubfolderIds.addAll(f.get());
                    } else if (LOG.isWarnEnabled()) {
                        LOG.warn("Completion service's task did not complete in a timely manner!");
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                if (FolderException.class.isAssignableFrom(t.getClass())) {
                    throw (FolderException) t;
                } else if (AbstractOXException.class.isAssignableFrom(t.getClass())) {
                    throw new FolderException((AbstractOXException) t);
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
                }
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
        final CompletionService<Object> completionService = new ExecutorCompletionService<Object>(
            ServerServiceRegistry.getInstance().getService(com.openexchange.timer.TimerService.class).getExecutor());
        for (int i = 0; i < size; i++) {
            final int index = i;
            completionService.submit(new Callable<Object>() {

                public Object call() throws Exception {
                    final SortableId sortableId = allSubfolderIds.get(index);
                    final String id = sortableId.getId();
                    final StorageParameters newParameters = newStorageParameters();
                    final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                    try {
                        final FolderStorage tmp = getOpenedStorage(id, treeId, newParameters, openedStorages);
                        /*
                         * Get subfolder from appropriate storage
                         */
                        final Folder subfolder;
                        try {
                            subfolder = tmp.getFolder(treeId, id, newParameters);
                        } catch (final FolderException e) {
                            LOG.warn(new StringBuilder(128).append("The folder with ID \"").append(id).append("\" in tree \"").append(
                                treeId).append("\" could not be fetched from storage \"").append(tmp.getClass().getSimpleName()).append(
                                "\"").toString(), e);
                            return null;
                        }
                        /*
                         * Check for subscribed status dependent on parameter "all"
                         */
                        if (all ? true : subfolder.isSubscribed()) {
                            final Permission userPermission;
                            if (null == getSession()) {
                                userPermission = CalculatePermission.calculate(subfolder, getUser(), getContext());
                            } else {
                                userPermission = CalculatePermission.calculate(subfolder, getSession());
                            }
                            final UserizedFolder userizedFolder = getUserizedFolder(
                                subfolder,
                                userPermission,
                                treeId,
                                all,
                                true,
                                newParameters,
                                openedStorages);
                            subfolders[index] = userizedFolder;
                        }
                        for (final FolderStorage openedStorage : openedStorages) {
                            openedStorage.commitTransaction(newParameters);
                        }
                        return null;
                    } catch (final Exception e) {
                        for (final FolderStorage openedStorage : openedStorages) {
                            openedStorage.rollback(newParameters);
                        }
                        throw e;
                    }
                }
            });
        }
        /*
         * Wait for completion
         */
        final int maxRunningMillis = getMaxRunningMillis();
        try {
            for (int i = 0; i < size; i++) {
                final Future<Object> f = completionService.poll(maxRunningMillis, TimeUnit.MILLISECONDS);
                if (null != f) {
                    f.get();
                } else if (LOG.isWarnEnabled()) {
                    LOG.warn("Completion service's task did not complete in a timely manner!");
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (FolderException.class.isAssignableFrom(t.getClass())) {
                throw (FolderException) t;
            } else if (AbstractOXException.class.isAssignableFrom(t.getClass())) {
                throw new FolderException((AbstractOXException) t);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        }
        final java.util.List<UserizedFolder> subfolderList = new ArrayList<UserizedFolder>(subfolders.length);
        for (int i = 0; i < subfolders.length; i++) {
            final UserizedFolder uf = subfolders[i];
            if (null != uf) {
                subfolderList.add(uf);
            }
        }
        return subfolderList.toArray(new UserizedFolder[subfolderList.size()]);
    }

    private static final int DEFAULT_MAX_RUNNING_MILLIS = 120000;

    private int getMaxRunningMillis() {
        final ConfigurationService confService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == confService) {
            // Default of 2 minutes
            return DEFAULT_MAX_RUNNING_MILLIS;
        }
        // 2 * AJP_WATCHER_MAX_RUNNING_TIME
        return confService.getIntProperty("AJP_WATCHER_MAX_RUNNING_TIME", DEFAULT_MAX_RUNNING_MILLIS) * 2;
    }

}
