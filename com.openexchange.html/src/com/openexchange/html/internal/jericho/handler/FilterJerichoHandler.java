/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.css.CSSMatcher;
import com.openexchange.html.internal.filtering.FilterMaps;
import com.openexchange.html.internal.jericho.JerichoHandler;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

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

    // A decimal digit: [0-9]
    private static final Pattern PAT_NUMERIC = Pattern.compile("\\p{Digit}+");

    private static final Set<String> ALL;

    static {
        /*
         * ALL tags
         */
        Set<String> s = new HashSet<>(HTMLElements.getElementNames());
        s.add("smarttagtype");
        ALL = Collections.unmodifiableSet(s);
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
     * Used to track script/svg tags
     */
    private boolean insideScriptTag = false;

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
    private boolean suppressLinks;
    private final LinkedList<CellPadding> tablePaddings;
    private final boolean changed = false;


    /**
     * Initializes a new {@link FilterJerichoHandler}.
     */
    public FilterJerichoHandler(final int capacity, final HtmlServiceImpl htmlService) {
        super();
        tablePaddings = new LinkedList<>();
        this.htmlService = htmlService;
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlBuilder = new StringBuilder(capacity);
        this.attrBuilder = new StringBuilder(128);
        this.htmlMap = FilterMaps.getStaticHTMLMap();
        this.styleMap = FilterMaps.getStaticStyleMap();
    }

    /**
     * Initializes a new {@link FilterJerichoHandler}.
     */
    public FilterJerichoHandler(final int capacity, final String mapStr, final HtmlServiceImpl htmlService) {
        super();
        tablePaddings = new LinkedList<>();
        this.htmlService = htmlService;
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlBuilder = new StringBuilder(capacity);
        this.attrBuilder = new StringBuilder(128);
        final Map<String, Map<String, Set<String>>> map = FilterMaps.parseHTMLMap(mapStr);
        if (!map.containsKey("html")) {
            map.put("html", null);
        }
        if (!map.containsKey("head")) {
            map.put("head", null);
        }
        if (!map.containsKey("body")) {
            map.put("body", null);
        }
        for (final String tagName : FilterMaps.getSingleTags()) {
            if (!map.containsKey(tagName)) {
                map.put(tagName, null);
            }
        }
        htmlMap = Collections.unmodifiableMap(map);
        styleMap = Collections.unmodifiableMap(FilterMaps.parseStyleMap(mapStr));
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
     * Sets whether to suppress links
     *
     * @param suppressLinks <code>true</code> to suppress links; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public FilterJerichoHandler setSuppressLinks(boolean suppressLinks) {
        this.suppressLinks = suppressLinks;
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
                        imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), FilterMaps.getImageStyleMap(), null, false);
                        checkedCSS = cssBuffer.toString();
                        cssBuffer.setLength(0);
                    }
                    if (checkMaxContentSize(checkedCSS.length())) {
                        htmlBuilder.append(checkedCSS);
                    }
                }
            } else {
                if (checkMaxContentSize(content.length())) {
                    htmlBuilder.append(content);
                }
            }
        }
    }

    @Override
    public void markCssEnd(EndTag endTag) {
        if (skipLevel == 0) {
            isCss = false;
            if (depth == 0) {
                htmlBuilder.append("</").append(endTag.getName()).append('>');
            } else if (!getAndUnmark()) {
                htmlBuilder.append("</").append(endTag.getName()).append('>');
            }
        } else {
            skipLevel--;
        }
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
            if (isCss) {
                // Ignore end tags in CSS content
                return;
            }
            if (depth == 0) {
                htmlBuilder.append("</").append(name).append('>');
            } else if (!getAndUnmark()) {
                htmlBuilder.append("</").append(name).append('>');
            }
        } else {
            skipLevel--;
            insideScriptTag = false;
        }
    }

    @Override
    public void markCssStart(StartTag startTag) {
        if (skipLevel > 0) {
            skipLevel++;
            return;
        }
        isCss = true;
        if (depth > 0) {
            depth++;
        }
        addStartTag(startTag, false, htmlMap.get(startTag.getName()));
    }

    @Override
    public void handleStartTag(final StartTag startTag) {
        if (maxContentSizeExceeded) {
            // Do not append more elements
            return;
        }
        if (insideScriptTag) {
            // Ignore script tags completely
            return;
        }
        if (isCss) {
            // Ignore tags in CSS content
            return;
        }
        final String tagName = startTag.getName();
        if (startTag.isEndTagForbidden() || FilterMaps.getSingleTags().contains(tagName)) {
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
                    insideScriptTag = true;
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
        return (HTMLElementName.SCRIPT == check || "svg".equals(check) || check.startsWith("w:") || check.startsWith("o:"));
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
                    if (isNonJavaScriptURL(val, tagName, "url=")) {
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
                    prependWidthHeightToStyleIfAbsent(mapFor("width", width + "px", "height", height + "px"), attrMap);
                }
            }
            String src = attrMap.get("src");
            if (Strings.isNotEmpty(src) && false == isInlineImage(src) && (src.indexOf('<') >= 0 || src.indexOf('\n') >= 0 || src.indexOf('\r') >= 0)) {
                // Invalid <img> tag
                return;
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
        } else if (suppressLinks) {
            if (isHrefTag(tagName) && attrMap.containsKey("href")) {
                attrMap.put("href", "#");
                attrBuilder.append(' ').append("onclick=\"return false\"");
                attrBuilder.append(' ').append("data-disabled=\"true\"");
            }
        }

        Set<String> uriAttributes = replaceUrls ? setFor(startTag.getURIAttributes()) : Collections.<String> emptySet();
        for (Map.Entry<String, String> attribute : attrMap.entrySet()) {
            String attr = attribute.getKey();
            if ("style".equals(attr)) {
                /*
                 * Handle style attribute
                 */
                String css = attribute.getValue();
                if (Strings.isNotEmpty(css)) {
                    checkCSS(cssBuffer.append(css), styleMap, true);
                    css = cssBuffer.toString();
                    cssBuffer.setLength(0);
                    if (dropExternalImages) {
                        imageURLFound |= checkCSS(cssBuffer.append(css), FilterMaps.getImageStyleMap(), true, false);
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
                if (false == HtmlServices.containsEventHandler(attr)) {
                    final String val = attribute.getValue();
                    if (null == allowedAttributes) { // No restrictions
                        if (isSafe(val, tagName)) {
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
                                if (replaceUrls && (uriAttributes.contains(attr) || isHrefTag(tagName))) {
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
                                    if (isSafe(val, tagName)) {
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
                                            if (replaceUrls && (uriAttributes.contains(attr) || isHrefTag(tagName))) {
                                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(checkPossibleURL(val))).append('"');
                                            } else {
                                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                                            }
                                        }
                                    }
                                } else if (FilterMaps.getNumAttribs() == allowedValues) {
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
        }

        htmlBuilder.append('<').append(tagName).append(attrBuilder.toString());
        if (startTag.isEmptyElementTag() && !startTag.isEndTagForbidden()) {
            htmlBuilder.append('/');
        }
        htmlBuilder.append('>');
    }

    private Set<String> setFor(List<Attribute> attributes) {
        if (null == attributes) {
            return Collections.emptySet();
        }

        int size = attributes.size();
        if (size <= 0) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<>(size);
        Iterator<Attribute> iter = attributes.iterator();
        for (int i = size; i-- > 0;) {
            names.add(iter.next().getKey());
        }
        return names;
    }

    /**
     * Checks if denoted tag may hold a <code>"href"</code> attribute according to <a href="http://www.w3schools.com/tags/att_href.asp">this specification</a>.
     *
     * @param tagName The name of the tag to check
     * @return <code>true</code> if tag possibly holds a <code>"href"</code> attribute; otherwise <code>false</code>
     */
    private boolean isHrefTag(String tagName) {
        return HTMLElementName.A == tagName || HTMLElementName.AREA == tagName || HTMLElementName.BASE == tagName || HTMLElementName.LINK == tagName;
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

    private void prependWidthHeightToStyleIfAbsent(Map<String, String> styleNvps, Map<String, String> attrMap) {
        String style = attrMap.get("style");
        if (null == style) {
            style = generateStyleString(styleNvps);
        } else {
            // Filter out all, but "width"/"height"
            CSSMatcher.checkCSSElements(cssBuffer.append(style), FilterMaps.getHeightWidthStyleMap(), true);
            String toCheck = cssBuffer.toString();
            cssBuffer.setLength(0);
            if (Strings.isEmpty(toCheck)) {
                // Need to prepend width & height
                style = generateStyleString(styleNvps) + " " + style;
            }
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
        Map<String, String> map = new LinkedHashMap<>(attributes.size());
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

        Map<String, String> map = new LinkedHashMap<>(length >> 1);
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
                    builder.append('#').append(cssPrefix).append('-').append(word.substring(1)).append(' ');
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
        String ret = Strings.replaceWhitespacesWith(val, null);
        if (Strings.isEmpty(ret)) {
            return ret;
        }
        final Matcher m = PATTERN_URL_SOLE.matcher(ret);
        if (!m.matches()) {
            return ret;
        }
        urlBuilder.setLength(0);
        urlBuilder.append(ret.substring(0, m.start()));
        //replaceURL(urlDecode(m.group()), urlBuilder);
        replaceURL(m.group(), urlBuilder);
        urlBuilder.append(ret.substring(m.end()));
        return urlBuilder.toString();
    }

    private static void replaceURL(final String url, final StringBuilder builder) {
        /*
         * Contains any non-ascii character in host part?
         */
        final int restoreLen = builder.length();
        try {
            builder.append(HtmlServiceImpl.checkURL(url));
        } catch (MalformedURLException e) {
            /*
             * Not a valid URL
             */
            builder.setLength(restoreLen);
            builder.append(url);
        } catch (Exception e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HTMLURLReplacerHandler.class);
            log.warn("URL replacement failed.", e);
            builder.setLength(restoreLen);
            builder.append(url);
        }
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        htmlBuilder.append(sanitizeDocDeclaration(docDecl));
    }

    private String sanitizeDocDeclaration(String docDecl) {
        String str = docDecl.trim();
        if (!str.startsWith("<!")) {
            str = "<!" + str;
        }
        if (!str.endsWith(">")) {
            str = str + ">";
        }

        StringBuilder sb = null;
        int end = str.length() - 1;
        for (int i = 2; i < end; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    if (null == sb) {
                        sb = new StringBuilder(docDecl.length());
                        sb.append(str, 0, i);
                    }
                    break;
                case '>':
                    if (null == sb) {
                        sb = new StringBuilder(docDecl.length());
                        sb.append(str, 0, i);
                    }
                    break;
                default:
                    if (null != sb) {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return null == sb ? str : sb.append(">").toString();
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
                        imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), FilterMaps.getImageStyleMap(), null, false);
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
    public void handleComment(String comment) {
        if (isCss) {
            if (false == maxContentSizeExceeded) {
                String cleanCss = removeSurroundingHTMLComments(comment);
                checkCSS(cssBuffer.append(cleanCss), styleMap, cssPrefix);
                String checkedCSS = "\n<!--\n" + cssBuffer.toString() + "\n-->\n";
                cssBuffer.setLength(0);
                if (dropExternalImages) {
                    imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), FilterMaps.getImageStyleMap(), null, false);
                    checkedCSS = cssBuffer.toString();
                    cssBuffer.setLength(0);
                }
                if (checkMaxContentSize(checkedCSS.length())) {
                    htmlBuilder.append(checkedCSS);
                }
            }
        } else {
            String cmt = sanitizeComment(comment);
            if (checkMaxContentSize(cmt.length())) {
                htmlBuilder.append(cmt);
            }
        }
    }

    private String sanitizeComment(String comment) {
        String str = comment.trim();
        if (!str.startsWith("<!--")) {
            str = "<!--" + str;
        }
        if (!str.endsWith("-->")) {
            str = str + "-->";
        }

        StringBuilder sb = null;
        int end = str.length() - 3;
        for (int i = 4; i < end; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    if (null == sb) {
                        sb = new StringBuilder(comment.length());
                        sb.append(str, 0, i);
                    }
                    break;
                case '>':
                    if (null == sb) {
                        sb = new StringBuilder(comment.length());
                        sb.append(str, 0, i);
                    }
                    break;
                default:
                    if (null != sb) {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return null == sb ? str : sb.append("-->").toString();
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

}
