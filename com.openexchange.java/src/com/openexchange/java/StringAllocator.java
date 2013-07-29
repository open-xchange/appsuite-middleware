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

import java.lang.reflect.Constructor;

/**
 * {@link StringAllocator} - A mutable sequence of characters. This class provides an API compatible with <code>StringBuffer</code>, but
 * with no guarantee of synchronization.
 * <p>
 * &nbsp;&nbsp;&nbsp;<b>NOTE: The {@link #toString()} method shares underlying character array!</b>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StringAllocator extends AbstractStringAllocator implements java.io.Serializable {

    private static final long serialVersionUID = -7570175078892044337L;

    private static final Constructor<String> STRING_CONSTRUCTOR;
    static {
        Constructor<String> strConstructor;
        try {
            strConstructor = String.class.getDeclaredConstructor(int.class, int.class, char[].class);
            strConstructor.setAccessible(true);
        } catch (final SecurityException e) {
            strConstructor = null;
        } catch (final NoSuchMethodException e) {
            strConstructor = null;
        }
        STRING_CONSTRUCTOR = strConstructor;
    }

    /**
     * Constructs a string allocator with no characters in it and an initial capacity of 16 characters.
     */
    public StringAllocator() {
        super(16);
    }

    /**
     * Constructs a string allocator with no characters in it and an initial capacity specified by the <code>capacity</code> argument.
     *
     * @param capacity the initial capacity.
     * @throws NegativeArraySizeException if the <code>capacity</code> argument is less than <code>0</code>.
     */
    public StringAllocator(final int capacity) {
        super(capacity);
    }

    /**
     * Constructs a string allocator initialized to the contents of the specified string. The initial capacity of the string allocator is
     * <code>16</code> plus the length of the string argument.
     *
     * @param str the initial contents of the buffer.
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    public StringAllocator(final String str) {
        this(null == str ? "null" : str, 16);
    }

    /** Only internally used */
    private StringAllocator(final String str, final int off) {
        super(str.length() + off);
        append(str);
    }

    /**
     * Constructs a string allocator that contains the same characters as the specified <code>CharSequence</code>. The initial capacity of
     * the string allocator is <code>16</code> plus the length of the <code>CharSequence</code> argument.
     *
     * @param seq the sequence to copy.
     * @throws NullPointerException if <code>seq</code> is <code>null</code>
     */
    public StringAllocator(final CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    /**
     * @see java.lang.String#valueOf(java.lang.Object)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final Object obj) {
        return append(String.valueOf(obj));
    }

    @Override
    public StringAllocator append(final String str) {
        super.append(str);
        return this;
    }

    // Appends the specified string allocator to this sequence.
    private StringAllocator append(final StringAllocator sb) {
        if (sb == null) {
            return append("null");
        }
        final int len = sb.length();
        final int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        sb.getChars(0, len, value, count);
        count = newcount;
        return this;
    }

    /**
     * Appends the specified <tt>StringBuffer</tt> to this sequence.
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
    @Override
    public StringAllocator append(final StringBuffer sb) {
        super.append(sb);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator append(final CharSequence s) {
        if (s == null) {
            return this.append("null");
        }
        if (s instanceof String) {
            return this.append((String) s);
        }
        if (s instanceof StringBuffer) {
            return this.append((StringBuffer) s);
        }
        if (s instanceof StringAllocator) {
            return this.append((StringAllocator) s);
        }
        return this.append(s, 0, s.length());
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator append(final CharSequence s, final int start, final int end) {
        super.append(s, start, end);
        return this;
    }

    @Override
    public StringAllocator append(final char str[]) {
        super.append(str);
        return this;
    }

    @Override
    public StringAllocator append(final char str[], final int offset, final int len) {
        super.append(str, offset, len);
        return this;
    }

    /**
     * @see java.lang.String#valueOf(boolean)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final boolean b) {
        super.append(b);
        return this;
    }

    @Override
    public StringAllocator append(final char c) {
        super.append(c);
        return this;
    }

    /**
     * @see java.lang.String#valueOf(int)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final int i) {
        super.append(i);
        return this;
    }

    /**
     * @see java.lang.String#valueOf(long)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final long lng) {
        super.append(lng);
        return this;
    }

    /**
     * @see java.lang.String#valueOf(float)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final float f) {
        super.append(f);
        return this;
    }

    /**
     * @see java.lang.String#valueOf(double)
     * @see #append(java.lang.String)
     */
    @Override
    public StringAllocator append(final double d) {
        super.append(d);
        return this;
    }

    /**
     * @since 1.5
     */
    @Override
    public StringAllocator appendCodePoint(final int codePoint) {
        super.appendCodePoint(codePoint);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator delete(final int start, final int end) {
        super.delete(start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator deleteCharAt(final int index) {
        super.deleteCharAt(index);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator replace(final int start, final int end, final String str) {
        super.replace(start, end, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator insert(final int index, final char str[], final int offset, final int len) {
        super.insert(index, str, offset, len);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(java.lang.Object)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final String str) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator insert(final int offset, final char str[]) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator insert(final int dstOffset, final CharSequence s) {
        if (s == null) {
            return this.insert(dstOffset, "null");
        }
        if (s instanceof String) {
            return this.insert(dstOffset, (String) s);
        }
        return this.insert(dstOffset, s, 0, s.length());
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringAllocator insert(final int dstOffset, final CharSequence s, final int start, final int end) {
        super.insert(dstOffset, s, start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(boolean)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final boolean b) {
        super.insert(offset, b);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final char c) {
        super.insert(offset, c);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(int)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final int i) {
        return insert(offset, String.valueOf(i));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(long)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final long l) {
        return insert(offset, String.valueOf(l));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(float)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final float f) {
        return insert(offset, String.valueOf(f));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     * @see java.lang.String#valueOf(double)
     * @see #insert(int, java.lang.String)
     * @see #length()
     */
    @Override
    public StringAllocator insert(final int offset, final double d) {
        return insert(offset, String.valueOf(d));
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public int indexOf(final String str) {
        return indexOf(str, 0);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public int indexOf(final String str, final int fromIndex) {
        return AbstractStringAllocator.indexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final String str) {
        return lastIndexOf(str, count);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final String str, final int fromIndex) {
        return AbstractStringAllocator.lastIndexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    @Override
    public StringAllocator reverse() {
        super.reverse();
        return this;
    }

    @Override
    public String substring(final int start) {
        return substring(start, count);
    }

    @Override
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
        final Constructor<String> stringConstructor = STRING_CONSTRUCTOR;
        if (null == stringConstructor) {
            // Create a copy, don't share the array
            return new String(value, start, end - start);
        }
        try {
            return stringConstructor.newInstance(Integer.valueOf(start), Integer.valueOf(end - start), value);
        } catch (final Exception e) {
            // Create a copy, don't share the array
            return new String(value, start, end - start);
        }
    }

    @Override
    public String toString() {
        final Constructor<String> stringConstructor = STRING_CONSTRUCTOR;
        if (null == stringConstructor) {
            // Create a copy, don't share the array
            return new String(value, 0, count);
        }
        try {
            return stringConstructor.newInstance(Integer.valueOf(0), Integer.valueOf(count), value);
        } catch (final Exception e) {
            // Create a copy, don't share the array
            return new String(value, 0, count);
        }
    }

    /**
     * Save the state of the <tt>StringAllocator</tt> instance to a stream (that is, serialize it).
     *
     * @serialData the number of characters currently stored in the string builder (<tt>int</tt>), followed by the characters in the string
     *             allocator (<tt>char[]</tt>). The length of the <tt>char</tt> array may be greater than the number of characters currently
     *             stored in the string allocator, in which case extra characters are ignored.
     */
    private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(count);
        s.writeObject(value);
    }

    /**
     * readObject is called to restore the state of the StringBuffer from a stream.
     */
    private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        count = s.readInt();
        value = (char[]) s.readObject();
    }

}
