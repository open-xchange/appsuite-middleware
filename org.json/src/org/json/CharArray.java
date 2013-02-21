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

import java.util.Arrays;

/**
 * {@link CharArray} - Represents a character array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CharArray implements ICharArray {

    protected final char[] chars;
    protected int pos;
    protected Integer hash;

    /**
     * Initializes a new {@link CharArray}.
     */
    protected CharArray() {
        super();
        chars = null;
    }

    /**
     * Initializes a new {@link CharArray}.
     */
    protected CharArray(final int capacity) {
        super();
        chars = new char[capacity];
        pos = 0;
    }

    /**
     * Initializes a new {@link CharArray}.
     */
    private CharArray(final char[] chars) {
        super();
        this.chars = chars;
        pos = 0;
    }

    @Override
    public String toString() {
        try {
            return AbstractJSONValue.directString(0, pos, chars);
        } catch (Exception e) {
            return new String(chars, 0, pos);
        }
    }

    @Override
    public void reset() {
        pos = 0;
    }

    @Override
    public int capacity() {
        return chars.length;
    }

    @Override
    public int remainingCapacity() {
        return chars.length - pos;
    }

    @Override
    public int length() {
        return pos;
    }

    @Override
    public char charAt(final int index) {
        if (index < 0 || index >= pos) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        return chars[index];
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > length()) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException(end - start);
        }
        final char[] chars = new char[end - start];
        System.arraycopy(this.chars, start, chars, 0, chars.length);
        return new CharArray(chars);
    }

    @Override
    public void copyTo(final CharArray other) {
        other.reset();
        System.arraycopy(this.chars, 0, other.chars, 0, pos);
    }

    @Override
    public void copyFrom(final CharArray other) {
        reset();
        final int otherLen = other.pos;
        System.arraycopy(other.chars, 0, chars, 0, otherLen);
        pos += otherLen;
        hash = null;
    }

    @Override
    public int hashCode() {
        Integer hash = this.hash;
        if (null == hash) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(chars);
            result = prime * result + pos;
            hash = Integer.valueOf(result);
            this.hash = hash;
        }
        return hash.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CharArray)) {
            return false;
        }
        CharArray other = (CharArray) obj;
        if (pos != other.pos) {
            return false;
        }
        if (!Arrays.equals(chars, other.chars)) {
            return false;
        }
        return true;
    }


}
