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

package com.openexchange.push;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link PushListenerService} - The singleton push listener service to manually start/stop push listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface PushListenerService {

    /**
     * Starts a new listener for specified session.
     *
     * @param session The session
     * @return A newly started listener or <code>null</code> if a listener could not be started
     * @throws OXException If operation fails
     */
    PushListener startListenerFor(Session session) throws OXException;

    /**
     * Stops the listener for specified session.
     *
     * @param session The session
     * @return <code>true</code> if listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean stopListenerFor(Session session) throws OXException;

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the users with permanent listeners
     *
     * @return The users with permanent listeners
     * @throws OXException If users cannot be returned
     */
    List<PushUser> getUsersWithPermanentListeners() throws OXException;

    /**
     * Has push registration
     *
     * @param pushUser The push user to check
     * @return <code>true</code> if a push registration is available; otherwise <code>false</code>
     * @throws OXException If push registrations cannot be returned
     */
    boolean hasRegistration(PushUser pushUser) throws OXException;

    /**
     * Generates a session for specified push user according to configuration settings/possibilities.
     *
     * @param pushUser The push user
     * @return The generated session
     * @throws OXException If no session can be generated for specified push user
     */
    Session generateSessionFor(PushUser pushUser) throws OXException;

    /**
     * Registers a permanent listener for specified user.
     *
     * @param session The session
     * @param clientId The client identifier
     * @return <code>true</code> if a permanent listener is successfully registered; otherwise <code>false</code> if there is already such a listener
     * @throws OXException If operation fails
     */
    boolean registerPermanentListenerFor(Session session, String clientId) throws OXException;

    /**
     * Unregisters a permanent listener for specified user.
     *
     * @param session The session
     * @param clientId The client identifier
     * @return <code>true</code> if a permanent listener is successfully unregistered; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean unregisterPermanentListenerFor(Session session, String clientId) throws OXException;

    /**
     * Unregisters the permanent listener for specified push user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param clientId The client identifier
     * @return <code>true</code> on successful unregistration; otherwise <code>false</code>
     * @throws OXException If unregistration fails
     */
    default boolean unregisterPermanentListenerFor(int userId, int contextId, String clientId) throws OXException {
        return unregisterPermanentListenerFor(new PushUser(userId, contextId), clientId);
    }

    /**
     * Unregisters the permanent listener for specified push user
     *
     * @param pushUser The push user
     * @param clientId The client identifier
     * @return <code>true</code> on successful unregistration; otherwise <code>false</code>
     * @throws OXException If unregistration fails
     */
    boolean unregisterPermanentListenerFor(PushUser pushUser, String clientId) throws OXException;

}
