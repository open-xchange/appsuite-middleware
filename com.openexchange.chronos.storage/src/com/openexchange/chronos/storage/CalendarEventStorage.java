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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link CalendarEventStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarEventStorage {

    /**
     * Generates the next object unique identifier for inserting new event data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next object identifier
     */
    String nextEventId() throws OXException;

    /**
     * Inserts a new event into the database.
     *
     * @param event The event to insert
     */
    void insertEvent(Event event) throws OXException;

    /**
     * Updates an existing event.
     *
     * @param event The event data to update
     */
    void updateEvent(Event event) throws OXException;

    /**
     * Deletes an existing event.
     *
     * @param objectID The identifier of the event to delete
     */
    void deleteEvent(String objectID) throws OXException;

    /**
     * Loads a specific event.
     *
     * @param objectID The object identifier of the event to load
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The event
     */
    Event loadEvent(String objectID, EventField[] fields) throws OXException;

    /**
     * Searches for events.
     *
     * @param searchTerm The search term to use
     * @param sortOptions The sort options to apply, or <code>null</code> if not specified
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The found events
     */
    List<Event> searchEvents(SearchTerm<?> searchTerm, SearchOptions sortOptions, EventField[] fields) throws OXException;

}
