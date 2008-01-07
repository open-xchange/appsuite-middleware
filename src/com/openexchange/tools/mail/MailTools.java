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

package com.openexchange.tools.mail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.configuration.SystemConfig;

/**
 * MailTools
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public final class MailTools {

	private static final String HTML_BR = "<br>";

	private static final String REPL_LINEBREAK = "\r?\n";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailTools.class);

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static Map<Character, String> htmlCharMap;

	private static void initHtmlCharMap() {
		synchronized (initialized) {
			if (null == htmlCharMap) {
				final Properties htmlEntities = new Properties();
				InputStream in = null;
				try {
					in = new FileInputStream(SystemConfig.getProperty(SystemConfig.Property.HTMLEntities));
					htmlEntities.load(in);
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
					htmlCharMap = null;
				} finally {
					if (null != in) {
						try {
							in.close();
						} catch (final IOException e) {
							LOG.error(e.getLocalizedMessage(), e);
						}
					}
				}
				/*
				 * Build up map
				 */
				htmlCharMap = new HashMap<Character, String>();
				final Iterator<Map.Entry<Object, Object>> iter = htmlEntities.entrySet().iterator();
				final int size = htmlEntities.size();
				for (int i = 0; i < size; i++) {
					final Map.Entry<Object, Object> entry = iter.next();
					htmlCharMap.put(Character.valueOf((char) Integer.parseInt((String) entry.getValue())),
							(String) entry.getKey());
				}
				initialized.set(true);
			}
		}
	}

	/*
	 * private static final String[] tags = new String[] { "\"", "'", ">", "<",
	 * "\r?\n" };
	 * 
	 * private static final String[] replace = new String[] { "&quot;", "&#39;",
	 * "&gt;", "&lt;", "<br>" };
	 */

	public static final String NUMBER_DIGITS = "###,##0.00";

	public static final String NUMBER_WO_DIGITS = "#####0";

	private static String escape(final String s, final boolean withQuote) {
		if (!initialized.get()) {
			initHtmlCharMap();
		}
		final int len = s.length();
		final StringBuilder sb = new StringBuilder(len);
		/*
		 * Escape
		 */
		for (int i = 0; i < len; i++) {
			final Character c = Character.valueOf(s.charAt(i));
			if (withQuote ? htmlCharMap.containsKey(c) : (c.charValue() == '"' ? false : htmlCharMap.containsKey(c))) {
				sb.append('&').append(htmlCharMap.get(c)).append(';');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Formats plain text to html by escaping html special characters e.g.
	 * <code>&lt;</code> => <code>&amp;lt;</code>
	 * 
	 * @param plainText
	 *            The plain text
	 * @param withQuote
	 *            Whether to escape quotes (<code>&quot;</code>) or not
	 * @return properly escaped html content
	 */
	public static String htmlFormat(final String plainText, final boolean withQuote) {
		/*
		 * String retval = str; for (int i = withQuote ? 0 : 1; i < tags.length;
		 * i++) { retval = retval.replaceAll(tags[i], replace[i]); } return
		 * retval;
		 */
		return escape(plainText, withQuote).replaceAll(REPL_LINEBREAK, HTML_BR);
	}

	/**
	 * Formats plain text to html by escaping html special characters e.g.
	 * <code>&lt;</code> => <code>&amp;lt;</code>
	 * <p>
	 * This is just a convenience method which invokes
	 * <code>{@link #htmlFormat(String, boolean)}</code> with latter parameter
	 * set to <code>true</code>
	 * 
	 * @param plainText
	 *            The plain text
	 * @return properly escaped html content
	 * @see #htmlFormat(String, boolean)
	 */
	public static String htmlFormat(final String plainText) {
		// XXX: Maybe set to false
		return htmlFormat(plainText, true);
	}

	public static Pattern PATTERN_HREF = Pattern
			.compile(
					"<a\\s+href[^>]+>.*?</a>|((?:https?://|ftp://|mailto:|news\\.|www\\.)(?:[-A-Z0-9+@#/%?=~_|!:,.;]|&amp;|&(?!\\w+;))*(?:[-A-Z0-9+@#/%=~_|]|&amp;|&(?!\\w+;)))",
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	/**
	 * Searches for non-html links and convert them to valid html links
	 * <p>
	 * Example: <code>http://www.somewhere.com</code> is converted to
	 * <code>&lt;a&nbsp;href=&quot;http://www.somewhere.com&quot;&gt;http://www.somewhere.com&lt;/a&gt;</code>
	 * 
	 * @param content
	 *            The content to search in
	 * @return The given content with all non-html links converted to valid html
	 *         links
	 * @see #PATTERN_HREF
	 */
	public static String formatHrefLinks(final String content) {
		try {
			final Matcher m = PATTERN_HREF.matcher(content);
			final StringBuffer sb = new StringBuffer(content.length());
			final StringBuilder tmp = new StringBuilder(200);
			while (m.find()) {
				final String nonHtmlLink = m.group(1);
				if (nonHtmlLink == null || (isImgSrc(content, m.start(1)))) {
					m.appendReplacement(sb, Matcher.quoteReplacement(checkTarget(m.group())));
				} else {
					tmp.setLength(0);
					m.appendReplacement(sb, tmp.append("<a href=\"").append(
							(nonHtmlLink.startsWith("www") || nonHtmlLink.startsWith("news") ? "http://" : "")).append(
							"$1\" target=\"_blank\">$1</a>").toString());
				}
			}
			m.appendTail(sb);
			return sb.toString();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return content;
	}

	private static final Pattern PATTERN_TARGET = Pattern.compile("(<a.*target=\"?)([^\\s\">]+)(\"?.*</a>)",
			Pattern.CASE_INSENSITIVE);

	private static final String STR_BLANK = "_blank";

	private static String checkTarget(final String anchorTag) {
		final Matcher m = PATTERN_TARGET.matcher(anchorTag);
		if (m.matches()) {
			if (!STR_BLANK.equalsIgnoreCase(m.group(2))) {
				final StringBuilder sb = new StringBuilder(128);
				return sb.append(m.group(1)).append(STR_BLANK).append(m.group(3)).toString();
			}
			return anchorTag;
		}
		/*
		 * No target specified
		 */
		final int pos = anchorTag.indexOf('>');
		if (pos == -1) {
			return anchorTag;
		}
		final StringBuilder sb = new StringBuilder(128);
		return sb.append(anchorTag.substring(0, pos)).append(" target=\"").append(STR_BLANK).append('"').append(
				anchorTag.substring(pos)).toString();
	}

	private static final String STR_IMG_SRC = "src=\"";

	private static boolean isImgSrc(final String line, final int start) {
		return start >= 5 && STR_IMG_SRC.equalsIgnoreCase(line.substring(start - 5, start));
	}

}
