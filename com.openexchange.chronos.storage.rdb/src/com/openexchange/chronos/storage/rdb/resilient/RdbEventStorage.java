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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbEventStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbEventStorage extends RdbResilientStorage implements EventStorage {

    private final EventStorage delegate;

    /**
     * Initializes a new {@link RdbEventStorage}.
     *
     * @param services A service lookup reference
     * @param delegate The delegate storage
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     * @param unsupportedDataThreshold The threshold defining up to which severity unsupported data errors can be ignored, or
     *            <code>null</code> to not ignore any unsupported data error at all
     */
    public RdbEventStorage(ServiceLookup services, EventStorage delegate, boolean handleTruncations, boolean handleIncorrectStrings, ProblemSeverity unsupportedDataThreshold) {
        super(services, handleTruncations, handleIncorrectStrings);
        this.delegate = delegate;
        setUnsupportedDataThreshold(unsupportedDataThreshold, delegate);
    }

    @Override
    public String nextId() throws OXException {
        return delegate.nextId();
    }

    @Override
    public long countEvents() throws OXException {
        return delegate.countEvents();
    }

    @Override
    public long countEvents(SearchTerm<?> searchTerm) throws OXException {
        return delegate.countEvents(searchTerm);
    }

    @Override
    public long countEventTombstones(SearchTerm<?> searchTerm) throws OXException {
        return delegate.countEventTombstones(searchTerm);
    }

    @Override
    public Event loadEvent(String eventId, EventField[] fields) throws OXException {
        return delegate.loadEvent(eventId, fields);
    }

    @Override
    public List<Event> loadEvents(List<String> eventIds, EventField[] fields) throws OXException {
        return delegate.loadEvents(eventIds, fields);
    }

    @Override
    public Event loadException(String seriesId, RecurrenceId recurrenceId, EventField[] fields) throws OXException {
        return delegate.loadException(seriesId, recurrenceId, fields);
    }

    @Override
    public List<Event> loadExceptions(String seriesId, EventField[] fields) throws OXException {
        return delegate.loadExceptions(seriesId, fields);
    }

    @Override
    public List<Event> searchEvents(SearchTerm<?> searchTerm, SearchOptions searchOptions, EventField[] fields) throws OXException {
        return delegate.searchEvents(searchTerm, searchOptions, fields);
    }

    @Override
    public List<Event> searchEvents(SearchTerm<?> searchTerm, List<SearchFilter> filters, SearchOptions searchOptions, EventField[] fields) throws OXException {
        return delegate.searchEvents(searchTerm, filters, searchOptions, fields);
    }

    @Override
    public List<Event> searchEventTombstones(SearchTerm<?> searchTerm, SearchOptions searchOptions, EventField[] fields) throws OXException {
        return delegate.searchEventTombstones(searchTerm, searchOptions, fields);
    }

    @Override
    public List<Event> searchOverlappingEvents(List<Attendee> attendees, boolean includeTransparent, SearchOptions searchOptions, EventField[] fields) throws OXException {
        return delegate.searchOverlappingEvents(attendees, includeTransparent, searchOptions, fields);
    }

    @Override
    public void insertEvent(Event event) throws OXException {
        runWithRetries(() -> delegate.insertEvent(event), f -> handle(event, f));
    }

    @Override
    public void insertEvents(List<Event> events) throws OXException {
        runWithRetries(() -> delegate.insertEvents(events), f -> handle(events, f));
    }

    @Override
    public void updateEvent(Event event) throws OXException {
        runWithRetries(() -> delegate.updateEvent(event), f -> handle(event, f));
    }

    @Override
    public void deleteEvent(String eventId) throws OXException {
        delegate.deleteEvent(eventId);
    }

    @Override
    public void deleteEvents(List<String> eventIds) throws OXException {
        delegate.deleteEvents(eventIds);
    }

    @Override
    public boolean deleteAllEvents() throws OXException {
        return delegate.deleteAllEvents();
    }

    @Override
    public void insertEventTombstone(Event event) throws OXException {
        runWithRetries(() -> delegate.insertEventTombstone(event), f -> handle(event, f));
    }

    @Override
    public void insertEventTombstones(List<Event> events) throws OXException {
        runWithRetries(() -> delegate.insertEventTombstones(events), f -> handle(events, f));
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param events The events being stored
     * @param failure The exception
     * @return <code>true</code> if the event data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(List<Event> events, Throwable failure) {
        for (Event event : events) {
            if (handle(event, failure)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param event The event being stored
     * @param failure The exception
     * @return <code>true</code> if the event data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(Event event, Throwable failure) {
        return handle(event.getId(), event, failure);
    }

}
