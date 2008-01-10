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

package com.openexchange.mail;

import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MailPath} - Represents a message's unique path inside a mailbox.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailPath implements Cloneable {

	public static final char SEPERATOR = '/';

	private static final Pattern DELIM_PATTERN = Pattern.compile(new StringBuilder(15).append("(.+)(")
			.append(SEPERATOR).append(")([0-9]+)").toString());

	/**
	 * A <code>null</code> {@link MailPath}
	 */
	public static final MailPath NULL = null;

	/*
	 * Fields
	 */
	private String folder;

	private long uid;

	private String str;

	/**
	 * 
	 */
	public MailPath() {
		super();
	}

	public MailPath(final String mailPath) throws MailException {
		final Matcher m = DELIM_PATTERN.matcher(mailPath);
		if (!m.matches()) {
			throw new MailException(MailException.Code.INVALID_MAIL_IDENTIFIER, mailPath);
		}
		uid = Long.parseLong(m.group(3));
		folder = m.group(1);
		str = mailPath;
	}

	public MailPath(final String folder, final long uid) {
		this.folder = folder;
		this.uid = uid;
		str = new StringBuilder(folder).append(SEPERATOR).append(uid).toString();
	}

	public MailPath setMailIdentifierString(final String mailIdentifier) throws MailException {
		final Matcher m = DELIM_PATTERN.matcher(mailIdentifier);
		if (!m.matches()) {
			throw new MailException(MailException.Code.INVALID_MAIL_IDENTIFIER, mailIdentifier);
		}
		uid = Long.parseLong(m.group(3));
		folder = m.group(1);
		str = mailIdentifier;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			/*
			 * Cannot occur since Cloneable is implemented
			 */
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return str;
	}

	public String getFolder() {
		return folder;
	}

	public long getUid() {
		return uid;
	}

	public String getStr() {
		return str;
	}

	/*
	 * Some static helpers
	 */

	private static Comparator<MailPath> COMP;

	private static boolean compInitialized;

	private static final Lock LOCK_COMP = new ReentrantLock();

	public static Comparator<MailPath> getMailPathComparator() {
		if (!compInitialized) {
			LOCK_COMP.lock();
			try {
				if (COMP == null) {
					COMP = new Comparator<MailPath>() {
						public int compare(final MailPath mi1, final MailPath mi2) {
							final int res = mi1.folder.compareTo(mi2.folder);
							if (res != 0) {
								return res;
							}
							return Long.valueOf(mi1.uid).compareTo(Long.valueOf(mi2.uid));
						}
					};
					compInitialized = true;
				}
			} finally {
				LOCK_COMP.unlock();
			}
		}
		return COMP;
	}

	public static MailPath[] getMailPaths(final String[] mailIDs) throws MailException {
		final MailPath[] retval = new MailPath[mailIDs.length];
		for (int i = 0; i < mailIDs.length; i++) {
			retval[i] = new MailPath(mailIDs[i]);
		}
		return retval;
	}

	public static long[] getUIDs(final String[] mailIDs) throws MailException {
		final long[] retval = new long[mailIDs.length];
		for (int i = 0; i < mailIDs.length; i++) {
			retval[i] = new MailPath(mailIDs[i]).uid;
		}
		return retval;
	}

	/**
	 * Gets the mail path corresponding to given folder fullname and message UID
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param uid
	 *            The message UID
	 * @return The mail path as {@link String}
	 */
	public static String getMailPath(final String folder, final long uid) {
		return new StringBuilder(folder).append(SEPERATOR).append(uid).toString();
	}
}
