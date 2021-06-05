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
