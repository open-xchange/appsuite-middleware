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
import java.util.List;
import com.openexchange.session.Session;

/**
 * {@link SessiondServiceExtended} - The extended {@link SessiondService SessionD service}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessiondServiceExtended extends SessiondService {

    /**
     * Checks for any active session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if at least one active session is found; otherwise <code>false</code>
     */
    boolean hasForContext(final int contextId);

    /**
     * Checks if denoted session is <code>locally</code> available and located in short-term container.
     *
     * @param sessionId The session identifier
     * @return <code>true</code> if <code>locally</code> active; otherwise <code>false</code>
     */
    boolean isActive(String sessionId);

    /**
     * Gets a list of <i>active</i> sessions, i.e. those sessions that are <code>locally</code> available and located in one of the
     * short-term containers.
     *
     * @return The identifiers of all active sessions in a list
     */
    List<String> getActiveSessionIDs();

    /**
     * Get the session object related to the given session identifier.
     *
     * @param sessionId The Session identifier
     * @param considerSessionStorage <code>true</code> to consider session storage for possible distributed session; otherwise
     *            <code>false</code>
     * @return Returns the session or <code>null</code> if no session exists for the given identifier or if the session is expired
     */
    Session getSession(String sessionId, boolean considerSessionStorage);

    /**
     * Gets the sessions associated with specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param considerSessionStorage <code>true</code> to also consider session storage; otherwise <code>false</code>
     * @return The <b>local-only</b> sessions associated with specified user in given context
     */
    Collection<Session> getSessions(int userId, int contextId, boolean considerSessionStorage);

    /**
     * Checks if specified session is applicable for session storage.
     *
     * @param session The session to check
     * @return <code>true</code> if applicable for session storage; otherwise <code>false</code>
     */
    boolean isApplicableForSessionStorage(Session session);

}
