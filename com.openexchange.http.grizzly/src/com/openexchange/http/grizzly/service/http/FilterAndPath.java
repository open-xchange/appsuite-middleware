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

package com.openexchange.http.grizzly.service.http;

import javax.servlet.Filter;

/**
 * {@link FilterAndPath}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class FilterAndPath {

    private final Filter filter;
    private final String path;
    private final int hash;

    /**
     * Initializes a new {@link FilterAndPath}.
     */
    public FilterAndPath(Filter filter, String path) {
        super();
        this.filter = filter;
        this.path = path;
        int prime = 31;
        int result = 1;
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        hash = result;
    }

    /**
     * Gets the filter
     *
     * @return The filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    public String getPath() {
        return path;
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
        if (!(obj instanceof FilterAndPath)) {
            return false;
        }
        FilterAndPath other = (FilterAndPath) obj;
        if (filter == null) {
            if (other.filter != null) {
                return false;
            }
        } else if (!filter.equals(other.filter)) {
            return false;
        }
        return true;
    }

}
