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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllVisibleFoldersPerformer} - Serves the request to deliver all visible folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllVisibleFoldersPerformer extends AbstractUserizedFolderPerformer {

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public AllVisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param user The user
     * @param context The context final
     * @param decorator The optional folder service decorator
     */
    public AllVisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public AllVisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AllVisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Gets all visible folders
     *
     * @param treeId The tree identifier
     * @param filter The (optional) folder filter; set to <code>null</code> to filter none
     * @return All visible folders
     * @throws OXException If a folder error occurs
     */
    public UserizedFolder[] doAllVisibleFolders(final String treeId, final FolderFilter filter) throws OXException {
        final FolderStorage rootStorage = folderStorageDiscoverer.getFolderStorage(treeId, FolderStorage.ROOT_ID);
        if (null == rootStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, FolderStorage.ROOT_ID);
        }
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (rootStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(rootStorage);
        }
        try {
            final List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
            final ListPerformer listAction =
                null == session ? new ListPerformer(user, context, getDecorator()) : new ListPerformer(session, getDecorator());

            fillSubfolders(treeId, FolderStorage.ROOT_ID, filter, visibleFolders, listAction, openedStorages);

            final UserizedFolder[] ret = visibleFolders.toArray(new UserizedFolder[visibleFolders.size()]);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            return ret;
        } catch (final OXException e) {
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

    private void fillSubfolders(final String treeId, final String parentId, final FolderFilter filter, final List<UserizedFolder> visibleFolders, final ListPerformer listAction, final Collection<FolderStorage> openedStorages) throws OXException {
        final UserizedFolder[] subfolders = getSubfolders(treeId, parentId, listAction, openedStorages);
        if (subfolders.length > 0) {
            if (null == filter) {
                /*
                 * No filter
                 */
                visibleFolders.addAll(Arrays.asList(subfolders));
                for (final UserizedFolder subfolder : subfolders) {
                    fillSubfolders(treeId, subfolder.getID(), filter, visibleFolders, listAction, openedStorages);
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
                for (final UserizedFolder subfolder : subfolders) {
                    if (null != subfolder) {
                        fillSubfolders(treeId, subfolder.getID(), filter, visibleFolders, listAction, openedStorages);
                    }
                }
            }
        }
    }

    private UserizedFolder[] getSubfolders(final String treeId, final String parentId, final ListPerformer listAction, final Collection<FolderStorage> openedStorages) throws OXException {
        return listAction.doList(treeId, parentId, true, openedStorages, false);
    }

}
