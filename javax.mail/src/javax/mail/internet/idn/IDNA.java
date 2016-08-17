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

package javax.mail.internet.idn;

import gnu.inet.encoding.IDNAException;
import javax.mail.internet.AddressException;
import org.slf4j.LoggerFactory;

/**
 * {@link IDNA} - Helper class for internationalized domain names (IDN).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDNA {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IDNA.class);

    /**
     * No Initialization.
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
     * @return The ASCII-encoded (punycode) of given internet address or <code>null</code> if argument is <code>null</code>
     * @throws AddressException If ASCII representation of given internet address cannot be created
     */
    public static String toACE(String idnAddress) throws AddressException {
        if (null == idnAddress || isAscii(idnAddress)) {
            return idnAddress;
        }
        try {
            // Check for presence of '@' character
            int pos = idnAddress.indexOf('@');
            if (pos < 0) {
                return idnAddress;
            }

            // Check location of '@' character (should no be at the very end)
            int length = idnAddress.length();
            if (pos == length - 1) {
                return idnAddress;
            }

            // Generate the ACE representation for given address (known to contain non-ascii characters)
            StringBuilder sb = new StringBuilder(length + 8).append(idnAddress.substring(0, pos)).append('@');
            if (idnAddress.endsWith(">")) {
                sb.append(gnu.inet.encoding.IDNA.toASCII(idnAddress.substring(pos + 1, idnAddress.length() - 1), true)).append('>');
            } else {
                sb.append(gnu.inet.encoding.IDNA.toASCII(idnAddress.substring(pos + 1), true));
            }
            return sb.toString();
        } catch (gnu.inet.encoding.IDNAException e) {
            AddressException ae = new AddressException(new StringBuilder(e.getMessage()).append(": ").append(idnAddress).toString());
            ae.setNextException(e);
            throw ae;
        } catch (RuntimeException e) {
            AddressException ae = new AddressException(new StringBuilder("Failed to convert IDN to ACE/puny-code address: '").append(idnAddress).append('\'').toString());
            ae.setNextException(e);
            throw ae;
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
     * @return The unicode representation of given internet address or <code>null</code> if argument is <code>null</code>
     */
    public static String toIDN(String aceAddress) {
        if (null == aceAddress) {
            return null;
        }
        int pos = aceAddress.indexOf('@');
        if (pos < 0 || aceAddress.indexOf(ACE_PREFIX) < 0) {
            return aceAddress;
        }
        try {
            String unicode = gnu.inet.encoding.IDNA.toUnicode(aceAddress.substring(pos + 1), true);
            return new StringBuilder(aceAddress.length()).append(aceAddress.substring(0, pos)).append('@').append(unicode).toString();
        } catch (RuntimeException e) {
            // Decoding punycode failed
            LOGGER.error("Failed to convert ACE/puny-code to IDN address: {}", aceAddress, e);
            return aceAddress;
        }
    }

    private static final String SCHEME_DELIM = "://";

    private static final char[] CHARS = { ':', '/', '?' };

    /**
     * Converts a Unicode string to ASCII using the procedure in RFC3490 section 4.1. Unassigned characters are not allowed and STD3 ASCII
     * rules are enforced. The input string may be a domain name containing dots.
     *
     * @param unicodeHostName The host name as Unicode string.
     * @return The encoded host name or <code>null</code> if argument is <code>null</code>
     */
    public static String toASCII(final String unicodeHostName) {
        if (null == unicodeHostName || isAscii(unicodeHostName)) {
            return unicodeHostName;
        }
        try {
            int pos = unicodeHostName.indexOf(SCHEME_DELIM);
            if (pos < 0) {
                return gnu.inet.encoding.IDNA.toASCII(unicodeHostName, true);
            }
            pos += SCHEME_DELIM.length();
            final StringBuilder b = new StringBuilder(unicodeHostName.length() + 16);
            b.append(unicodeHostName.substring(0, pos));
            final String host = unicodeHostName.substring(pos);
            pos = -1;
            for (int k = 0; pos < 0 && k < CHARS.length; k++) {
                pos = host.indexOf(CHARS[k]);
            }
            if (pos < 0) {
                b.append(gnu.inet.encoding.IDNA.toASCII(host, true));
            } else {
                b.append(gnu.inet.encoding.IDNA.toASCII(host.substring(0, pos), true)).append(host.substring(pos));
            }
            return b.toString();
        } catch (final IDNAException e) {
            LoggerFactory.getLogger(IDNA.class).warn("Couldn''t create ASCII representation for host name: {}", unicodeHostName, e);
            return unicodeHostName;
        }
    }

    /**
     * Converts an ASCII-encoded string to Unicode. Unassigned characters are not allowed and STD3 host names are enforced. Input may be
     * domain name containing dots.
     *
     * @param asciiHostName The host name as ASCII string.
     * @return The Unicode host name or <code>null</code> if argument is <code>null</code>
     */
    public static String toUnicode(final String asciiHostName) {
        if (null == asciiHostName || asciiHostName.indexOf(ACE_PREFIX) < 0) {
            return asciiHostName;
        }
        int pos = asciiHostName.indexOf(SCHEME_DELIM);
        if (pos < 0) {
            return gnu.inet.encoding.IDNA.toUnicode(asciiHostName, true);
        }
        pos += SCHEME_DELIM.length();
        final StringBuilder b = new StringBuilder(asciiHostName.length());
        b.append(asciiHostName.substring(0, pos));
        final String host = asciiHostName.substring(pos);
        pos = -1;
        for (int k = 0; pos < 0 && k < CHARS.length; k++) {
            pos = host.indexOf(CHARS[k]);
        }
        if (pos < 0) {
            b.append(gnu.inet.encoding.IDNA.toUnicode(host, true));
        } else {
            b.append(gnu.inet.encoding.IDNA.toUnicode(host.substring(0, pos), true)).append(host.substring(pos));
        }
        return b.toString();
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    private static boolean isAscii(final String s) {
        final int length = s.length();
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < length); i++) {
            isAscci = (s.charAt(i) < 128);
        }
        return isAscci;
    }

}
