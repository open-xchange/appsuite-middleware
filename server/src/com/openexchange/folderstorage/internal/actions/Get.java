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
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Get} - Serves the <code>GET</code> request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Get extends AbstractUserizedFolderAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Get.class);

    /**
     * Initializes a new {@link Get}.
     * 
     * @param session The session
     */
    public Get(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Get}.
     * 
     * @param user The user
     * @param context The context
     */
    public Get(final User user, final Context context) {
        super(user, context);
    }

    public UserizedFolder doGet(final String treeId, final String folderId) throws FolderException {
        final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final long start = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        folderStorage.startTransaction(storageParameters, false);
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(folderStorage);
        try {
            final Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
            /*
             * Check folder permission for folder
             */
            final Permission ownPermission;
            if (null == getSession()) {
                ownPermission = CalculatePermission.calculate(folder, getUser(), getContext());
            } else {
                ownPermission = CalculatePermission.calculate(folder, getSession());
            }
            if (ownPermission.getFolderPermission() <= Permission.NO_PERMISSIONS) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(
                    folderId,
                    getUser().getDisplayName(),
                    Integer.valueOf(getContext().getContextId()));
            }
            // TODO: All or only subscribed subfolders?
            final UserizedFolder userizedFolder = getUserizedFolder(folder, ownPermission, treeId, true, true, openedStorages);
            if (LOG.isDebugEnabled()) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("Get.doGet() took ").append(duration).append("msec for folder: ").append(folderId).toString());
            }

            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            return userizedFolder;
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
