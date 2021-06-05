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

package com.openexchange.folderstorage.cache.service;

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;


/**
 * {@link FolderCacheInvalidationService} - The singleton service to invalidate folder cache entries.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface FolderCacheInvalidationService {

    /**
     * Invalidates the specified folder from caches.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier; e.g. {@link FolderStorage#REAL_TREE_ID}
     * @param session The associated session
     * @throws OXException If operation fails
     */
    void invalidateSingle(String folderId, String treeId, Session session) throws OXException;

    /**
     * Invalidates the specified folder from caches.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier; e.g. {@link FolderStorage#REAL_TREE_ID}
     * @param includeParents <code>false</code> if only specified folder should be removed; otherwise <code>true</code> for complete folder's path to root folder
     * @param session The associated session
     * @throws OXException If operation fails
     */
    void invalidate(String folderId, String treeId, boolean includeParents, Session session) throws OXException;

}
