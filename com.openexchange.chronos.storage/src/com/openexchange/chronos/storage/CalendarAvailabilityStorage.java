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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;

/**
 * {@link CalendarAvailabilityStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CalendarAvailabilityStorage {

    /**
     * Generates the next unique identifier for inserting new {@link CalendarAvailability} data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next unique event identifier
     * @throws OXException if the next identifier cannot be generated or any other error is occurred
     */
    String nextCalendarAvailabilityId() throws OXException;

    /**
     * Generates the next unique identifier for inserting new {@link CalendarFreeSlot} data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next unique event identifier
     * @throws OXException if the next identifier cannot be generated or any other error is occurred
     */
    String nextCalendarFreeSlotId() throws OXException;

    /**
     * Inserts the specified {@link CalendarAvailability} block to the storage
     * 
     * @param calendarAvailability The {@link CalendarAvailability} to insert
     * @throws OXException if the object cannot be inserted or any other error is occurred
     */
    void insertCalendarAvailability(CalendarAvailability calendarAvailability) throws OXException;

    /**
     * Inserts a the specified {@link List} of {@link CalendarAvailability} objects to the storage
     * 
     * @param calendarAvailabilities The {@link List} with the {@link CalendarAvailability} objects
     * @throws OXException if the objects cannot be inserted or any other error is occurred
     */
    void insertCalendarAvailabilities(List<CalendarAvailability> calendarAvailabilities) throws OXException;

    /**
     * Inserts the specified {@link CalendarFreeSlot} for the specified calendar availability to the storage
     * 
     * @param calendarAvailabilityId the {@link CalendarAvailability} identifier
     * @param freeSlot The {@link CalendarFreeSlot} to insert
     * @throws OXException if the object cannot be inserted to the storage or any other error is occurred
     */
    void insertCalendarFreeSlot(String calendarAvailabilityId, CalendarFreeSlot freeSlot) throws OXException;

    /**
     * Loads from the storage the {@link CalendarAvailability} with the specified identifier
     * 
     * @param calendarAvailabilityId The calendar availability identifier
     * @return The {@link CalendarAvailability}
     * @throws OXException if an error is occurred
     */
    CalendarAvailability loadCalendarAvailability(String calendarAvailabilityId) throws OXException;

    /**
     * Loads the {@link CalendarAvailability} information for the specified {@link Attendee}s in the specified interval.
     * 
     * @param attendees The {@link List} of {@link Attendee}s
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return A {@link Map} with the {@link CalendarAvailability} for each {@link Attendee}
     * @throws OXException if the items cannot be retrieved
     */
    Map<Attendee, List<CalendarAvailability>> loadAttendeeCalendarAvailability(List<Attendee> attendees, Date from, Date until) throws OXException;
    
    Map<User, List<CalendarAvailability>> loadUserCalendarAvailability(List<User> users, Date from, Date until) throws OXException;

    /**
     * Load all {@link CalendarAvailability} blocks for the specified user
     * 
     * @param userId The user identifier
     * @return A {@link List} with all the {@link CalendarAvailability} objects for the user
     * @throws OXException if an error is occurred
     */
    List<CalendarAvailability> loadCalendarAvailabilities(int userId) throws OXException;

    /**
     * Loads the {@link CalendarAvailability} blocks for the specified user in the specified interval
     * 
     * @param userId The user identifier
     * @param from The starting point in time
     * @param until The ending point in time
     * @return A {@link List} with all {@link CalendarAvailability} blocks or an empty {@link List}
     * @throws OXException if the items cannot be retrieved or any other error is occurred
     */
    List<CalendarAvailability> loadCalenarAvailabilityInRange(int userId, Date from, Date until) throws OXException;

    /**
     * Loads from the storage the {@link CalendarFreeSlot}s for the {@link CalendarAvailability}
     * with the specified identifier
     * 
     * @param calendarAvailabilityId The calendar availability identifier
     * @return A {@link List} with all {@link CalendarFreeSlot}s bound to the {@link CalendarAvailability}
     *         with the specified id
     * @throws OXException if an error is occurred
     */
    List<CalendarFreeSlot> loadCalendarFreeSlots(String calendarAvailabilityId) throws OXException;

    /**
     * Loads from the storage the {@link CalendarFreeSlot} with the specified identifier bound
     * to the {@link CalendarAvailability} with the specified identifier
     * 
     * @param calendarAvailabilityId The calendar availability identifier
     * @param freeSlotId The free slot identifier
     * @return The {@link CalendarFreeSlot}
     * @throws OXException if an error is occurred
     */
    CalendarFreeSlot loadCalendarFreeSlot(String calendarAvailability, String freeSlotId) throws OXException;

    /**
     * Deletes the {@link CalendarAvailability} and all free slots associated to it with the specified identifier
     * 
     * @param calendarAvailabilityId The calendar availability identifier
     * @throws OXException if the object cannot be deleted
     */
    void deleteCalendarAvailability(String calendarAvailabilityId) throws OXException;

    /**
     * Deletes the {@link CalendarAvailability} blocks and all free slots associated to them
     * 
     * @param calendarAvailabilityIds The calendar availability identifiers
     * @throws OXException if the objects cannot be deleted
     */
    void deleteCalendarAvailabilities(List<String> calendarAvailabilityIds) throws OXException;

    /**
     * Purges all calendar availability blocks and free slots for the specified user
     * 
     * @throws OXException if the objects cannot be purged
     */
    void purgeCalendarAvailabilities(int userId) throws OXException;
}
