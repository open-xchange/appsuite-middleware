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

package com.openexchange.imap.cache;

import javax.mail.MessagingException;

import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Rights;

/**
 * {@link RightsCache}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RightsCache {

	/**
	 * No instance
	 */
	private RightsCache() {
		super();
	}

	/**
	 * Gets cached <code>MYRIGHTS</code> command invoked on given IMAP folder
	 * 
	 * @param f
	 *            The IMAP folder
	 * @param load
	 *            Whether <code>MYRIGHTS</code> command should be invoked if
	 *            no cache entry present or not
	 * @param session
	 *            The session providing the session-bound cache
	 * @return The cached rights or <code>null</code>
	 * @throws MessagingException
	 *             If <code>MYRIGHTS</code> command fails
	 */
	public static Rights getCachedRights(final IMAPFolder f, final boolean load, final Session session)
			throws MessagingException {
		final RightsCacheEntry entry = new RightsCacheEntry(f.getFullName());
		final SessionMailCache mailCache = SessionMailCache.getInstance(session);
		mailCache.get(entry);
		if (load && null == entry.getValue()) {
			entry.setValue(f.myRights());
			mailCache.put(entry);
		}
		return entry.getValue();
	}

	/**
	 * Removes cached <code>MYRIGHTS</code> command invoked on given IMAP
	 * folder
	 * 
	 * @param f
	 *            The IMAP folder
	 * @param session
	 *            The session providing the session-bound cache
	 */
	public static void removeCachedRights(final IMAPFolder f, final Session session) {
		SessionMailCache.getInstance(session).remove(new RightsCacheEntry(f.getFullName()));
	}

	private static final class RightsCacheEntry implements SessionMailCacheEntry<Rights> {

		private String fullname;

		private Rights rights;

		private CacheKey key;

		public RightsCacheEntry(final String fullname) {
			this(fullname, null);
		}

		public RightsCacheEntry(final String fullname, final Rights rights) {
			super();
			this.fullname = fullname;
			this.rights = rights;
		}

		private CacheKey getKeyInternal() {
			if (null == key) {
				key = IMAPServiceRegistry.getServiceRegistry().getService(CacheService.class).newCacheKey(
						MailCacheCode.RIGHTS.getCode(), fullname);
			}
			return key;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.mail.cache.SessionBoundMailCacheEntry#getKey()
		 */
		public CacheKey getKey() {
			return getKeyInternal();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.mail.cache.SessionBoundMailCacheEntry#getValue()
		 */
		public Rights getValue() {
			return rights;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.openexchange.mail.cache.SessionBoundMailCacheEntry#setValue(java.lang.Object)
		 */
		public void setValue(final Rights value) {
			this.rights = value;
		}
	}

}
