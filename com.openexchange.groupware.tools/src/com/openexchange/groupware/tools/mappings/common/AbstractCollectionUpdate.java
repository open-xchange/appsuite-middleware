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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.groupware.tools.mappings.DefaultMapper;

/**
 * {@link AbstractCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractCollectionUpdate<O, E extends Enum<E>> extends AbstractSimpleCollectionUpdate<O> implements CollectionUpdate<O, E> {

    /**
     * Creates an empty collection update.
     *
     * @return A new, empty collection update
     */
    public static <O, E extends Enum<E>> CollectionUpdate<O, E> emptyUpdate() {
        return new CollectionUpdate<O, E>() {

            @Override
            public List<O> getAddedItems() {
                return Collections.emptyList();
            }

            @Override
            public List<O> getRemovedItems() {
                return Collections.emptyList();
            }

            @Override
            public List<ItemUpdate<O, E>> getUpdatedItems() {
                return Collections.emptyList();
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };
    }

    protected final List<ItemUpdate<O, E>> updatedItems;

    /**
     * Initializes a new {@link AbstractCollectionUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     */
    public AbstractCollectionUpdate(DefaultMapper<O, E> mapper, List<O> originalItems, List<O> newItems) {
        this(mapper, originalItems, newItems, true, (E[]) null);
    }

    /**
     * Initializes a new {@link AbstractCollectionUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences between updated items
     */
    public AbstractCollectionUpdate(DefaultMapper<O, E> mapper, List<O> originalItems, List<O> newItems, boolean considerUnset, E[] ignoredFields) {
        super(originalItems, newItems);
        if (null != originalItems && null != newItems) {
            updatedItems = new ArrayList<ItemUpdate<O, E>>();
            for (O newItem : newItems) {
                O originalItem = find(originalItems, newItem);
                if (null != originalItem) {
                    Set<E> differentFields = mapper.getDifferentFields(originalItem, newItem, considerUnset, ignoredFields);
                    if (0 < differentFields.size()) {
                        updatedItems.add(new DefaultItemUpdate<O, E>(originalItem, newItem, differentFields));
                    }
                }
            }
        } else {
            updatedItems = Collections.emptyList();
        }
    }

    @Override
    public List<ItemUpdate<O, E>> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && updatedItems.isEmpty();
    }

    @Override
    public String toString() {
        return "CollectionUpdate [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

}
