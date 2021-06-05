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

package com.openexchange.authentication.application.storage;

import java.util.List;
import com.openexchange.authentication.application.AppLoginRequest;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLoginHistoryStorage;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordStorage} handles storage of application specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface AppPasswordStorage {

    /**
     * Checks if this storage handles this login string.
     *
     * @param loginRequest The login request
     * @return True if the storage handles this application password
     * @throws OXException if an invalid login request object is supplied
     */
    boolean handles(AppLoginRequest loginRequest) throws OXException;

    /**
     * Checks if this storage handles this type of application, prior adding a new password.
     *
     * @param session The session
     * @param appType The application type identifier to check
     * @return <code>true</code> if the storage is handling app-specific passwords for this application type, <code>false</code>, otherwise
     */
    boolean handles(Session session, String appType) throws OXException;

    /**
     * Remove an application specific password from the database
     *
     * @param session The session
     * @param passwordid The identifier of the ApplicationPassword to remove
     * @return The removed password, or <code>null</code> if not found
     * @throws OXException if an error is occurred
     */
    boolean removePassword(Session session, String passwordId) throws OXException;

    /**
     * Add a password to the database.
     * 
     * @param session The session
     * @param appName Application name, user chosen
     * @param appType The application type
     * @throws OXException if an error is occurred
     */
    ApplicationPassword addPassword(Session session, String appName, String appType) throws OXException;

    /**
     * Authenticates and resolves the login and password against the database.
     * Returns the ApplicationPassword if found.
     *
     * @param login The login string
     * @param password The users password
     * @return An ApplicationPassword populated with the user information and permissions
     * @throws OXException If bad authentication or error
     */
    AuthenticatedApplicationPassword doAuth(String login, String password) throws OXException;

    /**
     * Get a list of the passwords for a users account
     *
     * @param session The session
     * @return Collection of ApplicationPasswords
     * @throws OXException if an error is occurred
     */
    List<ApplicationPassword> getList(Session session) throws OXException;

    /**
     * Gets the accompanying login history storage.
     * 
     * @return The login history storage
     */
    AppPasswordLoginHistoryStorage getLoginHistoryStorage();

}
