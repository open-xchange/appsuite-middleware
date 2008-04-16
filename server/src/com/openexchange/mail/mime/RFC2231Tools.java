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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RFC2231Tools} - A collection of RFC2231 related utility methods
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RFC2231Tools {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RFC2231Tools.class);

	/**
	 * No instantiation
	 */
	private RFC2231Tools() {
		super();
	}

	private static final Pattern PAT_CL = Pattern.compile("([\\p{ASCII}&&[^']]+)'([\\p{ASCII}&&[^']]*)'(\\p{ASCII}+)");

	/**
	 * Parses given RFC2231 value into its charset, language and rfc2231-encoded
	 * value. Therefore RFC2231 value should match pattern:
	 * 
	 * <pre>
	 * &lt;charset-name&gt; + &quot;'&quot; + &lt;language-code&gt; + &quot;'&quot; + &lt;encoded-data&gt;
	 * </pre>
	 * 
	 * @param rfc2231Value
	 *            The rfc2231 value
	 * @return An array of {@link String} containing charset, language, and
	 *         rfc2231-encoded value or <code>null</code> if value does not
	 *         match pattern.
	 */
	public static String[] parseRFC2231Value(final String rfc2231Value) {
		final Matcher m = PAT_CL.matcher(rfc2231Value);
		if (!m.matches()) {
			return null;
		}
		return new String[] { m.group(1), m.group(2), m.group(3) };
	}

	/**
	 * Decodes specified string according to mail-safe encoding introduced in
	 * RFC2231
	 * <p>
	 * This method assumes that encoding informations are contained in given
	 * string; e.g.
	 * 
	 * <pre>
	 * utf-8'EN'%C2%A4%20txt
	 * </pre>
	 * 
	 * @param encoded
	 *            The encoded string
	 * @return The decoded string
	 */
	public static String rfc2231Decode(final String encoded) {
		final Matcher m = PAT_CL.matcher(encoded);
		if (!m.matches()) {
			return encoded;
		}
		return rfc2231Decode(m.group(3), m.group(1));
	}

	/**
	 * Decodes specified string according to mail-safe encoding introduced in
	 * RFC2231
	 * 
	 * @param encoded
	 *            The encoded string
	 * @param charset
	 *            The charset name
	 * 
	 * @return The decoded string
	 */
	public static String rfc2231Decode(final String encoded, final String charset) {
		if ((encoded == null) || (encoded.length() == 0)) {
			return encoded;
		} else if (!Charset.isSupported(charset)) {
			return encoded;
		}
		final char[] chars = encoded.toCharArray();
		final ByteBuffer bb = ByteBuffer.allocateDirect(chars.length);
		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];
			if ((c == '%') && isHexDigit(chars[i + 1]) && isHexDigit(chars[i + 2])) {
				bb.put((byte) ((Character.digit(chars[i + 1], 16) << 4) + Character.digit(chars[i + 2], 16)));
				i += 2;
			} else {
				bb.put((byte) c);
			}
		}
		bb.flip();
		return Charset.forName(charset).decode(bb).toString();
	}

	private static boolean isHexDigit(final char c) {
		final char ch = Character.toLowerCase(c);
		return ((ch >= '0') && (ch <= '9')) || ((ch >= 'a') && (ch <= 'f'));
	}

	/**
	 * Encodes given string according to mechanism provided in RFC2231
	 * 
	 * @param toEncode
	 *            The string to encode
	 * @param charset
	 *            The charset encoding
	 * @param language
	 *            The language to append
	 * @param prepend
	 *            <code>true</code> to prepend charset and language
	 *            informations; otherwise <code>false</code>
	 * @return The encoded string
	 */
	public static String rfc2231Encode(final String toEncode, final String charset, final String language,
			final boolean prepend) {
		return rfc2231Encode(toEncode, charset, language, prepend, false);
	}

	/**
	 * Encodes given string according to mechanism provided in RFC2231
	 * 
	 * @param toEncode
	 *            The string to encode
	 * @param charset
	 *            The charset encoding
	 * @param language
	 *            The language to append
	 * @param prepend
	 *            <code>true</code> to prepend charset and language
	 *            informations; otherwise <code>false</code>
	 * @param force
	 *            <code>true</code> to force encoding even if string top
	 *            encode only consists of ASCII characters; otherwise
	 *            <code>false</code>
	 * @return The encoded string
	 */
	public static String rfc2231Encode(final String toEncode, final String charset, final String language,
			final boolean prepend, final boolean force) {
		if ((toEncode == null) || (toEncode.length() == 0)) {
			return toEncode;
		} else if (!force && isAscii(toEncode)) {
			return toEncode;
		} else if (!Charset.isSupported(charset)) {
			return toEncode;
		}
		final StringBuilder retval = new StringBuilder(toEncode.length() * 3);
		if (prepend) {
			retval.append(charset.toLowerCase(Locale.ENGLISH)).append('\'').append(
					(language == null) || (language.length() == 0) ? "" : language).append('\'');
		}
		final char[] chars = toEncode.toCharArray();
		try {
			for (int i = 0; i < chars.length; i++) {
				if (!isAscii(chars[i]) || (chars[i] == ' ')) {
					final byte[] bytes = String.valueOf(chars[i]).getBytes(charset);
					for (int j = 0; j < bytes.length; j++) {
						retval.append('%').append(Integer.toHexString(bytes[j] & 0xFF).toUpperCase(Locale.ENGLISH));
					}
				} else {
					retval.append(chars[i]);
				}
			}
		} catch (final java.io.UnsupportedEncodingException e) {
			/*
			 * Cannot occur
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
		return retval.toString();
	}

	/**
	 * Checks whether the specified string's characters are ASCII 7 bit
	 * 
	 * @param s
	 *            The string to check
	 * @return <code>true</code> if string's characters are ASCII 7 bit;
	 *         otherwise <code>false</code>
	 */
	public static boolean isAscii(final String s) {
		final char[] chars = s.toCharArray();
		boolean isAscci = true;
		for (int i = 0; (i < chars.length) && isAscci; i++) {
			isAscci &= (chars[i] < 128);
		}
		return isAscci;
	}

	/**
	 * Checks whether the character is ASCII 7 bit
	 * 
	 * @param c
	 *            The character to check
	 * @return <code>true</code> if character is ASCII 7 bit; otherwise
	 *         <code>false</code>
	 */
	public static boolean isAscii(final char c) {
		return (c < 128);
	}

}
