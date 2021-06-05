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

package com.openexchange.caching.events;

import java.io.Serializable;
import java.util.List;


/**
 * {@link ConditionalCacheEvent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ConditionalCacheEvent extends CacheEvent {

    private static final long serialVersionUID = -671101704515080161L;

    /**
     * Initializes a new {@link ConditionalCacheEvent}.
     *
     * @param operation The cache operation
     * @param region The cache region
     * @param keys The keys of the affected cache entries
     * @param groupName The cache group name
     * @param condition The condition to wait for; must not be <code>null</code>
     */
    public ConditionalCacheEvent(CacheOperation operation, String region, List<Serializable> keys, String groupName, Condition condition) {
        super(operation, region, keys, groupName, condition);
        if (null == condition) {
            throw new IllegalArgumentException("condition must not be null");
        }
    }

    /**
     * Initializes a new {@link ConditionalCacheEvent}.
     *
     * @param event The cache event
     * @param condition The condition to wait for; must not be <code>null</code>
     */
    public ConditionalCacheEvent(CacheEvent event, Condition condition) {
        super(event.operation, event.region, event.keys, event.groupName, condition);
        if (null == condition) {
            throw new IllegalArgumentException("condition must not be null");
        }
    }

    /**
     * Gets the associated condition.
     *
     * @return The condition to wait for.
     */
    public Condition getCondition() {
        return condition;
    }
}
