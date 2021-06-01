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

package com.openexchange.websockets;

/**
 * {@link WebSocketInfo} - Provides basic Web Socket information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class WebSocketInfo implements Comparable<WebSocketInfo> {

    /**
     * Creates a new builder.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>WebSocketInfo</code> */
    public static final class Builder {

        private int userId;
        private int contextId;
        private String address;
        private ConnectionId connectionId;
        private String path;

        Builder() {
            super();
        }

        /**
         * Sets the user identifier
         *
         * @param userId The user identifier
         */
        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the context identifier
         *
         * @param contextId The context identifier
         */
        public Builder contextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the path
         *
         * @param path The path
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the member address
         *
         * @param address The member address
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the connection identifier
         *
         * @param memberUuid The connection identifier
         */
        public Builder connectionId(ConnectionId connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        /**
         * Creates the <code>WebSocketInfo</code> instance from this builder's properties.
         *
         * @return The <code>WebSocketInfo</code> instance
         */
        public WebSocketInfo build() {
            return new WebSocketInfo(userId, contextId, address, connectionId, path);
        }
    }

    // -------------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String address;
    private final ConnectionId connectionId;
    private final String path;
    private int hash;

    /**
     * Initializes a new {@link WebSocketInfo}.
     */
    WebSocketInfo(int userId, int contextId, String address, ConnectionId connectionId, String path) {
        super();
        hash = 0;
        this.userId = userId;
        this.contextId = contextId;
        this.address = address;
        this.connectionId = connectionId;
        this.path = path;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the member address
     *
     * @return The member address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the connection identifier
     *
     * @return The connection identifier
     */
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

    @Override
    public int compareTo(WebSocketInfo other) {
        int result = Integer.compare(contextId, other.contextId);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(userId, other.userId);
        if (result != 0) {
            return result;
        }

        if (address == null) {
            if (other.address != null) {
                return -1;
            }
        } else {
            result = null == other.address ? 1 : address.compareTo(other.address);
            if (result != 0) {
                return result;
            }
        }

        if (connectionId == null) {
            if (other.connectionId != null) {
                return -1;
            }
        } else {
            result = null == other.connectionId ? 1 : connectionId.compareTo(other.connectionId);
            if (result != 0) {
                return result;
            }
        }

        if (path == null) {
            if (other.path != null) {
                return -1;
            }
        } else {
            result = null == other.path ? 1 : path.compareTo(other.path);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }


    @Override
    public int hashCode() {
        int result = hash; // No need to synchronize...
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((address == null) ? 0 : address.hashCode());
            result = prime * result + ((connectionId == null) ? 0 : connectionId.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != WebSocketInfo.class) {
            return false;
        }
        WebSocketInfo other = (WebSocketInfo) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
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
        StringBuilder builder2 = new StringBuilder(64);
        builder2.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (address != null) {
            builder2.append("address=").append(address).append(", ");
        }
        if (path != null) {
            builder2.append("path=").append(path).append(", ");
        }
        if (connectionId != null) {
            builder2.append("connectionId=").append(connectionId);
        }
        builder2.append("}");
        return builder2.toString();
    }

}
