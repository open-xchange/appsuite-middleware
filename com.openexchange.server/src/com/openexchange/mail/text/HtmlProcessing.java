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

package com.openexchange.mail.text;

import static com.openexchange.java.Strings.isWhitespace;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlSanitizeResult;
import com.openexchange.html.HtmlService;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.HashUtility;
import com.openexchange.tools.regex.MatcherReplacer;

/**
 * {@link HtmlProcessing} - Various methods for HTML processing for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlProcessing {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HtmlProcessing.class);

    private static final String CHARSET_US_ASCII = "US-ASCII";

    /**
     * Performs all the formatting for text content for a proper display according to specified user's mail settings.
     *
     * @param content The plain text content
     * @param usm The settings used for formatting content
     * @param mode The display mode
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static HtmlSanitizeResult formatTextForDisplay(final String content, final UserSettingMail usm, final DisplayMode mode, final int maxContentSize) {
        return formatContentForDisplay(content, null, false, null, null, usm, null, mode, false, maxContentSize);
    }

    /**
     * Performs all the formatting for text content for a proper display according to specified user's mail settings.
     *
     * @param content The plain text content
     * @param usm The settings used for formatting content
     * @param mode The display mode
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static String formatTextForDisplay(final String content, final UserSettingMail usm, final DisplayMode mode) {
        return formatTextForDisplay(content, usm, mode, -1).getContent();
    }

    /**
     * Performs all the formatting for HTML content for a proper display according to specified user's mail settings.
     *
     * @param content The HTML content
     * @param charset The character encoding
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise <code>false</code>
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static String formatHTMLForDisplay(final String content, final String charset, final Session session, final MailPath mailPath, final UserSettingMail usm, final boolean[] modified, final DisplayMode mode, final boolean embedded) {
        return formatHTMLForDisplay(content, charset, session, mailPath, usm, modified, mode, embedded, -1).getContent();
    }

    /**
     * Performs all the formatting for HTML content for a proper display according to specified user's mail settings.
     *
     * @param content The HTML content
     * @param charset The character encoding
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise <code>false</code>
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static String formatHTMLForDisplay(String content, String charset, Session session, MailPath mailPath, MailPath originalMailPath, UserSettingMail usm, boolean[] modified, DisplayMode mode, boolean embedded) {
        return formatHTMLForDisplay(content, charset, session, mailPath, originalMailPath, usm, modified, mode, embedded, -1).getContent();
    }

    /**
     * Performs all the formatting for HTML content for a proper display according to specified user's mail settings.
     *
     * @param content The HTML content
     * @param charset The character encoding
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise
     *            <code>false</code>
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static HtmlSanitizeResult formatHTMLForDisplay(String content, String charset, Session session, MailPath mailPath, UserSettingMail usm, boolean[] modified, DisplayMode mode, boolean embedded, int maxContentSize) {
        return formatHTMLForDisplay(content, charset, session, mailPath, null, usm, modified, mode, embedded, maxContentSize);
    }

    /**
     * Performs all the formatting for HTML content for a proper display according to specified user's mail settings.
     *
     * @param content The HTML content
     * @param charset The character encoding
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param originalMailPath The original mail path or <code>null</code>
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise
     *            <code>false</code>
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @see #formatContentForDisplay(String, String, boolean, Session, MailPath, UserSettingMail, boolean[], DisplayMode)
     * @return The formatted content
     */
    public static HtmlSanitizeResult formatHTMLForDisplay(String content, String charset, Session session, MailPath mailPath, MailPath originalMailPath, UserSettingMail usm, boolean[] modified, DisplayMode mode, boolean embedded, int maxContentSize) {
        return formatContentForDisplay(content, charset, true, session, mailPath, originalMailPath, usm, modified, mode, embedded, maxContentSize);
    }

    /**
     * Performs all the formatting for both text and HTML content for a proper display according to specified user's mail settings.
     * <p>
     * If content is <b>plain text</b>:<br>
     * <ol>
     * <li>Plain text content is converted to valid HTML if at least {@link DisplayMode#MODIFYABLE} is given</li>
     * <li>If enabled by settings simple quotes are turned to colored block quotes if {@link DisplayMode#DISPLAY} is given</li>
     * <li>HTML links and URLs found in content are going to be prepared for proper display if {@link DisplayMode#DISPLAY} is given</li>
     * </ol>
     * If content is <b>HTML</b>:<br>
     * <ol>
     * <li>Both inline and non-inline images found in HTML content are prepared according to settings if {@link DisplayMode#DISPLAY} is
     * given</li>
     * </ol>
     *
     * @param content The content
     * @param charset The character encoding (only needed by HTML content; may be <code>null</code> on plain text)
     * @param isHtml <code>true</code> if content is of type <code>text/html</code>; otherwise <code>false</code>
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter (only
     *            needed by HTML content; may be <code>null</code> on plain text)
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise
     *            <code>false</code>
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @return The formatted content
     */
    public static HtmlSanitizeResult formatContentForDisplay(String content, String charset, boolean isHtml, Session session, MailPath mailPath, UserSettingMail usm, boolean[] modified, DisplayMode mode, boolean embedded, int maxContentSize) {
        return formatContentForDisplay(content, charset, isHtml, session, mailPath, null, usm, modified, mode, embedded, maxContentSize);
    }

    private static final String COMMENT_ID = "anchor-5fd15ca8-a027-4b14-93ea-35de1747419e:";

    /**
     * Performs all the formatting for both text and HTML content for a proper display according to specified user's mail settings.
     * <p>
     * If content is <b>plain text</b>:<br>
     * <ol>
     * <li>Plain text content is converted to valid HTML if at least {@link DisplayMode#MODIFYABLE} is given</li>
     * <li>If enabled by settings simple quotes are turned to colored block quotes if {@link DisplayMode#DISPLAY} is given</li>
     * <li>HTML links and URLs found in content are going to be prepared for proper display if {@link DisplayMode#DISPLAY} is given</li>
     * </ol>
     * If content is <b>HTML</b>:<br>
     * <ol>
     * <li>Both inline and non-inline images found in HTML content are prepared according to settings if {@link DisplayMode#DISPLAY} is
     * given</li>
     * </ol>
     *
     * @param content The content
     * @param charset The character encoding (only needed by HTML content; may be <code>null</code> on plain text)
     * @param isHtml <code>true</code> if content is of type <code>text/html</code>; otherwise <code>false</code>
     * @param session The session
     * @param mailPath The message's unique path in mailbox
     * @param originalMailPath The original mail path or <code>null</code>
     * @param usm The settings used for formatting content
     * @param modified A <code>boolean</code> array with length <code>1</code> to store modified status of external images filter (only
     *            needed by HTML content; may be <code>null</code> on plain text)
     * @param mode The display mode
     * @param embedded <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise
     *            <code>false</code>
     * @param maxContentSize maximum number of bytes that is will be returned for content. '<=0' means unlimited.
     * @return The formatted content
     */
    public static HtmlSanitizeResult formatContentForDisplay(String content, String charset, boolean isHtml, Session session, MailPath mailPath, MailPath originalMailPath, UserSettingMail usm, boolean[] modified, DisplayMode mode, boolean embedded, int maxContentSize) {
        HtmlSanitizeResult retval = new HtmlSanitizeResult(content);
        final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
        if (isHtml) {
            if (DisplayMode.RAW.equals(mode)) {
                retval.setContent(content);
            } else {
                retval.setContent(htmlService.dropScriptTagsInHeader(content));

                if (DisplayMode.MODIFYABLE.isIncluded(mode) && usm.isDisplayHtmlInlineContent()) {
                    final boolean externalImagesAllowed = usm.isAllowHTMLImages();
                    // Resolve <base> tags
                    retval.setContent(htmlService.checkBaseTag(retval.getContent(), externalImagesAllowed));
                    // TODO: Use static string "o1p2e3n4-x5c6h7a8n9g0e" ?
                    final String cssPrefix = null == mailPath ? null : (embedded ? "ox-" + getHash(mailPath.toString(), 10) : null);
                    if (useSanitize()) {
                        // No need to generate well-formed HTML
                        if (externalImagesAllowed) {
                            retval = htmlService.sanitize(retval.getContent(), null, false, null, cssPrefix, maxContentSize);
                        } else {
                            retval = htmlService.sanitize(retval.getContent(), null, true, modified, cssPrefix, maxContentSize);
                        }
                    } else {
                        retval.setContent(htmlService.getConformHTML(retval.getContent(), charset == null ? CHARSET_US_ASCII : charset, false));
                        /*
                         * Filter according to white-list
                         */
                        retval.setContent(htmlService.filterWhitelist(retval.getContent()));
                        if (externalImagesAllowed) {
                            /*
                             * TODO: Does not work reliably by now
                             */
                            // retval = htmlService.checkExternalImages(retval);
                        } else {
                            retval.setContent(htmlService.filterExternalImages(retval.getContent(), modified));
                        }
                    }
                    /*
                     * Filter inlined images
                     */
                    if (null != session) {
                        if (null != originalMailPath) {
                            retval.setContent(filterInlineImages(retval.getContent(), session, originalMailPath));
                        } else if (null != mailPath) {
                            retval.setContent(filterInlineImages(retval.getContent(), session, mailPath));
                        }
                    }
                    if (embedded) {
                        /*
                         * Replace <body> with <div>
                         */
                        retval.setContent(replaceBodyWithJericho(retval.getContent(), cssPrefix));
                    }
                }
            }
        } else {
            if (DisplayMode.MODIFYABLE.isIncluded(mode)) {
                if (DisplayMode.DISPLAY.isIncluded(mode)) {
                    retval.setContent(htmlService.formatURLs(retval.getContent(), COMMENT_ID));
                    retval = htmlService.htmlFormat(retval.getContent(), true, COMMENT_ID, maxContentSize);
                    if (usm.isUseColorQuote()) {
                        retval.setContent(replaceHTMLSimpleQuotesForDisplay(retval.getContent()));
                    }
                    if (DisplayMode.DOCUMENT.equals(mode)) {
                        retval.setContent(htmlService.getConformHTML(retval.getContent(), "UTF-8"));
                    }
                } else {
                    retval = htmlService.htmlFormat(retval.getContent(), true, null, maxContentSize);
                }
            }
        }
        return retval;
    }

    private static volatile Boolean useSanitize;

    /**
     * Whether to use sanitize.
     */
    public static boolean useSanitize() {
        if (null == useSanitize) {
            synchronized (HtmlProcessing.class) {
                if (null == useSanitize) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    useSanitize = Boolean.valueOf((null == service) || (service.getBoolProperty("com.openexchange.mail.text.useSanitize", true)));
                }
            }
        }
        return useSanitize.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                useSanitize = null;
                imageHost = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.text.useSanitize", "com.openexchange.mail.imageHost");
            }
        });
    }

    /**
     * Calculates the MD5 for given string.
     *
     * @param str The string
     * @param maxLen The max. length or <code>-1</code>
     * @return The MD5 hash
     */
    public static String getHash(final String str, final int maxLen) {
        if (isEmpty(str)) {
            return str;
        }
        if (maxLen <= 0) {
            return HashUtility.getHash(str, "md5", "hex");
        }
        return abbreviate(HashUtility.getHash(str, "md5", "hex"), 0, maxLen);
    }

    private static String abbreviate(final String str, int offset, final int maxWidth) {
        if (str == null) {
            return null;
        }
        final int length = str.length();
        if (length <= maxWidth) {
            return str;
        }
        int off = offset;
        if (off > length) {
            off = length;
        }
        if ((length - off) < (maxWidth)) {
            off = length - (maxWidth);
        }
        if (off < 1) {
            return str.substring(0, maxWidth);
        }
        if ((off + (maxWidth)) < length) {
            return abbreviate(str.substring(off), 0, maxWidth);
        }
        return str.substring(length - (maxWidth));
    }

    /**
     * Replaces &lt;body&gt; tag with an appropriate &lt;div&gt; tag.
     *
     * @param htmlContent The HTML content
     * @param cssPrefix The CSS prefix
     * @return The HTML content with replaced body tag
     */
    private static String replaceBodyWithJericho(String htmlContent, String cssPrefix) {
        Source source = new Source(htmlContent);
        source.fullSequentialParse();
        OutputDocument outputDocument = new OutputDocument(source);

        List<Element> htmlElements = source.getAllElements(HTMLElementName.HTML);
        if (null == htmlElements || htmlElements.isEmpty()) {
            // No <html> element
            lookUpHeadAndReplaceBody(source, outputDocument, source, cssPrefix);
            return outputDocument.toString().trim();

            //replaceAllBodiesIfPresentElseSurround(source, outputDocument, null, cssPrefix);
            //return outputDocument.toString().trim();
        }

        for (Element htmlElement : htmlElements) {
            lookUpHeadAndReplaceBody(source, outputDocument, htmlElement, cssPrefix);
        }
        return outputDocument.toString().trim();
    }

    private static Set<String> ALLOWED_IN_HEAD = ImmutableSet.<String> builder().add("head", "title", "style", "base", "link", "meta", "script", "noscript").build();

    private static void lookUpHeadAndReplaceBody(Source source, OutputDocument outputDocument, Segment toLookIn, String cssPrefix) {
        Element headElement = toLookIn.getFirstElement(HTMLElementName.HEAD);

        List<Element> styleElements = null;
        List<Element> other = null;
        if (null != headElement) {
            // Grab all <style> elements
            styleElements = headElement.getAllElements(HTMLElementName.STYLE);

            // Check for other content that should not reside in <head>
            List<Element> allElements = headElement.getChildElements();
            if (null != allElements) {
                for (Element element : allElements) {
                    if (false == ALLOWED_IN_HEAD.contains(Strings.asciiLowerCase(element.getName()))) {
                        if (null == other) {
                            other = new ArrayList<>(allElements.size());
                        }
                        other.add(element);
                    }
                }
            }
        }

        replaceAllBodiesIfPresentElseSurround(source, outputDocument, styleElements, other, null == headElement ? 0 : headElement.getBegin(), cssPrefix);

        if (toLookIn instanceof Element) {
            Element element = (Element) toLookIn;
            outputDocument.remove(element.getStartTag());
            if (null != headElement) {
                outputDocument.remove(headElement);
            }
            EndTag endTag = element.getEndTag();
            if (null != endTag) {
                outputDocument.remove(endTag);
            }
        } else {
            if (null != headElement) {
                outputDocument.remove(headElement);
            }
        }
    }

    private static void replaceAllBodiesIfPresentElseSurround(Source source, OutputDocument outputDocument, List<Element> styleElements, List<Element> other, int insertPosIfBodyAbsent, String cssPrefix) {
        List<Element> bodyElements = source.getAllElements(HTMLElementName.BODY);
        int numOfBodies;
        if (null == bodyElements || 0 == (numOfBodies = bodyElements.size())) {
            // No body - Append closing <div>
            outputDocument.insert(source.length(), "</div>");

            // Insert in reverse order
            if (null != other) {
                Collections.reverse(other);
                for (Element element : other) {
                    outputDocument.insert(insertPosIfBodyAbsent, element);
                }
            }

            if (null != styleElements) {
                Collections.reverse(styleElements);
                for (Element styleElement : styleElements) {
                    outputDocument.insert(insertPosIfBodyAbsent, styleElement);
                }
            }

            outputDocument.insert(0, "<div id=\"" + cssPrefix + "\">");
            return;
        }

        if (1 == numOfBodies) {
            Element bodyElement = bodyElements.get(0);
            Segment content = bodyElement.getContent();
            StringBuilder sb = new StringBuilder(content.length());
            getDivStartTagHTML(bodyElement.getStartTag(), cssPrefix, sb);
            if (null != styleElements) {
                for (Element styleElement : styleElements) {
                    sb.append(styleElement);
                }
            }
            if (null != other) {
                for (Element element : other) {
                    sb.append(element);
                }
            }
            sb.append(content);
            sb.append("</div>");
            outputDocument.replace(bodyElement, sb);
            return;
        }

        for (Element bodyElement : bodyElements) {
            Segment content = bodyElement.getContent();
            StringBuilder sb = new StringBuilder(content.length());
            getDivStartTagHTML(bodyElement.getStartTag(), cssPrefix, sb);
            if (null != styleElements) {
                for (Element styleElement : styleElements) {
                    sb.append(styleElement);
                }
            }
            if (null != other) {
                for (Element element : other) {
                    sb.append(element);
                }
            }
            sb.append(content);
            sb.append("</div>");
            outputDocument.replace(bodyElement, sb);
        }
    }

    private static void getDivStartTagHTML(StartTag startTag, String cssPrefix, StringBuilder appendTo) {
        // Tidies and filters out non-approved attributes
        appendTo.append("<div id=\"").append(cssPrefix).append('"');

        for (Attribute attribute : startTag.getAttributes()) {
            if (!"id".equals(attribute.getKey())) {
                appendTo.append(' ').append(attribute.getName());
                if (attribute.getValue() != null) {
                    appendTo.append("=\"").append(CharacterReference.encode(attribute.getValue())).append('"');
                }
            }
        }

        appendTo.append('>');
    }

    private static final Pattern PATTERN_CSS_CLASS_NAME = Pattern.compile("\\s?\\.[a-zA-Z0-9\\s:,\\.#_-]*\\s*\\{.*?\\}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_HTML_BODY = Pattern.compile("<body.*?>(.*?)</body>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_BODY_TAG = Pattern.compile("(<body.*?>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_BODY_STYLE = Pattern.compile("(style=[\"].*?[\"]|style=['].*?['])", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_BODY_CLASS = Pattern.compile("(class=[\"].*?[\"]|class=['].*?['])", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes possible CSS style sheets contained in provided HTML content.
     *
     * @param htmlContent The HTML content
     * @param optHtmlService The optional HTML service
     * @return The HTML content with sanitized CSS style sheets
     */
    public static String saneCss(final String htmlContent, final HtmlService optHtmlService, final String cssPrefix) {
        if (null == htmlContent) {
            return null;
        }
        String retval = htmlContent;
        final String css = (optHtmlService == null ? ServerServiceRegistry.getInstance().getService(HtmlService.class) : optHtmlService).getCSSFromHTMLHeader(retval);
        final Matcher cssClassMatcher = PATTERN_CSS_CLASS_NAME.matcher(css);
        if (cssClassMatcher.find()) {
            // Examine body tag
            final Matcher bodyTagMatcher = PATTERN_BODY_TAG.matcher(retval);
            String className = "", styleName = "";
            if (bodyTagMatcher.find()) {
                final String body = bodyTagMatcher.group(1);
                Matcher m = PATTERN_BODY_CLASS.matcher(body);
                if (m.find()) {
                    className = m.group();
                }
                m = PATTERN_BODY_STYLE.matcher(body);
                if (m.find()) {
                    styleName = m.group();
                }
            }
            // Proceed replacing CSS
            final StringBuilder tmp = new StringBuilder(64);
            String newCss = css;
            do {
                final String cssClass = cssClassMatcher.group();
                tmp.setLength(0);
                newCss =
                    newCss.replace(cssClass, tmp.append('#').append(cssPrefix).append(' ').append(cssClass).toString());
            } while (cssClassMatcher.find());
            tmp.setLength(0);
            newCss = tmp.append("<style>").append(newCss).append("</style>").toString();
            retval = HtmlProcessing.dropStyles(retval);
            final Matcher htmlBodyMatcher = PATTERN_HTML_BODY.matcher(retval);
            if (htmlBodyMatcher.find()) {
                tmp.setLength(0);
                retval =
                    tmp.append(newCss).append("<div id=\"").append(cssPrefix).append("\" ").append(className).append(' ').append(
                        styleName).append('>').append(htmlBodyMatcher.group(1)).append("</div>").toString();
            }
        }
        return retval;
    }

    private static final Pattern PATTERN_STYLE = Pattern.compile("<style.*?>.*?</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_STYLE_FILE = Pattern.compile("<link.*?(type=['\"]text/css['\"].*?href=['\"](.*?)['\"]|href=['\"](.*?)['\"].*?type=['\"]text/css['\"]).*?/>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Drops CSS style sheet information from given HTML content.
     *
     * @param htmlContent The HTML content
     * @return The HTML content cleansed by CSS style sheet information
     */
    private static String dropStyles(final String htmlContent) {
        final StringBuffer buf = new StringBuffer(htmlContent.length());

        Matcher matcher = PATTERN_STYLE.matcher(htmlContent);
        while (matcher.find()) {
            matcher.appendReplacement(buf, "");
        }
        matcher.appendTail(buf);

        matcher = PATTERN_STYLE_FILE.matcher(buf.toString());
        buf.setLength(0);
        while (matcher.find()) {
            matcher.appendReplacement(buf, "");
        }
        matcher.appendTail(buf);

        return buf.toString();
    }

    /**
     * Converts specified HTML content to plain text.
     *
     * @param htmlContent The <b>validated</b> HTML content
     * @param appendHref <code>true</code> to append URLs contained in <i>href</i>s and <i>src</i>s; otherwise <code>false</code>.<br>
     *            Example: <code>&lt;a&nbsp;href=\"www.somewhere.com\"&gt;Link&lt;a&gt;</code> would be
     *            <code>Link&nbsp;[www.somewhere.com]</code>
     * @return The plain text representation of specified HTML content
     */
    public static String html2text(final String htmlContent, final boolean appendHref) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).html2text(htmlContent, appendHref);
    }

    /**
     * Searches for non-HTML links and convert them to valid HTML links.
     * <p>
     * Example: <code>http://www.somewhere.com</code> is converted to
     * <code>&lt;a&nbsp;href=&quot;http://www.somewhere.com&quot;&gt;http://www.somewhere.com&lt;/a&gt;</code>.
     *
     * @param content The content to search in
     * @return The given content with all non-HTML links converted to valid HTML links
     */
    public static String formatHrefLinks(final String content) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).formatHrefLinks(content);
    }

    /**
     * Creates valid HTML from specified HTML content conform to W3C standards.
     *
     * @param htmlContent The HTML content
     * @param contentType The corresponding content type (including charset parameter)
     * @return The HTML content conform to W3C standards
     */
    public static String getConformHTML(final String htmlContent, final ContentType contentType) {
        return getConformHTML(htmlContent, contentType.getCharsetParameter());
    }

    /**
     * Creates valid HTML from specified HTML content conform to W3C standards.
     *
     * @param htmlContent The HTML content
     * @param charset The charset parameter
     * @return The HTML content conform to W3C standards
     */
    public static String getConformHTML(final String htmlContent, final String charset) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).getConformHTML(htmlContent, charset);
    }

    /**
     * Creates a {@link Document DOM document} from specified XML/HTML string.
     *
     * @param string The XML/HTML string
     * @return A newly created DOM document or <code>null</code> if given string cannot be transformed to a DOM document
     */
    public static Document createDOMDocument(final String string) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(string)));
        } catch (final ParserConfigurationException e) {
            LOG.error("", e);
        } catch (final SAXException e) {
            LOG.error("", e);
        } catch (final IOException e) {
            LOG.error("", e);
        } catch (final Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    /**
     * Pretty-prints specified XML/HTML string.
     *
     * @param string The XML/HTML string to pretty-print
     * @return The pretty-printed XML/HTML string
     */
    public static String prettyPrintXML(final String string) {
        return prettyPrintXML(createDOMDocument(string), string);
    }

    /**
     * Pretty-prints specified XML/HTML node.
     *
     * @param node The XML/HTML node pretty-print
     * @return The pretty-printed XML/HTML node
     */
    public static String prettyPrintXML(final Node node) {
        return prettyPrintXML(node, null);
    }

    private static final int INDENT = 2;

    /**
     * Pretty-prints specified XML/HTML string.
     *
     * @param node The XML/HTML node pretty-print
     * @param fallback The fallback string to return on error
     * @return The pretty-printed XML/HTML string
     */
    private static String prettyPrintXML(final Node node, final String fallback) {
        if (null == node) {
            return fallback;
        }
        /*
         * Pretty-print using Transformer
         */
        final TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            tfactory.setAttribute("indent-number", new Integer(INDENT));
            final Transformer serializer = tfactory.newTransformer();
            /*
             * Setup indenting to "pretty print"
             */
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(INDENT));
            final AllocatingStringWriter sw = new AllocatingStringWriter();
            serializer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (final TransformerException e) {
            LOG.error("", e);
            return fallback;
        } catch (final Exception e) {
            LOG.error("", e);
            return fallback;
        }
    }

    /**
     * Pretty prints specified HTML content.
     *
     * @param htmlContent The HTML content
     * @return Pretty printed HTML content
     */
    public static String prettyPrint(final String htmlContent) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).prettyPrint(htmlContent);
    }

    /**
     * Replaces all HTML entities occurring in specified HTML content.
     *
     * @param content The content
     * @return The content with HTML entities replaced
     */
    public static String replaceHTMLEntities(final String content) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).replaceHTMLEntities(content);
    }

    /**
     * Maps specified HTML entity - e.g. <code>&amp;uuml;</code> - to corresponding ASCII character.
     *
     * @param entity The HTML entity
     * @return The corresponding ASCII character or <code>null</code>
     */
    public static Character getHTMLEntity(final String entity) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).getHTMLEntity(entity);
    }

    /**
     * Formats plain text to HTML by escaping HTML special characters e.g. <code>&quot;&lt;&quot;</code> is converted to
     * <code>&quot;&amp;lt;&quot;</code>.
     *
     * @param plainText The plain text
     * @param withQuote Whether to escape quotes (<code>&quot;</code>) or not
     * @return properly escaped HTML content
     */
    public static String htmlFormat(final String plainText, final boolean withQuote) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).htmlFormat(plainText, withQuote);
    }

    /**
     * Formats plain text to HTML by escaping HTML special characters e.g. <code>&quot;&lt;&quot;</code> is converted to
     * <code>&quot;&amp;lt;&quot;</code>.
     * <p>
     * This is just a convenience method which invokes <code>{@link #htmlFormat(String, boolean)}</code> with latter parameter set to
     * <code>true</code>.
     *
     * @param plainText The plain text
     * @return properly escaped HTML content
     * @see #htmlFormat(String, boolean)
     */
    public static String htmlFormat(final String plainText) {
        return ServerServiceRegistry.getInstance().getService(HtmlService.class).htmlFormat(plainText);
    }

    private static final String DEFAULT_COLOR = "#0026ff";

    private static final String BLOCKQUOTE_START_TEMPLATE = "<blockquote type=\"cite\" style=\"margin-left: 0px; margin-right: 0px; padding-left: 10px; color:%s; border-left: solid 1px %s;\">";

    /**
     * Determines the quote color for given <code>quotelevel</code>.
     *
     * @param quotelevel - the quote level
     * @return The color for given <code>quotelevel</code>
     */
    private static String getLevelColor(final int quotelevel) {
        final String[] colors = MailProperties.getInstance().getQuoteLineColors();
        return (colors != null) && (colors.length > 0) ? (quotelevel >= colors.length ? colors[colors.length - 1] : colors[quotelevel]) : DEFAULT_COLOR;
    }

    private static final String BLOCKQUOTE_END = "</blockquote>\n";

    private static final String STR_HTML_QUOTE = "&gt;";

    private static final String STR_SPLIT_BR = "<br[ \t]*/?>";

    private static final String HTML_BREAK = "<br>";

    /**
     * Turns all simple quotes "&amp;gt; " occurring in specified HTML text to colored "&lt;blockquote&gt;" tags according to configured
     * quote colors.
     *
     * @param htmlText The HTML text
     * @return The HTML text with simple quotes replaced with block quotes
     */
    private static String replaceHTMLSimpleQuotesForDisplay(final String htmlText) {
        final StringBuilder sb = new StringBuilder(htmlText.length());
        final String[] lines = htmlText.split(STR_SPLIT_BR);
        int levelBefore = 0;
        final int llen = lines.length - 1;
        for (int i = 0; i <= llen; i++) {
            String line = lines[i];
            int currentLevel = 0;
            if (line.trim().equalsIgnoreCase("&gt;")) {
                currentLevel++;
                line = "";
            } else {
                int offset = 0;
                if ((offset = startsWithQuote(line)) != -1) {
                    currentLevel++;
                    int pos = -1;
                    boolean next = true;
                    while (next && ((pos = line.indexOf(STR_HTML_QUOTE, offset)) > -1)) {
                        /*
                         * Continue only if next starting position is equal to offset or if just one whitespace character has been skipped
                         */
                        next = ((offset == pos) || ((pos - offset == 1) && Strings.isWhitespace(line.charAt(offset))));
                        if (next) {
                            currentLevel++;
                            offset = (pos + 4);
                        }
                    }
                }
                if (offset > 0) {
                    try {
                        offset = (offset < line.length()) && Strings.isWhitespace(line.charAt(offset)) ? offset + 1 : offset;
                    } catch (final StringIndexOutOfBoundsException e) {
                        LOG.trace("", e);
                    }
                    line = line.substring(offset);
                }
            }
            if (levelBefore < currentLevel) {
                for (; levelBefore < currentLevel; levelBefore++) {
                    final String color = getLevelColor(levelBefore);
                    sb.append(String.format(BLOCKQUOTE_START_TEMPLATE, color, color));
                }
            } else if (levelBefore > currentLevel) {
                for (; levelBefore > currentLevel; levelBefore--) {
                    sb.append(BLOCKQUOTE_END);
                }
            }
            sb.append(line);
            if (i < llen) {
                sb.append(HTML_BREAK);
            }
        }
        return sb.toString();
    }

    // private static final Pattern PAT_STARTS_WITH_QUOTE = Pattern.compile("\\s*&gt;\\s*", Pattern.CASE_INSENSITIVE);

    /**
     * Checks if passed String matches (ignore-case) to <code>"\s*&amp;gt;\s*"</code>.
     *
     * @param str The String to check
     * @return <code>true</code> if String matches (ignore-case) to <code>"\s*&amp;gt;\s*"</code>; otherwise <code>false</code>
     */
    private static int startsWithQuote(final String str) {
        if (isEmpty(str)) {
            return -1;
        }
        // Detect starting "> "
        final int mlen = str.length() - 1;
        if (mlen < 3) {
            return -1;
        }
        int i = 0;
        char c = str.charAt(i);
        while (isWhitespace(c)) {
            if (i >= mlen) {
                return -1;
            }
            c = str.charAt(++i);
        }
        if ((c != '&') || (i >= mlen)) {
            return -1;
        }
        c = str.charAt(++i);
        if (((c != 'g') && (c != 'G')) || (i >= mlen)) {
            return -1;
        }
        c = str.charAt(++i);
        if (((c != 't') && (c != 'T')) || (i >= mlen)) {
            return -1;
        }
        c = str.charAt(++i);
        if (c != ';') {
            return -1;
        }
        if (i >= mlen) {
            return i;
        }
        c = str.charAt(++i);
        while (isWhitespace(c)) {
            if (i >= mlen) {
                return i;
            }
            c = str.charAt(++i);
        }
        return i;
    }

    private static final Pattern BACKGROUND_CSS_PATTERN = Pattern.compile(
        "(background|background-image\\s*:\\s*)url\\(cid:([^\\s>\\)]*)\\)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern BACKGROUND_PATTERN = Pattern.compile(
        "(<[a-zA-Z]+[^>]*?)(?:(?:background=cid:([^\\s>]*))|(?:background=\"cid:([^\"]*)\"))([^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern CID_PATTERN = Pattern.compile(
        "(?:src=cid:([^\\s>]*))|(?:src=\"cid:([^\"]*)\")",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
        "src=\"?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)\"?",
        Pattern.CASE_INSENSITIVE);

    /**
     * Filters inline images occurring in HTML content of a message:
     * <ul>
     * <li>Inline images<br>
     * The source of inline images is in the message itself. Thus loading the inline image is redirected to the appropriate message (image)
     * attachment identified through header <code>Content-Id</code>; e.g.: <code>&lt;img
     * src=&quot;cid:[cid-value]&quot; ... /&gt;</code>.</li>
     * </ul>
     *
     * @param content The HTML content possibly containing images
     * @param session The session
     * @param msgUID The message's unique path in mailbox
     * @return The HTML content with all inline images replaced with valid links
     */
    public static String filterInlineImages(String content, Session session, MailPath msgUID) {
        return filterInlineImages(content, session, msgUID, DefaultImageUriGenerator.getInstance());
    }

    /**
     * Filters inline images occurring in HTML content of a message:
     * <ul>
     * <li>Inline images<br>
     * The source of inline images is in the message itself. Thus loading the inline image is redirected to the appropriate message (image)
     * attachment identified through header <code>Content-Id</code>; e.g.: <code>&lt;img
     * src=&quot;cid:[cid-value]&quot; ... /&gt;</code>.</li>
     * </ul>
     *
     * @param content The HTML content possibly containing images
     * @param session The session
     * @param msgUID The message's unique path in mailbox
     * @param generator The image URI generator to use
     * @return The HTML content with all inline images replaced with valid links
     */
    public static String filterInlineImages(String content, Session session, MailPath msgUID, ImageUriGenerator generator) {
        String ret = filterImgInlineImages(content, session, msgUID, generator);
        ret = filterBackgroundInlineImages(ret, session, msgUID, generator);
        ret = filterBackgroundCssInlineImages(ret, session, msgUID, generator);
        return ret;
    }

    private static volatile String imageHost;

    /**
     * Gets the optional image host
     *
     * @return The optional image host
     */
    public static String imageHost() {
        String tmp = imageHost;
        if (null == tmp) {
            synchronized (HtmlProcessing.class) {
                tmp = imageHost;
                if (null == tmp) {
                    final ConfigurationService cs = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == cs) {
                        // No ConfigurationService available at the moment
                        return "";
                    }
                    tmp = cs.getProperty("com.openexchange.mail.imageHost", "");
                    imageHost = tmp;
                }
            }
        }
        return tmp;
    }

    private static String filterBackgroundCssInlineImages(final String content, final Session session, final MailPath msgUID, ImageUriGenerator generator) {
        String reval = content;
        try {
            final Matcher imgMatcher = BACKGROUND_CSS_PATTERN.matcher(reval);
            final MatcherReplacer imgReplacer = new MatcherReplacer(imgMatcher, reval);
            final StringBuilder sb = new StringBuilder(reval.length());
            if (imgMatcher.find()) {
                final StringBuilder linkBuilder = new StringBuilder(256);
                // Replace inline images with Content-ID
                do {
                    // Extract Content-ID
                    String cid = imgMatcher.group(2);

                    // Compose corresponding image data
                    linkBuilder.setLength(0);
                    linkBuilder.append(imgMatcher.group(1)).append("url(");
                    linkBuilder.append(generator.getPlainImageUri(cid, msgUID, session));
                    linkBuilder.append(")");
                    imgReplacer.appendLiteralReplacement(sb, 0 == linkBuilder.length() ? imgMatcher.group() : linkBuilder.toString());
                } while (imgMatcher.find());
            }
            imgReplacer.appendTail(sb);
            reval = sb.toString();
        } catch (final Exception e) {
            LOG.warn("Unable to filter cid background images: {}", e.getMessage());
        }
        return reval;
    }

    private static String filterBackgroundInlineImages(final String content, final Session session, final MailPath msgUID, ImageUriGenerator generator) {
        String reval = content;
        try {
            final Matcher imgMatcher = BACKGROUND_PATTERN.matcher(reval);
            final MatcherReplacer imgReplacer = new MatcherReplacer(imgMatcher, reval);
            final StringBuilder sb = new StringBuilder(reval.length());
            if (imgMatcher.find()) {
                final StringBuilder linkBuilder = new StringBuilder(256);
                // Replace inline images with Content-ID
                do {
                    // Extract Content-ID
                    String cid = imgMatcher.group(2);
                    if (cid == null) {
                        cid = imgMatcher.group(3);
                    }

                    // Compose corresponding image data
                    linkBuilder.setLength(0);
                    String prefix = linkBuilder.append(imgMatcher.group(1)).append("background=").toString();
                    linkBuilder.setLength(0);
                    generator.generateImageUri(linkBuilder, prefix, imgMatcher.group(4), cid, msgUID, session);
                    imgReplacer.appendLiteralReplacement(sb, 0 == linkBuilder.length() ? imgMatcher.group() : linkBuilder.toString());
                } while (imgMatcher.find());
            }
            imgReplacer.appendTail(sb);
            reval = sb.toString();
        } catch (final Exception e) {
            LOG.warn("Unable to filter cid background images: {}", e.getMessage());
        }
        return reval;
    }

    /**
     * Filters inline images occurring in HTML content of a message:
     * <ul>
     * <li>Inline images<br>
     * The source of inline images is in the message itself. Thus loading the inline image is redirected to the appropriate message (image)
     * attachment identified through header <code>Content-Id</code>; e.g.: <code>&lt;img
     * src=&quot;cid:[cid-value]&quot; ... /&gt;</code>.</li>
     * </ul>
     *
     * @param content The HTML content possibly containing images
     * @param session The session
     * @param msgUID The message's unique path in mailbox
     * @param generator
     * @return The HTML content with all inline images replaced with valid links
     */
    private static String filterImgInlineImages(final String content, final Session session, final MailPath msgUID, ImageUriGenerator generator) {
        String reval = content;
        try {
            final Matcher imgMatcher = IMG_PATTERN.matcher(reval);
            final MatcherReplacer imgReplacer = new MatcherReplacer(imgMatcher, reval);
            final StringBuilder sb = new StringBuilder(reval.length());
            if (imgMatcher.find()) {
                final StringBuilder strBuffer = new StringBuilder(256);
                final MatcherReplacer mr = new MatcherReplacer();
                final StringBuilder linkBuilder = new StringBuilder(256);
                /*
                 * Replace inline images with Content-ID
                 */
                do {
                    final String imgTag = imgMatcher.group();
                    if (!(replaceImgSrc(session, msgUID, imgTag, strBuffer, linkBuilder, generator))) {
                        /*
                         * No cid pattern found, try with filename
                         */
                        strBuffer.setLength(0);
                        final Matcher m = FILENAME_PATTERN.matcher(imgTag);
                        mr.resetTo(m, imgTag);
                        if (m.find()) {
                            final String filename = m.group(1);
                            /*
                             * Compose corresponding image data
                             */
                            linkBuilder.setLength(0);
                            generator.generateImageUri(linkBuilder, "src=", null, filename, msgUID, session);
                            mr.appendLiteralReplacement(strBuffer, 0 == linkBuilder.length() ? imgMatcher.group() : linkBuilder.toString());
                        }
                        mr.appendTail(strBuffer);
                    }
                    imgReplacer.appendLiteralReplacement(sb, strBuffer.toString());
                    strBuffer.setLength(0);
                } while (imgMatcher.find());
            }
            imgReplacer.appendTail(sb);
            reval = sb.toString();
        } catch (final Exception e) {
            LOG.warn("Unable to filter cid Images: {}", e.getMessage());
        }
        return reval;
    }

    private static boolean replaceImgSrc(final Session session, final MailPath msgUID, final String imgTag, final StringBuilder cidBuffer, final StringBuilder linkBuilder, ImageUriGenerator generator) throws OXException {
        boolean retval = false;
        final Matcher cidMatcher = CID_PATTERN.matcher(imgTag);
        final MatcherReplacer cidReplacer = new MatcherReplacer(cidMatcher, imgTag);
        if (cidMatcher.find()) {
            retval = true;
            do {
                /*
                 * Extract Content-ID
                 */
                String cid = cidMatcher.group(2);
                if (cid == null) {
                    cid = cidMatcher.group(1);
                }
                /*
                 * Compose corresponding image data
                 */
                linkBuilder.setLength(0);
                generator.generateImageUri(linkBuilder, "src=", null, cid, msgUID, session);
                cidReplacer.appendLiteralReplacement(cidBuffer, 0 == linkBuilder.length() ? cidMatcher.group() : linkBuilder.toString());
            } while (cidMatcher.find());
        }
        cidReplacer.appendTail(cidBuffer);
        return retval;
    }

    /**
     * Translates specified string into application/x-www-form-urlencoded format using a specific encoding scheme. This method uses the
     * supplied encoding scheme to obtain the bytes for unsafe characters.
     *
     * @param text The string to be translated.
     * @param charset The character encoding to use; should be <code>UTF-8</code> according to W3C
     * @return The translated string or the string itself if any error occurred
     */
    public static String urlEncodeSafe(final String text, final String charset) {
        try {
            return URLEncoder.encode(text, charset);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("", e);
            return text;
        }
    }

    private static void dumpToFile(final String content, final String fileName) {
        if (isEmpty(content)) {
            return;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.flush();
        } catch (final IOException e) {
            LOG.error("", e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
        } finally {
            Streams.close(writer);
        }
    }

    private static boolean isEmpty(final CharSequence charSeq) {
        if (null == charSeq) {
            return true;
        }
        final int len = charSeq.length();
        boolean isWhitespace = true;
        for (int i = len; isWhitespace && i-- > 0;) {
            isWhitespace = isWhitespace(charSeq.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Initializes a new {@link HtmlProcessing}.
     */
    private HtmlProcessing() {
        super();
    }
}
