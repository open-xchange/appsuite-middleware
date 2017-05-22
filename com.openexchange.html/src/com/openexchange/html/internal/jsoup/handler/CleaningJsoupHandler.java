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
import java.util.ArrayList;
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
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
 * {@link CleaningJsoupHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CleaningJsoupHandler implements JsoupHandler {

    private static final CellPadding CELLPADDING_EMPTY = new CellPadding(null);

    protected static class CellPadding {

        final String cellPadding;

        CellPadding(String cellPadding) {
            super();
            this.cellPadding = cellPadding;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /** Such attributes that possibly contain an URI as value */
    public static final Set<String> URI_ATTRS = ImmutableSet.of("action","archive","background","cite","href","longdesc","src","usemap");

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

    private final StringBuilder urlBuilder;

    private boolean body;
    private boolean isCss;
    private final Stringer cssBuffer;

    /**
     * Used to track all subsequent elements of a tag that ought to be removed completely.
     */
    private int skipLevel;

    private final Set<Node> keepChildrenNodes;
    private final Set<Node> removedNodes;
    private final Map<Node, Node> replaceWith;

    /** The max. content size (-1 or >=10000) */
    private int maxContentSize;
    private int curContentSize;
    private boolean maxContentSizeExceeded;

    private boolean dropExternalImages;
    private boolean imageURLFound;
    private boolean replaceUrls = true;
    private String cssPrefix;
    private boolean suppressLinks;
    private boolean replaceBodyWithDiv;
    private final LinkedList<CellPadding> tablePaddings;
    private final boolean changed = false;

    private Document document;
    private Element div;


    /**
     * Initializes a new {@link CleaningJsoupHandler}.
     */
    public CleaningJsoupHandler() {
        super();
        removedNodes = new HashSet<>(16, 0.9F);
        keepChildrenNodes = new HashSet<>(16, 0.9F);
        replaceWith = new HashMap<>(16, 0.9F);
        tablePaddings = new LinkedList<>();
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlMap = FilterMaps.getStaticHTMLMap();
        this.styleMap = FilterMaps.getStaticStyleMap();
    }

    /**
     * Initializes a new {@link CleaningJsoupHandler}.
     */
    public CleaningJsoupHandler(String mapStr) {
        super();
        removedNodes = new HashSet<>(16, 0.9F);
        keepChildrenNodes = new HashSet<>(16, 0.9F);
        replaceWith = new HashMap<>(16, 0.9F);
        tablePaddings = new LinkedList<>();
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
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
    public CleaningJsoupHandler setMaxContentSize(final int maxContentSize) {
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
    public CleaningJsoupHandler setSuppressLinks(boolean suppressLinks) {
        this.suppressLinks = suppressLinks;
        return this;
    }

    /**
     * Sets whether <code>&lt;body&gt;</code> is supposed to be replaced with a <code>&lt;div&gt;</code> tag for embedded display
     *
     * @param replaceBodyWithDiv <code>true</code> to replace; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public CleaningJsoupHandler setReplaceBodyWithDiv(boolean replaceBodyWithDiv) {
        this.replaceBodyWithDiv = replaceBodyWithDiv;
        return this;
    }

    /**
     * Sets the CSS prefix
     *
     * @param cssPrefix The CSS prefix to set
     * @return This handler with new behavior applied
     */
    public CleaningJsoupHandler setCssPrefix(final String cssPrefix) {
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
    public CleaningJsoupHandler setReplaceUrls(final boolean replaceUrls) {
        this.replaceUrls = replaceUrls;
        return this;
    }

    /**
     * Sets whether to replace image URLs.
     *
     * @param dropExternalImages <code>true</code> to replace image URLs; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public CleaningJsoupHandler setDropExternalImages(final boolean dropExternalImages) {
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

        curContentSize += addLen;
        maxContentSizeExceeded = curContentSize > maxContentSize;
        return !maxContentSizeExceeded;
    }

    /**
     * Gets the sanitized HTML document
     *
     * @return The HTML document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Gets the sanitized HTML
     * <p>
     * Either the full HTML document or only the <code>&lt;div&gt;</code> fragment in case a CSS prefix was specified
     *
     * @return The HTML
     */
    public String getHtml() {
        return null == div ? document.outerHtml() : div.outerHtml();
    }

    @Override
    public void finished(Document document) {
        for (Map.Entry<Node, Node> toReplace : replaceWith.entrySet()) {
            toReplace.getKey().replaceWith(toReplace.getValue());
        }
        for (Node node : removedNodes) {
            node.remove();
        }
        for (Node node : keepChildrenNodes) {
            List<Node> nodes = new ArrayList<>(node.childNodes());

            Node pred = node;
            for (Node child : nodes) {
                pred.after(child);
                pred = child;
            }
            node.remove();
        }

        if (replaceBodyWithDiv) {
            Attributes attributes = new Attributes();
            attributes.put("id", cssPrefix);
            Element div = new Element(org.jsoup.parser.Tag.valueOf("div"), "", attributes);

            Element head = document.head();
            for (Element el : head.children()) {
                if ("style".equals(el.tagName())) {
                    div.appendChild(el);
                }
            }

            Element body = document.body();
            for (Node child : new ArrayList<>(body.childNodes())) {
                div.appendChild(child);
            }

            this.div = div;
        }

        this.document = document;
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
                    replaceWith.put(comment, new Comment(checkedCSS, ""));
                } else {
                    removedNodes.add(comment);
                }
            }
        } else {
            String cmt = comment.toString();
            if (false == checkMaxContentSize(cmt.length())) {
                removedNodes.add(comment);
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
                        replaceWith.put(dataNode, new DataNode(checkedCSS, ""));
                    } else {
                        removedNodes.add(dataNode);
                    }
                }
            } else {
                String cdata = dataNode.toString();
                if (false == checkMaxContentSize(cdata.length())) {
                    removedNodes.add(dataNode);
                }
            }
        }
    }

    @Override
    public void handleDocumentType(DocumentType documentType) {
        // Don't care
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
                        replaceWith.put(textNode, new TextNode(checkedCSS, ""));
                    } else {
                        removedNodes.add(textNode);
                    }
                }
            } else {
                String content = textNode.toString();
                if (false == checkMaxContentSize(content.length())) {
                    removedNodes.add(textNode);
                }
            }
        }
    }

    @Override
    public void handleXmlDeclaration(XmlDeclaration xmlDeclaration) {
        // Don't care
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
        } else {
            skipLevel--;
        }
    }

    @Override
    public void handleElementStart(Element element) {
        if (maxContentSizeExceeded) {
            // Do not append more elements
            removedNodes.add(element);
            return;
        }
        if (isCss) {
            // Ignore tags in CSS content
            removedNodes.add(element);
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
            } else {
                removedNodes.add(element);
            }
            return;
        }

        // A non-empty tag
        if (skipLevel > 0) {
            if (!"body".equals(tagName)) {
                skipLevel++;
                return;
            }
            // <body> tag...
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
                removedNodes.add(element);
                skipLevel++;
            } else if (isMSTag(tagName)) {
                /*
                 * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                 */
                keepChildrenNodes.add(element);
            } else if (isRemoveWholeTag(tagName)) {
                /*
                 * Remove whole tag incl. subsequent content and tags
                 */
                removedNodes.add(element);
                skipLevel++;
            } else {
                /*
                 * Just remove tag definition: "<tag>text<subtag>text</subtag></tag>" would be "text<subtag>text</subtag>"
                 */
                keepChildrenNodes.add(element);
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
        Attributes attributes = startTag.attributes();
        if (simple && "meta".equals(tagName) && allowedAttributes.containsKey("http-equiv")) {
            if (attributes.hasKeyIgnoreCase("http-equiv")) {
                /*
                 * Special handling for allowed meta tag which provides an allowed HTTP header indicated through 'http-equiv' attribute
                 */
                boolean isFine = true;
                for (final Attribute attribute : attributes) {
                    final String val = attribute.getValue();
                    if (isNonJavaScriptURL(val, tagName, "url=")) {
                        // Nothing
                    } else {
                        isFine = false;
                        break;
                    }
                }
                if (false == isFine) {
                    removedNodes.add(startTag);
                }
                return;
            }
        }

        // Handle start tag
        if ("img".equals(tagName)) {
            String width = attributes.getIgnoreCase("width");
            if (Strings.isNotEmpty(width)) {
                String height = attributes.getIgnoreCase("height");
                if (Strings.isNotEmpty(height)) {
                    prependWidthHeightToStyleIfAbsent(mapFor("width", width + "px", "height", height + "px"), attributes, startTag);
                }
            }
        } else if ("table".equals(tagName)) {
            addTableTag(attributes, startTag);
        } else if ("td".equals(tagName) || "th".equals(tagName)) {
            CellPadding cellPadding = tablePaddings.peek();
            if (CELLPADDING_EMPTY != cellPadding && null != cellPadding) {
                String style = attributes.getIgnoreCase("style");
                if (Strings.isEmpty(style) || style.indexOf("padding") < 0) {
                    prependToStyle("padding: " + cellPadding.cellPadding + "px;", attributes, startTag);
                }
            }
        } else if (suppressLinks) {
            if (isHrefTag(tagName) && attributes.hasKeyIgnoreCase("href")) {
                startTag.attr("href", "#");
                startTag.attr("onclick", "return false");
                startTag.attr("data-disabled", "true");
            }
        }

        for (Attribute attribute : listFor(attributes)) {
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
                    startTag.attr("style", css);
                } else {
                    startTag.removeAttr(attr);
                }
            } else if ("class".equals(attr) || "id".equals(attr)) {
                String value = prefixBlock(CharacterReference.encode(attribute.getValue()), cssPrefix);
                startTag.attr(attribute.getKey(), value);
            } else {
                if (isAttributeNameForbidden(attr) || HtmlServices.containsEventHandler(attr)) {
                    startTag.removeAttr(attr);
                } else {
                    final String val = attribute.getValue();
                    if (null == allowedAttributes) { // No restrictions
                        if (false == isSafeAttributeValue(tagName, startTag, attribute, attr, val)) {
                            startTag.removeAttr(attr);
                        }
                    } else {
                        if (allowedAttributes.containsKey(attr)) {
                            if (null == val) {
                                // Nothing
                            } else {
                                Set<String> allowedValues = allowedAttributes.get(attr);
                                if (null == allowedValues || allowedValues.contains(toLowerCase(val))) {
                                    if (false == isSafeAttributeValue(tagName, startTag, attribute, attr, val)) {
                                        startTag.removeAttr(attr);
                                    }
                                } else if (FilterMaps.getNumAttribs() == allowedValues) {
                                    /*
                                     * Only numeric attribute value allowed
                                     */
                                    if (false == PAT_NUMERIC.matcher(val.trim()).matches()) {
                                        startTag.removeAttr(attr);
                                    }
                                }
                            }
                        } else {
                            startTag.removeAttr(attr);
                        }
                    }
                }
            }
        }
    }

    private static boolean isAttributeNameForbidden(String attr) {
        return attr.indexOf('"') >= 0;
    }

    private List<Attribute> listFor(Attributes attributes) {
        List<Attribute> copy = new ArrayList<>(attributes.size());
        for (Attribute attribute : attributes) {
            copy.add(attribute);
        }
        return copy;
    }

    private boolean isSafeAttributeValue(String tagName, Element el, Attribute attribute, String attr, String val) {
        if (false == isSafe(val, tagName)) {
            // Unsafe value
            return false;
        }

        if (dropExternalImages && "background".equals(attr) && PATTERN_URL.matcher(val).matches()) {
            attribute.setValue("");
            imageURLFound = true;
            return true;
        }

        if (dropExternalImages && ("img".equals(tagName) || "input".equals(tagName)) && "src".equals(attr)) {
            if (isInlineImage(val)) {
                // Allow inline images
                return true;
            }

            attribute.setValue("");
            el.attr("data-original-src", val);
            imageURLFound = true;
            return true;
        }

        if (replaceUrls && URI_ATTRS.contains(attr)) {
            attribute.setValue(checkPossibleURL(val, urlBuilder));
        }

        return true;
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
     */
    private void addTableTag(Attributes attributes, Element startTag) {
        // Has attribute "bgcolor"
        String bgcolor = attributes.getIgnoreCase("bgcolor");
        if (Strings.isNotEmpty(bgcolor)) {
            prependToStyle("background-color: " + bgcolor + ';', attributes, startTag);
        }

        handleTableCellpaddingAttribute(attributes, startTag);
    }

    /**
     * Handles 'cellpadding' attribute for the given table start tag to be able to add its value to nested tags.<br>
     * <br>
     * If 'cellpadding' attribute is available, add given 'cellpadding' (-value) to table stack. If 'cellpadding' attribute is not available have a look if 'padding' is nested in 'style' attribute of the table. I so add 'padding' to nested tags.
     *
     * @param attrMap - map containing attributes of the 'table' tag
     */
    protected void handleTableCellpaddingAttribute(Attributes attributes, Element startTag) {
        if (attributes == null) {
            return;
        }

        // Has attribute "cellpadding"?
        String cellpadding = attributes.getIgnoreCase("cellpadding");
        if (Strings.isEmpty(cellpadding)) {
            String style = attributes.getIgnoreCase("style");
            if (Strings.isEmpty(style) || style.indexOf("padding") < 0) {
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
            if ("0".equals(attributes.getIgnoreCase("cellspacing"))) {
                prependToStyle("border-collapse: collapse;", attributes, startTag);
            }

            // Table has cell padding -> Remember it for all child elements
            tablePaddings.addFirst(new CellPadding(cellpadding));
        }
    }

    private void prependToStyle(String stylePrefix, Attributes attributes, Element startTag) {
        String style = attributes.getIgnoreCase("style");
        if (Strings.isEmpty(style)) {
            style = stylePrefix;
        } else {
            style = stylePrefix + " " + style;
        }
        startTag.attr("style", style);
    }

    private void prependWidthHeightToStyleIfAbsent(Map<String, String> styleNvps, Attributes attributes, Element startTag) {
        String style = attributes.getIgnoreCase("style");
        if (Strings.isEmpty(style)) {
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
        startTag.attr("style", style);
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

    /**
     * Checks specified attribute value if it possibly is an URI.
     *
     * @param val The value to check
     * @param urlBuilder The <code>StringBuilder</code> to use
     * @return The checked attribute value
     */
    public static String checkPossibleURL(String val, StringBuilder urlBuilder) {
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
