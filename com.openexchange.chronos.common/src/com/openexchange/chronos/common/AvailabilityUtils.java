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

package com.openexchange.chronos.common;

import java.util.Date;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.AvailableTimeSlot;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.FbType;
import com.openexchange.chronos.FreeBusyTime;

/**
 * {@link AvailabilityUtils}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class AvailabilityUtils {

    /**
     * Checks if the {@link DateTime} interval of the specified {@link CalendarAvailability} A precedes and intersects with the
     * {@link DateTime} interval of the specified {@link CalendarAvailability} interval B
     * 
     * @param a The {@link CalendarAvailability} A
     * @param b The {@link CalendarAvailability} B
     * @return <code>true</code> if the {@link DateTime} interval of the specified {@link CalendarAvailability} A precedes and intersects
     *         with {@link DateTime} interval of the specified {@link CalendarAvailability} B; <code>false</code> otherwise
     */
    public static boolean precedesAndIntersects(CalendarAvailability a, CalendarAvailability b) {
        return precedesAndIntersects(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the {@link DateTime} interval of the specified {@link CalendarAvailability} A succeeds and intersects with the
     * {@link DateTime} interval of the specified {@link CalendarAvailability} interval B
     * 
     * @param a The {@link CalendarAvailability} A
     * @param b The {@link CalendarAvailability} B
     * @return <code>true</code> if the {@link DateTime} interval of the specified {@link CalendarAvailability} A succeeds and intersects
     *         with {@link DateTime} interval of the specified {@link CalendarAvailability} B; <code>false</code> otherwise
     */
    public static boolean succeedsAndIntersects(CalendarAvailability a, CalendarAvailability b) {
        return succeedsAndIntersects(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the {@link DateTime} interval of the specified {@link CalendarAvailability} A is completely contained
     * with in the {@link DateTime} interval of the specified {@link CalendarAvailability} B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the {@link DateTime} interval of the specified {@link CalendarAvailability} A is completely contained
     *         with in the {@link DateTime} interval of the specified {@link CalendarAvailability} B; <code>false</code> otherwise
     */
    public static boolean contained(CalendarAvailability a, CalendarAvailability b) {
        return contained(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the {@link DateTime} interval of the specified {@link CalendarFreeSlot} A precedes and intersects with the
     * {@link DateTime} interval of the specified {@link CalendarFreeSlot} interval B
     * 
     * @param a The {@link CalendarFreeSlot} A
     * @param b The {@link CalendarFreeSlot} B
     * @return <code>true</code> if the {@link DateTime} interval of the specified {@link CalendarFreeSlot} A precedes and intersects
     *         with {@link DateTime} interval of the specified {@link CalendarFreeSlot} B; <code>false</code> otherwise
     */
    public static boolean precedesAndIntersects(CalendarFreeSlot a, CalendarFreeSlot b) {
        return precedesAndIntersects(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the {@link DateTime} interval of the specified {@link CalendarFreeSlot} A succeeds and intersects with the
     * {@link DateTime} interval of the specified {@link CalendarFreeSlot} interval B
     * 
     * @param a The {@link CalendarFreeSlot} A
     * @param b The {@link CalendarFreeSlot} B
     * @return <code>true</code> if the {@link DateTime} interval of the specified {@link CalendarFreeSlot} A succeeds and intersects
     *         with {@link DateTime} interval of the specified {@link CalendarFreeSlot} B; <code>false</code> otherwise
     */
    public static boolean succeedsAndIntersects(CalendarFreeSlot a, CalendarFreeSlot b) {
        return succeedsAndIntersects(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the {@link Date} interval of the specified {@link CalendarFreeSlot} A is completely contained
     * with in the {@link Date} interval of the specified {@link CalendarFreeSlot} B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the {@link Date} interval of the specified {@link CalendarFreeSlot} A is completely contained
     *         with in the {@link Date} interval of the specified {@link CalendarFreeSlot} B; <code>false</code> otherwise
     */
    public static boolean contained(CalendarFreeSlot a, CalendarFreeSlot b) {
        return contained(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the specified {@link DateTime} interval A is completely contained with in the specified {@link DateTime} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link DateTime} interval A is completely
     *         contained with in the specified {@link DateTime} interval B; <code>false</code> otherwise
     */
    public static boolean contained(DateTime startA, DateTime endA, DateTime startB, DateTime endB) {
        return startA.after(startB) && endA.before(endB);
    }

    /**
     * Checks if the specified {@link Date} interval A is completely contained with in the specified {@link Date} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link Date} interval A is completely
     *         contained with in the specified {@link Date} interval B; <code>false</code> otherwise
     */
    public static boolean contained(Date startA, Date endA, Date startB, Date endB) {
        return startA.after(startB) && endA.before(endB);
    }

    /**
     * Checks if the specified {@link Date} interval A precedes and intersects with the specified {@link Date} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link Date} interval A precedes and intersects
     *         with the specified {@link Date} interval B; <code>false</code> otherwise
     */
    public static boolean precedesAndIntersects(Date startA, Date endA, Date startB, Date endB) {
        if (!intersect(startA, endA, startB, endB)) {
            return false;
        }
        return startA.before(startB) && endA.after(startB);
    }

    /**
     * Checks if the specified {@link DateTime} interval A precedes and intersects with the specified {@link DateTime} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link DateTime} interval A precedes and intersects
     *         with the specified {@link DateTime} interval B; <code>false</code> otherwise
     */
    public static boolean precedesAndIntersects(DateTime startA, DateTime endA, DateTime startB, DateTime endB) {
        if (!intersect(startA, endA, startB, endB)) {
            return false;
        }
        return startA.before(startB) && endA.after(startB);
    }

    /**
     * Checks if the specified {@link Date} interval A succeeds and intersects with the specified {@link Date} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link Date} interval A succeeds and intersects
     *         with the specified {@link Date} interval B; <code>false</code> otherwise
     */
    public static boolean succeedsAndIntersects(Date startA, Date endA, Date startB, Date endB) {
        if (!intersect(startA, endA, startB, endB)) {
            return false;
        }
        return startA.before(endB) && endA.after(endB);
    }

    /**
     * Checks if the specified {@link DateTime} interval A succeeds and intersects with the specified {@link DateTime} interval B
     * 
     * @param startA The start date of the interval A
     * @param endA The end date of the interval A
     * @param startB the start date of the interval B
     * @param endB The end date of the interval B
     * @return <code>true</code> if the specified {@link DateTime} interval A succeeds and intersects
     *         with the specified {@link DateTime} interval B; <code>false</code> otherwise
     */
    public static boolean succeedsAndIntersects(DateTime startA, DateTime endA, DateTime startB, DateTime endB) {
        if (!intersect(startA, endA, startB, endB)) {
            return false;
        }
        return startA.before(endB) && endA.after(endB);
    }

    /**
     * Checks if the specified availability A intersects with the specified availability B.
     * Two checks are performed internally:
     * <ul>
     * <li>whether the starting point of A is before the ending point of B</li>
     * <li>whether the ending point of A is after the starting point of B</li>
     * </ul>
     * 
     * @param a The {@link CalendarAvailability} A
     * @param b The {@link CalendarAvailability} B
     * @return <code>true</code> if they intersect; <code>false</code> otherwise
     */
    public static boolean intersect(CalendarAvailability a, CalendarAvailability b) {
        return intersect(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Checks if the specified free slot A intersects with the specified free slot B.
     * Two checks are performed internally:
     * <ul>
     * <li>whether the starting point of A is before the ending point of B</li>
     * <li>whether the ending point of A is after the starting point of B</li>
     * </ul>
     * 
     * @param a The {@link CalendarAvailability} A
     * @param b The {@link CalendarAvailability} B
     * @return <code>true</code> if they intersect; <code>false</code> otherwise
     */
    public static boolean intersect(CalendarFreeSlot a, CalendarFreeSlot b) {
        return intersect(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    /**
     * Two checks are performed:
     * <ul>
     * <li>whether the starting point of A is before the ending point of B</li>
     * <li>whether the ending point of A is after the starting point of B</li>
     * </ul>
     * 
     * @param startA The starting date A
     * @param endA The ending date A
     * @param startB The starting date B
     * @param endB The ending date B
     * @return <code>true</code> if they intersect; <code>false</code> otherwise
     */
    public static boolean intersect(DateTime startA, DateTime endA, DateTime startB, DateTime endB) {
        return startA.before(endB) && endA.after(startB);
    }

    /**
     * Two checks are performed:
     * <ul>
     * <li>whether the starting point of A is before the ending point of B</li>
     * <li>whether the ending point of A is after the starting point of B</li>
     * </ul>
     * 
     * @param startA The starting date A
     * @param endA The ending date A
     * @param startB The starting date B
     * @param endB The ending date B
     * @return <code>true</code> if they intersect; <code>false</code> otherwise
     */
    public static boolean intersect(Date startA, Date endA, Date startB, Date endB) {
        return startA.before(endB) && endA.after(startB);
    }

    /**
     * Merge the start and end times of the specified {@link CalendarFreeSlot}s
     * 
     * @param a The {@link CalendarFreeSlot} A
     * @param b The {@link CalendarFreeSlot} B
     * @return An {@link AvailableTimeSlot} with the merged start and end times of the specified {@link CalendarFreeSlot}
     */
    public static AvailableTimeSlot mergeFreeSlots(CalendarFreeSlot a, CalendarFreeSlot b) {
        AvailableTimeSlot ats = new AvailableTimeSlot();
        ats.setFrom(a.getStartTime().before(b.getStartTime()) ? a.getStartTime() : b.getStartTime());
        ats.setUntil(a.getEndTime().after(b.getEndTime()) ? a.getEndTime() : b.getEndTime());
        return ats;
    }

    /**
     * Merges the specified {@link CalendarFreeSlot}s
     * 
     * @param a The {@link CalendarFreeSlot} A
     * @param b The {@link CalendarFreeSlot} B
     * @return a new merged {@link CalendarFreeSlot} instance
     */
    public static CalendarFreeSlot merge(CalendarFreeSlot a, CalendarFreeSlot b) {
        CalendarFreeSlot ats = new CalendarFreeSlot();
        ats.setStartTime(a.getStartTime().before(b.getStartTime()) ? a.getStartTime() : b.getStartTime());
        ats.setEndTime(a.getEndTime().after(b.getEndTime()) ? a.getEndTime() : b.getEndTime());
        return ats;
    }

    /**
     * Converts the specified {@link CalendarFreeSlot} to an {@link AvailableTimeSlot}
     * 
     * @param freeSlot The {@link CalendarFreeSlot} to convert
     * @return The converted {@link AvailableTimeSlot}
     */
    public static AvailableTimeSlot convert(CalendarFreeSlot freeSlot) {
        AvailableTimeSlot ats = new AvailableTimeSlot();
        ats.setFrom(freeSlot.getStartTime());
        ats.setUntil(freeSlot.getEndTime());
        return ats;
    }

    /**
     * Converts the specified {@link AvailableTimeSlot} to a {@link FreeBusyTime} object
     * and sets the {@link FbType} to {@link FbType#FREE}.
     * 
     * @param availableTimeSlot The {@link AvailableTimeSlot} to convert
     * @return The converted {@link FreeBusyTime} object
     */
    public static FreeBusyTime convert(AvailableTimeSlot availableTimeSlot) {
        FreeBusyTime freeBusyTime = new FreeBusyTime();
        freeBusyTime.setStartTime(new Date(availableTimeSlot.getFrom().getTimestamp()));
        freeBusyTime.setEndTime(new Date(availableTimeSlot.getUntil().getTimestamp()));
        freeBusyTime.setFbType(FbType.FREE);
        return freeBusyTime;
    }

    public static FbType convertFreeBusyType(BusyType busyType) {
        switch (busyType) {
            case BUSY:
                return FbType.BUSY;
            case BUSY_TENTATIVE:
                return FbType.BUSY_TENTATIVE;
            case BUSY_UNAVAILABLE:
                return FbType.BUSY_UNAVAILABLE;
            default:
                return FbType.BUSY;
        }
    }
}
