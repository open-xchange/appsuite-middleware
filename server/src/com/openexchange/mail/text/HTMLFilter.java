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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
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
 * list or black list of HTML tags and their attributes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLFilter {

	private static final class HTMLFilterParserCallback extends ParserCallback {

		private final Map<String, Set<String>> filter;

		private final StringBuilder htmlBuilder;

		private final boolean isWhitelist;

		private int level;

		public HTMLFilterParserCallback(final boolean isWhitelist, final int capacity,
				final Map<String, Set<String>> filter) {
			super();
			this.isWhitelist = isWhitelist;
			htmlBuilder = new StringBuilder(capacity);
			this.filter = filter;
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
				addAttribute(e.nextElement(), a);
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
				if (DEFAULT_WHITELIST_ATTRIBUTES.contains(attributeName.toString())) {
					addAttribute(attributeName, a);
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
				final Set<String> attribs) {
			if (null == attribs) {
				addDefaultStartTag(tag, a, simple);
			} else {
				htmlBuilder.append('<').append(tag.toString());
				for (final Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements();) {
					final Object attributeName = e.nextElement();
					if (attribs.contains(attributeName.toString())
							|| DEFAULT_WHITELIST_ATTRIBUTES.contains(attributeName.toString())) {
						addAttribute(attributeName, a);
					}
				}
				if (simple) {
					htmlBuilder.append('/');
				}
				htmlBuilder.append('>');
			}
		}

		private void addAttribute(final Object attributeName, final MutableAttributeSet a) {
			htmlBuilder.append(' ').append(attributeName.toString());
			final Object attribute = a.getAttribute(attributeName);
			if (!HTML.NULL_ATTRIBUTE_VALUE.equals(attribute)) {
				/*
				 * A non-empty attribute
				 */
				htmlBuilder.append("=\"").append(attribute.toString()).append('"');
			}
		}

		public String getHTML(final String doctype) {
			if (null != doctype) {
				htmlBuilder.insert(0, doctype);
			}
			return prettyPrint(htmlBuilder.toString(), "UTF-8");
		}

		@Override
		public void handleComment(final char[] data, final int pos) {
			if (level == 0) {
				htmlBuilder.append("<!--").append(data).append("-->");
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
				if (filter.containsKey(tag.toString())) {
					addStartTag(tag, a, true, filter.get(tag.toString()));
				} else if (DEFAULT_WHITELIST_TAGS.contains(tag.toString())) {
					addDefaultStartTag(tag, a, true);
				}
			} else {
				if (!filter.containsKey(tag.toString())) {
					addCompleteStartTag(tag, a, true);
				}
			}
		}

		@Override
		public void handleStartTag(final HTML.Tag tag, final MutableAttributeSet a, final int pos) {
			if (level > 0) {
				level++;
				return;
			}
			if (isWhitelist) {
				if (filter.containsKey(tag.toString())) {
					addStartTag(tag, a, false, filter.get(tag.toString()));
				} else if (DEFAULT_WHITELIST_TAGS.contains(tag.toString())) {
					addDefaultStartTag(tag, a, false);
				} else {
					level++;
				}
			} else {
				if (filter.containsKey(tag.toString())) {
					level++;
				} else {
					addCompleteStartTag(tag, a, false);
				}
			}
		}

		@Override
		public void handleText(final char[] data, final int pos) {
			if (level == 0) {
				htmlBuilder.append(data);
			}
		}

		public void reset() {
			htmlBuilder.setLength(0);
		}
	}

	private static final Set<String> DEFAULT_WHITELIST_ATTRIBUTES = new HashSet<String>(asList(new String[] { "abbr",
			"accept", "accept-charset", "accesskey", HTML.Attribute.ACTION.toString(), HTML.Attribute.ALIGN.toString(),
			HTML.Attribute.ALT.toString(), "axis", HTML.Attribute.BORDER.toString(),
			HTML.Attribute.CELLPADDING.toString(), HTML.Attribute.CELLSPACING.toString(), "char", "charoff", "charset",
			HTML.Attribute.CHECKED.toString(), "class", HTML.Attribute.CLASS.toString(),
			HTML.Attribute.CLEAR.toString(), HTML.Attribute.COLS.toString(), HTML.Attribute.COLSPAN.toString(),
			HTML.Attribute.COLOR.toString(), HTML.Attribute.COMPACT.toString(), HTML.Attribute.COORDS.toString(),
			"datetime", HTML.Attribute.DIR.toString(), "disabled", HTML.Attribute.ENCTYPE.toString(), "for", "frame",
			"headers", HTML.Attribute.HEIGHT.toString(), HTML.Attribute.HREF.toString(), "hreflang",
			HTML.Attribute.HSPACE.toString(), HTML.Attribute.ID.toString(), HTML.Attribute.ISMAP.toString(), "label",
			HTML.Attribute.LANG.toString(), "longdesc", HTML.Attribute.MAXLENGTH.toString(),
			HTML.Attribute.METHOD.toString(), HTML.Attribute.MULTIPLE.toString(), HTML.Attribute.NAME.toString(),
			HTML.Attribute.NOHREF.toString(), HTML.Attribute.NOSHADE.toString(), HTML.Attribute.NOWRAP.toString(),
			HTML.Attribute.PROMPT.toString(), HTML.Attribute.REL.toString(), "readonly", HTML.Attribute.REV.toString(),
			HTML.Attribute.ROWS.toString(), HTML.Attribute.ROWSPAN.toString(), "rules", "scope",
			HTML.Attribute.SELECTED.toString(), HTML.Attribute.SHAPE.toString(), HTML.Attribute.SIZE.toString(),
			HTML.Attribute.SRC.toString(), HTML.Attribute.START.toString(), "summary", "tabindex",
			HTML.Attribute.TARGET.toString(), HTML.Attribute.TITLE.toString(), HTML.Attribute.TYPE.toString(),
			HTML.Attribute.USEMAP.toString(), HTML.Attribute.VALIGN.toString(), HTML.Attribute.VALUE.toString(),
			HTML.Attribute.VSPACE.toString(), HTML.Attribute.WIDTH.toString() }));

	private static final Set<String> DEFAULT_WHITELIST_TAGS = new HashSet<String>(asList(new String[] {
			HTML.Tag.A.toString(), "abbr", "acronym", HTML.Tag.ADDRESS.toString(), HTML.Tag.AREA.toString(),
			HTML.Tag.B.toString(), HTML.Tag.BIG.toString(), HTML.Tag.BLOCKQUOTE.toString(), HTML.Tag.BODY.toString(),
			HTML.Tag.BR.toString(), "button", HTML.Tag.CAPTION.toString(), HTML.Tag.CENTER.toString(),
			HTML.Tag.CITE.toString(), HTML.Tag.CODE.toString(), "col", "colgroup", HTML.Tag.DD.toString(), "del",
			HTML.Tag.DFN.toString(), HTML.Tag.DIR.toString(), HTML.Tag.DIV.toString(), HTML.Tag.DL.toString(),
			HTML.Tag.DT.toString(), HTML.Tag.EM.toString(), "fieldset", HTML.Tag.FONT.toString(),
			HTML.Tag.FORM.toString(), HTML.Tag.H1.toString(), HTML.Tag.H2.toString(), HTML.Tag.H3.toString(),
			HTML.Tag.H4.toString(), HTML.Tag.H5.toString(), HTML.Tag.H6.toString(), HTML.Tag.HEAD.toString(),
			HTML.Tag.HR.toString(), HTML.Tag.HTML.toString(), HTML.Tag.I.toString(), HTML.Tag.IMG.toString(),
			HTML.Tag.INPUT.toString(), "ins", HTML.Tag.KBD.toString(), "label", "legend", HTML.Tag.LI.toString(),
			HTML.Tag.MAP.toString(), HTML.Tag.MENU.toString(), HTML.Tag.OL.toString(), "optgroup",
			HTML.Tag.OPTION.toString(), HTML.Tag.P.toString(), HTML.Tag.PRE.toString(), "q", HTML.Tag.S.toString(),
			HTML.Tag.SAMP.toString(), HTML.Tag.SELECT.toString(), HTML.Tag.SMALL.toString(), HTML.Tag.SPAN.toString(),
			HTML.Tag.STRIKE.toString(), HTML.Tag.STRONG.toString(), HTML.Tag.SUB.toString(), HTML.Tag.TABLE.toString(),
			"tbody", HTML.Tag.TD.toString(), HTML.Tag.TEXTAREA.toString(), "tfoot", HTML.Tag.TH.toString(), "thead",
			HTML.Tag.TR.toString(), HTML.Tag.TT.toString(), HTML.Tag.U.toString(), HTML.Tag.UL.toString(),
			HTML.Tag.VAR.toString() }));

	private static final Set<String> EMPTY_SET = Collections.unmodifiableSet(new HashSet<String>(0));

	private static final HTMLEditorKit.Parser PARSER = new ParserDelegator();

	private static final Pattern PATTERN_DOCTYPE = Pattern.compile("<!DOCTYPE[^>]+>", Pattern.CASE_INSENSITIVE);

	/**
	 * Parses specified list; e.g:
	 * 
	 * <pre>
	 * &quot;a[name|href|target|title|onclick],img[class|src|border=0|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],..&quot;
	 * </pre>
	 * 
	 * @param list
	 *            The list string
	 * @return The parsed map
	 */
	private static Map<String, Set<String>> parse(final String list) {
		final String[] tags = list.split("\\s*,\\s*");
		final Map<String, Set<String>> map = new HashMap<String, Set<String>>(tags.length);
		NextTag: for (final String tagStr : tags) {
			final int bracketStart = tagStr.indexOf('[');
			if (bracketStart == -1) {
				map.put(tagStr.trim().toLowerCase(Locale.ENGLISH), EMPTY_SET);
			} else {
				final String tag = tagStr.substring(0, bracketStart).trim().toLowerCase(Locale.ENGLISH);
				final int bracketEnd = tagStr.indexOf(']', bracketStart + 1);
				if (bracketEnd == -1) {
					/*
					 * Invalid list string. Skip parsing attributes.
					 */
					map.put(tag, EMPTY_SET);
					continue NextTag;
				}
				final String[] attribs = tagStr.substring(bracketStart + 1, bracketEnd).split("\\s*\\|\\s*");
				final Set<String> attributeSet = new HashSet<String>(attribs.length);
				for (final String attribStr : attribs) {
					attributeSet.add(attribStr.trim().toLowerCase(Locale.ENGLISH));
				}
				map.put(tag, attributeSet);
			}
		}
		return map;
	}

	private static final Pattern PATTERN_TAG_LINE = Pattern.compile(
			"_map\\[\"(\\p{Alnum}+)\"\\]\\s*=\\s*\"(\\p{Print}+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("(\\p{Alnum}+)(?:\\[(\\p{Print}+?)\\])?");

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
	public String applyBlacklist(final String html, final Map<String, Set<String>> blacklist) throws IOException {
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

	/**
	 * Applies specified black list to given HTML content
	 * 
	 * @param html
	 *            The HTML content
	 * @param blacklist
	 *            The black list's string representation; e.g.
	 * 
	 * <pre>
	 * a[name|href|target|title|onclick],img[src|alt|title|hspace|vspace|width|height|align|name],...
	 * </pre>
	 * 
	 * @return The stripped HTML content according to specified black list
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public String applyBlacklist(final String html, final String blacklist) throws IOException {
		return applyBlacklist(html, parse(blacklist));
	}

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
	public String applyWhitelist(final String html, final Map<String, Set<String>> whitelist) throws IOException {
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
	 * Applies specified white list to given HTML content
	 * 
	 * @param html
	 *            The HTML content
	 * @param whitelist
	 *            The white list's string representation; e.g.
	 * 
	 * <pre>
	 * a[name|href|target|title|onclick],img[src|alt|title|hspace|vspace|width|height|align|name],...
	 * </pre>
	 * 
	 * @return The stripped HTML content according to specified white list
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public String applyWhitelist(final String html, final String whitelist) throws IOException {
		return applyWhitelist(html, parse(whitelist));
	}

}
