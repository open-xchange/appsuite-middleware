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
