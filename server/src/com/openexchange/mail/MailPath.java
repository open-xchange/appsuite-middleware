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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MailPath} - Represents a message's unique path inside a mailbox, that
 * is the folder fullname followed by the value of {@link #SEPERATOR} followed
 * by mail's unique numeric ID:<br>
 * Example: <i>INBOX.Subfolder/1234</i>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailPath implements Cloneable {

	private static Comparator<MailPath> comparator;

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	/**
	 * A <code>null</code> {@link MailPath}
	 */
	public static final MailPath NULL = null;

	/**
	 * The <code>'/'</code> character which separates folder's fullname from
	 * mail's UID in a mail path
	 */
	public static final char SEPERATOR = '/';

	private static final Pattern DELIM_PATTERN = Pattern.compile(new StringBuilder(16).append("(.+)(")
			.append(SEPERATOR).append(")([0-9]+)").toString());

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

	/**
	 * Returns an appropriate {@link Comparator} implementation for
	 * {@link MailPath} instances
	 * 
	 * @return A {@link Comparator} implementation for {@link MailPath}
	 *         instances
	 */
	public static Comparator<MailPath> getMailPathComparator() {
		if (!INITIALIZED.get()) {
			synchronized (INITIALIZED) {
				if (comparator == null) {
					comparator = new Comparator<MailPath>() {
						public int compare(final MailPath mi1, final MailPath mi2) {
							final int res = mi1.folder.compareTo(mi2.folder);
							if (res != 0) {
								return res;
							}
							return Long.valueOf(mi1.uid).compareTo(Long.valueOf(mi2.uid));
						}
					};
					INITIALIZED.set(true);
				}
			}
		}
		return comparator;
	}

	/**
	 * Returns the mail paths for given comma-separated mail IDs each conform to
	 * pattern &lt;folder-path&gt;&lt;value-of-{@link #SEPERATOR}&gt;&lt;mail-ID&gt;
	 * 
	 * @param mailPaths
	 *            The comma-separated mail IDs
	 * @return The corresponding mail paths
	 * @throws MailException
	 *             If mail paths cannot be generated
	 */
	public static MailPath[] getMailPaths(final String mailPaths) throws MailException {
		return getMailPaths(mailPaths.split(" *, *"));
	}

	/**
	 * Returns the mail paths for given mail IDs each conform to pattern
	 * &lt;folder-path&gt;&lt;value-of-{@link #SEPERATOR}&gt;&lt;mail-ID&gt;
	 * 
	 * @param mailPaths
	 *            The mail IDs
	 * @return The corresponding mail paths
	 * @throws MailException
	 *             If mail paths cannot be generated
	 */
	public static MailPath[] getMailPaths(final String[] mailPaths) throws MailException {
		final MailPath[] retval = new MailPath[mailPaths.length];
		for (int i = 0; i < mailPaths.length; i++) {
			retval[i] = new MailPath(mailPaths[i]);
		}
		return retval;
	}

	/**
	 * Extracts the UIDs from given mail paths
	 * 
	 * @param mailPaths
	 *            The mail IDs
	 * @return The extracted UIDs
	 */
	public static long[] getUIDs(final MailPath[] mailPaths) {
		final long[] retval = new long[mailPaths.length];
		for (int i = 0; i < mailPaths.length; i++) {
			retval[i] = mailPaths[i].uid;
		}
		return retval;
	}

	/*
	 * Fields
	 */
	private String folder;

	private String str;

	private long uid;

	/**
	 * Default constructor
	 */
	public MailPath() {
		super();
	}

	/**
	 * Initializes a new {@link MailPath}
	 * 
	 * @param mailPathStr
	 *            The mail path's string representation
	 * @throws MailException
	 *             If mail path's string representation does not match expected
	 *             pattern: <i>(.+)(value of {@link #SEPERATOR})([0-9]+)</i>
	 */
	public MailPath(final String mailPathStr) throws MailException {
		final Matcher m = DELIM_PATTERN.matcher(mailPathStr);
		if (!m.matches()) {
			throw new MailException(MailException.Code.INVALID_MAIL_IDENTIFIER, mailPathStr);
		}
		uid = Long.parseLong(m.group(3));
		folder = m.group(1);
		str = mailPathStr;
	}

	/**
	 * Initializes a new {@link MailPath}
	 * 
	 * @param folder
	 *            Folder fullname
	 * @param uid
	 *            The mail's unique ID
	 */
	public MailPath(final String folder, final long uid) {
		this.folder = folder;
		this.uid = uid;
		str = new StringBuilder(folder).append(SEPERATOR).append(uid).toString();
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

	public String getFolder() {
		return folder;
	}

	public String getStr() {
		return str;
	}

	public long getUid() {
		return uid;
	}

	/**
	 * Sets this mail path's folder fullname and mail's unique ID (for
	 * re-usage).
	 * 
	 * @param mailPathStr
	 *            The mail paths string representation
	 * @return The mail path itself
	 * @throws MailException
	 *             If mail path's string representation does not match expected
	 *             pattern: <i>(.+)(value of {@link #SEPERATOR})([0-9]+)</i>
	 */
	public MailPath setMailIdentifierString(final String mailPathStr) throws MailException {
		final Matcher m = DELIM_PATTERN.matcher(mailPathStr);
		if (!m.matches()) {
			throw new MailException(MailException.Code.INVALID_MAIL_IDENTIFIER, mailPathStr);
		}
		uid = Long.parseLong(m.group(3));
		folder = m.group(1);
		str = mailPathStr;
		return this;
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
}
