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
 * {@link StringAppender} - A string appender using a customizable delimiter character.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class StringAppender {

    private final StringBuilder sb;
    private final String delim;
    private boolean first = true;

    /**
     * Initializes a new {@link StringAppender}.
     *
     * @param delim The delimiter character
     */
    public StringAppender(char delim) {
        this(delim, 256);
    }

    /**
     * Initializes a new {@link StringAppender}.
     *
     * @param delim The delimiter character
     * @param initialCapacity The initial capacity
     */
    public StringAppender(char delim, int initialCapacity) {
        super();
        sb = new StringBuilder(initialCapacity);
        this.delim = String.valueOf(delim);
    }

    /**
     * Initializes a new {@link StringAppender}.
     *
     * @param delim The delimiter sequence
     */
    public StringAppender(String delim) {
        this(delim, 256);
    }

    /**
     * Initializes a new {@link StringAppender}.
     *
     * @param delim The delimiter sequence
     * @param initialCapacity The initial capacity
     */
    public StringAppender(String delim, int initialCapacity) {
        super();
        sb = new StringBuilder(initialCapacity);
        this.delim = null == delim ? "null" : delim;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Gets the length (character count).
     *
     * @return The length of the sequence of characters currently represented by this object
     */
    public int length() {
        return sb.length();
    }

    /**
     * Appends the string representation of the <code>boolean</code> argument to the sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param b a <code>boolean</code>.
     * @return a reference to this object.
     */
    public StringAppender append(boolean b) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(b);
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is appended to the contents of this sequence. The length of this sequence increases by <code>1</code>.
     * <p>
     * The overall effect is exactly as if the argument were converted to a string by the method {@link String#valueOf(char)} and the
     * character in that string were then {@link #append(String) appended} to this character sequence.
     *
     * @param c a <code>char</code>.
     * @return a reference to this object.
     */
    public StringAppender append(char c) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(c);
        return this;
    }

    /**
     * Appends the string representation of the <code>int</code> argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param i an <code>int</code>.
     * @return a reference to this object.
     */
    public StringAppender append(int i) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(i);
        return this;
    }

    /**
     * Appends the string representation of the <code>long</code> argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param l a <code>long</code>.
     * @return a reference to this object.
     */
    public StringAppender append(long l) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(l);
        return this;
    }

    /**
     * Appends the string representation of the <code>float</code> argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this string sequence.
     *
     * @param f a <code>float</code>.
     * @return a reference to this object.
     */
    public StringAppender append(float f) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(f);
        return this;
    }

    /**
     * Appends the string representation of the <code>double</code> argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param d a <code>double</code>.
     * @return a reference to this object.
     */
    public StringAppender append(double d) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(d);
        return this;
    }

    /**
     * Appends the string representation of the <code>Object</code> argument (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param obj an <code>Object</code>.
     * @return a reference to this object.
     */
    public StringAppender append(Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the specified string to this character sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The characters of the <code>String</code> argument are appended, in order, increasing the length of this sequence by the length of
     * the argument. If <code>str</code> is <code>null</code>, then the four characters <code>"null"</code> are appended.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to execution of the <code>append</code> method. Then the character
     * at index <i>k</i> in the new character sequence is equal to the character at index <i>k</i> in the old character sequence, if
     * <i>k</i> is less than <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> in the argument <code>str</code>.
     *
     * @param str a string.
     * @return a reference to this object.
     */
    public StringAppender append(String str) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(str);
        return this;
    }

    /**
     * Appends the specified <tt>StringBuffer</tt> to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The characters of the <tt>StringBuffer</tt> argument are appended, in order, to this sequence, increasing the length of this sequence
     * by the length of the argument. If <tt>sb</tt> is <tt>null</tt>, then the four characters <tt>"null"</tt> are appended to this
     * sequence.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to execution of the <tt>append</tt> method. Then the character at
     * index <i>k</i> in the new character sequence is equal to the character at index <i>k</i> in the old character sequence, if <i>k</i>
     * is less than <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> in the argument <code>sb</code>.
     *
     * @param sb the <tt>StringBuffer</tt> to append.
     * @return a reference to this object.
     */
    public StringAppender append(StringBuffer other) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(other);
        return this;
    }

    /**
     * Appends the specified <tt>StringBuilder</tt> to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The characters of the <tt>StringBuilder</tt> argument are appended, in order, to this sequence, increasing the length of this
     * sequence by the length of the argument. If <tt>sb</tt> is <tt>null</tt>, then the four characters <tt>"null"</tt> are appended to
     * this sequence.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to execution of the <tt>append</tt> method. Then the character at
     * index <i>k</i> in the new character sequence is equal to the character at index <i>k</i> in the old character sequence, if <i>k</i>
     * is less than <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> in the argument <code>sb</code>.
     *
     * @param sb the <tt>StringBuilder</tt> to append.
     * @return a reference to this object.
     */
    public StringAppender append(StringBuilder other) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(other);
        return this;
    }

    /**
     * Appends the specified <code>CharSequence</code> to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * If <code>s</code> is <code>null</code>, then this method appends characters as if the s parameter was a sequence containing the four
     * characters <code>"null"</code>.
     *
     * @param s the sequence to append.
     * @return a reference to this object.
     */
    public StringAppender append(CharSequence s) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(s);
        return this;
    }

    /**
     * Appends a subsequence of the specified <code>CharSequence</code> to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * Characters of the argument <code>s</code>, starting at index <code>start</code>, are appended, in order, to the contents of this
     * sequence up to the (exclusive) index <code>end</code>. The length of this sequence is increased by the value of
     * <code>end - start</code>.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to execution of the <code>append</code> method. Then the character
     * at index <i>k</i> in this character sequence becomes equal to the character at index <i>k</i> in this sequence, if <i>k</i> is less
     * than <i>n</i>; otherwise, it is equal to the character at index <i>k+start-n</i> in the argument <code>s</code>.
     * <p>
     * If <code>s</code> is <code>null</code>, then this method appends characters as if the s parameter was a sequence containing the four
     * characters <code>"null"</code>.
     *
     * @param s the sequence to append.
     * @param start the starting index of the subsequence to be appended.
     * @param end the end index of the subsequence to be appended.
     * @return a reference to this object.
     * @throws IndexOutOfBoundsException if <code>start</code> or <code>end</code> are negative, or <code>start</code> is greater than
     *             <code>end</code> or <code>end</code> is greater than <code>s.length()</code>
     */
    public StringAppender append(CharSequence s, int start, int end) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(s, start, end);
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> array argument to this sequence (prefixed by delimiter character in case of subsequent append operation).
     * <p>
     * The characters of the array argument are appended, in order, to the contents of this sequence. The length of this sequence increases
     * by the length of the argument.
     * <p>
     * The overall effect is exactly as if the argument were converted to a string by the method {@link String#valueOf(char[])} and the
     * characters of that string were then {@link #append(String) appended} to this character sequence.
     *
     * @param str the characters to be appended.
     * @return a reference to this object.
     */
    public StringAppender append(char str[]) {
        if (first) {
            first = false;
        } else {
            sb.append(delim);
        }
        sb.append(str);
        return this;
    }

}
