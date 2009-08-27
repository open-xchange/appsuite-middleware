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
import java.util.Collection;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllVisibleFolders} - Serves the request to deliver all visible folders.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllVisibleFolders extends AbstractUserizedFolderAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AllVisibleFolders.class);

    /**
     * Initializes a new {@link AllVisibleFolders}.
     * 
     * @param session The session
     */
    public AllVisibleFolders(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link AllVisibleFolders}.
     * 
     * @param user The user
     * @param context The context final
     */
    public AllVisibleFolders(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link AllVisibleFolders}.
     * 
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AllVisibleFolders(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link AllVisibleFolders}.
     * 
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AllVisibleFolders(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Gets all visible folders
     * 
     * @param treeId The tree identifier
     * @param filter The (optional) folder filter; set to <code>null</code> to filter none
     * @return All visible folders
     * @throws FolderException If a folder error occurs
     */
    public UserizedFolder[] doAllVisibleFolders(final String treeId, final FolderFilter filter) throws FolderException {
        final FolderStorage rootStorage = folderStorageDiscoverer.getFolderStorage(treeId, FolderStorage.ROOT_ID);
        if (null == rootStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, FolderStorage.ROOT_ID);
        }
        rootStorage.startTransaction(storageParameters, false);
        final long start = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(rootStorage);
        try {
            final java.util.List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
            final List listAction = null == session ? new List(user, context) : new List(session);

            fillSubfolders(treeId, FolderStorage.ROOT_ID, filter, visibleFolders, listAction, openedStorages);

            final UserizedFolder[] ret = visibleFolders.toArray(new UserizedFolder[visibleFolders.size()]);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            if (LOG.isDebugEnabled()) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("AllVisibleFolders.doAllVisibleFolders() took ").append(duration).append("msec").toString());
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

    private void fillSubfolders(final String treeId, final String parentId, final FolderFilter filter, final java.util.List<UserizedFolder> visibleFolders, final List listAction, final Collection<FolderStorage> openedStorages) throws FolderException {
        final UserizedFolder[] subfolders = getSubfolders(treeId, parentId, listAction, openedStorages);
        if (subfolders.length > 0) {
            if (null == filter) {
                /*
                 * No filter
                 */
                visibleFolders.addAll(Arrays.asList(subfolders));
                for (int i = 0; i < subfolders.length; i++) {
                    fillSubfolders(treeId, subfolders[i].getID(), filter, visibleFolders, listAction, openedStorages);
                }
            } else {
                /*
                 * With filter
                 */
                for (int i = 0; i < subfolders.length; i++) {
                    final UserizedFolder subfolder = subfolders[i];
                    if (filter.accept(subfolder)) {
                        visibleFolders.add(subfolder);
                    } else {
                        subfolders[i] = null;
                    }
                }
                for (int i = 0; i < subfolders.length; i++) {
                    final UserizedFolder subfolder = subfolders[i];
                    if (null != subfolder) {
                        fillSubfolders(treeId, subfolder.getID(), filter, visibleFolders, listAction, openedStorages);
                    }
                }
            }
        }
    }

    private UserizedFolder[] getSubfolders(final String treeId, final String parentId, final List listAction, final Collection<FolderStorage> openedStorages) throws FolderException {
        return listAction.doList(treeId, parentId, true, openedStorages);
    }

}
