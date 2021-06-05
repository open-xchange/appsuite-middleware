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

package com.openexchange.chronos.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmTriggerStorage} is a storage for alarm triggers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface AlarmTriggerStorage {

    /**
     * Calculates and inserts any pending triggers for the alarms of multiple users of a specific event.
     *
     * @param event The event to insert triggers for
     * @param alarmsPerUserId The alarms associated with the event, mapped to the corresponding entity identifiers of attending users
     */
    void insertTriggers(Event event, Map<Integer, List<Alarm>> alarmsPerUserId) throws OXException;

    /**
     * Inserts all necessary triggers for the given alarm objects.
     * This method adds alarm trigger objects as a batch operation and does some additional performance optimizations.
     *
     * @param alarmsPerAttendee A map of alarms per user per event.
     * @param event A list of events.
     */
    void insertTriggers(Map<String, Map<Integer, List<Alarm>>> alarms, List<Event> events) throws OXException;

    /**
     * Removes all existing triggers for the given event
     *
     * @param eventId The event id
     * @throws OXException
     */
    void deleteTriggers(String eventId) throws OXException;

    /**
     * Deletes any existing triggers for multiple events.
     *
     * @param eventIds The identifiers of the events to delete the triggers for
     * @throws OXException
     */
    void deleteTriggers(List<String> eventIds) throws OXException;

    /**
     * Removes the triggers for the given alarm ids
     *
     * @param alarmIds The alarm ids
     * @throws OXException
     */
    void deleteTriggersById(List<Integer> alarmIds) throws OXException;


    /**
     * Deletes any existing triggers of a specific user for multiple events.
     *
     * @param eventIds The identifiers of the events to delete the triggers for
     * @param userId The identifier of the user to delete the triggers for
     */
    void deleteTriggers(List<String> eventIds, int userId) throws OXException;

    /**
     * Deletes all existing triggers for any alarm stored for a specific user.
     *
     * @param userId The identifier of the user to delete the triggers for
     */
    void deleteTriggers(int userId) throws OXException;

    /**
     * Deletes all existing alarm triggers for an account.
     *
     * @return <code>true</code> if something was actually deleted, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean deleteAllTriggers() throws OXException;

    /**
     * Retrieves all not acknowledged alarm triggers for the given user with a trigger time within the given limit.
     *
     * @param userId The user id
     * @param rangeFrom The lower (inclusive) boundary of the requested time range, or <code>null</code> if not limited.
     *            Ignored for triggers of recurring event series.
     * @param rangeUntil The upper (exclusive) boundary of the requested time range, or <code>null</code> if not limited
     * @return A list of {@link AlarmTrigger}s
     * @throws OXException
     */
    List<AlarmTrigger> loadTriggers(int userId, Date rangeFrom, Date rangeUntil) throws OXException;

    /**
     * Retrieves the given trigger
     *
     * @param id The alarm id
     * @return The {@link AlarmTrigger} or null
     * @throws OXException
     */
    AlarmTrigger loadTrigger(int id) throws OXException;


    /**
     * Recalculates the trigger time for floating events. E.g. to adapt to a timezone change of the user.
     *
     * @param userId The user id
     * @return The number of changed alarm triggers
     * @throws OXException
     */
    Integer recalculateFloatingAlarmTriggers(int userId) throws OXException;

    /**
     * Loads information about which events have at least one alarm trigger for the user in the storage.
     *
     * @param userId The identifier of the user to get the trigger information for
     * @param eventIds The identifiers of the event to get the trigger information for
     * @return A set holding the identifiers of those events where at least one alarm trigger is stored for the user
     */
    Set<String> hasTriggers(int userId, String[] eventIds) throws OXException;

}
