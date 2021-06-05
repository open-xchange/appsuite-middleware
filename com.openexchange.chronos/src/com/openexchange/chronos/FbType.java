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

package com.openexchange.chronos;

import com.openexchange.java.EnumeratedProperty;

/**
 * {@link FbType}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.2.9">RFC 5545, section 3.2.9</a>
 */
public class FbType extends EnumeratedProperty {

    /**
     * Indicates that the time interval is free for scheduling.
     */
    public static final FbType FREE = new FbType("FREE", 0);

    /**
     * Indicates that the time interval is busy because one or more events have been scheduled for that interval.
     */
    public static final FbType BUSY = new FbType("BUSY", 3);

    /**
     * Indicates that the time interval is busy and that the interval can not be scheduled.
     */
    public static final FbType BUSY_UNAVAILABLE = new FbType("BUSY-UNAVAILABLE", 4);

    /**
     * indicates that the time interval is busy because one or more events have been tentatively scheduled for that interval.
     */
    public static final FbType BUSY_TENTATIVE = new FbType("BUSY-TENTATIVE", 2);

    private final int order;

    /**
     * Initializes a new {@link FbType}.
     *
     * @param value The property value
     */
    public FbType(String value) {
        this(value, 1);
    }

    /**
     * Initializes a new {@link FbType}.
     *
     * @param value The property value
     * @param order The order of the type for sorting
     */
    public FbType(String value, int order) {
        super(value);
        this.order = order;
    }

    @Override
    public String getDefaultValue() {
        return BUSY.getValue();
    }

    /**
     * Compares this free/busy type with another one for ordering.
     * <p/>
     * The following order is used: {@link FbType#BUSY_UNAVAILABLE} > {@link FbType#BUSY} > {@link FbType#BUSY_TENTATIVE} >
     * [any unknown value] > {@link FbType#FREE}.
     *
     * @param other The free/busy type to compare against
     * @return <code>0</code> if the supplied free/busy type is equal to this type, a value <code>&lt; 0</code> if this type is <i>less
     *         conflicting</i> than the passed free/busy type, or a value <code>&gt; 0</code> if this type is <i>more conflicting</i> than
     *         the passed instance.
     */
    public int compareTo(FbType other) {
        if (null == other) {
            return 1;
        }
        return Integer.compare(order, other.order);
    }

    @Override
    protected String[] getStandardValues() {
        return getValues(FREE, BUSY, BUSY_UNAVAILABLE, BUSY_TENTATIVE);
    }

}
