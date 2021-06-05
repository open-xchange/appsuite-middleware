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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SimSessionReservationService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SimSessionReservationService implements SessionReservationService {

    private final ConcurrentMap<String, Reservation> reservations = new ConcurrentHashMap<String, Reservation>();

    @Override
    public String reserveSessionFor(int userId, int contextId, long timeout, TimeUnit unit, Map<String, String> optState) throws OXException {
        String token = UUIDs.getUnformattedString(UUID.randomUUID());
        ReservationImpl reservationImpl = new ReservationImpl(token, userId, contextId, unit.toMillis(timeout), System.currentTimeMillis(), optState);
        reservations.put(token, reservationImpl);
        return token;
    }

    @Override
    public Reservation removeReservation(String token) throws OXException {
        return reservations.remove(token);
    }

    private static class ReservationImpl implements Reservation {

        private final String token;

        private final int userId;

        private final int contextId;

        private final long timeoutMillis;

        private final long creationStamp;

        private final Map<String, String> state;

        /**
         * Initializes a new {@link ReservationImpl}.
         * @param token
         * @param userId
         * @param contextId
         * @param timeoutMillis
         * @param creationStamp
         * @param state
         */
        public ReservationImpl(String token, int userId, int contextId, long timeoutMillis, long creationStamp, Map<String, String> state) {
            super();
            this.token = token;
            this.userId = userId;
            this.contextId = contextId;
            this.timeoutMillis = timeoutMillis;
            this.creationStamp = creationStamp;
            this.state = state;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public int getContextId() {
            return contextId;
        }

        @Override
        public long getTimeoutMillis() {
            return timeoutMillis;
        }

        @Override
        public long getCreationStamp() {
            return creationStamp;
        }

        @Override
        public Map<String, String> getState() {
            return state;
        }

    }

}
