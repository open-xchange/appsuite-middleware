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

package com.openexchange.chronos.operation;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import java.util.List;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * {@link CreateOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreateOperation extends AbstractOperation {

    /**
     * Prepares a create operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared create operation
     */
    public static CreateOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new CreateOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link CreateOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private CreateOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the creation of an event.
     *
     * @param event The event to create
     * @param alarms The alarms to insert
     * @return The result
     */
    public CalendarResultImpl perform(Event event, List<Alarm> alarms) throws OXException {
        /*
         * check current session user's permissions
         */
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        Consistency.setCreated(timestamp, event, calendarUser.getId());
        Consistency.setModified(timestamp, event, session.getUser().getId());
        if (null == event.getOrganizer()) {
            Consistency.setOrganizer(event, calendarUser, session.getUser());
        }
        Consistency.setTimeZone(event, calendarUser);
        Consistency.adjustAllDayDates(event);
        event.setSequence(0);
        if (Strings.isNotEmpty(event.getUid())) {
            if (0 < resolveUid(event.getUid())) {
                throw OXException.general("Duplicate uid"); //TODO
            }
        } else {
            event.setUid(UUID.randomUUID().toString());
        }
        event.setPublicFolderId(PublicType.getInstance().equals(folder.getType()) ? i(folder) : 0);
        /*
         * assign new object identifier
         */
        int objectID = storage.nextObjectID();
        event.setId(objectID);
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            event.setSeriesId(objectID);
        }
        /*
         * insert event, attendees & alarms of user
         */
        storage.getEventStorage().insertEvent(event);
        storage.getAttendeeStorage().insertAttendees(objectID, new AttendeeHelper(session, folder, null, event.getAttendees()).getAttendeesToInsert());
        if (null != alarms && 0 < alarms.size()) {
            storage.getAlarmStorage().insertAlarms(objectID, calendarUser.getId(), alarms);
        }
        result.addCreation(new CreateResultImpl(loadEventData(objectID)));
        return result;
    }

    private int resolveUid(String uid) throws OXException {
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.UID, SingleOperation.EQUALS, uid))
            .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.ISNULL))
                .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
            )
        ;
        /*
         * search for an event matching the UID
         */
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, null, new EventField[] { EventField.ID });
        return 0 < events.size() ? events.get(0).getId() : 0;
    }


}
