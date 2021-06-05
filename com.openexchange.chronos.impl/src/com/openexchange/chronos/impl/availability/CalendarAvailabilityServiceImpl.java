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

package com.openexchange.chronos.impl.availability;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.impl.availability.performer.DeletePerformer;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.chronos.impl.availability.performer.PurgePerformer;
import com.openexchange.chronos.impl.availability.performer.SetPerformer;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarAvailabilityServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarAvailabilityServiceImpl implements CalendarAvailabilityService {

    /**
     * Initialises a new {@link CalendarAvailabilityServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public CalendarAvailabilityServiceImpl() {
        super();
    }

    @Override
    public void setAvailability(CalendarSession session, final Availability availability) throws OXException {
        new AbstractCalendarAvailabilityStorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                new SetPerformer(storage, session).perform(availability);
                return null;
            }
        }.executeUpdate();
    }

    @Override
    public Availability getAvailability(CalendarSession session) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Availability>(session) {

            @Override
            protected Availability execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).perform();
            }

        }.executeQuery();
    }

    @Override
    public Map<Attendee, Availability> getCombinedAvailability(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, Availability>>(session) {

            @Override
            protected Map<Attendee, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).getCombinedAvailability(attendees, from, until);
            }

        }.executeQuery();
    }

    @Override
    public Map<CalendarUser, Availability> getUserAvailability(CalendarSession session, final List<CalendarUser> users, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<CalendarUser, Availability>>(session) {

            @Override
            protected Map<CalendarUser, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).performForUsers(users, from, until);
            }

        }.executeQuery();
    }

    @Override
    public Map<Attendee, Availability> getAttendeeAvailability(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, Availability>>(session) {

            @Override
            protected Map<Attendee, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).performForAttendees(attendees, from, until);
            }

        }.executeQuery();
    }

    @Override
    public void deleteAvailability(CalendarSession session) throws OXException {
        purgeAvailabilities(session);
    }

    @Override
    public void deleteAvailablesByUid(CalendarSession session, final List<String> availableUIds) throws OXException {
        new AbstractCalendarAvailabilityStorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                new DeletePerformer(storage, session).performByUid(availableUIds);
                return null;
            }

        }.executeUpdate();
    }

    @Override
    public void deleteAvailablesById(CalendarSession session, final List<Integer> availableIds) throws OXException {
        new AbstractCalendarAvailabilityStorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                new DeletePerformer(storage, session).performById(availableIds);
                return null;
            }

        }.executeUpdate();
    }

    @Override
    public void purgeAvailabilities(CalendarSession session) throws OXException {
        new AbstractCalendarAvailabilityStorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                new PurgePerformer(storage, session).perform();
                return null;
            }

        }.executeUpdate();
    }
}
