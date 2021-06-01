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

package com.openexchange.chronos.impl;

import java.util.Date;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.FbType;
import com.openexchange.chronos.FreeBusyTime;

/**
 * {@link FreeBusyTimeFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class PropsFactory {

    /**
     * Creates an {@link Attendee} with the specified e-mail address
     * 
     * @param mail the e-mail address of the {@link Attendee}
     * @return The new {@link Attendee}
     */
    static Attendee createAttendee(String mail) {
        Attendee attendee = new Attendee();
        attendee.setEMail(mail);
        return attendee;
    }

    /**
     * Creates a {@link FreeBusyTime} with the specified {@link FbType} with in the
     * specified interval
     * 
     * @param fbType The {@link FbType}
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return The new {@link FreeBusyTime}
     */
    static FreeBusyTime createFreeBusyTime(FbType fbType, Date from, Date until) {
        return new FreeBusyTime(fbType, from, until);
    }

    /**
     * Creates a {@link Date} with the specified year, month, day
     * 
     * @param year The year
     * @param month The month
     * @param day the day
     * @return The new {@link Date}
     */
    static Date createDate(int year, int month, int day) {
        return new Date(createDateTime(year, month, day).getTimestamp());
    }

    /**
     * Creates a {@link DateTime} with the specified year, month, day
     * 
     * @param year The year
     * @param month The month
     * @param day the day
     * @return The new {@link Date}
     */
    static DateTime createDateTime(int year, int month, int day) {
        return new DateTime(year, month, day);
    }

    /**
     * Creates a {@link Available} with the specified summary within the specified interval
     * 
     * @param summary The summary
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return The {@link Available}
     */
    static Available createCalendarAvailable(String summary, DateTime from, DateTime until) {
        Available cfs = new Available();
        cfs.setSummary(summary);
        cfs.setStartTime(from);
        cfs.setEndTime(until);
        return cfs;
    }

    /**
     * Creates a recurring {@link Available} with the specified summary with in the specified interval
     * 
     * @param summary The summary
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @param recurrenceRule The recurrence rule as string
     * @return The recurring {@link Available}
     */
    static Available createRecurringCalendarFreeSlot(String summary, DateTime from, DateTime until, String recurrenceRule) {
        Available cfs = createCalendarAvailable(summary, from, until);
        cfs.setRecurrenceRule(recurrenceRule);
        return cfs;
    }

    /**
     * Creates a {@link Availability} with the specified {@link BusyType} and the specified free slots
     * 
     * @param busyType The {@link BusyType} of the availability
     * @param freeSlots The free slots
     * @return The {@link Availability}
     */
    static Availability createCalendarAvailability(BusyType busyType, List<Available> freeSlots) {
        Availability ca = new Availability();
        ca.setBusyType(busyType);
        ca.setAvailable(freeSlots);
        return ca;
    }

    /**
     * Creates a {@link Availability} with the specified {@link BusyType} and the specified free slots
     * 
     * @param busyType The {@link BusyType} of the availability
     * @param freeSlots The free slots
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return The {@link Availability}
     */
    static Availability createCalendarAvailability(BusyType busyType, List<Available> freeSlots, DateTime from, DateTime until) {
        Availability ca = createCalendarAvailability(busyType, freeSlots);
        ca.setStartTime(from);
        ca.setEndTime(until);
        return ca;
    }

    /**
     * Creates a {@link Availability} with in the specified interval,
     * with the specified {@link BusyType}, the specified free slots and the specified priority
     * 
     * @param busyType The {@link BusyType}
     * @param freeSlots The free slots
     * @param from The starting point of the {@link Availability}'s interval
     * @param until The ending point of the {@link Availability}'s interval
     * @param priority the priority of the {@link Availability}
     * @return The new {@link Availability}
     */
    static Availability createCalendarAvailability(BusyType busyType, List<Available> freeSlots, DateTime from, DateTime until, int priority) {
        Availability ca = createCalendarAvailability(busyType, freeSlots, from, until);
        ca.setPriority(priority);
        return ca;
    }
}
