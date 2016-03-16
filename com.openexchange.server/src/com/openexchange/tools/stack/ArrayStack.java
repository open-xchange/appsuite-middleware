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

package com.openexchange.tools.stack;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * ArrayStack - an array-based stack
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ArrayStack<T> implements Stack<T> {

    private T[] arr;

    private int top;

    private final Class<T> clazz;

    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Construct the stack with default capacity of 10
     *
     * @param clazz - the class of the objects kept in this stack
     */
    public ArrayStack(final Class<T> clazz) {
        this(clazz, DEFAULT_CAPACITY);
    }

    /**
     * Construct the stack.
     *
     * @param clazz - the class of the objects kept in this stack
     * @param capacity - the initial capacity
     */
    @SuppressWarnings("unchecked")
    public ArrayStack(final Class<T> clazz, final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Stack capacity must be greater than 0");
        }
        this.clazz = clazz;
        arr = (T[]) Array.newInstance(clazz, capacity);
        top = -1;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return top == -1;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#makeEmpty()
     */
    @Override
    public void clear() {
        Arrays.fill(arr, null);
        top = -1;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#top()
     */
    @Override
    public T top() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return arr[top];
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#pop()
     */
    @Override
    public void pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        arr[top--] = null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#topAndPop()
     */
    @Override
    public T topAndPop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        final T retval = arr[top];
        arr[top--] = null;
        return retval;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#push(java.lang.Object)
     */
    @Override
    public void push(final T x) {
        if (top + 1 == arr.length) {
            doubleArray();
        }
        arr[++top] = x;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.stack.Stack#size()
     */
    @Override
    public int size() {
        return top + 1;
    }

    /**
     * Extend the array.
     */
    @SuppressWarnings("unchecked")
    private void doubleArray() {
        final T[] newArr = (T[]) Array.newInstance(clazz, arr.length << 1);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        arr = newArr;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder tmp = new StringBuilder(1024);
        tmp.append('[');
        if (arr[0] != null) {
            tmp.append(arr[0].toString());
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] == null) {
                    break;
                }
                tmp.append(',').append(arr[i].toString());
            }
        }
        tmp.append(']');
        return tmp.toString();
    }

}
