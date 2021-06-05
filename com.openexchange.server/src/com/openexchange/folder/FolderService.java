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

package com.openexchange.folder;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.impl.EffectivePermission;

/**
 * {@link FolderService} - The folder service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface FolderService {

    /**
     * Determines what storage to look-up.
     */
    public static enum Storage {
        /**
         * The working storage (possibly cached data).
         */
        WORKING,
        /**
         * The backup storage (possibly cached data).
         */
        BACKUP,
        /**
         * Performs live look-up on working table.
         */
        LIVE_WORKING,
        /**
         * Performs live look-up on backup table.
         */
        LIVE_BACKUP;
    }

    /**
     * Gets specified folder from given context.
     * <p>
     * First look-up is performed for {@link Storage#WORKING}. If a "folder not found" is indicated, then retry is performed for
     * {@link Storage#BACKUP}.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(int folderId, int contextId) throws OXException;

    /**
     * Gets specified folder from given context.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param working Whether to look-up working or backup table
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(int folderId, int contextId, boolean working) throws OXException;

    /**
     * Gets specified folder from given context.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param storage What storage source to look-up
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(final int folderId, final int contextId, final Storage storage) throws OXException;

    /**
     * Determines specified user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @return The user's effective permission
     * @throws OXException If effective permission cannot be determined
     */
    public EffectivePermission getFolderPermission(int folderId, int userId, int contextId) throws OXException;

    /**
     * Determines specified user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param working Whether to look-up working or backup table
     * @return The user's effective permission
     * @throws OXException If effective permission cannot be determined
     */
    public EffectivePermission getFolderPermission(int folderId, int userId, int contextId, boolean working) throws OXException;

}
