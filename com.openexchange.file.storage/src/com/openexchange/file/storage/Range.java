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

package com.openexchange.file.storage;


/**
 * {@link Range} - Represents a requested range providing a <i>"from"</i> (inclusive) and a <i>"to"</i> (exclusive) position.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Range {

    /**
     * Initializes a new {@link Range}.
     *
     * @param indexes The <i>"from"</i> (inclusive) and <i>"to"</i> (exclusive) positions
     * @return The range or <code>null</code>
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    public static Range valueOf(int[] indexes) {
        return null == indexes || 2 != indexes.length ? null : new Range(indexes[0], indexes[1]);
    }

    /**
     * Initializes a new {@link Range}.
     *
     * @param from The <i>"from"</i> position (inclusive)
     * @param to The <i>"to"</i> position (exclusive)
     * @return The range
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    public static Range valueOf(int from, int to) {
        return new Range(from, to);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /** The <i>"from"</i> position (inclusive) */
    public final int from;

    /** The <i>"to"</i> position (exclusive) */
    public final int to;

    /** The internal hash code */
    private final int hash;

    /**
     * Initializes a new {@link Range}.
     *
     * @param from The <i>"from"</i> position (inclusive)
     * @param to The <i>"to"</i> position (exclusive)
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    private Range(int from, int to) {
        super();
        if (from < 0 || to < 0 || to < from) {
            throw new IllegalArgumentException("Invalid from/to positions. From=" + from + ", To=" + to);
        }
        this.from = from;
        this.to = to;

        int prime = 31;
        int result = prime * 1 + from;
        result = prime * result + to;
        this.hash = result;
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
        if (!(obj instanceof Range)) {
            return false;
        }
        Range other = (Range) obj;
        if (from != other.from) {
            return false;
        }
        if (to != other.to) {
            return false;
        }
        return true;
    }

}
