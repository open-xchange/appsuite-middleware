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
