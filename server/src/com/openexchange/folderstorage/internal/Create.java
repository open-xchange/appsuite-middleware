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
import java.util.List;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.virtual.VirtualFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Create} - Serves the create request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Create extends AbstractAction {

    /**
     * Initializes a new {@link Create}.
     * 
     * @param session The session
     */
    public Create(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Create}.
     * 
     * @param user The user
     * @param context The context
     */
    public Create(final User user, final Context context) {
        super(user, context);
    }

    public void doCreate(final Folder toCreate) throws FolderException {
        final String parentId = toCreate.getParentID();
        if (null == parentId) {
            throw FolderExceptionErrorMessage.MISSING_PARENT_ID.create(new Object[0]);
        }
        final String treeId = toCreate.getTreeID();
        if (null == treeId) {
            throw FolderExceptionErrorMessage.MISSING_TREE_ID.create(new Object[0]);
        }
        final FolderStorage parentStorage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, parentId);
        if (null == parentStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        parentStorage.startTransaction(storageParameters, true);
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(parentStorage);
        try {
            final Folder parent = parentStorage.getFolder(treeId, parentId, storageParameters);
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
            /*
             * Create folder dependent on folder is virtual or not
             */
            if (toCreate.isVirtual()) {

            } else {
                doCreateReal(toCreate, parentId, treeId, parentStorage);
            }
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void doCreateReal(final Folder toCreate, final String parentId, final String treeId, final FolderStorage parentStorage) throws FolderException {
        final ContentType[] contentTypes = parentStorage.getSupportedContentTypes();
        boolean supported = false;
        final ContentType folderContentType = toCreate.getContentType();
        for (final ContentType contentType : contentTypes) {
            if (contentType.equals(folderContentType)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            /*
             * Real tree is not capable to create a folder of an unsupported content type
             */
            throw FolderExceptionErrorMessage.INVALID_CONTENT_TYPE.create(
                parentId,
                folderContentType.toString(),
                treeId,
                Integer.valueOf(user.getId()),
                Integer.valueOf(context.getContextId()));
        }
        parentStorage.createFolder(toCreate, storageParameters);
    }

    private void doCreateVirtual(final VirtualFolder toCreate, final String parentId, final String treeId, final FolderStorage virtualStorage, final List<FolderStorage> openedStorages) throws FolderException {
        final ContentType folderContentType = toCreate.getContentType();
        final FolderStorage realStorage = FolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, parentId);
        /*
         * Check if real storage supports folder's content types
         */
        if (supportsContentType(folderContentType, realStorage)) {
            checkOpenedStorage(realStorage, openedStorages);
            // 1. Create in real storage
            realStorage.createFolder(toCreate, storageParameters);
            // 2. Create in virtual storage
            // TODO: Pass this one? final Folder created = realStorage.getFolder(treeId, toCreate.getID(), storageParameters);
            virtualStorage.createFolder(toCreate, storageParameters);
        } else {
            /*
             * Find the real storage which is capable to create the folder
             */
            final FolderStorage capStorage = FolderStorageRegistry.getInstance().getFolderStorageByContentType(
                FolderStorage.REAL_TREE_ID,
                folderContentType);
            if (null == capStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(FolderStorage.REAL_TREE_ID, folderContentType.toString());
            }
            checkOpenedStorage(capStorage, openedStorages);
            
            // KEEP CONSISTENT STRUCTURE IN ALL REAL FOLDER STORAGES
            /*
             * Get all real storages to create corresponding duplicate folders
             */
            final FolderStorage[] realStorages = FolderStorageRegistry.getInstance().getFolderStoragesForTreeID(FolderStorage.REAL_TREE_ID);
            for (final FolderStorage fs : realStorages) {
                if (!capStorage.equals(fs)) {
                    checkOpenedStorage(fs, openedStorages);
                    /*
                     * Create corresponding folder in current real storage
                     */
                    final Folder clone = (Folder) toCreate.clone();
                    clone.setContentType(fs.getDefaultContentType());
                    clone.setParentID("blubber"); // Look-up parent ID in current storage
                    
                }
            }
            /*
             * Create folder in actual storage
             */
            final Folder clone = (Folder) toCreate.clone();
            clone.setParentID("blubber"); // Look-up parent ID in storage
            capStorage.createFolder(clone, storageParameters);
            
            
            
            // CREATE AT DEFAULT LOCATION
            /*
             * Determine where to create the folder in real storage
             */
            String pId = parentId;
            while (!capStorage.getFolderType().servesFolderId(pId) || capStorage.containsFolder(FolderStorage.REAL_TREE_ID, parentId, storageParameters)) {
                pId = virtualStorage.getFolder(treeId, pId, storageParameters).getParentID();
            }
            
            
        }

    }

    

    private void checkOpenedStorage(final FolderStorage storage, final List<FolderStorage> openedStorages) throws FolderException {
        for (final FolderStorage openedStorage : openedStorages) {
            if (openedStorage.equals(storage)) {
                return;
            }
        }
        storage.startTransaction(storageParameters, true);
        openedStorages.add(storage);
    }

    private static boolean supportsContentType(final ContentType folderContentType, final FolderStorage folderStorage) {
        final ContentType[] supportedContentTypes = folderStorage.getSupportedContentTypes();
        if (null == supportedContentTypes) {
            return false;
        }
        if (0 == supportedContentTypes.length) {
            return true;
        }
        for (final ContentType supportedContentType : supportedContentTypes) {
            if (supportedContentType.equals(folderContentType)) {
                return true;
            }
        }
        return false;
    }

}
