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

package com.openexchange.html.internal.jericho.handler;

import static com.openexchange.html.HtmlServices.isNonJavaScriptURL;
import static com.openexchange.html.HtmlServices.isSafe;
import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL;
import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL_SOLE;
import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.css.CSSMatcher;
import com.openexchange.html.internal.jericho.JerichoHandler;
import com.openexchange.html.internal.parser.handler.HTMLFilterHandler;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.Streams;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;

/**
 * {@link FilterJerichoHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FilterJerichoHandler implements JerichoHandler {

    private static final CellPadding CELLPADDING_EMPTY = new CellPadding(null);

    protected static class CellPadding {

        final String cellPadding;

        CellPadding(String cellPadding) {
            super();
            this.cellPadding = cellPadding;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    private final static char[] IMMUNE_HTMLATTR = { ',', '.', '-', '_', '/', ';', '=', ' ' };

    private static final Set<String> NUM_ATTRIBS = new HashSet<String>(0);

    private static volatile Map<String, Map<String, Set<String>>> staticHTMLMap;

    private static volatile Map<String, Set<String>> staticStyleMap;

    // A decimal digit: [0-9]
    private static final Pattern PAT_NUMERIC = Pattern.compile("\\p{Digit}+");

    private static final Map<String, Set<String>> IMAGE_STYLE_MAP;

    private static final Set<String> ALL;

    private static final Set<String> SINGLE_TAGS;

    static {
        IMAGE_STYLE_MAP = new HashMap<String, Set<String>>();
        Set<String> values = new HashSet<String>();
        /*
         * background
         */
        values.add("iNc");
        values.add("scroll");
        values.add("fixed");
        values.add("transparent");
        values.add("top");
        values.add("bottom");
        values.add("center");
        values.add("left");
        values.add("right");
        values.add("repeat");
        values.add("repeat-x");
        values.add("repeat-y");
        values.add("no-repeat");
        IMAGE_STYLE_MAP.put("background", values);
        values = new HashSet<String>();
        /*
         * background-image
         */
        values.add("i"); // Only "cid:" URLs
        IMAGE_STYLE_MAP.put("background-image", values);
        /*
         * ALL tags
         */
        Set<String> s = new HashSet<String>(HTMLElements.getElementNames());
        s.add("smarttagtype");
        ALL = Collections.unmodifiableSet(s);
        /*
         * Single tags
         */
        s = new HashSet<String>();
        s.add("wbr");
        s.add("time");
        SINGLE_TAGS = Collections.unmodifiableSet(s);
    }

    /**
     * Gets the static HTML map.
     *
     * @return The HTML map
     */
    public static Map<String, Map<String, Set<String>>> getStaticHTMLMap() {
        return staticHTMLMap;
    }

    /**
     * Gets the static CSS map.
     *
     * @return The CSS map
     */
    public static Map<String, Set<String>> getStaticStyleMap() {
        return staticStyleMap;
    }

    /**
     * Gets the image CSS map.
     *
     * @return The image CSS map
     */
    public static Map<String, Set<String>> getImageStyleMap() {
        return IMAGE_STYLE_MAP;
    }

    /*-
     * Member stuff
     */
    private final Map<String, Map<String, Set<String>>> htmlMap;
    private final Map<String, Set<String>> styleMap;

    private final StringBuilder htmlBuilder;
    private final StringBuilder attrBuilder;
    private final StringBuilder urlBuilder;

    private final HtmlServiceImpl htmlService;

    private boolean body;
    private boolean isCss;
    private final Stringer cssBuffer;

    /**
     * Used to track all subsequent elements of a tag that ought to be removed completely.
     */
    private int skipLevel;

    /**
     * Used to track all subsequent elements of a tag from which only its tag elements ought to be removed.
     */
    private int depth;

    /**
     * The max. content size (-1 or >=10000)
     */
    private int maxContentSize;

    private boolean maxContentSizeExceeded;

    private boolean[] depthInfo;
    private boolean dropExternalImages;
    private boolean imageURLFound;
    private boolean replaceUrls = true;
    private String cssPrefix;
    private final LinkedList<CellPadding> tablePaddings;

    private final boolean changed = false;

    /**
     * Initializes a new {@link FilterJerichoHandler}.
     */
    public FilterJerichoHandler(final int capacity, final HtmlServiceImpl htmlService) {
        super();
        tablePaddings = new LinkedList<FilterJerichoHandler.CellPadding>();
        this.htmlService = htmlService;
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlBuilder = new StringBuilder(capacity);
        this.attrBuilder = new StringBuilder(128);
        if (null == staticHTMLMap) {
            loadWhitelist();
        }
        this.htmlMap = staticHTMLMap;
        this.styleMap = staticStyleMap;
    }

    /**
     * Initializes a new {@link FilterJerichoHandler}.
     */
    public FilterJerichoHandler(final int capacity, final String mapStr, final HtmlServiceImpl htmlService) {
        super();
        tablePaddings = new LinkedList<FilterJerichoHandler.CellPadding>();
        this.htmlService = htmlService;
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlBuilder = new StringBuilder(capacity);
        this.attrBuilder = new StringBuilder(128);
        final Map<String, Map<String, Set<String>>> map = parseHTMLMap(mapStr);
        if (!map.containsKey("html")) {
            map.put("html", null);
        }
        if (!map.containsKey("head")) {
            map.put("head", null);
        }
        if (!map.containsKey("body")) {
            map.put("body", null);
        }
        for (final String tagName : SINGLE_TAGS) {
            if (!map.containsKey(tagName)) {
                map.put(tagName, null);
            }
        }
        htmlMap = Collections.unmodifiableMap(map);
        styleMap = Collections.unmodifiableMap(parseStyleMap(mapStr));
    }

    /**
     * Sets the max. content size. &lt;= <code>0</code> means unlimited, &lt; <code>10000</code> will be set to <code>10000</code>.
     *
     * @param maxContentSize The max. content size to set
     * @return This handler with new behavior applied
     */
    public FilterJerichoHandler setMaxContentSize(final int maxContentSize) {
        if ((maxContentSize >= 10000) || (maxContentSize <= 0)) {
            this.maxContentSize = maxContentSize;
        } else {
            this.maxContentSize = 10000;
        }

        this.maxContentSizeExceeded = false;
        return this;
    }

    /**
     * Sets the CSS prefix
     *
     * @param cssPrefix The CSS prefix to set
     * @return This handler with new behavior applied
     */
    public FilterJerichoHandler setCssPrefix(final String cssPrefix) {
        this.cssPrefix = cssPrefix;
        return this;
    }

    /**
     * Returns if maxContentSize is exceeded
     *
     * @return true, if exceeded, otherwise false
     */
    public boolean isMaxContentSizeExceeded() {
        return maxContentSizeExceeded;
    }

    /**
     * Sets whether to replace URLs.
     *
     * @param replaceUrls <code>true</code> to replace URLs; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public FilterJerichoHandler setReplaceUrls(final boolean replaceUrls) {
        this.replaceUrls = replaceUrls;
        return this;
    }

    /**
     * Sets whether to replace image URLs.
     *
     * @param dropExternalImages <code>true</code> to replace image URLs; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public FilterJerichoHandler setDropExternalImages(final boolean dropExternalImages) {
        this.dropExternalImages = dropExternalImages;
        return this;
    }

    /**
     * Checks whether an image URL was replaced.
     *
     * @return <code>true</code> if an image URL was replaced; otherwise <code>false</code>
     */
    public boolean isImageURLFound() {
        return imageURLFound;
    }

    /**
     * Checks whether changes were performed.
     * <p>
     * TODO: Implement appropriately! Not yet ready!
     *
     * @return <code>true</code> if changes were performed; otherwise <code>false</code>
     * @deprecated Implement appropriately! Not yet ready!
     */
    @Deprecated
    public boolean isChanged() {
        return changed;
    }

    /**
     * Gets the filtered HTML content.
     *
     * @return The filtered HTML content
     */
    public String getHTML() {
        return htmlBuilder.toString();
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
     * Checks if max. allowed content size is exceeded.
     *
     * @param addLen The expected length the content will grow
     * @return <code>true</code> if not exceeded; otherwise <code>false</code> if exceeded
     */
    private boolean checkMaxContentSize(final int addLen) {
        if (maxContentSize <= 0) {
            return true;
        }
        if (maxContentSizeExceeded) {
            return false;
        }

        maxContentSizeExceeded = htmlBuilder.length() + addLen > maxContentSize;
        return !maxContentSizeExceeded;
    }

    @Override
    public void markBodyAbsent() {
        // If there is no body tag, assume parsing starts in body
        body = true;
    }

    @Override
    public void handleUnknownTag(final Tag tag) {
        if (!body) {
            htmlBuilder.append(tag);
        }
    }

    @Override
    public void handleCharacterReference(final CharacterReference characterReference) {
        if (skipLevel == 0) {
            final String str = CharacterReference.getDecimalCharacterReferenceString(characterReference.getChar());
            if (checkMaxContentSize(str.length())) {
                htmlBuilder.append(str);
            }
        }
    }

    @Override
    public void handleSegment(final CharSequence content) {
        if (skipLevel == 0) {
            if (isCss) {
                if (false == maxContentSizeExceeded) {
                    /*
                     * Handle style attribute
                     */
                    checkCSS(cssBuffer.append(content), styleMap, cssPrefix);
                    String checkedCSS = cssBuffer.toString();
                    cssBuffer.setLength(0);
                    if (dropExternalImages) {
                        imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, null, false);
                        checkedCSS = cssBuffer.toString();
                        cssBuffer.setLength(0);
                    }
                    if (checkMaxContentSize(checkedCSS.length())) {
                        htmlBuilder.append(checkedCSS);
                    }
                }
            } else {
                if (checkMaxContentSize(content.length())) {
                    //    if (content instanceof Segment ? ((Segment) content).isWhiteSpace() : isWhiteSpace(content)) {
                    //        htmlBuilder.append(content);
                    //    } else {
                    //        /*-
                    //         * Should we re-encode prior to appending?
                    //         * E.g. "<" ==> "&lt;"
                    //         *
                    //         * htmlBuilder.append(CharacterReference.reencode(content));
                    //         */
                    //        htmlBuilder.append(content);
                    //    }

                    htmlBuilder.append(content);

                }
            }
        }
    }

    /**
     * Indicates whether this segment consists entirely of white spaces.
     */
    private boolean isWhiteSpace(CharSequence content) {
        for (int i = content.length(); i-- > 0;) {
            if (!Segment.isWhiteSpace(content.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleEndTag(final EndTag endTag) {
        if (skipLevel == 0) {
            final String name = endTag.getName();
            if (body && HTMLElementName.BODY == name) {
                body = false;
            } else if (isCss && HTMLElementName.STYLE == name) {
                isCss = false;
            } else if (HTMLElementName.TABLE == name) {
                tablePaddings.poll();
            }
            if (depth == 0) {
                htmlBuilder.append("</").append(name).append('>');
            } else if (!getAndUnmark()) {
                htmlBuilder.append("</").append(name).append('>');
            }
        } else {
            skipLevel--;
        }
    }

    @Override
    public void handleStartTag(final StartTag startTag) {
        if (maxContentSizeExceeded) {
            // Do not append more elements
            return;
        }
        final String tagName = startTag.getName();
        if (startTag.isEndTagForbidden() || SINGLE_TAGS.contains(tagName)) {
            // Simple tag
            if (skipLevel > 0) {
                return;
            }
            if (htmlMap.containsKey(tagName)) {
                addStartTag(startTag, true, htmlMap.get(tagName));
            }
        } else {
            if (skipLevel > 0) {
                if (HTMLElementName.BODY != tagName) {
                    skipLevel++;
                    return;
                }
                skipLevel = 0;
            }
            if (htmlMap.containsKey(tagName)) {
                if (depth > 0) {
                    depth++;
                }
                if (HTMLElementName.BODY == tagName) {
                    body = true;
                } else if (HTMLElementName.STYLE == tagName) {
                    isCss = true;
                }
                addStartTag(startTag, false, htmlMap.get(tagName));
            } else {
                if (!body) {
                    /*
                     * Remove whole tag incl. subsequent content and tags
                     */
                    skipLevel++;
                } else if (isMSTag(startTag)) {
                    /*
                     * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                     */
                    if (startTag.isSyntacticalEmptyElementTag()) {
                        // Swallow
                    } else {
                        mark();
                    }
                } else if (isRemoveWholeTag(startTag)) {
                    /*
                     * Remove whole tag incl. subsequent content and tags
                     */
                    skipLevel++;
                } else {
                    /*
                     * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                     */
                    if (startTag.isSyntacticalEmptyElementTag()) {
                        // Swallow
                    } else {
                        mark();
                    }
                }
            }
        }
    }

    private static boolean isMSTag(final Tag tag) {
        final String check = tag.getName();
        final char c;
        if (check.length() < 2 || ':' != check.charAt(1) || (('w' != (c = check.charAt(0))) && ('W' != c) && ('o' != c) && ('O' != c))) {
            return false;
        }
        return ALL.contains(check.substring(2));
    }

    private static boolean isRemoveWholeTag(final Tag tag) {
        final String check = tag.getName();
        return (HTMLElementName.SCRIPT == check || check.startsWith("w:") || check.startsWith("o:"));
    }

    private static final Pattern PATTERN_STYLE_VALUE = Pattern.compile("([\\p{Alnum}-_]+)\\s*:\\s*([\\p{Print}\\p{L}&&[^;]]+);?", Pattern.CASE_INSENSITIVE);

    /**
     * Adds tag occurring in white list to HTML result.
     *
     * @param startTag The start tag to add
     * @param simple <code>true</code> to write a simple tag; otherwise <code>false</code>
     * @param allowedAttributes The allowed tag's attributes or <code>null</code> to allow all
     */
    private void addStartTag(final StartTag startTag, final boolean simple, final Map<String, Set<String>> allowedAttributes) {
        attrBuilder.setLength(0);
        String tagName = startTag.getName();

        if (simple && HTMLElementName.META == tagName && allowedAttributes.containsKey("http-equiv")) {
            Attributes attributes = startTag.getAttributes();
            if (null != attributes.get("http-equiv")) {
                /*
                 * Special handling for allowed meta tag which provides an allowed HTTP header indicated through 'http-equiv' attribute
                 */
                for (final Attribute attribute : attributes) {
                    final String val = attribute.getValue();
                    if (isNonJavaScriptURL(val, "url=")) {
                        attrBuilder.append(' ').append(attribute.getName()).append("=\"").append(htmlService.encodeForHTMLAttribute(IMMUNE_HTMLATTR, val)).append('"');
                    } else {
                        attrBuilder.setLength(0);
                        break;
                    }
                }
                if (attrBuilder.length() > 0) {
                    htmlBuilder.append('<').append(tagName).append(attrBuilder.toString()).append('>');
                }
                return;
            }
        }

        // Handle start tag
        Map<String, String> attrMap = createMapFrom(startTag.getAttributes());
        if (HTMLElementName.IMG == tagName) {
            String width = attrMap.get("width");
            if (null != width) {
                String height = attrMap.get("height");
                if (null != height) {
                    prependToStyle(mapFor("width", width + "px", "height", height + "px"), attrMap);
                }
            }
        } else if (HTMLElementName.TABLE == tagName) {
            addTableTag(attrMap);
        } else if (HTMLElementName.TD == tagName || HTMLElementName.TH == tagName) {
            CellPadding cellPadding = tablePaddings.peek();
            if (CELLPADDING_EMPTY != cellPadding && null != cellPadding) {
                String style = attrMap.get("style");
                if (null == style || style.indexOf("padding") < 0) {
                    prependToStyle("padding: " + cellPadding.cellPadding + "px;", attrMap);
                }
            }
        }

        List<Attribute> uriAttributes = replaceUrls ? startTag.getURIAttributes() : Collections.<Attribute> emptyList();
        for (Map.Entry<String, String> attribute : attrMap.entrySet()) {
            String attr = attribute.getKey();
            if ("style".equals(attr)) {
                /*
                 * Handle style attribute
                 */
                String css = attribute.getValue();
                if (!Strings.isEmpty(css)) {
                    checkCSS(cssBuffer.append(css), styleMap, true);
                    css = cssBuffer.toString();
                    cssBuffer.setLength(0);
                    if (dropExternalImages) {
                        imageURLFound |= checkCSS(cssBuffer.append(css), IMAGE_STYLE_MAP, true, false);
                        css = cssBuffer.toString();
                        cssBuffer.setLength(0);
                    }
                }
                if (containsCSSElement(css)) {
                    if (css.indexOf('"') == -1) {
                        attrBuilder.append(' ').append("style").append("=\"").append(css).append('"');
                    } else {
                        attrBuilder.append(' ').append("style").append("='").append(css).append('\'');
                    }
                }
            } else if ("class".equals(attr) || "id".equals(attr)) {
                final String value = prefixBlock(CharacterReference.encode(attribute.getValue()), cssPrefix);
                attrBuilder.append(' ').append(attr).append("=\"").append(value).append('"');
            } else {
                final String val = attribute.getValue();
                if (null == allowedAttributes) { // No restrictions
                    if (isSafe(val)) {
                        if (dropExternalImages && "background".equals(attr) && PATTERN_URL.matcher(val).matches()) {
                            attrBuilder.append(' ').append(attr).append("=\"\"");
                            imageURLFound = true;
                        } else if (dropExternalImages && (HTMLElementName.IMG == tagName || HTMLElementName.INPUT == tagName) && "src".equals(attr)) {
                            if (isInlineImage(val)) {
                                // Allow inline images
                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                            } else {
                                attrBuilder.append(' ').append(attr).append("=\"\" data-original-src=\"").append(CharacterReference.encode(val)).append('"');
                                imageURLFound = true;
                                // return;
                            }
                        } else {
                            if (replaceUrls && uriAttributes.contains(attribute)) {
                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(checkPossibleURL(val))).append('"');
                            } else {
                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                            }
                        }
                    }
                } else {
                    if (allowedAttributes.containsKey(attr)) {
                        if (null == val) {
                            attrBuilder.append(' ').append(attr);
                        } else {
                            final Set<String> allowedValues = allowedAttributes.get(attr);
                            if (null == allowedValues || allowedValues.contains(toLowerCase(val))) {
                                if (isSafe(val)) {
                                    if (dropExternalImages && "background".equals(attr) && PATTERN_URL.matcher(val).matches()) {
                                        attrBuilder.append(' ').append(attr).append("=\"\"");
                                        imageURLFound = true;
                                    } else if (dropExternalImages && (HTMLElementName.IMG == tagName || HTMLElementName.INPUT == tagName) && "src".equals(attr)) {
                                        if (isInlineImage(val)) {
                                            // Allow inline images
                                            attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                                        } else {
                                            attrBuilder.append(' ').append(attr).append("=\"\" data-original-src=\"").append(CharacterReference.encode(val)).append('"');
                                            imageURLFound = true;
                                            // return;
                                        }
                                    } else {
                                        if (replaceUrls && uriAttributes.contains(attribute)) {
                                            attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(checkPossibleURL(val))).append('"');
                                        } else {
                                            attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                                        }
                                    }
                                }
                            } else if (NUM_ATTRIBS == allowedValues) {
                                /*
                                 * Only numeric attribute value allowed
                                 */
                                if (PAT_NUMERIC.matcher(val.trim()).matches()) {
                                    attrBuilder.append(' ').append(attr).append("=\"").append(val).append('"');
                                }
                            }
                        }
                    }
                } // End of else
            }
        }

        htmlBuilder.append('<').append(tagName).append(attrBuilder.toString());
        if (startTag.isEmptyElementTag() && !startTag.isEndTagForbidden()) {
            htmlBuilder.append('/');
        }
        htmlBuilder.append('>');
    }

    /**
     * Prepares the environment and output based on the given table tag.
     *
     * @param attrMap
     */
    private void addTableTag(Map<String, String> attrMap) {
        // Has attribute "bgcolor"
        String bgcolor = attrMap.get("bgcolor");
        if (null != bgcolor) {
            prependToStyle("background-color: " + bgcolor + ';', attrMap);
        }

        handleTableCellpaddingAttribute(attrMap);
    }

    /**
     * Handles 'cellpadding' attribute for the given table start tag to be able to add its value to nested tags.<br>
     * <br>
     * If 'cellpadding' attribute is available, add given 'cellpadding' (-value) to table stack. If 'cellpadding' attribute is not available have a look if 'padding' is nested in 'style' attribute of the table. I so add 'padding' to nested tags.
     *
     * @param attrMap - map containing attributes of the 'table' tag
     */
    protected void handleTableCellpaddingAttribute(Map<String, String> attrMap) {
        if (attrMap == null) {
            return;
        }

        // Has attribute "cellpadding"?
        String cellpadding = attrMap.get("cellpadding");
        if (null == cellpadding) {
            String style = attrMap.get("style");
            if (null == style || style.indexOf("padding") < 0) {
                tablePaddings.addFirst(CELLPADDING_EMPTY);
            } else {
                boolean found = false;
                final Matcher m = PATTERN_STYLE_VALUE.matcher(InterruptibleCharSequence.valueOf(style));
                while (!found && m.find()) {
                    final String elementName = m.group(1);
                    if ((null != elementName) && (elementName.equalsIgnoreCase("padding"))) {
                        final String elementValue = m.group(2);

                        tablePaddings.addFirst(new CellPadding(elementValue));
                        found = true;
                    }
                }
            }
        } else {
            if ("0".equals(attrMap.get("cellspacing"))) {
                prependToStyle("border-collapse: collapse;", attrMap);
            }

            // Table has cell padding -> Remember it for all child elements
            tablePaddings.addFirst(new CellPadding(cellpadding));
        }
    }

    private void prependToStyle(String stylePrefix, Map<String, String> attrMap) {
        String style = attrMap.get("style");
        if (null == style) {
            style = stylePrefix;
        } else {
            style = stylePrefix + " " + style;
        }
        attrMap.put("style", style);
    }

    private void prependToStyle(Map<String, String> styleNvps, Map<String, String> attrMap) {
        String style = attrMap.get("style");
        if (null == style) {
            style = generateStyleString(styleNvps);
        } else {
            // Filter out existing entries
            Map<String, Set<String>> styleMap = new HashMap<String, Set<String>>(this.styleMap);
            styleMap.keySet().removeAll(styleNvps.keySet());
            CSSMatcher.checkCSSElements(cssBuffer.append(style), styleMap, true);
            style = cssBuffer.toString();
            cssBuffer.setLength(0);
            style = generateStyleString(styleNvps) + " " + style;
        }
        attrMap.put("style", style);
    }

    private String generateStyleString(Map<String, String> styleNvps) {
        if (null == styleNvps || styleNvps.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder(styleNvps.size() << 2);
        boolean first = true;
        for (Map.Entry<String, String> nvp : styleNvps.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(nvp.getKey()).append(": ").append(nvp.getValue()).append(';');
        }
        return sb.toString();
    }

    private Map<String, String> createMapFrom(List<Attribute> attributes) {
        Map<String, String> map = new LinkedHashMap<String, String>(attributes.size());
        for (Attribute attribute : attributes) {
            map.put(attribute.getKey(), attribute.getValue());
        }
        return map;
    }

    private Map<String, String> mapFor(String... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Map<String, String> map = new LinkedHashMap<String, String>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    // --------------------------------- Image check --------------------------------------- //

    private static final String CID = "cid:";
    private static final String DATA_BASE64 = "data:;base64,";
    private static final Pattern PATTERN_FILENAME = Pattern.compile("([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)");

    private boolean isInlineImage(final String val) {
        final String tmp = toLowerCase(val);
        return tmp.startsWith(CID) || tmp.startsWith(DATA_BASE64) || PATTERN_FILENAME.matcher(tmp).matches();
    }

    // ----------------------------------------------------------------------------------- //

    private static final Pattern SPLIT_WORDS = Pattern.compile(" +");

    private static String prefixBlock(final String value, final String cssPrefix) {
        if (com.openexchange.java.Strings.isEmpty(value) || com.openexchange.java.Strings.isEmpty(cssPrefix)) {
            return value;
        }
        final int length = value.length();
        int pos = 0;
        while (pos < length && Strings.isWhitespace(value.charAt(pos))) {
            pos++;
        }
        final StringBuilder builder = new StringBuilder(length << 1);
        if (pos > 0) {
            builder.append(value.substring(0, pos));
        }
        for (final String word : SPLIT_WORDS.split(value.substring(pos), 0)) {
            final char first = word.charAt(0);
            if ('.' == first) {
                builder.append('.').append(cssPrefix).append('-').append(replaceDots(word.substring(1), cssPrefix)).append(' ');
            } else if ('#' == first) {
                if (word.indexOf('.') < 0) { // contains no dots
                    builder.append('#').append(cssPrefix).append('-').append(replaceDots(word.substring(1), cssPrefix)).append(' ');
                } else {
                    builder.append('#').append(cssPrefix).append('-').append(replaceDots(word.substring(1), cssPrefix)).append(' ');
                }
            } else {
                builder.append(cssPrefix).append('-').append(replaceDots(word, cssPrefix)).append(' ');
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private static final Pattern DOT = Pattern.compile("\\.");

    private static String replaceDots(final String word, final String cssPrefix) {
        return DOT.matcher(word).replaceAll('.' + cssPrefix + '-');
    }

    private String checkPossibleURL(final String val) {
        final Matcher m = PATTERN_URL_SOLE.matcher(val);
        if (!m.matches()) {
            return val;
        }
        urlBuilder.setLength(0);
        urlBuilder.append(val.substring(0, m.start()));
        //replaceURL(urlDecode(m.group()), urlBuilder);
        replaceURL(m.group(), urlBuilder);
        urlBuilder.append(val.substring(m.end()));
        return urlBuilder.toString();
    }

    private static void replaceURL(final String url, final StringBuilder builder) {
        /*
         * Contains any non-ascii character in host part?
         */
        final int restoreLen = builder.length();
        try {
            builder.append(HtmlServiceImpl.checkURL(url));
        } catch (final MalformedURLException e) {
            /*
             * Not a valid URL
             */
            builder.setLength(restoreLen);
            builder.append(url);
        } catch (final Exception e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HTMLURLReplacerHandler.class);
            log.warn("URL replacement failed.", e);
            builder.setLength(restoreLen);
            builder.append(url);
        }
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        htmlBuilder.append(docDecl);
    }

    @Override
    public void handleCData(final String cdata) {
        if (skipLevel == 0) {
            if (isCss) {
                if (false == maxContentSizeExceeded) {
                    /*
                     * Handle style attribute
                     */
                    checkCSS(cssBuffer.append(cdata), styleMap, cssPrefix);
                    String checkedCSS = cssBuffer.toString();
                    cssBuffer.setLength(0);
                    if (dropExternalImages) {
                        imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, null, false);
                        // imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, true, false);
                        checkedCSS = cssBuffer.toString();
                        cssBuffer.setLength(0);
                    }
                    if (checkMaxContentSize(checkedCSS.length())) {
                        htmlBuilder.append(checkedCSS);
                    }
                }
            } else {
                if (checkMaxContentSize(cdata.length())) {
                    htmlBuilder.append(cdata);
                }
            }
        }
    }

    @Override
    public void handleComment(final String comment) {
        if (isCss) {
            if (false == maxContentSizeExceeded) {
                String cleanCss = removeSurroundingHTMLComments(comment);
                checkCSS(cssBuffer.append(cleanCss), styleMap, cssPrefix);
                String checkedCSS = "\n<!--\n" + cssBuffer.toString() + "\n-->\n";
                cssBuffer.setLength(0);
                if (dropExternalImages) {
                    imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, null, false);
                    checkedCSS = cssBuffer.toString();
                    cssBuffer.setLength(0);
                }
                if (checkMaxContentSize(checkedCSS.length())) {
                    htmlBuilder.append(checkedCSS);
                }
            }
        } else {
            if (checkMaxContentSize(comment.length())) {
                htmlBuilder.append(comment);
            }
        }
    }

    /*-
     * ########################## HELPERS #######################################
     */

    private String removeSurroundingHTMLComments(String css) {
        String retval = css;
        if (css.startsWith("<!--") && css.endsWith("-->")) {
            retval = css.substring(4, css.length() - 3);
        }
        return retval;
    }

    private static final byte[] DEFAULT_WHITELIST = String
        .valueOf(
            "# HTML tags and attributes\n"
                + "\n"
                + "html.tag.a=\",href,name,tabindex,target,type,\"\n"
                + "html.tag.area=\",alt,coords,href,nohref[nohref],shape[:rect:circle:poly:default:],tabindex,target,\"\n"
                + "html.tag.b=\"\"\n"
                + "html.tag.basefont=\",color,face,size,\"\n"
                + "html.tag.bdo=\",dir[:ltr:rtl:]\"\n"
                + "html.tag.blockquote=\",type,\"\n"
                + "html.tag.body=\",alink,background,bgcolor,link,text,vlink,\"\n"
                + "html.tag.br=\",clear[:left:right:all:none:]\"\n"
                + "html.tag.button=\",disabled[disabled],name,tabindex,type[:button:submit:reset:],value,\"\n"
                + "html.tag.caption=\",align[:top:bottom:left:right:]\"\n"
                + "html.tag.col=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.colgroup=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.del=\",datetime,\"\n"
                + "html.tag.dir=\",compact[compact]\"\n"
                + "html.tag.div=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.dl=\",compact[compact]\"\n"
                + "html.tag.em=\"\"\n"
                + "html.tag.font=\",color,face,size,\"\n"
                + "html.tag.form=\",action,accept,accept-charset,enctype,method[:get:post:],name,target,\"\n"
                + "html.tag.h1=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h2=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h3=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h4=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h5=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h6=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.hr=\",align[:left:center:right:],noshade[noshade],size,width,\"\n"
                + "html.tag.html=\",version,xmlns,\"\n"
                + "html.tag.img=\",align[:top:middle:bottom:left:right:],alt,border,height,hspace,ismap[ismap],name,src,usemap,vspace,width,\"\n"
                + "html.tag.input=\",accept,align[:top:middle:bottom:left:right:center:],alt,checked[checked],disabled[disabled],maxlength[],name,readonly[readonly],size,src,tabindex,type[:text:checkbox:radio:submit:reset:hidden:image:button:password:],value,\"\n"
                + "html.tag.ins=\",datetime,\"\n"
                + "html.tag.label=\",for,\"\n"
                + "html.tag.legend=\",align[:left:top:right:bottom:]\"\n"
                + "html.tag.li=\",type[:disc:square:circle:1:a:A:i:I:],value[],\"\n"
                + "html.tag.map=\",name,\"\n"
                + "html.tag.meta=\",http-equiv[:content-type:],\"\n"
                + "html.tag.ol=\",compact[compact],start[],type[:1:a:A:i:I:],\"\n"
                + "html.tag.optgroup=\",disabled[disabled],label,\"\n"
                + "html.tag.option=\",disabled[disabled],label,selected[selected],value,\"\n"
                + "html.tag.p=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.pre=\",width[],\"\n"
                + "html.tag.select=\",disabled[disabled],multiple[multiple],name,size,tabindex[],\"\n"
                + "html.tag.span=\"\"\n"
                + "html.tag.strong=\"\"\n"
                + "html.tag.style=\",media,type,\"\n"
                + "html.tag.table=\",align[:left:center:right:],background,border,bgcolor,cellpadding,cellspacing,frame[:void:above:below:hsides:ihs:rhs:vsides:box:border:],rules[:none:groups:rows:cols:all:],summary,width,\"\n"
                + "html.tag.tbody=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.td=\",abbr,align[:left:center:right:justify:char:],axis,background,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.textarea=\",cols[],disabled[disabled],name,readonly[readonly],rows[],tabindex[],\"\n"
                + "html.tag.tfoot=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.th=\",abbr,align[:left:center:right:justify:char:],axis,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.thead=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.tr=\",align[:left:center:right:justify:char:],bgcolor,char,charoff,valign[:top:middle:bottom:baseline:],height,\"\n"
                + "html.tag.u=\"\"\n"
                + "html.tag.ul=\",compact[compact],type[:disc:square:circle:],\"\n"
                + "\n"
                + "\n"
                + "# CSS key-value-pairs.\n"
                + "# An empty value indicates a reference to style's combi-map.\n"
                + "# Placeholders:\n"
                + "# c: Any CSS color value\n"
                + "# u: An URL; e.g. url(http://www.somewhere.com/myimage.jpg);\n"
                + "# n: Any CSS number value without '%'\n"
                + "# N: Any CSS number value\n"
                + "# *: Any value allowed\n"
                + "# d: delete\n"
                + "# t: time\n"
                + "\n"
                + "html.style.azimuth=\",left-side,left-side behind,far-left,far-left behind,left,left behind,center-left,center-left behind,center,center behind,center-right,center-right behind,right,right behind,far-right,far-right behind,right-side,right behind,\"\n"
                + "html.style.background=\"\"\n"
                + "html.style.background-attachment=\",scroll,fixed,\"\n"
                + "html.style.background-color=\"c,transparent,\"\n"
                + "html.style.background-image=\"u\"\n"
                + "html.style.background-position=\",N,top,bottom,center,left,right,\"\n"
                + "html.style.background-repeat=\",repeat,repeat-x,repeat-y,no-repeat,\"\n"
                + "html.style.border=\"\"\n"
                + "html.style.border-bottom=\"\"\n"
                + "html.style.border-bottom-color=\"c,transparent,\"\n"
                + "html.style.border-bottom-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-bottom-width=\"n\"\n"
                + "html.style.border-collapse=\",separate,collapse,\"\n"
                + "html.style.border-color=\"c,transparent,\"\n"
                + "html.style.border-left=\"\"\n"
                + "html.style.border-left-color=\"c,transparent,\"\n"
                + "html.style.border-left-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-left-width=\"n\"\n"
                + "html.style.border-right=\"\"\n"
                + "html.style.border-right-color=\"c,transparent,\"\n"
                + "html.style.border-right-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-right-width=\"n\"\n"
                + "html.style.border-spacing=\"N\"\n"
                + "html.style.border-style=\"\"\n"
                + "html.style.border-top=\"\"\n"
                + "html.style.border-top-color=\"c,transparent,\"\n"
                + "html.style.border-top-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-top-width=\"n\"\n"
                + "html.style.border-width=\"\"\n"
                + "html.style.bottom=\"N,auto,\"\n"
                + "html.style.caption-side=\",top,bottom,left,right,\"\n"
                + "html.style.centerline=\"d\"\n"
                + "html.style.clear=\",left,right,both,none,\"\n"
                + "html.style.clip=\"d\"\n"
                + "html.style.color=\"c,transparent,\"\n"
                + "html.style.content=\"d\"\n"
                + "html.style.counter-increment=\"d\"\n"
                + "html.style.counter-reset=\"d\"\n"
                + "html.style.counter=\"d\"\n"
                + "html.style.cue=\"u\"\n"
                + "html.style.cue-after=\"u\"\n"
                + "html.style.cue-before=\"u\"\n"
                + "html.style.cursor=\",auto,default,crosshair,pointer,move,n-resize,ne-resize,e-resize,se-resize,s-resize,sw-resize,w-resize,nw-resize,text,wait,help,\"\n"
                + "html.style.definition-src=\"d\"\n"
                + "html.style.direction=\",ltr,rtl,\"\n"
                + "html.style.display=\",block,inline,list-item,marker,run-in,compact,none,table,inline-table,table-row,table-cell,table-row-group,table-header-group,table-footer-group,table-column,table-column-group,table-caption,\"\n"
                + "html.style.empty-cells=\",show,hide,\"\n"
                + "html.style.elevation=\",below,level,above,higher,lower,\"\n"
                + "html.style.filter=\"d\" \n"
                + "html.style.float=\",left,right,none,\"\n"
                + "html.style.font=\"\"\n"
                + "html.style.font-family=\"*\"\n"
                + "html.style.font-color=\"c,transparent,\"\n"
                + "html.style.font-size=\"N,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,\"\n"
                + "html.style.font-stretch=\",wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,\"\n"
                + "html.style.font-style=\",italic,oblique,normal,\"\n"
                + "html.style.font-variant=\",small-caps,normal,\"\n"
                + "html.style.font-weight=\",bold,bolder,lighter,100,200,300,400,500,600,700,800,900,normal,\"\n"
                + "html.style.height=\"N,auto,\"\n"
                + "html.style.left=\"N,auto,\"\n"
                + "html.style.letter-spacing=\"n\"\n"
                + "html.style.line-height=\"N\"\n"
                + "html.style.list-style=\"\"    \n"
                + "html.style.list-style-image=\"u,none,\"\n"
                + "html.style.list-style-position=\",inside,outside,\"\n"
                + "html.style.list-style-type=\",decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,none,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n"
                + "html.style.margin=\"\"\n"
                + "html.style.margin-bottom=\"N,auto,inherit,\"\n"
                + "html.style.margin-left=\"N,auto,inherit,\"\n"
                + "html.style.margin-right=\"N,auto,inherit,\"\n"
                + "html.style.margin-top=\"N,auto,inherit,\"\n"
                + "html.style.max-height=\"N\"\n"
                + "html.style.max-width=\"N\"\n"
                + "html.style.min-height=\"N\"\n"
                + "html.style.min-width=\"N\"\n"
                + "html.style.orphans=\"0\"\n"
                + "html.style.overflow=\",visible,hidden,scroll,auto,\"\n"
                + "html.style.padding=\"\"\n"
                + "html.style.padding-bottom=\"N\"\n"
                + "html.style.padding-left=\"N\"\n"
                + "html.style.padding-right=\"N\"\n"
                + "html.style.padding-top=\"N\"\n"
                + "html.style.page-break-after=\",always,avoid,left,right,inherit,auto,\"\n"
                + "html.style.page-break-before=\",always,avoid,left,right,inherit,auto,\"\n"
                + "html.style.page-break-inside=\",avoid,auto,\"\n"
                + "html.style.pause=\"t\"\n"
                + "html.style.pause-after=\"t\"\n"
                + "html.style.pause-before=\"t\"\n"
                + "html.style.pitch=\",x-low,low,medium,high,x-high,\"\n"
                + "html.style.pitch-range=\"0\"\n"
                + "html.style.play-during=\"u,mix,repeat,auto,\"\n"
                + "html.style.position=\",absolute,fixed,relative,static,\"\n"
                + "html.style.quotes=\"d\"\n"
                + "html.style.richness=\"0\"\n"
                + "html.style.right=\"N,auto,\"\n"
                + "html.style.scrollbar-3dlight-color=\"c\"\n"
                + "html.style.scrollbar-arrow-color=\"c\"\n"
                + "html.style.scrollbar-base-color=\"c\"\n"
                + "html.style.scrollbar-darkshadow-color=\"c\"\n"
                + "html.style.scrollbar-face-color=\"c\"\n"
                + "html.style.scrollbar-highlight-color=\"c\"\n"
                + "html.style.scrollbar-shadow-color=\"c\"\n"
                + "html.style.scrollbar-track-color=\"c\"\n"
                + "html.style.speak=\",none,normal,spell-out,\"\n"
                + "html.style.speak-header=\",always,once,\"\n"
                + "html.style.speak-numeral=\",digits,continuous,\"\n"
                + "html.style.speak-punctuation=\",code,none,\"\n"
                + "html.style.speech-rate=\"0,x-slow,slow,slower,medium,faster,fast,x-fase,\"\n"
                + "html.style.stress=\"0\"\n"
                + "html.style.table-layout=\",auto,fixed,\"\n"
                + "html.style.text-align=\",left,center,right,justify,\"\n"
                + "html.style.text-decoration=\",underline,overline,line-through,blink,none,\"\n"
                + "html.style.text-indent=\"N\"\n"
                + "html.style.text-shadow=\"nc,none,\" or color\n"
                + "html.style.text-transform=\",capitalize,uppercase,lowercase,none,\"\n"
                + "html.style.top=\"N,auto,\"\n"
                + "html.style.vertical-align=\",top,middle,bottom,baseline,sub,super,text-top,text-bottom,\"\n"
                + "html.style.visibility=\",hidden,visible,\"\n"
                + "html.style.voice-family=\",male,female,old,young,child,\"\n"
                + "html.style.volume=\"0,silent,x-soft,soft,medium,loud,x-loud,\"\n"
                + "html.style.white-space=\",normal,pre,nowrap,\"\n"
                + "html.style.widows=\"0\"\n"
                + "html.style.width=\"N,auto,\"\n"
                + "html.style.word-spacing=\"n\"\n"
                + "html.style.z-index=\"0\"\n"
                + "\n"
                + "\n"
                + "# CSS combi-map\n"
                + "\n"
                + "html.style.combimap.background=\"uNc,scroll,fixed,transparent,top,bottom,center,left,right,repeat,repeat-x,repeat-y,no-repeat,radial-gradient,\"\n"
                + "html.style.combimap.border=\"Nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,separate,collapse,\"\n"
                + "html.style.combimap.border-bottom=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-left=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-right=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-top=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-width=\"n\"\n"
                + "html.style.combimap.font=\"N*,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,italic,oblique,small-caps,bold,bolder,lighter,100,200,300,400,500,600,700,800,900,\"\n"
                + "html.style.combimap.list-style=\"u,none,inside,outside,decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n"
                + "html.style.combimap.margin=\"N,auto,inherit,\"\n" + "html.style.combimap.padding=\"N\"\n").getBytes();

    private static final Pattern PATTERN_TAG_LINE = Pattern.compile("html\\.tag\\.(\\p{Alnum}+)\\s*(?:=\\s*\"(\\p{Print}*)\")?",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("([\\p{Alnum}-_]+)(?:\\[([\\p{Print}&&[^\\]]]*)\\])?");

    /**
     * Parses specified HTML map.
     *
     * @param htmlMapStr The HTML map string
     * @return The parsed map
     */
    private static Map<String, Map<String, Set<String>>> parseHTMLMap(final String htmlMapStr) {
        final Matcher m = PATTERN_TAG_LINE.matcher(htmlMapStr);
        final Map<String, Map<String, Set<String>>> tagMap = new HashMap<String, Map<String, Set<String>>>();
        while (m.find()) {
            final String attributes = m.group(2);
            final String tagName = toLowerCase(m.group(1));
            if (null == attributes) {
                tagMap.put(tagName, null);
            } else {
                final Matcher attribMatcher = PATTERN_ATTRIBUTE.matcher(attributes);
                final Map<String, Set<String>> attribMap = new HashMap<String, Set<String>>();
                while (attribMatcher.find()) {
                    final String values = attribMatcher.group(2);
                    final String attributeName = toLowerCase(attribMatcher.group(1));
                    if (null == values) {
                        attribMap.put(attributeName, null);
                    } else if (values.length() == 0) {
                        attribMap.put(attributeName, NUM_ATTRIBS);
                    } else {
                        final Set<String> valueSet = new HashSet<String>();
                        final String[] valArr = values.charAt(0) == ':' ? values.substring(1).split("\\s*:\\s*") : values
                            .split("\\s*:\\s*");
                        for (final String value : valArr) {
                            valueSet.add(toLowerCase(value));
                        }
                        attribMap.put(attributeName, valueSet);
                    }
                }
                tagMap.put(tagName, attribMap);
            }
        }
        return tagMap;
    }

    private static final Pattern PATTERN_STYLE_LINE = Pattern.compile(
        "html\\.style\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]*)\"", Pattern.CASE_INSENSITIVE);

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
                final String cssElement = toLowerCase(m.group(1));
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
                styleMap.put(toLowerCase(m.group(1)), valueSet);
            }
        }
        return styleMap;
    }

    private static final Pattern PATTERN_COMBI_LINE = Pattern.compile(
        "html\\.style\\.combimap\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]+)\"", Pattern.CASE_INSENSITIVE);

    /**
     * Parses specified combination map for CSS elements.
     *
     * @param combiMapStr The string representation for combination map
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
            combiMap.put(toLowerCase(m.group(1)), valueSet);
        }
        return combiMap;
    }

    /**
     * Loads the white list.
     */
    public static void loadWhitelist() {
        synchronized (HTMLFilterHandler.class) {
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FilterJerichoHandler.class);
            if (null == staticHTMLMap) {
                String mapStr = null;
                {
                    final ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final File whitelist = null == service ? null : service.getFileByName("whitelist.properties");
                    if (null == whitelist) {
                        LOG.warn("Using default white list");
                        mapStr = new String(DEFAULT_WHITELIST);
                    } else {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new AsciiReader(new FileInputStream(whitelist)));
                            final StringBuilder sb = new StringBuilder();
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                if (line.length() > 0 && '#' != line.charAt(0)) {
                                    /*
                                     * No comment line
                                     */
                                    sb.append(line).append("\r\n");
                                }
                            }
                            mapStr = sb.toString();
                        } catch (final Exception e) {
                            LOG.warn("Using default white list", e);
                            mapStr = new String(DEFAULT_WHITELIST);
                        } finally {
                            Streams.close(reader);
                        }
                    }
                }
                final Map<String, Map<String, Set<String>>> map = parseHTMLMap(mapStr);
                if (!map.containsKey("html")) {
                    map.put("html", null);
                }
                if (!map.containsKey("head")) {
                    map.put("head", null);
                }
                if (!map.containsKey("body")) {
                    map.put("body", null);
                }
                for (final String tagName : SINGLE_TAGS) {
                    if (!map.containsKey(tagName)) {
                        map.put(tagName, null);
                    }
                }
                staticHTMLMap = Collections.unmodifiableMap(map);
                staticStyleMap = Collections.unmodifiableMap(parseStyleMap(mapStr));
            }
        }
    }

    /**
     * Resets the white list.
     */
    public static void resetWhitelist() {
        synchronized (HTMLFilterHandler.class) {
            staticHTMLMap = null;
            staticStyleMap = null;
        }
    }

}
