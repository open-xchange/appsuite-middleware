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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.isEnforceDefaultAttendee;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultUpdatesResult;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link UpdatesPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatesPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public UpdatesPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param timestamp The timestamp since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(long timestamp) throws OXException {
        /*
         * search for events the current session's user attends
         */
        SearchTerm<?> searchTerm = getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(session.getUserId()));
        if (false == isEnforceDefaultAttendee(session)) {
            /*
             * also include not group-scheduled events associated with the calendar user
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.CALENDAR_USER, SingleOperation.EQUALS, I(session.getUserId())))
                .addSearchTerm(searchTerm);
        }
        /*
         * ... modified after supplied date
         */
        searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, L(timestamp)))
            .addSearchTerm(searchTerm);
        /*
         * perform search & userize the results for the current session's user
         */
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session, EventField.ATTENDEES, EventField.ORGANIZER);
        List<Event> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventData(session.getUserId(), events, fields);
            newAndModifiedEvents = postProcessor().process(events, session.getUserId()).getEvents();
        }
        List<Event> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
            deletedEvents = postProcessor().processTombstones(events, session.getUserId()).getEvents();
        }
        return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the parent folder to get the event from
     * @param since The timestamp since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(String folderId, long since) throws OXException {
        CalendarFolder folder = getFolder(session, folderId);
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(session, folder))
            .addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, L(since))
        );
        /*
         * perform search & userize the results based on the requested folder
         */
        SearchOptions searchOptions = prepareSearchOptions(session);
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session, EventField.ATTENDEES, EventField.ORGANIZER);
        List<Event> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
            storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
            newAndModifiedEvents = postProcessor().process(events, folder).getEvents();
        }
        List<Event> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, searchOptions, fields);
            storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
            deletedEvents = postProcessor().processTombstones(events, folder).getEvents();
        }
        if (isNullOrEmpty(deletedEvents) && isNullOrEmpty(newAndModifiedEvents)) {
            /*
             * no changes, return empty result with client-supplied timestamp
             */
            return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents, since, false);
        }
        /*
         * limit returned results as requested
         */
        return getResult(newAndModifiedEvents, deletedEvents, 0 < searchOptions.getLimit() ? searchOptions.getLimit() - 1 : searchOptions.getLimit());
    }

    private static SearchOptions prepareSearchOptions(CalendarParameters parameters) {
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.addOrder(SortOrder.getSortOrder(EventField.TIMESTAMP, SortOrder.Order.ASC));
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        searchOptions.setRange(from, until);
        Integer leftHandLimit = parameters.get(CalendarParameters.PARAMETER_LEFT_HAND_LIMIT, Integer.class);
        Integer rightHandLimit = parameters.get(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, Integer.class);
        searchOptions.setLimits(null != leftHandLimit ? i(leftHandLimit) : 0, null != rightHandLimit ? i(rightHandLimit) + 1 : -1);
        return searchOptions;
    }

    protected static DefaultUpdatesResult getResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents, int limit) {
        if (0 >= limit || (isNullOrEmpty(deletedEvents) && isNullOrEmpty(newAndModifiedEvents))) {
            /*
             * not limited or no results, so no truncation necessary
             */
            return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents);
        }
        /*
         * truncate both results as needed
         */
        boolean newAndModifiedTruncated = truncateEvents(newAndModifiedEvents, limit);
        boolean deletedTruncated = truncateEvents(deletedEvents, limit);
        if (newAndModifiedTruncated) {
            /*
             * 'new and modified' list was truncated, ensure maximum timestamp of 'deleted' list is still in scope
             */
            long timestamp = newAndModifiedEvents.get(newAndModifiedEvents.size() - 1).getTimestamp();
            deletedEvents = removeWithGreaterTimestamp(deletedEvents, timestamp);
            return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents, timestamp, true);
        }
        if (deletedTruncated) {
            /*
             * 'deleted' list was truncated, ensure maximum timestamp of 'new and modified' list is still in scope
             */
            long timestamp = deletedEvents.get(deletedEvents.size() - 1).getTimestamp();
            newAndModifiedEvents = removeWithGreaterTimestamp(newAndModifiedEvents, timestamp);
            return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents, timestamp, true);
        }
        /*
         * both results within limit, so no truncation necessary
         */
        return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents);
    }

    protected static List<Event> removeWithGreaterTimestamp(List<Event> events, long maximumTimestamp) {
        if (isNullOrEmpty(events)) {
            return events;
        }
        for (ListIterator<Event> iterator = events.listIterator(events.size()); iterator.hasPrevious();) {
            if (maximumTimestamp < iterator.previous().getTimestamp()) {
                iterator.remove();
            }
        }
        return events;
    }

    protected static boolean truncateEvents(List<Event> events, int limit) {
        if (isNullOrEmpty(events) || 0 >= limit || events.size() < limit) {
            return false;
        }
        /*
         * resulting event list at limit or beyond, remove last events, and further events with equal timestamp at tail
         * (as there could be more events with this timestamp in storage)
         */
        long lastTimestamp = events.get(events.size() - 1).getTimestamp();
        for (ListIterator<Event> iterator = events.listIterator(events.size()); iterator.hasPrevious();) {
            long timestamp = iterator.previous().getTimestamp();
            if (iterator.nextIndex() >= limit || lastTimestamp == timestamp) {
                iterator.remove();
                lastTimestamp = timestamp;
            } else {
                break;
            }
        }
        return true;
    }

}
