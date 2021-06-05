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

package com.openexchange.mail.compose.security;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceKeyStorage} - The storage for space-association AES keys.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface CompositionSpaceKeyStorage {

    /**
     * Checks if this key storage is applicable for given session and capabilities.
     *
     * @param capabilities The capabilities granted to session-associated user
     * @param session The session providing user data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException
     */
    boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException;

    /**
     * Gets or creates & stores a random AES key for specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param createIfAbsent <code>true</code> to create & store a new random AES key if no such key is available for given composition space
     * @param session The session providing user information
     * @return The key or <code>null</code>; never <code>null</code> if <code>createIfAbsent</code> is <code>true</code>
     * @throws OXException If key cannot be returned
     */
    Key getKeyFor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException;

    /**
     * Deletes the AES key associated with specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @return <code>true</code> if deleted; otherwise <code>false</code>
     * @throws OXException If delete operation fails
     */
    default boolean deleteKeyFor(UUID compositionSpaceId, Session session) throws OXException {
        if (null == compositionSpaceId) {
            return false;
        }

        List<UUID> nonDeletedKeys = deleteKeysFor(Collections.singletonList(compositionSpaceId), session);
        return nonDeletedKeys.isEmpty();
    }

    /**
     * Deletes the AES keys associated with specified composition spaces.
     *
     * @param compositionSpaceIds The composition space identifiers
     * @param session The session providing user information
     * @return A listing of those composition space identifiers whose key could not be deleted
     * @throws OXException If delete operation fails
     */
    List<UUID> deleteKeysFor(Collection<UUID> compositionSpaceIds, Session session) throws OXException;

}
