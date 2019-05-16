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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isFirstOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isLastOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.applyExceptionDates;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getPersonalFolderIds;
import static com.openexchange.chronos.impl.Utils.isInFolder;
import static com.openexchange.chronos.impl.Utils.isResolveOccurrences;
import static com.openexchange.chronos.impl.Utils.mapEventOccurrences;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.EventOccurrence;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.InternalCalendarStorageOperation;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link ResultTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ResultTracker {

    private final CalendarStorage storage;
    private final CalendarSession session;
    private final CalendarFolder folder;
    private final long timestamp;
    private final InternalCalendarResult result;
    private final SelfProtection protection;
    private final Map<CalendarFolder, Map<String, Event>> originalUserizedEvents;
    private final Map<String, RecurrenceData> knownRecurrenceData;

    /**
     * Initializes a new {@link ResultTracker}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param timestamp The timestamp to apply for the result
     */
    public ResultTracker(CalendarStorage storage, CalendarSession session, CalendarFolder folder, long timestamp, SelfProtection protection) {
        super();
        this.storage = storage;
        this.session = session;
        this.folder = folder;
        this.timestamp = timestamp;
        this.protection = protection;
        this.originalUserizedEvents = new HashMap<CalendarFolder, Map<String, Event>>();
        this.knownRecurrenceData = new HashMap<String, RecurrenceData>();
        this.result = new InternalCalendarResult(session, getCalendarUserId(folder), folder);
    }

    /**
     * Gets the calendar result.
     *
     * @return The calendar result
     */
    public InternalCalendarResult getResult() {
        return result;
    }

    /**
     * Remembers data of a an original event before it is updated to speed up the generation of <i>userized</i> event results afterwards.
     *
     * @param originalEvent The original event to remember
     */
    public void rememberOriginalEvent(Event originalEvent) throws OXException {
        if (includeAllFolderViews(session)) {
            for (CalendarFolder visibleFolder : getVisibleFolderViews(originalEvent)) {
                rememberOriginalUserizedEvent(visibleFolder, originalEvent);
            }
        } else {
            rememberOriginalUserizedEvent(folder, originalEvent);
        }
    }

    /**
     * Tracks suitable results for a created event in the current internal calendar result, which includes adding a <i>plain</i> creation
     * for the new event data, as well as an appropriate creation from the acting user's point of view.
     *
     * @param createdEvent The created event
     */
    public void trackCreation(Event createdEvent) throws OXException {
        trackCreation(createdEvent, null);
    }

    /**
     * Tracks suitable results for a created event in the current internal calendar result, which includes adding a <i>plain</i> creation
     * for the new event data, as well as an appropriate creation from the acting user's point of view.
     *
     * @param createdEvent The created event
     * @param originalSeriesMaster The series master event in case a new overridden instance was created, or <code>null</code>, otherwise
     */
    public void trackCreation(Event createdEvent, Event originalSeriesMaster) throws OXException {
        /*
         * track affected folders and add 'plain' event creation, as well as (possibly expanded) 'userized' versions
         */
        result.addAffectedFolderIds(folder.getId(), getPersonalFolderIds(createdEvent.getAttendees()));
        result.addPlainCreation(createdEvent);
        if (includeAllFolderViews(session)) {
            for (CalendarFolder visibleFolder : getVisibleFolderViews(createdEvent)) {
                trackUserizedCreation(visibleFolder, createdEvent, originalSeriesMaster);
            }
        } else {
            trackUserizedCreation(folder, createdEvent, originalSeriesMaster);
        }
    }

    /**
     * Tracks suitable results for a created event in the current internal calendar result, based on a specific folder representing the
     * targeted view for userization of the event data.
     *
     * @param folder The folder representing the targeted view for userization of the event data
     * @param createdEvent The created event
     * @param originalSeriesMaster The series master event in case a new overridden instance was created, or <code>null</code>, otherwise
     */
    private void trackUserizedCreation(CalendarFolder folder, Event createdEvent, Event originalSeriesMaster) throws OXException {
        if (isInFolder(createdEvent, folder)) {
            /*
             * prepare 'userized' version of created event & expand event series if needed prior adding results
             */
            if (isSeriesMaster(createdEvent) && isResolveOccurrences(session)) {
                result.addUserizedCreations(resolveOccurrences(userize(createdEvent, folder)));
            } else {
                result.addUserizedCreation(userize(createdEvent, folder));
            }
        } else {
            /*
             * possible for a new change exception w/o the calendar user attending
             */
            if (isSeriesMaster(originalSeriesMaster) && isSeriesException(createdEvent)) {
                Event originalUserizedMasterEvent = getOriginalUserizedEvent(originalSeriesMaster, folder);
                result.addUserizedDeletion(timestamp, new EventOccurrence(originalUserizedMasterEvent, createdEvent.getRecurrenceId()));
            }
        }
    }

    /**
     * Tracks suitable results for an updated event in the current internal calendar result, which includes adding a <i>plain</i> update
     * for the modified event data, as well as an update, deletion or creation from the acting user's point of view.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    public void trackUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * track affected folders and add a 'plain' event update, as well as (possibly expanded) 'userized' versions
         */
        result.addAffectedFolderIds(folder.getId(), getPersonalFolderIds(originalEvent.getAttendees()), getPersonalFolderIds(updatedEvent.getAttendees()));
        result.addPlainUpdate(originalEvent, updatedEvent);
        if (includeAllFolderViews(session)) {
            for (CalendarFolder visibleFolder : getVisibleFolderViews(originalEvent, updatedEvent)) {
                trackUserizedUpdate(visibleFolder, originalEvent, updatedEvent);
            }
        } else {
            trackUserizedUpdate(folder, originalEvent, updatedEvent);
        }
    }

    /**
     * Tracks suitable results for an updated event in the current internal calendar result, based on a specific folder representing the
     * targeted view for userization of the event data.
     *
     * @param folder The folder representing the targeted view for userization of the event data
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    private void trackUserizedUpdate(CalendarFolder folder, Event originalEvent, Event updatedEvent) throws OXException {
        if (isInFolder(originalEvent, folder)) {
            if (isInFolder(updatedEvent, folder)) {
                /*
                 * "update" from calendar user's point of view
                 */
                if (isResolveOccurrences(session) && (isSeriesMaster(originalEvent) || isSeriesMaster(updatedEvent))) {
                    if (isSeriesMaster(originalEvent) && isSeriesMaster(updatedEvent)) {
                        for (Entry<Event, Event> entry : mapEventOccurrences(resolveOccurrences(getOriginalUserizedEvent(originalEvent, folder)), resolveOccurrences(userize(updatedEvent, folder)))) {
                            if (null == entry.getKey()) {
                                result.addUserizedCreation(entry.getValue());
                            } else if (null == entry.getValue()) {
                                result.addUserizedDeletion(timestamp, entry.getKey());
                            } else {
                                result.addUserizedUpdate(entry.getKey(), entry.getValue());
                            }
                        }
                    } else if (isSeriesMaster(originalEvent)) {
                        for (Event originalOccurrence : resolveOccurrences(getOriginalUserizedEvent(originalEvent, folder))) {
                            result.addUserizedDeletion(timestamp, originalOccurrence);
                        }
                        result.addUserizedDeletion(timestamp, userize(originalEvent, folder));
                        result.addUserizedCreation(userize(updatedEvent, folder));
                    } else if (isSeriesMaster(updatedEvent)) {
                        result.addUserizedDeletion(timestamp, userize(originalEvent, folder));
                        result.addUserizedCreations(resolveOccurrences(userize(updatedEvent, folder)));
                    }
                } else {
                    result.addUserizedUpdate(userize(originalEvent, folder), userize(updatedEvent, folder));
                }
            } else {
                /*
                 * "delete" from calendar user's point of view
                 */
                if (isSeriesMaster(originalEvent) && isResolveOccurrences(session)) {
                    for (Event originalOccurrence : resolveOccurrences(getOriginalUserizedEvent(originalEvent, folder))) {
                        result.addUserizedDeletion(timestamp, originalOccurrence);
                    }
                } else {
                    result.addUserizedDeletion(timestamp, userize(originalEvent, folder));
                }
            }
        } else if (isInFolder(updatedEvent, folder)) {
            /*
             * "create" from calendar user's point of view - possible for attendee being added to an existing event, so that it shows up
             * in the new attendee's folder afterwards (after #needsExistenceCheckInTargetFolder() was false)
             */
            if (isSeriesMaster(updatedEvent) && isResolveOccurrences(session)) {
                result.addUserizedCreations(resolveOccurrences(userize(updatedEvent, folder)));
            } else {
                result.addUserizedCreation(userize(updatedEvent, folder));
            }
        }
    }

    /**
     * Tracks suitable results for a deleted event in the current internal calendar result, which includes adding a <i>plain</i> deletion
     * for the removed event data, as well as an appropriate deletion from the acting user's point of view.
     *
     * @param deletedEvent The deleted event
     */
    public void trackDeletion(Event deletedEvent) throws OXException {
        result.addAffectedFolderIds(folder.getId(), getPersonalFolderIds(deletedEvent.getAttendees()));
        result.addPlainDeletion(timestamp, deletedEvent);
        if (includeAllFolderViews(session)) {
            for (CalendarFolder visibleFolder : getVisibleFolderViews(deletedEvent)) {
                trackUserizedDeletion(visibleFolder, deletedEvent);
            }
        } else {
            trackUserizedDeletion(folder, deletedEvent);
        }
    }

    /**
     * Tracks suitable results for a deleted event in the current internal calendar result, based on a specific folder representing the
     * targeted view for userization of the event data.
     *
     * @param folder The folder representing the targeted view for userization of the event data
     * @param deletedEvent The deleted event
     */
    private void trackUserizedDeletion(CalendarFolder folder, Event deletedEvent) throws OXException {
        Event originalUserizedEvent = getOriginalUserizedEvent(deletedEvent, folder);
        if (isSeriesMaster(deletedEvent) && isResolveOccurrences(session)) {
            for (Event deletedOccurrence : resolveOccurrences(originalUserizedEvent)) {
                result.addUserizedDeletion(timestamp, deletedOccurrence);
            }
        } else {
            result.addUserizedDeletion(timestamp, originalUserizedEvent);
        }
    }

    /**
     * Gets the folders representing all possible <i>views</i> on the event data the current session user has.
     *
     * @param event The event
     * @return A list of all folders in which the event appears for the current session user
     */
    private List<CalendarFolder> getVisibleFolderViews(Event event) throws OXException {
        if (PublicType.getInstance().equals(folder.getType()) || false == CalendarUtils.isGroupScheduled(event)) {
            return Collections.singletonList(folder);
        }
        return getVisibleFolderViews(getPersonalFolderIds(event.getAttendees()));
    }

    /**
     * Gets the folders representing all possible <i>views</i> for an original and updated event data the current session user has.
     *
     * @param originalEvent The original event data
     * @param updatedEvent The updated event data
     * @return A list of all folders in which the event appears for the current session user
     */
    private List<CalendarFolder> getVisibleFolderViews(Event originalEvent, Event updatedEvent) throws OXException {
        Set<String> affectedFolderIds = new HashSet<String>();
        if (null != originalEvent.getFolderId()) {
            affectedFolderIds.add(originalEvent.getFolderId());
        } else {
            affectedFolderIds.addAll(getPersonalFolderIds(originalEvent.getAttendees()));
        }
        if (null != updatedEvent.getFolderId()) {
            affectedFolderIds.add(updatedEvent.getFolderId());
        } else {
            affectedFolderIds.addAll(getPersonalFolderIds(updatedEvent.getAttendees()));
        }
        return getVisibleFolderViews(affectedFolderIds);
    }

    private List<CalendarFolder> getVisibleFolderViews(Collection<String> affectedFolderIds) throws OXException {
        List<CalendarFolder> folderViews = new ArrayList<CalendarFolder>();
        if (affectedFolderIds.remove(folder.getId())) {
            folderViews.add(folder);
        }
        folderViews.addAll(Utils.getVisibleFolders(session, affectedFolderIds));
        return folderViews;
    }

    private List<Event> resolveOccurrences(Event master) throws OXException {
        Iterator<Event> iterator = Utils.resolveOccurrences(session, master);
        List<Event> list = new ArrayList<Event>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
            protection.checkEventCollection(list);
        }
        return list;
    }

    /**
     * Gets the original, <i>userized</i> version of an event, representing a specific user's point of view on the original event data, by
     * loading the auxiliary event within a separate database transaction (assuming the current, modifying one is not yet committed).
     *
     * @param event The event to load the original userized data for
     * @param folder The folder representing the view on the event
     * @return The original userized version of the event
     */
    private Event getOriginalUserizedEvent(Event event, CalendarFolder folder) throws OXException {
        Event originalUserizedEvent = optOriginalUserizedEvent(folder, event.getId());
        if (null != originalUserizedEvent) {
            return originalUserizedEvent;
        }
        Connection oldConnection = session.get(PARAMETER_CONNECTION(), Connection.class);
        session.set(PARAMETER_CONNECTION(), null);
        try {
            return new InternalCalendarStorageOperation<Event>(session) {

                @Override
                protected Event execute(CalendarSession session, CalendarStorage storage) throws OXException {
                    return userize(event, getCalendarUserId(folder));
                }
            }.executeQuery();
        } finally {
            session.set(PARAMETER_CONNECTION(), oldConnection);
        }
    }

    private Event optOriginalUserizedEvent(CalendarFolder folder, String eventId) {
        Map<String, Event> userizedEventsById = originalUserizedEvents.get(folder);
        return null != userizedEventsById ? userizedEventsById.get(eventId) : null;
    }

    private void rememberOriginalUserizedEvent(CalendarFolder folder, Event originalEvent) throws OXException {
        if (isInFolder(originalEvent, folder)) {
            Map<String, Event> userizedEventsById = originalUserizedEvents.get(folder);
            if (null == userizedEventsById) {
                userizedEventsById = new HashMap<String, Event>();
                originalUserizedEvents.put(folder, userizedEventsById);
            }
            if (false == userizedEventsById.containsKey(originalEvent.getId())) {
                userizedEventsById.put(originalEvent.getId(), userize(originalEvent, folder));
            }
        }
    }

    /**
     * Creates a <i>userized</i> version of an event, representing a specific user's point of view on the event data. This includes
     * <ul>
     * <li><i>anonymization</i> of restricted event data in case the event it is not marked as {@link Classification#PUBLIC}, and the
     * current session's user is neither creator, nor attendee of the event.</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>generate and apply event flags</li>
     * <li>taking over the user's personal list of alarms for the event</li>
     * </ul>
     *
     * @param event The event to userize
     * @param folder The folder representing the view on the event
     * @return The <i>userized</i> event
     * @see Utils#applyExceptionDates
     * @see Utils#anonymizeIfNeeded
     */
    private Event userize(Event event, CalendarFolder folder) throws OXException {
        return userize(event, getCalendarUserId(folder));
    }

    /**
     * Creates a <i>userized</i> version of an event, representing a specific user's point of view on the event data. This includes
     * <ul>
     * <li><i>anonymization</i> of restricted event data in case the event it is not marked as {@link Classification#PUBLIC}, and the
     * current session's user is neither creator, nor attendee of the event.</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>generate and apply event flags</li>
     * <li>taking over the user's personal list of alarms for the event</li>
     * </ul>
     *
     * @param storage The calendar storage to use
     * @param session The calendar session
     * @param event The event to userize
     * @param forUser The identifier of the user in whose point of view the event should be adjusted
     * @return The <i>userized</i> event
     * @see Utils#applyExceptionDates
     * @see Utils#anonymizeIfNeeded
     */
    Event userize(Event event, int forUser) throws OXException {
        return userize(event, forUser, false);
    }

    /**
     * Creates a <i>userized</i> version of an event, representing a specific user's point of view on the event data. This includes
     * <ul>
     * <li><i>anonymization</i> of restricted event data in case the event it is not marked as {@link Classification#PUBLIC}, and the
     * current session's user is neither creator, nor attendee of the event.</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>generate and apply event flags</li>
     * <li>optionally apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance</li>
     * <li>taking over the user's personal list of alarms for the event</li>
     * </ul>
     *
     * @param event The event to userize
     * @param forUser The identifier of the user in whose point of view the event should be adjusted
     * @param applyExceptionDates <code>true</code> to apply individual exception dates for series master events, <code>false</code> if not needed
     * @return The <i>userized</i> event
     * @see Utils#applyExceptionDates
     * @see Utils#anonymizeIfNeeded
     */
    private Event userize(Event event, int forUser, boolean applyExceptionDates) throws OXException {
        if (applyExceptionDates && isSeriesMaster(event)) {
            event = applyExceptionDates(storage, event, forUser);
        }
        final List<Alarm> alarms = storage.getAlarmStorage().loadAlarms(event, forUser);
        final String folderView = getFolderView(event, forUser);
        EnumSet<EventFlag> flags = CalendarUtils.getFlags(event, forUser, session.getUserId());
        if (null != alarms && 0 < alarms.size()) {
            flags.add(EventFlag.ALARMS);
        }
        if (isSeriesException(event)) {
            RecurrenceData recurrenceData = optRecurrenceData(event);
            if (null != recurrenceData) {
                if (isLastOccurrence(event.getRecurrenceId(), recurrenceData, session.getRecurrenceService())) {
                    flags.add(EventFlag.LAST_OCCURRENCE);
                }
                if (isFirstOccurrence(event.getRecurrenceId(), recurrenceData, session.getRecurrenceService())) {
                    flags.add(EventFlag.FIRST_OCCURRENCE);
                }
            }
        }
        event = new DelegatingEvent(event) {

            @Override
            public String getFolderId() {
                return folderView;
            }

            @Override
            public boolean containsFolderId() {
                return true;
            }

            @Override
            public List<Alarm> getAlarms() {
                return alarms;
            }

            @Override
            public boolean containsAlarms() {
                return true;
            }

            @Override
            public EnumSet<EventFlag> getFlags() {
                return flags;
            }

            @Override
            public boolean containsFlags() {
                return true;
            }

        };
        return anonymizeIfNeeded(session, event);
    }

    private RecurrenceData optRecurrenceData(Event event) throws OXException {
        String seriesId = event.getSeriesId();
        if (null == seriesId) {
            return null;
        }
        if (RecurrenceData.class.isInstance(event.getRecurrenceId())) {
            return ((RecurrenceData) event.getRecurrenceId());
        }
        RecurrenceData recurrenceData = knownRecurrenceData.get(seriesId);
        if (null == recurrenceData) {
            EventField[] fields = new EventField[] { EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.RECURRENCE_DATES, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES };
            Event seriesMaster = storage.getEventStorage().loadEvent(seriesId, fields);
            if (null != seriesMaster) {
                recurrenceData = new DefaultRecurrenceData(seriesMaster);
                knownRecurrenceData.put(seriesId, recurrenceData);
            }
        }
        return recurrenceData;
    }

    /**
     * Gets a value indicating whether all possible folder views should be tracked in the <i>userized</i> results or not.
     *
     * @param session The calendar session
     * @return <code>true</code> if all visible folder views should be included, <code>false</code>, otherwise
     */
    private static boolean includeAllFolderViews(CalendarSession session) {
        return isResolveOccurrences(session);
    }

}
