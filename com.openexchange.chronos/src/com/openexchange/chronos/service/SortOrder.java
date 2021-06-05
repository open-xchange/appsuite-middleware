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

package com.openexchange.chronos.service;

import com.openexchange.chronos.EventField;
import com.openexchange.java.Strings;

/**
 * {@link SortOrder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SortOrder {

    /**
     * {@link Order} describes the order of a sort operation
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.0
     */
    public static enum Order {
        ASC,
        DESC;

        /**
         * Parses a string to a {@link Order}
         *
         * @param str The string to parse
         * @param defaultOrder The fallback {@link Order}
         * @return the parsed {@link Order} or the defaultOrder
         */
        public static Order parse(String str, Order defaultOrder) {
            if (Strings.isEmpty(str)) {
                return defaultOrder;
            }
            try {
                return Order.valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return defaultOrder;
            }
        }
    }

    /**
     * Initializes a new {@link SortOrder} for a specific event field with the given order.
     *
     * @param by The event field to use for ordering
     * @param order The order to use for ordering
     * @return The sort order
     */
    public static SortOrder getSortOrder(EventField by, Order order) {
        return new SortOrder(by, order);
    }

    private final EventField by;
    private final boolean descending;

    /**
     * Initializes a new {@link SortOrder}.
     *
     * @param by The event field to use for ordering
     * @param descending <code>true</code> if descending, <code>false</code>, otherwise
     */
    private SortOrder(EventField by, Order order) {
        super();
        this.by = by;
        this.descending = Order.DESC.equals(order);
    }

    /**
     * Gets the event field to use for ordering.
     *
     * @return The event field to use for ordering
     */
    public EventField getBy() {
        return by;
    }

    /**
     * Gets a value indicating whether a descending or ascending direction is defined.
     *
     * @return <code>true</code> if descending, <code>false</code> if ascending
     */
    public boolean isDescending() {
        return descending;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((by == null) ? 0 : by.hashCode());
        result = prime * result + (descending ? 1231 : 1237);
        return result;
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
        SortOrder other = (SortOrder) obj;
        if (by != other.by) {
            return false;
        }
        if (descending != other.descending) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return by + (descending ? " DESC" : " ASC");
    }

}
