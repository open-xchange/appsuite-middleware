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

package com.openexchange.html.internal.jsoup.handler;

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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import com.google.common.collect.ImmutableSet;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.css.CSSMatcher;
import com.openexchange.html.internal.filtering.FilterMaps;
import com.openexchange.html.internal.jsoup.JsoupHandler;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.HTMLElements;

/**
 * {@link FilterJsoupHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FilterJsoupHandler implements JsoupHandler {

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

    private final Set<Element> removedTags;
    private boolean hasRemovedTags;

    /**
     * The max. content size (-1 or >=10000)
     */
    private int maxContentSize;

    private boolean maxContentSizeExceeded;

    private boolean dropExternalImages;
    private boolean imageURLFound;
    private boolean replaceUrls = true;
    private String cssPrefix;
    private boolean suppressLinks;
    private final LinkedList<CellPadding> tablePaddings;
    private final boolean changed = false;


    /**
     * Initializes a new {@link FilterJsoupHandler}.
     */
    public FilterJsoupHandler(final int capacity, final HtmlServiceImpl htmlService) {
        super();
        removedTags = new HashSet<>(4, 0.9F);
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
     * Initializes a new {@link FilterJsoupHandler}.
     */
    public FilterJsoupHandler(final int capacity, final String mapStr, final HtmlServiceImpl htmlService) {
        super();
        removedTags = new HashSet<>(4, 0.9F);
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
    public FilterJsoupHandler setMaxContentSize(final int maxContentSize) {
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
    public FilterJsoupHandler setSuppressLinks(boolean suppressLinks) {
        this.suppressLinks = suppressLinks;
        return this;
    }

    /**
     * Sets the CSS prefix
     *
     * @param cssPrefix The CSS prefix to set
     * @return This handler with new behavior applied
     */
    public FilterJsoupHandler setCssPrefix(final String cssPrefix) {
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
    public FilterJsoupHandler setReplaceUrls(final boolean replaceUrls) {
        this.replaceUrls = replaceUrls;
        return this;
    }

    /**
     * Sets whether to replace image URLs.
     *
     * @param dropExternalImages <code>true</code> to replace image URLs; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public FilterJsoupHandler setDropExternalImages(final boolean dropExternalImages) {
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
     * Marks specified element as being removed.
     */
    private void mark(Element element) {
        if (removedTags.add(element)) {
            hasRemovedTags = true;
        }
    }

    /**
     * Checks if given element has previously been marked as being removed
     *
     * @return <code>true</code> if element was marked as removed; otherwise <code>false</code>
     */
    private boolean getAndUnmark(Element element) {
        return hasRemovedTags ? false : removedTags.remove(element);
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
    public void finished(Document document) {
        // Nothing
    }

    @Override
    public void handleComment(Comment comment) {
        if (isCss) {
            if (false == maxContentSizeExceeded) {
                String cleanCss = removeSurroundingHTMLComments(comment.toString());
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
            String cmt = comment.toString();
            if (checkMaxContentSize(cmt.length())) {
                htmlBuilder.append(cmt);
            }
        }
    }

    @Override
    public void handleDataNode(DataNode dataNode) {
        if (skipLevel == 0) {
            if (isCss) {
                if (false == maxContentSizeExceeded) {
                    /*
                     * Handle style attribute
                     */
                    String cdata = dataNode.toString();
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
                String cdata = dataNode.toString();
                if (checkMaxContentSize(cdata.length())) {
                    htmlBuilder.append(cdata);
                }
            }
        }
    }

    @Override
    public void handleDocumentType(DocumentType documentType) {
        htmlBuilder.append(documentType);
    }

    @Override
    public void handleTextNode(TextNode textNode) {
        if (skipLevel == 0) {
            if (isCss) {
                if (false == maxContentSizeExceeded) {
                    /*
                     * Handle style attribute
                     */
                    String content = textNode.toString();
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
                String content = textNode.toString();
                if (checkMaxContentSize(content.length())) {
                    htmlBuilder.append(content);
                }
            }
        }
    }

    @Override
    public void handleXmlDeclaration(XmlDeclaration xmlDeclaration) {
        htmlBuilder.append(xmlDeclaration);
    }

    @Override
    public void handleElementEnd(Element element) {
        if (skipLevel == 0) {
            final String name = Strings.asciiLowerCase(element.tagName());
            if (body && "body".equals(name)) {
                body = false;
            } else if (isCss && "style".equals(name)) {
                isCss = false;
            } else if ("table".equals(name)) {
                tablePaddings.poll();
            }
            if (isCss) {
                // Ignore end tags in CSS content
                return;
            }
            if (!getAndUnmark(element)) {
                htmlBuilder.append("</").append(element.tagName()).append('>');
            }
        } else {
            skipLevel--;
            insideScriptTag = false;
        }
    }

    @Override
    public void handleElementStart(Element element) {
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

        String tagName = Strings.asciiLowerCase(element.tagName());
        org.jsoup.parser.Tag tag = element.tag();
        if (tag.isEmpty()) {
            // Simple tag
            if (skipLevel > 0) {
                return;
            }
            if (htmlMap.containsKey(tagName)) {
                addStartTag(element, tagName, true, htmlMap.get(tagName));
            }
            return;
        }

        // A non-empty tag
        if (skipLevel > 0) {
            if (!"body".equals(tagName)) {
                skipLevel++;
                return;
            }
            skipLevel = 0;
        }
        if (htmlMap.containsKey(tagName)) {
            if ("body".equals(tagName)) {
                body = true;
            } else if ("style".equals(tagName)) {
                isCss = true;
            }
            addStartTag(element, tagName, false, htmlMap.get(tagName));
        } else {
            if (!body) {
                /*
                 * Remove whole tag incl. subsequent content and tags
                 */
                skipLevel++;
            } else if (isMSTag(tagName)) {
                /*
                 * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                 */
                mark(element);
            } else if (isRemoveWholeTag(tagName)) {
                /*
                 * Remove whole tag incl. subsequent content and tags
                 */
                insideScriptTag = true;
                skipLevel++;
            } else {
                /*
                 * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                 */
                mark(element);
            }
        }
    }

    private static boolean isMSTag(String tagName) {
        final char c;
        if (tagName.length() < 2 || ':' != tagName.charAt(1) || (('w' != (c = tagName.charAt(0))) && ('W' != c) && ('o' != c) && ('O' != c))) {
            return false;
        }
        return ALL.contains(tagName.substring(2));
    }

    private static boolean isRemoveWholeTag(String tagName) {
        return ("script".equals(tagName) || "svg".equals(tagName) || tagName.startsWith("w:") || tagName.startsWith("o:"));
    }

    private static final Pattern PATTERN_STYLE_VALUE = Pattern.compile("([\\p{Alnum}-_]+)\\s*:\\s*([\\p{Print}\\p{L}&&[^;]]+);?", Pattern.CASE_INSENSITIVE);

    /**
     * Adds tag occurring in white list to HTML result.
     *
     * @param startTag The start tag to add
     * @param simple <code>true</code> to write a simple tag; otherwise <code>false</code>
     * @param allowedAttributes The allowed tag's attributes or <code>null</code> to allow all
     */
    private void addStartTag(Element startTag, String tagName, final boolean simple, final Map<String, Set<String>> allowedAttributes) {
        attrBuilder.setLength(0);

        if (simple && "meta".equals(tagName) && allowedAttributes.containsKey("http-equiv")) {
            Attributes attributes = startTag.attributes();
            if (attributes.hasKeyIgnoreCase("http-equiv")) {
                /*
                 * Special handling for allowed meta tag which provides an allowed HTTP header indicated through 'http-equiv' attribute
                 */
                for (final Attribute attribute : attributes) {
                    final String val = attribute.getValue();
                    if (isNonJavaScriptURL(val, tagName, "url=")) {
                        attrBuilder.append(' ').append(attribute.getKey()).append("=\"").append(htmlService.encodeForHTMLAttribute(IMMUNE_HTMLATTR, val)).append('"');
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
        Map<String, String> attrMap = createMapFrom(startTag.attributes());
        if ("img".equals(tagName)) {
            String width = attrMap.get("width");
            if (null != width) {
                String height = attrMap.get("height");
                if (null != height) {
                    prependWidthHeightToStyleIfAbsent(mapFor("width", width + "px", "height", height + "px"), attrMap);
                }
            }
        } else if ("table".equals(tagName)) {
            addTableTag(attrMap);
        } else if ("td".equals(tagName) || "th".equals(tagName)) {
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

        Set<String> uriAttributes = replaceUrls ? setForUriAttributes(startTag.attributes()) : Collections.<String> emptySet();
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
                            } else if (dropExternalImages && ("img".equals(tagName) || "input".equals(tagName)) && "src".equals(attr)) {
                                if (isInlineImage(val)) {
                                    // Allow inline images
                                    attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                                } else {
                                    attrBuilder.append(' ').append(attr).append("=\"\" data-original-src=\"").append(CharacterReference.encode(val)).append('"');
                                    imageURLFound = true;
                                    // return;
                                }
                            } else {
                                if (replaceUrls && uriAttributes.contains(attr)) {
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
                                        } else if (dropExternalImages && ("img".equals(tagName) || "input".equals(tagName)) && "src".equals(attr)) {
                                            if (isInlineImage(val)) {
                                                // Allow inline images
                                                attrBuilder.append(' ').append(attr).append("=\"").append(CharacterReference.encode(val)).append('"');
                                            } else {
                                                attrBuilder.append(' ').append(attr).append("=\"\" data-original-src=\"").append(CharacterReference.encode(val)).append('"');
                                                imageURLFound = true;
                                                // return;
                                            }
                                        } else {
                                            if (replaceUrls && uriAttributes.contains(attr)) {
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

        htmlBuilder.append('<').append(startTag.tagName()).append(attrBuilder.toString());
        /*-
         *
        if (startTag.isEmptyElementTag() && !startTag.isEndTagForbidden()) {
            htmlBuilder.append('/');
        }
        */
        htmlBuilder.append('>');
    }

    private static final Set<String> URI_ATTRS = ImmutableSet.of("action","archive","background","cite","href","longdesc","src","usemap");

    private Set<String> setForUriAttributes(Attributes attributes) {
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
            String attrName = iter.next().getKey();
            if (URI_ATTRS.contains(attrName)) {
                names.add(attrName);
            }
        }
        return names;
    }

    private static final Set<String> HREF_TAGS = ImmutableSet.of("a","area","base","link");

    /**
     * Checks if denoted tag may hold a <code>"href"</code> attribute according to <a href="http://www.w3schools.com/tags/att_href.asp">this specification</a>.
     *
     * @param tagName The name of the tag to check
     * @return <code>true</code> if tag possibly holds a <code>"href"</code> attribute; otherwise <code>false</code>
     */
    private boolean isHrefTag(String tagName) {
        return HREF_TAGS.contains(tagName);
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

            // Filter out existing entries
            /*-
             *
            Map<String, Set<String>> styleMap = new HashMap<>(this.styleMap);
            styleMap.keySet().removeAll(styleNvps.keySet());
            CSSMatcher.checkCSSElements(cssBuffer.append(style), styleMap, true);s
            style = cssBuffer.toString();
            cssBuffer.setLength(0);
            style = generateStyleString(styleNvps) + " " + style;
             *
             */
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

    private Map<String, String> createMapFrom(Attributes attributes) {
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
