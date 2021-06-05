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
