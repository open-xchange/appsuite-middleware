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

import java.util.EmptyStackException;

/**
 * Stack
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Stack<T> {

    /**
     * Inserts a new item into the stack.
     *
     * @param x - the item to insert.
     */
    void push(T x);

    /**
     * Removes the most recently inserted item from the stack.
     *
     * @exception EmptyStackException - if the stack is empty.
     */
    void pop();

    /**
     * Peeks the most recently inserted item in the stack.
     *
     * @return the most recently inserted item in the stack.
     * @exception EmptyStackException - if the stack is empty.
     */
    T top();

    /**
     * Returns and removes the most recently inserted item from the stack.
     *
     * @return the most recently inserted item in the stack.
     * @exception EmptyStackException - if the stack is empty.
     */
    T topAndPop();

    /**
     * Tests if the stack is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Clears the stack
     */
    void clear();

    /**
     * Determines the stack's size
     *
     * @return stack's size
     */
    int size();
}
