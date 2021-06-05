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

package com.openexchange.pns.subscription.storage.rdb;

import java.util.Date;
import com.openexchange.pns.PushMatch;


/**
 * {@link RdbPushMatch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RdbPushMatch implements PushMatch {

    private final int contextId;
    private final int userId;
    private final String client;
    private final String transportId;
    private final String token;
    private final String topic;
    private final Date lastModified;
    private final Date expires;
    private int hash; // Default to 0

    /**
     * Initializes a new {@link RdbPushMatch}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param client The client identifier
     * @param transportId The transport identifier
     * @param token The token
     * @param topic The matching topic
     * @param lastModified The last-modified date
     */
    public RdbPushMatch(int userId, int contextId, String client, String transportId, String token, String topic, Date lastModified, Date expires) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.client = client;
        this.transportId = transportId;
        this.token = token;
        this.topic = topic;
        this.lastModified = lastModified;
        this.expires = expires;
    }

    /**
     * Gets the last-modified date
     *
     * @return The last-modified date or <code>null</code>
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Gets the expiration date
     *
     * @return The expiration date, or <code>null</code> if not set
     */
    public Date getExpires() {
        return expires;
    }

    @Override
    public String getClient() {
        return client;
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
    public String getTransportId() {
        return transportId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0 ) {
            int prime = 31;
            result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((token == null) ? 0 : token.hashCode());
            result = prime * result + ((client == null) ? 0 : client.hashCode());
            result = prime * result + ((topic == null) ? 0 : topic.hashCode());
            result = prime * result + ((transportId == null) ? 0 : transportId.hashCode());

            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushMatch)) {
            return false;
        }
        PushMatch other = (PushMatch) obj;
        if (contextId != other.getContextId()) {
            return false;
        }
        if (userId != other.getUserId()) {
            return false;
        }
        if (token == null) {
            if (other.getToken() != null) {
                return false;
            }
        } else if (!token.equals(other.getToken())) {
            return false;
        }
        if (client == null) {
            if (other.getClient() != null) {
                return false;
            }
        } else if (!client.equals(other.getClient())) {
            return false;
        }
        if (topic == null) {
            if (other.getTopic() != null) {
                return false;
            }
        } else if (!topic.equals(other.getTopic())) {
            return false;
        }
        if (transportId == null) {
            if (other.getTransportId() != null) {
                return false;
            }
        } else if (!transportId.equals(other.getTransportId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("{contextId=").append(contextId).append(", userId=").append(userId);
        if (client != null) {
            builder.append(", ").append("client=").append(client);
        }
        if (transportId != null) {
            builder.append(", ").append("transportId=").append(transportId);
        }
        if (token != null) {
            builder.append(", ").append("token=").append(token);
        }
        if (topic != null) {
            builder.append(", ").append("topic=").append(topic);
        }
        if (lastModified != null) {
            builder.append(", ").append("lastModified=").append(lastModified);
        }
        builder.append("}");
        return builder.toString();
    }

}
