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

package com.openexchange.push;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link PushListenerService} - The singleton push listener service to manually start/stop push listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface PushListenerService {

    /**
     * Starts a new listener for specified session.
     *
     * @param session The session
     * @return A newly started listener or <code>null</code> if a listener could not be started
     * @throws OXException If operation fails
     */
    PushListener startListenerFor(Session session) throws OXException;

    /**
     * Stops the listener for specified session.
     *
     * @param session The session
     * @return <code>true</code> if listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean stopListenerFor(Session session) throws OXException;

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the users with permanent listeners
     *
     * @return The users with permanent listeners
     * @throws OXException If users cannot be returned
     */
    List<PushUser> getUsersWithPermanentListeners() throws OXException;

    /**
     * Has push registration
     *
     * @param pushUser The push user to check
     * @return <code>true</code> if a push registration is available; otherwise <code>false</code>
     * @throws OXException If push registrations cannot be returned
     */
    boolean hasRegistration(PushUser pushUser) throws OXException;

    /**
     * Generates a session for specified push user according to configuration settings/possibilities.
     *
     * @param pushUser The push user
     * @return The generated session
     * @throws OXException If no session can be generated for specified push user
     */
    Session generateSessionFor(PushUser pushUser) throws OXException;

    /**
     * Registers a permanent listener for specified user.
     *
     * @param session The session
     * @param clientId The client identifier
     * @return <code>true</code> if a permanent listener is successfully registered; otherwise <code>false</code> if there is already such a listener
     * @throws OXException If operation fails
     */
    boolean registerPermanentListenerFor(Session session, String clientId) throws OXException;

    /**
     * Unregisters a permanent listener for specified user.
     *
     * @param session The session
     * @param clientId The client identifier
     * @return <code>true</code> if a permanent listener is successfully unregistered; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean unregisterPermanentListenerFor(Session session, String clientId) throws OXException;

}
