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

package com.openexchange.java;

import javax.annotation.concurrent.NotThreadSafe;


/**
 * {@link Reference} - A simple reference class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 * @param <V> The type of object referred to by this reference
 */
@NotThreadSafe
public final class Reference<V> {

    /** The value */
    private V value;

    /**
     * Initializes a new {@link Reference}.
     */
    public Reference() {
        this(null);
    }

    /**
     * Initializes a new {@link Reference}.
     *
     * @param value The value to set
     */
    public Reference(V value) {
        super();
        this.value = value;
    }

    /**
     * Checks for a <code>null</code> value
     *
     * @return <code>true</code> if this reference holds a <code>null</code>  value; otherwise <code>false</code>
     */
    public boolean hasNoValue() {
        return value == null;
    }

    /**
     * Checks for a non-<code>null</code> value
     *
     * @return <code>true</code> if this reference holds a non-<code>null</code>  value; otherwise <code>false</code>
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public V getValue() {
        return value;
    }

    /**
     * Gets the current value & sets to the new value.
     *
     * @return The current value
     */
    public V getAndSetValue(V newValue) {
        V value = this.value;
        this.value = newValue;
        return value;
    }

    /**
     * Sets the value
     *
     * @param value The value to set
     * @return This reference with new value applied
     */
    public Reference<V> setValue(V value) {
        this.value = value;
        return this;
    }

}
