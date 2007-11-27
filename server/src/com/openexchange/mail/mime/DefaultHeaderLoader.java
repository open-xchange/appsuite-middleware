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

package com.openexchange.mail.mime;

import static javax.mail.internet.MimeUtility.unfold;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.openexchange.mail.MailException;

/**
 * {@link DefaultHeaderLoader} - Default header loader
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DefaultHeaderLoader extends MIMEHeaderLoader {

	/**
	 * Initializes a new {@link DefaultHeaderLoader}
	 */
	public DefaultHeaderLoader() {
		super();
	}

	private static final Pattern PATTERN_PARSE_HEADER = Pattern
			.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.mime.MIMEHeaderLoader#loadHeaders(javax.mail.Message,
	 *      boolean)
	 */
	@Override
	public Map<String, String> loadHeaders(final Message msg, final boolean uid) throws MailException {
		try {
			final String headers = getMessageHeaders(msg);
			final StringBuilder valBuilder = new StringBuilder(128);
			final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(headers));
			final Map<String, String> retval = new HashMap<String, String>();
			while (m.find()) {
				valBuilder.append(m.group(2));
				if (m.group(3) != null) {
					valBuilder.append(unfold(m.group(3)));
				}
				retval.put(m.group(1), valBuilder.toString());
				valBuilder.setLength(0);
			}
			return retval;
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
	}

	private static String getMessageHeaders(final Message msg) throws IOException, MessagingException {
		final StringBuilder sb = new StringBuilder(1024);
		/*
		 * Get message's source
		 */
		final ByteArrayOutputStream out = new ByteArrayOutputStream(msg.getSize());
		msg.writeTo(out);
		/*
		 * Transform into an input stream
		 */
		final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out
				.toByteArray()), "US-ASCII"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		return sb.toString();
	}
}
