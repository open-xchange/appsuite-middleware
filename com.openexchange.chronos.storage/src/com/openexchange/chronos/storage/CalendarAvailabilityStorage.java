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

import java.util.List;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAvailabilityStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CalendarAvailabilityStorage {

    /**
     * Generates the next unique identifier for inserting new {@link Available} data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialised using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favour of an externally controlled transaction.
     *
     * @return The next unique event identifier
     * @throws OXException if the next identifier cannot be generated or any other error is occurred
     */
    String nextAvailableId() throws OXException;

    /**
     * Inserts the specified {@link Availability} block to the storage
     * 
     * @param availability The {@link Availability} to insert
     * @throws OXException if the object cannot be inserted or any other error is occurred
     * @deprecated Use {@link #insertAvailable(List)} instead
     */
    void insertAvailability(Availability availability) throws OXException;

    /**
     * Inserts the specified {@link List} of {@link Availability} objects to the storage
     * 
     * @param availabilities The {@link List} with the {@link Availability} objects
     * @throws OXException if the objects cannot be inserted or any other error is occurred
     * @deprecated Use {@link #insertAvailable(List)} instead
     */
    void insertAvailabilities(List<Availability> availabilities) throws OXException;

    /**
     * Inserts the specified {@link Available} blocks
     * 
     * @param available The {@link Available} blocks to insert
     * @throws OXException if the objects cannot be inserted to the storage or any other error is occurred
     */
    void insertAvailable(List<Available> available) throws OXException;

    /**
     * Loads from the storage all {@link Available} blocks for the specified user
     * 
     * @param userId The user identifier
     * @return A {@link List} with all {@link Available} blocks
     * @throws OXException if the blocks cannot be loaded or any other error is occurred
     */
    List<Available> loadAvailable(int userId) throws OXException;

    /**
     * Loads from the storage all {@link Available} blocks for the specified users
     * 
     * @param userIds The user identifiers
     * @return A {@link List} with all {@link Available} blocks for the specified users
     * @throws OXException if the blocks cannot be loaded or any other error is occurred
     */
    List<Available> loadAvailable(List<Integer> userIds) throws OXException;

    /**
     * Loads from the storage the {@link Availability} with the specified identifier
     * 
     * @param availabilityId The calendar availability identifier
     * @return The {@link Availability}
     * @throws OXException if an error is occurred
     * @deprecated Use {@link #loadAvailable(int)} instead
     */
    Availability loadAvailability(String availabilityId) throws OXException;

    /**
     * Loads the {@link Availability} information for the users with the specified identifiers in the specified interval.
     * 
     * @param userIds The {@link List} of user identifiers
     * @return A {@link List} with the {@link Availability} for each user
     * @throws OXException if the items cannot be retrieved
     * @deprecated Use {@link #loadAvailable(List)} instead.
     */
    List<Availability> loadAvailabilities(List<Integer> userIds) throws OXException;

    /**
     * Load all {@link Availability} blocks for the specified user
     * 
     * @param userId The user identifier
     * @return A {@link List} with all the {@link Availability} objects for the user
     * @throws OXException if an error is occurred
     * @deprecated Use {@link #loadAvailable(int)} instead.
     */
    List<Availability> loadCalendarAvailabilities(int userId) throws OXException;

    /**
     * Loads from the storage the {@link Available}s for the {@link Availability}
     * with the specified identifier
     * 
     * @param availabilityId The calendar availability identifier
     * @return A {@link List} with all {@link Available}s bound to the {@link Availability}
     *         with the specified id
     * @throws OXException if an error is occurred
     * @deprecated Use {@link #loadAvailable(int)} instead
     */
    List<Available> loadAvailable(String availabilityId) throws OXException;

    /**
     * Loads from the storage the {@link Available} with the specified identifier bound
     * to the {@link Availability} with the specified identifier
     * 
     * @param calendarAvailabilityId The calendar availability identifier
     * @param availableId The {@link Available} identifier
     * @return The {@link Available}
     * @throws OXException if an error is occurred
     * @deprecated Use {@link #loadAvailable(int)} instead
     */
    Available loadAvailable(String availability, String availableId) throws OXException;

    /**
     * Deletes the {@link Availability} and all {@link Available} blocks associated to it with the specified identifier
     * 
     * @param availabilityId The calendar availability identifier
     * @throws OXException if the object cannot be deleted
     * @deprecated Use {@link #deleteAvailable(String)} instead.
     */
    void deleteAvailability(String availabilityId) throws OXException;

    /**
     * Deletes all {@link Available} blocks for the specified user
     * 
     * @param userId The user identifier
     * @throws OXException if the {@link Available} blocks cannot be deleted
     */
    void deleteAvailable(int userId) throws OXException;

    /**
     * Deletes the {@link Available} block with the specified unique identifier
     * 
     * @param availableUid The {@link Available} unique identifier
     * @throws OXException if the {@link Available} block cannot be deleted
     */
    void deleteAvailable(String availableUid) throws OXException;

    /**
     * Deletes the {@link Available} block with the specified identifier
     * 
     * @param availableId The {@link Available} identifier
     * @throws OXException if the {@link Available} block cannot be deleted
     */
    void deleteAvailable(int userId, int availableId) throws OXException;

    /**
     * Deletes from the storage all {@link Available} blocks with the specified unique identifiers
     * 
     * @param availableIds The {@link Available} unique identifiers
     * @throws OXException if the blocks cannot be deleted
     */
    void deleteAvailableByUid(List<String> availableIds) throws OXException;

    /**
     * Deletes from the storage all {@link Available} blocks with the specified identifiers
     * 
     * @param availableIds The {@link Available} identifiers
     * @throws OXException if the blocks cannot be deleted
     */
    void deleteAvailableById(List<Integer> availableIds) throws OXException;

    /**
     * Deletes from the storage all {@link Available} blocks for the specified users
     * 
     * @param userIds The user identifiers
     * @throws OXException if the blocks cannot be deleted
     */
    void deleteAvailableByUserId(List<Integer> userIds) throws OXException;

    /**
     * Deletes the {@link Availability} blocks and all {@link Available} blocks associated to them
     * 
     * @param availabilityIds The calendar availability identifiers
     * @throws OXException if the objects cannot be deleted
     * @deprecated Use {@link #deleteAvailableByUid(List)}
     */
    void deleteAvailabilities(List<String> availabilityIds) throws OXException;

    /**
     * Purges all {@link Availability} blocks and {@link Available} blocks for the specified user
     * 
     * @throws OXException if the objects cannot be purged
     * @deprecated Use {@link #deleteAvailable(int)} instead.
     */
    void purgeAvailabilities(int userId) throws OXException;
}
