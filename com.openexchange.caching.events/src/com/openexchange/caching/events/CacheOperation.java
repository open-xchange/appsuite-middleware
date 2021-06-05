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

/**
 * {@link CacheOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum CacheOperation {

    /**
     * Invalidation of a cache entry, due to update or removal
     */
    INVALIDATE("invalidate"),

    /**
     * Invalidation of a cache group
     */
    INVALIDATE_GROUP("invalidate_group"),

    /**
     * Clear cache
     */
    CLEAR("clear"),
    ;

    private final String id;

    private CacheOperation(final String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the cache operation for given identifier.
     *
     * @param id The identifier
     * @return The cache operation or <code>null</code>
     */
    public static CacheOperation cacheOperationFor(final String id) {
        if (null == id) {
            return null;
        }
        for (final CacheOperation cacheOperation : CacheOperation.values()) {
            if (id.equals(cacheOperation.getId())) {
                return cacheOperation;
            }
        }
        return null;
    }
}
