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

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link RemoteMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RemoteMessage {

    private final Queue<String> payloads;
    private final String pathFilter;
    private final int userId;
    private final int contextId;
    private volatile Integer hash;

    /**
     * Initializes a new {@link RemoteMessage}.
     */
    public RemoteMessage(String message, String pathFilter, int userId, int contextId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.pathFilter = pathFilter;
        payloads = new ConcurrentLinkedQueue<>(Arrays.asList(message));
    }

    /**
     * Gets the path filter
     *
     * @return The path filter
     */
    public String getPathFilter() {
        return pathFilter;
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
     * Gets the payloads
     *
     * @return The payloads
     */
    public Queue<String> getPayloads() {
        return payloads;
    }

    /**
     * Merges this distribution's payloads with other ones
     *
     * @param other The distribution providing the pay,oads to merge
     */
    public void mergeWith(RemoteMessage other) {
        if (this == other) {
            return;
        }
        if (null == other) {
            return;
        }
        Queue<String> thisPayloads = payloads;
        synchronized (thisPayloads) {
            for (String otherPayload : other.payloads) {
                if (!thisPayloads.contains(otherPayload)) {
                    thisPayloads.add(otherPayload);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        Integer tmp = hash;
        if (null == tmp) {
            // May be computed concurrently...
            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((pathFilter == null) ? 0 : pathFilter.hashCode());
            tmp = Integer.valueOf(result);
            hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RemoteMessage other = (RemoteMessage) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (pathFilter == null) {
            if (other.pathFilter != null) {
                return false;
            }
        } else if (!pathFilter.equals(other.pathFilter)) {
            return false;
        }
        return true;
    }

}
