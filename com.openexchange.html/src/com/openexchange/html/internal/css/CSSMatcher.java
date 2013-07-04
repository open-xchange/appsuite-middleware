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

package com.openexchange.html.internal.css;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.html.internal.MatcherReplacer;
import com.openexchange.html.internal.RegexUtility;
import com.openexchange.html.internal.RegexUtility.GroupType;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.StringBufferStringer;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;
import com.openexchange.java.Strings;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CSSMatcher} - Provides several utility methods to check CSS content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSSMatcher {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CSSMatcher.class);

    /** Perform CSS sanitizing with respect to nested blocks */
    private static final boolean CONSIDER_NESTED_BLOCKS = true;

    /**
     * Initializes a new {@link CSSMatcher}
     */
    private CSSMatcher() {
        super();
    }

    /*
     * The patterns for values
     */

    private static final Pattern PAT_N;

    private static final Pattern PAT_n;

    private static final Pattern PAT_c;

    private static final Pattern PAT_u;

    private static final Pattern PAT_t;

    private static final Pattern PATTERN_IS_PATTERN;

    private static final Pattern PATTERN_COLOR_RGB;

    static {
        /*
         * Regular expression for CSS2 values
         */
        final String strINTEGER = "(?:(?:\\+|-)?[0-9]+)";

        final String strREAL = "(?:(?:\\+|-)?[0-9]*\\.[0-9]+)";

        final String strNUMBER = RegexUtility.group(RegexUtility.OR(strINTEGER, strREAL), GroupType.NON_CAPTURING);

        final String strREL_UNITS = "(?:em|ex|px)";

        final String strABS_UNITS = "(?:in|cm|mm|pt|pc)";

        final String strUNITS = RegexUtility.group(RegexUtility.OR(strREL_UNITS, strABS_UNITS), GroupType.NON_CAPTURING);

        final String strLENGTH = RegexUtility.group(RegexUtility.OR(RegexUtility.concat(strNUMBER, strUNITS), RegexUtility.concat(
            "(?:\\\\+|-)?",
            "0",
            RegexUtility.optional(strUNITS))), GroupType.NON_CAPTURING);

        final String strPERCENTAGE = RegexUtility.group(RegexUtility.concat(strNUMBER, "%"), GroupType.NON_CAPTURING);

        final String strLENGTH_OR_PERCENTAGE = RegexUtility.group(RegexUtility.OR(strLENGTH, strPERCENTAGE), GroupType.NON_CAPTURING);

        final String strTIME_UNITS = "(?:ms|s)";

        final String strTIME = RegexUtility.group(RegexUtility.concat(strNUMBER, strTIME_UNITS), GroupType.NON_CAPTURING);

        final String strURL = "url\\(\"?[\\p{ASCII}\\p{L}]+\"?\\)";

        final String strCOLOR_KEYWORD = RegexUtility.group(
            "aqua|black|blue|fuchsia|gray|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow",
            GroupType.NON_CAPTURING);

        final String strCOLOR_SYSTEM = "ActiveBorder|ActiveCaption|AppWorkspace|Background|" +
                "ButtonFace|ButtonHighlight|ButtonShadow|ButtonText|CaptionTextGrayText|" +
                "Highlight|HighlightText|InactiveBorder|InactiveCaption|InactiveCaptionText|" +
                "InfoBackground|InfoText|Menu|MenuText|Scrollbar|ThreeDDarkShadow|" +
                "ThreeDFace|ThreeDHighlight|ThreeDLightShadow|ThreeDShadow|Window|WindowFrame|WindowText";

        final String strCOLOR_RGB_HEX = "#?\\p{XDigit}{3,6}";

        final String strCSV_DELIM = "\\s*,\\s*";

        final String strCOLOR_RGB_FUNC = RegexUtility.concat("rgb\\(", RegexUtility.group(
            RegexUtility.OR(RegexUtility.concat(strINTEGER, strCSV_DELIM, strINTEGER, strCSV_DELIM, strINTEGER), RegexUtility.concat(
                strPERCENTAGE,
                strCSV_DELIM,
                strPERCENTAGE,
                strCSV_DELIM,
                strPERCENTAGE)),
            GroupType.NON_CAPTURING), "\\)");

        final String strCOLOR = RegexUtility.group(RegexUtility.concat(
            strCOLOR_KEYWORD,
            "|",
            strCOLOR_SYSTEM,
            "|",
            strCOLOR_RGB_HEX,
            "|",
            strCOLOR_RGB_FUNC), GroupType.NON_CAPTURING);
        /*
         * Initialize patterns with strings
         */
        PAT_N = Pattern.compile(strLENGTH_OR_PERCENTAGE);

        PAT_n = Pattern.compile(strLENGTH);

        PAT_c = Pattern.compile(strCOLOR, Pattern.CASE_INSENSITIVE);

        PAT_u = Pattern.compile(strURL, Pattern.CASE_INSENSITIVE);

        PAT_t = Pattern.compile(strTIME, Pattern.CASE_INSENSITIVE);

        PATTERN_IS_PATTERN = Pattern.compile("[unNcd*t]+");

        PATTERN_COLOR_RGB = Pattern.compile(strCOLOR_RGB_FUNC, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Checks if specified CSS value is matched by given allowed values
     * <p>
     * The allowed values may contain following patterns to cover certain CSS types:
     * <ul>
     * <li><b>u</b>:&nbsp;url(&lt;URL&gt;)</li>
     * <li><b>n</b>:&nbsp;number string without %</li>
     * <li><b>N</b>:&nbsp;number string</li>
     * <li><b>c</b>:&nbsp;color</li>
     * <li><b>d</b>:&nbsp;delete</li>
     * <li><b>*</b>:&nbsp;any value</li>
     * <li><b>t</b>:&nbsp;time</li>
     * </ul>
     *
     * @param value The value
     * @param allowedValuesSet The allowed values
     * @return <code>true</code> if value is matched by given allowed values; otherwise <code>false</code>
     */
    public static boolean matches(final String value, final Set<String> allowedValuesSet) {
        final int size = allowedValuesSet.size();
        final String[] allowedValues = allowedValuesSet.toArray(new String[size]);
        /*
         * Ensure to check against pattern first
         */
        final Set<Integer> patIndices = new HashSet<Integer>(2);
        for (int i = 0; i < size; i++) {
            final String allowedValue = allowedValues[i];
            if (PATTERN_IS_PATTERN.matcher(allowedValue).matches()) {
                patIndices.add(Integer.valueOf(i));
                if (allowedValue.indexOf('d') >= 0) {
                    return false;
                }
                if (allowedValue.indexOf('*') >= 0) {
                    return true;
                }
                final int length = allowedValue.length();
                for (int j = 0; j < length; j++) {
                    if (matchesPattern(allowedValue.charAt(j), value)) {
                        return true;
                    }
                }
            }
        }
        /*
         * Now check against values
         */
        boolean retval = false;
        for (int i = 0; i < size && !retval; i++) {
            if (!patIndices.contains(Integer.valueOf(i))) {
                /*
                 * Check against non-pattern allowed value
                 */
                retval = allowedValues[i].equalsIgnoreCase(value);
            }
        }
        return retval;
    }

    private static boolean matchesPattern(final char pattern, final String value) {
        // u: url(<URL>);
        // n: number string without %
        // N: number string
        // c: color
        // d: delete
        // t: time
        switch (pattern) {
        case 'u':
            return PAT_u.matcher(value).matches();
        case 'n':
            return PAT_n.matcher(value).matches();
        case 'N':
            return PAT_N.matcher(value).matches();
        case 'c':
            return PAT_c.matcher(value).matches();
        case 'd':
            return false;
        case 't':
            return PAT_t.matcher(value).matches();
        default:
            return false;
        }
    }

    /** Matches a starting CSS block */
    static final Pattern PATTERN_STYLE_STARTING_BLOCK = Pattern.compile("(?:#|\\.|@|[a-zA-Z])[^{/]*?\\{");
    /** Matches a complete CSS block, but not appropriate for possible nested blocks */
    private static final Pattern PATTERN_STYLE_BLOCK = Pattern.compile("((?:#|\\.|[a-zA-Z])[^{]*?\\{)([^}/]+)\\}");
    /** Matches a CR?LF plus indention */
    static final Pattern CRLF = Pattern.compile("\r?\n( {2,})?");

    /**
     * Iterates over CSS contained in specified string argument and checks each found element/block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param cssPrefix The CSS prefix
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix) {
        return checkCSS(cssBuilder, styleMap, cssPrefix, false);
    }

    private static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix, final boolean internallyInvoked) {
        // Schedule separate task to monitor duration
        // User StringBuffer-based invocation to honor concurrency
        final Stringer cssBld = new StringBufferStringer(new StringBuffer(cssBuilder.toString()));
        cssBuilder.setLength(0);
        final Task<Boolean> task = new AbstractTask<Boolean>() {

            @Override
            public Boolean call() {
                if (isEmpty(cssPrefix)) {
                    return Boolean.valueOf(checkCSS(cssBld, styleMap, true));
                }
                boolean modified = false;
                /*
                 * Feed matcher with buffer's content and reset
                 */
                if (cssBld.indexOf("{") < 0) {
                    return Boolean.valueOf(checkCSSElements(cssBld, styleMap, true));
                }
                final String css = CRLF.matcher(cssBld).replaceAll(" ");
                final int length = css.length();
                cssBld.setLength(0);
                final Stringer cssElemsBuffer = new StringBuilderStringer(new StringBuilder(length));
                // Block-wise sanitizing
                if (CONSIDER_NESTED_BLOCKS) {
                    int off = 0;
                    Matcher m;
                    while (off < length && (m = PATTERN_STYLE_STARTING_BLOCK.matcher(css.substring(off))).find()) {
                        final int end = m.end() + off;
                        int index = end;
                        int level = 1;
                        while (level > 0 && index < length) {
                            final char c = css.charAt(index++);
                            if ('{' == c) {
                                level++;
                            } else if ('}' == c) {
                                level--;
                            }
                        }
                        // Check prefix part
                        final int start = m.start() + off;
                        cssElemsBuffer.append(css.substring(off, start));
                        modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
                        final String prefix = cssElemsBuffer.toString();
                        cssElemsBuffer.setLength(0);
                        // Check block part
                        cssElemsBuffer.append(css.substring(end, index - 1));
                        modified |= checkCSS(cssElemsBuffer, styleMap, cssPrefix, true);
                        // Check selector part
                        cssElemsBuffer.insert(0, prefixBlock(css.substring(start, end - 1), cssPrefix)).append('}').append('\n'); // Surround with block definition
                        final String block = cssElemsBuffer.toString();
                        cssElemsBuffer.setLength(0);
                        // Add to main builder
                        cssBld.append(prefix);
                        cssBld.append(block);
                        off = index;
                    }
                    cssElemsBuffer.append(css.substring(off, css.length()));
                } else {
                    final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
                    cssBld.setLength(0);
                    int lastPos = 0;
                    while (m.find()) {
                        // Check prefix part
                        cssElemsBuffer.append(css.substring(lastPos, m.start()));
                        modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
                        final String prefix = cssElemsBuffer.toString();
                        cssElemsBuffer.setLength(0);
                        // Check block part
                        cssElemsBuffer.append(m.group(2));
                        modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
                        cssElemsBuffer.insert(0, prefixBlock(m.group(1), cssPrefix)).append('}').append('\n'); // Surround with block definition
                        final String block = cssElemsBuffer.toString();
                        cssElemsBuffer.setLength(0);
                        // Add to main builder
                        cssBld.append(prefix);
                        cssBld.append(block);
                        lastPos = m.end();
                    }
                    cssElemsBuffer.append(css.substring(lastPos, css.length()));
                }
                modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
                final String tail = cssElemsBuffer.toString();
                cssElemsBuffer.setLength(0);
                cssBld.append(tail);
                return Boolean.valueOf(modified);
            }
        };
        // Check for internal invocation
        final ThreadPoolService threadPool;
        if (internallyInvoked || (null == (threadPool = ThreadPools.getThreadPool()))) {
            // Invoke with current thread
            boolean ran = false;
            task.beforeExecute(Thread.currentThread());
            try {
                final boolean retval = task.call().booleanValue();
                cssBuilder.append(cssBld);
                ran = true;
                task.afterExecute(null);
                return retval;
            } catch (final Exception ex) {
                if (!ran) {
                    task.afterExecute(ex);
                }
                LOG.error(ex.getMessage(), ex);
                cssBuilder.setLength(0);
                return false;
            }
        }
        // Submit to thread pool ...
        final Future<Boolean> f = threadPool.submit(task);
        // ... and await response
        final int timeout = 7;
        final TimeUnit timeUnit = TimeUnit.SECONDS;
        try {
            final boolean retval = f.get(timeout, timeUnit).booleanValue();
            cssBuilder.append(cssBld);
            return retval;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            cssBuilder.setLength(0);
            return false;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            LOG.error(cause.getMessage(), cause);
            cssBuilder.setLength(0);
            f.cancel(true);
            return false;
        } catch (final TimeoutException e) {
            // Wait time exceeded
            LOG.warn(Strings.concat(Strings.getLineSeparator(), "Parsing of CSS content exceeded max. response time of ", Integer.valueOf(timeout), toLowerCase(timeUnit.name()), Strings.getLineSeparator()));
            cssBuilder.setLength(0);
            f.cancel(true);
            return false;
        }
    }

    private static final Pattern SPLIT_LINES = Pattern.compile("\r?\n");
    private static final Pattern SPLIT_WORDS = Pattern.compile("\\s+");
    private static final Pattern SPLIT_COMMA = Pattern.compile(",");

    static String prefixBlock(final String match, final String cssPrefix) {
        if (isEmpty(match) || isEmpty(cssPrefix)) {
            return match;
        }
        final int length = match.length();
        if (1 == length) {
            return match;
        }
        // Cut off trailing '{' character
        final String s = match.indexOf('{') < 0 ? match : match.substring(0, length - 1);
        if (isEmpty(s)) {
            return match;
        }
        final int len = s.length();
        int pos = 0;
        while (pos < len && Strings.isWhitespace(s.charAt(pos))) {
            pos++;
        }
        final StringBuilder builder = new StringBuilder(length << 1);
        if (pos > 0) {
            builder.append(s.substring(0, pos));
        }
        final StringBuilder helper = new StringBuilder(length << 1);
        /*-
         * SIMPLE SELECTORS
         *
         * #id -> #prefix #prefix-id
         * .class -> #prefix .prefix-class
         * element -> #prefix element
         *
         * GROUPS (set of simple selectors; comma separated)
         *
         * #id, .class -> #prefix #prefix-id, #prefix .prefix-class
         * #id, element -> #prefix #prefix-id, #prefix element
         * .class-A, .class-B -> #prefix .prefix-class-A, #prefix .prefix-class-B
         *
         * Escape every ID, escape every class name, prefix every SIMPLE SELECTOR with #prefix,
         * every time when occurring within a line
         */
        final String input = s.substring(pos);
        if (input.indexOf('\n') < 0) {
            handleLine(input, cssPrefix, builder, helper);
        } else {
            for (final String line : SPLIT_LINES.split(input, 0)) {
                handleLine(line, cssPrefix, builder, helper);
                builder.append('\n');
            }
            builder.deleteCharAt(builder.length()-1);
        }
        return builder.append('{').toString();
    }

    private static void handleLine(final String line, final String cssPrefix, final StringBuilder builder, final StringBuilder helper) {
        if (line.charAt(0) == '@') {
            builder.append(line);
            return;
        }
        final String[] splits = SPLIT_COMMA.split(line, 0);
        if (1 == splits.length) {
            handleWords(line, cssPrefix, builder, helper);
        } else {
            boolean fst = true;
            for (final String sWords : splits) {
                if (!isEmpty(sWords)) {
                    if (fst) {
                        fst = false;
                    } else {
                        builder.append(", ");
                    }
                    handleWords(sWords, cssPrefix, builder, helper);
                }
            }
        }
    }

    private static void handleWords(final String sWords, final String cssPrefix, final StringBuilder builder, final StringBuilder helper) {
        final String[] words = SPLIT_WORDS.split(sWords, 0);
        if (1 == words.length && toLowerCase(words[0]).indexOf("body") >= 0) {
            // Special treatment for "body" selector
            builder.append('#').append(cssPrefix).append(' ');
        } else {
            boolean first = true;
            for (final String word : words) {
                if (isEmpty(word)) {
                    builder.append(word);
                } else {
                    if (first) {
                        handleSelector(cssPrefix, builder, helper, word, true);
                        first = false;
                    } else {
                        builder.append(' ');
                        handleSelector(cssPrefix, builder, helper, word, false);
                    }
                }
            }
            builder.append(' ');
        }
    }

    private static void handleSelector(final String cssPrefix, final StringBuilder builder, final StringBuilder helper, final String selector, final boolean first) {
        final char firstChar = selector.charAt(0);
        if ('.' == firstChar) {
            // .class -> #prefix .prefix-class
            if (first) {
                builder.append('#').append(cssPrefix).append(' ');
            }
            builder.append('.').append(cssPrefix).append('-');
            builder.append(replaceDotsAndHashes(selector.substring(1), cssPrefix, helper));
        } else if ('#' == firstChar) {
            // #id -> #prefix #prefix-id
            if (first) {
                builder.append('#').append(cssPrefix).append(' ');
            }
            builder.append('#').append(cssPrefix).append('-');
            builder.append(replaceDotsAndHashes(selector.substring(1), cssPrefix, helper));
        } else {
            // element -> #prefix element
            if (first) {
                builder.append('#').append(cssPrefix).append(' ');
            }
            builder.append(replaceDotsAndHashes(selector, cssPrefix, helper));
        }
    }

    private static String replaceDotsAndHashes(final String word, final String cssPrefix, final StringBuilder helper) {
        final int length = word.length();
        helper.setLength(0);
        int prev = 0;
        for (int i = 0; i < length; i++) {
            final char c = word.charAt(i);
            if ('.' == c) {
                helper.append(prev == i ? "" : word.substring(prev, i));
                helper.append('.').append(cssPrefix).append('-');
                prev = i + 1;
            } else if ('#' == c) {
                helper.append(prev == i ? "" : word.substring(prev, i));
                helper.append('#').append(cssPrefix).append('-');
                prev = i + 1;
            }
        }
        if (prev < length) {
            helper.append(word.substring(prev));
        }
        return helper.toString();
    }

    /**
     * Iterates over CSS contained in specified string argument and checks each found element/block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param removeIfAbsent <code>true</code> to completely remove CSS element if not contained in specified style map; otherwise
     *            <code>false</code>
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final boolean removeIfAbsent) {
        boolean modified = false;
        /*
         * Feed matcher with buffer's content and reset
         */
        if (cssBuilder.indexOf("{") < 0) {
            return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
        }
        final String css = CRLF.matcher(cssBuilder).replaceAll(" ");
        final Stringer cssElemsBuffer = new StringBuilderStringer(new StringBuilder(css.length()));
        final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
        if (!m.find()) {
            return false;
        }
        cssBuilder.setLength(0);
        int lastPos = 0;
        do {
            // Check prefix part
            cssElemsBuffer.append(css.substring(lastPos, m.start()));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            final String prefix = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Check block part
            cssElemsBuffer.append(m.group(2));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            cssElemsBuffer.insert(0, m.group(1)).append('}').append('\n'); // Surround with block definition
            final String block = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Add to main builder
            cssBuilder.append(prefix);
            cssBuilder.append(block);
            lastPos = m.end();
        } while (m.find());
        cssElemsBuffer.append(css.substring(lastPos, css.length()));
        modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
        final String tail = cssElemsBuffer.toString();
        cssElemsBuffer.setLength(0);
        cssBuilder.append(tail);
        return modified;
    }

    /**
     * Iterates over CSS blocks contained in specified string argument and checks each block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param findBlocks <code>true</code> to iterate over CSS blocks; otherwise <code>false</code> to iterate over CSS elements
     * @param removeIfAbsent <code>true</code> to completely remove CSS element if not contained in specified style map; otherwise
     *            <code>false</code>
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final boolean findBlocks, final boolean removeIfAbsent) {
        if (findBlocks) {
            boolean modified = false;
            final Stringer cssElemsBuffer = new StringBuilderStringer(new StringBuilder(128));
            final StringBuilder tmpBuilder = new StringBuilder(128);
            /*
             * Feed matcher with buffer's content and reset
             */
            if (cssBuilder.indexOf("{") < 0) {
                return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
            }
            final String css = CRLF.matcher(cssBuilder.toString()).replaceAll(" ");
            final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
            final MatcherReplacer mr = new MatcherReplacer(m, css);
            cssBuilder.setLength(0);
            while (m.find()) {
                modified |= checkCSSElements(cssElemsBuffer.append(m.group(2)), styleMap, removeIfAbsent);
                tmpBuilder.setLength(0);
                mr.appendLiteralReplacement(
                    cssBuilder,
                    tmpBuilder.append(m.group(1)).append(cssElemsBuffer.toString()).append('}').append('\n').toString());
                cssElemsBuffer.setLength(0);
            }
            mr.appendTail(cssBuilder);
            return modified;
        }
        return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
    }

    private static final Pattern PATTERN_STYLE_LINE = Pattern.compile(
        "([\\p{Alnum}-_]+)\\s*:\\s*([\\p{Print}&&[^;{}]]+);?",
        Pattern.CASE_INSENSITIVE);

    /**
     * Corrects rgb functions; e.g.<br>
     * "<i>rgb(238,&nbsp;239,&nbsp;240)</i>"&nbsp;-&gt;&nbsp; "<i>rgb(238,239,240)</i>"
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     */
    private static void correctRGBFunc(final Stringer cssBuilder) {
        final Matcher rgb;
        final MatcherReplacer mr;
        {
            final String s = cssBuilder.toString();
            rgb = PATTERN_COLOR_RGB.matcher(s);
            mr = new MatcherReplacer(rgb, s);
        }
        cssBuilder.setLength(0);
        while (rgb.find()) {
            mr.appendLiteralReplacement(cssBuilder, rgb.group().replaceAll("\\s+", ""));
        }
        mr.appendTail(cssBuilder);
    }

    /**
     * Iterates over CSS elements contained in specified string argument and checks each element and its value against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing the CSS content
     * @param styleMap The style map
     * @param removeIfAbsent <code>true</code> to completely remove CSS element if not contained in specified style map; otherwise
     *            <code>false</code>
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    static boolean checkCSSElements(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final boolean removeIfAbsent) {
        if (null == styleMap) {
            return false;
        }
        boolean modified = false;
        correctRGBFunc(cssBuilder);
        /*
         * Feed matcher with buffer's content and reset
         */
        final Matcher m;
        final MatcherReplacer mr;
        {
            final String str = cssBuilder.toString();
            m = PATTERN_STYLE_LINE.matcher(str);
            mr = new MatcherReplacer(m, str);
        }
        cssBuilder.setLength(0);
        final StringBuilder elemBuilder = new StringBuilder(128);
        while (m.find()) {
            final String elementName = m.group(1);
            if (null != elementName) {
                if (styleMap.containsKey(elementName.toLowerCase(Locale.ENGLISH))) {
                    elemBuilder.append(elementName).append(':').append(' ');
                    final Set<String> allowedValuesSet = styleMap.get(elementName.toLowerCase(Locale.ENGLISH));
                    final String elementValues = m.group(2);
                    boolean hasValues = false;
                    if (matches(elementValues, allowedValuesSet)) {
                        /*
                         * Direct match
                         */
                        elemBuilder.append(elementValues);
                        hasValues = true;
                    } else {
                        final String[] tokens = Strings.splitByWhitespaces(elementValues);
                        for (int j = 0; j < tokens.length; j++) {
                            if (matches(tokens[j], allowedValuesSet)) {
                                if (j > 0) {
                                    elemBuilder.append(' ');
                                }
                                elemBuilder.append(tokens[j]);
                                hasValues = true;
                            } else {
                                modified = true;
                            }
                        }
                    }
                    if (hasValues) {
                        elemBuilder.append(';');
                        mr.appendLiteralReplacement(cssBuilder, elemBuilder.toString());
                    } else {
                        /*
                         * Remove element since none of its values is allowed
                         */
                        modified = true;
                        mr.appendReplacement(cssBuilder, "");
                    }
                    elemBuilder.setLength(0);
                } else if (removeIfAbsent) {
                    /*
                     * Remove forbidden element
                     */
                    modified = true;
                    mr.appendReplacement(cssBuilder, "");
                }
            }
        }
        mr.appendTail(cssBuilder);
        return modified;
    }

    /**
     * Checks if specified string argument contains at least one CSS element
     *
     * @param css The CSS string
     * @return <code>true</code> if specified string argument contains at least one CSS element; otherwise <code>false</code>
     */
    public static boolean containsCSSElement(final String css) {
        if (null == css || isEmpty(css)) {
            return false;
        }
        return PATTERN_STYLE_LINE.matcher(css).find();
    }

    static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static String toLowerCase(final CharSequence chars) {
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
