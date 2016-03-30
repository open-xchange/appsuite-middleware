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

package com.openexchange.groupware.results;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link CollectionDelta} - The collection delta.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 * @param <T> The type
 */
public class CollectionDelta<T> {

	/**
	 * The visitor for collection delta elements.
	 */
	public interface DeltaVisitor<T> {

		void newOrModified(T thing);

		void deleted(T thing);
	}

	// ---------------------------------------------------------------------------------------------------

	private final List<T> newAndModified;
	private final List<T> deleted;

    /**
     * Initializes a new {@link CollectionDelta}.
     */
    public CollectionDelta() {
        super();
        newAndModified = new LinkedList<T>();
        deleted = new LinkedList<T>();
    }

	/**
     * Initializes a new {@link CollectionDelta}.
     *
     * @param newAndModified
     * @param deleted
     */
    public CollectionDelta(List<T> newAndModified, List<T> deleted) {
        super();
        this.newAndModified = newAndModified;
        this.deleted = deleted;
    }

    /**
     * Adds the item the new-and-modified list.
     *
     * @param thing The item to add
     * @return This collection delta
     */
    public CollectionDelta<T> addNewOrModified(T thing) {
		newAndModified.add(thing);
		return this;
	}

    /**
     * Adds the item the deleted list.
     *
     * @param thing The item to add
     * @return This collection delta
     */
	public CollectionDelta<T> addDeleted(T thing) {
		deleted.add(thing);
        return this;
	}

	/**
	 * Gets the new-and-modified list
	 *
	 * @return The new-and-modified list
	 */
	public List<T> getNewAndModified() {
		return newAndModified;
	}

	/**
     * Gets the deleted list
     *
     * @return The deleted list
     */
	public List<T> getDeleted() {
		return deleted;
	}

	/**
	 * Traverses this collection's elements using given visitor instance
	 *
	 * @param visitor The visitor
	 */
	public void visitAll(DeltaVisitor<T> visitor) {
		for(T thing : newAndModified) {
			visitor.newOrModified(thing);
		}

		for(T thing : deleted) {
			visitor.deleted(thing);
		}
	}

}
