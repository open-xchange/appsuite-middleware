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

package com.openexchange.sessiond.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link RotatableCopyOnWriteArrayList}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class RotatableCopyOnWriteArrayList<E> extends CopyOnWriteArrayList<E> {

    private static final long serialVersionUID = -1469823809932459073L;

    /** The lock protecting all mutators */
    private final transient ReentrantLock superLock;

    private final transient Method getArrayMethod;
    private final transient Method setArrayMethod;

    /**
     * Initializes a new {@link RotatableCopyOnWriteArrayList}, which is initially empty.
     */
    public RotatableCopyOnWriteArrayList() {
        super();
        try {
            Field lockField = CopyOnWriteArrayList.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            superLock = (ReentrantLock) lockField.get(this);

            Method getArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("getArray", new Class[0]);
            getArrayMethod.setAccessible(true);
            this.getArrayMethod = getArrayMethod;

            Method setArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("setArray", Object[].class);
            setArrayMethod.setAccessible(true);
            this.setArrayMethod = setArrayMethod;
        } catch (Exception e) {
            // Should not occur
            throw new IllegalStateException("Failed initialization", e);
        }
    }

    /**
     * Initializes a new {@link RotatableCopyOnWriteArrayList} containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c The collection of initially held elements
     */
    public RotatableCopyOnWriteArrayList(Collection<? extends E> c) {
        super(c);
        try {
            Field lockField = CopyOnWriteArrayList.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            superLock = (ReentrantLock) lockField.get(this);

            Method getArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("getArray", new Class[0]);
            getArrayMethod.setAccessible(true);
            this.getArrayMethod = getArrayMethod;

            Method setArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("setArray", Object[].class);
            setArrayMethod.setAccessible(true);
            this.setArrayMethod = setArrayMethod;
        } catch (Exception e) {
            // Should not occur
            throw new IllegalStateException("Failed initialization", e);
        }
    }

    /**
     * Initializes a new {@link RotatableCopyOnWriteArrayList} holding a copy of the given array.
     *
     * @param toCopyIn The array (a copy of this array is used as the
     *            internal array)
     */
    public RotatableCopyOnWriteArrayList(E[] toCopyIn) {
        super(toCopyIn);
        try {
            Field lockField = CopyOnWriteArrayList.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            superLock = (ReentrantLock) lockField.get(this);

            Method getArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("getArray", new Class[0]);
            getArrayMethod.setAccessible(true);
            this.getArrayMethod = getArrayMethod;

            Method setArrayMethod = CopyOnWriteArrayList.class.getDeclaredMethod("setArray", Object[].class);
            setArrayMethod.setAccessible(true);
            this.setArrayMethod = setArrayMethod;
        } catch (Exception e) {
            // Should not occur
            throw new IllegalStateException("Failed initialization", e);
        }
    }

    /**
     * Gets the array.
     */
    private final Object[] invokeGetArray() {
        try {
            return (Object[]) getArrayMethod.invoke(this, new Object[0]);
        } catch (Exception e) {
            // Should not occur
            throw new IllegalStateException("Could not get array", e);
        }
    }

    /**
     * Sets the array.
     */
    private final void invokeSetArray(Object[] a) {
        try {
            setArrayMethod.invoke(this, new Object[] { a });
        } catch (Exception e) {
            // Should not occur
            throw new IllegalStateException("Could not set array", e);
        }
    }

    /**
     * Rotates this list:
     * <ul>
     * <li>Removes (& returns) the last element in this list</li>
     * <li>Adds specified new element to the beginning of this list</li>
     * <li>Rotates remaining elements by 1</li>
     * </ul>
     *
     * @param newElement The new element, which is supposed to be set to the beginning of this list
     * @return The removed last element
     * @throw IndexOutOfBoundsException If this list is empty
     */
    public E rotate(E newElement) {
        ReentrantLock lock = this.superLock;
        lock.lock();
        try {
            Object[] elements = invokeGetArray();
            int len = elements.length;
            int lastIndex = len - 1;
            if (lastIndex < 0) {
                // Cannot rotate an empty list
                throw new IndexOutOfBoundsException();
            }

            @SuppressWarnings("unchecked") E oldValue = (E) elements[lastIndex];
            Object[] newElements = new Object[len];
            System.arraycopy(elements, 0, newElements, 1, lastIndex);
            newElements[0] = newElement;
            invokeSetArray(newElements);
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

}
