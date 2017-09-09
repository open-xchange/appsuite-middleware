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

package com.openexchange.chronos.impl.availability.performer;

import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.FieldAware;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.chronos.service.AvailableField;
import com.openexchange.chronos.service.CalendarAvailabilityField;
import com.openexchange.exception.OXException;

/**
 * {@link CheckUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
class CheckUtil {

    /**
     * Defines the lower time bound or the minimum possible date
     */
    static final DateTime MIN_DATE_TIME = new DateTime(0);
    /**
     * Defines the upper time bound or the maximum possible date
     * 
     * @see <a href link=https://dev.mysql.com/doc/refman/5.7/en/datetime.html">https://dev.mysql.com/doc/refman/5.7/en/datetime.html</a>
     */
    static final DateTime MAX_DATE_TIME = new DateTime(9999, 11, 31, 23, 59, 59);

    /**
     * Check the validity of the values of the specified {@link List} with {@link Availability} blocks
     * according to the <a href="https://tools.ietf.org/html/rfc7953">RFC-7953</a>
     * 
     * @param availabilities The {@link List} of {@link Availability} blocks to check
     * @throws OXException if any of the fields in any of the {@link Availability} does not meet
     *             the regulations of the RFC
     */
    static void check(List<Availability> availabilities) throws OXException {
        for (Availability availability : availabilities) {
            check(availability);
        }
    }

    /**
     * Check the validity of the values of the specified {@link Availability} block
     * according to the <a href="https://tools.ietf.org/html/rfc7953">RFC-7953</a>
     * 
     * @param availability The {@link Availability} block to check
     * @throws OXException if any of the fields in any of the {@link Availability} does not meet
     *             the regulations of the RFC
     */
    static void check(Availability availability) throws OXException {
        checkMandatory(availability, AvailabilityField.uid);
        checkConstraints(availability);
        checkRanges(availability);
    }

    /**
     * Check specific constraints that apply to a {@link Availability} DAO
     * 
     * @param availability The {@link Availability} to check
     * @throws OXException if any of the constraints is violated
     */
    private static void checkConstraints(Availability availability) throws OXException {
        if (!availability.contains(AvailabilityField.dtstart) && availability.contains(AvailabilityField.duration)) {
            throw new OXException(31145, "The 'duration' field is set, but the 'start' field is not");
        }

        if (availability.contains(AvailabilityField.dtend) && availability.contains(AvailabilityField.duration)) {
            throw new OXException(31145, "The 'duration' field and 'end' field are mutually exclusive");
        }

        if (availability.getAvailable() == null) {
            return;
        }
        for (Available freeSlot : availability.getAvailable()) {
            checkMandatory(freeSlot, AvailableField.uid, AvailableField.dtstart);
            checkConstraints(freeSlot);
        }
    }

    /**
     * Check specific constraints that apply to a {@link Available} DAO
     * 
     * @param availability The {@link Available} to check
     * @throws OXException if any of the constraints is violated
     */
    private static void checkConstraints(Available freeSlot) throws OXException {
        if (freeSlot.contains(AvailableField.dtend) && freeSlot.contains(AvailableField.duration)) {
            throw new OXException(31145, "The 'duration' field and 'end' field are mutually exclusive");
        }
    }

    /**
     * Checks whether the specified mandatory fields of the specified {@link FieldAware} DAO are present
     * 
     * @param fieldAware The {@link FieldAware} DAO
     * @param mandatoryFields The mandatory fields
     * @throws OXException if any of the specified mandatory fields is not set on the specified {@link FieldAware} DAO
     */
    private static void checkMandatory(FieldAware fieldAware, CalendarAvailabilityField... mandatoryFields) throws OXException {
        for (CalendarAvailabilityField field : mandatoryFields) {
            if (!fieldAware.contains(field)) {
                throw new OXException(31145, "The mandatory field '" + field + "' is not set");
            }
        }
    }

    /**
     * Check ranges
     * 
     * @param availability
     * @throws OXException
     */
    private static void checkRanges(Availability availability) throws OXException {
        // If "DTSTART" is not present, then the start time is unbounded.
        if (!availability.contains(AvailabilityField.dtstart)) {
            availability.setStartTime(MIN_DATE_TIME);
        }

        // If "DTEND" or "DURATION" are not present, then the end time is unbounded. 
        if (!availability.contains(AvailabilityField.dtend) && !availability.contains(AvailabilityField.duration)) {
            availability.setEndTime(MAX_DATE_TIME);
        }

        // Within the specified time period, availability defaults to a free-busy type of "BUSY-UNAVAILABLE" 
        if (!availability.contains(AvailabilityField.busytype)) {
            availability.setBusyType(BusyType.BUSY_UNAVAILABLE);
        }

        // Valid 'PRIORITY' values are 0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        if (availability.contains(AvailabilityField.priority) && (availability.getPriority() < 0 || availability.getPriority() > 9)) {
            throw new OXException(31145, "The 'priority' range is out of bounds.");
        }
    }
}
