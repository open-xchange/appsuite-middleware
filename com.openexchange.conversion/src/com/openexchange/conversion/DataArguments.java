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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataArguments} - A container for data conversion arguments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataArguments {

    /**
     * Constant for empty data arguments
     */
    public static final DataArguments EMPTY_ARGS = new DataArguments(true, 0);

    private final Map<String, String> map;

    private volatile String id;

    /**
     * Initializes a new {@link DataArguments} with the default initial capacity (4).
     */
    public DataArguments() {
        this(false, 4);
    }

    /**
     * Initializes a new {@link DataArguments}
     *
     * @param initialCapacity The initial capacity
     */
    public DataArguments(final int initialCapacity) {
        this(false, initialCapacity);
    }

    /**
     * Construct DataArguments out of an existing Map.
     *
     * @param arguments The Map containing the key-value pairs for these arguments
     */
    public DataArguments(Map<String, String> arguments) {
        this.map = new HashMap<String, String>(arguments);
    }

    private DataArguments(final boolean empty, final int initialCapacity) {
        super();
        if (empty) {
            map = Collections.unmodifiableMap(new HashMap<String, String>(initialCapacity));
        } else {
            map = new HashMap<String, String>(initialCapacity);
        }
    }

    /**
     * Returns <code>true</code> if these data arguments contain a mapping for the specified key.
     *
     * @param key The key whose presence in these data arguments is to be tested.
     * @return <code>true</code> if this data arguments contain a mapping for the specified key; otherwise <code>false</code>
     */
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value for the specified key. Returns <code>null</code> if the data arguments contain no mapping for this key. A return
     * value of <code>null</code> does not necessarily indicate that the data arguments contain no mapping for the key; it's also possible
     * that the data arguments explicitly map the key to <code>null</code>. The {@link #containsKey(String)} operation may be used to
     * distinguish these two cases.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value to for the specified key, or <code>null</code> if the data arguments contain no mapping for this key.
     */
    public String get(final String key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key. If the data arguments previously contained a mapping for this key, the old
     * value is replaced by the specified value. A <code>null</code> value removes the key from mapping.
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     */
    public String put(final String key, final String value) {
        if (null == value) {
            return remove(key);
        }
        id = null;
        return map.put(key, value);
    }

    /**
     * Removes the mapping for this key from these data arguments if it is present.
     *
     * @param key The key whose mapping is to be removed from the data arguments.
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     */
    public String remove(final String key) {
        id = null;
        return map.remove(key);
    }

    /**
     * Gets the ID for this data properties.
     * <p>
     * The ID is identical for equal instances of {@link DataArguments}.
     *
     * @return The ID for this data properties
     */
    public String getID() {
        String retval = id;
        if (retval != null) {
            return retval;
        }
        if (map.isEmpty()) {
            return "";
        }
        synchronized (map) {
            retval = id;
            if (retval == null) {
                /*
                 * Sort keys
                 */
                final int size = map.size();
                final String[] sortedKeys = map.keySet().toArray(new String[size]);
                Arrays.sort(sortedKeys);
                /*
                 * Compute ID
                 */
                final StringBuilder builder = new StringBuilder(10 * size);
                builder.append(computeHash(sortedKeys[0]));
                for (int i = 1; i < size; i++) {
                    builder.append('-');
                    builder.append(computeHash(sortedKeys[i]));
                }
                retval = id = builder.toString();
            }
        }
        return retval;
    }

    private int computeHash(final String key) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        final String val = map.get(key);
        result = prime * result + ((val == null) ? 0 : val.hashCode());
        return (result < 0) ? -result : result;
    }

}
