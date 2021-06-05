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

package com.openexchange.mail.compose;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceServiceFactory} - The composition space service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
@Service
public interface CompositionSpaceServiceFactory {

    /**
     * The default identifier of the composition space service factory.
     */
    public static final String DEFAULT_SERVICE_ID = "rdb";

    /**
     * Gets the identifier for this composition space service factory.
     *
     * @return The service identifier
     */
    default String getServiceId() {
        return DEFAULT_SERVICE_ID;
    }

    /**
     * Gets the ranking for this composition space service factory.
     *
     * @return The ranking
     */
    default int getRanking() {
        return 0;
    }

    /**
     * Checks if this composition space service is enabled/available for session-associated user.
     *
     * @param session The session providing user information
     * @return <code>true</code> if enabled/available; otherwise <code>false</code>
     * @throws OXException If check for availability fails
     */
    default boolean isEnabled(Session session) throws OXException {
        return true;
    }

    /**
     * Creates a new composition space service for given session.
     *
     * @param session The session providing user information
     * @return The newly created composition space service
     * @throws OXException If an appropriate composition space service cannot be created
     */
    CompositionSpaceService createServiceFor(Session session) throws OXException;

}
