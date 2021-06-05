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

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;


/**
 * {@link DefaultPushNotification} - The default push notification.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultPushNotification implements PushNotification {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>DefaultPushNotification</code> */
    public static class Builder {

        private int userId;
        private int contextId;
        private String topic;
        private String sourceToken;
        private Map<String, Object> messageData;

        /**
         * Initializes a new {@link DefaultPushNotification.Builder}.
         */
        Builder() {
            super();
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
         * Sets the topic
         * @param topic The topic
         * @return This builder
         * @throws IllegalArgumentException If the topic name is invalid
         */
        public Builder topic(String topic) {
            PushNotifications.validateTopicName(topic);
            this.topic = topic;
            return this;
        }

        /**
         * Sets the source token
         * @param sourceToken The source token
         * @return This builder
         */
        public Builder sourceToken(String sourceToken) {
            this.sourceToken = sourceToken;
            return this;
        }

        /**
         * Sets the message data
         * @param messageData The message data
         * @return This builder
         */
        public Builder messageData(Map<String, Object> messageData) {
            this.messageData = messageData;
            return this;
        }

        /**
         * Creates the appropriate instance of <code>DefaultPushNotification</code> from this builder's arguments.
         *
         * @return The <code>DefaultPushNotification</code> instance
         * @throws IllegalArgumentException If insufficient arguments are specified
         */
        public DefaultPushNotification build() {
            if (userId <= 0) {
                throw new IllegalArgumentException("User identifier not specified");
            }
            if (contextId <= 0) {
                throw new IllegalArgumentException("Context identifier not specified");
            }
            if (Strings.isEmpty(topic)) {
                throw new IllegalArgumentException("Topic not specified");
            }
            return new DefaultPushNotification(userId, contextId, topic, sourceToken, messageData);
        }

    }

    // ----------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String topic;
    private final String sourceToken;
    private final Map<String, Object> messageData;

    /**
     * Initializes a new {@link DefaultPushNotification}.
     */
    DefaultPushNotification(int userId, int contextId, String topic, String sourceToken, Map<String, Object> messageData) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.topic = topic;
        this.sourceToken = sourceToken;
        this.messageData = null == messageData ? null : ImmutableMap.copyOf(messageData);
    }

    @Override
    public Map<String, Object> getMessageData() {
        return messageData;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getSourceToken() {
        return sourceToken;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = prime * 1 + contextId;
        result = prime * result + userId;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + ((sourceToken == null) ? 0 : sourceToken.hashCode());
        result = prime * result + ((messageData == null) ? 0 : messageData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushNotification)) {
            return false;
        }
        PushNotification other = (PushNotification) obj;
        if (contextId != other.getContextId()) {
            return false;
        }
        if (userId != other.getUserId()) {
            return false;
        }
        if (topic == null) {
            if (other.getTopic() != null) {
                return false;
            }
        } else if (!topic.equals(other.getTopic())) {
            return false;
        }
        if (sourceToken == null) {
            if (other.getSourceToken() != null) {
                return false;
            }
        } else if (!sourceToken.equals(other.getSourceToken())) {
            return false;
        }
        if (messageData == null) {
            if (other.getMessageData() != null) {
                return false;
            }
        } else if (!messageData.equals(other.getMessageData())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (topic != null) {
            sb.append("topic=").append(topic).append(", ");
        }
        if (sourceToken != null) {
            sb.append("sourceToken=").append(sourceToken).append(", ");
        }
        if (messageData != null) {
            sb.append("messageData=").append(messageData);
        }
        sb.append("}");
        return sb.toString();
    }

}
