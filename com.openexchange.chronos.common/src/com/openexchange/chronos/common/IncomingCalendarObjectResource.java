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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.chronos.common;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.java.Strings;

/**
 * 
 * {@link IncomingCalendarObjectResource} - Resource that orders event based on recurrence IDs or rules
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class IncomingCalendarObjectResource extends DefaultCalendarObjectResource {

    /**
     * Initializes a new {@link IncomingCalendarObjectResource} for a single event.
     * 
     * @param event The event of the calendar object resource
     */
    public IncomingCalendarObjectResource(Event event) {
        super(event);
    }

    /**
     * Initializes a new {@link IncomingCalendarObjectResource} from one specific and further events.
     * 
     * @param event One event of the calendar object resource
     * @param events Further events of the calendar object resource
     * 
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public IncomingCalendarObjectResource(Event event, List<Event> events) {
        super(event, events);
    }

    /**
     * Initializes a new {@link IncomingCalendarObjectResource}.
     * 
     * @param events The events of the calendar object resource
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public IncomingCalendarObjectResource(List<Event> events) {
        super(events);
    }

    @Override
    public Event getSeriesMaster() {
        Event firstEvent = events.get(0);
        if (Strings.isNotEmpty(firstEvent.getRecurrenceRule()) || 
            null != firstEvent.getRecurrenceDates() && false == firstEvent.getRecurrenceDates().isEmpty()) {
            return firstEvent;
        }
        return null;
    }

    @Override
    public List<Event> getChangeExceptions() {
        List<Event> changeExceptions = new ArrayList<Event>(events.size());
        for (Event event : events) {
            if (null != event.getRecurrenceId()) {
                changeExceptions.add(event);
            }
        }
        return changeExceptions;
    }

}
