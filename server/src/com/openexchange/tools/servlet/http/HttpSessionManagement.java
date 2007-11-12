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

package com.openexchange.tools.servlet.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import com.openexchange.server.ServerTimer;

/**
 * HttpSessionManagement
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpSessionManagement {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HttpSessionManagement.class);

	private static Map<String, HttpSession> sessions = new ConcurrentHashMap<String, HttpSession>();

	static {
		ServerTimer.getTimer().schedule(new SessionRemover(), 100, 3600000);
	}

	private HttpSessionManagement() {
		super();
	}

	public static HttpSession getHttpSession(final String sessionId) {
		return sessions.get(sessionId);
	}

	public static void putHttpSession(final HttpSession httpSession) {
		sessions.put(httpSession.getId(), httpSession);
	}

	public static boolean containsHttpSession(final String sessionId) {
		return sessions.containsKey(sessionId);
	}

	public static void removeHttpSession(final String sessionId) {
		sessions.remove(sessionId);
	}

	public static HttpSession createHttpSession(final String uniqueId) {
		final HttpSessionWrapper httpSession = new HttpSessionWrapper(uniqueId);
		putHttpSession(httpSession);
		return httpSession;
	}

	public static HttpSession createHttpSession() {
		return createHttpSession(getNewUniqueId());
	}

	public final static boolean isHttpSessionExpired(final HttpSession httpSession) {
		return httpSession.getMaxInactiveInterval() > 0 ? (System.currentTimeMillis() - httpSession
				.getLastAccessedTime()) > httpSession.getMaxInactiveInterval() : false;
	}

	private static final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	/**
	 * Creates a unique ID
	 * 
	 * @return The unique ID
	 */
	public static final String getNewUniqueId() {
		String retval = null;
		try {
			final SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
			final String rndNumber = Integer.valueOf(secRandom.nextInt()).toString();
			final MessageDigest sha = MessageDigest.getInstance("SHA-1");
			final byte[] inputBytes = sha.digest(rndNumber.getBytes());
			final StringBuilder result = new StringBuilder();
			for (int i = 0; i < inputBytes.length; i++) {
				final byte b = inputBytes[i];
				result.append(digits[(b & 0xf0) >> 4]);
				result.append(digits[(b & 0x0f)]);
			}
			retval = result.toString();
		} catch (final NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
		}
		return retval;
	}

	private static class SessionRemover extends TimerTask {
		@Override
		public void run() {
			try {
				for (final Iterator<Map.Entry<String, HttpSession>> iter = sessions.entrySet().iterator(); iter
						.hasNext();) {
					final Map.Entry<String, HttpSession> entry = iter.next();
					if (isHttpSessionExpired(entry.getValue())) {
						entry.getValue().invalidate();
						iter.remove();
					}
				}
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
