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
import static com.openexchange.html.internal.HtmlServiceImpl.checkURL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.parser.HtmlHandler;

/**
 * {@link HTML2TextHandler} - A handler to generate plain text version from parsed HTML content which is then accessible via
 * {@link #getText()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HTML2TextHandler implements HtmlHandler {

    private static final String TAG_OL = "ol";

    private static final String TAG_UL = "ul";

    private static final String TAG_LI = "li";

    private static final String TAG_IMG = "img";

    private static final String ATTR_ALT = "alt";

    private static final String ATTR_ALT2 = "ALT";

    private static final String ATTR_HREF = "href";

    private static final String ATTR_SRC = "src";

    private static final String ATTR_SRC2 = "SRC";

    private static final String CRLF = "\r\n";

    private static final String QUOTE = "> ";

    private static final String TAG_A = "a";

    private static final String TAG_ADDRESS = "address";

    private static final String TAG_BLOCKQUOTE = "blockquote";

    private static final String TAG_BODY = "body";

    private static final String TAG_BR = "br";

    private static final String TAG_DIV = "div";

    private static final String TAG_H1 = "h1";

    private static final String TAG_H2 = "h2";

    private static final String TAG_H3 = "h3";

    private static final String TAG_H4 = "h4";

    private static final String TAG_H5 = "h5";

    private static final String TAG_H6 = "h6";

    private static final String TAG_P = "p";

    private static final String TAG_PRE = "pre";

    private static final String TAG_TD = "td";

    private static final String TAG_TR = "tr";

    private static final String TAG_STYLE = "style";

    private static final String TAG_SCRIPT = "script";

    /*-
     * ----------------- Member stuff -----------------
     */

    private final HtmlService htmlService;

    private boolean insideBody;

    private boolean ignore;

    private boolean anchorTag;

    private boolean preTag;

    private boolean appendHref;

    private String hrefContent;

    private int quote;

    private final StringBuilder textBuilder;

    private long mailId;

    private int userId;

    private int contextId;

    private String prevTag;

    /**
     * Initializes a new {@link HTML2TextHandler}.
     *
     * @param capacity The initial capacity
     */
    public HTML2TextHandler(final HtmlService htmlService, final int capacity) {
        this(htmlService, capacity, false);
    }

    /**
     * Initializes a new {@link HTML2TextHandler}.
     *
     * @param capacity The initial capacity
     * @param appendHref <code>true</code> to append URLs contained in <i>href</i>s and <i>src</i>s; otherwise <code>false</code>.<br>
     *            Example: <code>&lt;a&nbsp;href=\"www.somewhere.com\"&gt;Link&lt;a&gt;</code> would be
     *            <code>Link&nbsp;[www.somewhere.com]</code>
     */
    public HTML2TextHandler(final HtmlService htmlService, final int capacity, final boolean appendHref) {
        super();
        this.htmlService = htmlService;
        textBuilder = new StringBuilder(capacity);
        this.appendHref = appendHref;
    }

    /**
     * Sets whether to append URLs contained in <i>href</i>s and <i>src</i>.<br>
     * Example: <code>&lt;a&nbsp;href=\"www.somewhere.com\"&gt;Link&lt;a&gt;</code> would be <code>Link&nbsp;[www.somewhere.com]</code>
     *
     * @param appendHref <code>true</code> to append URLs contained in <i>href</i>s and <i>src</i>s; otherwise <code>false</code>
     * @return This handler with behavior applied
     */
    public HTML2TextHandler setAppendHref(final boolean appendHref) {
        this.appendHref = appendHref;
        return this;
    }

    /**
     * Gets the extracted text.
     *
     * @return The extracted text
     */
    public String getText() {
        return textBuilder.toString();
    }

    /**
     * Sets the mail ID for debugging purpose on {@link #handleError(String)}.
     *
     * @param mailId The mail ID to set
     */
    public void setMailId(final long mailId) {
        this.mailId = mailId;
    }

    /**
     * Sets the user ID for debugging purpose on {@link #handleError(String)}.
     *
     * @param userId The user ID to set
     */
    public void setUserId(final int userId) {
        this.userId = userId;
    }

    /**
     * Sets the context ID for debugging purpose on {@link #handleError(String)}.
     *
     * @param contextId The context ID to set
     */
    public void setContextId(final int contextId) {
        this.contextId = contextId;
    }

    @Override
    public void handleComment(final String comment) {
        // Nothing to do
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        // Nothing to do
    }

    @Override
    public void handleEndTag(final String tag) {
        final String ltag = tag.toLowerCase(Locale.US);
        if (TAG_BODY.equals(ltag)) {
            insideBody = false;
        } else if (appendHref && ltag.equals(TAG_A)) {
            anchorTag = false;
        } else if (TAG_STYLE.equals(ltag)) {
            ignore = false;
        } else if (TAG_SCRIPT.equals(ltag)) {
            ignore = false;
        } else if (insideBody) {
            if (ltag.equals(TAG_BLOCKQUOTE)) {
                textBuilder.append(CRLF);
                quote--;
            } else if (ltag.equals(TAG_P)) {
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_TR)) {
                // Ending table row
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_LI)) {
                // Ending list entry
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_TD)) {
                // Ending table column
                textBuilder.append('\t');
            } else if (ltag.equals(TAG_OL) || ltag.equals(TAG_UL)) {
                // Ending list
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_PRE)) {
                textBuilder.append(CRLF);
                quoteText();
                textBuilder.append(CRLF);
                quoteText();
                preTag = false;
            } else if (ltag.equals(TAG_H1) || ltag.equals(TAG_H2) || ltag.equals(TAG_H3) || ltag.equals(TAG_H4) || ltag.equals(TAG_H5) || ltag.equals(TAG_H6) || ltag.equals(TAG_ADDRESS)) {
                textBuilder.append(CRLF);
                quoteText();
                textBuilder.append(CRLF);
                quoteText();
            }
        }
        prevTag = ltag;
    }

    @Override
    public void handleError(final String errorMsg) {
        final StringBuilder sb = new StringBuilder(128 + (null == errorMsg ? 0 : errorMsg.length()));
        sb.append("HTML parsing error occurred: ");
        if (null != errorMsg) {
            sb.append(errorMsg);
        }
        boolean prefix = false;
        if (0 != mailId) {
            if (!prefix) {
                sb.append("\nError information:");
                prefix = true;
            }
            sb.append(" mail-ID='").append(mailId).append('\'');
        }
        if (0 != userId) {
            if (!prefix) {
                sb.append("\nError information:");
                prefix = true;
            }
            sb.append(" user-ID='").append(userId).append('\'');
        }
        if (0 != contextId) {
            if (!prefix) {
                sb.append("\nError information:");
                prefix = true;
            }
            sb.append(" context-ID='").append(contextId).append('\'');
        }
    }

    @Override
    public void handleSimpleTag(final String tag, final Map<String, String> attributes) {
        if (insideBody) {
            final String ltag = tag.toLowerCase(Locale.US);
            if (ltag.equals(TAG_BR)) {
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_IMG)) {
                if (attributes.containsKey(ATTR_ALT)) {
                    textBuilder.append(' ').append(attributes.get(ATTR_ALT)).append(' ');
                } else if (attributes.containsKey(ATTR_ALT2)) {
                    textBuilder.append(' ').append(attributes.get(ATTR_ALT2)).append(' ');
                }
                if (appendHref) {
                    final String src =
                        attributes.containsKey(ATTR_SRC) ? attributes.get(ATTR_SRC) : (attributes.containsKey(ATTR_SRC2) ? attributes.get(ATTR_SRC2) : null);
                    if (src != null && src.indexOf("cid:") == -1) {
                        textBuilder.append(" [");
                        try {
                            textBuilder.append(checkURL(src));
                        } catch (final Exception e) {
                            textBuilder.append(src);
                        }
                        textBuilder.append("] ");
                    }
                }
            }
        }
    }

    @Override
    public void handleStartTag(final String tag, final Map<String, String> attributes) {
        final String ltag = tag.toLowerCase(Locale.US);
        if (TAG_BODY.equals(ltag)) {
            insideBody = true;
        } else if (TAG_STYLE.equals(ltag)) {
            ignore = true;
        } else if (TAG_SCRIPT.equals(ltag)) {
            ignore = true;
        } else if (insideBody) {
            if (ltag.equals(TAG_BLOCKQUOTE)) {
                textBuilder.append(CRLF);
                quote++;
                quoteText();
            } else if (ltag.equals(TAG_DIV)) {
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_P)) {
                // textBuilder.append(CRLF);
                // quoteText();
            } else if (ltag.equals(TAG_OL) || ltag.equals(TAG_UL)) {
                // Starting list
                textBuilder.append(CRLF);
                quoteText();
            } else if (ltag.equals(TAG_PRE)) {
                preTag = true;
            } else if (appendHref && ltag.equals(TAG_A)) {
                anchorTag = true;
                hrefContent = null;
                final int size = attributes.size();
                if (size > 0) {
                    final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
                    for (int i = 0; i < size && hrefContent == null; i++) {
                        final Entry<String, String> e = iter.next();
                        if (ATTR_HREF.equalsIgnoreCase(e.getKey())) {
                            hrefContent = e.getValue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleCDATA(final String text) {
        if (insideBody && !ignore) {
            textBuilder.append(text);
        }
    }

    /**
     * Detects the appendix added by JTidy's pretty-printer to a text line
     */
    private static final Pattern PAT_TRIM = Pattern.compile("[^\n\f\r]\r?\n +$");

    /**
     * Detects control characters: \n, \f, and \r
     */
    private static final Pattern PAT_CONTROL = Pattern.compile("[\n\f\r]+");

    /**
     * Detects starting indentions: \t or "    "
     */
    private static final Pattern PAT_START_INDENT = Pattern.compile("^(?:(\t)|([ ]{2,}))+");

    /**
     * Detects indentions: \t or "    "
     */
    private static final Pattern PAT_INDENT = Pattern.compile("(?:(\t)|([ ]{2,}))+");

    /**
     * The special signature delimiter.
     */
    private static final Pattern PAT_SIGNATURE_DELIM = Pattern.compile(" *-- (?:&#160;)?$");

    private static final Set<String> FORMATS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("b", "i", "em", "strong")));

    private static final String STR_EMPTY = "";

    private static final String STR_BLANK = " ";

    @Override
    public void handleText(final String text, final boolean ignorable) {
        if (insideBody && !ignore) {
            /*
             * Add normal text
             */
            if (preTag) {
                /*
                 * Keep control characters
                 */
                textBuilder.append(htmlService.replaceHTMLEntities(text));
            } else {
                /*
                 * Ignorable?
                 */
                if (!ignorable || (null != prevTag && FORMATS.contains(prevTag))) {
                    /*
                     * Cut off JTidy's pretty-printer appendix if present
                     */
                    String preparedText;
                    {
                        final Matcher m = PAT_TRIM.matcher(text);
                        if (m.find()) {
                            preparedText = text.substring(0, m.start() + 1);
                        } else {
                            preparedText = text;
                        }
                    }
                    /*
                     * Remove any control characters: \n, \f, and \r
                     */
                    preparedText = PAT_CONTROL.matcher(preparedText).replaceAll(STR_EMPTY);
                    /*
                     * Remove starting blanks: \t or " "
                     */
                    {
                        final int len = preparedText.length();
                        if (len == 0) {
                            /*
                             * A zero length string can be ignored
                             */
                            return;
                        }
                        int i = 0;
                        char c = '\0';
                        while ((i < len) && ((c = preparedText.charAt(i)) == '\t'/* || c == ' '*/)) {
                            i++;
                        }
                        if (i > 0) {
                            if (i >= len) {
                                /*
                                 * Abort since length is exceeded
                                 */
                                return;
                            }
                            preparedText = preparedText.substring(i);
                        }
                    }
                    /*
                     * Remove starting indention
                     */
                    preparedText = PAT_START_INDENT.matcher(preparedText).replaceFirst(STR_EMPTY);
                    /*
                     * Turn remaining indentions to space characters
                     */
                    preparedText = PAT_INDENT.matcher(preparedText).replaceAll(STR_BLANK);
                    /*
                     * Check for special signature delimiter
                     */
                    if (PAT_SIGNATURE_DELIM.matcher(preparedText).matches()) {
                        preparedText = "-- ";
                    }
                    /*
                     * Finally append to text builder
                     */
                    textBuilder.append(htmlService.replaceHTMLEntities(preparedText));
                }
            }
            if (anchorTag && hrefContent != null && !text.equalsIgnoreCase(hrefContent) && !PATTERN_URL.matcher(text).matches()) {
                textBuilder.append(" [");
                try {
                    textBuilder.append(checkURL(hrefContent));
                } catch (final Exception e) {
                    textBuilder.append(hrefContent);
                }
                textBuilder.append("] ");
            }
        }
    }

    private void quoteText() {
        /*
         * Start line with quotes if necessary
         */
        for (int b = 1; b <= quote; b++) {
            textBuilder.append(QUOTE);
        }
    }

    /**
     * Resets this handler for re-usage
     *
     * @return This html2text handler
     */
    public HTML2TextHandler reset() {
        quote = 0;
        textBuilder.setLength(0);
        return this;
    }

    @Override
    public void handleXMLDeclaration(final String version, final Boolean standalone, final String encoding) {
        // Nothing to do
    }

}
