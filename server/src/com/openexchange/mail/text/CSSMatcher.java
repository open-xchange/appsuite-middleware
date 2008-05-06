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

import java.util.regex.Pattern;

import com.openexchange.tools.regex.RegexUtility;
import com.openexchange.tools.regex.RegexUtility.GroupType;

/**
 * {@link CSSMatcher} - Something like:
 * 
 * <pre>
 * matches(final String value, final String pattern) {
 * 	...
 * }
 * </pre>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CSSMatcher {

	/**
	 * Initializes a new {@link CSSMatcher}
	 */
	public CSSMatcher() {
		// TODO Auto-generated constructor stub
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

	private static final String STR_LENGTH_OR_PERC = RegexUtility.group(RegexUtility.OR(STR_LENGTH, STR_PERCENTAGE),
			GroupType.NON_CAPTURING);

	private static final String STR_URL = "url\\(\"?\\p{ASCII}+\"?\\)";

	private static final String STR_COLOR_KEYWORD = RegexUtility.group(
			"aqua|black|blue|fuchsia|gray|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow",
			GroupType.NON_CAPTURING);

	private static final String STR_COLOR_SYSTEM = "ActiveBorder|ActiveCaption|AppWorkspace|Background|"
			+ "ButtonFace|ButtonHighlight|ButtonShadow|ButtonText|CaptionTextGrayText|"
			+ "Highlight|HighlightText|InactiveBorder|InactiveCaption|InactiveCaptionText|"
			+ "InfoBackground|InfoText|Menu|MenuText|Scrollbar|ThreeDDarkShadow|"
			+ "ThreeDFace|ThreeDHighlight|ThreeDLightShadow|ThreeDShadow|Window|WindowFrame|WindowText";

	private static final String STR_COLOR_RGB_HEX = "#\\p{XDigit}{3,6}";

	private static final String STR_COLOR_RGB_FUNC = RegexUtility.concat("rgb\\(", RegexUtility
			.group(RegexUtility.OR(
					RegexUtility.concat(STR_INTEGER, "\\s*,\\s*", STR_INTEGER, "\\s*,\\s*", STR_INTEGER), RegexUtility
							.concat(STR_PERCENTAGE, "\\s*,\\s*", STR_PERCENTAGE, "\\s*,\\s*", STR_PERCENTAGE)),
					GroupType.NON_CAPTURING), "\\)");

	private static final String STR_COLOR = RegexUtility.group(RegexUtility.concat(STR_COLOR_KEYWORD, "|",
			STR_COLOR_SYSTEM, "|", STR_COLOR_RGB_HEX, "|", STR_COLOR_RGB_FUNC), GroupType.NON_CAPTURING);

	/*
	 * The patterns for values
	 */

	private static final Pattern N = Pattern.compile(STR_LENGTH_OR_PERC);

	private static final Pattern N_WO_PERCENTAGE = Pattern.compile(STR_LENGTH);

	private static final Pattern C = Pattern.compile(STR_COLOR, Pattern.CASE_INSENSITIVE);

	private static final Pattern U = Pattern.compile(STR_URL, Pattern.CASE_INSENSITIVE);

	public static void main(String[] args) {
		Pattern p = Pattern.compile(STR_COLOR);
		System.out.println(p.matcher("maroon").matches());
		System.out.println(p.matcher("MenuText").matches());
		System.out.println(p.matcher("#f00").matches());
		System.out.println(p.matcher("#ff0000").matches());
		System.out.println(p.matcher("rgb(255,0,0)").matches());
		System.out.println(p.matcher("rgb(110%, 0%, 0%)").matches());
	}

}
