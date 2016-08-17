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
 * {@link StringBuilderStringer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StringBuilderStringer implements Stringer {

    private static final long serialVersionUID = -5617579874001869121L;

    private final StringBuilder sb;

    /**
     * Initializes a new {@link StringBuilderStringer}.
     */
    public StringBuilderStringer(final StringBuilder sb) {
        super();
        this.sb = sb;
    }

    @Override
    public boolean isEmpty() {
        int length = sb.length();
        boolean empty = true;
        for (int i = length; empty && i-- > 0;) {
            empty = Strings.isWhitespace(sb.charAt(i));
        }
        return empty;
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public int capacity() {
        return sb.capacity();
    }

    @Override
    public void ensureCapacity(int minimumCapacity) {
        sb.ensureCapacity(minimumCapacity);
    }

    @Override
    public void trimToSize() {
        sb.trimToSize();
    }

    @Override
    public void setLength(int newLength) {
        sb.setLength(newLength);
    }

    @Override
    public Stringer append(Object obj) {
        sb.append(obj);
        return this;
    }

    @Override
    public Stringer append(String str) {
        sb.append(str);
        return this;
    }

    @Override
    public Stringer append(StringBuilder sb) {
        this.sb.append(sb);
        return this;
    }

    @Override
    public Stringer append(StringBuffer sb) {
        this.sb.append(sb);
        return this;
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public Stringer append(CharSequence s) {
        sb.append(s);
        return this;
    }

    @Override
    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    @Override
    public Stringer append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    @Override
    public Stringer append(char[] str) {
        sb.append(str);
        return this;
    }

    @Override
    public Stringer append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    @Override
    public Stringer append(boolean b) {
        sb.append(b);
        return this;
    }

    @Override
    public Stringer append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public Stringer append(int i) {
        sb.append(i);
        return this;
    }

    @Override
    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    @Override
    public Stringer append(long lng) {
        sb.append(lng);
        return this;
    }

    @Override
    public Stringer append(float f) {
        sb.append(f);
        return this;
    }

    @Override
    public Stringer append(double d) {
        sb.append(d);
        return this;
    }

    @Override
    public Stringer appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    @Override
    public Stringer delete(int start, int end) {
        sb.delete(start, end);
        return this;
    }

    @Override
    public Stringer deleteCharAt(int index) {
        sb.deleteCharAt(index);
        return this;
    }

    @Override
    public Stringer replace(int start, int end, String str) {
        sb.replace(start, end, str);
        return this;
    }

    @Override
    public int codePointCount(int beginIndex, int endIndex) {
        return sb.codePointCount(beginIndex, endIndex);
    }

    @Override
    public Stringer insert(int index, char[] str, int offset, int len) {
        sb.insert(index, str, offset, len);
        return this;
    }

    @Override
    public Stringer insert(int offset, Object obj) {
        sb.insert(offset, obj);
        return this;
    }

    @Override
    public Stringer insert(int offset, String str) {
        sb.insert(offset, str);
        return this;
    }

    @Override
    public Stringer insert(int offset, char[] str) {
        sb.insert(offset, str);
        return this;
    }

    @Override
    public Stringer insert(int dstOffset, CharSequence s) {
        sb.insert(dstOffset, s);
        return this;
    }

    @Override
    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public Stringer insert(int dstOffset, CharSequence s, int start, int end) {
        sb.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public Stringer insert(int offset, boolean b) {
        sb.insert(offset, b);
        return this;
    }

    @Override
    public Stringer insert(int offset, char c) {
        sb.insert(offset, c);
        return this;
    }

    @Override
    public Stringer insert(int offset, int i) {
        sb.insert(offset, i);
        return this;
    }

    @Override
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        sb.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public Stringer insert(int offset, long l) {
        sb.insert(offset, l);
        return this;
    }

    @Override
    public Stringer insert(int offset, float f) {
        sb.insert(offset, f);
        return this;
    }

    @Override
    public Stringer insert(int offset, double d) {
        sb.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    @Override
    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    @Override
    public Stringer reverse() {
        sb.reverse();
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void setCharAt(int index, char ch) {
        sb.setCharAt(index, ch);
    }

    @Override
    public String substring(int start) {
        return sb.substring(start);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    @Override
    public String substring(int start, int end) {
        return sb.substring(start, end);
    }

}
