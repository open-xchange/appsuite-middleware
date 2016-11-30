/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            return new DefaultPushNotification(userId, contextId, topic, messageData);
        }

    }

    // ----------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String topic;
    private final Map<String, Object> messageData;

    /**
     * Initializes a new {@link DefaultPushNotification}.
     */
    DefaultPushNotification(int userId, int contextId, String topic, Map<String, Object> messageData) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.topic = topic;
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
        if (messageData != null) {
            sb.append("messageData=").append(messageData);
        }
        sb.append("}");
        return sb.toString();
    }

}
