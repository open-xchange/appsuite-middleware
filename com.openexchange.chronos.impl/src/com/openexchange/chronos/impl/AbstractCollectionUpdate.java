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

package com.openexchange.chronos.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;

/**
 * {@link AbstractCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractCollectionUpdate<O, E extends Enum<E>> implements CollectionUpdate<O, E> {

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

    protected final List<O> removedItems;
    protected final List<O> addedItems;
    protected final List<ItemUpdate<O, E>> updatedItems;

    /**
     * Initializes a new {@link AbstractCollectionUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     * @throws OXException
     */
    public AbstractCollectionUpdate(DefaultMapper<O, E> mapper, List<O> originalItems, List<O> newItems) throws OXException {
        super();
        if (null == originalItems || 0 == originalItems.size()) {
            removedItems = Collections.emptyList();
            updatedItems = Collections.emptyList();
            if (null == newItems || 0 == newItems.size()) {
                addedItems = Collections.emptyList();
            } else {
                addedItems = new ArrayList<O>(newItems);
            }
        } else if (null == newItems || 0 == newItems.size()) {
            removedItems = new ArrayList<O>(originalItems);
            updatedItems = Collections.emptyList();
            addedItems = Collections.emptyList();
        } else {
            addedItems = new ArrayList<O>();
            updatedItems = new ArrayList<ItemUpdate<O, E>>();
            removedItems = new ArrayList<O>();
            for (O newItem : newItems) {
                O originalItem = find(originalItems, newItem);
                if (null == originalItem) {
                    addedItems.add(newItem);
                } else {
                    O deltaItem = mapper.getDifferences(originalItem, newItem);
                    E[] updatedFields = mapper.getAssignedFields(deltaItem);
                    if (0 < updatedFields.length) {
                        updatedItems.add(new DefaultItemUpdate<O, E>(mapper, originalItem, newItem));
                    } else {
                        // not changed
                    }
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

    @Override
    public List<ItemUpdate<O, E>> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public boolean isEmpty() {
        return 0 == addedItems.size() && 0 == removedItems.size() && 0 == updatedItems.size();
    }

    @Override
    public String toString() {
        return "CollectionUpdate [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

    private O find(Collection<O> items, O item) {
        for (O o : items) {
            if (matches(item, o)) {
                return o;
            }
        }
        return null;
    }

}
