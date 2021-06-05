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

package com.openexchange.authentication.application.storage.history;

import java.util.Map;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordLoginHistoryStorage}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface AppPasswordLoginHistoryStorage {

    /**
     * Tracks a login for an application-specific password.
     *
     * @param authenticatedPasssword The authenticated application password
     * @param login The login information to track
     */
    void trackLogin(AuthenticatedApplicationPassword authenticatedPasssword, AppPasswordLogin login) throws OXException;

    /**
     * Gets a history of all application-specific passwords for a user's session.
     *
     * @param session The session to get the login history for
     * @return The login history, mapped to the corresponding password's identifier
     */
    Map<String, AppPasswordLogin> getHistory(Session session) throws OXException;

    /**
     * Deletes the history for a specific application password.
     *
     * @param session The session
     * @param passwordId The identifier of the password to delete the history for
     */
    void deleteHistory(Session session, String passwordId) throws OXException;

}
