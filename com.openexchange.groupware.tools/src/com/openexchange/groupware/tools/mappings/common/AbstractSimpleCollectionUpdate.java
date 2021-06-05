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

package com.openexchange.groupware.tools.mappings.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link AbstractSimpleCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractSimpleCollectionUpdate<O> implements SimpleCollectionUpdate<O> {

    protected final List<O> removedItems;
    protected final List<O> addedItems;
    protected final List<O> retainedItems;

    /**
     * Initializes a new {@link AbstractSimpleCollectionUpdate}.
     *
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     */
    public AbstractSimpleCollectionUpdate(Collection<O> originalItems, Collection<O> newItems) {
        super();
        if (null == originalItems || 0 == originalItems.size()) {
            removedItems = Collections.emptyList();
            retainedItems = Collections.emptyList();
            if (null == newItems || 0 == newItems.size()) {
                addedItems = Collections.emptyList();
            } else {
                addedItems = new ArrayList<O>(newItems);
            }
        } else if (null == newItems || 0 == newItems.size()) {
            removedItems = new ArrayList<O>(originalItems);
            addedItems = Collections.emptyList();
            retainedItems = Collections.emptyList();
        } else {
            addedItems = new ArrayList<O>();
            removedItems = new ArrayList<O>();
            retainedItems = new ArrayList<O>();
            for (O newItem : newItems) {
                O originalItem = find(originalItems, newItem);
                if (null == originalItem) {
                    addedItems.add(newItem);
                } else {
                    retainedItems.add(originalItem);
                }
            }
            for (O originalItem : originalItems) {
                O newItem = find(newItems, originalItem);
                if (null == newItem) {
                    removedItems.add(originalItem);
                }
            }
        }
    }

    /**
     * Gets a value indicating whether a specific item <i>matches</i> another one, i.e. they denote the same underlying resource.
     *
     * @param item1 The first item to check
     * @param item2 The second item to check
     * @return <code>true</code> if the items match, <code>false</code>, otherwise
     */
    protected abstract boolean matches(O item1, O item2);

    @Override
    public List<O> getAddedItems() {
        return addedItems;
    }

    @Override
    public List<O> getRemovedItems() {
        return removedItems;
    }

    /**
     * Gets the list of retained items, i.e. those items that are present in both collections.
     *
     * @return The retained items, or an empty list if there are none
     */
    public List<O> getRetainedItems() {
        return retainedItems;
    }

    @Override
    public boolean isEmpty() {
        return 0 == addedItems.size() && 0 == removedItems.size();
    }

    /**
     * Searches a collection for a specific item, utilizing the {@link #matches(Object, Object)} method.
     *
     * @param items The items to search
     * @param item The item to lookup
     * @return The matching item, or <code>null</code> if no matching item was found
     */
    protected O find(Collection<O> items, O item) {
        if (null != items) {
            for (O o : items) {
                if (matches(item, o)) {
                    return o;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "SimpleCollectionUpdate [" + removedItems.size() + " removed, " + addedItems.size() + " added]";
    }

}
