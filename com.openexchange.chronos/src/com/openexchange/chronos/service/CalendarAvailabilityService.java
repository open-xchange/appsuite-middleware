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

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAvailabilityService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CalendarAvailabilityService {

    /**
     * Sets the availability for the specified user. Any existing {@link Availability} blocks will be overriden
     * 
     * @param session The {@link CalendarSession}
     * @param availability The {@link Availability} to set
     * @return A {@link SetResult} with the unique identifier of the {@link Availability} block and any
     *         warnings that occurred during the operation
     * @throws OXException if the availability cannot be set
     */
    void setAvailability(CalendarSession session, Availability availability) throws OXException;

    /**
     * Gets the {@link Availability} for the current user
     * 
     * @param session the {@link CalendarSession}
     * @return The {@link Availability} block
     * @throws OXException if an error is occurred
     */
    Availability getAvailability(CalendarSession session) throws OXException;

    /**
     * Gets the combined {@link Availability} blocks for the specified {@link Attendee}s in the specified time interval,
     * 
     * @param session The calendar session
     * @param attendees The {@link List} with the {@link Attendee}s to retrieve the {@link Availability} for
     * @param from The start point in the time interval
     * @param until The end point in the time interval
     * @return A {@link Map} with {@link Availability} slots for the {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    Map<Attendee, Availability> getCombinedAvailability(CalendarSession session, List<Attendee> attendees, Date from, Date until) throws OXException;

    /**
     * Gets the {@link Availability} information for the specified {@link CalendarUser}s in the specified
     * interval.
     * 
     * @param session The {@link CalendarSession}
     * @param attendees The {@link List} of the {@link CalendarUser}s
     * @param from The start date of the interval
     * @param until The end date of the interval
     * @return An unmodifiable {@link Map} with {@link Availability} for the specified {@link CalendarUser}s
     * @throws OXException if an error is occurred
     */
    Map<CalendarUser, Availability> getUserAvailability(CalendarSession session, List<CalendarUser> users, Date from, Date until) throws OXException;

    /**
     * Gets the {@link Availability} information for the specified {@link Attendee}s in the specified
     * interval.
     * 
     * @param session The {@link CalendarSession}
     * @param attendees The {@link List} of the {@link Attendee}s
     * @param from The start date of the interval
     * @param until The end date of the interval
     * @return An unmodifiable {@link Map} with {@link Availability} for the specified {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    Map<Attendee, Availability> getAttendeeAvailability(CalendarSession session, List<Attendee> attendees, Date from, Date until) throws OXException;

    /**
     * Deletes the {@link Availability} of the current user
     * 
     * @param session The {@link CalendarSession}
     * @throws OXException if the {@link Availability} cannot be deleted
     */
    void deleteAvailability(CalendarSession session) throws OXException;

    /**
     * Deletes the {@link Available} blocks with the specified unique identifiers
     * 
     * @param session The {@link CalendarSession}
     * @param availableUids A {@link List} with {@link Available} unique identifiers
     * @throws OXException if the {@link Availability} blocks cannot be deleted
     */
    void deleteAvailablesByUid(CalendarSession session, List<String> availableUids) throws OXException;

    /**
     * Deletes the {@link Available} blocks with the specified identifiers
     * 
     * @param session The {@link CalendarSession}
     * @param availableUids A {@link List} with {@link Available} identifiers
     * @throws OXException if the {@link Availability} blocks cannot be deleted
     */
    void deleteAvailablesById(CalendarSession session, List<Integer> availableIds) throws OXException;

    /**
     * Purges all {@link Availability} blocks for the specified user
     * 
     * @param session The {@link CalendarSession}
     * @throws OXException if the {@link Availability} blocks cannot be purged or any other error is occurred
     */
    void purgeAvailabilities(CalendarSession session) throws OXException;
}
