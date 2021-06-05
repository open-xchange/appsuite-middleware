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

package com.openexchange.chronos.service;

import java.util.List;
import java.util.Map;
import com.openexchange.session.Session;

/**
 * {@link CalendarEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarEvent extends TimestampedResult {

    /**
     * Gets the identifier of the context the event originates in.
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the identifier of the calendar account the event originates in.
     *
     * @return The account identifier
     */
    int getAccountId();

    /**
     * Gets the the actual target calendar user based on the folder view the action has been performed in. This is either the current
     * session's user when operating in <i>private</i> or <i>public</i> folders, or the folder owner for <i>shared</i> calendar folders.
     *
     * @return The identifier of the actual calendar user, or <code>-1</code> if not available
     */
    int getCalendarUser();

    /**
     * Gets the client session if available.
     *
     * @return The session, or <code>null</code> if not available
     */
    Session getSession();

    /**
     * Gets the entity resolver if available.
     *
     * @return The entity resolver, or <code>null</code> if not available
     */
    EntityResolver getEntityResolver();

    /**
     * Gets a map holding the identifiers of all folders that are affected by the event, associated to the corresponding user identifier.
     *
     * @return The identifiers of the affected folders for each user
     */
    Map<Integer, List<String>> getAffectedFoldersPerUser();

    /**
     * Gets the delete results.
     *
     * @return The delete results, or an empty list if there are none
     */
    List<DeleteResult> getDeletions();

    /**
     * Gets the update results.
     *
     * @return The update results, or an empty list if there are none
     */
    List<UpdateResult> getUpdates();

    /**
     * Gets the create results.
     *
     * @return The create results, or an empty list if there are none
     */
    List<CreateResult> getCreations();

    /**
     * Gets the {@link CalendarParameters}.
     *
     * @return The {@link CalendarParameters} or <code>null</code> if not available
     */
    CalendarParameters getCalendarParameters();

}
