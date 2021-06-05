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

package com.openexchange.jump;

import java.util.Set;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link EndpointHandler} - Handles a given token for a certain end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface EndpointHandler {

    /**
     * Handles given token for specified end-point.
     *
     * @param token The token
     * @param endpoint The targeted end-point
     * @param session The associated session
     * @return <code>true</code> if successfully handled; otherwise <code>false</code>
     * @throws OXException If end-point could not be handled successfully
     */
    boolean handleEndpoint(UUID token, Endpoint endpoint, Session session) throws OXException;

    /**
     * Gets a set containing the system names (in lower-case) this handler is interested in.
     *
     * @return A set of system names; never <code>null</code> or an empty {@link Set}.
     */
    Set<String> systemNamesOfInterest();

}
