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

package com.openexchange.folderstorage.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
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
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link List} - Serves the list request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class List extends AbstractAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(List.class);

    private TimeZone timeZone;

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

    private TimeZone getTimeZone() {
        if (null == timeZone) {
            timeZone = Tools.getTimeZone(getUser().getTimeZone());
        }
        return timeZone;
    }

    public UserizedFolder[] doList(final String treeId, final String parentId, final boolean all) throws FolderException {
        final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        folderStorage.startTransaction(getStorageParameters(), false);
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(folderStorage);
        final UserizedFolder[] ret;
        try {
            final Folder parent = folderStorage.getFolder(treeId, parentId, getStorageParameters());
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
                ret = null;
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
                            final Folder subfolder = folderStorage.getFolder(treeId, subfolderIds[index], getStorageParameters());
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
                                final UserizedFolder userizedFolder = getUserizedFolder(
                                    subfolder,
                                    subfolderPermission,
                                    treeId,
                                    all,
                                    openedStorages);
                                subfolders[index] = userizedFolder;
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
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(getStorageParameters());
            }
        } catch (final FolderException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(getStorageParameters());
            }
            throw e;
        } catch (final Exception e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(getStorageParameters());
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        if (null == ret) {
            return getSubfoldersFromMultipleStorages(treeId, parentId, all);
        }
        return ret;
    }

    private UserizedFolder[] getSubfoldersFromMultipleStorages(final String treeId, final String parentId, final boolean all) throws FolderException {
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(
            Arrays.asList(FolderStorageRegistry.getInstance().getFolderStoragesForParent(treeId, parentId)));
        for (final FolderStorage ps : openedStorages) {
            ps.startTransaction(getStorageParameters(), false);
        }
        try {
            final java.util.List<SortableId> allSubfolderIds = new ArrayList<SortableId>(openedStorages.size() * 8);
            {
                final CompletionService<java.util.List<SortableId>> completionService = new ExecutorCompletionService<java.util.List<SortableId>>(
                    ServerServiceRegistry.getInstance().getService(com.openexchange.timer.TimerService.class).getExecutor());
                /*
                 * Get all visible subfolders from each storage
                 */
                for (final FolderStorage ps : openedStorages) {
                    completionService.submit(new Callable<java.util.List<SortableId>>() {

                        public java.util.List<SortableId> call() throws Exception {
                            return Arrays.asList(ps.getSubfolders(treeId, parentId, getStorageParameters()));
                        }
                    });
                }
                /*
                 * Wait for completion
                 */
                final int maxRunningMillis = getMaxRunningMillis();
                try {
                    for (int i = openedStorages.size(); i > 0; i--) {
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
            final java.util.List<UserizedFolder> subfolders = new ArrayList<UserizedFolder>(allSubfolderIds.size());
            /*
             * Get corresponding user-sensitive folder
             */
            for (final SortableId sortableId : allSubfolderIds) {
                final String id = sortableId.getId();
                final FolderStorage tmp = getOpenedStorage(id, treeId, openedStorages);
                /*
                 * Get subfolder from appropriate storage
                 */
                final Folder subfolder = tmp.getFolder(treeId, id, getStorageParameters());
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
                    final UserizedFolder userizedFolder = getUserizedFolder(subfolder, userPermission, treeId, all, openedStorages);
                    subfolders.add(userizedFolder);
                }
            }
            /*
             * Commit all used storages
             */
            for (final FolderStorage ps : openedStorages) {
                ps.commitTransaction(getStorageParameters());
            }
            return subfolders.toArray(new UserizedFolder[subfolders.size()]);
        } catch (final FolderException e) {
            for (final FolderStorage storage : openedStorages) {
                storage.rollback(getStorageParameters());
            }
            throw e;
        } catch (final Exception e) {
            for (final FolderStorage storage : openedStorages) {
                storage.rollback(getStorageParameters());
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final java.util.List<FolderStorage> openedStorages) throws FolderException {
        final UserizedFolder userizedFolder = new UserizedFolderImpl(folder);
        userizedFolder.setLocale(getUser().getLocale());
        // Permissions
        userizedFolder.setOwnPermission(ownPermission);
        CalculatePermission.calculateUserPermissions(userizedFolder, getContext());
        // Type
        final boolean isShared;
        if (userizedFolder.getCreatedBy() != getUser().getId() && PrivateType.getInstance().equals(userizedFolder.getType())) {
            userizedFolder.setType(SharedType.getInstance());
            userizedFolder.setSubfolderIDs(new String[0]);
            isShared = true;
        } else {
            isShared = false;
        }
        // Time zone offset and last-modified in UTC
        {
            final Date cd = folder.getCreationDate();
            if (null != cd) {
                userizedFolder.setCreationDate(new Date(addTimeZoneOffset(cd.getTime(), getTimeZone())));
            }
        }
        {
            final Date lm = folder.getLastModified();
            if (null != lm) {
                userizedFolder.setLastModified(new Date(addTimeZoneOffset(lm.getTime(), getTimeZone())));
                userizedFolder.setLastModifiedUTC(new Date(lm.getTime()));
            }
        }
        if (!isShared) {
            hasVisibleSubfolderIDs(folder, treeId, all, userizedFolder, openedStorages);
        }
        return userizedFolder;
    }

    private void hasVisibleSubfolderIDs(final Folder folder, final String treeId, final boolean all, final UserizedFolder userizedFolder, final java.util.List<FolderStorage> openedStorages) throws FolderException {
        // Subfolders
        final String[] subfolders = folder.getSubfolderIDs();
        final java.util.List<String> visibleSubfolderIds;
        if (null == subfolders) {
            // Get appropriate storages and start transaction
            final String folderId = folder.getID();
            final FolderStorage[] ss = FolderStorageRegistry.getInstance().getFolderStoragesForParent(treeId, folderId);
            visibleSubfolderIds = new ArrayList<String>(1);
            for (int i = 0; visibleSubfolderIds.size() <= 0 && i < ss.length; i++) {
                final FolderStorage curStorage = ss[i];
                boolean alreadyOpened = false;
                for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                    if (openedStorages.get(j).equals(curStorage)) {
                        alreadyOpened = true;
                    }
                }
                if (!alreadyOpened) {
                    curStorage.startTransaction(getStorageParameters(), false);
                    openedStorages.add(curStorage);
                }
                final SortableId[] visibleIds = curStorage.getSubfolders(treeId, folderId, getStorageParameters());
                if (visibleIds.length > 0) {
                    /*
                     * Found a storage which offers visible subfolder(s)
                     */
                    visibleSubfolderIds.add(visibleIds[0].getId());
                }
            }
        } else {
            visibleSubfolderIds = new ArrayList<String>(1);
            /*
             * Check until first visible subfolder found
             */
            for (int i = 0; visibleSubfolderIds.size() <= 0 && i < subfolders.length; i++) {
                final String id = subfolders[i];
                final FolderStorage tmp = getOpenedStorage(id, treeId, openedStorages);
                /*
                 * Get subfolder from appropriate storage
                 */
                final Folder subfolder = tmp.getFolder(treeId, id, getStorageParameters());
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
                    visibleSubfolderIds.add(id);
                }
            }
        }
        userizedFolder.setSubfolderIDs(visibleSubfolderIds.toArray(new String[visibleSubfolderIds.size()]));
    }

    private FolderStorage getOpenedStorage(final String id, final String treeId, final java.util.List<FolderStorage> openedStorages) throws FolderException {
        FolderStorage tmp = null;
        for (final FolderStorage ps : openedStorages) {
            if (ps.getFolderType().servesFolderId(id)) {
                // Found an already opened storage which is capable to server given folderId-treeId-pair
                tmp = ps;
            }
        }
        if (null == tmp) {
            // None opened storage is capable to server given folderId-treeId-pair
            tmp = FolderStorageRegistry.getInstance().getFolderStorage(treeId, id);
            if (null == tmp) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
            }
            // Open storage and add to list of opened storages
            tmp.startTransaction(getStorageParameters(), false);
            openedStorages.add(tmp);
        }
        return tmp;
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
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
