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

package com.openexchange.chronos.availability.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.FbType;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.FreeSlotField;
import com.openexchange.chronos.service.SetResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarAvailabilityServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarAvailabilityServiceImpl implements CalendarAvailabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAvailabilityServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initialises a new {@link CalendarAvailabilityServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public CalendarAvailabilityServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#setAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
    @Override
    public SetResult setAvailability(CalendarSession session, List<CalendarAvailability> availabilities) throws OXException {
        // Pre-condition checks
        check(availabilities);

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#setAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, boolean)
     */
    @Override
    public SetResult setAvailability(CalendarSession session, List<CalendarAvailability> availabilities, boolean merge) throws OXException {
        // Pre-condition checks
        check(availabilities);

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession)
     */
    @Override
    public List<CalendarAvailability> getAvailability(CalendarSession session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession, java.util.Date, java.util.Date)
     */
    @Override
    public List<CalendarAvailability> getAvailability(CalendarSession session, Date from, Date until) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#getAvailability(com.openexchange.chronos.service.CalendarSession, java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public Map<Attendee, List<CalendarAvailability>> getAvailability(CalendarSession session, List<Attendee> attendees, Date from, Date until) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailability(com.openexchange.chronos.service.CalendarSession, java.lang.String)
     */
    @Override
    public void deleteAvailability(CalendarSession session, String availabilityId) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailabilities(com.openexchange.chronos.service.CalendarSession, java.util.List)
     */
    @Override
    public void deleteAvailabilities(CalendarSession session, List<String> availabilityIds) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#deleteAvailabilities(com.openexchange.chronos.service.CalendarSession, java.util.Date, java.util.Date)
     */
    @Override
    public void deleteAvailabilities(CalendarSession session, Date from, Date until) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.CalendarAvailabilityService#purgeAvailabilities(com.openexchange.chronos.service.CalendarSession)
     */
    @Override
    public void purgeAvailabilities(CalendarSession session) throws OXException {
        // TODO Auto-generated method stub

    }

    /**
     * Check the validity of the values of the specified {@link List} with {@link CalendarAvailability} blocks
     * according to the <a href="https://tools.ietf.org/html/rfc7953">RFC-7953</a>
     * 
     * @param availabilities The {@link List} of {@link CalendarAvailability} blocks to check
     * @throws OXException if any of the fields in any of the {@link CalendarAvailability} does not meed
     *             the regulations of the RFC
     */
    private void check(List<CalendarAvailability> availabilities) throws OXException {
        for (CalendarAvailability availability : availabilities) {
            checkMandatory(availability);
            checkConstrains(availability);
            checkRanges(availability);
        }
    }

    /**
     * 
     * @param availability
     * @throws OXException
     */
    private void checkMandatory(CalendarAvailability availability) throws OXException {
        if (availability.contains(AvailabilityField.dtstamp) && availability.contains(AvailabilityField.uid)) {
            return;
        }

        throw new OXException(31145, "Mandatory fields are not set");
    }

    /**
     * 
     * @param availability
     * @throws OXException
     */
    private void checkConstrains(CalendarAvailability availability) throws OXException {
        if (!availability.contains(AvailabilityField.dtstart) && availability.contains(AvailabilityField.duration)) {
            throw new OXException(31145, "The 'duration' field is set, but the 'start' field is not");
        }

        if (availability.contains(AvailabilityField.dtend) && availability.contains(AvailabilityField.duration)) {
            throw new OXException(31145, "The 'duration' field and 'end' field are mutually exclusive");
        }

        for (CalendarFreeSlot freeSlot : availability.getCalendarFreeSlots()) {
            checkMandatory(freeSlot);
            checkConstrains(freeSlot);
        }
    }

    /**
     * 
     * @param availability
     * @throws OXException
     */
    private void checkMandatory(CalendarFreeSlot freeSlot) throws OXException {
        if (freeSlot.contains(FreeSlotField.dtstamp) && freeSlot.contains(FreeSlotField.uid) && freeSlot.contains(FreeSlotField.dtstart)) {
            return;
        }

        throw new OXException(31145, "Mandatory fields are not set");
    }

    /**
     * 
     * @param freeSlot
     * @throws OXException
     */
    private void checkConstrains(CalendarFreeSlot freeSlot) throws OXException {
        if (freeSlot.contains(FreeSlotField.dtend) && freeSlot.contains(FreeSlotField.duration)) {
            throw new OXException(31145, "The 'duration' field and 'end' field are mutually exclusive");
        }
    }

    /**
     * Check ranges
     * 
     * @param availability
     * @throws OXException
     */
    private void checkRanges(CalendarAvailability availability) throws OXException {
        // If "DTSTART" is not present, then the start time is unbounded.
        if (!availability.contains(AvailabilityField.dtstart)) {
            availability.setStartTime(new Date(Long.MAX_VALUE));
            //availability.setStartTimeZone(startTimeZone); //FIXME: set user's timezone?
        }

        // If "DTEND" or "DURATION" are not present, then the end time is unbounded. 
        if (!availability.contains(AvailabilityField.dtend) && !availability.contains(AvailabilityField.duration)) {
            availability.setEndTime(new Date(Long.MAX_VALUE));
            //availability.setEndTimezone(endTimeZone); //FIXME: set user's timezone?
        }

        // Within the specified time period, availability defaults to a free-busy type of "BUSY-UNAVAILABLE" 
        // Furthermore, the values of the 'BUSYTYPE' property correspond to those used by the "FBTYPE" parameter 
        // used on a "FREEBUSY" property, with the exception that the "FREE" value is not used in this property.
        // FIXME: Should we throw an exception instead?
        if (!availability.contains(AvailabilityField.busytype) || availability.getBusyType().equals(FbType.FREE)) {
            availability.setBusyType(FbType.BUSY_UNAVAILABLE);
        }

        // Valid 'PRIORITY' values are 0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        if (availability.contains(AvailabilityField.priority) && (availability.getPriority() < 0 || availability.getPriority() > 9)) {
            throw new OXException(31145, "The 'priority' range is out of bounds.");
        }
    }
}
