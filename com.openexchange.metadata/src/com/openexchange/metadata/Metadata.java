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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link Metadata} - Represents an immutable metadata directory.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface Metadata {

    /**
     * Gets the number of metadata entries.
     *
     * @return The number of metadata entries
     */
    int size();

    /**
     * Checks if this metadata is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    boolean isEmpty();

    /**
     * Checks is this metadata contains a metadata entry associated with given key
     *
     * @param key The key
     * @return <code>true</code> if such a metadata entry is contained; otherwise <code>false</code>
     */
    boolean containsKey(String key);

    /**
     * Gets a {@link Set} view of the keys contained in this metadata. The set is unmodifiable.
     *
     * @return A set view of the keys
     */
    Set<String> keySet();

    /**
     * Gets a {@link Collection} view of the metadata entries contained in this map. The collection is unmodifiable.
     *
     * @return A collection view of the metadata entries
     */
    Collection<MetadataEntry> values();

    /**
     * Gets a {@link Set} view of the key-to-metadata-entry mappings contained in this map. The set is unmodifiable.
     *
     * @return A set view of the key-to-metadata-entry mappings
     */
    Set<Entry<String, MetadataEntry>> entrySet();

    /**
     * Creates the map view for this metadata.
     *
     * @return The map view
     */
    Map<String, Object> asMap();

}
