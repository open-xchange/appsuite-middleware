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

package com.openexchange.html.internal;

import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL;
import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.parser.handler.HTMLFilterHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;

/**
 * {@link HtmlWhitelistFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlWhitelistFilter {

    /**
     * Gets the default {@link HtmlWhitelistFilter} instance.
     * @param capacity The capacity
     *
     * @return The default {@link HtmlWhitelistFilter} instance
     */
    public static HtmlWhitelistFilter newDefaultHtmlWhitelistFilter(final int capacity) {
        return new HtmlWhitelistFilter(capacity);
    }

    private static final Set<String> NUM_ATTRIBS = new HashSet<String>(0);

    // A decimal digit: [0-9]
    private static final Pattern PAT_NUMERIC = Pattern.compile("\\p{Digit}+");

    private static final Object VALID_MARKER = new Object();

    private static final String BACKGROUND = "background";

    private static final String CRLF = "\r\n";

    private static final String STYLE = "style";

    private static final Map<String, Set<String>> IMAGE_STYLE_MAP;

    static {
        IMAGE_STYLE_MAP = new HashMap<String, Set<String>>();
        Set<String> values = new HashSet<String>();
        /*
         * background
         */
        values.add("Nc");
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
        IMAGE_STYLE_MAP.put(BACKGROUND, values);
        values = new HashSet<String>();
        /*
         * background-image
         */
        values.add("d"); // delete
        IMAGE_STYLE_MAP.put("background-image", values);
    }

    private static volatile Map<String, Map<String, Set<String>>> staticHTMLMap;

    private static volatile Map<String, Set<String>> staticStyleMap;

    /*-
     * Member stuff
     */

    private final Map<String, Map<String, Set<String>>> htmlMap;

    private final Map<String, Set<String>> styleMap;

    private final Stringer cssBuffer;

    private final StringBuilder sb;

    private boolean body;

    private boolean imageURLFound;

    /**
     * Initializes a new {@link HtmlWhitelistFilter}.
     */
    public HtmlWhitelistFilter(final Map<String, Map<String, Set<String>>> htmlMap, final Map<String, Set<String>> styleMap) {
        super();
        sb = new StringBuilder(256);
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.htmlMap = htmlMap;
        this.styleMap = styleMap;
        if (!htmlMap.containsKey("html")) {
            htmlMap.put("html", null);
        }
        if (!htmlMap.containsKey("head")) {
            htmlMap.put("head", null);
        }
        if (!htmlMap.containsKey("body")) {
            htmlMap.put("body", null);
        }
    }

    /**
     * Initializes a new {@link HtmlWhitelistFilter}.
     */
    public HtmlWhitelistFilter(final int capacity, final String mapStr) {
        super();
        sb = new StringBuilder(capacity);
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
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
        htmlMap = Collections.unmodifiableMap(map);
        styleMap = Collections.unmodifiableMap(parseStyleMap(mapStr));
    }

    /**
     * Initializes a new {@link HtmlWhitelistFilter}.
     */
    private HtmlWhitelistFilter(final int capacity) {
        super();
        sb = new StringBuilder(capacity);
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        if (null == staticHTMLMap) {
            loadWhitelist();
        }
        htmlMap = staticHTMLMap;
        styleMap = staticStyleMap;
    }

    private static final Pattern PAT_HEX_ENTITIES = Pattern.compile("&#x([0-9a-fA-F]+);", Pattern.CASE_INSENSITIVE);

    private static String replaceHexEntities(final String validated) {
        final Matcher m = PAT_HEX_ENTITIES.matcher(validated);
        if (!m.find()) {
            return validated;
        }
        final MatcherReplacer mr = new MatcherReplacer(m, validated);
        final Stringer builder = new StringBuilderStringer(new StringBuilder(validated.length()));
        final StringBuilder tmp = new StringBuilder(8).append("&#");
        do {
            try {
                tmp.setLength(2);
                tmp.append(Integer.parseInt(m.group(1), 16)).append(';');
                mr.appendLiteralReplacement(builder, tmp.toString());
            } catch (final NumberFormatException e) {
                tmp.setLength(0);
                tmp.append("&amp;#x").append(m.group(1)).append("&#59;");
                mr.appendLiteralReplacement(builder, tmp.toString());
                tmp.setLength(0);
                tmp.append("&#");
            }
        } while (m.find());
        mr.appendTail(builder);
        return builder.toString();
    }

    /**
     * Checks whether an image URL was found and replaced.
     *
     * @return <code>true</code> if an image URL was found and replaced; otherwise <code>false</code>
     */
    public boolean isImageURLFound() {
        return imageURLFound;
    }

    /**
     * Sanitizes specified HTML content.
     *
     * @param pseudoHTML The possibly broken HTML content to sanitize
     * @param formatWhiteSpace Whether whitespace characters shall be replaced with HTML markup
     * @param stripInvalidElements Whether invalid elements shall be removed
     * @param dropExternalImages Whether image URLs shall be replaced with blank string
     * @return The sanitized HTML content
     */
    public String sanitize(final String pseudoHTML, final boolean formatWhiteSpace, final boolean stripInvalidElements, final boolean dropExternalImages) {
        // MicrosoftConditionalCommentTagTypes.register();
        // PHPTagTypes.register();
        // MasonTagTypes.register();
        final Source source = new Source(replaceHexEntities(pseudoHTML));
        source.fullSequentialParse();
        final OutputDocument outputDocument = new OutputDocument(source);
        final List<Tag> tags = source.getAllTags();
        int pos = 0;
        for (final Tag tag : tags) {
            if (processTag(tag, outputDocument, dropExternalImages)) {
                tag.setUserData(VALID_MARKER);
                // Handle content of an allowed tag
                if (HTMLElementName.STYLE == tag.getName()) {
                    reencodeCSSTextSegment(source, outputDocument, pos, tag.getBegin(), dropExternalImages);
                } else {
                    reencodeTextSegment(source, outputDocument, pos, tag.getBegin(), formatWhiteSpace);
                }
            } else {
                if (!stripInvalidElements) {
                    continue; // element will be encoded along with surrounding text
                }
                // Handle content of a forbidden tag
                outputDocument.remove(tag);
                if (HTMLElementName.STYLE == tag.getName()) {
                    reencodeCSSTextSegment(source, outputDocument, pos, tag.getBegin(), dropExternalImages);
                } else {
                    if (!body || isRemoveWholeTag(tag)) {
                        removeTextSegment(source, outputDocument, pos, tag.getBegin());
                    } else { // Within body: retain content
                        reencodeTextSegment(source, outputDocument, pos, tag.getBegin(), formatWhiteSpace);
                    }
                }
            }
            pos = tag.getEnd();
        }
        reencodeTextSegment(source, outputDocument, pos, source.getEnd(), formatWhiteSpace);
        return outputDocument.toString();
    }

    private boolean isRemoveWholeTag(final Tag tag) {
        final String check = tag.getName();
        return (HTMLElementName.SCRIPT == check || check.startsWith("w:worddocument") || check.startsWith("o:officedocumentsettings"));
    }

    private boolean processTag(final Tag tag, final OutputDocument outputDocument, final boolean dropExternalImages) {
        final String elementName = tag.getName();
        if (!htmlMap.containsKey(elementName)) {
            return false;
        }
        /*
         * Process allowed tag
         */
        final Map<String, Set<String>> allowedAttributes = htmlMap.get(elementName);
        if (tag.getTagType() == StartTagType.NORMAL) {
            if (HTMLElementName.BODY == elementName) {
                body = true;
            }
            final Element element = tag.getElement();
            if (HTMLElements.getEndTagRequiredElementNames().contains(elementName)) {
                if (element.getEndTag() == null) {
                    return false; // reject start tag if its required end tag is missing
                }
            } else if (HTMLElements.getEndTagOptionalElementNames().contains(elementName)) {
                if (HTMLElementName.LI == elementName && !isValidLITag(tag)) {
                    return false; // reject invalid LI tags
                }
                if (element.getEndTag() == null) {
                    outputDocument.insert(element.getEnd(), getEndTagHTML(elementName)); // insert optional end tag if it is missing
                }
            }
            /*
             * Replace start tag
             */
            outputDocument.replace(tag, getStartTagHTML(element.getStartTag(), allowedAttributes, dropExternalImages));
        } else if (tag.getTagType() == EndTagType.NORMAL) {
            if (tag.getElement() == null) {
                return false; // reject end tags that aren't associated with a start tag
            }
            if (HTMLElementName.LI == elementName && !isValidLITag(tag)) {
                return false; // reject invalid LI tags
            }
            if (body && HTMLElementName.BODY == elementName) {
                body = false;
            }
            /*
             * Replace end tag
             */
            outputDocument.replace(tag, getEndTagHTML(elementName));
        } else {
            return false; // reject abnormal tags
        }
        return true;
    }

    private static boolean isValidLITag(final Tag tag) {
        final Element parentElement = tag.getElement().getParentElement();
        if (parentElement == null) {
            return false; // ignore LI elements without a parent
        }
        if (parentElement.getStartTag().getUserData() != VALID_MARKER) {
            return false; // ignore LI elements who's parent is not valid
        }
        /*
         * only accept LI tags who's immediate parent is UL or OL.
         */
        return parentElement.getName() == HTMLElementName.UL || parentElement.getName() == HTMLElementName.OL;
    }

    private static void reencodeTextSegment(final Source source, final OutputDocument outputDocument, final int begin, final int end, final boolean formatWhiteSpace) {
        if (begin >= end) {
            return;
        }
        final Segment textSegment = new Segment(source, begin, end);
        final String decodedText = CharacterReference.decode(textSegment);
        final String encodedText =
            formatWhiteSpace ? CharacterReference.encodeWithWhiteSpaceFormatting(decodedText) : CharacterReference.encode(decodedText);
        outputDocument.replace(textSegment, encodedText);
    }

    private void reencodeCSSTextSegment(final Source source, final OutputDocument outputDocument, final int begin, final int end, final boolean dropExternalImages) {
        if (begin >= end) {
            return;
        }
        final Segment textSegment = new Segment(source, begin, end);
        checkCSS(cssBuffer.append(textSegment.toString()), styleMap, true);
        String checkedCSS = cssBuffer.toString();
        cssBuffer.setLength(0);
        if (dropExternalImages) {
            imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, true, false);
            checkedCSS = cssBuffer.toString();
            cssBuffer.setLength(0);
        }
        if (containsCSSElement(checkedCSS)) {
            outputDocument.replace(textSegment, checkedCSS);
        }
    }

    private static void removeTextSegment(final Source source, final OutputDocument outputDocument, final int begin, final int end) {
        if (begin >= end) {
            return;
        }
        final Segment textSegment = new Segment(source, begin, end);
        outputDocument.replace(textSegment, "");
    }

    private static final String CID = "cid:";

    private static final Pattern PATTERN_FILENAME = Pattern.compile("([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)");

    private CharSequence getStartTagHTML(final StartTag startTag, final Map<String, Set<String>> allowedAttributes, final boolean dropExternalImages) {
        // tidies and filters out non-approved attributes
        final String tagName = startTag.getName();
        final Attributes attributes = startTag.getAttributes();

        sb.setLength(0);
        sb.append('<').append(tagName);
        if (HTMLElementName.META == tagName && attributes.get("http-equiv") != null && allowedAttributes.containsKey("http-equiv")) {
            /*
             * Special handling for allowed meta tag which provides an allowed HTTP header indicated through 'http-equiv' attribute
             */
            for (final Attribute attribute : attributes) {
                sb.append(' ').append(attribute.getName()).append("=\"").append(attribute.getValue()).append('"');
            }
        } else {
            for (final Attribute attribute : attributes) {
                final String name = attribute.getKey();
                if (STYLE.equals(name)) {
                    /*
                     * Handle style attribute
                     */
                    checkCSS(cssBuffer.append(attribute.getValue()), styleMap, true);
                    String checkedCSS = cssBuffer.toString();
                    cssBuffer.setLength(0);
                    if (dropExternalImages) {
                        imageURLFound |= checkCSS(cssBuffer.append(checkedCSS), IMAGE_STYLE_MAP, true, false);
                        checkedCSS = cssBuffer.toString();
                        cssBuffer.setLength(0);
                    }
                    if (containsCSSElement(checkedCSS)) {
                        if (checkedCSS.indexOf('"') == -1) {
                            sb.append(' ').append(STYLE).append("=\"").append(checkedCSS).append('"');
                        } else {
                            sb.append(' ').append(STYLE).append("='").append(checkedCSS).append('\'');
                        }
                    }
                } else if ("class".equals(name) || "id".equals(name)) {
                    /*
                     * TODO: Is it safe to allow "class"/"id" attribute in any case?
                     */
                    sb.append(' ').append(name).append("=\"").append(CharacterReference.encode(attribute.getValue())).append('"');
                } else {
                    final String val = attribute.getValue();
                    if (null == allowedAttributes) { // No restrictions
                        if (isNonJavaScriptURL(val)) {
                            if (dropExternalImages && "background".equals(name) && PATTERN_URL.matcher(val).matches()) {
                                sb.append(' ').append(name).append("=\"\"");
                                imageURLFound = true;
                            } else if (dropExternalImages && (HTMLElementName.IMG == tagName || HTMLElementName.INPUT == tagName) && "src".equals(name)) {
                                if (val.regionMatches(true, 0, CID, 0, 4) || PATTERN_FILENAME.matcher(val).matches()) {
                                    // Allow inline images
                                    sb.append(' ').append(name).append("=\"").append(CharacterReference.encode(val)).append('"');
                                } else {
                                    sb.append(' ').append(name).append("=\"\"");
                                    imageURLFound = true;
                                }
                            } else {
                                sb.append(' ').append(name).append("=\"").append(CharacterReference.encode(val)).append('"');
                            }
                        }
                    } else {
                        if (allowedAttributes.containsKey(name)) {
                            final Set<String> allowedValues = allowedAttributes.get(name);
                            if (null == allowedValues || allowedValues.contains(val.toLowerCase(Locale.US))) {
                                if (isNonJavaScriptURL(val)) {
                                    if (dropExternalImages && (HTMLElementName.IMG == tagName || HTMLElementName.INPUT == tagName) && "src".equals(name)) {
                                        if (val.regionMatches(true, 0, CID, 0, 4) || PATTERN_FILENAME.matcher(val).matches()) {
                                            // Allow inline images
                                            sb.append(' ').append(name).append("=\"").append(CharacterReference.encode(val)).append('"');
                                        } else {
                                            sb.append(' ').append(name).append("=\"\"");
                                            imageURLFound = true;
                                        }
                                    } else {
                                        sb.append(' ').append(name).append("=\"").append(CharacterReference.encode(val)).append('"');
                                    }
                                }
                            } else if (NUM_ATTRIBS == allowedValues) {
                                /*
                                 * Only numeric attribute value allowed
                                 */
                                if (PAT_NUMERIC.matcher(val.trim()).matches()) {
                                    sb.append(' ').append(name).append("=\"").append(val).append('"');
                                }
                            }
                        }
                    }
                }
            }
        }
        if (startTag.getElement().getEndTag() == null && !HTMLElements.getEndTagOptionalElementNames().contains(tagName)) {
            sb.append(" /");
        }
        sb.append('>');
        return sb.toString();
    }

    private static boolean isNonJavaScriptURL(final String val) {
        if (null == val) {
            return false;
        }
        final String lc = val.trim().toLowerCase(Locale.US);
        return !lc.startsWith("javascript:") && !lc.startsWith("vbscript:");
    }

    private CharSequence getEndTagHTML(final String tagName) {
        sb.setLength(0);
        return sb.append("</").append(tagName).append('>').toString();
    }

    /*
     * ######################### HELPERS ##############################
     */

    private static final byte[] DEFAULT_WHITELIST =
        String.valueOf(
            "# HTML tags and attributes\n" + "\n" + "html.tag.a=\",href,name,tabindex,target,type,\"\n" + "html.tag.area=\",alt,coords,href,nohref[nohref],shape[:rect:circle:poly:default:],tabindex,target,\"\n" + "html.tag.b=\"\"\n" + "html.tag.base=\",href\"\n" + "html.tag.basefont=\",color,face,size,\"\n" + "html.tag.bdo=\",dir[:ltr:rtl:]\"\n" + "html.tag.blockquote=\",type,\"\n" + "html.tag.body=\",alink,background,bgcolor,link,text,vlink,\"\n" + "html.tag.br=\",clear[:left:right:all:none:]\"\n" + "html.tag.button=\",disabled[disabled],name,tabindex,type[:button:submit:reset:],value,\"\n" + "html.tag.caption=\",align[:top:bottom:left:right:]\"\n" + "html.tag.col=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n" + "html.tag.colgroup=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n" + "html.tag.del=\",datetime,\"\n" + "html.tag.dir=\",compact[compact]\"\n" + "html.tag.div=\",align[:left:center:right:justify:],dir[:ltr:rtl:auto:],\"\n" + "html.tag.dd=\"\"\n" + "html.tag.dl=\",compact[compact]\"\n" + "html.tag.dt=\"\"\n" + "html.tag.em=\"\"\n" + "html.tag.font=\",color,face,size,\"\n" + "html.tag.form=\",action,accept,accept-charset,enctype,method[:get:post:],name,target,\"\n" + "html.tag.h1=\",align[:left:center:right:justify:]\"\n" + "html.tag.h2=\",align[:left:center:right:justify:]\"\n" + "html.tag.h3=\",align[:left:center:right:justify:]\"\n" + "html.tag.h4=\",align[:left:center:right:justify:]\"\n" + "html.tag.h5=\",align[:left:center:right:justify:]\"\n" + "html.tag.h6=\",align[:left:center:right:justify:]\"\n" + "html.tag.hr=\",align[:left:center:right:],noshade[noshade],size,width,\"\n" + "html.tag.html=\",version,xmlns,\"\n" + "html.tag.i=\"\"\n" + "html.tag.img=\",align[:top:middle:bottom:left:right:],alt,border,height,hspace,ismap[ismap],name,src,usemap,vspace,width,\"\n" + "html.tag.input=\",accept,align[:top:middle:bottom:left:right:center:],alt,checked[checked],disabled[disabled],maxlength[],name,readonly[readonly],size,src,tabindex,type[:text:checkbox:radio:submit:reset:hidden:image:button:password:],value,\"\n" + "html.tag.ins=\",datetime,\"\n" + "html.tag.label=\",for,\"\n" + "html.tag.legend=\",align[:left:top:right:bottom:]\"\n" + "html.tag.li=\",type[:disc:square:circle:1:a:A:i:I:],value[],\"\n" + "html.tag.map=\",name,\"\n" + "html.tag.meta=\",http-equiv[:content-type:],\"\n" + "html.tag.ol=\",compact[compact],start[],type[:1:a:A:i:I:],\"\n" + "html.tag.optgroup=\",disabled[disabled],label,\"\n" + "html.tag.option=\",disabled[disabled],label,selected[selected],value,\"\n" + "html.tag.p=\",align[:left:center:right:justify:]\"\n" + "html.tag.pre=\",width[],\"\n" + "html.tag.select=\",disabled[disabled],multiple[multiple],name,size,tabindex[],\"\n" + "html.tag.span=\"\"\n" + "html.tag.strong=\"\"\n" + "html.tag.style=\",media,type,\"\n" + "html.tag.sub=\"\"\n" + "html.tag.sup=\"\"\n" + "html.tag.table=\",align[:left:center:right:],background,border,bgcolor,cellpadding,cellspacing,frame[:void:above:below:hsides:ihs:rhs:vsides:box:border:],rules[:none:groups:rows:cols:all:],summary,width,\"\n" + "html.tag.tbody=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n" + "html.tag.td=\",abbr,align[:left:center:right:justify:char:],axis,background,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n" + "html.tag.textarea=\",cols[],disabled[disabled],name,readonly[readonly],rows[],tabindex[],\"\n" + "html.tag.tfoot=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n" + "html.tag.th=\",abbr,align[:left:center:right:justify:char:],axis,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n" + "html.tag.thead=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n" + "html.tag.tr=\",align[:left:center:right:justify:char:],bgcolor,char,charoff,valign[:top:middle:bottom:baseline:],height,\"\n" + "html.tag.u=\"\"\n" + "html.tag.ul=\",compact[compact],type[:disc:square:circle:],\"\n" + "\n" + "\n" + "# CSS key-value-pairs.\n" + "# An empty value indicates a reference to style's combi-map.\n" + "# Placeholders:\n" + "# c: Any CSS color value\n" + "# u: An URL; e.g. url(http://www.somewhere.com/myimage.jpg);\n" + "# n: Any CSS number value without '%'\n" + "# N: Any CSS number value\n" + "# *: Any value allowed\n" + "# d: delete\n" + "# t: time\n" + "\n" + "html.style.azimuth=\",left-side,left-side behind,far-left,far-left behind,left,left behind,center-left,center-left behind,center,center behind,center-right,center-right behind,right,right behind,far-right,far-right behind,right-side,right behind,\"\n" + "html.style.background=\"\"\n" + "html.style.background-attachment=\",scroll,fixed,\"\n" + "html.style.background-color=\"c,transparent,\"\n" + "html.style.background-image=\"u\"\n" + "html.style.background-position=\",top,bottom,center,left,right,\"\n" + "html.style.background-repeat=\",repeat,repeat-x,repeat-y,no-repeat,\"\n" + "html.style.border=\"\"\n" + "html.style.border-bottom=\"\"\n" + "html.style.border-bottom-color=\"c,transparent,\"\n" + "html.style.border-bottom-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.border-bottom-width=\"n\"\n" + "html.style.border-collapse=\",separate,collapse,\"\n" + "html.style.border-color=\"c,transparent,\"\n" + "html.style.border-left=\"\"\n" + "html.style.border-left-color=\"c,transparent,\"\n" + "html.style.border-left-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.border-left-width=\"n\"\n" + "html.style.border-right=\"\"\n" + "html.style.border-right-color=\"c,transparent,\"\n" + "html.style.border-right-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.border-right-width=\"n\"\n" + "html.style.border-spacing=\"N\"\n" + "html.style.border-style=\"\"\n" + "html.style.border-top=\"\"\n" + "html.style.border-top-color=\"c,transparent,\"\n" + "html.style.border-top-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.border-top-width=\"n\"\n" + "html.style.border-width=\"\"\n" + "html.style.bottom=\"N,auto,\"\n" + "html.style.caption-side=\",top,bottom,left,right,\"\n" + "html.style.centerline=\"d\"\n" + "html.style.clear=\",left,right,both,none,\"\n" + "html.style.clip=\"d\"\n" + "html.style.color=\"c,transparent,\"\n" + "html.style.content=\"d\"\n" + "html.style.counter-increment=\"d\"\n" + "html.style.counter-reset=\"d\"\n" + "html.style.counter=\"d\"\n" + "html.style.cue=\"u\"\n" + "html.style.cue-after=\"u\"\n" + "html.style.cue-before=\"u\"\n" + "html.style.cursor=\",auto,default,crosshair,pointer,move,n-resize,ne-resize,e-resize,se-resize,s-resize,sw-resize,w-resize,nw-resize,text,wait,help,\"\n" + "html.style.definition-src=\"d\"\n" + "html.style.direction=\",ltr,rtl,\"\n" + "html.style.display=\",block,inline,list-item,marker,run-in,compact,none,table,inline-table,table-row,table-cell,table-row-group,table-header-group,table-footer-group,table-column,table-column-group,table-caption,\"\n" + "html.style.empty-cells=\",show,hide,\"\n" + "html.style.elevation=\",below,level,above,higher,lower,\"\n" + "html.style.filter=\"d\" \n" + "html.style.float=\",left,right,none,\"\n" + "html.style.font=\"\"\n" + "html.style.font-family=\"*\"\n" + "html.style.font-color=\"c,transparent,\"\n" + "html.style.font-size=\"N,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,\"\n" + "html.style.font-stretch=\",wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,\"\n" + "html.style.font-style=\",italic,oblique,normal,\"\n" + "html.style.font-variant=\",small-caps,normal,\"\n" + "html.style.font-weight=\",bold,bolder,lighter,100,200,300,400,500,600,700,800,900,normal,\"\n" + "html.style.height=\"N,auto,\"\n" + "html.style.left=\"N,auto,\"\n" + "html.style.letter-spacing=\"n\"\n" + "html.style.line-height=\"N\"\n" + "html.style.list-style=\"\"	\n" + "html.style.list-style-image=\"u,none,\"\n" + "html.style.list-style-position=\",inside,outside,\"\n" + "html.style.list-style-type=\",decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,none,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n" + "html.style.margin=\"\"\n" + "html.style.margin-bottom=\"N,auto,inherit,\"\n" + "html.style.margin-left=\"N,auto,inherit,\"\n" + "html.style.margin-right=\"N,auto,inherit,\"\n" + "html.style.margin-top=\"N,auto,inherit,\"\n" + "html.style.max-height=\"N\"\n" + "html.style.max-width=\"N\"\n" + "html.style.min-height=\"N\"\n" + "html.style.min-width=\"N\"\n" + "html.style.orphans=\"0\"\n" + "html.style.overflow=\",visible,hidden,scroll,auto,\"\n" + "html.style.padding=\"\"\n" + "html.style.padding-bottom=\"N\"\n" + "html.style.padding-left=\"N\"\n" + "html.style.padding-right=\"N\"\n" + "html.style.padding-top=\"N\"\n" + "html.style.page-break-after=\",always,avoid,left,right,inherit,auto,\"\n" + "html.style.page-break-before=\",always,avoid,left,right,inherit,auto,\"\n" + "html.style.page-break-inside=\",avoid,auto,\"\n" + "html.style.pause=\"t\"\n" + "html.style.pause-after=\"t\"\n" + "html.style.pause-before=\"t\"\n" + "html.style.pitch=\",x-low,low,medium,high,x-high,\"\n" + "html.style.pitch-range=\"0\"\n" + "html.style.play-during=\"u,mix,repeat,auto,\"\n" + "html.style.position=\",absolute,fixed,relative,static,\"\n" + "html.style.quotes=\"d\"\n" + "html.style.richness=\"0\"\n" + "html.style.right=\"N,auto,\"\n" + "html.style.scrollbar-3dlight-color=\"c\"\n" + "html.style.scrollbar-arrow-color=\"c\"\n" + "html.style.scrollbar-base-color=\"c\"\n" + "html.style.scrollbar-darkshadow-color=\"c\"\n" + "html.style.scrollbar-face-color=\"c\"\n" + "html.style.scrollbar-highlight-color=\"c\"\n" + "html.style.scrollbar-shadow-color=\"c\"\n" + "html.style.scrollbar-track-color=\"c\"\n" + "html.style.speak=\",none,normal,spell-out,\"\n" + "html.style.speak-header=\",always,once,\"\n" + "html.style.speak-numeral=\",digits,continuous,\"\n" + "html.style.speak-punctuation=\",code,none,\"\n" + "html.style.speech-rate=\"0,x-slow,slow,slower,medium,faster,fast,x-fase,\"\n" + "html.style.stress=\"0\"\n" + "html.style.table-layout=\",auto,fixed,\"\n" + "html.style.text-align=\",left,center,right,justify,\"\n" + "html.style.text-decoration=\",underline,overline,line-through,blink,none,\"\n" + "html.style.text-indent=\"N\"\n" + "html.style.text-shadow=\"nc,none,\" or color\n" + "html.style.text-transform=\",capitalize,uppercase,lowercase,none,\"\n" + "html.style.top=\"N,auto,\"\n" + "html.style.vertical-align=\",top,middle,bottom,baseline,sub,super,text-top,text-bottom,\"\n" + "html.style.visibility=\",hidden,visible,\"\n" + "html.style.voice-family=\",male,female,old,young,child,\"\n" + "html.style.volume=\"0,silent,x-soft,soft,medium,loud,x-loud,\"\n" + "html.style.white-space=\",normal,pre,nowrap,\"\n" + "html.style.widows=\"0\"\n" + "html.style.width=\"N,auto,\"\n" + "html.style.word-spacing=\"n\"\n" + "html.style.z-index=\"0\"\n" + "\n" + "\n" + "# CSS combi-map\n" + "\n" + "html.style.combimap.background=\"uNc,scroll,fixed,transparent,top,bottom,center,left,right,repeat,repeat-x,repeat-y,no-repeat,\"\n" + "html.style.combimap.border=\"Nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,separate,collapse,\"\n" + "html.style.combimap.border-bottom=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.combimap.border-left=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.combimap.border-right=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.combimap.border-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.combimap.border-top=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n" + "html.style.combimap.border-width=\"n\"\n" + "html.style.combimap.font=\"N*,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,italic,oblique,small-caps,bold,bolder,lighter,100,200,300,400,500,600,700,800,900,\"\n" + "html.style.combimap.list-style=\"u,none,inside,outside,decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n" + "html.style.combimap.margin=\"N,auto,inherit,\"\n" + "html.style.combimap.padding=\"N\"").getBytes();

    /**
     * Loads the white list.
     */
    public static void loadWhitelist() {
        synchronized (HTMLFilterHandler.class) {
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HtmlWhitelistFilter.class);
            if (null == staticHTMLMap) {
                String mapStr = null;
                {
                    final File whitelist = ServiceRegistry.getInstance().getService(ConfigurationService.class).getFileByName("whitelist.properties");
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
                                    sb.append(line).append(CRLF);
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

    private static final Pattern PATTERN_TAG_LINE = Pattern.compile(
        "html\\.tag\\.(\\p{Alnum}+)\\s*(?:=\\s*\"(\\p{Print}*)\")?",
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
            final String tagName = m.group(1).toLowerCase(Locale.US);
            if (null == attributes) {
                tagMap.put(tagName, null);
            } else {
                final Matcher attribMatcher = PATTERN_ATTRIBUTE.matcher(attributes);
                final Map<String, Set<String>> attribMap = new HashMap<String, Set<String>>();
                while (attribMatcher.find()) {
                    final String values = attribMatcher.group(2);
                    final String attributeName = attribMatcher.group(1).toLowerCase(Locale.US);
                    if (null == values) {
                        attribMap.put(attributeName, null);
                    } else if (values.length() == 0) {
                        attribMap.put(attributeName, NUM_ATTRIBS);
                    } else {
                        final Set<String> valueSet = new HashSet<String>();
                        final String[] valArr =
                            values.charAt(0) == ':' ? values.substring(1).split("\\s*:\\s*") : values.split("\\s*:\\s*");
                        for (final String value : valArr) {
                            valueSet.add(value.toLowerCase(Locale.US));
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
        "html\\.style\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]*)\"",
        Pattern.CASE_INSENSITIVE);

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
                final String cssElement = m.group(1).toLowerCase(Locale.US);
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
                styleMap.put(m.group(1).toLowerCase(Locale.US), valueSet);
            }
        }
        return styleMap;
    }

    private static final Pattern PATTERN_COMBI_LINE = Pattern.compile(
        "html\\.style\\.combimap\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]+)\"",
        Pattern.CASE_INSENSITIVE);

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
            combiMap.put(m.group(1).toLowerCase(Locale.US), valueSet);
        }
        return combiMap;
    }

}
