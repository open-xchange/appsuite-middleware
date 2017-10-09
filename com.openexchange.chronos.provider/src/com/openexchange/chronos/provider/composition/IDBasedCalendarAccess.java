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

package com.openexchange.chronos.provider.composition;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionAware;

/**
 * {@link IDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public interface IDBasedCalendarAccess extends TransactionAware, CalendarParameters {

    /**
     * Gets the session associated with this calendar access instance.
     *
     * @return The session the access was initialized for
     */
    Session getSession();

    /**
     * Gets the user's default calendar folder.
     *
     * @return The default calendar folder
     */
    CalendarFolder getDefaultFolder() throws OXException;

    /**
     * Gets a list of all visible calendar folders.
     *
     * @param type The type to get the visible folders for
     * @return A list of all visible calendar folders of the type
     */
    List<CalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException;

    /**
     * Gets a specific calendar folder.
     *
     * @param folderId The fully qualified identifier of the folder to get the events from
     * @return The calendar folder
     */
    CalendarFolder getFolder(String folderId) throws OXException;

    /**
     * Create a new calendar folder.
     *
     * @param parentFolderId The fully qualified identifier of the parent folder
     * @param folder The calendar folder to create
     * @return The fully qualified identifier of the newly created folder
     */
    String createFolder(String parentFolderId, CalendarFolder folder) throws OXException;

    /**
     * Updates a calendar folder.
     *
     * @param folderId The fully qualified identifier of the folder to update
     * @param folder The updated calendar folder data
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The (possibly changed) fully qualified identifier of the updated folder
     */
    String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException;

    /**
     * Deletes a calendar folder.
     *
     * @param folderId The fully qualified identifier of the folder to delete
     */
    void deleteFolder(String folderId, long clientTimestamp) throws OXException;

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
     * <li>{@link CalendarParameters#PARAMETER_INCLUDE_PRIVATE}</li>
     * </ul>
     *
     * @param folderId The fully qualified identifier of the folder to get the events from
     * @return The events
     */
    List<Event> getEventsInFolder(String folderId) throws OXException;

    /**
     * Gets all events of the session's user.
     * <p/>
     * <b>Note:</b> Only events from the internal <i>groupware</i> calendar provider are considered.
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
     * @return The events
     */
    List<Event> getEventsOfUser() throws OXException;

    /**
     * Searches for events by one or more queries in the fields {@link EventField#SUMMARY}, {@link EventField#DESCRIPTION} and
     * {@link EventField#CATEGORIES}. The queries are surrounded by wildcards implicitly to follow a <i>contains</i> semantic.
     * Additional, storage-specific search filters can be applied.
     * <p/>
     * <b>Note:</b> Only available for {@link SearchAware} calendar providers.
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
     * @param folderIds The identifiers of the folders to perform the search in, or <code>null</code> to search across all visible folders
     *            of the user's {@link SearchAware} calendar accounts
     * @param filters A list of additional filters to be applied on the search, or <code>null</code> if not specified
     * @param queries The queries to search for, or <code>null</code> if not specified
     * @return The found events, or an empty list if there are none
     */
    List<Event> searchEvents(String[] folderIds, List<SearchFilter> filters, List<String> queries) throws OXException;

    /**
     * Gets lists of new and updated as well as deleted events since a specific timestamp in a folder.
     * <p/>
     * <b>Note:</b> Only available for {@link SyncAware} calendar providers.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE} ("changed" and "deleted")</li>
     * <li>{@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES}</li>
     * <li>{@link CalendarParameters#PARAMETER_INCLUDE_PRIVATE}</li>
     * </ul>
     *
     * @param folderId The fully qualified identifier of the folder to get the updated events from
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     */
    UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException;

    /**
     * Gets lists of new and updated as well as deleted events since a specific timestamp of a user.
     * <p/>
     * <b>Note:</b> Only events from the internal <i>groupware</i> calendar provider are considered.
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
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     */
    UpdatesResult getUpdatedEventsOfUser(long updatedSince) throws OXException;

    /**
     * Gets a specific event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param eventID The identifier of the event to get
     * @return The event
     */
    Event getEvent(EventID eventID) throws OXException;

    /**
     * Gets a list of events.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param eventIDs A list of the identifiers of the events to get
     * @return The events
     */
    List<Event> getEvents(List<EventID> eventIDs) throws OXException;

    /**
     * Gets all change exceptions of a recurring event series.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param folderId The fully qualified identifier of the parent folder of the event series to get the change exceptions for
     * @param seriesId The identifier of the series to get the change exceptions for
     * @return The change exceptions, or an empty list if there are none
     */
    List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException;

    /**
     * Creates a new event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param folderId The fully qualified identifier of the parent folder to create the event in
     * @param event The event data to create
     * @return The create result
     */
    CalendarResult createEvent(String folderId, Event event) throws OXException;

    /**
     * Updates an existing event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param eventID The identifier of the event to update
     * @param event The event data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateEvent(EventID eventID, Event event, long clientTimestamp) throws OXException;

    /**
     * Moves an existing event into another folder.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param eventID The identifier of the event to move
     * @param targetFolderId The fully qualified identifier of the destination folder to move the event into
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The move result
     */
    CalendarResult moveEvent(EventID eventID, String targetFolderId, long clientTimestamp) throws OXException;

    /**
     * Updates a specific attendee of an existing event.
     *
     * @param eventID The identifier of the event to get
     * @param attendee The attendee to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAttendee(EventID eventID, Attendee attendee, long clientTimestamp) throws OXException;

    /**
     * Updates the user's personal alarms of a specific event, independently of the user's write access permissions for the corresponding
     * event.
     * <p/>
     * <b>Note:</b> Only available for {@link PersonalAlarmAware} calendar providers.
     * <p/>
     *
     * @param eventID The identifier of the event to update the alarms for
     * @param alarms The updated list of alarms to apply, or <code>null</code> to remove any previously stored alarms
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing event.
     *
     * @param eventID The identifier of the event to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The delete result
     */
    CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException;

    /**
     * Retrieves all upcoming alarm triggers until the given time.
     *
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * </ul>
     *
     * @param actions The actions to retrieve
     * @return A list of upcoming alarm triggers
     * @throws OXException
     */
    List<AlarmTrigger> getAlarmTrigger(Set<String> actions) throws OXException;

    /**
     * Retrieves the {@link IFileHolder} with the specified managed identifier from the {@link Event}
     * with the specified {@link EventID}
     *
     * @param eventID The {@link Event} identifier
     * @param managedId The managed identifier of the {@link Attachment}
     * @return The {@link IFileHolder}
     * @throws OXException if an error is occurred
     */
    IFileHolder getAttachment(EventID eventID, int managedId) throws OXException;

    /**
     * Queries the free/busy time for a list of attendees.
     *
     * @param attendees The queried attendees
     * @param from The start of the requested time range
     * @param until The end of the requested time range
     * @return The free/busy results for each of the queried attendees
     */
    Map<Attendee, FreeBusyResult> queryFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException;

}
