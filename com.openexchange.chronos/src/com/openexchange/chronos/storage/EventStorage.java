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

package com.openexchange.chronos.storage;

import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link EventStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface EventStorage {

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
     * @param sortOptions The sort options to apply, or <code>null</code> if not specified
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The found events
     */
    List<Event> searchEvents(SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws OXException;

    /**
     * Searches for previously deleted events.
     *
     * @param searchTerm The search term to use
     * @param sortOptions The sort options to apply, or <code>null</code> if not specified
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to query all available data
     * @return The found events
     */
    List<Event> searchDeletedEvents(SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws OXException;

    /**
     * Generates the next object unique identifier for inserting new event data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
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

}
