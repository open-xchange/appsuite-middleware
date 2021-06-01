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

package com.openexchange.antiabuse;

import java.util.Collections;
import java.util.Map;

/**
 * {@link Status} - Represents the status result as returned by Anti-Abuse service
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public class Status {

    /** The OK status */
    public static final int OK = 0;

    /** The BLOCKED status */
    public static final int BLOCKED = -1;

    // ------------------------------------------------------------------------------------------------------------------------ //

    private final int status;
    private final String message;
    private final Map<String, Object> attributes;
    private final Map<String, String> properties;

    /**
     * Initializes a new {@link Status}.
     *
     * @param status The status code returned by Anti-Abuse service
     */
    public Status(int status) {
        this(status, null, null, null);
    }

    /**
     * Initializes a new {@link Status}.
     *
     * @param status The status code returned by Anti-Abuse service
     * @param properties The optional properties returned by Anti-Abuse service
     */
    public Status(int status, Map<String, String> properties) {
        this(status, null, null, properties);
    }

    /**
     * Initializes a new {@link Status}.
     *
     * @param status The status code returned by Anti-Abuse service
     * @param message The optional message
     * @param attributes Additional attributes
     * @param properties The optional properties returned by Anti-Abuse service
     */
    public Status(int status, String message, Map<String, Object> attributes, Map<String, String> properties) {
        super();
        this.status = status;
        this.message = message;
        this.attributes = null == attributes ? Collections.<String, Object> emptyMap() : attributes;
        this.properties = null == properties ? Collections.<String, String> emptyMap() : properties;
    }

    /**
     * Checks if this status signals that authentication attempt is supposed to be paused for a certain number of seconds.
     *
     * @return A positive integer representing the number of seconds to wait; otherwise <code>0</code> (zero)
     */
    public int getWaitSeconds() {
        return status > 0 ? status : 0;
    }

    /**
     * Checks if this status signals that authentication attempt is all fine.
     *
     * @return <code>true</code> if OK; otherwise <code>false</code>
     */
    public boolean isOk() {
        return status == OK;
    }

    /**
     * Checks if this status signals that authentication attempt is supposed to be blocked.
     *
     * @return <code>true</code> if blocked; otherwise <code>false</code>
     */
    public boolean isBlocked() {
        return status == BLOCKED;
    }

    /**
     * Gets the status.
     *
     * @return The status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Gets the attributes
     *
     * @return The attributes or an empty map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets the message
     *
     * @return The message or <code>null</code>
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the properties.
     *
     * @return The properties or an empty map
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[status=").append(status).append(", ");
        if (properties != null) {
            builder.append("properties=").append(properties);
        }
        builder.append("]");
        return builder.toString();
    }
}
