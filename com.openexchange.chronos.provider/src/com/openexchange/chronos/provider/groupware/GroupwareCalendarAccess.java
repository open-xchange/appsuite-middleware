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

package com.openexchange.chronos.provider.groupware;

import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.provider.extensions.PermissionAware;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;

/**
 * {@link GroupwareCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public interface GroupwareCalendarAccess extends FolderCalendarAccess, PermissionAware {

    /**
     * Gets a list of all visible calendar folders.
     *
     * @param type The type to get the visible folders for
     * @return A list of all visible calendar folders of the type
     */
    List<GroupwareCalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException;

    /**
     * Gets the default calendar folder.
     *
     * @return The default folder
     */
    GroupwareCalendarFolder getDefaultFolder() throws OXException;

    /**
     * Gets all events of the session's user.
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
     * @param partStats The participation status to include, or <code>null</code> to include all events independently of the user
     *            attendee's participation status
     * @param rsvp The reply expectation to include, or <code>null</code> to include all events independently of the user attendee's
     *            rsvp status
     * @return The events
     */
    List<Event> getEventsOfUser(Boolean rsvp, ParticipationStatus[] partStats) throws OXException;

    /**
     * Resolves an event identifier to an event, and returns it in the perspective of the current session's user, i.e. having an
     * appropriate parent folder identifier assigned.
     *
     * @param eventId The identifier of the event to resolve
     * @return The resolved event from the user's point of view, or <code>null</code> if not found
     */
    Event resolveEvent(String eventId) throws OXException;

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
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     */
    UpdatesResult getUpdatedEventsOfUser(long updatedSince) throws OXException;

    /**
     * Creates a new event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param folderId The identifier of the folder to create the event in
     * @param event The event data to create
     * @return The create result
     */
    CalendarResult createEvent(String folderId, Event event) throws OXException;

    /**
     * Updates an existing event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
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
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * </ul>
     *
     * @param eventID The identifier of the event to update
     * @param folderId The identifier of the folder to move the event to
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The move result
     */
    CalendarResult moveEvent(EventID eventID, String folderId, long clientTimestamp) throws OXException;

    /**
     * Updates a specific attendee of an existing event.
     *
     * @param eventID The identifier of the event to update
     * @param attendee The attendee to update
     * @param alarms The alarms to update, or <code>null</code> to not change alarms, or an empty array to delete any existing alarms
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAttendee(EventID eventID, Attendee attendee, List<Alarm> alarms, long clientTimestamp) throws OXException;
    
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
     * <ul> a specific recurrence of the series, efficiently performing a series split</ul>
     * </li>
     * 
     * @param eventID The {@link EventID} of the event to change. Optional having a recurrence ID set to perform a series split.
     * @param organizer The new organizer
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The updated event
     * @throws OXException In case the organizer change is not allowed
     */
    CalendarResult changeOrganizer(EventID eventID, CalendarUser organizer, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing event.
     *
     * @param eventID The identifier of the event to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The delete result
     */
    CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException;

    /**
     * Splits an existing event series into two separate event series.
     *
     * @param eventID The identifier of the event series to split
     * @param splitPoint The date or date-time where the split is to occur
     * @param uid A new unique identifier to assign to the new part of the series, or <code>null</code> if not set
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The split result
     */
    CalendarResult splitSeries(EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException;

    /**
     * Imports a list of events into a specific folder.
     * <p/>
     * <b>Note:</b> Only available for the internal <i>groupware</i> calendar provider.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}</li>
     * <li>{@link CalendarParameters#PARAMETER_NOTIFICATION}</li>
     * <li>{@link CalendarParameters#UID_CONFLICT_STRATEGY}</li>
     * </ul>
     *
     * @param folderId The identifier of the target folder
     * @param events The events to import
     * @return A list of results holding further information about each imported event
     */
    List<ImportResult> importEvents(String folderId, List<Event> events) throws OXException;

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

}
