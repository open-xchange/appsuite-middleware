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

package com.openexchange.mail.text.parser.handler;

import static com.openexchange.mail.text.HTMLProcessing.replaceHTMLEntities;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.openexchange.mail.text.parser.HTMLHandler;

/**
 * {@link HTML2TextHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTML2TextHandler implements HTMLHandler {

	private static final String TAG_OL = "ol";

	private static final String TAG_UL = "ul";

	private static final String TAG_LI = "li";

	private static final String ATG_IMG = "img";

	private static final String ATTR_ALT = "alt";

	private static final String ATTR_HREF = "href";

	private static final String ATTR_SRC = "src";

	private static final String CRLF = "\r\n";

	private static final String QUOTE = "> ";

	private static final String TAG_A = "a";

	private static final String TAG_ADDRESS = "address";

	private static final String TAG_BLOCKQUOTE = "blockquote";

	private static final String TAG_BODY = "body";

	private static final String TAG_BR = "br";

	private static final String TAG_DIV = "div";

	private static final String TAG_H1 = "h1";

	private static final String TAG_H2 = "h2";

	private static final String TAG_H3 = "h3";

	private static final String TAG_H4 = "h4";

	private static final String TAG_H5 = "h5";

	private static final String TAG_H6 = "h6";

	private static final String TAG_P = "p";

	private static final String TAG_PRE = "pre";

	private static final String TAG_TD = "td";

	private static final String TAG_TR = "tr";

	private boolean insideBody;

	private boolean anchorTag;

	private boolean preTag;

	private final boolean appendHref;

	private String hrefContent;

	private int quote;

	private final StringBuilder textBuilder;

	/**
	 * Initializes a new {@link HTML2TextHandler}
	 * 
	 * @param capacity
	 *            The initial capacity
	 * @param appendHref
	 *            <code>true</code> to append URLs contained in <i>href</i>s
	 *            and <i>src</i>s; otherwise <code>false</code>
	 */
	public HTML2TextHandler(final int capacity, final boolean appendHref) {
		super();
		this.textBuilder = new StringBuilder(capacity);
		this.appendHref = appendHref;
	}

	/**
	 * Gets the extracted text
	 * 
	 * @return The extracted text
	 */
	public String getText() {
		return textBuilder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleComment(java.lang.String)
	 */
	public void handleComment(final String comment) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleDocDeclaration(java.lang.String)
	 */
	public void handleDocDeclaration(final String docDecl) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleEndTag(java.lang.String)
	 */
	public void handleEndTag(final String tag) {
		if (TAG_BODY.equalsIgnoreCase(tag)) {
			insideBody = false;
		} else if (tag.equalsIgnoreCase(TAG_BLOCKQUOTE)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quote--;
			}
		} else if (tag.equalsIgnoreCase(TAG_P)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quote--;
			}
		} else if (tag.equalsIgnoreCase(TAG_TR)) {
			// Ending table row
			if (insideBody) {
				textBuilder.append(CRLF);
				quote--;
			}
		} else if (tag.equalsIgnoreCase(TAG_LI)) {
			// Ending list entry
			if (insideBody) {
				textBuilder.append(CRLF);
				quote--;
			}
		} else if (tag.equalsIgnoreCase(TAG_TD)) {
			// Ending table column
			if (insideBody) {
				textBuilder.append('\t');
			}
		} else if (appendHref && tag.equalsIgnoreCase(TAG_A)) {
			anchorTag = false;
		} else if (tag.equalsIgnoreCase(TAG_PRE)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quoteText();
				textBuilder.append(CRLF);
				quoteText();
				preTag = false;
			}
		} else if (tag.equalsIgnoreCase(TAG_H1) || tag.equalsIgnoreCase(TAG_H2) || tag.equalsIgnoreCase(TAG_H3)
				|| tag.equalsIgnoreCase(TAG_H4) || tag.equalsIgnoreCase(TAG_H5) || tag.equalsIgnoreCase(TAG_H6)
				|| tag.equalsIgnoreCase(TAG_ADDRESS)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quoteText();
				textBuilder.append(CRLF);
				quoteText();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleError(java.lang.String)
	 */
	public void handleError(final String errorMsg) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleSimpleTag(java.lang.String,
	 *      java.util.Map)
	 */
	public void handleSimpleTag(final String tag, final Map<String, String> attributes) {
		if (tag.equalsIgnoreCase(TAG_BR)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quoteText();
			}
		} else if (tag.equalsIgnoreCase(ATG_IMG)) {
			if (insideBody) {
				final int size = attributes.size();
				if (size > 0) {
					final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
					for (int i = 0; i < size; i++) {
						final Entry<String, String> e = iter.next();
						if (ATTR_ALT.equalsIgnoreCase(e.getKey())) {
							textBuilder.append(' ').append(e.getValue()).append(' ');
						} else if (appendHref && ATTR_SRC.equalsIgnoreCase(e.getKey())) {
							textBuilder.append(" [").append(e.getValue()).append("] ");
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleStartTag(java.lang.String,
	 *      java.util.Map)
	 */
	public void handleStartTag(final String tag, final Map<String, String> attributes) {
		if (TAG_BODY.equalsIgnoreCase(tag)) {
			insideBody = true;
		} else if (tag.equalsIgnoreCase(TAG_BLOCKQUOTE)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quote++;
				quoteText();
			}
		} else if (tag.equalsIgnoreCase(TAG_DIV)) {
			if (insideBody) {
				textBuilder.append(CRLF);
				quoteText();
			}
		} else if (tag.equalsIgnoreCase(TAG_OL) || tag.equalsIgnoreCase(TAG_UL)) {
			// Starting list
			if (insideBody) {
				textBuilder.append(CRLF);
				quoteText();
			}
		} else if (tag.equalsIgnoreCase(TAG_PRE)) {
			if (insideBody) {
				preTag = true;
			}
		} else if (appendHref && tag.equalsIgnoreCase(TAG_A)) {
			if (insideBody) {
				anchorTag = true;
				final int size = attributes.size();
				if (size > 0) {
					final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
					for (int i = 0; i < size; i++) {
						final Entry<String, String> e = iter.next();
						if (ATTR_HREF.equalsIgnoreCase(e.getKey())) {
							hrefContent = e.getValue();
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleCDATA(java.lang.String)
	 */
	public void handleCDATA(final String text) {
		if (insideBody) {
			textBuilder.append(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleText(java.lang.String)
	 */
	public void handleText(final String text, final boolean ignorable) {
		if (insideBody) {
			/*
			 * Add normal text
			 */
			if (preTag) {
				/*
				 * Keep control characters
				 */
				textBuilder.append(replaceHTMLEntities(text));
			} else {
				if (!ignorable) {
					textBuilder.append(replaceHTMLEntities(text.replaceAll("\\s+", " ")));
				}
			}
			if (anchorTag && hrefContent != null && !text.equalsIgnoreCase(hrefContent)) {
				textBuilder.append(" [").append(hrefContent).append(']');
			}
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

	/**
	 * Resets this handler for re-usage
	 */
	public void reset() {
		quote = 0;
		textBuilder.setLength(0);
	}

	public void handleXMLDeclaration(final String version, final Boolean standalone, final String encoding) {
	}

}
