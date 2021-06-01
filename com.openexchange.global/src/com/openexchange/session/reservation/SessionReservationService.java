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
import java.util.concurrent.TimeUnit;
import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * {@link SessionReservationService} - Used to reserve a session by obtaining a token and redeeming that token against a valid session
 * later on. The according login action is <code>redeemReservation</code>. The {@link Authenticated} instance that is created by this
 * action and used to create a session can be further enhanced via {@link Enhancer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 * @see com.openexchange.ajax.login.RedeemReservationLogin
 */
@SingletonService
public interface SessionReservationService {

    /**
     * Generates a session reservation for given arguments.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param timeout The associated timeout
     * @param unit The timeout's time unit
     * @param optState An optional state that will be applied to resulting reservation
     * @return The generated reservation
     * @throws OXException If operation fails
     */
    String reserveSessionFor(int userId, int contextId, long timeout, TimeUnit unit, Map<String, String> optState) throws OXException;

    /**
     * Removes the reservation associated with the given token and returns it.
     *
     * @param token The reservation's token
     * @return The reservation or <code>null</code> if there is no such reservation or reservation is elapsed
     * @throws OXException If operation fails
     */
    Reservation removeReservation(String token) throws OXException;

}
