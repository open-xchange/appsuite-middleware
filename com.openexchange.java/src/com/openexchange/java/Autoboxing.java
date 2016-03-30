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

    public static Byte B(final byte b) {
        return Byte.valueOf(b);
    }

    public static byte b(final Byte b) {
        return b.byteValue();
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
     * Short method name for unboxing an {@link Integer} object.
     * @param integer {@link Integer} to unbox.
     * @return the int value
     * @throws NullPointerException If passed {@code java.lang.Integer} instance is <code>null</code>
     */
    public static int i(final Integer integer) {
        return integer.intValue();
    }

    /**
     * Short method name for {@link Long#valueOf(long)} that uses cached instances for small values of long.
     * @param l long value to be converted to a Long object.
     * @return Long object.
     */
    public static Long L(final long l) {
        return Long.valueOf(l);
    }

    public static long l(final Long l) {
        return l.longValue();
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
     * Short method name for {@link Boolean#booleanValue()}.
     * @param b {@link Boolean} object to be converted to a boolean value.
     * @return boolean value.
     */
    public static boolean b(final Boolean b) {
        return b.booleanValue();
    }

    /**
     * Short method name for {@link Float#valueOf(float)} that uses cached instances.
     * @param f float value to be converted to a Float object.
     * @return Float object.
     */
    public static Float F(final float f) {
        return Float.valueOf(f);
    }

    public static float f(final Float f) {
        return f.floatValue();
    }

    /**
     * Converts an int-array into an Integer-array.
     * @param intArray int[] to be converted to Integer[]
     * @return Integer[]
     */
    public static Integer[] i2I(final int[] intArray) {
        final Integer[] integerArray = new Integer[intArray.length];
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
    public static int[] I2i(final Integer[] integerArray) {
        int[] intArray = new int[integerArray.length];
        int pos = 0;
        for (Integer i : integerArray) {
            if (null != i) {
                intArray[pos++] = i.intValue();
            }
        }
        if (pos != intArray.length) {
            final int[] tmpArray = new int[pos];
            System.arraycopy(intArray, 0, tmpArray, 0, pos);
            intArray = tmpArray;
        }
        return intArray;
    }

    /**
     * Converts an Integer-list into an int-array.
     * @param integerCollection List of Integers to be converted to int[]
     * @return int[]
     */
    public static int[] I2i(final Collection<Integer> integerCollection) {
        int[] intArray = new int[integerCollection.size()];
        int pos = 0;
        for (final Integer i : integerCollection) {
            if (null != i) {
                intArray[pos++] = i.intValue();
            }
        }
        if (pos != intArray.length) {
            final int[] tmpArray = new int[pos];
            System.arraycopy(intArray, 0, tmpArray, 0, pos);
            intArray = tmpArray;
        }
        return intArray;
    }

    public static byte[] B2b(final Collection<Byte> byteCollection) {
        byte[] byteArray = new byte[byteCollection.size()];
        int pos = 0;
        for (final Byte b : byteCollection) {
            if (null != b) {
                byteArray[pos++] = b.byteValue();
            }
        }
        if (pos != byteArray.length) {
            final byte[] tmpArray = new byte[pos];
            System.arraycopy(byteArray, 0, tmpArray, 0, pos);
            byteArray = tmpArray;
        }
        return byteArray;
    }

    /**
     * Converts a long-array into a Long-array.
     * @param longArray long[] to be converted to Long[]
     * @return Long[]
     */
    public static Long[] l2L(final long[] longArray) {
        final Long[] longerArray = new Long[longArray.length];
        for (int i = 0; i < longArray.length; i++) {
            longerArray[i] = L(longArray[i]);
        }
        return longerArray;
    }

    /**
     * Conversts an objec-array into a Boolean-array
     * @param source
     * @return
     */
    public static Boolean[] O2B(final Object[] source) {
        final Boolean[] target = new Boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = (Boolean) source[i];
        }
        return target;
    }

    /**
     * Conversta an Object-array into a Number-array
     * @param source
     * @return
     */
    public static Number[] O2N(final Object[] source) {
        final Number[] target = new Number[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = (Number) source[i];
        }
        return target;
    }

    /**
     * Converst an Object-array into a String-array
     * @param source
     * @return
     */
    public static String[] O2S(final Object[] source) {
        final String[] target = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = (String) source[i];
        }
        return target;
    }

    /**
     * Converts an Object-array into a Long-array
     * @param source
     * @return
     */
    public static Long[] O2L(final Object[] source) {
        final Long[] target = new Long[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = (Long) source[i];
        }
        return target;
    }

    /**
     * Converts a collection of integers into an int-array
     */
    public static int[] Coll2i(final Collection<Integer> collection){
    	final int[] results = new int[collection.size()];
    	int position = 0;
    	for(final Integer value : collection) {
            results[position++] = value.intValue();
        }
    	return results;
    }

    // Type Coercion

    public static int a2i(final Object anything) {
        if(anything == null) {
            throw new NullPointerException("Can't convert null into integer");
        }
        if(Integer.class.isInstance(anything)){
            return ((Integer) anything).intValue();
        }
        if(Byte.class.isInstance(anything)) {
            return ((Byte) anything).intValue();
        }
        if(Long.class.isInstance(anything)) {
            return ((Long) anything).intValue();
        }
        if(String.class.isInstance(anything)) {
            return Integer.parseInt((String) anything);
        }

        throw new ClassCastException("I don't know how to turn "+anything+" of class "+anything.getClass().getName()+" into an int.");
    }

    public static boolean a2b(final Object anything) {
        if(anything == null) {
            throw new NullPointerException("Can't convert null into boolean");
        }
        if(Boolean.class.isInstance(anything)){
            return ((Boolean) anything).booleanValue();
        }

        if(String.class.isInstance(anything)) {
            return Boolean.parseBoolean((String) anything);
        }

        throw new ClassCastException("I don't know how to turn "+anything+" of class "+anything.getClass().getName()+" into a boolean.");
    }

}
