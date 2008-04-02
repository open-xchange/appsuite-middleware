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
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link StorageUtility} - Offers utility methods for both folder and message
 * storage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class StorageUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(StorageUtility.class);

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

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

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
