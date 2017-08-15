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

package com.openexchange.chronos.provider.caching.internal.handler;

import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.exception.OXException;

/**
 * The {@link CachingHandler} defines the general caching workflow that will be invoked for each request.<br>
 * <br>
 * There should be one implementation for each available {@link ProcessingType} that will be returned from the {@link CachingHandlerFactory}.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public interface CachingHandler {

    /**
     * Returns a list of {@link Event}s for the underlying account
     * 
     * @param folderId The folder id of the account
     * @return A list of {@link Event}s
     * @throws OXException
     */
    List<Event> getExternalEvents(String folderId) throws OXException;

    /**
     * Returns the currently persisted {@link Event}s identified by the given folder identifier
     * 
     * @param folderId The folder identifier to get {@link Event}s for
     * @return A list of {@link Event}s
     * @throws OXException
     */
    List<Event> getPersistedEvents(String folderId) throws OXException;

    /**
     * Persists the given {@link EventUpdates}
     * 
     * @param diff The {@link EventUpdate} diff to persist
     * @throws OXException
     */
    void persist(EventUpdates diff) throws OXException;

    /**
     * Returns the requested events (if available)
     * 
     * @param eventIds The requested {@link Event}s
     * @return A list of the requested {@link Event}s if available
     * @throws OXException
     */
    List<Event> search(List<EventID> eventIds) throws OXException;

    /**
     * Returns the events for given folder (if available)
     * 
     * @param folderId The folder containing the {@link Event}s
     * @return A list of the requested {@link Event}s if available
     * @throws OXException
     */
    List<Event> search(String folderId) throws OXException;

    /**
     * Returns a dedicated {@link Event} (if available)
     * 
     * @param folderId The folder id of the event
     * @param eventId The event id
     * @param recurrenceId The recurrence id of the {@link Event}
     * @return The desired {@link Event}
     * @throws OXException
     */
    Event search(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException;

    /**
     * Allows to handle errors (cleanups) if an {@link OXException} occurred
     * 
     * @param e The occurred {@link OXException}
     * @throws OXException
     */
    void handleExceptions(OXException e) throws OXException;

    /**
     * Updates the last modified timestamp of the account
     * 
     * @throws OXException
     */
    void updateLastUpdated() throws OXException;
}
