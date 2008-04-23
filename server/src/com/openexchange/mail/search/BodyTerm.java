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

package com.openexchange.mail.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.text.Html2TextConverter;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link BodyTerm}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class BodyTerm extends SearchTerm<String> {

	private final String pattern;

	/**
	 * Initializes a new {@link BodyTerm}
	 */
	public BodyTerm(final String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public void addMailField(final Collection<MailField> col) {
		col.add(MailField.BODY);
	}

	@Override
	public boolean matches(final MailMessage mailMessage) throws MailException {
		final String text = getTextContent(mailMessage);
		if (text == null) {
			if (null == pattern) {
				return true;
			}
			return false;
		}
		if (null == pattern) {
			return false;
		}
		return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
	}

	@Override
	public boolean matches(final Message msg) throws MailException {
		final String text = getTextContent(msg);
		if (text == null) {
			if (null == pattern) {
				return true;
			}
			return false;
		}
		if (null == pattern) {
			return false;
		}
		return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
	}

	@Override
	public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
		return new javax.mail.search.BodyTerm(pattern);
	}

	/**
	 * Extracts textual content out of given message's body
	 * 
	 * @param part
	 *            The message whose textual content shall be extracted
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getTextContent(final Part part) throws MailException {
		try {
			if (ContentType.isMimeType(part.getContentType(), "multipart/*")) {
				final Multipart multipart = (Multipart) part.getContent();
				final int count = multipart.getCount();
				for (int i = 0; i < count; i++) {
					final String text = getTextContent(multipart.getBodyPart(i));
					if (text != null) {
						return text;
					}
				}
			}
			return getPartTextContent(part);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
	}

	/**
	 * Extracts textual content out of given mail part's body
	 * 
	 * @param mailPart
	 *            The mail message whose textual content shall be extracted
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getTextContent(final MailPart mailPart) throws MailException {
		if (mailPart.getContentType().isMimeType("multipart/*")) {
			final int count = mailPart.getEnclosedCount();
			for (int i = 0; i < count; i++) {
				final String text = getTextContent(mailPart.getEnclosedMailPart(i));
				if (text != null) {
					return text;
				}
			}
		}
		return getPartTextContent(mailPart);
	}

	/**
	 * Extracts textual content out of given part's body
	 * 
	 * @param part
	 *            The part
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getPartTextContent(final Part part) throws MailException {
		try {
			final ContentType ct = new ContentType(part.getContentType());
			if (!ct.isMimeType("text/*")) {
				/*
				 * No textual content
				 */
				return null;
			}
			String charset = ct.getCharsetParameter();
			if (null == charset) {
				charset = CharsetDetector.detectCharset(part.getInputStream());
			}
			if (ct.isMimeType("text/htm*")) {
				return new Html2TextConverter().convert(MessageUtility.readMimePart(part, charset));
			}
			return MessageUtility.readMimePart(part, charset);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
	}

	/**
	 * Extracts textual content out of given part's body
	 * 
	 * @param mailPart
	 *            The part
	 * @return The textual content or <code>null</code> if none found
	 * @throws MailException
	 *             If text extraction fails
	 */
	private static String getPartTextContent(final MailPart mailPart) throws MailException {
		if (!mailPart.getContentType().isMimeType("text/*")) {
			/*
			 * No textual content
			 */
			return null;
		}
		String charset = mailPart.getContentType().getCharsetParameter();
		if (null == charset) {
			charset = CharsetDetector.detectCharset(mailPart.getInputStream());
		}
		try {
			if (mailPart.getContentType().isMimeType("text/htm*")) {
				return new Html2TextConverter().convert(MessageUtility.readMailPart(mailPart, charset));
			}
			return MessageUtility.readMailPart(mailPart, charset);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}
}
