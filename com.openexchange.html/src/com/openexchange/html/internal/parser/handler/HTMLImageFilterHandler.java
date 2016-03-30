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

import static com.openexchange.html.internal.HtmlServiceImpl.PATTERN_URL;
import static com.openexchange.html.internal.css.CSSMatcher.checkCSS;
import static com.openexchange.html.internal.css.CSSMatcher.containsCSSElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;

/**
 * {@link HTMLImageFilterHandler} - Removes all possible sources of externally loaded images inside HTML content.
 * <p>
 * By now the following possible sources are handled:
 * <ol>
 * <li> <code>'&lt;img src=&quot;...&quot; /&gt;'</code> --&gt; <code>'&lt;img src=&quot;&quot; /&gt;'</code></li>
 * <li> <code>'&lt;input src=&quot;...&quot; /&gt;'</code> --&gt; <code>'&lt;input src=&quot;&quot; /&gt;'</code></li>
 * <li> <code>'&lt;sometag background=&quot;an-url&quot;&gt;'</code> --&gt; <code>'&lt;sometag background=&quot;&quot;&gt;'</code></li>
 * <li>Removed CSS: <code>background: url(an-url);</code></li>
 * <li>Removed CSS: <code>background-image: url(an-url);</code></li>
 * </ol>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HTMLImageFilterHandler implements HtmlHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HTMLImageFilterHandler.class);

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    private static final String BLANK = "";

    private static final String SRC = "src";

    private static final String BACKGROUND = "background";

    private static final String CRLF = "\r\n";

    private static final String STYLE = "style";

    private static final String IMG = "img";

    private static final String INPUT = "input";

    private static final Map<String, Set<String>> STYLE_MAP;

    static {
        STYLE_MAP = new HashMap<String, Set<String>>();
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
        STYLE_MAP.put(BACKGROUND, values);
        values = new HashSet<String>();
        /*
         * background-image
         */
        values.add("d"); // delete
        STYLE_MAP.put("background-image", values);
    }

    private final HtmlService htmlService;

    private final StringBuilder htmlBuilder;

    private final StringBuilder attrBuilder;

    private boolean isCss;

    private boolean imageURLFound;

    private final Stringer cssBuffer;

    public HTMLImageFilterHandler(final HtmlService htmlService, final int capacity) {
        super();
        this.htmlService = htmlService;
        cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        htmlBuilder = new StringBuilder(capacity);
        attrBuilder = new StringBuilder(128);
    }

    @Override
    public void handleCDATA(final String text) {
        htmlBuilder.append("<![CDATA[");
        if (isCss) {
            /*
             * Handle style attribute
             */
            imageURLFound |= checkCSS(cssBuffer.append(text), STYLE_MAP, true, false);
            htmlBuilder.append(cssBuffer.toString());
            cssBuffer.setLength(0);
        } else {
            htmlBuilder.append(text);
        }
        htmlBuilder.append("]]>");
    }

    @Override
    public void handleComment(final String comment) {
        htmlBuilder.append("<!--").append(comment).append("-->");
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        htmlBuilder.append("<!DOCTYPE").append(docDecl).append('>').append(CRLF).append(CRLF);
    }

    @Override
    public void handleEndTag(final String tag) {
        if (isCss && STYLE.equals(tag)) {
            isCss = false;
        }
        htmlBuilder.append("</").append(tag).append('>');
    }

    @Override
    public void handleError(final String errorMsg) {
        LOG.error(errorMsg);
    }

    private static final String CID = "cid:";

    private static final Pattern PATTERN_FILENAME = Pattern.compile("([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)");

    private static boolean isInlineImageSource(final String src) {
        final String lcSrc = src.toLowerCase(Locale.ENGLISH);
        if (lcSrc.startsWith(CID, 0) || PATTERN_FILENAME.matcher(lcSrc).matches()) {
            return true;
        }
        try {
            final URI uri = new URI(lcSrc);
            return (null == uri.getHost()) && uri.getPath().startsWith(PREFIX.get().getPrefix()+"image");
        } catch (final URISyntaxException e) {
            // Invalid image URI; return false
            return false;
        }
    }
    @Override
    public void handleSimpleTag(final String tag, final Map<String, String> attributes) {
        if (IMG.equals(tag) || INPUT.equals(tag)) {
            final String src = attributes.get(SRC);
            if (null == src) {
                attributes.put(SRC, BLANK);
            } else if (!isInlineImageSource(src)) {
                /*
                 * Don't replace inline images
                 */
                attributes.put(SRC, BLANK);
                imageURLFound = true;
            }
        } else if (attributes.containsKey(BACKGROUND)) {
            /*
             * Check for URL inside background attribute
             */
            try {
                if (PATTERN_URL.matcher(attributes.get(BACKGROUND)).matches()) {
                    attributes.put(BACKGROUND, BLANK);
                    imageURLFound = true;
                }
            } catch (final StackOverflowError e) {
                LOG.error("", e);
                attributes.remove(BACKGROUND);
            }
        }
        handleStart(tag, attributes, true);
    }

    @Override
    public void handleStartTag(final String tag, final Map<String, String> attributes) {
        if (STYLE.equals(tag)) {
            isCss = true;
        } else {
            if (attributes.containsKey(BACKGROUND)) {
                /*
                 * Check for URL inside background attribute
                 */
                try {
                    if (PATTERN_URL.matcher(attributes.get(BACKGROUND)).matches()) {
                        attributes.put(BACKGROUND, BLANK);
                        imageURLFound = true;
                    }
                } catch (final StackOverflowError e) {
                    LOG.error("", e);
                    attributes.remove(BACKGROUND);
                }
            }
        }
        handleStart(tag, attributes, false);
    }

    private static final String VAL_START = "=\"";

    private void handleStart(final String tag, final Map<String, String> attributes, final boolean simple) {
        attrBuilder.setLength(0);
        final int size = attributes.size();
        final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Entry<String, String> e = iter.next();
            if (STYLE.equals(e.getKey())) {
                /*
                 * Handle style attribute
                 */
                imageURLFound |= checkCSS(cssBuffer.append(e.getValue()), STYLE_MAP, false);
                final String checkedCSS = cssBuffer.toString();
                cssBuffer.setLength(0);
                if (containsCSSElement(checkedCSS)) {
                    if (checkedCSS.indexOf('"') == -1) {
                        attrBuilder.append(' ').append(STYLE).append(VAL_START).append(checkedCSS).append('"');
                    } else {
                        attrBuilder.append(' ').append(STYLE).append("='").append(checkedCSS).append('\'');
                    }
                }
            } else {
                attrBuilder.append(' ').append(e.getKey()).append(VAL_START).append(htmlService.htmlFormat(e.getValue(), false)).append('"');
            }
        }
        if (simple) {
            if (attrBuilder.length() > 0 || size == 0) {
                htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append(' ').append('/').append('>');
            }
        } else {
            htmlBuilder.append('<').append(tag).append(attrBuilder.toString()).append('>');
        }
    }

    @Override
    public void handleText(final String text, final boolean ignorable) {
        if (isCss) {
            if (ignorable) {
                htmlBuilder.append(text);
            } else {
                /*
                 * Handle style attribute
                 */
                imageURLFound |= checkCSS(cssBuffer.append(text), STYLE_MAP, true, false);
                htmlBuilder.append(cssBuffer.toString());
                cssBuffer.setLength(0);
            }
        } else {
            htmlBuilder.append(text);
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

    /**
     * Gets the HTML content with external image content removed
     *
     * @return The HTML content with external image content removed
     */
    public String getHTML() {
        return htmlBuilder.toString();
    }

    /**
     * Indicates if an image source has been found (and suppressed)
     *
     * @return <code>true</code> if an image source has been found; otherwise <code>false</code>
     */
    public boolean isImageURLFound() {
        return imageURLFound;
    }

    /**
     * Resets
     */
    public void reset() {
        imageURLFound = false;
        isCss = false;
        htmlBuilder.setLength(0);
        attrBuilder.setLength(0);
    }
}
