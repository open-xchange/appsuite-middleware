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

import java.util.Date;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Delete} - Serves the <code>DELETE</code> request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Delete extends AbstractAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Delete.class);

    /**
     * Initializes a new {@link Delete}.
     * 
     * @param session The session
     */
    public Delete(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Delete}.
     * 
     * @param user The user
     * @param context The context
     */
    public Delete(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Performs the <code>DELETE</code> request.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @throws FolderException If an error occurs during deletion
     */
    public void doDelete(final String treeId, final String folderId, final Date timeStamp) throws FolderException {
        final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final long start = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        folderStorage.startTransaction(storageParameters, true);
        if (null != timeStamp) {
            storageParameters.setTimeStamp(timeStamp);
        }
        try {
            folderStorage.deleteFolder(treeId, folderId, storageParameters);
            if (LOG.isDebugEnabled()) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("Delete.doDelete() took ").append(duration).append("msec for folder: ").append(
                    folderId).toString());
            }
            folderStorage.commitTransaction(storageParameters);
        } catch (final FolderException e) {
            folderStorage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            folderStorage.rollback(storageParameters);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
