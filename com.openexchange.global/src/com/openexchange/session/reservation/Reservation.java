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

package com.openexchange.session.reservation;

import java.util.Map;


/**
 * {@link Reservation} - A session reservation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface Reservation {

    /**
     * Gets the token bound to this reservation
     *
     * @return The token
     */
    String getToken();

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the reservation's timeout in milliseconds
     *
     * @return The timeout milliseconds
     */
    long getTimeoutMillis();

    /**
     * Gets the creation stamp
     *
     * @return The creation stamp
     */
    long getCreationStamp();

    /**
     * Gets the optional state.
     *
     * @return The state or <code>null</code> if absent
     */
    Map<String, String> getState();

}
