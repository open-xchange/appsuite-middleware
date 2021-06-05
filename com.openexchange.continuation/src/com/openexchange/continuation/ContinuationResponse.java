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

package com.openexchange.continuation;

import java.util.Date;

/**
 * {@link ContinuationResponse} - A continuation response.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class ContinuationResponse<V> {

    private final boolean completed;
    private final String format;
    private final Date timeStamp;
    private final V value;

    /**
     * Initializes a new {@link ContinuationResponse}.
     *
     * @param value The (interim) value
     * @param The result's format
     * @param completed <code>true</code> to signal final value; else <code>false</code> to further await completion
     */
    public ContinuationResponse(final V value, final Date timeStamp, final String format, final boolean completed) {
        super();
        this.value = value;
        this.timeStamp = timeStamp;
        this.format = format;
        this.completed = completed;
    }

    /**
     * Gets the completed flag
     *
     * @return The completed flag
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Gets the format
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public V getValue() {
        return value;
    }

    /**
     * Gets the time stamp.
     *
     * @return The time stamp or <code>null</code>
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

}
