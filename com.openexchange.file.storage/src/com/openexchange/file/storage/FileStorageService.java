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

import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageService} - The file storage service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageService extends FileStorageConstants {

    /**
     * Gets the identifier of this file storage service.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Get the form description.
     *
     * @return The form description
     */
    DynamicFormDescription getFormDescription();

    /**
     * Gets those properties from configuration which should be encrypted.
     *
     * @return Those properties from configuration which should be encrypted
     */
    Set<String> getSecretProperties();

    /**
     * Gets the account manager for this file storage service.
     *
     * @return The account manager
     * @throws OXException
     */
    FileStorageAccountManager getAccountManager() throws OXException;

    /**
     * Gets the account access for specified account identifier.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The account access for specified account identifier
     * @throws OXException If account access cannot be returned for given account identifier
     */
    FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException;

}
