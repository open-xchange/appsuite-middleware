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

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.openexchange.ajax.Mail;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Html2TextConverter;

/**
 * {@link MessageUtility} - Provides various helper methods for message
 * processing
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageUtility.class);

	/**
	 * No instantiation
	 */
	private MessageUtility() {
		super();
	}

	private static final Pattern PAT_QUOTED = Pattern.compile("(^\")([^\"]+?)(\"$)");

	private static final Pattern PAT_QUOTABLE_CHAR = Pattern.compile("[.,:;<>\"]");

	/**
	 * Quotes given personal part of an internet address according to RFC 822
	 * syntax if needed; otherwise the personal is returned unchanged.
	 * <p>
	 * This method guarantees that the resulting string can be used to build an
	 * internet address according to RFC 822 syntax so that the
	 * <code>{@link InternetAddress#parse(String)}</code> constructor won't
	 * throw an instance of <code>{@link AddressException}</code>.
	 * 
	 * <pre>
	 * final String quotedPersonal = quotePersonal(&quot;Doe, Jane&quot;);
	 * 
	 * final String buildAddr = quotedPersonal + &quot; &lt;someone@somewhere.com&gt;&quot;;
	 * System.out.println(buildAddr);
	 * //Plain Address: &quot;=?UTF-8?Q?Doe=2C_Jan=C3=A9?=&quot; &lt;someone@somewhere.com&gt;
	 * 
	 * final InternetAddress ia = new InternetAddress(buildAddr);
	 * System.out.println(ia.toUnicodeString());
	 * //Unicode Address: &quot;Doe, Jane&quot; &lt;someone@somewhere.com&gt;
	 * </pre>
	 * 
	 * @param personal
	 *            The personal
	 * @return The properly quoted personal for building an internet address
	 *         according to RFC 822 syntax
	 */
	public static String quotePersonal(final String personalArg) {
		try {
			final String personal = MimeUtility.encodeWord(personalArg);
			if (PAT_QUOTED.matcher(personal).matches() ? false : PAT_QUOTABLE_CHAR.matcher(personal).find()) {
				/*
				 * Quote
				 */
				return new StringBuilder(personal.length() + 2).append('"').append(
						personal.replaceAll("\"", "\\\\\\\"")).append('"').toString();
			}
			return personal;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return personalArg;
		}
	}

	/**
	 * Parse the given sequence of addresses into InternetAddress objects by
	 * invoking <code>{@link InternetAddress#parse(String, boolean)}</code>.
	 * If <code>strict</code> is false, simple email addresses separated by
	 * spaces are also allowed. If <code>strict</code> is true, many (but not
	 * all) of the RFC822 syntax rules are enforced. In particular, even if
	 * <code>strict</code> is true, addresses composed of simple names (with
	 * no "@domain" part) are allowed. Such "illegal" addresses are not uncommon
	 * in real messages.
	 * <p>
	 * Non-strict parsing is typically used when parsing a list of mail
	 * addresses entered by a human. Strict parsing is typically used when
	 * parsing address headers in mail messages.
	 * <p>
	 * Additionally the personal parts are MIME encoded using default MIME
	 * charset
	 * 
	 * @param addresslist -
	 *            comma separated address strings
	 * @param strict -
	 *            <code>true</code> to enforce RFC822 syntax; otherwise
	 *            <code>false</code>
	 * @return array of <code>InternetAddress</code> objects
	 * @throws AddressException -
	 *             if parsing fails
	 */
	public static InternetAddress[] parseAddressList(final String addresslist, final boolean strict)
			throws AddressException {
		final InternetAddress[] addrs = InternetAddress
				.parse(replaceWithComma(MimeUtility.unfold(addresslist)), strict);
		for (int i = 0; i < addrs.length; i++) {
			try {
				addrs[i].setPersonal(addrs[i].getPersonal(), MailConfig.getDefaultMimeCharset());
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			}
		}
		return addrs;
	}

	private static final Pattern PATTERN_REPLACE = Pattern.compile("([^\"]\\S+?)(\\s*)([;|])(\\s*)");

	private static String replaceWithComma(final String addressList) {
		final StringBuilder sb = new StringBuilder();
		final Matcher m = PATTERN_REPLACE.matcher(addressList);
		int lastMatch = 0;
		while (m.find()) {
			sb.append(addressList.substring(lastMatch, m.start()));
			sb.append(m.group(1)).append(m.group(2)).append(',').append(m.group(4));
			lastMatch = m.end();
		}
		sb.append(addressList.substring(lastMatch));
		return sb.toString();
	}

	private static final Pattern ENC_PATTERN = Pattern.compile("(=\\?\\S+?\\?\\S+?\\?)(\\S+?)(\\?=)");

	/**
	 * Decodes a multi-mime-encoded header value using the algorithm specified
	 * in RFC 2047, Section 6.1
	 * <p>
	 * If the charset-conversion fails for any sequence, an
	 * {@link UnsupportedEncodingException} is thrown.
	 * <p>
	 * If the String is not an RFC 2047 style encoded header, it is returned
	 * as-is
	 * 
	 * @param headerValArg
	 *            The possibly encoded header value
	 * @return The possibly decoded header value
	 */
	public static String decodeMultiEncodedHeader(final String headerValArg) {
		if (headerValArg == null) {
			return null;
		}
		final String hdrVal = MimeUtility.unfold(headerValArg);
		final Matcher m = ENC_PATTERN.matcher(hdrVal);
		if (m.find()) {
			final StringBuilder sb = new StringBuilder(hdrVal.length());
			int lastMatch = 0;
			do {
				try {
					sb.append(hdrVal.substring(lastMatch, m.start()));
					sb.append(Matcher.quoteReplacement(MimeUtility.decodeText(m.group())));
					lastMatch = m.end();
				} catch (final UnsupportedEncodingException e) {
					LOG.error("Unsupported encoding in a message detected and monitored.", e);
					MailInterfaceImpl.mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
					sb.append(hdrVal.substring(lastMatch));
				}
			} while (m.find());
			sb.append(hdrVal.substring(lastMatch));
			return sb.toString();
		}
		return hdrVal;
	}

	private static final String SPLIT_LINES = "(\r)?\n";

	private static final char CHAR_BREAK = '\n';

	/**
	 * Performs the line folding after specified number of characters through
	 * <code>linewrap</code>. Occuring html links are excluded.
	 * <p>
	 * If parameter <code>isHtml</code> is set to <code>true</code> the
	 * content is returned unchanged.
	 * 
	 * @param content
	 *            The content
	 * @param isHtml
	 *            <code>true</code> if content is html content; otherwise
	 *            <code>false</code>
	 * @param linewrap
	 *            The number of characters which may fit into a line
	 * @return The line-folded content
	 */
	public static String performLineFolding(final String content, final boolean isHtml, final int linewrap) {
		if (linewrap <= 0) {
			return content;
		}
		final StringBuilder sb = new StringBuilder(content.length() + 128);
		if (isHtml) {
			return content;
		}
		final String[] lines = content.split(SPLIT_LINES);
		for (int i = 0; i < lines.length; i++) {
			sb.append(foldTextLine(lines[i], linewrap)).append(CHAR_BREAK);
		}
		return sb.toString();
	}

	private static String foldTextLine(final String line, final int linewrap) {
		return foldTextLineRecursive(line, linewrap, null, true);
	}

	private static String foldTextLineRecursive(final String line, final int linewrap, final String quoteArg,
			final boolean lookUpQuote) {
		final int length = line.length();
		if (length <= linewrap) {
			return line;
		}
		final int[] hrefIndices = getHrefIndices(line);
		final String quote = lookUpQuote ? getQuotePrefix(line) : quoteArg;
		final int startPos = quote == null ? 0 : quote.length();
		final char c = line.charAt(linewrap);
		final StringBuilder sb = new StringBuilder(length + 5);
		final StringBuilder sub = new StringBuilder();
		if (Character.isWhitespace(c)) {
			/*
			 * Find last non-whitespace character before
			 */
			int i = linewrap - 1;
			int[] sep = null;
			while (i >= startPos) {
				if (!Character.isWhitespace(line.charAt(i))) {
					if ((sep = isLineBreakInsideHref(hrefIndices, i)) != null) {
						i = sep[0] - 1;
						continue;
					}
					sb.setLength(0);
					sub.setLength(0);
					return sb.append(line.substring(0, i + 1)).append(CHAR_BREAK).append(
							foldTextLineRecursive(quote == null ? line.substring(linewrap + 1) : sub.append(quote)
									.append(line.substring(i + 1)).toString(), linewrap, quote, false)).toString();
				}
				i--;
			}
		} else {
			/*
			 * Find last whitespace before
			 */
			int i = linewrap - 1;
			int[] sep = null;
			while (i >= startPos) {
				if (Character.isWhitespace(line.charAt(i))) {
					if ((sep = isLineBreakInsideHref(hrefIndices, i)) != null) {
						i = sep[0] - 1;
						continue;
					}
					sb.setLength(0);
					sub.setLength(0);
					return sb.append(line.substring(0, i)).append(CHAR_BREAK).append(
							foldTextLineRecursive(quote == null ? line.substring(i + 1) : sub.append(quote).append(
									line.substring(i + 1)).toString(), linewrap, quote, false)).toString();
				}
				i--;
			}
		}
		final int[] sep = isLineBreakInsideHref(hrefIndices, linewrap);
		if (sep == null) {
			return new StringBuilder(line.length() + 1).append(line.substring(0, linewrap)).append(CHAR_BREAK).append(
					foldTextLineRecursive(quote == null ? line.substring(linewrap) : new StringBuilder().append(quote)
							.append(line.substring(linewrap)).toString(), linewrap, quote, false)).toString();
		} else if (sep[1] == length) {
			if (sep[0] == startPos) {
				return line;
			}
			return new StringBuilder(line.length() + 1).append(line.substring(0, sep[0])).append(CHAR_BREAK).append(
					foldTextLineRecursive(quote == null ? line.substring(sep[0]) : new StringBuilder().append(quote)
							.append(line.substring(sep[0])).toString(), linewrap, quote, false)).toString();
		}
		return new StringBuilder(line.length() + 1).append(line.substring(0, sep[1])).append(CHAR_BREAK).append(
				foldTextLineRecursive(quote == null ? line.substring(sep[1]) : new StringBuilder().append(quote)
						.append(line.substring(sep[1])).toString(), linewrap, quote, false)).toString();
	}

	private static final Pattern PATTERN_QP = Pattern.compile("((?:\\s?>)+)(\\s?)(.*)");

	private static String getQuotePrefix(final String line) {
		final Matcher m = PATTERN_QP.matcher(line);
		return m.matches() ? new StringBuilder(m.group(1)).append(m.group(2)).toString() : null;
	}

	private static int[] getHrefIndices(final String line) {
		final SmartIntArray sia = new SmartIntArray(10);
		final Matcher m = PATTERN_HREF.matcher(line);
		while (m.find()) {
			sia.append(m.start());
			sia.append(m.end());
		}
		return sia.toArray();
	}

	private static int[] isLineBreakInsideHref(final int[] hrefIndices, final int linewrap) {
		for (int i = 0; i < hrefIndices.length; i += 2) {
			if (hrefIndices[i] <= linewrap && hrefIndices[i + 1] > linewrap) {
				return new int[] { hrefIndices[i], hrefIndices[i + 1] };
			}
		}
		/*
		 * Not inside a href declaration
		 */
		return null;
	}

	/**
	 * Performs all the formatting for both text and html content for a proper
	 * display according to user's configuration. The original content keeps
	 * unaffected.
	 * 
	 * @param content
	 *            The content
	 * @param isHtml
	 *            <code>true</code> if content is of type
	 *            <code>text/html</code>; otherwise <code>false</code>
	 * @param session
	 *            The session providing needed user data
	 * @param mailPath
	 *            The message's unique path in mailbox
	 * @param displayVersion
	 *            <code>true</code> to prepare content ready for display-only
	 *            purpose; otherwise to prepare content for some kind of
	 *            editable display
	 * @return The formatted content
	 */
	public static String formatContentForDisplay(final String content, final boolean isHtml,
			final SessionObject session, final String mailPath, final boolean displayVersion) {
		final UserSettingMail usm = session.getUserSettingMail();
		String retval = content;
		if (isHtml) {
			/*
			 * Filter inline images
			 */
			if (displayVersion && usm.isDisplayHtmlInlineContent()) {
				retval = filterInlineImages(retval, session, mailPath);
			}
		} else {
			/*
			 * Replace special characters and insert color quotes
			 */
			retval = htmlFormat(retval);
			if (usm.isUseColorQuote()) {
				retval = replaceHTMLSimpleQuotesForDisplay(retval);
			}
			retval = formatHrefLinks(retval);
		}
		return retval;
	}

	private static final String[] COLORS = MailConfig.getQuoteLineColors();

	private static final String DEFAULT_COLOR = "#0026ff";

	private static final String BLOCKQUOTE_START_TEMPLATE = "<blockquote type=\"cite\" style=\"margin-left: 0px; padding-left: 10px; color:%s; border-left: solid 1px %s;\">";

	/**
	 * Determines the quote color for given <code>quotelevel</code>
	 * 
	 * @param quotelevel -
	 *            tho quote level
	 * @return the color for given <code>quotelevel</code>
	 */
	private static String getLevelColor(final int quotelevel) {
		return COLORS != null && COLORS.length > 0 ? (quotelevel >= COLORS.length ? COLORS[COLORS.length - 1]
				: COLORS[quotelevel]) : DEFAULT_COLOR;
	}

	private static final String BLOCKQUOTE_END = "</blockquote>\n";

	private static final String REGEX_HTML_QUOTE = "\\s*&gt;\\s*";

	private static final String STR_HTML_QUOTE = "&gt;";

	private static final String STR_SPLIT_BR = "<br/?>";

	private static final String HTML_BREAK = "<br>";

	/**
	 * Turns all simple quotes "&amp;gt; " to colored "&lt;blockquote&gt;" tags
	 * according to configured quote colors in file "imap.properties"
	 */
	public static String replaceHTMLSimpleQuotesForDisplay(final String htmlText) {
		final StringBuilder sb = new StringBuilder();
		final String[] lines = htmlText.split(STR_SPLIT_BR);
		int levelBefore = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int currentLevel = 0;
			int offset = 0;
			if ((offset = startsWithRegex(line, REGEX_HTML_QUOTE)) != -1) {
				currentLevel++;
				int pos = -1;
				boolean next = true;
				while (next && (pos = line.indexOf(STR_HTML_QUOTE, offset)) > -1) {
					/*
					 * Continue only if next starting position is equal to
					 * offset or if just one whitespace character has been
					 * skipped
					 */
					next = (offset == pos || (pos - offset == 1 && Character.isWhitespace(line.charAt(offset))));
					if (next) {
						currentLevel++;
						offset = (pos + 4);
					}
				}
			}
			if (offset > 0) {
				try {
					offset = offset < line.length() && Character.isWhitespace(line.charAt(offset)) ? offset + 1
							: offset;
				} catch (final StringIndexOutOfBoundsException e) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(e.getMessage(), e);
					}
				}
				line = line.substring(offset);
			}
			if (levelBefore < currentLevel) {
				for (; levelBefore < currentLevel; levelBefore++) {
					final String color = getLevelColor(levelBefore);
					sb.append(String.format(BLOCKQUOTE_START_TEMPLATE, color, color));
				}
			} else if (levelBefore > currentLevel) {
				for (; levelBefore > currentLevel; levelBefore--) {
					sb.append(BLOCKQUOTE_END);
				}
			}
			sb.append(line).append(HTML_BREAK);
		}
		return sb.toString();
	}

	private static int startsWithRegex(final String str, final String regex) {
		final Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(str);
		if (m.find() && m.start() == 0) {
			return m.end();
		}
		return -1;
	}

	private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern CID_PATTERN = Pattern.compile("cid:([^\\s>]*)|\"cid:([^\"]*)\"",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern FILENAME_PATTERN = Pattern.compile(
			"src=\"?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)\"?", Pattern.CASE_INSENSITIVE);

	private static final String STR_AJAX_MAIL = "\"/ajax/mail?";

	private static final String CHARSET_ISO8859 = "ISO-8859-1";

	/**
	 * Replaces all occurences of <code>&lt;img cid:&quot;[cid]&quot;...</code>
	 * with links to the image content to let the browser display the inlined
	 * images
	 * 
	 * @param content
	 *            The html content possibly containing inline images
	 * @param session
	 *            The session providing needed user data
	 * @param msgUID
	 *            The message's unique path in mailbox
	 * @return The html content with all inline images replaced with valid links
	 */
	public static String filterInlineImages(final String content, final SessionObject session, final String msgUID) {
		String reval = content;
		try {
			final Matcher imgMatcher = IMG_PATTERN.matcher(reval);
			final StringBuffer sb = new StringBuffer(reval.length());
			if (imgMatcher.find()) {
				final StringBuffer cidBuffer = new StringBuffer(256);
				do {
					final String imgTag = imgMatcher.group();
					if (!replaceImgSrc(session, msgUID, imgTag, cidBuffer)) {
						/*
						 * No cid found, try with filename
						 */
						cidBuffer.setLength(0);
						final Matcher m = FILENAME_PATTERN.matcher(imgTag);
						if (m.find()) {
							final StringBuilder linkBuilder = new StringBuilder(256);
							final String filename = m.group(1);
							linkBuilder.append("src=").append(STR_AJAX_MAIL).append(Mail.PARAMETER_SESSION).append('=')
									.append(session.getSecret()).append('&').append(Mail.PARAMETER_ACTION).append('=')
									.append(Mail.ACTION_MATTACH).append('&').append(Mail.PARAMETER_ID).append('=')
									.append(urlEncodeSafe(msgUID, CHARSET_ISO8859)).append('&').append(
											Mail.PARAMETER_MAILCID).append('=').append(filename).append('"');
							m.appendReplacement(cidBuffer, Matcher.quoteReplacement(linkBuilder.toString()));
						}
						m.appendTail(cidBuffer);
					}
					imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(cidBuffer.toString()));
					cidBuffer.setLength(0);
				} while (imgMatcher.find());
			}
			imgMatcher.appendTail(sb);
			reval = sb.toString();
		} catch (final Exception e) {
			LOG.warn("Unable to filter cid Images: " + e.getMessage());
		}
		return reval;
	}

	private static boolean replaceImgSrc(final SessionObject session, final String msgUID, final String imgTag,
			final StringBuffer cidBuffer) {
		boolean retval = false;
		final Matcher cidMatcher = CID_PATTERN.matcher(imgTag);
		if (cidMatcher.find()) {
			retval = true;
			final StringBuilder linkBuilder = new StringBuilder(256);
			do {
				final String cid = (cidMatcher.group(1) == null ? cidMatcher.group(2) : cidMatcher.group(1));
				linkBuilder.setLength(0);
				linkBuilder.append(STR_AJAX_MAIL).append(Mail.PARAMETER_SESSION).append('=')
						.append(session.getSecret()).append('&').append(Mail.PARAMETER_ACTION).append('=').append(
								Mail.ACTION_MATTACH).append('&').append(Mail.PARAMETER_ID).append('=').append(
								urlEncodeSafe(msgUID, CHARSET_ISO8859)).append('&').append(Mail.PARAMETER_MAILCID)
						.append('=').append(cid).append('"');
				cidMatcher.appendReplacement(cidBuffer, Matcher.quoteReplacement(linkBuilder.toString()));
			} while (cidMatcher.find());
		}
		cidMatcher.appendTail(cidBuffer);
		return retval;
	}

	private static String urlEncodeSafe(final String text, final String charset) {
		try {
			return URLEncoder.encode(text, charset);
		} catch (final UnsupportedEncodingException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return text;
		}
	}

	private static final Pattern PATTERN_BLOCKQUOTE = Pattern.compile("(?:(<blockquote.*?>)|(</blockquote>))",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/**
	 * onverts given HTML content into plain text, but keeps
	 * <code>&lt;blockquote&gt;</code> tags if any present. <b>NOTE:</b>
	 * returned content again is html content
	 * 
	 * @param htmlContent
	 *            The html content
	 * @param converter
	 *            The instance of {@link Html2TextConverter}
	 * @return The partially converted plain text version of given html content
	 *         as html content
	 * @throws IOException
	 */
	public static String convertAndKeepQuotes(final String htmlContent, final Html2TextConverter converter)
			throws IOException {

		final StringBuilder sb = new StringBuilder(htmlContent.length() + 128);
		final Matcher m = PATTERN_BLOCKQUOTE.matcher(htmlContent);
		int lastMatch = 0;
		while (m.find()) {
			sb.append(htmlFormat(converter.convert(htmlContent.substring(lastMatch, m.start()))));
			sb.append(m.group());
			lastMatch = m.end();
		}
		sb.append(htmlFormat(converter.convert(htmlContent.substring(lastMatch))));
		return sb.toString();
	}

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

	private static final String HTML_BR = "<br>";

	private static final String REPL_LINEBREAK = "\r?\n";

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
		return htmlFormat(plainText, true);
	}

	private static final Pattern PATTERN_HREF = Pattern
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

	private static final Pattern PATTERN_TARGET = Pattern.compile("(<a[^>]*?target=\"?)([^\\s\">]+)(\"?.*</a>)",
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

	private static final String STR_CHARSET = "charset";

	/**
	 * Reads the string out of MIME part's input stream. On first try the input
	 * stream retrieved by <code>javax.mail.Part.getInputStream()</code> is
	 * used. If an I/O error occurs (<code>java.io.IOException</code>) then
	 * the next try is with part's raw input stream. If everything fails an
	 * empty string is returned.
	 * 
	 * @param p -
	 *            the <code>javax.mail.Part</code> object
	 * @param ct -
	 *            the part's content type
	 * @return the string read from part's input stream or the empty string ""
	 *         if everything failed
	 * @throws MessagingException -
	 *             if an error occurs in part's getter methods
	 */
	public static String readMimePart(final Part p, final ContentType ct) throws MessagingException {
		/*
		 * Use specified charset if available else use default one
		 */
		String charset = ct.getParameter(STR_CHARSET);
		if (null == charset) {
			charset = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
		}
		try {
			return readStream(p.getInputStream(), charset);
		} catch (final IOException e) {
			/*
			 * Try to get data from raw input stream
			 */
			final InputStream inStream;
			if (p instanceof MimeBodyPart) {
				final MimeBodyPart mpb = (MimeBodyPart) p;
				inStream = mpb.getRawInputStream();
			} else if (p instanceof MimeMessage) {
				final MimeMessage mm = (MimeMessage) p;
				inStream = mm.getRawInputStream();
			} else {
				inStream = null;
			}
			if (inStream == null) {
				/*
				 * Neither a MimeBodyPart nor a MimeMessage
				 */
				return "";
			}
			try {
				return readStream(inStream, charset);
			} catch (final IOException e1) {
				LOG.error(e1.getLocalizedMessage(), e1);
				return e1.getLocalizedMessage();
				// return STR_EMPTY;
			} finally {
				try {
					inStream.close();
				} catch (final IOException e1) {
					LOG.error(e1.getLocalizedMessage(), e1);
				}
			}
		}
	}

	/**
	 * Reads the stream content from given mail part
	 * 
	 * @param mailPart
	 *            The mail part
	 * @param charset
	 *            The charset encoding used to generate a {@link String} object
	 *            from raw bytes
	 * @return the <code>String</code> read from mail part's stream
	 * @throws IOException
	 * @throws MailException
	 */
	public static String readMailPart(final MailPart mailPart, final String charset) throws IOException, MailException {
		return readStream(mailPart.getInputStream(), charset);
	}

	private static final int BUFSIZE = 8192; // 8K

	private static final int STRBLD_SIZE = 32768; // 32K

	/**
	 * Reads a string from given input stream using direct buffering
	 * 
	 * @param inStream -
	 *            the input stream
	 * @param charset -
	 *            the charset
	 * @return the <code>String</code> read from input stream
	 * @throws IOException -
	 *             if an I/O error occurs
	 */
	public static String readStream(final InputStream inStream, final String charset) throws IOException {
		InputStreamReader isr = null;
		try {
			int count = 0;
			final char[] c = new char[BUFSIZE];
			isr = new InputStreamReader(inStream, charset);
			if ((count = isr.read(c)) > 0) {
				final StringBuilder sb = new StringBuilder(STRBLD_SIZE);
				do {
					sb.append(c, 0, count);
				} while ((count = isr.read(c)) > 0);
				return sb.toString();
			}
			return "";
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return "";
		} finally {
			if (null != isr) {
				try {
					isr.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}
}
