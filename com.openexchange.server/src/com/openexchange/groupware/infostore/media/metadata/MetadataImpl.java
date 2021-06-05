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
import com.openexchange.metadata.MetadataEntry;

/**
 * {@link MetadataImpl} - Represents an immutable metadata directory.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataImpl implements Metadata {

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

    /** A builder for an instance of <code>Metadata</code> */
    public static class Builder {

        private final Map<String, MetadataEntry> map;

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

        public Builder putEntry(String key, MetadataEntry entry) {
            map.put(key, entry);
            return this;
        }

        public boolean containsKey(String key) {
            return null == key ? false : map.containsKey(key);
        }

        public MetadataImpl build() {
            return new MetadataImpl(map.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(map));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private final Map<String, MetadataEntry> map;

    /**
     * Initializes a new {@link MetadataImpl}.
     */
    MetadataImpl(Map<String, MetadataEntry> map) {
        super();
        this.map = map;
    }

    /**
     * Gets the number of metadata entries.
     *
     * @return The number of metadata entries
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Checks if this metadata is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Checks is this metadata contains a metadata entry associated with given key
     *
     * @param key The key
     * @return <code>true</code> if such a metadata entry is contained; otherwise <code>false</code>
     */
    @Override
    public boolean containsKey(String key) {
        return null == key ? false : map.containsKey(key);
    }

    /**
     * Gets a {@link Set} view of the keys contained in this metadata. The set is unmodifiable.
     *
     * @return A set view of the keys
     */
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Gets a {@link Collection} view of the metadata entries contained in this map. The collection is unmodifiable.
     *
     * @return A collection view of the metadata entries
     */
    @Override
    public Collection<MetadataEntry> values() {
        return map.values();
    }

    /**
     * Gets a {@link Set} view of the key-to-metadata-entry mappings contained in this map. The set is unmodifiable.
     *
     * @return A set view of the key-to-metadata-entry mappings
     */
    @Override
    public Set<Entry<String, MetadataEntry>> entrySet() {
        return map.entrySet();
    }

    /**
     * Creates the map view for this metadata.
     *
     * @return The map view
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> m = new LinkedHashMap<String, Object>(map.size());
        for (Map.Entry<String, MetadataEntry> e : map.entrySet()) {
            m.put(e.getKey(), e.getValue().asMap());
        }
        return m;
    }

}
