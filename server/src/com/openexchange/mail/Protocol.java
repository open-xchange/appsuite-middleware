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

package com.openexchange.mail;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.groupware.Component;

/**
 * {@link Protocol} - Represents both a mail and transport protocol
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class Protocol implements Component {

	private static final Pattern PAT_PROT = Pattern.compile("([a-z]+)(?:((?:_[a-z]+)*))?");

	/**
	 * Parses specified protocol string whose syntax match pattern:<br>
	 * <i>([a-z]+)(?:((?:_[a-z]+)*))?</i><br>
	 * E.g.: <code>http</code> or <code>http_https</code>.
	 * 
	 * @param protocol
	 *            The protocol string to parse
	 * @return Corresponding instance of {@link Protocol}
	 * @throws MailException
	 *             If parsing the specified protocol string fails
	 */
	public static Protocol parseProtocol(final String protocol) throws MailException {
		final Matcher m = PAT_PROT.matcher(protocol);
		if (!m.matches()) {
			throw new MailException(MailException.Code.PROTOCOL_PARSE_ERROR, protocol);
		}
		final String[] aliases;
		{
			final String s = m.group(2);
			if (null != s) {
				final String[] sa = s.split("_");
				aliases = new String[sa.length - 1];
				System.arraycopy(sa, 1, aliases, 0, aliases.length);
			} else {
				aliases = null;
			}
		}
		return new Protocol(m.group(1), aliases);
	}

	private final String[] aliases;

	private final int hashCode;

	private final String name;

	private String abbr;

	/**
	 * Initializes a new {@link Protocol}
	 * 
	 * @param name
	 *            The protocol's name in lower case
	 * @throws IllegalArgumentException
	 *             If name is <code>null</code>
	 */
	public Protocol(final String name) {
		super();
		if (null == name) {
			throw new IllegalArgumentException("name is null");
		}
		this.name = name.toLowerCase(Locale.ENGLISH);
		aliases = null;
		hashCode = 31 * 1 + ((name == null) ? 0 : name.hashCode());
	}

	/**
	 * Initializes a new {@link Protocol}
	 * 
	 * @param name
	 *            The protocol's name in lower case
	 * @param secureName
	 *            The protocol's secure name in lower case
	 * @throws IllegalArgumentException
	 *             If name is <code>null</code>
	 */
	public Protocol(final String name, final String secureName) {
		super();
		if (null == name) {
			throw new IllegalArgumentException("name is null");
		}
		this.name = name.toLowerCase(Locale.ENGLISH);
		if (secureName == null) {
			aliases = null;
		} else {
			aliases = new String[] { secureName.toLowerCase(Locale.ENGLISH) };
		}
		hashCode = 31 * 1 + ((name == null) ? 0 : name.hashCode());
	}

	/**
	 * Initializes a new {@link Protocol}
	 * 
	 * @param name
	 *            The protocol's name in lower case
	 * @param aliases
	 *            The protocol's aliases in lower case
	 * @throws IllegalArgumentException
	 *             If name is <code>null</code>
	 */
	public Protocol(final String name, final String[] aliases) {
		super();
		if (null == name) {
			throw new IllegalArgumentException("name is null");
		}
		this.name = name.toLowerCase(Locale.ENGLISH);
		if (null == aliases) {
			this.aliases = null;
		} else {
			this.aliases = new String[aliases.length];
			System.arraycopy(aliases, 0, this.aliases, 0, aliases.length);
			for (int i = 0; i < this.aliases.length; i++) {
				this.aliases[i] = this.aliases[i].toLowerCase(Locale.ENGLISH);
			}
		}
		hashCode = 31 * 1 + ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Protocol other = (Protocol) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the aliases
	 * 
	 * @return the aliases
	 */
	public String[] getAliases() {
		if (null == aliases) {
			return null;
		}
		final String[] retval = new String[aliases.length];
		System.arraycopy(aliases, 0, retval, 0, aliases.length);
		return retval;
	}

	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Checks if given protocol name is supported by this protocol
	 * 
	 * @param protocolName
	 *            The protocol name to check
	 * @return <code>true</code> if supported; otherwise <code>false</code>
	 */
	public boolean isSupported(final String protocolName) {
		final String oName = protocolName.toLowerCase(Locale.ENGLISH);
		if (name.equals(oName)) {
			return true;
		}
		if (null != aliases) {
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].equals(oName)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(name);
		if (null != aliases) {
			for (int i = 0; i < aliases.length; i++) {
				sb.append('_').append(aliases[i]);
			}
		}
		return sb.toString();
	}

	public String getAbbreviation() {
		if (null == abbr) {
			abbr = name.toUpperCase(Locale.ENGLISH);
		}
		return abbr;
	}
}
