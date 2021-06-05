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

package com.openexchange.html;

/**
 * A range inside HTML content.
 * <p>
 * The range begins at the specified <i>start</i> and extends to the character at index <i>end - 1</i>. Thus the length of the substring is
 * <i>end-start</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Range {

    /**
     * The start position.
     */
    public final int start;

    /**
     * The end position.
     */
    public final int end;

    /**
     * Initializes a new {@link Range}.
     *
     * @param start The start position
     * @param end The end position
     */
    public Range(final int start, final int end) {
        super();
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return new StringBuilder(16).append("start=").append(start).append(" end=").append(end).toString();
    }

}
