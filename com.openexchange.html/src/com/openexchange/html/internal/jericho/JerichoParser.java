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
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;
import net.htmlparser.jericho.TagType;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.internal.parser.HtmlHandler;
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

    /**
     * Ensure given HTML content has a <code>&lt;body&gt;</code> tag.
     *
     * @param html The HTML content to check
     * @return The checked HTML content possibly with surrounded with a <code>&lt;body&gt;</code> tag
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    private boolean checkBody(String html, boolean checkSize) {
        if (null == html) {
            return false;
        }
        if (checkSize) {
            int maxLength = HtmlServices.htmlThreshold();
            if (html.length() > maxLength) {
                throw new ParsingDeniedException("HTML content is too big: max. " + maxLength + ", but is " + html.length());
            }
        }
        return (html.indexOf("<body") >= 0) || (html.indexOf("<BODY") >= 0);
    }

    private static final Pattern FIX_START_TAG = Pattern.compile("\\s*(<[a-zA-Z][^>]+)(>?)\\s*");

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    public void parse(String html, JerichoHandler handler) {
        parse(html, handler, true);
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @param checkSize Whether this call is supposed to check the size of given HTML content against <i>"com.openexchange.html.maxLength"</i> property
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    public void parse(String html, JerichoHandler handler, boolean checkSize) {
        StreamedSource streamedSource = null;
        try {
            if (false == checkBody(html, checkSize)) {
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
                handleSegment(handler, segment, true);
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

    private static void handleSegment(JerichoHandler handler, Segment segment, boolean fixStartTags) {
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
            // Safety re-parse
            safeParse(handler, segment, fixStartTags);
        }
    }

    private static void safeParse(JerichoHandler handler, Segment segment, boolean fixStartTags) {
        if (fixStartTags && containsStartTag(segment)) {
            Matcher m = FIX_START_TAG.matcher(segment);
            if (m.find()) {
                // Re-parse start tag

                String startTag = m.group(1);
                if (startTag.startsWith("<!--")) {
                    handler.handleComment(m.group());
                    return;
                }

                int start = m.start();
                if (start > 0) {
                    handler.handleSegment(segment.subSequence(0, start));
                }
                int[] remainder = null;

                int end = m.end();
                if (end < segment.length()) {
                    int pos = indexOf('>', end, segment);
                    if (pos >= 0) {
                        startTag = startTag + segment.subSequence(end, pos + 1);
                        remainder = new int[] { pos + 1, segment.length() };
                    } else {
                        remainder = new int[] { end, segment.length() };
                    }
                }

                @SuppressWarnings("resource")
                StreamedSource nestedSource = new StreamedSource(dropWeirdAttributes(startTag)); // No need to close since String-backed (all in memory)!
                Thread thread = Thread.currentThread();
                for (Iterator<Segment> iter = nestedSource.iterator(); !thread.isInterrupted() && iter.hasNext();) {
                    Segment nestedSegment = iter.next();
                    handleSegment(handler, nestedSegment, false);
                }
                if (null != remainder) {
                    safeParse(handler, new Segment(new Source(segment), remainder[0], remainder[1]), fixStartTags);
                    // handler.handleSegment(remainder);
                }
            } else {
                handler.handleSegment(segment);
            }
        } else {
            handler.handleSegment(segment);
        }
    }

    private static boolean containsStartTag(CharSequence toCheck) {
        if (null == toCheck) {
            return false;
        }
        int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        for (int k = len - 1, index = 0; k-- > 0; index++) {
            if ('<' == toCheck.charAt(index) && isAsciLetter(toCheck.charAt(index + 1))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAsciLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static int indexOf(int ch, int fromIndex, CharSequence cs) {
        int max = cs.length();
        if (fromIndex >= max) {
            return -1;
        }

        for (int i = fromIndex; i < max; i++) {
            if (cs.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
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
