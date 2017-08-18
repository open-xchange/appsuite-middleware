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

package com.openexchange.chronos.provider.groupware;

import java.util.List;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;

/**
 * {@link GroupwareCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface GroupwareCalendarAccess extends CalendarAccess {

    /**
     * Creates a new folder.
     *
     * @param parentFolderId The identifier of the parent folder
     * @param folder The folder data to create
     * @return The identifier of the newly created folder
     */
    String createFolder(String parentFolderId, CalendarFolder folder) throws OXException;

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
     * <li>{@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS}</li>
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
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The update result
     */
    CalendarResult updateAttendee(EventID eventID, Attendee attendee, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing event.
     *
     * @param eventID The identifier of the event to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The delete result
     */
    CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException;

    /**
     * Retrieves upcoming alarm trigger.
     * 
     * The following calendar parameters are evaluated:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * </ul>
     *
     * @return A list of {@link AlarmTrigger}
     * @throws OXException
     */
    List<AlarmTrigger> getAlarmTrigger() throws OXException;

}
