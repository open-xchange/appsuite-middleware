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

package com.openexchange.html.internal.jsoup;

import static com.openexchange.html.HtmlServices.isSafe;
import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL;
import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL_SOLE;
import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import static com.openexchange.java.Strings.toLowerCase;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import com.google.common.collect.ImmutableSet;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.filtering.FilterMaps;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;


/**
 * {@link FilteringWhitelist} - A Jsoup whitelist based on white-list file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class FilteringWhitelist extends Whitelist {

    // A decimal digit: [0-9]
    private static final Pattern PAT_NUMERIC = Pattern.compile("\\p{Digit}+");

    private final Map<String, Map<String, Set<String>>> htmlMap;
    private final Map<String, Set<String>> styleMap;
    private boolean replaceUrls = true;
    private final Stringer cssBuffer;
    private boolean dropExternalImages;
    private boolean imageURLFound;
    private String cssPrefix;

    private final StringBuilder urlBuilder;

    /** The max. content size (-1 or >=10000) */
    private int maxContentSize;
    private int curContentSize;
    private boolean maxContentSizeExceeded;

    /**
     * Initializes a new {@link FilteringWhitelist}.
     */
    public FilteringWhitelist() {
        super();
        this.urlBuilder = new StringBuilder(256);
        this.cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        this.styleMap = FilterMaps.getStaticStyleMap();
        Map<String, Map<String, Set<String>>> htmlMap = FilterMaps.getStaticHTMLMap();
        this.htmlMap = htmlMap;
        for (final Entry<String, Map<String, Set<String>>> entry : htmlMap.entrySet()) {
            String tagName = entry.getKey();
            addTags(tagName);
            Map<String, Set<String>> attrsMap = entry.getValue();
            if (null != attrsMap) {
                addAttributes(tagName, attrsMap.keySet().toArray(new String[0]));
            }
        }

    }

    /**
     * Sets whether to replace URLs.
     *
     * @param replaceUrls <code>true</code> to replace URLs; otherwise <code>false</code>
     * @return This whitelist with new behavior applied
     */
    public FilteringWhitelist setReplaceUrls(final boolean replaceUrls) {
        this.replaceUrls = replaceUrls;
        return this;
    }

    /**
     * Sets the CSS prefix
     *
     * @param cssPrefix The CSS prefix to set
     * @return This whitelist with new behavior applied
     */
    public FilteringWhitelist setCssPrefix(final String cssPrefix) {
        this.cssPrefix = cssPrefix;
        return this;
    }

    /**
     * Sets whether to replace image URLs.
     *
     * @param dropExternalImages <code>true</code> to replace image URLs; otherwise <code>false</code>
     * @return This whitelist with new behavior applied
     */
    public FilteringWhitelist setDropExternalImages(final boolean dropExternalImages) {
        this.dropExternalImages = dropExternalImages;
        return this;
    }

    /**
     * Sets the max. content size. &lt;= <code>0</code> means unlimited, &lt; <code>10000</code> will be set to <code>10000</code>.
     *
     * @param maxContentSize The max. content size to set
     * @return This whitelist with new behavior applied
     */
    public FilteringWhitelist setMaxContentSize(final int maxContentSize) {
        if ((maxContentSize >= 10000) || (maxContentSize <= 0)) {
            this.maxContentSize = maxContentSize;
        } else {
            this.maxContentSize = 10000;
        }

        this.maxContentSizeExceeded = false;
        return this;
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

    @Override
    protected boolean isSafeTag(String tag) {
        return super.isSafeTag(tag);
    }

    @Override
    protected boolean isSafeAttribute(String tagName, Element el, Attribute attribute) {
        String attr = attribute.getKey();
        if (Strings.asciiLowerCase(attr).startsWith("on")) {
            return false;
        }

        if ("style".equals(attr)) {
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
                attribute.setValue(css);
                return true;
            }
            return false;
        }

        if ("class".equals(attr) || "id".equals(attr)) {
            String value = prefixBlock(attribute.getValue(), cssPrefix);
            attribute.setValue(value);
            return true;
        }

        Map<String, Set<String>> allowedAttributes = htmlMap.get(tagName);
        String val = attribute.getValue();
        if (Strings.isEmpty(val)) {
            return true;
        }

        if (null == allowedAttributes) {
            // No restrictions
            return isSafeAttributeValue(tagName, el, attribute, attr, val);
        }

        // Check if attribute is allowed
        if (false == allowedAttributes.containsKey(attr)) {
            // No attributes defined for tag, try :all tag
            return !tagName.equals(":all") && super.isSafeAttribute(":all", el, attribute);
        }

        Set<String> allowedValues = allowedAttributes.get(attr);
        if (null == allowedValues || allowedValues.contains(toLowerCase(val))) {
            return isSafeAttributeValue(tagName, el, attribute, attr, val);
        }

        if (FilterMaps.getNumAttribs() == allowedValues) {
            // Only numeric attribute value allowed
            if (PAT_NUMERIC.matcher(val.trim()).matches()) {
                return true;
            }
        }

        return false;
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
            attribute.setValue(checkPossibleURL(val));
        }

        return true;
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

    private static final Set<String> URI_ATTRS = ImmutableSet.of("action","archive","background","cite","href","longdesc","src","usemap");

    private static Set<String> setForUriAttributes(Attributes attributes) {
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

}
