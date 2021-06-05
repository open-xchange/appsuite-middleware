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

package com.openexchange.mail.compose.impl.storage.db.filecache;

import java.util.Optional;
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * {@link FileCache} - A file cache storing very big message contents for database-backed composition space storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public interface FileCache {

    /** The file name prefix */
    public static final String FILE_NAME_PREFIX = "open-xchange-tmpcscontent-";

    /**
     * Gets the optional cached content for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The cached content or empty
     * @throws OXException If loading cached content fails fatally
     */
    Optional<String> getCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Stores given content in cache.
     *
     * @param content The content to store
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if content has been successfully cached; otherwise <code>false</code>
     * @throws OXException If store attempt fails
     */
    boolean storeCachedContent(String content, UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Deletes cached content for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deletion fails
     */
    void deleteCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Signals that application is going to be stopped.
     *
     * @throws OXException If operation fails fatally
     */
    void signalStop() throws OXException;

}
