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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;

/**
 * {@link CalendarSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarSession extends CalendarParameters {

    /**
     * Gets the underlying server session.
     *
     * @return The underlying server session
     */
    Session getSession();

    /**
     * Gets host data information about the underlying HTTP request.
     *
     * @return The host data
     */
    HostData getHostData();

    /**
     * Gets the session's user identifier.
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the session's context identifier.
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the entity resolver.
     *
     * @return The entity resolver
     */
    EntityResolver getEntityResolver();

    /**
     * Gets a reference to the calendar service.
     *
     * @return The calendar service
     */
    CalendarService getCalendarService();

    /**
     * Gets a reference to the free/busy service.
     *
     * @return The free/busy service
     */
    FreeBusyService getFreeBusyService();

    /**
     * Gets a reference to the recurrence service.
     *
     * @return The recurrence service
     */
    RecurrenceService getRecurrenceService();

    /**
     * Provides access to additional calendar utilities.
     *
     * @return The utilities
     */
    CalendarUtilities getUtilities();

    /**
     * Provides access to several calendar-related configuration settings.
     *
     * @return The calendar config
     */
    CalendarConfig getConfig();

    /**
     * Adds a warning.
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Gets a list of warnings that occurred while processing.
     *
     * @return The warnings, or <code>null</code> or an empty list if there were none
     */
    List<OXException> getWarnings();

}
