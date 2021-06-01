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

package com.openexchange.groupware.search;

import java.util.Date;
import java.util.Set;

public class TaskSearchObject extends CalendarSearchObject {

    /**
     * No date range.
     */
    public static final Date[] NO_RANGE = null;

    /**
     * No priority search.
     */
    public static final int NO_PRIORITY = -1;

    /**
     * No status search.
     */
    public static final int NO_STATUS = -1;

    private int status = NO_STATUS;

    private Set<Integer> stateFilters;

    private boolean seriesFilter;

    private boolean singleOccurrenceFilter;

    private Set<String> externalParticipants;

    private int start;

    private int size;

    /**
     * This array contains 2 values between them the task ends. If the task has
     * no end date it won't appear if the range is defined.
     */
    private Date[] range = NO_RANGE;

    public TaskSearchObject() {
        super();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public Date[] getRange() {
        return range;
    }

    public void setRange(final Date[] range) {
        this.range = range;
    }

    /**
     * Gets the stateFilters
     *
     * @return The stateFilters
     */
    public Set<Integer> getStateFilters() {
        return stateFilters;
    }

    /**
     * Sets the stateFilters
     *
     * @param stateFilters The stateFilters to set
     */
    public void setStateFilters(Set<Integer> stateFilters) {
        this.stateFilters = stateFilters;
    }

    /**
     * Gets the seriesFilter
     *
     * @return The seriesFilter
     */
    public boolean isSeriesFilter() {
        return seriesFilter;
    }

    /**
     * Sets the seriesFilter
     *
     * @param seriesFilter The seriesFilter to set
     */
    public void setSeriesFilter(boolean seriesFilter) {
        this.seriesFilter = seriesFilter;
    }

    /**
     * Gets the singleFilter
     *
     * @return The singleFilter
     */
    public boolean isSingleOccurenceFilter() {
        return singleOccurrenceFilter;
    }

    /**
     * Sets the singleFilter
     *
     * @param singleFilter The singleFilter to set
     */
    public void setSingleOccurrenceFilter(boolean singleFilter) {
        this.singleOccurrenceFilter = singleFilter;
    }

    /**
     * Gets the externalParticipants
     *
     * @return The externalParticipants
     */
    public Set<String> getExternalParticipants() {
        return externalParticipants;
    }

    /**
     * Sets the externalParticipants
     *
     * @param externalParticipants The externalParticipants to set
     */
    public void setExternalParticipants(Set<String> externalParticipants) {
        this.externalParticipants = externalParticipants;
    }

    /**
     * Gets the start
     *
     * @return The start
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start
     *
     * @param start The start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the size
     *
     * @return The size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size
     *
     * @param size The size to set
     */
    public void setSize(int size) {
        this.size = size;
    }
}
