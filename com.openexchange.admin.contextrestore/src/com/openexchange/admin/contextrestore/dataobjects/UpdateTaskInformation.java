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

package com.openexchange.admin.contextrestore.dataobjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link UpdateTaskInformation} - Update task information.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskInformation {

    private final List<UpdateTaskEntry> entries;

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation() {
        super();
        entries = new LinkedList<UpdateTaskEntry>();
    }

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation(final int capacity) {
        super();
        entries = new ArrayList<UpdateTaskEntry>(capacity);
    }

    /**
     * Gets the size of the update task collection.
     * 
     * @return The size
     */
    public int size() {
        return entries.size();
    }

    /**
     * Checks whether the update task collection is empty.
     * 
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Checks if the update task collection contains specified element.
     * 
     * @param e The element possibly contained
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(final UpdateTaskEntry e) {
        return entries.contains(e);
    }

    /**
     * Gets an unmodifiable {@link Iterator} for the update task collection.
     * 
     * @return The iterator
     */
    public Iterator<UpdateTaskEntry> iterator() {
        return new UnmodifiableIterator<UpdateTaskEntry>(entries.iterator());
    }

    /**
     * Adds specified element.
     * 
     * @param e The element to add
     */
    public void add(final UpdateTaskEntry e) {
        entries.add(e);
    }

    /**
     * Removes specified element.
     * 
     * @param e The element to remove
     */
    public void remove(final UpdateTaskEntry e) {
        entries.remove(e);
    }

    /**
     * Clears the update task collection.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Gets the element at specified index.
     * 
     * @param index The index
     * @return The element.
     */
    public UpdateTaskEntry get(final int index) {
        return entries.get(index);
    }

    /**
     * Adds element at given index position.
     * 
     * @param index The index
     * @param element The element
     */
    public void add(final int index, final UpdateTaskEntry element) {
        entries.add(index, element);
    }

    /**
     * Removes element from given index position.
     * 
     * @param index The index
     * @return The removed element
     */
    public UpdateTaskEntry remove(final int index) {
        return entries.remove(index);
    }

    private static final class UnmodifiableIterator<E> implements Iterator<E> {

        private final Iterator<E> it;

        /** Constructor. */
        protected UnmodifiableIterator(final Iterator<E> it) {
            super();
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public E next() {
            return it.next();
        }

        /**
         * Guaranteed to throw an exception and leave the underlying data unmodified.
         * 
         * @throws UnsupportedOperationException always
         */
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
