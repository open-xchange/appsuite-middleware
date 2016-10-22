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

package com.openexchange.html.internal.css;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.Result;
import com.openexchange.html.internal.MatcherReplacer;
import com.openexchange.html.internal.RegexUtility;
import com.openexchange.html.internal.RegexUtility.GroupType;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.InterruptibleCharSequence;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSSMatcher.class);

    private static volatile Integer cssParseTimeoutSec;

    protected static int cssParseTimeoutSec() {
        Integer tmp = cssParseTimeoutSec;
        if (null == tmp) {
            synchronized (CSSMatcher.class) {
                tmp = cssParseTimeoutSec;
                if (null == tmp) {
                    final ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final int defaultValue = 4;
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.html.css.parse.timeout", defaultValue));
                    cssParseTimeoutSec = tmp;
                }
            }
        }
        return tmp.intValue();
    }

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

    private static final Pattern PAT_u_cid;

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

        final String strURLCid = "url\\(\"?cid:[\\p{ASCII}\\p{L}]+\"?\\)";

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

        PAT_u_cid = Pattern.compile(strURLCid, Pattern.CASE_INSENSITIVE);

        PAT_t = Pattern.compile(strTIME, Pattern.CASE_INSENSITIVE);

        PATTERN_IS_PATTERN = Pattern.compile("[uinNcd*t]+");

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
        final Set<String> allowedValues = new HashSet<String>(allowedValuesSet);
        /*
         * Ensure to check against pattern first
         */
        for (Iterator<String> it = allowedValues.iterator(); it.hasNext();) {
            String allowedValue = it.next();
            if (PATTERN_IS_PATTERN.matcher(allowedValue).matches()) {
                it.remove();
                if (allowedValue.indexOf('d') >= 0) {
                    return false;
                }
                if (allowedValue.indexOf('*') >= 0) {
                    return true;
                }
                int length = allowedValue.length();
                for (int k = length, j = 0; k-- > 0; j++) {
                    Result result = matchesPattern(allowedValue.charAt(j), value);
                    if (Result.DENY == result) {
                        return false;
                    }
                    if (Result.ALLOW == result) {
                        return true;
                    }
                }
            }
        }
        /*
         * Now check against values
         */
        boolean retval = false;
        int pos = value.indexOf('(');
        String checkEqualsWith = pos > 0 && value.indexOf(')', pos) > 0 ? value.substring(0, pos) : value;
        for (Iterator<String> it = allowedValues.iterator(); !retval && it.hasNext();) {
            /*
             * Check against non-pattern allowed value
             */
            retval = it.next().equalsIgnoreCase(checkEqualsWith);
        }
        return retval;
    }

    private static Result matchesPattern(final char pattern, final String value) {
        // u: url(<URL>);
        // n: number string without %
        // N: number string
        // c: color
        // d: delete
        // t: time
        switch (pattern) {
        case 'i':
            return PAT_u_cid.matcher(value).matches() ? Result.ALLOW : Result.NEUTRAL;
        case 'u':
            {
                Matcher matcher = PAT_u.matcher(value);
                if (!matcher.matches()) {
                    return Result.NEUTRAL;
                }
                Result dataUriResult = HtmlServices.isAcceptableDataUri(value, null);
                return Result.NEUTRAL == dataUriResult ? Result.ALLOW : dataUriResult;
            }
        case 'n':
            return PAT_n.matcher(value).matches() ? Result.ALLOW : Result.NEUTRAL;
        case 'N':
            return PAT_N.matcher(value).matches() ? Result.ALLOW : Result.NEUTRAL;
        case 'c':
            return PAT_c.matcher(value).matches() ? Result.ALLOW : Result.NEUTRAL;
        case 'd':
            return Result.NEUTRAL;
        case 't':
            return PAT_t.matcher(value).matches() ? Result.ALLOW : Result.NEUTRAL;
        default:
            return Result.NEUTRAL;
        }
    }

    /** Matches a starting CSS block */
    protected static final Pattern PATTERN_STYLE_STARTING_BLOCK = Pattern.compile("(?:\\*|#|\\.|@|[a-zA-Z])[^{/;]*?\\{");

    /** Matches a complete CSS block, but not appropriate for possible nested blocks */
    private static final Pattern PATTERN_STYLE_BLOCK = Pattern.compile("((?:\\*|#|\\.|[a-zA-Z])[^{]*?\\{)([^}/]+)\\}");

    /** Matches a CR?LF plus indention */
    protected static final Pattern CRLF = Pattern.compile("\r?\n( {2,})?");

    /** Matches multiple white-spaces */
    protected static final Pattern WS = Pattern.compile(" +");

    /**
     * Iterates over CSS contained in specified string argument and checks each found element/block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param cssPrefix The CSS prefix
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix) {
        return checkCSS(cssBuilder, styleMap, cssPrefix, true, false);
    }

    /**
     * Iterates over CSS contained in specified string argument and checks each found element/block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param cssPrefix The CSS prefix
     * @param removeIfAbsent <code>true</code> to remove if not contained in style map
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix, final boolean removeIfAbsent) {
        return checkCSS(cssBuilder, styleMap, cssPrefix, removeIfAbsent, false);
    }

    private static final int MAX_ALLOWED_CSS_SELECTOR_SIZE = 2048;

    /**
     * Checks if CSS content shall be further parsed or not.
     *
     * @param off The offset
     * @param css The CSS content
     * @return <code>true</code> to continue parsing; otherwise <code>false</code> to stop
     */
    protected static boolean continueParsing(final int off, final String css) {
        final int pos = css.indexOf('{', off);
        if (pos < 0) {
            return false;
        }
        final int diff = pos - off;
        LOG.debug("Next '{' is {} characters away -- {}", diff, (diff <= 2048 ? "Continue" : "Abort"));
        return diff <= MAX_ALLOWED_CSS_SELECTOR_SIZE;
    }

    /**
     * Drops comments from given CSS snippet.
     *
     * @param cssSnippet The CSS snippet
     * @return The CSS snippet cleansed by comments
     */
    protected static String dropComments(final String cssSnippet) {
        // Check for comment
        int cstart = cssSnippet.indexOf("/*");
        if (cstart < 0) {
            return cssSnippet;
        }
        int cend = cssSnippet.indexOf("*/");
        if (cend <= 0 || cend <= cstart) {
            return cssSnippet;
        }
        final StringBuilder hlp = new StringBuilder(cssSnippet);
        do {
            hlp.delete(cstart, cend + 2);
            cstart = hlp.indexOf("/*");
            cend = hlp.indexOf("*/");
        } while (cstart >= 0 && cend > 0 && cend > cstart);
        return hlp.toString();
    }

    /**
     * Checks the CSS provided by given <code>Stringer</code>.
     *
     * @param cssBuilder The CSS content
     * @param styleMap The style map
     * @param cssPrefix The CSS prefix
     * @param removeIfAbsent <code>true</code> if CSS elements should be removed if they are not within the styleMap; otherwise
     *            <code>false</code>
     * @param internallyInvoked <code>true</code> if already internally invoked; otherwise <code>false</code>
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    protected static boolean checkCSS(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix, final boolean removeIfAbsent, final boolean internallyInvoked) {
        if (cssBuilder.isEmpty()) {
            return false;
        }

        // Schedule separate task to monitor duration
        // User StringBuffer-based invocation to honor concurrency
        final Stringer cssBld = new StringBufferStringer(new StringBuffer(cssBuilder.toString()));
        cssBuilder.setLength(0);

        // Check for internal invocation and thread pool availability
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (internallyInvoked || (threadPool == null) || (Thread.currentThread().getName().startsWith("JerichoParser"))) {
            boolean retval = doCheckCss(cssBld, styleMap, cssPrefix, removeIfAbsent);
            cssBuilder.append(cssBld);
            return retval;
        }

        // Run as a separate task
        Task<Boolean> task = new AbstractTask<Boolean>() {

            @Override
            public Boolean call() {
                return Boolean.valueOf(doCheckCss(cssBld, styleMap, cssPrefix, removeIfAbsent));
            }
        };

        // Submit to thread pool ...
        Future<Boolean> f = threadPool.submit(task);
        // ... and await response
        int timeout = cssParseTimeoutSec();
        TimeUnit timeUnit = TimeUnit.SECONDS;
        try {
            boolean retval = f.get(timeout, timeUnit).booleanValue();
            cssBuilder.append(cssBld);
            return retval;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cssBuilder.setLength(0);
            return false;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            LOG.error("", cause);
            cssBuilder.setLength(0);
            f.cancel(true);
            return false;
        } catch (TimeoutException e) {
            // Wait time exceeded
            LOG.warn("{}Parsing of CSS content exceeded max. response time of {}{}{}", Strings.getLineSeparator(), Integer.valueOf(timeout), toLowerCase(timeUnit.name()), Strings.getLineSeparator());
            cssBuilder.setLength(0);
            f.cancel(true);
            return false;
        }
    }

    /**
     * Performs the actual CSS sanitizing
     *
     * @param cssBld The {@link Stringer} helper instance providing the CSS content to check and used to hold the sanitized CSS content as well once this routine terminated
     * @param styleMap The style map providing allowed elements/values
     * @param cssPrefix The optional CSS prefix
     * @param removeIfAbsent Whether to remove non-whitelisted CSS content or not
     * @return <code>true</code> if any modification has been performed; otherwise <code>false</code>
     */
    public static boolean doCheckCss(final Stringer cssBld, final Map<String, Set<String>> styleMap, final String cssPrefix, final boolean removeIfAbsent) {
        boolean modified = false;
        /*
         * Feed matcher with buffer's content and reset
         */
        if (cssBld.indexOf("{") < 0) {
            return checkCSSElements(cssBld, styleMap, removeIfAbsent);
        }
        final String css = dropComments(WS.matcher(CRLF.matcher(cssBld).replaceAll(" ")).replaceAll(" "));
        final int length = css.length();
        cssBld.setLength(0);
        final Stringer cssElemsBuffer = new StringBuilderStringer(new StringBuilder(length));
        final Thread thread = Thread.currentThread();

        // Block-wise sanitizing
        int off = 0;
        Matcher m;
        while ((off < length) && (continueParsing(off, css)) && (!thread.isInterrupted()) && (m = PATTERN_STYLE_STARTING_BLOCK.matcher(InterruptibleCharSequence.valueOf(css.substring(off)))).find()) {
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
            final String prefix;
            if (cssElemsBuffer.isEmpty()) {
                prefix = cssElemsBuffer.toString();
            } else {
                modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
                prefix = cssElemsBuffer.toString();
            }
            cssElemsBuffer.setLength(0);
            // Check block part
            if (end < length && end < index) {
                cssElemsBuffer.append(css.substring(end, index - 1));
            }
            modified |= checkCSS(cssElemsBuffer, styleMap, cssPrefix, removeIfAbsent, true);
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

        if (false == cssElemsBuffer.isEmpty()) {
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            final String tail = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            cssBld.append(tail);
        }
        return modified;
    }

    private static final Pattern SPLIT_LINES = Pattern.compile("\r?\n");
    private static final Pattern SPLIT_WORDS = Pattern.compile("\\s+");
    private static final Pattern SPLIT_COMMA = Pattern.compile(",");

    static String prefixBlock(final String match, final String cssPrefix) {
        if (isEmpty(match)) {
            return match;
        }
        if (isEmpty(cssPrefix)) {
            return new StringBuilder(match).append('{').toString();
        }
        final int length = match.length();
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
            if (!"body".equalsIgnoreCase(selector)) {
                builder.append(replaceDotsAndHashes(selector, cssPrefix, helper));
            }
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
        try {
            final int cssLength = css.length();
            final Stringer cssElemsBuffer = new StringBuilderStringer(new StringBuilder(cssLength));
            final Matcher m = PATTERN_STYLE_STARTING_BLOCK.matcher(InterruptibleCharSequence.valueOf(css));
            if (!m.find()) {
                return false;
            }
            final Thread thread = Thread.currentThread();
            cssBuilder.setLength(0);
            int lastPos = 0;
            do {
                // Check prefix part
                cssElemsBuffer.append(css.substring(lastPos, m.start()));
                modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
                final String prefix = cssElemsBuffer.toString();
                cssElemsBuffer.setLength(0);
                // Check block part
                {
                    int i = m.end();
                    for (char c; i < cssLength && (c = css.charAt(i++)) != '}';) {
                        cssElemsBuffer.append(c);
                    }
                    lastPos = i + 1;
                }
                modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
                cssElemsBuffer.insert(0, m.group()).append('}').append('\n'); // Surround with block definition
                // Add to main builder
                cssBuilder.append(prefix);
                cssBuilder.append(cssElemsBuffer);
                cssElemsBuffer.setLength(0);
            } while (!thread.isInterrupted() && (lastPos < cssLength) && m.find(lastPos));
            if (lastPos < cssLength) {
                cssElemsBuffer.append(css.substring(lastPos, cssLength));
            }
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            final String tail = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            cssBuilder.append(tail);
            return modified;
        } catch (final RuntimeException unchecked) {
            LOG.debug("Unchecked exception while processing CSS content:{}{}", System.getProperty("line.separator"), css, unchecked);
            throw unchecked;
        }
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

            // Feed matcher with buffer's content and reset
            if (cssBuilder.indexOf("{") < 0) {
                return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
            }
            final String css = CRLF.matcher(cssBuilder.toString()).replaceAll(" ");
            final Matcher m = PATTERN_STYLE_STARTING_BLOCK.matcher(InterruptibleCharSequence.valueOf(css));
            final MatcherReplacer mr = new MatcherReplacer(m, css);
            final Thread thread = Thread.currentThread();

            // Check for CSS blocks
            if (!thread.isInterrupted() && m.find()) {
                cssBuilder.setLength(0);
                int lastPos = 0;
                do {
                    {
                        int i = m.end();
                        for (char c; (c = css.charAt(i++)) != '}';) {
                            cssElemsBuffer.append(c);
                        }
                        lastPos = i + 1;
                    }
                    modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
                    tmpBuilder.setLength(0);
                    mr.appendLiteralReplacement(
                        cssBuilder,
                        tmpBuilder.append(m.group()).append(cssElemsBuffer.toString()).append('}').append('\n').toString());
                    cssElemsBuffer.setLength(0);
                } while (!thread.isInterrupted() && m.find());
                if (lastPos < css.length()) {
                    cssBuilder.append(css.substring(lastPos));
                }
                return modified;
            }
        }
        return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
    }

    private static final Pattern PATTERN_STYLE_LINE = Pattern.compile("([\\p{Alnum}-_]+)\\s*:\\s*([\\p{Print}\\p{L}&&[^;]]+);?", Pattern.CASE_INSENSITIVE);

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
            rgb = PATTERN_COLOR_RGB.matcher(InterruptibleCharSequence.valueOf(s));
            mr = new MatcherReplacer(rgb, s);
        }
        cssBuilder.setLength(0);
        final Thread thread = Thread.currentThread();
        while (!thread.isInterrupted() && rgb.find()) {
            mr.appendLiteralReplacement(cssBuilder, rgb.group().replaceAll("\\s+", ""));
        }
        mr.appendTail(cssBuilder);
    }

    private static final Pattern PATTERN_POSITION_FIXED = Pattern.compile("position\\s*:\\s*fixed");

    /**
     * Fix for 36024: Replaces all occurrences of position:fixed by display:fixed to prevent defacing the appsuite ui
     */
    private static boolean replacePositionFixedWithDisplayBlock(final Stringer cssBuilder) {
        // search for position tag
        if (cssBuilder.indexOf("position") < 0) {
            return false;
        }

        Matcher m = PATTERN_POSITION_FIXED.matcher(InterruptibleCharSequence.valueOf(cssBuilder.toString()));
        StringBuffer sb = new StringBuffer(cssBuilder.length());
        Thread thread = Thread.currentThread();
        boolean modified = false;
        while (!thread.isInterrupted() && m.find()) {
            m.appendReplacement(sb, "display: block");
            modified = true;
        }
        m.appendTail(sb);
        cssBuilder.setLength(0);
        cssBuilder.append(sb);
        return modified;
    }

    private static final Pattern PATTERN_INLINE_DATA = Pattern.compile("url\\(data:[^,]+,.+?\\)");

    private static boolean dropInlineData(final Stringer cssBuilder) {
        // url(data:font/woff;charset=utf-8;base64,
        if (cssBuilder.indexOf("data") < 0) {
            return false;
        }

        Matcher m = PATTERN_INLINE_DATA.matcher(InterruptibleCharSequence.valueOf(cssBuilder.toString()));
        StringBuffer sb = new StringBuffer(cssBuilder.length());
        Thread thread = Thread.currentThread();
        boolean modified = false;
        while (!thread.isInterrupted() && m.find()) {
            m.appendReplacement(sb, "");
            modified = true;
        }
        m.appendTail(sb);
        cssBuilder.setLength(0);
        cssBuilder.append(sb);
        return modified;
    }

    private static final Pattern PATTERN_HTML_ENTITIES = Pattern.compile("&([^;\\W]+);");

    /**
     * Replaces possible HTML entities with their Unicode representation.<br>
     * "<i>color:&amp;nbsp;#0000ff;</i>"&nbsp;-&gt;&nbsp; "<i>color:&nbsp;#0000ff;</i>"
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @return The replaced CSS content
     */
    public static String replaceHtmlEntities(final String css) {
        if (null == css || css.indexOf('&') < 0) {
            return css;
        }
        final Matcher m = PATTERN_HTML_ENTITIES.matcher(InterruptibleCharSequence.valueOf(css));
        if (!m.find()) {
            return css;
        }
        final StringBuffer sb = new StringBuffer(css.length());
        final Map<String, Character> entityToCharacterMap = HtmlEntityUtility.getEntityToCharacterMap();
        do {
            final Character character = entityToCharacterMap.get(Strings.toLowerCase(m.group(1)));
            if (null != character) {
                m.appendReplacement(sb, Matcher.quoteReplacement(character.toString()));
            }
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Replaces possible HTML entities with their Unicode representation.<br>
     * "<i>color:&amp;nbsp;#0000ff;</i>"&nbsp;-&gt;&nbsp; "<i>color:&nbsp;#0000ff;</i>"
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     */
    public static void replaceHtmlEntities(final Stringer cssBuilder) {
        if (cssBuilder.indexOf("&") < 0) {
            return;
        }
        final String repl = replaceHtmlEntities(cssBuilder.toString());
        cssBuilder.setLength(0);
        cssBuilder.append(repl);
    }

    /**
     * Iterates over CSS elements contained in specified string argument and checks each element and its value against given style map<br>
     * <br>
     * E. g. from <code>h1, h2, h3, h4, h5, h6 { color: black !important; line-height: 100% !important; }</code><br>
     * only <code><b>color: black !important; line-height: 100% !important;</b></code><br>
     * should be provided within the cssBuilder parameter.
     *
     * @param cssBuilder A {@link StringBuilder} containing the CSS content
     * @param styleMap The style map
     * @param removeIfAbsent <code>true</code> to completely remove CSS element if not contained in specified style map; otherwise
     *            <code>false</code>
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSSElements(final Stringer cssBuilder, final Map<String, Set<String>> styleMap, final boolean removeIfAbsent) {
        if ((null == styleMap) || (cssBuilder == null) || cssBuilder.isEmpty()) {
            return false;
        }
        boolean modified = false;
        correctRGBFunc(cssBuilder);
        // replaceHtmlEntities(cssBuilder);
        modified = dropInlineData(cssBuilder);
        modified |= replacePositionFixedWithDisplayBlock(cssBuilder);

        /*
         * Feed matcher with buffer's content and reset
         */
        final Matcher m;
        final MatcherReplacer mr;
        {
            final String str = cssBuilder.toString();
            m = PATTERN_STYLE_LINE.matcher(InterruptibleCharSequence.valueOf(str));
            mr = new MatcherReplacer(m, str);
        }
        cssBuilder.setLength(0);
        final StringBuilder elemBuilder = new StringBuilder(128);
        final Thread thread = Thread.currentThread();
        while (!thread.isInterrupted() && m.find()) {
            final String elementName = m.group(1);
            if (null != elementName) {
                Set<String> allowedValuesSet = styleMap.get(toLowerCase(elementName));
                if (null != allowedValuesSet) {
                    elemBuilder.append(elementName).append(':').append(' ');
                    final String elementValues = m.group(2);
                    boolean hasValues = false;
                    if (matches(elementValues, allowedValuesSet)) {
                        /*
                         * Direct match
                         */
                        elemBuilder.append(elementValues);
                        hasValues = true;
                    } else {
                        boolean first = true;
                        for (String token : splitToTokens(elementValues)) {
                            if (matches(token, allowedValuesSet)) {
                                if (first) {
                                    first = false;
                                } else {
                                    elemBuilder.append(' ');
                                }
                                elemBuilder.append(token);
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

    private static List<String> splitToTokens(final String elementValues) {
        List<String> l = new LinkedList<String>();
        int length = elementValues.length();
        StringBuilder token = new StringBuilder(16);
        boolean open = false;
        for (int i = 0; i < length; i++) {
            char c = elementValues.charAt(i);
            switch (c) {
            case '\r': // fall-through
            case '\f': // fall-through
            case '\n': // fall-through
            case '\t': // fall-through
            case ' ':
            {
                if (open) {
                    token.append(c);
                } else {
                    if (token.length() > 0) {
                        l.add(token.toString());
                        token.setLength(0);
                    }
                }
            }
            break;
            case '(':
                token.append(c);
                open = true;
                break;
            case ')':
                token.append(c);
                open = false;
                break;
            default:
                token.append(c);
                break;
            }
        }
        if (token.length() > 0) {
            l.add(token.toString());
            token.setLength(0);
        }
        return l;
    }

    /**
     * Checks if specified string argument contains at least one CSS element
     *
     * @param css The CSS string
     * @return <code>true</code> if specified string argument contains at least one CSS element; otherwise <code>false</code>
     */
    public static boolean containsCSSElement(final String css) {
        if (null == css || Strings.isEmpty(css)) {
            return false;
        }
        return PATTERN_STYLE_LINE.matcher(css).find();
    }

}
