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
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Subscribe} - Serves the <code>SUBSCRIBE</code> action.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Subscribe extends AbstractAction {

    /**
     * Initializes a new {@link Subscribe}.
     * 
     * @param session
     */
    public Subscribe(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Subscribe}.
     * 
     * @param user
     * @param context
     */
    public Subscribe(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link Create}.
     * 
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public Subscribe(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link Create}.
     * 
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public Subscribe(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>SUBSCRIBE</code> action.
     * 
     * @param sourceTreeId The source tree identifier
     * @param folderId The folder identifier
     * @param targetTreeId The target tree identifier
     * @param targetParentId The target parent identifier
     * @throws FolderException If a folder error occurs
     */
    public void doSubscribe(final String sourceTreeId, final String folderId, final String targetTreeId, final String targetParentId) throws FolderException {
        if (FolderStorage.REAL_TREE_ID.equals(targetTreeId)) {
            throw FolderExceptionErrorMessage.NO_REAL_SUBSCRIBE.create(targetTreeId);
        }
        final FolderStorage sourceStorage = folderStorageDiscoverer.getFolderStorage(sourceTreeId, folderId);
        if (null == sourceStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(sourceTreeId, folderId);
        }
        sourceStorage.startTransaction(storageParameters, false);
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(sourceStorage);
        try {
            final Folder sourceFolder = sourceStorage.getFolder(sourceTreeId, folderId, storageParameters);
            {
                /*
                 * Check folder permission for parent folder
                 */
                final Permission parentPermission;
                if (null == getSession()) {
                    parentPermission = CalculatePermission.calculate(sourceFolder, getUser(), getContext());
                } else {
                    parentPermission = CalculatePermission.calculate(sourceFolder, getSession());
                }
                if (parentPermission.getFolderPermission() <= Permission.NO_PERMISSIONS) {
                    throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(
                        folderId,
                        getUser().getDisplayName(),
                        Integer.valueOf(getContext().getContextId()));
                }
            }
            final FolderStorage targetStorage = getOpenedStorage(targetParentId, targetTreeId, openedStorages);
            {
                /*
                 * Check for equally named folder
                 */
                final UserizedFolder[] subfolders = (session == null ? new List(user, context) : new List(session)).doList(
                    targetTreeId,
                    targetParentId,
                    true,
                    openedStorages);
                for (final UserizedFolder userizedFolder : subfolders) {
                    if (userizedFolder.getName().equals(sourceFolder.getName())) {
                        throw FolderExceptionErrorMessage.EQUAL_NAME.create(sourceFolder.getName(), targetParentId, targetTreeId);
                    }
                }
            }
            final Folder virtualFolder = (Folder) sourceFolder.clone();
            virtualFolder.setParentID(targetParentId);
            virtualFolder.setTreeID(targetTreeId);
            virtualFolder.setSubfolderIDs(null);
            targetStorage.createFolder(virtualFolder, storageParameters);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
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

}
