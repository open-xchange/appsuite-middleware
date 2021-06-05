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

package com.openexchange.contact.vcard.storage;

import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link VCardStorageMetadataStore}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface VCardStorageMetadataStore {

    /**
     * Returns all persisted VCard references within the given context.
     *
     * @param contextId The context identifier
     * @return {@link Set<String>} with VCard identifiers for the given context
     * @throws OXException
     */
    Set<String> loadRefIds(int contextId) throws OXException;

    /**
     * Returns all persisted VCard references within the given context and user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return {@link Set<String>} with VCard identifiers for the given context and user
     * @throws OXException
     */
    Set<String> loadRefIds(int contextId, int userId) throws OXException;

    /**
     * Removes all provided reference entries for the given context from the storage.
     *
     * @param contextId The context identifier
     * @param refIds {@link Set<String>} with VCard references to removed the entry from the contact
     * @throws OXException
     */
    void removeByRefId(int contextId, Set<String> refIds) throws OXException;
}
