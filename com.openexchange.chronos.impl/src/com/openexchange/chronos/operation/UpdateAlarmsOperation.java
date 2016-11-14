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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AlarmMapper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.ldap.User;

/**
 * {@link UpdateAlarmsOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateAlarmsOperation extends AbstractUpdateOperation {

    /**
     * Prepares an alarm update operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared alarm update operation
     */
    public static UpdateAlarmsOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new UpdateAlarmsOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link UpdateAlarmsOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private UpdateAlarmsOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the alarm update of the calendar user for the targeted event. No further updates are processed.
     *
     * @param objectID The identifier of the event to update the alarms for
     * @param updatedAlarms The alarms to update, or <code>null</code> to remove any alarms
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public CalendarResultImpl perform(int objectID, List<Alarm> updatedAlarms, long clientTimestamp) throws OXException {
        return perform(loadEventData(objectID), updatedAlarms, clientTimestamp);
    }

    /**
     * Performs the alarm update of the calendar user for the targeted event. No further updates are processed.
     *
     * @param objectID The identifier of the event to update the alarms for
     * @param updatedAlarms The alarms to update, or <code>null</code> to remove any alarms
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public CalendarResultImpl perform(Event originalEvent, List<Alarm> updatedAlarms, long clientTimestamp) throws OXException {
        /*
         * check current session user's permissions
         */
        Check.eventIsInFolder(originalEvent, folder);
        if (session.getUser().getId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
        }
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (null == find(originalEvent.getAttendees(), calendarUser.getId())) {
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(String.valueOf(calendarUser.getId()), I(originalEvent.getId()));
        }
        /*
         * update alarms
         */
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = updateAlarms(originalEvent.getId(), calendarUser, updatedAlarms);
        result.addUpdate(new UpdateResultImpl(originalEvent, i(folder), originalEvent).setAlarmUpdates(alarmUpdates));
        return result;
    }

    /**
     * Updates a calendar user's alarms for a specific event.
     *
     * @param objectID The identifier of the event to update the alarms in
     * @param calendarUser The calendar user whose alarms are updated
     * @param updatedAlarms The updated alarms
     * @return A corresponding collection update
     */
    private CollectionUpdate<Alarm, AlarmField> updateAlarms(int objectID, User calendarUser, List<Alarm> updatedAlarms) throws OXException {
        List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(objectID, calendarUser.getId());
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = AlarmMapper.getInstance().getAlarmUpdate(originalAlarms, updatedAlarms);
        if (false == alarmUpdates.isEmpty()) {
            // TODO distinct alarm update
            storage.getAlarmStorage().deleteAlarms(objectID, calendarUser.getId());
            if (null != updatedAlarms) {
                storage.getAlarmStorage().insertAlarms(objectID, calendarUser.getId(), updatedAlarms);
            }
            List<Alarm> newAlarms = storage.getAlarmStorage().loadAlarms(objectID, calendarUser.getId());
            return AlarmMapper.getInstance().getAlarmUpdate(originalAlarms, newAlarms);
        }
        return alarmUpdates;
    }

}
