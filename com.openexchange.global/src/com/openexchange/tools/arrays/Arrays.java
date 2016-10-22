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

package com.openexchange.tools.arrays;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains convenience methods for dealing with arrays.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Arrays {

    /**
     * Prevent instantiation
     */
    private Arrays() {
        super();
    }

    /**
     * Concatenates the specified arrays.
     * <pre>
     *   int[] a = {1,2,3};
     *   int[] b = {4,5,6};
     *   int[] c = concatenate(a, b);
     *   System.out.println(Arrays.toString(c))
     * </pre>
     * Outputs: <code>{1,2,3,4,5,6}</code>
     *
     * @param a The first array
     * @param b The second array
     * @return The resulting array
     */
    public static int[] concatenate(int[] a, int[] b) {
        if (a == null) {
            return clone(b);
        }

        if (b == null) {
            return clone(a);
        }

        int[] c = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * Clones specified array
     *
     * @param data The array to clone
     * @return The cloned array
     */
    public static int[] clone(int[] data) {
        if (data == null) {
            return null;
        }

        int[] copy = new int[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    /**
     * Searches the given int value in the int array.
     *
     * @param array int array tested for containing the search parameter.
     * @param search this int is tested if the array contains it.
     * @return <code>true</code> if the array contains the int value.
     */
    public static boolean contains(int[] array, int search) {
        for (int i = array.length; i-- > 0;) {
            if (array[i] == search) {
                return true;
            }
        }
        return false;
    }

    public static int[] addUniquely(final int[] toExtend, final int... other) {
        if (toExtend == null || other == null) {
            return toExtend;
        }
        final TIntSet tmp = new TIntHashSet(toExtend.length + other.length);
        for (final int i : toExtend) {
            tmp.add(i);
        }
        for (final int i : other) {
            tmp.add(i);
        }
        return tmp.toArray();
    }

    public static <T> T[] remove(T[] removeFrom, T... toRemove) {
        List<T> tmp = new ArrayList<T>();
        for (T copy : removeFrom) {
            tmp.add(copy);
        }
        for (T remove : toRemove) {
            tmp.remove(remove);
        }
        @SuppressWarnings("unchecked")
        T[] retval = tmp.toArray((T[]) Array.newInstance(removeFrom.getClass().getComponentType(), tmp.size()));
        return retval;
    }

    public static <T> T[] add(T[] toExtend, T... other) {
        if (other == null) {
            return toExtend;
        }
        @SuppressWarnings("unchecked")
        T[] tmp = (T[]) Array.newInstance(toExtend.getClass().getComponentType(), toExtend.length + other.length);
        System.arraycopy(toExtend, 0, tmp, 0, toExtend.length);
        System.arraycopy(other, 0, tmp, toExtend.length, other.length);
        return tmp;
    }

    public static <T> T[] clone(T[] toClone) {
        @SuppressWarnings("unchecked")
        T[] retval = (T[]) Array.newInstance(toClone.getClass().getComponentType(), toClone.length);
        System.arraycopy(toClone, 0, retval, 0, toClone.length);
        return retval;
    }

    public static int[] extract(int[] source, int start) {
        return extract(source, start, source.length - start);
    }

    public static int[] extract(int[] source, int start, int length) {
        final int realLength = determineRealSize(source.length, start, length);
        final int[] retval = new int[realLength];
        System.arraycopy(source, start, retval, 0, realLength);
        return retval;
    }

    /**
     * Extracts specified sub-array from given source array starting at given offset.
     *
     * @param source The source array to extract from
     * @param start The start offset
     * @param length The number of elements to extract
     * @param clazz The array's type
     * @return The extracted sub-array
     */
    public static <T> T[] extract(T[] source, int start, int length, Class<? extends T> clazz) {
        final int realLength = determineRealSize(source.length, start, length);
        @SuppressWarnings("unchecked")
        final T[] retval = (T[]) Array.newInstance(clazz, realLength);
        System.arraycopy(source, start, retval, 0, realLength);
        return retval;
    }

    /**
     * Determines the real size
     *
     * @param size The size/length of the source array
     * @param start The start offset
     * @param length The number of elements to extract
     * @return The size of the resulting array carrying the extracted elements
     */
    public static int determineRealSize(int size, int start, int length) {
        return start + length > size ? size - start : length;
    }

    public static Serializable[] toSerializable(Integer[] ids) {
        final Serializable[] retval = new Serializable[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = ids[i];
        }
        return retval;
    }

    /**
     * Reverses the order of the elements contained in the supplied array.
     *
     * @param array The array to reverse
     */
    public static <T> void reverse(T[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            T t = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = t;
        }
    }

    /**
     * Gets a value indicating whether the supplied array contains an element that is "equal to" the supplied one.
     *
     * @param array The array to check
     * @param t The element to lookup
     * @return <code>true</code> if an equal element was found, <code>false</code>, otherwise
     */
    public static <T> boolean contains(T[] array, T t) {
        if (null != t) {
            if (null == array) {
                return false;
            }

            for (int i = array.length; i-- > 0;) {
                if (t.equals(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }

}
