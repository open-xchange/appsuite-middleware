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

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface AlarmStorage {

    /**
     * Generates the next unique identifier for inserting new alarm data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next unique alarm identifier
     */
    int nextId() throws OXException;

    /**
     * Inserts alarms for a specific user of an event.
     *
     * @param event The event the alarms are associated with
     * @param userID The identifier of the user the alarms should be inserted for
     * @param alarms The alarms to insert
     */
    void insertAlarms(Event event, int userID, List<Alarm> alarms) throws OXException;

    /**
     * Inserts alarms for multiple users of an event.
     *
     * @param event The event the alarms are associated with
     * @param alarmsByUserId The alarms to insert, mapped to the corresponding user identifier
     */
    void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException;

    /**
     * Inserts alarms for multiple users of multiple events.
     *
     * @param alarmsByUserByEventId The alarms to insert by user, mapped to the corresponding event identifier
     */
    void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException;

    /**
     * Loads all user alarms for the given event
     *
     * @param event The event
     * @return A map of alarms per user id
     * @throws OXException
     */
    Map<Integer, List<Alarm>> loadAlarms(Event event) throws OXException;

    /**
     * Loads the alarms of a given user for a given event.
     *
     * @param event The event
     * @param userID The user id
     * @return A list of alarms
     * @throws OXException
     */
    List<Alarm> loadAlarms(Event event, int userID) throws OXException;

    /**
     * Loads all alarms for the given user for the given events
     *
     * @param events The events
     * @param userID The user id
     * @return A map of a list of alarms per event id
     * @throws OXException
     */
    Map<String, List<Alarm>> loadAlarms(List<Event> events, int userID) throws OXException;

    /**
     * Loads all alarms for the given events
     *
     * @param events The events
     * @return A map of alarms per user id per event id
     * @throws OXException
     */
    Map<String, Map<Integer, List<Alarm>>> loadAlarms(List<Event> events) throws OXException;

    /**
     * Updates the alarms for a given event and user.
     *
     * @param event The event
     * @param userID The user id
     * @param alarms The updated alarms
     * @throws OXException
     */
    void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException;

    /**
     * Deletes all alarms stored for a specific event.
     *
     * @param eventId The identifier of the event to remove the alarms for
     */
    void deleteAlarms(String eventId) throws OXException;

    /**
     * Deletes all alarms stored for multiple events.
     *
     * @param eventIds The identifiers of the events to remove the alarms for
     */
    void deleteAlarms(List<String> eventIds) throws OXException;

    /**
     * Deletes all alarms stored for a specific user.
     *
     * @param userId The identifier of the user to delete the alarms for
     */
    void deleteAlarms(int userId) throws OXException;

    /**
     * Deletes all alarms for an account.
     *
     * @return <code>true</code> if something was actually deleted, <code>false</code>, otherwise
     */
    boolean deleteAllAlarms() throws OXException;

    /**
     * Deletes all alarms of a user stored for a specific event.
     *
     * @param eventId The identifier of the event to remove the alarms for
     * @param userId The identifier of the user to remove the alarms for
     */
    void deleteAlarms(String eventId, int userId) throws OXException;

    /**
     * Deletes all alarms of multiple users stored for a specific event.
     *
     * @param eventId The identifier of the event to remove the alarms for
     * @param userIds The identifiers of the users to remove the alarms for
     */
    void deleteAlarms(String eventId, int[] userIds) throws OXException;

    /**
     * Deletes one or multiple specific alarms of a user stored for a specific event.
     *
     * @param eventId The identifier of the event to remove the alarms for
     * @param userId The identifier of the user to remove the alarms for
     * @param alarmIds The identifiers of the alarms to remove
     */
    void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException;

    /**
     * Gets the alarm with the given id
     *
     * @param alarmId The alarm id
     * @return The alarm
     * @throws OXException
     */
    Alarm loadAlarm(int alarmId) throws OXException;

    /**
     * Returns the latest timestamp of any alarm for the given user.
     *
     * @param userId The user identifier
     * @return The latest timestamp
     * @throws OXException 
     */
    long getLatestTimestamp(int userId) throws OXException;

    /**
     * Returns the latest timestamp of any alarm for the given user for the given event.
     *
     * @param eventId The event identifier
     * @param userId The user identifier
     * @return The latest timestamp
     * @throws OXException 
     */
    long getLatestTimestamp(String eventId, int userId) throws OXException;

    /**
     * Returns the latest timestamps of any alarm for the given user for the given events.
     *
     * @param eventId The event identifier
     * @param userId The user identifier
     * @return The latest timestamps of the events
     * @throws OXException 
     */
    Map<String, Long> getLatestTimestamp(List<String> eventIds, int userId) throws OXException;
}
