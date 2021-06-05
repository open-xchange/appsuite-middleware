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
 * {@link ImmutablePair} - An immutable pair.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @param <T> The key of the pair
 * @param <V> The value of the pair
 * @since v7.10.0
 */
public final class ImmutablePair<T, V> {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static <T, V> Builder<T, V> builder() {
        return new Builder<>();
    }

    /**
     * A builder for an <code>ImmutablePair</code>
     *
     * @param <T> The key of the pair
     * @param <V> The value of the pair
     */
    public static final class Builder<T, V> {

        private T first;
        private V second;

        Builder() {
            super();
        }

        /**
         * Sets the first element.
         *
         * @param first The first element to set
         * @return This builder
         */
        public Builder<T, V> first(T first) {
            this.first = first;
            return this;
        }

        /**
         * Sets the second element.
         *
         * @param second The second element to set
         * @return This builder
         */
        public Builder<T, V> second(V second) {
            this.second = second;
            return this;
        }

        /**
         * Spawns a <code>ImmutablePair</code> instance from this builder's arguments.
         *
         * @return The resulting <code>ImmutablePair</code> instance
         */
        public ImmutablePair<T, V> build() {
            return new ImmutablePair<T, V>(first, second);
        }
    }

    /**
     * Creates a new <code>ImmutablePair</code> instance from given arguments.
     *
     * @param <T> The type of the first argument
     * @param <V> The type of the second argument
     * @param first The first argument
     * @param second The second argument
     * @return The resulting <code>ImmutablePair</code> instance
     */
    public static <T, V> ImmutablePair<T, V> newInstance(T first, V second) {
        return new ImmutablePair<T, V>(first, second);
    }

    // -------------------------------------------------------------------------------

    private final T first;
    private final V second;
    private final int hash;

    /**
     * Initializes a new {@link ImmutablePair}.
     */
    ImmutablePair(T first, V second) {
        super();
        this.first = first;
        this.second = second;
        int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        this.hash = result;
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
     * Gets the second value
     *
     * @return The 2nd value
     */
    public V getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImmutablePair<?, ?> other = (ImmutablePair<?, ?>) obj;
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

}
