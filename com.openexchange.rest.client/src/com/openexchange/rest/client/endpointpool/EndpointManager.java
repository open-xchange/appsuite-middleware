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

package com.openexchange.rest.client.endpointpool;

import java.util.List;

/**
 * {@link EndpointManager} - Manages a list of end-points with the possibility to black-list unavailable ones (according to associated {@link EndpointAvailableStrategy strategy})
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface EndpointManager {

    /**
     * Gets an available end-point.
     *
     * @return The end-point or <code>null</code> if all end-points have been blacklisted
     */
    Endpoint get();

    /**
     * Checks if this end-point pool has any working/available end-point left.
     *
     * @return <code>true</code> if there is any available end-point; otherwise <code>false</code>
     */
    boolean hasAny();

    /**
     * Removes an end-point from the list of available ones and adds it to the blacklist.
     *
     * @param endpoint The end-point
     * @return <code>true</code> if there is any available end-point; otherwise <code>false</code>
     */
    boolean blacklist(Endpoint endpoint);

    /**
     * Removes an end-point from the blacklist and adds it back to list of available ones.
     *
     * @param endpoint The end-point
     */
    void unblacklist(Endpoint endpoint);

    /**
     * Closes this end-point pool instance. The blacklist heartbeat task is cancelled.
     */
    void close();

    /**
     * Gets the currently blacklisted end-points.
     *
     * @return The currently blacklisted end-points
     */
    List<Endpoint> getBlacklist();

    /**
     * Gets the number of total end-points.
     *
     * @return The number of total end-points
     */
    int getNumberOfEndpoints();

}
