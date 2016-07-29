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

package com.openexchange.chronos;

import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarStorage {

    /**
     * Loads a specific event.
     *
     * @param objectID The object identifier of the event to load
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The event
     */
    Event loadEvent(int objectID, EventField[] fields) throws OXException;

    /**
     * Searches for events.
     *
     * @param searchTerm The search term to use
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The found events
     */
    List<Event> searchEvents(SearchTerm<?> searchTerm, EventField[] fields) throws OXException;

    /**
     * Searches for previously deleted events.
     *
     * @param searchTerm The search term to use
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The found events
     */
    List<Event> searchDeletedEvents(SearchTerm<?> searchTerm, EventField[] fields) throws OXException;

    /**
     * Loads events located in a specific folder.
     *
     * @param folderID The identifier of the folder to load the events from
     * @param from The minimum (inclusive) end time of the events, or <code>null</code> for no restrictions
     * @param until The maximum (exclusive) start time of the events, or <code>null</code> for no restrictions
     * @param createdBy The identifier of the event's creator, or <code>-1</code> for no restrictions
     * @param updatedSince The minimum (exclusive) last modification time of the events, or <code>null</code> for no restrictions
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The events
     */
    //    List<Event> loadEventsInFolder(int folderID, Date from, Date until, int createdBy, Date updatedSince, EventField[] fields) throws OXException;

    /**
     * Loads deleted events previously located in a specific folder.
     *
     * @param folderID The identifier of the folder to load the events from
     * @param from The minimum (inclusive) end time of the events, or <code>null</code> for no restrictions
     * @param until The maximum (exclusive) start time of the events, or <code>null</code> for no restrictions
     * @param createdBy The identifier of the event's creator, or <code>-1</code> for no restrictions
     * @param deletedSince The minimum (exclusive) last modification time of the events, or <code>null</code> for no restrictions
     * @return The events
     */
    //    List<Event> loadDeletedEventsInFolder(int folderID, Date from, Date until, int createdBy, Date deletedSince) throws OXException;

    //    List<Event> loadEventsOfUser(int userID, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException;

    //    List<Event> loadDeletedEventsOfUser(int userID, Date from, Date until, Date deletedSince) throws OXException;

    /**
     * Generates the next object unique identifier for inserting new event data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS}.
     *
     * @return The next object identifier
     */
    int nextObjectID() throws OXException;

    /**
     * Inserts a new event into the database.
     *
     * @param event The event to insert
     */
    void insertEvent(Event event) throws OXException;

    void updateEvent(Event event) throws OXException;

    void deleteEvent(int objectID) throws OXException;

    void insertTombstoneEvent(Event event) throws OXException;

    void insertAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException;

    List<Alarm> loadAlarms(int objectID, int userID) throws OXException;

    Map<Integer, List<Alarm>> loadAlarms(int[] objectIDs, int userID) throws OXException;

    void deleteAlarms(int objectID, int userID) throws OXException;

    void updateAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException;

    void deleteAlarms(int objectID) throws OXException;

}
