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

package com.openexchange.websockets;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link WebSocketService} - The service for sending/receiving data to/from established Web Socket connections.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
@SingletonService
public interface WebSocketService {

    /**
     * Checks if there is any open Web Socket connection associated with specified user; either locally or on any remote cluster member.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if there is an open Web Socket; otherwise <code>false</code>
     * @throws OXException If availability check fails
     */
    boolean exists(int userId, int contextId) throws OXException;

    /**
     * Checks if there is any filter-satisfying Web Socket connection associated with specified user on a remote cluster member.
     *
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>) or <code>null</code> to not filter at all
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a filter-satisfying Web Socket exists; otherwise <code>false</code>
     * @throws OXException If availability check fails
     */
    boolean exists(String pathFilter, int userId, int contextId) throws OXException;

    // --------------------------------------------------------------------------------------------------------------

    /**
     * Sends a text message to denoted user's remote end-points, blocking until all of the message has been transmitted.
     *
     * @param message The message to be sent
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If there is a problem delivering the message.
     */
    void sendMessage(String message, int userId, int contextId) throws OXException;

    /**
     * Initiates the asynchronous transmission of a text message to denoted user's remote end-points.
     * <p>
     * This method returns before the message is transmitted.
     *
     * @param message The message being sent.
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The handler which will be notified of progress.
     */
    SendControl sendMessageAsync(String message, int userId, int contextId) throws OXException;

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Sends a text message to those end-points of the denoted user having its {@link WebSocket#getPath() path} starting with given filter.
     * <p>
     * This method blocks until all of the message has been transmitted.
     *
     * <pre>
     * Examples:
     *   Filter "/websockets/push"
     *    matches:
     *     "/websockets/push"
     *    does not match:
     *     "/websockets/push/foo"
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "/websockets/push/*"
     *    matches:
     *     "/websockets/push"
     *     "/websockets/push/foo"
     *    does not match:
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "*"
     *    matches all
     * </pre>
     *
     * @param message The message to be sent
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>); if <code>null</code> it is the same behavior as {@link #sendMessage(String, int, int)}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If there is a problem delivering the message.
     */
    void sendMessage(String message, String pathFilter, int userId, int contextId) throws OXException;

    /**
     * Initiates the asynchronous transmission of a text message to those end-points of the denoted user having its {@link WebSocket#getPath() path} starting with given filter.
     * <p>
     * This method returns before the message is transmitted.
     *
     * <pre>
     * Examples:
     *   Filter "/websockets/push"
     *    matches:
     *     "/websockets/push"
     *    does not match:
     *     "/websockets/push/foo"
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "/websockets/push/*"
     *    matches:
     *     "/websockets/push"
     *     "/websockets/push/foo"
     *    does not match:
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "*"
     *    matches all
     * </pre>
     *
     * @param message The message being sent.
     * @param pathFilter The path to filter by (e.g. <code>"/websockets/push"</code>); if <code>null</code> it is the same behavior as {@link #sendMessageAsync(String, int, int)}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The handler which will be notified of progress.
     */
    SendControl sendMessageAsync(String message, String pathFilter, int userId, int contextId) throws OXException;

}
