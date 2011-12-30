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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
        return new HashKey(key);
    }

    private static final int DEFAULT_HASH = 5381;

    private static final int MULTIPLICATION_CONSTANT = 33;

    private static int calcSafeHashCode(final char[] val) {
        int h = DEFAULT_HASH;
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
    private final char value[];

    /** The count is the number of characters in the String. */
    private final int count;

    /** Cache the hash code for the string */
    private final int hash; // Default to 5381

    /**
     * Initializes a new {@link HashKey}.
     */
    private HashKey(final String key) {
        super();
        value = key.toCharArray();
        count = value.length;
        hash = calcSafeHashCode(value);
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
        if (count != other.count) {
            return false;
        }
        int n = count;
        final char v1[] = value;
        final char v2[] = other.value;
        int i = 0;
        while (n-- != 0) {
            if (v1[i] != v2[i++]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return new String(value);
    }

}
