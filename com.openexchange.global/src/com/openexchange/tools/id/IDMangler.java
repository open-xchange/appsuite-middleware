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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
        final com.openexchange.java.StringAllocator id = new com.openexchange.java.StringAllocator(50);
        id.append(escape(components[0]));
        boolean first = true;
        for (int i = 1; i < components.length; i++) {
            if (first) {
                id.append(PRIMARY_DELIM);
                first = false;
            } else {
                id.append(CHAR_SECONDARY_DELIM);
            }
            id.append(escape(components[i]));
        }
        return id.toString();
    }

    private static String escape(final String string) {
        if (string == null) {
            return null;
        }
        return encodeQP(string);
    }

    private static enum ParserState {
        APPEND, APPEND_PREFIX, PRIMARY_DELIM1, PRIMARY_DELIM2, ESCAPED;
    }

    /**
     * Parses specified mangled identifier into its String components.
     *
     * @param mangled The mangled identifier
     * @return The identifier's components
     */
    public static List<String> unmangle(final String mangled) {
        return unmangle(mangled, false);
    }

    /**
     * Parses specified mangled identifier into its String components.
     *
     * @param mangled The mangled identifier
     * @param stateMachine <code>true</code> for state machine based parsing; otherwise <code>false</code>
     * @return The identifier's components
     */
    public static List<String> unmangle(String mangled, final boolean stateMachine) {
        final List<String> list = new ArrayList<String>(5);
        if (stateMachine) {
            final com.openexchange.java.StringAllocator buffer = new com.openexchange.java.StringAllocator(50);
            ParserState state = ParserState.APPEND_PREFIX;
            ParserState unescapedState = null;

            final int length = mangled.length();
            for (int i = 0; i < length; i++) {
                final char c = mangled.charAt(i);
                switch (c) {
                case '[': {
                    if (state == ParserState.ESCAPED) {
                        buffer.append(c);
                    } else {
                        unescapedState = state;
                        state = ParserState.ESCAPED;
                    }
                    break;
                }
                case ']': {
                    if (state == ParserState.ESCAPED) {
                        state = unescapedState;
                    } else {
                        buffer.append(c);
                    }
                    break;
                }
                case ':': {
                    switch (state) {
                    case APPEND:
                    case ESCAPED:
                        buffer.append(c);
                        break;
                    case APPEND_PREFIX:
                        state = ParserState.PRIMARY_DELIM1;
                        break;
                    }
                    break;
                }
                case '/': {
                    switch (state) {
                    case APPEND:
                        list.add(buffer.toString());
                        buffer.reinitTo(0);
                        break;
                    case APPEND_PREFIX:
                    case ESCAPED:
                        buffer.append(c);
                        break;
                    case PRIMARY_DELIM1:
                        state = ParserState.PRIMARY_DELIM2;
                        break;
                    case PRIMARY_DELIM2:
                        list.add(buffer.toString());
                        buffer.reinitTo(0);
                        state = ParserState.APPEND;
                        break;
                    }
                    break;
                }
                default: {
                    switch(state) {
                    case PRIMARY_DELIM1: buffer.append(':'); state = ParserState.APPEND_PREFIX; break;
                    case PRIMARY_DELIM2: buffer.append(":/"); state = ParserState.APPEND_PREFIX; break;
                    }
                    buffer.append(c);
                    break;
                }
                }
            }
            if (buffer.length() != 0) {
                list.add(buffer.toString());
            }
        } else {
            // Find first delimiter
            int prev = 0;
            int pos = mangled.indexOf(PRIMARY_DELIM, prev);
            if (pos == -1) {
            	list.add(mangled);
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
        } catch (final UnsupportedCharsetException e) {
            // Cannot occur
            return string;
        }
    }

    private static String decodeQP(final String string) {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(string)), com.openexchange.java.Charsets.UTF_8);
        } catch (final DecoderException e) {
            return string;
        }
    }
}
