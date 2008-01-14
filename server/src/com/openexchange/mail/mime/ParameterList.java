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

import java.io.UnsupportedEncodingException;
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
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link ParameterList}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ParameterList implements Cloneable {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ParameterList.class);

	/**
	 * The regular expression to parse parameters
	 */
	private static final Pattern PATTERN_PARAM_LIST = Pattern
			.compile("(?:\\s*;\\s*|\\s+)([\\p{ASCII}&&[^=\"\\s;]]+)(?:=((?:[^\"][\\p{ASCII}&&[^\\s,;:\\\\\"/\\[\\]?()<>@]]*)|(?:\"\\p{ASCII}+?\")))?");

	// "(?:\\s*;\\s*|\\s+)([\\p{ASCII}&&[^=\"\\s;]]+)(?:=((?:[^\"][\\p{ASCII}&&[^\\s,;:\\\\\"/\\[\\]?=()<>@]]*)|(?:\"\\p{ASCII}+?\")))?"

	private static final String CHARSET_UTF_8 = "utf-8";

	private Map<String, Parameter> parameters;

	/**
	 * Initializes a new, empty parameter list
	 */
	public ParameterList() {
		super();
		parameters = new HashMap<String, Parameter>();
	}

	/**
	 * Initializes a new parameter list from specified parameter list's string
	 * representation
	 * 
	 * @param parameterList
	 *            The parameter list's string representation
	 */
	public ParameterList(final String parameterList) {
		this();
		parseParameterList(parameterList.trim());
	}

	@Override
	public Object clone() {
		try {
			final ParameterList clone = (ParameterList) super.clone();
			final int size = parameters.size();
			clone.parameters = new HashMap<String, Parameter>(size);
			final Iterator<Map.Entry<String, Parameter>> iter = parameters.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Map.Entry<String, Parameter> e = iter.next();
				clone.parameters.put(e.getKey(), (Parameter) e.getValue().clone());
			}
			return clone;
		} catch (final CloneNotSupportedException e) {
			/*
			 * Cannot occur since it's cloneable
			 */
			throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
		}

	}

	private void parseParameterList(final String parameterList) {
		final Matcher m = PATTERN_PARAM_LIST.matcher(parameterList);
		while (m.find()) {
			parseParameter(m.group(1).toLowerCase(Locale.ENGLISH), m.group(2));
		}
	}

	private void parseParameter(final String name, final String value) {
		String val;
		if (value == null) {
			val = "";
		} else {
			val = value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"' ? value.substring(1,
					value.length() - 1) : value;
		}
		int pos = name.indexOf('*');
		if (pos == -1) {
			parameters.put(name, new Parameter(name, MessageUtility.decodeMultiEncodedHeader(val)));
		} else {
			Parameter p = null;
			final String soleName = name.substring(0, pos);
			String procName = name;
			/*
			 * Check if parameter is marked as encoded: name*
			 */
			if (procName.charAt(procName.length() - 1) == '*') { // encoded
				procName = procName.substring(0, procName.length() - 1);
				pos = procName.indexOf('*');
				p = parameters.get(soleName);
				if (null == p) {
					/*
					 * Expect utf-8'EN'%C2%A4%20txt
					 */
					final String[] encInfos = RFC2231Tools.parseRFC2231Value(val);
					if (null == encInfos) {
						return;
					}
					val = RFC2231Tools.rfc2231Decode(encInfos[2], encInfos[0]);
					p = new Parameter(soleName);
					p.charset = encInfos[0];
					p.language = encInfos[1];
					parameters.put(soleName, p);
				} else {
					/*
					 * Expect %C2%A4%20txt
					 */
					val = RFC2231Tools.rfc2231Decode(val, p.charset);
				}
			} else { // non-encoded
				p = parameters.get(soleName);
				if (null == p) {
					p = new Parameter(soleName);
					parameters.put(soleName, p);
				}
			}
			/*
			 * Check for parameter continuation: name*1
			 */
			if (pos != -1) {
				int num = -1;
				try {
					num = Integer.parseInt(procName.substring(pos + 1));
				} catch (final NumberFormatException e) {
					num = -1;
				}
				if (num != -1) {
					p.setContiguousValue(num, val);
				}
			} else {
				p.addContiguousValue(val);
			}
		}
	}

	/**
	 * Sets the given parameter. Existing value is overwritten.
	 * 
	 * @param name
	 *            The sole parameter name
	 * @param value
	 *            The parameter value
	 */
	public void setParameter(final String name, final String value) {
		if (null == name || containsSpecial(name)) {
			final MailException me = new MailException(MailException.Code.INVALID_PARAMETER, name);
			LOG.error(me.getLocalizedMessage(), me);
			return;
		}
		parameters.put(name.toLowerCase(Locale.ENGLISH), new Parameter(name, value));
	}

	/**
	 * Adds specified value to given parameter name. If existing, the parameter
	 * is treated as a contiguous parameter according to RFC2231.
	 * 
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value to add
	 */
	public void addParameter(final String name, final String value) {
		if (null == name || containsSpecial(name)) {
			final MailException me = new MailException(MailException.Code.INVALID_PARAMETER, name);
			LOG.error(me.getLocalizedMessage(), me);
			return;
		}
		final String key = name.toLowerCase(Locale.ENGLISH);
		final Parameter p = parameters.get(key);
		if (null == p) {
			parameters.put(key, new Parameter(name, value));
			return;
		}
		if (!p.rfc2231) {
			p.rfc2231 = true;
		}
		p.addContiguousValue(value);
	}

	/**
	 * Gets specified parameter's value
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter's value or <code>null</code> if not existing
	 */
	public String getParameter(final String name) {
		if (null == name) {
			return null;
		}
		final Parameter p = parameters.get(name.toLowerCase(Locale.ENGLISH));
		if (null == p) {
			return null;
		}
		return p.getValue();
	}

	/**
	 * Removes specified parameter and returns its value
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter's value or <code>null</code> if not existing
	 */
	public String removeParameter(final String name) {
		if (null == name) {
			return null;
		}
		final Parameter p = parameters.remove(name.toLowerCase(Locale.ENGLISH));
		if (null == p) {
			return null;
		}
		return p.getValue();
	}

	/**
	 * Checks if parameter is present
	 * 
	 * @param name
	 *            the parameter name
	 * @return <code>true</code> if parameter is present; otherwise
	 *         <code>false</code>
	 */
	public boolean containsParameter(final String name) {
		if (null == name) {
			return false;
		}
		final Parameter p = parameters.get(name.toLowerCase(Locale.ENGLISH));
		if (null == p) {
			return false;
		}
		return true;
	}

	/**
	 * Gets all parameter names wrapped in an {@link Iterator}
	 * 
	 * @return All parameter names wrapped in an {@link Iterator}
	 */
	public Iterator<String> getParameterNames() {
		return parameters.keySet().iterator();
	}

	public void appendUnicodeString(final StringBuilder sb) {
		final int size = parameters.size();
		final Iterator<Parameter> iter = parameters.values().iterator();
		for (int i = 0; i < size; i++) {
			iter.next().appendUnicodeString(sb);
		}
	}

	/**
	 * Returns the unicode (mail-safe) string representation of this parameter
	 * list
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(64);
		final int size = parameters.size();
		final Iterator<Parameter> iter = parameters.values().iterator();
		for (int i = 0; i < size; i++) {
			iter.next().appendUnicodeString(sb);
		}
		return sb.toString();
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

	private static String checkQuotation(final String str) {
		if (containsSpecial(str)) {
			return new StringBuilder(2 + str.length()).append('"').append(str).append('"').toString();
		}
		return str;
	}

	/**
	 * {@link Parameter} - Inner class to represent a parameter
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class Parameter implements Cloneable {

		private boolean rfc2231;

		private final String name;

		private List<String> contiguousValues;

		private String charset;

		private String language;

		private String value;

		/**
		 * Initializes a new rfc2231 parameter
		 * 
		 * @param name
		 *            The parameter name without asterix characters
		 */
		public Parameter(final String name) {
			super();
			this.rfc2231 = true;
			this.name = name;
			this.contiguousValues = new ArrayList<String>(2);
		}

		/**
		 * Initializes a new rfc2047 parameter
		 * 
		 * @param name
		 *            The parameter name
		 * @param value
		 *            The parameter value
		 */
		public Parameter(final String name, final String value) {
			super();
			rfc2231 = false;
			this.name = name;
			contiguousValues = new ArrayList<String>(1);
			if (null != value && value.length() > 0) {
				contiguousValues.add(value);
			}
		}

		@Override
		public Object clone() {
			try {
				final Parameter clone = (Parameter) super.clone();
				final int size = contiguousValues.size();
				clone.contiguousValues = new ArrayList<String>(size);
				for (int i = 0; i < size; i++) {
					clone.contiguousValues.add(contiguousValues.get(i));
				}
				clone.value = null;
				return clone;
			} catch (final CloneNotSupportedException e) {
				LOG.error(e.getLocalizedMessage(), e);
				throw new RuntimeException("Clone failed even though 'Cloneable' interface is implemented");
			}

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

		public void addContiguousValue(final String contiguousValue) {
			if (null != value) {
				value = null;
			}
			if (null != contiguousValue && contiguousValue.length() > 0) {
				contiguousValues.add(contiguousValue);
			}
		}

		public void setContiguousValue(final int num, final String contiguousValue) {
			if (null != value) {
				value = null;
			}
			if (num < 1) {
				return;
			}
			if (null != contiguousValue && contiguousValue.length() > 0) {
				final int index = num - 1;
				while (index >= contiguousValues.size()) {
					contiguousValues.add("");
				}
				contiguousValues.set(num - 1, contiguousValue);
			}
		}

		public String getValue() {
			if (null == value) {
				final StringBuilder sb = new StringBuilder(64);
				final int size = contiguousValues.size();
				for (int i = 0; i < size; i++) {
					sb.append(contiguousValues.get(i));
				}
				value = sb.toString();
			}
			return value;
		}

		public void appendUnicodeString(final StringBuilder sb) {
			final int size = contiguousValues.size();
			if (size == 0) {
				sb.append("; ").append(name);
				return;
			}
			if (rfc2231) {
				if (size == 1) {
					sb.append("; ").append(name);
					if (RFC2231Tools.isAscii(getValue())) {
						sb.append('=').append(checkQuotation(getValue()));
					} else {
						sb.append("*=").append(
								checkQuotation(RFC2231Tools.rfc2231Encode(getValue(), CHARSET_UTF_8, null, true)));
					}
				} else {
					boolean needsEncoding = false;
					for (int i = 0; i < size && !needsEncoding; i++) {
						needsEncoding |= !RFC2231Tools.isAscii(contiguousValues.get(i));
					}
					/*
					 * Append first
					 */
					sb.append("; ").append(name).append('*').append(1);
					if (needsEncoding) {
						sb.append("*=").append(
								checkQuotation(RFC2231Tools.rfc2231Encode(contiguousValues.get(0), CHARSET_UTF_8, null,
										true, true)));
					} else {
						sb.append('=').append(checkQuotation(contiguousValues.get(0)));
					}
					/*
					 * Append remaining values
					 */
					for (int i = 1; i < size; i++) {
						sb.append("; ").append(name).append('*').append(i + 1);
						final String chunk = contiguousValues.get(i);
						if (RFC2231Tools.isAscii(chunk)) {
							sb.append('=').append(checkQuotation(chunk));
						} else {
							sb.append("*=").append(
									checkQuotation(RFC2231Tools.rfc2231Encode(chunk, CHARSET_UTF_8, null, false)));
						}
					}
				}
				return;
			}
			try {
				sb.append("; ").append(name).append('=').append(
						checkQuotation(MimeUtility.encodeText(getValue(), CHARSET_UTF_8, "Q")));
			} catch (final UnsupportedEncodingException e) {
				/*
				 * Cannot occur
				 */
				LOG.error(e.getLocalizedMessage(), e);
			}
		}

		public String toUnicodeString() {
			final int size = contiguousValues.size();
			if (size == 0) {
				return "; " + name;
			}
			if (rfc2231) {
				final StringBuilder sb = new StringBuilder(64);
				if (size == 1) {
					sb.append("; ").append(name);
					if (RFC2231Tools.isAscii(getValue())) {
						sb.append('=').append(checkQuotation(getValue()));
					} else {
						sb.append("*=").append(
								checkQuotation(RFC2231Tools.rfc2231Encode(getValue(), CHARSET_UTF_8, null, true)));
					}
				} else {
					boolean needsEncoding = false;
					for (int i = 0; i < size && !needsEncoding; i++) {
						needsEncoding |= !RFC2231Tools.isAscii(contiguousValues.get(i));
					}
					/*
					 * Append first
					 */
					sb.append("; ").append(name).append('*').append(1);
					if (needsEncoding) {
						sb.append("*=").append(
								checkQuotation(RFC2231Tools.rfc2231Encode(contiguousValues.get(0), CHARSET_UTF_8, null,
										true, true)));
					} else {
						sb.append('=').append(checkQuotation(contiguousValues.get(0)));
					}
					/*
					 * Append remaining values
					 */
					for (int i = 1; i < size; i++) {
						sb.append("; ").append(name).append('*').append(i + 1);
						final String chunk = contiguousValues.get(i);
						if (RFC2231Tools.isAscii(chunk)) {
							sb.append('=').append(checkQuotation(chunk));
						} else {
							sb.append("*=").append(
									checkQuotation(RFC2231Tools.rfc2231Encode(chunk, CHARSET_UTF_8, null, false)));
						}
					}
				}
				return sb.toString();
			}
			try {
				return new StringBuilder(64).append("; ").append(name).append('=').append(
						checkQuotation(MimeUtility.encodeText(CHARSET_UTF_8, "Q", getValue()))).toString();
			} catch (final UnsupportedEncodingException e) {
				/*
				 * Cannot occur
				 */
				LOG.error(e.getLocalizedMessage(), e);
				return null;
			}
		}
	}
}
