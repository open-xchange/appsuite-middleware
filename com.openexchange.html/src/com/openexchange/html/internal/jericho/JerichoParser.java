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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.html.internal.jericho;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;
import net.htmlparser.jericho.TagType;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link JerichoParser} - Parses specified real-life HTML document.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JerichoParser {

    /**
     * {@link ParsingDeniedException} - Thrown if HTML content cannot be parsed by {@link JerichoParser#parse(String, JerichoHandler)}
     * without wasting too many JVM resources.
     */
    public static final class ParsingDeniedException extends RuntimeException {

        private static final long serialVersionUID = 150733382242549446L;

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException() {
            super();
        }

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException(final String message) {
            super(message);
        }

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException(final Throwable cause) {
            super(cause);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

    } // End of ParsingDeniedException

    private static final JerichoParser INSTANCE = new JerichoParser();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static JerichoParser getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link JerichoParser}.
     */
    private JerichoParser() {
        super();
    }

    private volatile Integer maxLength;
    private int maxLength() {
        Integer i = maxLength;
        if (null == i) {
            synchronized (this) {
                i = maxLength;
                if (null == i) {
                    // Default is 1MB
                    final ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final int defaultMaxLength = 1048576;
                    if (null == service) {
                        return defaultMaxLength;
                    }
                    int prop = service.getIntProperty("com.openexchange.html.maxLength", defaultMaxLength);
                    if (prop <= 0) {
                        prop = Integer.MAX_VALUE;
                    }
                    i = Integer.valueOf(prop);
                    maxLength = i;
                }
            }
        }
        return i.intValue();
    }

    /**
     * Ensure given HTML content has a <code>&lt;body&gt;</code> tag.
     *
     * @param html The HTML content to check
     * @return The checked HTML content possibly with surrounded with a <code>&lt;body&gt;</code> tag
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    private boolean checkBody(String html) {
        if (null == html) {
            return false;
        }
        final int maxLength = maxLength();
        final boolean big = html.length() > maxLength;
        if (big) {
            throw new ParsingDeniedException("HTML content is too big: max. " + maxLength + ", but is " + html.length());
        }
        return (html.indexOf("<body") >= 0) || (html.indexOf("<BODY") >= 0);
    }

    private static final Pattern INVALID_DELIM = Pattern.compile("\" *, *\"");
    private static final Pattern FIX_START_TAG = Pattern.compile("^\\s*(<[^>]+)(>?)\\s*$");

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    public void parse(String html, JerichoHandler handler) {
        StreamedSource streamedSource = null;
        try {
            if (false == checkBody(html)) {
                // <body> tag not available
                handler.markBodyAbsent();
            }

            // Start regular parsing
            streamedSource = new StreamedSource(html);
            streamedSource.setLogger(null);
            Thread thread = Thread.currentThread();
            int lastSegmentEnd = 0;
            for (Iterator<Segment> iter = streamedSource.iterator(); !thread.isInterrupted() && iter.hasNext();) {
                Segment segment = iter.next();
                if (segment.getEnd() <= lastSegmentEnd) {
                    // If this tag is inside the previous tag (e.g. a server tag) then ignore it as it was already output along with the previous tag.
                    continue;
                }
                lastSegmentEnd = segment.getEnd();

                // Handle current segment
                handleSegment(handler, segment);
            }
        } catch (StackOverflowError parserOverflow) {
            throw new ParsingDeniedException("Parser overflow detected.", parserOverflow);
        } finally {
            Streams.close(streamedSource);
        }
    }

    private static enum EnumTagType {
        START_TAG, END_TAG, DOCTYPE_DECLARATION, CDATA_SECTION, COMMENT;

        private static final Map<TagType, EnumTagType> MAPPING;
        static {
            final Map<TagType, EnumTagType> m = new HashMap<TagType, EnumTagType>(5);
            m.put(StartTagType.NORMAL, START_TAG);
            m.put(EndTagType.NORMAL, END_TAG);
            m.put(StartTagType.DOCTYPE_DECLARATION, DOCTYPE_DECLARATION);
            m.put(StartTagType.CDATA_SECTION, CDATA_SECTION);
            m.put(StartTagType.COMMENT, COMMENT);
            MAPPING = Collections.unmodifiableMap(m);
        }

        protected static EnumTagType enumFor(TagType tagType) {
            return MAPPING.get(tagType);
        }
    }

    private static void handleSegment(JerichoHandler handler, Segment segment) {
        if (segment instanceof Tag) {
            Tag tag = (Tag) segment;
            TagType tagType = tag.getTagType();

            EnumTagType enumType = EnumTagType.enumFor(tagType);
            if (null == enumType) {
                if (!segment.isWhiteSpace()) {
                    handler.handleUnknownTag(tag);
                }
            } else {
                switch (enumType) {
                case START_TAG:
                    handler.handleStartTag((StartTag) tag);
                    break;
                case END_TAG:
                    handler.handleEndTag((EndTag) tag);
                    break;
                case DOCTYPE_DECLARATION:
                    handler.handleDocDeclaration(segment.toString());
                    break;
                case CDATA_SECTION:
                    handler.handleCData(segment.toString());
                    break;
                case COMMENT:
                    handler.handleComment(segment.toString());
                    break;
                default:
                    break;
                }
            }
        } else if (segment instanceof CharacterReference) {
            CharacterReference characterReference = (CharacterReference) segment;
            handler.handleCharacterReference(characterReference);
        } else {
            /*
             * Safety re-parse
             */
            if (contains('<', segment)) {
                Matcher m = FIX_START_TAG.matcher(segment);
                if (m.find()) {
                    /*
                     * Re-parse start tag
                     */
                    StreamedSource nestedSource = new StreamedSource(dropWeirdAttributes(m.group(1)));
                    Thread thread = Thread.currentThread();
                    for (Iterator<Segment> iter = nestedSource.iterator(); !thread.isInterrupted() && iter.hasNext();) {
                        Segment nestedSegment = iter.next();
                        handleSegment(handler, nestedSegment);
                    }
                } else {
                    handler.handleSegment(segment);
                }
            } else {
                handler.handleSegment(segment);
            }
        }
    }

    private String fixStyleAttribute(String startTag) {
        if (startTag.indexOf("style=") <= 0) {
            return startTag;
        }
        return INVALID_DELIM.matcher(startTag).replaceAll("; ");
    }

    private static boolean startsWith(char startingChar, CharSequence toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        int i = 0;
        if (Strings.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Strings.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    private static boolean contains(char c, CharSequence toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (c == toCheck.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private static Pattern PATTERN_ATTRIBUTE = Pattern.compile("([a-zA-Z_0-9-]+)=((?:\".*?\")|(?:'.*?')|(?:[a-zA-Z_0-9-]+))");

    private static String dropWeirdAttributes(String startTag) {
        int length = startTag.length();
        if (length <= 0 || '<' != startTag.charAt(0)) {
            return startTag;
        }

        StringBuilder sb = new StringBuilder(length).append('<');
        int i = 1;

        // Consume tag name
        boolean ws = false;
        for (; !ws && i < length; i++) {
            char c = startTag.charAt(i);
            if (Strings.isWhitespace(c)) {
                ws = true;
            } else {
                sb.append(c);
            }
        }

        // Grep attributes
        Matcher m = PATTERN_ATTRIBUTE.matcher(startTag.substring(i));
        while (m.find()) {
            sb.append(' ').append(m.group());
        }
        sb.append('>');
        return sb.toString();
    }

}
