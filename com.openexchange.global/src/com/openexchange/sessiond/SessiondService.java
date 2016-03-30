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

package com.openexchange.sessiond;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link SessiondService} - The SessionD service.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface SessiondService {

    /**
     * The reference to {@link SessiondService} instance.
     */
    public static final AtomicReference<SessiondService> SERVICE_REFERENCE = new AtomicReference<SessiondService>();

    /**
     * Creates a new session object in the SessionD storage with the given session parameters.
     * <p>
     *
     * @param parameterObject The parameter object describing the session to create
     * @return The session object interface of the newly created session.
     * @throws OXException If creating the session fails
     */
    public Session addSession(AddSessionParameter parameterObject) throws OXException;

    /**
     * Stores the session associated with given identifier into session storage (if such a session exists).
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if stored; otherwise <code>false</code>
     * @throws OXException If storing the session fails
     */
    public boolean storeSession(String sessionId) throws OXException;

    /**
     * Replaces the currently stored password in session identified through given session identifier with specified <code>newPassword</code>.
     *
     * @param sessionId The session identifier
     * @param newPassword The new password to apply
     * @throws OXException If new password cannot be applied or corresponding session does not exist or is expired
     */
    public void changeSessionPassword(String sessionId, String newPassword) throws OXException;

    /**
     * Removes the session with the given session identifier.
     *
     * @param sessionId The Session identifier
     * @return <code>true</code> if the session was removed or <code>false</code> if the session identifier doesn't exist
     */
    public boolean removeSession(String sessionId);

    /**
     * Removes all sessions belonging to given user in specified context.
     *
     * @param userId The user identifier
     * @param ctx The context
     * @return The number of removed session or zero if no session was removed
     */
    public int removeUserSessions(int userId, Context ctx);

    /**
     * Removes all sessions belonging to given context.
     *
     * @param contextId The context identifier
     */
    public void removeContextSessions(int contextId);

    /**
     * Removes all sessions belonging to given contexts from this and all other cluster nodes.
     *
     * @param contextIds - Set with the context identifiers to remove sessions for
     * @throws OXException - if removing session fails on one of the remote nodes
     */
    public void removeContextSessionsGlobal(Set<Integer> contextIds) throws OXException;

    /**
     * Removes all sessions belonging to given contexts from this and all other cluster nodes.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If removing session fails on one of the remote nodes
     */
    public void removeUserSessionsGlobally(int userId, int contextId) throws OXException;

    /**
     * Removes all sessions which match the given {@link SessionFilter}. The filter is matched against all sessions in the
     * (hazelcast-)cluster.
     *
     * @param filter The filter
     * @return The IDs of the removed sessions, possibly empty but never <code>null</code>
     * @throws OXException If an error occurs while removing
     */
    Collection<String> removeSessionsGlobally(SessionFilter filter) throws OXException;

    /**
     * Gets the number of active sessions belonging to given user in specified context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The number of active sessions belonging to given user in specified context
     */
    public int getUserSessions(int userId, int contextId);

    /**
     * Gets the <b>local-only</b> sessions associated with specified user in given context.
     * <p>
     * <b>Note</b>: Remote sessions are not considered by this method.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The <b>local-only</b> sessions associated with specified user in given context
     */
    public Collection<Session> getSessions(int userId, int contextId);

    /**
     * Finds the first session of the specified user that matches the give criterion.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param matcher The session matcher
     * @return The first matching session or <code>null</code> if none matches
     */
    public Session findFirstMatchingSessionForUser(int userId, int contextId, SessionMatcher matcher);

    /**
     * Get the session object related to the given session identifier.
     *
     * @param sessionId The Session identifier
     * @return Returns the session or <code>null</code> if no session exists for the given identifier or if the session is expired
     * @see SessiondServiceExtended#getSession(String, boolean)
     */
    public Session getSession(String sessionId);

    /**
     * Get the session object related to the given alternative identifier.
     *
     * @param altId The alternative identifier
     * @return Return the session object or null if no session exists for the given alternative identifier or if the session is expired
     */
    public Session getSessionByAlternativeId(String altId);

    /**
     * Get the session object related to the given alternative identifier.
     *
     * @param altId The alternative identifier
     * @param lookupSessionStorage Whether to allow to look-up session storage, too
     * @return Return the session object or null if no session exists for the given alternative identifier or if the session is expired
     */
    public Session getSessionByAlternativeId(String altId, boolean lookupSessionStorage);

    /**
     * Get the session object related to the given random token.
     *
     * @param randomToken The random token of the session
     * @param localIp The new local IP to apply to session; pass <code>null</code> to not replace existing IP in session
     * @return The session object or <code>null</code> if no session exists for the given random token or if the random token is already expired
     */
    public Session getSessionByRandomToken(String randomToken, String localIp);

    /**
     * Get the session object related to the given random token.
     *
     * @param randomToken The random token of the session
     * @return The session object or <code>null</code> if no session exists for the given random token or if the random token is already expired
     */
    public Session getSessionByRandomToken(String randomToken);

    /**
     * Picks up the session associated with the given client and server token. If a session exists for the given tokens and both tokens
     * match, the session object is put into the normal session container and into the session storage. It is removed from the session
     * container with tokens so a second request with the same tokens will fail.
     * @param clientToken Client side token passed within the {@link #addSession(AddSessionParameter)} call.
     * @param serverToken Server side token returned inside the session from the {@link #addSession(AddSessionParameter)} call.
     * @return the matching session
     * @throws OXException if one of the tokens does not match.
     */
    Session getSessionWithTokens(String clientToken, String serverToken) throws OXException;

    /**
     * Returns all session IDs whose sessions match the given {@link SessionFilter}. The filter is matched against all sessions in the
     * <b>local</b> containers.
     *
     * @param filter The filter
     * @return The IDs of the found sessions, possibly empty but never <code>null</code>
     * @throws OXException If an error occurs while filtering
     */
    Collection<String> findSessions(SessionFilter filter) throws OXException;

    /**
     * Returns all session IDs whose sessions match the given {@link SessionFilter}. The filter is matched against all sessions in the
     * (hazelcast-)cluster.
     *
     * @param filter The filter
     * @return The IDs of the found sessions, possibly empty but never <code>null</code>
     * @throws OXException If an error occurs while filtering
     */
    Collection<String> findSessionsGlobally(SessionFilter filter) throws OXException;

    /**
     * Gets the number of active sessions.
     *
     * @return The number of active sessions
     */
    public int getNumberOfActiveSessions();

    /**
     * Gets the first session that matches the given userId and contextId.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public Session getAnyActiveSessionForUser(int userId, int contextId);

    /**
     * Sets the local IP address for denoted session.
     *
     * @param sessionId The session Id
     * @param localIp The new local IP address
     * @throws OXException If changing local IP address fails or any reason
     */
    public void setLocalIp(String sessionId, String localIp) throws OXException;

    /**
     * Sets the client identifier for denoted session.
     *
     * @param sessionId The session Id
     * @param client The new client identifier
     * @throws OXException If changing client identifier fails or any reason
     */
    public void setClient(String sessionId, String client) throws OXException;

    /**
     * Sets the hash identifier for denoted session.
     *
     * @param sessionId The session Id
     * @param client The new hash identifier
     * @throws OXException If changing hash identifier fails or any reason
     */
    public void setHash(String sessionId, String hash) throws OXException;

}
