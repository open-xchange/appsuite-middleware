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

package com.openexchange.websockets.grizzly.remote;

/**
 * {@link MapKey} - A key for Hazelcast map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
class MapKey {

    /**
     * Parses the map key information from specified string.
     *
     * @param key The key as string
     * @return The parsed key
     * @throws IllegalArgumentException If key cannot be parsed
     */
    static MapKey parseFrom(String key) {
        int atPos = key.indexOf('@', 0);
        if (atPos < 0) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        int usPos = key.indexOf('_', atPos + 1);
        if (usPos < 0) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        try {
            return new MapKey(Integer.parseInt(key.substring(0, atPos)), Integer.parseInt(key.substring(atPos + 1, usPos)), key.substring(usPos + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid key: " + key, e);
        }
    }

    // ---------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String address;

    /**
     * Initializes a new {@link MapKey}.
     */
    MapKey(int userId, int contextId, String address) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.address = address;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId() {
        return contextId;
    }

    /**
     * Gets the member address; e.g. <code>"192.168.2.109:5557"</code>.
     *
     * @return The member address
     */
    String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(48);
        builder.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (address != null) {
            builder.append("address=").append(address);
        }
        builder.append("}");
        return builder.toString();
    }

}
