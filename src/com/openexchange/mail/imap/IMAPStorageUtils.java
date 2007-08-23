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

package com.openexchange.mail.imap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.mail.MailListField;
import com.sun.mail.imap.IMAPFolder;

/**
 * IMAPStorageUtils
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPStorageUtils {

	public static final class DummyAddress extends InternetAddress {

		private static final long serialVersionUID = -3276144799717449603L;

		private static final String TYPE = "rfc822";

		private final String address;

		public DummyAddress(final String address) {
			this.address = MessageUtils.decodeMultiEncodedHeader(address);
		}

		@Override
		public String getType() {
			return TYPE;
		}

		@Override
		public String toString() {
			return address;
		}

		@Override
		public String getAddress() {
			return address;
		}

		@Override
		public String getPersonal() {
			return null;
		}

		@Override
		public boolean equals(final Object address) {
			if (address instanceof InternetAddress) {
				final InternetAddress ia = (InternetAddress) address;
				return this.address.equalsIgnoreCase(ia.getAddress());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return address.hashCode();
		}

	}

	public static final int INDEX_DRAFTS = 0;

	public static final int INDEX_SENT = 1;

	public static final int INDEX_SPAM = 2;

	public static final int INDEX_TRASH = 3;

	public static final int INDEX_CONFIRMED_SPAM = 4;

	public static final int INDEX_CONFIRMED_HAM = 5;

	public static final int INDEX_INBOX = 6;

	public static final int MAIL_PARAM_HARD_DELETE = 1;

	public static final int UNLIMITED_QUOTA = -1;

	public static final int ORDER_ASC = 1;

	public static final int ORDER_DESC = 2;

	public static final Message[] EMPTY_MSGS = new Message[0];

	/**
	 * Prevent instantiation
	 */
	private IMAPStorageUtils() {
		super();
	}

	/**
	 * Virtual ID of mailbox's root folder
	 * 
	 * @value default
	 */
	public static final String DEFAULT_IMAP_FOLDER_ID = "default";

	public static String prepareMailFolderParam(final String folderStringArg) {
		if (folderStringArg == null) {
			return null;
		} else if (DEFAULT_IMAP_FOLDER_ID.equals(folderStringArg)) {
			return folderStringArg;
		} else if (folderStringArg.startsWith(DEFAULT_IMAP_FOLDER_ID)) {
			return folderStringArg.substring(8);
		}
		return folderStringArg;
	}

	public static String prepareFullname(final String fullname, final char sep) {
		if (DEFAULT_IMAP_FOLDER_ID.equals(fullname)) {
			return fullname;
		}
		return new StringBuilder(32).append(DEFAULT_IMAP_FOLDER_ID).append(sep).append(fullname).toString();
	}

	/**
	 * Gets the appropiate IMAP fetch profile
	 * 
	 * @param fields
	 *            The fields
	 * @param sortField
	 *            The sort field
	 * @return The appropiate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailListField[] fields, final MailListField sortField) {
		return getFetchProfile(fields, null, sortField);
	}

	/**
	 * Gets the appropiate IMAP fetch profile
	 * 
	 * @param fields
	 *            The fields
	 * @param searchFields
	 *            The search fields
	 * @param sortField
	 *            The sort field
	 * @return The appropiate IMAP fetch profile
	 */
	public static FetchProfile getFetchProfile(final MailListField[] fields, final MailListField[] searchFields,
			final MailListField sortField) {
		final FetchProfile retval = new FetchProfile();
		/*
		 * Use a set to avoid duplicate entries
		 */
		final Set<MailListField> set = new HashSet<MailListField>();
		if (fields != null) {
			set.addAll(Arrays.asList(fields));
		}
		if (searchFields != null) {
			set.addAll(Arrays.asList(searchFields));
		}
		if (sortField != null) {
			set.add(sortField);
		}
		final int size = set.size();
		final Iterator<MailListField> iter = set.iterator();
		for (int i = 0; i < size; i++) {
			addFetchItem(retval, iter.next());
		}
		return retval;
	}

	private static void addFetchItem(final FetchProfile fp, final MailListField field) {
		switch (field) {
		case ID:
			fp.add(UIDFolder.FetchProfileItem.UID);
			break;
		case ATTACHMENT:
			fp.add(FetchProfile.Item.CONTENT_INFO);
			break;
		case FROM:
			fp.add(MessageHeaders.HDR_FROM);
			break;
		case TO:
			fp.add(MessageHeaders.HDR_TO);
			break;
		case CC:
			fp.add(MessageHeaders.HDR_CC);
			break;
		case BCC:
			fp.add(MessageHeaders.HDR_BCC);
			break;
		case SUBJECT:
			fp.add(MessageHeaders.HDR_SUBJECT);
			break;
		case SIZE:
			fp.add(IMAPFolder.FetchProfileItem.SIZE);
			break;
		case SENT_DATE:
			fp.add(MessageHeaders.HDR_DATE);
			break;
		case FLAGS:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case DISPOSITION_NOTIFICATION_TO:
			fp.add(MessageHeaders.HDR_DISP_NOT_TO);
			break;
		case PRIORITY:
			fp.add(MessageHeaders.HDR_X_PRIORITY);
			break;
		case COLOR_LABEL:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		case FLAG_SEEN:
			if (!fp.contains(FetchProfile.Item.FLAGS)) {
				fp.add(FetchProfile.Item.FLAGS);
			}
			break;
		default:
			return;
		}
	}
}
