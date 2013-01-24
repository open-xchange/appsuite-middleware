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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package org.json;


/**
 * {@link DynamicCharArray} - The dynamic character array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DynamicCharArray implements ICharArray {

    private final int initialCapacity;
    private CharArray delegatee;

    /**
     * Initializes a new small-sized {@link DynamicCharArray}.
     */
    protected DynamicCharArray() {
        super();
        initialCapacity = CharArrayPool.getInstance().getSmallLength();
    }

    /**
     * Initializes a new {@link DynamicCharArray}.
     */
    public DynamicCharArray(final int capacity) {
        super();
        initialCapacity = capacity;
    }

    private CharArray delegatee() {
        CharArray ca = delegatee;
        if (null == ca) {
            ca = CharArrayPool.getInstance().getCharArrayFor(initialCapacity);
            if (null == ca) {
                // Pool is currently empty
                ca = new CharArray(initialCapacity);
            }
            delegatee = ca;
        }
        return ca;
    }

    /**
     * Appends specified character
     *
     * @param c The character to append
     * @return This character array with character appended
     */
    public DynamicCharArray append(final char c) {
        CharArray delegatee = delegatee();
        if (delegatee.remainingCapacity() < 1) {
            // Increase character array's size
            delegatee = increase(delegatee.capacity() + 1);
        }
        delegatee.chars[delegatee.pos++] = c;
        delegatee.hash = null;
        return this;
    }

    /**
     * Appends specified String
     *
     * @param s The String
     * @return This character array with String appended
     */
    public DynamicCharArray append(final String s) {
        String str = s;
        if (str == null) {
            str = "null";
        }
        final int len = str.length();
        if (len == 0) {
            return this;
        }
        CharArray delegatee = delegatee();
        if (delegatee.remainingCapacity() < len) {
            // Increase character array's size
            delegatee = increase(delegatee.capacity() + len);
        }
        str.getChars(0, len, delegatee.chars, delegatee.pos);
        delegatee.pos += len;
        delegatee.hash = null;
        return this;
    }

    /**
     * Appends specified characters
     *
     * @param chars The characters
     * @return This character array with characters appended
     */
    public DynamicCharArray append(final char chars[]) {
        return append(chars, 0, chars.length);
    }

    /**
     * Appends specified characters
     *
     * @param chars The characters
     * @param offset The offset position
     * @param count The number of characters to append
     * @return This character array with characters appended
     */
    public DynamicCharArray append(final char chars[], final int offset, final int count) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(offset));
        }
        if (count < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(count));
        }
        // Note: offset or count might be near -1>>>1.
        if (offset > chars.length - count) {
            throw new IndexOutOfBoundsException(Integer.toString(offset + count));
        }
        CharArray delegatee = delegatee();
        if (delegatee.remainingCapacity() < count) {
            // Increase character array's size
            delegatee = increase(delegatee.capacity() + delegatee.remainingCapacity());
        }
        System.arraycopy(chars, offset, delegatee.chars, delegatee.pos, count);
        delegatee.pos += count;
        delegatee.hash = null;
        return this;
    }

    private CharArray increase(final int minCapacity) {
        final CharArrayPool pool = CharArrayPool.getInstance();
        CharArray ca = pool.getCharArrayFor(minCapacity);
        if (null == ca) {
            int newCapacity = (delegatee.capacity() * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            ca = new CharArray(pool.getCharArrayLength(newCapacity));
        }
        ca.copyFrom(delegatee);
        pool.offer(delegatee);
        delegatee = ca;
        return ca;
    }

    @Override
    public void reset() {
        if (null != delegatee) {
            CharArrayPool.getInstance().offer(delegatee);
            delegatee = null;
        }
    }

    @Override
    public int capacity() {
        return delegatee().capacity();
    }

    @Override
    public int remainingCapacity() {
        return delegatee().remainingCapacity();
    }

    @Override
    public int length() {
        return delegatee().length();
    }

    @Override
    public char charAt(final int index) {
        return delegatee().charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return delegatee().subSequence(start, end);
    }

    @Override
    public void copyTo(final CharArray other) {
        delegatee().copyTo(other);
    }

    @Override
    public void copyFrom(final CharArray other) {
        delegatee().copyFrom(other);
    }

    @Override
    public String toString() {
        return delegatee().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((delegatee == null) ? 0 : delegatee.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof DynamicCharArray)) {
            return false;
        }
        final DynamicCharArray other = (DynamicCharArray) obj;
        if (delegatee == null) {
            if (other.delegatee != null) {
                return false;
            }
        } else if (!delegatee.equals(other.delegatee)) {
            return false;
        }
        return true;
    }

}
