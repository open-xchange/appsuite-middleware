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

package com.openexchange.chronos.impl.availability.performer;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
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
            reverseLookup.put(I(a.getEntity()), a);
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
                reverseLookup.put(I(u.getEntity()), u);
                filtered.add(I(u.getEntity()));
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
    public Availability getCombinedAvailableTime() throws OXException {
        int userId = getSession().getUserId();
        Availability availability = prepareForDelivery(getStorage().loadAvailable(userId));
        availability.setAvailable(combine(availability.getAvailable()));
        return availability;
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
            availability.setAvailable(combine(availability.getAvailable()));
            availableTimes.put(attendee, availability);
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
            T type = reverseLookup.get(Integer.valueOf(a.getCalendarUser()));
            Collections.put(map, type, a);
        }
        Map<T, Availability> availabilities = new HashMap<>();
        for (Map.Entry<T, List<Available>> entry : map.entrySet()) {
            availabilities.put(entry.getKey(), prepareForDelivery(entry.getValue()));
        }
        return availabilities;
    }

    /**
     * Combines the specified {@link Available}s
     *
     * @param available The {@link Available}s to combine
     */
    private List<Available> combine(List<Available> available) {
        // Sort by starting date
        java.util.Collections.sort(available, Comparators.AVAILABLE_DATE_TIME_COMPARATOR);

        List<Available> combined = new ArrayList<>(available.size());
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
            combined.add(a);
        }
        // Sort by starting date
        java.util.Collections.sort(combined, Comparators.AVAILABLE_DATE_TIME_COMPARATOR);
        return combined;
    }
}
