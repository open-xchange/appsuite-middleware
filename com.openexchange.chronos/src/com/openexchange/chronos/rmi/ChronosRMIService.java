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

package com.openexchange.chronos.rmi;

import java.rmi.RemoteException;

/**
 * {@link ChronosRMIService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public interface ChronosRMIService extends java.rmi.Remote {

    public static final String RMI_NAME = "ChronosRMIService";

    /**
     * Sets a new organizer for the given event in the given context.
     * If this is performed on a recurring event (master or exception), all exceptions and the master are changed.
     * The new organizer must be an internal user and the old organizer must not be an external user.
     * If the organizer is no attendee, the organizer will automatically be added as attendee.
     * If the organizer is already set but not yet an attendee, the organizer will be added as attendee as well.
     * If the organizer is already set and also an attendee this is a no-op.
     * Bear in mind, that external users/clients may not support organizer changes, thus this operation is not propagated to external attendees.
     * 
     * @param contextId The context identifier
     * @param eventId The event identifier
     * @param userId The user identifier
     * @throws RemoteException If an error occurs
     */
    void setEventOrganizer(int contextId, int eventId, int userId) throws RemoteException;

}
