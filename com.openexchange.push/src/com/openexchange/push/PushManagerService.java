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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link PushManagerService} - Manages push listeners on session appearance/disappearance events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PushManagerService {

    /**
     * Starts a new listener for specified session.
     * <p>
     * The push manager is supposed to keep track of started listeners; e.g. only one listener per session or per user-context-pair exists.
     *
     * @param session The session
     * @return A newly started listener or <code>null</code> if a listener could not be started
     * @throws OXException If listener cannot be started due to an error
     */
    PushListener startListener(Session session) throws OXException;

    /**
     * Stops the listener for specified session.
     *
     * @param session The session
     * @return <code>true</code> if listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If listener cannot be stopped due to an error
     */
    boolean stopListener(Session session) throws OXException;

    /**
     * Checks if listeners actually need any kind of socket, connection, whatever and therefore represent an acquired resource that needs to
     * be managed; e.g. orderly closed once no more needed.
     *
     * @return <code>true</code> if any kind of resource is acquired; otherwise <code>false</code>
     */
    default boolean listenersRequireResources() {
        return true;
    }

}
