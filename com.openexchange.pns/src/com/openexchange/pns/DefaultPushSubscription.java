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

import java.util.Date;
import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link DefaultPushSubscription} - The default implementation for {@code PushSubscription}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultPushSubscription implements PushSubscription {

    /**
     * Gets the appropriate {@link DefaultPushSubscription} instance from specified subscription.
     *
     * @param subscription The subscription
     * @return The appropriate {@link DefaultPushSubscription} instance
     */
    public static DefaultPushSubscription instanceFor(PushMatch match) {
        Builder builder = new Builder()
            .contextId(match.getContextId())
            .token(match.getToken())
            .transportId(match.getTransportId())
            .userId(match.getUserId())
            .client(match.getClient());

        return builder.build();
    }

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>DefaultPushSubscription</code> instance */
    public static class Builder {

        int userId;
        int contextId;
        List<String> topics;
        String transportId;
        String token;
        String client;
        Date expires;

        /** Creates a new builder */
        Builder() {
            super();
        }

        /**
         * Sets the client identifier
         * @param client The client identifier
         * @return This builder
         */
        public Builder client(String client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the user identifier
         * @param userId The user identifier
         * @return This builder
         */
        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the context identifier
         * @param contextId The context identifier
         * @return This builder
         */
        public Builder contextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the topics
         * @param topics The topics
         * @return This builder
         * @throws IllegalArgumentException If a topic name is invalid.
         */
        public Builder topics(List<String> topics) {
            if (null != topics) {
                if (topics.isEmpty()) {
                    throw new IllegalArgumentException("empty topics");
                }
                for (String topic : topics) {
                    PushNotifications.validateTopicName(topic);
                }
            }
            this.topics = topics;
            return this;
        }

        /**
         * Sets the transport identifier
         * @param transportId The transport identifier
         * @return This builder
         */
        public Builder transportId(String transportId) {
            this.transportId = transportId;
            return this;
        }

        /**
         * Sets the token
         * <p>
         * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
         * Note: A non-empty token is required to be set in case nature is set to {@link Nature#PERSISTENT}
         * </div>
         * @param token The token
         * @return This builder
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the expires
         * @param expires The expires
         * @return This builder
         */
        public Builder expires(Date expires) {
            this.expires =expires;
            return this;
        }

        /**
         * Builds the <code>DefaultPushSubscription</code> instance.
         * @return The resulting <code>DefaultPushSubscription</code> instance
         */
        public DefaultPushSubscription build() {
            return new DefaultPushSubscription(this);
        }
    }

    // --------------------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String client;
    private final List<String> topics;
    private final String transportId;
    private final String token;
    private final Date expires;

    /**
     * Initializes a new {@link DefaultPushSubscription}.
     */
    DefaultPushSubscription(Builder builder) {
        super();
        this.topics = null == builder.topics ? null : ImmutableList.copyOf(builder.topics);
        this.contextId = builder.contextId;
        this.token = builder.token;
        this.transportId = builder.transportId;
        this.userId = builder.userId;
        this.client = builder.client;
        this.expires = builder.expires;
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
    public List<String> getTopics() {
        return topics;
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
    public String getClient() {
        return client;
    }

    @Override
    public Date getExpires() {
        return expires;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((transportId == null) ? 0 : transportId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushSubscription)) {
            return false;
        }
        PushSubscription other = (PushSubscription) obj;
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
        StringBuilder sb = new StringBuilder(96);
        sb.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (client != null) {
            sb.append("client=").append(client).append(", ");
        }
        if (topics != null) {
            sb.append("topics=").append(topics).append(", ");
        }
        if (transportId != null) {
            sb.append("transportId=").append(transportId).append(", ");
        }
        if (token != null) {
            sb.append("token=").append(token).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
