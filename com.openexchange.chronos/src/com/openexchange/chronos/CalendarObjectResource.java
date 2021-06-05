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

package com.openexchange.chronos;

import java.util.Date;
import java.util.List;
import com.openexchange.annotation.Nullable;

/**
 * {@link CalendarObjectResource}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface CalendarObjectResource {
    
    /**
     * Gets the common unique identifier of this calendar object resource.
     * 
     * @return The unique identifier
     */
    String getUid();
    
    /**
     * Gets the common organizer of this calendar object resource.
     * 
     * @return The organizer, or <code>null</code> if not set
     */
    @Nullable
    Organizer getOrganizer();
    
    /**
     * Gets all events in this calendar object resource.
     * 
     * @return The events
     */
    List<Event> getEvents();

    /**
     * Gets the series master event in case it is available in this calendar object resource. 
     * 
     * @return The series master event, or <code>null</code> if not available
     */
    @Nullable
    Event getSeriesMaster();

    /**
     * Gets all overridden instances / change exceptions contained in this calendar object resource. 
     * 
     * @return The change exceptions, or an empty list if there are none
     */
    List<Event> getChangeExceptions();

    /**
     * Gets the <i>first</i> event in this calendar object resource.
     * 
     * @return The first event
     */
    Event getFirstEvent();

    /**
     * Gets an overridden instance of this calendar object resource with a specific recurrence identifier.
     * 
     * @param recurrenceId The recurrence identifier of the overridden instance to lookup
     * @return The matching overridden instance / change exception event, or <code>null</code> if not found
     */
    Event getChangeException(RecurrenceId recurrenceId);

    /**
     * Gets the (maximum) timestamp of all contained events in this calendar object resource.
     * 
     * @return The timestamp
     */
    Date getTimestamp();

}
