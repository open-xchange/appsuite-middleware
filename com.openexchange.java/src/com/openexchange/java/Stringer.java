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
 * {@link Stringer} - Intermediary for {@link StringBuffer} and {@link StringBuilder}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Stringer extends CharSequence, Appendable, java.io.Serializable {

    /**
     * Checks if this Stringer's content is empty, that is length is lower than 1 or does only contain white-space characters.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty();

    /**
     * Returns the length (character count).
     *
     * @return the length of the sequence of characters currently represented by this object
     */
    @Override
    public int length();

    /**
     * Returns the current capacity. The capacity is the amount of storage available for newly inserted characters, beyond which an
     * allocation will occur.
     *
     * @return the current capacity
     */
    public int capacity();

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
    public void ensureCapacity(int minimumCapacity);

    /**
     * Attempts to reduce storage used for the character sequence. If the buffer is larger than necessary to hold its current sequence of
     * characters, then it may be resized to become more space efficient. Calling this method may, but is not required to, affect the value
     * returned by a subsequent call to the {@link #capacity()} method.
     */
    public void trimToSize();

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
    public void setLength(int newLength);

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
    public char charAt(int index);

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
    public int codePointAt(int index);

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
    public int codePointBefore(int index);

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
    public int codePointCount(int beginIndex, int endIndex);

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
    public int offsetByCodePoints(int index, int codePointOffset);

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
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin);

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
    public void setCharAt(int index, char ch);

    /**
     * Appends the string representation of the <code>Object</code> argument.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param obj an <code>Object</code>.
     * @return a reference to this object.
     */
    public Stringer append(Object obj);

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
    public Stringer append(String str);

    // Documentation in subclasses because of synchro difference
    public Stringer append(StringBuilder sb);

    // Documentation in subclasses because of synchro difference
    public Stringer append(StringBuffer sb);

    // Documentation in subclasses because of synchro difference
    @Override
    public Stringer append(CharSequence s);

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
    public Stringer append(CharSequence s, int start, int end);

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
    public Stringer append(char str[]);

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
    public Stringer append(char str[], int offset, int len);

    /**
     * Appends the string representation of the <code>boolean</code> argument to the sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param b a <code>boolean</code>.
     * @return a reference to this object.
     */
    public Stringer append(boolean b);

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
    public Stringer append(char c);

    /**
     * Appends the string representation of the <code>int</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param i an <code>int</code>.
     * @return a reference to this object.
     */
    public Stringer append(int i);

    /**
     * Appends the string representation of the <code>long</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param l a <code>long</code>.
     * @return a reference to this object.
     */
    public Stringer append(long l);

    /**
     * Appends the string representation of the <code>float</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this string sequence.
     *
     * @param f a <code>float</code>.
     * @return a reference to this object.
     */
    public Stringer append(float f);

    /**
     * Appends the string representation of the <code>double</code> argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method <code>String.valueOf</code>, and the characters of that string are then
     * appended to this sequence.
     *
     * @param d a <code>double</code>.
     * @return a reference to this object.
     */
    public Stringer append(double d);

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
    public Stringer delete(int start, int end);

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
    public Stringer appendCodePoint(int codePoint);

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
    public Stringer deleteCharAt(int index);

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
    public Stringer replace(int start, int end, String str);

    /**
     * Returns a new <code>String</code> that contains a subsequence of characters currently contained in this character sequence. The
     * substring begins at the specified index and extends to the end of this sequence.
     *
     * @param start The beginning index, inclusive.
     * @return The new string.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is less than zero, or greater than the length of this object.
     */
    public String substring(int start);

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
    public CharSequence subSequence(int start, int end);

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
    public String substring(int start, int end);

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
    public Stringer insert(int index, char str[], int offset, int len);

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
    public Stringer insert(int offset, Object obj);

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
    public Stringer insert(int offset, String str);

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
    public Stringer insert(int offset, char str[]);

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
    public Stringer insert(int dstOffset, CharSequence s);

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
    public Stringer insert(int dstOffset, CharSequence s, int start, int end);

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
    public Stringer insert(int offset, boolean b);

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
    public Stringer insert(int offset, char c);

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
    public Stringer insert(int offset, int i);

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
    public Stringer insert(int offset, long l);

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
    public Stringer insert(int offset, float f);

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
    public Stringer insert(int offset, double d);

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
    public int indexOf(String str);

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
    public int indexOf(String str, int fromIndex);

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
    public int lastIndexOf(String str);

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
    public int lastIndexOf(String str, int fromIndex);

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
    public Stringer reverse();

    /**
     * Returns a string representing the data in this sequence. A new <code>String</code> object is allocated and initialized to contain the
     * character sequence currently represented by this object. This <code>String</code> is then returned. Subsequent changes to this
     * sequence do not affect the contents of the <code>String</code>.
     *
     * @return a string representation of this sequence of characters.
     */
    @Override
    public abstract String toString();
}
