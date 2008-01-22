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

import java.util.regex.Pattern;

/**
 * {@link RFC2616Regex} - Provides a collection of regular expression patterns
 * defined in <a href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>
 * (Hypertext Transfer Protocol -- HTTP/1.1) or - as extension - defined in <a
 * href="http://www.faqs.org/rfcs/rfc2965.html">RFC 2965</a> (HTTP State
 * Management Mechanism).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RFC2616Regex {

	/**
	 * Initializes a new {@link RFC2616Regex}
	 */
	private RFC2616Regex() {
		super();
	}

	private static final String tokenRegex = "[\\p{ASCII}&&[^\\p{Cntrl}()<>@,;:\\\"/\\[\\]?={}\\p{Blank}]]*";

	/**
	 * Regular expression that satisfies a <i>token</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>:
	 * 
	 * <pre>
	 * token          = 1*&lt;any CHAR except CTLs or separators&gt;
	 * separators     = &quot;(&quot; | &quot;)&quot; | &quot;&lt;&quot; | &quot;&gt;&quot; | &quot;@&quot;
	 * 	              | &quot;,&quot; | &quot;;&quot; | &quot;:&quot; | &quot;\&quot; | &lt;&quot;&gt;
	 * 	              | &quot;/&quot; | &quot;[&quot; | &quot;]&quot; | &quot;?&quot; | &quot;=&quot;
	 * 	              | &quot;{&quot; | &quot;}&quot; | SP | HT
	 * </pre>
	 */
	public static final Pattern TOKEN = Pattern.compile(tokenRegex);

	private static final String qdtextRegex = "[\\p{ASCII}\\p{Blank}&&[^\\p{Cntrl}\"]]*";

	/**
	 * Regular expression that satisfies a <i>qdtext</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>:
	 * 
	 * <pre>
	 * qdtext	= &lt;any TEXT except &lt;&quot;&gt;&gt;
	 * TEXT		= &lt;any OCTET except CTLs, but including LWS&gt;
	 * </pre>
	 */
	public static final Pattern QDTEXT = Pattern.compile(qdtextRegex);

	private static final String quotedPairRegex = "(?:\\\\\\p{ASCII})*";

	/**
	 * Regular expression that satisfies a <i>quoted-pair</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>:
	 * 
	 * <pre>
	 * &quot;\&quot; CHAR
	 * CHAR		= &lt;any US-ASCII character (octets 0 - 127)&gt;
	 * </pre>
	 */
	public static final Pattern QUOTED_PAIR = Pattern.compile(quotedPairRegex);

	private static final String quotedStringRegex = "(?:\"(?:[\\p{ASCII}\\p{Blank}&&[^\\p{Cntrl}\"]]|(?:\\\\\\p{ASCII}))*\")";

	/**
	 * Regular expression that satisfies a <code>quoted-string</code> as per
	 * <a href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>:
	 * 
	 * <pre>
	 * quoted-string	= ( &lt;&quot;&gt; *(qdtext | quoted-pair ) &lt;&quot;&gt; )
	 * </pre>
	 */
	public static final Pattern QUOTED_STRING = Pattern.compile(quotedStringRegex);

	private static final String valueRegex = RegexUtility.OR(tokenRegex, quotedStringRegex);

	/**
	 * Regular expression that satisfies a <i>value</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2616.html">RFC 2616</a>:
	 * 
	 * <pre>
	 * value = token | quoted - string
	 * </pre>
	 */
	public static final Pattern VALUE = Pattern.compile(valueRegex);

	private static final String pathRegex = RegexUtility.concat(";\\$Path=", RegexUtility.group(valueRegex, true));

	private static final String domainRegex = RegexUtility.concat(";\\$Domain=", RegexUtility.group(valueRegex, true));

	private static final String portRegex = RegexUtility.concat(";\\$Port(=\"", RegexUtility.group(valueRegex, false),
			"\")?");

	private static final String cookieValueRegex = RegexUtility.concat(RegexUtility.group(tokenRegex, true), "=",
			RegexUtility.group(valueRegex, true), RegexUtility
					.optional(pathRegex, RegexUtility.GroupType.NON_CAPTURING), RegexUtility.optional(domainRegex,
					RegexUtility.GroupType.NON_CAPTURING), RegexUtility.optional(portRegex,
					RegexUtility.GroupType.NON_CAPTURING));

	/**
	 * Regular expression that satisfies <i>cookie-value</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2965.html">RFC 2965</a>:
	 * 
	 * <pre>
	 * cookie-value    =  NAME &quot;=&quot; VALUE [&quot;;&quot; path] [&quot;;&quot; domain] [&quot;;&quot; port]
	 * </pre>
	 * 
	 * The group count defined in this pattern is 5 with the following
	 * associations:
	 * <ol>
	 * <li>cookie name</li>
	 * <li>cookie value</li>
	 * <li>path (optional)</li>
	 * <li>domain (optional)</li>
	 * <li>port (optional)</li>
	 * </ol>
	 */
	public static final Pattern COOKIE_VALUE = Pattern.compile(cookieValueRegex);

	private static final String cookieVersionRegex = RegexUtility.concat("\\$Version=", RegexUtility.group(valueRegex,
			true));

	/**
	 * Regular expression that satisfies <i>cookie-version</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2965.html">RFC 2965</a>:
	 * 
	 * <pre>
	 * cookie-version  =  &quot;$Version&quot; &quot;=&quot; value
	 * </pre>
	 */
	public static final Pattern COOKIE_VERSION = Pattern.compile(cookieVersionRegex);

	private static final String cookiesRegex = RegexUtility.concat(RegexUtility.optional(RegexUtility.group(
			RegexUtility.concat(cookieVersionRegex, "(?:;|,)\\p{Blank}*"), false)), RegexUtility.group(
			cookieValueRegex, false), RegexUtility.zeroOrMoreTimes(RegexUtility.group(RegexUtility.concat(
			"(?:;|,)\\p{Blank}*", cookieValueRegex), false)));

	/**
	 * Regular expression that satisfies <i>cookies</i> as per <a
	 * href="http://www.faqs.org/rfcs/rfc2965.html">RFC 2965</a> except that
	 * heading cookie version is optional instead of forced.
	 * 
	 * <pre>
	 * cookie          =  [cookie-version] 1*((&quot;;&quot; | &quot;,&quot;) cookie-value)
	 * </pre>
	 * 
	 */
	public static final Pattern COOKIES = Pattern.compile(cookiesRegex);
}
