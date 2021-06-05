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
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.SessionEnhancement;


/**
 * An enhancer is used to enhance the {@link Authenticated} instance that is created by the <code>redeemReservation</code> login action
 * to create a new session. Every user of the {@link SessionReservationService} may register his own enhancer as OSGi service. When
 * the logout is performed then, all enhancers are called. You should mark your reservations via a property in the state map to recognize
 * them later on.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see EnhancedAuthenticated
 */
public interface Enhancer {

    /**
     * Allows customization of the {@link Authenticated} instance created by the <code>redeemReservation</code> login action.
     * Probably you want to return a copy of the passed authenticated here, which additionally extends {@link ResponseEnhancement}
     * and/or {@link SessionEnhancement} to set cookies/headers/session parameters. Multiple enhancers are called subsequently, so
     * you need to preserve already made enhancements. Best practice is to use {@link EnhancedAuthenticated}, which will do all the
     * magic for you.
     *
     * @param authenticated The authenticated based on the user and context of the according reservation
     * @param reservationState The state map that was passed when the reservation was attempted via
     *        {@link SessionReservationService#reserveSessionFor(int, int, long, java.util.concurrent.TimeUnit, Map)}
     * @return The enhanced authenticated. Never return <code>null</code> here!
     * @see SessionReservationService#reserveSessionFor(int, int, long, java.util.concurrent.TimeUnit, Map)
     */
    Authenticated enhance(Authenticated authenticated, Map<String, String> reservationState);

}
