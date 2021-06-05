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

package com.openexchange.data.conversion.ical;


/**
 * {@link TruncationInfo} - Provides possible truncation information about parsed objects.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TruncationInfo {

    private final int limit;
    private final int total;

    /**
     * Initializes a new {@link TruncationInfo}.
     *
     * @param limit The limit, which is the number of objects that were parsed due to configured constraint
     * @param total The total number of objects that were supposed to be parsed
     */
    public TruncationInfo(int limit, int total) {
        super();
        this.limit = limit;
        this.total = total;
    }

    /**
     * Checks if number of parsed objects were truncated.
     *
     * @return <code>true</code> if truncated; otherwise <code>false</code>
     */
    public boolean isTruncated() {
        return limit < total;
    }

    /**
     * Gets the limit, which is the number of objects that were parsed due to configured constraint.
     *
     * @return The limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the total number of objects that were supposed to be parsed.
     *
     * @return The total number of objects
     */
    public int getTotal() {
        return total;
    }

}
