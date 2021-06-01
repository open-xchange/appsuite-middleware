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

package com.openexchange.pns;

/**
 * {@link EnabledKey} - A helper class acting as a key to cache availability checks based on topic, client, user identifier and/or context identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class EnabledKey {

    private final String topic;
    private final String client;
    private final int userId;
    private final int contextId;
    private final int hash;

    /**
     * Initializes a new {@link EnabledKey}.
     *
     * @param topic The topic; e.g. <code>"ox:mail:new"</code>
     * @param client The client identifier; e.g <code>"open-xchange-appsuite"</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public EnabledKey(String topic, String client, int userId, int contextId) {
        super();
        this.topic = topic;
        this.client = client;
        this.userId = userId;
        this.contextId = contextId;

        int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != EnabledKey.class) {
            return false;
        }
        EnabledKey other = (EnabledKey) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!topic.equals(other.topic)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the topic
     *
     * @return The topic or <code>null</code>
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier or <code>null</code>
     */
    public String getClient() {
        return client;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("{");
        builder.append("userId=").append(userId).append(", ");
        builder.append("contextId=").append(contextId).append(", ");
        builder.append("topic=").append(topic).append(", ");
        builder.append("client=").append(client);
        builder.append("}");
        return builder.toString();
    }

}
