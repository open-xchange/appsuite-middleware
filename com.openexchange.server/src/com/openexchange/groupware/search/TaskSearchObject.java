/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    private int priority = NO_PRIORITY;
    
    private Set<String> titleFilters;
    
    private Set<String> descriptionFilters;
    
    private Set<Integer> stateFilters;
    
    private Set<String> queries;

    /**
     * This array contains 2 values between them the task ends. If the task has
     * no end date it won't appear if the range is defined.
     */
    private Date[] range = NO_RANGE;

    public TaskSearchObject() {
        super();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
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
     * Set filters for the title field
     * @param tf title filters
     */
    public void setTitleFilters(Set<String> tf) {
        titleFilters = tf;
    }
    
    /**
     * Gets the title filters
     *
     * @return The title filters
     */
    public Set<String> getTitleFilters() {
        return titleFilters;
    }
    
    /**
     * Set the filters for the description field
     * @param df description filters
     */
    public void setDescriptionFilters(Set<String> df) {
        descriptionFilters = df;
    }
    
    /**
     * Gets the description filters
     *
     * @return The description filters
     */
    public Set<String> getDescriptionFilters() {
        return descriptionFilters;
    }

    /**
     * Gets the queries
     *
     * @return The queries
     */
    public Set<String> getQueries() {
        return queries;
    }

    /**
     * Sets the queries
     *
     * @param queries The queries to set
     */
    public void setQueries(Set<String> queries) {
        this.queries = queries;
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
}
