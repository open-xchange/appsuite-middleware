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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * {@link Strings} - A library for performing operations that create Strings
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Strings {

    /**
     * Returns a literal replacement <code>String</code> for the specified <code>String</code>. This method produces a <code>String</code>
     * that will work as a literal replacement <code>s</code> in the <code>appendReplacement</code> method of the {@link Matcher} class. The
     * <code>String</code> produced will match the sequence of characters in <code>s</code> treated as a literal sequence. Slashes ('\') and
     * dollar signs ('$') will be given no special meaning.
     * 
     * @param s The string to be literalized
     * @return A literal string replacement
     */
    public static String quoteReplacement(final String s) {
        if (isEmpty(s) || ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))) {
            return s;
        }
        final int length = s.length();
        final StringAllocator sb = new StringAllocator(length << 1);
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks for an empty string.
     * 
     * @param string The string
     * @return <code>true</code> if empty; else <code>false</code>
     */
    public static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Joins a collection of objects by connecting the results of their #toString() method with a connector
     *
     * @param coll Collection to be connected
     * @param connector Connector place between two objects
     * @return connected strings or null if collection == null or empty string if collection is empty
     */
    public static String join(final Collection<? extends Object> coll, final String connector) {
        if (coll == null) {
            return null;
        }
        if (coll.size() == 0) {
            return "";
        }
        final com.openexchange.java.StringAllocator builder = new com.openexchange.java.StringAllocator();
        for (final Object obj : coll) {
            if (obj == null) {
                builder.append("null");
            } else {
                builder.append(obj.toString());
            }
            builder.append(connector);
        }
        return builder.substring(0, builder.length() - connector.length());
    }

    public static <T> String join(final T[] arr, final String connector) {
        return join(Arrays.asList(arr), connector);
    }

    public static String join(final int[] arr, final String connector) {
        final List<Integer> list = new LinkedList<Integer>();
        for (final int i : arr) {
            list.add(Autoboxing.I(i));
        }
        return join(list, connector);
    }

    public static String join(final byte[] arr, final String connector) {
        final List<Byte> list = new LinkedList<Byte>();
        for (final Byte i : arr) {
            list.add(i);
        }
        return join(list, connector);
    }

    /**
     * Removes byte order marks from UTF8 strings.
     * @return new instance of trimmed string - or reference to old one if unchanged
     */
    public static String trimBOM(String str) {
		final byte[][] byteOrderMarks = new byte[][]{
				new byte[]{(byte)0x00, (byte)0x00, (byte)0xFE,(byte)0xFF},
				new byte[]{(byte)0xFF, (byte)0xFE, (byte)0x00,(byte)0x0},
				new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF},
				new byte[]{(byte)0xFE, (byte)0xFF},
				new byte[]{(byte)0xFE, (byte)0xFF}
			};

		byte[] bytes = str.getBytes();
		for(byte[] bom: byteOrderMarks){
			if(bom.length > bytes.length) {
                continue;
            }

			String pattern = new String(bom);
			if(! str.startsWith(pattern)) {
                continue;
            }

			int bomLen = new String(bom).getBytes().length; //sadly the BOM got encoded meanwhile

			int len = bytes.length-bomLen;
			byte[] trimmed = new byte[len];
			for(int i = 0; i < len; i++) {
                trimmed[i] = bytes[i+bomLen];
            }
			return new String(trimmed);
		}

		return str;
	}

}
