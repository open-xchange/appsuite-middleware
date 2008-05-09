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
import java.util.Enumeration;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * {@link HTML2Text} - This class converts HTML content to valid text/plain
 * based on {@link HTMLEditorKit}.
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HTML2Text {

	// private static final EditorKit EDITOR_KIT = new HTMLEditorKit();

	private static final HTMLEditorKit.Parser PARSER = new ParserDelegator();

	private static final int DEFAULT_CAPACITY = 8192;

	// private static final String PROP_IGNORE_CHARSET_DIRECTIVE =
	// "IgnoreCharsetDirective";

	private static final String CRLF = "\r\n";

	private static final String QUOTE = "> ";

	private final HTML2TextParserCallback parserCallback;

	/**
	 * Initializes a new {@link HTML2Text} with default capacity of
	 * <code>8192</code>
	 * 
	 * @param appendHref
	 *            <code>true</code> to append <i>href</i> and <i>src</i>
	 *            attributes' content in brackets; otherwise <code>false</code>.<br>
	 *            Example:
	 *            <code>&lt;a&nbsp;href=\"www.somewhere.com\"&gt;Link&lt;a&gt;</code>
	 *            would be <code>Link&nbsp;[www.somewhere.com]</code>
	 */
	public HTML2Text(final boolean appendHref) {
		this(DEFAULT_CAPACITY, appendHref);
	}

	/**
	 * Initializes a new {@link HTML2Text}
	 * 
	 * @param capacity
	 *            The initial capacity.
	 * @param appendHref
	 *            <code>true</code> to append <i>href</i> and <i>src</i>
	 *            attributes' content in brackets; otherwise <code>false</code>.<br>
	 *            Example:
	 *            <code>&lt;a&nbsp;href=\"www.somewhere.com\"&gt;Link&lt;a&gt;</code>
	 *            would be <code>Link&nbsp;[www.somewhere.com]</code>
	 */
	public HTML2Text(final int capacity, final boolean appendHref) {
		super();
		parserCallback = new HTML2TextParserCallback(capacity, appendHref);
	}

	/**
	 * Converts specified HTML content to plain text
	 * 
	 * @param html
	 *            The HTML content to convert
	 * @return The corresponding text extracted from HTML content
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public String html2text(final String html) throws IOException {
		// final HTMLDocument doc = (HTMLDocument)
		// EDITOR_KIT.createDefaultDocument();
		/*
		 * The Document class does not yet handle charset's properly.
		 */
		// doc.putProperty(PROP_IGNORE_CHARSET_DIRECTIVE, Boolean.TRUE);
		/*
		 * Parse HTML document (true means to ignore character set).
		 * Unfortunately the HTMLEditorKit.Parser cannot deal with XHTML conform
		 * HTML content, thus simple tags such as "img" or "br" must not end
		 * with " />" but with ">"
		 */
		PARSER.parse(new StringReader(html.replaceAll(" />", ">")), parserCallback, true);
		final String text = parserCallback.getText();
		parserCallback.reset();
		return text;
	}

	private static final class HTML2TextParserCallback extends HTMLEditorKit.ParserCallback {

		private final StringBuilder textBuilder;

		private final boolean appendHref;

		private boolean anchorTag;

		private String hrefContent;

		private int quote;

		public HTML2TextParserCallback(final int capacity, final boolean appendHref) {
			super();
			this.textBuilder = new StringBuilder(capacity);
			this.appendHref = appendHref;
		}

		public String getText() {
			return textBuilder.toString();
		}

		public void reset() {
			quote = 0;
			textBuilder.setLength(0);
		}

		@Override
		public void handleStartTag(final HTML.Tag tag, final MutableAttributeSet a, final int pos) {
			if (tag.equals(HTML.Tag.BLOCKQUOTE)) {
				textBuilder.append(CRLF);
				quote++;
				quoteText();
			} else if (tag.equals(HTML.Tag.DIV)) {
				textBuilder.append(CRLF);
				quoteText();
			} else if (appendHref && tag.equals(HTML.Tag.A)) {
				anchorTag = true;
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements() && hrefContent == null;) {
					final Object attributeName = e.nextElement();
					if (javax.swing.text.html.HTML.Attribute.HREF.equals(attributeName)) {
						hrefContent = a.getAttribute(attributeName).toString();
					}
				}
			}
		}

		@Override
		public void handleEndTag(final HTML.Tag tag, final int pos) {
			if (tag.equals(HTML.Tag.BLOCKQUOTE)) {
				textBuilder.append(CRLF);
				quote--;
			} else if (tag.equals(HTML.Tag.P)) {
				textBuilder.append(CRLF);
				quoteText();
			} else if (tag.equals(HTML.Tag.TR)) {
				// Ending table row
				textBuilder.append(CRLF);
				quoteText();
			} else if (tag.equals(HTML.Tag.TD)) {
				// Ending table column
				textBuilder.append('\t');
			} else if (appendHref && tag.equals(HTML.Tag.A)) {
				anchorTag = false;
			} else if (tag.equals(HTML.Tag.H1) || tag.equals(HTML.Tag.H2) || tag.equals(HTML.Tag.H3)
					|| tag.equals(HTML.Tag.H4) || tag.equals(HTML.Tag.H5) || tag.equals(HTML.Tag.H6)
					|| tag.equals(HTML.Tag.ADDRESS) || tag.equals(HTML.Tag.PRE)) {
				textBuilder.append(CRLF);
				quoteText();
				textBuilder.append(CRLF);
				quoteText();
			}
		}

		@Override
		public void handleSimpleTag(final HTML.Tag tag, final MutableAttributeSet a, final int pos) {
			if (tag.equals(HTML.Tag.BR)) {
				textBuilder.append(CRLF);
				quoteText();
			} else if (tag.equals(HTML.Tag.IMG)) {
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					if (javax.swing.text.html.HTML.Attribute.ALT.equals(attributeName)) {
						textBuilder.append(' ').append(a.getAttribute(attributeName).toString()).append(' ');
					} else if (appendHref && javax.swing.text.html.HTML.Attribute.SRC.equals(attributeName)) {
						textBuilder.append(" [").append(a.getAttribute(attributeName).toString()).append("] ");
					}
				}
			}
		}

		@Override
		public void handleText(final char[] data, final int pos) {
			final String text = new String(data);
			/*
			 * Add normal text
			 */
			textBuilder.append(text);
			if (anchorTag && hrefContent != null && !text.equalsIgnoreCase(hrefContent)) {
				textBuilder.append(" [").append(hrefContent).append("] ");
			}
		}

		private void quoteText() {
			/*
			 * Start line with quotes if necessary
			 */
			for (int b = 1; b <= quote; b++) {
				textBuilder.append(QUOTE);
			}
		}
	}

}
