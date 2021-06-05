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

package com.openexchange.sessiond.event;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SessiondEventListener} - The listener for sessiond events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessiondEventListener {

    /**
     * Handles the removal of specified session
     *
     * @param session The removed session
     */
    void handleSessionRemoval(Session session);

    /**
     * Handles the removal of specified session container
     *
     * @param sessions The removed session container
     */
    void handleContainerRemoval(Map<String, Session> sessions);

    /**
     * Handles the specified error
     *
     * @param error The error
     */
    public void handleError(OXException error);

    /**
     * Implementations should remove all temporary data for those sessions because they are not used for a longer time frame and are now
     * stored in the long term session life time container. This should use as less memory as possible.
     *
     * @param sessions the sessions put into long term container.
     */
    void handleSessionDataRemoval(Map<String, Session> sessions);

    /**
     * Implementations can restore temporary session information on this event. It is emitted if a session walks from the long term life
     * time container back to the normal ones.
     *
     * @param session reactivated sessions.
     */
    void handleSessionReactivation(Session session);
}
