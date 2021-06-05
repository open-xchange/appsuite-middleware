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

package com.openexchange.authentication.application;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordService} entry point for handling the storage of application specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@SingletonService
public interface AppPasswordService {

    /**
     * Remove an application specific password from the database
     *
     * @param session The session
     * @param passwordId The identifier of the ApplicationPassword to remove
     * @throws OXException if an error is occurred
     */
    void removePassword(Session session, String passwordId) throws OXException;

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
     * Get a list of the passwords for a users account
     *
     * @param session The session
     * @return Collection of ApplicationPasswords
     * @throws OXException if an error is occurred
     */
    List<ApplicationPassword> getList(Session session) throws OXException;

    /**
     * Gets the last logins for all application-specific passwords of the session's user.
     * 
     * @param session The session
     * @return The last logins, associated to the corresponding password's identifier, or an empty map if there are none
     */
    Map<String, AppPasswordLogin> getLastLogins(Session session) throws OXException;

    /**
     * Returns list of applications the can be configured for application passwords
     * getApplications
     *
     * @param session The session
     * @return A list of applications that can be configured
     * @throws OXException if an error is occurred
     */
    List<AppPasswordApplication> getApplications(Session session) throws OXException;

}
