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
 * {@link BoundedStringBuilder} - A bounded string builder that throws an <code>BoundaryExceededException</code> if given <code>maxCount</code>
 * is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class BoundedStringBuilder implements java.io.Serializable, CharSequence {

    private static final long serialVersionUID = 298165706640235317L;

    private final StringBuilder sb;
    private final int maxCount;

    /**
     * Constructs a string builder with no characters in it and an
     * initial capacity of 16 characters.
     *
     * @param      maxCount The max. number of characters
     */
    public BoundedStringBuilder(int maxCount) {
        super();
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        sb = new StringBuilder();
        this.maxCount = maxCount;
    }

    /**
     * Constructs a string builder with no characters in it and an
     * initial capacity specified by the <code>capacity</code> argument.
     *
     * @param      capacity  the initial capacity.
     * @param      maxCount The max. number of characters
     * @throws     NegativeArraySizeException  if the <code>capacity</code>
     *               argument is less than <code>0</code>.
     */
    public BoundedStringBuilder(int capacity, int maxCount) {
        super();
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        if (capacity < 0) {
            throw new NegativeArraySizeException("capacity is less than zero");
        }
        if (capacity > maxCount) {
            throw new BoundaryExceededException("capacity is bigger than maxCount");
        }
        sb = new StringBuilder(capacity);
        this.maxCount = maxCount;
    }

    /**
     * Constructs a string builder initialized to the contents of the
     * specified string. The initial capacity of the string builder is
     * <code>16</code> plus the length of the string argument.
     *
     * @param   str   the initial contents of the buffer.
     * @param      maxCount The max. number of characters
     * @throws    NullPointerException if <code>str</code> is <code>null</code>
     */
    public BoundedStringBuilder(String str, int maxCount) {
        super();
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        if (null == str) {
            throw new NullPointerException("str is null");
        }
        if (str.length() > maxCount) {
            throw new BoundaryExceededException("str is bigger than maxCount");
        }
        sb = new StringBuilder(str);
        this.maxCount = maxCount;
    }

    /**
     * Constructs a string builder that contains the same characters
     * as the specified <code>CharSequence</code>. The initial capacity of
     * the string builder is <code>16</code> plus the length of the
     * <code>CharSequence</code> argument.
     *
     * @param      seq   the sequence to copy.
     * @param      maxCount The max. number of characters
     * @throws    NullPointerException if <code>seq</code> is <code>null</code>
     */
    public BoundedStringBuilder(CharSequence seq, int maxCount) {
        super();
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        if (null == seq) {
            throw new NullPointerException("seq is null");
        }
        if (seq.length() > maxCount) {
            throw new BoundaryExceededException("seq is bigger than maxCount");
        }
        sb = new StringBuilder(seq);
        this.maxCount = maxCount;
    }

    private void checkLength() {
        if (sb.length() > maxCount) {
            throw new BoundaryExceededException("maxCount exceeded");
        }
    }

    @Override
    public int length() {
        return sb.length();
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage
     * available for newly inserted characters, beyond which an allocation
     * will occur.
     *
     * @return  the current capacity
     */
    public int capacity() {
        return sb.capacity();
    }

    /**
     * Attempts to reduce storage used for the character sequence.
     * If the buffer is larger than necessary to hold its current sequence of
     * characters, then it may be resized to become more space efficient.
     * Calling this method may, but is not required to, affect the value
     * returned by a subsequent call to the {@link #capacity()} method.
     */
    public void trimToSize() {
        sb.trimToSize();
    }

    /**
     * Sets the length of the character sequence.
     * The sequence is changed to a new character sequence
     * whose length is specified by the argument. For every nonnegative
     * index <i>k</i> less than <code>newLength</code>, the character at
     * index <i>k</i> in the new character sequence is the same as the
     * character at index <i>k</i> in the old sequence if <i>k</i> is less
     * than the length of the old character sequence; otherwise, it is the
     * null character <code>'&#92;u0000'</code>.
     *
     * In other words, if the <code>newLength</code> argument is less than
     * the current length, the length is changed to the specified length.
     * <p>
     * If the <code>newLength</code> argument is greater than or equal
     * to the current length, sufficient null characters
     * (<code>'&#92;u0000'</code>) are appended so that
     * length becomes the <code>newLength</code> argument.
     * <p>
     * The <code>newLength</code> argument must be greater than or equal
     * to <code>0</code>.
     *
     * @param      newLength   the new length
     * @throws     IndexOutOfBoundsException  if the
     *               <code>newLength</code> argument is negative.
     */
    public void setLength(int newLength) {
        sb.setLength(newLength);
    }

    /**
     * Appends the string representation of the {@code Object} argument.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(Object)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   obj   an {@code Object}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the specified string to this character sequence.
     * <p>
     * The characters of the {@code String} argument are appended, in
     * order, increasing the length of this sequence by the length of the
     * argument. If {@code str} is {@code null}, then the four
     * characters {@code "null"} are appended.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to
     * execution of the {@code append} method. Then the character at
     * index <i>k</i> in the new character sequence is equal to the character
     * at index <i>k</i> in the old character sequence, if <i>k</i> is less
     * than <i>n</i>; otherwise, it is equal to the character at index
     * <i>k-n</i> in the argument {@code str}.
     *
     * @param   str   a string.
     * @return  a reference to this object.
     * @throws IllegalStateException If maxCount is exceeded
     */
    public BoundedStringBuilder append(String str) {
        sb.append(str);
        checkLength();
        return this;
    }

    /**
     * Appends the specified <tt>StringBuffer</tt> to this sequence.
     * <p>
     * The characters of the <tt>StringBuffer</tt> argument are appended,
     * in order, to the contents of this <tt>StringBuffer</tt>, increasing the
     * length of this <tt>StringBuffer</tt> by the length of the argument.
     * If <tt>sb</tt> is <tt>null</tt>, then the four characters
     * <tt>"null"</tt> are appended to this <tt>StringBuffer</tt>.
     * <p>
     * Let <i>n</i> be the length of the old character sequence, the one
     * contained in the <tt>StringBuffer</tt> just prior to execution of the
     * <tt>append</tt> method. Then the character at index <i>k</i> in
     * the new character sequence is equal to the character at index <i>k</i>
     * in the old character sequence, if <i>k</i> is less than <i>n</i>;
     * otherwise, it is equal to the character at index <i>k-n</i> in the
     * argument <code>sb</code>.
     * <p>
     * This method synchronizes on <code>this</code> (the destination)
     * object but does not synchronize on the source (<code>sb</code>).
     *
     * @param   sb   the <tt>StringBuffer</tt> to append.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(StringBuffer sb) {
        this.sb.append(sb);
        checkLength();
        return this;
    }

    /**
     * Returns the <code>char</code> value in this sequence at the specified index.
     * The first <code>char</code> value is at index <code>0</code>, the next at index
     * <code>1</code>, and so on, as in array indexing.
     * <p>
     * The index argument must be greater than or equal to
     * <code>0</code>, and less than the length of this sequence.
     *
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param      index   the index of the desired <code>char</code> value.
     * @return     the <code>char</code> value at the specified index.
     * @throws     IndexOutOfBoundsException  if <code>index</code> is
     *             negative or greater than or equal to <code>length()</code>.
     */
    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    /**
     * Appends the specified character sequence.
     *
     * @param  s The character sequence to append.
     * @return  A reference to this
     */
    public BoundedStringBuilder append(CharSequence s) {
        sb.append(s);
        checkLength();
        return this;
    }

    /**
     * Appends a subsequence of the specified {@code CharSequence} to this
     * sequence.
     * <p>
     * Characters of the argument {@code s}, starting at
     * index {@code start}, are appended, in order, to the contents of
     * this sequence up to the (exclusive) index {@code end}. The length
     * of this sequence is increased by the value of {@code end - start}.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to
     * execution of the {@code append} method. Then the character at
     * index <i>k</i> in this character sequence becomes equal to the
     * character at index <i>k</i> in this sequence, if <i>k</i> is less than
     * <i>n</i>; otherwise, it is equal to the character at index
     * <i>k+start-n</i> in the argument {@code s}.
     * <p>
     * If {@code s} is {@code null}, then this method appends
     * characters as if the s parameter was a sequence containing the four
     * characters {@code "null"}.
     *
     * @param   s the sequence to append.
     * @param   start   the starting index of the subsequence to be appended.
     * @param   end     the end index of the subsequence to be appended.
     * @return  a reference to this object.
     * @throws     IndexOutOfBoundsException if
     *             {@code start} is negative, or
     *             {@code start} is greater than {@code end} or
     *             {@code end} is greater than {@code s.length()}
     */
    public BoundedStringBuilder append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        checkLength();
        return this;
    }

    /**
     * @param index
     * @return
     * @see java.lang.AbstractStringBuilder#codePointAt(int)
     */
    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    /**
     * Appends the string representation of the {@code char} array
     * argument to this sequence.
     * <p>
     * The characters of the array argument are appended, in order, to
     * the contents of this sequence. The length of this sequence
     * increases by the length of the argument.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(char[])},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   str   the characters to be appended.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(char[] str) {
        sb.append(str);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of a subarray of the
     * {@code char} array argument to this sequence.
     * <p>
     * Characters of the {@code char} array {@code str}, starting at
     * index {@code offset}, are appended, in order, to the contents
     * of this sequence. The length of this sequence increases
     * by the value of {@code len}.
     * <p>
     * The overall effect is exactly as if the arguments were converted
     * to a string by the method {@link String#valueOf(char[],int,int)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   str      the characters to be appended.
     * @param   offset   the index of the first {@code char} to append.
     * @param   len      the number of {@code char}s to append.
     * @return  a reference to this object.
     * @throws IndexOutOfBoundsException
     *         if {@code offset < 0} or {@code len < 0}
     *         or {@code offset+len > str.length}
     */
    public BoundedStringBuilder append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code boolean}
     * argument to the sequence.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(boolean)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   b   a {@code boolean}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(boolean b) {
        sb.append(b);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code char}
     * argument to this sequence.
     * <p>
     * The argument is appended to the contents of this sequence.
     * The length of this sequence increases by {@code 1}.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(char)},
     * and the character in that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   c   a {@code char}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(char c) {
        sb.append(c);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code int}
     * argument to this sequence.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(int)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   i   an {@code int}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(int i) {
        sb.append(i);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code long}
     * argument to this sequence.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(long)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   l   a {@code long}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(long lng) {
        sb.append(lng);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code float}
     * argument to this sequence.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(float)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   f   a {@code float}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(float f) {
        sb.append(f);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code double}
     * argument to this sequence.
     * <p>
     * The overall effect is exactly as if the argument were converted
     * to a string by the method {@link String#valueOf(double)},
     * and the characters of that string were then
     * {@link #append(String) appended} to this character sequence.
     *
     * @param   d   a {@code double}.
     * @return  a reference to this object.
     */
    public BoundedStringBuilder append(double d) {
        sb.append(d);
        checkLength();
        return this;
    }

    /**
     * Appends the string representation of the {@code codePoint}
     * argument to this sequence.
     *
     * <p> The argument is appended to the contents of this sequence.
     * The length of this sequence increases by
     * {@link Character#charCount(int) Character.charCount(codePoint)}.
     *
     * <p> The overall effect is exactly as if the argument were
     * converted to a {@code char} array by the method
     * {@link Character#toChars(int)} and the character in that array
     * were then {@link #append(char[]) appended} to this character
     * sequence.
     *
     * @param   codePoint   a Unicode code point
     * @return  a reference to this object.
     * @exception IllegalArgumentException if the specified
     * {@code codePoint} isn't a valid Unicode code point
     */
    public BoundedStringBuilder appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        checkLength();
        return this;
    }

    /**
     * Removes the characters in a substring of this sequence.
     * The substring begins at the specified {@code start} and extends to
     * the character at index {@code end - 1} or to the end of the
     * sequence if no such character exists. If
     * {@code start} is equal to {@code end}, no changes are made.
     *
     * @param      start  The beginning index, inclusive.
     * @param      end    The ending index, exclusive.
     * @return     This object.
     * @throws     StringIndexOutOfBoundsException  if {@code start}
     *             is negative, greater than {@code length()}, or
     *             greater than {@code end}.
     */
    public BoundedStringBuilder delete(int start, int end) {
        sb.delete(start, end);
        return this;
    }

    /**
     * Returns the character (Unicode code point) before the specified
     * index. The index refers to <code>char</code> values
     * (Unicode code units) and ranges from <code>1</code> to {@link
     * #length()}.
     *
     * <p> If the <code>char</code> value at <code>(index - 1)</code>
     * is in the low-surrogate range, <code>(index - 2)</code> is not
     * negative, and the <code>char</code> value at <code>(index -
     * 2)</code> is in the high-surrogate range, then the
     * supplementary code point value of the surrogate pair is
     * returned. If the <code>char</code> value at <code>index -
     * 1</code> is an unpaired low-surrogate or a high-surrogate, the
     * surrogate value is returned.
     *
     * @param     index the index following the code point that should be returned
     * @return    the Unicode code point value before the given index.
     * @exception IndexOutOfBoundsException if the <code>index</code>
     *            argument is less than 1 or greater than the length
     *            of this sequence.
     */
    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    /**
     * Removes the <code>char</code> at the specified position in this
     * sequence. This sequence is shortened by one <code>char</code>.
     *
     * <p>Note: If the character at the given index is a supplementary
     * character, this method does not remove the entire character. If
     * correct handling of supplementary characters is required,
     * determine the number of <code>char</code>s to remove by calling
     * <code>Character.charCount(thisSequence.codePointAt(index))</code>,
     * where <code>thisSequence</code> is this sequence.
     *
     * @param       index  Index of <code>char</code> to remove
     * @return      This object.
     * @throws      StringIndexOutOfBoundsException  if the <code>index</code>
     *              is negative or greater than or equal to
     *              <code>length()</code>.
     */
    public BoundedStringBuilder deleteCharAt(int index) {
        sb.deleteCharAt(index);
        return this;
    }

    /**
     * @param start
     * @param end
     * @param str
     * @return
     * @see java.lang.StringBuilder#replace(int, int, java.lang.String)
     */
    public BoundedStringBuilder replace(int start, int end, String str) {
        sb.replace(start, end, str);
        return this;
    }

    /**
     * @param index
     * @param str
     * @param offset
     * @param len
     * @return
     * @see java.lang.StringBuilder#insert(int, char[], int, int)
     */
    public BoundedStringBuilder insert(int index, char[] str, int offset, int len) {
        sb.insert(index, str, offset, len);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param obj
     * @return
     * @see java.lang.StringBuilder#insert(int, java.lang.Object)
     */
    public BoundedStringBuilder insert(int offset, Object obj) {
        sb.insert(offset, obj);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param str
     * @return
     * @see java.lang.StringBuilder#insert(int, java.lang.String)
     */
    public BoundedStringBuilder insert(int offset, String str) {
        sb.insert(offset, str);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param str
     * @return
     * @see java.lang.StringBuilder#insert(int, char[])
     */
    public BoundedStringBuilder insert(int offset, char[] str) {
        sb.insert(offset, str);
        checkLength();
        return this;
    }

    /**
     * @param dstOffset
     * @param s
     * @return
     * @see java.lang.StringBuilder#insert(int, java.lang.CharSequence)
     */
    public BoundedStringBuilder insert(int dstOffset, CharSequence s) {
        sb.insert(dstOffset, s);
        checkLength();
        return this;
    }

    /**
     * @param beginIndex
     * @param endIndex
     * @return
     * @see java.lang.AbstractStringBuilder#codePointCount(int, int)
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return sb.codePointCount(beginIndex, endIndex);
    }

    /**
     * @param dstOffset
     * @param s
     * @param start
     * @param end
     * @return
     * @see java.lang.StringBuilder#insert(int, java.lang.CharSequence, int, int)
     */
    public BoundedStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        sb.insert(dstOffset, s, start, end);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param b
     * @return
     * @see java.lang.StringBuilder#insert(int, boolean)
     */
    public BoundedStringBuilder insert(int offset, boolean b) {
        sb.insert(offset, b);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param c
     * @return
     * @see java.lang.StringBuilder#insert(int, char)
     */
    public BoundedStringBuilder insert(int offset, char c) {
        sb.insert(offset, c);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param i
     * @return
     * @see java.lang.StringBuilder#insert(int, int)
     */
    public BoundedStringBuilder insert(int offset, int i) {
        sb.insert(offset, i);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param l
     * @return
     * @see java.lang.StringBuilder#insert(int, long)
     */
    public BoundedStringBuilder insert(int offset, long l) {
        sb.insert(offset, l);
        checkLength();
        return this;
    }

    /**
     * @param index
     * @param codePointOffset
     * @return
     * @see java.lang.AbstractStringBuilder#offsetByCodePoints(int, int)
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * @param offset
     * @param f
     * @return
     * @see java.lang.StringBuilder#insert(int, float)
     */
    public BoundedStringBuilder insert(int offset, float f) {
        sb.insert(offset, f);
        checkLength();
        return this;
    }

    /**
     * @param offset
     * @param d
     * @return
     * @see java.lang.StringBuilder#insert(int, double)
     */
    public BoundedStringBuilder insert(int offset, double d) {
        sb.insert(offset, d);
        checkLength();
        return this;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. The integer returned is the smallest value
     * <i>k</i> such that:
     * <blockquote><pre>
     * this.toString().startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * is <code>true</code>.
     *
     * @param   str   any string.
     * @return  if the string argument occurs as a substring within this
     *          object, then the index of the first character of the first
     *          such substring is returned; if it does not occur as a
     *          substring, <code>-1</code> is returned.
     * @throws  java.lang.NullPointerException if <code>str</code> is
     *          <code>null</code>.
     */
    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index.  The integer
     * returned is the smallest value <tt>k</tt> for which:
     * <blockquote><pre>
     *     k >= Math.min(fromIndex, this.length()) &&
     *                   this.toString().startsWith(str, k)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then -1 is returned.
     *
     * @param   str         the substring for which to search.
     * @param   fromIndex   the index from which to start the search.
     * @return  the index within this string of the first occurrence of the
     *          specified substring, starting at the specified index.
     * @throws  java.lang.NullPointerException if <code>str</code> is
     *            <code>null</code>.
     */
    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    /**
     * @param str
     * @return
     * @see java.lang.StringBuilder#lastIndexOf(java.lang.String)
     */
    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    /**
     * @param str
     * @param fromIndex
     * @return
     * @see java.lang.StringBuilder#lastIndexOf(java.lang.String, int)
     */
    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    /**
     * Causes this character sequence to be replaced by the reverse of
     * the sequence. If there are any surrogate pairs included in the
     * sequence, these are treated as single characters for the
     * reverse operation. Thus, the order of the high-low surrogates
     * is never reversed.
     *
     * Let <i>n</i> be the character length of this character sequence
     * (not the length in <code>char</code> values) just prior to
     * execution of the <code>reverse</code> method. Then the
     * character at index <i>k</i> in the new character sequence is
     * equal to the character at index <i>n-k-1</i> in the old
     * character sequence.
     *
     * <p>Note that the reverse operation may result in producing
     * surrogate pairs that were unpaired low-surrogates and
     * high-surrogates before the operation. For example, reversing
     * "&#92;uDC00&#92;uD800" produces "&#92;uD800&#92;uDC00" which is
     * a valid surrogate pair.
     *
     * @return  a reference to this object.
     */
    public StringBuilder reverse() {
        return sb.reverse();
    }

    /**
     * Characters are copied from this sequence into the
     * destination character array <code>dst</code>. The first character to
     * be copied is at index <code>srcBegin</code>; the last character to
     * be copied is at index <code>srcEnd-1</code>. The total number of
     * characters to be copied is <code>srcEnd-srcBegin</code>. The
     * characters are copied into the subarray of <code>dst</code> starting
     * at index <code>dstBegin</code> and ending at index:
     * <p><blockquote><pre>
     * dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   start copying at this offset.
     * @param      srcEnd     stop copying at this offset.
     * @param      dst        the array to copy the data into.
     * @param      dstBegin   offset into <code>dst</code>.
     * @throws     NullPointerException if <code>dst</code> is
     *             <code>null</code>.
     * @throws     IndexOutOfBoundsException  if any of the following is true:
     *             <ul>
     *             <li><code>srcBegin</code> is negative
     *             <li><code>dstBegin</code> is negative
     *             <li>the <code>srcBegin</code> argument is greater than
     *             the <code>srcEnd</code> argument.
     *             <li><code>srcEnd</code> is greater than
     *             <code>this.length()</code>.
     *             <li><code>dstBegin+srcEnd-srcBegin</code> is greater than
     *             <code>dst.length</code>
     *             </ul>
     */
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        sb.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * The character at the specified index is set to <code>ch</code>. This
     * sequence is altered to represent a new character sequence that is
     * identical to the old character sequence, except that it contains the
     * character <code>ch</code> at position <code>index</code>.
     * <p>
     * The index argument must be greater than or equal to
     * <code>0</code>, and less than the length of this sequence.
     *
     * @param      index   the index of the character to modify.
     * @param      ch      the new character.
     * @throws     IndexOutOfBoundsException  if <code>index</code> is
     *             negative or greater than or equal to <code>length()</code>.
     */
    public void setCharAt(int index, char ch) {
        sb.setCharAt(index, ch);
    }

    /**
     * Returns a new <code>String</code> that contains a subsequence of
     * characters currently contained in this character sequence. The
     * substring begins at the specified index and extends to the end of
     * this sequence.
     *
     * @param      start    The beginning index, inclusive.
     * @return     The new string.
     * @throws     StringIndexOutOfBoundsException  if <code>start</code> is
     *             less than zero, or greater than the length of this object.
     */
    public String substring(int start) {
        return sb.substring(start);
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * <p> An invocation of this method of the form
     *
     * <blockquote><pre>
     * sb.subSequence(begin,&nbsp;end)</pre></blockquote>
     *
     * behaves in exactly the same way as the invocation
     *
     * <blockquote><pre>
     * sb.substring(begin,&nbsp;end)</pre></blockquote>
     *
     * This method is provided so that this class can
     * implement the {@link CharSequence} interface. </p>
     *
     * @param      start   the start index, inclusive.
     * @param      end     the end index, exclusive.
     * @return     the specified subsequence.
     *
     * @throws  IndexOutOfBoundsException
     *          if <tt>start</tt> or <tt>end</tt> are negative,
     *          if <tt>end</tt> is greater than <tt>length()</tt>,
     *          or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    /**
     * Returns a new <code>String</code> that contains a subsequence of
     * characters currently contained in this sequence. The
     * substring begins at the specified <code>start</code> and
     * extends to the character at index <code>end - 1</code>.
     *
     * @param      start    The beginning index, inclusive.
     * @param      end      The ending index, exclusive.
     * @return     The new string.
     * @throws     StringIndexOutOfBoundsException  if <code>start</code>
     *             or <code>end</code> are negative or greater than
     *             <code>length()</code>, or <code>start</code> is
     *             greater than <code>end</code>.
     */
    public String substring(int start, int end) {
        return sb.substring(start, end);
    }

}
