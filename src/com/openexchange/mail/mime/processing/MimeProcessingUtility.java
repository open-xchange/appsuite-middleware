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

package com.openexchange.mail.mime.processing;

import static com.openexchange.mail.utils.MessageUtility.htmlFormat;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.UUEncodedMultiPart;

/**
 * {@link MimeProcessingUtility} - Provides some utility methods for
 * {@link MimeForward} and {@link MimeReply}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MimeProcessingUtility {

	/**
	 * No instantiation
	 */
	private MimeProcessingUtility() {
		super();
	}

	/**
	 * Checks if given part's disposition is inline; meaning rather a regular
	 * message body than an attachment
	 * 
	 * @param part
	 *            The message's part
	 * @return <code>true</code> if given part is considered to be an inline
	 *         part; otherwise <code>false</code>
	 * @throws MessagingException
	 *             If part's attributes cannot be accessed
	 */
	static boolean isInline(final Part part) throws MessagingException {
		return ((part.getDisposition() == null || Part.INLINE.equalsIgnoreCase(part.getDisposition())) && part
				.getFileName() == null);
	}

	/**
	 * Determines the proper text version according to user's mail settings.
	 * Given content type is altered accordingly
	 * 
	 * @param textPart
	 *            The text part
	 * @param contentType
	 *            The text part's content type
	 * @return The proper text version
	 * @throws MessagingException
	 * @throws IOException
	 * @throws MailException
	 */
	static String handleInlineTextPart(final Part textPart, final ContentType contentType, final UserSettingMail usm)
			throws MessagingException, IOException, MailException {
		if (contentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
			if (usm.isDisplayHtmlInlineContent()) {
				return MessageUtility.readPart(textPart, contentType);
			}
			contentType.setBaseType("text/plain");
			return new Html2TextConverter().convertWithQuotes(MessageUtility.readPart(textPart, contentType));
		} else if (contentType.isMimeType(MIMETypes.MIME_TEXT_PLAIN)) {
			final String content = MessageUtility.readPart(textPart, contentType);
			final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
			if (uuencodedMP.isUUEncoded()) {
				/*
				 * UUEncoded content detected. Extract normal text.
				 */
				return uuencodedMP.getCleanText();
			}
			return MessageUtility.readPart(textPart, contentType);
		}
		return MessageUtility.readPart(textPart, contentType);
	}

	/**
	 * Creates a {@link String} from given array of {@link InternetAddress}
	 * instances through invoking {@link InternetAddress#toUnicodeString()}
	 * 
	 * @param addrs
	 *            The rray of {@link InternetAddress} instances
	 * @return A comma-separated list of addresses as a {@link String}
	 */
	static String addrs2String(final InternetAddress[] addrs) {
		final StringBuilder tmp = new StringBuilder(addrs.length * 16);
		tmp.append(addrs[0].toUnicodeString());
		for (int i = 1; i < addrs.length; i++) {
			tmp.append(", ").append(addrs[i].toUnicodeString());
		}
		return tmp.toString();
	}

	/**
	 * Appends the appropriate text version dependent on root's content type and
	 * current text's content type
	 * 
	 * @param rootType
	 *            The root's content type
	 * @param contentType
	 *            Current text's content type
	 * @param text
	 *            The text content
	 * @param textBuilder
	 *            The text builder to append to
	 * @throws IOException
	 */
	static void appendRightVersion(final ContentType rootType, final ContentType contentType, final String text,
			final StringBuilder textBuilder) throws IOException {
		if (rootType.getBaseType().equalsIgnoreCase(contentType.getBaseType())) {
			textBuilder.append(text);
		} else if (rootType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
			textBuilder.append(htmlFormat(text));
		} else {
			textBuilder.append(new Html2TextConverter().convertWithQuotes(text));
		}
	}

}
