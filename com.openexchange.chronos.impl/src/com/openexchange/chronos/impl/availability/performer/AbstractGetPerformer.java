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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;

/**
 * {@link AbstractGetPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractGetPerformer extends AbstractPerformer {

    /**
     * Initialises a new {@link AbstractGetPerformer}.
     * 
     * @param storage The {@link CalendarAvailabilityStorage}
     * @param session The groupware {@link CalendarSession}
     */
    public AbstractGetPerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    /**
     * Prepares the specified {@link Available} blocks for delivery by wrapping them
     * into an {@link Availability} container
     * 
     * @param available The {@link List} with the {@link Available} blocks to prepare
     * @return The {@link Availability} object
     */
    public Availability prepareForDelivery(List<Available> available) {
        Organizer organizer = new Organizer();
        organizer.setEntity(getSession().getUserId());
        organizer.setUri(ResourceId.forUser(getSession().getContextId(), getSession().getUserId()));

        Availability availability = new Availability();
        availability.setCalendarUser(getSession().getUserId());
        availability.setCreationTimestamp(new Date(System.currentTimeMillis()));
        availability.setOrganizer(organizer);
        availability.setAvailable(available);
        availability.setUid(UUID.randomUUID().toString());
        // Set start and end times to "infinity"
        availability.setStartTime(CheckUtil.MIN_DATE_TIME);
        availability.setEndTime(CheckUtil.MAX_DATE_TIME);

        return availability;
    }
}
