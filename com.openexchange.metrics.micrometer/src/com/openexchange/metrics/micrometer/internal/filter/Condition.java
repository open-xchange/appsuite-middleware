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

package com.openexchange.metrics.micrometer.internal.filter;

/**
 * {@link Condition}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
class Condition {

    private final String value;
    private final boolean isRegex;
    private final boolean negated;

    /**
     * Initializes a new {@link Condition}.
     *
     * @param value The condition value
     * @param isRegex whether the condition is a regex
     * @param negated whether the condition should be negated
     */
    public Condition(String value, boolean isRegex, boolean negated) {
        super();
        this.value = value;
        this.isRegex = isRegex;
        this.negated = negated;
    }

    /**
     * Gets the condition value
     *
     * @return The condition value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns whether the condition contains a regex or not
     *
     * @return <code>true</code> if the filter is a regex; <code>false</code> otherwise
     */
    public boolean isRegex() {
        return isRegex;
    }

    /**
     * Whether the condition should be negated or not
     *
     * @return <code>true</code> if the condition should be negated, <code>false</code> otherwise
     */
    public boolean isNegated() {
        return negated;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Condition [value=").append(value).append(", isRegex=").append(isRegex).append(", negated=").append(negated).append("]");
        return builder.toString();
    }
}
