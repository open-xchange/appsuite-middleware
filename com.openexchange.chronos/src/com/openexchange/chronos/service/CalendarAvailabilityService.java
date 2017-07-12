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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAvailabilityService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CalendarAvailabilityService {

    /**
     * Sets the availability for the specified user. If any of the specified {@link CalendarAvailability} overlaps
     * with any of the existing {@link CalendarAvailability} both blocks will be stored
     * 
     * @param session The {@link CalendarSession}
     * @param availability A list with {@link CalendarAvailability} groupings to set
     * @return A {@link SetResult} with the unique identifiers of the {@link CalendarAvailability} blocks and any
     *         warnings that occurred during the operation
     * @throws OXException if the availability cannot be set
     */
    SetResult setAvailability(CalendarSession session, List<CalendarAvailability> availability) throws OXException;

    /**
     * Gets the {@link CalendarAvailability} for the current user
     * 
     * @param session the {@link CalendarSession}
     * @return An unmodifiable {@link List} with {@link CalendarAvailability} blocks
     * @throws OXException if an error is occurred
     */
    List<CalendarAvailability> getAvailability(CalendarSession session) throws OXException;

    /**
     * Gets the {@link CalendarAvailability} for the current user in the specified interval
     * 
     * @param session The {@link CalendarSession}
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return An unmodifiable {@link List} with {@link CalendarAvailability} blocks for the specified user
     * @throws OXException if an error is occurred
     */
    List<CalendarAvailability> getAvailability(CalendarSession session, Date from, Date until) throws OXException;

    /**
     * Gets the {@link CalendarAvailability} information for the specified {@link Attendee}s in the specified
     * interval.
     * 
     * @param session The {@link CalendarSession}
     * @param attendees The {@link List} of the {@link Attendee}s
     * @param from The start date of the interval
     * @param until The end date of the interval
     * @return An unmodifiable {@link Map} with {@link CalendarAvailability} for the specified {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    Map<Attendee, List<CalendarAvailability>> getAvailability(CalendarSession session, List<Attendee> attendees, Date from, Date until) throws OXException;

    /**
     * Deletes the {@link CalendarAvailability} block with the specified identifier
     * 
     * @param session The {@link CalendarSession}
     * @param availabilityId The {@link CalendarAvailability} unique identifier
     * @throws OXException if the {@link CalendarAvailability} cannot be deleted
     */
    void deleteAvailability(CalendarSession session, String availabilityId) throws OXException;

    /**
     * Deletes the {@link CalendarAvailability} blocks with the specified identifiers
     * 
     * @param session The {@link CalendarSession}
     * @param availabilityIds A {@link List} with the {@link CalendarAvailability} identifiers to delete
     * @throws OXException if the {@link CalendarAvailability} blocks cannot be deleted
     */
    void deleteAvailabilities(CalendarSession session, List<String> availabilityIds) throws OXException;

    /**
     * Deletes the {@link CalendarAvailability} blocks within the specified time range
     * 
     * @param session The {@link CalendarSession}
     * @param from The start date of the interval
     * @param until the end date of the interval
     * @throws OXException if any error is occurred
     */
    void deleteAvailabilities(CalendarSession session, Date from, Date until) throws OXException;

    /**
     * Purges all {@link CalendarAvailability} blocks for the specified user
     * 
     * @param session The {@link CalendarSession}
     * @throws OXException if the {@link CalendarAvailability} blocks cannot be purged or any other error is occurred
     */
    void purgeAvailabilities(CalendarSession session) throws OXException;
}
