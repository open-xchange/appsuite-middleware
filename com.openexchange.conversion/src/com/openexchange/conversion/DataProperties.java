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

package com.openexchange.conversion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataProperties} - Container for data properties like content type, version, character set, name , size, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataProperties implements Cloneable {

    /**
     * Property for content-type
     */
    public static final String PROPERTY_CONTENT_TYPE = "com.openexchange.conversion.content-type";

    /**
     * Property for version
     */
    public static final String PROPERTY_VERSION = "com.openexchange.conversion.version";

    /**
     * Property for charset
     */
    public static final String PROPERTY_CHARSET = "com.openexchange.conversion.charset";

    /**
     * Property for name
     */
    public static final String PROPERTY_NAME = "com.openexchange.conversion.name";

    /**
     * Property for size
     */
    public static final String PROPERTY_SIZE = "com.openexchange.conversion.size";

    /**
     * Property for disposition
     */
    public static final String PROPERTY_DISPOSITION = "com.openexchange.conversion.disposition";

    /**
     * Property for E-Mail header prefix.
     */
    public static final String PROPERTY_EMAIL_HEADER_PREFIX= "com.openexchange.conversion.email.header";

    /**
     * Property for identifier
     */
    public static final String PROPERTY_ID = "com.openexchange.conversion.id";

    /**
     * Property for folder identifier.
     */
    public static final String PROPERTY_FOLDER_ID = "com.openexchange.conversion.folderId";

    /**
     * Constant for empty data arguments
     */
    public static final DataProperties EMPTY_PROPS = new DataProperties(true, 0);

    private Map<String, String> map;

    /**
     * Initializes a new {@link DataProperties} with the default initial capacity (4).
     */
    public DataProperties() {
        this(false, 4);
    }

    /**
     * Initializes a new {@link DataProperties}
     *
     * @param initialCapacity The initial capacity
     */
    public DataProperties(final int initialCapacity) {
        this(false, initialCapacity);
    }

    private DataProperties(final boolean empty, final int initialCapacity) {
        super();
        if (empty) {
            map = Collections.unmodifiableMap(new HashMap<String, String>(initialCapacity));
        } else {
            map = new HashMap<String, String>(initialCapacity);
        }
    }

    /**
     * Returns <code>true</code> if these data properties contain a mapping for the specified key.
     *
     * @param key The key whose presence in these data properties is to be tested.
     * @return <code>true</code> if this data properties contain a mapping for the specified key; otherwise <code>false</code>
     */
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value for the specified key. Returns <code>null</code> if the data properties contain no mapping for this key. A return
     * value of <code>null</code> does not necessarily indicate that the data properties contain no mapping for the key; it's also possible
     * that the data properties explicitly map the key to <code>null</code>. The {@link #containsKey(String)} operation may be used to
     * distinguish these two cases.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value to for the specified key, or <code>null</code> if the data properties contain no mapping for this key.
     */
    public String get(final String key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key. If the data properties previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key. A <code>null</code>
     *         return can also indicate that the data properties previously associated <code>null</code> with the specified key.
     */
    public String put(final String key, final String value) {
        if (null == value) {
            /*
             * Consider as a remove
             */
            return map.remove(key);
        }
        return map.put(key, value);
    }

    /**
     * Removes the mapping for this key from these data properties if it is present.
     *
     * @param key The key whose mapping is to be removed from the data properties.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     */
    public String remove(final String key) {
        return map.remove(key);
    }

    /**
     * Gets this data properties as a {@link Map java.util.Map}
     *
     * @return This data properties as a {@link Map java.util.Map}
     */
    public Map<String, String> toMap() {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<String, String>(map);
    }

    @Override
    public Object clone() {
        try {
            final DataProperties clone = (DataProperties) super.clone();
            clone.map = new HashMap<String, String>(map.size());
            clone.map.putAll(map);
            return clone;
        } catch (CloneNotSupportedException e) {
            // Cannot occur
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
        }
    }
}
