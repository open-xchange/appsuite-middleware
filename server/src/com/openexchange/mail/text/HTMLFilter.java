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

import static com.openexchange.mail.text.HTMLProcessing.prettyPrint;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
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
 * {@link HTMLFilter} - Filters a specified HTML content against a given white
 * list or black list of HTML tags and tag's attributes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLFilter {

	private static final class HTMLTag extends HTML.Tag {

		/**
		 * Initializes a new {@link HTMLTag}
		 * 
		 * @param name
		 *            The name
		 */
		public HTMLTag(final String name) {
			super(name);
		}

	}

	private static final Set<HTML.Tag> DEFAULT_WHITELIST_TAGS = new HashSet<HTML.Tag>(asList(new HTML.Tag[] {
			HTML.Tag.A, new HTMLTag("abbr"), new HTMLTag("acronym"), HTML.Tag.ADDRESS, HTML.Tag.AREA, HTML.Tag.B,
			HTML.Tag.BIG, HTML.Tag.BLOCKQUOTE, HTML.Tag.BODY, HTML.Tag.BR, new HTMLTag("button"), HTML.Tag.CAPTION,
			HTML.Tag.CENTER, HTML.Tag.CITE, HTML.Tag.CODE, new HTMLTag("col"), new HTMLTag("colgroup"), HTML.Tag.DD,
			new HTMLTag("del"), HTML.Tag.DFN, HTML.Tag.DIR, HTML.Tag.DIV, HTML.Tag.DL, HTML.Tag.DT, HTML.Tag.EM,
			new HTMLTag("fieldset"), HTML.Tag.FONT, HTML.Tag.FORM, HTML.Tag.H1, HTML.Tag.H2, HTML.Tag.H3, HTML.Tag.H4,
			HTML.Tag.H5, HTML.Tag.H6, HTML.Tag.HEAD, HTML.Tag.HR, HTML.Tag.HTML, HTML.Tag.I, HTML.Tag.IMG,
			HTML.Tag.INPUT, new HTMLTag("ins"), HTML.Tag.KBD, new HTMLTag("label"), new HTMLTag("legend"), HTML.Tag.LI,
			HTML.Tag.MAP, HTML.Tag.MENU, HTML.Tag.OL, new HTMLTag("optgroup"), HTML.Tag.OPTION, HTML.Tag.P,
			HTML.Tag.PRE, new HTMLTag("q"), HTML.Tag.S, HTML.Tag.SAMP, HTML.Tag.SELECT, HTML.Tag.SMALL, HTML.Tag.SPAN,
			HTML.Tag.STRIKE, HTML.Tag.STRONG, HTML.Tag.SUB, HTML.Tag.TABLE, new HTMLTag("tbody"), HTML.Tag.TD,
			HTML.Tag.TEXTAREA, new HTMLTag("tfoot"), HTML.Tag.TH, new HTMLTag("thead"), HTML.Tag.TR, HTML.Tag.TT,
			HTML.Tag.U, HTML.Tag.UL, HTML.Tag.VAR }));

	private static final Set<HTML.Attribute> DEFAULT_WHITELIST_ATTRIBUTES = new HashSet<HTML.Attribute>(
			asList(new HTML.Attribute[] { HTML.Attribute.ACTION, HTML.Attribute.ALIGN, HTML.Attribute.ALT,
					HTML.Attribute.BORDER, HTML.Attribute.CELLPADDING, HTML.Attribute.CELLSPACING,
					HTML.Attribute.CHECKED, HTML.Attribute.CLASS, HTML.Attribute.CLEAR, HTML.Attribute.COLS,
					HTML.Attribute.COLSPAN, HTML.Attribute.COLOR, HTML.Attribute.COMPACT, HTML.Attribute.COORDS,
					HTML.Attribute.DIR, HTML.Attribute.ENCTYPE, HTML.Attribute.HEIGHT, HTML.Attribute.HREF,
					HTML.Attribute.HSPACE, HTML.Attribute.ID, HTML.Attribute.ISMAP, HTML.Attribute.LANG,
					HTML.Attribute.MAXLENGTH, HTML.Attribute.METHOD, HTML.Attribute.MULTIPLE, HTML.Attribute.NAME,
					HTML.Attribute.NOHREF, HTML.Attribute.NOSHADE, HTML.Attribute.NOWRAP, HTML.Attribute.PROMPT,
					HTML.Attribute.REL, HTML.Attribute.REV, HTML.Attribute.ROWS, HTML.Attribute.ROWSPAN,
					HTML.Attribute.SELECTED, HTML.Attribute.SHAPE, HTML.Attribute.SIZE, HTML.Attribute.SRC,
					HTML.Attribute.START, HTML.Attribute.TARGET, HTML.Attribute.TITLE, HTML.Attribute.TYPE,
					HTML.Attribute.USEMAP, HTML.Attribute.VALIGN, HTML.Attribute.VALUE, HTML.Attribute.VSPACE,
					HTML.Attribute.WIDTH }));

	private static final HTMLEditorKit.Parser PARSER = new ParserDelegator();

	private final int capacity;

	/**
	 * Initializes a new {@link HTMLFilter}
	 * 
	 * @param capacity
	 *            The initial capacity
	 */
	public HTMLFilter(final int capacity) {
		super();
		this.capacity = capacity;
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
		final HTMLFilterParserCallback parserCallback = new HTMLFilterParserCallback(true, capacity, whitelist);
		PARSER.parse(new StringReader(html), parserCallback, true);
		return parserCallback.getHTML(doctype);
	}

	/**
	 * Applies specified black list to given HTML content
	 * 
	 * @param html
	 *            The HTML content
	 * @param blacklist
	 *            The black list
	 * @return The stripped HTML content according to specified black list
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public String applyBlacklist(final String html, final Map<HTML.Tag, Set<HTML.Attribute>> blacklist)
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
		final HTMLFilterParserCallback parserCallback = new HTMLFilterParserCallback(false, capacity, blacklist);
		PARSER.parse(new StringReader(html), parserCallback, true);
		return parserCallback.getHTML(doctype);
	}

	private static final class HTMLFilterParserCallback extends ParserCallback {

		private final StringBuilder htmlBuilder;

		private final boolean isWhitelist;

		private final Map<HTML.Tag, Set<HTML.Attribute>> filter;

		private int level;

		public HTMLFilterParserCallback(final boolean isWhitelist, final int capacity,
				final Map<HTML.Tag, Set<HTML.Attribute>> filter) {
			super();
			this.isWhitelist = isWhitelist;
			this.htmlBuilder = new StringBuilder(capacity);
			this.filter = filter;
		}

		public String getHTML(final String doctype) {
			if (null != doctype) {
				htmlBuilder.insert(0, doctype);
			}
			return prettyPrint(htmlBuilder.toString(), "UTF-8");
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
			if (isWhitelist) {
				if (filter.containsKey(tag)) {
					addStartTag(tag, a, false, filter.get(tag));
				} else if (DEFAULT_WHITELIST_TAGS.contains(tag)) {
					addDefaultStartTag(tag, a, false);
				} else {
					level++;
				}
			} else {
				if (filter.containsKey(tag)) {
					level++;
				} else {
					addCompleteStartTag(tag, a, false);
				}
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
			if (isWhitelist) {
				if (filter.containsKey(tag)) {
					addStartTag(tag, a, true, filter.get(tag));
				} else if (DEFAULT_WHITELIST_TAGS.contains(tag)) {
					addDefaultStartTag(tag, a, true);
				}
			} else {
				if (!filter.containsKey(tag)) {
					addCompleteStartTag(tag, a, true);
				}
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
			System.out.println("Comment: " + new String(data));
			htmlBuilder.append(data);
		}

		/**
		 * Adds complete tag to HTML result with all attributes.
		 * 
		 * @param tag
		 *            The tag to add
		 * @param a
		 *            The tag's attribute set
		 * @param simple
		 *            <code>true</code> to write a simple tag; otherwise
		 *            <code>false</code>
		 */
		private void addCompleteStartTag(final HTML.Tag tag, final MutableAttributeSet a, final boolean simple) {
			htmlBuilder.append('<').append(tag.toString());
			for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
				final Object attributeName = e.nextElement();
				htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
						a.getAttribute(attributeName).toString()).append('"');
			}
			if (simple) {
				htmlBuilder.append('/');
			}
			htmlBuilder.append('>');
		}

		/**
		 * Adds tag occurring in default white list to HTML result only with
		 * attributes contained in default allowed attributes.
		 * 
		 * @param tag
		 *            The tag to add
		 * @param a
		 *            The tag's attribute set
		 * @param simple
		 *            <code>true</code> to write a simple tag; otherwise
		 *            <code>false</code>
		 */
		private void addDefaultStartTag(final HTML.Tag tag, final MutableAttributeSet a, final boolean simple) {
			htmlBuilder.append('<').append(tag.toString());
			for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
				final Object attributeName = e.nextElement();
				if (DEFAULT_WHITELIST_ATTRIBUTES.contains(attributeName)) {
					htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
							a.getAttribute(attributeName).toString()).append('"');
				}
			}
			if (simple) {
				htmlBuilder.append('/');
			}
			htmlBuilder.append('>');
		}

		/**
		 * Adds tag occurring in white list to HTML result
		 * 
		 * @param tag
		 *            The tag to add
		 * @param a
		 *            The tag's attribute set
		 * @param simple
		 *            <code>true</code> to write a simple tag; otherwise
		 *            <code>false</code>
		 * @param attribs
		 *            The allowed tag's attributes or <code>null</code> to
		 *            refer to default allowed attributes
		 */
		private void addStartTag(final HTML.Tag tag, final MutableAttributeSet a, final boolean simple,
				final Set<HTML.Attribute> attribs) {
			if (null == attribs) {
				addDefaultStartTag(tag, a, simple);
			} else {
				htmlBuilder.append('<').append(tag.toString());
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					if (attribs.contains(attributeName) || DEFAULT_WHITELIST_ATTRIBUTES.contains(attributeName)) {
						htmlBuilder.append(' ').append(attributeName.toString()).append("=\"").append(
								a.getAttribute(attributeName).toString()).append('"');
					}
				}
				if (simple) {
					htmlBuilder.append('/');
				}
				htmlBuilder.append('>');
			}
		}
	}

}
