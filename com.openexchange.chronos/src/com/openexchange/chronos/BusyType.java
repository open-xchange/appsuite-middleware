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

package com.openexchange.chronos;

/**
 * {@link BusyType}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7953#section-3.2">RFC 7953, section 3.2</a>
 */
public enum BusyType {

    BUSY_TENTATIVE("BUSY-TENTATIVE"),
    BUSY_UNAVAILABLE("BUSY-UNAVAILABLE"),
    BUSY("BUSY"),
    ;

    private final String value;

    /**
     * Initialises a new {@link BusyType}.
     */
    private BusyType(String value) {
        this.value = value;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses the specified value to a {@link BusyType} enum item
     * 
     * @param value The value to parse
     * @return The {@link BusyType} that corresponds to that value
     * @throws IllegalArgumentException if the specified value does not map to any known
     *             {@link BusyType} enum item
     */
    public static BusyType parseFromString(String value) {
        for (BusyType bt : values()) {
            if (bt.getValue().equals(value)) {
                return bt;
            }
        }
        throw new IllegalArgumentException("Unknown BusyType for value '" + value + "'");
    }
}
