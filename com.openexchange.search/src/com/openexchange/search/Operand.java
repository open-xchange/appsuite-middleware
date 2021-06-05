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

package com.openexchange.search;

/**
 * {@link Operand} - Represents a operand within a search term.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Operand<V> {

    /**
     * The operand type.
     */
    public static enum Type {
        /**
         * A constant operand.
         */
        CONSTANT("constant"),
        /**
         * A column/field within a data object.
         */
        COLUMN("column"),
        /**
         * A attachment operand.
         */
        ATTACHMENT("attachment"),
        /**
         * A header within a data object.
         */
        HEADER("header"),
        ;

        private final String str;

        private Type(final String str) {
            this.str = str;
        }

        /**
         * Gets this type's string representation.
         *
         * @return The type's string representation.
         */
        public String getType() {
            return str;
        }

        /**
         * Checks if specified string equals this type's string representation.
         *
         * @param other The other string to check
         * @return <code>true</code> if specified string equals this type's string representation; otherwise <code>false</code>.
         */
        public boolean isType(final String other) {
            return str.equalsIgnoreCase(other);
        }
    }

    /**
     * Gets this operand's type.
     *
     * @return The operand's type.
     */
    public Type getType();

    /**
     * Gets this operand's value.
     *
     * @return The operand's value.
     */
    public V getValue();
}
