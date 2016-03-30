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

package com.openexchange.sessionstorage;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SessionStorageService} - The session storage service.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionStorageService {

    /**
     * Gets the session associated with given session identifier
     *
     * @param sessionId The session identifier
     * @return The session associated with given session identifier
     * @throws OXException If no session with given session Id found
     * @see SessionStorageExceptionCodes#NO_SESSION_FOUND
     */
    Session lookupSession(String sessionId) throws OXException;

    /**
     * Gets the session associated with given session identifier
     *
     * @param sessionId The session identifier
     * @param timeoutMillis The timeout in milliseconds to await the look-up to return; a timeout of less than equal to zero is similar to {@link #lookupSession(String)} semantic
     * @return The session associated with given session identifier
     * @throws OXException If no session with given session identifier found or timeout elapsed
     * @see SessionStorageExceptionCodes#NO_SESSION_FOUND
     */
    Session lookupSession(String sessionId, long timeoutMillis) throws OXException;

    /**
     * Adds a new session to session storage.
     *
     * @param session The session
     * @throws OXException If adding session failed, e.g. duplicate session
     */
    void addSession(Session session) throws OXException;

    /**
     * Adds given sessions to session storage if not already contained.
     *
     * @param sessions The sessions
     * @throws OXException If adding sessions fails for some reason
     */
    void addSessionsIfAbsent(Collection<Session> sessions) throws OXException;

    /**
     * Adds given session to session storage if not already contained.
     *
     * @param session The session
     * @throws OXException If adding session fails for some reason
     * @return <code>true</code> if session could be added; otherwise <code>false</code> if such a session is already present
     */
    boolean addSessionIfAbsent(Session session) throws OXException;

    /**
     * Removes the session with given session Id from session storage
     *
     * @param sessionId The session Id
     * @throws OXException If no session with given session Id found
     */
    void removeSession(String sessionId) throws OXException;

    /**
     * Removes the sessions with given session Ids from session storage
     *
     * @param sessionIds The session Ids to remove
     * @return List with removed sessions
     * @throws OXException If no session with given session Id found
     */
    List<Session> removeSessions(List<String> sessionIds) throws OXException;

    /**
     * Removes all locally available sessions for the denoted user from session storage.
     *
     * @param userId The user Id
     * @param contextId The context Id
     * @throws OXException If remove of user sessions failed
     */
    Session[] removeLocalUserSessions(int userId, int contextId) throws OXException;

    /**
     * Removes all sessions for the denoted user from session storage.
     *
     * @param userId The user Id
     * @param contextId The context Id
     * @throws OXException If remove of user sessions failed
     */
    Session[] removeUserSessions(int userId, int contextId) throws OXException;

    /**
     * Removes all locally available sessions for the denoted context from session storage.
     *
     * @param contextId The context Id
     * @throws OXException If remove of context sessions failed
     */
    void removeLocalContextSessions(int contextId) throws OXException;

    /**
     * Removes all sessions for the denoted context from session storage.
     *
     * @param contextId The context Id
     * @throws OXException If remove of context sessions failed
     */
    void removeContextSessions(int contextId) throws OXException;

    /**
     * Checks for active sessions in context with given context Id
     *
     * @param contextId The context Id
     * @return <code>true</code> if context has active sessions, otherwise <code>false</code>
     * @throws OXException
     */
    boolean hasForContext(int contextId) throws OXException;

    /**
     * Gets all sessions from user with given user Id and context Id
     *
     * @param userId The user Id
     * @param contextId The context Id
     * @return Array of all user sessions
     * @throws OXException If there are no sessions from this user
     */
    Session[] getUserSessions(int userId, int contextId) throws OXException;

    /**
     * Gets an active session from user with given user Id and context Id
     *
     * @param userId The user Id
     * @param contextId The context Id
     * @return An active session from the user
     * @throws OXException If there are no sessions from this user
     */
    Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException;

    /**
     * Gets the first found session from user with given user Id and context Id
     *
     * @param userId The user Id
     * @param contextId The context Id
     * @return The user session
     * @throws OXException If there are no sessions from this user
     */
    Session findFirstSessionForUser(int userId, int contextId) throws OXException;

    /**
     * Gets a list of all stored sessions
     *
     * @return A list of all stored sessions
     */
    List<Session> getSessions();

    /**
     * Gets the number of active sessions in session storage
     *
     * @return The number of active sessions
     */
    int getNumberOfActiveSessions();

    /**
     * Gets session identified by given random token
     *
     * @param randomToken The random token
     * @param newIP New IP of client
     * @return The session identified by given random token
     * @throws OXException If no session with given random token found
     */
    Session getSessionByRandomToken(String randomToken, String newIP) throws OXException;

    /**
     * Gets session identified by given alternative Id
     *
     * @param altId The alternative Id
     * @return The session identified by given alternative Id
     * @throws OXException If no session with given alternative Id found
     */
    Session getSessionByAlternativeId(String altId) throws OXException;

    /**
     * Gets the session associated with given session Id
     *
     * @param sessionId The session Id
     * @return The session associated with given session Id
     * @throws OXException If no session with given session Id found
     */
    Session getCachedSession(String sessionId) throws OXException;

    /**
     * Changes password in session with given session Id
     *
     * @param sessionId The session Id
     * @param newPassword The new password
     * @throws OXException If changing password fails or any reason
     */
    void changePassword(String sessionId, String newPassword) throws OXException;

    /**
     * Sets the local IP address for denoted session.
     *
     * @param sessionId The session Id
     * @param localIp The new local IP address
     * @throws OXException If changing local IP address fails or any reason
     */
    void setLocalIp(String sessionId, String localIp) throws OXException;

    /**
     * Sets the client identifier for denoted session.
     *
     * @param sessionId The session Id
     * @param client The new client identifier
     * @throws OXException If changing client identifier fails or any reason
     */
    void setClient(String sessionId, String client) throws OXException;

    /**
     * Sets the hash identifier for denoted session.
     *
     * @param sessionId The session Id
     * @param client The new hash identifier
     * @throws OXException If changing hash identifier fails or any reason
     */
    void setHash(String sessionId, String hash) throws OXException;

    /**
     * Checks authId for duplicates
     *
     * @param login Login name to check
     * @param authId AuthId to check
     * @throws OXException If duplicate found
     */
    void checkAuthId(String login, String authId) throws OXException;

    /**
     * Cleans up session storage
     *
     * @throws OXException On error while clearing
     */
    void cleanUp() throws OXException;

    /**
     * Gets the number of currently stored sessions of a user.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of sessions
     * @throws OXException
     */
    int getUserSessionCount(int userId, int contextId) throws OXException;

}
