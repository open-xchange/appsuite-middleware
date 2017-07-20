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

package com.openexchange.chronos.provider.caching.internal.handler.impl;

import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import static com.openexchange.java.Autoboxing.L;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AbstractHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractHandler implements CachingHandler {

    protected static EventField[] FIELDS = new EventField[] { EventField.ID, EventField.UID, EventField.DESCRIPTION, EventField.SUMMARY, EventField.LOCATION, EventField.SEQUENCE, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.LAST_MODIFIED, EventField.CREATED_BY, EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.ATTENDEES };

    protected final CachingCalendarAccess cachedCalendarAccess;

    public AbstractHandler(CachingCalendarAccess cachedCalendarAccess) {
        super();
        this.cachedCalendarAccess = cachedCalendarAccess;
    }

    protected CalendarStorage getCalendarStorage() throws OXException {
        return this.cachedCalendarAccess.getCalendarStorage();
    }

    protected void updateLastModified() throws OXException {
        Map<String, Object> configuration = this.cachedCalendarAccess.getAccount().getConfiguration();
        configuration.put("lastUpdate", L(System.currentTimeMillis()));
        this.cachedCalendarAccess.saveConfig(configuration);
    }

    protected List<Event> searchEvents(List<EventID> eventIDs) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);

        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (EventID event : eventIDs) {
            CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
            andTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, event.getObjectID())).addSearchTerm(getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, event.getFolderID()));
            orTerm.addSearchTerm(andTerm);
        }
        searchTerm.addSearchTerm(orTerm);

        return getCalendarStorage().getEventStorage().searchEvents(searchTerm, new SearchOptions(this.cachedCalendarAccess.getParameters()), FIELDS);
    }

    protected List<Event> searchEvents(String folderId) throws OXException {
        SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
        return getCalendarStorage().getEventStorage().searchEvents(searchTerm, new SearchOptions(this.cachedCalendarAccess.getParameters()), FIELDS);
    }

    protected Event searchEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        //TODO requires search?
        return getCalendarStorage().getEventStorage().loadEvent(eventId, FIELDS);
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
        CalendarStorage calendarStorage = getCalendarStorage();
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
        if (null != importedEvent.getAttendees() && 0 < importedEvent.getAttendees().size()) {
            calendarStorage.getAttendeeStorage().insertAttendees(id, importedEvent.getAttendees());
        }
        if (this.cachedCalendarAccess.supportsAlarms() && null != importedEvent.getAlarms() && 0 < importedEvent.getAlarms().size()) {
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
                if (null != importedChangeException.getAttendees() && 0 < importedChangeException.getAttendees().size()) {
                    calendarStorage.getAttendeeStorage().insertAttendees(importedChangeException.getId(), importedChangeException.getAttendees());
                }
                if (this.cachedCalendarAccess.supportsAlarms() && null != importedChangeException.getAlarms() && 0 < importedChangeException.getAlarms().size()) {
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
        importedEvent.setCreated(now);
        importedEvent.setCreatedBy(this.cachedCalendarAccess.getSession().getUserId());
        importedEvent.setLastModified(now);
        importedEvent.setModifiedBy(this.cachedCalendarAccess.getSession().getUserId());
        return importedEvent;
    }

    protected List<Event> getAndPrepareExtEvents(String folderId) throws OXException {
        List<Event> extEventsInFolder = this.cachedCalendarAccess.getEventsInFolderExt(folderId);
        filterByRanges(extEventsInFolder);

        HandlerHelper.setFolderId(extEventsInFolder, folderId);
        return extEventsInFolder;
    }

    protected List<Event> getAndPrepareExtEvents(List<EventID> eventIDs) throws OXException {
        List<Event> externalEvents = cachedCalendarAccess.getEventsExt(eventIDs);
        filterByRanges(externalEvents);

        HandlerHelper.setFolderId(externalEvents, eventIDs);
        return externalEvents;
    }

    protected Date getFrom() {
        return this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    protected void filterByRanges(List<Event> events) {
        Iterator<Event> iterator = events.iterator();
        Date from = getFrom();
        Date until = getUntil();

        if ((from == null) && (until == null)) {
            return;
        }

        while (iterator.hasNext()) {
            Event event = iterator.next();

            if ((from != null && !(event.containsStartDate() && event.getStartDate().after(new DateTime(from.getTime())))) || (until != null && !(event.containsEndDate() && event.getEndDate().before(new DateTime(until.getTime()))))) {
                iterator.remove();
            }
        }
        //        List<Event> filteredEventsInFolder = new ArrayList<>();
        //
        //        Date from = getFrom();
        //        Date until = getUntil();
        //        for (Event event : events) {
        //            Event filterByRanges = filterByRanges(event, from, until);
        //            if (filterByRanges != null) {
        //                filteredEventsInFolder.add(event);
        //            }
        //        }
        //        return filteredEventsInFolder;
    }

    //    protected Event filterByRanges(Event event, Date from, Date until) {
    //        if (event.containsEndDate() && event.getEndDate().before(new DateTime(until.getTime())) && event.containsStartDate() && event.getStartDate().after(new DateTime(from.getTime()))) {
    //            return event;
    //        }
    //        return null;
    //    }

}
