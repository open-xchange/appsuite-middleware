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

package com.openexchange.mail.mime;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.mail.MailException;

/**
 * {@link ContentType} - Parses value of MIME header <code>Content-Type</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ContentType extends ParameterizedHeader implements Serializable {

	private static final long serialVersionUID = -9197784872892324694L;

	/**
	 * The regular expression that should match whole content type
	 */
	private static final Pattern PATTERN_CONTENT_TYPE = Pattern
			.compile("(?:([\\p{ASCII}&&[^/;\\s\"]]+)(?:/([\\p{ASCII}&&[^;\\s\"]]+))?)");

	/**
	 * The MIME type delimiter
	 * 
	 * @value /
	 */
	private static final char DELIMITER = '/';

	private static final String DEFAULT_SUBTYPE = "OCTET-STREAM";

	private static final String PARAM_CHARSET = "charset";

	private String primaryType;

	private String subType;

	private String baseType;

	/**
	 * Initializes a new {@link ContentType}
	 */
	public ContentType() {
		super();
		parameterList = new ParameterList();
	}

	/**
	 * Initializes a new {@link ContentType}
	 * 
	 * @param contentType
	 *            The content type
	 * @throws MailException
	 *             If content type cannot be parsed
	 */
	public ContentType(final String contentType) throws MailException {
		super();
		parseContentType(contentType);
	}

	private void parseContentType(final String contentType) throws MailException {
		parseContentType(contentType, true);
	}

	private void parseContentType(final String contentTypeArg, final boolean paramList) throws MailException {
		final String contentType = prepareParameterizedHeader(contentTypeArg);
		final Matcher ctMatcher = PATTERN_CONTENT_TYPE.matcher(contentType);
		if (!ctMatcher.find() || ctMatcher.start() != 0) {
			throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, contentTypeArg);
		}
		primaryType = ctMatcher.group(1);
		subType = ctMatcher.group(2);
		if (subType == null || subType.length() == 0) {
			subType = DEFAULT_SUBTYPE;
		}
		baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
		if (paramList) {
			parameterList = new ParameterList(contentType.substring(ctMatcher.end()));
		}
	}

	// /**
	// * Initializes a new {@link ContentType}
	// *
	// * @param contentTypeArg
	// * The content type
	// * @param strict
	// * <code>true</code> for strict parsing; otherwise
	// * <code>false</code>
	// * @throws MailException
	// * If content type cannot be parsed
	// */
	// public ContentType(final String contentTypeArg, final boolean strict)
	// throws MailException {
	// super();
	// final String contentType = prepareContentType(contentTypeArg);
	// if (strict) {
	// /*
	// * Expect a correct base type (e.g. text/plain) and
	// * semicolon-separated parameters (if any)
	// */
	// parseContentType(contentType);
	// } else {
	// int pos = NONE;
	// final Matcher m = PATTERN_BASETYPE.matcher(contentType);
	// if (m.find()) {
	// baseType = null;
	// primaryType = m.group(1);
	// subType = m.group(2);
	// if (subType == null || subType.length() == 0) {
	// subType = DEFAULT_SUBTYPE;
	// }
	// pos = m.end();
	// } else {
	// throw new MailException(MailException.Code.INVALID_CONTENT_TYPE,
	// contentType);
	// }
	// if (pos != NONE) {
	// final String paramStr = contentType.substring(pos);
	// final int delim = paramStr.charAt(0) == SEMICOLON ? SEMICOLON :
	// Character.isWhitespace(paramStr
	// .charAt(0)) ? paramStr.charAt(0) : NONE;
	// if (delim != NONE) {
	// final String[] paramArr = paramStr.split(new
	// StringBuilder(SPLIT).append((char) delim)
	// .append(SPLIT).toString());
	// NextParam: for (int i = 0; i < paramArr.length; i++) {
	// final Matcher paramMatcher;
	// if (paramArr[i].length() == 0) {
	// continue NextParam;
	// } else if ((paramMatcher =
	// PATTERN_SINGLE_PARAM.matcher(paramArr[i])).matches()) {
	// parameters.put(paramMatcher.group(1).toLowerCase(Locale.ENGLISH),
	// paramMatcher.group(3));
	// }
	// }
	// }
	// }
	// }
	// }

	private void parseBaseType(final String baseType) throws MailException {
		parseContentType(baseType, false);
	}

	/**
	 * Applies given content type to this content type
	 * 
	 * @param contentType
	 *            The content type to apply
	 * @throws MailException
	 */
	public void setContentType(final ContentType contentType) throws MailException {
		if (contentType == this) {
			return;
		}
		setBaseType(contentType.getBaseType());
		this.parameterList = (ParameterList) contentType.parameterList.clone();
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
		return (baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString());
	}

	/**
	 * Sets base type (e.g. text/plain)
	 */
	public void setBaseType(final String baseType) throws MailException {
		parseBaseType(baseType);
	}

	/**
	 * Sets charset parameter
	 */
	public void setCharsetParameter(final String charset) {
		setParameter(PARAM_CHARSET, charset);
	}

	/**
	 * @return the charset value or <code>null</code> if not present
	 */
	public String getCharsetParameter() {
		return getParameter(PARAM_CHARSET);
	}

	/**
	 * @return <code>true</code> if charset parameter is present,
	 *         <code>false</code> otherwise
	 */
	public boolean containsCharsetParameter() {
		return containsParameter(PARAM_CHARSET);
	}

	/**
	 * Sets Content-Type
	 */
	public void setContentType(final String contentType) throws MailException {
		parseContentType(contentType);
	}

	/**
	 * Checks if Content-Type's base type matches given wildcard pattern (e.g
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
	 * Checks if given MIME type's base type matches given wildcard pattern (e.g
	 * text/plain, text/* or text/htm*)
	 * 
	 * 
	 * @param mimeType
	 *            The MIME type
	 * @param pattern
	 *            The pattern
	 * @return <code>true</code> if pattern matches; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 *             If an invalid MIME type is detected
	 */
	public static boolean isMimeType(final String mimeType, final String pattern) throws MailException {
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
	 * @throws MailException
	 *             If an invalid MIME type is detected
	 */
	public static String getBaseType(final String mimeType) throws MailException {
		final Matcher m = PATTERN_CONTENT_TYPE.matcher(mimeType);
		if (m.find()) {
			String subType = m.group(2);
			if (subType == null || subType.length() == 0) {
				subType = DEFAULT_SUBTYPE;
			}
			return new StringBuilder(32).append(m.group(1)).append('/').append(subType).toString();
		}
		throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, mimeType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append(primaryType).append(DELIMITER).append(subType);
		parameterList.appendUnicodeString(sb);
		return sb.toString();
	}

}
