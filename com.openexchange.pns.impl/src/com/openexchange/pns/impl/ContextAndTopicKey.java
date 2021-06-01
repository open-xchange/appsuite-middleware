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

package com.openexchange.pns.impl;

import com.openexchange.pns.PushNotification;

/**
 * Simple key that combines context identifier and topic.
 */
class ContextAndTopicKey {

    final int contextId;
    final String topic;
    private final int hash;

    /**
     * Initializes a new {@link ContextAndTopicKey}.
     */
    ContextAndTopicKey(PushNotification notification) {
        this(notification.getTopic(), notification.getContextId());
    }

    /**
     * Initializes a new {@link ContextAndTopicKey}.
     */
    ContextAndTopicKey(String topic, int contextId) {
        super();
        this.contextId = contextId;
        this.topic = topic;

        int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        this.hash = result;
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
        if (!(obj instanceof ContextAndTopicKey)) {
            return false;
        }
        ContextAndTopicKey other = (ContextAndTopicKey) obj;
        if (contextId != other.contextId) {
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

}
