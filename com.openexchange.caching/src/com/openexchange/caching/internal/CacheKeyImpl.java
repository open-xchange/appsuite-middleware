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

package com.openexchange.caching.internal;

import java.util.Arrays;
import com.openexchange.caching.CacheKey;

/**
 * {@link CacheKeyImpl} - A cache key that consists of a context ID and an unique (serializable) identifier of any type.
 */
public class CacheKeyImpl implements CacheKey {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -3144968305668671430L;

    /**
     * Unique identifier of the context.
     */
    private final int contextId;

    /**
     * Object keys of the cached object.
     */
    private final String[] keyObjs;

    /**
     * Hash code of the context specific object.
     */
    private final int hash;

    /**
     * Initializes a new {@link CacheKeyImpl}
     *
     * @param contextId The context ID
     * @param objectId The object ID
     */
    public CacheKeyImpl(final int contextId, final int objectId) {
        this(contextId, String.valueOf(objectId));
    }

    /**
     * Initializes a new {@link CacheKeyImpl}
     *
     * @param contextId The context ID
     * @param key A string to identify the cached object
     * @throws IllegalArgumentException If specified key is <code>null</code>
     */
    public CacheKeyImpl(final int contextId, final String key) {
        this(contextId, new String[] { key });
    }

    /**
     * Initializes a new {@link CacheKeyImpl}.
     *
     * @param contextId The context ID
     * @param keys Strings to identify the cached object.
     * @throws IllegalArgumentException If specified keys are <code>null</code>
     */
    public CacheKeyImpl(final int contextId, final String... keys) {
        super();
        if (null == keys) {
            throw new IllegalArgumentException("keys are null");
        }
        this.contextId = contextId;
        keyObjs = keys;
        // Generate hash
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        for (final String key : keys) {
            result = prime * result + ((key == null) ? 0 : key.hashCode());
        }
        hash = result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CacheKeyImpl)) {
            return false;
        }
        final CacheKeyImpl other = (CacheKeyImpl) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (!Arrays.equals(keyObjs, other.keyObjs)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append('[');
        sb.append(contextId);
        for (final String item : keyObjs) {
            sb.append(',').append(null == item ? "null" : item.toString());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String[] getKeys() {
        final String[] retval = new String[keyObjs.length];
        System.arraycopy(keyObjs, 0, retval, 0, keyObjs.length);
        return retval;
    }
}
