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
import java.util.Map.Entry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link PushNotifications} - The utility class for push notification module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushNotifications {

    private PushNotifications() {
        super();
    }

    /**
     * Creates a new message data builder instance.
     *
     * @return The new builder instance
     */
    public static MessageDataBuilder messageDataBilder() {
        return new MessageDataBuilder();
    }

    /** A builder for message data */
    public static final class MessageDataBuilder {

        private final Builder<String, Object> builder;

        MessageDataBuilder() {
            builder = ImmutableMap.builder();
        }

        /**
         * Associates field with value in the built map.
         * <p>
         * Duplicate keys are not allowed, and will cause {@link #build() build} to fail.
         *
         * @param field The field to insert
         * @param value The value to associate with
         * @return This builder instance
         */
        public MessageDataBuilder put(PushNotificationField field, Object value) {
            if (field != null && value != null) {
                builder.put(field.getId(), value);
            }
            return this;
        }

        /**
         * Associates key with value in the built map.
         * <p>
         * Duplicate keys are not allowed, and will cause {@link #build() build} to fail.
         *
         * @param key The key to insert
         * @param value The value to associate with
         * @return This builder instance
         */
        public MessageDataBuilder put(String key, Object value) {
            if (key != null && value != null) {
                builder.put(key, value);
            }
            return this;
        }

        /**
         * Puts specified entry into the built map.
         * <p>
         * Duplicate keys are not allowed, and will cause {@link #build() build} to fail.
         *
         * @param entry The entry to insert
         * @return This builder instance
         */
        public MessageDataBuilder put(Entry<? extends String, ? extends Object> entry) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                builder.put(entry);
            }
            return this;
        }

        /**
         * Associates all of the given map's keys and values in the built map.
         * <p>
         * Duplicate keys are not allowed, and will cause {@link #build() build} to fail.
         *
         * @param map The map to insert
         * @return This builder instance
         */
        public MessageDataBuilder putAll(Map<? extends String, ? extends Object> map) {
            if (null != map) {
                for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        builder.put(entry);
                    }
                }
            }
            return this;
        }

        /**
         * Adds all of the given entries to the built map.
         * <p>
         * Duplicate keys are not allowed, and will cause {@link #build() build} to fail.
         *
         * @param entries The entries to insert
         * @return This builder instance
         */
        public MessageDataBuilder putAll(Iterable<? extends Entry<? extends String, ? extends Object>> entries) {
            if (entries != null) {
                for (Entry<? extends String,? extends Object> entry : entries) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        builder.put(entry);
                    }
                }
            }
            return this;
        }

        /**
         * Builds the resulting (immutable) message data.
         *
         * @return The (immutable) message data
         */
        public Map<String, Object> build() {
            return builder.build();
        }
    }

    /**
     * Builds the (immutable) message data for specified arguments.
     *
     * @param args The arguments
     * @return The (immutable) message data
     */
    public static Map<String, Object> messageDataFor(Object... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Builder<String, Object> builder = ImmutableMap.builder();
        for (int i = 0; i < length; i+=2) {
            if (args[i] != null && args[i+1] != null) {
                builder.put(args[i].toString(), args[i+1]);
            }
        }
        return builder.build();
    }

    /**
     * Gets the length in bytes for specified payload string.
     *
     * @param payload The payload
     * @return The length in bytes
     */
    public static int getPayloadLength(String payload) {
        if (null == payload) {
            return 0;
        }

        byte[] bytes;
        try {
            bytes = payload.getBytes(Charsets.UTF_8);
        } catch (Exception ex) {
            bytes = payload.getBytes();
        }
        return bytes.length;
    }

    /**
     * Gets the value from notification's data associated with specified field.
     *
     * @param field The field
     * @param notification The notification to grab from
     * @return The value or <code>null</code>
     */
    public static <V> V getValueFor(PushNotificationField field, PushNotification notification) {
        if (null == field) {
            return null;
        }
        return getValueFor(field.getId(), notification);
    }

    /**
     * Gets the value from notification's data associated with specified field.
     *
     * @param field The field
     * @param notification The notification to grab from
     * @return The value or <code>null</code>
     */
    public static <V> V getValueFor(String field, PushNotification notification) {
        if (null == field || null == notification) {
            return null;
        }
        return (V) notification.getMessageData().get(field);
    }

    // -----------------------------------------------------------------------------------------------------------

    private static final String ALL = KnownTopic.ALL.getName();

    /**
     * Validates the topic name.
     *
     * @param topic The topic name to validate.
     * @throws IllegalArgumentException If the topic name is invalid.
     */
    public static void validateTopicName(String topic) {
        if (Strings.isEmpty(topic)) {
            throw new IllegalArgumentException("topic is null, empty or only consists of white-space characters");
        }

        if (ALL.equals(topic)) {
            return;
        }

        if (topic.endsWith(":*")) {
            topic = topic.substring(0, topic.length() - 2);
        }

        int length = topic.length();
        if (length == 0) {
            throw new IllegalArgumentException("empty topic");
        }
        for (int i = 0; i < length; i++) {
            char ch = topic.charAt(i);
            if (ch == ':') {
                // Can't start or end with a ':' but anywhere else is okay
                if (i == 0 || (i == length - 1)) {
                    throw new IllegalArgumentException("invalid topic: " + topic);
                }
                // Can't have "::" as that implies empty token
                if (topic.charAt(i - 1) == ':') {
                    throw new IllegalArgumentException("invalid topic: " + topic);
                }
                continue;
            }
            if (('A' <= ch) && (ch <= 'Z')) {
                continue;
            }
            if (('a' <= ch) && (ch <= 'z')) {
                continue;
            }
            if (('0' <= ch) && (ch <= '9')) {
                continue;
            }
            if ((ch == '_') || (ch == '-')) {
                continue;
            }
            throw new IllegalArgumentException("invalid topic: " + topic);
        }
    }

}
