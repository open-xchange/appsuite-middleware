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
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Conference;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link ConferenceStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public interface ConferenceStorage {

    /**
     * Generates the next unique identifier for inserting new conference data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next unique conference identifier
     */
    int nextId() throws OXException;

    /**
     * Loads all conferences for a specific event.
     *
     * @param eventId The identifier of the event to load the conferences for
     * @return The conferences
     */
    List<Conference> loadConferences(String eventId) throws OXException;

    /**
     * Loads the conferences for specific events.
     *
     * @param eventIds The identifiers of the events to load the conferences for
     * @return The conferences, mapped to the identifiers of the corresponding events
     */
    Map<String, List<Conference>> loadConferences(String[] eventIds) throws OXException;

    /**
     * Loads information about which events have at least one conference in the storage.
     *
     * @param eventIds The identifiers of the event to get the conference information for
     * @return A set holding the identifiers of those events where at least one conference stored
     */
    Set<String> hasConferences(String[] eventIds) throws OXException;

    /**
     * Deletes all conferences for a specific event.
     *
     * @param eventId The identifier of the event to delete the conferences for
     */
    void deleteConferences(String eventId) throws OXException;

    /**
     * Deletes all conferences for multiple events.
     *
     * @param eventIds The identifiers of the events to delete the conferences for
     */
    void deleteConferences(List<String> eventIds) throws OXException;

    /**
     * Deletes multiple conferences for a specific event.
     *
     * @param eventId The identifier of the event to delete the conferences for
     * @param conferenceIds The identifiers of the conferences to delete
     */
    void deleteConferences(String eventId, int[] conferencesIds) throws OXException;

    /**
     * Deletes all existing conferences for an account.
     *
     * @return <code>true</code> if something was actually deleted, <code>false</code>, otherwise
     */
    boolean deleteAllConferences() throws OXException;

    /**
     * Inserts conferences for a specific event.
     *
     * @param eventId The identifier of the event to insert the conferences for
     * @param conferences The conferences to insert
     */
    void insertConferences(String eventId, List<Conference> conferences) throws OXException;

    /**
     * Updates conferences for a specific event.
     *
     * @param eventId The identifier of the event to update the conferences for
     * @param conferences The conferences to update
     */
    void updateConferences(String eventId, List<Conference> conferences) throws OXException;

}
