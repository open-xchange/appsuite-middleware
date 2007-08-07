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

package com.openexchange.tools.mail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.api2.OXException;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;

/**
 * ContentType
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ContentType implements Serializable {

	private static final long serialVersionUID = -9197784872892324694L;

	private static final Pattern PATTERN_CONTENT_TYPE = Pattern
			.compile("([^\\s]+)(/)([^\\s]+?\\s*)((?:(?:;\\s*|\\s+)\\S+=(?:(?:[^;]*)|(?:\"\\S+?\")))*)",
					Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_PARAMETER = Pattern.compile(
			"(;\\s*|\\s+)(\\S+)(=)((?:(?:[^;]*)|(?:\"\\S+?\")))", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_BASETYPE = Pattern
			.compile("([^\\s]+)(/)([^\\s^;]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_SINGLE_PARAM = Pattern.compile("([^\\s]+)(\\s*[=|:]\\s*)([^\\s^;]+)",
			Pattern.CASE_INSENSITIVE);

	private static final char DELIMITER = '/';

	private static final char SEMICOLON = ';';

	private static final int NONE = -1;

	private static final String SPLIT = "\\s*";

	private String primaryType;

	private String subType;

	private String baseType;

	private final Map<String, String> parameters;

	public ContentType() {
		super();
		parameters = new HashMap<String, String>();
	}

	public ContentType(final String contentType) throws OXException {
		this(contentType, true);
	}

	public ContentType(final String contentType, final boolean strict) throws OXException {
		this();
		if (strict) {
			/*
			 * Expect a correct base type (e.g. text/plain) and
			 * semicolon-separated parameters (if any)
			 */
			parseContentType(contentType);
		} else {
			int pos = NONE;
			final Matcher m = PATTERN_BASETYPE.matcher(contentType);
			if (m.find()) {
				baseType = null;
				primaryType = m.group(1);
				subType = m.group(3);
				pos = m.end();
			} else {
				throw new OXMailException(MailCode.INVALID_CONTENT_TYPE, contentType);
			}
			if (pos != NONE) {
				final String paramStr = contentType.substring(pos);
				final int delim = paramStr.charAt(0) == SEMICOLON ? SEMICOLON : Character.isWhitespace(paramStr
						.charAt(0)) ? paramStr.charAt(0) : NONE;
				if (delim != NONE) {
					final String[] paramArr = paramStr.split(new StringBuilder(SPLIT).append((char) delim)
							.append(SPLIT).toString());
					NextParam: for (int i = 0; i < paramArr.length; i++) {
						final Matcher paramMatcher;
						if (paramArr[i].length() == 0) {
							continue NextParam;
						} else if ((paramMatcher = PATTERN_SINGLE_PARAM.matcher(paramArr[i])).matches()) {
							parameters.put(paramMatcher.group(1).toLowerCase(Locale.ENGLISH), paramMatcher.group(3));
						}
					}
				}
			}
		}
	}

	private void parseContentType(final String ct) throws OXException {
		final Matcher ctMatcher = PATTERN_CONTENT_TYPE.matcher(ct);
		if (!ctMatcher.matches()) {
			throw new OXMailException(MailCode.INVALID_CONTENT_TYPE, ct);
		}
		primaryType = ctMatcher.group(1);
		subType = ctMatcher.group(3);
		baseType = null;
		parseParameters(ct);
	}

	private void parseParameters(final String ct) {
		final Matcher paramMatcher = PATTERN_PARAMETER.matcher(ct);
		NextParam: while (paramMatcher.find()) {
			final String value = paramMatcher.group(4);
			if (value.length() == 0 || "\"\"".equals(value)) {
				continue NextParam;
			}
			parameters.put(paramMatcher.group(2).toLowerCase(Locale.ENGLISH), value);
		}
	}

	private void parseBaseType(final String baseType) throws OXException {
		final Matcher baseTypeMatcher = PATTERN_BASETYPE.matcher(baseType);
		if (!baseTypeMatcher.matches()) {
			throw new OXMailException(MailCode.INVALID_CONTENT_TYPE, baseType);
		}
		primaryType = baseTypeMatcher.group(1);
		subType = baseTypeMatcher.group(3);
		this.baseType = null;
	}

	/**
	 * @return primary type
	 */
	public String getPrimaryType() {
		return primaryType;
	}

	/**
	 * Sets primary type
	 */
	public void setPrimaryType(final String primaryType) {
		this.primaryType = primaryType;
		this.baseType = null;
	}

	/**
	 * @return sub-type
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * Sets sub-type
	 */
	public void setSubType(final String subType) {
		this.subType = subType;
		this.baseType = null;
	}

	/**
	 * @return base type (e.g. text/plain)
	 */
	public String getBaseType() {
		if (baseType != null) {
			return baseType;
		}
		return (baseType = new StringBuilder().append(primaryType).append(DELIMITER).append(subType).toString());
	}

	/**
	 * Sets base type (e.g. text/plain)
	 */
	public void setBaseType(final String baseType) throws OXException {
		parseBaseType(baseType);
	}

	/**
	 * Adds given key-value-pair to content-type's parameter list. Any existing
	 * parameters are overwritten.
	 */
	public void addParameter(final String key, final String value) {
		parameters.put(key.toLowerCase(Locale.ENGLISH), value);
	}

	/**
	 * Sets given parameter
	 */
	public void setParameter(final String key, final String value) {
		addParameter(key, value);
	}

	/**
	 * @return the value associated with given key or <code>null</code> if not
	 *         present
	 */
	public String getParameter(final String key) {
		return parameters.get(key.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Removes & returns the value associated with given key or
	 * <code>null</code> if not present
	 * 
	 * @return the value associated with given key or <code>null</code> if not
	 *         present
	 */
	public String removeParameter(final String key) {
		return parameters.remove(key.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * @return <code>true</code> if parameter is present, <code>false</code>
	 *         otherwise
	 */
	public boolean containsParameter(final String key) {
		return parameters.containsKey(key.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * @return an <code>java.util.Iterator</code> of available parameter names
	 */
	public Iterator<String> getParameterNames() {
		return parameters.keySet().iterator();
	}

	/**
	 * Sets Content-Type
	 */
	public void setContentType(final String contentType) throws OXException {
		parseContentType(contentType);
	}

	/**
	 * Checks if Content-Type's base type matches given wilcard pattern (e.g
	 * text/plain, text/* or text/htm*)
	 * 
	 * @return <code>true</code> if Content-Type's base type matches given
	 *         pattern, <code>false</code> otherwise
	 */
	public boolean isMimeType(final String pattern) {
		final Pattern p = Pattern.compile(pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".?"),
				Pattern.CASE_INSENSITIVE);
		return p.matcher(getBaseType()).matches();
	}

	/**
	 * Checks if given MIME type's base type matches given wilcard pattern (e.g
	 * text/plain, text/* or text/htm*)
	 * 
	 * 
	 * @param mimeType
	 *            The MIME type
	 * @param pattern
	 *            The pattern
	 * @return <code>true</code> if pattern matches; otherwise
	 *         <code>false</code>
	 * @throws OXMailException
	 *             If an invalid MIME type is detected
	 */
	public static boolean isMimeType(final String mimeType, final String pattern) throws OXMailException {
		final Pattern p = Pattern.compile(pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".?"),
				Pattern.CASE_INSENSITIVE);
		return p.matcher(getBaseType(mimeType)).matches();
	}

	/**
	 * Detects the base type of given MIME type
	 * 
	 * @param mimeType
	 *            The MIME type
	 * @return the base type
	 * @throws OXMailException
	 *             If an invalid MIME type is detected
	 */
	public static String getBaseType(final String mimeType) throws OXMailException {
		final Matcher m = PATTERN_BASETYPE.matcher(mimeType);
		if (m.find()) {
			return new StringBuilder(32).append(m.group(1)).append('/').append(m.group(3)).toString();
		}
		throw new OXMailException(MailCode.INVALID_CONTENT_TYPE, mimeType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(primaryType).append(DELIMITER).append(subType);
		final int size = parameters.size();
		final Iterator<Map.Entry<String, String>> iter = parameters.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, String> entry = iter.next();
			sb.append("; ").append(entry.getKey()).append('=').append(entry.getValue());
		}
		return sb.toString();
	}

}
