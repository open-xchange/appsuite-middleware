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

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;

/**
 * {@link DefaultCalendarObjectResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class DefaultCalendarObjectResource implements CalendarObjectResource {

    protected final List<Event> events;

    /**
     * Initializes a new {@link DefaultCalendarObjectResource} for a single event.
     * 
     * @param event The event of the calendar object resource
     */
    public DefaultCalendarObjectResource(Event event) {
        this(Collections.singletonList(event));
    }

    /**
     * Initializes a new {@link DefaultCalendarObjectResource} from one specific and further events.
     * 
     * @param event One event of the calendar object resource
     * @param events Further events of the calendar object resource
     * 
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public DefaultCalendarObjectResource(Event event, List<Event> events) {
        this(merge(event, events));
    }

    /**
     * Initializes a new {@link DefaultCalendarObjectResource}.
     * 
     * @param events The events of the calendar object resource
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public DefaultCalendarObjectResource(List<Event> events) {
        super();
        this.events = sortSeriesMasterFirst(new ArrayList<Event>(checkObjectResource(events)));
    }

    private static List<Event> merge(Event event, List<Event> events) {
        List<Event> mergedEvents = new ArrayList<Event>();
        if (null != event) {
            mergedEvents.add(event);
        }
        if (null != events) {
            mergedEvents.addAll(events);
        }
        return mergedEvents;
    }

    private static List<Event> checkObjectResource(List<Event> events) throws IllegalArgumentException {
        if (null == events || events.isEmpty()) {
            throw new IllegalArgumentException("No events in calendar object resource");
        }
        String uid = events.get(0).getUid();
        Organizer organizer = events.get(0).getOrganizer();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (false == matches(organizer, event.getOrganizer())) {
                throw new IllegalArgumentException("Different organizer in calendar object resource.");
            }
            if (false == Objects.equals(uid, event.getUid())) {
                throw new IllegalArgumentException("Different UID in calendar object resource.");
            }
        }
        return events;
    }

    @Override
    public String getUid() {
        return events.get(0).getUid();
    }

    @Override
    public Organizer getOrganizer() {
        return events.get(0).getOrganizer();
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }

    @Override
    public Event getSeriesMaster() {
        Event firstEvent = events.get(0);
        return isSeriesMaster(firstEvent) ? firstEvent : null;
    }

    @Override
    public List<Event> getChangeExceptions() {
        List<Event> changeExceptions = new ArrayList<Event>(events.size());
        for (Event event : events) {
            if (isSeriesException(event)) {
                changeExceptions.add(event);
            }
        }
        return changeExceptions;
    }

    @Override
    public Event getFirstEvent() {
        return events.get(0);
    }

    @Override
    public Event getChangeException(RecurrenceId recurrenceId) {
        for (Event event : events) {
            if (matches(recurrenceId, event.getRecurrenceId())) {
                return event;
            }
        }
        return null;
    }

    @Override
    public Date getTimestamp() {
        return getMaximumTimestamp(events);
    }

    @Override
    public String toString() {
        return "DefaultCalendarObjectResource [uid=" + getUid() + ", events=" + events + "]";
    }

}
