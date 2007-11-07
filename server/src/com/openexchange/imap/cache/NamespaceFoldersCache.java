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

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.openexchange.cache.CacheKey;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link NamespaceFoldersCache}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class NamespaceFoldersCache {

	private static final Integer NS_PERSONAL = Integer.valueOf(1);

	private static final Integer NS_USER = Integer.valueOf(2);

	private static final Integer NS_SHARED = Integer.valueOf(3);

	/**
	 * No instance
	 */
	private NamespaceFoldersCache() {
		super();
	}

	/**
	 * Gets cached personal namespaces when invoking <code>NAMESPACE</code>
	 * command on given IMAP store
	 * 
	 * @param imapStore
	 *            The IMAP store on which <code>NAMESPACE</code> command is
	 *            invoked
	 * @param load
	 *            Whether <code>NAMESPACE</code> command should be invoked if
	 *            no cache entry present or not
	 * @param session
	 *            The session providing the session-bound cache
	 * @return The personal namespace folders
	 * @throws MessagingException
	 *             If <code>NAMESPACE</code> command fails
	 */
	public static Folder[] getPersonalNamespaces(final IMAPStore imapStore, final boolean load,
			final SessionObject session) throws MessagingException {
		final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_PERSONAL);
		session.getMailCache().get(entry);
		if (load && null == entry.getValue()) {
			entry.setValue(imapStore.getPersonalNamespaces());
			session.getMailCache().put(entry);
		}
		return entry.getValue();
	}

	/**
	 * Gets cached user namespaces when invoking <code>NAMESPACE</code>
	 * command on given IMAP store
	 * 
	 * @param imapStore
	 *            The IMAP store on which <code>NAMESPACE</code> command is
	 *            invoked
	 * @param load
	 *            Whether <code>NAMESPACE</code> command should be invoked if
	 *            no cache entry present or not
	 * @param session
	 *            The session providing the session-bound cache
	 * @return The user namespace folders
	 * @throws MessagingException
	 *             If <code>NAMESPACE</code> command fails
	 */
	public static Folder[] getUserNamespaces(final IMAPStore imapStore, final boolean load, final SessionObject session)
			throws MessagingException {
		final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_USER);
		session.getMailCache().get(entry);
		if (load && null == entry.getValue()) {
			entry.setValue(imapStore.getUserNamespaces(null));
			session.getMailCache().put(entry);
		}
		return entry.getValue();
	}

	/**
	 * Gets cached shared namespaces when invoking <code>NAMESPACE</code>
	 * command on given IMAP store
	 * 
	 * @param imapStore
	 *            The IMAP store on which <code>NAMESPACE</code> command is
	 *            invoked
	 * @param load
	 *            Whether <code>NAMESPACE</code> command should be invoked if
	 *            no cache entry present or not
	 * @param session
	 *            The session providing the session-bound cache
	 * @return The shared namespace folders
	 * @throws MessagingException
	 *             If <code>NAMESPACE</code> command fails
	 */
	public static Folder[] getSharedNamespaces(final IMAPStore imapStore, final boolean load,
			final SessionObject session) throws MessagingException {
		final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_SHARED);
		session.getMailCache().get(entry);
		if (load && null == entry.getValue()) {
			entry.setValue(imapStore.getSharedNamespaces());
			session.getMailCache().put(entry);
		}
		return entry.getValue();
	}

	private static final class NamespaceFoldersCacheEntry implements SessionMailCacheEntry<Folder[]> {

		private Integer namespaceKey;

		private Folder[] folders;

		private CacheKey key;

		public NamespaceFoldersCacheEntry(final Integer namespaceKey) {
			this(namespaceKey, null);
		}

		public NamespaceFoldersCacheEntry(final Integer namespaceKey, final Folder[] folders) {
			super();
			this.namespaceKey = namespaceKey;
			this.folders = folders;
		}

		private CacheKey getKeyInternal() {
			if (null == key) {
				key = new CacheKey(MailCacheCode.NAMESPACE_FOLDERS.getCode(), namespaceKey);
			}
			return key;
		}

		public CacheKey getKey() {
			return getKeyInternal();
		}

		public Folder[] getValue() {
			return folders;
		}

		public void setValue(final Folder[] value) {
			this.folders = value;
		}

	}
}
