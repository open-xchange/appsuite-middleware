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

package com.openexchange.chronos.impl;

import java.util.Date;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
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
        return new Date(new DateTime(year, month, day).getTimestamp());
    }

    /**
     * Creates a {@link CalendarFreeSlot} with the specified summary within the specified interval
     * 
     * @param summary The summary
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return The {@link CalendarFreeSlot}
     */
    static CalendarFreeSlot createCalendarFreeSlot(String summary, DateTime from, DateTime until) {
        CalendarFreeSlot cfs = new CalendarFreeSlot();
        cfs.setSummary(summary);
        cfs.setStartTime(from);
        cfs.setEndTime(until);
        return cfs;
    }

    /**
     * Creates a {@link CalendarAvailability} with the specified {@link BusyType} and the specified free slots
     * 
     * @param busyType The {@link BusyType} of the availability
     * @param freeSlots The free slots
     * @return The {@link CalendarAvailability}
     */
    static CalendarAvailability createCalendarAvailability(BusyType busyType, List<CalendarFreeSlot> freeSlots) {
        CalendarAvailability ca = new CalendarAvailability();
        ca.setBusyType(busyType);
        ca.setCalendarFreeSlots(freeSlots);
        return ca;
    }

    /**
     * Creates a {@link CalendarAvailability} with the specified {@link BusyType} and the specified free slots
     * 
     * @param busyType The {@link BusyType} of the availability
     * @param freeSlots The free slots
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return The {@link CalendarAvailability}
     */
    static CalendarAvailability createCalendarAvailability(BusyType busyType, List<CalendarFreeSlot> freeSlots, DateTime from, DateTime until) {
        CalendarAvailability ca = createCalendarAvailability(busyType, freeSlots);
        ca.setStartTime(from);
        ca.setEndTime(until);
        return ca;
    }

    static CalendarAvailability createCalendarAvailability(BusyType busyType, List<CalendarFreeSlot> freeSlots, DateTime from, DateTime until, int priority) {
        CalendarAvailability ca = createCalendarAvailability(busyType, freeSlots, from, until);
        ca.setPriority(priority);
        return ca;
    }
}
