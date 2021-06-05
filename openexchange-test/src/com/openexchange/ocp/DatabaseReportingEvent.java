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

package com.openexchange.ocp;

/**
 * {@link DatabaseReportingEvent}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
public class DatabaseReportingEvent {

    private final int id;
    private final String reseller;
    private final int contextId;
    private final int userId;
    private final int eventType;
    private final long timestamp;

    /**
     * Initialises a new {@link DatabaseReportingEvent}.
     */
    public DatabaseReportingEvent(int id, String reseller, int contextId, int userId, int eventType, long timestamp) {
        super();
        this.id = id;
        this.reseller = reseller;
        this.contextId = contextId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the eventType
     *
     * @return The eventType
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Gets the timestamp
     *
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the reseller
     *
     * @return The reseller
     */
    public String getReseller() {
        return reseller;
    }
}
