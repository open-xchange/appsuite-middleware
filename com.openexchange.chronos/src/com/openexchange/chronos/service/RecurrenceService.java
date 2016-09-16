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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.service;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Event;

/**
 * {@link RecurrenceService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public interface RecurrenceService {

    /**
     * Calculates the expanded instances of a recurring event with optional boundaries and an optional limit.
     * If no limit is given an internal limit kicks in avoiding an endless calculation.
     * If no boundaries are given the calculation starts with the first occurrence and lasts until the end of the series.
     * 
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @return
     */
    public Iterator<Event> calculateInstances(Event master, Calendar start, Calendar end, Integer limit);
    
    /**
     * Calculates the expanded instances of a recurring event with optional boundaries and an optional limit.
     * If no limit is given an internal limit kicks in avoiding an endless calculation.
     * If no boundaries are given the calculation starts with the first occurrence and lasts until the end of the series.
     * 
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @param changeExceptions List of changeExceptions. Make sure this matches the change exception dates of the master, otherwise you might get weird results. Optional, can be null;
     * @return
     */
    public Iterator<Event> calculateInstancesRespectExceptions(Event master, Calendar start, Calendar end, Integer limit, List<Event> changeExceptions);

    /**
     * Calculates a reccurrence date position for a given 1-based position of a recurring event.
     *
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param position The 1-based position.
     * @return The date position of a given 1-based position. Null if the position is out of boundaries.
     */
    public Calendar calculateRecurrenceDatePosition(Event master, int position);

    /**
     * Calculates a 1-based recurrence position for a given reccurence date position of a recurring event.
     * 
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param datePosition The date position. Must match a start date.
     * @return The Position of the given datePosition. 1-based. 0 if not found or out of boundaries.
     */
    public int calculateRecurrencePosition(Event master, Calendar datePosition);

}
