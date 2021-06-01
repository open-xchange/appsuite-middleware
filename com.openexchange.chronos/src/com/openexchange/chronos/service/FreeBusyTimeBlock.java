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

package com.openexchange.chronos.service;

import java.util.ArrayList;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.FreeBusyTime;

/**
 * {@link FreeBusyTimeBlock}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FreeBusyTimeBlock extends ArrayList<FreeBusyTime> {

    private static final long serialVersionUID = 6137703417014122714L;

    private BusyType busyType = BusyType.BUSY_UNAVAILABLE;

    /**
     * Initialises a new {@link FreeBusyTimeBlock}.
     */
    public FreeBusyTimeBlock() {
        super();
    }

    /**
     * Gets the busyType
     *
     * @return The busyType
     */
    public BusyType getBusyType() {
        return busyType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AvailableTime [busyType=").append(busyType).append(", \ntoString()=").append(super.toString()).append("]");
        return builder.toString();
    }
}
