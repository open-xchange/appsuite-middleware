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
 * {@link MapValue} - The value for Hazelcast map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
class MapValue {

    /**
     * Parses the map value information from specified string.
     *
     * @param socketInfo The socket information as string
     * @return The parsed value
     */
    static MapValue parseFrom(String socketInfo) {
        if (null == socketInfo) {
            return null;
        }

        int pos = socketInfo.lastIndexOf(':');
        if (pos <= 0) {
            throw new IllegalArgumentException("Illegal socket info: " + socketInfo);
        }

        return new MapValue(socketInfo.substring(0, pos), ++pos == socketInfo.length() ? null : socketInfo.substring(pos));
    }

    /**
     * Parses the Web Socket path information from specified string.
     *
     * @param socketInfo The socket information as string
     * @return The parsed path
     */
    static String parsePathFrom(String socketInfo) {
        if (null == socketInfo) {
            return null;
        }

        int pos = socketInfo.lastIndexOf(':');
        if (pos <= 0) {
            throw new IllegalArgumentException("Illegal socket info: " + socketInfo);
        }

        return ++pos == socketInfo.length() ? null : socketInfo.substring(pos);
    }

    // ------------------------------------------------------------------------------------

    private final String connectionId;
    private final String path;

    private int hash;

    /**
     * Initializes a new {@link MapValue}.
     *
     * @param connectionId The connection identifier
     * @param path The path
     */
    MapValue(String connectionId, String path) {
        super();
        this.connectionId = connectionId;
        this.path = path;
    }

    /**
     * Gets the connection identifier
     *
     * @return The connection identifier
     */
    String getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        // No need to be thread-safe. Each thread then computes itself in worst case
        int h = hash;
        if (h == 0 && (null != connectionId || null != path)) {
            int prime = 31;
            h = prime * 1 + ((connectionId == null) ? 0 : connectionId.hashCode());
            h = prime * h + ((path == null) ? 0 : path.hashCode());
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MapValue)) {
            return false;
        }
        MapValue other = (MapValue) obj;
        if (connectionId == null) {
            if (other.connectionId != null) {
                return false;
            }
        } else if (!connectionId.equals(other.connectionId)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(48);
        builder.append("{");
        if (connectionId != null) {
            builder.append("connectionId=").append(connectionId).append(", ");
        }
        if (path != null) {
            builder.append("path=").append(path);
        }
        builder.append("}");
        return builder.toString();
    }

}
