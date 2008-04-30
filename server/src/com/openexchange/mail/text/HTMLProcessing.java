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

package com.openexchange.mail.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.tidy.Tidy;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.session.Session;

/**
 * {@link HTMLProcessing} - Various methods for HTML processing
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLProcessing {

	/**
	 * Performs all the formatting for both text and HTML content for a proper
	 * display according to specified user's mail settings.
	 * <p>
	 * If content is <b>plain text</b>:<br>
	 * <ol>
	 * <li>Plain text content is converted to valid HTML if at least
	 * {@link DisplayMode#MODIFYABLE} is given</li>
	 * <li>If enabled by settings simple quotes are turned to colored block
	 * quotes if {@link DisplayMode#DISPLAY} is given</li>
	 * <li>HTML links and URLs found in content are going to be prepared for
	 * proper display if {@link DisplayMode#DISPLAY} is given</li>
	 * </ol>
	 * If content is <b>HTML</b>:<br>
	 * <ol>
	 * <li>Both inline and non-inline images found in HTML content are prepared
	 * according to settings if {@link DisplayMode#DISPLAY} is given</li>
	 * </ol>
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
	 * @param usm
	 *            The settings used for formatting content
	 * @param mode
	 *            The display mode
	 * @return The formatted content
	 */
	public static String formatContentForDisplay(final String content, final boolean isHtml, final Session session,
			final MailPath mailPath, final UserSettingMail usm, final DisplayMode mode) {
		String retval = content;
		if (isHtml) {
			if (DisplayMode.DISPLAY.equals(mode) && usm.isDisplayHtmlInlineContent()) {
				retval = filterImages(retval, session, usm, mailPath);
			}
		} else {
			if (DisplayMode.MODIFYABLE.isIncluded(mode)) {
				retval = htmlFormat(retval);
			}
			if (DisplayMode.DISPLAY.equals(mode)) {
				if (usm.isUseColorQuote()) {
					retval = replaceHTMLSimpleQuotesForDisplay(retval);
				}
				retval = formatHrefLinks(retval);
			}
		}
		return retval;
	}

	/**
	 * The regular expression to match links inside both plain text and HTML
	 * content.
	 * <p>
	 * <b>WARNING</b>: May throw a {@link StackOverflowError} if a matched link
	 * is too large. Usages should handle this case.
	 */
	public static final Pattern PATTERN_HREF = Pattern
			.compile(
					"<a\\s+href[^>]+>.*?</a>|((?:https?://|ftp://|mailto:|news\\.|www\\.)(?:[-A-Z0-9+@#/%?=~_|!:,.;]|&amp;|&(?!\\w+;))*(?:[-A-Z0-9+@#/%=~_|]|&amp;|&(?!\\w+;)))",
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	/**
	 * Searches for non-HTML links and convert them to valid HTML links
	 * <p>
	 * Example: <code>http://www.somewhere.com</code> is converted to
	 * <code>&lt;a&nbsp;href=&quot;http://www.somewhere.com&quot;&gt;http://www.somewhere.com&lt;/a&gt;</code>
	 * 
	 * @param content
	 *            The content to search in
	 * @return The given content with all non-HTML links converted to valid HTML
	 *         links
	 */
	public static String formatHrefLinks(final String content) {
		try {
			final Matcher m = PATTERN_HREF.matcher(content);
			final StringBuffer sb = new StringBuffer(content.length());
			final StringBuilder tmp = new StringBuilder(256);
			while (m.find()) {
				final String nonHtmlLink = m.group(1);
				if ((nonHtmlLink == null) || (isImgSrc(content, m.start(1)))) {
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
		} catch (final StackOverflowError error) {
			LOG.error(StackOverflowError.class.getName(), error);
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
		return (start >= 5) && STR_IMG_SRC.equalsIgnoreCase(line.substring(start - 5, start));
	}

	private static final String HTML_META_TEMPLATE = "\r\n    <meta content=\"#CT#\" http-equiv=\"Content-Type\">";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HTMLProcessing.class);

	private static final Pattern PAT_META_CT = Pattern.compile("<meta[^>]*?http-equiv=\"?content-type\"?[^>]*?>",
			Pattern.CASE_INSENSITIVE);

	private static final String TAG_E_HEAD = "</head>";

	private static final String TAG_S_HEAD = "<head>";

	/**
	 * Creates valid HTML from specified HTML content conform to W3C standards.
	 * 
	 * @param htmlContentArg
	 *            The HTML content
	 * @param contentType
	 *            The corresponding content type (including charset parameter)
	 * @return The HTML content conform to W3C standards
	 */
	public static String getConformHTML(final String htmlContentArg, final ContentType contentType) {
		if ((htmlContentArg == null) || (htmlContentArg.length() == 0)) {
			/*
			 * Nothing to do...
			 */
			return htmlContentArg;
		}
		/*
		 * Validate with JTidy library
		 */
		final String htmlContent;
		{
			String charset = contentType.getCharsetParameter();
			if (null == charset) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Missing charset in HTML content type. Using fallback \"US-ASCII\" instead.");
				}
				charset = "US-ASCII";
				contentType.setCharsetParameter(charset);
			}
			htmlContent = validate(htmlContentArg, charset);
		}
		/*
		 * Check for meta tag in validated html content which indicates
		 * documents content type. Add if missing.
		 */
		final int start = htmlContent.indexOf(TAG_S_HEAD) + 6;
		if (start >= 6) {
			final Matcher m = PAT_META_CT.matcher(htmlContent.substring(start, htmlContent.indexOf(TAG_E_HEAD)));
			if (!m.find()) {
				final StringBuilder sb = new StringBuilder(htmlContent);
				sb.insert(start, HTML_META_TEMPLATE.replaceFirst("#CT#", contentType.toString()));
				return sb.toString();
			}
		}
		return htmlContent;
	}

	/**
	 * Validates specified HTML content with <a
	 * href="http://tidy.sourceforge.net/">tidy html</a> library
	 * 
	 * @param htmlContent
	 *            The HTML content
	 * @param charset
	 *            The character set encoding
	 * @return The validated HTML content
	 */
	public static String validate(final String htmlContent, final String charset) {
		/*
		 * Obtain a new Tidy instance
		 */
		final Tidy tidy = new Tidy();
		/*
		 * Set desired config options using tidy setters
		 */
		tidy.setXHTML(true);
		tidy.setConfigurationFromFile(SystemConfig.getProperty(SystemConfig.Property.TidyConfiguration));
		tidy.setForceOutput(true);
		tidy.setOutputEncoding(charset);
		/*
		 * Suppress tidy outputs
		 */
		tidy.setShowErrors(0);
		tidy.setShowWarnings(false);
		tidy.setErrout(new PrintWriter(new StringWriter()));
		/*
		 * Run tidy, providing an input and output stream
		 */
		final StringWriter writer = new StringWriter(htmlContent.length());
		tidy.parse(new StringReader(htmlContent), writer);
		return writer.toString();
	}

	private static final Pattern PATTERN_BLOCKQUOTE = Pattern.compile("(?:(<blockquote.*?>)|(</blockquote>))",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/**
	 * Converts given HTML content into plain text, but keeps
	 * <code>&lt;blockquote&gt;</code> tags if any present. <b>NOTE:</b>
	 * returned content is again HTML content
	 * 
	 * @param htmlContent
	 *            The HTML content
	 * @param converter
	 *            The instance of {@link Html2TextConverter}
	 * @return The partially converted plain text version of given HTML content
	 *         as HTML content
	 * @throws IOException
	 *             If an I/O error occurs
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

	private static Map<Character, String> htmlCharMap;

	private static Map<String, Character> htmlEntityMap;

	static void setMaps(final Map<Character, String> htmlCharMap, final Map<String, Character> htmlEntityMap) {
		HTMLProcessing.htmlCharMap = htmlCharMap;
		HTMLProcessing.htmlEntityMap = htmlEntityMap;
	}

	/**
	 * Maps specified HTML entity - e.g. <code>&amp;uuml;</code> - to
	 * corresponding ASCII character
	 * 
	 * @param entity
	 *            The HTML entity
	 * @return The corresponding ASCII character or <code>null</code>
	 */
	public static Character getHTMLEntity(final String entity) {
		if (null == entity) {
			return null;
		}
		String key = entity;
		if (key.charAt(0) == '&') {
			key = key.substring(1);
		}
		{
			final int lastPos = key.length() - 1;
			if (key.charAt(lastPos) == ';') {
				key = key.substring(0, lastPos);
			}
		}
		final Character tmp = htmlEntityMap.get(key);
		if (tmp != null) {
			return tmp;
		}
		return null;
	}

	private static String escape(final String s, final boolean withQuote) {
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
	 * Formats plain text to HTML by escaping HTML special characters e.g.
	 * <code>&lt;</code> => <code>&amp;lt;</code>
	 * 
	 * @param plainText
	 *            The plain text
	 * @param withQuote
	 *            Whether to escape quotes (<code>&quot;</code>) or not
	 * @return properly escaped HTML content
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
	 * Formats plain text to HTML by escaping HTML special characters e.g.
	 * <code>&lt;</code> => <code>&amp;lt;</code>
	 * <p>
	 * This is just a convenience method which invokes
	 * <code>{@link #htmlFormat(String, boolean)}</code> with latter parameter
	 * set to <code>true</code>
	 * 
	 * @param plainText
	 *            The plain text
	 * @return properly escaped HTML content
	 * @see #htmlFormat(String, boolean)
	 */
	public static String htmlFormat(final String plainText) {
		return htmlFormat(plainText, true);
	}

	private static final String DEFAULT_COLOR = "#0026ff";

	private static final String BLOCKQUOTE_START_TEMPLATE = "<blockquote type=\"cite\" style=\"margin-left: 0px; padding-left: 10px; color:%s; border-left: solid 1px %s;\">";

	/**
	 * Determines the quote color for given <code>quotelevel</code>
	 * 
	 * @param quotelevel -
	 *            the quote level
	 * @return the color for given <code>quotelevel</code>
	 */
	private static String getLevelColor(final int quotelevel) {
		final String[] colors = MailConfig.getQuoteLineColors();
		return (colors != null) && (colors.length > 0) ? (quotelevel >= colors.length ? colors[colors.length - 1]
				: colors[quotelevel]) : DEFAULT_COLOR;
	}

	private static final String BLOCKQUOTE_END = "</blockquote>\n";

	private static final String STR_HTML_QUOTE = "&gt;";

	private static final String STR_SPLIT_BR = "<br/?>";

	private static final String HTML_BREAK = "<br>";

	/**
	 * Turns all simple quotes "&amp;gt; " occurring in specified HTML text to
	 * colored "&lt;blockquote&gt;" tags according to configured quote colors
	 * 
	 * @param htmlText
	 *            The HTML text
	 * @return HTML text with simple quotes replaced with block quotes
	 */
	public static String replaceHTMLSimpleQuotesForDisplay(final String htmlText) {
		final StringBuilder sb = new StringBuilder(htmlText.length());
		final String[] lines = htmlText.split(STR_SPLIT_BR);
		int levelBefore = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int currentLevel = 0;
			int offset = 0;
			if ((offset = startsWithQuote(line)) != -1) {
				currentLevel++;
				int pos = -1;
				boolean next = true;
				while (next && ((pos = line.indexOf(STR_HTML_QUOTE, offset)) > -1)) {
					/*
					 * Continue only if next starting position is equal to
					 * offset or if just one whitespace character has been
					 * skipped
					 */
					next = ((offset == pos) || ((pos - offset == 1) && Character.isWhitespace(line.charAt(offset))));
					if (next) {
						currentLevel++;
						offset = (pos + 4);
					}
				}
			}
			if (offset > 0) {
				try {
					offset = (offset < line.length()) && Character.isWhitespace(line.charAt(offset)) ? offset + 1
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
			sb.append(line);
			if (i < lines.length - 1) {
				sb.append(HTML_BREAK);
			}
		}
		return sb.toString();
	}

	private static final Pattern PAT_STARTS_WITH_QUOTE = Pattern.compile("\\s*&gt;\\s*", Pattern.CASE_INSENSITIVE);

	private static int startsWithQuote(final String str) {
		final Matcher m = PAT_STARTS_WITH_QUOTE.matcher(str);
		if (m.find() && (m.start() == 0)) {
			return m.end();
		}
		return -1;
	}

	private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern CID_PATTERN = Pattern.compile("(?:src=cid:([^\\s>]*))|(?:src=\"cid:([^\"]*)\")",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern FILENAME_PATTERN = Pattern.compile(
			"src=\"?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)\"?", Pattern.CASE_INSENSITIVE);

	private static final Pattern SRC_PATTERN = Pattern.compile("src=", Pattern.CASE_INSENSITIVE);

	private static final String STR_AJAX_MAIL = "\"/ajax/mail?";

	private static final String STR_SRC = "src=";

	private static final String STR_OXSRC = "oxsrc=";

	private static final String CHARSET_ISO8859 = "ISO-8859-1";

	/**
	 * Filters images occurring in HTML content of a message:
	 * <ul>
	 * <li>Inline images<br>
	 * The source of inline images is in the message itself. Thus loading the
	 * inline image is redirected to the appropriate message (image) attachment
	 * identified through header <code>Content-Id</code>; e.g.:
	 * <code>&lt;img src=&quot;cid:[cid-value]&quot; ... /&gt;</code>.</li>
	 * <li>Non-Inline images<br>
	 * The source of non-inline images is somewhere in the internet and
	 * therefore loading such an image should be done with extreme caution.
	 * That's why the user is able to generally deny/allow loading of non-inline
	 * images. If denied the <i>"src"</i> attribute of corresponding <i>"img"</i>
	 * tag is replaced with <i>"oxsrc"</i> to suppress image loading but also
	 * allow front-end load them on demand. </li>
	 * </ul>
	 * 
	 * @param content
	 *            The HTML content possibly containing images
	 * @param session
	 *            The session providing needed user data
	 * @param usm
	 *            The user's mail settings
	 * @param msgUID
	 *            The message's unique path in mailbox
	 * @return The HTML content with all inline images replaced with valid links
	 */
	public static String filterImages(final String content, final Session session, final UserSettingMail usm,
			final MailPath msgUID) {
		String reval = content;
		try {
			final Matcher imgMatcher = IMG_PATTERN.matcher(reval);
			final StringBuffer sb = new StringBuffer(reval.length());
			if (imgMatcher.find()) {
				final StringBuffer strBuffer = new StringBuffer(256);
				/*
				 * Replace inline images with Content-ID
				 */
				do {
					final String imgTag = imgMatcher.group();
					boolean isInline = false;
					if (!(isInline = replaceImgSrc(session, msgUID, imgTag, strBuffer))) {
						/*
						 * No cid pattern found, try with filename
						 */
						strBuffer.setLength(0);
						final Matcher m = FILENAME_PATTERN.matcher(imgTag);
						if (m.find()) {
							isInline = true;
							final StringBuilder linkBuilder = new StringBuilder(256);
							final String filename = m.group(1);
							linkBuilder.append(STR_SRC).append(STR_AJAX_MAIL).append(AJAXServlet.PARAMETER_SESSION)
									.append('=').append(session.getSecret()).append('&').append(
											AJAXServlet.PARAMETER_ACTION).append('=')
									.append(AJAXServlet.ACTION_MATTACH).append('&').append(
											AJAXServlet.PARAMETER_FOLDERID).append('=').append(
											urlEncodeSafe(msgUID.getFolder(), CHARSET_ISO8859)).append('&').append(
											AJAXServlet.PARAMETER_ID).append('=').append(msgUID.getUid()).append('&')
									.append(Mail.PARAMETER_MAILCID).append('=').append(filename).append('"');
							m.appendReplacement(strBuffer, Matcher.quoteReplacement(linkBuilder.toString()));
						}
						m.appendTail(strBuffer);
					}
					if (!isInline && !(usm.isAllowHTMLImages())) {
						/*
						 * User does not allow to display images by default:
						 * Replace "src" with "oxsrc"
						 */
						strBuffer.setLength(0);
						final Matcher m = SRC_PATTERN.matcher(imgTag);
						if (m.find()) {
							m.appendReplacement(strBuffer, Matcher.quoteReplacement(STR_OXSRC));
						}
						m.appendTail(strBuffer);
					}
					imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(strBuffer.toString()));
					strBuffer.setLength(0);
				} while (imgMatcher.find());
			}
			imgMatcher.appendTail(sb);
			reval = sb.toString();
		} catch (final Exception e) {
			LOG.warn("Unable to filter cid Images: " + e.getMessage());
		}
		return reval;
	}

	private static boolean replaceImgSrc(final Session session, final MailPath msgUID, final String imgTag,
			final StringBuffer cidBuffer) {
		boolean retval = false;
		final Matcher cidMatcher = CID_PATTERN.matcher(imgTag);
		if (cidMatcher.find()) {
			retval = true;
			final StringBuilder linkBuilder = new StringBuilder(256);
			do {
				final String cid = (cidMatcher.group(1) == null ? cidMatcher.group(2) : cidMatcher.group(1));
				linkBuilder.setLength(0);
				linkBuilder.append(STR_SRC).append(STR_AJAX_MAIL).append(AJAXServlet.PARAMETER_SESSION).append('=')
						.append(session.getSecret()).append('&').append(AJAXServlet.PARAMETER_ACTION).append('=')
						.append(AJAXServlet.ACTION_MATTACH).append('&').append(AJAXServlet.PARAMETER_FOLDERID).append(
								'=').append(urlEncodeSafe(msgUID.getFolder(), CHARSET_ISO8859)).append('&').append(
								AJAXServlet.PARAMETER_ID).append('=').append(msgUID.getUid()).append('&').append(
								Mail.PARAMETER_MAILCID).append('=').append(cid).append('"');
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

	/**
	 * Initializes a new {@link HTMLProcessing}
	 */
	private HTMLProcessing() {
		super();
	}
}
