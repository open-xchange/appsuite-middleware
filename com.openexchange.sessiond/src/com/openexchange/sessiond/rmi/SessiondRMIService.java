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

package com.openexchange.sessiond.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link SessiondRMIService} - The RMI service for {@link Session} operations.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface SessiondRMIService extends Remote {

    public static final String RMI_NAME = "SessiondRMIService";

    /**
     * Clears the session with the specified identifier
     * 
     * @param sessionId The session identifier
     * @return <code>true</code> if the sessions was successfully removed; <code>false</code> otherwise
     * @throws RemoteException if the operation fails or any other error is occurred
     */
    boolean clearUserSession(String sessionId) throws RemoteException;

    /**
     * Clears the session with the specified identifier
     * 
     * @param sessionId The session identifier
     * @param global <code>true</code> if the sessions should be cleared globally
     * @return <code>true</code> if the sessions was successfully removed; <code>false</code> otherwise
     * @throws RemoteException if the operation fails or any other error is occurred
     */
    boolean clearUserSession(String sessionId, boolean global) throws RemoteException;

    /**
     * Clears all sessions belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of removed sessions belonging to the user or <code>-1</code> if an error occurred
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    int clearUserSessions(int userId, int contextId) throws RemoteException;

    /**
     * Clears all sessions from cluster belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    void clearUserSessionsGlobally(int userId, int contextId) throws RemoteException;

    /**
     * Clears all sessions belonging to specified context
     *
     * @param contextId The context identifier
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    void clearContextSessions(int contextId) throws RemoteException;

    /**
     * Clears all sessions belonging to given context.
     *
     * @param contextId The context identifier to remove sessions for
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    void clearContextSessionsGlobally(int contextId) throws RemoteException;

    /**
     * Clears all sessions belonging to given contexts.
     *
     * @param contextId The context identifiers to remove sessions for
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    void clearContextSessionsGlobally(Set<Integer> contextIds) throws RemoteException;

    /**
     * Clear all sessions in central session storage. This does not affect the local short term session container.
     * 
     * @throws RemoteException If the operation fails or any other error is occurred
     */
    void clearSessionStorage() throws RemoteException;
}
