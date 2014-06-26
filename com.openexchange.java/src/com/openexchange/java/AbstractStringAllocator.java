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

package com.openexchange.java;

import static java.lang.Character.MIN_HIGH_SURROGATE;
import static java.lang.Character.MIN_LOW_SURROGATE;
import static java.lang.Character.MIN_SUPPLEMENTARY_CODE_POINT;
import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import java.lang.reflect.Method;
import java.util.Arrays;
import com.openexchange.java.misc.FloatingDecimal;


/**
 * {@link AbstractStringBuilder} - A mutable sequence of characters.
 * <p>
 * Copied from <tt>java.lang.AbstractStringBuilder</tt> class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractStringBuilder implements Appendable, CharSequence {

    /**
     * The <code>getChars(char dst[], int dstBegin)</code> method that copies characters from associated string into <code>dst</code>
     * starting at <code>dstBegin</code>.
     */
    private static final Method getChars;
    static {
        Method method;
        try {
            method = String.class.getDeclaredMethod("getChars", char[].class, int.class);
            method.setAccessible(true);
        } catch (final SecurityException e) {
            method = null;
        } catch (final NoSuchMethodException e) {
            method = null;
        }
        getChars = method;
    }

    /**
     * The value is used for character storage.
     */
    char value[];

    /**
     * The count is the number of characters used.
     */
    int count;

    /**
     * Initializes a new {@link AbstractStringBuilder}.
     */
    protected AbstractStringBuilder() {
        super();
    }

    /**
     * Creates an AbstractStringBuilder of the specified capacity.
     */
    protected AbstractStringBuilder(final int capacity) {
        super();
        value = new char[capacity];
    }

    /**
     * Returns the length (character count).
     *
     * @return the length of the sequence of characters currently represented by this object
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage available for newly inserted characters, beyond which an
     * allocation will occur.
     *
     * @return the current capacity
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum. If the current capacity is less than the argument, then a new
     * internal array is allocated with greater capacity. The new capacity is the larger of:
     * <ul>
     * <li>The <code>minimumCapacity</code> argument.
     * <li>Twice the old capacity, plus <code>2</code>.
     * </ul>
     * If the <code>minimumCapacity</code> argument is nonpositive, this method takes no action and simply returns.
     *
     * @param minimumCapacity the minimum desired capacity.
     */
    public void ensureCapacity(final int minimumCapacity) {
        if (minimumCapacity > value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * This implements the expansion semantics of ensureCapacity with no size check or synchronization.
     */
    void expandCapacity(final int minimumCapacity) {
        int newCapacity = (value.length + 1) << 1;
        if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
        } else if (minimumCapacity > newCapacity) {
            newCapacity = minimumCapacity;
        }
        value = Arrays.copyOf(value, newCapacity);
    }

    /**
     * Attempts to reduce storage used for the character sequence. If the buffer is larger than necessary to hold its current sequence of
     * characters, then it may be resized to become more space efficient. Calling this method may, but is not required to, affect the value
     * returned by a subsequent call to the {@link #capacity()} method.
     */
    public void trimToSize() {
        if (count < value.length) {
            value = Arrays.copyOf(value, count);
        }
    }

    /**
     * Reinitializes to specified length.
     * <p>
     * Initializes a new <code>char[]</code> to not affect possibly previously created Strings via {@link #toString()} method.
     *
     * @param newLength The new length
     * @throws IndexOutOfBoundsException if the <code>newLength</code> argument is negative.
     */
    public void reinitTo(final int newLength) {
        if (newLength < 0) {
            throw new StringIndexOutOfBoundsException(newLength);
        }
        final char[] tmp = value;
        if (newLength <= tmp.length) {
            value = new char[tmp.length];
            if (newLength > 0) {
                System.arraycopy(tmp, 0, value, 0, newLength);
            }
        } else {
            expandCapacity(newLength);
        }
        count = newLength;
    }

    /**
     * Sets the length of the character sequence. The sequence is changed to a new character sequence whose length is specified by the
     * argument. For every nonnegative index <i>k</i> less than <code>newLength</code>, the character at index <i>k</i> in the new character
     * sequence is the same as the character at index <i>k</i> in the old sequence if <i>k</i> is less than the length of the old character
     * sequence; otherwise, it is the null character <code>'&#92;u0000'</code>. In other words, if the <code>newLength</code> argument is
     * less than the current length, the length is changed to the specified length.
     * <p>
     * If the <code>newLength</code> argument is greater than or equal to the current length, sufficient null characters (
     * <code>'&#92;u0000'</code>) are appended so that length becomes the <code>newLength</code> argument.
     * <p>
     * The <code>newLength</code> argument must be greater than or equal to <code>0</code>.
     *
     * @param newLength the new length
     * @throws IndexOutOfBoundsException if the <code>newLength</code> argument is negative.
     */
    public void setNewLength(int newLength) {
        if (newLength < 0) {
            throw new StringIndexOutOfBoundsException(newLength);
        }
        if (newLength > value.length) {
            expandCapacity(newLength);
        }

        if (count < newLength) {
            for (; count < newLength; count++) {
                value[count] = '\0';
            }
        } else {
            count = newLength;
        }
    }

    /**
     * Returns the <code>char</code> value in this sequence at the specified index. The first <code>char</code> value is at index
     * <code>0</code>, the next at index <code>1</code>, and so on, as in array indexing.
     * <p>
     * The index argument must be greater than or equal to <code>0</code>, and less than the length of this sequence.
     * <p>
     * If the <code>char</code> value specified by the index is a <a href="Character.html#unicode">surrogate</a>, the surrogate value is
     * returned.
     *
     * @param index the index of the desired <code>char</code> value.
     * @return the <code>char</code> value at the specified index.
     * @throws IndexOutOfBoundsException if <code>index</code> is negative or greater than or equal to <code>length()</code>.
     */
    @Override
    public char charAt(final int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    /**
     * Returns the character (Unicode code point) at the specified index. The index refers to <code>char</code> values (Unicode code units)
     * and ranges from <code>0</code> to {@link #length()}<code> - 1</code>.
     * <p>
     * If the <code>char</code> value specified at the given index is in the high-surrogate range, the following index is less than the
     * length of this sequence, and the <code>char</code> value at the following index is in the low-surrogate range, then the supplementary
     * code point corresponding to this surrogate pair is returned. Otherwise, the <code>char</code> value at the given index is returned.
     *
     * @param index the index to the <code>char</code> values
     * @return the code point value of the character at the <code>index</code>
     * @exception IndexOutOfBoundsException if the <code>index</code> argument is negative or not less than the length of this sequence.
     */
    public int codePointAt(final int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAt(value, index);
    }

    /**
     * Returns the character (Unicode code point) before the specified index. The index refers to <code>char</code> values (Unicode code
     * units) and ranges from <code>1</code> to {@link #length()}.
     * <p>
     * If the <code>char</code> value at <code>(index - 1)</code> is in the low-surrogate range, <code>(index - 2)</code> is not negative,
     * and the <code>char</code> value at <code>(index -
     * 2)</code> is in the high-surrogate range, then the supplementary code point value of the surrogate pair is returned. If the
     * <code>char</code> value at <code>index -
     * 1</code> is an unpaired low-surrogate or a high-surrogate, the surrogate value is returned.
     *
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @exception IndexOutOfBoundsException if the <code>index</code> argument is less than 1 or greater than the length of this sequence.
     */
    public int codePointBefore(final int index) {
        final int i = index - 1;
        if ((i < 0) || (i >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointBefore(value, index);
    }

    /**
     * Returns the number of Unicode code points in the specified text range of this sequence. The text range begins at the specified
     * <code>beginIndex</code> and extends to the <code>char</code> at index <code>endIndex - 1</code>. Thus the length (in
     * <code>char</code>s) of the text range is <code>endIndex-beginIndex</code>. Unpaired surrogates within this sequence count as one code
     * point each.
     *
     * @param beginIndex the index to the first <code>char</code> of the text range.
     * @param endIndex the index after the last <code>char</code> of the text range.
     * @return the number of Unicode code points in the specified text range
     * @exception IndexOutOfBoundsException if the <code>beginIndex</code> is negative, or <code>endIndex</code> is larger than the length
     *                of this sequence, or <code>beginIndex</code> is larger than <code>endIndex</code>.
     */
    public int codePointCount(final int beginIndex, final int endIndex) {
        if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        return codePointCountImpl(value, beginIndex, endIndex - beginIndex);
    }

    static int codePointCountImpl(final char[] a, final int offset, final int count) {
        final int endIndex = offset + count;
        int n = 0;
        for (int i = offset; i < endIndex;) {
            n++;
            if (isHighSurrogate(a[i++])) {
                if (i < endIndex && isLowSurrogate(a[i])) {
                    i++;
                }
            }
        }
        return n;
    }

    /**
     * Returns the index within this sequence that is offset from the given <code>index</code> by <code>codePointOffset</code> code points.
     * Unpaired surrogates within the text range given by <code>index</code> and <code>codePointOffset</code> count as one code point each.
     *
     * @param index the index to be offset
     * @param codePointOffset the offset in code points
     * @return the index within this sequence
     * @exception IndexOutOfBoundsException if <code>index</code> is negative or larger then the length of this sequence, or if
     *                <code>codePointOffset</code> is positive and the subsequence starting with <code>index</code> has fewer than
     *                <code>codePointOffset</code> code points, or if <code>codePointOffset</code> is negative and the subsequence before
     *                <code>index</code> has fewer than the absolute value of <code>codePointOffset</code> code points.
     */
    public int offsetByCodePoints(final int index, final int codePointOffset) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException();
        }
        return offsetByCodePointsImpl(value, 0, count, index, codePointOffset);
    }

    static int offsetByCodePointsImpl(final char[] a, final int start, final int count, final int index, final int codePointOffset) {
        int x = index;
        if (codePointOffset >= 0) {
            final int limit = start + count;
            int i;
            for (i = 0; x < limit && i < codePointOffset; i++) {
                if (isHighSurrogate(a[x++])) {
                    if (x < limit && isLowSurrogate(a[x])) {
                        x++;
                    }
                }
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i;
            for (i = codePointOffset; x > start && i < 0; i++) {
                if (isLowSurrogate(a[--x])) {
                    if (x > start && isHighSurrogate(a[x - 1])) {
                        x--;
                    }
                }
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    /**
     * Characters are copied from this sequence into the destination character array <code>dst</code>. The first character to be copied is
     * at index <code>srcBegin</code>; the last character to be copied is at index <code>srcEnd-1</code>. The total number of characters to
     * be copied is <code>srcEnd-srcBegin</code>. The characters are copied into the subarray of <code>dst</code> starting at index
     * <code>dstBegin</code> and ending at index:
     * <p>
     * <blockquote>
     *
     * <pre>
     * dstbegin + (srcEnd - srcBegin) - 1
     * </pre>
     *
     * </blockquote>
     *
     * @param srcBegin start copying at this offset.
     * @param srcEnd stop copying at this offset.
     * @param dst the array to copy the data into.
     * @param dstBegin offset into <code>dst</code>.
     * @throws NullPointerException if <code>dst</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException if any of the following is true:
     *             <ul>
     *             <li><code>srcBegin</code> is negative <li><code>dstBegin</code> is negative <li>the <code>srcBegin</code> argument is
     *             greater than the <code>srcEnd</code> argument. <li><code>srcEnd</code> is greater than <code>this.length()</code>. <li>
     *             <code>dstBegin+srcEnd-srcBegin</code> is greater than <code>dst.length</code>
     *             </ul>
     */
    public void getChars(final int srcBegin, final int srcEnd, final char dst[], final int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if ((srcEnd < 0) || (srcEnd > count)) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        }
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * The character at the specified index is set to <code>ch</code>. This sequence is altered to represent a new character sequence that
     * is identical to the old character sequence, except that it contains the character <code>ch</code> at position <code>index</code>.
     * <p>
     * The index argument must be greater than or equal to <code>0</code>, and less than the length of this sequence.
     *
     * @param index the index of the character to modify.
     * @param ch the new character.
     * @throws IndexOutOfBoundsException if <code>index</code> is negative or greater than or equal to <code>length()</code>.
     */
    public void setCharAt(final int index, final char ch) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        value[index] = ch;
    }

    /**
     * Appends the string representation of the <code>Object</code> argument.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param obj an <code>Object</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the specified string to this character sequence.
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
    public AbstractStringBuilder append(String str) {
        if (str == null) {
            str = "null";
        }
        final int len = str.length();
        if (len == 0) {
            return this;
        }
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        str.getChars(0, len, value, count);
        count = newCount;
        return this;
    }

    // Documentation in subclasses because of synchro difference
    public AbstractStringBuilder append(final StringBuffer sb) {
        if (sb == null) {
            return append("null");
        }
        final int len = sb.length();
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        sb.getChars(0, len, value, count);
        count = newCount;
        return this;
    }

    // Documentation in subclasses because of synchro difference
    @Override
    public AbstractStringBuilder append(CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return this.append((String) s);
        }
        if (s instanceof StringBuffer) {
            return this.append((StringBuffer) s);
        }
        return this.append(s, 0, s.length());
    }

    /**
     * Appends a subsequence of the specified <code>CharSequence</code> to this sequence.
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
    @Override
    public AbstractStringBuilder append(CharSequence s, final int start, final int end) {
        if (s == null) {
            s = "null";
        }
        if ((start < 0) || (end < 0) || (start > end) || (end > s.length())) {
            throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
        }
        final int len = end - start;
        if (len == 0) {
            return this;
        }
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        for (int i = start; i < end; i++) {
            value[count++] = s.charAt(i);
        }
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> array argument to this sequence.
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
    public AbstractStringBuilder append(final char str[]) {
        final int newCount = count + str.length;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(str, 0, value, count, str.length);
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of a subarray of the <code>char</code> array argument to this sequence.
     * <p>
     * Characters of the <code>char</code> array <code>str</code>, starting at index <code>offset</code>, are appended, in order, to the
     * contents of this sequence. The length of this sequence increases by the value of <code>len</code>.
     * <p>
     * The overall effect is exactly as if the arguments were converted to a string by the method {@link String#valueOf(char[],int,int)} and
     * the characters of that string were then {@link #append(String) appended} to this character sequence.
     *
     * @param str the characters to be appended.
     * @param offset the index of the first <code>char</code> to append.
     * @param len the number of <code>char</code>s to append.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final char str[], final int offset, final int len) {
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(str, offset, value, count, len);
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of the <code>boolean</code> argument to the sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param b a <code>boolean</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final boolean b) {
        if (b) {
            final int newCount = count + 4;
            if (newCount > value.length) {
                expandCapacity(newCount);
            }
            value[count++] = 't';
            value[count++] = 'r';
            value[count++] = 'u';
            value[count++] = 'e';
        } else {
            final int newCount = count + 5;
            if (newCount > value.length) {
                expandCapacity(newCount);
            }
            value[count++] = 'f';
            value[count++] = 'a';
            value[count++] = 'l';
            value[count++] = 's';
            value[count++] = 'e';
        }
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> argument to this sequence.
     * <p>
     * The argument is appended to the contents of this sequence. The length of this sequence increases by <code>1</code>.
     * <p>
     * The overall effect is exactly as if the argument were converted to a string by the method {@link String#valueOf(char)} and the
     * character in that string were then {@link #append(String) appended} to this character sequence.
     *
     * @param c a <code>char</code>.
     * @return a reference to this object.
     */
    @Override
    public AbstractStringBuilder append(final char c) {
        final int newCount = count + 1;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        value[count++] = c;
        return this;
    }

    /**
     * Appends the string representation of the <code>int</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param i an <code>int</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final int i) {
        if (i == Integer.MIN_VALUE) {
            append("-2147483648");
            return this;
        }
        final int appendedLength = (i < 0) ? stringSizeOfInt(-i) + 1 : stringSizeOfInt(i);
        final int spaceNeeded = count + appendedLength;
        if (spaceNeeded > value.length) {
            expandCapacity(spaceNeeded);
        }
        getChars(i, spaceNeeded, value);
        count = spaceNeeded;
        return this;
    }

    /**
     * All possible chars for representing a number as a String
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
        };

    final static char [] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        };

    final static char [] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };

    /**
     * Places characters representing the integer i into the character array buf. The characters are placed into the buffer backwards
     * starting with the least significant digit at the specified index (exclusive), and working backwards from there. Will fail if i ==
     * Integer.MIN_VALUE
     */
    static void getChars(int i, final int index, final char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
                                     99999999, 999999999, Integer.MAX_VALUE };

    // Requires positive x
    static int stringSizeOfInt(final int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    /**
     * Appends the string representation of the <code>long</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param l a <code>long</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final long l) {
        if (l == Long.MIN_VALUE) {
            append("-9223372036854775808");
            return this;
        }
        final int appendedLength = (l < 0) ? stringSizeOfLong(-l) + 1 : stringSizeOfLong(l);
        final int spaceNeeded = count + appendedLength;
        if (spaceNeeded > value.length) {
            expandCapacity(spaceNeeded);
        }
        getChars(l, spaceNeeded, value);
        count = spaceNeeded;
        return this;
    }

    /**
     * Places characters representing the integer i into the character array buf. The characters are placed into the buffer backwards
     * starting with the least significant digit at the specified index (exclusive), and working backwards from there. Will fail if i ==
     * Long.MIN_VALUE
     */
    static void getChars(long i, final int index, final char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    // Requires positive x
    static int stringSizeOfLong(final long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    /**
     * Appends the string representation of the <code>float</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this string sequence.
     *
     * @param f a <code>float</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final float f) {
        new FloatingDecimal(f).appendTo(this);
        return this;
    }

    /**
     * Appends the string representation of the <code>double</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param d a <code>double</code>.
     * @return a reference to this object.
     */
    public AbstractStringBuilder append(final double d) {
        new FloatingDecimal(d).appendTo(this);
        return this;
    }

    /**
     * Removes the characters in a substring of this sequence. The substring begins at the specified <code>start</code> and extends to the
     * character at index <code>end - 1</code> or to the end of the sequence if no such character exists. If <code>start</code> is equal to
     * <code>end</code>, no changes are made.
     *
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive.
     * @return This object.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is negative, greater than <code>length()</code>, or greater than
     *             <code>end</code>.
     */
    public AbstractStringBuilder delete(final int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > count) {
            end = count;
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException();
        }
        final int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, count - end);
            count -= len;
        }
        return this;
    }

    /**
     * Appends the string representation of the <code>codePoint</code> argument to this sequence.
     * <p>
     * The argument is appended to the contents of this sequence. The length of this sequence increases by {@link Character#charCount(int)
     * Character.charCount(codePoint)}.
     * <p>
     * The overall effect is exactly as if the argument were converted to a <code>char</code> array by the method
     * {@link Character#toChars(int)} and the character in that array were then {@link #append(char[]) appended} to this character sequence.
     *
     * @param codePoint a Unicode code point
     * @return a reference to this object.
     * @exception IllegalArgumentException if the specified <code>codePoint</code> isn't a valid Unicode code point
     */
    public AbstractStringBuilder appendCodePoint(final int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException();
        }
        int n = 1;
        if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            n++;
        }
        final int newCount = count + n;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        if (n == 1) {
            value[count++] = (char) codePoint;
        } else {
            toSurrogates(codePoint, value, count);
            count += n;
        }
        return this;
    }

    static void toSurrogates(final int codePoint, final char[] dst, final int index) {
        final int offset = codePoint - MIN_SUPPLEMENTARY_CODE_POINT;
        dst[index + 1] = (char) ((offset & 0x3ff) + MIN_LOW_SURROGATE);
        dst[index] = (char) ((offset >>> 10) + MIN_HIGH_SURROGATE);
    }

    /**
     * Removes the <code>char</code> at the specified position in this sequence. This sequence is shortened by one <code>char</code>.
     * <p>
     * Note: If the character at the given index is a supplementary character, this method does not remove the entire character. If correct
     * handling of supplementary characters is required, determine the number of <code>char</code>s to remove by calling
     * <code>Character.charCount(thisSequence.codePointAt(index))</code>, where <code>thisSequence</code> is this sequence.
     *
     * @param index Index of <code>char</code> to remove
     * @return This object.
     * @throws StringIndexOutOfBoundsException if the <code>index</code> is negative or greater than or equal to <code>length()</code>.
     */
    public AbstractStringBuilder deleteCharAt(final int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        System.arraycopy(value, index + 1, value, index, count - index - 1);
        count--;
        return this;
    }

    /**
     * Removes the last <code>char</code> in this sequence. This sequence is shortened by one <code>char</code>.
     * <p>
     * Note: If the character at the given index is a supplementary character, this method does not remove the entire character. If correct
     * handling of supplementary characters is required, determine the number of <code>char</code>s to remove by calling
     * <code>Character.charCount(thisSequence.codePointAt(index))</code>, where <code>thisSequence</code> is this sequence.
     *
     * @return This object.
     * @throws StringIndexOutOfBoundsException if the this sequence is empty
     */
    public AbstractStringBuilder deleteLastChar() {
        return deleteCharAt(count - 1);
    }

    /**
     * Replaces the characters in a substring of this sequence with characters in the specified <code>String</code>. The substring begins at
     * the specified <code>start</code> and extends to the character at index <code>end - 1</code> or to the end of the sequence if no such
     * character exists. First the characters in the substring are removed and then the specified <code>String</code> is inserted at
     * <code>start</code>. (This sequence will be lengthened to accommodate the specified String if necessary.)
     *
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive.
     * @param str String that will replace previous contents.
     * @return This object.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is negative, greater than <code>length()</code>, or greater than
     *             <code>end</code>.
     */
    public AbstractStringBuilder replace(final int start, int end, final String str) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (start > count) {
            throw new StringIndexOutOfBoundsException("start > length()");
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException("start > end");
        }

        if (end > count) {
            end = count;
        }
        final int len = str.length();
        final int newCount = count + len - (end - start);
        if (newCount > value.length) {
            expandCapacity(newCount);
        }

        System.arraycopy(value, end, value, start + len, count - end);
        getChars(str, start);
        count = newCount;
        return this;
    }

    private void getChars(final String str, final int start) {
        try {
            getChars.invoke(str, value, Integer.valueOf(start));
        } catch (final Exception e) {
            final char[] charArray = str.toCharArray();
            System.arraycopy(charArray, 0, value, start, charArray.length);
        }
    }

    /**
     * Returns a new <code>String</code> that contains a subsequence of characters currently contained in this character sequence. The
     * substring begins at the specified index and extends to the end of this sequence.
     *
     * @param start The beginning index, inclusive.
     * @return The new string.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is less than zero, or greater than the length of this object.
     */
    public String substring(final int start) {
        return substring(start, count);
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     * <p>
     * An invocation of this method of the form <blockquote>
     *
     * <pre>
     * sb.subSequence(begin, end)
     * </pre>
     *
     * </blockquote> behaves in exactly the same way as the invocation <blockquote>
     *
     * <pre>
     * sb.substring(begin, end)
     * </pre>
     *
     * </blockquote> This method is provided so that this class can implement the {@link CharSequence} interface.
     * </p>
     *
     * @param start the start index, inclusive.
     * @param end the end index, exclusive.
     * @return the specified subsequence.
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative, if <tt>end</tt> is greater than <tt>length()</tt>,
     *             or if <tt>start</tt> is greater than <tt>end</tt>
     * @spec JSR-51
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return substring(start, end);
    }

    /**
     * Returns a new <code>String</code> that contains a subsequence of characters currently contained in this sequence. The substring
     * begins at the specified <code>start</code> and extends to the character at index <code>end - 1</code>.
     *
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive.
     * @return The new string.
     * @throws StringIndexOutOfBoundsException if <code>start</code> or <code>end</code> are negative or greater than <code>length()</code>,
     *             or <code>start</code> is greater than <code>end</code>.
     */
    public String substring(final int start, final int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > count) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException(end - start);
        }
        return new String(value, start, end - start);
    }

    /**
     * Inserts the string representation of a subarray of the <code>str</code> array argument into this sequence. The subarray begins at the
     * specified <code>offset</code> and extends <code>len</code> <code>char</code>s. The characters of the subarray are inserted into this
     * sequence at the position indicated by <code>index</code>. The length of this sequence increases by <code>len</code> <code>char</code>
     * s.
     *
     * @param index position at which to insert subarray.
     * @param str A <code>char</code> array.
     * @param offset the index of the first <code>char</code> in subarray to be inserted.
     * @param len the number of <code>char</code>s in the subarray to be inserted.
     * @return This object
     * @throws StringIndexOutOfBoundsException if <code>index</code> is negative or greater than <code>length()</code>, or
     *             <code>offset</code> or <code>len</code> are negative, or <code>(offset+len)</code> is greater than
     *             <code>str.length</code>.
     */
    public AbstractStringBuilder insert(final int index, final char str[], final int offset, final int len) {
        if ((index < 0) || (index > length())) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if ((offset < 0) || (len < 0) || (offset > str.length - len)) {
            throw new StringIndexOutOfBoundsException("offset " + offset + ", len " + len + ", str.length " + str.length);
        }
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(value, index, value, index + len, count - index);
        System.arraycopy(str, offset, value, index, len);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>Object</code> argument into this character sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the indicated offset.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param obj an <code>Object</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * Inserts the string into this character sequence.
     * <p>
     * The characters of the <code>String</code> argument are inserted, in order, into this sequence at the indicated offset, moving up any
     * characters originally above that position and increasing the length of this sequence by the length of the argument. If
     * <code>str</code> is <code>null</code>, then the four characters <code>"null"</code> are inserted into this sequence.
     * <p>
     * The character at index <i>k</i> in the new character sequence is equal to:
     * <ul>
     * <li>the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than <code>offset</code>
     * <li>the character at index <i>k</i><code>-offset</code> in the argument <code>str</code>, if <i>k</i> is not less than
     * <code>offset</code> but is less than <code>offset+str.length()</code>
     * <li>the character at index <i>k</i><code>-str.length()</code> in the old character sequence, if <i>k</i> is not less than
     * <code>offset+str.length()</code>
     * </ul>
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param str a string.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, String str) {
        if ((offset < 0) || (offset > length())) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (str == null) {
            str = "null";
        }
        final int len = str.length();
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(value, offset, value, offset + len, count - offset);
        getChars(str, offset);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>char</code> array argument into this sequence.
     * <p>
     * The characters of the array argument are inserted into the contents of this sequence at the position indicated by <code>offset</code>
     * . The length of this sequence increases by the length of the argument.
     * <p>
     * The overall effect is exactly as if the argument were converted to a string by the method {@link String#valueOf(char[])} and the
     * characters of that string were then {@link #insert(int,String) inserted} into this character sequence at the position indicated by
     * <code>offset</code>.
     *
     * @param offset the offset.
     * @param str a character array.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final char str[]) {
        if ((offset < 0) || (offset > length())) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        final int len = str.length;
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(value, offset, value, offset + len, count - offset);
        System.arraycopy(str, 0, value, offset, len);
        count = newCount;
        return this;
    }

    /**
     * Inserts the specified <code>CharSequence</code> into this sequence.
     * <p>
     * The characters of the <code>CharSequence</code> argument are inserted, in order, into this sequence at the indicated offset, moving
     * up any characters originally above that position and increasing the length of this sequence by the length of the argument s.
     * <p>
     * The result of this method is exactly the same as if it were an invocation of this object's insert(dstOffset, s, 0, s.length())
     * method.
     * <p>
     * If <code>s</code> is <code>null</code>, then the four characters <code>"null"</code> are inserted into this sequence.
     *
     * @param dstOffset the offset.
     * @param s the sequence to be inserted
     * @return a reference to this object.
     * @throws IndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int dstOffset, CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return this.insert(dstOffset, (String) s);
        }
        return this.insert(dstOffset, s, 0, s.length());
    }

    /**
     * Inserts a subsequence of the specified <code>CharSequence</code> into this sequence.
     * <p>
     * The subsequence of the argument <code>s</code> specified by <code>start</code> and <code>end</code> are inserted, in order, into this
     * sequence at the specified destination offset, moving up any characters originally above that position. The length of this sequence is
     * increased by <code>end - start</code>.
     * <p>
     * The character at index <i>k</i> in this sequence becomes equal to:
     * <ul>
     * <li>the character at index <i>k</i> in this sequence, if <i>k</i> is less than <code>dstOffset</code>
     * <li>the character at index <i>k</i><code>+start-dstOffset</code> in the argument <code>s</code>, if <i>k</i> is greater than or equal
     * to <code>dstOffset</code> but is less than <code>dstOffset+end-start</code>
     * <li>the character at index <i>k</i><code>-(end-start)</code> in this sequence, if <i>k</i> is greater than or equal to
     * <code>dstOffset+end-start</code>
     * </ul>
     * <p>
     * The dstOffset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     * <p>
     * The start argument must be nonnegative, and not greater than <code>end</code>.
     * <p>
     * The end argument must be greater than or equal to <code>start</code>, and less than or equal to the length of s.
     * <p>
     * If <code>s</code> is <code>null</code>, then this method inserts characters as if the s parameter was a sequence containing the four
     * characters <code>"null"</code>.
     *
     * @param dstOffset the offset in this sequence.
     * @param s the sequence to be inserted.
     * @param start the starting index of the subsequence to be inserted.
     * @param end the end index of the subsequence to be inserted.
     * @return a reference to this object.
     * @throws IndexOutOfBoundsException if <code>dstOffset</code> is negative or greater than <code>this.length()</code>, or
     *             <code>start</code> or <code>end</code> are negative, or <code>start</code> is greater than <code>end</code> or
     *             <code>end</code> is greater than <code>s.length()</code>
     */
    public AbstractStringBuilder insert(int dstOffset, CharSequence s, final int start, final int end) {
        if (s == null) {
            s = "null";
        }
        if ((dstOffset < 0) || (dstOffset > this.length())) {
            throw new IndexOutOfBoundsException("dstOffset " + dstOffset);
        }
        if ((start < 0) || (end < 0) || (start > end) || (end > s.length())) {
            throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
        }
        final int len = end - start;
        if (len == 0) {
            return this;
        }
        final int newCount = count + len;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(value, dstOffset, value, dstOffset + len, count - dstOffset);
        for (int i = start; i < end; i++) {
            value[dstOffset++] = s.charAt(i);
        }
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>boolean</code> argument into this sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the indicated offset.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param b a <code>boolean</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final boolean b) {
        return insert(offset, String.valueOf(b));
    }

    /**
     * Inserts the string representation of the <code>char</code> argument into this sequence.
     * <p>
     * The second argument is inserted into the contents of this sequence at the position indicated by <code>offset</code>. The length of
     * this sequence increases by one.
     * <p>
     * The overall effect is exactly as if the argument were converted to a string by the method {@link String#valueOf(char)} and the
     * character in that string were then {@link #insert(int, String) inserted} into this character sequence at the position indicated by
     * <code>offset</code>.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param c a <code>char</code>.
     * @return a reference to this object.
     * @throws IndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final char c) {
        final int newCount = count + 1;
        if (newCount > value.length) {
            expandCapacity(newCount);
        }
        System.arraycopy(value, offset, value, offset + 1, count - offset);
        value[offset] = c;
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the second <code>int</code> argument into this sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the indicated offset.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param i an <code>int</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final int i) {
        return insert(offset, String.valueOf(i));
    }

    /**
     * Inserts the string representation of the <code>long</code> argument into this sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the position indicated by <code>offset</code>.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param l a <code>long</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final long l) {
        return insert(offset, String.valueOf(l));
    }

    /**
     * Inserts the string representation of the <code>float</code> argument into this sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the indicated offset.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param f a <code>float</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final float f) {
        return insert(offset, String.valueOf(f));
    }

    /**
     * Inserts the string representation of the <code>double</code> argument into this sequence.
     * <p>
     * The second argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are
     * then inserted into this sequence at the indicated offset.
     * <p>
     * The offset argument must be greater than or equal to <code>0</code>, and less than or equal to the length of this sequence.
     *
     * @param offset the offset.
     * @param d a <code>double</code>.
     * @return a reference to this object.
     * @throws StringIndexOutOfBoundsException if the offset is invalid.
     */
    public AbstractStringBuilder insert(final int offset, final double d) {
        return insert(offset, String.valueOf(d));
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring. The integer returned is the smallest value
     * <i>k</i> such that: <blockquote>
     *
     * <pre>
     * this.toString().startsWith(str, <i>k</i>)
     * </pre>
     *
     * </blockquote> is <code>true</code>.
     *
     * @param str any string.
     * @return if the string argument occurs as a substring within this object, then the index of the first character of the first such
     *         substring is returned; if it does not occur as a substring, <code>-1</code> is returned.
     * @throws java.lang.NullPointerException if <code>str</code> is <code>null</code>.
     */
    public int indexOf(final String str) {
        return indexOf(str, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring, starting at the specified index. The integer
     * returned is the smallest value <tt>k</tt> for which: <blockquote>
     *
     * <pre>
     * k &gt;= Math.min(fromIndex, str.length()) &amp;&amp; this.toString().startsWith(str, k)
     * </pre>
     *
     * </blockquote> If no such value of <i>k</i> exists, then -1 is returned.
     *
     * @param str the substring for which to search.
     * @param fromIndex the index from which to start the search.
     * @return the index within this string of the first occurrence of the specified substring, starting at the specified index.
     * @throws java.lang.NullPointerException if <code>str</code> is <code>null</code>.
     */
    public int indexOf(final String str, final int fromIndex) {
        return indexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    /**
     * Code shared by String and StringBuffer to do searches. The source is the character array being searched, and the target is the string
     * being searched for.
     *
     * @param source the characters being searched.
     * @param sourceOffset offset of the source string.
     * @param sourceCount count of the source string.
     * @param target the characters being searched for.
     * @param targetOffset offset of the target string.
     * @param targetCount count of the target string.
     * @param fromIndex the index to begin searching from.
     */
    static int indexOf(final char[] source, final int sourceOffset, final int sourceCount, final char[] target, final int targetOffset, final int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        final char first = target[targetOffset];
        final int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) {
                    ;
                }
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                final int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++) {
                    ;
                }

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index within this string of the rightmost occurrence of the specified substring. The rightmost empty string "" is
     * considered to occur at the index value <code>this.length()</code>. The returned index is the largest value <i>k</i> such that
     * <blockquote>
     *
     * <pre>
     * this.toString().startsWith(str, k)
     * </pre>
     *
     * </blockquote> is true.
     *
     * @param str the substring to search for.
     * @return if the string argument occurs one or more times as a substring within this object, then the index of the first character of
     *         the last such substring is returned. If it does not occur as a substring, <code>-1</code> is returned.
     * @throws java.lang.NullPointerException if <code>str</code> is <code>null</code>.
     */
    public int lastIndexOf(final String str) {
        return lastIndexOf(str, count);
    }

    /**
     * Returns the index within this string of the last occurrence of the specified substring. The integer returned is the largest value
     * <i>k</i> such that: <blockquote>
     *
     * <pre>
     * k &lt;= Math.min(fromIndex, str.length()) &amp;&amp; this.toString().startsWith(str, k)
     * </pre>
     *
     * </blockquote> If no such value of <i>k</i> exists, then -1 is returned.
     *
     * @param str the substring to search for.
     * @param fromIndex the index to start the search from.
     * @return the index within this sequence of the last occurrence of the specified substring.
     * @throws java.lang.NullPointerException if <code>str</code> is <code>null</code>.
     */
    public int lastIndexOf(final String str, final int fromIndex) {
        return lastIndexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    /**
     * Code shared by String and StringBuffer to do searches. The source is the character array being searched, and the target is the string
     * being searched for.
     *
     * @param source the characters being searched.
     * @param sourceOffset offset of the source string.
     * @param sourceCount count of the source string.
     * @param target the characters being searched for.
     * @param targetOffset offset of the target string.
     * @param targetCount count of the target string.
     * @param fromIndex the index to begin searching from.
     */
    static int lastIndexOf(final char[] source, final int sourceOffset, final int sourceCount, final char[] target, final int targetOffset, final int targetCount, int fromIndex) {
        /*
         * Check arguments; return immediately where possible. For consistency, don't check for null str.
         */
        final int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* Empty string always matches. */
        if (targetCount == 0) {
            return fromIndex;
        }

        final int strLastIndex = targetOffset + targetCount - 1;
        final char strLastChar = target[strLastIndex];
        final int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar: while (true) {
            while (i >= min && source[i] != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            final int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start) {
                if (source[j--] != target[k--]) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }

    /**
     * Causes this character sequence to be replaced by the reverse of the sequence. If there are any surrogate pairs included in the
     * sequence, these are treated as single characters for the reverse operation. Thus, the order of the high-low surrogates is never
     * reversed. Let <i>n</i> be the character length of this character sequence (not the length in <code>char</code> values) just prior to
     * execution of the <code>reverse</code> method. Then the character at index <i>k</i> in the new character sequence is equal to the
     * character at index <i>n-k-1</i> in the old character sequence.
     * <p>
     * Note that the reverse operation may result in producing surrogate pairs that were unpaired low-surrogates and high-surrogates before
     * the operation. For example, reversing "&#92;uDC00&#92;uD800" produces "&#92;uD800&#92;uDC00" which is a valid surrogate pair.
     *
     * @return a reference to this object.
     */
    public AbstractStringBuilder reverse() {
        boolean hasSurrogate = false;
        final int n = count - 1;
        for (int j = (n - 1) >> 1; j >= 0; --j) {
            final char temp = value[j];
            final char temp2 = value[n - j];
            if (!hasSurrogate) {
                hasSurrogate =
                    (temp >= Character.MIN_SURROGATE && temp <= Character.MAX_SURROGATE) || (temp2 >= Character.MIN_SURROGATE && temp2 <= Character.MAX_SURROGATE);
            }
            value[j] = temp2;
            value[n - j] = temp;
        }
        if (hasSurrogate) {
            // Reverse back all valid surrogate pairs
            for (int i = 0; i < count - 1; i++) {
                final char c2 = value[i];
                if (Character.isLowSurrogate(c2)) {
                    final char c1 = value[i + 1];
                    if (Character.isHighSurrogate(c1)) {
                        value[i++] = c1;
                        value[i] = c2;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Returns a string representing the data in this sequence. A new <code>String</code> object is allocated and initialized to contain the
     * character sequence currently represented by this object. This <code>String</code> is then returned. Subsequent changes to this
     * sequence do not affect the contents of the <code>String</code>.
     *
     * @return a string representation of this sequence of characters.
     */
    @Override
    public abstract String toString();

    /**
     * Needed by <tt>String</tt> for the contentEquals method.
     */
    final char[] getValue() {
        return value;
    }

}
