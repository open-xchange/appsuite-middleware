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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;

/**
 * {@link IMailSharedFolderPathResolver} - Extends {@link IMailFolderStorage} by the possibility to resolve a path for a folder path, which
 * is about to be shared for a certain target user.
 * <p>
 * Requires that associated mail storage supports {@link com.openexchange.mail.api.MailCapabilities#hasPermissions() permissions}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface IMailSharedFolderPathResolver extends IMailFolderStorage {

    /**
     * Whether this instance actually supports resolving a shared folder's path.
     *
     * @param folder The identifier of the folder to resolve
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    boolean isResolvingSharedFolderPathSupported(String folder);

    /**
     * Resolves the shared folder path for given target user.
     *
     * @param folder The identifier of the folder to resolve
     * @param targetUserId The target user identifier
     * @return The resolved path
     * @throws OXException If path cannot be resolved
     */
    String resolveSharedFolderPath(String folder, int targetUserId) throws OXException;
}
