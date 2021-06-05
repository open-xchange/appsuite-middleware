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

package com.openexchange.pop3.connect;

import java.lang.reflect.Array;

/**
 * {@link HashCodeUtil} - Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashCodeUtil {

    /**
     * An initial value for a <code>hashCode</code>, to which is added contributions from fields. Using a non-zero value decreases collisions
     * of <code>hashCode</code> values.
     */
    public static final int SEED = 23;

    /**
     * booleans.
     */
    public static int hash(final int aSeed, final boolean aBoolean) {
//        System.out.println("boolean...");
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    /**
     * chars.
     */
    public static int hash(final int aSeed, final char aChar) {
//        System.out.println("char...");
        return firstTerm(aSeed) + aChar;
    }

    /**
     * ints.
     */
    public static int hash(final int aSeed, final int aInt) {
        /*
         * Implementation Note Note that byte and short are handled by this method, through implicit conversion.
         */
//        System.out.println("int...");
        return firstTerm(aSeed) + aInt;
    }

    /**
     * longs.
     */
    public static int hash(final int aSeed, final long aLong) {
//        System.out.println("long...");
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    /**
     * floats.
     */
    public static int hash(final int aSeed, final float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /**
     * doubles.
     */
    public static int hash(final int aSeed, final double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an array. If <code>aObject</code> is an array, then each element
     * may be a primitive or a possibly-null object.
     */
    public static int hash(final int aSeed, final Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            final int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                final Object item = Array.get(aObject, idx);
                // recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }

    // / PRIVATE ///
    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm(final int aSeed) {
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(final Object aObject) {
        // Too slow: return aObject.getClass().isArray();
        return (null != aObject && '[' == aObject.getClass().getName().charAt(0));
    }

}
