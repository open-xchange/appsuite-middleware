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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package javax.mail.internet;

import gnu.inet.encoding.IDNAException;

/**
 * {@link IDNA} - Helper class for internationalized domain names (IDN).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDNA {

    /**
     * Initializes a new {@link IDNA}.
     */
    private IDNA() {
        super();
    }

    private final static String ACE_PREFIX = gnu.inet.encoding.IDNA.ACE_PREFIX;

    /**
     * Converts a unicode representation of an internet address to ASCII using the procedure in RFC3490 section 4.1. Unassigned characters
     * are not allowed and STD3 ASCII rules are enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@m&uuml;ller.de"</code> is converted to <code>"someone@xn--mller-kva.de"</code>
     * 
     * @param idnAddress The unicode representation of an internet address
     * @return The ASCII-encoded (punycode) of given internet address
     * @throws AddressException If ASCII representation of given internet address cannot be created
     */
    public static String toACE(final String idnAddress) throws AddressException {
        if (null == idnAddress) {
            return null;
        }
        try {
            final int pos = idnAddress.indexOf('@');
            if (pos < 0) {
                return idnAddress;
            }
            final int length = idnAddress.length();
            if (pos == length - 1) {
                return idnAddress;
            }
            return new StringBuilder(length + 8).append(idnAddress.substring(0, pos)).append('@').append(
                gnu.inet.encoding.IDNA.toASCII(idnAddress.substring(pos + 1), true)).toString();
        } catch (final gnu.inet.encoding.IDNAException e) {
            throw new AddressException(new StringBuilder(e.getMessage()).append(": ").append(idnAddress).toString());
        }
    }

    /**
     * Converts an ASCII-encoded address to its unicode representation. Unassigned characters are not allowed and STD3 hostnames are
     * enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@xn--mller-kva.de"</code> is converted to <code>"someone@m&uuml;ller.de"</code>
     * 
     * @param aceAddress The ASCII-encoded (punycode) address
     * @return The unicode representation of given internet address
     * @see #getIDNAddress()
     */
    public static String toIDN(final String aceAddress) {
        if (null == aceAddress) {
            return null;
        }
        final int pos = aceAddress.indexOf('@');
        if (pos < 0 || aceAddress.indexOf(ACE_PREFIX) < 0) {
            return aceAddress;
        }
        return new StringBuilder(aceAddress.length()).append(aceAddress.substring(0, pos)).append('@').append(
            gnu.inet.encoding.IDNA.toUnicode(aceAddress.substring(pos + 1), true)).toString();
    }

    /**
     * Converts a Unicode string to ASCII using the procedure in RFC3490 section 4.1. Unassigned characters are not allowed and STD3 ASCII
     * rules are enforced. The input string may be a domain name containing dots.
     * 
     * @param unicodeHostName The host name as Unicode string.
     * @param useIDNA2008 <code>true</code> to use PVALID code points
     * @return The encoded host name
     */
    public static String toASCII(final String unicodeHostName, final boolean useIDNA2008) {
        if (null == unicodeHostName) {
            return null;
        }
        try {
            return gnu.inet.encoding.IDNA.toASCII(unicodeHostName, useIDNA2008);
        } catch (final IDNAException e) {
            return unicodeHostName;
        }
    }

    /**
     * Converts an ASCII-encoded string to Unicode. Unassigned characters are not allowed and STD3 host names are enforced. Input may be
     * domain name containing dots.
     * 
     * @param asciiHostName The host name as ASCII string.
     * @param useIDNA2008 <code>true</code> to allow PVALID code points
     * @return The Unicode host name
     */
    public static String toUnicode(final String asciiHostName, final boolean useIDNA2008) {
        if (null == asciiHostName || asciiHostName.indexOf(ACE_PREFIX) < 0) {
            return asciiHostName;
        }
        return gnu.inet.encoding.IDNA.toUnicode(asciiHostName, useIDNA2008);
    }

}
