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

package com.openexchange.chronos.impl.availability.performer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.AvailableTime;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.common.AvailabilityUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.Comparators;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link GetPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetPerformer extends AbstractGetPerformer {

    /**
     * Initialises a new {@link GetPerformer}.
     */
    public GetPerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    /**
     * Retrieves a {@link List} with all {@link Availability} blocks of the current user
     * 
     * @return a {@link List} with all {@link Availability} blocks of the current user
     * @throws OXException if the list cannot be retrieved
     */
    public Availability perform() throws OXException {
        List<Available> available = getStorage().loadAvailable(getSession().getUserId());
        return prepareForDelivery(available);
    }

    /**
     * Retrieves a {@link Map} of {@link Availability} blocks for the specified {@link Attendee}s in the specified interval
     * 
     * @param attendees the {@link List} of {@link Attendee}s to fetch the availability for
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return a {@link Map} of {@link Availability} blocks for the specified {@link Attendee}s in the specified interval
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, Availability> performForAttendees(List<Attendee> attendees, Date from, Date until) throws OXException {
        // Prepare the attendees
        attendees = getSession().getEntityResolver().prepare(attendees);
        // Filter the external ones
        attendees = CalendarUtils.filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL);
        if (attendees.size() == 0) {
            return java.util.Collections.emptyMap();
        }

        // Create a reverse lookup index
        Map<Integer, Attendee> reverseLookup = new HashMap<>();
        for (Attendee a : attendees) {
            reverseLookup.put(a.getEntity(), a);
        }
        return loadAvailability(reverseLookup, from, until);
    }

    /**
     * Retrieves a {@link Map} of {@link Availability} blocks for the specified {@link CalendarUser}s in the specified interval
     * 
     * @param attendees the {@link List} of {@link CalendarUser}s to fetch the availability for
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return a {@link Map} of {@link Availability} blocks for the specified {@link CalendarUser}s in the specified interval
     * @throws OXException if an error is occurred
     */
    public Map<CalendarUser, Availability> performForUsers(List<CalendarUser> users, Date from, Date until) throws OXException {
        // Create a reverse lookup index and filter internal users
        Map<Integer, CalendarUser> reverseLookup = new HashMap<>();
        List<Integer> filtered = new ArrayList<>();
        for (CalendarUser u : users) {
            if (CalendarUtils.isInternal(u, CalendarUserType.INDIVIDUAL)) {
                reverseLookup.put(u.getEntity(), u);
                filtered.add(u.getEntity());
            }
        }

        return loadAvailability(reverseLookup, from, until);
    }

    /**
     * Retrieves the the combined {@link Availability} blocks for the current user.
     * 
     * @return A {@link List} with the combined {@link Availability} blocks for the user
     * @throws OXException if an error is occurred
     */
    public List<Availability> getCombinedAvailableTime() throws OXException {
        int userId = getSession().getUserId();
        List<Availability> calendarAvailabilities = getStorage().loadCalendarAvailabilities(userId);
        return combine(calendarAvailabilities);
    }

    /**
     * Retrieves the combined {@link Availability} blocks in the specified interval for the current user
     * 
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return A {@link List} with the combined {@link Availability} blocks for the user in the specified interval
     * @throws OXException
     */
    public List<Availability> getCombinedAvailableTimeInRange(Date from, Date until) throws OXException {
        int userId = getSession().getUserId();
        List<Availability> calendarAvailabilities = getStorage().loadCalendarAvailabilities(userId);
        return combine(calendarAvailabilities);
    }

    /**
     * Retrieves the {@link Availability} blocks for the specified {@link Attendee}s in the specified time interval
     * 
     * @param attendees The {@link List} with the {@link Attendee}s to retrieve the {@link Availability} blocks for
     * @param from The start point in the time interval
     * @param until The end point in the time interval
     * @return A {@link Map} with {@link Availability} slots for the {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, Availability> getCombinedAvailability(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, Availability> availableTimes = new HashMap<>();
        Map<Attendee, Availability> availabilityPerAttendee = performForAttendees(attendees, from, until);
        for (Attendee attendee : attendees) {
            Availability availability = availabilityPerAttendee.get(attendee);
            combine(availability);
            availableTimes.put(attendee, availability);
        }
        return availableTimes;
        //        Map<Attendee, List<Availability>> availabilitiesPerAttendee = performForAttendees(attendees, from, until);
        //        for (Attendee attendee : attendees) {
        //            List<Availability> calendarAvailabilities = availabilitiesPerAttendee.get(attendee);
        //            availableTimes.put(attendee, combine(calendarAvailabilities));
        //        }
    }

    /**
     * Retrieves the {@link Availability} blocks for the specified {@link Attendee}s in the specified time interval
     * 
     * @param attendees The {@link List} with the {@link Attendee}s to retrieve the {@link Availability} blocks for
     * @param from The start point in the time interval
     * @param until The end point in the time interval
     * @return A {@link Map} with {@link Availability} slots for the {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, List<Availability>> getCombinedAvailableTimes(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<Availability>> availableTimes = new HashMap<>();
        Map<Attendee, Availability> availabilitiesPerAttendee = performForAttendees(attendees, from, until);
        for (Attendee attendee : attendees) {
            Availability calendarAvailabilities = availabilitiesPerAttendee.get(attendee);
            availableTimes.put(attendee, java.util.Collections.singletonList(calendarAvailabilities));
        }
        return availableTimes;
    }

    /**
     * Retrieves the {@link AvailableTime} slots for the current user
     * 
     * @return The {@link AvailableTime} slots for the current user
     */
    public AvailableTime getAvailableTime() throws OXException {
        int userId = getSession().getUserId();
        List<Availability> calendarAvailabilities = getStorage().loadCalendarAvailabilities(userId);
        return getAvailableTime(userId, calendarAvailabilities);
    }

    /**
     * Retrieves the {@link AvailableTime} for the specified {@link Attendee}s in the specified time interval
     * 
     * @param attendees The {@link List} with the {@link Attendee}s to retrieve the {@link AvailableTime} for
     * @param from The start point in the time interval
     * @param until The end point in the time interval
     * @return A {@link Map} with {@link AvailableTime} slots for the {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, AvailableTime> getAvailableTime(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, AvailableTime> availableTimes = new HashMap<>();
        Map<Attendee, Availability> availabilitiesPerAttendee = performForAttendees(attendees, from, until);
        for (Attendee attendee : attendees) {
            Availability calendarAvailabilities = availabilitiesPerAttendee.get(attendee);
            availableTimes.put(attendee, getAvailableTime(attendee.getEntity(), java.util.Collections.singletonList(calendarAvailabilities)));
        }
        return availableTimes;
    }

    ///////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Retrieves the {@link Availability} blocks for the specified users
     * 
     * @param reverseLookup The reverse lookup map for the entities
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return A {@link Map} of {@link Availability} blocks for the specified users in the specified interval
     * @throws OXException if an error is occurred
     */
    private <T extends CalendarUser> Map<T, Availability> loadAvailability(Map<Integer, T> reverseLookup, Date from, Date until) throws OXException {
        List<Available> available = getStorage().loadAvailable(new ArrayList<>(reverseLookup.keySet()));
        Map<T, List<Available>> map = new HashMap<>();
        for (Available a : available) {
            T type = reverseLookup.get(a.getCalendarUser());
            Collections.put(map, type, a);
        }
        Map<T, Availability> availabilities = new HashMap<>();
        for (T t : map.keySet()) {
            availabilities.put(t, prepareForDelivery(map.get(t)));
        }
        return availabilities;
    }

    /**
     * Combines the {@link Available} blocks from the specified {@link Availability}
     * 
     * @param availability The {@link Availability} for which to combine the blocks
     */
    private void combine(Availability availability) {
        List<Available> available = availability.getAvailable();
        List<Available> availableTime = new ArrayList<>(available.size());
        Iterator<Available> iteratorA = available.iterator();
        int index = 0;
        // Keeps track of the removed objects
        List<Available> removed = new ArrayList<>();
        while (iteratorA.hasNext()) {
            Available a = iteratorA.next();
            if (removed.contains(a)) {
                iteratorA.remove();
                removed.remove(a);
                continue;
            }

            List<Available> lookAheadList = available.subList(++index, available.size());
            Iterator<Available> iteratorB = lookAheadList.iterator();
            while (iteratorB.hasNext()) {
                Available b = iteratorB.next();
                // If it is completely contained then skip it
                if (AvailabilityUtils.contained(b, a)) {
                    removed.add(b);
                    continue;
                }
                // If it in intersects, then merge
                if (AvailabilityUtils.intersect(b, a)) {
                    a = AvailabilityUtils.merge(b, a);
                    removed.add(b);
                }
            }
            availableTime.add(a);
        }
    }

    /**
     * Combines the specified {@link Availability} blocks and handles any intersects, overlaps and contains
     * 
     * @param calendarAvailabilities The {@link List} of the {@link Availability} blocks to combine
     * @return The combined {@link Availability} blocks
     * @deprecated Use {@link #combine(Availability)} instead.
     */
    private List<Availability> combine(List<Availability> calendarAvailabilities) {
        // Sort by priority; higher priority will be on top
        java.util.Collections.sort(calendarAvailabilities, Comparators.priorityComparator);

        // The combined available time
        List<Availability> availableTime = new ArrayList<>(calendarAvailabilities.size());
        // Auxiliary list to store any splits
        List<Availability> presAndPosts = new ArrayList<>();
        int index = 0;
        for (Iterator<Availability> iteratorA = calendarAvailabilities.iterator(); iteratorA.hasNext();) {
            Availability availabilityA = iteratorA.next();
            List<Availability> subList = calendarAvailabilities.subList(++index, calendarAvailabilities.size());
            Iterator<Availability> iteratorB = subList.iterator();
            while (iteratorB.hasNext()) {
                Availability availabilityB = iteratorB.next();
                // No intersection, carry on
                if (!AvailabilityUtils.intersect(availabilityA, availabilityB)) {
                    continue;
                }

                // Higher or equal priority, adjust times
                if (AvailabilityUtils.contained(availabilityB, availabilityA)) {
                    List<Available> flattenList = new ArrayList<>(availabilityB.getAvailable().size() + availabilityA.getAvailable().size());
                    flattenList.addAll(availabilityB.getAvailable());
                    flattenList.addAll(availabilityA.getAvailable());
                    availabilityA.setAvailable(combineSlots(flattenList));
                    iteratorB.remove();
                } else if (AvailabilityUtils.contained(availabilityA, availabilityB)) {
                    // Block A is entirely contained in block B, and since the list is sorted by priority
                    // block A will always have higher or equal priority with block B, so we have to split B
                    // Resulting in something similar to this:
                    //          +---------------------------+
                    //          |   Block A, Priority: 2    |                       +------------+---------------------------+-------------+
                    //          +---------------------------+               ====>   | Block preB |         Block A           | Block postB |
                    // +--------------------------------------------+               +------------+---------------------------+-------------+
                    // |           Block B, Priority: 5             |               
                    // +--------------------------------------------+
                    List<Available> preIntersections = new ArrayList<>();
                    List<Available> postIntersections = new ArrayList<>();
                    List<Available> flattenList = new ArrayList<>(availabilityB.getAvailable().size() + availabilityA.getAvailable().size());

                    Availability pre = availabilityB.clone();
                    pre.setEndTime(availabilityA.getStartTime());
                    // Find any slots that intersect with the new end time, adjust and split them 
                    // if the availability blocks are of equal priority, then append the first split 
                    // to the pre availability, and the last split to the preIntersections.
                    List<Available> preSlots = new ArrayList<>();
                    for (Iterator<Available> iterator = availabilityB.getAvailable().iterator(); iterator.hasNext();) {
                        Available cfs = iterator.next();
                        if (AvailabilityUtils.intersectsButNotContained(cfs.getStartTime(), cfs.getEndTime(), availabilityA.getStartTime(), availabilityA.getEndTime())) {
                            if (availabilityA.compareTo(availabilityB) == 0) {
                                // If the priorities are equal then we split the free slot
                                Available preSplit = cfs.clone();
                                preSplit.setEndTime(availabilityA.getStartTime());
                                pre.getAvailable().add(preSplit);

                                Available postSplit = cfs.clone();
                                postSplit.setStartTime(availabilityB.getEndTime());
                                preIntersections.add(postSplit);
                            } else {
                                // Otherwise the end time of the free slot will be shortened
                                // to match the the start time of the availability block A
                                cfs.setEndTime(availabilityA.getStartTime());
                            }
                        } else if (AvailabilityUtils.contained(cfs.getStartTime(), cfs.getEndTime(), pre.getStartTime(), pre.getEndTime())) {
                            preSlots.add(cfs);
                            iterator.remove();
                        }
                    }
                    pre.setAvailable(preSlots);
                    presAndPosts.add(pre);

                    Availability post = availabilityB.clone();
                    post.setStartTime(availabilityA.getEndTime());
                    // Find any slots that intersect with the new start time, adjust and split them 
                    // if the availability blocks are of equal priority,  then append the first split 
                    // to the postIntersections, and the last split to the post availability.
                    List<Available> postSlots = new ArrayList<>();
                    for (Iterator<Available> iterator = availabilityB.getAvailable().iterator(); iterator.hasNext();) {
                        Available cfs = iterator.next();
                        if (AvailabilityUtils.intersectsButNotContained(cfs.getStartTime(), cfs.getEndTime(), availabilityA.getStartTime(), availabilityA.getEndTime())) {
                            if (availabilityA.compareTo(availabilityB) == 0) {
                                Available preSplit = cfs.clone();
                                preSplit.setEndTime(availabilityB.getStartTime());
                                postIntersections.add(preSplit);

                                Available postSplit = cfs.clone();
                                postSplit.setStartTime(availabilityA.getEndTime());
                                post.getAvailable().add(postSplit);
                            } else {
                                cfs.setStartTime(availabilityA.getEndTime());
                            }
                        } else if (AvailabilityUtils.contained(cfs.getStartTime(), cfs.getEndTime(), post.getStartTime(), post.getEndTime())) {
                            postSlots.add(cfs);
                            iterator.remove();
                        }
                    }
                    post.setAvailable(postSlots);
                    presAndPosts.add(post);
                    flattenList.addAll(availabilityA.getAvailable());
                    flattenList.addAll(availabilityB.getAvailable());
                    availabilityA.setAvailable(combineSlots(flattenList));
                    // Add any splits from the intersections to the availability A
                    availabilityA.getAvailable().addAll(preIntersections);
                    availabilityA.getAvailable().addAll(postIntersections);

                    // Remove the contained availability
                    iteratorB.remove();
                } else if (AvailabilityUtils.precedesAndIntersects(availabilityA, availabilityB)) {
                    availabilityB.setStartTime(availabilityA.getEndTime());
                    adjustSlots(availabilityA, availabilityB);
                } else if (AvailabilityUtils.succeedsAndIntersects(availabilityA, availabilityB)) {
                    availabilityB.setEndTime(availabilityA.getStartTime());
                    adjustSlots(availabilityA, availabilityB);
                }
            }
            availableTime.add(availabilityA);
        }

        // Add any splits
        availableTime.addAll(presAndPosts);

        // Sort by start date
        java.util.Collections.sort(availableTime, Comparators.availabilityDateTimeComparator);
        return availableTime;
    }

    /**
     * Adjusts the {@link Available}s of the specified {@link Availability} blocks
     * 
     * @param a The {@link Availability} A
     * @param b The {@link Availability} B
     */
    private void adjustSlots(Availability a, Availability b) {
        List<Available> toAdd = new ArrayList<>();
        for (Available freeSlotA : a.getAvailable()) {
            for (Iterator<Available> iteratorB = b.getAvailable().iterator(); iteratorB.hasNext();) {
                Available freeSlotB = iteratorB.next();
                if (AvailabilityUtils.contained(freeSlotA, freeSlotB)) {
                    // split
                    Available pre = freeSlotB.clone();
                    pre.setEndTime(freeSlotA.getStartTime());
                    toAdd.add(pre);

                    Available post = freeSlotB.clone();
                    post.setStartTime(freeSlotA.getEndTime());
                    toAdd.add(post);

                    iteratorB.remove();
                } else if (AvailabilityUtils.contained(freeSlotB, freeSlotA)) {
                    iteratorB.remove();
                } else if (AvailabilityUtils.precedesAndIntersects(freeSlotA, freeSlotB)) {
                    freeSlotB.setStartTime(freeSlotA.getEndTime());
                } else if (AvailabilityUtils.succeedsAndIntersects(freeSlotA, freeSlotB)) {
                    freeSlotB.setEndTime(freeSlotA.getStartTime());
                }
            }
            // Add any splits
            b.getAvailable().addAll(toAdd);
        }
    }

    /**
     * Retrieves the {@link AvailableTime} slots for the specified user
     * 
     * @param userId The user identifier
     * @return The {@link AvailableTime} slots for the current user
     * @throws OXException if an error is occurred
     */
    private AvailableTime getAvailableTime(int userId, List<Availability> calendarAvailabilities) throws OXException {
        // Sort by priority (see: https://tools.ietf.org/html/rfc7953#section-4 RFC 7953, section 4</a>
        java.util.Collections.sort(calendarAvailabilities);

        BusyType busyType = BusyType.BUSY_TENTATIVE;
        List<Available> flattenSlots = new ArrayList<>();
        for (Availability calendarAvailability : calendarAvailabilities) {
            busyType = busyType.ordinal() >= calendarAvailability.getBusyType().ordinal() ? busyType : calendarAvailability.getBusyType();
            flattenSlots.addAll(calendarAvailability.getAvailable());
        }

        AvailableTime availableTime = new AvailableTime();
        availableTime.setUserId(userId);
        availableTime.setBusyType(busyType);

        // Combine
        combine(flattenSlots, availableTime);

        return availableTime;
    }

    /**
     * Combines the specified {@link Available}s to a single {@link AvailableTime}
     * 
     * @param freeSlots The {@link Available}s to combine
     * @param availableTime The {@link AvailableTime} to combine to
     */
    private void combine(List<Available> freeSlots, AvailableTime availableTime) {
        Iterator<Available> iteratorA = freeSlots.iterator();
        int index = 0;
        // Keeps track of the removed objects
        List<Available> removed = new ArrayList<>();
        while (iteratorA.hasNext()) {
            Available a = iteratorA.next();
            if (removed.contains(a)) {
                iteratorA.remove();
                removed.remove(a);
                continue;
            }

            List<Available> lookAheadList = freeSlots.subList(++index, freeSlots.size());
            Iterator<Available> iteratorB = lookAheadList.iterator();
            while (iteratorB.hasNext()) {
                Available b = iteratorB.next();
                // If it is completely contained then skip it
                if (AvailabilityUtils.contained(b, a)) {
                    removed.add(b);
                    continue;
                }
                // If it in intersects, then merge
                if (AvailabilityUtils.intersect(b, a)) {
                    a = AvailabilityUtils.merge(b, a);
                    removed.add(b);
                }
            }
            availableTime.add(AvailabilityUtils.convert(a));
        }
    }

    /**
     * Combines the specified {@link Available}s
     * 
     * @param freeSlots The {@link Available}s to combine
     */
    private List<Available> combineSlots(List<Available> freeSlots) {
        // Sort by starting date
        java.util.Collections.sort(freeSlots, Comparators.availableDateTimeComparator);

        List<Available> combined = new ArrayList<>(freeSlots.size());
        Iterator<Available> iteratorA = freeSlots.iterator();
        int index = 0;
        // Keeps track of the removed objects
        List<Available> removed = new ArrayList<>();
        while (iteratorA.hasNext()) {
            Available a = iteratorA.next();
            if (removed.contains(a)) {
                iteratorA.remove();
                removed.remove(a);
                continue;
            }

            List<Available> lookAheadList = freeSlots.subList(++index, freeSlots.size());
            Iterator<Available> iteratorB = lookAheadList.iterator();
            while (iteratorB.hasNext()) {
                Available b = iteratorB.next();
                // If it is completely contained then skip it
                if (AvailabilityUtils.contained(b, a)) {
                    removed.add(b);
                    continue;
                }
                // If it in intersects, then merge
                if (AvailabilityUtils.intersect(b, a)) {
                    a = AvailabilityUtils.merge(b, a);
                    removed.add(b);
                }
            }
            combined.add(a);
        }
        // Sort by starting date
        java.util.Collections.sort(combined, Comparators.availableDateTimeComparator);
        return combined;
    }
}
