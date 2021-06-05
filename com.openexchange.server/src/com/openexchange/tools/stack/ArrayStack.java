/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    @Override
    public boolean isEmpty() {
        return top == -1;
    }

    @Override
    public void clear() {
        Arrays.fill(arr, null);
        top = -1;
    }

    @Override
    public T top() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return arr[top];
    }

    @Override
    public void pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        arr[top--] = null;
    }

    @Override
    public T topAndPop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        final T retval = arr[top];
        arr[top--] = null;
        return retval;
    }

    @Override
    public void push(final T x) {
        if (top + 1 == arr.length) {
            doubleArray();
        }
        arr[++top] = x;
    }

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
