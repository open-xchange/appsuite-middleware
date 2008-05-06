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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.mail.text.parser.HTMLHandler;

/**
 * {@link HTMLFilterHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLFilterHandler implements HTMLHandler {

	private static final String STYLE = "style";

	private static final Pattern PATTERN_TAG_LINE = Pattern.compile(
			"_map\\[\"(\\p{Alnum}+)\"\\]\\s*=\\s*\"(\\p{Print}+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("(\\p{Alnum}+)(?:\\[(\\p{Print}+?)\\])?");

	private final Map<String, Map<String, Set<String>>> filter;

	private final StringBuilder htmlBuilder;

	private final boolean isWhitelist;

	private int level;

	private boolean isCss;

	/**
	 * Parses specified list; e.g:
	 * 
	 * <pre>
	 * _map[&quot;a&quot;] = &quot;,href,name,tabindex,target,type,&quot;;
	 * _map[&quot;area&quot;] = &quot;,alt,coords,href,nohref[nohref],shape[:rect:circle:poly:default:],tabindex,target,&quot;;
	 * _map[&quot;basefont&quot;] = &quot;,color,face,size,&quot;;
	 * _map[&quot;bdo&quot;] = &quot;,dir[:ltr:rtl:]&quot;;
	 * _map[&quot;blockquote&quot;] = &quot;,type,&quot;;
	 * _map[&quot;body&quot;] = &quot;,alink,background,bgcolor,link,text,vlink,&quot;;
	 * _map[&quot;br&quot;] = &quot;,clear[:left:right:all:none:]&quot;;
	 * _map[&quot;button&quot;] = &quot;,disabled[disabled],name,tabindex,type[:button:submit:reset:],value,&quot;;
	 * _map[&quot;caption&quot;] = &quot;,align[:top:bottom:left:right:]&quot;;
	 * </pre>
	 * 
	 * @param list
	 *            The list string
	 * @return The parsed map
	 */
	private static Map<String, Map<String, Set<String>>> parseList(final String list) {
		final Matcher m = PATTERN_TAG_LINE.matcher(list);
		final Map<String, Map<String, Set<String>>> tagMap = new HashMap<String, Map<String, Set<String>>>();
		while (m.find()) {
			final Matcher attribMatcher = PATTERN_ATTRIBUTE.matcher(m.group(2));
			final Map<String, Set<String>> attribMap = new HashMap<String, Set<String>>();
			while (attribMatcher.find()) {
				final String values = attribMatcher.group(2);
				if (null == values) {
					attribMap.put(attribMatcher.group(1).toLowerCase(Locale.ENGLISH), null);
				} else {
					final Set<String> valueSet = new HashSet<String>();
					final String[] valArr = values.charAt(0) == ':' ? values.substring(1).split("\\s*:\\s*") : values
							.split("\\s*:\\s*");
					for (final String value : valArr) {
						valueSet.add(value);
					}
					attribMap.put(attribMatcher.group(1).toLowerCase(Locale.ENGLISH), valueSet);
				}
			}
			tagMap.put(m.group(1).toLowerCase(Locale.ENGLISH), attribMap);
		}
		return tagMap;
	}

	/**
	 * 
	 * <pre>
	 * _stylemap[&quot;background&quot;] = &quot;&quot;; // combi
	 * _stylemap[&quot;background-attachment&quot;] = &quot;,scroll,fixed,&quot;;
	 * _stylemap[&quot;background-color&quot;] = &quot;c,transparent,&quot;; // color
	 * _stylemap[&quot;background-image&quot;] = &quot;u&quot;; // url
	 * _stylemap[&quot;background-position&quot;] = &quot;,top,bottom,center,left,right,&quot;;
	 * _stylemap[&quot;background-repeat&quot;] = &quot;,repeat,repeat-x,repeat-y,no-repeat,&quot;;
	 * _stylemap[&quot;border&quot;] = &quot;&quot;; // combi
	 * </pre>
	 * 
	 * 
	 * @param cssList
	 * @return
	 */
	private static Map<String, Map<String, Set<String>>> parseCSSList(final String cssList) {
		return null;
	}

	/**
	 * Initializes a new {@link HTMLFilterHandler}
	 * 
	 * @param isWhitelist
	 *            Whether to apply white list or black list filter
	 * @param capacity
	 *            The initial capacity
	 * @param filter
	 *            The filter map
	 */
	public HTMLFilterHandler(final boolean isWhitelist, final int capacity,
			final Map<String, Map<String, Set<String>>> filter) {
		super();
		this.isWhitelist = isWhitelist;
		htmlBuilder = new StringBuilder(capacity);
		this.filter = filter;
	}

	/**
	 * Initializes a new {@link HTMLFilterHandler}
	 * 
	 * @param isWhitelist
	 *            Whether to apply white list or black list filter
	 * @param capacity
	 *            The initial capacity
	 * @param list
	 *            The list as string representation
	 */
	public HTMLFilterHandler(final boolean isWhitelist, final int capacity, final String list) {
		this.isWhitelist = isWhitelist;
		htmlBuilder = new StringBuilder(capacity);
		this.filter = parseList(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleComment(java.lang.String)
	 */
	public void handleComment(final String comment) {
		htmlBuilder.append("<!--").append(comment).append("-->");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleDocDeclaration(java.lang.String)
	 */
	public void handleDocDeclaration(final String docDecl) {
		htmlBuilder.append("<!DOCTYPE").append(docDecl).append('>');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleEndTag(java.lang.String)
	 */
	public void handleEndTag(final String tag) {
		if (level == 0) {
			if (STYLE.equals(tag)) {
				isCss = false;
			}
			htmlBuilder.append("</").append(tag).append('>');
		} else {
			level--;
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
		if (level > 0) {
			return;
		}
		if (isWhitelist) {
			if (filter.containsKey(tag)) {
				addStartTag(tag, attributes, true, filter.get(tag));
			}
		} else {
			if (!filter.containsKey(tag)) {
				addCompleteStartTag(tag, attributes, true);
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
		if (level > 0) {
			level++;
			return;
		}
		if (isWhitelist) {
			if (STYLE.equals(tag)) {
				// TODO: Handle css
				isCss = true;
			} else if (filter.containsKey(tag)) {
				addStartTag(tag, attributes, false, filter.get(tag));
			} else {
				level++;
			}
		} else {
			if (STYLE.equals(tag)) {
				// TODO: Handle css
				isCss = true;
			} else if (filter.containsKey(tag)) {
				level++;
			} else {
				addCompleteStartTag(tag, attributes, false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleText(java.lang.String)
	 */
	public void handleText(final String text, final boolean ignorable) {
		if (level == 0) {
			htmlBuilder.append(text);
		}
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
	private void addCompleteStartTag(final String tag, final Map<String, String> a, final boolean simple) {
		htmlBuilder.append('<').append(tag);
		final int size = a.size();
		final Iterator<Entry<String, String>> iter = a.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Entry<String, String> e = iter.next();
			if (STYLE.equals(e.getKey())) {
				// TODO: handle css
			} else {
				htmlBuilder.append(' ').append(e.getKey()).append("=\"").append(e.getValue()).append('"');
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
	 *            The allowed tag's attributes or <code>null</code> to allow
	 *            all
	 */
	private void addStartTag(final String tag, final Map<String, String> a, final boolean simple,
			final Map<String, Set<String>> attribs) {
		if (null == attribs) {
			addCompleteStartTag(tag, a, simple);
		} else {
			htmlBuilder.append('<').append(tag);
			final int size = a.size();
			final Iterator<Entry<String, String>> iter = a.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Entry<String, String> e = iter.next();
				if (STYLE.equals(e.getKey())) {
					// TODO: handle css
				} else if (attribs.containsKey(e.getKey())) {
					final Set<String> allowedValues = attribs.get(e.getKey());
					if (null == allowedValues || allowedValues.contains(e.getValue())) {
						htmlBuilder.append(' ').append(e.getKey()).append("=\"").append(e.getValue()).append('"');
					}
				}
			}
			if (simple) {
				htmlBuilder.append('/');
			}
			htmlBuilder.append('>');
		}
	}

	/**
	 * Gets the filtered HTML content
	 * 
	 * @return The filtered HTML content
	 */
	public String getHTML() {
		return htmlBuilder.toString();
	}
}
