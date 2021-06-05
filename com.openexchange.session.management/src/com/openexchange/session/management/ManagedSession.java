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

package com.openexchange.session.management;

import com.openexchange.session.Session;

/**
 * {@link ManagedSession} - Represents a managed session providing login and session information.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public interface ManagedSession {

    /**
     * Gets the identifier associated with spawned session.
     *
     * @return The session identifier
     */
    String getSessionId();

    /**
     * Gets the IP address associated with spawned session.
     *
     * @return The IP address
     */
    String getIpAddress();

    /**
     * Gets the client identifier associated with spawned session.
     *
     * @return The client identifier
     */
    String getClient();

    /**
     * Gets the User-Agent associated with spawned session.
     *
     * @return The User-Agent identifier
     */
    String getUserAgent();

    /**
     * The time stamp when login happened, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The time stamp
     */
    long getLoginTime();

    /**
     * The time stamp of last activity with this session, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *
     * @return The time stamp
     */
    long getLastActive();

    /**
     * Gets the (optional) location
     *
     * @return The location or {@link SessionManagementStrings#UNKNOWN_LOCATION}
     */
    String getLocation();

    /**
     * Get the session object associated with spawned session
     *
     * @return The session
     */
    Session getSession();

}
