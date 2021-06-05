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

package com.openexchange.chronos.common.mapping;

import java.util.Collections;
import java.util.List;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;

/**
 * {@link DefaultCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCollectionUpdate<O, E extends Enum<E>> implements CollectionUpdate<O, E> {

    protected final List<O> addedItems;
    protected final List<O> removedItems;
    protected final List<ItemUpdate<O, E>> updatedItems;

    /**
     * Initializes a new {@link DefaultCollectionUpdate}.
     *
     * @param addedItems The list of added items
     * @param removedItems The list of removed items
     * @param updatedItems The list of updated items
     */
    public DefaultCollectionUpdate(List<O> addedItems, List<O> removedItems, List<ItemUpdate<O, E>> updatedItems) {
        super();
        this.addedItems = null != addedItems ? addedItems : Collections.<O> emptyList();
        this.removedItems = null != removedItems ? removedItems : Collections.<O> emptyList();
        this.updatedItems = null != updatedItems ? updatedItems : Collections.<ItemUpdate<O, E>> emptyList();
    }

    @Override
    public List<O> getAddedItems() {
        return addedItems;
    }

    @Override
    public List<O> getRemovedItems() {
        return removedItems;
    }

    @Override
    public List<ItemUpdate<O, E>> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public boolean isEmpty() {
        return addedItems.isEmpty() && removedItems.isEmpty() && updatedItems.isEmpty();
    }

    @Override
    public String toString() {
        return "CollectionUpdate [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

}
