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

package com.openexchange.contact.storage.registry;

import java.util.List;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;


/**
 * {@link ContactStorageRegistry} - Registry for {@link ContactStorage}s
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface ContactStorageRegistry {

    /**
     * Gets the {@link ContactStorage} for the supplied folder ID.
     *
     * @param session the session
     * @param folderId the ID of the folder to get the storage for
     * @return the storage
     * @throws OXException
     */
    ContactStorage getStorage(Session session, String folderId) throws OXException;

    /**
     * Gets a list of all registered {@link ContactStorage}s.
     *
     * @param session the session
     * @return the storages
     * @throws OXException
     */
    List<ContactStorage> getStorages(Session session) throws OXException;

}
