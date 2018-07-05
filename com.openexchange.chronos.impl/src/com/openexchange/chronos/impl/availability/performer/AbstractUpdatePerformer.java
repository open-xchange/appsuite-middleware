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
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.service.AvailableField;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractUpdatePerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractUpdatePerformer extends AbstractPerformer {

    /**
     * Initialises a new {@link AbstractUpdatePerformer}.
     */
    AbstractUpdatePerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    ////////////////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////////////

    /**
     * Prepares the specified {@link List} of {@link Availability} blocks for the storage.
     * <ul>
     * <li>Assigns identifiers for the {@link Available} blocks</li>
     * </ul>
     * 
     * @param storage The {@link CalendarAvailabilityStorage} instance
     * @param availabilities A {@link List} with {@link Availability} blocks to prepare
     * @return The {@link List} with the {@link Availability} identifiers
     * @throws OXException if an error is occurred
     */
    List<String> prepareForStorage(CalendarAvailabilityStorage storage, List<Availability> availabilities) throws OXException {
        List<String> caIds = new ArrayList<>(availabilities.size());
        for (Availability availability : availabilities) {
            prepareForStorage(storage, availability);
        }
        return caIds;
    }

    /**
     * Prepares the specified {@link Availability} block for the storage.
     * <ul>
     * <li>Assigns identifiers for the {@link Available} blocks</li>
     * </ul>
     * 
     * @param storage The {@link CalendarAvailabilityStorage} instance
     * @param availability An {@link Availability} block to prepare
     * @throws OXException if an error is occurred
     */
    void prepareForStorage(CalendarAvailabilityStorage storage, Availability availability) throws OXException {
        Date timeNow = new Date(System.currentTimeMillis());
        // Prepare the free slots
        for (Available available : availability.getAvailable()) {
            available.setId(available.contains(AvailableField.id) ? available.getId() : storage.nextAvailableId());
            available.setCalendarUser(getSession().getUserId());
            // Set the creation timestamp (a.k.a. dtstamp) from the last modified if not present
            available.setLastModified(timeNow);
            if (available.getCreated() == null) {
                available.setCreated(timeNow);
            }
            if (available.getCreationTimestamp() == null) {
                available.setCreationTimestamp(timeNow);
            }
            if (available.getStartTime() == null) {
                available.setStartTime(CheckUtil.MIN_DATE_TIME);
            }
            if (available.getEndTime() == null) {
                available.setEndTime(CheckUtil.MAX_DATE_TIME);
            }
        }
    }

}
