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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SessionStorageService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public interface SessionStorageService {
    
    /**
     * Gets the session associated with given session Id
     * @param sessionId The session Id
     * @return The session associated with given session Id
     * @throws OXException If no session with given session Id found
     */
    public Session lookupSession(String sessionId) throws OXException;
    
    /**
     * Adds a new session to session storage
     * @param session The session
     * @throws OXException If adding session failed, e.g. duplicate session
     */
    public void addSession(Session session) throws OXException;
    
    /**
     * Remove the session with given session Id from session storage
     * @param sessionId The session Id
     * @throws OXException If no session with given session Id found
     */
    public void removeSession(String sessionId) throws OXException;
    
    /**
     * Remove all sessions for user with given user Id and context Id from session storage
     * @param userId The user Id
     * @param contextId The context Id
     * @throws OXException If remove of user sessions failed
     */
    public void removeUserSessions(int userId, int contextId) throws OXException;
    
    /**
     * Remove all sessions from context with given context Id
     * @param contextId The context Id
     * @throws OXException If remove of context sessions failed
     */
    public void removeContextSessions(int contextId) throws OXException;
    
    /**
     * Check for active sessions in context with given context Id
     * @param contextId The context Id
     * @return <code>true</code> if context has active sessions, otherwise <code>false</code>
     * @throws OXException
     */
    public boolean hasForContext(int contextId) throws OXException;
    
    /**
     * Get all sessions from user with given user Id and context Id
     * @param userId The user Id
     * @param contextId The context Id
     * @return Array of all user sessions
     * @throws OXException If there are no sessions from this user
     */
    public Session[] getUserSessions(int userId, int contextId) throws OXException;
    
    /**
     * Get an active session from user with given user Id and context Id
     * @param userId The user Id
     * @param contextId The context Id
     * @return An active session from the user
     * @throws OXException If there are no sessions from this user
     */
    public Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException;
    
    /**
     * Get the first found session from user with given user Id and context Id
     * @param userId The user Id
     * @param contextId The context Id
     * @return The user session
     * @throws OXException If there are no sessions from this user
     */
    public Session findFirstSessionForUser(int userId, int contextId) throws OXException;
    
    /**
     * Get a list of all stored sessions
     * @return A list of all stored sessions
     */
    public List<Session> getSessions();
    
    /**
     * Get the number of active sessions in session storage
     * @return The number of active sessions
     */
    public int getNumberOfActiveSessions();
    
    /**
     * Get session identified by given random token
     * @param randomToken The random token
     * @param newIP New IP of client
     * @return The session identified by given random token
     * @throws OXException If no session with given random token found
     */
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException;
    
    /**
     * Get session identified by given alternative Id
     * @param altId The alternative Id
     * @return The session identified by given alternative Id
     * @throws OXException If no session with given alternative Id found
     */
    public Session getSessionByAlternativeId(String altId) throws OXException;
    
    /**
     * Gets the session associated with given session Id
     * @param sessionId The session Id
     * @return The session associated with given session Id
     * @throws OXException If no session with given session Id found
     */
    public Session getCachedSession(String sessionId) throws OXException;
    
    /**
     * Clean up session storage
     * @throws OXException If an error occurs during cleanup
     */
    public void cleanUp() throws OXException;
    
    /**
     * Change password in session with given session Id
     * @param sessionId The session Id
     * @param newPassword The new password
     * @throws OXException If changing password failed
     */
    public void changePassword(String sessionId, String newPassword) throws OXException;
    
    
}
