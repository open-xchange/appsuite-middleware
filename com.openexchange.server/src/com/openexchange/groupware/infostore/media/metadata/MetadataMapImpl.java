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

package com.openexchange.groupware.infostore.media.metadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.openexchange.metadata.Metadata;
import com.openexchange.metadata.MetadataMap;

/**
 * {@link MetadataMapImpl} - Represents an immutable collection of metadata directories.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataMapImpl implements MetadataMap {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder instance with expected size.
     *
     * @param expectedSize The expected size
     * @return The new builder instance
     */
    public static Builder builder(int expectedSize) {
        return new Builder(expectedSize);
    }

    /** A builder for an instance of <code>MetadataMap</code> */
    public static class Builder {

        private final Map<String, MetadataImpl.Builder> map;

        /**
         * Initializes a new {@link MetadataMapImpl}.
         */
        Builder() {
            super();
            this.map = new LinkedHashMap<>();
        }

        /**
         * Initializes a new {@link MetadataMapImpl}.
         */
        Builder(int expectedSize) {
            super();
            this.map = new LinkedHashMap<>(expectedSize);
        }

        public Builder putMetadata(String key, MetadataImpl.Builder metadataImpl) {
            map.put(key, metadataImpl);
            return this;
        }

        public MetadataImpl.Builder getMetadata(String key) {
            return null == key ? null : map.get(key);
        }

        public void removeMetadata(String key) {
            if (null != key) {
                map.remove(key);
            }
        }

        public MetadataMapImpl build() {
            int size = map.size();
            if (size <= 0) {
                return new MetadataMapImpl(ImmutableMap.of());
            }

            ImmutableMap.Builder<String, Metadata> im = ImmutableMap.builderWithExpectedSize(size);
            for (Map.Entry<String, MetadataImpl.Builder> e : map.entrySet()) {
                im.put(e.getKey(), e.getValue().build());
            }
            return new MetadataMapImpl(im.build());
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private final Map<String, Metadata> map;

    /**
     * Initializes a new {@link MetadataMapImpl}.
     */
    public MetadataMapImpl(Map<String, Metadata> map) {
        super();
        this.map = map;
    }

    /**
     * Gets the number of metadatas.
     *
     * @return The number of metadatas
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Checks if this metadata map is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Checks is this metadata map contains a metadata associated with given key
     *
     * @param key The key
     * @return <code>true</code> if such a metadata is contained; otherwise <code>false</code>
     */
    @Override
    public boolean containsKey(String key) {
        return null == key ? false : map.containsKey(key);
    }

    /**
     * Gets a {@link Set} view of the keys contained in this metadata map. The set is unmodifiable.
     *
     * @return A set view of the keys
     */
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Gets a {@link Collection} view of the metadatas contained in this map. The collection is unmodifiable.
     *
     * @return A collection view of the metadatas
     */
    @Override
    public Collection<Metadata> values() {
        return map.values();
    }

    /**
     * Gets a {@link Set} view of the key-to-metadata mappings contained in this map. The set is unmodifiable.
     *
     * @return A set view of the key-to-metadata mappings
     */
    @Override
    public Set<Entry<String, Metadata>> entrySet() {
        return map.entrySet();
    }

    /**
     * Creates the map view for this metadata map.
     *
     * @return The map view
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> m = new LinkedHashMap<String, Object>(map.size());
        for (Map.Entry<String, Metadata> e : map.entrySet()) {
            m.put(e.getKey(), e.getValue().asMap());
        }
        return m;
    }

}
