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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getExceptionDates;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.applyExceptionDates;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.getFrom;
import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.getUntil;
import static com.openexchange.chronos.impl.Utils.isExcluded;
import static com.openexchange.chronos.impl.Utils.isResolveOccurrences;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.RecurrenceIdComparator;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AbstractQueryPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractQueryPerformer {

    protected final CalendarSession session;
    protected final CalendarStorage storage;

    /**
     * Initializes a new {@link AbstractQueryPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    protected AbstractQueryPerformer(CalendarSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
    }

    protected List<Event> readEventsInFolder(UserizedFolder folder, String[] objectIDs, boolean tombstones, Long updatedSince) throws OXException {
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getFolderIdTerm(folder));
        if (null != objectIDs) {
            if (0 == objectIDs.length) {
                return Collections.emptyList();
            } else if (1 == objectIDs.length) {
                searchTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectIDs[0]));
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String objectID : objectIDs) {
                    orTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectID));
                }
                searchTerm.addSearchTerm(orTerm);
            }
        }
        if (null != updatedSince) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, updatedSince));
        }
        /*
         * perform search & userize the results
         */
        EventField[] fields = getFields(session);
        if (tombstones) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, new SearchOptions(session), fields);
            return storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
        } else {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            return storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
        }
    }

    protected Iterator<Event> resolveOccurrences(Event masterEvent, Date from, Date until) throws OXException {
        final TreeSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>(RecurrenceIdComparator.DEFAULT_COMPARATOR);
        recurrenceIds.addAll(getChangeExceptionDates(masterEvent.getSeriesId()));
        if (null != masterEvent.getDeleteExceptionDates()) {
            recurrenceIds.addAll(masterEvent.getDeleteExceptionDates());
        }
        Event adjustedSeriesMaster = new DelegatingEvent(masterEvent) {

            @Override
            public SortedSet<RecurrenceId> getDeleteExceptionDates() {
                return recurrenceIds;
            }

            @Override
            public boolean containsDeleteExceptionDates() {
                return true;
            }
        };
        //        return session.getRecurrenceService().iterateEventOccurrences(masterEvent, from, until);
        return session.getRecurrenceService().iterateEventOccurrences(adjustedSeriesMaster, from, until);
    }

    /**
     * Gets a recurrence iterator for the supplied series master event, iterating over the recurrence identifiers of the event.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()} and overridden instances (looked up dynamically in the
     * storage) are skipped implicitly, so that those recurrence identifiers won't be included in the resulting iterator.
     *
     * @param masterEvent The recurring event master
     * @param from The start of the iteration interval, or <code>null</code> to start with the first occurrence
     * @param until The end of the iteration interval, or <code>null</code> to iterate until the last occurrence
     * @return The recurrence iterator
     * @see #getChangeExceptionDates(String)
     */
    protected Iterator<RecurrenceId> getRecurrenceIterator(Event masterEvent, Date from, Date until) throws OXException {
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        if (null != masterEvent.getSeriesId()) {
            recurrenceIds.addAll(getChangeExceptionDates(masterEvent.getSeriesId()));
        }
        if (null != masterEvent.getDeleteExceptionDates()) {
            recurrenceIds.addAll(masterEvent.getDeleteExceptionDates());
        }
        RecurrenceData recurrenceData = new DefaultRecurrenceData(masterEvent.getRecurrenceRule(), masterEvent.getStartDate(), getExceptionDates(recurrenceIds));
        return session.getRecurrenceService().iterateRecurrenceIds(recurrenceData, from, until);
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding events that are excluded as per {@link Utils#isExcluded(Event, CalendarSession, boolean)}</li>
     * <li>applying the folder identifier from the passed folder</li>
     * <li>resolving occurrences of the series master event as per {@link Utils#isResolveOccurrences(com.openexchange.chronos.service.CalendarParameters)}</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the calendar user's actual attendance</li>
     * <li>sorting the resulting event list based on the requested sort options</li>
     * </ul>
     *
     * @param events The events to post-process
     * @param inFolder The parent folder representing the view on the event
     * @param includeClassified <code>true</code> to include <i>confidential</i> events in shared folders, <code>false</code>, otherwise
     * @return The processed events
     */
    protected List<Event> postProcess(List<Event> events, UserizedFolder inFolder, boolean includeClassified) throws OXException {
        List<Event> processedEvents = new ArrayList<Event>(events.size());
        for (Event event : events) {
            if (isExcluded(event, session, includeClassified)) {
                continue;
            }
            event.setFolderId(inFolder.getID());
            event = anonymizeIfNeeded(session, event);
            if (isSeriesMaster(event)) {
                if (isResolveOccurrences(session)) {
                    processedEvents.addAll(resolveOccurrences(event));
                } else {
                    processedEvents.add(applyExceptionDates(storage, event, getCalendarUserId(inFolder)));
                }
            } else {
                processedEvents.add(event);
            }
        }
        return sortEvents(processedEvents, new SearchOptions(session));
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding events that are excluded as per {@link Utils#isExcluded(Event, CalendarSession, boolean)}</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>resolving occurrences of the series master event as per {@link Utils#isResolveOccurrences(com.openexchange.chronos.service.CalendarParameters)}</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual attendance</li>
     * <li>sorting the resulting event list based on the requested sort options</li>
     * </ul>
     *
     * @param events The events to post-process
     * @param forUser The identifier of the user to apply the parent folder identifier for
     * @param includePrivate <code>true</code> to include private or confidential events in non-private folders, <code>false</code>, otherwise
     * @return The processed events
     */
    protected List<Event> postProcess(List<Event> events, int forUser, boolean includePrivate) throws OXException {
        List<Event> processedEvents = new ArrayList<Event>(events.size());
        for (Event event : events) {
            if (isExcluded(event, session, includePrivate)) {
                continue;
            }
            event.setFolderId(Utils.getFolderView(storage, event, forUser));
            event = anonymizeIfNeeded(session, event);
            if (isSeriesMaster(event)) {
                if (isResolveOccurrences(session)) {
                    processedEvents.addAll(resolveOccurrences(event));
                } else {
                    processedEvents.add(applyExceptionDates(storage, event, forUser));
                }
            } else {
                processedEvents.add(event);
            }
        }
        return sortEvents(processedEvents, new SearchOptions(session));
    }

    /**
     * Creates a <i>userized</i> version of an event, representing a specific user's point of view on the event data. This includes
     * <ul>
     * <li><i>anonymization</i> of restricted event data in case the event it is not marked as {@link Classification#PUBLIC}, and the
     * current session's user is neither creator, nor attendee of the event.</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance</li>
     * </ul>
     *
     * @param event The event to userize
     * @param forUser The identifier of the user in whose point of view the event should be adjusted
     * @return The <i>userized</i> event
     * @see Utils#applyExceptionDates
     * @see Utils#anonymizeIfNeeded
     */
    protected Event userize(Event event, int forUser) throws OXException {
        if (isSeriesMaster(event)) {
            event = applyExceptionDates(storage, event, forUser);
        }
        final String folderView = Utils.getFolderView(storage, event, forUser);
        if (false == folderView.equals(event.getFolderId())) {
            event = new DelegatingEvent(event) {

                @Override
                public String getFolderId() {
                    return folderView;
                }

                @Override
                public boolean containsFolderId() {
                    return true;
                }
            };
        }
        return anonymizeIfNeeded(session, event);
    }

    /**
     * Sorts a list of events.
     *
     * @param events The events to sort
     * @param sortOptions The sort options to use
     * @return The sorted events
     */
    protected List<Event> sortEvents(List<Event> events, SearchOptions sortOptions) throws OXException {
        if (null == events || 2 > events.size() || null == sortOptions || SearchOptions.EMPTY.equals(sortOptions) ||
            null == sortOptions.getSortOrders() || 0 == sortOptions.getSortOrders().length) {
            return events;
        }
        Collections.sort(events, session.getUtilities().getComparator(sortOptions.getSortOrders(), Utils.getTimeZone(session)));
        return events;
    }

    /**
     * Looks up the recurrence identifiers of all change exceptions for a specific event series from the storage.
     *
     * @param seriesId The identifier of the series to get the recurrence identifiers for
     * @return The recurrence identifiers in a sorted set, or an empty set if there are none
     */
    protected SortedSet<RecurrenceId> getChangeExceptionDates(String seriesId) throws OXException {
        EventField[] fields = new EventField[] { EventField.RECURRENCE_ID, EventField.ID, EventField.SERIES_ID };
        List<Event> changeExceptions = storage.getEventStorage().loadExceptions(seriesId, fields);
        return CalendarUtils.getRecurrenceIds(changeExceptions);
    }

    private List<Event> resolveOccurrences(Event master) throws OXException {
        return Utils.asList(resolveOccurrences(master, getFrom(session), getUntil(session)));
    }

}
