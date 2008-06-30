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

package com.openexchange.sessiond.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.session.Session;

/**
 * {@link SessionContainer} - A container for {@link Session} objects wrapped by
 * a {@link SessionControl} object.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
final class SessionContainer {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SessionContainer.class);

	private static final Integer DUMMY = Integer.valueOf(1);

	private final Map<String, SessionControl> sessionIdMap;

	private final Map<UserKey, Map<String, Integer>> userSessions;

	/**
	 * Initializes a new {@link SessionContainer session container}.
	 * 
	 * @param maxSessions
	 *            The max. number of sessions kept by this container
	 */
	SessionContainer(final int maxSessions) {
		super();
		sessionIdMap = new ConcurrentHashMap<String, SessionControl>(maxSessions);
		userSessions = new ConcurrentHashMap<UserKey, Map<String, Integer>>(maxSessions);
	}

	/**
	 * Gets the current number of sessions held by this container
	 * 
	 * @return The current number of sessions held by this container
	 */
	int size() {
		return sessionIdMap.size();
	}

	/**
	 * Checks if this container contains an entry for specified session ID
	 * 
	 * @param sessionId
	 *            The session ID
	 * @return <code>true</code> if this container contains an entry for
	 *         specified session ID; otherwise <code>false</code>
	 */
	boolean containsSessionId(final String sessionId) {
		return sessionIdMap.containsKey(sessionId);
	}

	/**
	 * Checks if this container contains a session for specified user in
	 * specified context
	 * 
	 * @param userId
	 *            The user ID
	 * @param contextId
	 *            The context ID
	 * @return <code>true</code> if this container contains an entry for
	 *         specified user; otherwise <code>false</code>
	 */
	boolean containsUser(final int userId, final int contextId) {
		return userSessions.containsKey(new UserKey(userId, contextId));
	}

	/**
	 * Gets the number of sessions bound to specified user in specified context
	 * 
	 * @param userId
	 *            The user ID
	 * @param contextId
	 *            The context ID
	 * @return The number of sessions bound to specified user in specified
	 *         context
	 */
	int numOfUserSessions(final int userId, final int contextId) {
		final Map<String, Integer> sessionIds = userSessions.get(new UserKey(userId, contextId));
		if (sessionIds == null) {
			return 0;
		}
		return sessionIds.size();
	}

	/**
	 * Gets the session bound to specified session ID.
	 * 
	 * @param sessionId
	 *            The session ID
	 * @return The session bound to specified session ID, or <code>null</code>
	 *         if there's no session for specified session ID.
	 */
	SessionControl getSessionById(final String sessionId) {
		return sessionIdMap.get(sessionId);
	}

	/**
	 * Gets the sessions bound to specified user ID and context ID.
	 * 
	 * @param userId
	 *            The user ID
	 * @param contextId
	 *            The context ID
	 * @return The sessions bound to specified user ID and context ID
	 */
	SessionControl[] getSessionsByUser(final int userId, final int contextId) {
		final Map<String, Integer> sessionIds = userSessions.get(new UserKey(userId, contextId));
		if (sessionIds == null) {
			return new SessionControl[0];
		}
		return sessionIds.keySet().toArray(new SessionControl[sessionIds.size()]);
	}

	/**
	 * Wraps specified session by a newly created {@link SessionControl} object
	 * and puts it into this container
	 * 
	 * @param session
	 *            The session to put
	 * @param timeToLive
	 *            The session's time to live
	 * @return The wrapping {@link SessionControl session control}.
	 */
	SessionControl put(final Session session, final int timeToLive) {
		final String sessionId = session.getSessionID();
		final SessionControl sessionControl = new SessionControl(session, timeToLive);
		if (sessionIdMap.containsKey(sessionId) && LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder("session REBORN sessionid=").append(sessionId));
		}
		sessionIdMap.put(sessionId, sessionControl);
		final UserKey key = new UserKey(session.getUserId(), session.getContextId());
		Map<String, Integer> sessionIds = userSessions.get(key);
		if (sessionIds == null) {
			sessionIds = new ConcurrentHashMap<String, Integer>();
			userSessions.put(key, sessionIds);
		}
		sessionIds.put(sessionId, DUMMY);
		return sessionControl;
	}

	/**
	 * Puts specified {@link SessionControl} object into this container
	 * 
	 * @param sessionControl
	 *            The session control to put
	 */
	void putSessionControl(final SessionControl sessionControl) {
		final Session session = sessionControl.getSession();
		final String sessionId = session.getSessionID();
		sessionIdMap.put(sessionId, sessionControl);
		final UserKey key = new UserKey(session.getUserId(), session.getContextId());
		Map<String, Integer> sessionIds = userSessions.get(key);
		if (sessionIds == null) {
			sessionIds = new ConcurrentHashMap<String, Integer>();
			userSessions.put(key, sessionIds);
		}
		sessionIds.put(sessionId, DUMMY);
	}

	/**
	 * Removes the session bound to specified session ID.
	 * 
	 * @param sessionId
	 *            The session Id
	 * @return The {@link SessionControl session control} previously associated
	 *         with specified session ID, or <code>null</code>.
	 */
	SessionControl removeSessionById(final String sessionId) {
		final SessionControl sessionControl = sessionIdMap.remove(sessionId);
		if (sessionControl != null) {
			final Session session = sessionControl.getSession();
			final UserKey key = new UserKey(session.getUserId(), session.getContextId());
			final Map<String, Integer> sessionIds = userSessions.get(key);
			sessionIds.remove(sessionId);
			if (sessionIds.isEmpty()) {
				userSessions.remove(key);
			}
		}
		return sessionControl;
	}

	/**
	 * Removes the sessions bound to specified user ID and context ID.
	 * 
	 * @param userId
	 *            The user ID
	 * @param contextId
	 *            The context ID
	 * @return The {@link SessionControl session controls} previously associated
	 *         with specified user ID and context ID.
	 */
	SessionControl[] removeSessionsByUser(final int userId, final int contextId) {
		final UserKey key = new UserKey(userId, contextId);
		final Map<String, Integer> sessionIds = userSessions.remove(key);
		if (sessionIds == null) {
			return new SessionControl[0];
		}
		final List<SessionControl> l = new ArrayList<SessionControl>(sessionIds.size());
		final Set<String> ids = sessionIds.keySet();
		for (final String sessionId : ids) {
			final SessionControl sc = sessionIdMap.remove(sessionId);
			if (sc != null) {
				l.add(sc);
			}
		}
		return l.toArray(new SessionControl[l.size()]);
	}

	/**
	 * Converts this container to a newly created {@link Map map} holding this
	 * container's current sessions.
	 * <p>
	 * Changes made to any {@link Session} objects are reflected in this
	 * container.
	 * 
	 * @return The newly created {@link Map map} holding this container's
	 *         sessions.
	 */
	Map<String, Session> convert() {
		final Map<String, Session> retval = new HashMap<String, Session>(sessionIdMap.size());
		for (final Iterator<Map.Entry<String, SessionControl>> iterator = sessionIdMap.entrySet().iterator(); iterator
				.hasNext();) {
			final Map.Entry<String, SessionControl> e = iterator.next();
			retval.put(e.getKey(), e.getValue().getSession());
		}
		return retval;
	}

	/**
	 * Creates an {@link Iterator iterator} over the session IDs in this
	 * container.
	 * <p>
	 * The {@link Iterator#remove()} is not reflected in this container.
	 * 
	 * @return A newly created {@link Iterator iterator} over the session IDs in
	 *         this container.
	 */
	Iterator<String> getSessionIds() {
		return new HashSet<String>(sessionIdMap.keySet()).iterator();
	}

	/**
	 * Returns a collection view of the {@link SessionControl} objects contained
	 * in this container. The collection is <b><small>not</small></b> backed by
	 * the container, so changes to the map are not reflected in the container,
	 * but changes made to any {@link SessionControl} object is reflected in
	 * this container.
	 * 
	 * @return A collection view of the {@link SessionControl} objects contained
	 *         in this container.
	 */
	Collection<SessionControl> getSessionControls() {
		return new ArrayList<SessionControl>(sessionIdMap.values());
	}

	private static final class UserKey {

		private final int userId;

		private final int cid;

		private final int hash;

		public UserKey(final int userId, final int cid) {
			super();
			this.userId = userId;
			this.cid = cid;
			this.hash = userId ^ cid;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final UserKey other = (UserKey) obj;
			if (cid != other.cid) {
				return false;
			}
			if (userId != other.userId) {
				return false;
			}
			return true;
		}

	}
}
