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

package com.openexchange.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * {@link MetadataMap} - Represents an immutable collection of metadata directories.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface MetadataMap {

    /** The empty metadata map */
    public static final MetadataMap EMPTY = new MetadataMap() {

        @Override
        public Collection<Metadata> values() {
            return Collections.emptyList();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Set<String> keySet() {
            return Collections.emptySet();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Set<Entry<String, Metadata>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public boolean containsKey(String key) {
            return false;
        }

        @Override
        public Map<String, Object> asMap() {
            return Collections.emptyMap();
        }
    };

    // -----------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the number of metadata directories.
     *
     * @return The number of metadata directories
     */
    int size();

    /**
     * Checks if this metadata map is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    boolean isEmpty();

    /**
     * Checks is this metadata map contains a metadata associated with given key
     *
     * @param key The key
     * @return <code>true</code> if such a metadata is contained; otherwise <code>false</code>
     */
    boolean containsKey(String key);

    /**
     * Gets a {@link Set} view of the keys contained in this metadata map. The set is unmodifiable.
     *
     * @return A set view of the keys
     */
    Set<String> keySet();

    /**
     * Gets a {@link Collection} view of the metadata directories contained in this map. The collection is unmodifiable.
     *
     * @return A collection view of the metadata directories
     */
    Collection<Metadata> values();

    /**
     * Gets a {@link Set} view of the key-to-metadata mappings contained in this map. The set is unmodifiable.
     *
     * @return A set view of the key-to-metadata mappings
     */
    Set<Entry<String, Metadata>> entrySet();

    /**
     * Creates the map view for this metadata map.
     *
     * @return The map view
     */
    Map<String, Object> asMap();

}
