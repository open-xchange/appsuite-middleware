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

package com.openexchange.session.reservation.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.session.reservation.Reservation;

/**
 * {@link ReservationImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class ReservationImpl implements Reservation {

    private int contextId;
    private int userId;
    private String token;
    private long timeoutMillis;
    private long creationStamp;
    private Map<String, String> state;

    /**
     * Initializes a new {@link ReservationImpl}.
     */
    public ReservationImpl() {
        super();
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

    /**
     * Sets the creation stamp
     *
     * @param creationStamp The creation stamp to set
     */
    public void setCreationStamp(long creationStamp) {
        this.creationStamp = creationStamp;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier to set
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the user identifier
     *
     * @param userId The user identifier to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the timeout millis
     *
     * @param timeoutMillis The timeout millis to set
     */
    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Sets the optional state.
     *
     * @param optState The state to apply
     */
    public void setState(Map<String, String> optState) {
        this.state = null == optState ? null : new LinkedHashMap<String, String>(optState);
    }

}
