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
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.FreeSlotField;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractUpdatePerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractUpdatePerformer extends AbstractPerformer {

    final SetResultImpl result;

    /**
     * Initialises a new {@link AbstractUpdatePerformer}.
     */
    AbstractUpdatePerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);

        result = new SetResultImpl();
    }

    ////////////////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////////////

    /**
     * Prepares the specified {@link List} of {@link Availability} blocks for the storage.
     * <ul>
     * <li>Assigns identifiers for the {@link Availability} blocks (if no identifiers are present)</li>
     * <li>Assigns identifiers for the {@link Available} blocks</li>
     * </ul>
     * 
     * @param storage The {@link CalendarAvailabilityStorage} instance
     * @param availabilities A {@link List} with {@link Availability} blocks to prepare
     * @return The {@link List} with the {@link Availability} identifiers
     * @throws OXException if an error is occurred
     */
    List<String> prepareForStorage(CalendarAvailabilityStorage storage, List<Availability> availabilities) throws OXException {
        Date timeNow = new Date(System.currentTimeMillis());

        List<String> caIds = new ArrayList<>(availabilities.size());
        for (Availability availability : availabilities) {
            String availabilityId = availability.contains(AvailabilityField.id) ? availability.getId() : storage.nextCalendarAvailabilityId();
            availability.setId(availabilityId);
            availability.setCalendarUser(session.getUserId());
            availability.setLastModified(timeNow);
            // Set the creation timestamp (a.k.a. dtstamp) from the last modified if not present
            if (availability.getCreationTimestamp() == null) {
                availability.setCreationTimestamp(timeNow);
            }
            if (availability.getStartTime() == null) {
                availability.setStartTime(new DateTime(0));
            }
            if (availability.getEndTime() == null) {
                availability.setEndTime(new DateTime(9999, 11, 31, 23, 59, 59)); //FIXME: Set MySQL's max value for Date 
            }
            caIds.add(availabilityId);

            // Prepare the free slots
            for (Available freeSlot : availability.getCalendarFreeSlots()) {
                freeSlot.setId(freeSlot.contains(FreeSlotField.id) ? freeSlot.getId() : storage.nextCalendarFreeSlotId());
                freeSlot.setCalendarAvailabilityId(availabilityId);
                freeSlot.setCalendarUser(session.getUserId());
                freeSlot.setLastModified(timeNow);
                // Set the creation timestamp (a.k.a. dtstamp) from the last modified if not present
                if (freeSlot.getCreationTimestamp() == null) {
                    freeSlot.setCreationTimestamp(timeNow);
                }
                if (freeSlot.getStartTime() == null) {
                    freeSlot.setStartTime(new DateTime(0));
                }
                if (freeSlot.getEndTime() == null) {
                    freeSlot.setEndTime(new DateTime(9999, 11, 31, 23, 59, 59)); //FIXME: Set MySQL's max value for Date 
                }
            }
        }
        return caIds;
    }

}
