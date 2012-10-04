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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.MatcherReplacer;
import com.openexchange.html.internal.RegexUtility;
import com.openexchange.html.internal.RegexUtility.GroupType;
import com.openexchange.html.services.ServiceRegistry;

/**
 * {@link CSSMatcher} - Provides several utility methods to check CSS content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSSMatcher {

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

    private static final Pattern PATTERN_STYLE_BLOCK;

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
        
        final String token = "[\\S&&[^{}\\*/]]+";
        
        PATTERN_STYLE_BLOCK = Pattern.compile("("+token+"(?:(?:\\s*,\\s*|\\s+)"+token+")*\\s*\\{)([^\\}]+)\\}");

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
                final char[] chars = allowedValue.toCharArray();
                Arrays.sort(chars);
                if (Arrays.binarySearch(chars, 'd') >= 0) {
                    return false;
                }
                if (Arrays.binarySearch(chars, '*') >= 0) {
                    return true;
                }
                for (int j = 0; j < chars.length; j++) {
                    if (matchesPattern(chars[j], value)) {
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

    private static volatile Integer maxCssBlockCount;
    private static int maxCssBlockCount() {
        Integer i = maxCssBlockCount;
        if (null == i) {
            synchronized (PATTERN_STYLE_BLOCK) {
                i = maxCssBlockCount;
                if (null == i) {
                    final ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                    i = Integer.valueOf(null == service ? 500 : service.getIntProperty("com.openexchange.html.maxCssBlockCount", 500));
                    maxCssBlockCount = i;
                }
            }
        }
        return i.intValue();
    }

    /**
     * Iterates over CSS contained in specified string argument and checks each found element/block against given style map
     *
     * @param cssBuilder A {@link StringBuilder} containing CSS content
     * @param styleMap The style map
     * @param cssPrefix The CSS prefix
     * @return <code>true</code> if modified; otherwise <code>false</code>
     */
    public static boolean checkCSS(final StringBuilder cssBuilder, final Map<String, Set<String>> styleMap, final String cssPrefix) {
        if (isEmpty(cssPrefix)) {
            return checkCSS(cssBuilder, styleMap, true);
        }
        boolean modified = false;
        /*
         * Feed matcher with buffer's content and reset
         */
        final String css = dropEmptyLines(cssBuilder);
        if (css.indexOf('{') < 0) {
            return checkCSSElements(cssBuilder, styleMap, true);
        }
        final StringBuilder cssElemsBuffer = new StringBuilder(css.length());
        final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
        cssBuilder.setLength(0);
        final int maxCount = maxCssBlockCount();
        int count = 0;
        int lastPos = 0;
        while ((count++ < maxCount) && m.find()) {
            // Check prefix part
            cssElemsBuffer.append(css.substring(lastPos, m.start()));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
            final String prefix = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Check block part
            cssElemsBuffer.append(m.group(2));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
            cssElemsBuffer.insert(0, prefixBlock(m.group(1), cssPrefix)).append('}'); // Surround with block definition
            final String block = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Add to main builder
            cssBuilder.append(prefix);
            cssBuilder.append(block);
            lastPos = m.end();
        }
        /*
         * Cut off remaining CSS content if maxCount exceeded
         */
        if (count < maxCount) {
            cssElemsBuffer.append(css.substring(lastPos, css.length()));
        }
        modified |= checkCSSElements(cssElemsBuffer, styleMap, true);
        final String tail = cssElemsBuffer.toString();
        cssElemsBuffer.setLength(0);
        cssBuilder.append(tail);
        return modified;
    }

    private static final Pattern SPLIT_LINES = Pattern.compile("\r?\n");
    private static final Pattern SPLIT_WORDS = Pattern.compile("\\s+");

    private static String prefixBlock(final String match, final String cssPrefix) {
        if (isEmpty(match) || isEmpty(cssPrefix)) {
            return match;
        }
        final int length = match.length();
        if (1 == length) {
            return match;
        }
        // Cut off trailing '{' character
        final String s = match.substring(0, length - 1);
        if (isEmpty(s)) {
            return match;
        }
        final int len = s.length();
        int pos = 0;
        while (pos < len && Character.isWhitespace(s.charAt(pos))) {
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
        for (final String line : SPLIT_LINES.split(s.substring(pos), 0)) {
            final String[] words = SPLIT_WORDS.split(line, 0);
            if (1 == words.length && "body".equalsIgnoreCase(words[0])) {
                // Special treatment for "body" selector
                builder.append('#').append(cssPrefix).append(' ');
            } else {
                for (final String word : words) {
                    if (isEmpty(word)) {
                        builder.append(word);
                    } else {
                        final char first = word.charAt(0);
                        if ('.' == first) {
                            // .class -> #prefix .prefix-class
                            builder.append('#').append(cssPrefix).append(' ');
                            builder.append('.').append(cssPrefix).append('-');
                            builder.append(replaceDotsAndHashes(word.substring(1), cssPrefix, helper)).append(' ');
                        } else if ('#' == first) {
                            // #id -> #prefix #prefix-id
                            builder.append('#').append(cssPrefix).append(' ');
                            builder.append('#').append(cssPrefix).append('-');
                            builder.append(replaceDotsAndHashes(word.substring(1), cssPrefix, helper)).append(' ');
                        } else {
                            // element -> #prefix element
                            builder.append('#').append(cssPrefix).append(' ');
                            builder.append(replaceDotsAndHashes(word, cssPrefix, helper)).append(' ');
                        }
                    }
                }
            }
            builder.append('\n');
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.append('{').toString();
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
    public static boolean checkCSS(final StringBuilder cssBuilder, final Map<String, Set<String>> styleMap, final boolean removeIfAbsent) {
        boolean modified = false;
        /*
         * Feed matcher with buffer's content and reset
         */
        final String css = dropEmptyLines(cssBuilder);
        if (css.indexOf('{') < 0) {
            return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
        }
        final StringBuilder cssElemsBuffer = new StringBuilder(css.length());
        final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
        cssBuilder.setLength(0);
        final int maxCount = maxCssBlockCount();
        int count = 0;
        int lastPos = 0;
        while ((count++ < maxCount) && m.find()) {
            // Check prefix part
            cssElemsBuffer.append(css.substring(lastPos, m.start()));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            final String prefix = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Check block part
            cssElemsBuffer.append(m.group(2));
            modified |= checkCSSElements(cssElemsBuffer, styleMap, removeIfAbsent);
            cssElemsBuffer.insert(0, m.group(1)).append('}'); // Surround with block definition
            final String block = cssElemsBuffer.toString();
            cssElemsBuffer.setLength(0);
            // Add to main builder
            cssBuilder.append(prefix);
            cssBuilder.append(block);
            lastPos = m.end();
        }
        /*
         * Cut off remaining CSS content if maxCount exceeded
         */
        if (count < maxCount) {
            cssElemsBuffer.append(css.substring(lastPos, css.length()));
        }
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
    public static boolean checkCSS(final StringBuilder cssBuilder, final Map<String, Set<String>> styleMap, final boolean findBlocks, final boolean removeIfAbsent) {
        if (findBlocks) {
            boolean modified = false;
            final StringBuilder cssElemsBuffer = new StringBuilder(128);
            final StringBuilder tmpBuilder = new StringBuilder(128);
            /*
             * Feed matcher with buffer's content and reset
             */
            final String css = cssBuilder.toString();
            if (css.indexOf('{') < 0) {
                return checkCSSElements(cssBuilder, styleMap, removeIfAbsent);
            }
            final Matcher m = PATTERN_STYLE_BLOCK.matcher(css);
            final MatcherReplacer mr = new MatcherReplacer(m, css);
            cssBuilder.setLength(0);
            while (m.find()) {
                modified |= checkCSSElements(cssElemsBuffer.append(m.group(2)), styleMap, removeIfAbsent);
                tmpBuilder.setLength(0);
                mr.appendLiteralReplacement(
                    cssBuilder,
                    tmpBuilder.append(m.group(1)).append(cssElemsBuffer.toString()).append('}').toString());
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
    private static void correctRGBFunc(final StringBuilder cssBuilder) {
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
    private static boolean checkCSSElements(final StringBuilder cssBuilder, final Map<String, Set<String>> styleMap, final boolean removeIfAbsent) {
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
            final String str = dropEmptyLines(cssBuilder);
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
                        final String[] tokens = elementValues.split("\\s+");
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

    private static final Pattern PATTERN_EMPY_LINE = Pattern.compile("(?m)^\\s+$");

    private static String dropEmptyLines(final CharSequence input) {
        if (null == input) {
            return null;
        }
        return PATTERN_EMPY_LINE.matcher(input).replaceAll("");
    }
}
