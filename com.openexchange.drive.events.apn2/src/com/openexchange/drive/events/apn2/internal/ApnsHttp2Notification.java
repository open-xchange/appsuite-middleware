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

package com.openexchange.drive.events.apn2.internal;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.clevertap.apns.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * {@link ApnsHttp2Notification}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class ApnsHttp2Notification extends Notification {

    private static String maptoString(Map<String, Object> root) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the size for given root map.
     *
     * @param root The root map
     * @return The size
     */
    public static int sizeFor(Map<String, Object> root) {
        try {
            return maptoString(root).getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a notification to be sent to APNS.
     */
    public static class Builder {

        private final HashMap<String, Object> root, aps, alert;
        private final String token;
        private String topic = null;
        private String collapseId = null;
        private long expiration = -1; // defaults to -1, as 0 is a valid value (included only if greater than -1)
        private Priority priority;
        private UUID uuid;

        /**
         * Creates a new notification builder.
         *
         * @param token The device token
         */
        public Builder(String token) {
            this.token = token;
            root = new HashMap<>();
            aps = new HashMap<>();
            alert = new HashMap<>();
        }

        public Builder withMutableContent(boolean mutable) {
            if (mutable) {
                aps.put("mutable-content", Integer.valueOf(1));
            } else {
                aps.remove("mutable-content");
            }

            return this;
        }

        public Builder withMutableContent() {
            return withMutableContent(true);
        }

        public Builder withContentAvailable(boolean contentAvailable) {
            if (contentAvailable) {
                aps.put("content-available", Integer.valueOf(1));
            } else {
                aps.remove("content-available");
            }

            return this;
        }

        public Builder withContentAvailable() {
            return withContentAvailable(true);
        }

        public Builder withAlertBody(String body) {
            alert.put("body", body);
            return this;
        }

        public Builder withAlertTitle(String title) {
            alert.put("title", title);
            return this;
        }

        public Builder withCustomAlertLocKey(String locKey) {
            alert.put("loc-key", locKey);
            return this;
        }

        public Builder withCustomAlertActionLocKey(String actionLocKey) {
            alert.put("action-loc-key", actionLocKey);
            return this;
        }

        public Builder withSound(String sound) {
            if (sound != null) {
                aps.put("sound", sound);
            } else {
                aps.remove("sound");
            }

            return this;
        }

        public Builder withCategory(String category) {
            if (category != null) {
                aps.put("category", category);
            } else {
                aps.remove("category");
            }
            return this;
        }

        public Builder withBadge(int badge) {
            aps.put("badge", Integer.valueOf(badge));
            return this;
        }

        public Builder withCustomField(String key, Object value) {
            root.put(key, value);
            return this;
        }

        public Builder withTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder withCollapseId(String collapseId) {
            this.collapseId = collapseId;
            return this;
        }

        public Builder withExpiration(long expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Builds the notification.
         *
         * @return The notification
         */
        public ApnsHttp2Notification build() {
            root.put("aps", aps);
            aps.put("alert", alert);
            return new ApnsHttp2Notification(root, token, topic, collapseId, expiration, priority, uuid);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ApnsHttp2Notification}.
     *
     * @param root The root map describing the payload
     * @param token The device token
     * @param topic The topic to advertise
     * @param collapseId The optional collapse identifier
     * @param expiration The optional expiration
     * @param priority The optional priority
     * @param uuid The optional UUID
     */
    public ApnsHttp2Notification(Map<String, Object> root, String token) {
        this(root, token, null, null, -1, null, null);
    }

    /**
     * Initializes a new {@link ApnsHttp2Notification}.
     *
     * @param root The root map describing the payload
     * @param token The device token
     * @param topic The topic to advertise
     * @param collapseId The optional collapse identifier
     * @param expiration The optional expiration
     * @param priority The optional priority
     * @param uuid The optional UUID
     */
    public ApnsHttp2Notification(Map<String, Object> root, String token, String topic, String collapseId, long expiration, Priority priority, UUID uuid) {
        super(maptoString(root), token, topic, collapseId, expiration, priority, uuid);
    }

}
