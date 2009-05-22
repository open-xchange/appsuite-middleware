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

package com.openexchange.java;

import java.util.Collection;

/**
 * Methods helping with Autoboxing to shorten method names and therefore source code.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Autoboxing {

    /**
     * Prevent instantiation.
     */
    private Autoboxing() {
        super();
    }

    /**
     * Short method name for {@link Integer#valueOf(int)} that uses cached instances for small values of integer.
     * @param i integer value to be converted to an Integer object.
     * @return Integer object.
     */
    public static Integer I(final int i) {
        return Integer.valueOf(i);
    }

    /**
     * Short method name for {@link Long#valueOf(long)} that uses cached instances for small values of long.
     * @param l long value to be converted to a Long object.
     * @return Long object.
     */
    public static Long L(final long l) {
        return Long.valueOf(l);
    }

    /**
     * Short method name for {@link Boolean#valueOf(boolean)} that uses cached instances.
     * @param b boolean value to be converted to a Boolean object.
     * @return Boolean object.
     */
    public static Boolean B(final boolean b) {
        return Boolean.valueOf(b);
    }

    /**
     * Short method name for {@link Float#valueOf(float)} that uses cached instances.
     * @param f float value to be converted to a Float object.
     * @return Float object.
     */
    public static Float F(final float f) {
        return Float.valueOf(f);
    }

    /**
     * Converts an int-array into an Integer-array.
     * @param intArray int[] to be converted to Integer[]
     * @return Integer[]
     */
    public static Integer[] i2I(int[] intArray) {
        Integer[] integerArray = new Integer[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            integerArray[i] = I(intArray[i]);
        }
        return integerArray;
    }

    /**
     * Converts an Integer-array into an int-array.
     * @param integerArray Integer[] to be converted to int[]
     * @return int[]
     */
    public static int[] I2i(Integer[] integerArray) {
        int[] intArray = new int[integerArray.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = integerArray[i].intValue();
        }
        return intArray;
    }

    /**
     * Converts an Integer-list into an int-array.
     * @param integerList List of Integers to be converted to int[]
     * @return int[]
     */
    public static int[] I2i(Collection<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        int pos = 0;
        for (Integer i : integerList) {
            intArray[pos++] = i.intValue();
        }
        return intArray;
    }
    
    /**
     * Converts a long-array into a Long-array.
     * @param longArray long[] to be converted to Long[]
     * @return Long[]
     */
    public static Long[] l2L(long[] longArray) {
        Long[] longerArray = new Long[longArray.length];
        for (int i = 0; i < longArray.length; i++) {
            longerArray[i] = L(longArray[i]);
        }
        return longerArray;
    }
}
