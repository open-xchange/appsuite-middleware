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

import static com.openexchange.mail.text.CSSMatcher.checkCSS;
import static com.openexchange.mail.text.CSSMatcher.checkCSSElements;
import static com.openexchange.mail.text.CSSMatcher.containsCSSElement;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.mail.text.parser.HTMLHandler;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link HTMLFilterHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLFilterHandler implements HTMLHandler {

	private static final String WARN_USING_DEFAULT_WHITE_LIST = "Using default white list";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HTMLFilterHandler.class);

	private static final String CRLF = "\r\n";

	private static final String STYLE = "style";

	private static final String HEAD = "head";

	private static final String META = "meta";

	private static final String HTTP_EQUIV = "http-equiv";

	private static Map<String, Map<String, Set<String>>> shtmlMap;

	private static Map<String, Set<String>> sstyleMap;

	private final Map<String, Map<String, Set<String>>> htmlMap;

	private final Map<String, Set<String>> styleMap;

	private final StringBuilder htmlBuilder;

	private final StringBuilder attrBuilder;

	private int level;

	private boolean isCss;

	private final String[] result;

	/**
	 * Initializes a new {@link HTMLFilterHandler}
	 * 
	 * @param capacity
	 *            The initial capacity
	 * @param htmlMap
	 *            The HTML map
	 * @param styleMap
	 *            The CSS style map
	 */
	public HTMLFilterHandler(final int capacity, final Map<String, Map<String, Set<String>>> htmlMap,
			final Map<String, Set<String>> styleMap) {
		super();
		result = new String[1];
		htmlBuilder = new StringBuilder(capacity);
		attrBuilder = new StringBuilder(128);
		this.htmlMap = htmlMap;
		this.styleMap = styleMap;
		checkHTMLMap();
	}

	/**
	 * Initializes a new {@link HTMLFilterHandler}
	 * 
	 * @param capacity
	 *            The initial capacity
	 * @param mapStr
	 *            The map as string representation
	 */
	public HTMLFilterHandler(final int capacity, final String mapStr) {
		super();
		result = new String[1];
		htmlBuilder = new StringBuilder(capacity);
		attrBuilder = new StringBuilder(128);
		htmlMap = parseHTMLMap(mapStr);
		styleMap = parseStyleMap(mapStr);
		checkHTMLMap();
	}

	/**
	 * Initializes a new {@link HTMLFilterHandler} with default white list given
	 * through property {@link Property#Whitelist}
	 * 
	 * @param capacity
	 *            The initial capacity
	 */
	public HTMLFilterHandler(final int capacity) {
		super();
		result = new String[1];
		htmlBuilder = new StringBuilder(capacity);
		attrBuilder = new StringBuilder(128);
		if (null == shtmlMap) {
			loadWhitelist();
		}
		htmlMap = shtmlMap;
		styleMap = sstyleMap;
		checkHTMLMap();
	}

	/**
	 * Loads the white list
	 */
	public static void loadWhitelist() {
		synchronized (HTMLFilterHandler.class) {
			if (null == shtmlMap) {
				String mapStr = null;
				{
					String whitelist = SystemConfig.getProperty(SystemConfig.Property.Whitelist);
					if (null == whitelist) {
						final ConfigurationService cs = ServerServiceRegistry.getInstance().getService(
								ConfigurationService.class);
						if (null != cs) {
							whitelist = cs.getProperty(SystemConfig.Property.Whitelist.getPropertyName());
						}
					}
					if (null == whitelist) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(WARN_USING_DEFAULT_WHITE_LIST);
						}
						mapStr = DEFAULT_WHITELIST;
					} else {
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(new FileInputStream(whitelist),
									"US-ASCII"));
							final StringBuilder sb = new StringBuilder();
							String line = null;
							while ((line = reader.readLine()) != null) {
								sb.append(line).append(CRLF);
							}
							mapStr = sb.toString();
						} catch (final UnsupportedEncodingException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn(WARN_USING_DEFAULT_WHITE_LIST, e);
							}
							mapStr = DEFAULT_WHITELIST;
						} catch (final FileNotFoundException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn(WARN_USING_DEFAULT_WHITE_LIST, e);
							}
							mapStr = DEFAULT_WHITELIST;
						} catch (final IOException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn(WARN_USING_DEFAULT_WHITE_LIST, e);
							}
							mapStr = DEFAULT_WHITELIST;
						} finally {
							if (null != reader) {
								try {
									reader.close();
								} catch (final IOException e) {
									LOG.error(e.getMessage(), e);
								}
							}
						}
					}
				}
				shtmlMap = parseHTMLMap(mapStr);
				if (!shtmlMap.containsKey(HEAD)) {
					shtmlMap.put(HEAD, null);
				}
				shtmlMap = Collections.unmodifiableMap(shtmlMap);
				sstyleMap = Collections.unmodifiableMap(parseStyleMap(mapStr));
			}
		}
	}

	/**
	 * Resets the white list
	 */
	public static void resetWhitelist() {
		synchronized (HTMLFilterHandler.class) {
			shtmlMap = null;
			sstyleMap = null;
		}
	}

	private void checkHTMLMap() {
		if (!htmlMap.containsKey(HEAD)) {
			htmlMap.put(HEAD, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleXMLDeclaration(java.lang.String,
	 *      java.lang.Boolean, java.lang.String)
	 */
	public void handleXMLDeclaration(final String version, final Boolean standalone, final String encoding) {
		if (null != version) {
			htmlBuilder.append("<?xml version=\"").append(version).append('"');
			if (null != standalone) {
				htmlBuilder.append(" standalone=\"").append(Boolean.TRUE.equals(standalone) ? "yes" : "no").append('"');
			}
			if (null != encoding) {
				htmlBuilder.append(" encoding=\"").append("encoding").append('"');
			}
			htmlBuilder.append("?>").append(CRLF);
		}
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
			if (isCss && STYLE.equals(tag)) {
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
		if (htmlMap.containsKey(tag)) {
			addStartTag(tag, attributes, true, htmlMap.get(tag));
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
		if (htmlMap.containsKey(tag)) {
			if (STYLE.equals(tag)) {
				isCss = true;
			}
			addStartTag(tag, attributes, false, htmlMap.get(tag));
		} else {
			level++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleCDATA(java.lang.String)
	 */
	public void handleCDATA(final String text) {
		if (level == 0) {
			htmlBuilder.append("<![CDATA[");
			if (isCss) {
				/*
				 * Handle style attribute
				 */
				checkCSS(text, styleMap, true, true, result);
				final String checkedCSS = result[0];
				htmlBuilder.append(checkedCSS);
			} else {
				htmlBuilder.append(text);
			}
			htmlBuilder.append("]]>");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.text.parser.HTMLHandler#handleText(java.lang.String,
	 *      boolean)
	 */
	public void handleText(final String text, final boolean ignorable) {
		if (level == 0) {
			if (isCss) {
				if (ignorable) {
					htmlBuilder.append(text);
				} else {
					/*
					 * Handle style attribute
					 */
					checkCSS(text, styleMap, true, true, result);
					final String checkedCSS = result[0];
					htmlBuilder.append(checkedCSS);
				}
			} else {
				htmlBuilder.append(text);
			}
		}
	}

	private static final String VAL_START = "=\"";

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
		attrBuilder.setLength(0);
		if (simple && META.equals(tag) && a.containsKey(HTTP_EQUIV) && attribs.containsKey(HTTP_EQUIV)) {
			/*
			 * Special handling for allowed meta tag which provides an allowed
			 * HTTP header indicated through 'http-equiv' attribute
			 */
			final int size = a.size();
			final Iterator<Entry<String, String>> iter = a.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Entry<String, String> e = iter.next();
				attrBuilder.append(' ').append(e.getKey()).append(VAL_START).append(e.getValue()).append('"');
			}
			htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append('/').append('>');
			return;
		}
		final int size = a.size();
		final Iterator<Entry<String, String>> iter = a.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Entry<String, String> e = iter.next();
			if (STYLE.equals(e.getKey())) {
				/*
				 * Handle style attribute
				 */
				checkCSSElements(e.getValue(), styleMap, true, result);
				final String checkedCSS = result[0];
				if (containsCSSElement(checkedCSS)) {
					attrBuilder.append(' ').append(STYLE).append(VAL_START).append(checkedCSS).append('"');
				}
			} else {
				if (null == attribs) {
					attrBuilder.append(' ').append(e.getKey()).append(VAL_START).append(e.getValue()).append('"');
				} else {
					final String nameLower = e.getKey().toLowerCase(Locale.ENGLISH);
					if (attribs.containsKey(nameLower)) {
						final Set<String> allowedValues = attribs.get(nameLower);
						if (null == allowedValues || allowedValues.contains(e.getValue().toLowerCase(Locale.ENGLISH))) {
							attrBuilder.append(' ').append(e.getKey()).append(VAL_START).append(e.getValue()).append(
									'"');
						}
					}
				}
			}
		}
		if (simple) {
			if (attrBuilder.length() > 0) {
				htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append('/').append('>');
			}
		} else {
			htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append('>');
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

	/*
	 * ######################### HELPERS ##############################
	 */

	private static final String DEFAULT_WHITELIST = "HTML-Tags and Attributes\n"
			+ "=======================\n"
			+ "	_map[\"a\"] = \",href,name,tabindex,target,type,\";\n"
			+ "	_map[\"area\"] = \",alt,coords,href,nohref[nohref],shape[:rect:circle:poly:default:],tabindex,target,\";\n"
			+ "	_map[\"basefont\"] = \",color,face,size,\";\n"
			+ "	_map[\"bdo\"] = \",dir[:ltr:rtl:]\";\n"
			+ "	_map[\"blockquote\"] = \",type,\";\n"
			+ "	_map[\"body\"] = \",alink,background,bgcolor,link,text,vlink,\";\n"
			+ "	_map[\"br\"] = \",clear[:left:right:all:none:]\";\n"
			+ "	_map[\"button\"] = \",disabled[disabled],name,tabindex,type[:button:submit:reset:],value,\";\n"
			+ "	_map[\"caption\"] = \",align[:top:bottom:left:right:]\";\n"
			+ "	_map[\"col\"] = \",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"; // span(Zahl)\n"
			+ "	_map[\"colgroup\"] = \",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"; // span(Zahl)\n"
			+ "	_map[\"del\"] = \",datetime,\";\n"
			+ "	_map[\"dir\"] = \",compact[compact]\";\n"
			+ "	_map[\"div\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"dl\"] = \",compact[compact]\";\n"
			+ "	_map[\"font\"] = \",color,face,size,\";\n"
			+ "	_map[\"form\"] = \",action,accept,accept-charset,enctype,method[:get:post:],name,target,\";\n"
			+ "	_map[\"h1\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"h2\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"h3\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"h4\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"h5\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"h6\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"hr\"] = \",align[:left:center:right:],noshade[noshade],size,width,\";\n"
			+ "	_map[\"html\"] = \",version,xmlns,\";\n"
			+ "	_map[\"img\"] = \",align[:top:middle:bottom:left:right:],alt,border,height,hspace,ismap[ismap],name,src,usemap,vspace,width,\";\n"
			+ "	_map[\"input\"] = \",accept,align[:top:middle:bottom:left:right:center:],alt,checked[checked],disabled[disabled],maxlength[],name,readonly[readonly],size,src,tabindex,type[:text:checkbox:radio:submit:reset:hidden:image:button:password:],value,\";\n"
			+ "	_map[\"ins\"] = \",datetime,\";\n"
			+ "	_map[\"label\"] = \",for,\";\n"
			+ "	_map[\"legend\"] = \",align[:left:top:right:bottom:]\";\n"
			+ "	_map[\"li\"] = \",type[:disc:square:circle:1:a:A:i:I:],value[],\"; // value(Zahl)\n"
			+ "	_map[\"map\"] = \",name,\";\n"
			+ "	_map[\"meta\"] = \",http-equiv[:content-type:],\";\n"
			+ "	_map[\"ol\"] = \",compact[compact],start[],type[:1:a:A:i:I:],\";\n"
			+ "	_map[\"optgroup\"] = \",disabled[disabled],label,\";\n"
			+ "	_map[\"option\"] = \",disabled[disabled],label,selected[selected],value,\";\n"
			+ "	_map[\"p\"] = \",align[:left:center:right:justify:]\";\n"
			+ "	_map[\"pre\"] = \",width[],\";\n"
			+ "	_map[\"select\"] = \",disabled[disabled],multiple[multiple],name,size,tabindex[],\";\n"
			+ "	_map[\"style\"] = \",media,type,\";\n"
			+ "	_map[\"table\"] = \",align[:left:center:right:],background,border,bgcolor,cellpadding,cellspacing,frame[:void:above:below:hsides:ihs:rhs:vsides:box:border:],rules[:none:groups:rows:cols:all:],summary,width,\";\n"
			+ "	_map[\"tbody\"] = \",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\";\n"
			+ "	_map[\"td\"] = \",abbr,align[:left:center:right:justify:char:],axis,background,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\";\n"
			+ "	_map[\"textarea\"] = \",cols[],disabled[disabled],name,readonly[readonly],rows[],tabindex[],\";\n"
			+ "	_map[\"tfoot\"] = \",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\";\n"
			+ "	_map[\"th\"] = \",abbr,align[:left:center:right:justify:char:],axis,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\";\n"
			+ "	_map[\"thead\"] = \",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\";\n"
			+ "	_map[\"tr\"] = \",align[:left:center:right:justify:char:],bgcolor,char,charoff,valign[:top:middle:bottom:baseline:],height,\";\n"
			+ "	_map[\"ul\"] = \",compact[compact],type[:disc:square:circle:],\";\n"
			+ "\n"
			+ "CSS-Key/Value-Pairs\n"
			+ "===================\n"
			+ "	_stylemap[\"azimuth\"] = \",left-side,left-side behind,far-left,far-left behind,left,left behind,center-left,center-left behind,center,center behind,center-right,center-right behind,right,right behind,far-right,far-right behind,right-side,right behind,\";\n"
			+ "	_stylemap[\"background\"] = \"\"; // combi\n"
			+ "	_stylemap[\"background-attachment\"] = \",scroll,fixed,\";\n"
			+ "	_stylemap[\"background-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"background-image\"] = \"u\"; // url\n"
			+ "	_stylemap[\"background-position\"] = \",top,bottom,center,left,right,\";\n"
			+ "	_stylemap[\"background-repeat\"] = \",repeat,repeat-x,repeat-y,no-repeat,\";\n"
			+ "	_stylemap[\"border\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-bottom\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-bottom-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"border-bottom-style\"] = \",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_stylemap[\"border-bottom-width\"] = \"n\"; // number string without %\n"
			+ "	_stylemap[\"border-collapse\"] = \",separate,collapse,\";\n"
			+ "	_stylemap[\"border-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"border-left\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-left-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"border-left-style\"] = \",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_stylemap[\"border-left-width\"] = \"n\"; // number string without %\n"
			+ "	_stylemap[\"border-right\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-right-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"border-right-style\"] = \",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_stylemap[\"border-right-width\"] = \"n\"; // number string without %\n"
			+ "	_stylemap[\"border-spacing\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"border-style\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-top\"] = \"\"; // combi\n"
			+ "	_stylemap[\"border-top-color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"border-top-style\"] = \",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_stylemap[\"border-top-width\"] = \"n\";\n"
			+ "	_stylemap[\"border-width\"] = \"\"; // combi\n"
			+ "	_stylemap[\"bottom\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"caption-side\"] = \",top,bottom,left,right,\";\n"
			+ "	_stylemap[\"centerline\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"clear\"] = \",left,right,both,none,\";\n"
			+ "	_stylemap[\"clip\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"color\"] = \"c,transparent,\"; // color\n"
			+ "	_stylemap[\"content\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"counter-increment\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"counter-reset\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"counter\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"cue\"] = \"u\"; // url\n"
			+ "	_stylemap[\"cue-after\"] = \"u\"; // url\n"
			+ "	_stylemap[\"cue-before\"] = \"u\"; // url\n"
			+ "	_stylemap[\"cursor\"] = \",auto,default,crosshair,pointer,move,n-resize,ne-resize,e-resize,se-resize,s-resize,sw-resize,w-resize,nw-resize,text,wait,help,\";\n"
			+ "	_stylemap[\"definition-src\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"direction\"] = \",ltr,rtl,\";\n"
			+ "	_stylemap[\"display\"] = \",block,inline,list-item,marker,run-in,compact,none,table,inline-table,table-row,table-cell,table-row-group,table-header-group,table-footer-group,table-column,table-column-group,table-caption,\";\n"
			+ "	_stylemap[\"empty-cells\"] = \",show,hide,\";\n"
			+ "	_stylemap[\"elevation\"] = \",below,level,above,higher,lower,\";\n"
			+ "	_stylemap[\"filter\"] = \"d\"; // delete \n"
			+ "	_stylemap[\"float\"] = \",left,right,none,\";\n"
			+ "	_stylemap[\"font\"] = \"\"; // combi\n"
			+ "	_stylemap[\"font-family\"] = \"*\"; // any input\n"
			+ "	_stylemap[\"font-color\"] = \"c,transparent,\";\n"
			+ "	_stylemap[\"font-size\"] = \"N,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,\";\n"
			+ "	_stylemap[\"font-stretch\"] = \",wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,\";\n"
			+ "	_stylemap[\"font-style\"] = \",italic,oblique,normal,\";\n"
			+ "	_stylemap[\"font-variant\"] = \",small-caps,normal,\";\n"
			+ "	_stylemap[\"font-weight\"] = \",bold,bolder,lighter,100,200,300,400,500,600,700,800,900,normal,\";\n"
			+ "	_stylemap[\"height\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"left\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"letter-spacing\"] = \"n\"; // number string without %\n"
			+ "	_stylemap[\"line-height\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"list-style\"] = \"\"; // combi	\n"
			+ "	_stylemap[\"list-style-image\"] = \"u,none,\"; // url\n"
			+ "	_stylemap[\"list-style-position\"] = \",inside,outside,\";\n"
			+ "	_stylemap[\"list-style-type\"] = \",decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,none,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\";\n"
			+ "	_stylemap[\"margin\"] = \"\"; // combi\n"
			+ "	_stylemap[\"margin-bottom\"] = \"N,auto,inherit,\"; // number string\n"
			+ "	_stylemap[\"margin-left\"] = \"N,auto,inherit,\"; // number string\n"
			+ "	_stylemap[\"margin-right\"] = \"N,auto,inherit,\"; // number string\n"
			+ "	_stylemap[\"margin-top\"] = \"N,auto,inherit,\"; // number string\n"
			+ "	_stylemap[\"max-height\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"max-width\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"min-height\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"min-width\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"orphans\"] = \"0\"; // number\n"
			+ "	_stylemap[\"overflow\"] = \",visible,hidden,scroll,auto,\";\n"
			+ "	_stylemap[\"padding\"] = \"\"; // combi\n"
			+ "	_stylemap[\"padding-bottom\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"padding-left\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"padding-right\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"padding-top\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"page-break-after\"] = \",always,avoid,left,right,inherit,auto,\";\n"
			+ "	_stylemap[\"page-break-before\"] = \",always,avoid,left,right,inherit,auto,\";\n"
			+ "	_stylemap[\"page-break-inside\"] = \",avoid,auto,\";\n"
			+ "	_stylemap[\"pause\"] = \"t\"; // time\n"
			+ "	_stylemap[\"pause-after\"] = \"t\"; // time\n"
			+ "	_stylemap[\"pause-before\"] = \"t\"; // time\n"
			+ "	_stylemap[\"pitch\"] = \",x-low,low,medium,high,x-high,\";\n"
			+ "	_stylemap[\"pitch-range\"] = \"0\"; // number\n"
			+ "	_stylemap[\"play-during\"] = \"u,mix,repeat,auto,\"; // url\n"
			+ "	_stylemap[\"position\"] = \",absolute,fixed,relative,static,\";\n"
			+ "	_stylemap[\"quotes\"] = \"d\"; // delete\n"
			+ "	_stylemap[\"richness\"] = \"0\"; // number\n"
			+ "	_stylemap[\"right\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"scrollbar-3dlight-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-arrow-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-base-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-darkshadow-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-face-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-highlight-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-shadow-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"scrollbar-track-color\"] = \"c\"; // color\n"
			+ "	_stylemap[\"speak\"] = \",none,normal,spell-out,\";\n"
			+ "	_stylemap[\"speak-header\"] = \",always,once,\";\n"
			+ "	_stylemap[\"speak-numeral\"] = \",digits,continuous,\";\n"
			+ "	_stylemap[\"speak-punctuation\"] = \",code,none,\";\n"
			+ "	_stylemap[\"speech-rate\"] = \"0,x-slow,slow,slower,medium,faster,fast,x-fase,\"; // number\n"
			+ "	_stylemap[\"stress\"] = \"0\"; // number\n"
			+ "	_stylemap[\"table-layout\"] = \",auto,fixed,\";\n"
			+ "	_stylemap[\"text-align\"] = \",left,center,right,justify,\";\n"
			+ "	_stylemap[\"text-decoration\"] = \",underline,overline,line-through,blink,none,\";\n"
			+ "	_stylemap[\"text-indent\"] = \"N\"; // number string\n"
			+ "	_stylemap[\"text-shadow\"] = \"nc,none,\"; // number string without % or color\n"
			+ "	_stylemap[\"text-transform\"] = \",capitalize,uppercase,lowercase,none,\";\n"
			+ "	_stylemap[\"top\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"vertical-align\"] = \",top,middle,bottom,baseline,sub,super,text-top,text-bottom,\";\n"
			+ "	_stylemap[\"visibility\"] = \",hidden,visible,\";\n"
			+ "	_stylemap[\"voice-family\"] = \",male,female,old,young,child,\";\n"
			+ "	_stylemap[\"volume\"] = \"0,silent,x-soft,soft,medium,loud,x-loud,\"; // number\n"
			+ "	_stylemap[\"white-space\"] = \",normal,pre,nowrap,\";\n"
			+ "	_stylemap[\"widows\"] = \"0\"; // number\n"
			+ "	_stylemap[\"width\"] = \"N,auto,\"; // number string\n"
			+ "	_stylemap[\"word-spacing\"] = \"n\"; // number string without %\n"
			+ "	_stylemap[\"z-index\"] = \"0\"; // number\n"
			+ "\n"
			+ "	_combimap[\"background\"] = \"uNc,scroll,fixed,transparent,top,bottom,center,left,right,repeat,repeat-x,repeat-y,no-repeat,\";\n"
			+ "	_combimap[\"border\"] = \"Nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,separate,collapse,\";\n"
			+ "	_combimap[\"border-bottom\"] = \"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_combimap[\"border-left\"] = \"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_combimap[\"border-right\"] = \"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_combimap[\"border-style\"] = \",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\";\n"
			+ "	_combimap[\"border-top\"] = \"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"; \n"
			+ "	_combimap[\"border-width\"] = \"n\";\n"
			+ "	_combimap[\"font\"] = \"N*,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,italic,oblique,small-caps,bold,bolder,lighter,100,200,300,400,500,600,700,800,900,\";\n"
			+ "	_combimap[\"list-style\"] = \"u,none,inside,outside,decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\";\n"
			+ "	_combimap[\"margin\"] = \"N,auto,inherit,\";\n" + "	_combimap[\"padding\"] = \"N\";";

	private static final Pattern PATTERN_TAG_LINE = Pattern.compile(
			"_map\\[\"(\\p{Alnum}+)\"\\]\\s*=\\s*\"(\\p{Print}+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("([\\p{Alnum}-_]+)(?:\\[(\\p{Print}+?)\\])?");

	/**
	 * Parses specified HTML map; e.g:
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
	 * @param htmlMapStr
	 *            The HTML map string
	 * @return The parsed map
	 */
	private static Map<String, Map<String, Set<String>>> parseHTMLMap(final String htmlMapStr) {
		final Matcher m = PATTERN_TAG_LINE.matcher(htmlMapStr);
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
						valueSet.add(value.toLowerCase(Locale.ENGLISH));
					}
					attribMap.put(attribMatcher.group(1).toLowerCase(Locale.ENGLISH), valueSet);
				}
			}
			tagMap.put(m.group(1).toLowerCase(Locale.ENGLISH), attribMap);
		}
		return tagMap;
	}

	private static final Pattern PATTERN_STYLE_LINE = Pattern.compile(
			"_stylemap\\[\"([\\p{Alnum}-_]+)\"\\]\\s*=\\s*\"(\\p{Print}*)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_VALUE = Pattern.compile("([\\p{Alnum}*-_ &&[^,]]+)");

	private static Map<String, Set<String>> parseStyleMap(final String styleMapStr) {
		/*
		 * Parse the combination map
		 */
		final Map<String, Set<String>> combiMap = parseCombiMap(styleMapStr);
		/*
		 * Parse style map
		 */
		final Matcher m = PATTERN_STYLE_LINE.matcher(styleMapStr);
		final Map<String, Set<String>> styleMap = new HashMap<String, Set<String>>();
		while (m.find()) {
			final String values = m.group(2);
			if (values.length() == 0) {
				/*
				 * Fetch from combination map
				 */
				final String cssElement = m.group(1).toLowerCase(Locale.ENGLISH);
				styleMap.put(cssElement, combiMap.get(cssElement));
			} else {
				/*
				 * Parse values
				 */
				final Matcher valueMatcher = PATTERN_VALUE.matcher(m.group(2));
				final Set<String> valueSet = new HashSet<String>();
				while (valueMatcher.find()) {
					valueSet.add(valueMatcher.group());
				}
				styleMap.put(m.group(1).toLowerCase(Locale.ENGLISH), valueSet);
			}
		}
		return styleMap;
	}

	private static final Pattern PATTERN_COMBI_LINE = Pattern.compile(
			"_combimap\\[\"([\\p{Alnum}-_]+)\"\\]\\s*=\\s*\"(\\p{Print}+)\"", Pattern.CASE_INSENSITIVE);

	/**
	 * Parses specified combination map for CSS elements
	 * 
	 * @param combiMapStr
	 *            The string representation for combination map
	 * @return The parsed map
	 */
	private static Map<String, Set<String>> parseCombiMap(final String combiMapStr) {
		final Matcher m = PATTERN_COMBI_LINE.matcher(combiMapStr);
		final Map<String, Set<String>> combiMap = new HashMap<String, Set<String>>();
		while (m.find()) {
			final Matcher valueMatcher = PATTERN_VALUE.matcher(m.group(2));
			final Set<String> valueSet = new HashSet<String>();
			while (valueMatcher.find()) {
				valueSet.add(valueMatcher.group());
			}
			combiMap.put(m.group(1).toLowerCase(Locale.ENGLISH), valueSet);
		}
		return combiMap;
	}
}
