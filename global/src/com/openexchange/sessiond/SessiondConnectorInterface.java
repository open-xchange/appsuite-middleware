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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.sessiond.exception.SessiondException;

/**
 * SessiondConnectorInterface
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public interface SessiondConnectorInterface {
	
	/**
	 * Creates a new session object in the sessiond storage with the given session parameters
	 * 
	 * @param userId -
	 *            The User ID
	 * @param loginName -
	 *            The login name of the user
	 * @param password -
	 *            The password of the user
	 * @param context -
	 *            The context of the user
	 * @param clientHost -
	 *            The hostname of the incoming client
	 * @return The session ID of the newly created session as a <code>String</code>
	 * @throws SessiondException
	 */
    public String addSession(final int userId, final String loginName, final String password, final Context context, final String clientHost) throws SessiondException;
    
	/**
	 * Refreshes the timestamp of the session with the given session ID
	 * 
	 * @param sessionId -
	 *            The Session ID
	 * @return True if the session timestamp was updated or false if the session ID was invalid or exired
	 */
    public boolean refreshSession(final String sessionId);
    
	/**
	 * Removes the session with the given session ID
	 * 
	 * @param sessionId -
	 *            The Session ID
	 * @return True if the session was removed or false if the session ID doesn't exist
	 */
    public boolean removeSession(final String sessionId);
    
	/**
	 * Get the session object related to the given session ID
	 * 
	 * @param sessionId -
	 *            The Session ID
	 * @return Return the session object or null if no session exists for the given ID or if the session is expired
	 */
    public Session getSession(final String sessionId);

	/**
	 * Get the session object related to the given random token
	 * 
	 * @param randomToken -
	 *            The random token of the session
	 * @return Return the session object or null if no session exists for the given random token or if 
	 * the random token is already expired
	 */
    public Session getSessionByRandomToken(final String randomToken);
    
	/**
	 * The number of active sessions
	 * 
	 * @return Return the number of active sessions 
	 */
    public int getNumberOfActiveSessions();

} 
