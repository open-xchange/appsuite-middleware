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

import java.util.List;
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
     * Sets the {@link Available} blocks for the current user and removes the old ones.
     * 
     * @param available The {@link Available} blocks to set
     * @throws OXException if the old objects cannot be deleted from the storage and/or if the new objects
     *             cannot be inserted to the storage, or any other error is occurred
     */
    void setAvailable(int userId, List<Available> available) throws OXException;

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
}
