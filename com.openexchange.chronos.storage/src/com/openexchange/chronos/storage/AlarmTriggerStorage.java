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

package com.openexchange.chronos.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
     * Retrieves all not acknowledged alarm triggers for the given user with a trigger time earlier than the given limit.
     *
     * @param userId The user id
     * @param until An optional upper limit
     * @return A list of {@link AlarmTrigger}s
     * @throws OXException
     */
    List<AlarmTrigger> loadTriggers(int userId, Date until) throws OXException;

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
     * @return A map that associates the identifiers of those events where at least one alarm trigger is stored for the user to {@link Boolean#TRUE}
     */
    Map<String, Boolean> hasTriggers(int userId, String[] eventIds) throws OXException;

}
