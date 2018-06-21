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

package com.openexchange.chronos.common.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.openexchange.chronos.service.SimpleCollectionUpdate;

/**
 * {@link AbstractSimpleCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractSimpleCollectionUpdate<O> implements SimpleCollectionUpdate<O> {

    protected final List<O> removedItems;
    protected final List<O> addedItems;

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
            if (null == newItems || 0 == newItems.size()) {
                addedItems = Collections.emptyList();
            } else {
                addedItems = new ArrayList<O>(newItems);
            }
        } else if (null == newItems || 0 == newItems.size()) {
            removedItems = new ArrayList<O>(originalItems);
            addedItems = Collections.emptyList();
        } else {
            addedItems = new ArrayList<O>();
            removedItems = new ArrayList<O>();
            for (O newItem : newItems) {
                O originalItem = find(originalItems, newItem);
                if (null == originalItem) {
                    addedItems.add(newItem);
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

    @Override
    public int hashCode() {
        return Objects.hash(addedItems, removedItems);
    }

    @Override
    public boolean equals(Object other) {
        if (null != other && AbstractSimpleCollectionUpdate.class.isInstance(other)) {
            AbstractSimpleCollectionUpdate otherAbstractSimpleCollectionUpdate = (AbstractSimpleCollectionUpdate) other;
            return Objects.equals(removedItems, otherAbstractSimpleCollectionUpdate.getRemovedItems()) && Objects.equals(addedItems, otherAbstractSimpleCollectionUpdate.getAddedItems());
        }
        return false;
    }
}
