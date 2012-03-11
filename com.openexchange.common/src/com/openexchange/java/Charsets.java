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

package com.openexchange.java;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class for storing character sets.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Charsets {

    /**
     * US-ASCII character set.
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * The name of "UTF-8" charset.
     */
    public static final String UTF_8_NAME = "UTF-8";

    /**
     * UTF-8 character set.
     */
    public static final Charset UTF_8 = Charset.forName(UTF_8_NAME);

    /**
     * ISO-8859-1 character set.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * The charset cache.
     */
    private static final ConcurrentMap<String, Charset> CACHE = new ConcurrentHashMap<String, Charset>();

    /**
     * Prevent instantiation
     */
    private Charsets() {
        super();
    }

    /**
     * Gets a {@link Charset charset} object for the named charset.
     *
     * @param charsetName The name of the requested charset; may be either a canonical name or an alias
     * @return The {@link Charset charset} object for the named charset
     * @throws IllegalCharsetNameException If the given charset name is illegal
     * @throws UnsupportedCharsetException If no support for the named charset is available in this instance of the Java virtual machine
     */
    public static Charset forName(final String charsetName) {
        Charset cs = CACHE.get(charsetName);
        if (null == cs) {
            final Charset ncs = Charset.forName(charsetName);
            cs = CACHE.putIfAbsent(charsetName, ncs);
            if (null == cs) {
                cs = ncs;
            }
        }
        return cs;
    }

    /**
     * Constructs a new <tt>String</tt> by decoding the specified array of bytes using the specified charset. The length of the new
     * <tt>String</tt> is a function of the charset, and hence may not be equal to the length of the byte array.
     *
     * @param bytes The bytes to construct the <tt>String</tt> from
     * @param charset The charset
     * @return The new <tt>String</tt>
     */
    public static String toString(final byte[] bytes, final Charset charset) {
        return charset.decode(ByteBuffer.wrap(bytes)).toString();
    }

    /**
     * Encodes specified <tt>String</tt> into a sequence of bytes using the given charset, storing the result into a new byte array.
     *
     * @param source The string
     * @param charset The charset
     * @return The resulting bytes
     */
    public static byte[] getBytes(final String source, final Charset charset) {
        final ByteBuffer buf = charset.encode(CharBuffer.wrap(source));
        final byte[] retval = new byte[buf.limit()];
        buf.get(retval);
        return retval;
    }

}
