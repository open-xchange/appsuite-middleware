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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.html.internal.parser.handler;

import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.safety.Whitelist;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.filtering.FilterMaps;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;

/**
 * {@link HTMLFilterHandler} - The HTML white-list filter.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HTMLFilterHandler implements HtmlHandler {

    private static final String COMMENT_END = "-->";

    private static final String COMMENT_START = "<!--";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HTMLFilterHandler.class);

    private static final String CRLF = "\r\n";

    private static final String STYLE = "style";

    private static final String CLASS = "class";

    private static final String ID = "id";

    private static final String HEAD = "head";

    private static final String BODY = "body";

    private static final String META = "meta";

    private static final String SCRIPT = "script";

    private static final String HTTP_EQUIV = "http-equiv";

    private static final Set<String> NUM_ATTRIBS = new HashSet<String>(0);

    // A decimal digit: [0-9]
    private static final Pattern PAT_NUMERIC = Pattern.compile("\\p{Digit}+");

    /*-
     * ----------------- Member stuff -----------------
     */

    private final HtmlService htmlService;

    private final Map<String, Map<String, Set<String>>> htmlMap;

    private final Map<String, Set<String>> styleMap;

    private final StringBuilder htmlBuilder;

    private final StringBuilder attrBuilder;

    /**
     * Used to track all subsequent elements of a tag that ought to be removed completely.
     */
    private int skipLevel;

    private boolean body;

    private boolean withinSuppress;

    /**
     * Used to track all subsequent elements of a tag from which only its tag elements ought to be removed.
     */
    private int depth;

    private boolean[] depthInfo;

    private boolean isCss;

    private final Stringer cssBuffer;

    /**
     * Initializes a new {@link HTMLFilterHandler}.
     *
     * @param capacity The initial capacity
     * @param htmlMap The HTML map
     * @param styleMap The CSS style map
     */
    public HTMLFilterHandler(final HtmlService htmlService, final int capacity, final Map<String, Map<String, Set<String>>> htmlMap, final Map<String, Set<String>> styleMap) {
        super();
        this.htmlService = htmlService;
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        htmlBuilder = new StringBuilder(capacity);
        attrBuilder = new StringBuilder(128);
        this.htmlMap = htmlMap;
        this.styleMap = styleMap;
        checkHTMLMap();
    }

    /**
     * Initializes a new {@link HTMLFilterHandler}.
     *
     * @param capacity The initial capacity
     * @param mapStr The map as string representation
     */
    public HTMLFilterHandler(final HtmlService htmlService, final int capacity, final String mapStr) {
        super();
        this.htmlService = htmlService;
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        htmlBuilder = new StringBuilder(capacity);
        attrBuilder = new StringBuilder(128);
        htmlMap = FilterMaps.parseHTMLMap(mapStr);
        styleMap = FilterMaps.parseStyleMap(mapStr);
        checkHTMLMap();
    }

    /**
     * Initializes a new {@link HTMLFilterHandler} with default white list.
     *
     * @param capacity The initial capacity
     */
    public HTMLFilterHandler(final HtmlService htmlService, final int capacity) {
        super();
        this.htmlService = htmlService;
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        htmlBuilder = new StringBuilder(capacity);
        attrBuilder = new StringBuilder(128);
        htmlMap = FilterMaps.getStaticHTMLMap();
        styleMap = FilterMaps.getStaticStyleMap();
        checkHTMLMap();
    }

    /**
     * Marks current <code>depth</code> position as <code>true</code> and increments <code>depth</code> counter.
     */
    private void mark() {
        if (null == depthInfo) {
            depthInfo = new boolean[8];
        } else {
            ensureCapacity(depth);
        }
        depthInfo[depth++] = true;
    }

    /**
     * Decrements <code>depth</code> counter and then marks its position as <code>false</code>.
     *
     * @return <code>true</code> if position's previous mark was set; otherwise <code>false</code>
     */
    private boolean getAndUnmark() {
        final int index = --depth;
        if (index < 0) {
            return false;
        }
        ensureCapacity(index);
        final boolean retval = depthInfo[index];
        depthInfo[depth] = false;
        return retval;
    }

    /**
     * Ensure capacity of <code>depthInfo</code> array. Double its length as long as specified index does not fit.
     *
     * @param index The index accessing the array
     */
    private void ensureCapacity(final int index) {
        int len = depthInfo.length;
        while (index >= len) {
            len = (len << 1);
        }
        final boolean[] tmp = depthInfo;
        depthInfo = new boolean[len];
        System.arraycopy(tmp, 0, depthInfo, 0, tmp.length);
    }

    /**
     * Gets a JSoup white-list according to <tt>whitelist.properties</tt> file.
     *
     * @return A JSoup white-list according to <tt>whitelist.properties</tt> file
     */
    public static Whitelist getJSoupWhitelist() {
        final Map<String, Map<String, Set<String>>> htmlMap = FilterMaps.getStaticHTMLMap();
        final Whitelist whitelist = new Whitelist();
        final Set<Entry<String, Map<String, Set<String>>>> entrySet = htmlMap.entrySet();
        for (final Entry<String, Map<String, Set<String>>> entry : entrySet) {
            final String tagName = entry.getKey();
            whitelist.addTags(tagName);
            final Map<String, Set<String>> attrsMap = entry.getValue();
            if (null != attrsMap) {
                whitelist.addAttributes(tagName, attrsMap.keySet().toArray(new String[0]));
            }
        }
        return whitelist;
    }

    private void checkHTMLMap() {
        if (!htmlMap.containsKey(HEAD)) {
            htmlMap.put(HEAD, null);
        }
    }

    @Override
    public void handleXMLDeclaration(final String version, final Boolean standalone, final String encoding) {
        if (null != version) {
            htmlBuilder.append("<?xml version=\"").append(version).append('"');
            if (null != standalone) {
                htmlBuilder.append(" standalone=\"").append(Boolean.TRUE.equals(standalone) ? "yes" : "no").append('"');
            }
            if (null != encoding) {
                htmlBuilder.append(" encoding=\"").append(encoding).append('"');
            }
            htmlBuilder.append("?>").append(CRLF);
        }
    }

    @Override
    public void handleComment(final String comment) {
        htmlBuilder.append(COMMENT_START).append(comment).append(COMMENT_END);
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        htmlBuilder.append("<!DOCTYPE").append(docDecl).append('>');
    }

    @Override
    public void handleEndTag(final String sTag) {
        final String tag = sTag.toLowerCase(Locale.US);
        if (skipLevel == 0) {
            if (body && BODY.equals(tag)) {
                body = false;
            } else if (isCss && STYLE.equals(tag)) {
                isCss = false;
            }
            if (depth == 0) {
                htmlBuilder.append("</").append(tag).append('>');
            } else if (!getAndUnmark()) {
                htmlBuilder.append("</").append(tag).append('>');
            }
        } else {
            skipLevel--;
        }
    }

    @Override
    public void handleError(final String errorMsg) {
        LOG.error(errorMsg);
    }

    @Override
    public void handleSimpleTag(final String tag, final Map<String, String> attributes) {
        if (skipLevel > 0) {
            return;
        }
        if (htmlMap.containsKey(tag)) {
            addStartTag(tag, attributes, true, htmlMap.get(tag));
        }
    }

    @Override
    public void handleStartTag(final String sTag, final Map<String, String> attributes) {
        if (skipLevel > 0) {
            skipLevel++;
            return;
        }
        final String tag = sTag.toLowerCase(Locale.US);
        if (htmlMap.containsKey(tag)) {
            if (depth > 0) {
                depth++;
            }
            if (BODY.equals(tag)) {
                body = true;
            } else if (STYLE.equals(tag)) {
                isCss = true;
            }
            addStartTag(tag, attributes, false, htmlMap.get(tag));
        } else {
            if (!body || isRemoveWholeTag(tag)) {
                /*
                 * Remove whole tag incl. subsequent content and tags
                 */
                skipLevel++;
            } else {
                /*
                 * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                 */
                mark();
            }
        }
    }

    private boolean isRemoveWholeTag(final String tag) {
        final String check = tag.toLowerCase(Locale.US);
        return (SCRIPT.equals(check) || check.startsWith("w:worddocument") || check.startsWith("o:officedocumentsettings"));
    }

    @Override
    public void handleCDATA(final String text) {
        if (skipLevel == 0) {
            htmlBuilder.append("<![CDATA[");
            if (isCss) {
                /*
                 * Handle style attribute
                 */
                checkCSS(cssBuffer.append(text), styleMap, true, true);
                htmlBuilder.append(cssBuffer.toString());
                cssBuffer.setLength(0);
            } else {
                htmlBuilder.append(text);
            }
            htmlBuilder.append("]]>");
        }
    }

    @Override
    public void handleText(final String text, final boolean ignorable) {
        if (skipLevel == 0) {
            if (isCss) {
                if (ignorable) {
                    htmlBuilder.append(text);
                } else {
                    /*
                     * Handle style attribute
                     */
                    checkCSS(cssBuffer.append(text), styleMap, true, true);
                    htmlBuilder.append(cssBuffer.toString());
                    cssBuffer.setLength(0);
                }
            } else {
                htmlBuilder.append(text);
            }
        }
    }

    private static final String VAL_START = "=\"";

    /**
     * Adds tag occurring in white list to HTML result.
     *
     * @param tag The tag to add
     * @param a The tag's attribute set
     * @param simple <code>true</code> to write a simple tag; otherwise <code>false</code>
     * @param attribs The allowed tag's attributes or <code>null</code> to allow all
     */
    private void addStartTag(final String tag, final Map<String, String> a, final boolean simple, final Map<String, Set<String>> attribs) {
        attrBuilder.setLength(0);
        if (simple && META.equals(tag) && a.containsKey(HTTP_EQUIV) && attribs.containsKey(HTTP_EQUIV)) {
            /*
             * Special handling for allowed meta tag which provides an allowed HTTP header indicated through 'http-equiv' attribute
             */
            for (final Entry<String, String> e : a.entrySet()) {
                attrBuilder.append(' ').append(e.getKey()).append(VAL_START).append(e.getValue()).append('"');
            }
            htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append('/').append('>');
            return;
        }
        for (final Entry<String, String> e : a.entrySet()) {
            final String attr = e.getKey().toLowerCase(Locale.US);
            final String val = e.getValue();
            if (STYLE.equals(attr)) {
                /*
                 * Handle style attribute
                 */
                checkCSS(cssBuffer.append(val), styleMap, true);
                final String checkedCSS = cssBuffer.toString();
                cssBuffer.setLength(0);
                if (containsCSSElement(checkedCSS)) {
                    if (checkedCSS.indexOf('"') == -1) {
                        attrBuilder.append(' ').append(STYLE).append(VAL_START).append(checkedCSS).append('"');
                    } else {
                        attrBuilder.append(' ').append(STYLE).append("='").append(checkedCSS).append('\'');
                    }
                }
            } else if (CLASS.equals(attr) || ID.equals(attr)) {
                /*
                 * TODO: Is it safe to allow "class"/"id" attribute in any case
                 */
                attrBuilder.append(' ').append(attr).append(VAL_START).append(htmlService.htmlFormat(val, false)).append('"');
            } else {
                if (null == attribs) {
                    if (isNonJavaScriptURL(val)) {
                        attrBuilder.append(' ').append(attr).append(VAL_START).append(htmlService.htmlFormat(val, false)).append('"');
                    }
                } else {
                    if (attribs.containsKey(attr)) {
                        final Set<String> allowedValues = attribs.get(attr);
                        if (null == allowedValues || allowedValues.contains(val.toLowerCase(Locale.US))) {
                            if (isNonJavaScriptURL(val)) {
                                attrBuilder.append(' ').append(attr).append(VAL_START).append(htmlService.htmlFormat(val, false)).append('"');
                            }
                        } else if (NUM_ATTRIBS == allowedValues) {
                            /*
                             * Only numeric attribute value allowed
                             */
                            if (PAT_NUMERIC.matcher(val.trim()).matches()) {
                                attrBuilder.append(' ').append(attr).append(VAL_START).append(val).append('"');
                            }
                        }
                    }
                }
            }
        }
        htmlBuilder.append('<').append(tag).append(attrBuilder.toString());
        if (simple) {
            htmlBuilder.append('/');
        }
        htmlBuilder.append('>');
    }

    private static boolean isNonJavaScriptURL(final String val) {
        if (null == val) {
            return false;
        }
        final String lc = val.trim().toLowerCase(Locale.US);
        return !lc.startsWith("javascript:") && !lc.startsWith("vbscript:");
    }

    /**
     * Gets the filtered HTML content.
     *
     * @return The filtered HTML content
     */
    public String getHTML() {
        return htmlBuilder.toString();
    }

}
