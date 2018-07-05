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

package com.openexchange.chronos.storage.rdb.replaying;

import java.util.List;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbEventStorage implements EventStorage {

    private final EventStorage delegate;
    private final EventStorage legacyDelegate;

    /**
     * Initializes a new {@link RdbEventStorage}.
     *
     * @param delegate The delegate storage
     * @param legacyDelegate The legacy delegate storage
     */
    public RdbEventStorage(EventStorage delegate, EventStorage legacyDelegate) {
        super();
        this.delegate = delegate;
        this.legacyDelegate = legacyDelegate;
    }

    @Override
    public String nextId() throws OXException {
        String nextLegacyId = legacyDelegate.nextId();
        String nextId = delegate.nextId();
        if (false == nextId.equals(nextLegacyId)) {
            LoggerFactory.getLogger(RdbStorage.class).warn(
                "Sequential identifiers in replaying storage have diverged: \"{}\" in default vs. \"{}\" in legacy storage.", nextId, nextLegacyId);
            try {
                int numericalLegacyId = Integer.parseInt(nextLegacyId);
                int numericalId = Integer.parseInt(nextId);
                while (numericalLegacyId < numericalId) {
                    nextLegacyId = legacyDelegate.nextId();
                    numericalLegacyId = Integer.parseInt(nextLegacyId);
                }
                while (numericalId < numericalLegacyId) {
                    nextId = delegate.nextId();
                    numericalId = Integer.parseInt(nextId);
                }
                LoggerFactory.getLogger(RdbStorage.class).info(
                    "Successfully synchronized sequential identifiers in replaying storage to \"{}\".", nextId);
            } catch (Exception e) {
                LoggerFactory.getLogger(RdbStorage.class).error("Unexpected error when synchronizing sequential identifiers in replaying storage", e);
            }
        }
        return nextId;
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
        delegate.insertEvent(event);
        legacyDelegate.insertEvent(event);
    }

    @Override
    public void insertEvents(List<Event> events) throws OXException {
        delegate.insertEvents(events);
        legacyDelegate.insertEvents(events);
    }

    @Override
    public void updateEvent(Event event) throws OXException {
        delegate.updateEvent(event);
        legacyDelegate.updateEvent(event);
    }

    @Override
    public void deleteEvent(String eventId) throws OXException {
        delegate.deleteEvent(eventId);
        legacyDelegate.deleteEvent(eventId);
    }

    @Override
    public void deleteEvents(List<String> eventIds) throws OXException {
        delegate.deleteEvents(eventIds);
        legacyDelegate.deleteEvents(eventIds);
    }

    @Override
    public boolean deleteAllEvents() throws OXException {
        return delegate.deleteAllEvents();
    }

    @Override
    public void insertEventTombstone(Event event) throws OXException {
        delegate.insertEventTombstone(event);
        legacyDelegate.insertEventTombstone(event);
    }

    @Override
    public void insertEventTombstones(List<Event> events) throws OXException {
        delegate.insertEventTombstones(events);
        legacyDelegate.insertEventTombstones(events);
    }

}
