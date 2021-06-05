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

import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.metadata.MetadataEntry;

/**
 * {@link MetadataEntryImpl} - Represents an immutable metadata directory entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataEntryImpl implements MetadataEntry {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for an instance of <code>MetadataEntry</code> */
    public static class Builder {

        private String id;
        private String name;
        private String value;
        private String description;

        /**
         * Initializes a new {@link MetadataMapImpl}.
         */
        Builder() {
            super();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public MetadataEntryImpl build() {
            return new MetadataEntryImpl(id, name, value, description);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final String name;
    private final String value;
    private final String description;

    /**
     * Initializes a new {@link MetadataEntryImpl}.
     */
    MetadataEntryImpl(String id, String name, String value, String description) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Creates the map view for this metadata.
     *
     * @return The map view
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> m = new LinkedHashMap<String, Object>(4);
        if (null != id) {
            m.put("id", id);
        }
        if (null != name) {
            m.put("name", name);
        }
        if (null != value) {
            m.put("value", value);
        }
        if (null != description) {
            m.put("description", description);
        }
        return m;
    }

}
