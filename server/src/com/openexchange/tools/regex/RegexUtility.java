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

package com.openexchange.tools.regex;

/**
 * {@link RegexUtility} - Provides simple helper methods to compose regular
 * expressions in an easier way.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RegexUtility {

	public static enum GroupType {

		/**
		 * X, with no grouping: <code>X</code>
		 */
		NONE(""),
		/**
		 * X, as a capturing group: <code>(X)</code>
		 */
		CAPTURING("("),
		/**
		 * X, as a non-capturing group: <code>(?:X)</code>
		 */
		NON_CAPTURING("(?:"),
		/**
		 * X, via zero-width positive lookahead: <code>(?=X)</code>
		 */
		ZERO_WIDTH_POSITIVE_LOOKAHEAD("(?="),
		/**
		 * X, via zero-width negative lookahead: <code>(?!X)</code>
		 */
		ZERO_WIDTH_NEGATIVE_LOOKAHEAD("(?!"),
		/**
		 * X, via zero-width positive lookbehind: <code>(?<=X)</code>
		 */
		ZERO_WIDTH_POSITIVE_LOOKBEHIND("(?<="),
		/**
		 * X, via zero-width negative lookbehind: <code>(?<!X)</code>
		 */
		ZERO_WIDTH_NEGATIVE_LOOKBEHIND("(?<!"),
		/**
		 * X, as an independent, non-capturing group: <code>(?>X)</code>
		 */
		INDEPENDENT_NON_CAPTURING("(?>X");

		private final String openingParenthesis;

		private GroupType(final String openingParenthesis) {
			this.openingParenthesis = openingParenthesis;
		}

		private String getOpeningParenthesis() {
			return openingParenthesis;
		}

	}

	/**
	 * Initializes a new {@link RegexUtility}
	 */
	private RegexUtility() {
		super();
	}

	/**
	 * Concatenates specified first regular expression with given regular
	 * expressions.
	 * 
	 * @param regex
	 *            The first regular expression
	 * @param regexes
	 *            The array of regular expressions to append
	 * @return The concatenated regex
	 */
	public static String concat(final String regex, final String... regexes) {
		int length = regex.length();
		for (int i = 0; i < regexes.length; i++) {
			length += regexes[i].length();
		}
		final StringBuilder sb = new StringBuilder(length).append(regex);
		for (int i = 0; i < regexes.length; i++) {
			sb.append(regexes[i]);
		}
		return sb.toString();
	}

	/**
	 * Combines specified regular expressions with OR operator <code>'|'</code>:
	 * 
	 * <pre>
	 * regex1 | regex2
	 * </pre>
	 * 
	 * @param regex1
	 *            The first regular expression
	 * @param regex2
	 *            The second regular expression
	 * @return The OR-combined regular expression
	 */
	public static String OR(final String regex1, final String regex2) {
		return new StringBuilder(regex1.length() + regex2.length() + 1).append(regex1).append('|').append(regex2)
				.toString();
	}

	/**
	 * Appends the optional operator <code>'?'</code> to specified regular
	 * expression
	 * 
	 * <pre>
	 * regex?
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @return The optional regular expression
	 */
	public static String optional(final String regex) {
		return optional(regex, GroupType.NONE);
	}

	/**
	 * Appends the optional operator <code>'?'</code> and grouping to
	 * specified regular expression
	 * 
	 * <pre>
	 * (regex)? OR (?:regex)? OR (?=regex)?...
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @param groupType
	 *            The group type
	 * @return The grouped optional regular expression
	 */
	public static String optional(final String regex, final GroupType groupType) {
		if (GroupType.NONE.equals(groupType)) {
			return new StringBuilder(regex.length() + 1).append(regex).append('?').toString();
		}
		return _optional(regex, groupType);
	}

	private static String _optional(final String regex, final GroupType groupType) {
		return new StringBuilder(regex.length() + 6).append(groupType.getOpeningParenthesis()).append(regex)
				.append(')').append('?').toString();
	}

	/**
	 * Groups given regular expression
	 * 
	 * <pre>
	 * (regex) OR (?:regex)
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @param capturing
	 *            <code>true</code> as capturing group; <code>false</code>
	 *            for non-capturing
	 * @return The grouped regular expression
	 */
	public static String group(final String regex, final boolean capturing) {
		return _group(regex, capturing ? GroupType.CAPTURING : GroupType.NON_CAPTURING);
	}

	/**
	 * Groups given regular expression as stated by specified
	 * <code>groupType</code>.
	 * 
	 * <pre>
	 * (regex) OR (?:regex) OR (?=regex)...
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @param groupType
	 *            The group type
	 * @return The grouped regular expression
	 */
	public static String group(final String regex, final GroupType groupType) {
		if (GroupType.NONE.equals(groupType)) {
			return regex;
		}
		return _group(regex, groupType);
	}

	private static String _group(final String regex, final GroupType groupType) {
		return new StringBuilder(regex.length() + 5).append(groupType.getOpeningParenthesis()).append(regex)
				.append(')').toString();
	}

	/**
	 * Appends the one-or-more-times operator <code>'+'</code> to specified
	 * regular expression
	 * 
	 * <pre>
	 * regex+
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @return The one-or-more-times regular expression
	 */
	public static String oneOrMoreTimes(final String regex) {
		return oneOrMoreTimes(regex, GroupType.NONE);
	}

	/**
	 * Appends the one-or-more-times operator <code>'+'</code> and grouping to
	 * specified regular expression
	 * 
	 * <pre>
	 * (regex)+ OR (?:regex)+ OR (?=regex)+...
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @param groupType
	 *            The group type
	 * @return The grouped one-or-more-times regular expression
	 */
	public static String oneOrMoreTimes(final String regex, final GroupType groupType) {
		if (GroupType.NONE.equals(groupType)) {
			return new StringBuilder(regex.length() + 1).append(regex).append('+').toString();
		}
		return _oneOrMoreTimes(regex, groupType);
	}

	private static String _oneOrMoreTimes(final String regex, final GroupType groupType) {
		return new StringBuilder(regex.length() + 6).append(groupType.getOpeningParenthesis()).append(regex)
				.append(')').append('+').toString();
	}

	/**
	 * Appends the zero-or-more-times operator <code>'*'</code> to specified
	 * regular expression
	 * 
	 * <pre>
	 * regex*
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @return The zero-or-more-times regular expression
	 */
	public static String zeroOrMoreTimes(final String regex) {
		return zeroOrMoreTimes(regex, GroupType.NONE);
	}

	/**
	 * Appends the zero-or-more-times operator <code>'*'</code> and grouping
	 * to specified regular expression
	 * 
	 * <pre>
	 * (regex)* OR (?:regex)* OR (?=regex)*...
	 * </pre>
	 * 
	 * @param regex
	 *            The regular expression
	 * @param groupType
	 *            The group type
	 * @return The grouped zero-or-more-times regular expression
	 */
	public static String zeroOrMoreTimes(final String regex, final GroupType groupType) {
		if (GroupType.NONE.equals(groupType)) {
			return new StringBuilder(regex.length() + 1).append(regex).append('*').toString();
		}
		return _zeroOrMoreTimes(regex, groupType);
	}

	private static String _zeroOrMoreTimes(final String regex, final GroupType groupType) {
		return new StringBuilder(regex.length() + 6).append(groupType.getOpeningParenthesis()).append(regex)
				.append(')').append('*').toString();
	}
}
