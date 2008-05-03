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
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * {@link HTMLWhitelist}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLWhitelist {

	private static final HTMLEditorKit.Parser PARSER = new ParserDelegator();

	private static final String CRLF = "\r\n";

	private final int capacity;

	/**
	 * Initializes a new {@link HTMLWhitelist}
	 * 
	 * @param capacity
	 *            The initial capacity
	 */
	public HTMLWhitelist(final int capacity) {
		super();
		this.capacity = capacity;
	}

	public static void main(final String[] args) {
		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n"
				+ "<html>\n"
				+ " <head>\n"
				+ "  <title>Index of /~thorben</title>\n"
				+ " </head>\n"
				+ " <body>\n"
				+ "<h1>Index of /~thorben</h1>\n"
				+ "<table><tr><th><img src=\"/icons/blank.gif\" alt=\"[ICO]\"></th><th><a href=\"?C=N;O=D\">Name</a></th><th><a href=\"?C=M;O=A\">Last modified</a></th><th><a href=\"?C=S;O=A\">Size</a></th><th><a href=\"?C=D;O=A\">Description</a></th></tr><tr><th colspan=\"5\"><hr></th></tr>\n"
				+ "\n"
				+ "<tr><td valign=\"top\"><img src=\"/icons/back.gif\" alt=\"[DIR]\"></td><td><a href=\"/\">Parent Directory</a></td><td>&nbsp;</td><td align=\"right\">  - </td></tr>\n"
				+ "<tr><td valign=\"top\"><img src=\"/icons/text.gif\" alt=\"[TXT]\"></td><td><a href=\"irc-links.html\">irc-links.html</a></td><td align=\"right\">03-Apr-2008 23:21  </td><td align=\"right\"> 99 </td></tr>\n"
				+ "<tr><td valign=\"top\"><img src=\"/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"open-xchange-gui/\">open-xchange-gui/</a></td><td align=\"right\">14-Apr-2008 17:07  </td><td align=\"right\">  - </td></tr>\n"
				+ "<tr><th colspan=\"5\"><hr></th></tr>\n" + "</table>\n" + "<img alt=\"fist\" src=\"faust\" />"
				+ "<address>Apache/2.2.4 (Ubuntu) mod_jk/1.2.23 Server at localhost Port 80</address>\n" + "\n"
				+ "</body></html>";
		html = HTMLProcessing.validate(html, "UTF-8");
		html = html.replaceAll(" />", ">");

		final HTMLWhitelist obj = new HTMLWhitelist(8192);
		final Map<HTML.Tag, Set<HTML.Attribute>> m = new HashMap<HTML.Tag, Set<HTML.Attribute>>();
		m.put(HTML.Tag.IMG, new HashSet<HTML.Attribute>(Arrays.asList(new HTML.Attribute[] { HTML.Attribute.ALT,
				HTML.Attribute.SRC })));
		try {
			System.out.println(obj.applyWhitelist(html, m));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static final Pattern PATTERN_DOCTYPE = Pattern.compile("<!DOCTYPE[^>]+>", Pattern.CASE_INSENSITIVE);

	/**
	 * Applies specified white list to given HTML content
	 * 
	 * @param html
	 *            The HTML content
	 * @param whitelist
	 *            The white list
	 * @return The stripped HTML content according to specified white list
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public String applyWhitelist(final String html, final Map<HTML.Tag, Set<HTML.Attribute>> whitelist)
			throws IOException {
		/*
		 * Determine doctype
		 */
		final Matcher m = PATTERN_DOCTYPE.matcher(html);
		final String doctype;
		if (m.find()) {
			doctype = m.group();
		} else {
			doctype = null;
		}
		final HTMLWhitelistParserCallback parserCallback = new HTMLWhitelistParserCallback(capacity, whitelist);
		PARSER.parse(new StringReader(html), parserCallback, true);
		return parserCallback.getHTML(doctype);
	}

	private static final class HTMLWhitelistParserCallback extends ParserCallback {

		private static final Set<HTML.Tag> COMMON_TAGS = new HashSet<HTML.Tag>(Arrays.asList(new HTML.Tag[] {
				HTML.Tag.HTML, HTML.Tag.HEAD, HTML.Tag.BODY, HTML.Tag.TABLE, HTML.Tag.TR, HTML.Tag.TH, HTML.Tag.TD }));

		private final StringBuilder htmlBuilder;

		private final Map<HTML.Tag, Set<HTML.Attribute>> whitelist;

		private int level;

		public HTMLWhitelistParserCallback(final int capacity, final Map<HTML.Tag, Set<HTML.Attribute>> whitelist) {
			super();
			this.htmlBuilder = new StringBuilder(capacity);
			this.whitelist = whitelist;
		}

		public String getHTML(final String doctype) {
			if (null != doctype) {
				htmlBuilder.insert(0, doctype);
			}
			return htmlBuilder.toString();
		}

		public void reset() {
			htmlBuilder.setLength(0);
		}

		@Override
		public void handleStartTag(final HTML.Tag tag, final MutableAttributeSet a, final int pos) {
			if (level > 0) {
				level++;
				return;
			}
			final Set<HTML.Attribute> attribs = whitelist.get(tag);
			if (attribs != null) {
				htmlBuilder.append(CRLF).append("<").append(tag.toString());
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					if (attribs.contains(attributeName)) {
						htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
								a.getAttribute(attributeName).toString()).append('"');
					}
				}
				htmlBuilder.append('>');
			} else if (COMMON_TAGS.contains(tag)) {
				htmlBuilder.append(CRLF).append("<").append(tag.toString());
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
							a.getAttribute(attributeName).toString()).append('"');
				}
				htmlBuilder.append('>');
			} else {
				level++;
			}
		}

		@Override
		public void handleEndTag(final HTML.Tag tag, final int pos) {
			if (level == 0) {
				htmlBuilder.append("</").append(tag.toString()).append('>');
			} else {
				level--;
			}
		}

		@Override
		public void handleSimpleTag(final HTML.Tag tag, final MutableAttributeSet a, final int pos) {
			if (level > 0) {
				return;
			}
			final Set<HTML.Attribute> attribs = whitelist.get(tag);
			if (attribs != null) {
				htmlBuilder.append(CRLF).append("<").append(tag.toString());
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					if (attribs.contains(attributeName)) {
						htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
								a.getAttribute(attributeName).toString()).append('"');
					}
				}
				htmlBuilder.append("/>");
			}
		}

		@Override
		public void handleText(final char[] data, final int pos) {
			if (level == 0) {
				htmlBuilder.append(data);
			}
		}

		@Override
		public void handleComment(final char[] data, final int pos) {
			htmlBuilder.append(data);
		}
	}

}
