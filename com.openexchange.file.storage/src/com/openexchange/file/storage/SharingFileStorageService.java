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
import com.openexchange.session.Session;

/**
 * {@link SharingFileStorageService} - A marker interface which will place the FileStorage content under folder 10 and 15
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public interface SharingFileStorageService extends FileStorageService, AccountAware {

    /**
     * Gets a value indicating if the user represented by the session has the capability to
     * use the file storage
     *
     * @param session The user session
     * @return <code>true</code> if the user is allowed to use the storage, <code>false</code> otherwise
     */
    boolean hasCapability(Session session);

    /**
     * Resets the last known, recent, error for the account
     *
     * @param accountId The id of the account to reset the errors for
     * @param session The {@link Session} object
     * @throws OXException
     */
    void resetRecentError(String accountId, Session session) throws OXException;
}
