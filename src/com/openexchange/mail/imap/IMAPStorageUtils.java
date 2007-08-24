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

import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.mail.MailListField;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UIDSet;

/**
 * {@link IMAPStorageUtils} - Offers general prurpose methods for both IMAP
 * implementations: folder and message storage
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

	/*
	 * Public constants
	 */
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

	private static final String STR_UID = "UID";

	private static final String TMPL_FETCH_HEADER_REV1 = "FETCH %s (BODY.PEEK[HEADER])";

	private static final String TMPL_FETCH_HEADER_NON_REV1 = "FETCH %s (RFC822.HEADER)";

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

	/**
	 * Call this method if JavaMail's routine fails to load a message's header.
	 * Header is read in a safe manner and filled into given
	 * <tt>javax.mail.Message</tt> instance
	 */
	public static Map<String, String> loadBrokenHeaders(final Message msg, final boolean uid)
			throws MessagingException, ProtocolException {
		final IMAPFolder fld = (IMAPFolder) msg.getFolder();
		final IMAPProtocol p = fld.getProtocol();
		final String tmpl = p.isREV1() ? TMPL_FETCH_HEADER_REV1 : TMPL_FETCH_HEADER_NON_REV1;
		final String cmd;
		if (uid) {
			cmd = new StringBuilder(50).append(STR_UID).append(' ').append(
					String.format(tmpl, Long.valueOf(fld.getUID(msg)))).toString();
		} else {
			cmd = String.format(tmpl, Integer.valueOf(msg.getMessageNumber()));
		}
		final Map<String, String> retval = new HashMap<String, String>();
		final Response[] r = p.command(cmd, null);
		final Response response = r[r.length - 1];
		try {
			if (response.isOK()) {
				final int len = r.length - 1;
				final StringBuilder valBuilder = new StringBuilder();
				NextResponse: for (int i = 0; i < len; i++) {
					if (r[i] == null) {
						continue NextResponse;
					} else if (!(r[i] instanceof FetchResponse)) {
						continue NextResponse;
					}
					final FetchResponse f = ((FetchResponse) r[i]);
					if (f.getNumber() != msg.getMessageNumber()) {
						continue NextResponse;
					}
					final Matcher m = PATTERN_PARSE_HEADER.matcher(f.toString());
					while (m.find()) {
						valBuilder.append(m.group(2));
						if (m.group(3) != null) {
							valBuilder.append(removeHdrLineBreak(m.group(3)));
						}
						retval.put(m.group(1), valBuilder.toString());
						valBuilder.setLength(0);
					}
					r[i] = null;
				}
			}
		} finally {
			// dispatch remaining untagged responses
			p.notifyResponseHandlers(r);
			p.handleResult(response);
		}
		return null;
	}

	/**
	 * Turns given array of <code>long</code> into an array of
	 * <code>com.sun.mail.imap.protocol.UIDSet</code> which in turn can be
	 * used for a varieties of <code>IMAPProtocol</code> methods.
	 * 
	 * @param uids -
	 *            the UIDs
	 * @return an array of <code>com.sun.mail.imap.protocol.UIDSet</code>
	 */
	public static UIDSet[] toUIDSet(final long[] uids) {
		final List<UIDSet> sets = new ArrayList<UIDSet>(uids.length);
		for (int i = 0; i < uids.length; i++) {
			long current = uids[i];
			final UIDSet set = new UIDSet();
			set.start = current;
			/*
			 * Look for contiguous UIDs
			 */
			Inner: for (++i; i < uids.length; i++) {
				final long next = uids[i];
				if (next == current + 1) {
					current = next;
				} else {
					/*
					 * Break in sequence. Need to reexamine this message at the
					 * top of the outer loop, so decrement 'i' to cancel the
					 * outer loop's autoincrement
					 */
					i--;
					break Inner;
				}
			}
			set.end = current;
			sets.add(set);
		}
		if (sets.isEmpty()) {
			return null;
		}
		return sets.toArray(new UIDSet[sets.size()]);
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
