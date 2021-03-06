/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folderstorage.internal.performers;

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link ConsistencyPerformer} - Serves the <code>CLEAR</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConsistencyPerformer extends AbstractPerformer {

    /**
     * Initializes a new {@link ConsistencyPerformer}.
     *
     * @param session The session
     * @throws OXException If passed session is invalid
     */
    public ConsistencyPerformer(final ServerSession session) throws OXException {
        super(session);
    }

    /**
     * Initializes a new {@link ConsistencyPerformer}.
     *
     * @param user The user
     * @param context The context
     */
    public ConsistencyPerformer(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link ConsistencyPerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public ConsistencyPerformer(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link ConsistencyPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public ConsistencyPerformer(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Performs the consistency check.
     *
     * @param treeId The tree identifier
     * @throws OXException If an error occurs during deletion
     */
    public void doConsistencyCheck(final String treeId) throws OXException {
        final FolderStorage[] folderStorages = folderStorageDiscoverer.getFolderStoragesForTreeID(treeId);
        for (final FolderStorage folderStorage : folderStorages) {
            final boolean started = folderStorage.startTransaction(storageParameters, true);
            try {
                folderStorage.checkConsistency(treeId, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (OXException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

}
