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
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarStorage {

    /**
     * Gets the event storage.
     *
     * @return The event storage
     */
    EventStorage getEventStorage();

    /**
     * Gets the alarm storage.
     *
     * @return The alarm storage
     */
    AlarmStorage getAlarmStorage();

    /**
     * Gets the attachment storage.
     *
     * @return The attachment storage
     */
    AttachmentStorage getAttachmentStorage();

    /**
     * Gets the attendee storage.
     *
     * @return The attendee storage
     */
    AttendeeStorage getAttendeeStorage();

    /**
     * Gets the alarm trigger storage.
     *
     * @return The alarm trigger storage
     */
    AlarmTriggerStorage getAlarmTriggerStorage();

    /**
     * Gets the conference storage.
     *
     * @return The conference storage
     */
    ConferenceStorage getConferenceStorage();

    /**
     * Gets the calendar account storage.
     *
     * @return The account storage
     */
    CalendarAccountStorage getAccountStorage();

    /**
     * Gets additional storage utilities.
     *
     * @return The storage utilities
     */
    CalendarStorageUtilities getUtilities();

    /**
     * Gets any tracked warnings that occurred when processing the stored data.
     *
     * @return The warnings, mapped to the associated event identifier, or an empty map if there are none
     */
    Map<String, List<OXException>> getWarnings();

    /**
     * Gets any tracked warnings that occurred when processing the stored data and flushes them, so that subsequent invocations would
     * return an empty map.
     *
     * @return The warnings, mapped to the associated event identifier, or an empty map if there are none
     */
    Map<String, List<OXException>> getAndFlushWarnings();

}
