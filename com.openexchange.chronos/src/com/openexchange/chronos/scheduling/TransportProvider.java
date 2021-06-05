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

package com.openexchange.chronos.scheduling;

import com.openexchange.annotation.NonNull;
import com.openexchange.osgi.Ranked;
import com.openexchange.session.Session;

/**
 * {@link TransportProvider} - Interface for the different transport the {@link SchedulingBroker} can delegate messages to
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public interface TransportProvider extends Ranked {

    /**
     * Handles a scheduling event.
     * 
     * @param session The {@link Session}
     * @param message The {@link SchedulingMessage}
     * @return A {@link ScheduleStatus}
     */
    @NonNull
    ScheduleStatus send(@NonNull Session session, @NonNull SchedulingMessage message);
    
    /**
     * Handles a scheduling event.
     * 
     * @param session The {@link Session}
     * @param message The {@link ChangeNotification}
     * @return A {@link ScheduleStatus}
     */
    @NonNull
    ScheduleStatus send(@NonNull Session session, @NonNull ChangeNotification message);

}
