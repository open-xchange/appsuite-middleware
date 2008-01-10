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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

import com.openexchange.mail.MailException;

/**
 * {@link ContentType} - Parses value of MIME header <code>Content-Type</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ContentType implements Serializable {

	private static final class ParameterContinuation {

		private final List<String> parameterContinuations;

		private String charset;

		private String language;

		public ParameterContinuation() {
			super();
			parameterContinuations = new ArrayList<String>(2);
		}

		/**
		 * Gets the charset
		 * 
		 * @return the charset
		 */
		public String getCharset() {
			return charset;
		}

		/**
		 * Sets the charset
		 * 
		 * @param charset
		 *            the charset to set
		 */
		public void setCharset(final String charset) {
			this.charset = charset;
		}

		/**
		 * Gets the language
		 * 
		 * @return the language
		 */
		public String getLanguage() {
			return language;
		}

		/**
		 * Sets the language
		 * 
		 * @param language
		 *            the language to set
		 */
		public void setLanguage(final String language) {
			this.language = language;
		}

		/**
		 * Adds a contiguous value to this continuation parameter
		 * 
		 * @param number
		 *            The parameter's number
		 * @param contiguousValue
		 *            The contiguous value
		 * @throws IndexOutOfBoundsException
		 *             If number does not fit into parameter continuations
		 */
		public void addParameterContinuation(final int number, final String contiguousValue) {
			final int index = number - 1;
			if (index < 0) {
				throw new IndexOutOfBoundsException(String.valueOf(number));
			}
			parameterContinuations.add(index, contiguousValue);
		}

		/**
		 * Writes this parameter's contiguous value into specified instance of
		 * {@link StringBuilder}
		 * 
		 * @param sb
		 *            The instance of {@link StringBuilder} to fill
		 */
		public void writeValue(final StringBuilder sb) {
			for (int i = 0; i < parameterContinuations.size(); i++) {
				if (null != parameterContinuations.get(i)) {
					sb.append(parameterContinuations.get(i));
				}
			}
		}

	}

	private static final long serialVersionUID = -9197784872892324694L;

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ContentType.class);

	/**
	 * The regular expression that should match whole content type
	 */
	private static final Pattern PATTERN_CONTENT_TYPE = Pattern
			.compile("(?:([\\p{ASCII}&&[^/;\\s\"]]+)(?:/([\\p{ASCII}&&[^;\\s\"]]+))?)((?:(?:\\s*;\\s*|\\s+)[\\p{ASCII}&&[^=\"\\s]]+(?:=(?:(?:[^\"][\\p{ASCII}&&[^\\s,;:\\\\\"/\\[\\]?=()<>@]]*)|(?:\"\\p{ASCII}+?\")))?)*)");

	/**
	 * The regular expression to parse parameters
	 */
	private static final Pattern PATTERN_PARAMETER = Pattern
			.compile("(?:\\s*;\\s*|\\s+)([\\p{ASCII}&&[^=\"\\s]]+)(?:=((?:[^\"][\\p{ASCII}&&[^\\s,;:\\\\\"/\\[\\]?=()<>@]]*)|(?:\"\\p{ASCII}+?\")))?");

	private static final Pattern PATTERN_BASETYPE = Pattern
			.compile("([\\x00-\\x7F&&[^/;\\s]]+)(?:/([\\x00-\\x7F&&[^;\\s]]+))?");

	private static final Pattern PATTERN_SINGLE_PARAM = Pattern.compile("([^\\s]+)(\\s*[=|:]\\s*)([^\\s^;]+)");

	/**
	 * The MIME type delimiter
	 * 
	 * @value /
	 */
	private static final char DELIMITER = '/';

	private static final char SEMICOLON = ';';

	private static final int NONE = -1;

	private static final String SPLIT = "\\s*";

	private static final String DEFAULT_SUBTYPE = "OCTET-STREAM";

	private String primaryType;

	private String subType;

	private String baseType;

	private final Map<String, String> parameters;

	private Map<String, ParameterContinuation> parameterContinuations;

	/**
	 * Initializes a new {@link ContentType}
	 */
	public ContentType() {
		super();
		parameters = new HashMap<String, String>();
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
		this(contentType, true);
	}

	/**
	 * Initializes a new {@link ContentType}
	 * 
	 * @param contentTypeArg
	 *            The content type
	 * @param strict
	 *            <code>true</code> for strict parsing; otherwise
	 *            <code>false</code>
	 * @throws MailException
	 *             If content type cannot be parsed
	 */
	public ContentType(final String contentTypeArg, final boolean strict) throws MailException {
		this();
		final String contentType = removeEndingSemicolon(contentTypeArg.trim().replaceAll("\\s*=\\s*", "="));
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
				subType = m.group(2);
				if (subType == null || subType.length() == 0) {
					subType = DEFAULT_SUBTYPE;
				}
				pos = m.end();
			} else {
				throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, contentType);
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

	private void parseContentType(final String ct) throws MailException {
		final Matcher ctMatcher = PATTERN_CONTENT_TYPE.matcher(ct);
		if (!ctMatcher.matches()) {
			throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, ct);
		}
		primaryType = ctMatcher.group(1);
		subType = ctMatcher.group(2);
		if (subType == null || subType.length() == 0) {
			subType = DEFAULT_SUBTYPE;
		}
		baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
		parseParameters(ctMatcher.group(3));
		mergeContinuationParameters();
	}

	private void parseParameters(final String params) {
		final Matcher paramMatcher = PATTERN_PARAMETER.matcher(params);
		NextParam: while (paramMatcher.find()) {
			String value = paramMatcher.group(2);
			if (value == null || value.length() == 0) {
				continue NextParam;
			}
			String name = paramMatcher.group(1).toLowerCase(Locale.ENGLISH);
			String charset = null;
			String language = null;
			boolean addCL = false;
			/*
			 * Check for decoding
			 */
			if (name.charAt(name.length() - 1) == '*') {
				/*
				 * An asterisk at the end of a parameter name acts as an
				 * indicator that character set and language information may
				 * appear at the beginning of the parameter value.
				 */
				name = name.substring(0, name.length() - 1);
				if (value.indexOf('\'') != -1) {
					int nextQuote = value.indexOf('\'', 1);
					charset = value.substring(value.charAt(0) == '\'' ? 1 : 0, nextQuote);
					language = value.substring(nextQuote + 1, (nextQuote = value.indexOf('\'', nextQuote + 1)));
					addCL = true;
					value = value.substring(nextQuote + 1);
				} else {
					final ParameterContinuation parameterContinuation = getParamaterContinuation(name);
					if (null != parameterContinuation) {
						charset = parameterContinuation.getCharset();
						language = parameterContinuation.getLanguage();
					}
				}
				if (null != charset) {
					value = rfc2231Decode(value, charset);
				}
			}
			final int pos = name.indexOf('*');
			if (pos != -1) {
				/*
				 * Parameter continuation mechanism:
				 * title*0*=us-ascii'en'This%20is%20even%20more%20
				 * title*1*=%2A%2A%2Afun%2A%2A%2A%20
				 */
				int number = -1;
				try {
					number = Integer.parseInt(name.substring(pos + 1));
				} catch (final NumberFormatException e) {
					LOG.error("Invalid contiguous parameter", e);
				}
				if (number != -1) {
					if (addCL) {
						addParamaterContinuation(name, number, value, charset, language);
					} else {
						addParamaterContinuation(name, number, value);
					}
				}
			} else {
				/*
				 * No continuation, add value immediately
				 */
				parameters.put(name, value);
			}
		}
	}

	private void mergeContinuationParameters() {
		if (null == parameterContinuations) {
			return;
		}
		final Iterator<Map.Entry<String, ParameterContinuation>> iter = parameterContinuations.entrySet().iterator();
		final int size = parameterContinuations.size();
		final StringBuilder sb = new StringBuilder(64);
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, ParameterContinuation> e = iter.next();
			sb.setLength(0);
			e.getValue().writeValue(sb);
			parameters.put(e.getKey(), sb.toString());
		}
	}

	private void parseBaseType(final String baseType) throws MailException {
		final Matcher baseTypeMatcher = PATTERN_BASETYPE.matcher(baseType);
		if (!baseTypeMatcher.matches()) {
			throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, baseType);
		}
		primaryType = baseTypeMatcher.group(1);
		subType = baseTypeMatcher.group(2);
		if (subType == null || subType.length() == 0) {
			subType = DEFAULT_SUBTYPE;
		}
		this.baseType = new StringBuilder(16).append(primaryType).append(DELIMITER).append(subType).toString();
	}

	/**
	 * Applies given content type to this content type
	 * 
	 * @param contentType
	 *            The content type to apply
	 * @throws OXException
	 */
	public void setContentType(final ContentType contentType) throws MailException {
		setBaseType(contentType.getBaseType());
		this.parameters.putAll(contentType.parameters);
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
	 * Adds given key-value-pair to content-type's parameter list. Any existing
	 * parameters are overwritten.
	 */
	public void addParameter(final String key, final String value) {
		if (containsSpecial(key)) {
			final MailException me = new MailException(MailException.Code.INVALID_PARAMETER, key);
			LOG.error(me.getLocalizedMessage(), me);
			return;
		}
		parameters.put(key.toLowerCase(Locale.ENGLISH), prepareParamSet(value));
	}

	/**
	 * Sets charset parameter
	 */
	public void setCharsetParameter(final String charset) {
		parameters.put(PARAM_CHARSET, prepareParamSet(charset));
	}

	/**
	 * Sets given parameter
	 */
	public void setParameter(final String key, final String value) {
		addParameter(key, value);
	}

	/**
	 * Special characters (binary sorted) that must be in quoted-string to be
	 * used within parameter values
	 */
	private static final char[] SPECIALS = { '\t', '\n', '\r', ' ', '"', '(', ')', ',', '/', ':', ';', '<', '=', '>',
			'?', '@', '[', '\\', ']' };

	private static boolean containsSpecial(final String str) {
		final char[] chars = str.toCharArray();
		boolean quote = false;
		for (int i = 0; i < chars.length && !quote; i++) {
			quote |= (Arrays.binarySearch(SPECIALS, chars[i]) >= 0);
		}
		return quote;
	}

	private static final String ENC_QUOTED_PRINTABLE = "Q";

	private static final String CHARSET_UTF8 = "UTF-8";

	private static String prepareParamSet(final String paramArg) {
		if (paramArg == null) {
			return paramArg;
		}
		try {
			final String param = MimeUtility.encodeText(paramArg, CHARSET_UTF8, ENC_QUOTED_PRINTABLE);
			if (containsSpecial(param)) {
				return new StringBuilder(param.length() + 2).append('"').append(param).append('"').toString();
			}
			return param;
		} catch (final UnsupportedEncodingException e) {
			return paramArg;
		}
	}

	private static final String PARAM_CHARSET = "charset";

	/**
	 * @return the charset value or <code>null</code> if not present
	 */
	public String getCharsetParameter() {
		return prepareParamGet(parameters.get(PARAM_CHARSET));
	}

	/**
	 * @return the value associated with given key or <code>null</code> if not
	 *         present
	 */
	public String getParameter(final String key) {
		return prepareParamGet(parameters.get(key.toLowerCase(Locale.ENGLISH)));
	}

	/**
	 * Removes & returns the value associated with given key or
	 * <code>null</code> if not present
	 * 
	 * @return the value associated with given key or <code>null</code> if not
	 *         present
	 */
	public String removeParameter(final String key) {
		return prepareParamGet(parameters.remove(key.toLowerCase(Locale.ENGLISH)));
	}

	private static String prepareParamGet(final String paramArg) {
		if (paramArg == null) {
			return paramArg;
		}
		try {
			final String param = MimeUtility.decodeText(paramArg);
			final int mlen = param.length() - 1;
			if (param.charAt(0) == '"' && param.charAt(mlen) == '"') {
				return param.substring(1, mlen);
			}
			return param;
		} catch (final UnsupportedEncodingException e) {
			return paramArg;
		}
	}

	/**
	 * @return <code>true</code> if charset parameter is present,
	 *         <code>false</code> otherwise
	 */
	public boolean containsCharsetParameter() {
		return parameters.containsKey(PARAM_CHARSET);
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
	public void setContentType(final String contentType) throws MailException {
		parseContentType(removeEndingSemicolon(contentType.trim().replaceAll("\\s*=\\s*", "=")));
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
		final Matcher m = PATTERN_BASETYPE.matcher(mimeType);
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

	private ParameterContinuation getParamaterContinuation(final String nameArg) {
		if (parameterContinuations == null) {
			return null;
		}
		final int pos = nameArg.indexOf('*');
		return parameterContinuations.get(pos == -1 ? nameArg : nameArg.substring(0, pos));
	}

	private void addParamaterContinuation(final String name, final int number, final String contiguousValue) {
		addParamaterContinuation(name, number, contiguousValue, null, null);
	}

	private void addParamaterContinuation(final String nameArg, final int number, final String contiguousValue,
			final String charset, final String language) {
		if (parameterContinuations == null) {
			parameterContinuations = new HashMap<String, ParameterContinuation>();
		}
		final String name;
		{
			final int pos = nameArg.indexOf('*');
			name = pos == -1 ? nameArg : nameArg.substring(0, pos);
		}
		final ParameterContinuation pc;
		if (parameterContinuations.containsKey(name)) {
			pc = parameterContinuations.get(name);
		} else {
			pc = new ParameterContinuation();
			parameterContinuations.put(name, pc);
		}
		if (null != charset && charset.length() > 0) {
			pc.setCharset(charset);
		}
		if (null != language && language.length() > 0) {
			pc.setLanguage(language);
		}
		pc.addParameterContinuation(number, contiguousValue);
	}

	/**
	 * Removes ending '<code>;</code>' character if present
	 * 
	 * @param contentTypeArg
	 *            The content type string argument
	 * @return The content type string w/o ending '<code>;</code>' character
	 */
	private static String removeEndingSemicolon(final String contentTypeArg) {
		String contentType = contentTypeArg;
		final int lastPos = contentType.length() - 1;
		if (contentType.charAt(lastPos) == ';') {
			contentType = contentType.substring(0, lastPos);
		}
		return contentType;
	}

	/**
	 * Decodes specified encoded value according to mechanism provided through
	 * RFC2231.
	 * 
	 * @param encoded
	 *            The encoded value
	 * @param charset
	 *            The charset name
	 * @return The decoded value
	 */
	private static String rfc2231Decode(final String encoded, final String charset) {
		if (!Charset.isSupported(charset)) {
			LOG.error("Unsupported charset: " + charset, new Throwable());
			return encoded;
		}
		final Charset cs = Charset.forName(charset);
		final char[] chars = encoded.toCharArray();
		final ByteBuffer bb = ByteBuffer.allocate(chars.length);
		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];
			if (c == '%' && isHexDigit(chars[i + 1]) && isHexDigit(chars[i + 2])) {
				final byte b = (byte) ((Character.digit(chars[i + 1], 16) << 4) | (Character.digit(chars[i + 2], 16)));
				bb.put(b);
				i += 2;
			} else {
				bb.put((byte) c);
			}
		}
		bb.flip();
		return cs.decode(bb).toString();
	}

	/**
	 * Checks if given character represents a hexadecimal digit.
	 * 
	 * @param charArg
	 *            The character
	 * @return <code>true</code> if given character represents a hexadecimal
	 *         digit, otherwise <code>false</code>
	 */
	private static boolean isHexDigit(final char charArg) {
		final char c = Character.toLowerCase(charArg);
		return (c >= '0' && c <= '9') || (c >= 'a' || c <= 'f');
	}

}
