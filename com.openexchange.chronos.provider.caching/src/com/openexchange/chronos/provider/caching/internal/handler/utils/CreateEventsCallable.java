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

package com.openexchange.chronos.provider.caching.internal.handler.utils;

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link CreateEventsCallable}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CreateEventsCallable implements Callable<Void> {

    private final List<Event> newEvents;
    private final CachingCalendarAccess cachedCalendarAccess;

    public CreateEventsCallable(CachingCalendarAccess cachingCalendarAccess, List<Event> lNewEvents) {
        this.cachedCalendarAccess = cachingCalendarAccess;
        this.newEvents = lNewEvents;
    }

    @Override
    public Void call() throws Exception {
        if (!newEvents.isEmpty()) {
            createEvents(newEvents);
        }
        return null;
    }

    protected void createEvents(List<Event> events) throws OXException {
        Map<String, List<Event>> extEventsByUID = getEventsByUID(events, false);
        for (Entry<String, List<Event>> event : extEventsByUID.entrySet()) {
            create(event);
        }
    }

    protected void create(Entry<String, List<Event>> entry) throws OXException {
        Date now = new Date();

        List<Event> events = sortSeriesMasterFirst(entry.getValue());
        insertEvents(now, events.toArray(new Event[events.size()]));
    }

    protected void insertEvents(Date now, Event... lEvents) throws OXException {
        if (null == lEvents || 0 == lEvents.length) {
            return;
        }
        List<Event> events = Arrays.asList(lEvents);
        CalendarStorage calendarStorage = this.cachedCalendarAccess.getCalendarStorage();
        /*
         * create first event (master or non-recurring)
         */
        String id = calendarStorage.getEventStorage().nextId();
        Event importedEvent = applyDefaults(events.get(0), now);
        importedEvent.setId(id);
        importedEvent.setCalendarUser(this.cachedCalendarAccess.getAccount().getUserId());
        if (Strings.isNotEmpty(importedEvent.getRecurrenceRule())) {
            importedEvent.setSeriesId(id);
        }
        calendarStorage.getEventStorage().insertEvent(importedEvent);
        if (null != importedEvent.getAttendees() && !importedEvent.getAttendees().isEmpty()) {
            calendarStorage.getAttendeeStorage().insertAttendees(id, importedEvent.getAttendees());
        }

        if (null != importedEvent.getAlarms() && !importedEvent.getAlarms().isEmpty()) {
            calendarStorage.getAlarmStorage().insertAlarms(importedEvent, this.cachedCalendarAccess.getSession().getUserId(), importedEvent.getAlarms());
        }
        /*
         * create further events as change exceptions
         */
        if (1 < events.size()) {
            SortedSet<RecurrenceId> changeExceptionDates = new TreeSet<RecurrenceId>();
            for (int i = 1; i < events.size(); i++) {
                Event importedChangeException = applyDefaults(events.get(i), now);
                importedChangeException.setSeriesId(id);
                importedChangeException.setId(calendarStorage.getEventStorage().nextId());
                calendarStorage.getEventStorage().insertEvent(importedChangeException);
                if (null != importedChangeException.getAttendees() && !importedChangeException.getAttendees().isEmpty()) {
                    calendarStorage.getAttendeeStorage().insertAttendees(importedChangeException.getId(), importedChangeException.getAttendees());
                }
                if (null != importedChangeException.getAlarms() && !importedChangeException.getAlarms().isEmpty()) {
                    calendarStorage.getAlarmStorage().insertAlarms(importedChangeException, this.cachedCalendarAccess.getSession().getUserId(), importedChangeException.getAlarms());
                }
                changeExceptionDates.add(importedChangeException.getRecurrenceId());
            }
            Event eventUpdate = new Event();
            eventUpdate.setId(id);
            eventUpdate.setChangeExceptionDates(changeExceptionDates);
            calendarStorage.getEventStorage().updateEvent(eventUpdate);
        }
    }

    private Event applyDefaults(Event importedEvent, Date now) {
        importedEvent.setCreatedBy(this.cachedCalendarAccess.getSession().getUserId());
        importedEvent.setModifiedBy(this.cachedCalendarAccess.getSession().getUserId());
        importedEvent.setTimestamp(now.getTime());
        return importedEvent;
    }
}
