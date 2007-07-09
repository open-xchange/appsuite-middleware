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

package com.openexchange.groupware.container.mail.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.swing.text.html.HTMLEditorKit;

import com.openexchange.ajax.Mail;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.imap.UserSettingMail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.Html2TextConverter;
import com.openexchange.tools.mail.MailTools;
import com.sun.mail.imap.IMAPFolder;

/**
 * MessageUtils
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MessageUtils {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageUtils.class);
	
	private static final int INT_100 = 100;
	
	private static final int INT_1000 = 1000;

	private static final String HTML_BREAK = "<br>";

	private static final Pattern PLAIN_TEXT_QUOTE_PATTERN = Pattern.compile("(\\G\\s?>)", Pattern.CASE_INSENSITIVE);

	private static final Pattern HTML_QUOTE_PATTERN = Pattern.compile("(\\G\\s?&gt;)", Pattern.CASE_INSENSITIVE);

	private static final Pattern IMG_PATTERN = Pattern.compile("(<img[^>]*>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern CID_PATTERN = Pattern.compile("cid:([^\\s>]*)|\"cid:([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern ENC_PATTERN = Pattern.compile("(=\\?\\S+?\\?\\S+?\\?)(\\S+?)(\\?=)");

	private static class HTMLParse extends HTMLEditorKit {

		/**
		 * Call to obtain a HTMLEditorKit.Parser object.
		 * 
		 * @return A new HTMLEditorKit.Parser object.
		 */
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	private static class HTMLCallback extends HTMLEditorKit.ParserCallback {

		private final List<int[]> positionList;

		private final int linewrap;

		public HTMLCallback(final int linewrap) {
			this.positionList = new ArrayList<int[]>();
			this.linewrap = linewrap;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.html.HTMLEditorKit$ParserCallback#handleText(char[],
		 *      int)
		 */
		public void handleText(final char[] chars, final int pos) {
			if (chars.length > linewrap) {
				positionList.add(new int[] { pos, pos + chars.length });
			}
		}

		/**
		 * @return a <code>java.util.List</code> containing all starting and
		 *         ending positions of html lines that have to be line-wraped
		 */
		public List<int[]> getPositionList() {
			return positionList;
		}
	}

	private MessageUtils() {
		super();
	}

	public static final String decodeMultiEncodedHeader(final String hdrVal) {
		if (hdrVal == null) {
			return null;
		}
		final Matcher m = ENC_PATTERN.matcher(hdrVal);
		if (m.find()) {
			final StringBuilder sb = new StringBuilder();
			int lastMatch = 0;
			do {
				try {
					sb.append(hdrVal.substring(lastMatch, m.start()));
					sb.append(Matcher.quoteReplacement(MimeUtility.decodeText(m.group())));
					lastMatch = m.end();
				} catch (UnsupportedEncodingException e) {
					sb.append(hdrVal.substring(lastMatch));
				}
			} while (m.find());
			sb.append(hdrVal.substring(lastMatch));
			return removeHdrLineBreak(sb.toString());
		}
		return removeHdrLineBreak(hdrVal);
	}
	
	private static final Pattern PATTERN_RMV_HDR_BR = Pattern.compile("(\r)?\n(\\s{1})?");
	
	private static final String STR_EMPTY = "";
	
	private static final char CHAR_BREAK = '\n';
	
	private static final char CHAR_CONTROL = '\r';
	
	public static final String removeHdrLineBreak(final String hdrVal) {
		return PATTERN_RMV_HDR_BR.matcher(hdrVal).replaceAll(STR_EMPTY);
	}

	public static final String performLineWrap(final String content, final boolean isHtml, final int linewrap) {
		if (linewrap <= 0) {
			return content;
		}
		final StringBuilder sb = new StringBuilder(content.length() + INT_100);
		if (isHtml) {
			return content;
//			/*
//			 * Use of javax.swing.text.html package to easily parse text content
//			 * out of html content
//			 */
//			final StringReader r = new StringReader(content);
//			final HTMLEditorKit.Parser parse = new HTMLParse().getParser();
//			final HTMLCallback callback = new HTMLCallback(linewrap);
//			try {
//				parse.parse(r, callback, true);
//			} catch (IOException e) {
//				LOG.error(e.getMessage(), e);
//			}
//			/*
//			 * Get list of start and end positions
//			 */
//			final List<int[]> pos = callback.getPositionList();
//			int from = 0;
//			final int size = pos.size();
//			final Iterator<int[]> iter = pos.iterator();
//			for (int i = 0; i < size; i++) {
//				final int[] startEndPos = iter.next();
//				sb.append(content.substring(from, startEndPos[0]));
//				sb.append(wrapHtmlText(content.substring(startEndPos[0], startEndPos[1]), linewrap));
//				from = startEndPos[1];
//			}
//			sb.append(content.substring(from));
//			return sb.toString();
		}
		final BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(wrapTextLine(line, linewrap)).append(CHAR_BREAK);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return sb.toString();
	}
	
	private static final String wrapTextLine(final String line, final int linewrap) {
		return wrapTextLineRecursive(line, linewrap, null, true);
	}

	private static final String wrapTextLineRecursive(final String line, final int linewrap, final String quoteArg,
			final boolean lookUpQuote) {
		final int length = line.length();
		if (length <= linewrap) {
			return line;
		}
		final String quote = lookUpQuote ? getQuotePrefix(line) : quoteArg;
		final int startPos = quote == null ? 0 : quote.length();
		final char[] chars = line.toCharArray();
		final char c = chars[linewrap];
		final StringBuilder sb = new StringBuilder(length + 5);
		final StringBuilder sub = new StringBuilder();
		if (Character.isWhitespace(c) && c != CHAR_BREAK && c != CHAR_CONTROL) {
			/*
			 * Find last non-whitespace character before
			 */
			int i = linewrap - 1;
			int[] sep = null;
			while (i >= startPos) {
				if (!Character.isWhitespace(chars[i])) {
					if ((sep = isLineBreakInsideHref(line, i)) != null) {
						i = sep[0] - 1;
						continue;
					}
					sb.setLength(0);
					sub.setLength(0);
					return sb.append(line.substring(0, i + 1)).append(CHAR_BREAK).append(
							wrapTextLineRecursive(quote == null ? line.substring(i + 1) : sub.append(quote).append(
									line.substring(i + 1)).toString(), linewrap, quote, false)).toString();
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
				if (Character.isWhitespace(chars[i])) {
					if ((sep = isLineBreakInsideHref(line, i)) != null) {
						i = sep[0] - 1;
						continue;
					}
					sb.setLength(0);
					sub.setLength(0);
					return sb.append(line.substring(0, i)).append(CHAR_BREAK).append(
							wrapTextLineRecursive(quote == null ? line.substring(i + 1) : sub.append(quote).append(
									line.substring(i + 1)).toString(), linewrap, quote, false)).toString();
				}
				i--;
			}
		}
		final int[] sep = isLineBreakInsideHref(line, linewrap);
		if (sep == null) {
			return new StringBuilder(line.length() + 1).append(line.substring(0, linewrap)).append(CHAR_BREAK).append(
					wrapTextLineRecursive(quote == null ? line.substring(linewrap) : new StringBuilder().append(quote)
							.append(line.substring(linewrap)).toString(), linewrap, quote, false)).toString();
		} else if (sep[1] == length) {
			if (sep[0] == startPos) {
				return line;
			}
			return new StringBuilder(line.length() + 1).append(line.substring(0, sep[0])).append(CHAR_BREAK).append(
					wrapTextLineRecursive(quote == null ? line.substring(sep[0]) : new StringBuilder().append(quote)
							.append(line.substring(sep[0])).toString(), linewrap, quote, false)).toString();
		}
		return new StringBuilder(line.length() + 1).append(line.substring(0, sep[1])).append(CHAR_BREAK).append(
				wrapTextLineRecursive(quote == null ? line.substring(sep[1]) : new StringBuilder().append(quote)
						.append(line.substring(sep[1])).toString(), linewrap, quote, false)).toString();
	}

	private static final Pattern PATTERN_QP = Pattern.compile("((?:\\s?>)+)(\\s?)(.*)");

	private static final String getQuotePrefix(final String line) {
		final Matcher m = PATTERN_QP.matcher(line);
		return m.matches() ? new StringBuilder(m.group(1)).append(m.group(2)).toString() : null;
	}

	private static final int[] isLineBreakInsideHref(final String line, final int linewrap) {
		final Matcher m = MailTools.PATTERN_HREF.matcher(line);
		while (m.find()) {
			if (m.start() <= linewrap && m.end() > linewrap) {
				return new int[] { m.start(), m.end() };
			}
		}
		/*
		 * Not inside a href declaration
		 */
		return null;
	}

	private static String wrapHtmlText(final String line, final int linewrap) {
		final int length = line.length();
		if (length <= linewrap) {
			return line;
		}
		final char[] chars = line.toCharArray();
		final char c = chars[linewrap];
		if (Character.isWhitespace(c)) {
			/*
			 * Find last non-whitespace character before
			 */
			for (int i = linewrap - 1; i >= 0; i--) {
				if (!Character.isWhitespace(chars[i])) {
					return new StringBuilder(length + 5).append(line.substring(0, i + 1)).append(HTML_BREAK).append(
							wrapHtmlText(line.substring(i + 2), linewrap)).toString();
				}
			}
		} else {
			/*
			 * Find last whitespace before
			 */
			for (int i = linewrap - 1; i >= 0; i--) {
				if (Character.isWhitespace(chars[i])) {
					return new StringBuilder(length + 5).append(line.substring(0, i)).append(HTML_BREAK).append(
							wrapHtmlText(line.substring(i + 1), linewrap)).toString();
				}
			}
		}
		return new StringBuilder(line.length() + 1).append(line.substring(0, linewrap)).append(HTML_BREAK).append(
				wrapHtmlText(line.substring(linewrap), linewrap)).toString();
	}
	
	private static final String STR_START_BLOCKQUOTE = "<blockquote";

	/**
	 * Performs all the formatting for both text and html content for a proper
	 * display according to user's configuration. The original content keeps
	 * unaffected.
	 */
	public static final String formatContentForDisplay(final String s, final boolean isHtml,
			final SessionObject session, final String msgUID) {
		final UserSettingMail usm = session.getUserConfiguration().getUserSettingMail();
		String retval = s;
		if (isHtml) {
			/*
			 * Insert color quotes in html and filter inline images
			 */
			if (usm.isUseColorQuote()) {
				if (retval.toLowerCase(Locale.ENGLISH).indexOf(STR_START_BLOCKQUOTE) > -1) {
					retval = replaceHTMLBlockQuotesForDisplay(retval);
				} else {
					retval = replaceHTMLSimpleQuotesForDisplay(retval);
				}
			}
			if (usm.isDisplayHtmlInlineContent()) {
				retval = filterInlineImages(retval, session, msgUID);
			}
		} else {
			/*
			 * Replace special characters and insert color quotes
			 */
			retval = MailTools.htmlFormat(retval);
			if (usm.isUseColorQuote()) {
				retval = replaceHTMLSimpleQuotesForDisplay(retval);
			}
			retval = MailTools.formatHrefLinks(retval);
		}
		return retval;
	}

	private static final String[] COLORS;
	
	static {
		String[] tmp = null;
		try {
			tmp = IMAPProperties.getQuoteLineColors();
		} catch (OXException e) {
			tmp = new String[] { "#454545" };
		}
		COLORS = tmp;
	}

	private static final String DEFAULT_COLOR = "#0026ff";

	private static final String BLOCKQUOTE_START_TEMPLATE = "<blockquote type=\"cite\" style=\"margin-left: 0px; padding-left: 10px; color:%s; border-left: solid 1px %s;\">";
	
	private static final String STR_BLOCKQUOTE = "blockquote";

	/**
	 * Replaces all occuring "&lt;blockquote&gt;" tags in given HTML content
	 * with colored "&lt;blockquote&gt;" tags according to configured quote
	 * colors in file "imap.properties"
	 */
	private static final String replaceHTMLBlockQuotesForDisplay(final String htmlText) {
		final StringBuilder sb = new StringBuilder(htmlText.length() + INT_100);
		int offset = 0;
		int pos = -1;
		int quotelevel = 0;
		/*
		 * Find blockquote tags and fill colors
		 */
		while ((pos = htmlText.indexOf(STR_BLOCKQUOTE, offset)) != -1) {
			final int end = htmlText.indexOf('>', pos + 1);
			if (htmlText.charAt(pos - 1) == '<') {
				quotelevel++;
				sb.append(htmlText.subSequence(offset, pos - 1));
				final String color = COLORS != null && COLORS.length > 0 ? (quotelevel >= COLORS.length ? COLORS[COLORS.length - 1]
						: COLORS[quotelevel])
						: DEFAULT_COLOR;
				sb.append(String.format(BLOCKQUOTE_START_TEMPLATE, color, color));
			} else if (htmlText.charAt(pos - 1) == '/') {
				quotelevel--;
				sb.append(htmlText.subSequence(offset, pos - 2));
				sb.append(htmlText.substring(pos - 2, end + 1));
			} else {
				LOG.error("Invalid blockquote tag");
			}
			offset = end + 1;
		}
		sb.append(htmlText.substring(offset));
		return sb.toString();
	}

	private static final String BLOCKQUOTE_END = "</blockquote>\n";
	
	private static final String STR_HTML_QUOTE = "&gt;";
	
	private static final String STR_SPLIT_BR = "<br/?>";

	/**
	 * Turns all simple quotes "&amp;gt; " to colored "&lt;blockquote&gt;" tags
	 * according to configured quote colors in file "imap.properties"
	 */
	private static final String replaceHTMLSimpleQuotesForDisplay(final String htmlText) {
		final StringBuilder sb = new StringBuilder();
		final String[] lines = htmlText.split(STR_SPLIT_BR);
		int levelBefore = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int currentLevel = 0;
			int offset = 0;
			if (line.startsWith(STR_HTML_QUOTE)) {
				currentLevel++;
				offset = 4;
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
					offset = offset < line.length() && Character.isWhitespace(line.charAt(offset)) ? offset + 1 : offset;
				} catch (StringIndexOutOfBoundsException e) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(e.getMessage(), e);
					}
				}
				line = line.substring(offset);
			}
			if (levelBefore < currentLevel) {
				for (; levelBefore < currentLevel; levelBefore++) {
					final String color = COLORS != null && COLORS.length > 0 ? (levelBefore >= COLORS.length ? COLORS[COLORS.length - 1]
							: COLORS[levelBefore])
							: DEFAULT_COLOR;
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
	
	private static final Html2TextConverter CONVERTER = new Html2TextConverter();
	
	private static final Pattern PATTERN_BLOCKQUOTE = Pattern.compile("(?:(<blockquote.*?>)|(</blockquote>))",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	/**
	 * Converts given HTML content into plain text, but keeps
	 * <code>&lt;blockquote&gt;</code> tags if any present. <b>NOTE:</b>
	 * returned content again is html content
	 * 
	 * @return partially converted plain text version of given html content as
	 *         html content
	 */
	public final static String convertAndKeepQuotes(final String htmlContent) throws IOException {
		final StringBuilder sb = new StringBuilder(htmlContent.length() + INT_100);
		final Matcher m = PATTERN_BLOCKQUOTE.matcher(htmlContent);
		int lastMatch = 0;
		while (m.find()) {
			sb.append(MailTools.htmlFormat(CONVERTER.convert(htmlContent.substring(lastMatch, m.start()))));
			sb.append(m.group());
			lastMatch = m.end();
		}
		sb.append(MailTools.htmlFormat(CONVERTER.convert(htmlContent.substring(lastMatch))));
		return sb.toString();
	}

	/*
	 * *************************************************************
	 */

	public static final String getFormattedText(final String text, final boolean isHtmlContent,
			final SessionObject session, final String msgUID) {
		final UserSettingMail usm = session.getUserConfiguration().getUserSettingMail();
		final StringBuilder formattedText = new StringBuilder();
		if (isHtmlContent) {
			String htmlContent = text;
			if (usm.isUseColorQuote()) {
				htmlContent = new StringBuilder(doHtmlColorQuoting(htmlContent)).toString();
			}
			if (usm.isDisplayHtmlInlineContent()) {
				formattedText.append(filterInlineImages(htmlContent, session, msgUID));
			} else {
				formattedText.append(htmlContent);
				formattedText.append(CHAR_BREAK);
			}
			return formattedText.toString();
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(text));
			String line = "";
			int quotelevel_before = 0;
			final StringBuffer sb = new StringBuffer(INT_100);
			final StringBuilder colorBuilder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (usm.isUseColorQuote()) {
					int quotelevel = 0;
					final Matcher quoteMatcher = PLAIN_TEXT_QUOTE_PATTERN.matcher(line);
					sb.setLength(0);
					while (quoteMatcher.find()) {
						quotelevel++;
						quoteMatcher.appendReplacement(sb, " ");
					}
					quoteMatcher.appendTail(sb);
					line = MailTools.htmlFormat(sb.toString());
					if (quotelevel > quotelevel_before) {
						for (int u = 0; u < (quotelevel - quotelevel_before); u++) {
							colorBuilder.setLength(0);
							final String styleStart = " style=\"margin-left: 0px; padding-left: 10px; color:";
							final String borderColor = "; border-left: solid 1px ";
							final String styleEnd = ";\"";
							if ((u + quotelevel_before) >= IMAPProperties.getQuoteLineColors().length) {
								colorBuilder
										.append(styleStart)
										.append(
												IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
										.append(borderColor)
										.append(
												IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
										.append(styleEnd);
							} else if ((u + quotelevel_before) < IMAPProperties.getQuoteLineColors().length) {
								colorBuilder.append(styleStart).append(
										IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(borderColor)
										.append(IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(
												styleEnd);
							}
							formattedText.append("<blockquote type=cite").append(colorBuilder).append('>');
						}
					} else if (quotelevel < quotelevel_before) {
						for (int u = 0; u < (quotelevel_before - quotelevel); u++) {
							formattedText.append("</blockquote>");
						}
					}
					formattedText.append(MailTools.formatHrefLinks(line)).append(HTML_BREAK);
					quotelevel_before = quotelevel;
				} else {
					line = MailTools.formatHrefLinks(MailTools.htmlFormat(line));
					formattedText.append(line).append(HTML_BREAK);
				}
			}
		} catch (Exception ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return text;
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
		return formattedText.toString();
	}
	
	private static final String STR_AJAX_MAIL = "\"/ajax/mail?";

	/**
	 * Replaces all occurences of <code>&lt;img cid:&quot;[cid]&quot;...</code>
	 * with links to the image content
	 */
	private static final String filterInlineImages(final String content, final SessionObject session,
			final String msgUID) {
		String reval = content;
		try {
			final Matcher imgMatcher = IMG_PATTERN.matcher(reval);
			final StringBuffer sb = new StringBuffer(reval.length());
			while (imgMatcher.find()) {
				final String foundImg = imgMatcher.group(1);
				final Matcher cidMatcher = CID_PATTERN.matcher(foundImg);
				final StringBuffer cidBuffer = new StringBuffer(foundImg.length());
				while (cidMatcher.find()) {
					final String cid = (cidMatcher.group(1) == null ? cidMatcher.group(2) : cidMatcher.group(1));
					final StringBuilder linkBuilder = new StringBuilder().append(STR_AJAX_MAIL).append(
							Mail.PARAMETER_SESSION).append('=').append(session.getSecret()).append('&').append(
							Mail.PARAMETER_ACTION).append('=').append(Mail.ACTION_MATTACH).append('&').append(
							Mail.PARAMETER_ID).append('=').append(msgUID).append('&').append(Mail.PARAMETER_MAILCID)
							.append('=').append(cid).append('"');
					cidMatcher.appendReplacement(cidBuffer, Matcher.quoteReplacement(linkBuilder.toString()));
				}
				cidMatcher.appendTail(cidBuffer);
				imgMatcher.appendReplacement(sb, Matcher.quoteReplacement(cidBuffer.toString()));
			}
			imgMatcher.appendTail(sb);
			reval = sb.toString();
		} catch (Exception e) {
			LOG.warn("Unable to filter cid Images: " + e.getMessage());
		}
		return reval;
	}

	/**
	 * Replaces all occuring "<code> &amp;gt;</code>" patterns with a
	 * surrounding <code>&lt;blockquote&gt;</code> tag
	 */
	private static final String doHtmlColorQuoting(final String htmlContent) {
		final StringBuffer htmlBuffer = new StringBuffer(htmlContent.length());
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(htmlContent));
			int quotelevel_before = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				int quotelevel = 0;
				final Matcher quoteMatcher = HTML_QUOTE_PATTERN.matcher(line);
				final StringBuffer quoteBuffer = new StringBuffer(line.length());
				while (quoteMatcher.find()) {
					quotelevel++;
					quoteMatcher.appendReplacement(quoteBuffer, " ");
				}
				quoteMatcher.appendTail(quoteBuffer);
				line = quoteBuffer.toString();
				if (quotelevel > quotelevel_before) {
					for (int u = 0; u < (quotelevel - quotelevel_before); u++) {
						final StringBuilder colorBuilder = new StringBuilder();
						final String styleStart = " style=\"color:";
						final String borderColor = "; border-color:";
						final String styleEnd = ";\"";
						if ((u + quotelevel_before) >= IMAPProperties.getQuoteLineColors().length) {
							colorBuilder
									.append(styleStart)
									.append(
											IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
									.append(borderColor)
									.append(
											IMAPProperties.getQuoteLineColors()[IMAPProperties.getQuoteLineColors().length - 1])
									.append(styleEnd);
						} else if ((u + quotelevel_before) < IMAPProperties.getQuoteLineColors().length) {
							colorBuilder.append(styleStart).append(
									IMAPProperties.getQuoteLineColors()[u + quotelevel_before]).append(borderColor)
									.append(IMAPProperties.getQuoteLineColors()[u + quotelevel_before])
									.append(styleEnd);
						}
						htmlBuffer.append("<blockquote type=cite").append(colorBuilder).append('>');
					}
				} else if (quotelevel < quotelevel_before) {
					for (int u = 0; u < (quotelevel_before - quotelevel); u++) {
						htmlBuffer.append("</blockquote>");
					}
				}
				htmlBuffer.append(line).append(CHAR_BREAK);
				quotelevel_before = quotelevel;
			}
			return htmlBuffer.toString();
		} catch (Exception ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return htmlContent;
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
	}

	private static final Charset DEFAULT_CHARSET = Charset.forName(ServerConfig.getProperty(Property.DefaultEncoding));
	
	private static final String STR_CHARSET = "charset";

	/**
	 * Delivers the <code>java.lang.String</code> object created from given
	 * <code>javax.mail.Part</code> instance by calling either its
	 * <code>getContent()</code> or <code>getInputStream()</code> (in
	 * sub-routine) method. This means the //SEEN flag is going to be set
	 * implicitely.
	 */
	public static final String getStringObject(final Part p) throws MessagingException, OXException {
		final StringBuilder sb = new StringBuilder(INT_1000);
		BufferedReader br = null;
		final String charsetStr = new ContentType(p.getContentType()).getParameter(STR_CHARSET);
		Charset charset = null;
		try {
			charset = charsetStr == null ? DEFAULT_CHARSET : Charset.forName(charsetStr);
		} catch (UnsupportedCharsetException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			}
			charset = DEFAULT_CHARSET;
		} catch (IllegalCharsetNameException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			}
			charset = DEFAULT_CHARSET;
		}
		try {
			br = new BufferedReader(new InputStreamReader(p.getInputStream(), charset));
			String contentLine = null;
			while ((contentLine = br.readLine()) != null) {
				sb.append(contentLine).append(CHAR_BREAK);
			}
		} catch (Throwable t) {
			try {
				br = getBufferedReaderFromPart(p, charset);
				String contentLine = null;
				while ((contentLine = br.readLine()) != null) {
					sb.append(contentLine).append(CHAR_BREAK);
				}
			} catch (IOException innerExc) {
				/*
				 * Common way does not work, try to read from raw stream
				 */
				LOG.warn(innerExc, innerExc);
			}
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
		return sb.toString();
	}

	private static final BufferedReader getBufferedReaderFromPart(final Part p, final Charset charset)
			throws MessagingException {
		try {
			InputStream partInputStream = p.getInputStream();
			if (!(partInputStream instanceof BufferedInputStream)) {
				partInputStream = new BufferedInputStream(partInputStream);
			}
			return new BufferedReader(new InputStreamReader(partInputStream, charset));
		} catch (Exception ee) {
			/*
			 * Common way does not work, try to read from raw stream
			 */
			return getBufferedReaderFromRawPart(p, charset);
		}
	}

	private static final BufferedReader getBufferedReaderFromRawPart(final Part p, final Charset charset)
			throws MessagingException {
		InputStream in = null;
		if (p instanceof MimeBodyPart) {
			final MimeBodyPart mpb = (MimeBodyPart) p;
			in = mpb.getRawInputStream();
		} else if (p instanceof MimeMessage) {
			final MimeMessage mm = (MimeMessage) p;
			in = mm.getRawInputStream();
		}
		if (in == null) {
			/*
			 * Neither a MimeBodyPart nor a MimeMessage
			 */
			return null;
		}
		if (!(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream(in);
		}
		return new BufferedReader(new InputStreamReader(in, charset));
	}

	public static final String addr2String(final InternetAddress[] addrs) {
		if (addrs == null || addrs.length == 0) {
			return STR_EMPTY;
		}
		final StringBuilder sb = new StringBuilder(200);
		sb.append(addrs[0].toUnicodeString());
		for (int i = 1; i < addrs.length; i++) {
			sb.append(", ");
			sb.append(addrs[i].toUnicodeString());
		}
		return sb.toString();
	}

	public static final String getMessageUniqueIdentifier(final Message msg) throws MessagingException {
		if (msg.getFolder() == null) {
			return null;
		}
		final IMAPFolder imapFolder = (IMAPFolder) msg.getFolder();
		boolean closeFolder = false;
		try {
			if (!imapFolder.isOpen()) {
				imapFolder.open(Folder.READ_ONLY);
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(true);
				closeFolder = true;
			}
			return new StringBuilder(imapFolder.getFullName()).append(Mail.SEPERATOR).append(imapFolder.getUID(msg))
					.toString();
		} finally {
			if (closeFolder) {
				imapFolder.close(false);
				MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
			}
		}
	}

	public static final String getMessageUniqueIdentifier(final IMAPFolder folder, final long msgUID) {
		return new StringBuilder(folder.getFullName()).append(Mail.SEPERATOR).append(msgUID).toString();
	}

	public static final int[] parseIdentifier(final String id) {
		final String[] sa = id.split("\\.");
		final int[] retval = new int[sa.length];
		for (int i = 0; i < sa.length; i++) {
			retval[i] = Integer.parseInt(sa[i]);
		}
		return retval;
	}

	public final static String getFileName(final Part p, final String identifier) throws MessagingException {
		String filename = p.getFileName();
		if (filename == null || isEmptyString(filename)) {
			filename = new StringBuilder(20).append("Part_").append(identifier).toString();
		} else {
			try {
				filename = MimeUtility.decodeText(filename.replaceAll("\\?==\\?", "?= =?"));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return filename;
	}

	public final static String getFileName(final Part p, final int[] indices) throws MessagingException {
		String filename = p.getFileName();
		if (filename == null || isEmptyString(filename)) {
			filename = new StringBuilder(20).append("Part_").append(getIdentifier(indices)).toString();
		} else {
			try {
				filename = MimeUtility.decodeText(filename.replaceAll("\\?==\\?", "?= =?"));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return filename;
	}

	private final static boolean isEmptyString(final String str) {
		final char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isWhitespace(chars[i])) {
				return false;
			}
		}
		return true;
	}

	public static final String getIdentifier(final String prefix, final int partCount) {
		if (prefix == null) {
			return String.valueOf(partCount);
		}
		return new StringBuilder(prefix).append('.').append(partCount).toString();
	}

	public static final String getIdentifier(final int[] indices) {
		final StringBuilder sb = new StringBuilder((2 * indices.length) - 1);
		sb.append(indices[0]);
		for (int i = 1; i < indices.length; i++) {
			sb.append('.').append(indices[i]);
		}
		return sb.toString();
	}

}
