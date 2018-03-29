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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.sessiond.impl;

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

            E oldValue = (E) elements[lastIndex];
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
