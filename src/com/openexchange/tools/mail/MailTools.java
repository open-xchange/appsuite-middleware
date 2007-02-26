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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

;

/**
 * MailTools
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public class MailTools {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailTools.class);

	private static final String[] tColor = new String[] { "tr-style-1", "tr-style-2" };

	private boolean colorState;

	private static final String[] tags = new String[] { "\"", "'", ">", "<", "(\\r)?\\n" };

	private static final String[] replace = new String[] { "&quot;", "&#39;", "&gt;", "&lt;", "<br>" };

	public static final String NUMBER_DIGITS = "###,##0.00";

	public static final String NUMBER_WO_DIGITS = "#####0";

	private static final String URL_CHARACTER_ENCODING = "UTF-8";

	public static String htmlFormat(final String str, final boolean withQuote) {
		String retval = str;
		for (int i = withQuote ? 0 : 1; i < tags.length; i++) {
			retval = retval.replaceAll(tags[i], replace[i]);
		}
		return retval;
	}

	public static String htmlFormat(final String originalTag) {
		return htmlFormat(originalTag, false);
	}

	public static String getFormatedSize(final int tmp, final String locale) {
		try {
			return (formatNumber((Double.valueOf(tmp).doubleValue() / 1024d), locale, MailTools.NUMBER_DIGITS));
		} catch (NumberFormatException nfe) {
			return "0.00";
		}
	}

	public static String formatNumber(final double tmp, final String locale, final String pattern) {
		try {
			final Locale l = new Locale(locale.toLowerCase(), locale);
			final NumberFormat nf = NumberFormat.getInstance(l);
			final DecimalFormat df = (DecimalFormat) nf;
			// df.setMinimumFractionDigits(2);
			// df.setMaximumFractionDigits(2);
			df.setDecimalSeparatorAlwaysShown(true);
			df.applyPattern(pattern);
			return df.format(tmp);
		} catch (NumberFormatException nfe) {
			return "0.00";
		}
	}

	public static String formatDate(final Date date, final String locale, final String format, final String timezone) {
		try {
			final Locale l = new Locale(locale, locale.toLowerCase());
			final DateFormat df = new SimpleDateFormat(format, l);
			df.setTimeZone(TimeZone.getTimeZone(timezone));
			return (df.format(date));
		} catch (Exception e) {
			return (date.toString());
		}
	}

	public static String decodeRText(final String data) {
		try {
			return (MimeUtility.decodeText(data));
		} catch (Exception e) {
			return data;
		}
	}

	public String getColorChanger() {
		if (!colorState) {
			colorState = true;
			return tColor[0];
		} else {
			colorState = false;
			return tColor[1];
		}
	}

	private static final Pattern htmlPattern = Pattern.compile(
			"<a\\s+href[^>]+>.*?</a>|((?:http|https|ftp|mailto|news|www)(?::|://)[^<\\s]+)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Searching for links using a regexp pattern and convert them to valid href
	 * 
	 * @param line
	 * @return
	 */
	public static String formatHrefLinks(final String lineArg) {
		String line = lineArg;
		try {
			final Matcher m = htmlPattern.matcher(line);
			final StringBuffer sb = new StringBuffer(line.length());
			while (m.find()) {
				if (m.group(1) != null) {
					m.appendReplacement(sb, new StringBuilder(200).append("<a href=\"").append(
							(m.group(1).startsWith("www") ? "http://" : "")).append(
							"$1\" target=\"_blank\" class=\"a-external\">$1</a>").toString());
				} else {
					m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
				}
			}
			m.appendTail(sb);
			line = sb.toString();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return line;
	}

	public static String encodeUrl(final String url) {
		String retval = "";
		try {
			retval = URLEncoder.encode(url, URL_CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}
		return retval;
	}

	public static String decodeUrl(final String encodedUrl) {
		String retval = "";
		try {
			retval = URLDecoder.decode(encodedUrl, URL_CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}
		return retval;
	}

	public static String replaceLeadingWhitespace(final String originalTagArg, final String replaceWith) {
		String originalTag = originalTagArg;
		final StringBuilder retval = new StringBuilder();
		try {
			final StringReader sr = new StringReader(originalTag);
			final BufferedReader br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.charAt(0) == ' ') {
					final StringTokenizer st = new StringTokenizer(line, " ", true);
					boolean end = false;
					while (st.hasMoreTokens()) {
						final String tmp = st.nextToken();
						if (!end && tmp.equals(" ")) {
							retval.append(replaceWith);
						} else {
							retval.append(tmp);
							end = true;
						}
					}
				} else {
					retval.append(line);
				}
				retval.append("\n");
			}
			originalTag = retval.toString();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return originalTag;
	}

}
