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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.java.Strings;

/**
 * {@link Protocol} - Represents both a mail and transport protocol
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Protocol implements Component, Serializable {

    /**
     * The all identifier.
     */
    public static final String ALL = "*";

    /**
     * All protocols supported.
     */
    public static final Protocol PROTOCOL_ALL = new Protocol(ALL) {

        private static final long serialVersionUID = 388987764125558623L;

        @Override
        public boolean isSupported(String protocolName) {
            return true;
        }

    };

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -4384010555880806765L;

    private static final Pattern PAT_PROT = Pattern.compile("([a-z0-9]+)(?:((?:_[a-z0-9]+)*))?");

    private static final Pattern SPLIT = Pattern.compile("_");

    /**
     * Parses specified protocol string whose syntax match pattern:<br>
     * <i>([a-z]+)(?:((?:_[a-z]+)*))?</i><br>
     * E.g.: <code>http</code> or <code>http_https</code>.
     *
     * @param protocol The protocol string to parse
     * @return Corresponding instance of {@link Protocol}
     * @throws OXException If parsing the specified protocol string fails
     */
    public static Protocol parseProtocol(String protocol) throws OXException {
        if (ALL.equals(protocol)) {
            return PROTOCOL_ALL;
        }
        final Matcher m = PAT_PROT.matcher(protocol);
        if (!m.matches()) {
            throw MailExceptionCode.PROTOCOL_PARSE_ERROR.create(protocol);
        }
        final String[] aliases;
        {
            final String s = m.group(2);
            if (null == s) {
                aliases = null;
            } else {
                final String[] sa = SPLIT.split(s, 0);
                aliases = new String[sa.length - 1];
                System.arraycopy(sa, 1, aliases, 0, aliases.length);
            }
        }
        return new Protocol(m.group(1), aliases);
    }

    // --------------------------------------------------------------------------------------------------------- //

    private final String[] aliases;
    private final int hashCode;
    private final String name;
    private String abbr;

    /**
     * Initializes a new {@link Protocol}
     *
     * @param name The protocol's name in lower case
     * @throws IllegalArgumentException If name is <code>null</code>
     */
    public Protocol(String name) {
        super();
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        this.name = Strings.asciiLowerCase(name);
        aliases = null;
        hashCode = 31 * 1 + (name.hashCode());
    }

    /**
     * Initializes a new {@link Protocol}
     *
     * @param name The protocol's name in lower case
     * @param secureName The protocol's secure name in lower case
     * @throws IllegalArgumentException If name is <code>null</code>
     */
    public Protocol(String name, String secureName) {
        super();
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        this.name = Strings.asciiLowerCase(name);
        aliases = secureName == null ? null : new String[] { Strings.asciiLowerCase(secureName) };
        hashCode = 31 * 1 + (name.hashCode());
    }

    /**
     * Initializes a new {@link Protocol}
     *
     * @param name The protocol's name in lower case
     * @param aliases The protocol's aliases in lower case
     * @throws IllegalArgumentException If name is <code>null</code>
     */
    public Protocol(String name, String... aliases) {
        super();
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        this.name = Strings.asciiLowerCase(name);
        if (null == aliases) {
            this.aliases = null;
        } else {
            this.aliases = new String[aliases.length];
            System.arraycopy(aliases, 0, this.aliases, 0, aliases.length);
            for (int i = 0; i < this.aliases.length; i++) {
                this.aliases[i] = Strings.asciiLowerCase(this.aliases[i]);
            }
        }
        hashCode = 31 * 1 + (name.hashCode());
    }

    /**
     * Gets the max. number of concurrent mail accesses for specified mail system host.
     *
     * @param host The mail system's host name
     * @param primary <code>true</code> if host denotes primary account; otherwise <code>false</code>
     * @return The max count or a value equal to or less than zero for no restrictions
     * @throws OXException If max-count setting could not be returned for specified host name
     */
    public int getMaxCount(final String host, final boolean primary) throws OXException {
        return -1;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Protocol)) {
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
     * @param protocolName The protocol name to check
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    public boolean isSupported(final String protocolName) {
        String oName = Strings.asciiLowerCase(protocolName);
        if (name.equals(oName)) {
            return true;
        }
        String[] aliases = this.aliases;
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
        StringBuilder sb = new StringBuilder(name);
        String[] aliases = this.aliases;
        if (null != aliases) {
            for (int i = 0; i < aliases.length; i++) {
                sb.append('_').append(aliases[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public String getAbbreviation() {
        if (null == abbr) {
            abbr = Strings.asciiLowerCase(name);
        }
        return abbr;
    }
}
