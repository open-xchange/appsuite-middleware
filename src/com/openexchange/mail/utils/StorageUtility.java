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

package com.openexchange.mail.utils;

import static javax.mail.internet.MimeUtility.unfold;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * {@link StorageUtility} - Offers general prurpose methods for both folder and
 * message storage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class StorageUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(StorageUtility.class);

	public static final class DummyAddress extends InternetAddress {

		private static final long serialVersionUID = -3276144799717449603L;

		private static final String TYPE = "rfc822";

		private final String address;

		public DummyAddress(final String address) {
			this.address = MessageUtility.decodeMultiEncodedHeader(address);
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

	public static final Message[] EMPTY_MSGS = new Message[0];

	/**
	 * Prevent instantiation
	 */
	private StorageUtility() {
		super();
	}

	public static String getAllAddresses(final InternetAddress[] internetAddrs) {
		if (internetAddrs == null || internetAddrs.length == 0) {
			return "";
		}
		final StringBuilder addressBuilder = new StringBuilder(32 * internetAddrs.length);
		addressBuilder.append(internetAddrs[0].toUnicodeString());
		for (int i = 1; i < internetAddrs.length; i++) {
			addressBuilder.append(',').append(internetAddrs[i].toUnicodeString());
		}

		return (addressBuilder.toString());
	}

	private static final String STR_UID = "UID";

	private static final String TMPL_FETCH_HEADER_REV1 = "FETCH %s (BODY.PEEK[HEADER])";

	private static final String TMPL_FETCH_HEADER_NON_REV1 = "FETCH %s (RFC822.HEADER)";

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

	/**
	 * Call this method if JavaMail's routine fails to load a message's header.
	 * Headers are read in a safe manner and filled into a map which is then
	 * returned
	 * 
	 * @param msg
	 *            The message which headers shall be loaded
	 * @param uid
	 *            <code>true</code> to reference to message via its UID;
	 *            otherwise via its sequence ID
	 * @return A {@link Map} containing the headers
	 * @throws MessagingException
	 * @throws ProtocolException
	 */
	public static Map<String, String> loadHeaders(final Message msg, final boolean uid) throws MessagingException,
			ProtocolException {
		final IMAPFolder fld = (IMAPFolder) msg.getFolder();
		final IMAPProtocol p = fld.getProtocol();
		final String tmpl = p.isREV1() ? TMPL_FETCH_HEADER_REV1 : TMPL_FETCH_HEADER_NON_REV1;
		final String cmd;
		if (uid) {
			cmd = new StringBuilder(64).append(STR_UID).append(' ').append(
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
					final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(f.toString()));
					while (m.find()) {
						valBuilder.append(m.group(2));
						if (m.group(3) != null) {
							valBuilder.append(unfold(m.group(3)));
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

	private static final String ENC_ASCII = "US-ASCII";

	/**
	 * Parses specified headers into a map
	 * 
	 * @param headers
	 *            The headers as raw bytes
	 * @return An instance of {@link Map} containing the headers
	 */
	public static Map<HeaderName, String> parseHeaders(final byte[] headers) {
		try {
			return parseHeaders(new String(headers, ENC_ASCII));
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Parses specified headers into a map
	 * 
	 * @param headers
	 *            The headers as {@link String}
	 * @return An instance of {@link Map} containing the headers
	 */
	public static Map<HeaderName, String> parseHeaders(final String headers) {
		final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(headers));
		final Map<HeaderName, String> retval = new HashMap<HeaderName, String>();
		final StringBuilder valBuilder = new StringBuilder(256);
		while (m.find()) {
			valBuilder.append(m.group(2));
			if (m.group(3) != null) {
				valBuilder.append(unfold(m.group(3)));
			}
			retval.put(HeaderName.valueOf(m.group(1)), valBuilder.toString());
			valBuilder.setLength(0);
		}
		return retval;
	}

	public static String prepareMailFolderParam(final String folderStringArg) {
		if (folderStringArg == null) {
			return null;
		} else if (MailFolder.DEFAULT_FOLDER_ID.equals(folderStringArg)) {
			return folderStringArg;
		} else if (folderStringArg.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
			return folderStringArg.substring(8);
		}
		return folderStringArg;
	}

	public static String prepareFullname(final String fullname, final char sep) {
		if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
			return fullname;
		}
		return new StringBuilder(32).append(MailFolder.DEFAULT_FOLDER_ID).append(sep).append(fullname).toString();
	}

	private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

	/**
	 * Determines the default folder names (<b>not</b> fullnames). The
	 * returned array of {@link String} indexes the names as given through
	 * constants: {@link StorageUtility#INDEX_DRAFTS},
	 * {@link StorageUtility#INDEX_SENT}, etc.
	 * 
	 * @param usm
	 *            The user's mail settings
	 * @return The default folder names as an array of {@link String}
	 * @throws MailConfigException
	 *             If spam enablement/disablement cannot be determined
	 */
	public static String[] getDefaultFolderNames(final UserSettingMail usm) throws MailConfigException {
		final String[] names = new String[usm.isSpamEnabled() ? 6 : 4];
		if (usm.getStdDraftsName() == null || usm.getStdDraftsName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_DRAFTS);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_DRAFTS), e);
			}
			names[INDEX_DRAFTS] = UserSettingMail.STD_DRAFTS;
		} else {
			names[INDEX_DRAFTS] = usm.getStdDraftsName();
		}
		if (usm.getStdSentName() == null || usm.getStdSentName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_SENT);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SENT), e);
			}
			names[INDEX_SENT] = UserSettingMail.STD_SENT;
		} else {
			names[INDEX_SENT] = usm.getStdSentName();
		}
		if (usm.getStdSpamName() == null || usm.getStdSpamName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_SPAM);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SPAM), e);
			}
			names[INDEX_SPAM] = UserSettingMail.STD_SPAM;
		} else {
			names[INDEX_SPAM] = usm.getStdSpamName();
		}
		if (usm.getStdTrashName() == null || usm.getStdTrashName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_TRASH);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_TRASH), e);
			}
			names[INDEX_TRASH] = UserSettingMail.STD_TRASH;
		} else {
			names[INDEX_TRASH] = usm.getStdTrashName();
		}
		if (usm.isSpamEnabled()) {
			if (usm.getConfirmedSpam() == null || usm.getConfirmedSpam().length() == 0) {
				if (LOG.isWarnEnabled()) {
					final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
							UserSettingMail.STD_CONFIRMED_SPAM);
					LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_SPAM), e);
				}
				names[INDEX_CONFIRMED_SPAM] = UserSettingMail.STD_CONFIRMED_SPAM;
			} else {
				names[INDEX_CONFIRMED_SPAM] = usm.getConfirmedSpam();
			}
			if (usm.getConfirmedHam() == null || usm.getConfirmedHam().length() == 0) {
				if (LOG.isWarnEnabled()) {
					final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME,
							UserSettingMail.STD_CONFIRMED_HAM);
					LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_HAM), e);
				}
				names[INDEX_CONFIRMED_HAM] = UserSettingMail.STD_CONFIRMED_HAM;
			} else {
				names[INDEX_CONFIRMED_HAM] = usm.getConfirmedHam();
			}
		}
		return names;
	}
}
