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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.CalendarUtilities;
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
        return OXException.class.isInstance(failure) ? handle(event, (OXException) failure) : false;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param event The event being stored
     * @param e The exception
     * @return <code>true</code> if the event data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(Event event, OXException e) {
        try {
            switch (e.getErrorCode()) {
                case "CAL-5071": // Incorrect string [string %1$s, field %2$s, column %3$s]
                    return handleIncorrectStrings && handleIncorrectString(e, event);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return handleTruncations && handleTruncation(e, event);
                default:
                    return false;
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
            return false;
        }
    }

    private boolean handleIncorrectString(OXException e, Event event) {
        LOG.debug("Incorrect string detected while storing calendar data, replacing problematic characters and trying again.", e);
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities && calendarUtilities.handleIncorrectString(e, event)) {
            addWarning(event.getId(), e);
            return true;
        }
        return false;
    }

    private boolean handleTruncation(OXException e, Event event) {
        LOG.debug("Data truncation detected while storing calendar data, trimming problematic fields and trying again.");
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities && calendarUtilities.handleDataTruncation(e, event)) {
            addWarning(event.getId(), e);
            return true;
        }
        return false;
    }

}
