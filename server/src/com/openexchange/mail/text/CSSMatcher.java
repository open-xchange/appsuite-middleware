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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.tools.regex.RegexUtility;
import com.openexchange.tools.regex.RegexUtility.GroupType;

/**
 * {@link CSSMatcher} - Provides several utility methods to check CSS content.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CSSMatcher {

	/**
	 * Initializes a new {@link CSSMatcher}
	 */
	private CSSMatcher() {
		super();
	}

	/*
	 * Regular expression for CSS2 values
	 */

	private static final String STR_INTEGER = "(?:(?:\\+|-)?[0-9]+)";

	private static final String STR_REAL = "(?:(?:\\+|-)?[0-9]*\\.[0-9]+)";

	private static final String STR_NUMBER = RegexUtility.group(RegexUtility.OR(STR_INTEGER, STR_REAL),
			GroupType.NON_CAPTURING);

	private static final String STR_REL_UNITS = "(?:em|ex|px)";

	private static final String STR_ABS_UNITS = "(?:in|cm|mm|pt|pc)";

	private static final String STR_UNITS = RegexUtility.group(RegexUtility.OR(STR_REL_UNITS, STR_ABS_UNITS),
			GroupType.NON_CAPTURING);

	private static final String STR_LENGTH = RegexUtility.group(RegexUtility.OR(RegexUtility.concat(STR_NUMBER,
			STR_UNITS), RegexUtility.concat("(?:\\\\+|-)?", "0", RegexUtility.optional(STR_UNITS))),
			GroupType.NON_CAPTURING);

	private static final String STR_PERCENTAGE = RegexUtility.group(RegexUtility.concat(STR_NUMBER, "%"),
			GroupType.NON_CAPTURING);

	private static final String STR_LENGTH_OR_PERCENTAGE = RegexUtility.group(RegexUtility.OR(STR_LENGTH,
			STR_PERCENTAGE), GroupType.NON_CAPTURING);

	private static final String STR_TIME_UNITS = "(?:ms|s)";

	private static final String STR_TIME = RegexUtility.group(RegexUtility.concat(STR_NUMBER, STR_TIME_UNITS),
			GroupType.NON_CAPTURING);

	// private static final String STR_MULTIPLE_LENGTH =
	// RegexUtility.concat(STR_LENGTH, RegexUtility.group(RegexUtility
	// .concat("\\p{Blank}+", STR_LENGTH), GroupType.NON_CAPTURING), "*");
	//
	// private static final String STR_MULTIPLE_PERCENTAGE =
	// RegexUtility.concat(STR_PERCENTAGE, RegexUtility.group(
	// RegexUtility.concat("\\p{Blank}+", STR_PERCENTAGE),
	// GroupType.NON_CAPTURING), "*");
	//
	// private static final String STR_MULTIPLE =
	// RegexUtility.group(RegexUtility.OR(STR_MULTIPLE_LENGTH,
	// STR_MULTIPLE_PERCENTAGE), GroupType.NON_CAPTURING);

	private static final String STR_URL = "url\\(\"?\\p{ASCII}+\"?\\)";

	private static final String STR_COLOR_KEYWORD = RegexUtility.group(
			"aqua|black|blue|fuchsia|gray|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow",
			GroupType.NON_CAPTURING);

	private static final String STR_COLOR_SYSTEM = "ActiveBorder|ActiveCaption|AppWorkspace|Background|"
			+ "ButtonFace|ButtonHighlight|ButtonShadow|ButtonText|CaptionTextGrayText|"
			+ "Highlight|HighlightText|InactiveBorder|InactiveCaption|InactiveCaptionText|"
			+ "InfoBackground|InfoText|Menu|MenuText|Scrollbar|ThreeDDarkShadow|"
			+ "ThreeDFace|ThreeDHighlight|ThreeDLightShadow|ThreeDShadow|Window|WindowFrame|WindowText";

	private static final String STR_COLOR_RGB_HEX = "#?\\p{XDigit}{3,6}";

	private static final String STR_CSV_DELIM = "\\s*,\\s*";

	private static final String STR_COLOR_RGB_FUNC = RegexUtility.concat("rgb\\(", RegexUtility.group(RegexUtility.OR(
			RegexUtility.concat(STR_INTEGER, STR_CSV_DELIM, STR_INTEGER, STR_CSV_DELIM, STR_INTEGER), RegexUtility
					.concat(STR_PERCENTAGE, STR_CSV_DELIM, STR_PERCENTAGE, STR_CSV_DELIM, STR_PERCENTAGE)),
			GroupType.NON_CAPTURING), "\\)");

	private static final String STR_COLOR = RegexUtility.group(RegexUtility.concat(STR_COLOR_KEYWORD, "|",
			STR_COLOR_SYSTEM, "|", STR_COLOR_RGB_HEX, "|", STR_COLOR_RGB_FUNC), GroupType.NON_CAPTURING);

	/*
	 * The patterns for values
	 */

	private static final Pattern PAT_N = Pattern.compile(STR_LENGTH_OR_PERCENTAGE);

	private static final Pattern PAT_n = Pattern.compile(STR_LENGTH);

	private static final Pattern PAT_c = Pattern.compile(STR_COLOR, Pattern.CASE_INSENSITIVE);

	private static final Pattern PAT_u = Pattern.compile(STR_URL, Pattern.CASE_INSENSITIVE);

	private static final Pattern PAT_t = Pattern.compile(STR_TIME, Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_IS_PATTERN = Pattern.compile("[unNcd*t]+");

	/**
	 * Checks if specified CSS value is matched by given allowed values
	 * <p>
	 * The allowed values may contain following patterns to cover certain CSS
	 * types:
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
	 * @param value
	 *            The value
	 * @param allowedValuesSet
	 *            The allowed values
	 * @return <code>true</code> if value is matched by given allowed values;
	 *         otherwise <code>false</code>
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

	private static final Pattern PATTERN_STYLE_BLOCK = Pattern.compile("(\\p{Print}+\\s*\\{)([^}]+)\\}");

	private static final Pattern PATTERN_COLOR_RGB = Pattern.compile(STR_COLOR_RGB_FUNC, Pattern.CASE_INSENSITIVE);

	/**
	 * Iterates over CSS blocks contained in specified string argument and
	 * checks each block against given style map
	 * 
	 * @param cssBuffer
	 *            A {@link StringBuffer} containing CSS content
	 * @param styleMap
	 *            The style map
	 * @param findBlocks
	 *            <code>true</code> to iterate over CSS blocks; otherwise
	 *            <code>false</code> to iterate over CSS elements
	 * @param removeIfAbsent
	 *            <code>true</code> to completely remove CSS element if not
	 *            contained in specified style map; otherwise <code>false</code>
	 * @return <code>true</code> if modified; otherwise <code>false</code>
	 */
	public static boolean checkCSS(final StringBuffer cssBuffer, final Map<String, Set<String>> styleMap,
			final boolean findBlocks, final boolean removeIfAbsent) {
		if (findBlocks) {
			boolean modified = false;
			final StringBuffer cssElemsBuffer = new StringBuffer(128);
			final StringBuilder cssBuilder = new StringBuilder(128);
			/*
			 * Feed matcher with buffer's content and reset
			 */
			final Matcher m = PATTERN_STYLE_BLOCK.matcher(cssBuffer.toString());
			cssBuffer.setLength(0);
			while (m.find()) {
				modified |= checkCSSElements(cssElemsBuffer.append(m.group(2)), styleMap, removeIfAbsent);
				cssBuilder.setLength(0);
				m.appendReplacement(cssBuffer, Matcher.quoteReplacement(cssBuilder.append(m.group(1)).append(
						cssElemsBuffer.toString()).append('}').toString()));
				cssElemsBuffer.setLength(0);
			}
			m.appendTail(cssBuffer);
			return modified;
		}
		return checkCSSElements(cssBuffer, styleMap, removeIfAbsent);
	}

	private static final Pattern PATTERN_STYLE_LINE = Pattern.compile(
			"([\\p{Alnum}-_]+)\\s*:\\s*([\\p{Print}&&[^;]]+);?", Pattern.CASE_INSENSITIVE);

	/**
	 * Corrects rgb functions; e.g.<br>
	 * "<i>rgb(238,&nbsp;239,&nbsp;240)</i>"&nbsp;-&gt;&nbsp;
	 * "<i>rgb(238,239,240)</i>"
	 * 
	 * @param cssBuffer
	 *            A {@link StringBuffer} containing CSS content
	 */
	private static void correctRGBFunc(final StringBuffer cssBuffer) {
		final Matcher rgb = PATTERN_COLOR_RGB.matcher(cssBuffer.toString());
		cssBuffer.setLength(0);
		while (rgb.find()) {
			rgb.appendReplacement(cssBuffer, Matcher.quoteReplacement(rgb.group().replaceAll("\\s+", "")));
		}
		rgb.appendTail(cssBuffer);
	}

	/**
	 * Iterates over CSS elements contained in specified string argument and
	 * checks each element and its value against given style map
	 * 
	 * @param cssBuffer
	 *            A {@link StringBuffer} containing the CSS content
	 * @param styleMap
	 *            The style map
	 * @param removeIfAbsent
	 *            <code>true</code> to completely remove CSS element if not
	 *            contained in specified style map; otherwise <code>false</code>
	 * @return <code>true</code> if modified; otherwise <code>false</code>
	 */
	public static boolean checkCSSElements(final StringBuffer cssBuffer, final Map<String, Set<String>> styleMap,
			final boolean removeIfAbsent) {
		boolean modified = false;
		correctRGBFunc(cssBuffer);
		/*
		 * Feed matcher with buffer's content and reset
		 */
		final Matcher m = PATTERN_STYLE_LINE.matcher(cssBuffer.toString());
		cssBuffer.setLength(0);
		final StringBuilder elemBuilder = new StringBuilder(128);
		while (m.find()) {
			final String elementName = m.group(1);
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
					m.appendReplacement(cssBuffer, Matcher.quoteReplacement(elemBuilder.toString()));
				} else {
					/*
					 * Remove element since none of its values is allowed
					 */
					modified = true;
					m.appendReplacement(cssBuffer, "");
				}
				elemBuilder.setLength(0);
			} else if (removeIfAbsent) {
				/*
				 * Remove forbidden element
				 */
				modified = true;
				m.appendReplacement(cssBuffer, "");
			}
		}
		m.appendTail(cssBuffer);
		return modified;
	}

	/**
	 * Checks if specified string argument contains at least one CSS element
	 * 
	 * @param css
	 *            The CSS string
	 * @return <code>true</code> if specified string argument contains at least
	 *         one CSS element; otherwise <code>false</code>
	 */
	public static boolean containsCSSElement(final String css) {
		if (null == css || isEmpty(css)) {
			return false;
		}
		return PATTERN_STYLE_LINE.matcher(css).find();
	}

	private static boolean isEmpty(final String s) {
		if (s.length() == 0) {
			return true;
		}
		boolean retval = true;
		final char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length && retval; i++) {
			retval = Character.isWhitespace(chars[i]);
		}
		return retval;
	}
}
