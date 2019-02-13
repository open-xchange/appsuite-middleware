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

package com.openexchange.chronos.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarService {

    /**
     * Initializes a new calendar session.
     *
     * @param session The underlying server session
     * @return A new calendar session
     */
    CalendarSession init(Session session) throws OXException;

    /**
     * Initializes a new calendar session.
     *
     * @param session The underlying server session
     * @param parameters Arbitrary calendar parameters to take over
     * @return A new calendar session
     */
    CalendarSession init(Session session, CalendarParameters parameters) throws OXException;

    /**
     * Provides access to additional utilities.
     *
     * @return The calendar service utilities
     */
    CalendarServiceUtilities getUtilities();

    /**
     * Gets the sequence number of a calendar folder, which is the highest last-modification timestamp of the folder itself and his
     * contents. Distinct object access permissions (e.g. <i>read own</i>) are not considered.
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get the sequence number for
     * @return The sequence number
     */
    long getSequenceNumber(CalendarSession session, String folderID) throws OXException;

    /**
     * Searches for events by pattern in the fields {@link EventField#SUMMARY}, {@link EventField#DESCRIPTION} and
     * {@link EventField#CATEGORIES}. The pattern is surrounded by wildcards implicitly to follow a <i>contains</i> semantic.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderIDs The identifiers of the folders to perform the search in, or <code>null</code> to search across all visible folders
     * @param pattern The pattern to search for
     * @return The found events, or an empty list if there are none
     */
    List<Event> searchEvents(CalendarSession session, String[] folderIDs, String pattern) throws OXException;

    /**
     * Searches for events by one or more queries in the fields {@link EventField#SUMMARY}, {@link EventField#DESCRIPTION} and
     * {@link EventField#CATEGORIES}. The queries are surrounded by wildcards implicitly to follow a <i>contains</i> semantic.
     * Additional, storage-specific search filters can be applied.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderIds The identifiers of the folders to perform the search in, or <code>null</code> to search across all visible folders
     * @param filters A list of additional filters to be applied on the search, or <code>null</code> if not specified
     * @param queries The queries to search for, or <code>null</code> if not specified
     * @return The found events per folder
     */
    Map<String, EventsResult> searchEvents(CalendarSession session, List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException;

    /**
     * Gets all change exceptions of a recurring event series.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param seriesID The identifier of the series to get the change exceptions for
     * @return The change exceptions, or an empty list if there are none
     */
    List<Event> getChangeExceptions(CalendarSession session, String folderID, String seriesID) throws OXException;

    /**
     * Gets a specific event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param eventId The identifier of the event to get
     * @return The event
     */
    Event getEvent(CalendarSession session, String folderID, EventID eventId) throws OXException;

    /**
     * Gets a list of events.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder representing the current user's calendar view
     * @param eventIDs A list of the identifiers of the events to get
     * @return The events
     */
    List<Event> getEvents(CalendarSession session, List<EventID> eventIDs) throws OXException;

    /**
     * Gets all events in a specific calendar folder.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get the events from
     * @return The events
     */
    List<Event> getEventsInFolder(CalendarSession session, String folderID) throws OXException;

    /**
     * Gets all events from one or more specific calendar folders.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderIds The identifier of the folders to get the events from
     * @return The resulting events from each requested folder
     */
    Map<String, EventsResult> getEventsInFolders(CalendarSession session, List<String> folderIds) throws OXException;

    /**
     * Gets all events of the session's user.
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @return The events
     */
    List<Event> getEventsOfUser(CalendarSession session) throws OXException;

    /**
     * Gets all events the session's user attends in, having a particular participation status.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_RIGHT_HAND_LIMIT}</li>
     * <li>{@link CalendarParameters#PARAMETER_LEFT_HAND_LIMIT}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param partStats The participation status to include, or <code>null</code> to include all events independently of the user
     *            attendee's participation status
     * @param rsvp The reply expectation to include, or <code>null</code> to include all events independently of the user attendee's
     *            rsvp status
     * @return The events
     */
    List<Event> getEventsOfUser(CalendarSession session, Boolean rsvp, ParticipationStatus[] partStats) throws OXException;

    /**
     * Gets lists of new and updated as well as deleted events since a specific timestamp in a folder.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE} ("changed" and "deleted")</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderID The identifier of the folder to get the updated events from
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     */
    UpdatesResult getUpdatedEventsInFolder(CalendarSession session, String folderID, long updatedSince) throws OXException;

    /**
     * Gets lists of new and updated as well as deleted events since a specific timestamp of a user.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE} ("changed" and "deleted")</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     */
    UpdatesResult getUpdatedEventsOfUser(CalendarSession session, long updatedSince) throws OXException;

    /**
     * Creates a new event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * <li>{@link CalendarParameters#PARAMETER_TRACK_ATTENDEE_USAGE}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to create the event in
     * @param event The event data to create
     * @return The create result
     */
    CalendarResult createEvent(CalendarSession session, String folderId, Event event) throws OXException;

    /**
     * Updates an existing event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * <li>{@link CalendarParameters#PARAMETER_TRACK_ATTENDEE_USAGE}</li>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE_FORBIDDEN_ATTENDEE_CHANGES}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to update
     * @param event The event data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateEvent(CalendarSession session, EventID eventID, Event event, long clientTimestamp) throws OXException;

    /**
     * Updates an existing event in the name of the organizer (bypasses permission checks).
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * <li>{@link CalendarParameters#PARAMETER_TRACK_ATTENDEE_USAGE}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to update
     * @param event The event data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateEventAsOrganizer(CalendarSession session, EventID eventID, Event event, long clientTimestamp) throws OXException;

    /**
     * <i>Touches</i> an existing event by setting a new, current last modification timestamp.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to touch
     * @return The update result
     */
    CalendarResult touchEvent(CalendarSession session, EventID eventID) throws OXException;

    /**
     * Moves an existing event into another folder.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to update
     * @param folderId The identifier of the folder to move the event to
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The move result
     */
    CalendarResult moveEvent(CalendarSession session, EventID eventID, String folderId, long clientTimestamp) throws OXException;

    /**
     * Updates a specific attendee of an existing event.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to update
     * @param attendee The attendee to update
     * @param alarms The alarms to update, or <code>null</code> to not change alarms, or an empty array to delete any existing alarms
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAttendee(CalendarSession session, EventID eventID, Attendee attendee, List<Alarm> alarms, long clientTimestamp) throws OXException;

    /**
     * Updates the user's personal alarms of a specific event, independently of the user's write access permissions for the corresponding
     * event.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to update the alarms for
     * @param alarms The updated list of alarms to apply, or <code>null</code> to remove any previously stored alarms
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAlarms(CalendarSession session, EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException;
    
    /**
     * Updates the event's organizer to the new one.
     * <p>
     * Current restrictions are:
     * 
     * <li>The event has to be a group scheduled event</li>
     * <li>All attendees of the event have to be internal</li>
     * <li>The new organizer must be an internal user</li>
     * <li>The change has to be performed for one of these:
     * <ul> a single event</ul>
     * <ul> a series master, efficiently updating for the complete series</ul>
     * <ul> a specific recurrence of the series, efficiently performing a series split. Only allowed if {@link com.openexchange.chronos.RecurrenceRange#THISANDFUTURE} is set</ul>
     * </li>
     * 
     * @param session The calendar session
     * @param eventID The {@link EventID} of the event to change. Optional having a recurrence ID set to perform a series split.
     * @param organizer The new organizer
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The updated event
     * @throws OXException In case the organizer change is not allowed
     */
    CalendarResult changeOrganizer(CalendarSession session, EventID eventID, Organizer organizer, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing event.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The delete result
     */
    CalendarResult deleteEvent(CalendarSession session, EventID eventID, long clientTimestamp) throws OXException;

    /**
     * Splits an existing event series into two separate event series.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event series to split
     * @param splitPoint The date or date-time where the split is to occur
     * @param uid A new unique identifier to assign to the new part of the series, or <code>null</code> if not set
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The split result
     */
    CalendarResult splitSeries(CalendarSession session, EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException;

    /**
     * Clears a folder's contents by deleting all contained events.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to clear
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The delete result
     */
    CalendarResult clearEvents(CalendarSession session, String folderId, long clientTimestamp) throws OXException;

    /**
     * Imports a list of events into a specific folder.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * <li>{@link CalendarParameters#PARAMETER_SUPPRESS_ITIP}, defaulting to {@link Boolean#TRUE} unless overridden</li>
     * <li>{@link CalendarParameters#UID_CONFLICT_STRATEGY}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param folderId The identifier of the target folder
     * @param events The events to import
     * @return A list of results holding further information about each imported event
     */
    List<ImportResult> importEvents(CalendarSession session, String folderId, List<Event> events) throws OXException;

    /**
     * Retrieves upcoming alarm triggers.
     *
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * </ul>
     *
     * @param session The calendar session
     * @param actions The actions to retrieve
     * @return A list of {@link AlarmTrigger}
     * @throws OXException
     */
    List<AlarmTrigger> getAlarmTriggers(CalendarSession session, Set<String> actions) throws OXException;

    /**
     * Retrieves the {@link IFileHolder} with the specified managed identifier from the {@link Event}
     * with the specified {@link EventID}
     *
     * @param session The {@link CalendarSession}
     * @param eventID The {@link Event} identifier
     * @param managedId The managed identifier of the {@link Attachment}
     * @return The {@link IFileHolder}
     * @throws OXException if an error is occurred
     */
    IFileHolder getAttachment(CalendarSession session, EventID eventID, int managedId) throws OXException;

}
