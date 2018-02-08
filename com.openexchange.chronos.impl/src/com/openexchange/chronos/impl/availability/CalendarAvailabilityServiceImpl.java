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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#setAvailability(com.openexchange.chronos.service.CalendarSession, com.openexchange.chronos.Availability)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession)
     */
    @Override
    public Availability getAvailability(CalendarSession session) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Availability>(session) {

            @Override
            protected Availability execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).perform();
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getCombinedAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<Attendee, Availability> getCombinedAvailability(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, Availability>>(session) {

            @Override
            protected Map<Attendee, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
                return new GetPerformer(storage, session).getCombinedAvailability(attendees, from, until);
            }

        }.executeQuery();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#getUserAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<CalendarUser, Availability> getUserAvailability(CalendarSession session, final List<CalendarUser> users, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<CalendarUser, Availability>>(session) {

            @Override
            protected Map<CalendarUser, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
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
    public Map<Attendee, Availability> getAttendeeAvailability(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new AbstractCalendarAvailabilityStorageOperation<Map<Attendee, Availability>>(session) {

            @Override
            protected Map<Attendee, Availability> execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException {
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
    public void deleteAvailability(CalendarSession session) throws OXException {
        purgeAvailabilities(session);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailabilities(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.CalendarAvailabilityService#deleteAvailablesById(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
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
