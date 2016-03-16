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

/**
 * {@link HashKey} - Uses a safe hash key computation if a <code>String</code> is intended to be used as hash key.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashKey {

    /**
     * Returns the computation-safe hash key for specified <code>String</code> key.
     *
     * @param key The <code>String</code> key
     * @return The computation-safe hash key
     */
    public static HashKey valueOf(final String key) {
        return new HashKey(key, DEFAULT_HASH, null);
    }

    /**
     * Returns the computation-safe hash key for specified <code>String</code> key.
     *
     * @param key The <code>String</code> key
     * @param hashStart The hash start
     * @return The computation-safe hash key
     */
    public static HashKey valueOf(final String key, final int hashStart) {
        return new HashKey(key, hashStart, null);
    }

    /**
     * Returns the computation-safe hash key for specified <code>String</code> key.
     *
     * @param key The <code>String</code> key
     * @param hashStart The hash start
     * @param salt The salt
     * @return The computation-safe hash key
     */
    public static HashKey valueOf(final String key, final int hashStart, final String salt) {
        return new HashKey(key, hashStart, salt);
    }

    /**
     * Returns the computation-safe hash key for specified <code>String</code> key.
     *
     * @param key The <code>String</code> key
     * @param hashStart The hash start
     * @param salt The salt
     * @return The computation-safe hash key
     */
    public static HashKey valueOf(final String key, final String salt) {
        return new HashKey(key, DEFAULT_HASH, salt);
    }

    /**
     * The default hash start: <code>5381</code>.
     */
    public static final int DEFAULT_HASH = 5381;

    private static final int MULTIPLICATION_CONSTANT = 33;

    private static int calcSafeHashCode(final int hashStart, final String val) {
        int h = hashStart;
        final int len = val.length();
        if (len > 0) {
            final int fac = MULTIPLICATION_CONSTANT;
            for (int i = 0; i < len; i++) {
                h = fac * h + val.charAt(i);
            }
        }
        return h;
    }

    private static int calcSafeHashCode(final int hashStart, final char[] val) {
        int h = hashStart;
        final int len = val.length;
        if (len > 0) {
            final int fac = MULTIPLICATION_CONSTANT;
            for (int i = 0; i < len; i++) {
                h = fac * h + val[i];
            }
        }
        return h;
    }

    /** The value is used for character storage. */
    private final String value;

    /** Cache the hash code for the string */
    private final int hash;

    /**
     * Initializes a new {@link HashKey}.
     */
    private HashKey(final String key, final int hashStart, final String salt) {
        super();
        value = key;
        if (null == salt) {
            hash = calcSafeHashCode(hashStart, key);
        } else {
            final StringBuilder sb = new StringBuilder(key).append('-').append(salt);
            final int count = sb.length();
            final char[] chars = new char[count];
            sb.getChars(0, count, chars, 0);
            hash = calcSafeHashCode(hashStart, chars);
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashKey)) {
            return false;
        }
        final HashKey other = (HashKey) obj;
        if (null == value) {
            if (null != other.value) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value;
    }

}
