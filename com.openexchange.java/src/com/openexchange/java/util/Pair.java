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

package com.openexchange.java.util;


/**
 * A generic pair class.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Pair<T, V> {

    private T first;
    private V second;

    /**
     * Initializes a new empty {@link Pair}.
     */
    public Pair() {
        this(null, null);
    }

    /**
     * Initializes a new {@link Pair}.
     *
     * @param first The first value
     * @param second The second value
     */
    public Pair(T first, V second) {
        super();
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first value
     *
     * @return The 1st value
     */
    public T getFirst() {
        return first;
    }

    /**
     * Sets the first value
     *
     * @param first The 1st value
     */
    public void setFirst(T first) {
        this.first = first;
    }

    /**
     * Gets the second value
     *
     * @return The 2nd value
     */
    public V getSecond() {
        return second;
    }

    /**
     * Sets the second value
     *
     * @param second The 2nd value
     */
    public void setSecond(V second) {
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pair [first=" + first + ", second=" + second + "]";
    }
}
