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
import java.util.List;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Update} - Serves the update request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Update extends AbstractAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Update.class);

    /**
     * Initializes a new {@link Update} from given session.
     * 
     * @param session The session
     */
    public Update(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Update} from given user-context-pair.
     * 
     * @param user The user
     * @param context The context
     */
    public Update(final User user, final Context context) {
        super(user, context);
    }

    public void doCreate(final Folder folder) throws FolderException {
        final String folderId = folder.getID();
        if (null == folderId) {
            throw FolderExceptionErrorMessage.MISSING_FOLDER_ID.create(new Object[0]);
        }
        final String treeId = folder.getTreeID();
        if (null == treeId) {
            throw FolderExceptionErrorMessage.MISSING_TREE_ID.create(new Object[0]);
        }
        final FolderStorage storage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        storage.startTransaction(storageParameters, true);
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(storage);
        /*
         * Load storage folder
         */
        final Folder storageFolder = storage.getFolder(treeId, folderId, storageParameters);
        final boolean move;
        {
            final String newParentId = folder.getParentID();
            move = (null != newParentId && !newParentId.equals(storageFolder.getParentID()));
        }
        final boolean rename;
        {
            final String newName = folder.getName();
            rename = (null != newName && !newName.equals(storageFolder.getName()));
        }
        final boolean changePermissions;
        {
            final Permission[] newPerms = folder.getPermissions();
            if (null == newPerms) {
                changePermissions = false;
            } else {
                final Permission[] oldPerms = storageFolder.getPermissions();
                if (newPerms.length != oldPerms.length) {
                    changePermissions = true;
                } else {
                    final boolean equals = true;
                    for (int i = 0; equals && i < oldPerms.length; i++) {
                        final Permission oldPerm = oldPerms[i];
                        if (0 == oldPerm.getSystem()) {
                            for (int j = 0; j < newPerms.length; j++) {
                                
                            }
                        }
                    }
                    
                    
                    for (final Permission oldPerm : oldPerms) {
                        if (0 == oldPerm.getSystem()) {
                            final Permission compareWith = null;
                            for (final Permission newPerm : newPerms) {
                                
                            }
                        }
                    }
                }
            }
        }
        
    }

}
