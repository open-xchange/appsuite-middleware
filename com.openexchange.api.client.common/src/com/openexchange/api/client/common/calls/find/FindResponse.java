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

package com.openexchange.api.client.common.calls.find;

import java.util.List;

/**
 * {@link FindResponse}
 *
 * @param <O> The type of the result objects
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FindResponse<O> {

    private final int found;
    private final int start;
    private final int size;
    private final List<O> resultObjects;

    /**
     * Initializes a new {@link FindResponse}.
     *
     * @param found The number of found items
     * @param start The start of the pagination
     * @param size The page size
     * @param resultObjects The result objects
     */
    public FindResponse(int found, int start, int size, List<O> resultObjects) {
        this.found = found;
        this.start = start;
        this.size = size;
        this.resultObjects = resultObjects;
    }

    /**
     * Gets the number of found items
     *
     * @return The number of found items
     */
    public int getFound() {
        return found;
    }

    /**
     * Gets the start of the pagination
     *
     * @return The start of the pagination
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the size of the page
     *
     * @return The size of the page
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the result objects
     *
     * @return The list of result objects
     */
    public List<O> getResultObjects() {
        return resultObjects;
    }
}
