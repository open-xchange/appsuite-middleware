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

package com.openexchange.session;

import java.util.NoSuchElementException;

/**
 * {@link SessionAttribute} - Represents a value (allowing <code>null</code>) for a session attribute that is supposed to be changed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SessionAttribute<V> {

    /** The common instance for an unset session attribute */
    private static final SessionAttribute<?> UNSET = new SessionAttribute<>(null, false);

    /**
     * Returns an unset {@code SessionAttribute} instance. No value is present for this SessionAttribute.
     *
     * @param <T> Type of the unset value
     * @return An unset {@code SessionAttribute}
     */
    public static <V> SessionAttribute<V> unset() {
        @SuppressWarnings("unchecked") SessionAttribute<V> t = (SessionAttribute<V>) UNSET;
        return t;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The common instance for session attribute with a <code>null</code> value */
    private static final SessionAttribute<?> EMPTY = new SessionAttribute<>(null, true);

    /**
     * Returns an empty {@code SessionAttribute} instance. A <code>null</code> value is present for this SessionAttribute.
     *
     * @param <T> Type of the non-existent value
     * @return An empty {@code SessionAttribute}
     */
    public static <V> SessionAttribute<V> empty() {
        @SuppressWarnings("unchecked") SessionAttribute<V> t = (SessionAttribute<V>) EMPTY;
        return t;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new session attribute carrying specified value.
     *
     * @param <V> The value type
     * @param value The value
     * @return The session attribute
     */
    public static <V> SessionAttribute<V> valueOf(V value) {
        return value == null ? empty() : new SessionAttribute<V>(value, true);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final V value;
    private final boolean set;

    /**
     * Initializes a new {@link SessionAttribute}.
     *
     * @param value The value for this session attribute
     */
    public SessionAttribute(V value) {
        this(value, true);
    }

    /**
     * Initializes a new {@link SessionAttribute}.
     *
     * @param value The value for this session attribute
     * @param set <code>true</code> if value has been explicitly set; otherwise <code>false</code>
     */
    private SessionAttribute(V value, boolean set) {
        super();
        this.value = value;
        this.set = set;
    }

    /**
     * Checks if this session attribute has been set.
     *
     * @return code>true</code> if set; otherwise <code>false</code>
     */
    public boolean isSet() {
        return set;
    }

    /**
     * Gets the value set for this session attribute.
     *
     * @return The value or <code>null</code>
     * @throws NoSuchElementException If no value has been set; that is invoking {@link #isSet()} returns <code>false</code>
     */
    public V get() {
        if (!set) {
            throw new NoSuchElementException("No value has been set");
        }
        return value;
    }

    @Override
    public String toString() {
        return set ? (value == null ? "null" : value.toString()) : "<empty>";
    }

}
