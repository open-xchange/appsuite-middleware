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
 *    trademarks of the OX Software GmbH. group of companies.
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
