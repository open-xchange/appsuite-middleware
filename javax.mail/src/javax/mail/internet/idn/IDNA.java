/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package javax.mail.internet.idn;

import javax.mail.internet.AddressException;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import gnu.inet.encoding.IDNAException;

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
            String unicode = gnu.inet.encoding.IDNA.toUnicode(Strings.asciiLowerCase(aceAddress.substring(pos + 1)), true);
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
        } catch (IDNAException e) {
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
