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

import java.util.List;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.exception.OXException;

/**
 * {@link DeletePerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeletePerformer extends AbstractUpdatePerformer {

    /**
     * Initialises a new {@link DeletePerformer}.
     * 
     * @param storage The storage instance
     * @param session The server session
     */
    public DeletePerformer(CalendarAvailabilityStorage storage, CalendarSession session) {
        super(storage, session);
    }

    /**
     * Performs the operation
     * 
     * @param availableUids
     * @throws OXException
     */
    public void performByUid(List<String> availableUids) throws OXException {
        getStorage().deleteAvailableByUid(availableUids);
    }

    /**
     * Performs the operation
     * 
     * @param availableIds
     * @throws OXException
     */
    public void performById(List<Integer> availableIds) throws OXException {
        getStorage().deleteAvailableById(availableIds);
    }
}
