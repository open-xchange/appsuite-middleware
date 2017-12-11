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
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlExceptionCodes;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.internal.jericho.control.JerichoParseControl;
import com.openexchange.html.internal.jericho.control.JerichoParseControlTask;
import com.openexchange.html.internal.jericho.control.JerichoParseTask;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.html.tools.CombinedCharSequence;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.InterruptibleCharSequence.InterruptedRuntimeException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;
import net.htmlparser.jericho.TagType;

/**
 * {@link JerichoParser} - Parses specified real-life HTML document.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JerichoParser {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JerichoParser.class);

    private static final JerichoParser INSTANCE = new JerichoParser();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static JerichoParser getInstance() {
        return INSTANCE;
    }

    /**
     * Shuts-down the parser.
     */
    public static void shutDown() {
        INSTANCE.stop();
    }

    private static final class CSS {

        private final int cssThreshold;
        private boolean exceeded;

        private boolean css;
        private StringBuilder content;

        CSS(int cssThreshold) {
            super();
            this.cssThreshold = cssThreshold;
        }

        Segment getContent() {
            return null == content ? null : new Segment(new Source(content), 0, content.length());
        }

        boolean isCss() {
            return css;
        }

        void addSegment(Segment segment) {
            if (exceeded) {
                return;
            }

            if (null == content) {
                content = new StringBuilder(segment.length() << 1);
            }
            content.append(segment);
            if (cssThreshold > 0 && content.length() > cssThreshold) {
                LOG.debug("Discarding content of <style> tag as its size exceeds max. allowed size ({})", Integer.valueOf(cssThreshold));
                content = null;
                exceeded = true;
            }
        }

        boolean handleStartTag(StartTag startTag) {
            if (HTMLElementName.STYLE != startTag.getName()) {
                return false;
            }

            // Start of <style>
            css = true;
            return true;
        }

        boolean handleEndTag(EndTag endTag) {
            if (HTMLElementName.STYLE != endTag.getName()) {
                return false;
            }

            if (!css) {
                return false;
            }

            css = false;
            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------- //

    private final int htmlParseTimeoutSec;
    private final Thread controlRunner;

    /**
     * Initializes a new {@link JerichoParser}.
     */
    private JerichoParser() {
        super();
        ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        int defaultValue = 10;
        htmlParseTimeoutSec = null == service ? defaultValue : service.getIntProperty("com.openexchange.html.parse.timeout", defaultValue);
        if (htmlParseTimeoutSec > 0) {
            controlRunner = new Thread(new JerichoParseControlTask(), "JerichoControl");
            controlRunner.start();
        } else {
            controlRunner = null;
        }
    }

    /**
     * Stops this parser.
     */
    private void stop() {
        if (htmlParseTimeoutSec > 0) {
            JerichoParseControl.getInstance().add(JerichoParseTask.POISON);
        }
        Thread controlRunner = this.controlRunner;
        if (null != controlRunner) {
            controlRunner.interrupt();
        }
    }

    /**
     * Ensure given HTML content has a <code>&lt;body&gt;</code> tag.
     *
     * @param html The HTML content to check
     * @return The checked HTML content possibly with surrounded with a <code>&lt;body&gt;</code> tag
     * @throws OXException If specified HTML content cannot be parsed without wasting too many JVM resources
     */
    private boolean checkBody(String html, boolean checkSize) throws OXException {
        if (null == html) {
            return false;
        }
        if (checkSize) {
            int maxLength = HtmlServices.htmlThreshold();
            if (maxLength > 0 && html.length() > maxLength) {
                LOG.info("HTML content is too big: max. '{}', but is '{}'.", maxLength, html.length());
                throw HtmlExceptionCodes.TOO_BIG.create(maxLength, html.length());
            }
        }
        return (html.indexOf("<body") >= 0) || (html.indexOf("<BODY") >= 0);
    }

    private static final Pattern FIX_START_TAG = Pattern.compile("\\s*(<[a-zA-Z][^<>]+)(>?)\\s*");

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void parse(String html, JerichoHandler handler) throws OXException {
        parse(html, handler, true);
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link JerichoHandler}.
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @param checkSize Whether this call is supposed to check the size of given HTML content against <i>"com.openexchange.html.maxLength"</i> property
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void parse(final String html, final JerichoHandler handler, final boolean checkSize) throws OXException {
        int timeout = htmlParseTimeoutSec;
        if (timeout <= 0) {
            doParse(html, handler, checkSize);
            return;
        }

        // Run as a separate task monitored by Jericho control
        new JerichoParseTask(html, handler, checkSize, timeout, this).call();
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link JerichoHandler}.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">Never call this method directly!</div>
     * <p>
     *
     * @param html The real-life HTML document
     * @param handler The HTML handler
     * @param checkSize Whether this call is supposed to check the size of given HTML content against <i>"com.openexchange.html.maxLength"</i> property
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void doParse(String html, JerichoHandler handler, boolean checkSize) throws OXException {
        Thread thread = Thread.currentThread();
        StreamedSource streamedSource = null;
        try {
            if (false == checkBody(html, checkSize)) {
                // <body> tag not available
                handler.markBodyAbsent();
            }

            // Start regular parsing
            streamedSource = new StreamedSource(InterruptibleCharSequence.valueOf(html));
            streamedSource.setLogger(null);
            CSS css = new CSS(HtmlServices.cssThreshold());
            int lastSegmentEnd = 0;
            Segment prev = null;
            for (Iterator<Segment> iter = streamedSource.iterator(); !thread.isInterrupted() && iter.hasNext();) {
                Segment segment = iter.next();
                if (segment.getEnd() <= lastSegmentEnd) {
                    // If this tag is inside the previous tag (e.g. a server tag) then ignore it as it was already output along with the previous tag.
                    continue;
                }
                lastSegmentEnd = segment.getEnd();

                // Parsing left-over?
                if (null != prev) {
                    if (combineable(segment)) {
                        segment = combineSegments(prev, segment);
                    } else {
                        handleSegment(handler, prev, css, true, true);
                    }
                }

                // Handle current segment
                prev = handleSegment(handler, segment, css, true, false);
            }
        } catch (InterruptedRuntimeException e) {
            throw HtmlExceptionCodes.PARSING_FAILED.create("Parser timeout.", e);
        } catch (StackOverflowError parserOverflow) {
            throw HtmlExceptionCodes.PARSING_FAILED.create("Parser overflow detected.", parserOverflow);
        } finally {
            Streams.close(streamedSource);
        }
    }

    static Segment combineSegments(Segment prev, Segment segment) {
        CharSequence cs = new CombinedCharSequence(prev, segment);
        return new Segment(new Source(cs), 0, cs.length());
    }

    private boolean combineable(Segment segment) {
        return !(segment instanceof Tag);
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

    private static Segment handleSegment(JerichoHandler handler, Segment segment, CSS css, boolean fixStartTags, boolean force) throws OXException {
        if (segment instanceof Tag) {
            Tag tag = (Tag) segment;
            TagType tagType = tag.getTagType();

            EnumTagType enumType = EnumTagType.enumFor(tagType);
            if (null == enumType) {
                if (!segment.isWhiteSpace()) {
                    if (css.isCss()) {
                        css.addSegment(segment);
                    } else {
                        if ((tag instanceof StartTag) && (indexOf('<', 1, tag) >= 0)) {
                            throw HtmlExceptionCodes.CORRUPT.create();
                        }
                        handler.handleUnknownTag(tag);
                    }
                }
            } else {
                switch (enumType) {
                        case START_TAG:
                        {
                            StartTag startTag = (StartTag) tag;
                            if (css.handleStartTag(startTag)) {
                                // Start of <style> section
                                handler.markCssStart(startTag);
                            } else {
                                if (css.isCss()) {
                                    css.addSegment(segment);
                                } else {
                                    handler.handleStartTag(startTag);
                                }

                            }
                        }
                        break;
                    case END_TAG:
                        {
                            EndTag endTag = (EndTag) tag;
                            if (css.handleEndTag(endTag)) {
                                // Leaving <style> section
                                Segment cssContent = css.getContent();
                                if (Strings.isNotEmptyCharSequence(cssContent)) {
                                    handler.handleSegment(cssContent);
                                }
                                handler.markCssEnd(endTag);
                            } else {
                                if (css.isCss()) {
                                    css.addSegment(segment);
                                } else {
                                    handler.handleEndTag(endTag);
                                }
                            }
                        }
                        break;
                    case DOCTYPE_DECLARATION:
                        if (css.isCss()) {
                            css.addSegment(segment);
                        } else {
                            handler.handleDocDeclaration(segment.toString());
                        }
                        break;
                    case CDATA_SECTION:
                        if (css.isCss()) {
                            css.addSegment(segment);
                        } else {
                            handler.handleCData(segment.toString());
                        }
                        break;
                    case COMMENT:
                        if (css.isCss()) {
                            css.addSegment(segment);
                        } else {
                            handler.handleComment(segment.toString());
                        }
                        break;
                    default:
                        break;
                }
            }
            return null;
        }

        if (css.isCss()) {
            css.addSegment(segment);
            return null;
        }

        if (segment instanceof CharacterReference) {
            CharacterReference characterReference = (CharacterReference) segment;
            handler.handleCharacterReference(characterReference);
            return null;
        }

        // Safety re-parse
        return safeParse(handler, segment, css, fixStartTags, force);
    }

    private static Segment safeParse(JerichoHandler handler, Segment segment, CSS css, boolean fixStartTags, boolean force) throws OXException {
        if (!fixStartTags || !containsStartTag(segment)) {
            handler.handleSegment(saneCharSequence(segment));
            return null;
        }

        Matcher m = FIX_START_TAG.matcher(segment);
        if (!m.find()) {
            handler.handleSegment(saneCharSequence(segment));
            return null;
        }

        String startTag = m.group(1);
        if (startTag.startsWith("<!--")) {
            handler.handleComment(m.group());
            return null;
        }

        String closing = m.group(2);
        boolean orderlyClosed = false == Strings.isEmpty(closing); // Ending '>' found?

        if (!force) {
            if (!orderlyClosed) {
                // No closing '>' found
                // throw HtmlExceptionCodes.CORRUPT.create();
            }
        }

        int start = m.start();
        if (start > 0) {
            handler.handleSegment(saneCharSequence(segment.subSequence(0, start)));
        }

        int[] remainder = null;
        int end = m.end();
        if (end < segment.length()) {
            if (orderlyClosed) {
                remainder = new int[] { end, segment.length() };
            } else {
                int pos = indexOf('>', end, segment);
                if (pos >= 0) {
                    startTag = startTag + segment.subSequence(end, pos + 1);
                    remainder = new int[] { pos + 1, segment.length() };
                } else {
                    remainder = new int[] { end, segment.length() };
                }
            }
        }

        @SuppressWarnings("resource") StreamedSource nestedSource = new StreamedSource(dropWeirdAttributes(startTag)); // No need to close since String-backed (all in memory)!
        Thread thread = Thread.currentThread();

        Iterator<Segment> iter = nestedSource.iterator();
        if (!thread.isInterrupted() && iter.hasNext()) {
            Segment firstSegment = iter.next();
            if (firstSegment instanceof Tag) {
                // All fine
                handleSegment(handler, firstSegment, css, false, true);
            } else if (firstSegment.length() <= 0 || firstSegment.charAt(0) != '<' || firstSegment.charAt(firstSegment.length() - 1) != '>' || (indexOf('<', 1, firstSegment) >= 0)) {
                // Start tag detection did not help
                throw HtmlExceptionCodes.CORRUPT.create();
            } else {
                // Seems to be a valid start tag that cannot be parsed. Most likely due to invalid attributes
                // Ignore...
            }

            while (!thread.isInterrupted() && iter.hasNext()) {
                Segment nestedSegment = iter.next();
                handleSegment(handler, nestedSegment, css, false, true);
            }
        }

        if (null != remainder) {
            return safeParse(handler, new Segment(new Source(segment), remainder[0], remainder[1]), css, fixStartTags, force);
            // handler.handleSegment(remainder);
        }
        return null;
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

    private static CharSequence saneCharSequence(CharSequence segment) {
        if (indexOf('<', 0, segment) < 0 && indexOf('>', 0, segment) < 0) {
            return segment;
        }

        String content = segment.toString();
        int len = content.length();
        StringBuilder sb = new StringBuilder(content.length() + 16);
        for (int i = 0, k = len; k-- > 0; i++) {
            char ch = content.charAt(i);
            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

}
