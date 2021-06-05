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

package com.openexchange.messaging;

/**
 * {@link IndexRange} - A simple class representing an index range.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class IndexRange {

    /**
     * The <code>null</code> index range
     */
    public static final IndexRange NULL = null;

    /**
     * The start index
     */
    public final int start;

    /**
     * The end index
     */
    public final int end;

    /**
     * Initializes a new {@link IndexRange}
     */
    public IndexRange(final int start, final int end) {
        super();
        if (start < 0) {
            throw new IllegalArgumentException("start index is less than zero");
        } else if (end < 0) {
            throw new IllegalArgumentException("end index is less than zero");
        } else if (end < start) {
            throw new IllegalArgumentException("end index is less than start index");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexRange other = (IndexRange) obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    /**
     * Gets the start index
     *
     * @return The start index
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the end index
     *
     * @return The end index
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append(" start=").append(start).append(", end=").append(end).toString();
    }
}
