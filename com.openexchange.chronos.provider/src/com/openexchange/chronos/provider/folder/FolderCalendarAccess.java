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

package com.openexchange.chronos.provider.folder;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.exception.OXException;

/**
 * {@link FolderCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface FolderCalendarAccess extends CalendarAccess {

    /**
     * Gets a specific calendar folder.
     *
     * @param folderId The identifier of the calendar folder to get
     * @return The calendar folder
     */
    CalendarFolder getFolder(String folderId) throws OXException;

    /**
     * Creates a new folder.
     *
     * @param folder The folder data to create
     * @return The identifier of the newly created folder
     */
    String createFolder(CalendarFolder folder) throws OXException;

    /**
     * Updates an existing folder.
     *
     * @param folderId The identifier of the folder to update
     * @param folder The folder data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The (possibly changed) identifier of the updated folder
     */
    String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing folder.
     *
     * @param folderId The identifier of the folder to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     */
    void deleteFolder(String folderId, long clientTimestamp) throws OXException;

    /**
     * Gets a list of all visible calendar folders.
     *
     * @return A list of all visible calendar folders.
     */
    List<CalendarFolder> getVisibleFolders() throws OXException;

    /**
     * Gets a specific event.
     * <p/>
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param folderId The identifier of the folder representing the current user's calendar view
     * @param eventId The identifier of the event to get
     * @param recurrenceId The recurrence identifier of the event occurrence to get from an event series, or <code>null</code> to not get
     *            a specific occurrence
     * @return The event
     */
    Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException;

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
     * @param folderId The identifier of the folder representing the current user's calendar view
     * @param seriesId The identifier of the series to get the change exceptions for
     * @return The change exceptions, or an empty list if there are none
     */
    List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException;

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
     * <li>{@link CalendarParameters#PARAMETER_SKIP_CLASSIFIED}</li>
     * </ul>
     *
     * @param folderId The identifier of the folder to get the events from
     * @return The events
     */
    List<Event> getEventsInFolder(String folderId) throws OXException;

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
     * <li>{@link CalendarParameters#PARAMETER_SKIP_CLASSIFIED}</li>
     * </ul>
     *
     * @param folderId The identifier of the folder to get the events from
     * @return The resulting events per folder
     */
    Map<String, EventsResult> getEventsInFolders(List<String> folderIds) throws OXException;

}
