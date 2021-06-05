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

package com.openexchange.dav.caldav.reports;

import java.util.List;

/**
 * {@link CompFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class CompFilter {

    private String name;
    private List<CompFilter> subFilters;
    private TimeRangeFilter timeRangeFilter;

    public CompFilter(String name, List<CompFilter> subFilters) {
        super();
        this.setSubFilters(subFilters);
        this.setName(name);
    }

    public CompFilter(String name, List<CompFilter> subFilters, TimeRangeFilter timeRangeFilter) {
        super();
        this.setSubFilters(subFilters);
        this.setName(name);
        this.setTimeRangeFilter(timeRangeFilter);
    }

    /**
     * Gets the subFilters
     *
     * @return The subFilters
     */
    public List<CompFilter> getSubFilters() {
        return subFilters;
    }

    /**
     * Sets the subFilters
     *
     * @param subFilters The subFilters to set
     */
    public void setSubFilters(List<CompFilter> subFilters) {
        this.subFilters = subFilters;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the timeRangeFilter
     *
     * @return The timeRangeFilter
     */
    public TimeRangeFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }

    /**
     * Sets the timeRangeFilter
     *
     * @param timeRangeFilter The timeRangeFilter to set
     */
    public void setTimeRangeFilter(TimeRangeFilter timeRangeFilter) {
        this.timeRangeFilter = timeRangeFilter;
    }

}
