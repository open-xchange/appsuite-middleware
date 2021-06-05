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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountManagerLookupService} - Performs a look-up for the appropriate {@link FileStorageAccountManager} for a certain
 * {@link FileStorageService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface FileStorageAccountManagerLookupService {

    /**
     * Gets the appropriate {@link FileStorageAccountManager account manager} for specified {@link FileStorageService file storage service}.
     *
     * @param serviceId The file storage service identifier
     * @return The appropriate account manager for specified file storage service.
     * @throws OXException If look-up fails
     */
    FileStorageAccountManager getAccountManagerFor(String serviceId) throws OXException;

    /**
     * Gets the appropriate file storage account manager for specified account identifier and session.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The file storage account manager or <code>null</code>
     * @throws OXException If retrieval fails
     */
    FileStorageAccountManager getAccountManager(String accountId, Session session) throws OXException;
}
