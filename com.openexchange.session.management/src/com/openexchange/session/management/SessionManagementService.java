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

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link SessionManagementService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
@SingletonService
public interface SessionManagementService {

    /**
     * Get all sessions (local and remote) for user identified by session
     *
     * @param session The user's session
     * @return Collection containing user's sessions
     * @throws OXException On error
     */
    Collection<ManagedSession> getSessionsForUser(Session session) throws OXException;

    /**
     * Remove session identified by sessionIdToRemove for user identified by session
     *
     * @param session The user's session
     * @param sessionIdToRemove The session ID to remove
     * @throws OXException On error
     */
    void removeSession(Session session, String sessionIdToRemove) throws OXException;

}
