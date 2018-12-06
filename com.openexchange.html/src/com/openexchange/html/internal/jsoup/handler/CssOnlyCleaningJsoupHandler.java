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

import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL;
import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import static com.openexchange.html.internal.jsoup.JsoupHandlers.isInlineImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.openexchange.html.internal.filtering.FilterMaps;
import com.openexchange.html.internal.jsoup.JsoupHandler;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;
import net.htmlparser.jericho.CharacterReference;

/**
 * {@link CssOnlyCleaningJsoupHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CssOnlyCleaningJsoupHandler implements JsoupHandler {

    /*-
     * Member stuff
     */
    private boolean isCss;
    private final Stringer cssBuffer;

    private final Set<Node> removedNodes;
    private final Map<Node, Node> replaceWith;

    /** The max. content size (-1 or >=10000) */
    private int maxContentSize;
    private int curContentSize;
    private boolean maxContentSizeExceeded;

    private boolean dropExternalImages;
    private boolean imageURLFound;
    private String cssPrefix;
    private boolean suppressLinks;
    private boolean replaceBodyWithDiv;

    private Document document;
    private Element div;


    /**
     * Initializes a new {@link CssOnlyCleaningJsoupHandler}.
     */
    public CssOnlyCleaningJsoupHandler() {
        super();
        removedNodes = new HashSet<>(16, 0.9F);
        replaceWith = new HashMap<>(16, 0.9F);
        this.maxContentSize = -1;
        this.maxContentSizeExceeded = false;
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
    }

    /**
     * Sets the max. content size. &lt;= <code>0</code> means unlimited, &lt; <code>10000</code> will be set to <code>10000</code>.
     *
     * @param maxContentSize The max. content size to set
     * @return This handler with new behavior applied
     */
    public CssOnlyCleaningJsoupHandler setMaxContentSize(final int maxContentSize) {
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
    public CssOnlyCleaningJsoupHandler setSuppressLinks(boolean suppressLinks) {
        this.suppressLinks = suppressLinks;
        return this;
    }

    /**
     * Sets whether <code>&lt;body&gt;</code> is supposed to be replaced with a <code>&lt;div&gt;</code> tag for embedded display
     *
     * @param replaceBodyWithDiv <code>true</code> to replace; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public CssOnlyCleaningJsoupHandler setReplaceBodyWithDiv(boolean replaceBodyWithDiv) {
        this.replaceBodyWithDiv = replaceBodyWithDiv;
        return this;
    }

    /**
     * Sets the CSS prefix
     *
     * @param cssPrefix The CSS prefix to set
     * @return This handler with new behavior applied
     */
    public CssOnlyCleaningJsoupHandler setCssPrefix(final String cssPrefix) {
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
     * Sets whether to replace image URLs.
     *
     * @param dropExternalImages <code>true</code> to replace image URLs; otherwise <code>false</code>
     * @return This handler with new behavior applied
     */
    public CssOnlyCleaningJsoupHandler setDropExternalImages(final boolean dropExternalImages) {
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
                checkCSS(cssBuffer.append(cleanCss), null, cssPrefix);
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
        }
    }

    @Override
    public void handleDataNode(DataNode dataNode) {
        if (isCss) {
            if (false == maxContentSizeExceeded) {
                /*
                 * Handle style attribute
                 */
                String cdata = dataNode.toString();
                checkCSS(cssBuffer.append(cdata), null, cssPrefix);
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
        }
    }

    @Override
    public void handleDocumentType(DocumentType documentType) {
        // Don't care
    }

    @Override
    public void handleTextNode(TextNode textNode) {
        if (isCss) {
            if (false == maxContentSizeExceeded) {
                /*
                 * Handle style attribute
                 */
                String content = textNode.toString();
                checkCSS(cssBuffer.append(content), null, cssPrefix);
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
        }
    }

    @Override
    public void handleXmlDeclaration(XmlDeclaration xmlDeclaration) {
        // Don't care
    }

    @Override
    public void handleElementEnd(Element element) {
        String name = Strings.asciiLowerCase(element.tagName());
        if (isCss && "style".equals(name)) {
            isCss = false;
        }
        if (isCss) {
            // Ignore end tags in CSS content
            return;
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
            addStartTag(element, tagName, true);
            return;
        }

        if ("style".equals(tagName)) {
            isCss = true;
        }
        addStartTag(element, tagName, false);
    }

    /**
     * Adds tag occurring in white list to HTML result.
     *
     * @param startTag The start tag to add
     * @param simple <code>true</code> to write a simple tag; otherwise <code>false</code>
     * @param allowedAttributes The allowed tag's attributes or <code>null</code> to allow all
     */
    private void addStartTag(Element startTag, String tagName, boolean simple) {
        // Handle start tag
        Attributes attributes = startTag.attributes();

        if (suppressLinks) {
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
                if (Strings.isNotEmpty(css)) {
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
                if (dropExternalImages) {
                    String val = attribute.getValue();
                    if ("background".equals(attr) && PATTERN_URL.matcher(val).matches()) {
                        attribute.setValue("");
                        imageURLFound = true;
                    } else if ("src".equals(attr) && ("img".equals(tagName) || "input".equals(tagName))) {
                        if (isInlineImage(val, true)) {
                            // Allow inline images
                        } else {
                            attribute.setValue("");
                            startTag.attr("data-original-src", val);
                            imageURLFound = true;
                        }
                    }
                }
            }
        }
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

    private List<Attribute> listFor(Attributes attributes) {
        List<Attribute> copy = new ArrayList<>(attributes.size());
        for (Attribute attribute : attributes) {
            copy.add(attribute);
        }
        return copy;
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
