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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;

/**
 * {@link JerichoParser} - Parses specified real-life HTML document.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JerichoParser {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(JerichoParser.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

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
        ParsingDeniedException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException(String message) {
            super(message);
        }

        /**
         * Initializes a new {@link ParsingDeniedException}.
         */
        ParsingDeniedException(Throwable cause) {
            super(cause);
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

    private final Pattern BODY_START = Pattern.compile("<body.*?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private volatile Integer maxLength;
    private int maxLength() {
        Integer i = maxLength;
        if (null == maxLength) {
            synchronized (JerichoParser.class) {
                i = maxLength;
                if (null == maxLength) {
                    // Default is 512KB
                    final ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final int defaultMaxLength = 1048576 >> 1;
                    i = Integer.valueOf(null == service ? defaultMaxLength : service.getIntProperty(
                        "com.openexchange.html.maxLength",
                        defaultMaxLength));
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
    private StreamedSource checkBody(final String html) {
        if (null == html) {
            return null;
        }
        final int maxLength = maxLength();
        final boolean big = html.length() > maxLength;
        if (big) {
            throw new ParsingDeniedException("HTML content is too big: max. " + maxLength + ", but is " + html.length());
        }
        if (BODY_START.matcher(html).find()) {
            return new StreamedSource(html);
        }
        // <body> tag missing
        String sep = System.getProperty("line.separator");
        if (null == sep) {
            sep = "\n";
        }
        return new StreamedSource(new com.openexchange.java.StringAllocator(html.length() + 16).append("<body>").append(sep).append(html).append(sep).append("</body>"));
    }

    private static final Pattern INVALID_DELIM = Pattern.compile("\" *, *\"");
    private static final Pattern FIX_START_TAG = Pattern.compile("(<[^>]+)(>?)");

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @throws ParsingDeniedException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    public void parse(final String html, final JerichoHandler handler) {
        final long st = DEBUG ? System.currentTimeMillis() : 0L;
        StreamedSource streamedSource = null;
        try {
            streamedSource = checkBody(html);
            streamedSource.setLogger(null);
            int lastSegmentEnd = 0;
            for (final Segment segment : streamedSource) {
                if (segment.getEnd() <= lastSegmentEnd) {
                    /*
                     * If this tag is inside the previous tag (e.g. a server tag) then ignore it as it was already output along with the
                     * previous tag.
                     */
                    continue;
                }
                lastSegmentEnd = segment.getEnd();
                /*
                 * Handle current segment
                 */
                handleSegment(handler, segment);
            }
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("\tJerichoParser.parse() took " + dur + "msec.");
            }
        } catch (final StackOverflowError parserOverflow) {
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

        protected static EnumTagType enumFor(final TagType tagType) {
            return MAPPING.get(tagType);
        }
    }

    private static void handleSegment(final JerichoHandler handler, final Segment segment) {
        if (segment instanceof Tag) {
            final Tag tag = (Tag) segment;
            final TagType tagType = tag.getTagType();

            final EnumTagType enumType = EnumTagType.enumFor(tagType);
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
            /*-
             *
            if (tagType == StartTagType.NORMAL) {
                handler.handleStartTag((StartTag) tag);
            } else if (tagType == EndTagType.NORMAL) {
                handler.handleEndTag((EndTag) tag);
            } else if (tagType == StartTagType.DOCTYPE_DECLARATION) {
                handler.handleDocDeclaration(segment.toString());
            } else if (tagType == StartTagType.CDATA_SECTION) {
                handler.handleCData(segment.toString());
            } else if (tagType == StartTagType.COMMENT) {
                handler.handleComment(segment.toString());
            } else {
                if (!segment.isWhiteSpace()) {
                    handler.handleUnknownTag(tag);
                }
            }
             *
             */
        } else if (segment instanceof CharacterReference) {
            final CharacterReference characterReference = (CharacterReference) segment;
            handler.handleCharacterReference(characterReference);
        } else {
            /*
             * Safety re-parse
             */
            if (contains('<', segment)) {
                final Matcher m = FIX_START_TAG.matcher(segment);
                if (m.find() && isEmpty(m.group(2))) {
                    /*
                     * Re-parse start tag
                     */
                    final StreamedSource nestedSource = new StreamedSource(new StringAllocator(m.group(1)).append('>').toString());
                    for (final Segment nestedSegment : nestedSource) {
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

    private String fixStyleAttribute(final String startTag) {
        if (startTag.indexOf("style=") <= 0) {
            return startTag;
        }
        return INVALID_DELIM.matcher(startTag).replaceAll("; ");
    }

    private static boolean startsWith(final char startingChar, final CharSequence toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        int i = 0;
        if (Character.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Character.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    private static boolean contains(final char c, final CharSequence toCheck) {
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

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
