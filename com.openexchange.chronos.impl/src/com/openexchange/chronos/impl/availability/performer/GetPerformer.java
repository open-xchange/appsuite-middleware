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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AvailableTime;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.common.AvailabilityUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DateTimeComparator;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link GetPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetPerformer extends AbstractPerformer {

    private static final Comparator<CalendarAvailability> availabilityDateTimeComparator = new AvailabilityDateTimeComparator();
    private static final Comparator<CalendarFreeSlot> freeSlotDateTimeComparator = new FreeSlotDateTimeComparator();
    private static final Comparator<CalendarAvailability> priorityComparator = new PriorityComparator();

    /**
     * Initialises a new {@link GetPerformer}.
     */
    public GetPerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    /**
     * Retrieves the {@link CalendarAvailability} with the specified identifier
     * 
     * @param calendarAvailabilityId The {@link CalendarAvailability}'s identifier
     * @return The {@link CalendarAvailability}
     * @throws OXException if the {@link CalendarAvailability} cannot be retrieved
     */
    public CalendarAvailability perform(String calendarAvailabilityId) throws OXException {
        return storage.loadCalendarAvailability(calendarAvailabilityId);
    }

    /**
     * Retrieves a {@link List} with all {@link CalendarAvailability} blocks of the current user
     * 
     * @return a {@link List} with all {@link CalendarAvailability} blocks of the current user
     * @throws OXException if the list cannot be retrieved
     */
    public List<CalendarAvailability> perform() throws OXException {
        return storage.loadCalendarAvailabilities(session.getUserId());
    }

    /**
     * Retrieves a {@link List} with {@link CalendarAvailability} blocks in the specified range
     * 
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return a {@link List} with {@link CalendarAvailability} blocks in the specified range
     * @throws OXException if an error occurs
     */
    public List<CalendarAvailability> performInRange(Date from, Date until) throws OXException {
        return storage.loadCalenarAvailabilityInRange(session.getUserId(), from, until);
    }

    /**
     * Retrieves a {@link Map} of {@link CalendarAvailability} blocks for the specified {@link Attendee}s in the specified interval
     * 
     * @param attendees the {@link List} of {@link Attendee}s to fetch the availability for
     * @param from The starting point in the interval
     * @param until The ending poing in the interval
     * @return a {@link Map} of {@link CalendarAvailability} blocks for the specified {@link Attendee}s in the specified interval
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, List<CalendarAvailability>> performForAttendees(List<Attendee> attendees, Date from, Date until) throws OXException {
        // Prepare the attendees
        attendees = session.getEntityResolver().prepare(attendees);
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
     * Retrieves a {@link Map} of {@link CalendarAvailability} blocks for the specified {@link CalendarUser}s in the specified interval
     * 
     * @param attendees the {@link List} of {@link CalendarUser}s to fetch the availability for
     * @param from The starting point in the interval
     * @param until The ending point in the interval
     * @return a {@link Map} of {@link CalendarAvailability} blocks for the specified {@link CalendarUser}s in the specified interval
     * @throws OXException if an error is occurred
     */
    public Map<CalendarUser, List<CalendarAvailability>> performForUsers(List<CalendarUser> users, Date from, Date until) throws OXException {
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
     * Retrieves the the combined {@link CalendarAvailability} blocks for the current user.
     * 
     * @return A {@link List} with the combined {@link CalendarAvailability} blocks for the user
     * @throws OXException if an error is occurred
     */
    public List<CalendarAvailability> getCombinedAvailableTime() throws OXException {
        int userId = session.getUserId();
        List<CalendarAvailability> calendarAvailabilities = storage.loadCalendarAvailabilities(userId);
        return combine(calendarAvailabilities);
    }

    /**
     * Retrieves the combined {@link CalendarAvailability} blocks in the specified interval for the current user
     * 
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return A {@link List} with the combined {@link CalendarAvailability} blocks for the user in the specified interval
     * @throws OXException
     */
    public List<CalendarAvailability> getCombinedAvailableTimeInRange(Date from, Date until) throws OXException {
        int userId = session.getUserId();
        List<CalendarAvailability> calendarAvailabilities = storage.loadCalenarAvailabilityInRange(userId, from, until);
        return combine(calendarAvailabilities);
    }

    /**
     * Retrieves the {@link CalendarAvailability} blocks for the specified {@link Attendee}s in the specified time interval
     * 
     * @param attendees The {@link List} with the {@link Attendee}s to retrieve the {@link CalendarAvailability} blocks for
     * @param from The start point in the time interval
     * @param until The end point in the time interval
     * @return A {@link Map} with {@link CalendarAvailability} slots for the {@link Attendee}s
     * @throws OXException if an error is occurred
     */
    public Map<Attendee, List<CalendarAvailability>> getCombinedAvailableTimes(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<CalendarAvailability>> availableTimes = new HashMap<>();
        Map<Attendee, List<CalendarAvailability>> availabilitiesPerAttendee = performForAttendees(attendees, from, until);
        for (Attendee attendee : attendees) {
            List<CalendarAvailability> calendarAvailabilities = availabilitiesPerAttendee.get(attendee);
            availableTimes.put(attendee, combine(calendarAvailabilities));
        }
        return availableTimes;
    }

    /**
     * Retrieves the {@link AvailableTime} slots for the current user
     * 
     * @return The {@link AvailableTime} slots for the current user
     */
    public AvailableTime getAvailableTime() throws OXException {
        int userId = session.getUserId();
        List<CalendarAvailability> calendarAvailabilities = storage.loadCalendarAvailabilities(userId);
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
        Map<Attendee, List<CalendarAvailability>> availabilitiesPerAttendee = performForAttendees(attendees, from, until);
        for (Attendee attendee : attendees) {
            List<CalendarAvailability> calendarAvailabilities = availabilitiesPerAttendee.get(attendee);
            availableTimes.put(attendee, getAvailableTime(attendee.getEntity(), calendarAvailabilities));
        }
        return availableTimes;
    }

    ///////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Retrieves the {@link CalendarAvailability} blocks for the specified users
     * 
     * @param reverseLookup The reverse lookup map for the entities
     * @param from The starting point of the interval
     * @param until The ending point of the interval
     * @return A {@link Map} of {@link CalendarAvailability} blocks for the specified users in the specified interval
     * @throws OXException if an error is occurred
     */
    private <T extends CalendarUser> Map<T, List<CalendarAvailability>> loadAvailability(Map<Integer, T> reverseLookup, Date from, Date until) throws OXException {
        List<CalendarAvailability> availabilities = storage.loadUserCalendarAvailability(new ArrayList<>(reverseLookup.keySet()), from, until);
        Map<T, List<CalendarAvailability>> map = new HashMap<>();
        for (CalendarAvailability availability : availabilities) {
            T type = reverseLookup.get(availability.getCalendarUser());
            Collections.put(map, type, map.get(type));
        }
        return map;
    }

    /**
     * Combines the specified {@link CalendarAvailability} blocks and handles any intersects, overlaps and contains
     * 
     * @param calendarAvailabilities The {@link List} of the {@link CalendarAvailability} blocks to combine
     * @return The combined {@link CalendarAvailability} blocks
     */
    private List<CalendarAvailability> combine(List<CalendarAvailability> calendarAvailabilities) {
        // Sort by priority; higher priority will be on top
        java.util.Collections.sort(calendarAvailabilities, priorityComparator);

        // The combined available time
        List<CalendarAvailability> availableTime = new ArrayList<>(calendarAvailabilities.size());
        // Auxiliary list to store any splits
        List<CalendarAvailability> presAndPosts = new ArrayList<>();
        int index = 0;
        for (Iterator<CalendarAvailability> iteratorA = calendarAvailabilities.iterator(); iteratorA.hasNext();) {
            CalendarAvailability availabilityA = iteratorA.next();
            List<CalendarAvailability> subList = calendarAvailabilities.subList(++index, calendarAvailabilities.size());
            Iterator<CalendarAvailability> iteratorB = subList.iterator();
            while (iteratorB.hasNext()) {
                CalendarAvailability availabilityB = iteratorB.next();
                // No intersection, carry on
                if (!AvailabilityUtils.intersect(availabilityA, availabilityB)) {
                    continue;
                }

                // Higher or equal priority, adjust times
                if (AvailabilityUtils.contained(availabilityB, availabilityA)) {
                    List<CalendarFreeSlot> flattenList = new ArrayList<>(availabilityB.getCalendarFreeSlots().size() + availabilityA.getCalendarFreeSlots().size());
                    flattenList.addAll(availabilityB.getCalendarFreeSlots());
                    flattenList.addAll(availabilityA.getCalendarFreeSlots());
                    availabilityA.setCalendarFreeSlots(combineSlots(flattenList));
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
                    List<CalendarFreeSlot> preIntersections = new ArrayList<>();
                    List<CalendarFreeSlot> postIntersections = new ArrayList<>();
                    List<CalendarFreeSlot> flattenList = new ArrayList<>(availabilityB.getCalendarFreeSlots().size() + availabilityA.getCalendarFreeSlots().size());

                    CalendarAvailability pre = availabilityB.clone();
                    pre.setEndTime(availabilityA.getStartTime());
                    // Find any slots that intersect with the new end time, adjust and split them 
                    // if the availability blocks are of equal priority, then append the first split 
                    // to the pre availability, and the last split to the preIntersections.
                    List<CalendarFreeSlot> preSlots = new ArrayList<>();
                    for (Iterator<CalendarFreeSlot> iterator = availabilityB.getCalendarFreeSlots().iterator(); iterator.hasNext();) {
                        CalendarFreeSlot cfs = iterator.next();
                        if (AvailabilityUtils.intersectsButNotContained(cfs.getStartTime(), cfs.getEndTime(), availabilityA.getStartTime(), availabilityA.getEndTime())) {
                            if (availabilityA.compareTo(availabilityB) == 0) {
                                // If the priorities are equal then we split the free slot
                                CalendarFreeSlot preSplit = cfs.clone();
                                preSplit.setEndTime(availabilityA.getStartTime());
                                pre.getCalendarFreeSlots().add(preSplit);

                                CalendarFreeSlot postSplit = cfs.clone();
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
                    pre.setCalendarFreeSlots(preSlots);
                    presAndPosts.add(pre);

                    CalendarAvailability post = availabilityB.clone();
                    post.setStartTime(availabilityA.getEndTime());
                    // Find any slots that intersect with the new start time, adjust and split them 
                    // if the availability blocks are of equal priority,  then append the first split 
                    // to the postIntersections, and the last split to the post availability.
                    List<CalendarFreeSlot> postSlots = new ArrayList<>();
                    for (Iterator<CalendarFreeSlot> iterator = availabilityB.getCalendarFreeSlots().iterator(); iterator.hasNext();) {
                        CalendarFreeSlot cfs = iterator.next();
                        if (AvailabilityUtils.intersectsButNotContained(cfs.getStartTime(), cfs.getEndTime(), availabilityA.getStartTime(), availabilityA.getEndTime())) {
                            if (availabilityA.compareTo(availabilityB) == 0) {
                                CalendarFreeSlot preSplit = cfs.clone();
                                preSplit.setEndTime(availabilityB.getStartTime());
                                postIntersections.add(preSplit);

                                CalendarFreeSlot postSplit = cfs.clone();
                                postSplit.setStartTime(availabilityA.getEndTime());
                                post.getCalendarFreeSlots().add(postSplit);
                            } else {
                                cfs.setStartTime(availabilityA.getEndTime());
                            }
                        } else if (AvailabilityUtils.contained(cfs.getStartTime(), cfs.getEndTime(), post.getStartTime(), post.getEndTime())) {
                            postSlots.add(cfs);
                            iterator.remove();
                        }
                    }
                    post.setCalendarFreeSlots(postSlots);
                    presAndPosts.add(post);
                    flattenList.addAll(availabilityA.getCalendarFreeSlots());
                    flattenList.addAll(availabilityB.getCalendarFreeSlots());
                    availabilityA.setCalendarFreeSlots(combineSlots(flattenList));
                    // Add any splits from the intersections to the availability A
                    availabilityA.getCalendarFreeSlots().addAll(preIntersections);
                    availabilityA.getCalendarFreeSlots().addAll(postIntersections);

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
        java.util.Collections.sort(availableTime, availabilityDateTimeComparator);
        return availableTime;
    }

    /**
     * Adjusts the {@link CalendarFreeSlot}s of the specified {@link CalendarAvailability} blocks
     * 
     * @param a The {@link CalendarAvailability} A
     * @param b The {@link CalendarAvailability} B
     */
    private void adjustSlots(CalendarAvailability a, CalendarAvailability b) {
        List<CalendarFreeSlot> toAdd = new ArrayList<>();
        for (CalendarFreeSlot freeSlotA : a.getCalendarFreeSlots()) {
            for (Iterator<CalendarFreeSlot> iteratorB = b.getCalendarFreeSlots().iterator(); iteratorB.hasNext();) {
                CalendarFreeSlot freeSlotB = iteratorB.next();
                if (AvailabilityUtils.contained(freeSlotA, freeSlotB)) {
                    // split
                    CalendarFreeSlot pre = freeSlotB.clone();
                    pre.setEndTime(freeSlotA.getStartTime());
                    toAdd.add(pre);

                    CalendarFreeSlot post = freeSlotB.clone();
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
            b.getCalendarFreeSlots().addAll(toAdd);
        }
    }

    /**
     * Retrieves the {@link AvailableTime} slots for the specified user
     * 
     * @param userId The user identifier
     * @return The {@link AvailableTime} slots for the current user
     * @throws OXException if an error is occurred
     */
    private AvailableTime getAvailableTime(int userId, List<CalendarAvailability> calendarAvailabilities) throws OXException {
        // Sort by priority (see: https://tools.ietf.org/html/rfc7953#section-4 RFC 7953, section 4</a>
        java.util.Collections.sort(calendarAvailabilities);

        BusyType busyType = BusyType.BUSY_TENTATIVE;
        List<CalendarFreeSlot> flattenSlots = new ArrayList<>();
        for (CalendarAvailability calendarAvailability : calendarAvailabilities) {
            busyType = busyType.ordinal() >= calendarAvailability.getBusyType().ordinal() ? busyType : calendarAvailability.getBusyType();
            flattenSlots.addAll(calendarAvailability.getCalendarFreeSlots());
        }

        AvailableTime availableTime = new AvailableTime();
        availableTime.setUserId(userId);
        availableTime.setBusyType(busyType);

        // Combine
        combine(flattenSlots, availableTime);

        return availableTime;
    }

    /**
     * Combines the specified {@link CalendarFreeSlot}s to a single {@link AvailableTime}
     * 
     * @param freeSlots The {@link CalendarFreeSlot}s to combine
     * @param availableTime The {@link AvailableTime} to combine to
     */
    private void combine(List<CalendarFreeSlot> freeSlots, AvailableTime availableTime) {
        Iterator<CalendarFreeSlot> iteratorA = freeSlots.iterator();
        int index = 0;
        // Keeps track of the removed objects
        List<CalendarFreeSlot> removed = new ArrayList<>();
        while (iteratorA.hasNext()) {
            CalendarFreeSlot a = iteratorA.next();
            if (removed.contains(a)) {
                iteratorA.remove();
                removed.remove(a);
                continue;
            }

            List<CalendarFreeSlot> lookAheadList = freeSlots.subList(++index, freeSlots.size());
            Iterator<CalendarFreeSlot> iteratorB = lookAheadList.iterator();
            while (iteratorB.hasNext()) {
                CalendarFreeSlot b = iteratorB.next();
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
     * Combines the specified {@link CalendarFreeSlot}s
     * 
     * @param freeSlots The {@link CalendarFreeSlot}s to combine
     */
    private List<CalendarFreeSlot> combineSlots(List<CalendarFreeSlot> freeSlots) {
        // Sort by starting date
        java.util.Collections.sort(freeSlots, freeSlotDateTimeComparator);

        List<CalendarFreeSlot> combined = new ArrayList<>(freeSlots.size());
        Iterator<CalendarFreeSlot> iteratorA = freeSlots.iterator();
        int index = 0;
        // Keeps track of the removed objects
        List<CalendarFreeSlot> removed = new ArrayList<>();
        while (iteratorA.hasNext()) {
            CalendarFreeSlot a = iteratorA.next();
            if (removed.contains(a)) {
                iteratorA.remove();
                removed.remove(a);
                continue;
            }

            List<CalendarFreeSlot> lookAheadList = freeSlots.subList(++index, freeSlots.size());
            Iterator<CalendarFreeSlot> iteratorB = lookAheadList.iterator();
            while (iteratorB.hasNext()) {
                CalendarFreeSlot b = iteratorB.next();
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
        java.util.Collections.sort(combined, freeSlotDateTimeComparator);
        return combined;
    }

    ///////////////////////////////////////// Comparators //////////////////////////////////////

    /**
     * {@link DateTimeComparator} - DateTime comparator. Orders {@link CalendarFreeSlot} items
     * by start date (ascending)
     */
    private static class FreeSlotDateTimeComparator implements Comparator<CalendarFreeSlot> {

        /**
         * Initialises a new {@link GetPerformer.DateTimeComparator}.
         */
        public FreeSlotDateTimeComparator() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(CalendarFreeSlot o1, CalendarFreeSlot o2) {
            if (o1.getStartTime().before(o2.getStartTime())) {
                return -1;
            } else if (o1.getStartTime().after(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * {@link DateTimeComparator} - DateTime comparator. Orders {@link CalendarAvailability} items
     * by start date (ascending)
     */
    private static class AvailabilityDateTimeComparator implements Comparator<CalendarAvailability> {

        /**
         * Initialises a new {@link GetPerformer.DateTimeComparator}.
         */
        public AvailabilityDateTimeComparator() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(CalendarAvailability o1, CalendarAvailability o2) {
            if (o1.getStartTime().before(o2.getStartTime())) {
                return -1;
            } else if (o1.getStartTime().after(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * {@link PriorityComparator} - Priority comparator. Orders {@link CalendarAvailability} items
     * by priority (descending). We want elements with higher priority (in this context '1' > '9' > '0')
     * to be on the top of the list.
     */
    private static class PriorityComparator implements Comparator<CalendarAvailability> {

        /**
         * Initialises a new {@link GetPerformer.PriorityComparator}.
         */
        public PriorityComparator() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(CalendarAvailability o1, CalendarAvailability o2) {
            // Use '10' for '0' as '0' has a lower priority than '9' 
            int o1Priority = o1.getPriority() == 0 ? 10 : o1.getPriority();
            int o2Priority = o2.getPriority() == 0 ? 10 : o2.getPriority();

            //We want elements with higher priority (in this context '1' > '9' > '0') to be on the top of the list
            if (o1Priority > o2Priority) {
                return 1;
            } else if (o1Priority < o2Priority) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
