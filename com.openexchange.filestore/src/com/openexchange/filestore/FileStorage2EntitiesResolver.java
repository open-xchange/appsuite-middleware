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

package com.openexchange.filestore;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorage2EntitiesResolver} - Resolves a certain file storage to those contexts or users that either itself or at least one of context's users use that file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public interface FileStorage2EntitiesResolver {

    /**
     * Gets the identifiers of all contexts that either itself or at least of its users uses the denoted file storage.
     *
     * @param fileStorageId The file storage identifier
     * @return The identifiers of all contexts
     * @throws OXException If identifiers cannot be returned
     */
    int[] getIdsOfContextsUsing(int fileStorageId) throws OXException;

    /**
     * Gets the identifiers of those file storages that are used by given context. The one used by itself and the ones used by context's users.
     *
     * @param contextId The context identifier
     * @return The identifiers of used file storages
     * @throws OXException If identifiers cannot be returned
     */
    int[] getIdsOfFileStoragesUsedBy(int contextId) throws OXException;

    /**
     * Gets those file storages that are used by given context. The one used by itself and the ones used by context's users.
     * <p>
     * The file storage used by context is always the first in return listing.
     *
     * @param contextId The context identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storages with the one used by context at first positions
     * @throws OXException If file storages cannot be returned
     */
    List<FileStorage> getFileStoragesUsedBy(int contextId, boolean quotaAware) throws OXException;

    /**
     * Gets the file storage that is used by the given context.
     *
     * @param contextId The context identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storage
     * @throws OXException If file storages cannot be returned
     */
    FileStorage getFileStorageUsedBy(int contextId, boolean quotaAware) throws OXException;

    /**
     * Gets the identifiers of all users that use the denoted file storage.
     *
     * @param fileStorageId The file storage identifier
     * @return The identifiers of all users
     * @throws OXException If identifiers cannot be returned
     */
    Map<Integer, List<Integer>> getIdsOfUsersUsing(int fileStorageId) throws OXException;

    /**
     * Gets the file storage that is used by the given user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storage
     * @throws OXException If file storages cannot be returned
     */
    FileStorage getFileStorageUsedBy(int contextId, int userId, boolean quotaAware) throws OXException;

}
