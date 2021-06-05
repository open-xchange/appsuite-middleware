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

package com.openexchange.tools.id;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import com.openexchange.java.Charsets;

/**
 * {@link IDMangler} - Utility class for generating & parsing a mangled/composite identifier.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IDMangler {

    /**
     * The primary delimiter: <code>"://"</code>
     */
    public static final String PRIMARY_DELIM = "://";

    private static final char CHAR_SECONDARY_DELIM = '/';

    /**
     * The secondary delimiter: <code>"/"</code>
     */
    public static final String SECONDARY_DELIM = Character.toString(CHAR_SECONDARY_DELIM);

    /**
     * Generates a mangled/composite identifier from specified String components.
     *
     * @param components The String components
     * @return The mangled/composite identifier
     */
    public static String mangle(final String... components) {
        final StringBuilder id = new StringBuilder(50);
        boolean first = true;
        for (String component : components) {
            component = escape(component);
            id.append(component);
            final String delim = first ? PRIMARY_DELIM : SECONDARY_DELIM;
            id.append(delim);
            first = false;
        }
        id.setLength(id.length()-1);
        return id.toString();
    }

    private static String escape(final String string) {
        if (string == null) {
            return null;
        }
        return encodeQP(string);
    }

    /**
     * Parses specified mangled identifier into its String components.
     *
     * @param mangled The mangled identifier
     * @return The identifier's components
     */
    public static List<String> unmangle(String mangled) {
        if (null == mangled) {
            return null;
        }
        List<String> list = new ArrayList<String>(5);
        // Find first delimiter
        int prev = 0;
        int pos = mangled.indexOf(PRIMARY_DELIM, prev);
        if (pos < 0) {
            pos = mangled.indexOf(CHAR_SECONDARY_DELIM, prev);
            if (pos <= 0) {
                list.add(mangled);
            } else {
                list.add(decodeQP(mangled.substring(prev, pos)));
                list.add(decodeQP(mangled.substring(pos + 1)));
            }
        	return list;
        }
        list.add(decodeQP(mangled.substring(prev, pos)));
        prev = pos + PRIMARY_DELIM.length();
        while (prev > 0) {
            pos = mangled.indexOf(CHAR_SECONDARY_DELIM, prev);
            if (pos > 0) {
                list.add(decodeQP(mangled.substring(prev, pos)));
                prev = pos + 1;
            } else {
                list.add(decodeQP(mangled.substring(prev)));
                prev = -1;
            }
        }
        return list;
    }

    private static final BitSet PRINTABLE_CHARS;
    // Static initializer for printable chars collection
    static {
        final BitSet bitSet = new BitSet(256);
        for (int i = '0'; i <= '9'; i++) {
            bitSet.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            bitSet.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            bitSet.set(i);
        }
        bitSet.set('.');
        bitSet.set('-');
        bitSet.set('_');
        PRINTABLE_CHARS = bitSet;
    }

    private static String encodeQP(final String string) {
        try {
            return Charsets.toAsciiString(QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, string.getBytes(com.openexchange.java.Charsets.UTF_8)));
        } catch (UnsupportedCharsetException e) {
            // Cannot occur
            return string;
        }
    }

    private static String decodeQP(final String string) {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(string)), com.openexchange.java.Charsets.UTF_8);
        } catch (DecoderException e) {
            return string;
        }
    }
}
