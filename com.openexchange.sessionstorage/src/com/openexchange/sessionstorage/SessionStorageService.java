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

package com.openexchange.sessionstorage;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.OptionalService;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.session.SessionAttributes;

/**
 * {@link SessionStorageService} - The session storage service.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
@OptionalService
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
     * Adds specified session to session storage.
     * <p>
     * Any existent session instance with the same session identifier is replaced.
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
     * Applies given attributes to denoted session.
     *
     * @param sessionId The session identifier
     * @param attrs The attributes to set
     * @throws OXException If arguments cannot be set
     */
    void setSessionAttributes(String sessionId, SessionAttributes attrs) throws OXException;

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
