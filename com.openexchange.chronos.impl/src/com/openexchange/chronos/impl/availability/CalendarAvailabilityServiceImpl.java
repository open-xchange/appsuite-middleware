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

package com.openexchange.chronos.impl.availability;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AvailableTime;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.impl.availability.performer.DeletePerformer;
import com.openexchange.chronos.impl.availability.performer.GetPerformer;
import com.openexchange.chronos.impl.availability.performer.PurgePerformer;
import com.openexchange.chronos.impl.availability.performer.SetPerformer;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SetResult;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#setAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
    @Override
    public SetResult setAvailability(CalendarSession session, final List<CalendarAvailability> availabilities) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<SetResult>(session) {

            @Override
            protected SetResult execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new SetPerformer(storage, session).perform(availabilities);
            }

        }.executeUpdate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession)
     */
    @Override
    public List<CalendarAvailability> getAvailability(CalendarSession session) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<List<CalendarAvailability>>(session) {

            @Override
            protected List<CalendarAvailability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).perform();
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getAvailableTime(com.openexchange.chronos.service.CalendarSession)
     */
    @Override
    public AvailableTime getAvailableTime(CalendarSession session) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<AvailableTime>(session) {

            @Override
            protected AvailableTime execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).getAvailableTime();
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getAvailableTime(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<Attendee, AvailableTime> getAvailableTime(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, AvailableTime>>(session) {

            @Override
            protected Map<Attendee, AvailableTime> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).getAvailableTime(attendees, from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession, java.util.Date, java.util.Date)
     */
    @Override
    public List<CalendarAvailability> getAvailability(CalendarSession session, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<List<CalendarAvailability>>(session) {

            @Override
            protected List<CalendarAvailability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).performInRange(from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getCombinedAvailableTime(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<Attendee, List<CalendarAvailability>> getCombinedAvailableTime(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, List<CalendarAvailability>>>(session) {

            @Override
            protected Map<Attendee, List<CalendarAvailability>> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).getCombinedAvailableTimes(attendees, from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getUserAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<CalendarUser, List<CalendarAvailability>> getUserAvailability(CalendarSession session, final List<CalendarUser> users, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<CalendarUser, List<CalendarAvailability>>>(session) {

            @Override
            protected Map<CalendarUser, List<CalendarAvailability>> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).performForUsers(users, from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<Attendee, List<CalendarAvailability>> getAttendeeAvailability(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, List<CalendarAvailability>>>(session) {

            @Override
            protected Map<Attendee, List<CalendarAvailability>> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).performForAttendees(attendees, from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailability(com.openexchange.chronos.service.CalendarSession, java.lang.String)
     */
    @Override
    public void deleteAvailability(CalendarSession session, String availabilityId) throws OXException {
        deleteAvailabilities(session, Collections.singletonList(availabilityId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailabilities(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
    @Override
    public void deleteAvailabilities(CalendarSession session, final List<String> availabilityIds) throws OXException {
        new AbstractCalendarAvailabilityStorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                new DeletePerformer(storage, session).perform(availabilityIds);
                return null;
            }

        }.executeUpdate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#purgeAvailabilities(com.openexchange.chronos.service.CalendarSession)
     */
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
